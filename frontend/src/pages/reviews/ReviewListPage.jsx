import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { deleteReview, getMyReviews, updateReview } from '../../services/reviewService.js';
import { uploadImage, uploadVideo } from '../../services/uploadService.js';
import { formatDateTime } from '../../utils/formatters.js';

function ReviewListPage() {
  const [reviews, setReviews] = useState([]);
  const [selected, setSelected] = useState(null);
  const [form, setForm] = useState({ rating: '5', comment: '', imageUrls: '', videoUrls: '' });
  const [uploading, setUploading] = useState(false);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  useEffect(() => { loadReviews(); }, []);

  async function loadReviews() {
    try {
      const result = await getMyReviews({ page: 0, size: 20, sort: 'createdAt,desc' });
      setReviews(result.content || []);
    } catch (err) {
      setError(err.message || 'Khong the tai danh gia cua toi.');
    }
  }

  function mediaUrls(review, type) {
    return (review.mediaAttachments || []).filter((media) => !type || media.mediaType === type || media.type === type).map((media) => media.url || media.mediaUrl).filter(Boolean);
  }

  function selectReview(review) {
    setSelected(review);
    setForm({
      rating: String(review.rating || 5),
      comment: review.comment || '',
      imageUrls: mediaUrls(review, 'IMAGE').join(','),
      videoUrls: mediaUrls(review, 'VIDEO').join(','),
    });
  }

  async function uploadMedia(event, type) {
    const file = event.target.files?.[0];
    if (!file) return;
    setUploading(true);
    setError('');
    try {
      const result = type === 'video' ? await uploadVideo(file, 'reviews') : await uploadImage(file, 'reviews');
      const url = result.secureUrl || result.url;
      const field = type === 'video' ? 'videoUrls' : 'imageUrls';
      setForm((current) => ({ ...current, [field]: [current[field], url].filter(Boolean).join(',') }));
    } catch (err) {
      setError(err.message || 'Upload media review that bai.');
    } finally {
      setUploading(false);
    }
  }

  async function saveReview(event) {
    event.preventDefault();
    setError('');
    setMessage('');
    try {
      const result = await updateReview(selected.id, {
        rating: Number(form.rating),
        comment: form.comment || null,
        imageUrls: form.imageUrls.split(',').map((url) => url.trim()).filter(Boolean),
        videoUrls: form.videoUrls.split(',').map((url) => url.trim()).filter(Boolean),
      });
      setSelected(result);
      setMessage('Da cap nhat review.');
      await loadReviews();
    } catch (err) {
      setError(err.message || 'Cap nhat review that bai.');
    }
  }

  async function removeReview(id) {
    setError('');
    setMessage('');
    try {
      await deleteReview(id);
      setSelected(null);
      setMessage('Da xoa review.');
      await loadReviews();
    } catch (err) {
      setError(err.message || 'Xoa review that bai.');
    }
  }

  return (
    <section className="container profile-page">
      <div className="admin-resource__hero"><div><p>Reviews</p><h2>Danh gia cua toi</h2><span>Xem lai, sua noi dung, cap nhat hinh anh/video hoac xoa review.</span></div><Link className="login-link" to="/reviews/new">Viet danh gia moi</Link></div>
      {error && <div className="form-alert">{error}</div>}{message && <div className="form-alert form-alert--success">{message}</div>}
      <div className="admin-crud-grid">
        <div className="admin-resource-table">
          <div className="admin-resource-table__head" style={{ gridTemplateColumns: '80px 1fr 100px 130px 170px 150px' }}><span>ID</span><span>Product</span><span>Rating</span><span>Status</span><span>Created</span><span>Actions</span></div>
          {reviews.map((review) => <div className="admin-resource-table__row" style={{ gridTemplateColumns: '80px 1fr 100px 130px 170px 150px' }} key={review.id}><span>{review.id}</span><span>{review.productName || `Product #${review.productId}`}</span><span>{review.rating} sao</span><span>{review.status?.code || review.status}</span><span>{formatDateTime(review.createdAt)}</span><span className="admin-resource-table__actions"><button type="button" onClick={() => selectReview(review)}>Edit</button><button type="button" className="is-danger" onClick={() => removeReview(review.id)}>Delete</button></span></div>)}
        </div>

        <aside className="admin-api-console">
          <div className="admin-panel__heading"><div><p>Selected</p><h2>{selected ? `Review #${selected.id}` : 'Chon review'}</h2></div></div>
          {selected && (
            <form className="admin-line-items" onSubmit={saveReview}>
              <div className="admin-detail-summary">
                <p><strong>Product:</strong> {selected.productName || selected.productId}</p>
                <p><strong>Variant:</strong> {selected.variantAttributesSnapshot || 'Default'}</p>
                <p><strong>Admin reply:</strong> {selected.adminReply || '-'}</p>
              </div>
              <label>Rating<input type="number" min="1" max="5" value={form.rating} onChange={(e) => setForm((current) => ({ ...current, rating: e.target.value }))} /></label>
              <label>Comment<textarea rows="5" value={form.comment} onChange={(e) => setForm((current) => ({ ...current, comment: e.target.value }))} /></label>
              <div className="admin-api-console__row"><label>Upload image<input type="file" accept="image/*" onChange={(e) => uploadMedia(e, 'image')} disabled={uploading} /></label><label>Upload video<input type="file" accept="video/*" onChange={(e) => uploadMedia(e, 'video')} disabled={uploading} /></label></div>
              <label>Image URLs<input value={form.imageUrls} onChange={(e) => setForm((current) => ({ ...current, imageUrls: e.target.value }))} /></label>
              <label>Video URLs<input value={form.videoUrls} onChange={(e) => setForm((current) => ({ ...current, videoUrls: e.target.value }))} /></label>
              <button type="submit" disabled={uploading}>Save review</button>
            </form>
          )}
        </aside>
      </div>
    </section>
  );
}

export default ReviewListPage;
