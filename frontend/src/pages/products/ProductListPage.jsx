import { useEffect, useMemo, useState } from 'react';
import { useParams, useSearchParams } from 'react-router-dom';
import BackLink from '../../components/common/BackLink.jsx';
import CategoryMenu from '../home/components/CategoryMenu.jsx';
import ProductCard from '../home/components/ProductCard.jsx';
import { sampleProducts } from '../../data/sampleData.js';
import {
  filterProducts,
  getProducts,
  getProductsByCategory,
  searchProducts,
} from '../../services/productService.js';

const pageSize = 8;

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

  const activeFilters = useMemo(() => ({
    page,
    size: pageSize,
    sort,
    keyword: searchParams.get('keyword') || '',
    minPrice: searchParams.get('minPrice') || '',
    maxPrice: searchParams.get('maxPrice') || '',
  }), [page, searchParams, sort]);

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

        setProducts(sampleProducts);
        setPageInfo({ number: 0, totalPages: 1, totalElements: sampleProducts.length });
        setError('Backend chua san sang, dang hien thi du lieu mau.');
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
    if (minPrice) {
      nextParams.minPrice = minPrice;
    }
    if (maxPrice) {
      nextParams.maxPrice = maxPrice;
    }
    if (sort) {
      nextParams.sort = sort;
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
    <div className="product-list-page container">
      <aside>
        <CategoryMenu />
      </aside>

      <section className="product-list-page__content">
        <BackLink fallback="/" label="Quay lai trang chu" />

        <div className="product-list-header">
          <div>
            <p>{categoryId ? `Danh muc #${categoryId}` : 'Tat ca san pham'}</p>
            <h1>Do choi cho be</h1>
          </div>
          <span>{pageInfo.totalElements} san pham</span>
        </div>

        <form className="product-filter" onSubmit={applyFilters}>
          <input
            value={keyword}
            onChange={(event) => setKeyword(event.target.value)}
            placeholder="Tim theo ten san pham"
          />
          <input
            value={minPrice}
            onChange={(event) => setMinPrice(event.target.value)}
            type="number"
            min="0"
            placeholder="Gia tu"
          />
          <input
            value={maxPrice}
            onChange={(event) => setMaxPrice(event.target.value)}
            type="number"
            min="0"
            placeholder="Gia den"
          />
          <select value={sort} onChange={(event) => setSort(event.target.value)}>
            <option value="createdAt,desc">Moi nhat</option>
            <option value="basePrice,asc">Gia thap den cao</option>
            <option value="basePrice,desc">Gia cao den thap</option>
            <option value="averageRating,desc">Danh gia cao</option>
          </select>
          <button type="submit">Loc</button>
          <button type="button" onClick={clearFilters}>Xoa</button>
        </form>

        {error && <div className="form-alert form-alert--soft">{error}</div>}
        {loading ? (
          <div className="product-grid product-grid--loading">
            {Array.from({ length: 8 }).map((_, index) => (
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
                Khong tim thay san pham phu hop.
              </div>
            )}
          </>
        )}

        <div className="pagination-bar">
          <button type="button" disabled={pageInfo.number <= 0} onClick={() => changePage(pageInfo.number - 1)}>
            Truoc
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
  if (categoryId) {
    return () => getProductsByCategory(categoryId, requestParams);
  }

  if (filters.keyword) {
    return () => searchProducts(filters.keyword, requestParams);
  }

  if (filters.minPrice || filters.maxPrice) {
    return () => filterProducts({
      ...requestParams,
      minPrice: filters.minPrice,
      maxPrice: filters.maxPrice,
      status: 'ACTIVE',
    });
  }

  return () => getProducts(requestParams);
}

export default ProductListPage;
