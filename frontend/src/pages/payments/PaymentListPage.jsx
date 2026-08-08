import { useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { getMyPayments } from '../../services/paymentService.js';
import { formatDateTime, formatPaymentMethodText, formatPrice, getPaymentStatusLabel } from '../../utils/formatters.js';

const pageSize = 8;

function PaymentListPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const [payments, setPayments] = useState([]);
  const [pageInfo, setPageInfo] = useState({ number: 0, totalPages: 1, totalElements: 0 });
  const [loading, setLoading] = useState(true);
  const [notice, setNotice] = useState('');
  const page = Number(searchParams.get('page') || 0);

  useEffect(() => {
    let active = true;
    setLoading(true);
    setNotice('');

    getMyPayments({ page, size: pageSize, sort: 'createdAt,desc' })
      .then((result) => {
        if (!active) {
          return;
        }
        setPayments(result.content || []);
        setPageInfo({
          number: result.number || 0,
          totalPages: result.totalPages || 1,
          totalElements: result.totalElements || result.content?.length || 0,
        });
      })
      .catch(() => {
        if (!active) {
          return;
        }
        setPayments([]);
        setPageInfo({ number: 0, totalPages: 1, totalElements: 0 });
      })
      .finally(() => {
        if (active) {
          setLoading(false);
        }
      });

    return () => {
      active = false;
    };
  }, [page]);

  function changePage(nextPage) {
    setSearchParams({ page: String(nextPage) });
  }

  if (loading) {
    return <div className="page-message">Đang tải thanh toán...</div>;
  }

  return (
    <div className="payments-page container">
      <div className="page-title-row">
        <div>
          <p>Thanh toán của tôi</p>
          <h1>Lich su thanh toan</h1>
        </div>
        <span>{pageInfo.totalElements} giao dich</span>
      </div>

      {notice && <div className="form-alert form-alert--soft">{notice}</div>}

      <div className="payment-list">
        {payments.map((payment) => (
          <article className="payment-card" key={payment.id}>
            <div>
              <span className="status-badge">{getPaymentStatusLabel(payment.status)}</span>
              <h2>Thanh toán #{payment.id}</h2>
              <p>Don hang #{payment.orderId} - {formatDateTime(payment.createdAt)}</p>
            </div>
            <div>
              <span>{formatPaymentMethodText(payment.method)}</span>
              <strong>{formatPrice(payment.amount)}</strong>
            </div>
            <Link to={`/orders/${payment.orderId}`}>Xem don</Link>
          </article>
        ))}
      </div>

      {payments.length === 0 && (
        <div className="empty-state">Bạn chưa có giao dịch thanh toán nào.</div>
      )}

      <div className="pagination-bar">
        <button type="button" disabled={pageInfo.number <= 0} onClick={() => changePage(pageInfo.number - 1)}>
          Truoc
        </button>
        <span>Trang {pageInfo.number + 1} / {pageInfo.totalPages}</span>
        <button
          type="button"
          disabled={pageInfo.number + 1 >= pageInfo.totalPages}
          onClick={() => changePage(pageInfo.number + 1)}
        >
          Sau
        </button>
      </div>
    </div>
  );
}

export default PaymentListPage;
