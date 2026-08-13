import { useEffect, useRef, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import useAuth from '../../hooks/useAuth.js';
import { getCart } from '../../services/cartService.js';
import { getUnreadNotificationCount } from '../../services/notificationService.js';
import { searchProducts } from '../../services/productService.js';
import { formatPrice, getProductPrice } from '../../utils/formatters.js';
import { isUserProfileComplete } from '../../utils/userValidation.js';
import NotificationPopover from './NotificationPopover.jsx';

function Header() {
  const navigate = useNavigate();
  const location = useLocation();
  const { isAuthenticated, user, logout } = useAuth();
  const [unreadCount, setUnreadCount] = useState(0);
  const [cartCount, setCartCount] = useState(0);

  function handleCartClick(e) {
    if (isAuthenticated && !isUserProfileComplete(user)) {
      e.preventDefault();
      navigate('/profile', {
        state: { requireInfoNotice: 'Bạn phải bổ sung đầy đủ họ tên, số điện thoại và địa chỉ.' },
      });
    }
  }
  const [searchKeyword, setSearchKeyword] = useState('');
  const [searchResults, setSearchResults] = useState([]);
  const [searching, setSearching] = useState(false);
  const [showSearchResults, setShowSearchResults] = useState(false);
  const [showUserDropdown, setShowUserDropdown] = useState(false);
  const dropdownRef = useRef(null);

  useEffect(() => {
    function handleClickOutside(e) {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target)) {
        setShowUserDropdown(false);
      }
    }
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  function refreshNotificationCount() {
    if (!isAuthenticated) {
      setUnreadCount(0);
      return;
    }
    getUnreadNotificationCount()
      .then((res) => setUnreadCount(res?.unreadCount || 0))
      .catch(() => setUnreadCount(0));
  }

  function refreshCartCount() {
    if (!isAuthenticated || !user?.id) {
      setCartCount(0);
      return;
    }
    getCart(user.id)
      .then((cart) => {
        const count = (cart?.items || []).reduce((sum, item) => sum + (item.quantity || 1), 0);
        setCartCount(count);
      })
      .catch(() => setCartCount(0));
  }

  useEffect(() => {
    refreshNotificationCount();
    refreshCartCount();

    function onNotificationsUpdated() {
      refreshNotificationCount();
    }
    function onCartUpdated() {
      refreshCartCount();
    }

    window.addEventListener('notifications_updated', onNotificationsUpdated);
    window.addEventListener('cart_updated', onCartUpdated);

    return () => {
      window.removeEventListener('notifications_updated', onNotificationsUpdated);
      window.removeEventListener('cart_updated', onCartUpdated);
    };
  }, [isAuthenticated, user?.id]);

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
          <Link to="/cart" onClick={handleCartClick} className="cart-link" aria-label="Giỏ hàng" style={{ position: 'relative' }}>
            🛒
            {cartCount > 0 && (
              <span
                className="cart-link__badge"
                style={{
                  position: 'absolute',
                  top: '-5px',
                  right: '-6px',
                  background: '#ea580c',
                  color: '#ffffff',
                  fontSize: '11px',
                  fontWeight: '900',
                  minWidth: '18px',
                  height: '18px',
                  borderRadius: '50%',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  boxShadow: '0 2px 6px rgba(234,88,12,0.4)',
                  border: '1.5px solid #ffffff',
                }}
              >
                {cartCount}
              </span>
            )}
          </Link>
          {isAuthenticated && (
            <NotificationPopover
              unreadCount={unreadCount}
              onNotificationsChanged={refreshNotificationCount}
            />
          )}
          {isAuthenticated ? (
            <div className="user-menu" ref={dropdownRef} style={{ position: 'relative', display: 'flex', alignItems: 'center', gap: '10px' }}>
              {/* USER MENU TRIGGER BUTTON */}
              <button
                type="button"
                onClick={() => setShowUserDropdown((prev) => !prev)}
                title="Bấm để mở danh mục chức năng cá nhân"
                style={{
                  display: 'inline-flex',
                  alignItems: 'center',
                  gap: '8px',
                  background: '#ffffff',
                  border: '1px solid #cbd5e1',
                  borderRadius: '20px',
                  padding: '5px 12px 5px 6px',
                  cursor: 'pointer',
                  fontWeight: '700',
                  color: '#1e293b',
                  boxShadow: '0 2px 6px rgba(0,0,0,0.04)',
                  transition: 'all 0.15s ease',
                }}
                onMouseEnter={(e) => e.currentTarget.style.borderColor = '#ea580c'}
                onMouseLeave={(e) => e.currentTarget.style.borderColor = '#cbd5e1'}
              >
                <span style={{
                  width: '26px',
                  height: '26px',
                  borderRadius: '50%',
                  background: '#fff7ed',
                  color: '#ea580c',
                  display: 'grid',
                  placeItems: 'center',
                  fontWeight: '900',
                  fontSize: '12px'
                }}>
                  {user?.fullName?.charAt(0) || 'U'}
                </span>
                <span style={{ fontSize: '13.5px', fontWeight: '800', maxWidth: '100px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                  {user?.fullName || 'Tài khoản'}
                </span>
                <span style={{ fontSize: '10px', color: '#64748b' }}>{showUserDropdown ? '▲' : '▼'}</span>
              </button>

              {/* NÚT TRANG QUẢN TRỊ ADMIN (HIỂN THỊ TRỰC TIẾP TRÊN HEADER CHO ADMIN, MANAGER, STAFF) */}
              {(user?.role === 'ADMIN' || user?.role === 'MANAGER' || user?.role === 'STAFF') && (
                <Link
                  to="/admin/dashboard"
                  style={{
                    minHeight: '36px',
                    border: '1px solid #fed7aa',
                    borderRadius: '20px',
                    padding: '0 14px',
                    background: '#fff7ed',
                    color: '#ea580c',
                    fontSize: '13px',
                    fontWeight: '800',
                    textDecoration: 'none',
                    display: 'inline-flex',
                    alignItems: 'center',
                    gap: '6px',
                    boxShadow: '0 2px 6px rgba(234,88,12,0.1)',
                    transition: 'all 0.15s ease',
                  }}
                  onMouseEnter={(e) => {
                    e.currentTarget.style.background = '#ea580c';
                    e.currentTarget.style.color = '#ffffff';
                  }}
                  onMouseLeave={(e) => {
                    e.currentTarget.style.background = '#fff7ed';
                    e.currentTarget.style.color = '#ea580c';
                  }}
                >
                  🛡️ Trang Admin
                </Link>
              )}

              {/* NÚT ĐĂNG XUẤT */}
              <button
                type="button"
                onClick={handleLogout}
                style={{
                  minHeight: '36px',
                  border: 'none',
                  borderRadius: '20px',
                  padding: '0 16px',
                  background: 'linear-gradient(135deg, #ea580c 0%, #c2410c 100%)',
                  color: '#ffffff',
                  fontSize: '13px',
                  fontWeight: '800',
                  cursor: 'pointer',
                  boxShadow: '0 2px 8px rgba(234,88,12,0.25)',
                }}
              >
                Đăng xuất
              </button>

              {/* DROPDOWN MENU KHI BẤM NÚT TÀI KHOẢN (THIẾT KẾ SANG TRỌNG MODERN UI) */}
              {showUserDropdown && (() => {
                const renderDropdownLink = (to, icon, label, bgGradient, iconColor = '#ffffff') => {
                  const isActive = location.pathname === to;

                  return (
                    <Link
                      key={to}
                      to={to}
                      onClick={() => setShowUserDropdown(false)}
                      style={{
                        padding: '9px 12px',
                        fontSize: '13px',
                        fontWeight: isActive ? '800' : '700',
                        color: isActive ? '#ea580c' : '#334155',
                        textDecoration: 'none',
                        borderRadius: '12px',
                        display: 'flex',
                        alignItems: 'center',
                        gap: '12px',
                        background: isActive ? 'linear-gradient(135deg, #fff7ed 0%, #ffedd5 100%)' : 'transparent',
                        borderLeft: isActive ? '4px solid #ea580c' : '4px solid transparent',
                        boxShadow: isActive ? '0 2px 8px rgba(234, 88, 12, 0.12)' : 'none',
                        transition: 'all 0.2s cubic-bezier(0.4, 0, 0.2, 1)',
                        marginBottom: '2px',
                      }}
                      onMouseEnter={(e) => {
                        if (!isActive) {
                          e.currentTarget.style.background = '#f8fafc';
                          e.currentTarget.style.color = '#ea580c';
                          e.currentTarget.style.transform = 'translateX(4px)';
                        }
                      }}
                      onMouseLeave={(e) => {
                        if (!isActive) {
                          e.currentTarget.style.background = 'transparent';
                          e.currentTarget.style.color = '#334155';
                          e.currentTarget.style.transform = 'translateX(0)';
                        }
                      }}
                    >
                      <span
                        style={{
                          width: '32px',
                          height: '32px',
                          borderRadius: '10px',
                          background: bgGradient,
                          color: iconColor,
                          display: 'flex',
                          alignItems: 'center',
                          justifyContent: 'center',
                          fontSize: '15px',
                          boxShadow: isActive ? '0 4px 10px rgba(234, 88, 12, 0.2)' : '0 2px 6px rgba(0,0,0,0.06)',
                          flexShrink: 0,
                          transition: 'transform 0.2s ease',
                        }}
                      >
                        {icon}
                      </span>
                      <span style={{ flex: 1, whiteSpace: 'nowrap' }}>{label}</span>
                      {isActive && (
                        <span style={{ width: '6px', height: '6px', borderRadius: '50%', background: '#ea580c', display: 'inline-block' }} />
                      )}
                    </Link>
                  );
                };

                return (
                  <div
                    style={{
                      position: 'absolute',
                      top: 'calc(100% + 12px)',
                      right: 0,
                      background: '#ffffff',
                      border: '1px solid rgba(226, 232, 240, 0.9)',
                      borderRadius: '20px',
                      boxShadow: '0 24px 50px -12px rgba(15, 23, 42, 0.18), 0 4px 16px rgba(0,0,0,0.04)',
                      width: '280px',
                      padding: '12px',
                      zIndex: 1000,
                      display: 'flex',
                      flexDirection: 'column',
                      animation: 'fadeIn 0.2s cubic-bezier(0.16, 1, 0.3, 1)',
                    }}
                  >
                    {/* HERO BANNER THÔNG TIN NGƯỜI DÙNG */}
                    <div
                      style={{
                        background: 'linear-gradient(135deg, #fff7ed 0%, #ffedd5 100%)',
                        borderRadius: '14px',
                        padding: '12px 14px',
                        marginBottom: '10px',
                        display: 'flex',
                        alignItems: 'center',
                        gap: '12px',
                        border: '1px solid #fed7aa',
                      }}
                    >
                      <div
                        style={{
                          width: '40px',
                          height: '40px',
                          borderRadius: '50%',
                          background: 'linear-gradient(135deg, #ea580c 0%, #c2410c 100%)',
                          color: '#ffffff',
                          display: 'flex',
                          alignItems: 'center',
                          justifyContent: 'center',
                          fontWeight: '900',
                          fontSize: '17px',
                          boxShadow: '0 4px 12px rgba(234, 88, 12, 0.3)',
                          flexShrink: 0,
                        }}
                      >
                        {user?.fullName ? user.fullName.charAt(0).toUpperCase() : 'U'}
                      </div>
                      <div style={{ flex: 1, minWidth: 0 }}>
                        <p style={{ margin: 0, fontSize: '14px', fontWeight: '800', color: '#0f172a', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                          {user?.fullName}
                        </p>
                        <span style={{ fontSize: '11px', color: '#ea580c', fontWeight: '800', textTransform: 'uppercase', letterSpacing: '0.4px' }}>
                          {['ADMIN', 'MANAGER', 'STAFF'].includes(user?.role) ? `🛡️ ${user.role}` : '⭐ Thành viên'}
                        </span>
                      </div>
                    </div>

                    {/* NHÓM 1: TÀI KHOẢN & ĐƠN HÀNG */}
                    <div style={{ fontSize: '10.5px', fontWeight: '800', color: '#94a3b8', textTransform: 'uppercase', letterSpacing: '0.8px', padding: '6px 10px 4px 10px', display: 'flex', alignItems: 'center', gap: '6px' }}>
                      <span style={{ width: '4px', height: '12px', borderRadius: '2px', background: '#ea580c', display: 'inline-block' }}></span>
                      <span>Tài khoản & Đơn hàng</span>
                    </div>

                    {renderDropdownLink('/profile', '👤', 'Trang cá nhân', 'linear-gradient(135deg, #fef3c7 0%, #fde68a 100%)')}
                    {renderDropdownLink('/orders', '📦', 'Lịch sử mua hàng', 'linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%)')}
                    {renderDropdownLink('/shipments', '🚚', 'Theo dõi vận chuyển', 'linear-gradient(135deg, #e0e7ff 0%, #c7d2fe 100%)')}

                    <div style={{ height: '1px', background: '#f1f5f9', margin: '6px 4px' }} />

                    {/* NHÓM 2: ĐÁNH GIÁ & ĐỔI TRẢ */}
                    <div style={{ fontSize: '10.5px', fontWeight: '800', color: '#94a3b8', textTransform: 'uppercase', letterSpacing: '0.8px', padding: '6px 10px 4px 10px', display: 'flex', alignItems: 'center', gap: '6px' }}>
                      <span style={{ width: '4px', height: '12px', borderRadius: '2px', background: '#ec4899', display: 'inline-block' }}></span>
                      <span>Đánh giá & Trả hàng</span>
                    </div>

                    {renderDropdownLink('/reviews/new', '✍️', 'Viết đánh giá', 'linear-gradient(135deg, #fce7f3 0%, #fbcfe8 100%)')}
                    {renderDropdownLink('/reviews/me', '⭐', 'Đánh giá của tôi', 'linear-gradient(135deg, #fef3c7 0%, #fde68a 100%)')}
                    {renderDropdownLink('/returns/new', '🔄', 'Tạo yêu cầu trả hàng', 'linear-gradient(135deg, #ccfbf1 0%, #99f6e4 100%)')}
                    {renderDropdownLink('/returns', '📋', 'Yêu cầu trả hàng của tôi', 'linear-gradient(135deg, #f1f5f9 0%, #e2e8f0 100%)')}

                    {/* NHÓM 3: HỆ THỐNG ADMIN (DÀNH CHO QUẢN TRỊ VIÊN) */}
                    {(user?.role === 'ADMIN' || user?.role === 'MANAGER' || user?.role === 'STAFF') && (
                      <>
                        <div style={{ height: '1px', background: '#fed7aa', margin: '6px 4px' }} />
                        <Link
                          to="/admin/dashboard"
                          onClick={() => setShowUserDropdown(false)}
                          style={{
                            padding: '10px 12px',
                            fontSize: '13.5px',
                            fontWeight: '800',
                            color: '#ffffff',
                            background: 'linear-gradient(135deg, #ea580c 0%, #c2410c 100%)',
                            textDecoration: 'none',
                            borderRadius: '12px',
                            display: 'flex',
                            alignItems: 'center',
                            gap: '10px',
                            boxShadow: '0 4px 14px rgba(234,88,12,0.3)',
                            transition: 'all 0.2s ease',
                            marginTop: '4px',
                          }}
                          onMouseEnter={(e) => { e.currentTarget.style.transform = 'translateY(-1px) scale(1.01)'; e.currentTarget.style.boxShadow = '0 6px 18px rgba(234,88,12,0.4)'; }}
                          onMouseLeave={(e) => { e.currentTarget.style.transform = 'translateY(0) scale(1)'; e.currentTarget.style.boxShadow = '0 4px 14px rgba(234,88,12,0.3)'; }}
                        >
                          <span style={{ fontSize: '16px' }}>🛡️</span>
                          <span>Trang quản trị Admin</span>
                        </Link>
                      </>
                    )}
                  </div>
                );
              })()}
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
