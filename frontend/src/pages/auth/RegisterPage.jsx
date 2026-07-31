import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import useAuth from '../../hooks/useAuth.js';

function RegisterPage() {
  const navigate = useNavigate();
  const { register, loading } = useAuth();
  const [form, setForm] = useState({
    email: '',
    password: '',
    fullName: '',
    phoneNumber: '',
  });
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  function handleChange(event) {
    const { name, value } = event.target;
    setForm((current) => ({ ...current, [name]: value }));
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setError('');
    setSuccess('');

    try {
      await register(form);
      setSuccess('Dang ky thanh cong. Neu he thong bat xac thuc email, hay kiem tra email truoc khi dang nhap.');
      setTimeout(() => navigate('/login'), 900);
    } catch (err) {
      setError(err.message || 'Dang ky that bai. Vui long kiem tra lai thong tin.');
    }
  }

  return (
    <section className="auth-page">
      <div className="auth-card">
        <div className="auth-card__heading">
          <p>Tao tai khoan mua sam</p>
          <h1>Dang ky</h1>
        </div>

        {error && <div className="form-alert">{error}</div>}
        {success && <div className="form-alert form-alert--success">{success}</div>}

        <form className="auth-form" onSubmit={handleSubmit}>
          <label>
            Ho va ten
            <input
              name="fullName"
              value={form.fullName}
              onChange={handleChange}
              placeholder="Nguyen Van A"
              required
            />
          </label>

          <label>
            Email
            <input
              name="email"
              type="email"
              value={form.email}
              onChange={handleChange}
              placeholder="customer@gmail.com"
              required
            />
          </label>

          <label>
            So dien thoai
            <input
              name="phoneNumber"
              value={form.phoneNumber}
              onChange={handleChange}
              placeholder="0900000000"
            />
          </label>

          <label>
            Mat khau
            <input
              name="password"
              type="password"
              minLength="6"
              value={form.password}
              onChange={handleChange}
              placeholder="It nhat 6 ky tu"
              required
            />
          </label>

          <button type="submit" disabled={loading}>
            {loading ? 'Dang xu ly...' : 'Tao tai khoan'}
          </button>
        </form>

        <p className="auth-card__switch">
          Da co tai khoan? <Link to="/login">Dang nhap</Link>
        </p>
      </div>
    </section>
  );
}

export default RegisterPage;
