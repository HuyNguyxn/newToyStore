import { Link } from 'react-router-dom';
import { useState } from 'react';
import useAuth from '../../hooks/useAuth.js';
import { resendVerificationEmail } from '../../services/authService.js';

function RegisterPage() {
  const { register, loading } = useAuth();
  const [form, setForm] = useState({
    email: '',
    password: '',
    confirmPassword: '',
    fullName: '',
    phoneNumber: '',
  });
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [registeredEmail, setRegisteredEmail] = useState('');
  const [resending, setResending] = useState(false);

  function handleChange(event) {
    const { name, value } = event.target;
    setForm((current) => ({ ...current, [name]: value }));
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setError('');
    setSuccess('');

    if (form.password !== form.confirmPassword) {
      setError('Mật khẩu xác nhận không khớp.');
      return;
    }

    try {
      const normalizedEmail = form.email.trim().toLowerCase();
      await register({
        email: normalizedEmail,
        password: form.password,
        fullName: form.fullName.trim(),
        phoneNumber: form.phoneNumber.trim() || null,
      });
      setRegisteredEmail(normalizedEmail);
      setSuccess('Đăng ký thành công. Vui lòng kiểm tra email để xác thực tài khoản trước khi đăng nhập.');
    } catch (err) {
      const message = err?.status === 503
        ? 'Hệ thống chưa gửi được email xác thực. Vui lòng thử lại sau hoặc liên hệ quản trị viên.'
        : err.message || 'Đăng ký thất bại. Vui lòng kiểm tra lại thông tin.';
      setError(message);
    }
  }

  async function handleResendVerification() {
    if (!registeredEmail) return;
    setResending(true);
    setError('');
    setSuccess('');
    try {
      await resendVerificationEmail({ email: registeredEmail });
      setSuccess('Đã gửi lại email xác thực. Vui lòng kiểm tra cả hộp thư rác nếu chưa thấy email.');
    } catch (err) {
      if (err?.status === 409) {
        setRegisteredEmail('');
        setSuccess('Tài khoản đã được xác thực. Bạn có thể đăng nhập ngay bây giờ.');
        return;
      }

      const message = err?.status === 503
        ? 'Hệ thống chưa gửi lại được email xác thực. Vui lòng thử lại sau.'
        : err.message || 'Không thể gửi lại email xác thực.';
      setError(message);
    } finally {
      setResending(false);
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

      <div className="auth-panel auth-panel--register">
        <img className="auth-panel__logo" src="/toystore-assets/logo.png" alt="ToyStore" />

        <div className="auth-tabs" aria-label="Auth navigation">
          <Link className="auth-tabs__item" to="/login">Đăng Nhập</Link>
          <Link className="auth-tabs__item auth-tabs__item--active" to="/register">Đăng Ký</Link>
        </div>

        {error && <div className="form-alert">{error}</div>}
        {success && <div className="form-alert form-alert--success">{success}</div>}

        <form className="auth-form auth-form--compact" onSubmit={handleSubmit}>
          <label>
            <span>👤 Họ và tên</span>
            <input name="fullName" value={form.fullName} onChange={handleChange} placeholder="Nhập họ tên của bạn" autoComplete="name" maxLength="120" required />
          </label>

          <label>
            <span>✉ Địa chỉ Email</span>
            <input name="email" type="email" value={form.email} onChange={handleChange} placeholder="Nhập email của bạn" autoComplete="email" maxLength="254" required />
          </label>

          <label>
            <span>☎ Số điện thoại</span>
            <input name="phoneNumber" value={form.phoneNumber} onChange={handleChange} placeholder="Nhập số điện thoại" autoComplete="tel" maxLength="20" />
          </label>

          <label>
            <span>🔒 Mật khẩu</span>
            <input name="password" type="password" minLength="6" maxLength="72" value={form.password} onChange={handleChange} placeholder="Nhập mật khẩu" autoComplete="new-password" required />
          </label>

          <label>
            <span>✔ Xác nhận mật khẩu</span>
            <input name="confirmPassword" type="password" minLength="6" maxLength="72" value={form.confirmPassword} onChange={handleChange} placeholder="Nhập lại mật khẩu" autoComplete="new-password" required />
          </label>

          <button type="submit" disabled={loading}>
            {loading ? 'ĐANG XỬ LÝ...' : 'ĐĂNG KÝ NGAY♣'}
          </button>
        </form>

        {registeredEmail && (
          <div className="auth-resend">
            <span>Chưa nhận được email xác thực?</span>
            <button type="button" onClick={handleResendVerification} disabled={resending || loading}>
              {resending ? 'Đang gửi lại...' : 'Gửi lại email'}
            </button>
          </div>
        )}
      </div>
    </section>
  );
}

export default RegisterPage;
