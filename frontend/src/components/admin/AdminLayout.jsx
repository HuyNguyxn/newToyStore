import { Link, NavLink, Outlet, useNavigate } from 'react-router-dom';
import useAuth from '../../hooks/useAuth.js';

// Role-based sidebar configuration
// requiredRoles: which roles can see this menu item
const adminNavItems = [
  { to: '/admin/statistics',       label: 'Thống kê',      icon: '↗',  requiredRoles: ['MANAGER', 'ADMIN'] },
  { to: '/admin/categories',       label: 'Danh mục',      icon: '▣',  requiredRoles: ['STAFF', 'MANAGER', 'ADMIN'] },
  { to: '/admin/products',         label: 'Sản phẩm',      icon: '▤',  requiredRoles: ['STAFF', 'MANAGER', 'ADMIN'] },
  { to: '/admin/suppliers',        label: 'Nhà cung cấp',  icon: '▥',  requiredRoles: ['STAFF', 'MANAGER', 'ADMIN'] },
  { to: '/admin/imports',          label: 'Nhập hàng',     icon: '↧',  requiredRoles: ['STAFF', 'MANAGER', 'ADMIN'] },
  { to: '/admin/users',            label: 'Người dùng',    icon: '●',  requiredRoles: ['ADMIN'] },
  { to: '/admin/orders',           label: 'Đơn hàng',      icon: '≡',  requiredRoles: ['STAFF', 'MANAGER', 'ADMIN'] },
  { to: '/admin/promotions',       label: 'Khuyến mãi',    icon: '◆',  requiredRoles: ['MANAGER', 'ADMIN'] },
  { to: '/admin/payments',         label: 'Thanh toán',     icon: '▧',  requiredRoles: ['STAFF', 'MANAGER', 'ADMIN'] },
  { to: '/admin/refunds',          label: 'Hoàn tiền',     icon: '↩',  requiredRoles: ['STAFF', 'MANAGER', 'ADMIN'] },
  { to: '/admin/inventory',        label: 'Tồn kho',       icon: '▨',  requiredRoles: ['STAFF', 'MANAGER', 'ADMIN'] },
  { to: '/admin/logistics',        label: 'Vận chuyển',    icon: '⇄',  requiredRoles: ['STAFF', 'MANAGER', 'ADMIN'] },
  { to: '/admin/returns',          label: 'Trả hàng',      icon: '▢',  requiredRoles: ['STAFF', 'MANAGER', 'ADMIN'] },
  { to: '/admin/supplier-returns', label: 'Trả NCC',       icon: '⇤',  requiredRoles: ['STAFF', 'MANAGER', 'ADMIN'] },
  { to: '/admin/reviews',          label: 'Đánh giá',      icon: '★',  requiredRoles: ['STAFF', 'MANAGER', 'ADMIN'] },
  { to: '/admin/moderation',       label: 'Kiểm duyệt',   icon: '◈',  requiredRoles: ['STAFF', 'MANAGER', 'ADMIN'] },
  { to: '/admin/notifications',    label: 'Thông báo',     icon: '●',  requiredRoles: ['MANAGER', 'ADMIN'] },
  { to: '/admin/uploads',          label: 'Tải ảnh',       icon: '▨',  requiredRoles: ['STAFF', 'MANAGER', 'ADMIN'] },
];

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

  // Filter nav items based on user role
  const visibleNavItems = adminNavItems.filter(
    (item) => item.requiredRoles.includes(userRole)
  );

  function handleLogout() {
    const confirmed = window.confirm('Bạn chắc chắn muốn đăng xuất khỏi trang quản trị?');

    if (confirmed) {
      logout();
      navigate('/');
    }
  }

  const badgeStyle = roleBadgeStyles[userRole] || roleBadgeStyles.STAFF;

  return (
    <div className="admin-shell">
      <aside className="admin-sidebar">
        <div className="admin-brand">
          <span>
            <img src="/toystore-assets/logo.png" alt="ToyStore" />
          </span>
          <div>
            <strong>ToyStore</strong>
            <small>Trang quản trị</small>
          </div>
        </div>

        <nav className="admin-nav" aria-label="Điều hướng quản trị">
          {visibleNavItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              className={({ isActive }) => (isActive ? 'is-active' : '')}
            >
              <span>{item.icon}</span>
              {item.label}
            </NavLink>
          ))}
        </nav>
      </aside>

      <div className="admin-workspace">
        <header className="admin-topbar">
          <div>
            <p>TRANG QUẢN TRỊ</p>
            <h1>Quản lý cửa hàng</h1>
          </div>

          <div className="admin-user">
            <Link to="/" className="admin-view-site">⌂ Xem cửa hàng</Link>
            <div>
              <strong>{user?.email || user?.fullName || 'admin@gmail.com'}</strong>
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
            <button type="button" onClick={handleLogout}>Đăng xuất</button>
          </div>
        </header>

        <main className="admin-main">
          <Outlet context={{ userRole }} />
        </main>
      </div>
    </div>
  );
}

export default AdminLayout;
