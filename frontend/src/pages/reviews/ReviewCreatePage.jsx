import { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { getMyOrders } from '../../services/orderService.js';
import { createReview } from '../../services/reviewService.js';
import { uploadImage, uploadVideo } from '../../services/uploadService.js';
import { formatPrice } from '../../utils/formatters.js';

const ratingLabels = {
  1: '1/5 Star - Rất không hài lòng 😡',
  2: '2/5 Stars - Không hài lòng 🙁',
  3: '3/5 Stars - Bình thường 😐',
  4: '4/5 Stars - Hài lòng 🙂',
  5: '5/5 Stars - Rất tuyệt vời ⭐⭐⭐⭐⭐',
};

function ReviewCreatePage() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const urlItemId = searchParams.get('orderItemId') || '';

  const [availableItems, setAvailableItems] = useState([]);
  const [selectedItem, setSelectedItem] = useState(null);
  const [orderItemId, setOrderItemId] = useState(urlItemId);
  const [rating, setRating] = useState(5);
  const [hoverRating, setHoverRating] = useState(0);
  const [comment, setComment] = useState('');
  const [images, setImages] = useState([]);
  const [videos, setVideos] = useState([]);

  const [loadingOrders, setLoadingOrders] = useState(true);
  const [uploading, setUploading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    loadCompletedOrderItems();
  }, []);

  async function loadCompletedOrderItems() {
    setLoadingOrders(true);
    try {
      const res = await getMyOrders({ page: 0, size: 50 });
      const orders = res.content || (Array.isArray(res) ? res : []);
      
      const items = [];
      orders.forEach((o) => {
        const status = String(o.status?.code || o.status?.name || o.status || '').toUpperCase();
        if (status.includes('COMPLETED') || status.includes('DELIVERED') || status.includes('SHIPPED') || status.includes('PAID') || status.includes('SUCCESS')) {
          if (Array.isArray(o.items)) {
            o.items.forEach((it) => {
              items.push({
                ...it,
                id: it.id || it.orderItemId,
                productName: it.productName || it.name || `Sản phẩm #${it.productId}`,
                productImage: it.productImage || it.imageUrl || it.thumbnail,
                orderCode: `#DH${o.id}`,
                orderId: o.id,
                orderDate: o.createdAt,
                price: it.price || it.unitPrice || 0,
              });
            });
          }
        }
      });

      setAvailableItems(items);

      if (urlItemId) {
        const found = items.find((it) => String(it.id) === String(urlItemId));
        if (found) {
          setSelectedItem(found);
          setOrderItemId(String(found.id));
        }
      } else if (items.length > 0) {
        setSelectedItem(items[0]);
        setOrderItemId(String(items[0].id));
      }
    } catch (err) {
      // Fallback
    } finally {
      setLoadingOrders(false);
    }
  }

  function handleSelectItem(itemId) {
    setOrderItemId(itemId);
    const found = availableItems.find((it) => String(it.id || it.orderItemId) === String(itemId));
    setSelectedItem(found || null);
  }

  async function handleMediaUpload(event, type) {
    const file = event.target.files?.[0];
    if (!file) return;
    setUploading(true);
    setError('');

    try {
      const result = type === 'video' ? await uploadVideo(file, 'reviews') : await uploadImage(file, 'reviews');
      const url = result.secureUrl || result.url;
      if (type === 'video') {
        setVideos((prev) => [...prev, url]);
      } else {
        setImages((prev) => [...prev, url]);
      }
    } catch (err) {
      setError(err.message || 'Upload ảnh/video thất bại. Vui lòng thử lại.');
    } finally {
      setUploading(false);
      event.target.value = '';
    }
  }

  function removeImage(index) {
    setImages((prev) => prev.filter((_, i) => i !== index));
  }

  function removeVideo(index) {
    setVideos((prev) => prev.filter((_, i) => i !== index));
  }

  async function submitReview(event) {
    event.preventDefault();
    setError('');
    setMessage('');

    const targetId = orderItemId || selectedItem?.id;
    if (!targetId) {
      setError('Vui lòng chọn sản phẩm cần đánh giá.');
      return;
    }

    if (!comment.trim()) {
      setError('Vui lòng nhập nội dung đánh giá sản phẩm.');
      return;
    }

    setSubmitting(true);
    try {
      await createReview({
        orderItemId: Number(targetId),
        rating: Number(rating),
        comment: comment.trim(),
        imageUrls: images,
        videoUrls: videos,
      });

      setMessage('🎉 Cảm ơn bạn! Đã gửi đánh giá sản phẩm thành công.');
      setTimeout(() => {
        navigate('/reviews/me');
      }, 1500);
    } catch (err) {
      setError(err.message || 'Gửi đánh giá thất bại. Vui lòng thử lại.');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="container" style={{ padding: '24px 16px', maxWidth: '800px', margin: '0 auto', fontFamily: 'system-ui, -apple-system, sans-serif' }}>
      {/* HERO BANNER */}
      <div
        style={{
          background: 'linear-gradient(135deg, #ffffff 0%, #fff7ed 50%, #ffedd5 100%)',
          borderRadius: '20px',
          padding: '24px 28px',
          color: '#0f172a',
          marginBottom: '24px',
          border: '1px solid #fed7aa',
          boxShadow: '0 10px 25px rgba(234, 88, 12, 0.08)',
        }}
      >
        <div style={{ display: 'inline-flex', alignItems: 'center', gap: '8px', background: '#ffedd5', color: '#ea580c', padding: '4px 12px', borderRadius: '20px', fontSize: '12px', fontWeight: '800', textTransform: 'uppercase', letterSpacing: '0.6px', marginBottom: '8px' }}>
          ✍️ ĐÁNH GIÁ SẢN PHẨM
        </div>
        <h1 style={{ fontSize: '24px', fontWeight: '900', color: '#0f172a', margin: '0 0 4px 0' }}>
          Viết đánh giá trải nghiệm
        </h1>
        <p style={{ margin: 0, color: '#475569', fontSize: '13.5px', fontWeight: '500' }}>
          Chia sẻ ý kiến chân thực của bạn để giúp các bố mẹ khác mua sắm thông minh hơn
        </p>
      </div>

      {error && (
        <div style={{ background: '#fef2f2', border: '1px solid #fecaca', color: '#dc2626', padding: '14px 18px', borderRadius: '12px', marginBottom: '20px', fontWeight: '600', fontSize: '14px' }}>
          ⚠️ {error}
        </div>
      )}

      {message && (
        <div style={{ background: '#f0fdf4', border: '1px solid #bbf7d0', color: '#15803d', padding: '14px 18px', borderRadius: '12px', marginBottom: '20px', fontWeight: '700', fontSize: '14px' }}>
          {message}
        </div>
      )}

      <form onSubmit={submitReview} style={{ background: '#ffffff', borderRadius: '20px', padding: '28px', border: '1px solid #e2e8f0', boxShadow: '0 4px 20px rgba(0,0,0,0.03)', display: 'flex', flexDirection: 'column', gap: '24px' }}>
        
        {/* STEP 1: CHỌN SẢN PHẨM CẦN ĐÁNH GIÁ */}
        <div>
          <label style={{ fontSize: '14px', fontWeight: '800', color: '#0f172a', display: 'block', marginBottom: '8px' }}>
            1. Chọn sản phẩm đã mua để đánh giá <span style={{ color: '#ea580c' }}>*</span>
          </label>

          {loadingOrders ? (
            <div style={{ padding: '14px', background: '#f8fafc', borderRadius: '12px', color: '#94a3b8', fontSize: '13px' }}>
              ⏳ Đang tải danh sách sản phẩm bạn đã mua...
            </div>
          ) : availableItems.length > 0 ? (
            <select
              value={orderItemId}
              onChange={(e) => handleSelectItem(e.target.value)}
              style={{
                width: '100%',
                padding: '12px 16px',
                fontSize: '14px',
                fontWeight: '700',
                color: '#0f172a',
                borderRadius: '12px',
                border: '2px solid #fed7aa',
                background: '#ffffff',
                cursor: 'pointer',
                outline: 'none',
                boxShadow: '0 2px 8px rgba(234,88,12,0.06)',
              }}
            >
              {availableItems.map((it) => (
                <option key={it.id || it.orderItemId} value={it.id || it.orderItemId}>
                  📦 {it.productName} ({it.orderCode} - {formatPrice(it.price)})
                </option>
              ))}
            </select>
          ) : (
            <div style={{ padding: '16px', background: '#fff7ed', border: '1px solid #fed7aa', borderRadius: '12px', color: '#c2410c', fontSize: '13.5px', fontWeight: '600' }}>
              🛍️ Bạn chưa có sản phẩm nào trong danh sách đơn hàng đã mua để đánh giá.
            </div>
          )}

          {/* SELECTED ITEM PREVIEW CARD */}
          {selectedItem && (
            <div style={{ marginTop: '14px', padding: '16px', background: '#f8fafc', borderRadius: '14px', border: '1px solid #e2e8f0', display: 'flex', alignItems: 'center', gap: '16px', boxShadow: '0 2px 8px rgba(0,0,0,0.02)' }}>
              {selectedItem.productImage || selectedItem.imageUrl ? (
                <img src={selectedItem.productImage || selectedItem.imageUrl} alt="" style={{ width: '56px', height: '56px', borderRadius: '12px', objectFit: 'cover', border: '1px solid #cbd5e1' }} />
              ) : (
                <div style={{ width: '56px', height: '56px', borderRadius: '12px', background: '#fff7ed', border: '1px solid #fed7aa', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '26px' }}>🎁</div>
              )}
              <div>
                <h4 style={{ margin: '0 0 4px 0', fontSize: '15px', fontWeight: '800', color: '#0f172a' }}>
                  {selectedItem.productName || `Sản phẩm #${selectedItem.productId}`}
                </h4>
                <div style={{ fontSize: '12.5px', color: '#64748b', fontWeight: '600', display: 'flex', gap: '12px' }}>
                  <span>Mã đơn hàng: <strong style={{ color: '#ea580c' }}>{selectedItem.orderCode}</strong></span>
                  <span>Đơn giá: <strong style={{ color: '#16a34a' }}>{formatPrice(selectedItem.price)}</strong></span>
                </div>
              </div>
            </div>
          )}
        </div>

        {/* STEP 2: ĐÁNH GIÁ SỐ SAO (INTERACTIVE STAR RATING) */}
        <div>
          <label style={{ fontSize: '14px', fontWeight: '800', color: '#0f172a', display: 'block', marginBottom: '8px' }}>
            2. Mức độ hài lòng của bạn <span style={{ color: '#ea580c' }}>*</span>
          </label>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', flexWrap: 'wrap' }}>
            {[1, 2, 3, 4, 5].map((star) => {
              const active = star <= (hoverRating || rating);
              return (
                <button
                  key={star}
                  type="button"
                  onClick={() => setRating(star)}
                  onMouseEnter={() => setHoverRating(star)}
                  onMouseLeave={() => setHoverRating(0)}
                  style={{
                    background: 'none',
                    border: 'none',
                    fontSize: '36px',
                    cursor: 'pointer',
                    filter: active ? 'drop-shadow(0 2px 6px rgba(234, 88, 12, 0.4))' : 'grayscale(1)',
                    opacity: active ? 1 : 0.35,
                    transform: active ? 'scale(1.15)' : 'scale(1)',
                    transition: 'all 0.15s ease',
                    padding: '0 4px',
                  }}
                >
                  ⭐
                </button>
              );
            })}
          </div>
          <div style={{ marginTop: '8px', fontSize: '13.5px', fontWeight: '800', color: '#ea580c' }}>
            {ratingLabels[hoverRating || rating]}
          </div>
        </div>

        {/* STEP 3: NỘI DUNG ĐÁNH GIÁ (COMMENT) */}
        <div>
          <label style={{ fontSize: '14px', fontWeight: '800', color: '#0f172a', display: 'block', marginBottom: '8px' }}>
            3. Viết cảm nhận chi tiết <span style={{ color: '#ea580c' }}>*</span>
          </label>
          <textarea
            rows="5"
            value={comment}
            onChange={(e) => setComment(e.target.value)}
            placeholder="Hãy chia sẻ thêm thông tin về chất lượng đồ chơi, thiết kế, độ an toàn cho bé hoặc trải nghiệm giao hàng của shop..."
            style={{
              width: '100%',
              padding: '14px 16px',
              fontSize: '14px',
              fontWeight: '500',
              color: '#0f172a',
              borderRadius: '14px',
              border: '1px solid #cbd5e1',
              fontFamily: 'inherit',
              lineHeight: '1.5',
              boxSizing: 'border-box',
            }}
            required
          />
        </div>

        {/* STEP 4: UPLOAD HÌNH ẢNH VÀ VIDEO */}
        <div>
          <label style={{ fontSize: '14px', fontWeight: '800', color: '#0f172a', display: 'block', marginBottom: '8px' }}>
            4. Thêm hình ảnh & video thực tế (tùy chọn)
          </label>
          
          <div style={{ display: 'flex', gap: '12px', flexWrap: 'wrap', marginBottom: '12px' }}>
            {/* UPLOAD IMAGE BUTTON */}
            <label style={{ cursor: uploading ? 'not-allowed' : 'pointer' }}>
              <input
                type="file"
                accept="image/*"
                onChange={(e) => handleMediaUpload(e, 'image')}
                disabled={uploading}
                style={{ display: 'none' }}
              />
              <div style={{ padding: '10px 18px', borderRadius: '12px', background: '#fff7ed', border: '1px solid #fed7aa', color: '#ea580c', fontSize: '13px', fontWeight: '800', display: 'inline-flex', alignItems: 'center', gap: '6px', transition: 'all 0.15s' }}>
                📸 Thêm ảnh thực tế
              </div>
            </label>

            {/* UPLOAD VIDEO BUTTON */}
            <label style={{ cursor: uploading ? 'not-allowed' : 'pointer' }}>
              <input
                type="file"
                accept="video/*"
                onChange={(e) => handleMediaUpload(e, 'video')}
                disabled={uploading}
                style={{ display: 'none' }}
              />
              <div style={{ padding: '10px 18px', borderRadius: '12px', background: '#eff6ff', border: '1px solid #bfdbfe', color: '#2563eb', fontSize: '13px', fontWeight: '800', display: 'inline-flex', alignItems: 'center', gap: '6px', transition: 'all 0.15s' }}>
                🎥 Thêm video thực tế
              </div>
            </label>
          </div>

          {uploading && <div style={{ fontSize: '12.5px', color: '#ea580c', fontWeight: '700', marginBottom: '8px' }}>⏳ Đang tải tệp lên hệ thống...</div>}

          {/* MEDIA THUMBNAILS PREVIEW GRID */}
          {(images.length > 0 || videos.length > 0) && (
            <div style={{ display: 'flex', gap: '12px', flexWrap: 'wrap', marginTop: '8px' }}>
              {images.map((imgUrl, idx) => (
                <div key={idx} style={{ position: 'relative', width: '80px', height: '80px', borderRadius: '12px', overflow: 'hidden', border: '1px solid #cbd5e1' }}>
                  <img src={imgUrl} alt="" style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                  <button
                    type="button"
                    onClick={() => removeImage(idx)}
                    style={{ position: 'absolute', top: '4px', right: '4px', background: 'rgba(0,0,0,0.6)', color: '#fff', border: 'none', borderRadius: '50%', width: '20px', height: '20px', cursor: 'pointer', fontSize: '11px', display: 'flex', alignItems: 'center', justifyContent: 'center' }}
                  >
                    ✕
                  </button>
                </div>
              ))}
              {videos.map((vidUrl, idx) => (
                <div key={idx} style={{ position: 'relative', width: '80px', height: '80px', borderRadius: '12px', overflow: 'hidden', border: '1px solid #bfdbfe', background: '#000' }}>
                  <video src={vidUrl} style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                  <button
                    type="button"
                    onClick={() => removeVideo(idx)}
                    style={{ position: 'absolute', top: '4px', right: '4px', background: 'rgba(0,0,0,0.6)', color: '#fff', border: 'none', borderRadius: '50%', width: '20px', height: '20px', cursor: 'pointer', fontSize: '11px', display: 'flex', alignItems: 'center', justifyContent: 'center' }}
                  >
                    ✕
                  </button>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* SUBMIT BUTTON */}
        <button
          type="submit"
          disabled={submitting || uploading}
          style={{
            padding: '16px 24px',
            fontSize: '16px',
            fontWeight: '900',
            color: '#ffffff',
            background: 'linear-gradient(135deg, #ea580c 0%, #c2410c 100%)',
            border: 'none',
            borderRadius: '14px',
            cursor: submitting || uploading ? 'not-allowed' : 'pointer',
            boxShadow: '0 6px 20px rgba(234, 88, 12, 0.3)',
            transition: 'all 0.2s ease',
            marginTop: '8px',
          }}
        >
          {submitting ? '⏳ Đang gửi đánh giá...' : '🚀 Gửi đánh giá ngay'}
        </button>
      </form>
    </div>
  );
}

export default ReviewCreatePage;
