import { useEffect, useRef, useState } from 'react';
import { useOutletContext, useNavigate, useSearchParams } from 'react-router-dom';
import { getAdminCategories, getAdminCategoryTree } from '../../services/adminCategoryService.js';
import {
  addProductImage,
  deleteAdminProduct,
  getAdminProducts,
  removeProductImage,
  setProductThumbnail,
  toggleProductFeatured,
  updateAdminProduct,
} from '../../services/adminProductService.js';
import { uploadImage } from '../../services/uploadService.js';
import { getSlowSellingProducts, getTopSellingProducts } from '../../services/statisticsService.js';
import { formatPrice } from '../../utils/formatters.js';

/* Helper function to get product status badge info strictly matching Backend ProductStatus enum */
function getProductStatusInfo(product) {
  if (!product) {
    return { code: 'ACTIVE', label: 'Đang kinh doanh', bg: '#f0fdf4', color: '#16a34a', border: '#bbf7d0' };
  }

  const rawStatus = typeof product.status === 'object' ? (product.status?.name || product.status?.code || '') : product.status;
  const statusStr = String(rawStatus || '').toUpperCase();
  const displayName = String(product.statusDetail?.displayName || product.status?.displayName || '').trim();

  if (
    statusStr === 'ACTIVE' ||
    displayName === 'Đang kinh doanh' ||
    displayName.toLowerCase().includes('đang kinh doanh') ||
    displayName.toLowerCase().includes('đang bán')
  ) {
    return {
      code: 'ACTIVE',
      label: 'Đang kinh doanh',
      bg: '#f0fdf4',
      color: '#16a34a',
      border: '#bbf7d0',
    };
  }

  if (
    statusStr === 'OUT_OF_STOCK' ||
    displayName === 'Hết hàng' ||
    displayName.toLowerCase().includes('hết hàng')
  ) {
    return {
      code: 'OUT_OF_STOCK',
      label: 'Hết hàng',
      bg: '#fff7ed',
      color: '#ea580c',
      border: '#fed7aa',
    };
  }

  return {
    code: 'INACTIVE',
    label: displayName || 'Ngừng kinh doanh',
    bg: '#fef2f2',
    color: '#dc2626',
    border: '#fecaca',
  };
}

/* ═══════════════════════════════════════════════════════════════════
   CATEGORY TREE DROPDOWN COMPONENT (CHO BỘ LỌC TÌM KIẾM)
   - Tên danh mục bên trái, nút mũi tên ▶ / ▼ chuyển sang HẾT BÊN PHẢI
   - Bấm nút mũi tên ▼ bên phải để mở rộng danh mục con
   - Bấm tên danh mục để chọn ngay
   ═══════════════════════════════════════════════════════════════════ */
function CategoryTreeSelect({ categoryTree, selectedId, onSelect }) {
  const [isOpen, setIsOpen] = useState(false);
  const [expandedIds, setExpandedIds] = useState([]);
  const dropdownRef = useRef(null);

  useEffect(() => {
    function handleClickOutside(e) {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target)) {
        setIsOpen(false);
      }
    }
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const toggleExpand = (e, id) => {
    e.stopPropagation();
    setExpandedIds((prev) =>
      prev.includes(id) ? prev.filter((item) => item !== id) : [...prev, id]
    );
  };

  const findCatName = (nodes, id) => {
    for (const n of nodes) {
      if (String(n.id) === String(id)) return n.name;
      const sub = n.subCategories || n.children || [];
      if (sub.length) {
        const found = findCatName(sub, id);
        if (found) return found;
      }
    }
    return null;
  };

  const selectedLabel = selectedId ? findCatName(categoryTree, selectedId) || `Danh mục #${selectedId}` : 'Tất cả danh mục';

  const renderTreeNodes = (nodes, level = 0) => {
    return nodes.map((node) => {
      const subList = node.subCategories || node.children || [];
      const hasChildren = subList.length > 0;
      const isExpanded = expandedIds.includes(node.id);
      const isSelected = String(selectedId) === String(node.id);

      return (
        <div key={node.id} style={{ userSelect: 'none' }}>
          <div
            style={{
              display: 'flex',
              alignItems: 'center',
              justify: 'space-between',
              padding: '9px 12px',
              paddingLeft: `${14 + level * 16}px`,
              background: isSelected ? '#eff6ff' : 'transparent',
              color: isSelected ? '#2563eb' : '#1e293b',
              fontWeight: isSelected ? '800' : '600',
              cursor: 'pointer',
              borderRadius: '8px',
              fontSize: '13px',
              transition: 'background 0.15s ease',
            }}
            onClick={() => {
              onSelect(node.id);
              setIsOpen(false);
            }}
            onMouseEnter={(e) => { if (!isSelected) e.currentTarget.style.background = '#f8fafc'; }}
            onMouseLeave={(e) => { if (!isSelected) e.currentTarget.style.background = 'transparent'; }}
          >
            {/* Category Name on the Left */}
            <span style={{ flex: 1, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis', paddingRight: '8px' }}>
              {node.name}
            </span>

            {/* Toggle Arrow ▶ / ▼ moved to the RIGHT side */}
            {hasChildren && (
              <span
                onClick={(e) => toggleExpand(e, node.id)}
                title={isExpanded ? 'Thu gọn' : 'Mở rộng danh mục con'}
                style={{
                  fontSize: '9px',
                  color: '#475569',
                  width: '20px',
                  height: '20px',
                  display: 'inline-flex',
                  alignItems: 'center',
                  justify: 'center',
                  borderRadius: '6px',
                  background: '#e2e8f0',
                  cursor: 'pointer',
                  fontWeight: '900',
                  flexShrink: 0,
                }}
              >
                {isExpanded ? '▼' : '▶'}
              </span>
            )}
          </div>

          {/* Render Subcategories */}
          {hasChildren && isExpanded && (
            <div>{renderTreeNodes(subList, level + 1)}</div>
          )}
        </div>
      );
    });
  };

  return (
    <div ref={dropdownRef} style={{ position: 'relative', width: '100%' }}>
      <div
        onClick={() => setIsOpen((prev) => !prev)}
        style={{
          width: '100%',
          padding: '10px 14px',
          border: '1px solid #cbd5e1',
          borderRadius: '12px',
          fontSize: '13px',
          background: '#fff',
          cursor: 'pointer',
          display: 'flex',
          justify: 'space-between',
          alignItems: 'center',
          fontWeight: '700',
          color: selectedId ? '#0f172a' : '#64748b',
        }}
      >
        <span style={{ whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>{selectedLabel}</span>
        <span style={{ fontSize: '10px', color: '#64748b', marginLeft: '6px' }}>{isOpen ? '▲' : '▼'}</span>
      </div>

      {isOpen && (
        <div
          style={{
            position: 'absolute',
            top: 'calc(100% + 4px)',
            left: 0,
            right: 0,
            background: '#ffffff',
            border: '1px solid #cbd5e1',
            borderRadius: '14px',
            boxShadow: '0 10px 30px rgba(0,0,0,0.12)',
            zIndex: 999,
            maxHeight: '320px',
            overflowY: 'auto',
            padding: '6px',
          }}
        >
          <div
            onClick={() => {
              onSelect('');
              setIsOpen(false);
            }}
            style={{
              padding: '8px 14px',
              fontSize: '13px',
              fontWeight: '700',
              color: !selectedId ? '#2563eb' : '#475569',
              background: !selectedId ? '#eff6ff' : 'transparent',
              borderRadius: '8px',
              cursor: 'pointer',
              marginBottom: '4px',
            }}
          >
            Tất cả danh mục
          </div>

          {renderTreeNodes(categoryTree.slice(0, 7))}
        </div>
      )}
    </div>
  );
}

/* ═══════════════════════════════════════════════════════════════════
   UNIFIED TABLE CATEGORY BADGE BUTTON (HIỂN THỊ DANH MỤC CẤP 1 TRƯỚC)
   - Nút pill xanh dương ngoài cùng hiển thị tên TÊN DANH MỤC CẤP 1 + MŨI TÊN ▼
   - Bấm ▼ để mở danh sách xổ xuống hiển thị sẵn tên các danh mục con thụt lùi
   ═══════════════════════════════════════════════════════════════════ */
function TableCategoryBadge({ product, categoriesMap }) {
  const [showList, setShowList] = useState(false);
  const dropdownRef = useRef(null);

  useEffect(() => {
    function handleClickOutside(e) {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target)) {
        setShowList(false);
      }
    }
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const catIds = product.categoryIds || [];
  const categoryChain = [];
  const visited = new Set();

  catIds.forEach((id) => {
    let curr = categoriesMap[id];
    while (curr && !visited.has(curr.id)) {
      visited.add(curr.id);
      categoryChain.push(curr);
      curr = curr.parentId ? categoriesMap[curr.parentId] : null;
    }
  });

  // Sort chain topologically: Root / Level 1 -> Level 2 -> Level 3
  categoryChain.sort((a, b) => {
    if (a.parentId === b.id) return 1;
    if (b.parentId === a.id) return -1;
    return (a.level ?? 1) - (b.level ?? 1);
  });

  // Combine categoryChain and product.categoryNames to guarantee ALL categories are shown
  const chainNamesSet = new Set(categoryChain.map((c) => c.name));
  const rawCategoryNames = Array.isArray(product.categoryNames) ? product.categoryNames : [];

  // Add any extra category names attached to the product
  const extraNames = rawCategoryNames.filter((name) => !chainNamesSet.has(name));

  const displayList = [
    ...categoryChain.map((c, idx) => ({
      id: c.id,
      name: c.name,
      level: idx,
      isPrimary: idx === 0,
    })),
    ...extraNames.map((name, idx) => ({
      id: `extra-${idx}`,
      name,
      level: categoryChain.length > 0 ? 1 : 0,
      isPrimary: categoryChain.length === 0 && idx === 0,
    })),
  ];

  const primaryLabel = displayList.length > 0 ? displayList[0].name : 'Chưa phân loại';

  return (
    <div ref={dropdownRef} style={{ position: 'relative', display: 'inline-block' }}>
      {/* MAIN BLUE PILL BUTTON */}
      <button
        type="button"
        onClick={() => setShowList((prev) => !prev)}
        title="Bấm để xem đầy đủ tất cả các danh mục liên quan"
        style={{
          background: 'linear-gradient(135deg, #0284c7 0%, #0369a1 100%)',
          color: '#ffffff',
          padding: '6px 14px',
          borderRadius: '14px',
          fontSize: '12px',
          fontWeight: '800',
          border: 'none',
          cursor: 'pointer',
          display: 'inline-flex',
          alignItems: 'center',
          gap: '6px',
          boxShadow: '0 2px 8px rgba(2,132,199,0.25)',
          transition: 'all 0.15s ease',
        }}
      >
        <span style={{ maxWidth: '130px', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
          {primaryLabel}
        </span>
        <span style={{ fontSize: '9px', opacity: 0.9 }}>▼</span>
      </button>

      {showList && (
        <div
          style={{
            position: 'absolute',
            top: 'calc(100% + 6px)',
            left: '50%',
            transform: 'translateX(-50%)',
            background: '#ffffff',
            border: '1px solid #cbd5e1',
            borderRadius: '14px',
            boxShadow: '0 10px 28px rgba(0,0,0,0.15)',
            zIndex: 900,
            minWidth: '200px',
            padding: '8px 10px',
            textAlign: 'left',
          }}
        >
          <div style={{ fontSize: '11px', fontWeight: '800', color: '#64748b', marginBottom: '6px', padding: '0 4px', textTransform: 'uppercase', letterSpacing: '0.5px' }}>
            🏷️ Danh mục liên quan ({displayList.length})
          </div>

          {displayList.length > 0 ? (
            displayList.map((item, idx) => (
              <div
                key={item.id || idx}
                style={{
                  fontSize: '12px',
                  fontWeight: item.isPrimary ? '800' : '600',
                  color: item.isPrimary ? '#0284c7' : '#1e293b',
                  padding: '6px 10px',
                  paddingLeft: `${8 + item.level * 14}px`,
                  borderRadius: '8px',
                  background: item.isPrimary ? '#f0f9ff' : 'transparent',
                  marginBottom: '2px',
                  whiteSpace: 'nowrap',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '6px',
                }}
              >
                {idx > 0 && <span style={{ color: '#94a3b8', fontSize: '11px' }}>↳</span>}
                <span>{item.name}</span>
              </div>
            ))
          ) : (
            <div style={{ fontSize: '12px', color: '#94a3b8', padding: '6px' }}>Chưa có danh mục nào</div>
          )}
        </div>
      )}
    </div>
  );
}

function AdminProductPage() {
  const { userRole } = useOutletContext();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const slowSellingPanelRef = useRef(null);
  const canDelete = userRole === 'ADMIN';
  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [categoryTree, setCategoryTree] = useState([]);
  const [categoriesMap, setCategoriesMap] = useState({});
  const [pageInfo, setPageInfo] = useState({ number: 0, totalPages: 1, totalElements: 0 });

  // Top & Slow selling products state
  const [topSellingList, setTopSellingList] = useState([]);
  const [slowSellingList, setSlowSellingList] = useState([]);
  const [slowPage, setSlowPage] = useState(0);
  const [slowSellingError, setSlowSellingError] = useState('');

  // Filters state (Keyword, Category, Status)
  const [filters, setFilters] = useState({ keyword: '', categoryId: '', status: '' });

  // Modal states
  const [viewProduct, setViewProduct] = useState(null);
  const [editProduct, setEditProduct] = useState(null);

  // Edit form state
  const [editForm, setEditForm] = useState({
    id: '',
    name: '',
    basePrice: '',
    categoryIds: '',
    status: 'ACTIVE',
    supplierId: '',
  });

  const [imageForm, setImageForm] = useState({ imageUrl: '', thumbnail: true });
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    loadCategories();
    loadTopAndSlowSelling();
  }, [searchParams]);

  useEffect(() => {
    if (searchParams.get('view') === 'SLOW_SELLING' && slowSellingPanelRef.current) {
      slowSellingPanelRef.current.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
  }, [searchParams, slowSellingList.length]);

  async function loadTopAndSlowSelling() {
    const reportParams = {
      from: searchParams.get('from') || undefined,
      to: searchParams.get('to') || undefined,
      timezone: 'Asia/Ho_Chi_Minh',
      groupBy: 'AUTO',
    };
    const maxUnits = Number(searchParams.get('maxUnits') || 5);
    setSlowSellingError('');

    const [topRes, slowRes] = await Promise.allSettled([
      getTopSellingProducts({ ...reportParams, limit: 10 }),
      // Backend validates limit with @Max(50). Using 100 returned HTTP 400 and
      // made this panel look empty while the operational badge still had data.
      getSlowSellingProducts({ ...reportParams, limit: 50, maxUnits }),
    ]);

    if (topRes.status === 'fulfilled') {
      const items = topRes.value?.content || (Array.isArray(topRes.value) ? topRes.value : []);
      setTopSellingList(items);
    } else {
      setTopSellingList([]);
    }

    if (slowRes.status === 'fulfilled') {
      const items = slowRes.value?.content || (Array.isArray(slowRes.value) ? slowRes.value : []);
      setSlowSellingList(items);
      setSlowPage(0);
    } else {
      setSlowSellingList([]);
      setSlowSellingError(
        slowRes.reason?.message || 'Không thể tải danh sách sản phẩm bán chậm. Vui lòng thử lại.',
      );
    }
  }

  useEffect(() => {
    loadProducts(0, filters);
  }, [filters]);

  async function loadCategories() {
    try {
      const [treeRes, flatRes] = await Promise.allSettled([
        getAdminCategoryTree(),
        getAdminCategories({ page: 0, size: 500 }),
      ]);

      const map = {};

      const flattenTree = (nodes) => {
        if (!Array.isArray(nodes)) return;
        nodes.forEach((n) => {
          if (n && n.id) {
            map[n.id] = n;
            const children = n.subCategories || n.children || [];
            if (children.length > 0) {
              flattenTree(children);
            }
          }
        });
      };

      if (flatRes.status === 'fulfilled') {
        const flatList = flatRes.value?.content || (Array.isArray(flatRes.value) ? flatRes.value : []);
        setCategories(flatList);
        flatList.forEach((c) => { if (c && c.id) map[c.id] = c; });
      }

      if (treeRes.status === 'fulfilled' && Array.isArray(treeRes.value)) {
        setCategoryTree(treeRes.value);
        flattenTree(treeRes.value);
      }

      setCategoriesMap(map);
    } catch {
      setCategoryTree([]);
      setCategories([]);
    }
  }

  function normalizePage(result) {
    return {
      content: result?.content || [],
      number: result?.number || 0,
      totalPages: result?.totalPages || 1,
      totalElements: result?.totalElements || result?.content?.length || 0,
    };
  }

  function loadProducts(page = pageInfo.number, activeFilters = filters) {
    setLoading(true);
    setError('');

    const queryParams = {
      page,
      size: 10,
      sort: 'createdAt,desc',
    };

    if (activeFilters.keyword && activeFilters.keyword.trim()) {
      queryParams.keyword = activeFilters.keyword.trim();
    }
    if (activeFilters.categoryId) {
      queryParams.categoryId = activeFilters.categoryId;
    }
    if (activeFilters.status) {
      queryParams.status = activeFilters.status;
    }

    getAdminProducts(queryParams)
      .then((result) => {
        const next = normalizePage(result);
        setProducts(next.content);
        setPageInfo({ number: next.number, totalPages: next.totalPages, totalElements: next.totalElements });
      })
      .catch((err) => setError(err.message || 'Không thể tải danh sách sản phẩm.'))
      .finally(() => setLoading(false));
  }

  function handleFilterSubmit(e) {
    e.preventDefault();
    loadProducts(0);
  }

  function handleResetFilters() {
    setFilters({ keyword: '', categoryId: '', status: '' });
    getAdminProducts({ page: 0, size: 10, sort: 'createdAt,desc' })
      .then((result) => {
        const next = normalizePage(result);
        setProducts(next.content);
        setPageInfo({ number: next.number, totalPages: next.totalPages, totalElements: next.totalElements });
      })
      .catch(() => {});
  }

  /* Open Edit Modal */
  function openEditModal(product) {
    setEditProduct(product);
    const rawStatus = typeof product.status === 'object' ? product.status?.name || product.status?.code : product.status;
    setEditForm({
      id: product.id || '',
      name: product.name || '',
      basePrice: product.basePrice ?? '',
      categoryIds: (product.categoryIds || []).join(','),
      status: String(rawStatus || 'ACTIVE').toUpperCase(),
      supplierId: product.supplierId || '',
    });
    setImageForm({ imageUrl: '', thumbnail: true });
  }

  /* Handle Update Product Submission */
  async function handleUpdateSubmit(e) {
    e.preventDefault();
    if (!editForm.id) return;
    setSubmitting(true);
    setMessage('');
    setError('');

    try {
      const payload = {
        name: editForm.name.trim(),
        basePrice: Number(editForm.basePrice || 0),
        categoryIds: editForm.categoryIds.split(',').map((id) => Number(id.trim())).filter(Boolean),
        status: editForm.status.trim() || 'ACTIVE',
        supplierId: editForm.supplierId ? Number(editForm.supplierId) : null,
      };

      await updateAdminProduct(editForm.id, payload);
      setMessage('Đã cập nhật thông tin sản phẩm thành công.');
      setEditProduct(null);
      loadProducts(pageInfo.number);
    } catch (err) {
      setError(err.message || 'Cập nhật sản phẩm thất bại.');
    } finally {
      setSubmitting(false);
    }
  }

  /* Handle Upload Image */
  async function handleUploadImage(event) {
    const file = event.target.files?.[0];
    if (!file || !editProduct?.id) return;

    setSubmitting(true);
    setError('');
    try {
      const result = await uploadImage(file, 'products');
      setImageForm((current) => ({ ...current, imageUrl: result.secureUrl || result.url || '' }));
      setMessage('Đã tải ảnh lên. Nhấn "Thêm ảnh" để lưu vào sản phẩm.');
    } catch (err) {
      setError(err.message || 'Tải ảnh lên thất bại.');
    } finally {
      setSubmitting(false);
    }
  }

  /* Handle Add Image to Product */
  async function handleAddImage() {
    if (!editProduct?.id || !imageForm.imageUrl.trim()) return;
    setSubmitting(true);
    setError('');
    try {
      const updated = await addProductImage(editProduct.id, {
        imageUrl: imageForm.imageUrl.trim(),
        thumbnail: imageForm.thumbnail,
      });
      setEditProduct(updated);
      setMessage('Đã thêm ảnh cho sản phẩm.');
      loadProducts(pageInfo.number);
    } catch (err) {
      setError(err.message || 'Không thể thêm ảnh.');
    } finally {
      setSubmitting(false);
    }
  }

  /* Delete Product */
  async function handleDeleteProduct(productId) {
    if (!window.confirm('Bạn có chắc chắn muốn xóa sản phẩm này không?')) return;
    setSubmitting(true);
    setError('');
    try {
      await deleteAdminProduct(productId);
      setMessage('Đã xóa sản phẩm thành công.');
      loadProducts(pageInfo.number);
    } catch (err) {
      setError(err.message || 'Xóa sản phẩm thất bại.');
    } finally {
      setSubmitting(false);
    }
  }

  /* Toggle Featured Product */
  async function handleToggleFeatured(productId) {
    try {
      const updated = await toggleProductFeatured(productId);
      setProducts((prev) =>
        prev.map((p) => (p.id === productId ? { ...p, featured: updated.featured } : p))
      );
      setMessage(
        updated.featured
          ? 'Đã ghim sản phẩm làm "Sản Phẩm Nổi Bật" ở Trang chủ.'
          : 'Đã hủy ghim sản phẩm nổi bật.'
      );
    } catch (err) {
      setError(err.message || 'Không thể thay đổi trạng thái nổi bật.');
    }
  }

  return (
    <section style={{ padding: '24px', background: '#f8fafc', minHeight: '100vh', fontFamily: 'system-ui, -apple-system, sans-serif' }}>
      
      {/* PAGE HEADER BAR */}
      <div style={{ background: '#ffffff', padding: '20px 28px', borderRadius: '20px', boxShadow: '0 2px 12px rgba(0,0,0,0.03)', border: '1px solid #f1f5f9', marginBottom: '24px', display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: '16px' }}>
        <h1 style={{ fontSize: '22px', fontWeight: '900', color: '#0f172a', margin: 0, textTransform: 'uppercase', letterSpacing: '-0.3px' }}>
          Quản Lý Sản Phẩm
        </h1>

        <span style={{ fontSize: '13px', background: '#fff7ed', border: '1px solid #fed7aa', color: '#ea580c', fontWeight: '800', padding: '6px 16px', borderRadius: '20px' }}>
          Tổng sản phẩm: {pageInfo.totalElements}
        </span>
      </div>

      {/* 2 PANELS: TOP SELLING & SLOW SELLING PRODUCTS */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(380px, 1fr))', gap: '20px', marginBottom: '24px' }}>
        
        {/* PANEL 1: 🔥 SẢN PHẨM BÁN CHẠY */}
        <div style={{ background: '#ffffff', padding: '20px 24px', borderRadius: '20px', boxShadow: '0 2px 12px rgba(0,0,0,0.03)', border: '1px solid #f1f5f9' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '14px' }}>
            <h3 style={{ fontSize: '15px', fontWeight: '900', color: '#166534', margin: 0, display: 'flex', alignItems: 'center', gap: '6px' }}>
              🔥 TOP SẢN PHẨM BÁN CHẠY (30 NGÀY)
            </h3>
            <span style={{ fontSize: '11px', background: '#f0fdf4', color: '#16a34a', border: '1px solid #bbf7d0', padding: '2px 8px', borderRadius: '8px', fontWeight: '800' }}>
              Doanh số cao
            </span>
          </div>

          <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '13px', textAlign: 'left' }}>
            <thead>
              <tr style={{ background: '#f8fafc', color: '#64748b', fontSize: '11px', fontWeight: '800' }}>
                <th style={{ padding: '8px 10px' }}>MÃ</th>
                <th style={{ padding: '8px 10px' }}>SẢN PHẨM</th>
                <th style={{ padding: '8px 10px', textAlign: 'center' }}>ĐÃ BÁN</th>
                <th style={{ padding: '8px 10px', textAlign: 'right' }}>DOANH THU</th>
                <th style={{ padding: '8px 10px', textAlign: 'center' }}>THAO TÁC</th>
              </tr>
            </thead>
            <tbody>
              {topSellingList.length === 0 ? (
                <tr>
                  <td colSpan="5" style={{ padding: '16px', textAlign: 'center', color: '#94a3b8' }}>Chưa có dữ liệu sản phẩm bán chạy.</td>
                </tr>
              ) : (
                topSellingList.slice(0, 5).map((item) => (
                  <tr key={item.productId} style={{ borderBottom: '1px solid #f1f5f9' }}>
                    <td style={{ padding: '10px', fontWeight: '800', color: '#475569' }}>PT{item.productId}</td>
                    <td style={{ padding: '10px', fontWeight: '700', color: '#0f172a' }}>{item.productName}</td>
                    <td style={{ padding: '10px', textAlign: 'center', fontWeight: '900', color: '#16a34a' }}>
                      {Number(item.soldQuantity ?? item.unitsSold ?? 0)} món
                    </td>
                    <td style={{ padding: '10px', textAlign: 'right', fontWeight: '800', color: '#dc2626' }}>
                      {formatPrice(item.grossRevenue ?? item.totalRevenue ?? 0)}
                    </td>
                    <td style={{ padding: '10px', textAlign: 'center' }}>
                      <button
                        type="button"
                        onClick={() => navigate(`/admin/inventory?view=restock&productId=${item.productId}&supplierId=${item.supplierId || ''}&productName=${encodeURIComponent(item.productName || '')}`)}
                        style={{
                          fontSize: '11px',
                          background: '#f0fdf4',
                          color: '#16a34a',
                          border: '1px solid #bbf7d0',
                          padding: '3px 10px',
                          borderRadius: '8px',
                          fontWeight: '800',
                          cursor: 'pointer',
                          whiteSpace: 'nowrap',
                        }}
                      >
                        + Nhập hàng
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        {/* PANEL 2: ⚠️ SẢN PHẨM BÁN CHẬM (CẢNH BÁO VẬN HÀNH) */}
        <div ref={slowSellingPanelRef} style={{ background: '#ffffff', padding: '20px 24px', borderRadius: '20px', boxShadow: '0 2px 12px rgba(0,0,0,0.03)', border: '1px solid #f1f5f9', scrollMarginTop: '24px' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '14px' }}>
            <h3 style={{ fontSize: '15px', fontWeight: '900', color: '#9a3412', margin: 0, display: 'flex', alignItems: 'center', gap: '6px' }}>
              ⚠️ SẢN PHẨM BÁN CHẬM (Ứ ĐỌNG VỐN)
            </h3>
            <span style={{ fontSize: '11px', background: '#fff7ed', color: '#ea580c', border: '1px solid #fed7aa', padding: '2px 8px', borderRadius: '8px', fontWeight: '800' }}>
              Hiển thị: {slowSellingList.length} SP ưu tiên
            </span>
          </div>

          <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '13px', textAlign: 'left' }}>
            <thead>
              <tr style={{ background: '#f8fafc', color: '#64748b', fontSize: '11px', fontWeight: '800' }}>
                <th style={{ padding: '8px 10px' }}>MÃ</th>
                <th style={{ padding: '8px 10px' }}>SẢN PHẨM</th>
                <th style={{ padding: '8px 10px', textAlign: 'center' }}>ĐÃ BÁN (30 NGÀY)</th>
                <th style={{ padding: '8px 10px', textAlign: 'right' }}>CẢNH BÁO VẬN HÀNH</th>
              </tr>
            </thead>
            <tbody>
              {slowSellingError ? (
                <tr>
                  <td colSpan="4" style={{ padding: '16px', textAlign: 'center', color: '#dc2626', fontWeight: '700' }}>
                    {slowSellingError}
                  </td>
                </tr>
              ) : slowSellingList.length === 0 ? (
                <tr>
                  <td colSpan="4" style={{ padding: '16px', textAlign: 'center', color: '#94a3b8' }}>Không có sản phẩm bị ứ đọng vốn.</td>
                </tr>
              ) : (
                slowSellingList.slice(slowPage * 5, (slowPage + 1) * 5).map((item) => (
                  <tr key={item.productId} style={{ borderBottom: '1px solid #f1f5f9' }}>
                    <td style={{ padding: '10px', fontWeight: '800', color: '#475569' }}>PT{item.productId}</td>
                    <td style={{ padding: '10px', fontWeight: '700', color: '#0f172a' }}>{item.productName}</td>
                    <td style={{ padding: '10px', textAlign: 'center', fontWeight: '900', color: '#d97706' }}>
                      {Number(item.soldQuantity ?? item.unitsSold ?? 0)} món
                    </td>
                    <td style={{ padding: '10px', textAlign: 'right' }}>
                      <button
                        type="button"
                        onClick={() => navigate(`/admin/promotions?createForProduct=${item.productId}&productName=${encodeURIComponent(item.productName || '')}`)}
                        style={{
                          fontSize: '11px',
                          background: '#fef2f2',
                          color: '#dc2626',
                          border: '1px solid #fecaca',
                          padding: '3px 10px',
                          borderRadius: '8px',
                          fontWeight: '800',
                          cursor: 'pointer',
                          transition: 'all 0.15s ease',
                        }}
                        onMouseEnter={(e) => { e.currentTarget.style.background = '#dc2626'; e.currentTarget.style.color = '#fff'; }}
                        onMouseLeave={(e) => { e.currentTarget.style.background = '#fef2f2'; e.currentTarget.style.color = '#dc2626'; }}
                      >
                        Giảm giá
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>

          {/* LOCAL PAGINATION CONTROLS FOR SLOW SELLING PRODUCTS */}
          {slowSellingList.length > 5 && (
            <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', gap: '8px', marginTop: '12px' }}>
              <button
                type="button"
                disabled={slowPage === 0}
                onClick={() => setSlowPage((p) => p - 1)}
                style={{ padding: '4px 10px', fontSize: '11px', background: '#fff', border: '1px solid #cbd5e1', borderRadius: '6px', cursor: slowPage === 0 ? 'not-allowed' : 'pointer', fontWeight: '700' }}
              >
                Trang trước
              </button>
              <span style={{ fontSize: '11.5px', fontWeight: '800', color: '#475569' }}>
                Trang {slowPage + 1} / {Math.ceil(slowSellingList.length / 5)}
              </span>
              <button
                type="button"
                disabled={slowPage >= Math.ceil(slowSellingList.length / 5) - 1}
                onClick={() => setSlowPage((p) => p + 1)}
                style={{ padding: '4px 10px', fontSize: '11px', background: '#fff', border: '1px solid #cbd5e1', borderRadius: '6px', cursor: slowPage >= Math.ceil(slowSellingList.length / 5) - 1 ? 'not-allowed' : 'pointer', fontWeight: '700' }}
              >
                Trang sau
              </button>
            </div>
          )}
        </div>

      </div>

      {/* FILTER PANEL WITH CUSTOM CATEGORY TREE DROPDOWN */}
      <form onSubmit={handleFilterSubmit} style={{ background: '#ffffff', padding: '20px 24px', borderRadius: '20px', boxShadow: '0 2px 12px rgba(0,0,0,0.03)', border: '1px solid #f1f5f9', marginBottom: '24px', display: 'flex', gap: '16px', alignItems: 'center', flexWrap: 'wrap' }}>
        <div style={{ flex: '1', minWidth: '220px' }}>
          <label style={{ fontSize: '12px', fontWeight: '800', color: '#475569', display: 'block', marginBottom: '6px' }}>Lọc theo tên sản phẩm</label>
          <input
            placeholder="Nhập tên sản phẩm..."
            value={filters.keyword}
            onChange={(e) => setFilters((c) => ({ ...c, keyword: e.target.value }))}
            style={{ width: '100%', padding: '10px 14px', border: '1px solid #cbd5e1', borderRadius: '12px', fontSize: '13px', outline: 'none', background: '#fff' }}
          />
        </div>

        {/* CATEGORY TREE SELECT */}
        <div style={{ flex: '1', minWidth: '240px' }}>
          <label style={{ fontSize: '12px', fontWeight: '800', color: '#475569', display: 'block', marginBottom: '6px' }}>Lọc theo cây danh mục</label>
          <CategoryTreeSelect
            categoryTree={categoryTree}
            selectedId={filters.categoryId}
            onSelect={(id) => setFilters((c) => ({ ...c, categoryId: id }))}
          />
        </div>

        <div style={{ flex: '1', minWidth: '180px' }}>
          <label style={{ fontSize: '12px', fontWeight: '800', color: '#475569', display: 'block', marginBottom: '6px' }}>Lọc theo trạng thái</label>
          <select
            value={filters.status}
            onChange={(e) => setFilters((c) => ({ ...c, status: e.target.value }))}
            style={{ width: '100%', padding: '10px 14px', border: '1px solid #cbd5e1', borderRadius: '12px', fontSize: '13px', outline: 'none', background: '#fff' }}
          >
            <option value="">Tất cả trạng thái</option>
            <option value="ACTIVE">Đang kinh doanh</option>
            <option value="INACTIVE">Ngừng kinh doanh</option>
            <option value="OUT_OF_STOCK">Hết hàng</option>
          </select>
        </div>

        <div style={{ display: 'flex', gap: '10px', marginTop: '22px' }}>
          <button
            type="submit"
            style={{ padding: '10px 24px', background: '#2563eb', color: '#fff', border: 'none', borderRadius: '12px', fontSize: '13px', fontWeight: '800', cursor: 'pointer' }}
          >
            Tìm kiếm
          </button>

          <button
            type="button"
            onClick={handleResetFilters}
            style={{ padding: '10px 18px', background: '#f1f5f9', color: '#64748b', border: '1px solid #cbd5e1', borderRadius: '12px', fontSize: '13px', fontWeight: '700', cursor: 'pointer' }}
          >
            Đặt lại
          </button>
        </div>
      </form>

      {/* MESSAGES */}
      {error && <div style={{ background: '#fef2f2', color: '#dc2626', border: '1px solid #fecaca', padding: '12px 16px', borderRadius: '12px', marginBottom: '20px', fontSize: '13px' }}>{error}</div>}
      {message && <div style={{ background: '#f0fdf4', color: '#16a34a', border: '1px solid #bbf7d0', padding: '12px 16px', borderRadius: '12px', marginBottom: '20px', fontSize: '13px' }}>{message}</div>}

      {/* PRODUCT TABLE WITH EXPANDABLE DANH MỤC BADGE */}
      <div style={{ background: '#ffffff', borderRadius: '20px', boxShadow: '0 4px 20px rgba(0,0,0,0.03)', border: '1px solid #e2e8f0', overflow: 'hidden' }}>
        <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'center', fontSize: '14px' }}>
          <thead>
            <tr style={{ background: '#e2e8f0', color: '#1e293b', fontWeight: '900', fontSize: '13px', borderBottom: '2px solid #cbd5e1' }}>
              <th style={{ padding: '16px 12px', width: '80px' }}>MÃ SP</th>
              <th style={{ padding: '16px 20px', textAlign: 'left' }}>TÊN SẢN PHẨM</th>
              <th style={{ padding: '16px 14px', width: '140px' }}>GIÁ</th>
              <th style={{ padding: '16px 12px', width: '90px' }}>TỒN KHO</th>
              <th style={{ padding: '16px 14px', width: '160px' }}>DANH MỤC</th>
              <th style={{ padding: '16px 14px', width: '130px' }}>NỔI BẬT</th>
              <th style={{ padding: '16px 14px', width: '150px' }}>TRẠNG THÁI</th>
              <th style={{ padding: '16px 14px', width: '90px' }}>ẢNH</th>
              <th style={{ padding: '16px 16px', width: '150px' }}>THAO TÁC</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr>
                <td colSpan="9" style={{ padding: '40px', color: '#64748b' }}>Đang tải danh sách sản phẩm từ hệ thống...</td>
              </tr>
            ) : products.length === 0 ? (
              <tr>
                <td colSpan="9" style={{ padding: '40px', color: '#94a3b8' }}>Không tìm thấy sản phẩm nào.</td>
              </tr>
            ) : (
              products.map((product, idx) => {
                const spCode = `PT${product.id}`;
                const statusInfo = getProductStatusInfo(product);

                return (
                  <tr
                    key={product.id}
                    style={{
                      borderBottom: '1px solid #f1f5f9',
                      background: idx % 2 === 0 ? '#ffffff' : '#fafafa',
                      transition: 'background 0.15s ease',
                    }}
                  >
                    {/* MÃ SP */}
                    <td style={{ padding: '16px 12px', fontWeight: '800', color: '#475569' }}>
                      {spCode}
                    </td>

                    {/* TÊN SẢN PHẨM */}
                    <td style={{ padding: '16px 20px', textAlign: 'left', fontWeight: '700', color: '#0f172a' }}>
                      {product.name}
                    </td>

                    {/* GIÁ (Nổi bật màu đỏ bold đ) */}
                    <td style={{ padding: '16px 14px', fontWeight: '900', color: '#dc2626', fontSize: '15px' }}>
                      {formatPrice(product.basePrice || 0)}
                    </td>

                    {/* TỒN KHO */}
                    <td style={{ padding: '16px 12px', fontWeight: '700', color: '#334155' }}>
                      {product.defaultVariantStockQuantity ?? 0}
                    </td>

                    {/* DANH MỤC (NÚT BLUE PILL THỐNG NHẤT CÓ CHỨA MŨI TÊN ▼ CHÍNH XÁC DANH MỤC CON) */}
                    <td style={{ padding: '16px 14px' }}>
                      <TableCategoryBadge product={product} categoriesMap={categoriesMap} />
                    </td>

                    {/* NỔI BẬT TOGGLE BUTTON */}
                    <td style={{ padding: '16px 14px' }}>
                      <button
                        type="button"
                        onClick={() => handleToggleFeatured(product.id)}
                        title={product.featured ? 'Đang hiển thị Nổi bật ở trang chủ (Bấm để hủy)' : 'Ghim làm Sản phẩm Nổi bật ở trang chủ'}
                        style={{
                          background: product.featured ? '#fff7ed' : '#f8fafc',
                          color: product.featured ? '#d97706' : '#94a3b8',
                          border: `1px solid ${product.featured ? '#fed7aa' : '#cbd5e1'}`,
                          padding: '5px 10px',
                          borderRadius: '12px',
                          fontSize: '12px',
                          fontWeight: '800',
                          cursor: 'pointer',
                          display: 'inline-flex',
                          alignItems: 'center',
                          gap: '4px',
                          transition: 'all 0.15s ease',
                        }}
                      >
                        <span style={{ fontSize: '14px', color: product.featured ? '#f59e0b' : '#cbd5e1' }}>
                          {product.featured ? '★' : '☆'}
                        </span>
                        <span style={{ fontSize: '11px', color: product.featured ? '#b45309' : '#64748b' }}>
                          {product.featured ? 'Nổi bật' : 'Thường'}
                        </span>
                      </button>
                    </td>

                    {/* TRẠNG THÁI */}
                    <td style={{ padding: '16px 14px' }}>
                      <span
                        style={{
                          background: statusInfo.bg,
                          color: statusInfo.color,
                          border: `1px solid ${statusInfo.border}`,
                          padding: '4px 10px',
                          borderRadius: '12px',
                          fontSize: '12px',
                          fontWeight: '800',
                          display: 'inline-block',
                          whiteSpace: 'nowrap',
                        }}
                      >
                        {statusInfo.label}
                      </span>
                    </td>

                    {/* ẢNH */}
                    <td style={{ padding: '12px 14px' }}>
                      <div style={{ width: '48px', height: '48px', margin: '0 auto', border: '1px solid #e2e8f0', borderRadius: '10px', padding: '3px', background: '#fff', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                        <img
                          src={product.thumbnailUrl || product.mainImageUrl || '/toystore-assets/logo.png'}
                          alt={product.name}
                          style={{ maxWidth: '100%', maxHeight: '100%', objectFit: 'contain', borderRadius: '6px' }}
                          onError={(e) => { e.target.src = '/toystore-assets/logo.png'; }}
                        />
                      </div>
                    </td>

                    {/* THAO TÁC */}
                    <td style={{ padding: '16px' }}>
                      <div style={{ display: 'flex', justifyContent: 'center', gap: '8px' }}>
                        {/* Nút 1: Xem (Xanh Dương) */}
                        <button
                          type="button"
                          onClick={() => setViewProduct(product)}
                          title="Xem thông tin sản phẩm"
                          style={{
                            width: '36px',
                            height: '36px',
                            background: '#0284c7',
                            color: '#ffffff',
                            border: 'none',
                            borderRadius: '8px',
                            cursor: 'pointer',
                            display: 'flex',
                            alignItems: 'center',
                            justify: 'center',
                            boxShadow: '0 2px 6px rgba(2,132,199,0.3)',
                          }}
                        >
                          <svg width="18" height="18" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
                            <path strokeLinecap="round" strokeLinejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                            <path strokeLinecap="round" strokeLinejoin="round" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                          </svg>
                        </button>

                        {/* Nút 2: Chỉnh sửa (Cam Vàng) */}
                        <button
                          type="button"
                          onClick={() => openEditModal(product)}
                          title="Chỉnh sửa sản phẩm"
                          style={{
                            width: '36px',
                            height: '36px',
                            background: '#d97706',
                            color: '#ffffff',
                            border: 'none',
                            borderRadius: '8px',
                            cursor: 'pointer',
                            display: 'flex',
                            alignItems: 'center',
                            justify: 'center',
                            boxShadow: '0 2px 6px rgba(217,119,6,0.3)',
                          }}
                        >
                          <svg width="18" height="18" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
                            <path strokeLinecap="round" strokeLinejoin="round" d="M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z" />
                          </svg>
                        </button>

                        {/* Nút 3: Xóa (Đỏ) - Chỉ MANAGER/ADMIN */}
                        {canDelete && (
                        <button
                          type="button"
                          onClick={() => handleDeleteProduct(product.id)}
                          title="Xóa sản phẩm"
                          style={{
                            width: '36px',
                            height: '36px',
                            background: '#dc2626',
                            color: '#ffffff',
                            border: 'none',
                            borderRadius: '8px',
                            cursor: 'pointer',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            boxShadow: '0 2px 6px rgba(220,38,38,0.3)',
                          }}
                        >
                          <svg width="18" height="18" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
                            <path strokeLinecap="round" strokeLinejoin="round" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                          </svg>
                        </button>
                        )}
                      </div>
                    </td>
                  </tr>
                );
              })
            )}
          </tbody>
        </table>
      </div>

      {/* PAGINATION BAR */}
      {pageInfo.totalPages > 1 && (
        <div style={{ display: 'flex', justifyContent: 'center', gap: '8px', marginTop: '24px', alignItems: 'center' }}>
          <button
            type="button"
            disabled={pageInfo.number === 0}
            onClick={() => loadProducts(pageInfo.number - 1)}
            style={{ padding: '8px 16px', background: '#ffffff', border: '1px solid #cbd5e1', borderRadius: '10px', fontSize: '13px', fontWeight: '700', cursor: pageInfo.number === 0 ? 'not-allowed' : 'pointer' }}
          >
            Trang trước
          </button>
          <span style={{ fontSize: '13px', fontWeight: '700', color: '#475569' }}>
            Trang {pageInfo.number + 1} / {pageInfo.totalPages}
          </span>
          <button
            type="button"
            disabled={pageInfo.number >= pageInfo.totalPages - 1}
            onClick={() => loadProducts(pageInfo.number + 1)}
            style={{ padding: '8px 16px', background: '#ffffff', border: '1px solid #cbd5e1', borderRadius: '10px', fontSize: '13px', fontWeight: '700', cursor: pageInfo.number >= pageInfo.totalPages - 1 ? 'not-allowed' : 'pointer' }}
          >
            Trang sau
          </button>
        </div>
      )}

      {/* MODAL 1: VIEW PRODUCT DETAILS */}
      {viewProduct && (
        <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, background: 'rgba(0,0,0,0.5)', zIndex: 1000, display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '20px' }}>
          <div style={{ background: '#ffffff', borderRadius: '24px', padding: '32px', width: '100%', maxWidth: '600px', maxHeight: '90vh', overflowY: 'auto', boxShadow: '0 20px 50px rgba(0,0,0,0.2)' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px', borderBottom: '1px solid #f1f5f9', paddingBottom: '12px' }}>
              <h2 style={{ fontSize: '20px', fontWeight: '900', color: '#0f172a', margin: 0 }}>
                Chi Tiết Sản Phẩm PT{viewProduct.id}
              </h2>
              <button type="button" onClick={() => setViewProduct(null)} style={{ border: 'none', background: '#f1f5f9', borderRadius: '50%', width: '32px', height: '32px', cursor: 'pointer', fontWeight: '900' }}>×</button>
            </div>

            <div style={{ display: 'flex', gap: '20px', marginBottom: '20px' }}>
              <img
                src={viewProduct.thumbnailUrl || viewProduct.mainImageUrl || '/toystore-assets/logo.png'}
                alt=""
                style={{ width: '120px', height: '120px', objectFit: 'contain', border: '1px solid #e2e8f0', borderRadius: '12px', padding: '6px' }}
              />
              <div style={{ display: 'flex', flexDirection: 'column', gap: '8px', fontSize: '14px' }}>
                <div><strong>Tên sản phẩm:</strong> {viewProduct.name}</div>
                <div><strong>Giá niêm yết:</strong> <span style={{ color: '#dc2626', fontWeight: '800' }}>{formatPrice(viewProduct.basePrice)}</span></div>
                <div><strong>Trạng thái:</strong> <span style={{ color: getProductStatusInfo(viewProduct).color, fontWeight: '800' }}>{getProductStatusInfo(viewProduct).label}</span></div>
                <div><strong>Tồn kho:</strong> {viewProduct.defaultVariantStockQuantity ?? 0} sản phẩm</div>
                <div><strong>Danh mục:</strong> {(viewProduct.categoryNames || []).join(', ') || 'Chưa phân loại'}</div>
                {viewProduct.supplierName && <div><strong>Nhà cung cấp:</strong> {viewProduct.supplierName}</div>}
              </div>
            </div>

            <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: '24px' }}>
              <button type="button" onClick={() => setViewProduct(null)} style={{ padding: '10px 24px', background: '#0284c7', color: '#fff', border: 'none', borderRadius: '12px', fontWeight: '800', cursor: 'pointer' }}>
                Đóng
              </button>
            </div>
          </div>
        </div>
      )}

      {/* MODAL 2: EDIT PRODUCT INFO ONLY */}
      {editProduct && (
        <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, background: 'rgba(0,0,0,0.5)', zIndex: 1000, display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '20px' }}>
          <div style={{ background: '#ffffff', borderRadius: '24px', padding: '32px', width: '100%', maxWidth: '700px', maxHeight: '90vh', overflowY: 'auto', boxShadow: '0 20px 50px rgba(0,0,0,0.2)' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px', borderBottom: '1px solid #f1f5f9', paddingBottom: '12px' }}>
              <h2 style={{ fontSize: '20px', fontWeight: '900', color: '#0f172a', margin: 0 }}>
                Chỉnh Sửa Sản Phẩm PT{editProduct.id}
              </h2>
              <button type="button" onClick={() => setEditProduct(null)} style={{ border: 'none', background: '#f1f5f9', borderRadius: '50%', width: '32px', height: '32px', cursor: 'pointer', fontWeight: '900' }}>×</button>
            </div>

            <form onSubmit={handleUpdateSubmit} style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: '16px' }}>
              <div style={{ gridColumn: '1 / -1' }}>
                <label style={{ fontSize: '13px', fontWeight: '800', display: 'block', marginBottom: '6px' }}>Tên sản phẩm *</label>
                <input
                  value={editForm.name}
                  onChange={(e) => setEditForm((c) => ({ ...c, name: e.target.value }))}
                  required
                  style={{ width: '100%', padding: '10px 14px', border: '1px solid #cbd5e1', borderRadius: '12px', fontSize: '14px' }}
                />
              </div>

              <div>
                <label style={{ fontSize: '13px', fontWeight: '800', display: 'block', marginBottom: '6px' }}>Giá cơ bản (VND) *</label>
                <input
                  type="number"
                  value={editForm.basePrice}
                  onChange={(e) => setEditForm((c) => ({ ...c, basePrice: e.target.value }))}
                  required
                  style={{ width: '100%', padding: '10px 14px', border: '1px solid #cbd5e1', borderRadius: '12px', fontSize: '14px' }}
                />
              </div>

              <div>
                <label style={{ fontSize: '13px', fontWeight: '800', display: 'block', marginBottom: '6px' }}>Trạng thái bán</label>
                <select
                  value={editForm.status}
                  onChange={(e) => setEditForm((c) => ({ ...c, status: e.target.value }))}
                  style={{ width: '100%', padding: '10px 14px', border: '1px solid #cbd5e1', borderRadius: '12px', fontSize: '14px' }}
                >
                  <option value="ACTIVE">Đang kinh doanh</option>
                  <option value="INACTIVE">Ngừng kinh doanh</option>
                  <option value="OUT_OF_STOCK">Hết hàng</option>
                </select>
              </div>

              <div>
                <label style={{ fontSize: '13px', fontWeight: '800', display: 'block', marginBottom: '6px' }}>ID Danh mục (ngăn cách bởi dấu phẩy)</label>
                <input
                  value={editForm.categoryIds}
                  onChange={(e) => setEditForm((c) => ({ ...c, categoryIds: e.target.value }))}
                  placeholder="Ví dụ: 1, 2"
                  style={{ width: '100%', padding: '10px 14px', border: '1px solid #cbd5e1', borderRadius: '12px', fontSize: '14px' }}
                />
              </div>

              {/* MEDIA & IMAGE MANAGEMENT */}
              <div style={{ gridColumn: '1 / -1', background: '#f8fafc', padding: '16px', borderRadius: '16px', border: '1px solid #e2e8f0', marginTop: '10px' }}>
                <h4 style={{ margin: '0 0 12px 0', fontSize: '14px', fontWeight: '800', color: '#334155' }}>Quản lý hình ảnh sản phẩm</h4>
                <div style={{ display: 'flex', gap: '12px', alignItems: 'center', marginBottom: '12px' }}>
                  <input type="file" accept="image/*" onChange={handleUploadImage} disabled={submitting} style={{ fontSize: '13px' }} />
                  <button type="button" onClick={handleAddImage} disabled={!imageForm.imageUrl || submitting} style={{ padding: '8px 16px', background: '#0284c7', color: '#fff', border: 'none', borderRadius: '10px', fontSize: '13px', fontWeight: '800', cursor: 'pointer' }}>
                    Thêm ảnh
                  </button>
                </div>

                <div style={{ display: 'flex', gap: '10px', flexWrap: 'wrap' }}>
                  {(editProduct?.images || []).map((img) => (
                    <div key={img.id} style={{ border: '1px solid #cbd5e1', borderRadius: '10px', padding: '6px', background: '#fff', textAlign: 'center' }}>
                      <img src={img.imageUrl} alt="" style={{ width: '60px', height: '60px', objectFit: 'contain' }} />
                      <div style={{ marginTop: '4px', display: 'flex', gap: '4px' }}>
                        <button type="button" onClick={() => setProductThumbnail(editProduct.id, img.id).then(() => loadProducts(pageInfo.number))} style={{ fontSize: '10px', padding: '2px 6px', background: '#16a34a', color: '#fff', border: 'none', borderRadius: '4px' }}>Dùng đại diện</button>
                        <button type="button" onClick={() => removeProductImage(editProduct.id, img.id).then(() => loadProducts(pageInfo.number))} style={{ fontSize: '10px', padding: '2px 6px', background: '#dc2626', color: '#fff', border: 'none', borderRadius: '4px' }}>Xóa</button>
                      </div>
                    </div>
                  ))}
                </div>
              </div>

              {/* ACTION BUTTONS */}
              <div style={{ gridColumn: '1 / -1', display: 'flex', justifyContent: 'flex-end', gap: '12px', marginTop: '16px' }}>
                <button type="button" onClick={() => setEditProduct(null)} style={{ padding: '12px 24px', background: '#f1f5f9', color: '#64748b', border: '1px solid #cbd5e1', borderRadius: '12px', fontWeight: '800', cursor: 'pointer' }}>
                  Hủy bỏ
                </button>
                <button type="submit" disabled={submitting} style={{ padding: '12px 28px', background: '#ea580c', color: '#fff', border: 'none', borderRadius: '14px', fontWeight: '800', cursor: 'pointer' }}>
                  {submitting ? 'Đang lưu...' : 'Lưu cập nhật'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

    </section>
  );
}

export default AdminProductPage;
