import { Link, Outlet, useLocation } from 'react-router-dom';
import Header from './Header.jsx';
import Footer from './Footer.jsx';
import useAuth from '../../hooks/useAuth.js';
import BackLink from '../common/BackLink.jsx';

const routeBackLinks = {
  '/notifications': { fallback: '/', label: 'Quay lại trang trước' },
  '/payments': { fallback: '/profile', label: 'Quay lại tài khoản' },
  '/payments/vnpay-return': { fallback: '/payments', label: 'Quay lại lịch sử thanh toán' },
  '/vnpay-return': { fallback: '/payments', label: 'Quay lại lịch sử thanh toán' },
  '/payment/vnpay-return': { fallback: '/payments', label: 'Quay lại lịch sử thanh toán' },
  '/reviews/new': { fallback: '/reviews/me', label: 'Quay lại đánh giá của tôi' },
  '/shipments': { fallback: '/profile', label: 'Quay lại tài khoản' },
};

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
  const backLink = routeBackLinks[location.pathname];

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
        {backLink && (
          <div className="container route-back-row">
            <BackLink fallback={backLink.fallback} label={backLink.label} />
          </div>
        )}
        <Outlet />
      </main>
      {!isAuthPage && <Footer />}
    </div>
  );
}

export default CustomerLayout;
