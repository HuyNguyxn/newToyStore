import { Link } from 'react-router-dom';
import {
  formatPrice,
  getProductOriginalPrice,
  getProductPrice,
  getProductStatusLabel,
} from '../../../utils/formatters.js';

function ProductCard({ product }) {
  const price = getProductPrice(product);
  const originalPrice = getProductOriginalPrice(product);
  const showOriginalPrice = originalPrice > price;
  const statusLabel = getProductStatusLabel(product);
  const rating = Number(product.averageRating || product.rating || 0);
  const firstLetter = product.name?.charAt(0) || 'P';
  const thumbnailUrl = normalizeThumbnailUrl(product.thumbnailUrl);
  const canQuickAdd = Boolean(product.quickAddAvailable);

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

        <div className="product-card__actions">
          {canQuickAdd ? (
            <button type="button">Thêm</button>
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
