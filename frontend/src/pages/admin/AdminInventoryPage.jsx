import { useState } from 'react';
import { getVariantInventoryBatches } from '../../services/inventoryService.js';
import { formatDateTime } from '../../utils/formatters.js';

const inventoryColumns = ['id', 'variantId', 'quantity', 'remainingQuantity', 'costPrice', 'expiryDate', 'createdAt'];

function AdminInventoryPage() {
  const [variantId, setVariantId] = useState('');
  const [batches, setBatches] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  async function handleSubmit(event) {
    event.preventDefault();
    setLoading(true);
    setError('');

    try {
      const result = await getVariantInventoryBatches(variantId);
      setBatches(Array.isArray(result) ? result : []);
    } catch (err) {
      setBatches([]);
      setError(err.message || 'Khong the tai batch ton kho.');
    } finally {
      setLoading(false);
    }
  }

  function renderValue(batch, column) {
    const value = batch[column];
    if (value === null || value === undefined || value === '') {
      return '-';
    }
    if (column.toLowerCase().includes('date') || column.endsWith('At')) {
      return formatDateTime(value);
    }
    return String(value);
  }

  return (
    <section className="admin-resource">
      <div className="admin-resource__hero">
        <div>
          <p>Admin API</p>
          <h2>Inventory</h2>
          <span>Check available inventory batches by product variant.</span>
        </div>
        <strong>{batches.length} batches</strong>
      </div>

      <form className="admin-filter" onSubmit={handleSubmit}>
        <label>
          variantId
          <input
            value={variantId}
            onChange={(event) => setVariantId(event.target.value)}
            placeholder="Enter variant ID"
            required
          />
        </label>
        <button type="submit" disabled={loading}>{loading ? 'Loading...' : 'Load batches'}</button>
      </form>

      {error && <div className="form-alert">{error}</div>}

      <div className="admin-resource-table">
        <div
          className="admin-resource-table__head"
          style={{ gridTemplateColumns: `repeat(${inventoryColumns.length}, minmax(130px, 1fr))` }}
        >
          {inventoryColumns.map((column) => <span key={column}>{column}</span>)}
        </div>
        {batches.map((batch) => (
          <div
            className="admin-resource-table__row"
            style={{ gridTemplateColumns: `repeat(${inventoryColumns.length}, minmax(130px, 1fr))` }}
            key={batch.id || JSON.stringify(batch)}
          >
            {inventoryColumns.map((column) => <span key={column}>{renderValue(batch, column)}</span>)}
          </div>
        ))}
      </div>

      {batches.length === 0 && <div className="empty-state">Nhap variant ID de xem batch ton kho.</div>}
    </section>
  );
}

export default AdminInventoryPage;
