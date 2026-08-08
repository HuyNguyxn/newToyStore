import { useEffect, useState } from 'react';
import { useOutletContext } from 'react-router-dom';
import { getAdminProducts } from '../../services/adminProductService.js';
import {
  changeSupplierStatus,
  createSupplier,
  deleteSupplier,
  getSuppliers,
  restoreSupplier,
  updateSupplier,
} from '../../services/adminSupplierService.js';
import { formatPrice } from '../../utils/formatters.js';

/* Helper function to parse supplier status safely without React object crashes */
function getSupplierStatusInfo(supplier) {
  if (!supplier) {
    return { code: 'ACTIVE', label: 'Đang hoạt động', bg: '#f0fdf4', color: '#16a34a', border: '#bbf7d0' };
  }

  const rawStatus = typeof supplier.status === 'object' 
    ? (supplier.status?.name || supplier.status?.code || '') 
    : supplier.status;
  
  const statusStr = String(rawStatus || '').toUpperCase();
  const displayName = typeof supplier.status === 'object' 
    ? supplier.status?.displayName 
    : supplier.statusDisplayName;

  if (statusStr === 'ACTIVE' || statusStr === 'OPEN' || displayName === 'Đang hoạt động') {
    return {
      code: 'ACTIVE',
      label: displayName || 'Đang hoạt động',
      bg: '#f0fdf4',
      color: '#16a34a',
      border: '#bbf7d0',
    };
  }

  if (statusStr === 'INACTIVE' || displayName === 'Ngừng hoạt động') {
    return {
      code: 'INACTIVE',
      label: displayName || 'Ngừng hoạt động',
      bg: '#fef2f2',
      color: '#dc2626',
      border: '#fecaca',
    };
  }

  return {
    code: statusStr || 'ACTIVE',
    label: displayName || String(rawStatus || 'Đang hoạt động'),
    bg: '#f1f5f9',
    color: '#475569',
    border: '#cbd5e1',
  };
}

function AdminSupplierPage() {
  const { userRole } = useOutletContext();
  const canDelete = userRole === 'ADMIN';
  const [suppliers, setSuppliers] = useState([]);
  const [pageInfo, setPageInfo] = useState({ number: 0, totalPages: 1, totalElements: 0 });

  // Filters state
  const [filters, setFilters] = useState({ name: '', phoneNumber: '', status: '' });

  // Modal states
  const [viewSupplier, setViewSupplier] = useState(null);
  const [supplierProducts, setSupplierProducts] = useState([]);
  const [loadingProducts, setLoadingProducts] = useState(false);

  const [editSupplier, setEditSupplier] = useState(null);
  const [isCreating, setIsCreating] = useState(false);

  // Form state
  const [form, setForm] = useState({
    id: '',
    name: '',
    phoneNumber: '',
    email: '',
    address: '',
    status: 'ACTIVE',
  });

  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    loadSuppliers(0);
  }, [filters]);

  useEffect(() => {
    if (viewSupplier?.id) {
      setLoadingProducts(true);
      getAdminProducts({ page: 0, size: 100 })
        .then((res) => {
          const list = res?.content || res || [];
          const matched = list.filter(
            (p) => String(p.supplierId) === String(viewSupplier.id) || p.supplierName === viewSupplier.name
          );
          setSupplierProducts(matched);
        })
        .catch(() => setSupplierProducts([]))
        .finally(() => setLoadingProducts(false));
    } else {
      setSupplierProducts([]);
    }
  }, [viewSupplier]);

  function normalizePage(result) {
    if (Array.isArray(result)) {
      return { content: result, number: 0, totalPages: 1, totalElements: result.length };
    }
    return {
      content: result?.content || [],
      number: result?.number || 0,
      totalPages: result?.totalPages || 1,
      totalElements: result?.totalElements || result?.content?.length || 0,
    };
  }

  async function loadSuppliers(page = pageInfo.number) {
    setLoading(true);
    setError('');
    try {
      const queryParams = {
        page,
        size: 10,
        sort: 'createdAt,desc',
      };
      if (filters.name.trim()) queryParams.name = filters.name.trim();
      if (filters.phoneNumber.trim()) queryParams.phoneNumber = filters.phoneNumber.trim();
      if (filters.status) queryParams.status = filters.status;

      const result = await getSuppliers(queryParams);
      const next = normalizePage(result);
      setSuppliers(next.content);
      setPageInfo({ number: next.number, totalPages: next.totalPages, totalElements: next.totalElements });
    } catch (err) {
      setError(err?.message || 'Không thể tải danh sách nhà cung cấp.');
      setSuppliers([]);
    } finally {
      setLoading(false);
    }
  }

  function openCreateModal() {
    setForm({ id: '', name: '', phoneNumber: '', email: '', address: '', status: 'ACTIVE' });
    setIsCreating(true);
    setEditSupplier(null);
  }

  function openEditModal(supplier) {
    const rawStatus = typeof supplier.status === 'object' ? supplier.status?.name || supplier.status?.code : supplier.status;
    setForm({
      id: supplier.id || '',
      name: supplier.name || '',
      phoneNumber: supplier.phoneNumber || '',
      email: supplier.email || '',
      address: supplier.address || '',
      status: String(rawStatus || 'ACTIVE').toUpperCase(),
    });
    setEditSupplier(supplier);
    setIsCreating(false);
  }

  async function handleSubmitForm(e) {
    e.preventDefault();
    setSubmitting(true);
    setError('');
    setMessage('');

    try {
      const payload = {
        name: form.name.trim(),
        phoneNumber: form.phoneNumber.trim(),
        email: form.email ? form.email.trim() : null,
        address: form.address ? form.address.trim() : null,
      };

      if (form.id) {
        await updateSupplier(form.id, payload);
        setMessage('Đã cập nhật nhà cung cấp thành công.');
      } else {
        await createSupplier(payload);
        setMessage('Đã thêm nhà cung cấp mới.');
      }

      setEditSupplier(null);
      setIsCreating(false);
      loadSuppliers(pageInfo.number);
    } catch (err) {
      setError(err?.message || 'Lưu nhà cung cấp thất bại.');
    } finally {
      setSubmitting(false);
    }
  }

  async function handleDeleteSupplier(id) {
    if (!window.confirm('Bạn có chắc chắn muốn xóa nhà cung cấp này không?')) return;
    setSubmitting(true);
    setError('');
    setMessage('');
    try {
      await deleteSupplier(id);
      setMessage('Đã xóa nhà cung cấp thành công.');
      loadSuppliers(pageInfo.number);
    } catch (err) {
      setError(err?.message || 'Xóa nhà cung cấp thất bại.');
    } finally {
      setSubmitting(false);
    }
  }

  function handleResetFilters() {
    setFilters({ name: '', phoneNumber: '', status: '' });
  }

  return (
    <section style={{ padding: '24px', background: '#f8fafc', minHeight: '100vh', fontFamily: 'system-ui, -apple-system, sans-serif' }}>
      
      {/* PAGE HEADER BAR */}
      <div style={{ background: '#ffffff', padding: '20px 28px', borderRadius: '20px', boxShadow: '0 2px 12px rgba(0,0,0,0.03)', border: '1px solid #f1f5f9', marginBottom: '24px', display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: '16px' }}>
        <h1 style={{ fontSize: '22px', fontWeight: '900', color: '#0f172a', margin: 0, textTransform: 'uppercase', letterSpacing: '-0.3px' }}>
          Quản Lý Nhà Cung Cấp
        </h1>

        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
          <span style={{ fontSize: '13px', background: '#fff7ed', border: '1px solid #fed7aa', color: '#ea580c', fontWeight: '800', padding: '6px 16px', borderRadius: '20px' }}>
            Tổng nhà cung cấp: {pageInfo.totalElements}
          </span>
          <button
            type="button"
            onClick={openCreateModal}
            style={{ padding: '10px 20px', background: '#2563eb', color: '#ffffff', border: 'none', borderRadius: '12px', fontSize: '13px', fontWeight: '800', cursor: 'pointer', boxShadow: '0 4px 12px rgba(37,99,235,0.25)' }}
          >
            + Thêm Nhà Cung Cấp
          </button>
        </div>
      </div>

      {/* FILTER PANEL */}
      <form onSubmit={(e) => { e.preventDefault(); loadSuppliers(0); }} style={{ background: '#ffffff', padding: '20px 24px', borderRadius: '20px', boxShadow: '0 2px 12px rgba(0,0,0,0.03)', border: '1px solid #f1f5f9', marginBottom: '24px', display: 'flex', gap: '16px', alignItems: 'center', flexWrap: 'wrap' }}>
        <div style={{ flex: '1', minWidth: '220px' }}>
          <label style={{ fontSize: '12px', fontWeight: '800', color: '#475569', display: 'block', marginBottom: '6px' }}>Tên nhà cung cấp</label>
          <input
            placeholder="Nhập tên nhà cung cấp..."
            value={filters.name}
            onChange={(e) => setFilters((c) => ({ ...c, name: e.target.value }))}
            style={{ width: '100%', padding: '10px 14px', border: '1px solid #cbd5e1', borderRadius: '12px', fontSize: '13px', outline: 'none', background: '#fff' }}
          />
        </div>

        <div style={{ flex: '1', minWidth: '180px' }}>
          <label style={{ fontSize: '12px', fontWeight: '800', color: '#475569', display: 'block', marginBottom: '6px' }}>Số điện thoại</label>
          <input
            placeholder="Nhập số điện thoại..."
            value={filters.phoneNumber}
            onChange={(e) => setFilters((c) => ({ ...c, phoneNumber: e.target.value }))}
            style={{ width: '100%', padding: '10px 14px', border: '1px solid #cbd5e1', borderRadius: '12px', fontSize: '13px', outline: 'none', background: '#fff' }}
          />
        </div>

        <div style={{ flex: '1', minWidth: '180px' }}>
          <label style={{ fontSize: '12px', fontWeight: '800', color: '#475569', display: 'block', marginBottom: '6px' }}>Trạng thái</label>
          <select
            value={filters.status}
            onChange={(e) => setFilters((c) => ({ ...c, status: e.target.value }))}
            style={{ width: '100%', padding: '10px 14px', border: '1px solid #cbd5e1', borderRadius: '12px', fontSize: '13px', outline: 'none', background: '#fff' }}
          >
            <option value="">Tất cả trạng thái</option>
            <option value="ACTIVE">Đang hoạt động</option>
            <option value="INACTIVE">Ngừng hoạt động</option>
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

      {/* SUPPLIER TABLE */}
      <div style={{ background: '#ffffff', borderRadius: '20px', boxShadow: '0 4px 20px rgba(0,0,0,0.03)', border: '1px solid #e2e8f0', overflow: 'hidden' }}>
        <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'center', fontSize: '14px' }}>
          <thead>
            <tr style={{ background: '#e2e8f0', color: '#1e293b', fontWeight: '900', fontSize: '13px', borderBottom: '2px solid #cbd5e1' }}>
              <th style={{ padding: '16px 12px', width: '90px' }}>MÃ NCC</th>
              <th style={{ padding: '16px 20px', textAlign: 'left' }}>TÊN NHÀ CUNG CẤP</th>
              <th style={{ padding: '16px 14px', width: '140px' }}>SỐ ĐIỆN THOẠI</th>
              <th style={{ padding: '16px 14px', textAlign: 'left' }}>EMAIL</th>
              <th style={{ padding: '16px 14px', textAlign: 'left' }}>ĐỊA CHỈ</th>
              <th style={{ padding: '16px 14px', width: '140px' }}>TRẠNG THÁI</th>
              <th style={{ padding: '16px 16px', width: '140px' }}>THAO TÁC</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr>
                <td colSpan="7" style={{ padding: '40px', color: '#64748b' }}>Đang tải danh sách nhà cung cấp...</td>
              </tr>
            ) : suppliers.length === 0 ? (
              <tr>
                <td colSpan="7" style={{ padding: '40px', color: '#94a3b8' }}>Chưa có nhà cung cấp nào.</td>
              </tr>
            ) : (
              suppliers.map((sup, idx) => {
                const supCode = `NCC${String(sup.id).padStart(2, '0')}`;
                const statusInfo = getSupplierStatusInfo(sup);

                return (
                  <tr
                    key={sup.id}
                    style={{
                      borderBottom: '1px solid #f1f5f9',
                      background: idx % 2 === 0 ? '#ffffff' : '#fafafa',
                      transition: 'background 0.15s ease',
                    }}
                  >
                    {/* MÃ NCC */}
                    <td style={{ padding: '16px 12px', fontWeight: '800', color: '#475569' }}>
                      {supCode}
                    </td>

                    {/* TÊN NCC */}
                    <td style={{ padding: '16px 20px', textAlign: 'left', fontWeight: '700', color: '#0f172a' }}>
                      {sup.name}
                    </td>

                    {/* SĐT */}
                    <td style={{ padding: '16px 14px', fontWeight: '700', color: '#0284c7' }}>
                      {sup.phoneNumber || '—'}
                    </td>

                    {/* EMAIL */}
                    <td style={{ padding: '16px 14px', textAlign: 'left', color: '#475569' }}>
                      {sup.email || '—'}
                    </td>

                    {/* ĐỊA CHỈ */}
                    <td style={{ padding: '16px 14px', textAlign: 'left', color: '#334155', maxWidth: '220px', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                      {sup.address || '—'}
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

                    {/* THAO TÁC */}
                    <td style={{ padding: '16px' }}>
                      <div style={{ display: 'flex', justifyContent: 'center', gap: '8px' }}>
                        {/* Nút Xem (Xanh Dương) */}
                        <button
                          type="button"
                          onClick={() => setViewSupplier(sup)}
                          title="Xem thông tin & sản phẩm cung cấp"
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

                        {/* Nút Sửa (Cam Vàng) */}
                        <button
                          type="button"
                          onClick={() => openEditModal(sup)}
                          title="Chỉnh sửa thông tin"
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

                        {/* Nút Xóa (Đỏ) - Chỉ dành cho MANAGER và ADMIN */}
                        {canDelete && (
                        <button
                          type="button"
                          onClick={() => handleDeleteSupplier(sup.id)}
                          title="Xóa nhà cung cấp"
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
                            justify: 'center',
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
            onClick={() => loadSuppliers(pageInfo.number - 1)}
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
            onClick={() => loadSuppliers(pageInfo.number + 1)}
            style={{ padding: '8px 16px', background: '#ffffff', border: '1px solid #cbd5e1', borderRadius: '10px', fontSize: '13px', fontWeight: '700', cursor: pageInfo.number >= pageInfo.totalPages - 1 ? 'not-allowed' : 'pointer' }}
          >
            Trang sau
          </button>
        </div>
      )}

      {/* MODAL 1: VIEW SUPPLIER DETAILS & PRODUCTS MATCHING USER SCREENSHOT */}
      {viewSupplier && (
        <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, background: 'rgba(0,0,0,0.5)', zIndex: 1000, display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '20px' }}>
          <div style={{ background: '#f8fafc', borderRadius: '24px', padding: '28px', width: '100%', maxWidth: '940px', maxHeight: '90vh', overflowY: 'auto', boxShadow: '0 20px 50px rgba(0,0,0,0.2)' }}>
            
            {/* Header with Close button */}
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px', paddingBottom: '12px', borderBottom: '1px solid #e2e8f0' }}>
              <h2 style={{ fontSize: '20px', fontWeight: '900', color: '#0f172a', margin: 0 }}>
                Chi Tiết Nhà Cung Cấp - {viewSupplier.name}
              </h2>
              <button type="button" onClick={() => setViewSupplier(null)} style={{ border: 'none', background: '#e2e8f0', borderRadius: '50%', width: '32px', height: '32px', cursor: 'pointer', fontWeight: '900' }}>×</button>
            </div>

            {/* Two-Column Grid matching user screenshot */}
            <div style={{ display: 'grid', gridTemplateColumns: '320px 1fr', gap: '20px' }}>
              
              {/* LEFT CARD: Thông tin liên hệ */}
              <div style={{ background: '#ffffff', borderRadius: '16px', padding: '24px', boxShadow: '0 2px 10px rgba(0,0,0,0.03)', border: '1px solid #e2e8f0', display: 'flex', flexDirection: 'column', gap: '14px' }}>
                <h3 style={{ fontSize: '14px', fontWeight: '800', color: '#64748b', margin: 0, paddingBottom: '10px', borderBottom: '1px solid #f1f5f9' }}>
                  Thông tin liên hệ
                </h3>

                <div style={{ fontSize: '20px', fontWeight: '900', color: '#0f172a' }}>
                  {viewSupplier.name}
                </div>

                <div style={{ fontSize: '13px', color: '#64748b', display: 'flex', alignItems: 'center', gap: '8px' }}>
                  <span style={{ fontWeight: '900' }}>||||</span> NCC{String(viewSupplier.id).padStart(2, '0')}
                </div>

                <div style={{ fontSize: '13px', color: '#334155', display: 'flex', alignItems: 'center', gap: '8px' }}>
                  <span>📞</span> <strong style={{ color: '#0f172a' }}>{viewSupplier.phoneNumber || '—'}</strong>
                </div>

                <div style={{ fontSize: '13px', color: '#334155', display: 'flex', alignItems: 'center', gap: '8px' }}>
                  <span>✉️</span> {viewSupplier.email || '—'}
                </div>

                <div style={{ fontSize: '13px', color: '#334155', display: 'flex', alignItems: 'flex-start', gap: '8px' }}>
                  <span>📍</span> <span>{viewSupplier.address || '—'}</span>
                </div>

                {/* Description Box */}
                <div style={{ background: '#fafafa', border: '1px solid #e2e8f0', borderRadius: '12px', padding: '14px', fontSize: '13px', color: '#475569', marginTop: '6px' }}>
                  {viewSupplier.description || 'Cung cấp đồ chơi cao cấp chính hãng'}
                </div>
              </div>

              {/* RIGHT CARD: Sản phẩm cung cấp */}
              <div style={{ background: '#ffffff', borderRadius: '16px', padding: '24px', boxShadow: '0 2px 10px rgba(0,0,0,0.03)', border: '1px solid #e2e8f0' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px', paddingBottom: '12px', borderBottom: '1px solid #f1f5f9' }}>
                  <h3 style={{ fontSize: '16px', fontWeight: '800', color: '#166534', margin: 0, display: 'flex', alignItems: 'center', gap: '8px' }}>
                    <span>🚚</span> Sản phẩm cung cấp
                  </h3>
                  <span style={{ background: '#f1f5f9', border: '1px solid #cbd5e1', color: '#475569', fontSize: '12px', fontWeight: '800', padding: '4px 12px', borderRadius: '8px' }}>
                    {supplierProducts.length} sản phẩm
                  </span>
                </div>

                {/* Product Table */}
                <div style={{ overflowX: 'auto' }}>
                  <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '13px' }}>
                    <thead>
                      <tr style={{ background: '#f8fafc', color: '#475569', fontWeight: '800', fontSize: '12px', textAlign: 'left', borderBottom: '1px solid #e2e8f0' }}>
                        <th style={{ padding: '12px' }}>TÊN SẢN PHẨM</th>
                        <th style={{ padding: '12px', textAlign: 'right' }}>GIÁ NHẬP (GỢI Ý)</th>
                      </tr>
                    </thead>
                    <tbody>
                      {loadingProducts ? (
                        <tr>
                          <td colSpan="2" style={{ padding: '24px', textAlign: 'center', color: '#64748b' }}>Đang tải danh sách sản phẩm...</td>
                        </tr>
                      ) : supplierProducts.length === 0 ? (
                        <tr>
                          <td colSpan="2" style={{ padding: '24px', textAlign: 'center', color: '#94a3b8' }}>Chưa có sản phẩm nào thuộc nhà cung cấp này.</td>
                        </tr>
                      ) : (
                        supplierProducts.map((p) => (
                          <tr key={p.id} style={{ borderBottom: '1px solid #f1f5f9' }}>
                            <td style={{ padding: '12px', display: 'flex', alignItems: 'center', gap: '12px' }}>
                              <img
                                src={p.thumbnailUrl || p.mainImageUrl || '/toystore-assets/logo.png'}
                                alt=""
                                style={{ width: '42px', height: '42px', objectFit: 'contain', borderRadius: '8px', border: '1px solid #e2e8f0', padding: '2px' }}
                              />
                              <div>
                                <div style={{ fontWeight: '800', color: '#0f172a' }}>{p.name}</div>
                                <div style={{ fontSize: '11px', color: '#94a3b8', marginTop: '2px' }}>PT{p.id}</div>
                              </div>
                            </td>
                            <td style={{ padding: '12px', textAlign: 'right', fontWeight: '900', color: '#dc2626', fontSize: '14px' }}>
                              {formatPrice(p.basePrice || 0)}
                            </td>
                          </tr>
                        ))
                      )}
                    </tbody>
                  </table>
                </div>

              </div>

            </div>

            {/* Bottom Close Button */}
            <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: '24px' }}>
              <button type="button" onClick={() => setViewSupplier(null)} style={{ padding: '10px 24px', background: '#0284c7', color: '#fff', border: 'none', borderRadius: '12px', fontWeight: '800', cursor: 'pointer' }}>
                Đóng
              </button>
            </div>

          </div>
        </div>
      )}

      {/* MODAL 2: CREATE / EDIT SUPPLIER FORM */}
      {(isCreating || editSupplier) && (
        <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, background: 'rgba(0,0,0,0.5)', zIndex: 1000, display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '20px' }}>
          <div style={{ background: '#ffffff', borderRadius: '24px', padding: '32px', width: '100%', maxWidth: '560px', boxShadow: '0 20px 50px rgba(0,0,0,0.2)' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px', borderBottom: '1px solid #f1f5f9', paddingBottom: '12px' }}>
              <h2 style={{ fontSize: '20px', fontWeight: '900', color: '#0f172a', margin: 0 }}>
                {form.id ? `Chỉnh Sửa Nhà Cung Cấp NCC${String(form.id).padStart(2, '0')}` : 'Thêm Nhà Cung Cấp Mới'}
              </h2>
              <button type="button" onClick={() => { setIsCreating(false); setEditSupplier(null); }} style={{ border: 'none', background: '#f1f5f9', borderRadius: '50%', width: '32px', height: '32px', cursor: 'pointer', fontWeight: '900' }}>×</button>
            </div>

            <form onSubmit={handleSubmitForm} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
              <div>
                <label style={{ fontSize: '13px', fontWeight: '800', display: 'block', marginBottom: '6px' }}>Tên nhà cung cấp *</label>
                <input
                  value={form.name}
                  onChange={(e) => setForm((c) => ({ ...c, name: e.target.value }))}
                  required
                  placeholder="Nhập tên nhà cung cấp..."
                  style={{ width: '100%', padding: '10px 14px', border: '1px solid #cbd5e1', borderRadius: '12px', fontSize: '14px' }}
                />
              </div>

              <div>
                <label style={{ fontSize: '13px', fontWeight: '800', display: 'block', marginBottom: '6px' }}>Số điện thoại *</label>
                <input
                  value={form.phoneNumber}
                  onChange={(e) => setForm((c) => ({ ...c, phoneNumber: e.target.value }))}
                  required
                  placeholder="Nhập số điện thoại liên hệ..."
                  style={{ width: '100%', padding: '10px 14px', border: '1px solid #cbd5e1', borderRadius: '12px', fontSize: '14px' }}
                />
              </div>

              <div>
                <label style={{ fontSize: '13px', fontWeight: '800', display: 'block', marginBottom: '6px' }}>Email liên hệ</label>
                <input
                  type="email"
                  value={form.email}
                  onChange={(e) => setForm((c) => ({ ...c, email: e.target.value }))}
                  placeholder="example@supplier.com"
                  style={{ width: '100%', padding: '10px 14px', border: '1px solid #cbd5e1', borderRadius: '12px', fontSize: '14px' }}
                />
              </div>

              <div>
                <label style={{ fontSize: '13px', fontWeight: '800', display: 'block', marginBottom: '6px' }}>Địa chỉ kho / trụ sở</label>
                <textarea
                  rows="3"
                  value={form.address}
                  onChange={(e) => setForm((c) => ({ ...c, address: e.target.value }))}
                  placeholder="Nhập địa chỉ trụ sở hoặc địa điểm kho hàng..."
                  style={{ width: '100%', padding: '10px 14px', border: '1px solid #cbd5e1', borderRadius: '12px', fontSize: '14px', fontFamily: 'inherit' }}
                />
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '12px', marginTop: '16px' }}>
                <button type="button" onClick={() => { setIsCreating(false); setEditSupplier(null); }} style={{ padding: '12px 24px', background: '#f1f5f9', color: '#64748b', border: '1px solid #cbd5e1', borderRadius: '12px', fontWeight: '800', cursor: 'pointer' }}>
                  Hủy bỏ
                </button>
                <button type="submit" disabled={submitting} style={{ padding: '12px 28px', background: '#2563eb', color: '#fff', border: 'none', borderRadius: '14px', fontWeight: '800', cursor: 'pointer' }}>
                  {submitting ? 'Đang lưu...' : (form.id ? 'Lưu cập nhật' : 'Tạo nhà cung cấp')}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

    </section>
  );
}

export default AdminSupplierPage;
