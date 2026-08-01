import { useEffect, useState } from 'react';
import { cancelImportNote, completeImportNote, createImportNote, getImports } from '../../services/adminImportService.js';
import { formatPrice } from '../../utils/formatters.js';

const sampleItems = '[{"productId":1,"variantId":1,"productName":"Sample toy","quantity":10,"importPrice":50000}]';

function AdminImportPage() {
  const [imports, setImports] = useState([]);
  const [filters, setFilters] = useState({ supplierId: '', status: '' });
  const [form, setForm] = useState({ supplierId: '', note: '', itemsJson: sampleItems });
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  useEffect(() => { loadImports(); }, []);

  async function loadImports() {
    try { const result = await getImports({ ...filters, page: 0, size: 20, sort: 'createdAt,desc' }); setImports(result.content || []); } catch (err) { setError(err.message || 'Khong the tai import.'); }
  }

  async function saveImport(event) {
    event.preventDefault(); setError(''); setMessage('');
    try {
      await createImportNote({ supplierId: Number(form.supplierId), note: form.note || null, items: JSON.parse(form.itemsJson) });
      setMessage('Da tao import note.'); setForm({ supplierId: '', note: '', itemsJson: sampleItems }); await loadImports();
    } catch (err) { setError(err.message || 'Tao import note that bai. Kiem tra items JSON.'); }
  }

  async function doAction(action, success) {
    setError(''); setMessage('');
    try { await action(); setMessage(success); await loadImports(); } catch (err) { setError(err.message || 'Thao tac import that bai.'); }
  }

  return <section className="admin-resource">
    <div className="admin-resource__hero"><div><p>Admin Workflow</p><h2>Imports</h2><span>Create import note and complete or cancel stock receiving workflow.</span></div></div>
    {error && <div className="form-alert">{error}</div>}{message && <div className="form-alert form-alert--success">{message}</div>}
    <form className="admin-filter" onSubmit={(e) => { e.preventDefault(); loadImports(); }}>{Object.keys(filters).map((f) => <label key={f}>{f}<input value={filters[f]} onChange={(e) => setFilters((c) => ({ ...c, [f]: e.target.value }))} /></label>)}<button type="submit">Filter</button></form>
    <form className="admin-api-console" onSubmit={saveImport}>
      <div className="admin-panel__heading"><div><p>Create</p><h2>Import note</h2></div></div>
      <label>Supplier ID<input value={form.supplierId} onChange={(e) => setForm((c) => ({ ...c, supplierId: e.target.value }))} required /></label>
      <label>Note<input value={form.note} onChange={(e) => setForm((c) => ({ ...c, note: e.target.value }))} /></label>
      <label>Items JSON<textarea rows="7" value={form.itemsJson} onChange={(e) => setForm((c) => ({ ...c, itemsJson: e.target.value }))} /></label>
      <button type="submit">Create import note</button>
    </form>
    <div className="admin-resource-table"><div className="admin-resource-table__head" style={{ gridTemplateColumns: '80px 120px 150px 150px 220px' }}><span>ID</span><span>Supplier</span><span>Status</span><span>Total</span><span>Actions</span></div>{imports.map((i) => <div className="admin-resource-table__row" style={{ gridTemplateColumns: '80px 120px 150px 150px 220px' }} key={i.id}><span>{i.id}</span><span>{i.supplierId}</span><span>{i.status}</span><span>{formatPrice(i.totalAmount)}</span><span className="admin-resource-table__actions"><button type="button" onClick={() => doAction(() => completeImportNote(i.id), 'Da complete import.')}>Complete</button><button type="button" className="is-danger" onClick={() => doAction(() => cancelImportNote(i.id), 'Da cancel import.')}>Cancel</button></span></div>)}</div>
  </section>;
}

export default AdminImportPage;
