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
  const rating = product.averageRating || product.rating || 0;
  const firstLetter = product.name?.charAt(0) || 'P';

  return (
    <article className="product-card">
      <div className="product-card__image">
        {product.thumbnailUrl ? (
          <img src={product.thumbnailUrl} alt={product.name} />
        ) : (
          <span>{firstLetter}</span>
        )}
        <strong>{statusLabel}</strong>
      </div>

      <div className="product-card__body">
        <h3>{product.name}</h3>
        <p className="product-card__rating">
          ⭐ {rating.toFixed(1)} <span>({product.reviewCount || 0} danh gia)</span>
        </p>
        <p className="product-card__price">
          {showOriginalPrice && <span>{formatPrice(originalPrice)}</span>}
          <strong>{formatPrice(price)}</strong>
        </p>

        <div className="product-card__actions">
          <Link to={`/products/${product.id}`}>Xem chi tiet</Link>
          {product.quickAddAvailable ? (
            <button type="button">Them gio 🛒</button>
          ) : (
            <Link to={`/products/${product.id}`} className="product-card__select">
              Chon loai
            </Link>
          )}
        </div>
      </div>
    </article>
  );
}

export default ProductCard;
