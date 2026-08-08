import { useEffect, useMemo, useState } from 'react';
import { useParams, useSearchParams } from 'react-router-dom';
import BackLink from '../../components/common/BackLink.jsx';
import { sampleProducts } from '../../data/sampleData.js';
import {
  filterProducts,
  getProducts,
  getProductsByCategory,
  searchProducts,
} from '../../services/productService.js';
import CategoryMenu from '../home/components/CategoryMenu.jsx';
import ProductCard from '../home/components/ProductCard.jsx';

const pageSize = 10;

function ProductListPage() {
  const { categoryId } = useParams();
  const [searchParams, setSearchParams] = useSearchParams();
  const [products, setProducts] = useState([]);
  const [pageInfo, setPageInfo] = useState({ number: 0, totalPages: 1, totalElements: 0 });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [keyword, setKeyword] = useState(searchParams.get('keyword') || '');
  const [minPrice, setMinPrice] = useState(searchParams.get('minPrice') || '');
  const [maxPrice, setMaxPrice] = useState(searchParams.get('maxPrice') || '');
  const [sort, setSort] = useState(searchParams.get('sort') || 'createdAt,desc');

  const page = Number(searchParams.get('page') || 0);
  const shouldShowCategoryMenu = searchParams.get('view') === 'categories' || Boolean(categoryId);

  const activeFilters = useMemo(() => ({
    page,
    size: pageSize,
    sort: searchParams.get('sort') || 'createdAt,desc',
    keyword: searchParams.get('keyword') || '',
    minPrice: searchParams.get('minPrice') || '',
    maxPrice: searchParams.get('maxPrice') || '',
  }), [page, searchParams]);

  useEffect(() => {
    setKeyword(searchParams.get('keyword') || '');
    setMinPrice(searchParams.get('minPrice') || '');
    setMaxPrice(searchParams.get('maxPrice') || '');
    setSort(searchParams.get('sort') || 'createdAt,desc');
  }, [searchParams]);

  useEffect(() => {
    const timerId = window.setTimeout(() => {
      const nextKeyword = keyword.trim();
      const currentKeyword = searchParams.get('keyword') || '';

      if (nextKeyword === currentKeyword) {
        return;
      }

      const nextParams = Object.fromEntries(searchParams.entries());

      if (nextKeyword) {
        nextParams.keyword = nextKeyword;
      } else {
        delete nextParams.keyword;
      }

      nextParams.page = '0';
      nextParams.sort = sort || 'createdAt,desc';
      if (shouldShowCategoryMenu) {
        nextParams.view = 'categories';
      }

      setSearchParams(nextParams);
    }, 360);

    return () => window.clearTimeout(timerId);
  }, [keyword]);

  useEffect(() => {
    let active = true;
    setLoading(true);
    setError('');

    const requestParams = {
      page: activeFilters.page,
      size: activeFilters.size,
      sort: activeFilters.sort,
    };

    const request = buildProductRequest(categoryId, activeFilters, requestParams);

    request()
      .then((result) => {
        if (!active) {
          return;
        }

        setProducts(result.content || []);
        setPageInfo({
          number: result.number || 0,
          totalPages: result.totalPages || 1,
          totalElements: result.totalElements || result.content?.length || 0,
        });
      })
      .catch(() => {
        if (!active) {
          return;
        }

        const filteredSample = sampleProducts.filter((product) => {
          if (categoryId && String(product.categoryId || product.category?.id) !== String(categoryId)) {
            return false;
          }
          if (activeFilters.keyword && activeFilters.keyword.trim()) {
            const kw = activeFilters.keyword.trim().toLowerCase();
            const name = String(product.name || product.productName || '').toLowerCase();
            const desc = String(product.description || '').toLowerCase();
            if (!name.includes(kw) && !desc.includes(kw)) {
              return false;
            }
          }
          const price = Number(product.basePrice || product.price || product.salePrice || 0);
          if (activeFilters.minPrice !== '' && activeFilters.minPrice !== null && activeFilters.minPrice !== undefined) {
            if (price < Number(activeFilters.minPrice)) return false;
          }
          if (activeFilters.maxPrice !== '' && activeFilters.maxPrice !== null && activeFilters.maxPrice !== undefined) {
            if (price > Number(activeFilters.maxPrice)) return false;
          }
          return true;
        });

        setProducts(filteredSample);
        setPageInfo({ number: 0, totalPages: 1, totalElements: filteredSample.length });
        setError('Không lấy được dữ liệu từ backend, đang hiển thị dữ liệu mẫu.');
      })
      .finally(() => {
        if (active) {
          setLoading(false);
        }
      });

    return () => {
      active = false;
    };
  }, [categoryId, activeFilters]);

  function applyFilters(event) {
    event.preventDefault();
    const nextParams = {};

    if (keyword.trim()) {
      nextParams.keyword = keyword.trim();
    }
    if (minPrice !== '') {
      nextParams.minPrice = minPrice;
    }
    if (maxPrice !== '') {
      nextParams.maxPrice = maxPrice;
    }
    if (sort) {
      nextParams.sort = sort;
    }
    if (shouldShowCategoryMenu) {
      nextParams.view = 'categories';
    }
    nextParams.page = '0';

    setSearchParams(nextParams);
  }

  function clearFilters() {
    setKeyword('');
    setMinPrice('');
    setMaxPrice('');
    setSort('createdAt,desc');
    setSearchParams({ page: '0', sort: 'createdAt,desc' });
  }

  function changePage(nextPage) {
    const nextParams = Object.fromEntries(searchParams.entries());
    nextParams.page = String(nextPage);
    nextParams.sort = sort;
    setSearchParams(nextParams);
  }

  return (
    <div className={shouldShowCategoryMenu ? 'product-list-page container' : 'product-list-page product-list-page--full container'}>
      {shouldShowCategoryMenu && (
        <aside>
          <CategoryMenu />
        </aside>
      )}

      <section className="product-list-page__content">
        <BackLink fallback="/" label="Quay lại trang chủ" />

        <div className="product-list-header">
          <div>
            <p>{categoryId ? `Danh mục #${categoryId}` : shouldShowCategoryMenu ? 'Chọn danh mục sản phẩm' : 'Tất cả sản phẩm'}</p>
            <h1>{shouldShowCategoryMenu ? 'Danh mục đồ chơi' : 'Tất cả đồ chơi cho bé'}</h1>
          </div>
          <span>{pageInfo.totalElements} sản phẩm</span>
        </div>

        <form className="product-filter" onSubmit={applyFilters}>
          <input
            value={keyword}
            onChange={(event) => setKeyword(event.target.value)}
            placeholder="Tìm theo tên sản phẩm"
          />
          <input
            value={minPrice}
            onChange={(event) => setMinPrice(event.target.value)}
            type="number"
            min="0"
            placeholder="Giá từ"
          />
          <input
            value={maxPrice}
            onChange={(event) => setMaxPrice(event.target.value)}
            type="number"
            min="0"
            placeholder="Giá đến"
          />
          <select value={sort} onChange={(event) => setSort(event.target.value)}>
            <option value="createdAt,desc">Mới nhất</option>
            <option value="basePrice,asc">Giá thấp đến cao</option>
            <option value="basePrice,desc">Giá cao đến thấp</option>
            <option value="averageRating,desc">Đánh giá cao</option>
          </select>
          <button type="submit">Lọc</button>
          <button type="button" onClick={clearFilters}>Xóa</button>
        </form>

        {error && <div className="form-alert form-alert--soft">{error}</div>}
        {loading ? (
          <div className="product-grid product-grid--loading">
            {Array.from({ length: 10 }).map((_, index) => (
              <div className="product-card-skeleton" key={index} />
            ))}
          </div>
        ) : (
          <>
            <div className="product-grid">
              {products.map((product) => (
                <ProductCard key={product.id} product={product} />
              ))}
            </div>

            {products.length === 0 && (
              <div className="empty-state">
                Không tìm thấy sản phẩm phù hợp.
              </div>
            )}
          </>
        )}

        <div className="pagination-bar">
          <button type="button" disabled={pageInfo.number <= 0} onClick={() => changePage(pageInfo.number - 1)}>
            Trước
          </button>
          <span>Trang {pageInfo.number + 1} / {pageInfo.totalPages}</span>
          <button
            type="button"
            disabled={pageInfo.number + 1 >= pageInfo.totalPages}
            onClick={() => changePage(pageInfo.number + 1)}
          >
            Sau
          </button>
        </div>
      </section>
    </div>
  );
}

function buildProductRequest(categoryId, filters, requestParams) {
  const queryParams = {
    ...requestParams,
    status: 'ACTIVE',
  };

  if (categoryId) {
    queryParams.categoryId = categoryId;
  }
  if (filters.keyword && filters.keyword.trim()) {
    queryParams.keyword = filters.keyword.trim();
  }
  if (filters.minPrice !== '' && filters.minPrice !== null && filters.minPrice !== undefined) {
    queryParams.minPrice = filters.minPrice;
  }
  if (filters.maxPrice !== '' && filters.maxPrice !== null && filters.maxPrice !== undefined) {
    queryParams.maxPrice = filters.maxPrice;
  }

  return () => filterProducts(queryParams);
}

export default ProductListPage;
