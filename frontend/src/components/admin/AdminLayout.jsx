import { NavLink, Outlet } from 'react-router-dom';
import useAuth from '../../hooks/useAuth.js';

const adminNavItems = [
  { to: '/admin/dashboard', label: 'Dashboard' },
  { to: '/admin/products', label: 'Products' },
  { to: '/admin/categories', label: 'Categories' },
  { to: '/admin/orders', label: 'Orders' },
  { to: '/admin/payments', label: 'Payments' },
  { to: '/admin/users', label: 'Users' },
  { to: '/admin/promotions', label: 'Promotions' },
  { to: '/admin/suppliers', label: 'Suppliers' },
  { to: '/admin/imports', label: 'Imports' },
  { to: '/admin/logistics', label: 'Logistics' },
  { to: '/admin/returns', label: 'Returns' },
  { to: '/admin/reviews', label: 'Reviews' },
  { to: '/admin/moderation', label: 'Moderation' },
];

function AdminLayout() {
  const { user, logout } = useAuth();

  return (
    <div className="admin-shell">
      <aside className="admin-sidebar">
        <div className="admin-brand">
          <span>NTS</span>
          <strong>Admin</strong>
        </div>

        <nav className="admin-nav" aria-label="Admin navigation">
          {adminNavItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              className={({ isActive }) => (isActive ? 'is-active' : '')}
            >
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
