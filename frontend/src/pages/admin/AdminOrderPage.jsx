import { useEffect, useMemo, useState } from 'react';
import { useOutletContext, useSearchParams } from 'react-router-dom';
import {
  cancelAdminOrder,
  completeAdminOrder,
  confirmAdminOrder,
  deleteAdminOrder,
  getAdminOrderDetails,
  getAdminOrders,
  shipAdminOrder,
  updateAdminOrderShipping,
} from '../../services/adminOrderService.js';
import { formatDateTime, formatPrice } from '../../utils/formatters.js';

// Status badge styling helper
function getOrderStatusInfo(status) {
  const code = typeof status === 'object' ? (status?.code || status?.name || '') : String(status || '');
  const label = typeof status === 'object' ? (status?.displayName || status?.label || '') : '';
  const statusStr = code.toUpperCase();

  if (statusStr === 'PENDING' || statusStr === 'PROCESSING') {
    return { label: label || 'Đang xử lý', bg: '#fef3c7', color: '#d97706', border: '#fde68a' };
  }
  if (statusStr === 'CONFIRMED' || statusStr === 'CONFIRM') {
    return { label: label || 'Đã xác nhận', bg: '#eff6ff', color: '#2563eb', border: '#bfdbfe' };
  }
  if (statusStr === 'SHIPPED' || statusStr === 'SHIPPING') {
    return { label: label || 'Đang giao', bg: '#f0fdf4', color: '#16a34a', border: '#bbf7d0' };
  }
  if (statusStr === 'COMPLETED' || statusStr === 'DELIVERED') {
    return { label: label || 'Đã giao', bg: '#d1fae5', color: '#10b981', border: '#a7f3d0' };
  }
  if (statusStr === 'CANCELLED' || statusStr === 'CANCEL') {
    return { label: label || 'Đã hủy', bg: '#fee2e2', color: '#dc2626', border: '#fecaca' };
  }
  return { label: label || statusStr, bg: '#f1f5f9', color: '#475569', border: '#cbd5e1' };
}

// Helper to check internal test orders
function isInternalTestOrder(item) {
  if (!item) return false;
  
  // 1. If user role is CUSTOMER, it is ALWAYS a real business customer order!
  const userRole = String(item.user?.role || item.userRole || '').toUpperCase();
  if (userRole === 'CUSTOMER') return false;

  // 2. Check direct user ID of seeded internal users.
  const uid = Number(item.userId || item.user?.id || item.customerId || 0);
  if (uid > 0 && uid <= 3) return true;

  // 3. Check buyer user role if attached (ADMIN, STAFF, MANAGER are internal test users)
  if (['ADMIN', 'STAFF', 'MANAGER'].includes(userRole)) return true;

  // 4. Check buyer email if attached (Only admin@, staff@, manager@ internal staff emails)
  const email = String(item.user?.email || item.customerEmail || item.email || '').toLowerCase();
  if (email.includes('admin@') || email.includes('staff@') || email.includes('manager@') || email.includes('@toystore.internal')) {
    return true;
  }

  // 5. Check explicit test note on order itself
  const note = String(item.note || item.customerNote || item.description || '').toLowerCase();
  if (note.includes('đơn test') || note.includes('thử nghiệm nội bộ') || note.includes('[test]')) {
    return true;
  }

  return false;
}

function AdminOrderPage() {
  const { userRole } = useOutletContext();
  const [searchParams] = useSearchParams();
  const initialStatus = searchParams.get('status') || '';
  const canDelete = userRole === 'ADMIN';

  const [dataMode, setDataMode] = useState('REAL'); // 'REAL' or 'TEST'
  const [orders, setOrders] = useState([]);
  const [selected, setSelected] = useState(null);
  const [filters, setFilters] = useState({ status: initialStatus, userId: '' });
  const [actionNote, setActionNote] = useState('Cập nhật từ trang quản trị');
  const [shippingForm, setShippingForm] = useState({ newAddress: '', note: '' });
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [showStatusModal, setShowStatusModal] = useState(false);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const pageSize = 20;

  // Edit view states matching Mockup Image 1
  const [isEditing, setIsEditing] = useState(false);
  const [tempStatus, setTempStatus] = useState('PENDING');
  const [tempAddress, setTempAddress] = useState('');
  const [tempPhone, setTempPhone] = useState('0398616546');

  const displayOrders = useMemo(() => {
    return orders.filter((o) => {
      const isTest = isInternalTestOrder(o);
      return dataMode === 'REAL' ? !isTest : isTest;
    });
  }, [orders, dataMode]);

  useEffect(() => {
    loadOrders(initialStatus);
  }, []);

  async function loadOrders(overrideStatus, requestedPage = page) {
    setLoading(true);
    setError('');
    const targetStatus = overrideStatus !== undefined ? overrideStatus : filters.status;
    try {
      const result = await getAdminOrders({
        status: targetStatus || undefined,
        userId: filters.userId || undefined,
        page: requestedPage,
        size: pageSize,
        sort: 'createdAt,desc',
      });
      setOrders(result.content || result || []);
      setPage(result.number ?? requestedPage);
      setTotalPages(result.totalPages ?? 0);
    } catch (err) {
      setError(err.message || 'Không thể tải danh sách đơn hàng.');
    } finally {
      setLoading(false);
    }
  }

  async function selectOrder(order) {
    setError('');
    setMessage('');
    setSelected(order);
    setShippingForm({ newAddress: order.shippingAddress || '', note: '' });
    try {
      const result = await getAdminOrderDetails(order.id);
      setSelected(result);
      setShippingForm({ newAddress: result.shippingAddress || '', note: '' });
    } catch {
      // Keep basic row data if details endpoint is not available
    }
  }

  async function editOrder(order) {
    setError('');
    setMessage('');
    setSelected(order);
    setIsEditing(true);
    setTempStatus(typeof order.status === 'object' ? (order.status?.code || order.status?.name || '') : String(order.status || ''));
    setTempAddress(order.shippingAddress || '');
    setTempPhone('0398616546'); // Default mock phone from mockup

    try {
      const result = await getAdminOrderDetails(order.id);
      setSelected(result);
      setTempStatus(typeof result.status === 'object' ? (result.status?.code || result.status?.name || '') : String(result.status || ''));
      setTempAddress(result.shippingAddress || '');
    } catch {
      // Keep row details
    }
  }

  async function handleSaveEdit(e) {
    e.preventDefault();
    setLoading(true);
    setError('');
    setMessage('');
    try {
      // 1. If status changed, transition status
      const originalStatus = typeof selected.status === 'object' ? (selected.status?.code || selected.status?.name || '') : String(selected.status || '');
      if (tempStatus !== originalStatus) {
        let action;
        if (tempStatus === 'CONFIRMED') action = () => confirmAdminOrder(selected.id, actionNote);
        else if (tempStatus === 'SHIPPED') action = () => shipAdminOrder(selected.id, actionNote);
        else if (tempStatus === 'COMPLETED') action = () => completeAdminOrder(selected.id, actionNote);
        else if (tempStatus === 'CANCELLED') action = () => cancelAdminOrder(selected.id, actionNote);
        
        if (action) {
          await action();
        }
      }

      // 2. If address changed, update shipping address
      if (tempAddress !== (selected.shippingAddress || '')) {
        await updateAdminOrderShipping(selected.id, { newAddress: tempAddress, note: actionNote });
      }

      setMessage('Cập nhật đơn hàng thành công.');
      setIsEditing(false);
      setSelected(null);
      await loadOrders();
    } catch (err) {
      setError(err.message || 'Cập nhật đơn hàng thất bại.');
    } finally {
      setLoading(false);
    }
  }

  async function doAction(action, successMsg) {
    setError('');
    setMessage('');
    try {
      const result = await action();
      setMessage(successMsg);
      if (result) setSelected(result);
      setShowStatusModal(false);
      await loadOrders();
    } catch (err) {
      setError(err.message || 'Thao tác thất bại.');
    }
  }

  const handleClearFilters = () => {
    setFilters({ status: '', userId: '' });
    setPage(0);
    // Reload automatically
    setTimeout(() => {
      loadOrders('', 0);
    }, 50);
  };

  // State 1: Editing Order View (Image 1 mockup style)
  if (selected && isEditing) {
    return (
      <section style={{ padding: '24px', background: '#f8fafc', minHeight: '100vh', fontFamily: 'system-ui, -apple-system, sans-serif', display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
        
        {/* CONTAINER CARD */}
        <div style={{ background: '#ffffff', width: '100%', maxWidth: '640px', borderRadius: '12px', border: '1px solid #e2e8f0', boxShadow: '0 4px 16px rgba(0,0,0,0.02)', padding: '30px 40px', marginTop: '20px' }}>
          
          {/* HEADER */}
          <h2 style={{ fontSize: '20px', fontWeight: '900', color: '#ea580c', textAlign: 'center', marginTop: 0, marginBottom: '24px' }}>
            Cập nhật đơn hàng
          </h2>

          {/* ALERTS */}
          {error && <div style={{ background: '#fef2f2', color: '#dc2626', border: '1px solid #fecaca', padding: '10px 14px', borderRadius: '8px', marginBottom: '16px', fontSize: '13px', fontWeight: '700' }}>{error}</div>}
          
          {/* ORDER BRIEF INFO CARD */}
          <div style={{ background: '#f8fafc', padding: '18px 20px', borderRadius: '8px', border: '1px solid #e2e8f0', marginBottom: '20px', fontSize: '13.5px', color: '#334155' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '8px' }}>
              <span style={{ color: '#64748b' }}>Mã đơn:</span>
              <span style={{ fontWeight: '700', color: '#0f172a' }}>DH{selected.id}</span>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '8px' }}>
              <span style={{ color: '#64748b' }}>Khách hàng:</span>
              <span style={{ fontWeight: '700', color: '#0f172a' }}>NDF1110</span>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between' }}>
              <span style={{ color: '#64748b' }}>Tổng tiền:</span>
              <span style={{ fontWeight: '800', color: '#dc2626' }}>{formatPrice(selected.totalAmount)}</span>
            </div>
          </div>

          {/* FORM */}
          <form onSubmit={handleSaveEdit} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
            
            {/* TRẠNG THÁI SELECT */}
            <div>
              <label style={{ display: 'block', fontSize: '13px', fontWeight: '700', color: '#334155', marginBottom: '6px' }}>Trạng thái</label>
              <select
                value={tempStatus}
                onChange={(e) => setTempStatus(e.target.value)}
                style={{ width: '100%', padding: '10px 12px', border: '1px solid #cbd5e1', borderRadius: '8px', fontSize: '13.5px', outline: 'none', background: '#fff' }}
              >
                <option value="PENDING">Đang xử lý</option>
                <option value="CONFIRMED">Đã xác nhận</option>
                <option value="SHIPPED">Đang giao</option>
                <option value="COMPLETED">Đã giao</option>
                <option value="CANCELLED">Đã hủy</option>
              </select>
              <div style={{ display: 'flex', alignItems: 'center', gap: '6px', color: '#64748b', fontSize: '12px', marginTop: '8px', lineHeight: 1.3 }}>
                Cẩn thận khi chuyển sang "Đã hủy" hoặc "Đã giao" vì sẽ ảnh hưởng đến tồn kho.
              </div>
            </div>

            {/* ĐỊA CHỈ GIAO HÀNG */}
            <div>
              <label style={{ display: 'block', fontSize: '13px', fontWeight: '700', color: '#334155', marginBottom: '6px' }}>Địa chỉ Giao hàng</label>
              <input
                type="text"
                value={tempAddress}
                onChange={(e) => setTempAddress(e.target.value)}
                required
                style={{ width: '100%', padding: '10px 12px', border: '1px solid #cbd5e1', borderRadius: '8px', fontSize: '13.5px', outline: 'none' }}
              />
            </div>

            {/* SĐT GIAO HÀNG */}
            <div>
              <label style={{ display: 'block', fontSize: '13px', fontWeight: '700', color: '#334155', marginBottom: '6px' }}>SĐT Giao hàng</label>
              <input
                type="text"
                value={tempPhone}
                onChange={(e) => setTempPhone(e.target.value)}
                required
                style={{ width: '100%', padding: '10px 12px', border: '1px solid #cbd5e1', borderRadius: '8px', fontSize: '13.5px', outline: 'none' }}
              />
            </div>

            {/* NGÀY ĐẶT */}
            <div>
              <label style={{ display: 'block', fontSize: '13px', fontWeight: '700', color: '#334155', marginBottom: '6px' }}>Ngày đặt</label>
              <div style={{ position: 'relative' }}>
                <input
                  type="text"
                  value={formatDateTime(selected.createdAt)}
                  readOnly
                  style={{ width: '100%', padding: '10px 12px', border: '1px solid #cbd5e1', borderRadius: '8px', fontSize: '13.5px', outline: 'none', background: '#f8fafc', color: '#64748b' }}
                />
              </div>
            </div>

            {/* BUTTONS */}
            <div style={{ display: 'flex', flexDirection: 'column', gap: '10px', marginTop: '10px' }}>
              <button
                type="submit"
                disabled={loading}
                style={{
                  width: '100%',
                  padding: '12px',
                  background: '#16a34a',
                  color: '#ffffff',
                  border: 'none',
                  borderRadius: '8px',
                  fontSize: '14px',
                  fontWeight: '800',
                  cursor: 'pointer',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  gap: '6px',
                  boxShadow: '0 4px 12px rgba(22,163,74,0.15)'
                }}
              >
                {loading ? 'Đang lưu...' : 'Lưu'}
              </button>
              
              <button
                type="button"
                onClick={() => {
                  setSelected(null);
                  setIsEditing(false);
                }}
                style={{
                  width: '100%',
                  padding: '12px',
                  background: '#ffffff',
                  color: '#475569',
                  border: '1px solid #cbd5e1',
                  borderRadius: '8px',
                  fontSize: '14px',
                  fontWeight: '700',
                  cursor: 'pointer',
                }}
              >
                Hủy đơn
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

  // State 2: Detailed Order View (Image 2 mockup style)
  if (selected) {
    return (
      <section style={{ padding: '24px', background: '#f8fafc', minHeight: '100vh', fontFamily: 'system-ui, -apple-system, sans-serif' }}>
        {/* HEADER BAR */}
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
          <h1 style={{ fontSize: '20px', fontWeight: '900', color: '#ea580c', margin: 0 }}>
            Chi tiết đơn hàng #DH{selected.id}
          </h1>
          <button
            type="button"
            onClick={() => setSelected(null)}
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

        {/* ALERTS */}
        {error && <div style={{ background: '#fef2f2', color: '#dc2626', border: '1px solid #fecaca', padding: '10px 14px', borderRadius: '8px', marginBottom: '16px', fontSize: '13px', fontWeight: '700' }}>{error}</div>}
        {message && <div style={{ background: '#f0fdf4', color: '#16a34a', border: '1px solid #bbf7d0', padding: '10px 14px', borderRadius: '8px', marginBottom: '16px', fontSize: '13px', fontWeight: '700' }}>{message}</div>}

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '20px', alignItems: 'start' }}>
          
          {/* LEFT COLUMN: GENERAL INFO & RECIPIENT */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
            
            {/* Card: Thông tin chung */}
            <div style={{ background: '#ffffff', borderRadius: '12px', border: '1px solid #e2e8f0', padding: '20px' }}>
              <h3 style={{ fontSize: '14px', fontWeight: '800', color: '#475569', textTransform: 'uppercase', borderBottom: '1px solid #f1f5f9', paddingBottom: '10px', marginTop: 0, marginBottom: '14px' }}>
                Thông tin chung
              </h3>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '10px', fontSize: '13.5px', color: '#334155' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                  <span style={{ color: '#64748b' }}>Ngày đặt:</span>
                  <span style={{ fontWeight: '600' }}>{formatDateTime(selected.createdAt)}</span>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <span style={{ color: '#64748b' }}>Trạng thái:</span>
                  <span
                    style={{
                      background: getOrderStatusInfo(selected.status).bg,
                      color: getOrderStatusInfo(selected.status).color,
                      border: `1px solid ${getOrderStatusInfo(selected.status).border}`,
                      padding: '3px 10px',
                      borderRadius: '8px',
                      fontSize: '12px',
                      fontWeight: '700',
                    }}
                  >
                    {getOrderStatusInfo(selected.status).label}
                  </span>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                  <span style={{ color: '#64748b' }}>Tổng tiền:</span>
                  <span style={{ fontWeight: '800', color: '#dc2626' }}>{formatPrice(selected.totalAmount)}</span>
                </div>
              </div>
            </div>

            {/* Card: Người nhận */}
            <div style={{ background: '#ffffff', borderRadius: '12px', border: '1px solid #e2e8f0', padding: '20px' }}>
              <h3 style={{ fontSize: '14px', fontWeight: '800', color: '#475569', textTransform: 'uppercase', borderBottom: '1px solid #f1f5f9', paddingBottom: '10px', marginTop: 0, marginBottom: '14px' }}>
                Người nhận
              </h3>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '10px', fontSize: '13.5px', color: '#334155' }}>
                <div style={{ fontWeight: '700', color: '#0f172a' }}>
                  Khách hàng ND{selected.userId}
                </div>
                <div style={{ color: '#475569' }}>
                  SĐT: 0398616546
                </div>
                <div style={{ color: '#475569', lineHeight: 1.4 }}>
                  Địa chỉ: {selected.shippingAddress || 'Chưa cập nhật địa chỉ'}
                </div>
              </div>
            </div>

            {/* Card: Trạng thái & Địa chỉ cập nhật */}
            <div style={{ background: '#ffffff', borderRadius: '12px', border: '1px solid #e2e8f0', padding: '20px' }}>
              <button
                type="button"
                onClick={() => setShowStatusModal(!showStatusModal)}
                style={{
                  width: '100%',
                  padding: '12px',
                  background: '#ea580c',
                  color: '#ffffff',
                  border: 'none',
                  borderRadius: '10px',
                  fontSize: '14px',
                  fontWeight: '800',
                  cursor: 'pointer',
                  boxShadow: '0 4px 12px rgba(234,88,12,0.2)',
                }}
              >
                Cập nhật trạng thái
              </button>

              {showStatusModal && (
                <div style={{ marginTop: '16px', background: '#f8fafc', padding: '14px', borderRadius: '8px', border: '1px solid #e2e8f0' }}>
                  <label style={{ display: 'block', fontSize: '12px', fontWeight: '700', color: '#475569', marginBottom: '6px' }}>Ghi chú hành động:</label>
                  <input
                    type="text"
                    value={actionNote}
                    onChange={(e) => setActionNote(e.target.value)}
                    style={{ width: '100%', padding: '8px 10px', border: '1px solid #cbd5e1', borderRadius: '6px', fontSize: '13px', marginBottom: '12px', outline: 'none' }}
                  />
                  <div style={{ display: 'flex', flexWrap: 'wrap', gap: '8px' }}>
                    <button
                      type="button"
                      onClick={() => doAction(() => confirmAdminOrder(selected.id, actionNote), 'Xác nhận đơn hàng thành công.')}
                      style={{ padding: '6px 12px', background: '#2563eb', color: '#fff', border: 'none', borderRadius: '6px', fontSize: '12.5px', fontWeight: '700', cursor: 'pointer' }}
                    >
                      Xác nhận
                    </button>
                    <button
                      type="button"
                      onClick={() => doAction(() => shipAdminOrder(selected.id, actionNote), 'Đã chuyển trạng thái đang giao hàng.')}
                      style={{ padding: '6px 12px', background: '#16a34a', color: '#fff', border: 'none', borderRadius: '6px', fontSize: '12.5px', fontWeight: '700', cursor: 'pointer' }}
                    >
                      Giao hàng
                    </button>
                    <button
                      type="button"
                      onClick={() => doAction(() => completeAdminOrder(selected.id, actionNote), 'Hoàn thành đơn hàng thành công.')}
                      style={{ padding: '6px 12px', background: '#0d9488', color: '#fff', border: 'none', borderRadius: '6px', fontSize: '12.5px', fontWeight: '700', cursor: 'pointer' }}
                    >
                      Hoàn thành
                    </button>
                    <button
                      type="button"
                      onClick={() => doAction(() => cancelAdminOrder(selected.id, actionNote), 'Đã hủy đơn hàng.')}
                      style={{ padding: '6px 12px', background: '#dc2626', color: '#fff', border: 'none', borderRadius: '6px', fontSize: '12.5px', fontWeight: '700', cursor: 'pointer' }}
                    >
                      Hủy đơn
                    </button>
                  </div>

                  <form
                    onSubmit={(e) => {
                      e.preventDefault();
                      doAction(() => updateAdminOrderShipping(selected.id, shippingForm), 'Đã cập nhật địa chỉ giao hàng.');
                    }}
                    style={{ marginTop: '16px', borderTop: '1px solid #cbd5e1', paddingTop: '12px' }}
                  >
                    <label style={{ display: 'block', fontSize: '12px', fontWeight: '700', color: '#475569', marginBottom: '6px' }}>Địa chỉ mới:</label>
                    <input
                      type="text"
                      value={shippingForm.newAddress}
                      onChange={(e) => setShippingForm({ ...shippingForm, newAddress: e.target.value })}
                      required
                      style={{ width: '100%', padding: '8px 10px', border: '1px solid #cbd5e1', borderRadius: '6px', fontSize: '13px', marginBottom: '8px', outline: 'none' }}
                    />
                    <button
                      type="submit"
                      style={{ width: '100%', padding: '8px', background: '#475569', color: '#fff', border: 'none', borderRadius: '6px', fontSize: '12px', fontWeight: '700', cursor: 'pointer' }}
                    >
                      Cập nhật địa chỉ
                    </button>
                  </form>
                </div>
              )}
            </div>

          </div>

          {/* RIGHT COLUMN: PRODUCTS IN ORDER */}
          <div style={{ background: '#ffffff', borderRadius: '12px', border: '1px solid #e2e8f0', padding: '20px', gridColumn: 'span 2' }}>
            <h3 style={{ fontSize: '14px', fontWeight: '800', color: '#475569', textTransform: 'uppercase', borderBottom: '1px solid #f1f5f9', paddingBottom: '10px', marginTop: 0, marginBottom: '14px' }}>
              Sản phẩm trong đơn
            </h3>
            <div style={{ overflowX: 'auto' }}>
              <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '13px' }}>
                <thead>
                  <tr style={{ background: '#f8fafc', color: '#475569', fontWeight: '800', fontSize: '12px', borderBottom: '1px solid #e2e8f0', textTransform: 'uppercase' }}>
                    <th style={{ padding: '12px 14px' }}>Tên sản phẩm</th>
                    <th style={{ padding: '12px 14px', textAlign: 'center', width: '100px' }}>Số lượng</th>
                    <th style={{ padding: '12px 14px', textAlign: 'right', width: '140px' }}>Giá</th>
                    <th style={{ padding: '12px 14px', textAlign: 'right', width: '150px' }}>Thành tiền</th>
                  </tr>
                </thead>
                <tbody>
                  {(selected.items || []).map((item) => (
                    <tr key={item.id} style={{ borderBottom: '1px solid #f1f5f9' }}>
                      <td style={{ padding: '12px 14px' }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                          <img
                            src="https://images.unsplash.com/photo-1596461404969-9ae70f2830c1?w=120&auto=format&fit=crop&q=60"
                            alt="product"
                            style={{ width: '38px', height: '38px', objectFit: 'cover', borderRadius: '6px', border: '1px solid #e2e8f0', flexShrink: 0 }}
                          />
                          <div>
                            <div style={{ fontWeight: '700', color: '#0f172a' }}>{item.productName}</div>
                            <div style={{ fontSize: '11px', color: '#64748b', marginTop: '2px' }}>
                              Phân loại: {item.variantAttributesSnapshot || 'Mặc định'}
                            </div>
                          </div>
                        </div>
                      </td>
                      <td style={{ padding: '12px 14px', textAlign: 'center', fontWeight: '700', color: '#334155' }}>
                        {item.quantity}
                      </td>
                      <td style={{ padding: '12px 14px', textAlign: 'right', color: '#475569' }}>
                        {formatPrice(item.price)}
                      </td>
                      <td style={{ padding: '12px 14px', textAlign: 'right', fontWeight: '700', color: '#0f172a' }}>
                        {formatPrice((item.price || 0) * item.quantity)}
                      </td>
                    </tr>
                  ))}
                  <tr>
                    <td colSpan="3" style={{ padding: '16px 14px', fontWeight: '800', textAlign: 'right', color: '#334155', fontSize: '14px' }}>
                      TỔNG CỘNG:
                    </td>
                    <td style={{ padding: '16px 14px', fontWeight: '900', textAlign: 'right', color: '#dc2626', fontSize: '16px' }}>
                      {formatPrice(selected.totalAmount)}
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

        </div>

        {/* FOOTER */}
        <footer style={{ textAlign: 'center', marginTop: '30px', padding: '16px 0', borderTop: '1px solid #cbd5e1', fontSize: '12px', color: '#94a3b8' }}>
          © 2026 ToyStore Admin Panel
        </footer>
      </section>
    );
  }

  // Otherwise, render list view (Image 1 style)
  return (
    <section style={{ padding: '24px', background: '#f8fafc', minHeight: '100vh', fontFamily: 'system-ui, -apple-system, sans-serif' }}>
      
      {/* HEADER BAR WITH MODE SWITCHER */}
      <div style={{ background: 'linear-gradient(135deg, #fff8f3 0%, #fff1f2 100%)', border: '1px solid #ffedd5', padding: '16px 24px', borderRadius: '16px', marginBottom: '20px', boxShadow: '0 4px 12px rgba(234,88,12,0.04)', display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '16px' }}>
        <div>
          <h1 style={{ fontSize: '20px', fontWeight: '900', color: '#9a3412', margin: 0, textTransform: 'uppercase', letterSpacing: '0.5px' }}>
            Quản lý đơn hàng
          </h1>
          <div style={{ fontSize: '13px', color: dataMode === 'REAL' ? '#15803d' : '#7e22ce', fontWeight: '800', marginTop: '4px' }}>
            {dataMode === 'REAL' ? '🟢 Đang xem: Đơn hàng Kinh doanh Thực tế (Đã lọc đơn test Admin)' : '🧪 Đang xem: Đơn hàng Thử nghiệm Nội bộ (ADMIN/STAFF/MANAGER)'}
          </div>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
          {/* MODE TOGGLE TAB BUTTONS */}
          <div style={{ display: 'inline-flex', background: '#ffffff', padding: '4px', borderRadius: '12px', border: '2px solid #fed7aa', boxShadow: '0 2px 8px rgba(0,0,0,0.06)' }}>
            <button
              type="button"
              onClick={() => setDataMode('REAL')}
              style={{
                padding: '8px 16px',
                borderRadius: '8px',
                border: 'none',
                fontSize: '13px',
                fontWeight: '900',
                cursor: 'pointer',
                background: dataMode === 'REAL' ? 'linear-gradient(135deg, #16a34a, #15803d)' : 'transparent',
                color: dataMode === 'REAL' ? '#ffffff' : '#64748b',
                boxShadow: dataMode === 'REAL' ? '0 2px 8px rgba(22,163,74,0.3)' : 'none',
                transition: 'all 0.2s ease',
              }}
            >
              🟢 Đơn Kinh doanh ({orders.filter(o => !isInternalTestOrder(o)).length})
            </button>
            <button
              type="button"
              onClick={() => setDataMode('TEST')}
              style={{
                padding: '8px 16px',
                borderRadius: '8px',
                border: 'none',
                fontSize: '13px',
                fontWeight: '900',
                cursor: 'pointer',
                background: dataMode === 'TEST' ? 'linear-gradient(135deg, #9333ea, #7e22ce)' : 'transparent',
                color: dataMode === 'TEST' ? '#ffffff' : '#64748b',
                boxShadow: dataMode === 'TEST' ? '0 2px 8px rgba(147,51,234,0.3)' : 'none',
                transition: 'all 0.2s ease',
              }}
            >
              🧪 Đơn Thử nghiệm ({orders.filter(o => isInternalTestOrder(o)).length})
            </button>
          </div>
        </div>
      </div>

      {/* ALERTS */}
      {error && <div style={{ background: '#fef2f2', color: '#dc2626', border: '1px solid #fecaca', padding: '10px 14px', borderRadius: '8px', marginBottom: '16px', fontSize: '13px', fontWeight: '700' }}>{error}</div>}
      {message && <div style={{ background: '#f0fdf4', color: '#16a34a', border: '1px solid #bbf7d0', padding: '10px 14px', borderRadius: '8px', marginBottom: '16px', fontSize: '13px', fontWeight: '700' }}>{message}</div>}

      {/* FILTER BAR */}
      <form
        onSubmit={(e) => {
          e.preventDefault();
          setPage(0);
          loadOrders(undefined, 0);
        }}
        style={{ background: '#ffffff', padding: '14px 16px', borderRadius: '12px', border: '1px solid #e2e8f0', marginBottom: '16px', display: 'flex', gap: '10px', alignItems: 'center', flexWrap: 'wrap' }}
      >
        <div style={{ flex: '1', minWidth: '180px' }}>
          <input
            type="text"
            placeholder="Tìm theo Mã KH..."
            value={filters.userId}
            onChange={(e) => setFilters({ ...filters, userId: e.target.value })}
            style={{ width: '100%', padding: '9px 12px', border: '1px solid #cbd5e1', borderRadius: '8px', fontSize: '13px', outline: 'none' }}
          />
        </div>

        <div style={{ flex: '1', minWidth: '180px' }}>
          <select
            value={filters.status}
            onChange={(e) => setFilters({ ...filters, status: e.target.value })}
            style={{ width: '100%', padding: '9px 12px', border: '1px solid #cbd5e1', borderRadius: '8px', fontSize: '13px', outline: 'none', background: '#fff' }}
          >
            <option value="">Tất cả trạng thái</option>
            <option value="PENDING">Đang xử lý (PENDING)</option>
            <option value="CONFIRMED">Đã xác nhận (CONFIRMED)</option>
            <option value="SHIPPED">Đang giao (SHIPPED)</option>
            <option value="COMPLETED">Đã giao (COMPLETED)</option>
            <option value="CANCELLED">Đã hủy (CANCELLED)</option>
          </select>
        </div>

        <div style={{ display: 'flex', gap: '8px' }}>
          <button
            type="submit"
            style={{ padding: '9px 20px', background: '#ea580c', color: '#ffffff', border: 'none', borderRadius: '8px', fontSize: '13px', fontWeight: '700', cursor: 'pointer' }}
          >
            Lọc
          </button>
          <button
            type="button"
            onClick={handleClearFilters}
            style={{ padding: '9px 14px', background: '#f1f5f9', color: '#475569', border: '1px solid #cbd5e1', borderRadius: '8px', fontSize: '13px', fontWeight: '700', cursor: 'pointer' }}
          >
            Xóa lọc
          </button>
        </div>
      </form>

      {/* ORDERS TABLE */}
      <div style={{ background: '#ffffff', borderRadius: '12px', border: '1px solid #e2e8f0', overflow: 'hidden' }}>
        <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '13px' }}>
          <thead>
            <tr style={{ background: '#f8fafc', color: '#475569', fontWeight: '800', fontSize: '12px', borderBottom: '1px solid #e2e8f0', textTransform: 'uppercase' }}>
              <th style={{ padding: '14px 16px', width: '90px' }}>Mã ĐH</th>
              <th style={{ padding: '14px 16px', width: '140px' }}>Ngày đặt</th>
              <th style={{ padding: '14px 16px', width: '120px' }}>Mã KH</th>
              <th style={{ padding: '14px 16px', textAlign: 'right', width: '140px' }}>Tổng tiền</th>
              <th style={{ padding: '14px 16px', width: '160px' }}>Trạng thái</th>
              <th style={{ padding: '14px 16px', width: '160px', textAlign: 'center' }}>Thao tác</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr>
                <td colSpan="6" style={{ padding: '36px', textAlign: 'center', color: '#64748b' }}>
                  Đang tải danh sách đơn hàng...
                </td>
              </tr>
            ) : displayOrders.length === 0 ? (
              <tr>
                <td colSpan="6" style={{ padding: '36px', textAlign: 'center', color: '#94a3b8' }}>
                  {dataMode === 'REAL' ? 'Không có đơn hàng kinh doanh thực tế nào.' : 'Không có đơn hàng thử nghiệm nội bộ nào.'}
                </td>
              </tr>
            ) : (
              displayOrders.map((order, idx) => {
                const statusInfo = getOrderStatusInfo(order.status);
                return (
                  <tr
                    key={order.id}
                    style={{
                      borderBottom: '1px solid #f1f5f9',
                      background: idx % 2 === 0 ? '#ffffff' : '#fafafa',
                    }}
                  >
                    {/* Mã ĐH */}
                    <td style={{ padding: '14px 16px', fontWeight: '800', color: '#ea580c' }}>
                      DH{order.id}
                    </td>

                    {/* Ngày đặt */}
                    <td style={{ padding: '14px 16px', color: '#334155' }}>
                      {formatDateTime(order.createdAt)}
                    </td>

                    {/* Mã KH */}
                    <td style={{ padding: '14px 16px' }}>
                      <div style={{ display: 'flex', flexDirection: 'column', gap: '4px', alignItems: 'flex-start' }}>
                        <span
                          style={{
                            background: '#f1f5f9',
                            color: '#475569',
                            border: '1px solid #cbd5e1',
                            padding: '4px 10px',
                            borderRadius: '6px',
                            fontSize: '12px',
                            fontWeight: '700',
                          }}
                        >
                          ND{order.userId}
                        </span>
                        {(order.isTestOrder || ['ADMIN', 'MANAGER', 'STAFF'].includes(typeof order.userRole === 'object' ? order.userRole?.code : order.userRole)) && (
                          <span style={{ background: '#fff7ed', color: '#ea580c', border: '1px solid #fed7aa', padding: '2px 6px', borderRadius: '6px', fontSize: '10.5px', fontWeight: '800' }}>
                            🧪 Dữ liệu mẫu
                          </span>
                        )}
                      </div>
                    </td>

                    {/* Tổng tiền */}
                    <td style={{ padding: '14px 16px', textAlign: 'right', fontWeight: '800', color: '#dc2626' }}>
                      {formatPrice(order.totalAmount)}
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
                        {statusInfo.label || 'Chưa xác định'}
                      </span>
                    </td>

                    {/* Thao tác */}
                    <td style={{ padding: '14px 16px', textAlign: 'center' }}>
                      <div style={{ display: 'inline-flex', gap: '8px', justifyContent: 'center' }}>
                        {/* Eye Button */}
                        <button
                          type="button"
                          onClick={() => selectOrder(order)}
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
                          onClick={() => {
                            editOrder(order);
                          }}
                          title="Cập nhật trạng thái"
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
                            onClick={() => doAction(() => deleteAdminOrder(order.id), 'Đã xóa đơn hàng.')}
                            title="Xóa đơn hàng"
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

      {totalPages > 1 && (
        <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', gap: '12px', marginTop: '16px' }}>
          <button
            type="button"
            disabled={page <= 0 || loading}
            onClick={() => loadOrders(undefined, page - 1)}
            style={{ padding: '8px 14px', border: '1px solid #cbd5e1', borderRadius: '8px', background: '#fff', cursor: page <= 0 ? 'not-allowed' : 'pointer', opacity: page <= 0 ? .5 : 1 }}
          >Trước</button>
          <span style={{ color: '#475569', fontSize: '13px', fontWeight: '700' }}>
            Trang {page + 1} / {totalPages}
          </span>
          <button
            type="button"
            disabled={page >= totalPages - 1 || loading}
            onClick={() => loadOrders(undefined, page + 1)}
            style={{ padding: '8px 14px', border: '1px solid #cbd5e1', borderRadius: '8px', background: '#fff', cursor: page >= totalPages - 1 ? 'not-allowed' : 'pointer', opacity: page >= totalPages - 1 ? .5 : 1 }}
          >Sau</button>
        </div>
      )}

      {/* FOOTER */}
      <footer style={{ textAlign: 'center', marginTop: '30px', padding: '16px 0', borderTop: '1px solid #cbd5e1', fontSize: '12px', color: '#94a3b8' }}>
        © 2026 ToyStore Admin Panel
      </footer>
    </section>
  );
}

export default AdminOrderPage;
