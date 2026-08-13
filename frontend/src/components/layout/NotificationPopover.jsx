import { useEffect, useMemo, useRef, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { getNotifications, markNotificationAsRead } from '../../services/notificationService.js';
import { formatDateTime } from '../../utils/formatters.js';
import {
  getNotificationEnumCode,
  getNotificationMessage,
  getNotificationTitle,
  resolveNotificationTarget,
} from '../../utils/notificationUtils.js';

const previewSize = 8;

function NotificationPopover({ unreadCount, onNotificationsChanged }) {
  const navigate = useNavigate();
  const rootRef = useRef(null);
  const [open, setOpen] = useState(false);
  const [activeTab, setActiveTab] = useState('all');
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    function closeOnOutsideClick(event) {
      if (rootRef.current && !rootRef.current.contains(event.target)) setOpen(false);
    }

    document.addEventListener('mousedown', closeOnOutsideClick);
    return () => document.removeEventListener('mousedown', closeOnOutsideClick);
  }, []);

  useEffect(() => {
    if (!open) return undefined;

    let active = true;
    setLoading(true);
    setError('');
    getNotifications({ page: 0, size: previewSize, sort: 'occurredAt,desc' })
      .then((result) => {
        if (active) setNotifications(result?.content || (Array.isArray(result) ? result : []));
      })
      .catch((requestError) => {
        if (active) setError(requestError?.message || 'Không thể tải thông báo.');
      })
      .finally(() => {
        if (active) setLoading(false);
      });

    return () => {
      active = false;
    };
  }, [open]);

  const visibleNotifications = useMemo(() => {
    if (activeTab === 'unread') {
      return notifications.filter((item) => getNotificationEnumCode(item.status) === 'UNREAD' || !item.readAt);
    }
    return notifications;
  }, [activeTab, notifications]);

  async function openNotification(item) {
    const isUnread = getNotificationEnumCode(item.status) === 'UNREAD' || !item.readAt;
    if (isUnread) {
      try {
        const updated = await markNotificationAsRead(item.id);
        setNotifications((current) => current.map((entry) => (entry.id === item.id ? updated : entry)));
        onNotificationsChanged?.();
        window.dispatchEvent(new Event('notifications_updated'));
      } catch {
        // Do not block navigation when updating the read state temporarily fails.
      }
    }

    setOpen(false);
    navigate(resolveNotificationTarget(item));
  }

  return (
    <div className="notification-popover" ref={rootRef}>
      <button
        type="button"
        className={`cart-link notification-popover__trigger${open ? ' is-open' : ''}`}
        aria-label="Thông báo"
        aria-expanded={open}
        aria-haspopup="dialog"
        onClick={() => setOpen((current) => !current)}
      >
        <span aria-hidden="true">🔔</span>
        {unreadCount > 0 && <span className="cart-link__badge">{unreadCount > 99 ? '99+' : unreadCount}</span>}
      </button>

      {open && (
        <section className="notification-popover__panel" role="dialog" aria-label="Thông báo gần đây">
          <div className="notification-popover__header">
            <h2>Thông báo</h2>
            <Link to="/notifications" onClick={() => setOpen(false)}>Xem tất cả</Link>
          </div>

          <div className="notification-popover__tabs" role="tablist" aria-label="Lọc thông báo">
            <button type="button" className={activeTab === 'all' ? 'is-active' : ''} onClick={() => setActiveTab('all')}>Tất cả</button>
            <button type="button" className={activeTab === 'unread' ? 'is-active' : ''} onClick={() => setActiveTab('unread')}>Chưa đọc</button>
          </div>

          <div className="notification-popover__body">
            {loading && <p className="notification-popover__state">Đang tải thông báo...</p>}
            {!loading && error && <p className="notification-popover__state is-error">{error}</p>}
            {!loading && !error && visibleNotifications.length === 0 && (
              <p className="notification-popover__state">Bạn chưa có thông báo {activeTab === 'unread' ? 'chưa đọc' : 'nào'}.</p>
            )}
            {!loading && !error && visibleNotifications.map((item) => {
              const isUnread = getNotificationEnumCode(item.status) === 'UNREAD' || !item.readAt;
              return (
                <button
                  type="button"
                  key={item.id}
                  className={`notification-popover__item${isUnread ? ' is-unread' : ''}`}
                  onClick={() => openNotification(item)}
                >
                  <img src="/toystore-assets/logo.png" alt="" />
                  <span className="notification-popover__content">
                    <strong>{getNotificationTitle(item)}</strong>
                    <span>{getNotificationMessage(item.message)}</span>
                    <small>{formatDateTime(item.occurredAt || item.createdAt)}</small>
                  </span>
                  {isUnread && <i className="notification-popover__unread-dot" aria-label="Chưa đọc" />}
                </button>
              );
            })}
          </div>

          <Link className="notification-popover__footer" to="/notifications" onClick={() => setOpen(false)}>
            Mở trung tâm thông báo
          </Link>
        </section>
      )}
    </div>
  );
}

export default NotificationPopover;
