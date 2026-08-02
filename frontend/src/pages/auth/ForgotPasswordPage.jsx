import { Link } from 'react-router-dom';
import { useState } from 'react';
import { requestPasswordReset } from '../../services/authService.js';

function ForgotPasswordPage() {
  const [email, setEmail] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [message, setMessage] = useState('');
  const [resetToken, setResetToken] = useState('');
  const [error, setError] = useState('');

  async function handleSubmit(event) {
    event.preventDefault();
    setSubmitting(true);
    setMessage('');
    setResetToken('');
    setError('');

    try {
      const result = await requestPasswordReset({ email: email.trim() });
      setResetToken(result?.token || result?.resetToken || '');
      setMessage('Đã tạo yêu cầu đặt lại mật khẩu. Nếu email chưa gửi thật, token test sẽ hiển thị bên dưới.');
    } catch (err) {
      setError(err.message || 'Không thể tạo yêu cầu đặt lại mật khẩu.');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <section className="auth-screen">
      <div className="auth-topbar">
        <Link to="/" className="auth-topbar__brand">
          <img src="/toystore-assets/logo.png" alt="ToyStore" />
          <strong>ToyStore</strong>
        </Link>
        <Link to="/" className="auth-topbar__home">⌂ Trang Chủ</Link>
      </div>

      <form className="auth-panel" onSubmit={handleSubmit}>
        <img className="auth-panel__logo" src="/toystore-assets/logo.png" alt="ToyStore" />

        <div className="auth-tabs auth-tabs--muted" aria-hidden="true">
          <span className="auth-tabs__item">Đăng Nhập</span>
          <span className="auth-tabs__item">Đăng Ký</span>
        </div>

        <div className="auth-panel__icon">👥</div>
        <p className="auth-panel__hint">Nhập email để nhận mã khôi phục</p>

        {error && <div className="form-alert">{error}</div>}
        {message && <div className="form-alert form-alert--success">{message}</div>}
        {resetToken && <code className="token-box">{resetToken}</code>}

        <div className="auth-form auth-form--compact">
          <label>
            <span>✉ Địa chỉ Email</span>
            <input
              type="email"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              placeholder="Nhập email của bạn"
              required
            />
          </label>

          <button type="submit" disabled={submitting}>
            {submitting ? 'ĐANG GỬI...' : 'GỬI YÊU CẦU✈'}
          </button>
        </div>

        <Link className="auth-forgot-link" to="/login">← Đăng Nhập</Link>
      </form>
    </section>
  );
}

export default ForgotPasswordPage;
