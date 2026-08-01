import { useState } from 'react';
import { Link } from 'react-router-dom';
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
      setMessage('Da tao yeu cau dat lai mat khau. Neu cau hinh email chua gui that, token se hien thi ben duoi de test.');
    } catch (err) {
      setError(err.message || 'Khong the tao yeu cau dat lai mat khau.');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <section className="auth-page">
      <form className="auth-card" onSubmit={handleSubmit}>
        <p>Quen mat khau</p>
        <h1>Lay token dat lai mat khau</h1>

        {error && <div className="form-alert">{error}</div>}
        {message && <div className="form-alert form-alert--success">{message}</div>}
        {resetToken && <code className="token-box">{resetToken}</code>}

        <label>
          Email
          <input
            type="email"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            placeholder="admin@gmail.com"
            required
          />
        </label>

        <button type="submit" disabled={submitting}>
          {submitting ? 'Dang gui...' : 'Tao reset token'}
        </button>

        <Link to="/reset-password">Da co token? Dat lai mat khau</Link>
      </form>
    </section>
  );
}

export default ForgotPasswordPage;
