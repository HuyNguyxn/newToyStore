import { Link, useSearchParams } from 'react-router-dom';
import { useState } from 'react';
import { resetPassword } from '../../services/authService.js';

function ResetPasswordPage() {
  const [searchParams] = useSearchParams();
  const [token, setToken] = useState(searchParams.get('token') || '');
  const [newPassword, setNewPassword] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  async function handleSubmit(event) {
    event.preventDefault();
    setSubmitting(true);
    setMessage('');
    setError('');

    try {
      await resetPassword({ token: token.trim(), newPassword });
      setMessage('Đã đặt lại mật khẩu. Bạn có thể đăng nhập bằng mật khẩu mới.');
      setNewPassword('');
    } catch (err) {
      setError(err.message || 'Đặt lại mật khẩu thất bại.');
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
        <h1 className="auth-panel__title">Đặt lại mật khẩu</h1>
        <div className="auth-panel__title-line" />
        <div className="auth-panel__icon">✉</div>

        {error && <div className="form-alert">{error}</div>}
        {message && <div className="form-alert form-alert--success">{message}</div>}

        <div className="auth-form auth-form--compact">
          <label>
            <span>Mã khôi phục</span>
            <input value={token} onChange={(event) => setToken(event.target.value)} placeholder="Nhập token khôi phục" required />
          </label>

          <label>
            <span>Mật khẩu mới</span>
            <input type="password" value={newPassword} onChange={(event) => setNewPassword(event.target.value)} minLength="6" placeholder="Nhập mật khẩu mới" required />
          </label>

          <button type="submit" disabled={submitting}>
            {submitting ? 'ĐANG XỬ LÝ...' : 'XÁC NHẬN✔'}
          </button>
        </div>

        <Link className="auth-forgot-link" to="/login">← Quay lại Đăng nhập</Link>
      </form>
    </section>
  );
}

export default ResetPasswordPage;
