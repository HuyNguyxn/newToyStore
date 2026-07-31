import { useEffect, useMemo, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import useAuth from '../../hooks/useAuth.js';
import { sampleCart } from '../../data/sampleData.js';
import { checkoutCart, getCart } from '../../services/cartService.js';
import { formatPrice } from '../../utils/formatters.js';

function CheckoutPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const { user } = useAuth();
  const [cart, setCart] = useState(null);
  const [shippingAddress, setShippingAddress] = useState('');
  const [promoCode, setPromoCode] = useState(location.state?.promoCode || '');
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    if (!user?.id) {
      return;
    }

    const defaultAddress = user.addresses?.find((address) => address.default || address.isDefault);
    if (defaultAddress) {
      setShippingAddress(formatAddress(defaultAddress));
    }

    getCart(user.id, promoCode)
      .then((result) => setCart(result))
      .catch(() => {
        setCart(sampleCart);
        setError('Backend chua san sang, dang hien thi du lieu mau.');
      })
      .finally(() => setLoading(false));
  }, [user?.id]);

  const selectedItems = useMemo(() => {
    return (cart?.items || []).filter((item) => Boolean(item.isSelected ?? item.selected));
  }, [cart]);

  async function handleSubmit(event) {
    event.preventDefault();
    setMessage('');
    setError('');

    if (!shippingAddress.trim()) {
      setError('Vui long nhap dia chi giao hang.');
      return;
    }

    setSubmitting(true);

    try {
      await checkoutCart(user.id, {
        shippingAddress: shippingAddress.trim(),
        promoCode: promoCode.trim() || null,
      });
      setMessage('Da tao yeu cau checkout. Buoc tiep theo la thanh toan COD hoac VNPAY.');
    } catch (err) {
      setError(err.message || 'Checkout that bai. Vui long kiem tra lai gio hang.');
    } finally {
      setSubmitting(false);
    }
  }

  if (loading) {
    return <div className="page-message">Dang tai thong tin thanh toan...</div>;
  }

  return (
    <div className="checkout-page container">
      <section className="checkout-form-card">
        <div className="cart-heading">
          <div>
            <p>Thanh toan</p>
            <h1>Xac nhan don hang</h1>
          </div>
          <Link to="/cart">Quay lai gio hang</Link>
        </div>

        {error && <div className="form-alert">{error}</div>}
        {message && <div className="form-alert form-alert--success">{message}</div>}

        <form className="checkout-form" onSubmit={handleSubmit}>
          <label>
            Dia chi giao hang
            <textarea
              value={shippingAddress}
              onChange={(event) => setShippingAddress(event.target.value)}
              placeholder="Nhap dia chi nhan hang chi tiet"
              rows="5"
              maxLength="500"
              required
            />
          </label>

          <label>
            Ma giam gia
            <input
              value={promoCode}
              onChange={(event) => setPromoCode(event.target.value)}
              placeholder="Neu co"
              maxLength="50"
            />
          </label>

          <button type="submit" disabled={submitting || selectedItems.length === 0}>
            {submitting ? 'Dang xu ly...' : 'Dat hang'}
          </button>
        </form>
      </section>

      <aside className="checkout-summary">
        <h2>San pham da chon</h2>
        <div className="checkout-items">
          {selectedItems.map((item) => (
            <div className="checkout-item" key={item.id}>
              <span>{item.productName} x {item.quantity}</span>
              <strong>{formatPrice(item.finalPrice * item.quantity)}</strong>
            </div>
          ))}
        </div>

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
      </aside>
    </div>
  );
}

function formatAddress(address) {
  return [
    address.receiverName,
    address.receiverPhone,
    address.detailAddress,
  ].filter(Boolean).join(' - ');
}

export default CheckoutPage;
