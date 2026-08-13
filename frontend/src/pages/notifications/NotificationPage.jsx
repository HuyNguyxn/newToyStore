import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  archiveNotification,
  getNotifications,
  markAllNotificationsAsRead,
  markNotificationAsRead,
} from '../../services/notificationService.js';
import { formatDateTime } from '../../utils/formatters.js';
import { resolveNotificationTarget } from '../../utils/notificationUtils.js';

const pageSize = 10;

const titleMap = {
  'Cart item expiring': 'Giỏ hàng',
  'Cart item is expiring': 'Sản phẩm giỏ hàng sắp hết hạn',
  'Cart status updated': 'Cập nhật trạng thái giỏ hàng',
  'Order created': 'Tạo đơn hàng',
  'Order status changed': 'Cập nhật đơn hàng',
  'Order cancelled': 'Hủy đơn hàng',
  'Payment completed': 'Thanh toán thành công',
  'Payment failed': 'Thanh toán thất bại',
  'Refund completed': 'Hoàn tiền thành công',
  'Payment refunded': 'Hoàn tiền thanh toán',
  'Shipment created': 'Tạo đơn vận chuyển',
  'Shipment in transit': 'Đang vận chuyển',
  'Shipment delivered': 'Giao hàng thành công',
  'Shipment returned': 'Trả hàng thành công',
  'Shipment cancelled': 'Hủy đơn vận chuyển',
  'Return status changed': 'Cập nhật yêu cầu trả hàng',
  'Return refunded': 'Hoàn tiền trả hàng',
  'Review replied': 'Phản hồi đánh giá',
  'Review status changed': 'Cập nhật đánh giá',
  'System announcement': 'Thông báo hệ thống',
};

function renderTypeLabel(type) {
  if (!type) return 'Hệ thống';
  if (typeof type === 'object') {
    const label = type.displayName || type.code || type.name || 'Hệ thống';
    return titleMap[label] || label;
  }
  const str = String(type);
  return titleMap[str] || str;
}

function formatTitle(title) {
  if (!title) return '';
  return titleMap[title] || title;
}

function formatMessage(msg) {
  if (!msg) return '';
  let text = String(msg);
  text = text.replace(/Payment for order #(\d+) was completed successfully\./gi, 'Thanh toán cho đơn hàng #$1 đã hoàn tất thành công.');
  text = text.replace(/Payment for order #(\d+) failed\./gi, 'Thanh toán cho đơn hàng #$1 thất bại.');
  text = text.replace(/Refund for order #(\d+) was completed\./gi, 'Hoàn tiền cho đơn hàng #$1 đã hoàn tất.');
  text = text.replace('Your cart status changed to ACTIVE.', 'Trạng thái giỏ hàng của bạn đã chuyển sang Đang hoạt động.');
  text = text.replace('Your cart status changed to CHECKING_OUT.', 'Trạng thái giỏ hàng của bạn đã chuyển sang Đang thanh toán.');
  text = text.replace('Your cart status changed to ABANDONED.', 'Giỏ hàng của bạn đã bị bỏ quên.');
  text = text.replace('Your cart status changed to CONVERTED.', 'Giỏ hàng của bạn đã được chuyển thành đơn hàng.');
  return text;
}

function NotificationPage() {
  const navigate = useNavigate();
  const [notifications, setNotifications] = useState([]);
  const [pageInfo, setPageInfo] = useState({ number: 0, totalPages: 1, totalElements: 0 });
  const [loading, setLoading] = useState(true);
  const [updatingId, setUpdatingId] = useState(null);
  const [notice, setNotice] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    loadNotifications(0);
  }, []);

  function loadNotifications(page = pageInfo.number) {
    setLoading(true);
    setError('');

    getNotifications({ page, size: pageSize, sort: 'occurredAt,desc' })
      .then((result) => {
        const rawList = result?.content || (Array.isArray(result) ? result : []);
        // Filter out redundant internal cart status logs (e.g. ACTIVE <-> CHECKING_OUT transitions)
        const list = rawList.filter((item) => {
          const title = item.title || '';
          const dedup = item.deduplicationKey || '';
          return !title.includes('Cart status updated') && !title.includes('Cập nhật trạng thái giỏ hàng') && !dedup.startsWith('CART_STATUS:');
        });
        setNotifications(list);
        setPageInfo({
          number: result?.number || 0,
          totalPages: result?.totalPages || 1,
          totalElements: result?.totalElements || list.length,
        });
        window.dispatchEvent(new Event('notifications_updated'));
      })
      .catch((err) => {
        console.error('Notification error:', err);
        setError(err?.message || 'Không thể tải danh sách thông báo.');
      })
      .finally(() => setLoading(false));
  }

  async function handleRead(notification) {
    setUpdatingId(notification.id);
    setNotice('');
    setError('');

    try {
      const updated = await markNotificationAsRead(notification.id);
      setNotifications((current) => current.map((item) => (item.id === updated.id ? updated : item)));
      setNotice('Đã đánh dấu thông báo là đã đọc.');
      window.dispatchEvent(new Event('notifications_updated'));
    } catch (err) {
      setError(err?.message || 'Không thể đánh dấu đã đọc.');
    } finally {
      setUpdatingId(null);
    }
  }

  async function handleOpen(notification) {
    if (!notification.readAt) {
      try {
        const updated = await markNotificationAsRead(notification.id);
        setNotifications((current) => current.map((item) => (item.id === updated.id ? updated : item)));
        window.dispatchEvent(new Event('notifications_updated'));
      } catch (err) {
        setError(err?.message || 'Không thể cập nhật trạng thái thông báo.');
      }
    }
    navigate(resolveNotificationTarget(notification));
  }

  async function handleArchive(notification) {
    setUpdatingId(notification.id);
    setNotice('');
    setError('');

    try {
      await archiveNotification(notification.id);
      setNotifications((current) => current.filter((item) => item.id !== notification.id));
      setNotice('Đã lưu trữ thông báo.');
      window.dispatchEvent(new Event('notifications_updated'));
    } catch (err) {
      setError(err?.message || 'Không thể lưu trữ thông báo.');
    } finally {
      setUpdatingId(null);
    }
  }

  async function handleReadAll() {
    setUpdatingId('all');
    setNotice('');
    setError('');

    try {
      const result = await markAllNotificationsAsRead();
      const count = result?.updatedCount ?? (typeof result === 'number' ? result : 0);
      setNotice(`Đã đánh dấu ${count} thông báo là đã đọc.`);
      window.dispatchEvent(new Event('notifications_updated'));
      loadNotifications(pageInfo.number);
    } catch (err) {
      setError(err?.message || 'Không thể đánh dấu tất cả thông báo.');
      setUpdatingId(null);
    }
  }

  if (loading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '60vh', fontFamily: 'system-ui' }}>
        <div style={{ color: '#ea580c', fontSize: '15px', fontWeight: '700' }}>Đang tải danh sách thông báo...</div>
      </div>
    );
  }

  return (
    <div style={{ background: '#f8fafc', minHeight: '100vh', padding: '30px 16px', fontFamily: 'system-ui, -apple-system, sans-serif' }}>
      <div className="container" style={{ maxWidth: '900px', margin: '0 auto' }}>
        
        {/* Header Card */}
        <div style={{ background: '#ffffff', borderRadius: '12px', border: '1px solid #e2e8f0', padding: '24px', marginBottom: '20px', boxShadow: '0 4px 16px rgba(0,0,0,0.02)', display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '16px' }}>
          <div>
            <span style={{ color: '#ea580c', fontWeight: '800', fontSize: '12px', textTransform: 'uppercase', letterSpacing: '0.5px' }}>
              Hệ thống
            </span>
            <h1 style={{ fontSize: '22px', fontWeight: '900', color: '#0f172a', margin: '4px 0 0 0' }}>
              🔔 Trung tâm thông báo
            </h1>
          </div>

          <button
            type="button"
            disabled={updatingId === 'all' || notifications.length === 0}
            onClick={handleReadAll}
            style={{
              display: 'inline-flex',
              alignItems: 'center',
              gap: '8px',
              padding: '10px 20px',
              background: 'linear-gradient(135deg, #fff7ed 0%, #ffedd5 100%)',
              color: '#ea580c',
              border: '1.5px solid #fed7aa',
              borderRadius: '20px',
              fontSize: '13px',
              fontWeight: '800',
              cursor: (updatingId === 'all' || notifications.length === 0) ? 'not-allowed' : 'pointer',
              boxShadow: '0 2px 10px rgba(234,88,12,0.1)',
              transition: 'all 0.2s ease',
              opacity: (updatingId === 'all' || notifications.length === 0) ? 0.6 : 1,
            }}
          >
            <span>✓✓</span>
            <span>{updatingId === 'all' ? 'Đang xử lý...' : 'Đánh dấu tất cả đã đọc'}</span>
          </button>
        </div>

        {/* Alerts */}
        {error && (
          <div style={{ background: '#fef2f2', color: '#dc2626', border: '1px solid #fecaca', padding: '12px 16px', borderRadius: '8px', fontSize: '13px', fontWeight: '700', marginBottom: '20px' }}>
            ⚠️ {error}
          </div>
        )}
        {notice && (
          <div style={{ background: '#f0fdf4', color: '#16a34a', border: '1px solid #bbf7d0', padding: '12px 16px', borderRadius: '8px', fontSize: '13px', fontWeight: '700', marginBottom: '20px' }}>
            ✓ {notice}
          </div>
        )}

        {/* Notifications List */}
        {notifications.length === 0 ? (
          <div style={{ background: '#ffffff', borderRadius: '12px', border: '1px solid #e2e8f0', padding: '40px 20px', textAlign: 'center', color: '#64748b', fontSize: '14px' }}>
            🎉 Bạn hiện không có thông báo nào.
          </div>
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '14px', marginBottom: '24px' }}>
            {notifications.map((item) => {
              const isUnread = !item.readAt;
              return (
                <div
                  key={item.id}
                  style={{
                    background: isUnread ? '#fffaf5' : '#ffffff',
                    borderRadius: '12px',
                    border: isUnread ? '1.5px solid #fed7aa' : '1px solid #e2e8f0',
                    padding: '20px',
                    boxShadow: isUnread ? '0 4px 14px rgba(234,88,12,0.06)' : '0 2px 8px rgba(0,0,0,0.02)',
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'flex-start',
                    gap: '16px',
                    transition: 'all 0.15s ease',
                  }}
                >
                  <div style={{ flex: 1 }}>

                    <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '6px' }}>
                      {isUnread && (
                        <span style={{ width: '8px', height: '8px', borderRadius: '50%', background: '#ea580c', display: 'inline-block' }} />
                      )}
                      <span style={{ fontSize: '11.5px', fontWeight: '800', color: '#ea580c', background: '#fff7ed', border: '1px solid #ffedd5', padding: '2px 8px', borderRadius: '12px' }}>
                        {renderTypeLabel(item.type)}
                      </span>
                    </div>

                    <h3 style={{ fontSize: '15px', fontWeight: '800', color: '#0f172a', margin: '0 0 6px 0' }}>
                      {formatTitle(item.title)}
                    </h3>
                    
                    <p style={{ fontSize: '13.5px', color: '#475569', margin: '0 0 10px 0', lineHeight: 1.5 }}>
                      {formatMessage(item.message)}
                    </p>

                    <span style={{ fontSize: '12px', color: '#94a3b8' }}>
                      🕒 {formatDateTime(item.occurredAt || item.createdAt)}
                    </span>
                  </div>

                  {/* Actions */}
                  <div style={{ display: 'flex', gap: '8px', alignItems: 'center', flexShrink: 0 }}>
                    {(item.actionUrl || item.referenceId) && (
                      <button
                        type="button"
                        onClick={() => handleOpen(item)}
                        style={{
                          padding: '6px 12px',
                          background: '#eff6ff',
                          color: '#2563eb',
                          borderRadius: '8px',
                          fontSize: '12px',
                          fontWeight: '700',
                          textDecoration: 'none',
                          border: '1px solid #bfdbfe',
                        }}
                      >
                        Xem nội dung
                      </button>
                    )}
                    {isUnread && (
                      <button
                        type="button"
                        disabled={updatingId === item.id}
                        onClick={() => handleRead(item)}
                        style={{
                          padding: '6px 12px',
                          background: '#fff7ed',
                          color: '#ea580c',
                          border: '1px solid #fed7aa',
                          borderRadius: '8px',
                          fontSize: '12px',
                          fontWeight: '700',
                          cursor: 'pointer',
                        }}
                      >
                        Đánh dấu đã đọc
                      </button>
                    )}
                    <button
                      type="button"
                      disabled={updatingId === item.id}
                      onClick={() => handleArchive(item)}
                      style={{
                        padding: '6px 12px',
                        background: '#ffffff',
                        color: '#64748b',
                        border: '1px solid #cbd5e1',
                        borderRadius: '8px',
                        fontSize: '12px',
                        fontWeight: '600',
                        cursor: 'pointer',
                      }}
                    >
                      Lưu trữ
                    </button>
                  </div>
                </div>
              );
            })}
          </div>
        )}

        {/* Pagination Bar */}
        {pageInfo.totalPages > 1 && (
          <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', gap: '12px', marginTop: '20px' }}>
            <button
              type="button"
              disabled={pageInfo.number <= 0}
              onClick={() => loadNotifications(pageInfo.number - 1)}
              style={{
                padding: '8px 16px',
                background: '#ffffff',
                color: pageInfo.number <= 0 ? '#cbd5e1' : '#475569',
                border: '1px solid #cbd5e1',
                borderRadius: '8px',
                fontSize: '13px',
                fontWeight: '700',
                cursor: pageInfo.number <= 0 ? 'not-allowed' : 'pointer',
              }}
            >
              ← Trang trước
            </button>
            <span style={{ fontSize: '13px', color: '#64748b', fontWeight: '600' }}>
              Trang {pageInfo.number + 1} / {pageInfo.totalPages}
            </span>
            <button
              type="button"
              disabled={pageInfo.number + 1 >= pageInfo.totalPages}
              onClick={() => loadNotifications(pageInfo.number + 1)}
              style={{
                padding: '8px 16px',
                background: '#ffffff',
                color: pageInfo.number + 1 >= pageInfo.totalPages ? '#cbd5e1' : '#475569',
                border: '1px solid #cbd5e1',
                borderRadius: '8px',
                fontSize: '13px',
                fontWeight: '700',
                cursor: pageInfo.number + 1 >= pageInfo.totalPages ? 'not-allowed' : 'pointer',
              }}
            >
              Trang sau →
            </button>
          </div>
        )}

      </div>
    </div>
  );
}

export default NotificationPage;
