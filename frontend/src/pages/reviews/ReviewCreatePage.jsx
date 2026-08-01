import { useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { createReview } from '../../services/reviewService.js';
import { uploadImage, uploadVideo } from '../../services/uploadService.js';

function ReviewCreatePage() {
  const [searchParams] = useSearchParams();
  const [form, setForm] = useState({ orderItemId: searchParams.get('orderItemId') || '', rating: '5', comment: '', imageUrls: '', videoUrls: '' });
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [uploading, setUploading] = useState(false);

  async function uploadMedia(event, type) {
    const file = event.target.files?.[0];
    if (!file) return;
    setUploading(true); setError('');
    try {
      const result = type === 'video' ? await uploadVideo(file, 'reviews') : await uploadImage(file, 'reviews');
      const url = result.secureUrl || result.url;
      const field = type === 'video' ? 'videoUrls' : 'imageUrls';
      setForm((current) => ({ ...current, [field]: [current[field], url].filter(Boolean).join(',') }));
    } catch (err) { setError(err.message || 'Upload media review that bai.'); } finally { setUploading(false); }
  }

  async function submitReview(event) {
    event.preventDefault(); setError(''); setMessage('');
    try {
      await createReview({
        orderItemId: Number(form.orderItemId),
        rating: Number(form.rating),
        comment: form.comment || null,
        imageUrls: form.imageUrls.split(',').map((url) => url.trim()).filter(Boolean),
        videoUrls: form.videoUrls.split(',').map((url) => url.trim()).filter(Boolean),
      });
      setMessage('Da gui danh gia san pham.');
    } catch (err) { setError(err.message || 'Gui review that bai.'); }
  }

  return <section className="container profile-page"><form className="admin-api-console" onSubmit={submitReview}>
    <div className="admin-panel__heading"><div><p>Product Review</p><h2>Viet danh gia</h2></div></div>
    {error && <div className="form-alert">{error}</div>}{message && <div className="form-alert form-alert--success">{message}</div>}
    <div className="admin-api-console__row"><label>Order item ID<input value={form.orderItemId} onChange={(e) => setForm((c) => ({ ...c, orderItemId: e.target.value }))} required /></label><label>Rating<input type="number" min="1" max="5" value={form.rating} onChange={(e) => setForm((c) => ({ ...c, rating: e.target.value }))} /></label></div>
    <label>Comment<textarea rows="5" value={form.comment} onChange={(e) => setForm((c) => ({ ...c, comment: e.target.value }))} /></label>
    <div className="admin-api-console__row"><label>Upload image<input type="file" accept="image/*" onChange={(e) => uploadMedia(e, 'image')} disabled={uploading} /></label><label>Upload video<input type="file" accept="video/*" onChange={(e) => uploadMedia(e, 'video')} disabled={uploading} /></label></div>
    <label>Image URLs<input value={form.imageUrls} onChange={(e) => setForm((c) => ({ ...c, imageUrls: e.target.value }))} /></label>
    <label>Video URLs<input value={form.videoUrls} onChange={(e) => setForm((c) => ({ ...c, videoUrls: e.target.value }))} /></label>
    <button type="submit">Gui danh gia</button>
  </form></section>;
}

export default ReviewCreatePage;
