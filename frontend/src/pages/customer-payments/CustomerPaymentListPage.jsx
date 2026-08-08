import { useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { getMyCustomerPayments } from '../../services/customerPaymentService.js';
import { formatDateTime, formatPaymentMethodText, formatPrice, getPaymentStatusLabel } from '../../utils/formatters.js';

const pageSize = 8;

function CustomerPaymentListPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const [payments, setPayments] = useState([]);
  const [pageInfo, setPageInfo] = useState({ number: 0, totalPages: 1, totalElements: 0 });
  const [loading, setLoading] = useState(true);
  const page = Number(searchParams.get('page') || 0);

  useEffect(() => {
    let active = true;
    setLoading(true);

    getMyCustomerPayments({ page, size: pageSize, sort: 'createdAt,desc' })
      .then((result) => {
        if (!active) return;
        setPayments(result.content || []);
        setPageInfo({
          number: result.number || 0,
          totalPages: result.totalPages || 1,
          totalElements: result.totalElements || result.content?.length || 0,
        });
      })
      .catch(() => {
        if (!active) return;
        setPayments([]);
        setPageInfo({ number: 0, totalPages: 1, totalElements: 0 });
      })
      .finally(() => {
        if (active) setLoading(false);
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
          <h1>Lịch sử thanh toán</h1>
        </div>
        <span>{pageInfo.totalElements} giao dịch</span>
      </div>

      <div className="payment-list">
        {payments.map((payment) => (
          <article className="payment-card" key={payment.id}>
            <div>
              <span className="status-badge">{getPaymentStatusLabel(payment.status)}</span>
              <h2>Thanh toán #{payment.id}</h2>
              <p>Đơn hàng #{payment.orderId} - {formatDateTime(payment.createdAt)}</p>
            </div>
            <div>
              <span>{formatPaymentMethodText(payment.method)}</span>
              <strong>{formatPrice(payment.amount)}</strong>
            </div>
            <Link to={`/orders/${payment.orderId}`}>Xem đơn</Link>
          </article>
        ))}
      </div>

      {payments.length === 0 && (
        <div className="empty-state">Bạn chưa có giao dịch thanh toán nào.</div>
      )}

      <div className="pagination-bar">
        <button type="button" disabled={pageInfo.number <= 0} onClick={() => changePage(pageInfo.number - 1)}>
          Trước
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

export default CustomerPaymentListPage;
