import { Link } from 'react-router-dom';

function Header() {
  return (
    <header className="site-header">
      <div className="container site-header__inner">
        <Link to="/" className="brand" aria-label="New Toy Store home">
          <span className="brand__logo">NTS</span>
          <span className="brand__name">New Toy Store</span>
        </Link>

        <form className="search-box" role="search">
          <input type="search" placeholder="Tim kiem do choi..." aria-label="Tim kiem do choi" />
          <button type="submit">Tim</button>
        </form>

        <nav className="header-actions" aria-label="Customer actions">
          <Link to="/cart" className="cart-link">
            Gio hang
            <span className="cart-link__badge">3</span>
          </Link>
          <Link to="/login" className="login-link">Dang nhap</Link>
        </nav>
      </div>
    </header>
  );
}

export default Header;
