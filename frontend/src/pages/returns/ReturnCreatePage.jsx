import { useState } from 'react';
import { createCustomerReturn } from '../../services/customerReturnService.js';
import { uploadImage } from '../../services/uploadService.js';

const sampleItems = '[{"orderItemId":1,"productId":1,"variantId":1,"quantity":1,"reasonCode":"DAMAGED","expectedRefundAmount":50000}]';

function ReturnCreatePage() {
  const [form, setForm] = useState({ orderId: '', reasonNote: '', proofImageUrls: '', itemsJson: sampleItems });
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [uploading, setUploading] = useState(false);

  async function uploadProof(event) {
    const file = event.target.files?.[0];
    if (!file) return;
    setUploading(true); setError('');
    try {
      const result = await uploadImage(file, 'returns');
      const url = result.secureUrl || result.url;
      setForm((current) => ({ ...current, proofImageUrls: [current.proofImageUrls, url].filter(Boolean).join(',') }));
    } catch (err) { setError(err.message || 'Upload anh minh chung that bai.'); } finally { setUploading(false); }
  }

  async function submitReturn(event) {
    event.preventDefault(); setError(''); setMessage('');
    try {
      await createCustomerReturn({
        orderId: Number(form.orderId),
        reasonNote: form.reasonNote || null,
        proofImageUrls: form.proofImageUrls.split(',').map((url) => url.trim()).filter(Boolean),
        items: JSON.parse(form.itemsJson),
      });
      setMessage('Da gui yeu cau tra hang.');
    } catch (err) { setError(err.message || 'Tao yeu cau tra hang that bai.'); }
  }

  return <section className="container profile-page"><form className="admin-api-console" onSubmit={submitReturn}>
    <div className="admin-panel__heading"><div><p>Customer Return</p><h2>Tao yeu cau tra hang</h2></div></div>
    {error && <div className="form-alert">{error}</div>}{message && <div className="form-alert form-alert--success">{message}</div>}
    <label>Order ID<input value={form.orderId} onChange={(e) => setForm((c) => ({ ...c, orderId: e.target.value }))} required /></label>
    <label>Reason note<input value={form.reasonNote} onChange={(e) => setForm((c) => ({ ...c, reasonNote: e.target.value }))} /></label>
    <label>Upload proof image<input type="file" accept="image/*" onChange={uploadProof} disabled={uploading} /></label>
    <label>Proof image URLs<input value={form.proofImageUrls} onChange={(e) => setForm((c) => ({ ...c, proofImageUrls: e.target.value }))} /></label>
    <label>Items JSON<textarea rows="7" value={form.itemsJson} onChange={(e) => setForm((c) => ({ ...c, itemsJson: e.target.value }))} /></label>
    <button type="submit">Gui yeu cau tra hang</button>
  </form></section>;
}

export default ReturnCreatePage;
