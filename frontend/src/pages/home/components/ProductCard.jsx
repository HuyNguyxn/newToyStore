import { Link } from 'react-router-dom';

function formatPrice(value) {
  return new Intl.NumberFormat('vi-VN').format(value) + 'd';
}

function ProductCard({ product }) {
  return (
    <article className="product-card">
      <div className="product-card__image">
        <span>{product.name.charAt(0)}</span>
        <strong>{product.status}</strong>
      </div>

      <div className="product-card__body">
        <h3>{product.name}</h3>
        <p className="product-card__rating">★★★★★ <span>({product.reviewCount} danh gia)</span></p>
        <p className="product-card__price">
          <span>{formatPrice(product.oldPrice)}</span>
          <strong>{formatPrice(product.price)}</strong>
        </p>

        <div className="product-card__actions">
          <Link to={`/products/${product.id}`}>Xem chi tiet</Link>
          {product.quickAddAvailable ? (
            <button type="button">Them gio</button>
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
