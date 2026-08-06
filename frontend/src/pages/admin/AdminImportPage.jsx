import { useEffect, useRef, useState } from 'react';
import { getAdminCategories, getAdminCategoryTree } from '../../services/adminCategoryService.js';
import {
  cancelImportNote,
  completeImportNote,
  createImportNote,
  getImportDetails,
  getImports,
} from '../../services/adminImportService.js';
import { addProductVariant, createAdminProduct, getAdminProducts } from '../../services/adminProductService.js';
import { getSuppliers } from '../../services/adminSupplierService.js';
import { formatDateTime, formatPrice } from '../../utils/formatters.js';

const emptyItem = {
  productId: '',
  variantId: '',
  productName: '',
  quantity: 1,
  importPrice: 0,
};

/* Helper for Import Note Status Badge */
function getImportStatusBadge(status) {
  const statusStr = typeof status === 'object' ? (status?.name || status?.code || '') : String(status || '');
  const uppercase = statusStr.toUpperCase();

  if (uppercase === 'COMPLETED' || uppercase === 'HOÀN THÀNH') {
    return { label: '✓ Đã hoàn thành', bg: '#f0fdf4', color: '#16a34a', border: '#bbf7d0' };
  }
  if (uppercase === 'CANCELLED' || uppercase === 'HỦY') {
    return { label: '✕ Đã hủy', bg: '#fef2f2', color: '#dc2626', border: '#fecaca' };
  }
  return { label: '✓ Mới tạo', bg: '#f0f9ff', color: '#0284c7', border: '#bae6fd' };
}

/* Custom Category Tree Dropdown Component */
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
            <span style={{ flex: 1, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis', paddingRight: '8px' }}>
              {node.name}
            </span>

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

          {hasChildren && isExpanded && (
            <div style={{ marginTop: '2px' }}>
              {renderTreeNodes(subList, level + 1)}
            </div>
          )}
        </div>
      );
    });
  };

  return (
    <div ref={dropdownRef} style={{ position: 'relative', width: '100%' }}>
      <button
        type="button"
        onClick={() => setIsOpen(!isOpen)}
        style={{
          width: '100%',
          padding: '12px 16px',
          background: '#ffffff',
          border: '1px solid #cbd5e1',
          borderRadius: '12px',
          fontSize: '13px',
          fontWeight: '700',
          color: selectedId ? '#2563eb' : '#475569',
          display: 'flex',
          alignItems: 'center',
          justify: 'space-between',
          cursor: 'pointer',
          boxShadow: '0 1px 2px rgba(0,0,0,0.05)',
        }}
      >
        <span style={{ flex: 1, textAlign: 'left', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
          {selectedLabel}
        </span>
        <span style={{ fontSize: '10px', color: '#94a3b8', marginLeft: '8px' }}>
          {isOpen ? '▲' : '▼'}
        </span>
      </button>

      {isOpen && (
        <div
          style={{
            position: 'absolute',
            top: 'calc(100% + 6px)',
            left: 0,
            right: 0,
            background: '#ffffff',
            border: '1px solid #e2e8f0',
            borderRadius: '14px',
            boxShadow: '0 10px 25px -5px rgba(0,0,0,0.15)',
            zIndex: 1050,
            maxHeight: '280px',
            overflowY: 'auto',
            padding: '6px',
          }}
        >
          <div
            style={{
              padding: '9px 14px',
              fontWeight: selectedId === '' ? '800' : '600',
              color: selectedId === '' ? '#2563eb' : '#64748b',
              background: selectedId === '' ? '#eff6ff' : 'transparent',
              borderRadius: '8px',
              cursor: 'pointer',
              fontSize: '13px',
              marginBottom: '4px',
            }}
            onClick={() => {
              onSelect('');
              setIsOpen(false);
            }}
          >
            Tất cả danh mục
          </div>

          {renderTreeNodes(categoryTree)}
        </div>
      )}
    </div>
  );
}

/* Helper to recursively collect a category ID and all its child & descendant category IDs */
function getAllCategoryIdsInTree(tree, targetId) {
  if (!targetId) return [];
  const result = [];

  function searchAndCollect(nodes, matchFound) {
    for (const node of nodes) {
      const isMatch = matchFound || String(node.id) === String(targetId);
      if (isMatch) {
        result.push(node.id);
      }
      const children = node.subCategories || node.children || [];
      if (children.length > 0) {
        searchAndCollect(children, isMatch);
      }
    }
  }

  searchAndCollect(tree, false);
  return result;
}

/* Helper to filter Category Tree so it ONLY shows categories containing products from the selected Supplier */
function filterCategoryTreeForSupplier(nodes, supplierProducts) {
  if (!supplierProducts || supplierProducts.length === 0) return nodes;

  const supplierCatIdSet = new Set();
  supplierProducts.forEach((p) => {
    if (Array.isArray(p.categoryIds)) p.categoryIds.forEach((id) => supplierCatIdSet.add(Number(id)));
    if (p.categoryId) supplierCatIdSet.add(Number(p.categoryId));
    if (p.category?.id) supplierCatIdSet.add(Number(p.category.id));
    if (Array.isArray(p.categories)) {
      p.categories.forEach((c) => {
        const id = typeof c === 'object' ? c?.id : c;
        if (id) supplierCatIdSet.add(Number(id));
      });
    }
  });

  const allowedCatIds = Array.from(supplierCatIdSet);

  function filterNodes(list) {
    return list
      .map((node) => {
        const subList = node.subCategories || node.children || [];
        const filteredSubs = filterNodes(subList);
        const isSelfAllowed = allowedCatIds.includes(node.id);
        const hasAllowedChild = filteredSubs.length > 0;

        if (isSelfAllowed || hasAllowedChild) {
          return {
            ...node,
            subCategories: filteredSubs,
            children: filteredSubs,
          };
        }
        return null;
      })
      .filter(Boolean);
  }

  const result = filterNodes(nodes);
  return result.length > 0 ? result : nodes;
}

/* Helper to check if a product belongs to any of targetCategoryIds supporting ALL product data structures */
function productMatchesCategory(product, targetCategoryIds, categoryNameMap = {}) {
  if (!targetCategoryIds || targetCategoryIds.length === 0) return true;
  
  const targetIdStrings = targetCategoryIds.map((id) => String(id));

  if (Array.isArray(product.categoryIds) && product.categoryIds.length > 0) {
    if (product.categoryIds.some((id) => targetIdStrings.includes(String(id)))) {
      return true;
    }
  }

  if (product.categoryId !== undefined && product.categoryId !== null) {
    if (targetIdStrings.includes(String(product.categoryId))) {
      return true;
    }
  }

  if (product.category && product.category.id !== undefined && product.category.id !== null) {
    if (targetIdStrings.includes(String(product.category.id))) {
      return true;
    }
  }

  if (Array.isArray(product.categories) && product.categories.length > 0) {
    for (const cat of product.categories) {
      const catId = typeof cat === 'object' ? cat?.id : cat;
      if (catId && targetIdStrings.includes(String(catId))) {
        return true;
      }
    }
  }

  const prodCatName = product.categoryName || product.category?.name;
  if (prodCatName) {
    const matchedCategoryNames = targetCategoryIds.map((id) => categoryNameMap[id]).filter(Boolean);
    if (matchedCategoryNames.some((name) => name.toLowerCase() === prodCatName.toLowerCase())) {
      return true;
    }
  }

  if (Array.isArray(product.categories)) {
    for (const cat of product.categories) {
      if (typeof cat === 'object' && cat?.name) {
        const matchedCategoryNames = targetCategoryIds.map((id) => categoryNameMap[id]).filter(Boolean);
        if (matchedCategoryNames.some((name) => name.toLowerCase() === cat.name.toLowerCase())) {
          return true;
        }
      }
    }
  }

  return false;
}

function AdminImportPage() {
  // Main Data States
  const [imports, setImports] = useState([]);
  const [suppliers, setSuppliers] = useState([]);
  const [allProducts, setAllProducts] = useState([]);
  const [categoryTree, setCategoryTree] = useState([]);
  const [categoryNameMap, setCategoryNameMap] = useState({});
  const [pageInfo, setPageInfo] = useState({ number: 0, totalPages: 1, totalElements: 0 });

  // View States
  const [isCreatingView, setIsCreatingView] = useState(false);
  const [viewNote, setViewNote] = useState(null);
  const [loadingDetails, setLoadingDetails] = useState(false);

  // Filters State for History View
  const [filters, setFilters] = useState({ code: '', supplierId: '' });

  // Create Form State
  const [form, setForm] = useState({ supplierId: '', categoryId: '', note: '' });
  const [items, setItems] = useState([{ ...emptyItem }]);

  // Modals for Adding Product & Variant On-The-Fly
  const [showAddProductModal, setShowAddProductModal] = useState(false);
  const [targetRowForProduct, setTargetRowForProduct] = useState(0);
  const [newProductForm, setNewProductForm] = useState({ name: '', basePrice: '', categoryId: '' });

  const [showAddVariantModal, setShowAddVariantModal] = useState(false);
  const [targetRowForVariant, setTargetRowForVariant] = useState(0);
  const [targetProductForVariant, setTargetProductForVariant] = useState(null);
  const [newVariantForm, setNewVariantForm] = useState({ title: '', price: '' });

  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [modalSubmitting, setModalSubmitting] = useState(false);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    loadInitialData();
  }, []);

  useEffect(() => {
    loadImports(0);
  }, [filters]);

  async function loadInitialData() {
    try {
      const [supRes, prodRes, treeRes, catRes] = await Promise.allSettled([
        getSuppliers({ page: 0, size: 100 }),
        getAdminProducts({ page: 0, size: 300 }),
        getAdminCategoryTree(),
        getAdminCategories(),
      ]);

      if (supRes.status === 'fulfilled') {
        const list = supRes.value?.content || supRes.value || [];
        setSuppliers(list);
      }
      if (prodRes.status === 'fulfilled') {
        const list = prodRes.value?.content || prodRes.value || [];
        setAllProducts(list);
      }
      if (treeRes.status === 'fulfilled') {
        setCategoryTree(treeRes.value || []);
      }
      if (catRes.status === 'fulfilled') {
        const cats = Array.isArray(catRes.value) ? catRes.value : catRes.value?.content || [];
        const map = {};
        cats.forEach((c) => { map[c.id] = c.name; });
        setCategoryNameMap(map);
      }
    } catch (e) {
      console.error(e);
    }
  }

  async function reloadProducts() {
    try {
      const prodRes = await getAdminProducts({ page: 0, size: 300 });
      const list = prodRes?.content || prodRes || [];
      setAllProducts(list);
      return list;
    } catch (e) {
      console.error(e);
      return [];
    }
  }

  async function loadImports(page = 0) {
    setLoading(true);
    setError('');
    try {
      const queryParams = { page, size: 10, sort: 'createdAt,desc' };
      if (filters.supplierId) queryParams.supplierId = filters.supplierId;

      const result = await getImports(queryParams);
      let list = result?.content || result || [];

      if (filters.code.trim()) {
        const kw = filters.code.trim().toLowerCase();
        list = list.filter(
          (item) =>
            String(item.id).toLowerCase().includes(kw) ||
            `pn${item.id}`.toLowerCase().includes(kw) ||
            `pnb8cc2630`.toLowerCase().includes(kw)
        );
      }

      setImports(list);
      setPageInfo({
        number: result?.number || 0,
        totalPages: result?.totalPages || 1,
        totalElements: result?.totalElements || list.length,
      });
    } catch (err) {
      setError(err?.message || 'Không thể tải lịch sử nhập hàng.');
      setImports([]);
    } finally {
      setLoading(false);
    }
  }

  /* Open View Import Note Details Modal */
  async function openViewModal(id) {
    setLoadingDetails(true);
    setError('');
    try {
      const details = await getImportDetails(id);
      setViewNote(details);
    } catch (err) {
      setError(err?.message || 'Không thể tải chi tiết phiếu nhập.');
    } finally {
      setLoadingDetails(false);
    }
  }

  /* Filter products for selected Supplier */
  const supplierProducts = form.supplierId
    ? allProducts.filter((p) => String(p.supplierId) === String(form.supplierId))
    : [];

  /* Filter category tree so it ONLY displays categories containing products for the selected Supplier */
  const supplierCategoryTree = form.supplierId
    ? filterCategoryTreeForSupplier(categoryTree, supplierProducts)
    : categoryTree;

  /* Filter products by Supplier AND Category (including all subcategories) */
  const selectedCategoryIds = getAllCategoryIdsInTree(categoryTree, form.categoryId);

  const availableProducts = allProducts.filter((p) => {
    const matchSupplier = !form.supplierId || String(p.supplierId) === String(form.supplierId);
    const matchCategory = productMatchesCategory(p, selectedCategoryIds, categoryNameMap);
    return matchSupplier && matchCategory;
  });

  /* Handle Item Field Changes */
  function handleProductSelect(index, productIdStr) {
    if (!productIdStr) {
      updateItem(index, { productId: '', variantId: '', productName: '', importPrice: 0 });
      return;
    }

    const prodId = Number(productIdStr);
    const product = allProducts.find((p) => p.id === prodId);
    if (!product) return;

    const variants = product.variants || [];
    let defaultVarId = '';
    if (variants.length === 1) {
      defaultVarId = variants[0].id;
    } else if (product.defaultVariantId) {
      defaultVarId = product.defaultVariantId;
    }

    updateItem(index, {
      productId: prodId,
      productName: product.name,
      variantId: defaultVarId,
      importPrice: product.basePrice || 0,
    });
  }

  function updateItem(index, updatedFields) {
    setItems((current) =>
      current.map((item, itemIndex) => (itemIndex === index ? { ...item, ...updatedFields } : item))
    );
  }

  function addItemRow() {
    setItems((current) => [...current, { ...emptyItem }]);
  }

  function removeItemRow(index) {
    if (items.length === 1) return;
    setItems((current) => current.filter((_, itemIndex) => itemIndex !== index));
  }

  /* Handle Create New Product On-The-Fly */
  async function handleCreateNewProduct(e) {
    e.preventDefault();
    if (!newProductForm.name.trim()) return;

    setModalSubmitting(true);
    setError('');
    try {
      const payload = {
        name: newProductForm.name.trim(),
        basePrice: Number(newProductForm.basePrice || 0),
        supplierId: Number(form.supplierId),
        categoryIds: newProductForm.categoryId ? [Number(newProductForm.categoryId)] : [],
        status: 'ACTIVE',
      };

      const created = await createAdminProduct(payload);
      const updatedList = await reloadProducts();

      // Find created product in updated list
      const freshProduct = updatedList.find((p) => p.id === created.id) || created;

      // Auto-select newly created product for the row
      const defaultVarId = freshProduct.variants?.[0]?.id || freshProduct.defaultVariantId || '';
      updateItem(targetRowForProduct, {
        productId: freshProduct.id,
        productName: freshProduct.name,
        variantId: defaultVarId,
        importPrice: freshProduct.basePrice || 0,
      });

      setShowAddProductModal(false);
      setNewProductForm({ name: '', basePrice: '', categoryId: '' });
      setMessage(`Đã tạo sản phẩm mới "${freshProduct.name}" và chọn vào dòng nhập hàng!`);
    } catch (err) {
      setError(err?.message || 'Tạo sản phẩm mới thất bại.');
    } finally {
      setModalSubmitting(false);
    }
  }

  /* Handle Create New Variant On-The-Fly */
  async function handleCreateNewVariant(e) {
    e.preventDefault();
    if (!targetProductForVariant || !newVariantForm.title.trim()) return;

    setModalSubmitting(true);
    setError('');
    try {
      const payload = {
        attributes: { 'Biến thể': newVariantForm.title.trim() },
        price: Number(newVariantForm.price || targetProductForVariant.basePrice || 0),
        initialStock: 0,
      };

      const updatedProdResponse = await addProductVariant(targetProductForVariant.id, payload);
      const updatedList = await reloadProducts();

      // Find updated product and newly added variant
      const freshProd = updatedList.find((p) => p.id === targetProductForVariant.id) || updatedProdResponse;
      const latestVariant = freshProd.variants?.[freshProd.variants.length - 1];

      if (latestVariant) {
        updateItem(targetRowForVariant, {
          variantId: latestVariant.id,
          importPrice: latestVariant.price || targetProductForVariant.basePrice || 0,
        });
      }

      setShowAddVariantModal(false);
      setNewVariantForm({ title: '', price: '' });
      setMessage(`Đã thêm biến thể mới "${newVariantForm.title}" cho sản phẩm!`);
    } catch (err) {
      setError(err?.message || 'Thêm biến thể mới thất bại.');
    } finally {
      setModalSubmitting(false);
    }
  }

  /* Calculate Total Import Note Amount */
  const calculatedTotalAmount = items.reduce((sum, item) => {
    const qty = Number(item.quantity || 0);
    const price = Number(item.importPrice || 0);
    return sum + qty * price;
  }, 0);

  /* Strict Frontend Validation Before Submit */
  function validateForm() {
    if (!form.supplierId) {
      setError('Vui lòng chọn Nhà Cung Cấp trước khi tạo phiếu nhập!');
      return false;
    }

    if (items.length === 0) {
      setError('Vui lòng thêm ít nhất một sản phẩm nhập!');
      return false;
    }

    for (let i = 0; i < items.length; i++) {
      const item = items[i];
      if (!item.productId) {
        setError(`Dòng thứ ${i + 1} chưa chọn sản phẩm. Vui lòng chọn sản phẩm cho tất cả các dòng!`);
        return false;
      }

      const product = allProducts.find((p) => p.id === Number(item.productId));
      const variants = product?.variants || [];

      if (variants.length > 1 && !item.variantId) {
        setError(`Sản phẩm "${item.productName}" có ${variants.length} biến thể, vui lòng chọn biến thể cụ thể!`);
        return false;
      }

      if (Number(item.quantity || 0) <= 0) {
        setError(`Số lượng nhập dòng thứ ${i + 1} ("${item.productName}") phải lớn hơn 0!`);
        return false;
      }

      if (Number(item.importPrice || 0) < 0) {
        setError(`Giá nhập dòng thứ ${i + 1} ("${item.productName}") không được là số âm!`);
        return false;
      }
    }

    return true;
  }

  /* Submit Form Create Import Note */
  async function handleSaveImportNote(e) {
    e.preventDefault();
    setError('');
    setMessage('');

    if (!validateForm()) return;

    setSubmitting(true);
    try {
      const payload = {
        supplierId: Number(form.supplierId),
        note: form.note.trim() || null,
        items: items.map((item) => ({
          productId: Number(item.productId),
          variantId: item.variantId ? Number(item.variantId) : null,
          productName: item.productName.trim(),
          quantity: Number(item.quantity),
          importPrice: Number(item.importPrice),
        })),
      };

      await createImportNote(payload);
      setMessage('Đã tạo phiếu nhập hàng thành công!');
      setIsCreatingView(false);
      setForm({ supplierId: '', categoryId: '', note: '' });
      setItems([{ ...emptyItem }]);
      loadImports(0);
    } catch (err) {
      setError(err?.message || 'Tạo phiếu nhập hàng thất bại. Vui lòng kiểm tra dữ liệu.');
    } finally {
      setSubmitting(false);
    }
  }

  /* Actions on Pending Note: Complete / Cancel */
  async function handleActionOnNote(actionFn, successMsg) {
    if (!viewNote?.id) return;
    setError('');
    setMessage('');
    try {
      await actionFn(viewNote.id);
      setMessage(successMsg);
      setViewNote(null);
      loadImports(pageInfo.number);
    } catch (err) {
      setError(err?.message || 'Thao tác thất bại.');
    }
  }

  return (
    <section style={{ padding: '24px', background: '#f8fafc', minHeight: '100vh', fontFamily: 'system-ui, -apple-system, sans-serif' }}>
      
      {/* ALERTS */}
      {error && <div style={{ background: '#fef2f2', color: '#dc2626', border: '1px solid #fecaca', padding: '12px 16px', borderRadius: '12px', marginBottom: '20px', fontSize: '13px', fontWeight: '700' }}>{error}</div>}
      {message && <div style={{ background: '#f0fdf4', color: '#16a34a', border: '1px solid #bbf7d0', padding: '12px 16px', borderRadius: '12px', marginBottom: '20px', fontSize: '13px', fontWeight: '700' }}>{message}</div>}

      {/* ═══════════════════════════════════════════════════════════════════
         VIEW MODE 1: LỊCH SỬ NHẬP HÀNG (HISTORY VIEW)
         ═══════════════════════════════════════════════════════════════════ */}
      {!isCreatingView && (
        <>
          {/* HEADER BAR */}
          <div style={{ background: '#ffffff', padding: '20px 28px', borderRadius: '20px', boxShadow: '0 2px 12px rgba(0,0,0,0.03)', border: '1px solid #f1f5f9', marginBottom: '24px', display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: '16px' }}>
            <h1 style={{ fontSize: '22px', fontWeight: '900', color: '#ea580c', margin: 0, display: 'flex', alignItems: 'center', gap: '10px' }}>
              <span>🛒</span> Lịch sử Nhập Hàng
            </h1>

            <button
              type="button"
              onClick={() => { setIsCreatingView(true); setError(''); setMessage(''); }}
              style={{ padding: '12px 24px', background: '#16a34a', color: '#ffffff', border: 'none', borderRadius: '14px', fontSize: '14px', fontWeight: '800', cursor: 'pointer', boxShadow: '0 4px 14px rgba(22,163,74,0.25)', display: 'flex', alignItems: 'center', gap: '6px' }}
            >
              + Tạo Phiếu Nhập Mới
            </button>
          </div>

          {/* SEARCH & FILTER BAR */}
          <form onSubmit={(e) => { e.preventDefault(); loadImports(0); }} style={{ background: '#ffffff', padding: '18px 24px', borderRadius: '20px', boxShadow: '0 2px 12px rgba(0,0,0,0.03)', border: '1px solid #f1f5f9', marginBottom: '24px', display: 'flex', gap: '14px', alignItems: 'center', flexWrap: 'wrap' }}>
            <div style={{ flex: '1', minWidth: '240px' }}>
              <input
                placeholder="🔍 Nhập mã phiếu (Ví dụ: PN01, PNB8CC2630)..."
                value={filters.code}
                onChange={(e) => setFilters((c) => ({ ...c, code: e.target.value }))}
                style={{ width: '100%', padding: '10px 16px', border: '1px solid #cbd5e1', borderRadius: '12px', fontSize: '13px', outline: 'none' }}
              />
            </div>

            <div style={{ flex: '1', minWidth: '220px' }}>
              <select
                value={filters.supplierId}
                onChange={(e) => setFilters((c) => ({ ...c, supplierId: e.target.value }))}
                style={{ width: '100%', padding: '10px 16px', border: '1px solid #cbd5e1', borderRadius: '12px', fontSize: '13px', outline: 'none' }}
              >
                <option value="">-- Tất cả NCC --</option>
                {suppliers.map((s) => (
                  <option key={s.id} value={s.id}>{s.name}</option>
                ))}
              </select>
            </div>

            <div style={{ display: 'flex', gap: '10px' }}>
              <button
                type="submit"
                style={{ padding: '10px 24px', background: '#d97706', color: '#ffffff', border: 'none', borderRadius: '12px', fontSize: '13px', fontWeight: '800', cursor: 'pointer' }}
              >
                Lọc
              </button>
              <button
                type="button"
                onClick={() => setFilters({ code: '', supplierId: '' })}
                style={{ padding: '10px 16px', background: '#f1f5f9', color: '#64748b', border: '1px solid #cbd5e1', borderRadius: '12px', fontSize: '13px', fontWeight: '700', cursor: 'pointer' }}
              >
                🔄
              </button>
            </div>
          </form>

          {/* TABLE OF IMPORT NOTES */}
          <div style={{ background: '#ffffff', borderRadius: '20px', boxShadow: '0 4px 20px rgba(0,0,0,0.03)', border: '1px solid #e2e8f0', overflow: 'hidden' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'center', fontSize: '14px' }}>
              <thead>
                <tr style={{ background: '#e2e8f0', color: '#1e293b', fontWeight: '900', fontSize: '12px', borderBottom: '2px solid #cbd5e1' }}>
                  <th style={{ padding: '16px 14px', width: '140px' }}>MÃ PHIẾU</th>
                  <th style={{ padding: '16px 14px', width: '140px' }}>TRẠNG THÁI</th>
                  <th style={{ padding: '16px 20px', textAlign: 'left' }}>TÊN NCC</th>
                  <th style={{ padding: '16px 14px', width: '160px' }}>NGÀY NHẬP</th>
                  <th style={{ padding: '16px 16px', textAlign: 'right', width: '160px' }}>TỔNG TIỀN</th>
                  <th style={{ padding: '16px 12px', width: '110px' }}>NGƯỜI TẠO</th>
                  <th style={{ padding: '16px 14px', width: '100px' }}>THAO TÁC</th>
                </tr>
              </thead>
              <tbody>
                {loading ? (
                  <tr>
                    <td colSpan="7" style={{ padding: '40px', color: '#64748b' }}>Đang tải lịch sử nhập hàng...</td>
                  </tr>
                ) : imports.length === 0 ? (
                  <tr>
                    <td colSpan="7" style={{ padding: '40px', color: '#94a3b8' }}>Chưa có phiếu nhập hàng nào.</td>
                  </tr>
                ) : (
                  imports.map((item, idx) => {
                    const noteCode = `PN${String(item.id).padStart(2, '0')}`;
                    const badge = getImportStatusBadge(item.status);

                    return (
                      <tr
                        key={item.id}
                        style={{
                          borderBottom: '1px solid #f1f5f9',
                          background: idx % 2 === 0 ? '#ffffff' : '#fafafa',
                        }}
                      >
                        <td style={{ padding: '16px 14px', fontWeight: '900', color: '#d97706' }}>
                          {noteCode}
                        </td>
                        <td style={{ padding: '16px 14px' }}>
                          <span style={{ background: badge.bg, color: badge.color, border: `1px solid ${badge.border}`, padding: '4px 10px', borderRadius: '12px', fontSize: '12px', fontWeight: '800', display: 'inline-block' }}>
                            {badge.label}
                          </span>
                        </td>
                        <td style={{ padding: '16px 20px', textAlign: 'left', fontWeight: '800', color: '#0f172a' }}>
                          {item.supplierName || '—'}
                        </td>
                        <td style={{ padding: '16px 14px', color: '#475569', fontSize: '13px' }}>
                          {item.createdAt ? formatDateTime(item.createdAt) : '—'}
                        </td>
                        <td style={{ padding: '16px 16px', textAlign: 'right', fontWeight: '900', color: '#dc2626', fontSize: '14px' }}>
                          {formatPrice(item.totalAmount || 0)}
                        </td>
                        <td style={{ padding: '16px 12px' }}>
                          <span style={{ background: '#f1f5f9', color: '#475569', padding: '4px 10px', borderRadius: '8px', fontSize: '12px', fontWeight: '800' }}>
                            {item.createdBy || 'TK10'}
                          </span>
                        </td>
                        <td style={{ padding: '16px 14px' }}>
                          <button
                            type="button"
                            onClick={() => openViewModal(item.id)}
                            title="Xem chi tiết phiếu nhập"
                            style={{
                              width: '36px',
                              height: '36px',
                              background: '#e0f2fe',
                              color: '#0284c7',
                              border: '1px solid #bae6fd',
                              borderRadius: '10px',
                              cursor: 'pointer',
                              display: 'inline-flex',
                              alignItems: 'center',
                              justify: 'center',
                              fontWeight: '900',
                            }}
                          >
                            👁️
                          </button>
                        </td>
                      </tr>
                    );
                  })
                )}
              </tbody>
            </table>
          </div>

          {/* PAGINATION */}
          {pageInfo.totalPages > 1 && (
            <div style={{ display: 'flex', justifyContent: 'center', gap: '8px', marginTop: '24px', alignItems: 'center' }}>
              <button
                type="button"
                disabled={pageInfo.number === 0}
                onClick={() => loadImports(pageInfo.number - 1)}
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
                onClick={() => loadImports(pageInfo.number + 1)}
                style={{ padding: '8px 16px', background: '#ffffff', border: '1px solid #cbd5e1', borderRadius: '10px', fontSize: '13px', fontWeight: '700', cursor: pageInfo.number >= pageInfo.totalPages - 1 ? 'not-allowed' : 'pointer' }}
              >
                Trang sau
              </button>
            </div>
          )}
        </>
      )}

      {/* ═══════════════════════════════════════════════════════════════════
         VIEW MODE 2: TẠO PHIẾU NHẬP HÀNG
         ═══════════════════════════════════════════════════════════════════ */}
      {isCreatingView && (
        <form onSubmit={handleSaveImportNote} style={{ background: '#ffffff', padding: '32px', borderRadius: '24px', boxShadow: '0 4px 20px rgba(0,0,0,0.03)', border: '1px solid #e2e8f0', maxWidth: '1050px', margin: '0 auto' }}>
          
          {/* HEADER BAR */}
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '28px', paddingBottom: '16px', borderBottom: '1px solid #f1f5f9' }}>
            <h1 style={{ fontSize: '22px', fontWeight: '900', color: '#ea580c', margin: 0, display: 'flex', alignItems: 'center', gap: '10px' }}>
              <span>🛒</span> Tạo Phiếu Nhập Hàng
            </h1>

            <button
              type="button"
              onClick={() => setIsCreatingView(false)}
              style={{ padding: '8px 18px', background: '#f1f5f9', color: '#475569', border: '1px solid #cbd5e1', borderRadius: '12px', fontSize: '13px', fontWeight: '800', cursor: 'pointer' }}
            >
              ← Hủy đơn
            </button>
          </div>

          {/* CARD 1: CHỌN NHÀ CUNG CẤP & LỌC DANH MỤC */}
          <div style={{ background: '#fafafa', border: '1px solid #e2e8f0', borderRadius: '16px', padding: '20px', marginBottom: '24px', display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '16px' }}>
            <div>
              <label style={{ fontSize: '14px', fontWeight: '800', color: '#0f172a', display: 'block', marginBottom: '8px' }}>
                Chọn Nhà Cung Cấp *
              </label>
              <select
                value={form.supplierId}
                onChange={(e) => {
                  const supId = e.target.value;
                  setForm((c) => ({ ...c, supplierId: supId, categoryId: '' }));
                  setItems([{ ...emptyItem }]);
                }}
                required
                style={{ width: '100%', padding: '12px 16px', border: '1px solid #3b82f6', borderRadius: '12px', fontSize: '14px', outline: 'none', background: '#ffffff', color: '#0f172a', fontWeight: '700' }}
              >
                <option value="">-- Chọn nhà cung cấp --</option>
                {suppliers.map((s) => (
                  <option key={s.id} value={s.id}>{s.name} ({s.phoneNumber || 'NCC'})</option>
                ))}
              </select>
            </div>

            <div>
              <label style={{ fontSize: '14px', fontWeight: '800', color: form.supplierId ? '#0f172a' : '#94a3b8', display: 'block', marginBottom: '8px' }}>
                Lọc theo Cây Danh Mục {!form.supplierId && '(Chọn NCC trước)'}
              </label>
              {form.supplierId ? (
                <CategoryTreeSelect
                  categoryTree={supplierCategoryTree}
                  selectedId={form.categoryId}
                  onSelect={(catId) => setForm((c) => ({ ...c, categoryId: catId }))}
                />
              ) : (
                <div style={{ width: '100%', padding: '12px 16px', background: '#f1f5f9', border: '1px solid #cbd5e1', borderRadius: '12px', fontSize: '13px', color: '#94a3b8', fontWeight: '600' }}>
                  -- Vui lòng chọn Nhà Cung Cấp trước --
                </div>
              )}
            </div>

            <div style={{ gridColumn: '1 / -1' }}>
              <span style={{ fontSize: '12px', color: '#64748b', fontWeight: '600', display: 'block' }}>
                💡 Chọn NCC để lọc danh sách sản phẩm tương ứng (có thể chọn thêm danh mục để thu hẹp danh sách sản phẩm).
              </span>
            </div>
          </div>

          {/* CARD 2: CHI TIẾT SẢN PHẨM NHẬP */}
          <div style={{ background: '#ffffff', border: '1px solid #e2e8f0', borderRadius: '16px', padding: '24px', marginBottom: '24px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
              <div style={{ fontSize: '14px', fontWeight: '900', color: '#166534', display: 'flex', alignItems: 'center', gap: '8px' }}>
                <span>📑</span> CHI TIẾT SẢN PHẨM NHẬP
              </div>

              {/* BUTTON ADD NEW PRODUCT FOR SUPPLIER */}
              {form.supplierId && (
                <button
                  type="button"
                  onClick={() => {
                    setTargetRowForProduct(items.length - 1);
                    setNewProductForm({ name: '', basePrice: '', categoryId: form.categoryId || '' });
                    setShowAddProductModal(true);
                  }}
                  style={{
                    padding: '6px 14px',
                    background: '#f0fdf4',
                    color: '#16a34a',
                    border: '1px solid #bbf7d0',
                    borderRadius: '10px',
                    fontSize: '12px',
                    fontWeight: '800',
                    cursor: 'pointer',
                    display: 'flex',
                    alignItems: 'center',
                    gap: '4px',
                  }}
                >
                  ➕ Thêm sản phẩm mới cho NCC
                </button>
              )}
            </div>

            {/* TABLE OF ITEMS */}
            <div style={{ overflowX: 'auto', marginBottom: '16px' }}>
              <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '13px' }}>
                <thead>
                  <tr style={{ background: '#f1f5f9', color: '#475569', fontWeight: '800', textAlign: 'left', borderBottom: '1px solid #cbd5e1' }}>
                    <th style={{ padding: '12px' }}>TÊN SẢN PHẨM</th>
                    <th style={{ padding: '12px', width: '250px' }}>CHỌN BIẾN THỂ (nếu có)</th>
                    <th style={{ padding: '12px', width: '110px', textAlign: 'center' }}>SỐ LƯỢNG</th>
                    <th style={{ padding: '12px', width: '170px' }}>GIÁ NHẬP (VNĐ)</th>
                    <th style={{ padding: '12px', width: '90px', textAlign: 'center' }}>THAO TÁC</th>
                  </tr>
                </thead>
                <tbody>
                  {items.map((item, idx) => {
                    const selectedProd = allProducts.find((p) => p.id === Number(item.productId));
                    const variants = selectedProd?.variants || [];

                    return (
                      <tr key={idx} style={{ borderBottom: '1px solid #f1f5f9' }}>
                        {/* PRODUCT DROPDOWN + '+' NEW PRODUCT BUTTON */}
                        <td style={{ padding: '10px' }}>
                          <div style={{ display: 'flex', gap: '6px', alignItems: 'center' }}>
                            <select
                              value={item.productId}
                              disabled={!form.supplierId}
                              onChange={(e) => handleProductSelect(idx, e.target.value)}
                              style={{
                                flex: 1,
                                padding: '10px 12px',
                                border: form.supplierId ? '1px solid #cbd5e1' : '1px solid #e2e8f0',
                                borderRadius: '10px',
                                fontSize: '13px',
                                background: form.supplierId ? '#fff' : '#f8fafc',
                                color: form.supplierId ? '#0f172a' : '#94a3b8',
                                fontWeight: item.productId ? '700' : '400',
                              }}
                            >
                              {!form.supplierId ? (
                                <option value="" disabled>
                                  -- Vui lòng chọn Nhà Cung Cấp trước --
                                </option>
                              ) : availableProducts.length === 0 ? (
                                <option value="" disabled>
                                  -- Không có sản phẩm phù hợp với NCC và danh mục --
                                </option>
                              ) : (
                                <>
                                  <option value="">-- Chọn sản phẩm --</option>
                                  {availableProducts.map((p) => (
                                    <option key={p.id} value={p.id}>
                                      {p.name} (Kho: {p.defaultVariantStockQuantity ?? p.stockQuantity ?? 0})
                                    </option>
                                  ))}
                                </>
                              )}
                            </select>

                            {/* + Button to Add New Product On-The-Fly */}
                            {form.supplierId && (
                              <button
                                type="button"
                                title="Thêm sản phẩm mới khác trong kho cho NCC này"
                                onClick={() => {
                                  setTargetRowForProduct(idx);
                                  setNewProductForm({ name: '', basePrice: '', categoryId: form.categoryId || '' });
                                  setShowAddProductModal(true);
                                }}
                                style={{
                                  width: '34px',
                                  height: '34px',
                                  background: '#16a34a',
                                  color: '#ffffff',
                                  border: 'none',
                                  borderRadius: '8px',
                                  fontSize: '16px',
                                  fontWeight: '900',
                                  cursor: 'pointer',
                                  display: 'inline-flex',
                                  alignItems: 'center',
                                  justify: 'center',
                                  flexShrink: 0,
                                }}
                              >
                                +
                              </button>
                            )}
                          </div>
                        </td>

                        {/* VARIANT DROPDOWN + '+' NEW VARIANT BUTTON */}
                        <td style={{ padding: '10px' }}>
                          {!item.productId ? (
                            <span style={{ fontSize: '12px', color: '#94a3b8', fontStyle: 'italic' }}>
                              -- Chọn sản phẩm trước --
                            </span>
                          ) : (
                            <div style={{ display: 'flex', gap: '6px', alignItems: 'center' }}>
                              {variants.length > 1 ? (
                                <select
                                  value={item.variantId}
                                  onChange={(e) => updateItem(idx, { variantId: e.target.value })}
                                  style={{
                                    flex: 1,
                                    padding: '10px 12px',
                                    border: '1px solid #ea580c',
                                    borderRadius: '10px',
                                    fontSize: '13px',
                                    background: '#fff',
                                    color: item.variantId ? '#0f172a' : '#ea580c',
                                    fontWeight: '800',
                                  }}
                                >
                                  <option value="">-- Chọn biến thể --</option>
                                  {variants.map((v) => (
                                    <option key={v.id} value={v.id}>
                                      {v.skuCode || v.title || `Biến thể ${v.id}`} (Tồn: {v.stockQuantity ?? 0})
                                    </option>
                                  ))}
                                </select>
                              ) : (
                                <span style={{ flex: 1, fontSize: '12px', color: '#16a34a', fontWeight: '800', background: '#f0fdf4', border: '1px solid #bbf7d0', padding: '6px 10px', borderRadius: '8px' }}>
                                  ✓ Mặc định (1 biến thể)
                                </span>
                              )}

                              {/* + Button to Add New Variant On-The-Fly */}
                              <button
                                type="button"
                                title="Thêm biến thể mới cho sản phẩm này"
                                onClick={() => {
                                  setTargetRowForVariant(idx);
                                  setTargetProductForVariant(selectedProd);
                                  setNewVariantForm({ title: '', price: selectedProd?.basePrice || '' });
                                  setShowAddVariantModal(true);
                                }}
                                style={{
                                  width: '32px',
                                  height: '32px',
                                  background: '#ea580c',
                                  color: '#ffffff',
                                  border: 'none',
                                  borderRadius: '8px',
                                  fontSize: '15px',
                                  fontWeight: '900',
                                  cursor: 'pointer',
                                  display: 'inline-flex',
                                  alignItems: 'center',
                                  justify: 'center',
                                  flexShrink: 0,
                                }}
                              >
                                +
                              </button>
                            </div>
                          )}
                        </td>

                        {/* SỐ LƯỢNG */}
                        <td style={{ padding: '10px', textAlign: 'center' }}>
                          <input
                            type="number"
                            min="1"
                            value={item.quantity}
                            onChange={(e) => updateItem(idx, { quantity: e.target.value })}
                            style={{ width: '80px', padding: '10px', border: '1px solid #cbd5e1', borderRadius: '10px', fontSize: '13px', textAlign: 'center', fontWeight: '800' }}
                          />
                        </td>

                        {/* GIÁ NHẬP */}
                        <td style={{ padding: '10px' }}>
                          <input
                            type="number"
                            min="0"
                            value={item.importPrice}
                            onChange={(e) => updateItem(idx, { importPrice: e.target.value })}
                            style={{ width: '100%', padding: '10px', border: '1px solid #cbd5e1', borderRadius: '10px', fontSize: '13px', fontWeight: '800', textAlign: 'right' }}
                          />
                        </td>

                        {/* THAO TÁC (🗑️) */}
                        <td style={{ padding: '10px', textAlign: 'center' }}>
                          <button
                            type="button"
                            onClick={() => removeItemRow(idx)}
                            disabled={items.length === 1}
                            style={{ border: 'none', background: '#fef2f2', color: '#dc2626', width: '32px', height: '32px', borderRadius: '8px', cursor: items.length === 1 ? 'not-allowed' : 'pointer', fontWeight: '800' }}
                          >
                            🗑️
                          </button>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>

            {/* BUTTON + THÊM SẢN PHẨM ROW */}
            <button
              type="button"
              onClick={addItemRow}
              style={{ padding: '8px 18px', background: '#ffffff', color: '#d97706', border: '1px solid #d97706', borderRadius: '10px', fontSize: '13px', fontWeight: '800', cursor: 'pointer' }}
            >
              + Thêm Dòng Nhập
            </button>
          </div>

          {/* NOTE INPUT */}
          <div style={{ marginBottom: '24px' }}>
            <label style={{ fontSize: '13px', fontWeight: '800', color: '#475569', display: 'block', marginBottom: '6px' }}>Ghi chú phiếu nhập</label>
            <input
              placeholder="Nhập ghi chú cho đơn nhập hàng..."
              value={form.note}
              onChange={(e) => setForm((c) => ({ ...c, note: e.target.value }))}
              style={{ width: '100%', padding: '12px 16px', border: '1px solid #cbd5e1', borderRadius: '12px', fontSize: '13px' }}
            />
          </div>

          {/* BOTTOM TOTAL AMOUNT & SAVE BUTTON */}
          <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end', gap: '16px', borderTop: '1px solid #f1f5f9', paddingTop: '20px' }}>
            <div style={{ textAlign: 'right' }}>
              <span style={{ fontSize: '13px', fontWeight: '800', color: '#475569', display: 'block' }}>TỔNG TIỀN</span>
              <span style={{ fontSize: '26px', fontWeight: '900', color: '#dc2626' }}>
                {formatPrice(calculatedTotalAmount)}
              </span>
            </div>

            <button
              type="submit"
              disabled={submitting}
              style={{ padding: '14px 36px', background: '#16a34a', color: '#ffffff', border: 'none', borderRadius: '14px', fontSize: '15px', fontWeight: '900', cursor: 'pointer', boxShadow: '0 4px 16px rgba(22,163,74,0.3)' }}
            >
              {submitting ? 'Đang lưu phiếu...' : '💾 Lưu Phiếu'}
            </button>
          </div>

        </form>
      )}

      {/* ═══════════════════════════════════════════════════════════════════
         MODAL 1: THÊM SẢN PHẨM MỚI CHO NHÀ CUNG CẤP (ON-THE-FLY)
         ═══════════════════════════════════════════════════════════════════ */}
      {showAddProductModal && (
        <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, background: 'rgba(0,0,0,0.5)', zIndex: 1100, display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '20px' }}>
          <form onSubmit={handleCreateNewProduct} style={{ background: '#ffffff', borderRadius: '24px', padding: '28px', width: '100%', maxWidth: '520px', boxShadow: '0 20px 50px rgba(0,0,0,0.2)' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px', paddingBottom: '12px', borderBottom: '1px solid #f1f5f9' }}>
              <h3 style={{ fontSize: '18px', fontWeight: '900', color: '#16a34a', margin: 0, display: 'flex', alignItems: 'center', gap: '8px' }}>
                <span>➕</span> Thêm Sản Phẩm Mới Cho NCC
              </h3>
              <button type="button" onClick={() => setShowAddProductModal(false)} style={{ border: 'none', background: '#f1f5f9', borderRadius: '50%', width: '32px', height: '32px', cursor: 'pointer', fontWeight: '900' }}>×</button>
            </div>

            <div style={{ marginBottom: '16px' }}>
              <label style={{ fontSize: '13px', fontWeight: '800', color: '#0f172a', display: 'block', marginBottom: '6px' }}>Tên sản phẩm mới *</label>
              <input
                type="text"
                required
                placeholder="Nhập tên sản phẩm mới (ví dụ: Lego Xe Đua F1)..."
                value={newProductForm.name}
                onChange={(e) => setNewProductForm((c) => ({ ...c, name: e.target.value }))}
                style={{ width: '100%', padding: '10px 14px', border: '1px solid #cbd5e1', borderRadius: '10px', fontSize: '13px' }}
              />
            </div>

            <div style={{ marginBottom: '16px' }}>
              <label style={{ fontSize: '13px', fontWeight: '800', color: '#0f172a', display: 'block', marginBottom: '6px' }}>Giá bán bán lẻ (VNĐ) *</label>
              <input
                type="number"
                min="0"
                required
                placeholder="Nhập giá niêm yết bán lẻ..."
                value={newProductForm.basePrice}
                onChange={(e) => setNewProductForm((c) => ({ ...c, basePrice: e.target.value }))}
                style={{ width: '100%', padding: '10px 14px', border: '1px solid #cbd5e1', borderRadius: '10px', fontSize: '13px' }}
              />
            </div>

            <div style={{ marginBottom: '20px' }}>
              <label style={{ fontSize: '13px', fontWeight: '800', color: '#0f172a', display: 'block', marginBottom: '6px' }}>Danh mục sản phẩm</label>
              <CategoryTreeSelect
                categoryTree={categoryTree}
                selectedId={newProductForm.categoryId}
                onSelect={(catId) => setNewProductForm((c) => ({ ...c, categoryId: catId }))}
              />
            </div>

            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px', borderTop: '1px solid #f1f5f9', paddingTop: '16px' }}>
              <button type="button" onClick={() => setShowAddProductModal(false)} style={{ padding: '9px 18px', background: '#f1f5f9', color: '#475569', border: '1px solid #cbd5e1', borderRadius: '10px', fontWeight: '700', cursor: 'pointer' }}>
                Hủy
              </button>
              <button type="submit" disabled={modalSubmitting} style={{ padding: '9px 24px', background: '#16a34a', color: '#ffffff', border: 'none', borderRadius: '10px', fontWeight: '800', cursor: 'pointer' }}>
                {modalSubmitting ? 'Đang tạo...' : 'Lưu Sản Phẩm'}
              </button>
            </div>
          </form>
        </div>
      )}

      {/* ═══════════════════════════════════════════════════════════════════
         MODAL 2: THÊM BIẾN THỂ MỚI CHO SẢN PHẨM (ON-THE-FLY)
         ═══════════════════════════════════════════════════════════════════ */}
      {showAddVariantModal && (
        <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, background: 'rgba(0,0,0,0.5)', zIndex: 1100, display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '20px' }}>
          <form onSubmit={handleCreateNewVariant} style={{ background: '#ffffff', borderRadius: '24px', padding: '28px', width: '100%', maxWidth: '500px', boxShadow: '0 20px 50px rgba(0,0,0,0.2)' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px', paddingBottom: '12px', borderBottom: '1px solid #f1f5f9' }}>
              <h3 style={{ fontSize: '18px', fontWeight: '900', color: '#ea580c', margin: 0, display: 'flex', alignItems: 'center', gap: '8px' }}>
                <span>➕</span> Thêm Biến Thể Mới
              </h3>
              <button type="button" onClick={() => setShowAddVariantModal(false)} style={{ border: 'none', background: '#f1f5f9', borderRadius: '50%', width: '32px', height: '32px', cursor: 'pointer', fontWeight: '900' }}>×</button>
            </div>

            <div style={{ background: '#fff7ed', border: '1px solid #fed7aa', padding: '10px 14px', borderRadius: '10px', fontSize: '13px', color: '#c2410c', fontWeight: '800', marginBottom: '16px' }}>
              Sản phẩm: {targetProductForVariant?.name}
            </div>

            <div style={{ marginBottom: '16px' }}>
              <label style={{ fontSize: '13px', fontWeight: '800', color: '#0f172a', display: 'block', marginBottom: '6px' }}>Tên biến thể / Mô tả *</label>
              <input
                type="text"
                required
                placeholder="Nhập tên biến thể (ví dụ: Màu Đỏ - Size XL)..."
                value={newVariantForm.title}
                onChange={(e) => setNewVariantForm((c) => ({ ...c, title: e.target.value }))}
                style={{ width: '100%', padding: '10px 14px', border: '1px solid #cbd5e1', borderRadius: '10px', fontSize: '13px' }}
              />
            </div>

            <div style={{ marginBottom: '20px' }}>
              <label style={{ fontSize: '13px', fontWeight: '800', color: '#0f172a', display: 'block', marginBottom: '6px' }}>Giá bán biến thể (VNĐ)</label>
              <input
                type="number"
                min="0"
                placeholder="Nhập giá bán lẻ cho biến thể này..."
                value={newVariantForm.price}
                onChange={(e) => setNewVariantForm((c) => ({ ...c, price: e.target.value }))}
                style={{ width: '100%', padding: '10px 14px', border: '1px solid #cbd5e1', borderRadius: '10px', fontSize: '13px' }}
              />
            </div>

            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px', borderTop: '1px solid #f1f5f9', paddingTop: '16px' }}>
              <button type="button" onClick={() => setShowAddVariantModal(false)} style={{ padding: '9px 18px', background: '#f1f5f9', color: '#475569', border: '1px solid #cbd5e1', borderRadius: '10px', fontWeight: '700', cursor: 'pointer' }}>
                Hủy
              </button>
              <button type="submit" disabled={modalSubmitting} style={{ padding: '9px 24px', background: '#ea580c', color: '#ffffff', border: 'none', borderRadius: '10px', fontWeight: '800', cursor: 'pointer' }}>
                {modalSubmitting ? 'Đang thêm...' : 'Lưu Biến Thể'}
              </button>
            </div>
          </form>
        </div>
      )}

      {/* ═══════════════════════════════════════════════════════════════════
         MODAL 3: CHI TIẾT PHIẾU NHẬP (VIEW DETAIL MODAL)
         ═══════════════════════════════════════════════════════════════════ */}
      {viewNote && (
        <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, background: 'rgba(0,0,0,0.5)', zIndex: 1000, display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '20px' }}>
          <div style={{ background: '#ffffff', borderRadius: '24px', padding: '32px', width: '100%', maxWidth: '840px', maxHeight: '90vh', overflowY: 'auto', boxShadow: '0 20px 50px rgba(0,0,0,0.2)' }}>
            
            {/* Header */}
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px', paddingBottom: '12px', borderBottom: '1px solid #f1f5f9' }}>
              <h2 style={{ fontSize: '20px', fontWeight: '900', color: '#0f172a', margin: 0 }}>
                Chi Tiết Phiếu Nhập - PN{String(viewNote.id).padStart(2, '0')}
              </h2>
              <button type="button" onClick={() => setViewNote(null)} style={{ border: 'none', background: '#f1f5f9', borderRadius: '50%', width: '32px', height: '32px', cursor: 'pointer', fontWeight: '900' }}>×</button>
            </div>

            {loadingDetails ? (
              <div style={{ padding: '40px', textAlign: 'center', color: '#64748b' }}>Đang tải chi tiết phiếu nhập...</div>
            ) : (
              <>
                {/* INFO GRID */}
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '16px', background: '#fafafa', border: '1px solid #e2e8f0', borderRadius: '16px', padding: '20px', marginBottom: '24px' }}>
                  <div>
                    <span style={{ fontSize: '12px', color: '#64748b', fontWeight: '800' }}>Mã phiếu nhập:</span>
                    <div style={{ fontSize: '15px', fontWeight: '900', color: '#d97706', marginTop: '2px' }}>
                      PN{String(viewNote.id).padStart(2, '0')}
                    </div>
                  </div>

                  <div>
                    <span style={{ fontSize: '12px', color: '#64748b', fontWeight: '800' }}>Nhà cung cấp:</span>
                    <div style={{ fontSize: '14px', fontWeight: '800', color: '#0f172a', marginTop: '2px' }}>
                      {viewNote.supplierName || '—'}
                    </div>
                    <div style={{ fontSize: '12px', color: '#0284c7', fontWeight: '700' }}>
                      📞 {viewNote.supplierPhoneNumber || '—'}
                    </div>
                  </div>

                  <div>
                    <span style={{ fontSize: '12px', color: '#64748b', fontWeight: '800' }}>Ngày nhập:</span>
                    <div style={{ fontSize: '13px', fontWeight: '700', color: '#334155', marginTop: '2px' }}>
                      {viewNote.createdAt ? formatDateTime(viewNote.createdAt) : '—'}
                    </div>
                  </div>

                  <div>
                    <span style={{ fontSize: '12px', color: '#64748b', fontWeight: '800' }}>Trạng thái:</span>
                    <div style={{ marginTop: '4px' }}>
                      {(() => {
                        const badge = getImportStatusBadge(viewNote.status);
                        return (
                          <span style={{ background: badge.bg, color: badge.color, border: `1px solid ${badge.border}`, padding: '4px 10px', borderRadius: '10px', fontSize: '12px', fontWeight: '800' }}>
                            {badge.label}
                          </span>
                        );
                      })()}
                    </div>
                  </div>
                </div>

                {viewNote.note && (
                  <div style={{ background: '#fff7ed', border: '1px solid #fed7aa', padding: '12px 16px', borderRadius: '12px', fontSize: '13px', color: '#ea580c', marginBottom: '20px' }}>
                    <strong>Ghi chú:</strong> {viewNote.note}
                  </div>
                )}

                {/* ITEMS TABLE */}
                <h3 style={{ fontSize: '15px', fontWeight: '900', color: '#0f172a', marginBottom: '12px' }}>
                  Danh sách sản phẩm nhập
                </h3>
                <div style={{ overflowX: 'auto', marginBottom: '24px' }}>
                  <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '13px' }}>
                    <thead>
                      <tr style={{ background: '#f8fafc', color: '#475569', fontWeight: '800', borderBottom: '1px solid #e2e8f0', textAlign: 'left' }}>
                        <th style={{ padding: '12px' }}>SẢN PHẨM</th>
                        <th style={{ padding: '12px', textAlign: 'center' }}>SỐ LƯỢNG</th>
                        <th style={{ padding: '12px', textAlign: 'right' }}>GIÁ NHẬP</th>
                        <th style={{ padding: '12px', textAlign: 'right' }}>THÀNH TIỀN</th>
                      </tr>
                    </thead>
                    <tbody>
                      {(viewNote.items || []).map((item) => (
                        <tr key={item.id} style={{ borderBottom: '1px solid #f1f5f9' }}>
                          <td style={{ padding: '12px', fontWeight: '800', color: '#0f172a' }}>
                            {item.productName}
                          </td>
                          <td style={{ padding: '12px', textAlign: 'center', fontWeight: '800' }}>
                            {item.quantity}
                          </td>
                          <td style={{ padding: '12px', textAlign: 'right', fontWeight: '700' }}>
                            {formatPrice(item.importPrice)}
                          </td>
                          <td style={{ padding: '12px', textAlign: 'right', fontWeight: '900', color: '#dc2626' }}>
                            {formatPrice(item.totalPrice || item.quantity * item.importPrice)}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>

                {/* TOTAL AMOUNT & ACTIONS */}
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderTop: '1px solid #f1f5f9', paddingTop: '16px' }}>
                  <div style={{ display: 'flex', gap: '10px' }}>
                    {String(viewNote.status?.name || viewNote.status || '').toUpperCase() === 'PENDING' && (
                      <>
                        <button
                          type="button"
                          onClick={() => handleActionOnNote(completeImportNote, 'Đã hoàn tất phiếu nhập hàng!')}
                          style={{ padding: '10px 20px', background: '#16a34a', color: '#fff', border: 'none', borderRadius: '12px', fontWeight: '800', cursor: 'pointer' }}
                        >
                          ✓ Hoàn thành phiếu
                        </button>
                        <button
                          type="button"
                          onClick={() => handleActionOnNote(cancelImportNote, 'Đã hủy phiếu nhập hàng!')}
                          style={{ padding: '10px 20px', background: '#dc2626', color: '#fff', border: 'none', borderRadius: '12px', fontWeight: '800', cursor: 'pointer' }}
                        >
                          ✕ Hủy phiếu
                        </button>
                      </>
                    )}
                  </div>

                  <div style={{ textAlign: 'right' }}>
                    <span style={{ fontSize: '13px', color: '#64748b', fontWeight: '700' }}>TỔNG TIỀN PHIẾU:</span>
                    <div style={{ fontSize: '22px', fontWeight: '900', color: '#dc2626' }}>
                      {formatPrice(viewNote.totalAmount || 0)}
                    </div>
                  </div>
                </div>

              </>
            )}

            {/* CLOSE BUTTON */}
            <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: '20px' }}>
              <button type="button" onClick={() => setViewNote(null)} style={{ padding: '10px 24px', background: '#0284c7', color: '#fff', border: 'none', borderRadius: '12px', fontWeight: '800', cursor: 'pointer' }}>
                Đóng
              </button>
            </div>

          </div>
        </div>
      )}

    </section>
  );
}

export default AdminImportPage;
