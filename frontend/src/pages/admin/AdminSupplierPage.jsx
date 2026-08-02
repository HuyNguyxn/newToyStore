import { useEffect, useState } from 'react';
import { changeSupplierStatus, createSupplier, deleteSupplier, getSuppliers, restoreSupplier, updateSupplier } from '../../services/adminSupplierService.js';

const emptyForm = { id: '', name: '', phoneNumber: '', email: '', address: '', status: 'ACTIVE' };

function AdminSupplierPage() {
  const [suppliers, setSuppliers] = useState([]);
  const [filters, setFilters] = useState({ name: '', phoneNumber: '', status: '' });
  const [form, setForm] = useState(emptyForm);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  useEffect(() => { loadSuppliers(); }, []);

  async function loadSuppliers() {
    try {
      const result = await getSuppliers({ ...filters, page: 0, size: 20, sort: 'createdAt,desc' });
      setSuppliers(result.content || []);
    } catch (err) { setError(err.message || 'Kh?ng th? t?i supplier.'); }
  }

  function selectSupplier(supplier) {
    setForm({ id: supplier.id || '', name: supplier.name || '', phoneNumber: supplier.phoneNumber || '', email: supplier.email || '', address: supplier.address || '', status: supplier.status || 'ACTIVE' });
  }

  async function saveSupplier(event) {
    event.preventDefault(); setError(''); setMessage('');
    try {
      const payload = { name: form.name.trim(), phoneNumber: form.phoneNumber.trim(), email: form.email || null, address: form.address || null };
      if (form.id) await updateSupplier(form.id, payload); else await createSupplier(payload);
      setMessage(form.id ? '?? c?p nh?t supplier.' : '?? t?o supplier.');
      setForm(emptyForm); await loadSuppliers();
    } catch (err) { setError(err.message || 'L?u supplier th?t b?i.'); }
  }

  async function doAction(action, success) {
    setError(''); setMessage('');
    try { await action(); setMessage(success); await loadSuppliers(); } catch (err) { setError(err.message || 'Thao t?c supplier th?t b?i.'); }
  }

  return (
    <section className="admin-resource">
      <div className="admin-resource__hero"><div><p>Admin CRUD</p><h2>Suppliers</h2><span>Create, update, change status, restore, and delete suppliers.</span></div></div>
      {error && <div className="form-alert">{error}</div>}{message && <div className="form-alert form-alert--success">{message}</div>}
      <form className="admin-filter" onSubmit={(e) => { e.preventDefault(); loadSuppliers(); }}>{Object.keys(filters).map((f) => <label key={f}>{f}<input value={filters[f]} onChange={(e) => setFilters((c) => ({ ...c, [f]: e.target.value }))} /></label>)}<button type="submit">Filter</button></form>
      <form className="admin-api-console" onSubmit={saveSupplier}>
        <div className="admin-panel__heading"><div><p>{form.id ? `Edit #${form.id}` : 'Create'}</p><h2>Supplier form</h2></div><button type="button" onClick={() => setForm(emptyForm)}>New</button></div>
        <div className="admin-api-console__row"><label>Name<input value={form.name} onChange={(e) => setForm((c) => ({ ...c, name: e.target.value }))} required /></label><label>Phone<input value={form.phoneNumber} onChange={(e) => setForm((c) => ({ ...c, phoneNumber: e.target.value }))} required /></label></div>
        <div className="admin-api-console__row"><label>Email<input type="email" value={form.email} onChange={(e) => setForm((c) => ({ ...c, email: e.target.value }))} /></label><label>Status<input value={form.status} onChange={(e) => setForm((c) => ({ ...c, status: e.target.value }))} /></label></div>
        <label>Address<input value={form.address} onChange={(e) => setForm((c) => ({ ...c, address: e.target.value }))} /></label>
        <button type="submit">{form.id ? 'Update supplier' : 'Create supplier'}</button>
      </form>
      <div className="admin-resource-table"><div className="admin-resource-table__head" style={{ gridTemplateColumns: '80px 1fr 150px 1fr 120px 260px' }}><span>ID</span><span>Name</span><span>Phone</span><span>Email</span><span>Status</span><span>Actions</span></div>{suppliers.map((s) => <div className="admin-resource-table__row" style={{ gridTemplateColumns: '80px 1fr 150px 1fr 120px 260px' }} key={s.id}><span>{s.id}</span><span>{s.name}</span><span>{s.phoneNumber}</span><span>{s.email}</span><span>{s.status}</span><span className="admin-resource-table__actions"><button type="button" onClick={() => selectSupplier(s)}>Edit</button><button type="button" onClick={() => doAction(() => changeSupplierStatus(s.id, form.status || 'ACTIVE'), '?? ??i status supplier.')}>Status</button><button type="button" onClick={() => doAction(() => restoreSupplier(s.id), '?? restore supplier.')}>Restore</button><button type="button" className="is-danger" onClick={() => doAction(() => deleteSupplier(s.id), '?? x?a supplier.')}>Delete</button></span></div>)}</div>
    </section>
  );
}

export default AdminSupplierPage;
