import { useEffect, useState } from 'react';
import {
  broadcastNotification,
  getNotificationPreferences,
  updateNotificationPreferences,
} from '../../services/notificationService.js';

const preferenceFields = [
  'inAppEnabled',
  'emailEnabled',
  'orderEnabled',
  'paymentEnabled',
  'shipmentEnabled',
  'returnEnabled',
  'reviewEnabled',
  'cartEnabled',
  'systemEnabled',
];

function AdminNotificationPage() {
  const [preferences, setPreferences] = useState(Object.fromEntries(preferenceFields.map((field) => [field, true])));
  const [broadcastForm, setBroadcastForm] = useState({
    requestKey: `broadcast-${Date.now()}`,
    title: '',
    message: '',
    actionUrl: '',
    sendEmail: false,
  });
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    getNotificationPreferences()
      .then((result) => setPreferences({ ...preferences, ...result }))
      .catch((err) => setError(err.message || 'Kh?ng th? t?i c?u h?nh th?ng b?o.'))
      .finally(() => setLoading(false));
  }, []);

  function updatePreference(field, value) {
    setPreferences((current) => ({ ...current, [field]: value }));
  }

  function updateBroadcast(field, value) {
    setBroadcastForm((current) => ({ ...current, [field]: value }));
  }

  async function savePreferences(event) {
    event.preventDefault();
    setError('');
    setMessage('');
    setLoading(true);
    try {
      const result = await updateNotificationPreferences(preferences);
      setPreferences(result);
      setMessage('?? l?u notification preferences.');
    } catch (err) {
      setError(err.message || 'L?u preferences th?t b?i.');
    } finally {
      setLoading(false);
    }
  }

  async function sendBroadcast(event) {
    event.preventDefault();
    setError('');
    setMessage('');
    setLoading(true);
    try {
      const result = await broadcastNotification(broadcastForm);
      setMessage(`?? broadcast: created ${result.createdCount || 0}, skipped ${result.skippedCount || 0}.`);
      setBroadcastForm({
        requestKey: `broadcast-${Date.now()}`,
        title: '',
        message: '',
        actionUrl: '',
        sendEmail: false,
      });
    } catch (err) {
      setError(err.message || 'Broadcast th?t b?i.');
    } finally {
      setLoading(false);
    }
  }

  if (loading && !message && !error) {
    return <div className="page-message">Dang tai notification admin...</div>;
  }

  return (
    <section className="admin-resource">
      <div className="admin-resource__hero">
        <div>
          <p>Admin Messaging</p>
          <h2>Notification</h2>
          <span>Manage current admin notification preferences and send admin broadcast messages.</span>
        </div>
      </div>

      {error && <div className="form-alert">{error}</div>}
      {message && <div className="form-alert form-alert--success">{message}</div>}

      <div className="admin-crud-grid">
        <form className="admin-api-console" onSubmit={savePreferences}>
          <div className="admin-panel__heading">
            <div>
              <p>Preferences</p>
              <h2>Notification channels</h2>
            </div>
          </div>

          {preferenceFields.map((field) => (
            <label className="inline-check" key={field}>
              <input
                type="checkbox"
                checked={Boolean(preferences[field])}
                onChange={(event) => updatePreference(field, event.target.checked)}
              />
              {field}
            </label>
          ))}

          <button type="submit" disabled={loading}>Save preferences</button>
        </form>

        <form className="admin-api-console" onSubmit={sendBroadcast}>
          <div className="admin-panel__heading">
            <div>
              <p>Broadcast</p>
              <h2>Send to users</h2>
            </div>
          </div>

          <label>Request key<input value={broadcastForm.requestKey} onChange={(event) => updateBroadcast('requestKey', event.target.value)} required maxLength="80" /></label>
          <label>Title<input value={broadcastForm.title} onChange={(event) => updateBroadcast('title', event.target.value)} required maxLength="150" /></label>
          <label>Message<textarea rows="5" value={broadcastForm.message} onChange={(event) => updateBroadcast('message', event.target.value)} required maxLength="500" /></label>
          <label>Action URL<input value={broadcastForm.actionUrl} onChange={(event) => updateBroadcast('actionUrl', event.target.value)} maxLength="255" /></label>
          <label className="inline-check"><input type="checkbox" checked={broadcastForm.sendEmail} onChange={(event) => updateBroadcast('sendEmail', event.target.checked)} /> Send email</label>
          <button type="submit" disabled={loading}>Send broadcast</button>
        </form>
      </div>
    </section>
  );
}

export default AdminNotificationPage;
