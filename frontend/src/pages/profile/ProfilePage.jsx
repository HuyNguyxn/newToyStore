import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import useAuth from '../../hooks/useAuth.js';
import { uploadImage } from '../../services/uploadService.js';

function ProfilePage() {
  const { user, logout, updateProfile } = useAuth();
  const [form, setForm] = useState({ fullName: '', phoneNumber: '', avatarUrl: '' });
  const [saving, setSaving] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    setForm({
      fullName: user?.fullName || '',
      phoneNumber: user?.phoneNumber || '',
      avatarUrl: user?.avatarUrl || '',
    });
  }, [user]);

  function updateField(field, value) {
    setForm((current) => ({ ...current, [field]: value }));
  }

  async function handleAvatarUpload(event) {
    const file = event.target.files?.[0];
    if (!file) {
      return;
    }

    setUploading(true);
    setError('');
    setMessage('');

    try {
      const result = await uploadImage(file, 'avatars');
      updateField('avatarUrl', result.secureUrl || result.url);
      setMessage('Da upload avatar. Bam Luu thay doi de cap nhat ho so.');
    } catch (err) {
      setError(err.message || 'Upload avatar that bai.');
    } finally {
      setUploading(false);
    }
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setSaving(true);
    setError('');
    setMessage('');

    try {
      await updateProfile({
        fullName: form.fullName.trim(),
        phoneNumber: form.phoneNumber.trim() || null,
        avatarUrl: form.avatarUrl.trim() || null,
      });
      setMessage('Da cap nhat ho so.');
    } catch (err) {
      setError(err.message || 'Khong the cap nhat ho so.');
    } finally {
      setSaving(false);
    }
  }

  return (
    <section className="profile-page container">
      <div className="profile-card">
        <img src={form.avatarUrl || user?.avatarUrl || 'https://placehold.co/96x96?text=NTS'} alt={user?.fullName || 'User avatar'} />
        <div>
          <p>Tai khoan cua toi</p>
          <h1>{user?.fullName}</h1>
          <span>{user?.email}</span>
          <strong>{user?.role}</strong>
        </div>
        <button type="button" onClick={logout}>Dang xuat</button>
      </div>

      <form className="profile-form" onSubmit={handleSubmit}>
        <h2>Cap nhat ho so</h2>

        {error && <div className="form-alert">{error}</div>}
        {message && <div className="form-alert form-alert--success">{message}</div>}

        <label>
          Ho va ten
          <input
            value={form.fullName}
            onChange={(event) => updateField('fullName', event.target.value)}
            required
            maxLength="120"
          />
        </label>

        <label>
          So dien thoai
          <input
            value={form.phoneNumber}
            onChange={(event) => updateField('phoneNumber', event.target.value)}
            maxLength="20"
          />
        </label>

        <label>
          Avatar URL
          <input
            value={form.avatarUrl}
            onChange={(event) => updateField('avatarUrl', event.target.value)}
            placeholder="Cloudinary URL"
            maxLength="1000"
          />
        </label>

        <label>
          Upload avatar
          <input type="file" accept="image/*" onChange={handleAvatarUpload} disabled={uploading} />
        </label>

        <button type="submit" disabled={saving || uploading}>
          {saving ? 'Dang luu...' : uploading ? 'Dang upload...' : 'Luu thay doi'}
        </button>
      </form>

      <div className="profile-form">
        <h2>Thao tac mua hang</h2>
        <div className="admin-resource-table__actions">
          <Link className="login-link" to="/reviews/new">Viet danh gia</Link>
          <Link className="login-link" to="/reviews/me">Danh gia cua toi</Link>
          <Link className="login-link" to="/returns/new">Tao yeu cau tra hang</Link>
          <Link className="login-link" to="/returns">Yeu cau tra hang cua toi</Link>
          <Link className="login-link" to="/shipments">Theo doi van chuyen</Link>
          <Link className="login-link" to="/orders">Xem don hang</Link>
        </div>
      </div>
    </section>
  );
}

export default ProfilePage;
