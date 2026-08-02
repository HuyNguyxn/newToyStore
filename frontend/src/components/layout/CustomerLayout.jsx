import { Link, Outlet, useLocation } from 'react-router-dom';
import Header from './Header.jsx';
import Footer from './Footer.jsx';
import useAuth from '../../hooks/useAuth.js';

function isProfileIncomplete(user) {
  if (!user) {
    return false;
  }

  const hasName = Boolean(user.fullName?.trim());
  const hasPhone = Boolean(user.phoneNumber?.trim());
  const hasAddress = Boolean(user.addresses?.length);

  return !hasName || !hasPhone || !hasAddress;
}

function CustomerLayout() {
  const location = useLocation();
  const { isAuthenticated, user } = useAuth();
  const isAuthPage = ['/login', '/register', '/forgot-password', '/reset-password'].includes(location.pathname);
  const shouldShowProfileNotice = isAuthenticated && !isAuthPage && location.pathname !== '/profile' && isProfileIncomplete(user);

  return (
    <div className="app-shell">
      {shouldShowProfileNotice && (
        <div className="profile-completion-notice">
          <div className="container">
            <span>⚠️ Bạn nên bổ sung đầy đủ họ tên, số điện thoại và địa chỉ giao hàng để đặt hàng nhanh hơn.</span>
            <Link to="/profile">Bổ sung ngay</Link>
          </div>
        </div>
      )}
      {!isAuthPage && <Header />}
      <main className={isAuthPage ? 'app-main app-main--auth' : 'app-main'}>
        <Outlet />
      </main>
      {!isAuthPage && <Footer />}
    </div>
  );
}

export default CustomerLayout;
