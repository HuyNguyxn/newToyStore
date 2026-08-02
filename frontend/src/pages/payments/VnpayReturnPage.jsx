import { useEffect, useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { handleVnpayReturn } from '../../services/paymentService.js';
import { formatPrice, getPaymentStatusLabel } from '../../utils/formatters.js';

function VnpayReturnPage() {
  const location = useLocation();
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(true);
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

  if (loading) {
    return <div className="page-message">Đang xác nhận thanh toán VNPAY...</div>;
  }

  return (
    <div className="vnpay-return-page container">
      <section className="result-card">
        <p>Ket qua thanh toan</p>
        <h1>{result?.message || error || 'Đã nhận phản hồi từ VNPAY'}</h1>

        {result?.payment && (
          <div className="result-details">
            <div>
              <span>Don hang</span>
              <strong>#{result.payment.orderId}</strong>
            </div>
            <div>
              <span>Trang thai</span>
              <strong>{getPaymentStatusLabel(result.payment.status)}</strong>
            </div>
            <div>
              <span>So tien</span>
              <strong>{formatPrice(result.payment.amount)}</strong>
            </div>
          </div>
        )}

        <div className="result-actions">
          {result?.payment?.orderId && <Link to={`/orders/${result.payment.orderId}`}>Xem đơn hàng</Link>}
          <Link to="/payments">Lich su thanh toan</Link>
        </div>
      </section>
    </div>
  );
}

export default VnpayReturnPage;
