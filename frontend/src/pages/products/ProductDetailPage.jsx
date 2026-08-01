import { useEffect, useMemo, useState } from 'react';
import { Link, useLocation, useNavigate, useParams } from 'react-router-dom';
import BackLink from '../../components/common/BackLink.jsx';
import useAuth from '../../hooks/useAuth.js';
import { sampleProducts } from '../../data/sampleData.js';
import { addCartItem } from '../../services/cartService.js';
import { getProductDetails } from '../../services/productService.js';
import { getProductReviews } from '../../services/reviewService.js';
import {
  formatPrice,
  getProductOriginalPrice,
  getProductPrice,
  getProductStatusLabel,
} from '../../utils/formatters.js';

function ProductDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const location = useLocation();
  const { isAuthenticated, user } = useAuth();
  const [product, setProduct] = useState(null);
  const [selectedVariantId, setSelectedVariantId] = useState('');
  const [selectedImageUrl, setSelectedImageUrl] = useState('');
  const [quantity, setQuantity] = useState(1);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [reviews, setReviews] = useState([]);
  const [reviewNotice, setReviewNotice] = useState('');

  useEffect(() => {
    let active = true;
    setLoading(true);
    setError('');
    setMessage('');

    getProductDetails(id)
      .then((result) => {
        if (active) {
          applyProduct(result);
        }
      })
      .catch(() => {
        if (!active) {
          return;
        }

        const fallback = sampleProducts.find((item) => String(item.id) === String(id)) || sampleProducts[0];
        applyProduct(fallback);
        setError('Backend chua san sang, dang hien thi du lieu mau.');
      })
      .finally(() => {
        if (active) {
          setLoading(false);
        }
      });

    function applyProduct(nextProduct) {
      const firstVariant = nextProduct?.variants?.[0];
      const thumbnail = nextProduct?.thumbnailUrl || nextProduct?.images?.[0]?.imageUrl || '';

      setProduct(nextProduct);
      setSelectedVariantId(String(nextProduct?.defaultVariantId || firstVariant?.id || ''));
      setSelectedImageUrl(thumbnail);
      setQuantity(1);
    }

    return () => {
      active = false;
    };
  }, [id]);

  useEffect(() => {
    let active = true;
    setReviewNotice('');

    getProductReviews(id, { page: 0, size: 6, sort: 'createdAt,desc' })
      .then((result) => {
        if (active) {
          setReviews(result.content || []);
        }
      })
      .catch(() => {
        if (active) {
          setReviews([]);
          setReviewNotice('Chua tai duoc danh gia san pham.');
        }
      });

    return () => {
      active = false;
    };
  }, [id]);

  const selectedVariant = useMemo(() => {
    return product?.variants?.find((variant) => String(variant.id) === String(selectedVariantId)) || null;
  }, [product, selectedVariantId]);

  const images = product?.images?.length
    ? product.images.map((image) => image.imageUrl)
    : product?.thumbnailUrl
      ? [product.thumbnailUrl]
      : [];

  const price = selectedVariant?.discountedPrice || selectedVariant?.price || getProductPrice(product);
  const originalPrice = selectedVariant?.price || getProductOriginalPrice(product);
  const stockQuantity = selectedVariant?.stockQuantity || 0;
  const canAddToCart = Boolean(product?.purchasable && selectedVariant && stockQuantity > 0);

  function increaseQuantity() {
    setQuantity((current) => Math.min(current + 1, Math.max(stockQuantity, 1)));
  }

  function decreaseQuantity() {
    setQuantity((current) => Math.max(current - 1, 1));
  }

  async function handleAddToCart() {
    setMessage('');
    setError('');

    if (!isAuthenticated) {
      navigate('/login', { state: { from: location } });
      return;
    }

    if (!canAddToCart) {
      setError('San pham hoac phan loai nay hien khong the them vao gio.');
      return;
    }

    setSubmitting(true);

    try {
      await addCartItem(user.id, {
        productId: product.id,
        variantId: selectedVariant.id,
        quantity,
      });
      setMessage('Da them san pham vao gio hang.');
    } catch (err) {
      setError(err.message || 'Khong the them san pham vao gio. Vui long thu lai.');
    } finally {
      setSubmitting(false);
    }
  }

  if (loading) {
    return <div className="page-message">Dang tai chi tiet san pham...</div>;
  }

  if (!product) {
    return (
      <div className="empty-state container">
        Khong tim thay san pham.
      </div>
    );
  }

  return (
    <div className="product-detail-page container">
      <BackLink fallback="/products" label="Quay lai san pham" />

      <div className="breadcrumb">
        <Link to="/products">San pham</Link>
        <span>/</span>
        <span>{product.name}</span>
      </div>

      {error && <div className="form-alert form-alert--soft">{error}</div>}
      {message && <div className="form-alert form-alert--success">{message}</div>}

      <section className="product-detail">
        <div className="product-gallery">
          <div className="product-gallery__main">
            {selectedImageUrl ? (
              <img src={selectedImageUrl} alt={product.name} />
            ) : (
              <span>{product.name?.charAt(0) || 'P'}</span>
            )}
          </div>

          <div className="product-gallery__thumbs">
            {images.map((imageUrl) => (
              <button
                type="button"
                key={imageUrl}
                className={selectedImageUrl === imageUrl ? 'is-active' : ''}
                onClick={() => setSelectedImageUrl(imageUrl)}
              >
                <img src={imageUrl} alt={product.name} />
              </button>
            ))}
          </div>
        </div>

        <div className="product-info">
          <span className="product-info__status">{getProductStatusLabel(product)}</span>
          <h1>{product.name}</h1>
          <p className="product-info__rating">
            {(product.averageRating || 0).toFixed(1)} sao | {product.reviewCount || 0} danh gia
          </p>

          <div className="product-info__price">
            {originalPrice > price && <span>{formatPrice(originalPrice)}</span>}
            <strong>{formatPrice(price)}</strong>
          </div>

          <div className="variant-picker">
            <p>Chon phan loai</p>
            <div className="variant-picker__options">
              {(product.variants || []).map((variant) => (
                <button
                  type="button"
                  key={variant.id}
                  className={String(selectedVariantId) === String(variant.id) ? 'is-active' : ''}
                  onClick={() => {
                    setSelectedVariantId(String(variant.id));
                    setQuantity(1);
                  }}
                >
                  {formatVariantName(variant)}
                </button>
              ))}
            </div>
          </div>

          <div className="stock-line">
            Ton kho: <strong>{stockQuantity}</strong>
          </div>

          <div className="quantity-stepper">
            <button type="button" onClick={decreaseQuantity}>-</button>
            <input value={quantity} readOnly aria-label="So luong" />
            <button type="button" onClick={increaseQuantity}>+</button>
          </div>

          <div className="product-detail__actions">
            <button type="button" disabled={!canAddToCart || submitting} onClick={handleAddToCart}>
              {submitting ? 'Dang them...' : 'Them vao gio'}
            </button>
            <Link to="/products">Tiep tuc mua sam</Link>
          </div>
        </div>
      </section>

      <section className="product-reviews">
        <div className="page-title-row">
          <div>
            <p>Danh gia thuc te</p>
            <h2>Khach hang noi gi ve san pham</h2>
          </div>
          <span>{product.reviewCount || reviews.length || 0} danh gia</span>
        </div>

        {reviewNotice && <div className="form-alert form-alert--soft">{reviewNotice}</div>}

        {reviews.length === 0 ? (
          <div className="empty-state">San pham chua co danh gia hien thi.</div>
        ) : (
          <div className="review-list">
            {reviews.map((review) => (
              <article className="review-card" key={review.id}>
                <div className="review-card__user">
                  <img src={review.userAvatar || 'https://placehold.co/48x48?text=U'} alt={review.userFullName || 'User'} />
                  <div>
                    <strong>{review.userFullName || 'Khach hang'}</strong>
                    <span>{'★'.repeat(review.rating)}{'☆'.repeat(Math.max(5 - review.rating, 0))}</span>
                  </div>
                </div>

                <p>{review.comment || 'Khach hang khong de lai binh luan.'}</p>

                {review.mediaAttachments?.length > 0 && (
                  <div className="review-card__media">
                    {review.mediaAttachments.map((media) => (
                      media.mediaType === 'VIDEO' ? (
                        <video key={media.id || media.url} src={media.url} controls />
                      ) : (
                        <img key={media.id || media.url} src={media.url} alt="Review media" />
                      )
                    ))}
                  </div>
                )}

                {review.adminReply && <div className="review-card__reply">Shop: {review.adminReply}</div>}
              </article>
            ))}
          </div>
        )}
      </section>
    </div>
  );
}

function formatVariantName(variant) {
  const attributes = variant.attributes || {};
  const entries = Object.entries(attributes);

  if (entries.length === 0) {
    return variant.type || 'Mac dinh';
  }

  return entries.map(([name, value]) => `${name}: ${value}`).join(', ');
}

export default ProductDetailPage;
