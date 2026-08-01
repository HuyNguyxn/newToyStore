import { NavLink, Outlet } from 'react-router-dom';
import useAuth from '../../hooks/useAuth.js';

const adminNavItems = [
  { to: '/admin/dashboard', label: 'Dashboard', icon: '📊' },
  { to: '/admin/products', label: 'Products', icon: '🧸' },
  { to: '/admin/categories', label: 'Categories', icon: '🗂️' },
  { to: '/admin/orders', label: 'Orders', icon: '🧾' },
  { to: '/admin/payments', label: 'Payments', icon: '💳' },
  { to: '/admin/refunds', label: 'Refunds', icon: '↩️' },
  { to: '/admin/users', label: 'Users', icon: '👤' },
  { to: '/admin/promotions', label: 'Promotions', icon: '🎁' },
  { to: '/admin/suppliers', label: 'Suppliers', icon: '🏭' },
  { to: '/admin/imports', label: 'Imports', icon: '📦' },
  { to: '/admin/supplier-returns', label: 'Supplier Returns', icon: '🔁' },
  { to: '/admin/inventory', label: 'Inventory', icon: '🏷️' },
  { to: '/admin/logistics', label: 'Logistics', icon: '🚚' },
  { to: '/admin/returns', label: 'Returns', icon: '📮' },
  { to: '/admin/reviews', label: 'Reviews', icon: '⭐' },
  { to: '/admin/moderation', label: 'Moderation', icon: '🛡️' },
  { to: '/admin/notifications', label: 'Notifications', icon: '🔔' },
  { to: '/admin/statistics', label: 'Statistics', icon: '📈' },
  { to: '/admin/uploads', label: 'Uploads', icon: '🖼️' },
];

function AdminLayout() {
  const { user, logout } = useAuth();

  return (
    <div className="admin-shell">
      <aside className="admin-sidebar">
        <div className="admin-brand">
          <span>
            <img src="/toystore-assets/logo.png" alt="New Toy Store" />
          </span>
          <div>
            <strong>New Toy Store</strong>
            <small>Admin Console</small>
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
            <p>New Toy Store</p>
            <h1>Management Console</h1>
          </div>

          <div className="admin-user">
            <span>{user?.fullName?.charAt(0) || 'A'}</span>
            <div>
              <strong>{user?.fullName || 'Admin'}</strong>
              <small>{user?.role || 'ADMIN'}</small>
            </div>
            <button type="button" onClick={logout}>Logout</button>
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
