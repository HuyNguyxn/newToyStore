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
      await login(form);
      navigate(from, { replace: true });
    } catch (err) {
      setError(err.message || 'Dang nhap that bai. Vui long kiem tra lai email va mat khau.');
    }
  }

  return (
    <section className="auth-page">
      <div className="auth-card">
        <div className="auth-card__heading">
          <p>Chao mung tro lai</p>
          <h1>Dang nhap</h1>
        </div>

        {error && <div className="form-alert">{error}</div>}

        <form className="auth-form" onSubmit={handleSubmit}>
          <label>
            Email
            <input
              name="email"
              type="email"
              value={form.email}
              onChange={handleChange}
              placeholder="admin@gmail.com"
              required
            />
          </label>

          <label>
            Mat khau
            <input
              name="password"
              type="password"
              value={form.password}
              onChange={handleChange}
              placeholder="Nhap mat khau"
              required
            />
          </label>

          <button type="submit" disabled={loading}>
            {loading ? 'Dang xu ly...' : 'Dang nhap'}
          </button>
        </form>

        <p className="auth-card__switch">
          Chua co tai khoan? <Link to="/register">Dang ky ngay</Link>
          <br />
          <Link to="/forgot-password">Quen mat khau?</Link>
        </p>
      </div>
    </section>
  );
}

export default LoginPage;
