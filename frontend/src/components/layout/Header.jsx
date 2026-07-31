import { Link, useNavigate } from 'react-router-dom';
import useAuth from '../../hooks/useAuth.js';

function Header() {
  const navigate = useNavigate();
  const { isAuthenticated, user, logout } = useAuth();

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
          <span className="brand__logo">NTS</span>
          <span className="brand__name">New Toy Store</span>
        </Link>

        <form className="search-box" role="search" onSubmit={handleSearch}>
          <input name="keyword" type="search" placeholder="Tim kiem do choi..." aria-label="Tim kiem do choi" />
          <button type="submit">Tim</button>
        </form>

        <nav className="header-actions" aria-label="Customer actions">
          <Link to="/cart" className="cart-link">
            Gio hang
            <span className="cart-link__badge">3</span>
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
