import { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import useAuth from '../../hooks/useAuth.js';

function LoginPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const { login, loading } = useAuth();
  const [form, setForm] = useState({ email: '', password: '' });
  const [error, setError] = useState('');

  const from = location.state?.from?.pathname || '/';

  function handleChange(event) {
    const { name, value } = event.target;
    setForm((current) => ({ ...current, [name]: value }));
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setError('');

    try {
      const loggedInUser = await login({ ...form, email: form.email.trim().toLowerCase() });
      const targetPath = loggedInUser?.role === 'ADMIN' ? '/admin/statistics' : from;
      navigate(targetPath, { replace: true });
    } catch (err) {
      setError(err.message || 'Đăng nhập thất bại. Vui lòng kiểm tra lại email và mật khẩu.');
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

      <div className="auth-panel">
        <img className="auth-panel__logo" src="/toystore-assets/logo.png" alt="ToyStore" />

        <div className="auth-tabs" aria-label="Auth navigation">
          <Link className="auth-tabs__item auth-tabs__item--active" to="/login">Đăng Nhập</Link>
          <Link className="auth-tabs__item" to="/register">Đăng Ký</Link>
        </div>

        {error && <div className="form-alert">{error}</div>}

        <form className="auth-form auth-form--compact" onSubmit={handleSubmit}>
          <label>
            <span>✉ Địa chỉ Email</span>
            <input
              name="email"
              type="email"
              value={form.email}
              onChange={handleChange}
              placeholder="admin@gmail.com"
              autoComplete="email"
              maxLength="254"
              required
            />
          </label>

          <label>
            <span>🔒 Mật khẩu</span>
            <input
              name="password"
              type="password"
              value={form.password}
              onChange={handleChange}
              placeholder="Nhập mật khẩu"
              autoComplete="current-password"
              maxLength="72"
              required
            />
          </label>

          <button type="submit" disabled={loading}>
            {loading ? 'ĐANG XỬ LÝ...' : 'ĐĂNG NHẬP↪'}
          </button>
        </form>

        <Link className="auth-forgot-link" to="/forgot-password">🔑 Quên mật khẩu?</Link>
      </div>
    </section>
  );
}

export default LoginPage;
