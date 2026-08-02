import { useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import BackLink from '../../components/common/BackLink.jsx';
import { cancelOrder, getMyOrders } from '../../services/orderService.js';
import { formatPrice, getOrderStatusLabel } from '../../utils/formatters.js';

const pageSize = 10;

function getOrderCode(order) {
  return `DH${String(order?.id || '').padStart(5, '0')}`;
}

function formatOrderDate(value) {
  if (!value) {
    return '';
  }

  return new Date(value).toLocaleDateString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  });
}

function getStatusCode(status) {
  if (!status) {
    return '';
  }

  if (typeof status === 'string') {
    return status;
  }

  return status.code || status.name || status.value || '';
}

function getStatusLabel(status) {
  if (!status) {
    return '';
  }

  if (typeof status === 'object') {
    return status.displayName || status.label || status.description || status.code || status.name || '';
  }

  return getOrderStatusLabel(status);
}

function getTotalQuantity(order) {
  return (order?.items || []).reduce((total, item) => total + Number(item.quantity || 0), 0);
}

function getPrimaryItem(order) {
  return order?.items?.[0] || null;
}

function getItemTitle(order) {
  const primaryItem = getPrimaryItem(order);

  if (!primaryItem) {
    return 'Đơn hàng chưa có sản phẩm';
  }

  const remainingCount = Math.max((order.items?.length || 0) - 1, 0);
  return remainingCount > 0
    ? `${primaryItem.productName} +${remainingCount} sản phẩm`
    : primaryItem.productName;
}

function getItemImage(item) {
  return item?.thumbnailUrl
    || item?.productThumbnailUrl
    || item?.imageUrl
    || '/toystore-assets/logo.png';
}

function isCancelable(order) {
  const statusCode = getStatusCode(order?.status);

  if (['CANCELLED', 'COMPLETED', 'FULLY_REFUNDED'].includes(statusCode)) {
    return false;
  }

  const actions = [
    ...(order?.availableActions || []),
    ...(order?.allowedNextActions || []),
    ...(order?.nextActions || []),
  ].map(getStatusCode);

  return actions.includes('CANCEL') || actions.includes('CANCELLED');
}

function OrderListPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const [orders, setOrders] = useState([]);
  const [pageInfo, setPageInfo] = useState({ number: 0, totalPages: 1, totalElements: 0 });
  const [loading, setLoading] = useState(true);
  const [notice, setNotice] = useState('');
  const [actionLoadingId, setActionLoadingId] = useState(null);
  const page = Number(searchParams.get('page') || 0);

  async function loadOrders(isActive = true) {
    setLoading(true);
    setNotice('');

    try {
      const result = await getMyOrders({ page, size: pageSize, sort: 'createdAt,desc' });

      if (!isActive) {
        return;
      }

      setOrders(result.content || []);
      setPageInfo({
        number: result.number || 0,
        totalPages: result.totalPages || 1,
        totalElements: result.totalElements || result.content?.length || 0,
      });
    } catch (err) {
      if (!isActive) {
        return;
      }

      setOrders([]);
      setPageInfo({ number: 0, totalPages: 1, totalElements: 0 });
      setNotice(err.status === 401 || err.status === 403
        ? 'Bạn cần đăng nhập lại để xem lịch sử mua hàng.'
        : err.message || 'Không thể tải lịch sử mua hàng. Vui lòng kiểm tra backend và thử lại.');
    } finally {
      if (isActive) {
        setLoading(false);
      }
    }
  }

  useEffect(() => {
    let active = true;
    loadOrders(active);

    return () => {
      active = false;
    };
  }, [page]);

  function changePage(nextPage) {
    setSearchParams({ page: String(nextPage) });
  }

  async function handleCancel(orderId) {
    const confirmed = window.confirm('Bạn chắc chắn muốn hủy đơn hàng này?');

    if (!confirmed) {
      return;
    }

    setActionLoadingId(orderId);
    setNotice('');

    try {
      await cancelOrder(orderId, 'Khách hàng hủy đơn từ lịch sử mua hàng');
      await loadOrders(true);
      setNotice('Đã gửi yêu cầu hủy đơn hàng.');
    } catch (err) {
      setNotice(err.message || 'Không thể hủy đơn hàng. Vui lòng thử lại.');
    } finally {
      setActionLoadingId(null);
    }
  }

  if (loading) {
    return <div className="page-message">Đang tải lịch sử mua hàng...</div>;
  }

  return (
    <div className="orders-page order-history-page container">
      <BackLink fallback="/" label="Quay lại trang chủ" />

      <section className="order-history-panel">
        <div className="order-history-panel__heading">
          <h1>↻ Lịch sử đơn hàng</h1>
          <span>{pageInfo.totalElements} đơn hàng</span>
        </div>

        {notice && <div className="form-alert form-alert--soft order-history-notice">{notice}</div>}

        <div className="order-history-table-wrap">
          <table className="order-history-table">
            <thead>
              <tr>
                <th>Mã ĐH</th>
                <th>Ngày đặt</th>
                <th>Sản phẩm</th>
                <th>SL</th>
                <th>Tổng tiền</th>
                <th>Trạng thái</th>
                <th>Hành động</th>
              </tr>
            </thead>
            <tbody>
              {orders.map((order) => {
                const primaryItem = getPrimaryItem(order);

                return (
                  <tr key={order.id}>
                    <td>
                      <strong className="order-code">{getOrderCode(order)}</strong>
                    </td>
                    <td>{formatOrderDate(order.createdAt)}</td>
                    <td>
                      <div className="order-product-cell">
                        <img src={getItemImage(primaryItem)} alt={primaryItem?.productName || 'Sản phẩm'} />
                        <span>{getItemTitle(order)}</span>
                      </div>
                    </td>
                    <td>{getTotalQuantity(order)}</td>
                    <td>
                      <strong className="order-total">{formatPrice(order.totalAmount)}</strong>
                    </td>
                    <td>
                      <span className={`order-status-badge order-status-badge--${getStatusCode(order.status).toLowerCase()}`}>
                        {getStatusLabel(order.status)}
                      </span>
                    </td>
                    <td>
                      <div className="order-row-actions">
                        <Link className="order-icon-button" to={`/orders/${order.id}`} title="Xem chi tiết">
                          👁
                        </Link>
                        {isCancelable(order) && (
                          <button
                            type="button"
                            className="order-icon-button order-icon-button--danger"
                            title="Hủy đơn"
                            disabled={actionLoadingId === order.id}
                            onClick={() => handleCancel(order.id)}
                          >
                            ×
                          </button>
                        )}
                      </div>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>

        {orders.length === 0 && (
          <div className="empty-state">Bạn chưa có đơn hàng nào.</div>
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
      </section>
    </div>
  );
}

export default OrderListPage;
