import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import useAuth from '../../hooks/useAuth.js';
import { sampleCart } from '../../data/sampleData.js';
import {
  clearCart,
  getCart,
  removeCartItem,
  toggleCartItemSelection,
  updateCartItemQuantity,
} from '../../services/cartService.js';
import { formatPrice } from '../../utils/formatters.js';

function CartPage() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const [cart, setCart] = useState(null);
  const [promoCode, setPromoCode] = useState('');
  const [loading, setLoading] = useState(true);
  const [updatingItemId, setUpdatingItemId] = useState(null);
  const [notice, setNotice] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    loadCart('');
  }, [user?.id]);

  function loadCart(code = promoCode) {
    if (!user?.id) {
      return;
    }

    setLoading(true);
    setError('');

    getCart(user.id, code)
      .then((result) => {
        setCart(result);
        setPromoCode(result.appliedPromoCode || code || '');
      })
      .catch(() => {
        setCart(sampleCart);
        setNotice('Backend chua san sang, dang hien thi gio hang mau.');
      })
      .finally(() => setLoading(false));
  }

  async function handleQuantityChange(item, nextQuantity) {
    if (nextQuantity < 1) {
      return;
    }

    setUpdatingItemId(item.id);
    setError('');

    try {
      const result = await updateCartItemQuantity(user.id, item.id, nextQuantity);
      setCart(result);
    } catch (err) {
      setError(err.message || 'Khong the cap nhat so luong.');
    } finally {
      setUpdatingItemId(null);
    }
  }

  async function handleToggleItem(item) {
    setUpdatingItemId(item.id);
    setError('');

    try {
      const result = await toggleCartItemSelection(user.id, item.id, !isSelected(item));
      setCart(result);
    } catch (err) {
      setError(err.message || 'Khong the cap nhat lua chon san pham.');
    } finally {
      setUpdatingItemId(null);
    }
  }

  async function handleRemoveItem(item) {
    setUpdatingItemId(item.id);
    setError('');

    try {
      const result = await removeCartItem(user.id, item.id);
      setCart(result);
    } catch (err) {
      setError(err.message || 'Khong the xoa san pham khoi gio.');
    } finally {
      setUpdatingItemId(null);
    }
  }

  async function handleClearCart() {
    setError('');

    try {
      await clearCart(user.id);
      setCart({ ...cart, items: [], cartTotal: 0, finalTotal: 0, orderDiscountAmount: 0 });
    } catch (err) {
      setError(err.message || 'Khong the xoa gio hang.');
    }
  }

  function applyPromo(event) {
    event.preventDefault();
    loadCart(promoCode.trim());
  }

  const items = cart?.items || [];
  const selectedItems = items.filter(isSelected);
  const canCheckout = selectedItems.length > 0 && selectedItems.every(isAvailable);

  if (loading) {
    return <div className="page-message">Dang tai gio hang...</div>;
  }

  return (
    <div className="cart-page container">
      <section className="cart-content">
        <div className="cart-heading">
          <div>
            <p>Gio hang cua ban</p>
            <h1>{items.length} san pham</h1>
          </div>
          {items.length > 0 && (
            <button type="button" onClick={handleClearCart}>Xoa tat ca</button>
          )}
        </div>

        {notice && <div className="form-alert form-alert--soft">{notice}</div>}
        {error && <div className="form-alert">{error}</div>}

        {items.length === 0 ? (
          <div className="empty-state">
            Gio hang dang trong. <Link to="/products">Tiep tuc mua sam</Link>
          </div>
        ) : (
          <div className="cart-items">
            {items.map((item) => (
              <article className={!isAvailable(item) ? 'cart-item is-disabled' : 'cart-item'} key={item.id}>
                <input
                  type="checkbox"
                  checked={isSelected(item)}
                  disabled={updatingItemId === item.id}
                  onChange={() => handleToggleItem(item)}
                  aria-label="Chon san pham"
                />

                <Link to={`/products/${item.productId}`} className="cart-item__image">
                  {item.thumbnailUrl ? (
                    <img src={item.thumbnailUrl} alt={item.productName} />
                  ) : (
                    <span>{item.productName?.charAt(0) || 'P'}</span>
                  )}
                </Link>

                <div className="cart-item__info">
                  <Link to={`/products/${item.productId}`}>{item.productName}</Link>
                  <p>{item.variantAttributes || 'Mac dinh'}</p>
                  {item.hasPriceChanged && <strong>Gia san pham da thay doi</strong>}
                  {item.message && <strong>{item.message}</strong>}
                </div>

                <div className="cart-item__price">
                  <span>{formatPrice(item.originalPrice)}</span>
                  <strong>{formatPrice(item.finalPrice)}</strong>
                </div>

                <div className="cart-item__qty">
                  <button
                    type="button"
                    disabled={updatingItemId === item.id}
                    onClick={() => handleQuantityChange(item, item.quantity - 1)}
                  >
                    -
                  </button>
                  <input value={item.quantity} readOnly aria-label="So luong" />
                  <button
                    type="button"
                    disabled={updatingItemId === item.id}
                    onClick={() => handleQuantityChange(item, item.quantity + 1)}
                  >
                    +
                  </button>
                </div>

                <button
                  type="button"
                  className="cart-item__remove"
                  disabled={updatingItemId === item.id}
                  onClick={() => handleRemoveItem(item)}
                >
                  Xoa
                </button>
              </article>
            ))}
          </div>
        )}
      </section>

      <aside className="cart-summary">
        <h2>Tong don hang</h2>

        <form className="promo-form" onSubmit={applyPromo}>
          <input
            value={promoCode}
            onChange={(event) => setPromoCode(event.target.value)}
            placeholder="Ma giam gia"
          />
          <button type="submit">Ap dung</button>
        </form>

        {cart?.promoMessage && <p className="promo-message">{cart.promoMessage}</p>}

        <div className="summary-line">
          <span>Tam tinh</span>
          <strong>{formatPrice(cart?.cartTotal)}</strong>
        </div>
        <div className="summary-line">
          <span>Giam gia</span>
          <strong>-{formatPrice(cart?.orderDiscountAmount)}</strong>
        </div>
        <div className="summary-line summary-line--total">
          <span>Tong thanh toan</span>
          <strong>{formatPrice(cart?.finalTotal)}</strong>
        </div>

        <button
          type="button"
          disabled={!canCheckout}
          onClick={() => navigate('/checkout', { state: { promoCode } })}
        >
          Thanh toan
        </button>
      </aside>
    </div>
  );
}

function isSelected(item) {
  return Boolean(item.isSelected ?? item.selected);
}

function isAvailable(item) {
  return Boolean(item.isAvailable ?? item.available);
}

export default CartPage;
