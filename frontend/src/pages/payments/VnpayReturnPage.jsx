import { useEffect, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { handleVnpayReturn, checkoutPayment, createIdempotencyKey } from '../../services/paymentService.js';
import { formatPrice, getPaymentStatusLabel } from '../../utils/formatters.js';

function VnpayReturnPage() {
  const location = useLocation();
  const navigate = useNavigate();
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    let active = true;

    handleVnpayReturn(location.search)
      .then((response) => {
        if (active) {
          setResult(response);
        }
      })
      .catch((err) => {
        if (active) {
          setError(err.message || 'Không thể xác nhận kết quả thanh toán VNPAY.');
        }
      })
      .finally(() => {
        if (active) {
          setLoading(false);
        }
      });

    return () => {
      active = false;
    };
  }, [location.search]);

  async function handleRetryVnpayPayment() {
    const orderId = result?.payment?.orderId;
    if (!orderId) return;

    setSubmitting(true);
    try {
      const payment = await checkoutPayment({
        orderId: orderId,
        method: 'VNPAY',
        idempotencyKey: createIdempotencyKey(orderId, 'VNPAY'),
      });

      if (payment.paymentUrl) {
        window.location.href = payment.paymentUrl;
        return;
      }
    } catch (err) {
      alert(err.message || 'Không thể khởi tạo lại cổng thanh toán VNPay.');
    } finally {
      setSubmitting(false);
    }
  }

  if (loading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '60vh', fontFamily: 'system-ui' }}>
        <div style={{ color: '#ea580c', fontSize: '16px', fontWeight: '800' }}>⏳ Đang xác nhận kết quả thanh toán VNPAY...</div>
      </div>
    );
  }

  const statusStr = (
    typeof result?.payment?.status === 'object' && result?.payment?.status !== null
      ? (result.payment.status.code || result.payment.status.name || '')
      : String(result?.payment?.status || '')
  ).toUpperCase();

  const isSuccess =
    result?.responseCode === '00' ||
    result?.vnp_ResponseCode === '00' ||
    statusStr === 'SUCCEEDED' ||
    statusStr === 'PAID' ||
    statusStr === 'SUCCESS';

  const orderId = result?.payment?.orderId;

  return (
    <div style={{ background: '#f8fafc', minHeight: '85vh', padding: '40px 16px', fontFamily: 'system-ui, -apple-system, sans-serif', display: 'flex', justifyContent: 'center', alignItems: 'center' }}>
      <div style={{ background: '#ffffff', width: '100%', maxWidth: '560px', borderRadius: '16px', border: '1px solid #e2e8f0', boxShadow: '0 10px 25px rgba(0,0,0,0.05)', padding: '40px 30px', textAlign: 'center' }}>
        
        {/* ICON */}
        <div style={{ width: '72px', height: '72px', borderRadius: '50%', background: isSuccess ? '#d1fae5' : '#fef2f2', color: isSuccess ? '#10b981' : '#ef4444', fontSize: '36px', fontWeight: 'bold', display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto 20px auto', border: `2px solid ${isSuccess ? '#a7f3d0' : '#fecaca'}` }}>
          {isSuccess ? '✓' : '✕'}
        </div>

        <h1 style={{ fontSize: '22px', fontWeight: '900', color: '#0f172a', margin: '0 0 10px 0' }}>
          {isSuccess ? 'Thanh toán VNPay thành công!' : 'Thanh toán VNPay chưa hoàn tất'}
        </h1>
        
        <p style={{ fontSize: '14px', color: '#64748b', margin: '0 0 24px 0', lineHeight: 1.5 }}>
          {isSuccess
            ? 'Cảm ơn bạn! Giao dịch thanh toán đã được xác nhận thành công trên hệ thống.'
            : 'Giao dịch bị hủy hoặc chưa hoàn tất. Đơn hàng của bạn đã được tạo và lưu trữ trong hệ thống.'}
        </p>

        {/* DETAILS CARD */}
        {result?.payment && (
          <div style={{ background: '#f8fafc', border: '1px solid #e2e8f0', borderRadius: '10px', padding: '16px 20px', marginBottom: '28px', textAlign: 'left', fontSize: '13.5px', color: '#334155' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '10px' }}>
              <span style={{ color: '#64748b' }}>Mã đơn hàng:</span>
              <strong style={{ color: '#ea580c', fontWeight: '800' }}>#{orderId}</strong>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '10px' }}>
              <span style={{ color: '#64748b' }}>Trạng thái thanh toán:</span>
              <strong style={{ color: isSuccess ? '#10b981' : '#ef4444' }}>
                {getPaymentStatusLabel(result.payment.status)}
              </strong>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between' }}>
              <span style={{ color: '#64748b' }}>Tổng số tiền:</span>
              <strong style={{ color: '#dc2626', fontSize: '15px' }}>{formatPrice(result.payment.amount)}</strong>
            </div>
          </div>
        )}

        {/* ACTIONS */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
          {!isSuccess && orderId && (
            <button
              type="button"
              onClick={handleRetryVnpayPayment}
              disabled={submitting}
              style={{
                width: '100%',
                padding: '13px',
                background: 'linear-gradient(135deg, #2563eb, #1d4ed8)',
                color: '#ffffff',
                border: 'none',
                borderRadius: '10px',
                fontSize: '14.5px',
                fontWeight: '800',
                cursor: 'pointer',
                boxShadow: '0 4px 12px rgba(37,99,235,0.25)',
              }}
            >
              {submitting ? 'Đang mở cổng VNPAY...' : '💳 Thanh toán lại ngay bằng VNPAY'}
            </button>
          )}

          {orderId && (
            <Link
              to={`/orders/${orderId}`}
              style={{
                display: 'block',
                padding: '12px',
                background: '#ffffff',
                color: '#0f172a',
                border: '1px solid #cbd5e1',
                borderRadius: '10px',
                fontSize: '14px',
                fontWeight: '700',
                textDecoration: 'none',
              }}
            >
              📋 Xem chi tiết đơn hàng #{orderId}
            </Link>
          )}

          <Link
            to="/products"
            style={{
              display: 'block',
              padding: '12px',
              background: '#f1f5f9',
              color: '#475569',
              borderRadius: '10px',
              fontSize: '13.5px',
              fontWeight: '700',
              textDecoration: 'none',
            }}
          >
            🛒 Tiếp tục mua sắm
          </Link>
        </div>

      </div>
    </div>
  );
}

export default VnpayReturnPage;
