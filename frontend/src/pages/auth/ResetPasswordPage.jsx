import { useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
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
      setMessage('Da dat lai mat khau. Ban co the dang nhap bang mat khau moi.');
      setNewPassword('');
    } catch (err) {
      setError(err.message || 'Dat lai mat khau that bai.');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <section className="auth-page">
      <form className="auth-card" onSubmit={handleSubmit}>
        <p>Reset password</p>
        <h1>Dat lai mat khau</h1>

        {error && <div className="form-alert">{error}</div>}
        {message && <div className="form-alert form-alert--success">{message}</div>}

        <label>
          Reset token
          <input
            value={token}
            onChange={(event) => setToken(event.target.value)}
            required
          />
        </label>

        <label>
          Mat khau moi
          <input
            type="password"
            value={newPassword}
            onChange={(event) => setNewPassword(event.target.value)}
            minLength="6"
            required
          />
        </label>

        <button type="submit" disabled={submitting}>
          {submitting ? 'Dang xu ly...' : 'Dat lai mat khau'}
        </button>

        <Link to="/login">Quay lai dang nhap</Link>
      </form>
    </section>
  );
}

export default ResetPasswordPage;
