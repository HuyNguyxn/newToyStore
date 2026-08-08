import { useState } from 'react';
import { Link, Outlet, useNavigate } from 'react-router-dom';
import useAuth from '../../hooks/useAuth.js';
import AdminSidebar from './AdminSidebar.jsx';

// Display name & rich badge styling for each role
const roleInfo = {
  ADMIN: { label: '👑 Quản trị viên', bg: 'linear-gradient(135deg, #fff7ed, #fef3c7)', color: '#b45309', border: '#fde68a' },
  MANAGER: { label: '⭐ Quản lý', bg: 'linear-gradient(135deg, #fef2f2, #fee2e2)', color: '#b91c1c', border: '#fecaca' },
  STAFF: { label: '👤 Nhân viên', bg: 'linear-gradient(135deg, #f3e8ff, #fae8ff)', color: '#7e22ce', border: '#e9d5ff' },
  CUSTOMER: { label: '🛒 Khách hàng', bg: 'linear-gradient(135deg, #eff6ff, #dbeafe)', color: '#1d4ed8', border: '#bfdbfe' },
};

function AdminLayout() {
  const navigate = useNavigate();
  const { user, logout } = useAuth();
  const [storeBtnHover, setStoreBtnHover] = useState(false);
  const [logoutBtnHover, setLogoutBtnHover] = useState(false);

  const userRole = user?.role || 'STAFF';
  const roleConfig = roleInfo[userRole] || roleInfo.STAFF;

  function handleLogout() {
    const confirmed = window.confirm('Bạn chắc chắn muốn đăng xuất khỏi trang quản trị?');
    if (confirmed) {
      logout();
      navigate('/');
    }
  }

  // Extract initial letter for avatar circle
  const userName = user?.fullName || user?.email || 'Admin';
  const initialLetter = userName.charAt(0).toUpperCase();

  return (
    <div className="admin-shell" style={{ display: 'flex', minHeight: '100vh', background: '#f8fafc' }}>
      
      {/* HYBRID ENTERPRISE SIDEBAR */}
      <AdminSidebar userRole={userRole} />

      {/* WORKSPACE AREA */}
      <div className="admin-workspace" style={{ flex: 1, display: 'flex', flexDirection: 'column', minWidth: 0 }}>
        
        {/* PREMIUM ENTERPRISE TOPBAR */}
        <header
          className="admin-topbar"
          style={{
            background: 'rgba(255, 255, 255, 0.96)',
            backdropFilter: 'blur(12px)',
            borderBottom: '1px solid #e2e8f0',
            padding: '14px 28px',
            display: 'flex',
            justify: 'space-between',
            alignItems: 'center',
            boxShadow: '0 4px 20px -2px rgba(15, 23, 42, 0.04)',
            position: 'sticky',
            top: 0,
            zIndex: 40,
          }}
        >
          {/* LEFT TITLE SECTION */}
          <div style={{ display: 'flex', alignItems: 'center', gap: '14px' }}>
            <div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '6px', marginBottom: '2px' }}>
                <span style={{ width: '7px', height: '7px', borderRadius: '50%', background: '#ea580c', display: 'inline-block' }}></span>
                <p style={{ fontSize: '11px', fontWeight: '800', color: '#ea580c', letterSpacing: '0.9px', margin: 0, textTransform: 'uppercase' }}>
                  HỆ THỐNG QUẢN TRỊ ENTERPRISE
                </p>
              </div>
              <h1 style={{ fontSize: '20px', fontWeight: '900', color: '#0f172a', margin: 0, letterSpacing: '-0.3px' }}>
                Bảng điều khiển Quản lý
              </h1>
            </div>
          </div>

          {/* RIGHT ACTION & USER PROFILE SECTION */}
          <div className="admin-user" style={{ display: 'flex', alignItems: 'center', gap: '18px' }}>
            
            {/* VIEW STORE LINK BUTTON */}
            <Link
              to="/"
              className="admin-view-site"
              onMouseEnter={() => setStoreBtnHover(true)}
              onMouseLeave={() => setStoreBtnHover(false)}
              style={{
                fontSize: '13px',
                color: storeBtnHover ? '#1d4ed8' : '#2563eb',
                fontWeight: '800',
                textDecoration: 'none',
                background: storeBtnHover ? 'linear-gradient(135deg, #dbeafe, #eff6ff)' : '#f0f9ff',
                padding: '8px 16px',
                borderRadius: '10px',
                border: '1px solid #bfdbfe',
                boxShadow: storeBtnHover ? '0 4px 12px rgba(37,99,235,0.15)' : 'none',
                transition: 'all 0.2s ease',
                display: 'flex',
                alignItems: 'center',
                gap: '8px',
              }}
            >
              <span>🏪</span> Xem cửa hàng
            </Link>

            {/* DIVIDER */}
            <div style={{ width: '1px', height: '32px', background: '#e2e8f0' }}></div>

            {/* USER CARD WITH AVATAR & BADGE */}
            <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
              {/* AVATAR CIRCLE */}
              <div style={{ position: 'relative', flexShrink: 0 }}>
                <div
                  style={{
                    width: '38px',
                    height: '38px',
                    borderRadius: '50%',
                    background: 'linear-gradient(135deg, #ea580c, #c2410c)',
                    color: '#ffffff',
                    fontWeight: '900',
                    fontSize: '16px',
                    display: 'grid',
                    placeItems: 'center',
                    lineHeight: 1,
                    textAlign: 'center',
                    boxShadow: '0 2px 8px rgba(234,88,12,0.25)',
                    userSelect: 'none',
                  }}
                >
                  <span style={{ display: 'block', lineHeight: '1', marginTop: '-1px' }}>{initialLetter}</span>
                </div>
                <span
                  style={{
                    position: 'absolute',
                    bottom: '0',
                    right: '0',
                    width: '10px',
                    height: '10px',
                    borderRadius: '50%',
                    background: '#22c55e',
                    border: '2px solid #ffffff',
                  }}
                  title="Đang hoạt động"
                ></span>
              </div>

              {/* USER NAME & ROLE BADGE */}
              <div style={{ textAlign: 'left' }}>
                <strong style={{ display: 'block', fontSize: '13.5px', fontWeight: '800', color: '#0f172a', lineHeight: 1.2 }}>
                  {userName}
                </strong>
                <span
                  style={{
                    background: roleConfig.bg,
                    color: roleConfig.color,
                    border: `1px solid ${roleConfig.border}`,
                    padding: '2px 10px',
                    borderRadius: '12px',
                    fontSize: '11px',
                    fontWeight: '800',
                    display: 'inline-block',
                    marginTop: '3px',
                    boxShadow: '0 1px 3px rgba(0,0,0,0.03)',
                  }}
                >
                  {roleConfig.label}
                </span>
              </div>
            </div>

            {/* LOGOUT BUTTON */}
            <button
              type="button"
              onClick={handleLogout}
              onMouseEnter={() => setLogoutBtnHover(true)}
              onMouseLeave={() => setLogoutBtnHover(false)}
              style={{
                padding: '8px 16px',
                background: logoutBtnHover ? '#fef2f2' : '#ffffff',
                color: logoutBtnHover ? '#dc2626' : '#475569',
                border: logoutBtnHover ? '1px solid #fecaca' : '1px solid #cbd5e1',
                borderRadius: '10px',
                fontSize: '13px',
                fontWeight: '800',
                cursor: 'pointer',
                transition: 'all 0.2s ease',
                display: 'flex',
                alignItems: 'center',
                gap: '6px',
                boxShadow: logoutBtnHover ? '0 2px 8px rgba(220,38,38,0.1)' : 'none',
              }}
            >
              <span>🚪</span> Đăng xuất
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
