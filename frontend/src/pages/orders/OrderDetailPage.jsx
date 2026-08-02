import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { sampleOrders } from '../../data/sampleData.js';
import { cancelOrder, getOrderDetails } from '../../services/orderService.js';
import { checkoutPayment, createIdempotencyKey } from '../../services/paymentService.js';
import { formatDateTime, formatPrice, getOrderStatusLabel, getPaymentStatusLabel } from '../../utils/formatters.js';

function OrderDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [order, setOrder] = useState(null);
  const [paymentMethod, setPaymentMethod] = useState('COD');
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    let active = true;
    setLoading(true);
    setError('');

    getOrderDetails(id)
      .then((result) => {
        if (active) {
          setOrder(result);
        }
      })
      .catch(() => {
        if (!active) {
          return;
        }
        setOrder(sampleOrders.find((item) => String(item.id) === String(id)) || sampleOrders[0]);
        setError('Backend chưa sẵn sàng, đang hiển thị đơn hàng mẫu.');
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

  async function handlePaymentCheckout() {
    setSubmitting(true);
    setMessage('');
    setError('');

    try {
      const payment = await checkoutPayment({
        orderId: order.id,
        method: paymentMethod,
        idempotencyKey: createIdempotencyKey(order.id, paymentMethod),
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
    setSubmitting(true);
    setMessage('');
    setError('');

    try {
      const result = await cancelOrder(order.id, 'Khách hàng hủy trên website');
      setOrder(result);
      setMessage('Đã hủy đơn hàng.');
    } catch (err) {
      setError(err.message || 'Không thể hủy đơn hàng.');
    } finally {
      setSubmitting(false);
    }
  }

  if (loading) {
    return <div className="page-message">Đang tải chi tiết đơn hàng...</div>;
  }

  if (!order) {
    return <div className="empty-state container">Không tìm thấy đơn hàng.</div>;
  }

  const canCancel = order.availableActions?.includes('CANCEL');

  return (
    <div className="order-detail-page container">
      <div className="breadcrumb">
        <Link to="/orders">Don hang</Link>
        <span>/</span>
        <span>#{order.id}</span>
      </div>

      {error && <div className="form-alert form-alert--soft">{error}</div>}
      {message && <div className="form-alert form-alert--success">{message}</div>}

      <section className="order-detail-grid">
        <div className="order-panel">
          <div className="page-title-row">
            <div>
              <p>{getOrderStatusLabel(order.status)}</p>
              <h1>Don hang #{order.id}</h1>
            </div>
            <span>{formatDateTime(order.createdAt)}</span>
          </div>

          <div className="order-items">
            {(order.items || []).map((item) => (
              <div className="order-item-row" key={item.id}>
                <div>
                  <strong>{item.productName}</strong>
                  <p>{item.variantAttributesSnapshot || 'Mac dinh'} x {item.quantity}</p>
                  <div className="order-item-row__actions">
                    <Link to={`/reviews/new?orderItemId=${item.id}`}>Viết đánh giá</Link>
                    <Link to={`/returns/new?orderId=${order.id}&orderItemId=${item.id}&productId=${item.productId || ''}&variantId=${item.variantId || ''}`}>Yêu cầu trả hàng</Link>
                  </div>
                </div>
                <span>{formatPrice(item.price * item.quantity)}</span>
              </div>
            ))}
          </div>

          <div className="shipping-box">
            <h2>Dia chi giao hang</h2>
            <p>{order.shippingAddress}</p>
          </div>

          <div className="order-history">
            <h2>Lich su trang thai</h2>
            {(order.histories || []).map((history) => (
              <div className="history-line" key={history.id}>
                <span>{getOrderStatusLabel(history.status)}</span>
                <p>{history.note || 'Cap nhat trang thai'} - {formatDateTime(history.createdAt)}</p>
              </div>
            ))}
          </div>
        </div>

        <aside className="payment-panel">
          <h2>Thanh toán</h2>
          <div className="summary-line">
            <span>Tạm tính</span>
            <strong>{formatPrice(order.totalAmount + (order.discountAmount || 0))}</strong>
          </div>
          <div className="summary-line">
            <span>Giam gia</span>
            <strong>-{formatPrice(order.discountAmount)}</strong>
          </div>
          <div className="summary-line summary-line--total">
            <span>Tong tien</span>
            <strong>{formatPrice(order.totalAmount)}</strong>
          </div>

          <div className="payment-methods">
            <label>
              <input
                type="radio"
                name="paymentMethod"
                value="COD"
                checked={paymentMethod === 'COD'}
                onChange={(event) => setPaymentMethod(event.target.value)}
              />
              COD
            </label>
            <label>
              <input
                type="radio"
                name="paymentMethod"
                value="VNPAY"
                checked={paymentMethod === 'VNPAY'}
                onChange={(event) => setPaymentMethod(event.target.value)}
              />
              VNPAY
            </label>
          </div>

          <button type="button" disabled={submitting} onClick={handlePaymentCheckout}>
            {submitting ? 'Đang xử lý...' : 'Thanh toán'}
          </button>

          {canCancel && (
            <button type="button" className="danger-button" disabled={submitting} onClick={handleCancelOrder}>
              Hủy đơn hàng
            </button>
          )}

          <button type="button" className="secondary-button" onClick={() => navigate('/payments')}>
            Xem thanh toán
          </button>
        </aside>
      </section>
    </div>
  );
}

export default OrderDetailPage;
