import { createContext, useContext, useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getAdminProducts } from '../../services/adminProductService.js';
import {
  cancelWarehouseBatch,
  completeWarehouseBatch,
  getWarehouseBatchDetails,
  getWarehouseBatches,
  publishWarehouseProduct,
} from '../../services/warehouseService.js';
import { formatDateTime, formatPrice } from '../../utils/formatters.js';

function statusCode(status) {
  if (status && typeof status === 'object') return String(status.code || status.name || '').toUpperCase();
  return String(status || '').toUpperCase();
}

function importStatus(status) {
  const code = statusCode(status);
  if (code === 'COMPLETED') return { label: 'Đã nhập kho', color: '#15803d', bg: '#f0fdf4', border: '#bbf7d0' };
  if (code === 'CANCELLED') return { label: 'Đã hủy', color: '#b91c1c', bg: '#fef2f2', border: '#fecaca' };
  return { label: 'Chờ kiểm đếm', color: '#0369a1', bg: '#f0f9ff', border: '#bae6fd' };
}

function productStatus(product) {
  const code = statusCode(product?.status);
  if (code === 'ACTIVE') return { label: 'Đang bán', color: '#15803d', bg: '#f0fdf4' };
  if (code === 'OUT_OF_STOCK') return { label: 'Hết hàng', color: '#b45309', bg: '#fffbeb' };
  return { label: 'Chưa bán', color: '#64748b', bg: '#f8fafc' };
}

function stockOf(product) {
  return (product?.variants || []).reduce((sum, variant) => {
    const inventory = variant.inventory || {};
    return sum + Number(inventory.availableQuantity ?? inventory.stockQuantity ?? variant.stockQuantity ?? 0);
  }, 0);
}

const BatchStatusActionContext = createContext(() => {});

function ImportBatchDetail({ note, products, onClose, onPublish, publishingId, statusAction, navigate }) {
  const onStatusAction = useContext(BatchStatusActionContext);
  if (!note) return null;
  const badge = importStatus(note.status);

  return (
    <aside style={{ background: '#ffffff', border: '1px solid #e2e8f0', borderRadius: 18, padding: 22, position: 'sticky', top: 20 }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'start', gap: 12, marginBottom: 18 }}>
        <div>
          <div style={{ color: '#ea580c', fontSize: 12, fontWeight: 900, letterSpacing: .6 }}>CHI TIẾT LÔ NHẬP</div>
          <h2 style={{ margin: '4px 0 0', color: '#0f172a', fontSize: 21 }}>PN{String(note.id).padStart(6, '0')}</h2>
        </div>
        <button type="button" onClick={onClose} style={{ border: 0, background: '#f1f5f9', borderRadius: 8, width: 32, height: 32, cursor: 'pointer', fontSize: 18 }}>×</button>
      </div>

      <div style={{ background: '#f8fafc', border: '1px solid #e2e8f0', borderRadius: 12, padding: 14, display: 'grid', gap: 8, fontSize: 13 }}>
        {(note.allowedNextActions || []).length > 0 && (
          <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap', marginBottom: 4 }}>
            {(note.allowedNextActions || []).map((action) => {
              const isComplete = action.action === 'COMPLETE';
              const isRunning = statusAction === action.action;
              return (
                <button
                  key={action.action}
                  type="button"
                  disabled={Boolean(statusAction)}
                  onClick={() => onStatusAction(action.action)}
                  style={{ border: 0, background: isComplete ? '#16a34a' : '#dc2626', color: '#ffffff', borderRadius: 8, padding: '8px 10px', cursor: statusAction ? 'wait' : 'pointer', fontSize: 12, fontWeight: 800 }}
                >
                  {isRunning ? 'Đang xử lý...' : action.label}
                </button>
              );
            })}
          </div>
        )}
        <div><strong>Nhà cung cấp:</strong> {note.supplierName || '—'}</div>
        <div><strong>Ngày nhập:</strong> {note.createdAt ? formatDateTime(note.createdAt) : '—'}</div>
        <div><strong>Trạng thái:</strong> <span style={{ color: badge.color, background: badge.bg, padding: '3px 8px', borderRadius: 8, fontWeight: 800 }}>{badge.label}</span></div>
        <div><strong>Tổng tiền:</strong> {formatPrice(note.totalAmount || 0)}</div>
      </div>

      <h3 style={{ fontSize: 15, margin: '22px 0 10px', color: '#0f172a' }}>Sản phẩm trong lô</h3>
      <div style={{ display: 'grid', gap: 10 }}>
        {(note.items || []).map((item) => {
          const product = products.get(Number(item.productId));
          const state = productStatus(product);
          const stock = stockOf(product);
          const canPublish = statusCode(note.status) === 'COMPLETED' && product && statusCode(product.status) !== 'ACTIVE' && stock > 0;
          return (
            <div key={item.id || `${item.productId}-${item.variantId}`} style={{ border: '1px solid #e2e8f0', borderRadius: 12, padding: 12 }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', gap: 10 }}>
                <div>
                  <div style={{ fontWeight: 800, color: '#0f172a' }}>{item.productName || `Sản phẩm #${item.productId}`}</div>
                  <div style={{ color: '#64748b', fontSize: 12, marginTop: 4 }}>Biến thể #{item.variantId} · Nhập {item.quantity} sản phẩm</div>
                </div>
                <span style={{ alignSelf: 'start', color: state.color, background: state.bg, borderRadius: 8, padding: '3px 7px', fontSize: 11, fontWeight: 800 }}>{state.label}</span>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: 10, gap: 8, flexWrap: 'wrap' }}>
                <span style={{ color: '#475569', fontSize: 12 }}>Tồn khả dụng: <strong>{stock}</strong></span>
                <div style={{ display: 'flex', gap: 6 }}>
                  <button type="button" onClick={() => navigate('/admin/products')} style={{ border: '1px solid #cbd5e1', background: '#ffffff', color: '#334155', borderRadius: 7, padding: '6px 9px', cursor: 'pointer', fontSize: 12, fontWeight: 700 }}>Quản lý sản phẩm</button>
                  {canPublish && (
                    <button type="button" disabled={publishingId === item.productId} onClick={() => onPublish(item.productId)} style={{ border: 0, background: '#16a34a', color: '#ffffff', borderRadius: 7, padding: '6px 9px', cursor: 'pointer', fontSize: 12, fontWeight: 800 }}>
                      {publishingId === item.productId ? 'Đang cập nhật...' : 'Đưa lên cửa hàng'}
                    </button>
                  )}
                </div>
              </div>
            </div>
          );
        })}
      </div>
    </aside>
  );
}

function AdminInventoryPage() {
  const navigate = useNavigate();
  const [imports, setImports] = useState([]);
  const [products, setProducts] = useState(new Map());
  const [selected, setSelected] = useState(null);
  const [filters, setFilters] = useState({ keyword: '', status: '' });
  const [loading, setLoading] = useState(true);
  const [detailLoading, setDetailLoading] = useState(false);
  const [publishingId, setPublishingId] = useState(null);
  const [statusAction, setStatusAction] = useState(null);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  async function loadWarehouse() {
    setLoading(true);
    setError('');
    try {
      const [importResult, productResult] = await Promise.all([
        getWarehouseBatches({ page: 0, size: 100, sort: 'createdAt,desc' }),
        getAdminProducts({ page: 0, size: 300 }),
      ]);
      const importList = importResult?.content || importResult || [];
      const productList = productResult?.content || productResult || [];
      setImports(importList);
      setProducts(new Map(productList.map((product) => [Number(product.id), product])));
    } catch (err) {
      setError(err?.message || 'Không thể tải dữ liệu kho.');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { loadWarehouse(); }, []);

  const visibleImports = useMemo(() => imports.filter((item) => {
    const keyword = filters.keyword.trim().toLowerCase();
    const matchesKeyword = !keyword || String(item.id).includes(keyword) || String(item.supplierName || '').toLowerCase().includes(keyword);
    const matchesStatus = !filters.status || statusCode(item.status) === filters.status;
    return matchesKeyword && matchesStatus;
  }), [imports, filters]);

  async function openDetails(id) {
    setDetailLoading(true);
    setError('');
    try {
      setSelected(await getWarehouseBatchDetails(id));
    } catch (err) {
      setError(err?.message || 'Không thể tải chi tiết lô nhập.');
    } finally {
      setDetailLoading(false);
    }
  }

  async function publishProduct(productId) {
    setPublishingId(productId);
    setError('');
    try {
      const updated = await publishWarehouseProduct(selected.id, productId);
      setProducts((current) => new Map(current).set(Number(productId), updated));
      setMessage('Đã đưa sản phẩm lên cửa hàng.');
    } catch (err) {
      setError(err?.message || 'Không thể đưa sản phẩm lên cửa hàng.');
    } finally {
      setPublishingId(null);
    }
  }

  async function changeBatchStatus(action) {
    const isComplete = action === 'COMPLETE';
    const confirmation = isComplete
      ? 'Xác nhận hoàn tất lô hàng này? Tồn kho của các biến thể sẽ được cộng ngay.'
      : 'Hủy lô hàng này? Lô đã hủy sẽ không thể xử lý tiếp.';
    if (!window.confirm(confirmation)) return;

    setStatusAction(action);
    setError('');
    setMessage('');
    try {
      const updated = isComplete
        ? await completeWarehouseBatch(selected.id)
        : await cancelWarehouseBatch(selected.id);
      setSelected(updated);
      setImports((current) => current.map((item) => item.id === updated.id ? { ...item, ...updated } : item));
      setMessage(isComplete
        ? 'Đã xác nhận lô hàng. Tồn kho hiện đã sẵn sàng để bán.'
        : 'Đã hủy lô hàng đang chờ xử lý.');
      await loadWarehouse();
    } catch (err) {
      setError(err?.message || 'Không thể thay đổi trạng thái lô hàng.');
    } finally {
      setStatusAction(null);
    }
  }

  return (
    <BatchStatusActionContext.Provider value={changeBatchStatus}>
      <section style={{ minHeight: '100vh', background: '#f8fafc', padding: 26, fontFamily: 'system-ui, sans-serif' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', gap: 16, alignItems: 'start', marginBottom: 20, flexWrap: 'wrap' }}>
          <div>
            <div style={{ color: '#ea580c', fontSize: 12, fontWeight: 900, letterSpacing: .8 }}>QUẢN LÝ KHO</div>
            <h1 style={{ margin: '5px 0 0', color: '#0f172a', fontSize: 26 }}>Quản lý Kho & Lô hàng</h1>
            <p style={{ margin: '6px 0 0', color: '#64748b', fontSize: 13 }}>Các lô hàng được tạo từ mục “Tạo phiếu nhập hàng”. Chọn một lô để xử lý sản phẩm đưa lên cửa hàng.</p>
          </div>
          <button type="button" onClick={() => navigate('/admin/imports')} style={{ border: 0, background: '#ea580c', color: '#ffffff', borderRadius: 10, padding: '10px 14px', cursor: 'pointer', fontWeight: 800 }}>+ Tạo phiếu nhập</button>
        </div>

        {message && <div style={{ background: '#f0fdf4', color: '#15803d', border: '1px solid #bbf7d0', padding: 11, borderRadius: 9, marginBottom: 14, fontSize: 13, fontWeight: 700 }}>{message}</div>}
        {error && <div style={{ background: '#fef2f2', color: '#b91c1c', border: '1px solid #fecaca', padding: 11, borderRadius: 9, marginBottom: 14, fontSize: 13, fontWeight: 700 }}>{error}</div>}

        <form onSubmit={(event) => event.preventDefault()} style={{ background: '#ffffff', border: '1px solid #e2e8f0', borderRadius: 14, padding: 14, display: 'flex', gap: 10, marginBottom: 18, flexWrap: 'wrap' }}>
          <input value={filters.keyword} onChange={(event) => setFilters((current) => ({ ...current, keyword: event.target.value }))} placeholder="Tìm mã lô hoặc nhà cung cấp..." style={{ flex: 1, minWidth: 220, border: '1px solid #cbd5e1', borderRadius: 8, padding: '9px 11px', fontSize: 13 }} />
          <select value={filters.status} onChange={(event) => setFilters((current) => ({ ...current, status: event.target.value }))} style={{ border: '1px solid #cbd5e1', borderRadius: 8, padding: '9px 11px', fontSize: 13, background: '#ffffff' }}>
            <option value="">Tất cả trạng thái</option>
            <option value="PENDING">Chờ kiểm đếm</option>
            <option value="COMPLETED">Đã nhập kho</option>
            <option value="CANCELLED">Đã hủy</option>
          </select>
        </form>

        <div style={{ display: 'grid', gridTemplateColumns: selected ? 'minmax(0, 1.4fr) minmax(340px, .9fr)' : '1fr', gap: 18, alignItems: 'start' }}>
          <div style={{ background: '#ffffff', border: '1px solid #e2e8f0', borderRadius: 14, overflowX: 'auto' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: 13 }}>
              <thead><tr style={{ background: '#f8fafc', color: '#475569', textAlign: 'left' }}>
                <th style={{ padding: 13 }}>Mã lô</th><th style={{ padding: 13 }}>Nhà cung cấp</th><th style={{ padding: 13 }}>Sản phẩm</th><th style={{ padding: 13 }}>Tổng tiền</th><th style={{ padding: 13 }}>Trạng thái</th><th style={{ padding: 13 }}>Ngày tạo</th><th style={{ padding: 13 }} />
              </tr></thead>
              <tbody>
                {loading ? <tr><td colSpan="7" style={{ padding: 36, textAlign: 'center', color: '#64748b' }}>Đang tải dữ liệu kho...</td></tr>
                  : visibleImports.length === 0 ? <tr><td colSpan="7" style={{ padding: 36, textAlign: 'center', color: '#94a3b8' }}>Chưa có lô hàng phù hợp.</td></tr>
                    : visibleImports.map((item, index) => {
                      const badge = importStatus(item.status);
                      return <tr key={item.id} style={{ borderTop: index ? '1px solid #f1f5f9' : 0 }}>
                        <td style={{ padding: 13, color: '#ea580c', fontWeight: 900 }}>PN{String(item.id).padStart(6, '0')}</td>
                        <td style={{ padding: 13, fontWeight: 700 }}>{item.supplierName || '—'}</td>
                        <td style={{ padding: 13 }}>{item.itemCount ?? 'Xem trong chi tiết'}</td>
                        <td style={{ padding: 13, fontWeight: 800 }}>{formatPrice(item.totalAmount || 0)}</td>
                        <td style={{ padding: 13 }}><span style={{ color: badge.color, background: badge.bg, border: `1px solid ${badge.border}`, borderRadius: 8, padding: '4px 8px', fontWeight: 800, fontSize: 11 }}>{badge.label}</span></td>
                        <td style={{ padding: 13, color: '#64748b' }}>{item.createdAt ? formatDateTime(item.createdAt) : '—'}</td>
                        <td style={{ padding: 13, textAlign: 'right' }}><button type="button" onClick={() => openDetails(item.id)} style={{ border: '1px solid #fed7aa', color: '#c2410c', background: '#fff7ed', borderRadius: 8, padding: '7px 10px', cursor: 'pointer', fontWeight: 800 }}>Chi tiết</button></td>
                      </tr>;
                    })}
              </tbody>
            </table>
          </div>
          {detailLoading ? <aside style={{ background: '#ffffff', border: '1px solid #e2e8f0', borderRadius: 18, padding: 30, color: '#64748b', textAlign: 'center' }}>Đang tải chi tiết lô...</aside> : <ImportBatchDetail note={selected} products={products} onClose={() => setSelected(null)} onPublish={publishProduct} publishingId={publishingId} statusAction={statusAction} navigate={navigate} />}
        </div>
      </section>
    </BatchStatusActionContext.Provider>
  );
}

export default AdminInventoryPage;
