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
        <Link to="/" className="brand" aria-label="New Toy Store home">
          <span className="brand__logo">
            <img src="/toystore-assets/logo.png" alt="New Toy Store" />
          </span>
          <span className="brand__name">New Toy Store</span>
        </Link>

        <form className="search-box" role="search" onSubmit={handleSearch}>
          <input name="keyword" type="search" placeholder="Tim kiem do choi..." aria-label="Tim kiem do choi" />
          <button type="submit">🔍</button>
        </form>

        <nav className="header-actions" aria-label="Customer actions">
          <Link to="/products" className="plain-header-link">San pham</Link>
          {isAuthenticated && <Link to="/orders" className="plain-header-link">Don hang</Link>}
          {isAuthenticated && (
            <Link to="/notifications" className="cart-link">
              🔔
              {unreadCount > 0 && <span className="cart-link__badge">{unreadCount}</span>}
            </Link>
          )}
          <Link to="/cart" className="cart-link">
            🛒
          </Link>
          {isAuthenticated ? (
            <div className="user-menu">
              <Link to="/profile" className="user-menu__profile">
                <span>{user?.fullName?.charAt(0) || 'U'}</span>
                <strong>{user?.fullName || 'Tai khoan'}</strong>
              </Link>
              <button type="button" onClick={logout}>Thoat</button>
            </div>
          ) : (
            <Link to="/login" className="login-link">Dang nhap</Link>
          )}
        </nav>
      </div>
    </header>
  );
}

export default Header;
