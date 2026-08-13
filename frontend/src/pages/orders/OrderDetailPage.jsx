import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import BackLink from '../../components/common/BackLink.jsx';
import { cancelOrder, getOrderDetails } from '../../services/orderService.js';
import { checkoutCustomerPayment, createCustomerPaymentIdempotencyKey, getLatestCustomerPaymentForOrder } from '../../services/customerPaymentService.js';
import { formatDateTime, formatPrice, getOrderStatusLabel, getPaymentStatusLabel } from '../../utils/formatters.js';

function OrderDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [order, setOrder] = useState(null);
  const [latestPayment, setLatestPayment] = useState(null);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    let active = true;
    setLoading(true);
    setError('');

    Promise.all([
      getOrderDetails(id),
      getLatestCustomerPaymentForOrder(id).catch(() => null),
    ])
      .then(([result, payment]) => {
        if (active) {
          setOrder(result);
          setLatestPayment(payment);
        }
      })
      .catch((err) => {
        if (!active) return;
        setError(err.message || 'Không thể tải chi tiết đơn hàng.');
      })
      .finally(() => {
        if (active) {
          setLoading(false);
        }
      });

    return () => {
      active = false;
    };
  }, [id]);

  async function handlePaymentCheckout(method = 'VNPAY') {
    setSubmitting(true);
    setMessage('');
    setError('');

    try {
      const payment = await checkoutCustomerPayment({
        orderId: order.id,
        method,
        idempotencyKey: createCustomerPaymentIdempotencyKey(order.id, method),
      });

      if (payment.paymentUrl) {
        window.location.href = payment.paymentUrl;
        return;
      }

      setMessage(`Thanh toán ${payment.method} đang ở trạng thái ${getPaymentStatusLabel(payment.status)}.`);
    } catch (err) {
      setError(err.message || 'Không thể tạo thanh toán cho đơn hàng.');
    } finally {
      setSubmitting(false);
    }
  }

  async function handleCancelOrder() {
    if (!window.confirm('Bạn có chắc chắn muốn hủy đơn hàng này không?')) return;
    setSubmitting(true);
    setMessage('');
    setError('');

    try {
      await cancelOrder(order.id, 'Khách hàng hủy trên website');
      const refreshedOrder = await getOrderDetails(order.id);
      setOrder(refreshedOrder);
      setMessage('Đã hủy đơn hàng thành công.');
    } catch (err) {
      setError(err.message || 'Không thể hủy đơn hàng.');
    } finally {
      setSubmitting(false);
    }
  }

  if (loading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '60vh', fontFamily: 'system-ui' }}>
        <div style={{ color: '#ea580c', fontSize: '15px', fontWeight: '700' }}>Đang tải chi tiết đơn hàng...</div>
      </div>
    );
  }

  if (!order) {
    return (
      <div style={{ padding: '60px 16px', textAlign: 'center', fontFamily: 'system-ui' }}>
        <h2 style={{ fontSize: '20px', color: '#0f172a', marginBottom: '12px' }}>Không tìm thấy thông tin đơn hàng #{id}</h2>
        <Link to="/orders" style={{ color: '#ea580c', fontWeight: '700', textDecoration: 'none' }}>
          ← Quay lại danh sách đơn hàng
        </Link>
      </div>
    );
  }

  const orderStatusCode = (typeof order.status === 'object' && order.status !== null) ? (order.status.name || order.status.code || '') : String(order.status || '');
  const paymentMethodCode = typeof latestPayment?.method === 'object'
    ? (latestPayment.method.code || latestPayment.method.name || '')
    : String(latestPayment?.method || '');
  const paymentStatusCode = typeof latestPayment?.status === 'object'
    ? (latestPayment.status.code || latestPayment.status.name || '')
    : String(latestPayment?.status || '');
  const canRetryVnpay = orderStatusCode === 'PENDING'
    && paymentMethodCode.toUpperCase() === 'VNPAY'
    && paymentStatusCode.toUpperCase() === 'PENDING';
  const canCancel = order.availableActions?.includes('CANCELLED') || orderStatusCode === 'PENDING';
  const statusColor = (
    orderStatusCode === 'COMPLETED' ? { bg: '#d1fae5', text: '#059669', border: '#a7f3d0' } :
    orderStatusCode === 'CANCELLED' ? { bg: '#fef2f2', text: '#dc2626', border: '#fecaca' } :
    orderStatusCode === 'SHIPPED' ? { bg: '#e0f2fe', text: '#0284c7', border: '#bae6fd' } :
    orderStatusCode === 'CONFIRMED' ? { bg: '#fef3c7', text: '#d97706', border: '#fde68a' } :
    { bg: '#fff7ed', text: '#ea580c', border: '#ffedd5' }
  );

  return (
    <div style={{ background: '#f8fafc', minHeight: '100vh', padding: '30px 16px', fontFamily: 'system-ui, -apple-system, sans-serif' }}>
      <div className="container" style={{ maxWidth: '1000px', margin: '0 auto' }}>
        
        {/* Navigation & Header */}
        <div style={{ marginBottom: '20px' }}>
          <BackLink fallback="/orders" label="Quay lại danh sách đơn hàng" />
        </div>

        {/* Page Title Card */}
        <div style={{ background: '#ffffff', borderRadius: '12px', border: '1px solid #e2e8f0', padding: '24px', marginBottom: '24px', boxShadow: '0 4px 16px rgba(0,0,0,0.02)', display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '16px' }}>
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '6px' }}>
              <h1 style={{ fontSize: '22px', fontWeight: '900', color: '#0f172a', margin: 0 }}>
                Chi tiết đơn hàng #{order.id}
              </h1>
              <span style={{ padding: '4px 12px', background: statusColor.bg, color: statusColor.text, border: `1px solid ${statusColor.border}`, borderRadius: '20px', fontSize: '12px', fontWeight: '800' }}>
                {getOrderStatusLabel(order.status)}
              </span>
            </div>
            <p style={{ fontSize: '13px', color: '#64748b', margin: 0 }}>
              Thời gian đặt hàng: {formatDateTime(order.createdAt)}
            </p>
          </div>

          <div style={{ display: 'flex', gap: '10px' }}>
            <Link
              to="/orders"
              style={{ padding: '8px 16px', background: '#f1f5f9', color: '#475569', textDecoration: 'none', borderRadius: '8px', fontSize: '13px', fontWeight: '700', border: '1px solid #cbd5e1' }}
            >
              Xem tất cả đơn hàng
            </Link>
          </div>
        </div>

        {/* Alerts */}
        {error && (
          <div style={{ background: '#fef2f2', color: '#dc2626', border: '1px solid #fecaca', padding: '12px 16px', borderRadius: '8px', fontSize: '13px', fontWeight: '700', marginBottom: '20px' }}>
            ⚠️ {error}
          </div>
        )}
        {message && (
          <div style={{ background: '#f0fdf4', color: '#16a34a', border: '1px solid #bbf7d0', padding: '12px 16px', borderRadius: '8px', fontSize: '13px', fontWeight: '700', marginBottom: '20px' }}>
            ✓ {message}
          </div>
        )}

        {/* Main 2-Column Grid */}
        <div style={{ display: 'grid', gridTemplateColumns: '1.8fr 1fr', gap: '24px', alignItems: 'start' }}>
          
          {/* LEFT COLUMN: Items, Shipping, Histories */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
            
            {/* 1. ORDER ITEMS CARD */}
            <div style={{ background: '#ffffff', borderRadius: '12px', border: '1px solid #e2e8f0', padding: '24px', boxShadow: '0 4px 16px rgba(0,0,0,0.02)' }}>
              <h2 style={{ fontSize: '16px', fontWeight: '800', color: '#0f172a', margin: '0 0 16px 0', borderBottom: '1px solid #f1f5f9', paddingBottom: '10px', display: 'flex', alignItems: 'center', gap: '8px' }}>
                <span>📦</span> Danh sách sản phẩm ({order.items?.length || 0})
              </h2>

              <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
                {(order.items || []).map((item) => (
                  <div key={item.id} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', borderBottom: '1px solid #f8fafc', paddingBottom: '14px' }}>
                    <div style={{ flex: '1', paddingRight: '16px' }}>
                      <strong style={{ fontSize: '14px', color: '#0f172a', display: 'block', marginBottom: '4px' }}>
                        {item.productName}
                      </strong>
                      <span style={{ fontSize: '12.5px', color: '#64748b', display: 'block', marginBottom: '8px' }}>
                        {item.variantAttributesSnapshot || 'Phiên bản mặc định'} • Số lượng: <strong>x{item.quantity}</strong>
                      </span>

                      {/* Item Actions */}
                      <div style={{ display: 'flex', gap: '10px', flexWrap: 'wrap' }}>
                        {orderStatusCode === 'COMPLETED' && (
                          <Link
                            to={`/reviews/new?orderItemId=${item.id}`}
                            style={{ fontSize: '12px', color: '#ea580c', fontWeight: '700', textDecoration: 'none', background: '#fff7ed', border: '1px solid #ffedd5', padding: '4px 10px', borderRadius: '6px' }}
                          >
                            ⭐ Viết đánh giá
                          </Link>
                        )}
                        {['COMPLETED', 'SHIPPED'].includes(orderStatusCode) && (
                          <Link
                            to={`/returns/new?orderId=${order.id}&orderItemId=${item.id}&productId=${item.productId || ''}&variantId=${item.variantId || ''}`}
                            style={{ fontSize: '12px', color: '#475569', fontWeight: '700', textDecoration: 'none', background: '#f1f5f9', border: '1px solid #cbd5e1', padding: '4px 10px', borderRadius: '6px' }}
                          >
                            🔄 Yêu cầu trả hàng
                          </Link>
                        )}
                      </div>
                    </div>

                    <div style={{ textAlign: 'right' }}>
                      <span style={{ fontSize: '14px', fontWeight: '800', color: '#0f172a' }}>
                        {formatPrice((item.price || item.addedPrice || 0) * item.quantity)}
                      </span>
                      <span style={{ fontSize: '11.5px', color: '#94a3b8', display: 'block', marginTop: '2px' }}>
                        ({formatPrice(item.price || item.addedPrice || 0)} / sp)
                      </span>
                    </div>
                  </div>
                ))}
              </div>
            </div>

            {/* 2. SHIPPING ADDRESS CARD */}
            <div style={{ background: '#ffffff', borderRadius: '12px', border: '1px solid #e2e8f0', padding: '24px', boxShadow: '0 4px 16px rgba(0,0,0,0.02)' }}>
              <h2 style={{ fontSize: '16px', fontWeight: '800', color: '#0f172a', margin: '0 0 12px 0', borderBottom: '1px solid #f1f5f9', paddingBottom: '10px', display: 'flex', alignItems: 'center', gap: '8px' }}>
                <span>📍</span> Thông tin nhận hàng
              </h2>
              <div style={{ background: '#f8fafc', border: '1px solid #e2e8f0', borderRadius: '8px', padding: '14px 16px', fontSize: '13.5px', color: '#334155', lineHeight: 1.6 }}>
                {order.shippingAddress || 'Chưa cập nhật địa chỉ giao hàng.'}
              </div>
            </div>

            {/* 3. ORDER STATUS HISTORY */}
            {order.histories && order.histories.length > 0 && (
              <div style={{ background: '#ffffff', borderRadius: '12px', border: '1px solid #e2e8f0', padding: '24px', boxShadow: '0 4px 16px rgba(0,0,0,0.02)' }}>
                <h2 style={{ fontSize: '16px', fontWeight: '800', color: '#0f172a', margin: '0 0 16px 0', borderBottom: '1px solid #f1f5f9', paddingBottom: '10px', display: 'flex', alignItems: 'center', gap: '8px' }}>
                  <span>📜</span> Lịch sử trạng thái đơn hàng
                </h2>

                <div style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
                  {order.histories.map((history, idx) => (
                    <div key={history.id || idx} style={{ display: 'flex', gap: '12px', alignItems: 'flex-start' }}>
                      <div style={{ width: '10px', height: '10px', borderRadius: '50%', background: idx === 0 ? '#ea580c' : '#cbd5e1', marginTop: '5px', flexShrink: 0 }} />
                      <div style={{ flex: 1, fontSize: '13px' }}>
                        <strong style={{ color: '#0f172a' }}>{getOrderStatusLabel(history.status)}</strong>
                        <p style={{ color: '#64748b', margin: '2px 0 0 0', fontSize: '12px' }}>
                          {history.note || 'Cập nhật hệ thống'} — {formatDateTime(history.createdAt)}
                        </p>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}

          </div>

          {/* RIGHT COLUMN: Payment Summary & Order Actions */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
            
            {/* PAYMENT SUMMARY CARD */}
            <div style={{ background: '#ffffff', borderRadius: '12px', border: '1px solid #e2e8f0', padding: '24px', boxShadow: '0 4px 16px rgba(0,0,0,0.02)' }}>
              <h2 style={{ fontSize: '15px', fontWeight: '800', color: '#0f172a', margin: '0 0 16px 0', borderBottom: '1px solid #f1f5f9', paddingBottom: '10px' }}>
                Tổng quan thanh toán
              </h2>

              <div style={{ display: 'flex', flexDirection: 'column', gap: '10px', fontSize: '13px', marginBottom: '20px' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', color: '#64748b' }}>
                  <span>Tạm tính</span>
                  <span style={{ color: '#334155', fontWeight: '600' }}>{formatPrice(order.totalAmount + (order.discountAmount || 0))}</span>
                </div>
                
                <div style={{ display: 'flex', justifyContent: 'space-between', color: '#64748b' }}>
                  <span>Giảm giá</span>
                  <span style={{ color: '#16a34a', fontWeight: '700' }}>-{formatPrice(order.discountAmount)}</span>
                </div>

                <div style={{ display: 'flex', justifyContent: 'space-between', color: '#64748b' }}>
                  <span>Phí vận chuyển</span>
                  <span style={{ color: '#334155', fontWeight: '600' }}>Miễn phí</span>
                </div>

                <div style={{ display: 'flex', justifyContent: 'space-between', color: '#0f172a', fontWeight: '800', borderTop: '1px solid #f1f5f9', paddingTop: '12px', fontSize: '15px' }}>
                  <span>Tổng tiền</span>
                  <span style={{ color: '#dc2626', fontSize: '17px', fontWeight: '900' }}>{formatPrice(order.totalAmount)}</span>
                </div>
              </div>

              {/* ACTION BUTTONS */}
              <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
                {canRetryVnpay && (
                  <button
                    type="button"
                    onClick={() => {
                      handlePaymentCheckout('VNPAY');
                    }}
                    disabled={submitting}
                    style={{
                      width: '100%',
                      padding: '13px',
                      background: 'linear-gradient(135deg, #0284c7, #2563eb)',
                      color: '#ffffff',
                      border: 'none',
                      borderRadius: '8px',
                      fontSize: '14px',
                      fontWeight: '800',
                      cursor: 'pointer',
                      boxShadow: '0 4px 12px rgba(37,99,235,0.25)',
                    }}
                  >
                    {submitting ? 'Đang chuyển hướng VNPAY...' : '💳 Thanh toán ngay qua VNPAY'}
                  </button>
                )}

                {canCancel && (
                  <button
                    type="button"
                    onClick={handleCancelOrder}
                    disabled={submitting}
                    style={{
                      width: '100%',
                      padding: '11px',
                      background: '#fef2f2',
                      color: '#dc2626',
                      border: '1px solid #fecaca',
                      borderRadius: '8px',
                      fontSize: '13.5px',
                      fontWeight: '800',
                      cursor: 'pointer',
                      transition: 'all 0.15s ease',
                    }}
                  >
                    {submitting ? 'Đang xử lý...' : '❌ Hủy đơn hàng'}
                  </button>
                )}

                <button
                  type="button"
                  onClick={() => navigate('/orders')}
                  style={{
                    width: '100%',
                    padding: '11px',
                    background: '#ffffff',
                    color: '#475569',
                    border: '1px solid #cbd5e1',
                    borderRadius: '8px',
                    fontSize: '13.5px',
                    fontWeight: '700',
                    cursor: 'pointer',
                  }}
                >
                  Quay lại Lịch sử đơn hàng
                </button>
              </div>

            </div>

          </div>

        </div>

      </div>
    </div>
  );
}

export default OrderDetailPage;
