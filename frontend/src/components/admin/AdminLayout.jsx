import { Link, Outlet, useNavigate } from 'react-router-dom';
import useAuth from '../../hooks/useAuth.js';
import AdminSidebar from './AdminSidebar.jsx';

// Display name for each role
const roleDisplayNames = {
  ADMIN: 'Quản trị viên',
  MANAGER: 'Quản lý',
  STAFF: 'Nhân viên',
};

// Badge color for each role
const roleBadgeStyles = {
  ADMIN:   { background: '#fef2f2', color: '#dc2626', border: '1px solid #fecaca' },
  MANAGER: { background: '#eff6ff', color: '#2563eb', border: '1px solid #bfdbfe' },
  STAFF:   { background: '#f0fdf4', color: '#16a34a', border: '1px solid #bbf7d0' },
};

function AdminLayout() {
  const navigate = useNavigate();
  const { user, logout } = useAuth();

  const userRole = user?.role || 'STAFF';

  function handleLogout() {
    const confirmed = window.confirm('Bạn chắc chắn muốn đăng xuất khỏi trang quản trị?');

    if (confirmed) {
      logout();
      navigate('/');
    }
  }

  const badgeStyle = roleBadgeStyles[userRole] || roleBadgeStyles.STAFF;

  return (
    <div className="admin-shell" style={{ display: 'flex', minHeight: '100vh', background: '#f8fafc' }}>
      
      {/* HYBRID ENTERPRISE SIDEBAR */}
      <AdminSidebar userRole={userRole} />

      {/* WORKSPACE AREA */}
      <div className="admin-workspace" style={{ flex: 1, display: 'flex', flexDirection: 'column', minWidth: 0 }}>
        
        {/* TOPBAR */}
        <header className="admin-topbar" style={{ background: '#ffffff', borderBottom: '1px solid #e2e8f0', padding: '16px 24px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <div>
            <p style={{ fontSize: '11px', fontWeight: '800', color: '#ea580c', letterSpacing: '0.8px', margin: 0, textTransform: 'uppercase' }}>HỆ THỐNG QUẢN TRỊ ENTERPRISE</p>
            <h1 style={{ fontSize: '20px', fontWeight: '900', color: '#0f172a', margin: '2px 0 0 0' }}>Bảng điều khiển Quản lý</h1>
          </div>

          <div className="admin-user" style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
            <Link to="/" className="admin-view-site" style={{ fontSize: '13px', color: '#2563eb', fontWeight: '700', textDecoration: 'none', background: '#eff6ff', padding: '6px 12px', borderRadius: '6px', border: '1px solid #bfdbfe' }}>
              ⌂ Xem cửa hàng
            </Link>
            <div style={{ textAlign: 'right' }}>
              <strong style={{ display: 'block', fontSize: '13px', color: '#0f172a' }}>{user?.email || user?.fullName || 'admin@gmail.com'}</strong>
              <span
                style={{
                  ...badgeStyle,
                  padding: '2px 8px',
                  borderRadius: '6px',
                  fontSize: '11px',
                  fontWeight: '700',
                  display: 'inline-block',
                  marginTop: '2px',
                }}
              >
                {roleDisplayNames[userRole] || userRole}
              </span>
            </div>
            <button
              type="button"
              onClick={handleLogout}
              style={{ padding: '7px 14px', background: '#f1f5f9', color: '#475569', border: '1px solid #cbd5e1', borderRadius: '6px', fontSize: '12.5px', fontWeight: '700', cursor: 'pointer' }}
            >
              Đăng xuất
            </button>
          </div>
        </header>

        {/* MAIN WORKSPACE CONTENT */}
        <main className="admin-main" style={{ flex: 1, padding: '0', background: '#f8fafc' }}>
          <Outlet context={{ userRole }} />
        </main>
      </div>

    </div>
  );
}

export default AdminLayout;
