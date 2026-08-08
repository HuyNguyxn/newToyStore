import { useEffect, useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import BackLink from '../../components/common/BackLink.jsx';
import useAuth from '../../hooks/useAuth.js';
import {
  addCurrentAddress,
  changeCurrentPassword,
  removeCurrentAddress,
  setCurrentDefaultAddress,
} from '../../services/authService.js';
import { uploadImage } from '../../services/uploadService.js';
import { isValidVietnamesePhoneNumber } from '../../utils/userValidation.js';

const emptyAddressForm = {
  receiverName: '',
  receiverPhone: '',
  detailAddress: '',
  isDefault: false,
};

const emptyPasswordForm = {
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
};

function ProfilePage() {
  const location = useLocation();
  const { user, logout, updateProfile, setUserProfile } = useAuth();
  const [profileForm, setProfileForm] = useState({ fullName: '', phoneNumber: '', avatarUrl: '' });
  const [addressForm, setAddressForm] = useState(emptyAddressForm);
  const [passwordForm, setPasswordForm] = useState(emptyPasswordForm);
  const [addresses, setAddresses] = useState([]);
  const [savingProfile, setSavingProfile] = useState(false);
  const [savingAddress, setSavingAddress] = useState(false);
  const [savingPassword, setSavingPassword] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [profileMessage, setProfileMessage] = useState('');
  const [addressMessage, setAddressMessage] = useState('');
  const [passwordMessage, setPasswordMessage] = useState('');
  const [profileError, setProfileError] = useState('');
  const [addressError, setAddressError] = useState('');
  const [passwordError, setPasswordError] = useState('');

  const requireInfoNotice = location.state?.requireInfoNotice || '';

  useEffect(() => {
    setProfileForm({
      fullName: user?.fullName || '',
      phoneNumber: user?.phoneNumber || '',
      avatarUrl: user?.avatarUrl || '',
    });
    setAddresses(user?.addresses || []);
  }, [user]);

  function updateProfileField(field, value) {
    setProfileForm((current) => ({ ...current, [field]: value }));
  }

  function updateAddressField(field, value) {
    setAddressForm((current) => ({ ...current, [field]: value }));
  }

  function updatePasswordField(field, value) {
    setPasswordForm((current) => ({ ...current, [field]: value }));
  }

  async function handleAvatarUpload(event) {
    const file = event.target.files?.[0];
    if (!file) {
      return;
    }

    setUploading(true);
    setProfileError('');
    setProfileMessage('');

    try {
      const result = await uploadImage(file, 'avatars');
      updateProfileField('avatarUrl', result.secureUrl || result.url);
      setProfileMessage('Đã upload avatar. Bấm cập nhật để lưu vào hồ sơ.');
    } catch (err) {
      setProfileError(err.message || 'Upload avatar thất bại.');
    } finally {
      setUploading(false);
    }
  }

  async function handleProfileSubmit(event) {
    event.preventDefault();
    setSavingProfile(true);
    setProfileError('');
    setProfileMessage('');

    if (profileForm.phoneNumber && !isValidVietnamesePhoneNumber(profileForm.phoneNumber)) {
      setProfileError('Số điện thoại không hợp lệ. Vui lòng nhập 10 chữ số chuẩn Việt Nam (ví dụ: 0987654321).');
      setSavingProfile(false);
      return;
    }

    try {
      await updateProfile({
        fullName: profileForm.fullName.trim(),
        phoneNumber: profileForm.phoneNumber.trim() || null,
        avatarUrl: profileForm.avatarUrl.trim() || null,
      });
      setProfileMessage('Đã cập nhật thông tin cá nhân.');
    } catch (err) {
      setProfileError(err.message || 'Không thể cập nhật hồ sơ.');
    } finally {
      setSavingProfile(false);
    }
  }

  async function handleAddressSubmit(event) {
    event.preventDefault();
    setSavingAddress(true);
    setAddressError('');
    setAddressMessage('');

    if (!isValidVietnamesePhoneNumber(addressForm.receiverPhone)) {
      setAddressError('Số điện thoại nhận hàng không hợp lệ. Vui lòng nhập 10 chữ số chuẩn Việt Nam (ví dụ: 0987654321).');
      setSavingAddress(false);
      return;
    }

    try {
      const updatedProfile = await addCurrentAddress({
        receiverName: addressForm.receiverName.trim(),
        receiverPhone: addressForm.receiverPhone.trim(),
        detailAddress: addressForm.detailAddress.trim(),
        isDefault: addressForm.isDefault,
      });
      setAddresses(updatedProfile?.addresses || []);
      if (updatedProfile && setUserProfile) {
        setUserProfile(updatedProfile);
      }
      setAddressForm(emptyAddressForm);
      setAddressMessage('Đã thêm địa chỉ giao hàng.');
    } catch (err) {
      setAddressError(err.message || 'Không thể thêm địa chỉ.');
    } finally {
      setSavingAddress(false);
    }
  }

  async function handleSetDefaultAddress(addressId) {
    setAddressError('');
    setAddressMessage('');

    try {
      const updatedProfile = await setCurrentDefaultAddress(addressId);
      setAddresses(updatedProfile?.addresses || []);
      if (updatedProfile && setUserProfile) {
        setUserProfile(updatedProfile);
      }
      setAddressMessage('Đã đặt địa chỉ mặc định.');
    } catch (err) {
      setAddressError(err.message || 'Không thể đặt địa chỉ mặc định.');
    }
  }

  async function handleRemoveAddress(addressId) {
    setAddressError('');
    setAddressMessage('');

    try {
      const updatedProfile = await removeCurrentAddress(addressId);
      setAddresses(updatedProfile?.addresses || []);
      if (updatedProfile && setUserProfile) {
        setUserProfile(updatedProfile);
      }
      setAddressMessage('Đã xóa địa chỉ.');
    } catch (err) {
      setAddressError(err.message || 'Không thể xóa địa chỉ.');
    }
  }

  async function handlePasswordSubmit(event) {
    event.preventDefault();
    setSavingPassword(true);
    setPasswordError('');
    setPasswordMessage('');

    if (passwordForm.newPassword !== passwordForm.confirmPassword) {
      setPasswordError('Mật khẩu xác nhận không khớp.');
      setSavingPassword(false);
      return;
    }

    try {
      await changeCurrentPassword({
        oldPassword: passwordForm.oldPassword,
        newPassword: passwordForm.newPassword,
      });
      setPasswordForm(emptyPasswordForm);
      setPasswordMessage('Đã thay đổi mật khẩu thành công.');
    } catch (err) {
      setPasswordError(err.message || 'Không thể thay đổi mật khẩu.');
    } finally {
      setSavingPassword(false);
    }
  }

  return (
    <section className="profile-page container">
      <BackLink fallback="/" label="Quay lại trang chủ" />

      {requireInfoNotice && (
        <div style={{
          background: '#fef2f2',
          color: '#b91c1c',
          border: '2px solid #ef4444',
          borderRadius: '12px',
          padding: '16px 20px',
          fontSize: '15px',
          fontWeight: '800',
          marginBottom: '24px',
          boxShadow: '0 4px 14px rgba(239,68,68,0.15)',
          display: 'flex',
          alignItems: 'center',
          gap: '12px'
        }}>
          <span style={{ fontSize: '22px' }}>🚨</span>
          <span>{requireInfoNotice}</span>
        </div>
      )}

      <div className="profile-card profile-card--hero">
        <img src={profileForm.avatarUrl || user?.avatarUrl || '/toystore-assets/logo.png'} alt={user?.fullName || 'User avatar'} />
        <div>
          <p>Tài khoản của tôi</p>
          <h1>{user?.fullName}</h1>
          <span>{user?.email}</span>
          <strong>{user?.role}</strong>
        </div>
        <button type="button" onClick={logout}>Đăng xuất</button>
      </div>

      <div className="profile-dashboard">
        <form className="profile-panel" onSubmit={handleProfileSubmit}>
          <h2>💳 Thông tin cá nhân</h2>

          {profileError && <div className="form-alert">{profileError}</div>}
          {profileMessage && <div className="form-alert form-alert--success">{profileMessage}</div>}

          <label>
            Email <span>(Không thể thay đổi)</span>
            <input value={user?.email || ''} disabled />
          </label>

          <label>
            Họ và tên
            <input value={profileForm.fullName} onChange={(event) => updateProfileField('fullName', event.target.value)} required maxLength="120" />
          </label>

          <label>
            Số điện thoại
            <input value={profileForm.phoneNumber} onChange={(event) => updateProfileField('phoneNumber', event.target.value)} maxLength="20" />
          </label>

          <label>
            Avatar URL
            <input value={profileForm.avatarUrl} onChange={(event) => updateProfileField('avatarUrl', event.target.value)} placeholder="Cloudinary URL" maxLength="1000" />
          </label>

          <label>
            Upload avatar
            <input type="file" accept="image/*" onChange={handleAvatarUpload} disabled={uploading} />
          </label>

          <button type="submit" disabled={savingProfile || uploading}>
            {savingProfile ? 'Đang lưu...' : uploading ? 'Đang upload...' : 'Cập nhật thông tin'}
          </button>
        </form>

        <form className="profile-panel" onSubmit={handlePasswordSubmit}>
          <h2>🔑 Thay đổi mật khẩu</h2>

          {passwordError && <div className="form-alert">{passwordError}</div>}
          {passwordMessage && <div className="form-alert form-alert--success">{passwordMessage}</div>}

          <label>
            Nhập mật khẩu cũ
            <input type="password" value={passwordForm.oldPassword} onChange={(event) => updatePasswordField('oldPassword', event.target.value)} required />
          </label>

          <label>
            Mật khẩu mới
            <input type="password" value={passwordForm.newPassword} onChange={(event) => updatePasswordField('newPassword', event.target.value)} minLength="6" required />
          </label>

          <label>
            Xác nhận mật khẩu mới
            <input type="password" value={passwordForm.confirmPassword} onChange={(event) => updatePasswordField('confirmPassword', event.target.value)} minLength="6" required />
          </label>

          <button type="submit" disabled={savingPassword}>
            {savingPassword ? 'Đang xử lý...' : 'Thay đổi mật khẩu'}
          </button>
        </form>
      </div>

      <section className="profile-panel profile-panel--wide">
        <div className="profile-panel__heading">
          <h2>📍 Địa chỉ giao hàng</h2>
          <span>{addresses.length} địa chỉ</span>
        </div>

        {addressError && <div className="form-alert">{addressError}</div>}
        {addressMessage && <div className="form-alert form-alert--success">{addressMessage}</div>}

        <form className="profile-address-form" onSubmit={handleAddressSubmit}>
          <label>
            Tên người nhận
            <input value={addressForm.receiverName} onChange={(event) => updateAddressField('receiverName', event.target.value)} required />
          </label>
          <label>
            Số điện thoại nhận hàng
            <input value={addressForm.receiverPhone} onChange={(event) => updateAddressField('receiverPhone', event.target.value)} required />
          </label>
          <label className="profile-address-form__wide">
            Địa chỉ chi tiết
            <input value={addressForm.detailAddress} onChange={(event) => updateAddressField('detailAddress', event.target.value)} required />
          </label>
          <label className="profile-checkbox">
            <input type="checkbox" checked={addressForm.isDefault} onChange={(event) => updateAddressField('isDefault', event.target.checked)} />
            Đặt làm địa chỉ mặc định
          </label>
          <button type="submit" disabled={savingAddress}>
            {savingAddress ? 'Đang thêm...' : 'Thêm địa chỉ'}
          </button>
        </form>

        <div className="profile-address-list">
          {addresses.length === 0 && <div className="empty-state">Bạn chưa có địa chỉ giao hàng.</div>}
          {addresses.map((address) => (
            <article className="profile-address-card" key={address.id}>
              <div>
                <strong>{address.receiverName}</strong>
                <span>{address.receiverPhone}</span>
                <p>{address.detailAddress}</p>
                {address.default && <em>Mặc định</em>}
              </div>
              <div>
                {!address.default && (
                  <button type="button" onClick={() => handleSetDefaultAddress(address.id)}>Đặt mặc định</button>
                )}
                <button type="button" className="danger-button" onClick={() => handleRemoveAddress(address.id)}>Xóa</button>
              </div>
            </article>
          ))}
        </div>
      </section>
    </section>
  );
}

export default ProfilePage;
