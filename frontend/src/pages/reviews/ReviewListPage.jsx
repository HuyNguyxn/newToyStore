import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import BackLink from '../../components/common/BackLink.jsx';
import { deleteReview, getMyReviews, updateReview } from '../../services/reviewService.js';
import { uploadImage, uploadVideo } from '../../services/uploadService.js';
import { formatDateTime } from '../../utils/formatters.js';

const getReviewStatusBadge = (statusObj) => {
  const code = (typeof statusObj === 'string' ? statusObj : statusObj?.code || statusObj?.name || '').toUpperCase();
  const displayName = statusObj?.displayName;

  if (code.includes('APPROVED') || code.includes('PUBLISHED') || code.includes('ACTIVE')) {
    return { label: displayName || 'Đã duyệt & Hiển thị', bg: '#dcfce7', color: '#15803d', icon: '✅' };
  }
  if (code.includes('PENDING') || code.includes('UNDER_REVIEW')) {
    return { label: displayName || 'Chờ kiểm duyệt', bg: '#fef3c7', color: '#b45309', icon: '⏳' };
  }
  if (code.includes('REJECTED') || code.includes('HIDDEN')) {
    return { label: displayName || 'Bị từ chối / Ẩn', bg: '#fee2e2', color: '#b91c1c', icon: '🚫' };
  }
  return { label: displayName || code || 'Đang cập nhật', bg: '#f1f5f9', color: '#475569', icon: '📌' };
};

const ratingLabels = {
  1: '1/5 Star - Rất không hài lòng 😡',
  2: '2/5 Stars - Không hài lòng 🙁',
  3: '3/5 Stars - Bình thường 😐',
  4: '4/5 Stars - Hài lòng 🙂',
  5: '5/5 Stars - Rất tuyệt vời ⭐⭐⭐⭐⭐',
};

function ReviewListPage() {
  const [reviews, setReviews] = useState([]);
  const [selected, setSelected] = useState(null);
  const [rating, setRating] = useState(5);
  const [hoverRating, setHoverRating] = useState(0);
  const [comment, setComment] = useState('');
  const [images, setImages] = useState([]);
  const [videos, setVideos] = useState([]);

  const [loading, setLoading] = useState(true);
  const [uploading, setUploading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    loadReviews();
  }, []);

  async function loadReviews() {
    setLoading(true);
    setError('');
    try {
      const result = await getMyReviews({ page: 0, size: 50, sort: 'createdAt,desc' });
      const list = result.content || (Array.isArray(result) ? result : []);
      setReviews(list);
    } catch (err) {
      setError(err.message || 'Không thể tải danh sách đánh giá của bạn.');
    } finally {
      setLoading(false);
    }
  }

  function getMediaUrls(review, type) {
    if (!review || !Array.isArray(review.mediaAttachments)) return [];
    return review.mediaAttachments
      .filter((media) => !type || media.mediaType === type || media.type === type)
      .map((media) => media.url || media.mediaUrl)
      .filter(Boolean);
  }

  function selectReview(review) {
    setSelected(review);
    setRating(review.rating || 5);
    setComment(review.comment || '');
    setImages(getMediaUrls(review, 'IMAGE'));
    setVideos(getMediaUrls(review, 'VIDEO'));
    setMessage('');
    setError('');
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
      setError(err.message || 'Upload ảnh/video thất bại.');
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

  async function saveReview(event) {
    event.preventDefault();
    if (!selected) return;
    setError('');
    setMessage('');
    setSaving(true);

    try {
      const result = await updateReview(selected.id, {
        rating: Number(rating),
        comment: comment.trim(),
        imageUrls: images,
        videoUrls: videos,
      });

      setSelected(result);
      setMessage('🎉 Đã cập nhật đánh giá thành công.');
      await loadReviews();
    } catch (err) {
      setError(err.message || 'Cập nhật đánh giá thất bại. Vui lòng thử lại.');
    } finally {
      setSaving(false);
    }
  }

  async function removeReview(id) {
    if (!window.confirm('Bạn có chắc chắn muốn xóa đánh giá này?')) return;
    setError('');
    setMessage('');
    try {
      await deleteReview(id);
      if (selected?.id === id) {
        setSelected(null);
      }
      setMessage('🗑️ Đã xóa đánh giá thành công.');
      await loadReviews();
    } catch (err) {
      setError(err.message || 'Xóa đánh giá thất bại. Vui lòng thử lại.');
    }
  }

  return (
    <div className="container" style={{ padding: '24px 16px', maxWidth: '1200px', margin: '0 auto', fontFamily: 'system-ui, -apple-system, sans-serif' }}>
      <BackLink fallback="/profile" label="Quay lại tài khoản" />

      {/* HERO BANNER */}
      <div
        style={{
          background: 'linear-gradient(135deg, #ffffff 0%, #fff7ed 50%, #ffedd5 100%)',
          borderRadius: '20px',
          padding: '28px 32px',
          color: '#0f172a',
          marginBottom: '24px',
          border: '1px solid #fed7aa',
          boxShadow: '0 10px 25px rgba(234, 88, 12, 0.08)',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          flexWrap: 'wrap',
          gap: '20px',
          marginTop: '12px',
        }}
      >
        <div>
          <div style={{ display: 'inline-flex', alignItems: 'center', gap: '8px', background: '#ffedd5', color: '#ea580c', padding: '4px 12px', borderRadius: '20px', fontSize: '12px', fontWeight: '800', textTransform: 'uppercase', letterSpacing: '0.6px', marginBottom: '10px' }}>
            ⭐ QUẢN LÝ ĐÁNH GIÁ
          </div>
          <h1 style={{ fontSize: '26px', fontWeight: '900', color: '#0f172a', margin: '0 0 6px 0', letterSpacing: '-0.5px' }}>
            Đánh giá sản phẩm của tôi
          </h1>
          <p style={{ margin: 0, color: '#475569', fontSize: '14px', fontWeight: '500' }}>
            Xem lại, chỉnh sửa cảm nhận, cập nhật hình ảnh/video hoặc quản lý các đánh giá của bạn
          </p>
        </div>

        <Link
          to="/reviews/new"
          style={{
            padding: '12px 22px',
            fontSize: '14px',
            fontWeight: '800',
            color: '#ffffff',
            background: 'linear-gradient(135deg, #ea580c 0%, #c2410c 100%)',
            textDecoration: 'none',
            borderRadius: '14px',
            boxShadow: '0 6px 20px rgba(234, 88, 12, 0.25)',
            display: 'inline-flex',
            alignItems: 'center',
            gap: '8px',
            transition: 'all 0.2s ease',
          }}
        >
          ✍️ Viết đánh giá mới
        </Link>
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

      {/* MAIN CONTENT GRID */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(340px, 1fr))', gap: '24px', alignItems: 'start' }}>
        
        {/* LEFT COLUMN: REVIEW CARDS LIST */}
        <div style={{ background: '#ffffff', borderRadius: '20px', padding: '20px', border: '1px solid #e2e8f0', boxShadow: '0 4px 20px rgba(0,0,0,0.03)' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px', paddingBottom: '12px', borderBottom: '1px solid #f1f5f9' }}>
            <h2 style={{ fontSize: '17px', fontWeight: '800', color: '#0f172a', margin: 0, display: 'flex', alignItems: 'center', gap: '8px' }}>
              💬 Tất cả đánh giá ({reviews.length})
            </h2>
            <button
              onClick={loadReviews}
              style={{ background: '#f8fafc', border: '1px solid #cbd5e1', borderRadius: '8px', padding: '4px 10px', fontSize: '12px', fontWeight: '700', color: '#475569', cursor: 'pointer' }}
            >
              🔄 Tải lại
            </button>
          </div>

          {loading ? (
            <div style={{ textAlign: 'center', padding: '40px 0', color: '#94a3b8' }}>
              Đang tải danh sách đánh giá...
            </div>
          ) : reviews.length === 0 ? (
            <div style={{ textAlign: 'center', padding: '48px 20px', background: '#f8fafc', borderRadius: '16px', border: '1px dashed #cbd5e1' }}>
              <span style={{ fontSize: '40px', display: 'block', marginBottom: '8px' }}>⭐</span>
              <h3 style={{ fontSize: '16px', fontWeight: '800', color: '#334155', margin: '0 0 4px 0' }}>Chưa có đánh giá nào</h3>
              <p style={{ fontSize: '13px', color: '#64748b', margin: '0 0 16px 0' }}>Hãy chia sẻ ý kiến đầu tiên của bạn về các sản phẩm đã mua.</p>
              <Link to="/reviews/new" style={{ display: 'inline-block', background: '#ea580c', color: '#fff', padding: '8px 16px', borderRadius: '10px', fontSize: '13px', fontWeight: '800', textDecoration: 'none' }}>
                ✍️ Viết đánh giá ngay
              </Link>
            </div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
              {reviews.map((review) => {
                const isSelected = selected?.id === review.id;
                const statusBadge = getReviewStatusBadge(review.status);
                const starsCount = Number(review.rating || 5);

                return (
                  <div
                    key={review.id}
                    onClick={() => selectReview(review)}
                    style={{
                      padding: '16px',
                      borderRadius: '16px',
                      background: isSelected ? 'linear-gradient(135deg, #fff7ed 0%, #ffedd5 100%)' : '#ffffff',
                      border: isSelected ? '2px solid #ea580c' : '1px solid #e2e8f0',
                      boxShadow: isSelected ? '0 6px 16px rgba(234, 88, 12, 0.15)' : '0 2px 6px rgba(0,0,0,0.02)',
                      cursor: 'pointer',
                      transition: 'all 0.2s ease',
                    }}
                  >
                    {/* TOP HEADER */}
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '8px' }}>
                      <h3 style={{ fontSize: '14.5px', fontWeight: '800', color: '#0f172a', margin: 0, lineHeight: '1.3' }}>
                        {review.productName || `Sản phẩm #${review.productId}`}
                      </h3>
                      <span
                        style={{
                          background: statusBadge.bg,
                          color: statusBadge.color,
                          padding: '3px 8px',
                          borderRadius: '12px',
                          fontSize: '11px',
                          fontWeight: '800',
                          flexShrink: 0,
                        }}
                      >
                        {statusBadge.icon} {statusBadge.label}
                      </span>
                    </div>

                    {/* RATING STARS */}
                    <div style={{ color: '#ea580c', fontSize: '15px', marginBottom: '8px' }}>
                      {'⭐'.repeat(starsCount)}
                      <span style={{ fontSize: '12.5px', fontWeight: '800', marginLeft: '6px', color: '#ea580c' }}>
                        {starsCount}/5
                      </span>
                    </div>

                    {/* COMMENT SNIPPET */}
                    {review.comment && (
                      <p style={{ margin: '0 0 10px 0', fontSize: '13px', color: '#475569', lineHeight: '1.4', overflow: 'hidden', textOverflow: 'ellipsis', display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical' }}>
                        "{review.comment}"
                      </p>
                    )}

                    {/* FOOTER ACTIONS */}
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', fontSize: '12px', color: '#94a3b8', paddingTop: '8px', borderTop: '1px solid rgba(0,0,0,0.04)' }}>
                      <span>🕒 {formatDateTime(review.createdAt)}</span>
                      <div style={{ display: 'flex', gap: '8px' }}>
                        <button
                          type="button"
                          onClick={(e) => { e.stopPropagation(); selectReview(review); }}
                          style={{ background: '#eff6ff', border: '1px solid #bfdbfe', color: '#2563eb', borderRadius: '6px', padding: '3px 8px', fontSize: '11.5px', fontWeight: '800', cursor: 'pointer' }}
                        >
                          ✏️ Sửa
                        </button>
                        <button
                          type="button"
                          onClick={(e) => { e.stopPropagation(); removeReview(review.id); }}
                          style={{ background: '#fef2f2', border: '1px solid #fecaca', color: '#dc2626', borderRadius: '6px', padding: '3px 8px', fontSize: '11.5px', fontWeight: '800', cursor: 'pointer' }}
                        >
                          🗑️ Xóa
                        </button>
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>

        {/* RIGHT COLUMN: REVIEW EDITOR & ADMIN REPLY PANEL */}
        <div style={{ background: '#ffffff', borderRadius: '20px', padding: '24px', border: '1px solid #e2e8f0', boxShadow: '0 4px 20px rgba(0,0,0,0.03)', position: 'sticky', top: '90px' }}>
          {!selected ? (
            <div style={{ textAlign: 'center', padding: '60px 20px' }}>
              <span style={{ fontSize: '48px', display: 'block', marginBottom: '12px' }}>⭐</span>
              <h3 style={{ fontSize: '17px', fontWeight: '800', color: '#1e293b', margin: '0 0 6px 0' }}>
                Chọn đánh giá để xem chi tiết
              </h3>
              <p style={{ fontSize: '13.5px', color: '#64748b', margin: 0, maxWidth: '280px', marginLeft: 'auto', marginRight: 'auto' }}>
                Bấm vào bất kỳ đánh giá nào ở danh sách bên trái để chỉnh sửa nội dung hoặc xem phản hồi từ Shop.
              </p>
            </div>
          ) : (
            <form onSubmit={saveReview} style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
              
              {/* HEADER */}
              <div style={{ paddingBottom: '14px', borderBottom: '1px solid #f1f5f9', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <div>
                  <span style={{ fontSize: '11.5px', fontWeight: '800', color: '#ea580c', textTransform: 'uppercase' }}>CHỈNH SỬA ĐÁNH GIÁ</span>
                  <h3 style={{ fontSize: '16.5px', fontWeight: '900', color: '#0f172a', margin: '2px 0 0 0' }}>
                    {selected.productName || `Sản phẩm #${selected.productId}`}
                  </h3>
                </div>
                {selected.status && (() => {
                  const badge = getReviewStatusBadge(selected.status);
                  return (
                    <span style={{ background: badge.bg, color: badge.color, padding: '4px 10px', borderRadius: '12px', fontSize: '11.5px', fontWeight: '800' }}>
                      {badge.icon} {badge.label}
                    </span>
                  );
                })()}
              </div>

              {/* ADMIN REPLY BOX (IF PRESENT) */}
              {selected.adminReply && (
                <div style={{ background: '#eff6ff', border: '1px solid #bfdbfe', padding: '14px', borderRadius: '14px' }}>
                  <span style={{ fontSize: '12px', fontWeight: '800', color: '#1d4ed8', display: 'flex', alignItems: 'center', gap: '6px' }}>
                    🛡️ Phản hồi từ Shop:
                  </span>
                  <p style={{ margin: '4px 0 0 0', fontSize: '13px', color: '#1e3a8a', fontWeight: '600', lineHeight: '1.4' }}>
                    "{selected.adminReply}"
                  </p>
                </div>
              )}

              {/* INTERACTIVE RATING PICKER */}
              <div>
                <label style={{ fontSize: '13.5px', fontWeight: '800', color: '#0f172a', display: 'block', marginBottom: '6px' }}>
                  Mức độ hài lòng
                </label>
                <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
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
                          fontSize: '32px',
                          cursor: 'pointer',
                          filter: active ? 'drop-shadow(0 2px 6px rgba(234, 88, 12, 0.4))' : 'grayscale(1)',
                          opacity: active ? 1 : 0.35,
                          transform: active ? 'scale(1.1)' : 'scale(1)',
                          transition: 'all 0.15s ease',
                          padding: '0 2px',
                        }}
                      >
                        ⭐
                      </button>
                    );
                  })}
                </div>
                <div style={{ marginTop: '4px', fontSize: '12.5px', fontWeight: '800', color: '#ea580c' }}>
                  {ratingLabels[hoverRating || rating]}
                </div>
              </div>

              {/* COMMENT TEXTAREA */}
              <div>
                <label style={{ fontSize: '13.5px', fontWeight: '800', color: '#0f172a', display: 'block', marginBottom: '6px' }}>
                  Nội dung đánh giá
                </label>
                <textarea
                  rows="4"
                  value={comment}
                  onChange={(e) => setComment(e.target.value)}
                  placeholder="Nhập cảm nhận của bạn..."
                  style={{
                    width: '100%',
                    padding: '12px 14px',
                    fontSize: '13.5px',
                    fontWeight: '500',
                    color: '#0f172a',
                    borderRadius: '12px',
                    border: '1px solid #cbd5e1',
                    fontFamily: 'inherit',
                    lineHeight: '1.4',
                    boxSizing: 'border-box',
                  }}
                  required
                />
              </div>

              {/* MEDIA UPLOAD SECTION */}
              <div>
                <label style={{ fontSize: '13.5px', fontWeight: '800', color: '#0f172a', display: 'block', marginBottom: '6px' }}>
                  Hình ảnh & Video đính kèm
                </label>

                <div style={{ display: 'flex', gap: '10px', flexWrap: 'wrap', marginBottom: '10px' }}>
                  <label style={{ cursor: uploading ? 'not-allowed' : 'pointer' }}>
                    <input type="file" accept="image/*" onChange={(e) => handleMediaUpload(e, 'image')} disabled={uploading} style={{ display: 'none' }} />
                    <div style={{ padding: '8px 14px', borderRadius: '10px', background: '#fff7ed', border: '1px solid #fed7aa', color: '#ea580c', fontSize: '12.5px', fontWeight: '800', display: 'inline-flex', alignItems: 'center', gap: '4px' }}>
                      📸 Thêm ảnh
                    </div>
                  </label>

                  <label style={{ cursor: uploading ? 'not-allowed' : 'pointer' }}>
                    <input type="file" accept="video/*" onChange={(e) => handleMediaUpload(e, 'video')} disabled={uploading} style={{ display: 'none' }} />
                    <div style={{ padding: '8px 14px', borderRadius: '10px', background: '#eff6ff', border: '1px solid #bfdbfe', color: '#2563eb', fontSize: '12.5px', fontWeight: '800', display: 'inline-flex', alignItems: 'center', gap: '4px' }}>
                      🎥 Thêm video
                    </div>
                  </label>
                </div>

                {uploading && <div style={{ fontSize: '12px', color: '#ea580c', fontWeight: '700', marginBottom: '6px' }}>⏳ Đang tải tệp...</div>}

                {/* THUMBNAILS PREVIEW GRID */}
                {(images.length > 0 || videos.length > 0) && (
                  <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap', marginTop: '6px' }}>
                    {images.map((imgUrl, idx) => (
                      <div key={idx} style={{ position: 'relative', width: '70px', height: '70px', borderRadius: '10px', overflow: 'hidden', border: '1px solid #cbd5e1' }}>
                        <img src={imgUrl} alt="" style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                        <button type="button" onClick={() => removeImage(idx)} style={{ position: 'absolute', top: '3px', right: '3px', background: 'rgba(0,0,0,0.6)', color: '#fff', border: 'none', borderRadius: '50%', width: '18px', height: '18px', cursor: 'pointer', fontSize: '10px', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>✕</button>
                      </div>
                    ))}
                    {videos.map((vidUrl, idx) => (
                      <div key={idx} style={{ position: 'relative', width: '70px', height: '70px', borderRadius: '10px', overflow: 'hidden', border: '1px solid #bfdbfe', background: '#000' }}>
                        <video src={vidUrl} style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                        <button type="button" onClick={() => removeVideo(idx)} style={{ position: 'absolute', top: '3px', right: '3px', background: 'rgba(0,0,0,0.6)', color: '#fff', border: 'none', borderRadius: '50%', width: '18px', height: '18px', cursor: 'pointer', fontSize: '10px', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>✕</button>
                      </div>
                    ))}
                  </div>
                )}
              </div>

              {/* SAVE BUTTON */}
              <button
                type="submit"
                disabled={saving || uploading}
                style={{
                  padding: '14px 20px',
                  fontSize: '15px',
                  fontWeight: '900',
                  color: '#ffffff',
                  background: 'linear-gradient(135deg, #ea580c 0%, #c2410c 100%)',
                  border: 'none',
                  borderRadius: '12px',
                  cursor: saving || uploading ? 'not-allowed' : 'pointer',
                  boxShadow: '0 6px 18px rgba(234, 88, 12, 0.25)',
                  transition: 'all 0.2s ease',
                  marginTop: '4px',
                }}
              >
                {saving ? '⏳ Đang lưu...' : '💾 Cập nhật đánh giá'}
              </button>
            </form>
          )}
        </div>
      </div>
    </div>
  );
}

export default ReviewListPage;
