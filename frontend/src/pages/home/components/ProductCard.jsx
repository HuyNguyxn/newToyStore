import { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import useAuth from '../../../hooks/useAuth.js';
import { addCartItem } from '../../../services/cartService.js';
import {
  formatPrice,
  getProductOriginalPrice,
  getProductPrice,
  getProductStatusLabel,
} from '../../../utils/formatters.js';

function ProductCard({ product }) {
  const navigate = useNavigate();
  const location = useLocation();
  const { isAuthenticated, user } = useAuth();
  const [adding, setAdding] = useState(false);
  const [addedMessage, setAddedMessage] = useState('');

  const price = getProductPrice(product);
  const originalPrice = getProductOriginalPrice(product);
  const showOriginalPrice = originalPrice > price;
  const statusLabel = getProductStatusLabel(product);
  const rating = Number(product.averageRating || product.rating || 0);
  const firstLetter = product.name?.charAt(0) || 'P';
  const thumbnailUrl = normalizeThumbnailUrl(product.thumbnailUrl);
  const defaultVariantId = product.defaultVariantId || product.variants?.[0]?.id;
  const canQuickAdd = Boolean((product.quickAddAvailable || defaultVariantId) && defaultVariantId);

  async function handleQuickAdd(e) {
    e.preventDefault();
    e.stopPropagation();

    if (!isAuthenticated) {
      navigate('/login', { state: { from: location } });
      return;
    }

    const targetUserId = user?.id || 1;
    const targetVariantId = defaultVariantId || product?.defaultVariantId || product?.variants?.[0]?.id;

    if (!targetVariantId) {
      navigate(`/products/${product.id}`);
      return;
    }

    setAdding(true);
    try {
      await addCartItem(targetUserId, {
        productId: product.id,
        variantId: targetVariantId,
        quantity: 1,
      });
      setAddedMessage('✓ Đã thêm vào giỏ hàng');
      setTimeout(() => setAddedMessage(''), 2500);
    } catch (err) {
      const errorText = typeof err === 'string' ? err : (err?.message || err?.error || err?.title || '');
      if (errorText) {
        setAddedMessage(`✖ ${errorText}`);
      } else {
        setAddedMessage('✖ Vui lòng kiểm tra Server Backend Java');
      }
      setTimeout(() => setAddedMessage(''), 4000);
    } finally {
      setAdding(false);
    }
  }

  return (
    <article className="product-card">
      <div className="product-card__image">
        {thumbnailUrl ? (
          <img src={thumbnailUrl} alt={product.name} loading="lazy" />
        ) : (
          <div className="product-card__placeholder" aria-hidden="true">
            <img src="/toystore-assets/logo.png" alt="" />
            <span>{firstLetter}</span>
          </div>
        )}
        <strong>{statusLabel}</strong>
      </div>

      <div className="product-card__body">
        <h3 title={product.name}>{product.name}</h3>
        <p className="product-card__rating">
          ★ {rating.toFixed(1)} <span>({product.reviewCount || 0} đánh giá)</span>
        </p>
        <p className="product-card__price">
          {showOriginalPrice && <span>{formatPrice(originalPrice)}</span>}
          <strong>{formatPrice(price)}</strong>
        </p>

        {addedMessage && (
          <div
            style={{
              fontSize: '11px',
              color: addedMessage.startsWith('✓') ? '#16a34a' : '#dc2626',
              fontWeight: '800',
              marginBottom: '8px',
              textAlign: 'center',
              background: addedMessage.startsWith('✓') ? '#f0fdf4' : '#fef2f2',
              border: `1px solid ${addedMessage.startsWith('✓') ? '#bbf7d0' : '#fecaca'}`,
              padding: '4px 8px',
              borderRadius: '8px',
              wordBreak: 'break-word',
            }}
          >
            {addedMessage}
          </div>
        )}

        <div className="product-card__actions">
          {canQuickAdd ? (
            <button type="button" onClick={handleQuickAdd} disabled={adding}>
              {adding ? '...' : 'Thêm'}
            </button>
          ) : (
            <Link to={`/products/${product.id}`} className="product-card__select">Chọn loại</Link>
          )}
          <Link to={`/products/${product.id}`}>Chi tiết</Link>
        </div>
      </div>
    </article>
  );
}

function normalizeThumbnailUrl(thumbnailUrl) {
  if (!thumbnailUrl) {
    return '';
  }

  if (thumbnailUrl.includes('placehold.co') && thumbnailUrl.includes('text=')) {
    return '';
  }

  return thumbnailUrl;
}

export default ProductCard;
