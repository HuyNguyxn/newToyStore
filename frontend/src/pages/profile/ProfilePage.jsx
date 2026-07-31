import useAuth from '../../hooks/useAuth.js';

function ProfilePage() {
  const { user, logout } = useAuth();

  return (
    <section className="profile-page container">
      <div className="profile-card">
        <img src={user?.avatarUrl || 'https://placehold.co/96x96?text=NTS'} alt={user?.fullName || 'User avatar'} />
        <div>
          <p>Tai khoan cua toi</p>
          <h1>{user?.fullName}</h1>
          <span>{user?.email}</span>
          <strong>{user?.role}</strong>
        </div>
        <button type="button" onClick={logout}>Dang xuat</button>
      </div>
    </section>
  );
}

export default ProfilePage;
