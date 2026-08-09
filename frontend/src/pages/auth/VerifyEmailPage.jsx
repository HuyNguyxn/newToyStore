import { Link, useSearchParams } from 'react-router-dom';
import { useEffect, useState } from 'react';
import { verifyEmail } from '../../services/authService.js';

function VerifyEmailPage() {
  const [searchParams] = useSearchParams();
  const [status, setStatus] = useState('loading');
  const [message, setMessage] = useState('Dang xac thuc email...');

  useEffect(() => {
    const token = searchParams.get('token');
    if (!token) {
      setStatus('error');
      setMessage('Link xac thuc khong hop le hoac thieu token.');
      return;
    }

    verifyEmail(token)
      .then(() => {
        setStatus('success');
        setMessage('Xac thuc email thanh cong. Ban co the dang nhap ngay bay gio.');
      })
      .catch((error) => {
        setStatus('error');
        setMessage(error?.message || 'Khong the xac thuc email. Token co the da het han.');
      });
  }, [searchParams]);

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

        <div className="auth-tabs auth-tabs--muted" aria-hidden="true">
          <span className="auth-tabs__item">Đăng Nhập</span>
          <span className="auth-tabs__item">Đăng Ký</span>
        </div>

        <div className={status === 'success' ? 'form-alert form-alert--success' : 'form-alert'}>
          {message}
        </div>

        {status !== 'loading' && (
          <Link className="auth-forgot-link" to="/login">← Quay lại Đăng Nhập</Link>
        )}
      </div>
    </section>
  );
}

export default VerifyEmailPage;
