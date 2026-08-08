import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import BackLink from '../../components/common/BackLink.jsx';
import useAuth from '../../hooks/useAuth.js';
import {
  clearCart,
  getCart,
  removeCartItem,
  toggleCartItemSelection,
  updateCartItemQuantity,
} from '../../services/cartService.js';
import { formatPrice } from '../../utils/formatters.js';

import { isUserProfileComplete } from '../../utils/userValidation.js';

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
    if (user && !isUserProfileComplete(user)) {
      navigate('/profile', {
        state: { requireInfoNotice: 'Bạn phải bổ sung đầy đủ họ tên, số điện thoại và địa chỉ.' },
      });
      return;
    }
    loadCart('');
  }, [user]);

  function loadCart(code = promoCode) {
    if (!user?.id) {
      setCart({ items: [], cartTotal: 0, finalTotal: 0, orderDiscountAmount: 0 });
      setLoading(false);
      return;
    }

    setLoading(true);
    setError('');

    getCart(user.id, code)
      .then((result) => {
        setCart(result || { items: [], cartTotal: 0, finalTotal: 0, orderDiscountAmount: 0 });
        setPromoCode(result?.appliedPromoCode || code || '');
      })
      .catch(() => {
        setCart({ items: [], cartTotal: 0, finalTotal: 0, orderDiscountAmount: 0 });
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
      setError(err.message || 'Không thể cập nhật số lượng.');
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
      setError(err.message || 'Không thể cập nhật lựa chọn sản phẩm.');
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
      setError(err.message || 'Không thể xóa sản phẩm khỏi giỏ.');
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
      setError(err.message || 'Không thể xóa giỏ hàng.');
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
    return <div className="page-message">Đang tải giỏ hàng...</div>;
  }

  return (
    <div className="cart-page container">
      <section className="cart-content">
        <BackLink fallback="/products" label="Tiếp tục mua sắm" />

        <div className="cart-heading">
          <div>
            <p>Giỏ hàng của bạn</p>
            <h1>{items.length} sản phẩm</h1>
          </div>
          {items.length > 0 && (
            <button type="button" onClick={handleClearCart}>Xóa tất cả</button>
          )}
        </div>

        {notice && <div className="form-alert form-alert--soft">{notice}</div>}
        {error && <div className="form-alert">{error}</div>}

        {items.length === 0 ? (
          <div className="empty-state">
            Giỏ hàng đang trống. <Link to="/products">Tiếp tục mua sắm</Link>
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
                  aria-label="Chọn sản phẩm"
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
                  <p>{item.variantAttributes || 'Mặc định'}</p>
                  {item.hasPriceChanged && <strong>Giá sản phẩm đã thay đổi</strong>}
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
                  <input value={item.quantity} readOnly aria-label="Số lượng" />
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
                  Xóa
                </button>
              </article>
            ))}
          </div>
        )}
      </section>

      <aside className="cart-summary">
        <h2>Tổng đơn hàng</h2>

        <form className="promo-form" onSubmit={applyPromo}>
          <input
            value={promoCode}
            onChange={(event) => setPromoCode(event.target.value)}
            placeholder="Mã giảm giá"
          />
          <button type="submit">Áp dụng</button>
        </form>

        {cart?.promoMessage && <p className="promo-message">{cart.promoMessage}</p>}

        <div className="summary-line">
          <span>Tạm tính</span>
          <strong>{formatPrice(cart?.cartTotal)}</strong>
        </div>
        <div className="summary-line">
          <span>Giảm giá</span>
          <strong>-{formatPrice(cart?.orderDiscountAmount)}</strong>
        </div>
        <div className="summary-line summary-line--total">
          <span>Tổng thanh toán</span>
          <strong>{formatPrice(cart?.finalTotal)}</strong>
        </div>

        <button
          type="button"
          disabled={!canCheckout}
          onClick={() => {
            if (user && !isUserProfileComplete(user)) {
              navigate('/profile', {
                state: { requireInfoNotice: 'Bạn phải bổ sung đầy đủ họ tên, số điện thoại và địa chỉ.' },
              });
              return;
            }
            navigate('/checkout', { state: { promoCode } });
          }}
        >
          Thanh toán
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
