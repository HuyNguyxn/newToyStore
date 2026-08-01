import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import useAuth from '../../hooks/useAuth.js';
import { getUnreadNotificationCount } from '../../services/notificationService.js';

function Header() {
  const navigate = useNavigate();
  const { isAuthenticated, user, logout } = useAuth();
  const [unreadCount, setUnreadCount] = useState(0);

  useEffect(() => {
    if (!isAuthenticated) {
      setUnreadCount(0);
      return;
    }

    let active = true;

    getUnreadNotificationCount()
      .then((result) => {
        if (active) {
          setUnreadCount(result.unreadCount || 0);
        }
      })
      .catch(() => {
        if (active) {
          setUnreadCount(0);
        }
      });

    return () => {
      active = false;
    };
  }, [isAuthenticated]);

  function handleSearch(event) {
    event.preventDefault();
    const keyword = new FormData(event.currentTarget).get('keyword')?.toString().trim();

    if (keyword) {
      navigate(`/products?keyword=${encodeURIComponent(keyword)}&page=0`);
      return;
    }

    navigate('/products');
  }

  return (
    <header className="site-header">
      <div className="container site-header__inner">
        <Link to="/" className="brand" aria-label="ToyStore home">
          <span className="brand__logo">
            <img src="/toystore-assets/logo.png" alt="ToyStore" />
          </span>
          <span className="brand__name">ToyStore</span>
        </Link>

        <nav className="header-main-nav" aria-label="Store navigation">
          <Link to="/products">Danh Mục⌄</Link>
          <Link to="/products">Tất cả sản phẩm</Link>
        </nav>

        <form className="search-box" role="search" onSubmit={handleSearch}>
          <input name="keyword" type="search" placeholder="Tìm kiếm sản phẩm..." aria-label="Tìm kiếm sản phẩm" />
          <button type="submit">🔍</button>
        </form>

        <nav className="header-actions" aria-label="Customer actions">
          <Link to="/cart" className="cart-link" aria-label="Giỏ hàng">
            🛒
          </Link>
          {isAuthenticated && (
            <Link to="/notifications" className="cart-link" aria-label="Thông báo">
              🔔
              {unreadCount > 0 && <span className="cart-link__badge">{unreadCount}</span>}
            </Link>
          )}
          {isAuthenticated ? (
            <div className="user-menu">
              <Link to="/profile" className="user-menu__profile">
                <span>{user?.fullName?.charAt(0) || 'U'}</span>
                <strong>{user?.fullName || 'Tài khoản'}</strong>
              </Link>
              <Link to="/orders" className="plain-header-link">Đơn hàng</Link>
              <button type="button" onClick={logout}>Thoát</button>
            </div>
          ) : (
            <Link to="/login" className="login-link">Đăng nhập</Link>
          )}
          <span className="header-flag" aria-hidden="true">🇻🇳</span>
        </nav>
      </div>
    </header>
  );
}

export default Header;
