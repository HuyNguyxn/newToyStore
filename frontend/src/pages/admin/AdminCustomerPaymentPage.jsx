import { useEffect, useMemo, useState } from 'react';
import { useOutletContext, useSearchParams } from 'react-router-dom';
import {
  cancelAdminCustomerPayment,
  deleteAdminCustomerPayment,
  getAdminCustomerPaymentDetails,
  getAdminCustomerPaymentRefunds,
  getAdminCustomerPayments,
  markCustomerPaymentFailed,
  markCustomerPaymentSucceeded,
} from '../../services/adminCustomerPaymentService.js';
import { formatDateTime, formatPaymentMethodText, formatPrice } from '../../utils/formatters.js';

function enumCode(value) {
  if (value && typeof value === 'object') return String(value.code || value.name || '').toUpperCase();
  return String(value || '').toUpperCase();
}

function paymentStatusInfo(status) {
  const code = enumCode(status);
  if (code === 'SUCCEEDED') return { label: 'Thành công', bg: '#dcfce7', color: '#15803d', border: '#bbf7d0' };
  if (code === 'PENDING') return { label: 'Chờ thanh toán', bg: '#fef3c7', color: '#d97706', border: '#fde68a' };
  if (code === 'FAILED') return { label: 'Thất bại', bg: '#fee2e2', color: '#dc2626', border: '#fecaca' };
  if (code === 'CANCELLED') return { label: 'Đã hủy', bg: '#f1f5f9', color: '#64748b', border: '#cbd5e1' };
  if (code === 'EXPIRED') return { label: 'Hết hạn', bg: '#f8fafc', color: '#475569', border: '#cbd5e1' };
  if (code === 'REFUND_PENDING') return { label: 'Chờ hoàn tiền', bg: '#fffbeb', color: '#b45309', border: '#fed7aa' };
  if (code === 'REFUNDED') return { label: 'Đã hoàn tiền', bg: '#e0f2fe', color: '#0369a1', border: '#bae6fd' };
  if (code === 'REFUND_FAILED') return { label: 'Hoàn tiền lỗi', bg: '#fee2e2', color: '#b91c1c', border: '#fecaca' };
  return { label: code || 'Chưa xác định', bg: '#f1f5f9', color: '#475569', border: '#cbd5e1' };
}

function refundStatusInfo(status) {
  const code = enumCode(status);
  if (code === 'SUCCEEDED') return { label: 'Thành công', color: '#15803d' };
  if (code === 'PENDING') return { label: 'Chờ xử lý', color: '#d97706' };
  if (code === 'PROCESSING') return { label: 'Đang xử lý', color: '#2563eb' };
  if (code === 'REJECTED') return { label: 'Từ chối', color: '#dc2626' };
  if (code === 'FAILED') return { label: 'Thất bại', color: '#dc2626' };
  return { label: code || 'Chưa rõ', color: '#64748b' };
}

function methodBadge(method) {
  const code = enumCode(method);
  if (code === 'VNPAY') return { bg: '#eff6ff', color: '#2563eb', border: '#bfdbfe' };
  if (code === 'COD') return { bg: '#f1f5f9', color: '#475569', border: '#cbd5e1' };
  return { bg: '#faf5ff', color: '#9333ea', border: '#e9d5ff' };
}

function AdminCustomerPaymentPage() {
  const context = useOutletContext() || {};
  const userRole = context.userRole || 'STAFF';
  const [searchParams] = useSearchParams();
  const initialStatus = searchParams.get('status') || '';
  const canDelete = userRole === 'ADMIN';

  const [payments, setPayments] = useState([]);
  const [selected, setSelected] = useState(null);
  const [refunds, setRefunds] = useState([]);
  const [filters, setFilters] = useState({ status: initialStatus, method: '', orderId: '', userId: '' });
  const [providerTransactionId, setProviderTransactionId] = useState('');
  const [reason, setReason] = useState('Cập nhật từ trang quản trị');
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const displayPayments = useMemo(() => payments, [payments]);

  useEffect(() => {
    loadPayments(initialStatus);
  }, []);

  async function loadPayments(overrideStatus) {
    setLoading(true);
    setError('');
    const targetStatus = overrideStatus !== undefined ? overrideStatus : filters.status;
    try {
      const result = await getAdminCustomerPayments({
        status: targetStatus || undefined,
        method: filters.method || undefined,
        orderId: filters.orderId || undefined,
        userId: filters.userId || undefined,
        page: 0,
        size: 50,
        sort: 'createdAt,desc',
      });
      setPayments(result.content || result || []);
    } catch (err) {
      setPayments([]);
      setError(err.message || 'Không thể tải danh sách giao dịch thanh toán.');
    } finally {
      setLoading(false);
    }
  }

  async function selectPayment(payment) {
    setSelected(payment);
    setProviderTransactionId(payment.providerTransactionId || '');
    setRefunds([]);
    try {
      const [detail, refundPage] = await Promise.all([
        getAdminCustomerPaymentDetails(payment.id),
        getAdminCustomerPaymentRefunds(payment.id, { page: 0, size: 10, sort: 'createdAt,desc' }),
      ]);
      setSelected(detail);
      setProviderTransactionId(detail.providerTransactionId || '');
      setRefunds(refundPage.content || refundPage || []);
    } catch (err) {
      setError(err.message || 'Không thể tải chi tiết giao dịch.');
      setRefunds([]);
    }
  }

  async function doAction(action, successMessage) {
    setError('');
    setMessage('');
    try {
      const result = await action();
      setMessage(successMessage);
      if (result) {
        setSelected(result);
        setProviderTransactionId(result.providerTransactionId || '');
      }
      await loadPayments();
    } catch (err) {
      setError(err.message || 'Thao tác giao dịch thất bại.');
    }
  }

  function clearFilters() {
    const nextFilters = { status: '', method: '', orderId: '', userId: '' };
    setFilters(nextFilters);
    setTimeout(() => loadPayments(''), 0);
  }

  const selectedStatus = enumCode(selected?.status);
  const selectedMethod = enumCode(selected?.method);
  const canMarkSucceeded = selectedStatus === 'PENDING' && selectedMethod !== 'COD';
  const canMarkFailed = selectedStatus === 'PENDING';
  const canCancel = selectedStatus === 'PENDING';
  const canDeleteSelected = canDelete && ['FAILED', 'CANCELLED', 'EXPIRED'].includes(selectedStatus);

  return (
    <section style={{ padding: 24, background: '#f8fafc', minHeight: '100vh', fontFamily: 'system-ui, -apple-system, sans-serif' }}>
      <div style={{ background: 'linear-gradient(135deg, #fff8f3 0%, #fff1f2 100%)', border: '1px solid #ffedd5', padding: '16px 24px', borderRadius: 16, marginBottom: 20, boxShadow: '0 4px 12px rgba(234,88,12,0.04)', display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 16 }}>
        <div>
          <h1 style={{ fontSize: 20, fontWeight: 900, color: '#9a3412', margin: 0, textTransform: 'uppercase', letterSpacing: .5 }}>
            Quản lý giao dịch thanh toán
          </h1>
          <div style={{ fontSize: 13, color: '#15803d', fontWeight: 800, marginTop: 4 }}>
            Hiển thị toàn bộ giao dịch do hệ thống ghi nhận
          </div>
        </div>
        <div style={{ color: '#475569', fontSize: 13, fontWeight: 800 }}>
          {displayPayments.length} giao dịch
        </div>
      </div>

      {error && <div style={{ background: '#fef2f2', color: '#dc2626', border: '1px solid #fecaca', padding: '10px 14px', borderRadius: 8, marginBottom: 16, fontSize: 13, fontWeight: 700 }}>{error}</div>}
      {message && <div style={{ background: '#f0fdf4', color: '#16a34a', border: '1px solid #bbf7d0', padding: '10px 14px', borderRadius: 8, marginBottom: 16, fontSize: 13, fontWeight: 700 }}>{message}</div>}

      <form onSubmit={(event) => { event.preventDefault(); loadPayments(); }} style={{ background: '#ffffff', padding: '14px 16px', borderRadius: 12, border: '1px solid #e2e8f0', marginBottom: 16, display: 'flex', gap: 10, alignItems: 'center', flexWrap: 'wrap' }}>
        <input type="number" placeholder="Mã đơn hàng..." value={filters.orderId} onChange={(event) => setFilters({ ...filters, orderId: event.target.value })} style={{ flex: 1, minWidth: 150, padding: '9px 12px', border: '1px solid #cbd5e1', borderRadius: 8, fontSize: 13, outline: 'none' }} />
        <input type="number" placeholder="Mã khách hàng..." value={filters.userId} onChange={(event) => setFilters({ ...filters, userId: event.target.value })} style={{ flex: 1, minWidth: 150, padding: '9px 12px', border: '1px solid #cbd5e1', borderRadius: 8, fontSize: 13, outline: 'none' }} />
        <select value={filters.method} onChange={(event) => setFilters({ ...filters, method: event.target.value })} style={{ flex: 1, minWidth: 170, padding: '9px 12px', border: '1px solid #cbd5e1', borderRadius: 8, fontSize: 13, outline: 'none', background: '#fff' }}>
          <option value="">T?t c? phuong th?c</option>
          <option value="COD">Thanh toán khi nhận hàng (COD)</option>
          <option value="VNPAY">Cổng VNPAY</option>
        </select>
        <select value={filters.status} onChange={(event) => setFilters({ ...filters, status: event.target.value })} style={{ flex: 1, minWidth: 180, padding: '9px 12px', border: '1px solid #cbd5e1', borderRadius: 8, fontSize: 13, outline: 'none', background: '#fff' }}>
          <option value="">Tất cả trạng thái</option>
          <option value="PENDING">Chờ thanh toán</option>
          <option value="SUCCEEDED">Thành công</option>
          <option value="FAILED">Thất bại</option>
          <option value="CANCELLED">Đã hủy</option>
          <option value="EXPIRED">Hết hạn</option>
          <option value="REFUND_PENDING">Chờ hoàn tiền</option>
          <option value="REFUNDED">Đã hoàn tiền</option>
          <option value="REFUND_FAILED">Hoàn tiền lỗi</option>
        </select>
        <button type="submit" style={{ padding: '9px 20px', background: '#ea580c', color: '#ffffff', border: 'none', borderRadius: 8, fontSize: 13, fontWeight: 700, cursor: 'pointer' }}>
          L?c
        </button>
        <button type="button" onClick={clearFilters} style={{ padding: '9px 14px', background: '#f1f5f9', color: '#475569', border: '1px solid #cbd5e1', borderRadius: 8, fontSize: 13, fontWeight: 700, cursor: 'pointer' }}>
          Xóa lọc
        </button>
      </form>

      <div style={{ display: 'grid', gridTemplateColumns: selected ? '1.5fr 1fr' : '1fr', gap: 20, alignItems: 'start' }}>
        <div style={{ background: '#ffffff', borderRadius: 12, border: '1px solid #e2e8f0', overflowX: 'auto' }}>
          <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: 13 }}>
            <thead>
              <tr style={{ background: '#f8fafc', color: '#475569', fontWeight: 800, fontSize: 12, borderBottom: '1px solid #e2e8f0', textTransform: 'uppercase' }}>
                <th style={{ padding: '14px 16px' }}>Mã GD</th>
                <th style={{ padding: '14px 16px' }}>Mã đơn</th>
                <th style={{ padding: '14px 16px' }}>Mã KH</th>
                <th style={{ padding: '14px 16px' }}>Phuong th?c</th>
                <th style={{ padding: '14px 16px' }}>Trạng thái</th>
                <th style={{ padding: '14px 16px', textAlign: 'right' }}>S? ti?n</th>
                <th style={{ padding: '14px 16px' }}>Ngày tạo</th>
                <th style={{ padding: '14px 16px', textAlign: 'center' }}>Thao tác</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr><td colSpan="8" style={{ padding: 36, textAlign: 'center', color: '#64748b' }}>Đang tải danh sách giao dịch...</td></tr>
              ) : displayPayments.length === 0 ? (
                <tr><td colSpan="8" style={{ padding: 36, textAlign: 'center', color: '#94a3b8' }}>Không có giao dịch thanh toán phù hợp.</td></tr>
              ) : (
                displayPayments.map((payment, index) => {
                  const status = paymentStatusInfo(payment.status);
                  const method = methodBadge(payment.method);
                  return (
                    <tr key={payment.id} style={{ borderBottom: '1px solid #f1f5f9', background: index % 2 === 0 ? '#ffffff' : '#fafafa' }}>
                      <td style={{ padding: '14px 16px', fontWeight: 700, color: '#334155' }}>#{payment.id}</td>
                      <td style={{ padding: '14px 16px', fontWeight: 800, color: '#ea580c' }}>DH{payment.orderId}</td>
                      <td style={{ padding: '14px 16px', color: '#475569' }}>KH{payment.userId}</td>
                      <td style={{ padding: '14px 16px' }}>
                        <span style={{ background: method.bg, color: method.color, border: `1px solid ${method.border}`, padding: '3px 8px', borderRadius: 6, fontSize: 12, fontWeight: 800 }}>
                          {formatPaymentMethodText(payment.method)}
                        </span>
                      </td>
                      <td style={{ padding: '14px 16px' }}>
                        <span style={{ background: status.bg, color: status.color, border: `1px solid ${status.border}`, padding: '3px 10px', borderRadius: 8, fontSize: 12, fontWeight: 800 }}>
                          {status.label}
                        </span>
                      </td>
                      <td style={{ padding: '14px 16px', textAlign: 'right', fontWeight: 900, color: '#dc2626' }}>{formatPrice(payment.amount)}</td>
                      <td style={{ padding: '14px 16px', color: '#64748b' }}>{formatDateTime(payment.createdAt)}</td>
                      <td style={{ padding: '14px 16px', textAlign: 'center' }}>
                        <button type="button" onClick={() => selectPayment(payment)} style={{ padding: '6px 12px', background: '#ffffff', color: '#475569', border: '1px solid #cbd5e1', borderRadius: 6, cursor: 'pointer', fontSize: 12.5, fontWeight: 800 }}>
                          Quản lý
                        </button>
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>

        {selected && (
          <aside style={{ background: '#ffffff', borderRadius: 12, border: '1px solid #e2e8f0', padding: 24, boxShadow: '0 4px 16px rgba(0,0,0,0.02)', display: 'flex', flexDirection: 'column', gap: 16 }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: '1px solid #f1f5f9', paddingBottom: 12 }}>
              <h3 style={{ fontSize: 15, fontWeight: 900, color: '#0f172a', margin: 0 }}>Chi tiết giao dịch #{selected.id}</h3>
              <button type="button" onClick={() => setSelected(null)} style={{ border: 'none', background: '#f1f5f9', borderRadius: 6, width: 28, height: 28, cursor: 'pointer', fontWeight: 900 }}>×</button>
            </div>

            <div style={{ background: '#f8fafc', padding: 14, borderRadius: 8, border: '1px solid #e2e8f0', display: 'grid', gap: 10, fontSize: 13 }}>
              <div style={{ display: 'flex', justifyContent: 'space-between' }}><span style={{ color: '#64748b' }}>Mã đơn:</span><strong>DH{selected.orderId}</strong></div>
              <div style={{ display: 'flex', justifyContent: 'space-between' }}><span style={{ color: '#64748b' }}>Mã khách:</span><strong>KH{selected.userId}</strong></div>
              <div style={{ display: 'flex', justifyContent: 'space-between' }}><span style={{ color: '#64748b' }}>Trạng thái:</span><strong style={{ color: paymentStatusInfo(selected.status).color }}>{paymentStatusInfo(selected.status).label}</strong></div>
              <div style={{ display: 'flex', justifyContent: 'space-between' }}><span style={{ color: '#64748b' }}>Phuong th?c:</span><strong>{formatPaymentMethodText(selected.method)}</strong></div>
              <div style={{ display: 'flex', justifyContent: 'space-between' }}><span style={{ color: '#64748b' }}>S? ti?n:</span><strong style={{ color: '#dc2626' }}>{formatPrice(selected.amount)}</strong></div>
              {selected.providerTransactionId && <div style={{ display: 'flex', justifyContent: 'space-between', gap: 10 }}><span style={{ color: '#64748b' }}>Mã đối tác:</span><strong style={{ color: '#334155' }}>{selected.providerTransactionId}</strong></div>}
              {selected.failureReason && <div style={{ color: '#dc2626' }}>Lý do thất bại: {selected.failureReason}</div>}
              {selected.cancelReason && <div style={{ color: '#64748b' }}>Lý do hủy: {selected.cancelReason}</div>}
            </div>

            <div style={{ display: 'grid', gap: 12 }}>
              <label style={{ fontSize: 12, fontWeight: 800, color: '#475569' }}>Mã giao dịch đối tác</label>
              <input type="text" placeholder="Nhập mã giao dịch VNPAY..." value={providerTransactionId} onChange={(event) => setProviderTransactionId(event.target.value)} style={{ width: '100%', padding: '9px 12px', border: '1px solid #cbd5e1', borderRadius: 8, fontSize: 13, outline: 'none' }} />
              <label style={{ fontSize: 12, fontWeight: 800, color: '#475569' }}>Lý do cập nhật</label>
              <input type="text" value={reason} onChange={(event) => setReason(event.target.value)} style={{ width: '100%', padding: '9px 12px', border: '1px solid #cbd5e1', borderRadius: 8, fontSize: 13, outline: 'none' }} />
            </div>

            <div style={{ display: 'grid', gap: 8 }}>
              {selectedStatus === 'PENDING' && selectedMethod === 'COD' && (
                <div style={{ background: '#fffbeb', color: '#92400e', border: '1px solid #fde68a', borderRadius: 8, padding: 10, fontSize: 12.5, fontWeight: 700 }}>
                  COD sẽ được ghi nhận thành công khi đơn giao hàng thành công và thu tiền. Không xác nhận thủ công ở đây.
                </div>
              )}
              {canMarkSucceeded && (
                <button type="button" onClick={() => doAction(() => markCustomerPaymentSucceeded(selected.id, providerTransactionId), 'Đã xác nhận giao dịch thành công.')} style={{ width: '100%', padding: 11, background: '#16a34a', color: '#ffffff', border: 'none', borderRadius: 8, fontSize: 13.5, fontWeight: 900, cursor: 'pointer' }}>
                  Xác nhận thành công
                </button>
              )}
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 8 }}>
                {canMarkFailed && <button type="button" onClick={() => doAction(() => markCustomerPaymentFailed(selected.id, reason), 'Đã cập nhật giao dịch thất bại.')} style={{ padding: 9, background: '#ffffff', color: '#dc2626', border: '1px solid #fecaca', borderRadius: 8, fontSize: 12.5, fontWeight: 800, cursor: 'pointer' }}>Thất bại</button>}
                {canCancel && <button type="button" onClick={() => doAction(() => cancelAdminCustomerPayment(selected.id, reason), 'Đã hủy giao dịch.')} style={{ padding: 9, background: '#ffffff', color: '#475569', border: '1px solid #cbd5e1', borderRadius: 8, fontSize: 12.5, fontWeight: 800, cursor: 'pointer' }}>Hủy bỏ</button>}
              </div>
              {canDeleteSelected && <button type="button" onClick={() => doAction(() => deleteAdminCustomerPayment(selected.id), 'Đã xóa giao dịch thanh toán.')} style={{ width: '100%', padding: 10, background: '#fef2f2', color: '#dc2626', border: '1px solid #fecaca', borderRadius: 8, fontSize: 13, fontWeight: 900, cursor: 'pointer' }}>Xóa giao dịch</button>}
            </div>

            <div style={{ borderTop: '1px solid #f1f5f9', paddingTop: 16, marginTop: 4 }}>
              <strong style={{ fontSize: 13, color: '#0f172a', display: 'block', marginBottom: 10 }}>Yêu cầu hoàn tiền</strong>
              {refunds.length === 0 ? (
                <div style={{ fontSize: 12, color: '#94a3b8', textAlign: 'center', padding: '10px 0' }}>Chưa có yêu cầu hoàn tiền nào.</div>
              ) : (
                <div style={{ display: 'grid', gap: 8 }}>
                  {refunds.map((refund) => {
                    const status = refundStatusInfo(refund.status);
                    return (
                      <div key={refund.id} style={{ background: '#f8fafc', border: '1px solid #e2e8f0', padding: '10px 12px', borderRadius: 8, fontSize: 12 }}>
                        <div style={{ display: 'flex', justifyContent: 'space-between', fontWeight: 800, color: '#334155', marginBottom: 4 }}>
                          <span>Hoàn tiền #{refund.id}</span>
                          <span style={{ color: status.color }}>{status.label}</span>
                        </div>
                        <div style={{ color: '#dc2626', fontWeight: 800, marginBottom: 2 }}>{formatPrice(refund.amount)}</div>
                        <div style={{ color: '#64748b' }}>Lý do: {refund.reason || 'Không có lý do'}</div>
                      </div>
                    );
                  })}
                </div>
              )}
            </div>
          </aside>
        )}
      </div>
    </section>
  );
}

export default AdminCustomerPaymentPage;
