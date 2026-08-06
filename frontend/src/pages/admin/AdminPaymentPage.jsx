import { useEffect, useState } from 'react';
import { useOutletContext } from 'react-router-dom';
import {
  cancelAdminPayment,
  deleteAdminPayment,
  getAdminPaymentDetails,
  getAdminPaymentRefunds,
  getAdminPayments,
  markPaymentFailed,
  markPaymentSucceeded,
} from '../../services/adminPaymentService.js';
import { formatDateTime, formatPrice } from '../../utils/formatters.js';

// Status badge styling helper
function getPaymentStatusInfo(status) {
  const code = status && typeof status === 'object' ? status.code : status;
  const statusStr = String(code || '').toUpperCase();
  if (statusStr === 'COMPLETED' || statusStr === 'SUCCESS' || statusStr === 'SUCCEEDED') {
    return { label: 'Thành công', bg: '#d1fae5', color: '#10b981', border: '#a7f3d0' };
  }
  if (statusStr === 'PENDING') {
    return { label: 'Đang xử lý', bg: '#fef3c7', color: '#d97706', border: '#fde68a' };
  }
  if (statusStr === 'FAILED' || statusStr === 'FAIL') {
    return { label: 'Thất bại', bg: '#fee2e2', color: '#dc2626', border: '#fecaca' };
  }
  if (statusStr === 'CANCELLED' || statusStr === 'CANCEL') {
    return { label: 'Đã hủy', bg: '#f1f5f9', color: '#64748b', border: '#cbd5e1' };
  }
  return { label: statusStr, bg: '#f1f5f9', color: '#475569', border: '#cbd5e1' };
}

// Payment method badge helper
function getMethodBadgeStyle(method) {
  const methodStr = String(method || '').toUpperCase();
  if (methodStr === 'VNPAY') {
    return { bg: '#eff6ff', color: '#2563eb', border: '#bfdbfe' };
  }
  if (methodStr === 'COD') {
    return { bg: '#f1f5f9', color: '#475569', border: '#cbd5e1' };
  }
  return { bg: '#faf5ff', color: '#9333ea', border: '#e9d5ff' };
}

function AdminPaymentPage() {
  const { userRole } = useOutletContext();
  const canDelete = userRole === 'MANAGER' || userRole === 'ADMIN';

  const [payments, setPayments] = useState([]);
  const [selected, setSelected] = useState(null);
  const [refunds, setRefunds] = useState([]);
  const [filters, setFilters] = useState({ status: '', method: '', orderId: '' });
  const [providerTransactionId, setProviderTransactionId] = useState('');
  const [reason, setReason] = useState('Cập nhật từ trang quản trị');
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    loadPayments();
  }, []);

  async function loadPayments() {
    setLoading(true);
    setError('');
    try {
      const result = await getAdminPayments({
        status: filters.status || undefined,
        method: filters.method || undefined,
        orderId: filters.orderId || undefined,
        page: 0,
        size: 50,
        sort: 'createdAt,desc',
      });
      setPayments(result.content || result || []);
    } catch (err) {
      setError(err.message || 'Không thể tải danh sách giao dịch.');
      setPayments([]);
    } finally {
      setLoading(false);
    }
  }

  async function selectPayment(payment) {
    setSelected(payment);
    setProviderTransactionId(payment.providerTransactionId || '');
    try {
      const [detail, refundPage] = await Promise.all([
        getAdminPaymentDetails(payment.id),
        getAdminPaymentRefunds(payment.id, { page: 0, size: 10 }),
      ]);
      setSelected(detail);
      setRefunds(refundPage.content || refundPage || []);
    } catch {
      setRefunds([]);
    }
  }

  async function doAction(action, successMsg) {
    setError('');
    setMessage('');
    try {
      const result = await action();
      setMessage(successMsg);
      if (result) setSelected(result);
      await loadPayments();
    } catch (err) {
      setError(err.message || 'Thao tác giao dịch thất bại.');
    }
  }

  const handleClearFilters = () => {
    setFilters({ status: '', method: '', orderId: '' });
    setTimeout(() => {
      loadPayments();
    }, 50);
  };

  return (
    <section style={{ padding: '24px', background: '#f8fafc', minHeight: '100vh', fontFamily: 'system-ui, -apple-system, sans-serif' }}>
      
      {/* HEADER ROW */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
        <h1 style={{ fontSize: '18px', fontWeight: '800', color: '#0f172a', margin: 0, textTransform: 'uppercase', letterSpacing: '0.5px' }}>
          Quản lý giao dịch thanh toán
        </h1>
        <div style={{ background: '#fff7ed', color: '#ea580c', border: '1px solid #ffedd5', padding: '6px 16px', borderRadius: '20px', fontSize: '13px', fontWeight: '700' }}>
          Tổng giao dịch: {payments.length}
        </div>
      </div>

      {/* ALERTS */}
      {error && <div style={{ background: '#fef2f2', color: '#dc2626', border: '1px solid #fecaca', padding: '10px 14px', borderRadius: '8px', marginBottom: '16px', fontSize: '13px', fontWeight: '700' }}>{error}</div>}
      {message && <div style={{ background: '#f0fdf4', color: '#16a34a', border: '1px solid #bbf7d0', padding: '10px 14px', borderRadius: '8px', marginBottom: '16px', fontSize: '13px', fontWeight: '700' }}>{message}</div>}

      {/* FILTER BAR */}
      <form
        onSubmit={(e) => {
          e.preventDefault();
          loadPayments();
        }}
        style={{ background: '#ffffff', padding: '14px 16px', borderRadius: '12px', border: '1px solid #e2e8f0', marginBottom: '16px', display: 'flex', gap: '10px', alignItems: 'center', flexWrap: 'wrap' }}
      >
        <div style={{ flex: '1', minWidth: '160px' }}>
          <input
            type="text"
            placeholder="Mã đơn hàng..."
            value={filters.orderId}
            onChange={(e) => setFilters({ ...filters, orderId: e.target.value })}
            style={{ width: '100%', padding: '9px 12px', border: '1px solid #cbd5e1', borderRadius: '8px', fontSize: '13px', outline: 'none' }}
          />
        </div>

        <div style={{ flex: '1', minWidth: '160px' }}>
          <select
            value={filters.method}
            onChange={(e) => setFilters({ ...filters, method: e.target.value })}
            style={{ width: '100%', padding: '9px 12px', border: '1px solid #cbd5e1', borderRadius: '8px', fontSize: '13px', outline: 'none', background: '#fff' }}
          >
            <option value="">Tất cả phương thức</option>
            <option value="COD">Thanh toán khi nhận hàng (COD)</option>
            <option value="VNPAY">Cổng VNPAY</option>
          </select>
        </div>

        <div style={{ flex: '1', minWidth: '160px' }}>
          <select
            value={filters.status}
            onChange={(e) => setFilters({ ...filters, status: e.target.value })}
            style={{ width: '100%', padding: '9px 12px', border: '1px solid #cbd5e1', borderRadius: '8px', fontSize: '13px', outline: 'none', background: '#fff' }}
          >
            <option value="">Tất cả trạng thái</option>
            <option value="PENDING">Đang xử lý (PENDING)</option>
            <option value="COMPLETED">Thành công (COMPLETED)</option>
            <option value="FAILED">Thất bại (FAILED)</option>
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

      {/* MAIN LAYOUT GRID */}
      <div style={{ display: 'grid', gridTemplateColumns: selected ? '1.5fr 1fr' : '1fr', gap: '20px', alignItems: 'start' }}>
        
        {/* LEFT COLUMN: TABLE */}
        <div style={{ background: '#ffffff', borderRadius: '12px', border: '1px solid #e2e8f0', overflow: 'visible' }}>
          <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '13px' }}>
            <thead>
              <tr style={{ background: '#f8fafc', color: '#475569', fontWeight: '800', fontSize: '12px', borderBottom: '1px solid #e2e8f0', textTransform: 'uppercase' }}>
                <th style={{ padding: '14px 16px', width: '60px' }}>ID</th>
                <th style={{ padding: '14px 16px', width: '80px' }}>Mã Đơn</th>
                <th style={{ padding: '14px 16px', width: '120px' }}>Phương thức</th>
                <th style={{ padding: '14px 16px', width: '130px' }}>Trạng thái</th>
                <th style={{ padding: '14px 16px', textAlign: 'right', width: '130px' }}>Số tiền</th>
                <th style={{ padding: '14px 16px', width: '140px' }}>Ngày tạo</th>
                <th style={{ padding: '14px 16px', width: '140px', textAlign: 'center' }}>Thao tác</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr>
                  <td colSpan="7" style={{ padding: '36px', textAlign: 'center', color: '#64748b' }}>
                    Đang tải danh sách giao dịch...
                  </td>
                </tr>
              ) : payments.length === 0 ? (
                <tr>
                  <td colSpan="7" style={{ padding: '36px', textAlign: 'center', color: '#94a3b8' }}>
                    Không tìm thấy giao dịch nào.
                  </td>
                </tr>
              ) : (
                payments.map((payment, idx) => {
                  const statusInfo = getPaymentStatusInfo(payment.status);
                  const methodStyle = getMethodBadgeStyle(payment.method);

                  return (
                    <tr
                      key={payment.id}
                      style={{
                        borderBottom: '1px solid #f1f5f9',
                        background: idx % 2 === 0 ? '#ffffff' : '#fafafa',
                      }}
                    >
                      <td style={{ padding: '14px 16px', fontWeight: '600', color: '#334155' }}>
                        #{payment.id}
                      </td>
                      <td style={{ padding: '14px 16px', fontWeight: '700', color: '#ea580c' }}>
                        DH{payment.orderId}
                      </td>
                      <td style={{ padding: '14px 16px' }}>
                        <span
                          style={{
                            background: methodStyle.bg,
                            color: methodStyle.color,
                            border: `1px solid ${methodStyle.border}`,
                            padding: '3px 8px',
                            borderRadius: '6px',
                            fontSize: '11.5px',
                            fontWeight: '700',
                          }}
                        >
                          {payment.method}
                        </span>
                      </td>
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
                      <td style={{ padding: '14px 16px', textAlign: 'right', fontWeight: '800', color: '#dc2626' }}>
                        {formatPrice(payment.amount)}
                      </td>
                      <td style={{ padding: '14px 16px', color: '#64748b' }}>
                        {formatDateTime(payment.createdAt)}
                      </td>
                      <td style={{ padding: '14px 16px', textAlign: 'center' }}>
                        <div style={{ display: 'inline-flex', gap: '6px', justifyContent: 'center' }}>
                          <button
                            type="button"
                            onClick={() => selectPayment(payment)}
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
                            Quản lý
                          </button>
                          {canDelete && (
                            <button
                              type="button"
                              onClick={() => doAction(() => deleteAdminPayment(payment.id), 'Đã xóa giao dịch thanh toán.')}
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

        {/* RIGHT COLUMN: DETAIL ASIDE PANEL */}
        {selected && (
          <aside style={{ background: '#ffffff', borderRadius: '12px', border: '1px solid #e2e8f0', padding: '24px', boxShadow: '0 4px 16px rgba(0,0,0,0.02)', display: 'flex', flexDirection: 'column', gap: '16px' }}>
            
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: '1px solid #f1f5f9', paddingBottom: '12px' }}>
              <h3 style={{ fontSize: '15px', fontWeight: '800', color: '#0f172a', margin: 0 }}>
                Chi tiết Giao dịch #{selected.id}
              </h3>
              <button
                type="button"
                onClick={() => setSelected(null)}
                style={{ border: 'none', background: '#f1f5f9', borderRadius: '6px', width: '26px', height: '26px', cursor: 'pointer', fontWeight: '700' }}
              >
                ✕
              </button>
            </div>

            {/* Summary details */}
            <div style={{ background: '#f8fafc', padding: '14px', borderRadius: '8px', border: '1px solid #e2e8f0', display: 'flex', flexDirection: 'column', gap: '10px', fontSize: '13px' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                <span style={{ color: '#64748b' }}>Trạng thái:</span>
                <strong style={{ color: getPaymentStatusInfo(selected.status).color }}>{getPaymentStatusInfo(selected.status).label}</strong>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                <span style={{ color: '#64748b' }}>Phương thức:</span>
                <strong style={{ color: '#0f172a' }}>{selected.method}</strong>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                <span style={{ color: '#64748b' }}>Số tiền:</span>
                <strong style={{ color: '#dc2626' }}>{formatPrice(selected.amount)}</strong>
              </div>
              {selected.providerTransactionId && (
                <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                  <span style={{ color: '#64748b' }}>Mã GD nhà CC:</span>
                  <strong style={{ color: '#334155' }}>{selected.providerTransactionId}</strong>
                </div>
              )}
            </div>

            {/* Actions Form */}
            <div style={{ display: 'flex', flexDirection: 'column', gap: '12px', marginTop: '6px' }}>
              <div>
                <label style={{ display: 'block', fontSize: '12px', fontWeight: '700', color: '#475569', marginBottom: '6px' }}>Mã GD đối tác (VNPay/MoMo)</label>
                <input
                  type="text"
                  placeholder="Nhập mã giao dịch..."
                  value={providerTransactionId}
                  onChange={(e) => setProviderTransactionId(e.target.value)}
                  style={{ width: '100%', padding: '9px 12px', border: '1px solid #cbd5e1', borderRadius: '8px', fontSize: '13px', outline: 'none' }}
                />
              </div>
              <div>
                <label style={{ display: 'block', fontSize: '12px', fontWeight: '700', color: '#475569', marginBottom: '6px' }}>Lý do cập nhật</label>
                <input
                  type="text"
                  value={reason}
                  onChange={(e) => setReason(e.target.value)}
                  style={{ width: '100%', padding: '9px 12px', border: '1px solid #cbd5e1', borderRadius: '8px', fontSize: '13px', outline: 'none' }}
                />
              </div>
            </div>

            {/* Manage Buttons */}
            <div style={{ display: 'flex', flexDirection: 'column', gap: '8px', marginTop: '10px' }}>
              <button
                type="button"
                onClick={() => doAction(() => markPaymentSucceeded(selected.id, providerTransactionId), 'Cập nhật giao dịch thành công.')}
                style={{
                  width: '100%',
                  padding: '11px',
                  background: '#16a34a',
                  color: '#ffffff',
                  border: 'none',
                  borderRadius: '8px',
                  fontSize: '13.5px',
                  fontWeight: '800',
                  cursor: 'pointer',
                }}
              >
                Xác nhận Thành công (Succeed)
              </button>
              
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '8px' }}>
                <button
                  type="button"
                  onClick={() => doAction(() => markPaymentFailed(selected.id, reason), 'Cập nhật giao dịch thất bại.')}
                  style={{
                    padding: '9px',
                    background: '#ffffff',
                    color: '#dc2626',
                    border: '1px solid #fecaca',
                    borderRadius: '8px',
                    fontSize: '12.5px',
                    fontWeight: '700',
                    cursor: 'pointer',
                  }}
                >
                  Thất bại
                </button>
                <button
                  type="button"
                  onClick={() => doAction(() => cancelAdminPayment(selected.id, reason), 'Đã hủy giao dịch.')}
                  style={{
                    padding: '9px',
                    background: '#ffffff',
                    color: '#475569',
                    border: '1px solid #cbd5e1',
                    borderRadius: '8px',
                    fontSize: '12.5px',
                    fontWeight: '700',
                    cursor: 'pointer',
                  }}
                >
                  Hủy bỏ
                </button>
              </div>
            </div>

            {/* Refund list if any */}
            <div style={{ borderTop: '1px solid #f1f5f9', paddingTop: '16px', marginTop: '10px' }}>
              <strong style={{ fontSize: '13px', color: '#0f172a', display: 'block', marginBottom: '10px' }}>Yêu cầu hoàn tiền</strong>
              {refunds.length === 0 ? (
                <div style={{ fontSize: '12px', color: '#94a3b8', textAlign: 'center', padding: '10px 0' }}>Chưa có yêu cầu hoàn tiền nào.</div>
              ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                  {refunds.map((refund) => (
                    <div
                      key={refund.id}
                      style={{
                        background: '#f8fafc',
                        border: '1px solid #e2e8f0',
                        padding: '10px 12px',
                        borderRadius: '8px',
                        fontSize: '12px',
                      }}
                    >
                      <div style={{ display: 'flex', justifyContent: 'space-between', fontWeight: '700', color: '#334155', marginBottom: '4px' }}>
                        <span>Hoàn tiền #{refund.id}</span>
                        <span style={{ color: refund.status === 'COMPLETED' ? '#16a34a' : '#d97706' }}>{refund.status}</span>
                      </div>
                      <div style={{ color: '#dc2626', fontWeight: '700', marginBottom: '2px' }}>{formatPrice(refund.amount)}</div>
                      <div style={{ color: '#64748b' }}>Lý do: {refund.reason || 'Không có lý do'}</div>
                    </div>
                  ))}
                </div>
              )}
            </div>

          </aside>
        )}
      </div>

      {/* FOOTER */}
      <footer style={{ textAlign: 'center', marginTop: '30px', padding: '16px 0', borderTop: '1px solid #cbd5e1', fontSize: '12px', color: '#94a3b8' }}>
        © 2026 ToyStore Admin Panel
      </footer>
    </section>
  );
}

export default AdminPaymentPage;
