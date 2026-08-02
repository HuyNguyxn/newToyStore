import { useEffect, useState } from 'react';
import {
  approveSupplierReturn,
  completeSupplierReturn,
  createSupplierReturn,
  getSupplierReturns,
  rejectSupplierReturn,
  shipSupplierReturn,
  submitSupplierReturn,
} from '../../services/adminReturnService.js';
import { formatPrice } from '../../utils/formatters.js';

const sampleItems = '[{"productId":1,"variantId":1,"productName":"Sample toy","quantity":1,"returnPrice":50000,"discountAmount":0,"reasonCode":"DAMAGED","batchNumber":"BATCH-001","expiryDate":"2027-12-31"}]';

function AdminSupplierReturnPage() {
  const [returns, setReturns] = useState([]);
  const [filters, setFilters] = useState({ supplierId: '', status: '' });
  const [form, setForm] = useState({ supplierId: '', importNoteId: '', freightCost: '0', restockingFee: '0', note: '', imageUrls: '', itemsJson: sampleItems });
  const [rejectReason, setRejectReason] = useState('Rejected from admin dashboard');
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  useEffect(() => { loadReturns(); }, []);

  async function loadReturns() {
    try { const result = await getSupplierReturns({ ...filters, page: 0, size: 20, sort: 'createdAt,desc' }); setReturns(result.content || []); } catch (err) { setError(err.message || 'Kh?ng th? t?i supplier return.'); }
  }

  async function saveReturn(event) {
    event.preventDefault(); setError(''); setMessage('');
    try {
      await createSupplierReturn({
        supplierId: Number(form.supplierId),
        importNoteId: form.importNoteId === '' ? null : Number(form.importNoteId),
        freightCost: Number(form.freightCost || 0),
        restockingFee: Number(form.restockingFee || 0),
        note: form.note || null,
        imageUrls: form.imageUrls.split(',').map((url) => url.trim()).filter(Boolean),
        items: JSON.parse(form.itemsJson),
      });
      setMessage('?? t?o supplier return draft.'); setForm({ supplierId: '', importNoteId: '', freightCost: '0', restockingFee: '0', note: '', imageUrls: '', itemsJson: sampleItems }); await loadReturns();
    } catch (err) { setError(err.message || 'T?o supplier return th?t b?i. Ki?m tra items JSON.'); }
  }

  async function doAction(action, success) {
    setError(''); setMessage('');
    try { await action(); setMessage(success); await loadReturns(); } catch (err) { setError(err.message || 'Thao t?c supplier return th?t b?i.'); }
  }

  return <section className="admin-resource">
    <div className="admin-resource__hero"><div><p>Admin Workflow</p><h2>Supplier Returns</h2><span>Create supplier return drafts and execute approval/shipping/completion workflow.</span></div></div>
    {error && <div className="form-alert">{error}</div>}{message && <div className="form-alert form-alert--success">{message}</div>}
    <form className="admin-filter" onSubmit={(e) => { e.preventDefault(); loadReturns(); }}>{Object.keys(filters).map((f) => <label key={f}>{f}<input value={filters[f]} onChange={(e) => setFilters((c) => ({ ...c, [f]: e.target.value }))} /></label>)}<button type="submit">Filter</button></form>
    <form className="admin-api-console" onSubmit={saveReturn}>
      <div className="admin-panel__heading"><div><p>Create</p><h2>Supplier return draft</h2></div></div>
      <div className="admin-api-console__row"><label>Supplier ID<input value={form.supplierId} onChange={(e) => setForm((c) => ({ ...c, supplierId: e.target.value }))} required /></label><label>Import note ID<input value={form.importNoteId} onChange={(e) => setForm((c) => ({ ...c, importNoteId: e.target.value }))} /></label></div>
      <div className="admin-api-console__row"><label>Freight cost<input value={form.freightCost} onChange={(e) => setForm((c) => ({ ...c, freightCost: e.target.value }))} /></label><label>Restocking fee<input value={form.restockingFee} onChange={(e) => setForm((c) => ({ ...c, restockingFee: e.target.value }))} /></label></div>
      <label>Image URLs<input value={form.imageUrls} onChange={(e) => setForm((c) => ({ ...c, imageUrls: e.target.value }))} placeholder="url1,url2" /></label>
      <label>Note<input value={form.note} onChange={(e) => setForm((c) => ({ ...c, note: e.target.value }))} /></label>
      <label>Items JSON<textarea rows="7" value={form.itemsJson} onChange={(e) => setForm((c) => ({ ...c, itemsJson: e.target.value }))} /></label>
      <button type="submit">Create supplier return</button>
    </form>
    <form className="admin-filter" onSubmit={(e) => e.preventDefault()}><label>Reject reason<input value={rejectReason} onChange={(e) => setRejectReason(e.target.value)} /></label></form>
    <div className="admin-resource-table"><div className="admin-resource-table__head" style={{ gridTemplateColumns: '80px 120px 130px 140px 340px' }}><span>ID</span><span>Supplier</span><span>Status</span><span>Refund</span><span>Actions</span></div>{returns.map((r) => <div className="admin-resource-table__row" style={{ gridTemplateColumns: '80px 120px 130px 140px 340px' }} key={r.id}><span>{r.id}</span><span>{r.supplierId}</span><span>{r.status}</span><span>{formatPrice(r.totalRefundAmount)}</span><span className="admin-resource-table__actions"><button type="button" onClick={() => doAction(() => submitSupplierReturn(r.id), '?? submit.')}>Submit</button><button type="button" onClick={() => doAction(() => approveSupplierReturn(r.id), '?? approve.')}>Approve</button><button type="button" className="is-danger" onClick={() => doAction(() => rejectSupplierReturn(r.id, rejectReason), '?? reject.')}>Reject</button><button type="button" onClick={() => doAction(() => shipSupplierReturn(r.id), '?? ship.')}>Ship</button><button type="button" onClick={() => doAction(() => completeSupplierReturn(r.id), '?? complete.')}>Complete</button></span></div>)}</div>
  </section>;
}

export default AdminSupplierReturnPage;
