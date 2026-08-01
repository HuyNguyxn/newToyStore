import { useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { sampleOrders } from '../../data/sampleData.js';
import { getMyOrders } from '../../services/orderService.js';
import { formatDateTime, formatPrice, getOrderStatusLabel } from '../../utils/formatters.js';

const pageSize = 8;

function OrderListPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const [orders, setOrders] = useState([]);
  const [pageInfo, setPageInfo] = useState({ number: 0, totalPages: 1, totalElements: 0 });
  const [loading, setLoading] = useState(true);
  const [notice, setNotice] = useState('');
  const page = Number(searchParams.get('page') || 0);

  useEffect(() => {
    let active = true;
    setLoading(true);
    setNotice('');

    getMyOrders({ page, size: pageSize, sort: 'createdAt,desc' })
      .then((result) => {
        if (!active) {
          return;
        }
        setOrders(result.content || []);
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
        setOrders(sampleOrders);
        setPageInfo({ number: 0, totalPages: 1, totalElements: sampleOrders.length });
        setNotice('Backend chua san sang, dang hien thi don hang mau.');
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
    return <div className="page-message">Dang tai don hang...</div>;
  }

  return (
    <div className="orders-page container">
      <div className="page-title-row">
        <div>
          <p>Don hang cua toi</p>
          <h1>Lich su mua hang</h1>
        </div>
        <span>{pageInfo.totalElements} don hang</span>
      </div>

      {notice && <div className="form-alert form-alert--soft">{notice}</div>}

      <div className="order-list">
        {orders.map((order) => (
          <article className="order-card" key={order.id}>
            <div>
              <span className="status-badge">{getOrderStatusLabel(order.status)}</span>
              <h2>Don hang #{order.id}</h2>
              <p>{formatDateTime(order.createdAt)}</p>
            </div>
            <div>
              <span>Tong tien</span>
              <strong>{formatPrice(order.totalAmount)}</strong>
            </div>
            <Link to={`/orders/${order.id}`}>Xem chi tiet</Link>
          </article>
        ))}
      </div>

      {orders.length === 0 && (
        <div className="empty-state">Ban chua co don hang nao.</div>
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

export default OrderListPage;
