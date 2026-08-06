import { useEffect, useMemo, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import BackLink from '../../components/common/BackLink.jsx';
import useAuth from '../../hooks/useAuth.js';
import { checkoutCart, getCart } from '../../services/cartService.js';
import { getMyOrders } from '../../services/orderService.js';
import { checkoutPayment, createIdempotencyKey } from '../../services/paymentService.js';
import { formatPrice } from '../../utils/formatters.js';

function CheckoutPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const { user } = useAuth();
  
  const [cart, setCart] = useState(null);
  const [shippingAddress, setShippingAddress] = useState('');
  const [promoCode, setPromoCode] = useState(location.state?.promoCode || '');
  const [promoError, setPromoError] = useState('');
  const [promoSuccess, setPromoSuccess] = useState('');
  
  const [paymentMethod, setPaymentMethod] = useState('COD'); // COD or VNPAY
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  
  // Success order state for COD orders
  const [successOrder, setSuccessOrder] = useState(null);

  useEffect(() => {
    if (!user?.id) return;

    // Load user's default address
    const defaultAddress = user.addresses?.find((addr) => addr.default || addr.isDefault);
    if (defaultAddress) {
      setShippingAddress(formatAddress(defaultAddress));
    }

    loadCartData(promoCode);
  }, [user?.id]);

  async function loadCartData(code = '') {
    try {
      const result = await getCart(user.id, code);
      setCart(result);
      if (code) {
        setPromoSuccess('Áp dụng mã giảm giá thành công!');
        setPromoError('');
      }
    } catch (err) {
      setPromoError('Mã giảm giá không hợp lệ hoặc đã hết hạn.');
      setPromoSuccess('');
      // Reload cart without code
      const backup = await getCart(user.id, '');
      setCart(backup);
    } finally {
      setLoading(false);
    }
  }

  const selectedItems = useMemo(() => {
    return (cart?.items || []).filter((item) => Boolean(item.isSelected ?? item.selected));
  }, [cart]);

  // Handle promo code application manually
  function handleApplyPromo(e) {
    e.preventDefault();
    if (!promoCode.trim()) {
      setPromoError('Vui lòng nhập mã giảm giá.');
      setPromoSuccess('');
      return;
    }
    loadCartData(promoCode.trim());
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setError('');

    if (!shippingAddress.trim()) {
      setError('Vui lòng nhập địa chỉ giao hàng.');
      return;
    }

    setSubmitting(true);

    try {
      // 1. Submit cart checkout to create the order in backend
      await checkoutCart(user.id, {
        shippingAddress: shippingAddress.trim(),
        promoCode: promoCode.trim() || null,
      });

      // 2. Wait 800ms to allow asynchronous order creation event to persist in db
      await new Promise((resolve) => setTimeout(resolve, 800));

      // 3. Query user's order history to find the latest created order (since it's sorted desc)
      const orderHistory = await getMyOrders({ page: 0, size: 1 });
      const ordersList = orderHistory.content || orderHistory || [];
      const latestOrder = ordersList[0];

      if (!latestOrder) {
        throw new Error('Không thể định danh đơn hàng vừa tạo. Vui lòng kiểm tra Lịch sử mua hàng.');
      }

      // 4. Create payment transaction for the order
      const paymentPayload = {
        orderId: latestOrder.id,
        method: paymentMethod,
        idempotencyKey: createIdempotencyKey(latestOrder.id, paymentMethod),
      };

      const payment = await checkoutPayment(paymentPayload);

      // 5. If VNPay -> redirect directly to gateway url
      if (payment.paymentUrl) {
        window.location.href = payment.paymentUrl;
        return;
      }

      // 6. If COD -> show checkout success screen!
      setSuccessOrder(latestOrder);
    } catch (err) {
      setError(err.message || 'Đặt hàng thất bại. Vui lòng thử lại sau.');
    } finally {
      setSubmitting(false);
    }
  }

  if (loading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '60vh', fontFamily: 'system-ui' }}>
        <div style={{ color: '#ea580c', fontSize: '15px', fontWeight: '700' }}>Đang chuẩn bị trang thanh toán...</div>
      </div>
    );
  }

  // ═══════════════════════════════════════════════════════════════════
  // STATE 1: CHECKOUT SUCCESS SCREEN (FOR COD ORDERS)
  // ═══════════════════════════════════════════════════════════════════
  if (successOrder) {
    return (
      <div style={{ padding: '40px 16px', background: '#f8fafc', minHeight: '80vh', fontFamily: 'system-ui, -apple-system, sans-serif', display: 'flex', justifyContent: 'center', alignItems: 'center' }}>
        <div style={{ background: '#ffffff', width: '100%', maxWidth: '540px', borderRadius: '12px', border: '1px solid #e2e8f0', boxShadow: '0 4px 16px rgba(0,0,0,0.03)', padding: '40px 30px', textAlign: 'center' }}>
          
          <div style={{ width: '64px', height: '64px', borderRadius: '50%', background: '#d1fae5', color: '#10b981', fontSize: '32px', fontWeight: 'bold', display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto 20px auto' }}>
            ✓
          </div>

          <h1 style={{ fontSize: '22px', fontWeight: '900', color: '#0f172a', margin: '0 0 10px 0' }}>Đặt hàng thành công!</h1>
          <p style={{ fontSize: '14px', color: '#64748b', margin: '0 0 24px 0', lineHeight: 1.5 }}>
            Cảm ơn bạn đã mua sắm tại ToyStore. Đơn hàng của bạn đã được tiếp nhận và đang được xử lý.
          </p>

          {/* Details Card */}
          <div style={{ background: '#f8fafc', border: '1px solid #e2e8f0', borderRadius: '8px', padding: '16px 20px', marginBottom: '30px', textAlign: 'left', fontSize: '13.5px', color: '#334155' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '8px' }}>
              <span style={{ color: '#64748b' }}>Mã đơn hàng:</span>
              <strong style={{ color: '#ea580c' }}>DH{successOrder.id}</strong>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '8px' }}>
              <span style={{ color: '#64748b' }}>Phương thức thanh toán:</span>
              <strong style={{ color: '#0f172a' }}>COD (Tiền mặt khi nhận hàng)</strong>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between' }}>
              <span style={{ color: '#64748b' }}>Tổng thanh toán:</span>
              <strong style={{ color: '#dc2626', fontSize: '15px' }}>{formatPrice(successOrder.totalAmount || cart?.finalTotal)}</strong>
            </div>
          </div>

          {/* Actions */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
            <Link
              to={`/orders/${successOrder.id}`}
              style={{ padding: '12px', background: '#ea580c', color: '#ffffff', textDecoration: 'none', borderRadius: '8px', fontSize: '14px', fontWeight: '800', display: 'block', boxShadow: '0 4px 12px rgba(234,88,12,0.15)' }}
            >
              Xem chi tiết đơn hàng
            </Link>
            
            <Link
              to="/"
              style={{ padding: '12px', background: '#ffffff', color: '#475569', textDecoration: 'none', border: '1px solid #cbd5e1', borderRadius: '8px', fontSize: '14px', fontWeight: '700', display: 'block' }}
            >
              Tiếp tục mua sắm
            </Link>
          </div>

        </div>
      </div>
    );
  }

  // ═══════════════════════════════════════════════════════════════════
  // STATE 2: MAIN CHECKOUT FORM (MODERN SPLIT COLUMN DESIGN)
  // ═══════════════════════════════════════════════════════════════════
  return (
    <div style={{ background: '#f8fafc', minHeight: '100vh', padding: '30px 16px', fontFamily: 'system-ui, -apple-system, sans-serif' }}>
      <div className="container" style={{ maxWidth: '1100px', margin: '0 auto' }}>
        
        {/* Navigation Link */}
        <div style={{ marginBottom: '18px' }}>
          <BackLink fallback="/cart" label="Quay lại giỏ hàng" />
        </div>

        {/* Main Grid Layout */}
        <div style={{ display: 'grid', gridTemplateColumns: '1.6fr 1fr', gap: '30px', alignItems: 'start' }}>
          
          {/* LEFT COLUMN: Shipping & Payments */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
            
            {/* 1. SHIPPING ADDRESS SECTION */}
            <div style={{ background: '#ffffff', borderRadius: '12px', border: '1px solid #e2e8f0', padding: '24px', boxShadow: '0 4px 16px rgba(0,0,0,0.02)' }}>
              <h2 style={{ fontSize: '16px', fontWeight: '800', color: '#0f172a', margin: '0 0 16px 0', borderBottom: '1px solid #f1f5f9', paddingBottom: '10px' }}>
                Địa chỉ giao hàng
              </h2>
              
              <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                <textarea
                  value={shippingAddress}
                  onChange={(e) => setShippingAddress(e.target.value)}
                  placeholder="Nhập tên người nhận, số điện thoại và địa chỉ giao hàng chi tiết..."
                  rows="4"
                  maxLength="500"
                  required
                  style={{
                    width: '100%',
                    padding: '12px 14px',
                    border: '1px solid #cbd5e1',
                    borderRadius: '8px',
                    fontSize: '13.5px',
                    lineHeight: 1.5,
                    outline: 'none',
                    resize: 'none',
                    fontFamily: 'inherit',
                  }}
                />
                <span style={{ fontSize: '11.5px', color: '#64748b' }}>
                  * Định dạng khuyên dùng: Tên người nhận - Số điện thoại - Số nhà, tên đường, phường/xã, quận/huyện, tỉnh/thành phố.
                </span>
              </div>
            </div>

            {/* 2. PAYMENT METHODS SECTION */}
            <div style={{ background: '#ffffff', borderRadius: '12px', border: '1px solid #e2e8f0', padding: '24px', boxShadow: '0 4px 16px rgba(0,0,0,0.02)' }}>
              <h2 style={{ fontSize: '16px', fontWeight: '800', color: '#0f172a', margin: '0 0 16px 0', borderBottom: '1px solid #f1f5f9', paddingBottom: '10px' }}>
                Phương thức thanh toán
              </h2>

              <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                
                {/* Method 1: COD */}
                <div
                  onClick={() => setPaymentMethod('COD')}
                  style={{
                    border: paymentMethod === 'COD' ? '2px solid #ea580c' : '1px solid #cbd5e1',
                    background: paymentMethod === 'COD' ? '#fff8f3' : '#ffffff',
                    borderRadius: '10px',
                    padding: '16px 20px',
                    cursor: 'pointer',
                    display: 'flex',
                    alignItems: 'flex-start',
                    gap: '12px',
                    transition: 'all 0.15s ease',
                  }}
                >
                  <input
                    type="radio"
                    name="paymentMethod"
                    checked={paymentMethod === 'COD'}
                    onChange={() => setPaymentMethod('COD')}
                    style={{ marginTop: '3px', accentColor: '#ea580c' }}
                  />
                  <div>
                    <strong style={{ display: 'block', fontSize: '14px', color: '#0f172a', marginBottom: '4px' }}>
                      Thanh toán khi nhận hàng (COD)
                    </strong>
                    <span style={{ fontSize: '12.5px', color: '#64748b', lineHeight: 1.4 }}>
                      Thanh toán trực tiếp bằng tiền mặt khi đơn hàng được giao đến tận nhà.
                    </span>
                  </div>
                </div>

                {/* Method 2: VNPAY */}
                <div
                  onClick={() => setPaymentMethod('VNPAY')}
                  style={{
                    border: paymentMethod === 'VNPAY' ? '2px solid #ea580c' : '1px solid #cbd5e1',
                    background: paymentMethod === 'VNPAY' ? '#fff8f3' : '#ffffff',
                    borderRadius: '10px',
                    padding: '16px 20px',
                    cursor: 'pointer',
                    display: 'flex',
                    alignItems: 'flex-start',
                    gap: '12px',
                    transition: 'all 0.15s ease',
                  }}
                >
                  <input
                    type="radio"
                    name="paymentMethod"
                    checked={paymentMethod === 'VNPAY'}
                    onChange={() => setPaymentMethod('VNPAY')}
                    style={{ marginTop: '3px', accentColor: '#ea580c' }}
                  />
                  <div>
                    <strong style={{ display: 'block', fontSize: '14px', color: '#0f172a', marginBottom: '4px' }}>
                      Cổng thanh toán điện tử VNPAY
                    </strong>
                    <span style={{ fontSize: '12.5px', color: '#64748b', lineHeight: 1.4 }}>
                      Quét mã QR qua ví điện tử hoặc thanh toán bằng Thẻ ATM, Visa, MasterCard nội địa & quốc tế.
                    </span>
                  </div>
                </div>

              </div>
            </div>

          </div>

          {/* RIGHT COLUMN: Order Summary & Placement (Sticky Sidebar) */}
          <div style={{ position: 'sticky', top: '20px', display: 'flex', flexDirection: 'column', gap: '20px' }}>
            
            {/* ORDER ITEMS CARD */}
            <div style={{ background: '#ffffff', borderRadius: '12px', border: '1px solid #e2e8f0', padding: '24px', boxShadow: '0 4px 16px rgba(0,0,0,0.02)' }}>
              <h2 style={{ fontSize: '15px', fontWeight: '800', color: '#0f172a', margin: '0 0 16px 0', borderBottom: '1px solid #f1f5f9', paddingBottom: '10px' }}>
                Đơn hàng đã chọn
              </h2>

              <div style={{ display: 'flex', flexDirection: 'column', gap: '14px', maxHeight: '240px', overflowY: 'auto', marginBottom: '20px', paddingRight: '4px' }}>
                {selectedItems.map((item) => (
                  <div key={item.id} style={{ display: 'flex', justifyContent: 'space-between', fontSize: '13px', color: '#334155' }}>
                    <div style={{ flex: '1', paddingRight: '12px' }}>
                      <span style={{ fontWeight: '600', color: '#0f172a' }}>{item.productName}</span>
                      <span style={{ color: '#64748b', display: 'block', fontSize: '11.5px', marginTop: '2px' }}>Số lượng: {item.quantity}</span>
                    </div>
                    <strong style={{ color: '#0f172a', flexShrink: 0 }}>{formatPrice(item.finalPrice * item.quantity)}</strong>
                  </div>
                ))}
                {selectedItems.length === 0 && (
                  <div style={{ textAlign: 'center', color: '#94a3b8', fontSize: '13px', padding: '16px 0' }}>Chưa chọn sản phẩm thanh toán.</div>
                )}
              </div>

              {/* PROMO CODE BOX */}
              <form onSubmit={handleApplyPromo} style={{ display: 'flex', gap: '8px', borderTop: '1px solid #f1f5f9', paddingTop: '16px', marginBottom: '16px' }}>
                <input
                  type="text"
                  placeholder="Mã giảm giá..."
                  value={promoCode}
                  onChange={(e) => setPromoCode(e.target.value)}
                  style={{
                    flex: '1',
                    padding: '8px 10px',
                    border: '1px solid #cbd5e1',
                    borderRadius: '6px',
                    fontSize: '12.5px',
                    outline: 'none',
                  }}
                />
                <button
                  type="submit"
                  style={{
                    padding: '8px 16px',
                    background: '#f1f5f9',
                    color: '#475569',
                    border: '1px solid #cbd5e1',
                    borderRadius: '6px',
                    fontSize: '12.5px',
                    fontWeight: '700',
                    cursor: 'pointer',
                  }}
                >
                  Áp dụng
                </button>
              </form>
              {promoError && <div style={{ color: '#dc2626', fontSize: '11.5px', fontWeight: '700', marginBottom: '12px' }}>{promoError}</div>}
              {promoSuccess && <div style={{ color: '#16a34a', fontSize: '11.5px', fontWeight: '700', marginBottom: '12px' }}>{promoSuccess}</div>}

              {/* PRICING STATS */}
              <div style={{ display: 'flex', flexDirection: 'column', gap: '10px', borderTop: '1px solid #f1f5f9', paddingTop: '16px', fontSize: '13px' }}>
                
                <div style={{ display: 'flex', justifyContent: 'space-between', color: '#64748b' }}>
                  <span>Tạm tính</span>
                  <span style={{ color: '#334155', fontWeight: '600' }}>{formatPrice(cart?.cartTotal)}</span>
                </div>
                
                <div style={{ display: 'flex', justifyContent: 'space-between', color: '#64748b' }}>
                  <span>Giảm giá</span>
                  <span style={{ color: '#16a34a', fontWeight: '700' }}>-{formatPrice(cart?.orderDiscountAmount)}</span>
                </div>
                
                <div style={{ display: 'flex', justifyContent: 'space-between', color: '#64748b' }}>
                  <span>Phí giao hàng</span>
                  <span style={{ color: '#334155', fontWeight: '600' }}>Miễn phí</span>
                </div>

                <div style={{ display: 'flex', justifyContent: 'space-between', color: '#0f172a', fontWeight: '800', borderTop: '1px solid #f1f5f9', paddingTop: '12px', fontSize: '14.5px' }}>
                  <span>Tổng thanh toán</span>
                  <span style={{ color: '#dc2626', fontSize: '17px', fontWeight: '900' }}>{formatPrice(cart?.finalTotal)}</span>
                </div>

              </div>

              {/* ERROR/SUCCESS MESSAGES */}
              {error && (
                <div style={{ background: '#fef2f2', color: '#dc2626', border: '1px solid #fecaca', padding: '10px 12px', borderRadius: '6px', fontSize: '12.5px', fontWeight: '700', marginTop: '16px' }}>
                  {error}
                </div>
              )}

              {/* MAIN PLACE ORDER BUTTON */}
              <button
                onClick={handleSubmit}
                disabled={submitting || selectedItems.length === 0}
                style={{
                  width: '100%',
                  padding: '14px',
                  background: '#ea580c',
                  color: '#ffffff',
                  border: 'none',
                  borderRadius: '8px',
                  fontSize: '14.5px',
                  fontWeight: '800',
                  cursor: 'pointer',
                  marginTop: '20px',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  boxShadow: '0 6px 20px rgba(234,88,12,0.15)',
                  transition: 'all 0.15s ease',
                }}
              >
                {submitting ? 'Đang xử lý đơn hàng...' : paymentMethod === 'VNPAY' ? 'Thanh toán qua VNPAY' : 'Xác nhận đặt hàng'}
              </button>

            </div>

          </div>

        </div>

      </div>
    </div>
  );
}

function formatAddress(address) {
  return [
    address.receiverName,
    address.receiverPhone,
    address.detailAddress || address.fullAddress || address.addressLine,
  ].filter(Boolean).join(' - ');
}

export default CheckoutPage;
