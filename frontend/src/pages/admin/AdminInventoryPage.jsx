import { useState } from 'react';
import { getVariantInventoryBatches } from '../../services/inventoryService.js';
import { formatDateTime, formatPrice } from '../../utils/formatters.js';

function AdminInventoryPage() {
  const [variantId, setVariantId] = useState('');
  const [batches, setBatches] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  async function handleSubmit(event) {
    event.preventDefault();
    if (!variantId.trim()) return;
    setLoading(true);
    setError('');

    try {
      const result = await getVariantInventoryBatches(variantId.trim());
      setBatches(Array.isArray(result) ? result : []);
    } catch (err) {
      setBatches([]);
      setError(err.message || 'Không thể tải danh sách lô hàng tồn kho.');
    } finally {
      setLoading(false);
    }
  }

  return (
    <section style={{ padding: '24px', background: '#f8fafc', minHeight: '100vh', fontFamily: 'system-ui, -apple-system, sans-serif' }}>
      
      {/* HEADER ROW */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
        <h1 style={{ fontSize: '18px', fontWeight: '800', color: '#0f172a', margin: 0, textTransform: 'uppercase', letterSpacing: '0.5px' }}>
          Quản lý lô hàng tồn kho
        </h1>
        <div style={{ background: '#fff7ed', color: '#ea580c', border: '1px solid #ffedd5', padding: '6px 16px', borderRadius: '20px', fontSize: '13px', fontWeight: '700' }}>
          Lô hàng: {batches.length}
        </div>
      </div>

      {/* ERROR ALERT */}
      {error && (
        <div style={{ background: '#fef2f2', color: '#dc2626', border: '1px solid #fecaca', padding: '10px 14px', borderRadius: '8px', marginBottom: '16px', fontSize: '13px', fontWeight: '700' }}>
          {error}
        </div>
      )}

      {/* FILTER SEARCH CARD */}
      <form
        onSubmit={handleSubmit}
        style={{ background: '#ffffff', padding: '14px 16px', borderRadius: '12px', border: '1px solid #e2e8f0', marginBottom: '20px', display: 'flex', gap: '10px', alignItems: 'center', flexWrap: 'wrap' }}
      >
        <div style={{ flex: '1', minWidth: '240px' }}>
          <input
            type="text"
            placeholder="Nhập mã biến thể sản phẩm (Variant ID)..."
            value={variantId}
            onChange={(event) => setVariantId(event.target.value)}
            required
            style={{ width: '100%', padding: '9px 12px', border: '1px solid #cbd5e1', borderRadius: '8px', fontSize: '13px', outline: 'none' }}
          />
        </div>
        <button
          type="submit"
          disabled={loading}
          style={{ padding: '9px 24px', background: '#ea580c', color: '#ffffff', border: 'none', borderRadius: '8px', fontSize: '13px', fontWeight: '700', cursor: 'pointer' }}
        >
          {loading ? 'Đang tải...' : 'Tìm kiếm lô hàng'}
        </button>
      </form>

      {/* DATA TABLE */}
      <div style={{ background: '#ffffff', borderRadius: '12px', border: '1px solid #e2e8f0', overflow: 'visible' }}>
        <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '13px' }}>
          <thead>
            <tr style={{ background: '#f8fafc', color: '#475569', fontWeight: '800', fontSize: '12px', borderBottom: '1px solid #e2e8f0', textTransform: 'uppercase' }}>
              <th style={{ padding: '14px 16px', width: '80px' }}>Mã Lô</th>
              <th style={{ padding: '14px 16px', width: '120px' }}>Mã Biến Thể</th>
              <th style={{ padding: '14px 16px', textAlign: 'right', width: '140px' }}>Số Lượng Nhập</th>
              <th style={{ padding: '14px 16px', textAlign: 'right', width: '120px' }}>Còn Lại</th>
              <th style={{ padding: '14px 16px', textAlign: 'right', width: '140px' }}>Giá Vốn</th>
              <th style={{ padding: '14px 16px', width: '160px' }}>Hạn Sử Dụng</th>
              <th style={{ padding: '14px 16px', width: '160px' }}>Ngày Nhập Lô</th>
            </tr>
          </thead>
          <tbody>
            {batches.length === 0 ? (
              <tr>
                <td colSpan="7" style={{ padding: '36px', textAlign: 'center', color: '#94a3b8' }}>
                  Vui lòng nhập Mã biến thể sản phẩm để xem danh sách lô hàng.
                </td>
              </tr>
            ) : (
              batches.map((batch, idx) => (
                <tr
                  key={batch.id || idx}
                  style={{
                    borderBottom: '1px solid #f1f5f9',
                    background: idx % 2 === 0 ? '#ffffff' : '#fafafa',
                  }}
                >
                  <td style={{ padding: '14px 16px', fontWeight: '600', color: '#334155' }}>
                    #{batch.id}
                  </td>
                  <td style={{ padding: '14px 16px', color: '#475569' }}>
                    {batch.variantId}
                  </td>
                  <td style={{ padding: '14px 16px', textAlign: 'right', fontWeight: '600' }}>
                    {batch.quantity}
                  </td>
                  <td style={{ padding: '14px 16px', textAlign: 'right', fontWeight: '700', color: batch.remainingQuantity <= 5 ? '#dc2626' : '#16a34a' }}>
                    {batch.remainingQuantity}
                  </td>
                  <td style={{ padding: '14px 16px', textAlign: 'right', fontWeight: '700', color: '#0f172a' }}>
                    {formatPrice(batch.costPrice)}
                  </td>
                  <td style={{ padding: '14px 16px', color: '#64748b' }}>
                    {batch.expiryDate ? formatDateTime(batch.expiryDate) : '-'}
                  </td>
                  <td style={{ padding: '14px 16px', color: '#64748b' }}>
                    {batch.createdAt ? formatDateTime(batch.createdAt) : '-'}
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {/* FOOTER */}
      <footer style={{ textAlign: 'center', marginTop: '30px', padding: '16px 0', borderTop: '1px solid #cbd5e1', fontSize: '12px', color: '#94a3b8' }}>
        © 2026 ToyStore Admin Panel
      </footer>
    </section>
  );
}

export default AdminInventoryPage;
