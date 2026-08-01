import { useEffect, useState } from 'react';
import { cancelImportNote, completeImportNote, createImportNote, getImports } from '../../services/adminImportService.js';
import { formatPrice } from '../../utils/formatters.js';

const emptyItem = { productId: '', variantId: '', productName: '', quantity: '1', importPrice: '0' };

function AdminImportPage() {
  const [imports, setImports] = useState([]);
  const [filters, setFilters] = useState({ supplierId: '', status: '' });
  const [form, setForm] = useState({ supplierId: '', note: '' });
  const [items, setItems] = useState([{ ...emptyItem }]);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  useEffect(() => { loadImports(); }, []);

  async function loadImports() {
    try {
      const result = await getImports({ ...filters, page: 0, size: 20, sort: 'createdAt,desc' });
      setImports(result.content || []);
    } catch (err) {
      setError(err.message || 'Khong the tai import.');
    }
  }

  function updateItem(index, field, value) {
    setItems((current) => current.map((item, itemIndex) => itemIndex === index ? { ...item, [field]: value } : item));
  }

  function addItem() {
    setItems((current) => [...current, { ...emptyItem }]);
  }

  function removeItem(index) {
    setItems((current) => current.length === 1 ? current : current.filter((_, itemIndex) => itemIndex !== index));
  }

  function buildItemsPayload() {
    return items.map((item) => ({
      productId: Number(item.productId),
      variantId: item.variantId === '' ? null : Number(item.variantId),
      productName: item.productName.trim(),
      quantity: Number(item.quantity),
      importPrice: Number(item.importPrice),
    }));
  }

  async function saveImport(event) {
    event.preventDefault();
    setError('');
    setMessage('');

    try {
      await createImportNote({
        supplierId: Number(form.supplierId),
        note: form.note || null,
        items: buildItemsPayload(),
      });
      setMessage('Da tao import note.');
      setForm({ supplierId: '', note: '' });
      setItems([{ ...emptyItem }]);
      await loadImports();
    } catch (err) {
      setError(err.message || 'Tao import note that bai. Kiem tra dong san pham.');
    }
  }

  async function doAction(action, success) {
    setError('');
    setMessage('');
    try {
      await action();
      setMessage(success);
      await loadImports();
    } catch (err) {
      setError(err.message || 'Thao tac import that bai.');
    }
  }

  return (
    <section className="admin-resource">
      <div className="admin-resource__hero">
        <div>
          <p>Admin Workflow</p>
          <h2>Imports</h2>
          <span>Create import note and complete or cancel stock receiving workflow.</span>
        </div>
      </div>

      {error && <div className="form-alert">{error}</div>}
      {message && <div className="form-alert form-alert--success">{message}</div>}

      <form className="admin-filter" onSubmit={(e) => { e.preventDefault(); loadImports(); }}>
        {Object.keys(filters).map((field) => (
          <label key={field}>{field}<input value={filters[field]} onChange={(e) => setFilters((current) => ({ ...current, [field]: e.target.value }))} /></label>
        ))}
        <button type="submit">Filter</button>
      </form>

      <form className="admin-api-console" onSubmit={saveImport}>
        <div className="admin-panel__heading">
          <div>
            <p>Create</p>
            <h2>Import note</h2>
          </div>
        </div>
        <div className="admin-api-console__row">
          <label>Supplier ID<input value={form.supplierId} onChange={(e) => setForm((current) => ({ ...current, supplierId: e.target.value }))} required /></label>
          <label>Note<input value={form.note} onChange={(e) => setForm((current) => ({ ...current, note: e.target.value }))} /></label>
        </div>

        <div className="admin-line-items">
          <div className="admin-line-items__title">
            <strong>Import items</strong>
            <button type="button" onClick={addItem}>+ Add item</button>
          </div>
          {items.map((item, index) => (
            <div className="admin-line-item" key={`import-item-${index}`}>
              <label>Product ID<input value={item.productId} onChange={(e) => updateItem(index, 'productId', e.target.value)} required /></label>
              <label>Variant ID<input value={item.variantId} onChange={(e) => updateItem(index, 'variantId', e.target.value)} /></label>
              <label>Name<input value={item.productName} onChange={(e) => updateItem(index, 'productName', e.target.value)} required /></label>
              <label>Qty<input type="number" min="1" value={item.quantity} onChange={(e) => updateItem(index, 'quantity', e.target.value)} required /></label>
              <label>Import price<input type="number" min="0" value={item.importPrice} onChange={(e) => updateItem(index, 'importPrice', e.target.value)} required /></label>
              <button type="button" className="is-danger" onClick={() => removeItem(index)} disabled={items.length === 1}>Remove</button>
            </div>
          ))}
        </div>

        <button type="submit">Create import note</button>
      </form>

      <div className="admin-resource-table">
        <div className="admin-resource-table__head" style={{ gridTemplateColumns: '80px 120px 150px 150px 220px' }}>
          <span>ID</span><span>Supplier</span><span>Status</span><span>Total</span><span>Actions</span>
        </div>
        {imports.map((item) => (
          <div className="admin-resource-table__row" style={{ gridTemplateColumns: '80px 120px 150px 150px 220px' }} key={item.id}>
            <span>{item.id}</span><span>{item.supplierId}</span><span>{item.status}</span><span>{formatPrice(item.totalAmount)}</span>
            <span className="admin-resource-table__actions">
              <button type="button" onClick={() => doAction(() => completeImportNote(item.id), 'Da complete import.')}>Complete</button>
              <button type="button" className="is-danger" onClick={() => doAction(() => cancelImportNote(item.id), 'Da cancel import.')}>Cancel</button>
            </span>
          </div>
        ))}
      </div>
    </section>
  );
}

export default AdminImportPage;
