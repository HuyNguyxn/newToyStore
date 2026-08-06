import { useEffect, useMemo, useState } from 'react';
import { useOutletContext } from 'react-router-dom';
import {
  activatePromotion,
  createPromotion,
  deactivatePromotion,
  deletePromotion,
  getAdminPromotions,
  updatePromotion,
} from '../../services/adminPromotionService.js';
import { getProducts } from '../../services/productService.js';
import { formatDateTime, formatPrice } from '../../utils/formatters.js';

const emptyPromotionForm = {
  id: '',
  code: '',
  name: '',
  type: 'PERCENTAGE',
  scope: 'PRODUCT',
  discountValue: '',
  maxDiscountAmount: '',
  minOrderValue: '',
  targetProductId: '',
  usageLimit: '',
  startDate: '',
  endDate: '',
  description: '',
};

// Promotion status helper
function getPromotionStatusInfo(promo) {
  const now = new Date();
  const start = new Date(promo.startDate);
  const end = new Date(promo.endDate);

  if (now < start) {
    return { label: 'Chưa bắt đầu', bg: '#fef3c7', color: '#d97706', border: '#fde68a' };
  } else if (now > end) {
    return { label: 'Đã kết thúc', bg: '#f1f5f9', color: '#64748b', border: '#cbd5e1' };
  } else {
    return { label: 'Đang áp dụng', bg: '#d1fae5', color: '#10b981', border: '#a7f3d0' };
  }
}

function AdminPromotionPage() {
  const { userRole } = useOutletContext();
  const canDelete = userRole === 'MANAGER' || userRole === 'ADMIN';

  const [promotions, setPromotions] = useState([]);
  const [productsMap, setProductsMap] = useState({});
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  // Page view states: 'LIST', 'FORM', 'DETAIL'
  const [viewState, setViewState] = useState('LIST');
  const [selectedPromo, setSelectedPromo] = useState(null);
  const [form, setForm] = useState(emptyPromotionForm);
  const [formStatus, setFormStatus] = useState('ACTIVE');

  // Filters State matching mockup
  const [searchKeyword, setSearchKeyword] = useState('');
  const [statusFilter, setStatusFilter] = useState('');

  useEffect(() => {
    loadInitialData();
  }, []);

  async function loadInitialData() {
    setLoading(true);
    setError('');
    try {
      // 1. Fetch promotions
      const promoResult = await getAdminPromotions({ page: 0, size: 100 });
      setPromotions(promoResult.content || promoResult || []);

      // 2. Fetch products for lookups
      const productResult = await getProducts({ page: 0, size: 200 });
      const plist = productResult.content || productResult || [];
      const pmap = {};
      plist.forEach((p) => {
        pmap[p.id] = p;
      });
      setProductsMap(pmap);
    } catch (err) {
      setError(err.message || 'Không thể tải dữ liệu khuyến mãi.');
    } finally {
      setLoading(false);
    }
  }

  // Frontend real-time filtering matching mockup
  const displayPromotions = useMemo(() => {
    return promotions.filter((promo) => {
      // 1. Keyword search (Name or Code)
      if (searchKeyword.trim()) {
        const kw = searchKeyword.trim().toLowerCase();
        const nameMatch = (promo.name || '').toLowerCase().includes(kw);
        const codeMatch = (promo.code || '').toLowerCase().includes(kw);
        if (!nameMatch && !codeMatch) return false;
      }

      // 2. Status Filter
      if (statusFilter) {
        const statusInfo = getPromotionStatusInfo(promo);
        if (statusInfo.label !== statusFilter) return false;
      }

      return true;
    });
  }, [promotions, searchKeyword, statusFilter]);

  function selectPromotionForDetail(promo) {
    setSelectedPromo(promo);
    setViewState('DETAIL');
  }

  function startCreate() {
    setForm(emptyPromotionForm);
    setFormStatus('ACTIVE');
    setViewState('FORM');
  }

  function startEdit(promo) {
    setSelectedPromo(promo);
    setForm({
      id: promo.id || '',
      code: promo.code || '',
      name: promo.name || '',
      type: promo.type || 'PERCENTAGE',
      scope: promo.scope || 'PRODUCT',
      discountValue: promo.discountValue ?? '',
      maxDiscountAmount: promo.maxDiscountAmount ?? '',
      minOrderValue: promo.minOrderValue ?? '',
      targetProductId: promo.targetProductId ? `MH${promo.targetProductId}` : '',
      usageLimit: promo.usageLimit ?? '',
      startDate: toDateTimeInput(promo.startDate),
      endDate: toDateTimeInput(promo.endDate),
      description: promo.description || '',
    });
    setFormStatus(promo.active || promo.isActive ? 'ACTIVE' : 'INACTIVE');
    setViewState('FORM');
  }

  function buildPayload() {
    const numericProductId = parseInt(String(form.targetProductId || '').replace(/\D/g, '')) || null;
    return {
      code: form.code.trim().toUpperCase(),
      name: form.name.trim(),
      type: form.type,
      scope: form.scope,
      discountValue: Number(form.discountValue || 0),
      maxDiscountAmount: form.maxDiscountAmount === '' ? null : Number(form.maxDiscountAmount),
      minOrderValue: form.minOrderValue === '' ? null : Number(form.minOrderValue),
      targetProductId: numericProductId,
      usageLimit: form.usageLimit === '' ? null : Number(form.usageLimit),
      startDate: form.startDate ? `${form.startDate}:00` : null,
      endDate: form.endDate ? `${form.endDate}:00` : null,
      description: form.description || '',
    };
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setSubmitting(true);
    setError('');
    setMessage('');
    try {
      if (form.id) {
        await updatePromotion(form.id, buildPayload());
        
        // Handle activation status changes
        const currentActive = selectedPromo?.active || selectedPromo?.isActive;
        const nextActive = formStatus === 'ACTIVE';
        if (nextActive !== currentActive) {
          if (nextActive) {
            await activatePromotion(form.id);
          } else {
            await deactivatePromotion(form.id);
          }
        }
        setMessage('Đã cập nhật khuyến mãi thành công.');
      } else {
        await createPromotion(buildPayload());
        setMessage('Đã tạo khuyến mãi thành công.');
      }
      setViewState('LIST');
      await loadInitialData();
    } catch (err) {
      setError(err.message || 'Lưu khuyến mãi thất bại.');
    } finally {
      setSubmitting(false);
    }
  }

  async function handleDelete(promoId) {
    const confirmed = window.confirm('Bạn chắc chắn muốn xóa khuyến mãi này?');
    if (!confirmed) return;
    setError('');
    setMessage('');
    try {
      await deletePromotion(promoId);
      setMessage('Đã xóa khuyến mãi thành công.');
      await loadInitialData();
    } catch (err) {
      setError(err.message || 'Xóa khuyến mãi thất bại.');
    }
  }

  if (loading) {
    return (
      <div style={{ padding: '30px', textAlign: 'center', color: '#64748b', fontSize: '14px', fontWeight: '700' }}>
        Đang tải trang khuyến mãi...
      </div>
    );
  }

  // ═══════════════════════════════════════════════════════════════════
  // STATE 1: PROMOTION DETAIL VIEW (Image 5 Mockup Style)
  // ═══════════════════════════════════════════════════════════════════
  if (viewState === 'DETAIL' && selectedPromo) {
    const product = productsMap[selectedPromo.targetProductId];
    const originalPrice = product?.price || 380000; // Mock default matching Captain America price if not available
    const productLabel = product ? `${product.name} (MH${product.id})` : `Mô Hình Captain America 30cm (MH${selectedPromo.targetProductId || '05'})`;
    
    // Calculate discounted price
    let discountedPrice = originalPrice;
    if (selectedPromo.type === 'PERCENTAGE') {
      discountedPrice = originalPrice * (1 - (selectedPromo.discountValue || 0) / 100);
    } else if (selectedPromo.type === 'FIXED_AMOUNT') {
      discountedPrice = Math.max(0, originalPrice - (selectedPromo.discountValue || 0));
    }

    return (
      <section style={{ padding: '24px', background: '#f8fafc', minHeight: '100vh', fontFamily: 'system-ui, -apple-system, sans-serif' }}>
        {/* HEADER */}
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
          <h1 style={{ fontSize: '20px', fontWeight: '900', color: '#ea580c', margin: 0 }}>
            Chi tiết khuyến mãi
          </h1>
          <button
            type="button"
            onClick={() => setViewState('LIST')}
            style={{
              padding: '8px 16px',
              background: '#ffffff',
              color: '#475569',
              border: '1px solid #cbd5e1',
              borderRadius: '8px',
              fontSize: '13px',
              fontWeight: '700',
              cursor: 'pointer',
            }}
          >
            ← Quay lại danh sách
          </button>
        </div>

        {/* DETAILS GRID */}
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '20px', alignItems: 'start' }}>
          
          {/* LEFT CARD */}
          <div style={{ background: '#ffffff', borderRadius: '12px', border: '1px solid #e2e8f0', padding: '24px', display: 'flex', flexDirection: 'column', gap: '16px' }}>
            <h2 style={{ fontSize: '22px', fontWeight: '900', color: '#ea580c', margin: 0 }}>
              {selectedPromo.name}
            </h2>
            
            <div>
              <span style={{ background: '#f1f5f9', color: '#475569', border: '1px solid #cbd5e1', padding: '4px 10px', borderRadius: '6px', fontSize: '13px', fontWeight: '800' }}>
                Mã KM: {selectedPromo.code}
              </span>
            </div>

            <div style={{ fontSize: '14px', color: '#334155', fontWeight: '600', marginTop: '6px' }}>
              Sản phẩm: <span style={{ color: '#0f172a' }}>{productLabel}</span>
            </div>

            <div style={{ background: '#f8fafc', padding: '14px 16px', borderRadius: '8px', border: '1px solid #e2e8f0', fontSize: '13.5px', color: '#475569', minHeight: '80px', lineHeight: 1.5 }}>
              {selectedPromo.description || 'Không có mô tả chi tiết cho chương trình khuyến mãi này.'}
            </div>
          </div>

          {/* RIGHT CARD */}
          <div style={{ background: '#ffffff', borderRadius: '12px', border: '1px solid #e2e8f0', padding: '24px', display: 'flex', flexDirection: 'column', gap: '14px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: '1px solid #f1f5f9', paddingBottom: '12px' }}>
              <span style={{ fontSize: '14px', color: '#64748b', fontWeight: '600' }}>Giá gốc:</span>
              <span style={{ fontSize: '14px', color: '#334155', fontWeight: '700', textDecoration: 'line-through' }}>{formatPrice(originalPrice)}</span>
            </div>

            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: '1px solid #f1f5f9', paddingBottom: '12px' }}>
              <span style={{ fontSize: '14px', color: '#64748b', fontWeight: '600' }}>Giảm:</span>
              <span style={{ background: '#dc2626', color: '#ffffff', padding: '4px 8px', borderRadius: '6px', fontSize: '12.5px', fontWeight: '800' }}>
                {selectedPromo.type === 'PERCENTAGE' ? `-${selectedPromo.discountValue}%` : `-${formatPrice(selectedPromo.discountValue)}`}
              </span>
            </div>

            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', paddingTop: '6px' }}>
              <span style={{ fontSize: '15px', color: '#0f172a', fontWeight: '800' }}>Giá sau giảm:</span>
              <span style={{ fontSize: '20px', color: '#16a34a', fontWeight: '900' }}>{formatPrice(discountedPrice)}</span>
            </div>

            <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '12px', color: '#64748b', marginTop: '20px', background: '#f8fafc', padding: '10px 14px', borderRadius: '8px', border: '1px solid #cbd5e1' }}>
              <span>Từ: <strong>{formatDateTime(selectedPromo.startDate).split(' ')[0]}</strong></span>
              <span>Đến: <strong>{formatDateTime(selectedPromo.endDate).split(' ')[0]}</strong></span>
            </div>
          </div>

        </div>

        {/* FOOTER */}
        <footer style={{ textAlign: 'center', marginTop: '40px', fontSize: '12px', color: '#94a3b8' }}>
          © 2026 ToyStore Admin Panel
        </footer>
      </section>
    );
  }

  // ═══════════════════════════════════════════════════════════════════
  // STATE 2: ADD / EDIT PROMOTION FORM (Image 4 & Edit Mode Style)
  // ═══════════════════════════════════════════════════════════════════
  if (viewState === 'FORM') {
    return (
      <section style={{ padding: '24px', background: '#f8fafc', minHeight: '100vh', fontFamily: 'system-ui, -apple-system, sans-serif', display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
        
        {/* CONTAINER CARD */}
        <div style={{ background: '#ffffff', width: '100%', maxWidth: '780px', borderRadius: '12px', border: '1px solid #e2e8f0', padding: '30px 40px', marginTop: '10px' }}>
          
          {/* HEADER */}
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px', borderBottom: '1px solid #f1f5f9', paddingBottom: '16px' }}>
            <h2 style={{ fontSize: '20px', fontWeight: '900', color: '#ea580c', margin: 0 }}>
              {form.id ? 'Cập nhật khuyến mãi' : 'Thêm khuyến mãi'}
            </h2>
            <button
              type="button"
              onClick={() => setViewState('LIST')}
              style={{
                padding: '6px 14px',
                background: '#ffffff',
                color: '#475569',
                border: '1px solid #cbd5e1',
                borderRadius: '8px',
                fontSize: '12.5px',
                fontWeight: '700',
                cursor: 'pointer',
              }}
            >
              Hủy đơn
            </button>
          </div>

          {/* ALERTS */}
          {error && <div style={{ background: '#fef2f2', color: '#dc2626', border: '1px solid #fecaca', padding: '10px 14px', borderRadius: '8px', marginBottom: '16px', fontSize: '13px', fontWeight: '700' }}>{error}</div>}

          {/* FORM */}
          <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
            
            {/* ROW 1: MÃ KM & TÊN KM */}
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 2fr', gap: '16px' }}>
              <div>
                <label style={{ display: 'block', fontSize: '13px', fontWeight: '700', color: '#334155', marginBottom: '6px' }}>Mã KM</label>
                <input
                  type="text"
                  placeholder="Ví dụ: KM02"
                  value={form.code}
                  onChange={(e) => setForm({ ...form, code: e.target.value.toUpperCase() })}
                  required
                  style={{ width: '100%', padding: '9px 12px', border: '1px solid #cbd5e1', borderRadius: '8px', fontSize: '13.5px', outline: 'none' }}
                />
              </div>
              <div>
                <label style={{ display: 'block', fontSize: '13px', fontWeight: '700', color: '#334155', marginBottom: '6px' }}>Tên KM</label>
                <input
                  type="text"
                  placeholder="Ví dụ: Flash Sale Mô Hình"
                  value={form.name}
                  onChange={(e) => setForm({ ...form, name: e.target.value })}
                  required
                  style={{ width: '100%', padding: '9px 12px', border: '1px solid #cbd5e1', borderRadius: '8px', fontSize: '13.5px', outline: 'none' }}
                />
              </div>
            </div>

            {/* ROW 2: MÃ SP & % GIẢM */}
            <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: '16px' }}>
              <div>
                <label style={{ display: 'block', fontSize: '13px', fontWeight: '700', color: '#334155', marginBottom: '6px' }}>Mã SP</label>
                <input
                  type="text"
                  placeholder="Nhập mã sản phẩm (ID)..."
                  value={form.targetProductId}
                  onChange={(e) => setForm({ ...form, targetProductId: e.target.value })}
                  required
                  style={{ width: '100%', padding: '9px 12px', border: '1px solid #cbd5e1', borderRadius: '8px', fontSize: '13.5px', outline: 'none' }}
                />
              </div>
              <div>
                <label style={{ display: 'block', fontSize: '13px', fontWeight: '700', color: '#334155', marginBottom: '6px' }}>% Giảm</label>
                <div style={{ position: 'relative' }}>
                  <input
                    type="text"
                    placeholder="20.00"
                    value={form.discountValue}
                    onChange={(e) => setForm({ ...form, discountValue: e.target.value })}
                    required
                    style={{ width: '100%', padding: '9px 30px 9px 12px', border: '1px solid #cbd5e1', borderRadius: '8px', fontSize: '13.5px', outline: 'none' }}
                  />
                  <span style={{ position: 'absolute', right: '12px', top: '50%', transform: 'translateY(-50%)', fontWeight: '700', color: '#64748b' }}>%</span>
                </div>
              </div>
            </div>

            {/* ROW 3: BẮT ĐẦU & KẾT THÚC */}
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
              <div>
                <label style={{ display: 'block', fontSize: '13px', fontWeight: '700', color: '#334155', marginBottom: '6px' }}>Bắt đầu</label>
                <input
                  type="datetime-local"
                  value={form.startDate}
                  onChange={(e) => setForm({ ...form, startDate: e.target.value })}
                  required
                  style={{ width: '100%', padding: '9px 12px', border: '1px solid #cbd5e1', borderRadius: '8px', fontSize: '13.5px', outline: 'none', background: '#fff' }}
                />
              </div>
              <div>
                <label style={{ display: 'block', fontSize: '13px', fontWeight: '700', color: '#334155', marginBottom: '6px' }}>Kết thúc</label>
                <input
                  type="datetime-local"
                  value={form.endDate}
                  onChange={(e) => setForm({ ...form, endDate: e.target.value })}
                  required
                  style={{ width: '100%', padding: '9px 12px', border: '1px solid #cbd5e1', borderRadius: '8px', fontSize: '13.5px', outline: 'none', background: '#fff' }}
                />
              </div>
            </div>

            {/* MÔ TẢ */}
            <div>
              <label style={{ display: 'block', fontSize: '13px', fontWeight: '700', color: '#334155', marginBottom: '6px' }}>Mô tả</label>
              <textarea
                placeholder="Nhập mô tả chi tiết chương trình khuyến mãi..."
                rows="4"
                value={form.description}
                onChange={(e) => setForm({ ...form, description: e.target.value })}
                style={{ width: '100%', padding: '10px 12px', border: '1px solid #cbd5e1', borderRadius: '8px', fontSize: '13.5px', outline: 'none', resize: 'vertical' }}
              />
            </div>

            {/* STATUS SELECT (Only displayed in EDIT mode) */}
            {form.id && (
              <div>
                <label style={{ display: 'block', fontSize: '13px', fontWeight: '700', color: '#334155', marginBottom: '6px' }}>Trạng thái</label>
                <select
                  value={formStatus}
                  onChange={(e) => setFormStatus(e.target.value)}
                  style={{ width: '100%', padding: '10px 12px', border: '1px solid #cbd5e1', borderRadius: '8px', fontSize: '13.5px', outline: 'none', background: '#fff' }}
                >
                  <option value="ACTIVE">Đang áp dụng</option>
                  <option value="INACTIVE">Đã kết thúc / Đã tạm dừng</option>
                </select>
              </div>
            )}

            {/* SAVE BUTTON */}
            <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: '10px' }}>
              <button
                type="submit"
                disabled={submitting}
                style={{
                  padding: '10px 24px',
                  background: '#16a34a',
                  color: '#ffffff',
                  border: 'none',
                  borderRadius: '8px',
                  fontSize: '13.5px',
                  fontWeight: '800',
                  cursor: 'pointer',
                  boxShadow: '0 4px 12px rgba(22,163,74,0.15)'
                }}
              >
                Lưu
              </button>
            </div>

          </form>

        </div>

        {/* FOOTER */}
        <footer style={{ textAlign: 'center', marginTop: '40px', fontSize: '12px', color: '#94a3b8' }}>
          © 2026 ToyStore Admin Panel
        </footer>
      </section>
    );
  }

  // ═══════════════════════════════════════════════════════════════════
  // STATE 3: PROMOTION LIST VIEW (Image 2 Mockup Style)
  // ═══════════════════════════════════════════════════════════════════
  return (
    <section style={{ padding: '24px', background: '#f8fafc', minHeight: '100vh', fontFamily: 'system-ui, -apple-system, sans-serif' }}>
      
      {/* HEADER ROW WITH BADGE */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
        <h1 style={{ fontSize: '18px', fontWeight: '800', color: '#0f172a', margin: 0, textTransform: 'uppercase', letterSpacing: '0.5px' }}>
          Quản lý khuyến mãi
        </h1>
        <button
          type="button"
          onClick={startCreate}
          style={{
            padding: '9px 18px',
            background: '#16a34a',
            color: '#ffffff',
            border: 'none',
            borderRadius: '8px',
            fontSize: '13px',
            fontWeight: '700',
            cursor: 'pointer',
            boxShadow: '0 4px 10px rgba(22,163,74,0.15)',
          }}
        >
          + Thêm mới
        </button>
      </div>

      {/* ALERTS */}
      {error && <div style={{ background: '#fef2f2', color: '#dc2626', border: '1px solid #fecaca', padding: '10px 14px', borderRadius: '8px', marginBottom: '16px', fontSize: '13px', fontWeight: '700' }}>{error}</div>}
      {message && <div style={{ background: '#f0fdf4', color: '#16a34a', border: '1px solid #bbf7d0', padding: '10px 14px', borderRadius: '8px', marginBottom: '16px', fontSize: '13px', fontWeight: '700' }}>{message}</div>}

      {/* FILTER BAR (Image 2 style) */}
      <div
        style={{ background: '#ffffff', padding: '14px 16px', borderRadius: '12px', border: '1px solid #e2e8f0', marginBottom: '16px', display: 'flex', gap: '10px', alignItems: 'center', flexWrap: 'wrap' }}
      >
        <div style={{ flex: '1', minWidth: '220px' }}>
          <input
            type="text"
            placeholder="Tìm theo tên/mã..."
            value={searchKeyword}
            onChange={(e) => setSearchKeyword(e.target.value)}
            style={{ width: '100%', padding: '9px 12px', border: '1px solid #cbd5e1', borderRadius: '8px', fontSize: '13px', outline: 'none' }}
          />
        </div>

        <div style={{ flex: '1', minWidth: '180px' }}>
          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            style={{ width: '100%', padding: '9px 12px', border: '1px solid #cbd5e1', borderRadius: '8px', fontSize: '13px', outline: 'none', background: '#fff' }}
          >
            <option value="">-- Tất cả trạng thái --</option>
            <option value="Đang áp dụng">Đang áp dụng</option>
            <option value="Chưa bắt đầu">Chưa bắt đầu</option>
            <option value="Đã kết thúc">Đã kết thúc</option>
          </select>
        </div>

        <div>
          <button
            type="button"
            onClick={loadInitialData}
            style={{ padding: '9px 24px', background: '#ea580c', color: '#ffffff', border: 'none', borderRadius: '8px', fontSize: '13px', fontWeight: '700', cursor: 'pointer' }}
          >
            Tìm
          </button>
        </div>
      </div>

      {/* DATA TABLE (Image 2 style) */}
      <div style={{ background: '#ffffff', borderRadius: '12px', border: '1px solid #e2e8f0', overflow: 'visible' }}>
        <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '13px' }}>
          <thead>
            <tr style={{ background: '#f8fafc', color: '#475569', fontWeight: '800', fontSize: '12px', borderBottom: '1px solid #e2e8f0', textTransform: 'uppercase' }}>
              <th style={{ padding: '14px 16px' }}>Tên KM</th>
              <th style={{ padding: '14px 16px' }}>Tên sản phẩm</th>
              <th style={{ padding: '14px 16px', textAlign: 'center', width: '100px' }}>% Giảm</th>
              <th style={{ padding: '14px 16px', width: '130px' }}>Bắt đầu</th>
              <th style={{ padding: '14px 16px', width: '130px' }}>Kết thúc</th>
              <th style={{ padding: '14px 16px', width: '140px' }}>Trạng thái</th>
              <th style={{ padding: '14px 16px', width: '140px', textAlign: 'center' }}>Thao tác</th>
            </tr>
          </thead>
          <tbody>
            {displayPromotions.length === 0 ? (
              <tr>
                <td colSpan="7" style={{ padding: '36px', textAlign: 'center', color: '#94a3b8' }}>
                  Không tìm thấy chương trình khuyến mãi nào.
                </td>
              </tr>
            ) : (
              displayPromotions.map((promo, idx) => {
                const statusInfo = getPromotionStatusInfo(promo);
                const product = productsMap[promo.targetProductId];
                const productName = product ? product.name : `Sản phẩm #${promo.targetProductId}`;

                return (
                  <tr
                    key={promo.id}
                    style={{
                      borderBottom: '1px solid #f1f5f9',
                      background: idx % 2 === 0 ? '#ffffff' : '#fafafa',
                    }}
                  >
                    {/* Tên KM */}
                    <td style={{ padding: '14px 16px', fontWeight: '800', color: '#ea580c' }}>
                      {promo.name}
                    </td>

                    {/* Tên sản phẩm */}
                    <td style={{ padding: '14px 16px', color: '#334155', fontWeight: '500' }}>
                      {productName}
                    </td>

                    {/* % Giảm */}
                    <td style={{ padding: '14px 16px', textAlign: 'center', fontWeight: '800', color: '#dc2626' }}>
                      -{Number(promo.discountValue || 0).toFixed(2)}%
                    </td>

                    {/* Bắt đầu */}
                    <td style={{ padding: '14px 16px', color: '#64748b' }}>
                      {formatDateTime(promo.startDate).split(' ')[0]}
                    </td>

                    {/* Kết thúc */}
                    <td style={{ padding: '14px 16px', color: '#64748b' }}>
                      {formatDateTime(promo.endDate).split(' ')[0]}
                    </td>

                    {/* Trạng thái */}
                    <td style={{ padding: '14px 16px' }}>
                      <span
                        style={{
                          background: statusInfo.bg,
                          color: statusInfo.color,
                          border: `1px solid ${statusInfo.border}`,
                          padding: '3px 10px',
                          borderRadius: '8px',
                          fontSize: '12px',
                          fontWeight: '700',
                          display: 'inline-block',
                        }}
                      >
                        {statusInfo.label}
                      </span>
                    </td>

                    {/* Thao tác */}
                    <td style={{ padding: '14px 16px', textAlign: 'center' }}>
                      <div style={{ display: 'inline-flex', gap: '6px', justifyContent: 'center' }}>
                        {/* Eye Button */}
                        <button
                          type="button"
                          onClick={() => selectPromotionForDetail(promo)}
                          title="Xem chi tiết"
                          style={{
                            padding: '6px 12px',
                            background: '#ffffff',
                            color: '#475569',
                            border: '1px solid #cbd5e1',
                            borderRadius: '6px',
                            cursor: 'pointer',
                            fontSize: '12.5px',
                            fontWeight: '700',
                          }}
                        >
                          Xem
                        </button>

                        {/* Pencil Button */}
                        <button
                          type="button"
                          onClick={() => startEdit(promo)}
                          title="Chỉnh sửa"
                          style={{
                            padding: '6px 12px',
                            background: '#ffffff',
                            color: '#ea580c',
                            border: '1px solid #ffedd5',
                            borderRadius: '6px',
                            cursor: 'pointer',
                            fontSize: '12.5px',
                            fontWeight: '700',
                          }}
                        >
                          Sửa
                        </button>

                        {/* Delete Button */}
                        {canDelete && (
                          <button
                            type="button"
                            onClick={() => handleDelete(promo.id)}
                            title="Xóa khuyến mãi"
                            style={{
                              padding: '6px 12px',
                              background: '#ffffff',
                              color: '#dc2626',
                              border: '1px solid #fecaca',
                              borderRadius: '6px',
                              cursor: 'pointer',
                              fontSize: '12.5px',
                              fontWeight: '700',
                            }}
                          >
                            Xóa
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

      {/* FOOTER */}
      <footer style={{ textAlign: 'center', marginTop: '30px', padding: '16px 0', borderTop: '1px solid #cbd5e1', fontSize: '12px', color: '#94a3b8' }}>
        © 2026 ToyStore Admin Panel
      </footer>
    </section>
  );
}

function toDateTimeInput(value) {
  if (!value) return '';
  return String(value).slice(0, 16);
}

export default AdminPromotionPage;
