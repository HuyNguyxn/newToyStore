import { Link, useSearchParams } from 'react-router-dom';
import { useEffect, useRef, useState } from 'react';
import { verifyEmail } from '../../services/authService.js';

function VerifyEmailPage() {
  const [searchParams] = useSearchParams();
  const [status, setStatus] = useState('loading');
  const [message, setMessage] = useState('Đang xác thực email...');
  const verifiedTokenRef = useRef('');

  useEffect(() => {
    const token = searchParams.get('token')?.trim();
    if (!token) {
      setStatus('error');
      setMessage('Link xác thực không hợp lệ hoặc thiếu token.');
      return;
    }

    if (verifiedTokenRef.current === token) {
      return;
    }

    verifiedTokenRef.current = token;
    setStatus('loading');
    setMessage('Đang xác thực email...');

    verifyEmail(token)
      .then(() => {
        setStatus('success');
        setMessage('Xác thực email thành công. Bạn có thể đăng nhập ngay bây giờ.');
      })
      .catch((error) => {
        setStatus('error');
        setMessage(error?.message || 'Không thể xác thực email. Token có thể đã hết hạn.');
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
