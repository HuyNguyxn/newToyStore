import { useEffect, useMemo, useState } from 'react';
import { useOutletContext } from 'react-router-dom';
import {
  deleteCustomerPaymentRefund,
  getAllCustomerPaymentRefunds,
  getCustomerPaymentRefunds,
  processCustomerPaymentRefund,
  rejectCustomerPaymentRefund,
  requestCustomerPaymentRefund,
} from '../../services/adminCustomerRefundService.js';
import { formatDateTime, formatPrice } from '../../utils/formatters.js';

function getRefundStatusInfo(status) {
  const code = status && typeof status === 'object' ? status.code : status;
  const statusStr = String(code || '').toUpperCase();
  if (statusStr === 'COMPLETED' || statusStr === 'SUCCESS' || statusStr === 'SUCCEEDED' || statusStr === 'APPROVED') {
    return { label: 'Thành công', bg: '#d1fae5', color: '#10b981', border: '#a7f3d0' };
  }
  if (statusStr === 'PENDING' || statusStr === 'PROCESSING') {
    return { label: 'Chờ duyệt', bg: '#fef3c7', color: '#d97706', border: '#fde68a' };
  }
  if (statusStr === 'REJECTED' || statusStr === 'REJECT' || statusStr === 'FAILED') {
    return { label: 'Từ chối', bg: '#fee2e2', color: '#dc2626', border: '#fecaca' };
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

// Helper to check internal test refund requests
function isInternalTestOrder(item) {
  if (!item) return false;
  
  // 1. If user role is CUSTOMER, it is ALWAYS a real business customer refund!
  const userRole = String(item.user?.role || item.userRole || '').toUpperCase();
  if (userRole === 'CUSTOMER') return false;

  // 2. Check direct user ID of the buyer (Seed Admin/Staff IDs 1 and 2 only)
  const uid = Number(item.userId || item.user?.id || item.customerId || 0);
  if (uid === 1 || uid === 2) return true;

  // 3. Check test seed order IDs 1, 2, 3
  const oid = Number(item.orderId || item.order?.id || 0);
  if (oid === 1 || oid === 2 || oid === 3) return true;

  // 4. Check buyer user role if attached
  if (['ADMIN', 'STAFF', 'MANAGER'].includes(userRole)) return true;

  // 5. Check buyer email if attached
  const email = String(item.user?.email || item.customerEmail || item.email || '').toLowerCase();
  if (email.includes('admin@') || email.includes('staff@') || email.includes('manager@') || email.includes('@toystore.internal')) {
    return true;
  }

  // 6. Check explicit test note on refund request
  const note = String(item.reason || item.note || '').toLowerCase();
  if (note.includes('đơn test') || note.includes('thử nghiệm nội bộ') || note.includes('[test]')) {
    return true;
  }

  return false;
}

function AdminCustomerRefundPage() {
  const context = useOutletContext() || {};
  const userRole = context.userRole || 'STAFF';
  const canDelete = userRole === 'ADMIN';

  const [dataMode, setDataMode] = useState('REAL'); // 'REAL' or 'TEST'
  const [paymentId, setPaymentId] = useState('');
  const [refunds, setRefunds] = useState([]);
  const [form, setForm] = useState({ amount: '', method: 'COD', reason: '' });
  const [rejectReason, setRejectReason] = useState('Từ chối yêu cầu từ quản trị viên');
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  const displayRefunds = useMemo(() => {
    return refunds.filter((r) => {
      const isTest = isInternalTestOrder(r);
      return dataMode === 'REAL' ? !isTest : isTest;
    });
  }, [refunds, dataMode]);

  useEffect(() => {
    loadRefunds();
  }, []);

  async function loadRefunds(event, requestedPage = page) {
    event?.preventDefault();
    setLoading(true);
    setError('');
    try {
      const result = paymentId
        ? await getCustomerPaymentRefunds(paymentId, { page: requestedPage, size: 20, sort: 'createdAt,desc' })
        : await getAllCustomerPaymentRefunds({ page: requestedPage, size: 20, sort: 'createdAt,desc' });
      setRefunds(result.content || result || []);
      setPage(result.number ?? requestedPage);
      setTotalPages(result.totalPages ?? 0);
    } catch (err) {
      setRefunds([]);
      setError(err.message || 'Không thể tải danh sách hoàn tiền.');
    } finally {
      setLoading(false);
    }
  }

  async function handleCreateRefund(event) {
    event.preventDefault();
    setLoading(true);
    setError('');
    setMessage('');
    try {
      await requestCustomerPaymentRefund(paymentId, {
        amount: Number(form.amount),
        method: form.method,
        reason: form.reason.trim(),
      });
      setMessage('Tạo yêu cầu hoàn tiền thành công.');
      setForm({ amount: '', method: 'COD', reason: '' });
      await loadRefunds();
    } catch (err) {
      setError(err.message || 'Tạo yêu cầu hoàn tiền thất bại.');
    } finally {
      setLoading(false);
    }
  }

  async function handleAction(action) {
    setLoading(true);
    setError('');
    setMessage('');
    try {
      await action();
      setMessage('Cập nhật trạng thái hoàn tiền thành công.');
      await loadRefunds();
    } catch (err) {
      setError(err.message || 'Thao tác hoàn tiền thất bại.');
    } finally {
      setLoading(false);
    }
  }

  return (
    <section style={{ padding: '24px', background: '#f8fafc', minHeight: '100vh', fontFamily: 'system-ui, -apple-system, sans-serif' }}>
      
      {/* HEADER ROW WITH MODE SWITCHER */}
      <div style={{ background: 'linear-gradient(135deg, #fff8f3 0%, #fff1f2 100%)', border: '1px solid #ffedd5', padding: '16px 24px', borderRadius: '16px', marginBottom: '20px', boxShadow: '0 4px 12px rgba(234,88,12,0.04)', display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '16px' }}>
        <div>
          <h1 style={{ fontSize: '20px', fontWeight: '900', color: '#9a3412', margin: 0, textTransform: 'uppercase', letterSpacing: '0.5px' }}>
            Quản lý hoàn tiền
          </h1>
          <div style={{ fontSize: '13px', color: dataMode === 'REAL' ? '#15803d' : '#7e22ce', fontWeight: '800', marginTop: '4px' }}>
            {dataMode === 'REAL' ? '🟢 Đang xem: Yêu cầu Hoàn tiền Thực tế (Đã lọc đơn test Admin)' : '🧪 Đang xem: Yêu cầu Hoàn tiền Thử nghiệm Nội bộ (ADMIN/STAFF/MANAGER)'}
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
              🟢 Hoàn tiền Kinh doanh ({refunds.filter(r => !isInternalTestOrder(r)).length})
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
              🧪 Đơn Thử nghiệm ({refunds.filter(r => isInternalTestOrder(r)).length})
            </button>
          </div>
        </div>
      </div>

      {/* ALERTS */}
      {error && <div style={{ background: '#fef2f2', color: '#dc2626', border: '1px solid #fecaca', padding: '10px 14px', borderRadius: '8px', marginBottom: '16px', fontSize: '13px', fontWeight: '700' }}>{error}</div>}
      {message && <div style={{ background: '#f0fdf4', color: '#16a34a', border: '1px solid #bbf7d0', padding: '10px 14px', borderRadius: '8px', marginBottom: '16px', fontSize: '13px', fontWeight: '700' }}>{message}</div>}

      {/* SEARCH/FILTER CARD */}
      <form
        onSubmit={(event) => { event.preventDefault(); setPage(0); loadRefunds(undefined, 0); }}
        style={{ background: '#ffffff', padding: '14px 16px', borderRadius: '12px', border: '1px solid #e2e8f0', marginBottom: '20px', display: 'flex', gap: '10px', alignItems: 'center', flexWrap: 'wrap' }}
      >
        <div style={{ flex: '1', minWidth: '240px' }}>
          <input
            type="number"
            placeholder="Nhập Mã Giao dịch thanh toán để xem danh sách hoàn tiền..."
            value={paymentId}
            onChange={(e) => setPaymentId(e.target.value)}
            style={{ width: '100%', padding: '9px 12px', border: '1px solid #cbd5e1', borderRadius: '8px', fontSize: '13px', outline: 'none' }}
          />
        </div>
        <button
          type="submit"
          disabled={loading}
          style={{ padding: '9px 20px', background: '#ea580c', color: '#ffffff', border: 'none', borderRadius: '8px', fontSize: '13px', fontWeight: '700', cursor: 'pointer' }}
        >
          {loading ? 'Đang tải...' : 'Tìm kiếm'}
        </button>
      </form>

      {/* CREATE REFUND & REJECT REASON DUAL CARD */}
      {paymentId && (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '20px', marginBottom: '20px' }}>
          
          {/* Create Refund Request */}
          <div style={{ background: '#ffffff', borderRadius: '12px', border: '1px solid #e2e8f0', padding: '20px' }}>
            <h3 style={{ fontSize: '14.5px', fontWeight: '800', color: '#0f172a', borderBottom: '1px solid #f1f5f9', paddingBottom: '10px', margin: '0 0 14px 0' }}>
              Tạo yêu cầu hoàn tiền
            </h3>
            <form onSubmit={handleCreateRefund} style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
                <div>
                  <label style={{ display: 'block', fontSize: '12.5px', fontWeight: '700', color: '#475569', marginBottom: '6px' }}>Số tiền</label>
                  <input
                    type="number"
                    value={form.amount}
                    onChange={(event) => setForm((current) => ({ ...current, amount: event.target.value }))}
                    required
                    style={{ width: '100%', padding: '8px 10px', border: '1px solid #cbd5e1', borderRadius: '6px', fontSize: '13px', outline: 'none' }}
                  />
                </div>
                <div>
                  <label style={{ display: 'block', fontSize: '12.5px', fontWeight: '700', color: '#475569', marginBottom: '6px' }}>Phương thức</label>
                  <select
                    value={form.method}
                    onChange={(event) => setForm((current) => ({ ...current, method: event.target.value }))}
                    style={{ width: '100%', padding: '8px 10px', border: '1px solid #cbd5e1', borderRadius: '6px', fontSize: '13px', outline: 'none', background: '#fff' }}
                  >
                    <option>COD</option>
                    <option>VNPAY</option>
                    <option>MANUAL</option>
                  </select>
                </div>
              </div>
              <div>
                <label style={{ display: 'block', fontSize: '12.5px', fontWeight: '700', color: '#475569', marginBottom: '6px' }}>Lý do hoàn tiền</label>
                <input
                  type="text"
                  value={form.reason}
                  onChange={(event) => setForm((current) => ({ ...current, reason: event.target.value }))}
                  required
                  maxLength="255"
                  style={{ width: '100%', padding: '8px 10px', border: '1px solid #cbd5e1', borderRadius: '6px', fontSize: '13px', outline: 'none' }}
                />
              </div>
              <button
                type="submit"
                disabled={loading}
                style={{ width: '100%', padding: '10px', background: '#16a34a', color: '#ffffff', border: 'none', borderRadius: '6px', fontSize: '13px', fontWeight: '700', cursor: 'pointer', marginTop: '4px' }}
              >
                Gửi yêu cầu hoàn tiền
              </button>
            </form>
          </div>

          {/* Rejection Reason Config */}
          <div style={{ background: '#ffffff', borderRadius: '12px', border: '1px solid #e2e8f0', padding: '20px' }}>
            <h3 style={{ fontSize: '14.5px', fontWeight: '800', color: '#0f172a', borderBottom: '1px solid #f1f5f9', paddingBottom: '10px', margin: '0 0 14px 0' }}>
              Cấu hình lý do từ chối
            </h3>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
              <div>
                <label style={{ display: 'block', fontSize: '12.5px', fontWeight: '700', color: '#475569', marginBottom: '6px' }}>Lý do từ chối (Dùng khi bấm Từ chối)</label>
                <textarea
                  value={rejectReason}
                  onChange={(event) => setRejectReason(event.target.value)}
                  rows="4"
                  style={{ width: '100%', padding: '10px 12px', border: '1px solid #cbd5e1', borderRadius: '8px', fontSize: '13px', outline: 'none', resize: 'none' }}
                />
              </div>
              <span style={{ fontSize: '11.5px', color: '#64748b', lineHeight: 1.4 }}>
                * Lý do này sẽ được đính kèm vào lịch sử yêu cầu hoàn tiền khi bạn từ chối giao dịch hoàn tiền của khách hàng.
              </span>
            </div>
          </div>

        </div>
      )}

      {/* DATA TABLE */}
      <div style={{ background: '#ffffff', borderRadius: '12px', border: '1px solid #e2e8f0', overflow: 'visible' }}>
        <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '13px' }}>
          <thead>
            <tr style={{ background: '#f8fafc', color: '#475569', fontWeight: '800', fontSize: '12px', borderBottom: '1px solid #e2e8f0', textTransform: 'uppercase' }}>
              <th style={{ padding: '14px 16px', width: '60px' }}>ID</th>
              <th style={{ padding: '14px 16px', width: '90px' }}>Mã GD</th>
              <th style={{ padding: '14px 16px', width: '90px' }}>Mã Đơn</th>
              <th style={{ padding: '14px 16px', width: '110px' }}>Phương thức</th>
              <th style={{ padding: '14px 16px', textAlign: 'right', width: '120px' }}>Số tiền</th>
              <th style={{ padding: '14px 16px' }}>Trạng thái & Thời gian</th>
              <th style={{ padding: '14px 16px', width: '200px', textAlign: 'center' }}>Thao tác</th>
            </tr>
          </thead>
          <tbody>
            {displayRefunds.length === 0 ? (
              <tr>
                <td colSpan="7" style={{ padding: '36px', textAlign: 'center', color: '#94a3b8' }}>
                  {paymentId ? 'Không tìm thấy yêu cầu hoàn tiền nào.' : 'Chưa có yêu cầu hoàn tiền nào.'}
                </td>
              </tr>
            ) : (
              displayRefunds.map((refund, idx) => {
                const statusInfo = getRefundStatusInfo(refund.status);
                const methodStyle = getMethodBadgeStyle(refund.method);

                return (
                  <tr
                    key={refund.id}
                    style={{
                      borderBottom: '1px solid #f1f5f9',
                      background: idx % 2 === 0 ? '#ffffff' : '#fafafa',
                    }}
                  >
                    <td style={{ padding: '14px 16px', fontWeight: '600', color: '#334155' }}>
                      #{refund.id}
                    </td>
                    <td style={{ padding: '14px 16px', color: '#334155' }}>
                      #{refund.paymentId}
                    </td>
                    <td style={{ padding: '14px 16px', fontWeight: '700', color: '#ea580c' }}>
                      DH{refund.orderId}
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
                        {refund.method}
                      </span>
                    </td>
                    <td style={{ padding: '14px 16px', textAlign: 'right', fontWeight: '800', color: '#dc2626' }}>
                      {formatPrice(refund.amount)}
                    </td>
                    <td style={{ padding: '14px 16px' }}>
                      <div style={{ display: 'flex', flexDirection: 'column', gap: '2px' }}>
                        <span
                          style={{
                            background: statusInfo.bg,
                            color: statusInfo.color,
                            border: `1px solid ${statusInfo.border}`,
                            padding: '2px 8px',
                            borderRadius: '6px',
                            fontSize: '11.5px',
                            fontWeight: '700',
                            display: 'inline-block',
                            alignSelf: 'flex-start',
                          }}
                        >
                          {statusInfo.label}
                        </span>
                        {refund.createdAt && (
                          <span style={{ fontSize: '11px', color: '#64748b' }}>
                            {formatDateTime(refund.createdAt)}
                          </span>
                        )}
                      </div>
                    </td>
                    <td style={{ padding: '14px 16px', textAlign: 'center' }}>
                      <div style={{ display: 'inline-flex', gap: '6px', justifyContent: 'center' }}>
                        <button
                          type="button"
                          onClick={() => handleAction(() => processCustomerPaymentRefund(refund.id))}
                          style={{
                            padding: '6px 12px',
                            background: '#ffffff',
                            color: '#16a34a',
                            border: '1px solid #bbf7d0',
                            borderRadius: '6px',
                            cursor: 'pointer',
                            fontSize: '12px',
                            fontWeight: '700',
                          }}
                        >
                          Duyệt
                        </button>
                        <button
                          type="button"
                          onClick={() => handleAction(() => rejectCustomerPaymentRefund(refund.id, rejectReason))}
                          style={{
                            padding: '6px 12px',
                            background: '#ffffff',
                            color: '#dc2626',
                            border: '1px solid #fecaca',
                            borderRadius: '6px',
                            cursor: 'pointer',
                            fontSize: '12px',
                            fontWeight: '700',
                          }}
                        >
                          Từ chối
                        </button>
                        {canDelete && (
                          <button
                            type="button"
                            onClick={() => handleAction(() => deleteCustomerPaymentRefund(refund.id))}
                            style={{
                              padding: '6px 12px',
                              background: '#ffffff',
                              color: '#64748b',
                              border: '1px solid #cbd5e1',
                              borderRadius: '6px',
                              cursor: 'pointer',
                              fontSize: '12px',
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
        <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', gap: 12, marginTop: 16 }}>
          <button type="button" disabled={page <= 0 || loading} onClick={() => loadRefunds(undefined, page - 1)} style={{ padding: '8px 14px', border: '1px solid #cbd5e1', borderRadius: 8, background: '#fff', cursor: page <= 0 ? 'not-allowed' : 'pointer', opacity: page <= 0 ? .5 : 1 }}>Trước</button>
          <span style={{ color: '#475569', fontSize: 13, fontWeight: 700 }}>Trang {page + 1} / {totalPages}</span>
          <button type="button" disabled={page >= totalPages - 1 || loading} onClick={() => loadRefunds(undefined, page + 1)} style={{ padding: '8px 14px', border: '1px solid #cbd5e1', borderRadius: 8, background: '#fff', cursor: page >= totalPages - 1 ? 'not-allowed' : 'pointer', opacity: page >= totalPages - 1 ? .5 : 1 }}>Sau</button>
        </div>
      )}

      {/* FOOTER */}
      <footer style={{ textAlign: 'center', marginTop: '30px', padding: '16px 0', borderTop: '1px solid #cbd5e1', fontSize: '12px', color: '#94a3b8' }}>
        © 2026 ToyStore Admin Panel
      </footer>
    </section>
  );
}

export default AdminCustomerRefundPage;
