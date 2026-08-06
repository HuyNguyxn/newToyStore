import { useEffect, useState } from 'react';
import {
  broadcastNotification,
  getNotificationPreferences,
  updateNotificationPreferences,
} from '../../services/notificationService.js';

const preferenceLabels = {
  inAppEnabled: 'Thông báo trong ứng dụng (In-App)',
  emailEnabled: 'Gửi qua hòm thư điện tử (Email)',
  orderEnabled: 'Thông báo liên quan đến Đơn hàng',
  paymentEnabled: 'Thông báo liên quan đến Thanh toán',
  shipmentEnabled: 'Thông báo liên quan đến Vận chuyển',
  returnEnabled: 'Thông báo liên quan đến Trả hàng / Hoàn tiền',
  reviewEnabled: 'Thông báo liên quan đến Đánh giá sản phẩm',
  cartEnabled: 'Thông báo nhắc nhở Giỏ hàng trống / Bỏ quên',
  systemEnabled: 'Thông báo bảo trì / Cập nhật hệ thống',
};

const preferenceFields = Object.keys(preferenceLabels);

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
      .catch((err) => setError(err.message || 'Không thể tải cấu hình nhận thông báo.'))
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
      setMessage('Lưu cấu hình thông báo thành công.');
    } catch (err) {
      setError(err.message || 'Lưu cấu hình thông báo thất bại.');
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
      setMessage(`Gửi thông báo thành công: đã tạo ${result.createdCount || 0} tin nhắn, bỏ qua ${result.skippedCount || 0} người dùng.`);
      setBroadcastForm({
        requestKey: `broadcast-${Date.now()}`,
        title: '',
        message: '',
        actionUrl: '',
        sendEmail: false,
      });
    } catch (err) {
      setError(err.message || 'Gửi thông báo hàng loạt thất bại.');
    } finally {
      setLoading(false);
    }
  }

  if (loading && !message && !error) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '60vh', fontFamily: 'system-ui' }}>
        <div style={{ color: '#ea580c', fontSize: '14px', fontWeight: '700' }}>Đang tải cấu hình thông báo...</div>
      </div>
    );
  }

  return (
    <section style={{ padding: '24px', background: '#f8fafc', minHeight: '100vh', fontFamily: 'system-ui, -apple-system, sans-serif' }}>
      
      {/* HEADER ROW */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
        <h1 style={{ fontSize: '18px', fontWeight: '800', color: '#0f172a', margin: 0, textTransform: 'uppercase', letterSpacing: '0.5px' }}>
          Cấu hình thông báo & Broadcast
        </h1>
      </div>

      {/* ALERTS */}
      {error && <div style={{ background: '#fef2f2', color: '#dc2626', border: '1px solid #fecaca', padding: '10px 14px', borderRadius: '8px', marginBottom: '16px', fontSize: '13px', fontWeight: '700' }}>{error}</div>}
      {message && <div style={{ background: '#f0fdf4', color: '#16a34a', border: '1px solid #bbf7d0', padding: '10px 14px', borderRadius: '8px', marginBottom: '16px', fontSize: '13px', fontWeight: '700' }}>{message}</div>}

      {/* GRID LAYOUT */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(360px, 1fr))', gap: '24px', alignItems: 'start' }}>
        
        {/* Left Form: Preferences */}
        <form
          onSubmit={savePreferences}
          style={{ background: '#ffffff', padding: '24px', borderRadius: '12px', border: '1px solid #e2e8f0', display: 'flex', flexDirection: 'column', gap: '16px', boxShadow: '0 4px 16px rgba(0,0,0,0.02)' }}
        >
          <h3 style={{ fontSize: '15px', fontWeight: '800', color: '#0f172a', borderBottom: '1px solid #f1f5f9', paddingBottom: '10px', margin: 0 }}>
            Kênh nhận thông báo của Admin
          </h3>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
            {preferenceFields.map((field) => (
              <label
                key={field}
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: '10px',
                  fontSize: '13.5px',
                  color: '#334155',
                  cursor: 'pointer',
                  padding: '4px 0',
                }}
              >
                <input
                  type="checkbox"
                  checked={Boolean(preferences[field])}
                  onChange={(event) => updatePreference(field, event.target.checked)}
                  style={{ accentColor: '#ea580c', width: '16px', height: '16px', cursor: 'pointer' }}
                />
                {preferenceLabels[field]}
              </label>
            ))}
          </div>

          <button
            type="submit"
            style={{ width: '100%', padding: '11px', background: '#ea580c', color: '#ffffff', border: 'none', borderRadius: '8px', fontSize: '13.5px', fontWeight: '800', cursor: 'pointer', marginTop: '10px' }}
          >
            Lưu cài đặt thông báo
          </button>
        </form>

        {/* Right Form: Send Broadcast Message */}
        <form
          onSubmit={sendBroadcast}
          style={{ background: '#ffffff', padding: '24px', borderRadius: '12px', border: '1px solid #e2e8f0', display: 'flex', flexDirection: 'column', gap: '14px', boxShadow: '0 4px 16px rgba(0,0,0,0.02)' }}
        >
          <h3 style={{ fontSize: '15px', fontWeight: '800', color: '#0f172a', borderBottom: '1px solid #f1f5f9', paddingBottom: '10px', margin: 0 }}>
            Gửi thông báo hàng loạt (Broadcast)
          </h3>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr', gap: '12px' }}>
            <div>
              <label style={{ display: 'block', fontSize: '12.5px', fontWeight: '700', color: '#475569', marginBottom: '6px' }}>Mã định danh yêu cầu (Request Key)</label>
              <input
                type="text"
                value={broadcastForm.requestKey}
                onChange={(event) => updateBroadcast('requestKey', event.target.value)}
                required
                maxLength="80"
                style={{ width: '100%', padding: '8px 10px', border: '1px solid #cbd5e1', borderRadius: '6px', fontSize: '13px', outline: 'none' }}
              />
            </div>
            
            <div>
              <label style={{ display: 'block', fontSize: '12.5px', fontWeight: '700', color: '#475569', marginBottom: '6px' }}>Tiêu đề thông báo *</label>
              <input
                type="text"
                placeholder="Nhập tiêu đề..."
                value={broadcastForm.title}
                onChange={(event) => updateBroadcast('title', event.target.value)}
                required
                maxLength="150"
                style={{ width: '100%', padding: '8px 10px', border: '1px solid #cbd5e1', borderRadius: '6px', fontSize: '13px', outline: 'none' }}
              />
            </div>

            <div>
              <label style={{ display: 'block', fontSize: '12.5px', fontWeight: '700', color: '#475569', marginBottom: '6px' }}>Nội dung thông báo *</label>
              <textarea
                rows="4"
                placeholder="Nhập chi tiết nội dung tin nhắn..."
                value={broadcastForm.message}
                onChange={(event) => updateBroadcast('message', event.target.value)}
                required
                maxLength="500"
                style={{ width: '100%', padding: '8px 10px', border: '1px solid #cbd5e1', borderRadius: '6px', fontSize: '13px', outline: 'none', resize: 'none' }}
              />
            </div>

            <div>
              <label style={{ display: 'block', fontSize: '12.5px', fontWeight: '700', color: '#475569', marginBottom: '6px' }}>Đường dẫn hành động (Action URL)</label>
              <input
                type="text"
                placeholder="ví dụ: /products/123..."
                value={broadcastForm.actionUrl}
                onChange={(event) => updateBroadcast('actionUrl', event.target.value)}
                maxLength="255"
                style={{ width: '100%', padding: '8px 10px', border: '1px solid #cbd5e1', borderRadius: '6px', fontSize: '13px', outline: 'none' }}
              />
            </div>

            <label
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '8px',
                fontSize: '13px',
                color: '#334155',
                cursor: 'pointer',
                padding: '4px 0',
              }}
            >
              <input
                type="checkbox"
                checked={broadcastForm.sendEmail}
                onChange={(event) => updateBroadcast('sendEmail', event.target.checked)}
                style={{ accentColor: '#ea580c', width: '15px', height: '15px', cursor: 'pointer' }}
              />
              Đồng thời gửi qua Email cho người dùng
            </label>
          </div>

          <button
            type="submit"
            style={{ width: '100%', padding: '11px', background: '#16a34a', color: '#ffffff', border: 'none', borderRadius: '8px', fontSize: '13.5px', fontWeight: '800', cursor: 'pointer', marginTop: '6px' }}
          >
            Bắt đầu gửi Broadcast
          </button>
        </form>

      </div>

      {/* FOOTER */}
      <footer style={{ textAlign: 'center', marginTop: '30px', padding: '16px 0', borderTop: '1px solid #cbd5e1', fontSize: '12px', color: '#94a3b8' }}>
        © 2026 ToyStore Admin Panel
      </footer>
    </section>
  );
}

export default AdminNotificationPage;
