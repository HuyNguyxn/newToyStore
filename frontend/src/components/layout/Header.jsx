import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import useAuth from '../../hooks/useAuth.js';
import { getUnreadNotificationCount } from '../../services/notificationService.js';
import { searchProducts } from '../../services/productService.js';
import { formatPrice, getProductPrice } from '../../utils/formatters.js';

function Header() {
  const navigate = useNavigate();
  const { isAuthenticated, user, logout } = useAuth();
  const [unreadCount, setUnreadCount] = useState(0);
  const [searchKeyword, setSearchKeyword] = useState('');
  const [searchResults, setSearchResults] = useState([]);
  const [searching, setSearching] = useState(false);
  const [showSearchResults, setShowSearchResults] = useState(false);

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

  useEffect(() => {
    const keyword = searchKeyword.trim();

    if (!keyword) {
      setSearchResults([]);
      setSearching(false);
      return;
    }

    let active = true;
    setSearching(true);

    const timerId = window.setTimeout(() => {
      searchProducts(keyword, { page: 0, size: 6, sort: 'createdAt,desc' })
        .then((result) => {
          if (active) {
            setSearchResults(result.content || []);
          }
        })
        .catch(() => {
          if (active) {
            setSearchResults([]);
          }
        })
        .finally(() => {
          if (active) {
            setSearching(false);
          }
        });
    }, 280);

    return () => {
      active = false;
      window.clearTimeout(timerId);
    };
  }, [searchKeyword]);

  function handleSearch(event) {
    event.preventDefault();
    const keyword = searchKeyword.trim();

    if (keyword) {
      setShowSearchResults(false);
      navigate(`/products?keyword=${encodeURIComponent(keyword)}&page=0`);
      return;
    }

    navigate('/products');
  }

  function handleLogout() {
    const confirmed = window.confirm('Bạn chắc chắn muốn đăng xuất?');

    if (confirmed) {
      logout();
      navigate('/');
    }
  }

  function goToProduct(productId) {
    setShowSearchResults(false);
    setSearchKeyword('');
    navigate(`/products/${productId}`);
  }

  return (
    <header className="site-header">
      <div className="container site-header__inner">
        <Link to="/" className="brand brand--logo-only" aria-label="ToyStore home">
          <span className="brand__logo">
            <img src="/toystore-assets/logo.png" alt="ToyStore" />
          </span>
        </Link>

        <nav className="header-main-nav" aria-label="Store navigation">
          <Link to="/products?view=categories">Danh Mục</Link>
          <Link to="/products">Tất cả sản phẩm</Link>
        </nav>

        <div className="search-area">
          <form className="search-box" role="search" onSubmit={handleSearch}>
            <input
              name="keyword"
              type="search"
              value={searchKeyword}
              placeholder="Tìm kiếm sản phẩm..."
              aria-label="Tìm kiếm sản phẩm"
              onChange={(event) => {
                setSearchKeyword(event.target.value);
                setShowSearchResults(true);
              }}
              onFocus={() => setShowSearchResults(Boolean(searchKeyword.trim()))}
              onBlur={() => window.setTimeout(() => setShowSearchResults(false), 160)}
            />
            <button type="submit">🔍</button>
          </form>

          {showSearchResults && searchKeyword.trim() && (
            <div className="search-suggestions">
              {searching && <div className="search-suggestions__status">Đang tìm...</div>}
              {!searching && searchResults.length === 0 && (
                <div className="search-suggestions__status">Không tìm thấy sản phẩm phù hợp.</div>
              )}
              {!searching && searchResults.map((product) => (
                <button type="button" key={product.id} onMouseDown={() => goToProduct(product.id)}>
                  <span>
                    {product.thumbnailUrl ? (
                      <img src={product.thumbnailUrl} alt="" />
                    ) : (
                      <em>{product.name?.charAt(0) || 'P'}</em>
                    )}
                  </span>
                  <strong>{product.name}</strong>
                  <small>{formatPrice(getProductPrice(product))}</small>
                </button>
              ))}
              {!searching && searchResults.length > 0 && (
                <Link
                  to={`/products?keyword=${encodeURIComponent(searchKeyword.trim())}&page=0`}
                  onMouseDown={() => setShowSearchResults(false)}
                >
                  Xem tất cả kết quả
                </Link>
              )}
            </div>
          )}
        </div>

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
              <Link to="/orders" className="plain-header-link">Lịch sử mua hàng</Link>
              {user?.role === 'ADMIN' && <Link to="/admin/dashboard" className="plain-header-link">Admin</Link>}
              <button type="button" onClick={handleLogout}>Đăng xuất</button>
            </div>
          ) : (
            <Link to="/login" className="login-link">Đăng nhập</Link>
          )}
        </nav>
      </div>
    </header>
  );
}

export default Header;
