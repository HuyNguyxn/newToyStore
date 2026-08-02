import { useEffect, useState } from 'react';
import {
  archiveNotification,
  getNotifications,
  markAllNotificationsAsRead,
  markNotificationAsRead,
} from '../../services/notificationService.js';
import { formatDateTime } from '../../utils/formatters.js';

const pageSize = 10;

function NotificationPage() {
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
        setNotifications(result.content || []);
        setPageInfo({
          number: result.number || 0,
          totalPages: result.totalPages || 1,
          totalElements: result.totalElements || result.content?.length || 0,
        });
      })
      .catch((err) => setError(err.message || 'Không thể tải thông báo.'))
      .finally(() => setLoading(false));
  }

  async function handleRead(notification) {
    setUpdatingId(notification.id);
    setNotice('');
    setError('');

    try {
      const updated = await markNotificationAsRead(notification.id);
      setNotifications((current) => current.map((item) => (item.id === updated.id ? updated : item)));
    } catch (err) {
      setError(err.message || 'Không thể đánh dấu đã đọc.');
    } finally {
      setUpdatingId(null);
    }
  }

  async function handleArchive(notification) {
    setUpdatingId(notification.id);
    setNotice('');
    setError('');

    try {
      await archiveNotification(notification.id);
      setNotifications((current) => current.filter((item) => item.id !== notification.id));
      setNotice('Đã lưu trữ thông báo.');
    } catch (err) {
      setError(err.message || 'Không thể lưu trữ thông báo.');
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
      setNotice(`Đã đánh dấu ${result.updatedCount || 0} thông báo là đã đọc.`);
      loadNotifications(pageInfo.number);
    } catch (err) {
      setError(err.message || 'Không thể đánh dấu tất cả thông báo.');
      setUpdatingId(null);
    }
  }

  if (loading) {
    return <div className="page-message">Đang tải thông báo...</div>;
  }

  return (
    <section className="notification-page container">
      <div className="page-title-row">
        <div>
          <p>Thong bao</p>
          <h1>Trung tâm thông báo</h1>
        </div>
        <button type="button" disabled={updatingId === 'all'} onClick={handleReadAll}>
          Danh dau tat ca da doc
        </button>
      </div>

      {error && <div className="form-alert">{error}</div>}
      {notice && <div className="form-alert form-alert--success">{notice}</div>}

      {notifications.length === 0 ? (
        <div className="empty-state">Bạn chưa có thông báo nào.</div>
      ) : (
        <div className="notification-list">
          {notifications.map((notification) => (
            <article
              className={notification.readAt ? 'notification-card' : 'notification-card is-unread'}
              key={notification.id}
            >
              <div>
                <span>{notification.type}</span>
                <h2>{notification.title}</h2>
                <p>{notification.message}</p>
                <small>{formatDateTime(notification.occurredAt)}</small>
              </div>

              <div className="notification-card__actions">
                {notification.actionUrl && <a href={notification.actionUrl}>Mo lien ket</a>}
                {!notification.readAt && (
                  <button type="button" disabled={updatingId === notification.id} onClick={() => handleRead(notification)}>
                    Đã đọc
                  </button>
                )}
                <button type="button" disabled={updatingId === notification.id} onClick={() => handleArchive(notification)}>
                  Luu tru
                </button>
              </div>
            </article>
          ))}
        </div>
      )}

      <div className="pagination-bar">
        <button type="button" disabled={pageInfo.number <= 0} onClick={() => loadNotifications(pageInfo.number - 1)}>
          Truoc
        </button>
        <span>Trang {pageInfo.number + 1} / {pageInfo.totalPages}</span>
        <button
          type="button"
          disabled={pageInfo.number + 1 >= pageInfo.totalPages}
          onClick={() => loadNotifications(pageInfo.number + 1)}
        >
          Sau
        </button>
      </div>
    </section>
  );
}

export default NotificationPage;
