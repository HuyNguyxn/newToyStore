import { NavLink, Outlet } from 'react-router-dom';
import useAuth from '../../hooks/useAuth.js';

const adminNavItems = [
  { to: '/admin/categories', label: 'Danh mục', icon: '▣' },
  { to: '/admin/products', label: 'Sản phẩm', icon: '□' },
  { to: '/admin/suppliers', label: 'Nhà cung cấp', icon: '▤' },
  { to: '/admin/imports', label: 'Nhập hàng', icon: '↧' },
  { to: '/admin/users', label: 'Người dùng', icon: '●' },
  { to: '/admin/orders', label: 'Đơn hàng', icon: '≡' },
  { to: '/admin/promotions', label: 'Khuyến mãi', icon: '◆' },
  { to: '/admin/statistics', label: 'Thống kê', icon: '↗' },
  { to: '/admin/payments', label: 'Thanh toán', icon: '▥' },
  { to: '/admin/refunds', label: 'Hoàn tiền', icon: '↩' },
  { to: '/admin/inventory', label: 'Tồn kho', icon: '▧' },
  { to: '/admin/logistics', label: 'Vận chuyển', icon: '⇄' },
  { to: '/admin/returns', label: 'Trả hàng', icon: '◫' },
  { to: '/admin/supplier-returns', label: 'Trả NCC', icon: '⇤' },
  { to: '/admin/reviews', label: 'Đánh giá', icon: '★' },
  { to: '/admin/moderation', label: 'Kiểm duyệt', icon: '◈' },
  { to: '/admin/notifications', label: 'Thông báo', icon: '●' },
  { to: '/admin/uploads', label: 'Uploads', icon: '▨' },
];

function AdminLayout() {
  const { user, logout } = useAuth();

  return (
    <div className="admin-shell">
      <aside className="admin-sidebar">
        <div className="admin-brand">
          <span>
            <img src="/toystore-assets/logo.png" alt="ToyStore" />
          </span>
          <div>
            <strong>ToyStore</strong>
            <small>Admin Panel</small>
          </div>
        </div>

        <nav className="admin-nav" aria-label="Admin navigation">
          {adminNavItems.map((item) => (
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
            <p>TRANG CHỦ ADMIN</p>
            <h1>Quản lý cửa hàng</h1>
          </div>

          <div className="admin-user">
            <a href="/" target="_blank" rel="noreferrer" className="admin-view-site">⌂ Xem Website</a>
            <span>🇻🇳</span>
            <div>
              <strong>{user?.email || user?.fullName || 'admin@gmail.com'}</strong>
              <small>{user?.role || 'ADMIN'}</small>
            </div>
            <button type="button" onClick={logout}>Đăng xuất</button>
          </div>
        </header>

        <main className="admin-main">
          <Outlet />
        </main>
      </div>
    </div>
  );
}

export default AdminLayout;
