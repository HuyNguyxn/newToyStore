import { useEffect, useState } from 'react';
import { getImportDetails } from '../../services/adminImportService.js';
import {
  approveSupplierReturn,
  completeSupplierReturn,
  createSupplierReturn,
  getSupplierReturns,
  rejectSupplierReturn,
  shipSupplierReturn,
  submitSupplierReturn,
  getSupplierReturnCriticalAlerts,
} from '../../services/adminReturnService.js';
import { formatDateTime, formatPrice } from '../../utils/formatters.js';

function getSupplierReturnStatusInfo(status) {
  const code = status && typeof status === 'object' ? status.code : status;
  const statusStr = String(code || '').toUpperCase();
  if (statusStr === 'COMPLETED' || statusStr === 'SUCCESS') {
    return { label: 'Đã hoàn thành', bg: '#d1fae5', color: '#10b981', border: '#a7f3d0' };
  }
  if (statusStr === 'DRAFT') {
    return { label: 'Bản nháp', bg: '#f1f5f9', color: '#475569', border: '#cbd5e1' };
  }
  if (statusStr === 'SUBMITTED' || statusStr === 'PENDING') {
    return { label: 'Chờ duyệt', bg: '#fef3c7', color: '#d97706', border: '#fde68a' };
  }
  if (statusStr === 'APPROVED') {
    return { label: 'Đã duyệt', bg: '#eff6ff', color: '#2563eb', border: '#bfdbfe' };
  }
  if (statusStr === 'SHIPPED') {
    return { label: 'Đang vận chuyển', bg: '#faf5ff', color: '#9333ea', border: '#e9d5ff' };
  }
  if (statusStr === 'SHIPPING_FAILED') {
    return { label: 'Giao vận thất bại', bg: '#fff1f2', color: '#e11d48', border: '#fecdd3' };
  }
  if (statusStr === 'PENDING_APPROVAL') {
    return { label: 'Chờ duyệt', bg: '#fef3c7', color: '#d97706', border: '#fde68a' };
  }
  if (statusStr === 'REJECTED') {
    return { label: 'Từ chối', bg: '#fee2e2', color: '#dc2626', border: '#fecaca' };
  }
  return { label: statusStr, bg: '#f1f5f9', color: '#475569', border: '#cbd5e1' };
}

function AdminSupplierReturnPage() {
  const [returns, setReturns] = useState([]);
  const [totalReturns, setTotalReturns] = useState(0);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [filters, setFilters] = useState({ supplierId: '', status: '' });
  
  // Basic info form fields
  const [supplierId, setSupplierId] = useState('');
  const [importNoteId, setImportNoteId] = useState('');
  const [freightCost, setFreightCost] = useState('0');
  const [restockingFee, setRestockingFee] = useState('0');
  const [note, setNote] = useState('');
  const [imageUrls, setImageUrls] = useState('');

  // Interactive items list in state
  const [items, setItems] = useState([]);
  const [slaAlerts, setSlaAlerts] = useState([]);

  const [loading, setLoading] = useState(true);
  const [loadingImport, setLoadingImport] = useState(false);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    loadReturns();
  }, [currentPage]);

  async function loadReturns() {
    setLoading(true);
    setError('');
    try {
      const [result, alertsResult] = await Promise.all([
        getSupplierReturns({
          supplierId: filters.supplierId || undefined,
          status: filters.status || undefined,
          page: currentPage,
          size: 50,
          sort: 'createdAt,desc',
        }),
        getSupplierReturnCriticalAlerts().catch(() => [])
      ]);
      setReturns(result.content || result || []);
      setTotalReturns(result.totalElements ?? (result.content ? result.content.length : result.length || 0));
      setSlaAlerts(alertsResult || []);
      if (result.totalPages !== undefined) {
        setTotalPages(result.totalPages);
      }
    } catch (err) {
      setError(err?.message || 'Không thể tải danh sách trả hàng nhà cung cấp.');
      setReturns([]);
    } finally {
      setLoading(false);
    }
  }

  // Load products dynamically from Import Note ID
  async function handleLoadImportNoteItems() {
    if (!importNoteId.trim()) {
      setError('Vui lòng nhập Mã phiếu nhập để tải sản phẩm.');
      return;
    }
    setLoadingImport(true);
    setError('');
    setMessage('');
    try {
      const details = await getImportDetails(importNoteId.trim());
      if (details) {
        setSupplierId(details.supplierId || '');
        const mappedItems = (details.items || []).map((item, index) => ({
          id: index,
          selected: true,
          productId: item.productId,
          variantId: item.variantId,
          productName: item.productName,
          importedQty: item.quantity,
          quantity: item.quantity,
          returnPrice: item.importPrice,
          reasonCode: 'DAMAGED',
          batchNumber: `BATCH-${importNoteId}`,
          expiryDate: '',
        }));
        setItems(mappedItems);
        setMessage(`Đã tải thành công ${mappedItems.length} sản phẩm từ phiếu nhập #${importNoteId}.`);
      }
    } catch (err) {
      setError(err.message || 'Không thể tải thông tin phiếu nhập hàng.');
      setItems([]);
    } finally {
      setLoadingImport(false);
    }
  }

  // Add manual product row
  function handleAddManualRow() {
    const newRow = {
      id: Date.now(),
      selected: true,
      productId: '',
      variantId: '',
      productName: '',
      importedQty: 0,
      quantity: 1,
      returnPrice: 0,
      reasonCode: 'DAMAGED',
      batchNumber: '',
      expiryDate: '',
    };
    setItems((current) => [...current, newRow]);
  }

  // Update specific field on item
  function handleUpdateItemField(id, field, value) {
    setItems((current) =>
      current.map((item) => (item.id === id ? { ...item, [field]: value } : item))
    );
  }

  // Remove manual row
  function handleRemoveRow(id) {
    setItems((current) => current.filter((item) => item.id !== id));
  }

  async function saveReturn(event) {
    event.preventDefault();
    setError('');
    setMessage('');

    const selectedItems = items.filter((item) => item.selected);
    if (!supplierId) {
      setError('Vui lòng cung cấp Mã nhà cung cấp.');
      return;
    }
    if (selectedItems.length === 0) {
      setError('Vui lòng chọn ít nhất 1 sản phẩm để thực hiện trả hàng.');
      return;
    }

    // Verify all fields are valid
    for (const item of selectedItems) {
      if (!item.productId || !item.variantId || !item.productName.trim()) {
        setError('Thông tin sản phẩm trong bảng không được để trống.');
        return;
      }
      if (item.quantity <= 0) {
        setError('Số lượng trả hàng phải lớn hơn 0.');
        return;
      }
    }

    try {
      const payload = {
        supplierId: Number(supplierId),
        importNoteId: importNoteId.trim() ? Number(importNoteId) : null,
        freightCost: Number(freightCost || 0),
        restockingFee: Number(restockingFee || 0),
        note: note || null,
        imageUrls: imageUrls.split(',').map((url) => url.trim()).filter(Boolean),
        items: selectedItems.map((item) => ({
          productId: Number(item.productId),
          variantId: Number(item.variantId),
          productName: item.productName.trim(),
          quantity: Number(item.quantity),
          returnPrice: Number(item.returnPrice),
          discountAmount: 0,
          reasonCode: item.reasonCode,
          batchNumber: item.batchNumber || '',
          expiryDate: item.expiryDate || null,
        })),
      };

      await createSupplierReturn(payload);
      setMessage('Tạo bản nháp phiếu trả nhà cung cấp thành công.');
      
      // Reset form
      setSupplierId('');
      setImportNoteId('');
      setFreightCost('0');
      setRestockingFee('0');
      setNote('');
      setImageUrls('');
      setItems([]);
      await loadReturns();
    } catch (err) {
      setError(err?.message || 'Tạo phiếu trả nhà cung cấp thất bại. Vui lòng kiểm tra lại.');
    }
  }

  async function doAction(action, successMsg) {
    setError('');
    setMessage('');
    try {
      await action();
      setMessage(successMsg);
      await loadReturns();
    } catch (err) {
      setError(err?.message || 'Thao tác trả nhà cung cấp thất bại.');
    }
  }

  const handleClearFilters = () => {
    setFilters({ supplierId: '', status: '' });
    setCurrentPage(0);
    setTimeout(() => {
      loadReturns();
    }, 50);
  };

  return (
    <section style={{ padding: '24px', background: '#f8fafc', minHeight: '100vh', fontFamily: 'system-ui, -apple-system, sans-serif' }}>
      
      {/* HEADER ROW */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
        <h1 style={{ fontSize: '18px', fontWeight: '800', color: '#0f172a', margin: 0, textTransform: 'uppercase', letterSpacing: '0.5px' }}>
          Yêu cầu trả hàng Nhà cung cấp
        </h1>
        <div style={{ background: '#fff7ed', color: '#ea580c', border: '1px solid #ffedd5', padding: '6px 16px', borderRadius: '20px', fontSize: '13px', fontWeight: '700' }}>
          Phiếu trả: {totalReturns}
        </div>
      </div>

      {/* ALERTS */}
      {error && <div style={{ background: '#fef2f2', color: '#dc2626', border: '1px solid #fecaca', padding: '10px 14px', borderRadius: '8px', marginBottom: '16px', fontSize: '13px', fontWeight: '700' }}>{error}</div>}
      {message && <div style={{ background: '#f0fdf4', color: '#16a34a', border: '1px solid #bbf7d0', padding: '10px 14px', borderRadius: '8px', marginBottom: '16px', fontSize: '13px', fontWeight: '700' }}>{message}</div>}

      {/* FILTER BAR */}
      <form
        onSubmit={(e) => {
          e.preventDefault();
          if (currentPage === 0) loadReturns();
          else setCurrentPage(0);
        }}
        style={{ background: '#ffffff', padding: '14px 16px', borderRadius: '12px', border: '1px solid #e2e8f0', marginBottom: '20px', display: 'flex', gap: '10px', alignItems: 'center', flexWrap: 'wrap' }}
      >
        <div style={{ flex: '1', minWidth: '160px' }}>
          <input
            type="text"
            placeholder="Nhập mã nhà cung cấp (Supplier ID)..."
            value={filters.supplierId}
            onChange={(e) => setFilters({ ...filters, supplierId: e.target.value })}
            style={{ width: '100%', padding: '9px 12px', border: '1px solid #cbd5e1', borderRadius: '8px', fontSize: '13px', outline: 'none' }}
          />
        </div>

        <div style={{ flex: '1', minWidth: '160px' }}>
          <select
            value={filters.status}
            onChange={(e) => setFilters({ ...filters, status: e.target.value })}
            style={{ width: '100%', padding: '9px 12px', border: '1px solid #cbd5e1', borderRadius: '8px', fontSize: '13px', outline: 'none', background: '#fff' }}
          >
            <option value="">Tất cả trạng thái</option>
            <option value="DRAFT">Bản nháp (DRAFT)</option>
            <option value="PENDING_APPROVAL">Chờ duyệt (PENDING_APPROVAL)</option>
            <option value="APPROVED">Đã duyệt (APPROVED)</option>
            <option value="SHIPPED">Đang vận chuyển (SHIPPED)</option>
            <option value="SHIPPING_FAILED">Giao vận thất bại (SHIPPING_FAILED)</option>
            <option value="COMPLETED">Đã hoàn thành (COMPLETED)</option>
            <option value="REJECTED">Bị từ chối (REJECTED)</option>
            <option value="CANCELLED">Đã hủy (CANCELLED)</option>
          </select>
        </div>

        <div style={{ display: 'flex', gap: '8px' }}>
          <button
            type="submit"
            style={{ padding: '9px 20px', background: '#ea580c', color: '#ffffff', border: 'none', borderRadius: '8px', fontSize: '13px', fontWeight: '700', cursor: 'pointer' }}
          >
            Lọc
          </button>
          <button
            type="button"
            onClick={handleClearFilters}
            style={{ padding: '9px 14px', background: '#f1f5f9', color: '#475569', border: '1px solid #cbd5e1', borderRadius: '8px', fontSize: '13px', fontWeight: '700', cursor: 'pointer' }}
          >
            Xóa lọc
          </button>
        </div>
      </form>

      {/* DUAL WORKSPACE: LEFT TABLE LIST, RIGHT DYNAMIC CREATION PANEL */}
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '24px', alignItems: 'start' }}>
        
        {/* LEFT CARD: TABLE LIST */}
        <div style={{ background: '#ffffff', padding: '20px', borderRadius: '12px', border: '1px solid #e2e8f0', minHeight: '600px' }}>
          <h3 style={{ fontSize: '15px', fontWeight: '800', marginBottom: '14px', color: '#0f172a', borderBottom: '1px solid #f1f5f9', paddingBottom: '10px' }}>
            Danh sách phiếu trả Nhà cung cấp
          </h3>

          {slaAlerts.length > 0 && (
            <div style={{ background: '#fef2f2', border: '1px solid #fecaca', padding: '12px 16px', borderRadius: '8px', marginBottom: '16px' }}>
              <div style={{ color: '#b91c1c', fontWeight: '700', fontSize: '13px', marginBottom: '4px' }}>
                Cảnh báo SLA: Có {slaAlerts.length} phiếu trả hàng quá hạn xử lý!
              </div>
              <div style={{ color: '#7f1d1d', fontSize: '12px' }}>
                Mã phiếu: {slaAlerts.map(a => `#${a.returnId || a.id}`).join(', ')}
              </div>
            </div>
          )}

          <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '13px' }}>
            <thead>
              <tr style={{ background: '#f8fafc', color: '#475569', fontWeight: '800', fontSize: '12px', borderBottom: '1px solid #e2e8f0', textTransform: 'uppercase' }}>
                <th style={{ padding: '10px 12px', width: '60px' }}>Mã</th>
                <th style={{ padding: '10px 12px', width: '90px' }}>Nhà CC</th>
                <th style={{ padding: '10px 12px', width: '80px' }}>Phiếu nhập</th>
                <th style={{ padding: '10px 12px', width: '110px' }}>Trạng thái</th>
                <th style={{ padding: '10px 12px', textAlign: 'right', width: '110px' }}>Phí VC</th>
                <th style={{ padding: '10px 12px', textAlign: 'right', width: '125px' }}>Tiền hoàn</th>
                <th style={{ padding: '10px 12px', width: '140px', textAlign: 'center' }}>Thao tác</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr>
                  <td colSpan="7" style={{ padding: '24px', textAlign: 'center', color: '#64748b' }}>
                    Đang tải danh sách...
                  </td>
                </tr>
              ) : returns.length === 0 ? (
                <tr>
                  <td colSpan="7" style={{ padding: '24px', textAlign: 'center', color: '#94a3b8' }}>
                    Chưa có phiếu trả nhà cung cấp nào.
                  </td>
                </tr>
              ) : (
                returns.map((ret, idx) => {
                  const statusCode = ret.status && typeof ret.status === 'object' ? ret.status.code : ret.status;
                  const statusInfo = getSupplierReturnStatusInfo(ret.status);
                  return (
                    <tr key={ret.id} style={{ borderBottom: '1px solid #f1f5f9', background: idx % 2 === 0 ? '#ffffff' : '#fafafa' }}>
                      <td style={{ padding: '10px 12px', fontWeight: '600' }}>#{ret.id}</td>
                      <td style={{ padding: '10px 12px', fontWeight: '600', color: '#475569' }}>NCC{ret.supplierId}</td>
                      <td style={{ padding: '10px 12px', color: '#475569' }}>{ret.importNoteId ? `PN${ret.importNoteId}` : '—'}</td>
                      <td style={{ padding: '10px 12px' }}>
                        <span style={{ background: statusInfo.bg, color: statusInfo.color, border: `1px solid ${statusInfo.border}`, padding: '2px 8px', borderRadius: '6px', fontSize: '11.5px', fontWeight: '700', display: 'inline-block' }}>
                          {statusInfo.label}
                        </span>
                      </td>
                      <td style={{ padding: '10px 12px', textAlign: 'right', fontWeight: '600' }}>
                        {formatPrice(ret.freightCost || 0)}
                      </td>
                      <td style={{ padding: '10px 12px', textAlign: 'right', fontWeight: '800', color: '#15803d' }}>
                        {formatPrice(ret.totalRefundAmount || 0)}
                      </td>
                      <td style={{ padding: '10px 12px', textAlign: 'center' }}>
                        <div style={{ display: 'inline-flex', gap: '4px', flexWrap: 'wrap', justifyContent: 'center' }}>
                          {statusCode === 'DRAFT' && (
                            <button
                              type="button"
                              onClick={() => doAction(() => submitSupplierReturn(ret.id), 'Đã trình duyệt thành công.')}
                              style={{ padding: '4px 8px', background: '#eff6ff', color: '#2563eb', border: '1px solid #bfdbfe', borderRadius: '4px', cursor: 'pointer', fontSize: '11.5px', fontWeight: '700' }}
                            >
                              Trình duyệt
                            </button>
                          )}
                          {statusCode === 'SUBMITTED' && (
                            <>
                              <button
                                type="button"
                                onClick={() => doAction(() => approveSupplierReturn(ret.id), 'Đã duyệt phiếu trả hàng.')}
                                style={{ padding: '4px 8px', background: '#f0fdf4', color: '#16a34a', border: '1px solid #bbf7d0', borderRadius: '4px', cursor: 'pointer', fontSize: '11.5px', fontWeight: '700' }}
                              >
                                Duyệt
                              </button>
                              <button
                                type="button"
                                onClick={() => doAction(() => rejectSupplierReturn(ret.id, 'Quản lý từ chối duyệt'), 'Đã từ chối phiếu.')}
                                style={{ padding: '4px 8px', background: '#fef2f2', color: '#dc2626', border: '1px solid #fecaca', borderRadius: '4px', cursor: 'pointer', fontSize: '11.5px', fontWeight: '700' }}
                              >
                                Từ chối
                              </button>
                            </>
                          )}
                          {statusCode === 'APPROVED' && (
                            <button
                              type="button"
                              onClick={() => doAction(() => shipSupplierReturn(ret.id), 'Đã xuất kho gửi nhà cung cấp.')}
                              style={{ padding: '4px 8px', background: '#faf5ff', color: '#9333ea', border: '1px solid #e9d5ff', borderRadius: '4px', cursor: 'pointer', fontSize: '11.5px', fontWeight: '700' }}
                            >
                              Xuất kho
                            </button>
                          )}
                          {statusCode === 'SHIPPED' && (
                            <button
                              type="button"
                              onClick={() => doAction(() => completeSupplierReturn(ret.id), 'Xác nhận hoàn thành phiếu cấn trừ.')}
                              style={{ padding: '4px 8px', background: '#f0fdf4', color: '#16a34a', border: '1px solid #bbf7d0', borderRadius: '4px', cursor: 'pointer', fontSize: '11.5px', fontWeight: '700' }}
                            >
                              Hoàn tất
                            </button>
                          )}
                        </div>
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>

          <div style={{ display: 'flex', justifyContent: 'center', gap: '10px', marginTop: '16px' }}>
            <button
              type="button"
              disabled={currentPage === 0}
              onClick={() => setCurrentPage(c => Math.max(0, c - 1))}
              style={{ padding: '6px 12px', border: '1px solid #cbd5e1', borderRadius: '6px', background: '#fff', cursor: currentPage === 0 ? 'not-allowed' : 'pointer', fontSize: '12px' }}
            >
              Trang trước
            </button>
            <span style={{ fontSize: '13px', display: 'flex', alignItems: 'center' }}>
              Trang {currentPage + 1} / {totalPages}
            </span>
            <button
              type="button"
              disabled={currentPage >= totalPages - 1 || totalPages === 0}
              onClick={() => setCurrentPage(c => c + 1)}
              style={{ padding: '6px 12px', border: '1px solid #cbd5e1', borderRadius: '6px', background: '#fff', cursor: currentPage >= totalPages - 1 || totalPages === 0 ? 'not-allowed' : 'pointer', fontSize: '12px' }}
            >
              Trang sau
            </button>
          </div>
        </div>

        {/* RIGHT CARD: DYNAMIC CREATION & INTERACTIVE PRODUCT SELECTOR */}
        <div style={{ background: '#ffffff', padding: '24px', borderRadius: '12px', border: '1px solid #e2e8f0', boxShadow: '0 4px 16px rgba(0,0,0,0.02)' }}>
          <h3 style={{ fontSize: '15px', fontWeight: '800', color: '#0f172a', borderBottom: '1px solid #f1f5f9', paddingBottom: '10px', margin: '0 0 16px 0' }}>
            Tạo bản nháp phiếu trả NCC
          </h3>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
            
            {/* Load from Import Note Section */}
            <div style={{ background: '#f8fafc', padding: '14px', borderRadius: '8px', border: '1px solid #cbd5e1', display: 'flex', gap: '10px', alignItems: 'flex-end' }}>
              <div style={{ flex: 1 }}>
                <label style={{ display: 'block', fontSize: '12.5px', fontWeight: '700', color: '#475569', marginBottom: '6px' }}>Mã phiếu nhập hàng nguồn</label>
                <input
                  type="text"
                  placeholder="Nhập mã phiếu nhập (Import ID)..."
                  value={importNoteId}
                  onChange={(e) => setImportNoteId(e.target.value)}
                  style={{ width: '100%', padding: '8px 10px', border: '1px solid #cbd5e1', borderRadius: '6px', fontSize: '13px', outline: 'none' }}
                />
              </div>
              <button
                type="button"
                onClick={handleLoadImportNoteItems}
                disabled={loadingImport}
                style={{ padding: '9px 16px', background: '#0f172a', color: '#ffffff', border: 'none', borderRadius: '6px', fontSize: '12.5px', fontWeight: '700', cursor: 'pointer' }}
              >
                {loadingImport ? 'Đang tải...' : 'Tải sản phẩm'}
              </button>
            </div>

            {/* Basic Info */}
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
              <div>
                <label style={{ display: 'block', fontSize: '12.5px', fontWeight: '700', color: '#475569', marginBottom: '6px' }}>Mã nhà cung cấp (Supplier ID) *</label>
                <input
                  type="number"
                  value={supplierId}
                  onChange={(e) => setSupplierId(e.target.value)}
                  required
                  style={{ width: '100%', padding: '8px 10px', border: '1px solid #cbd5e1', borderRadius: '6px', fontSize: '13px', outline: 'none' }}
                />
              </div>
              <div>
                <label style={{ display: 'block', fontSize: '12.5px', fontWeight: '700', color: '#475569', marginBottom: '6px' }}>Phí vận chuyển</label>
                <input
                  type="number"
                  value={freightCost}
                  onChange={(e) => setFreightCost(e.target.value)}
                  style={{ width: '100%', padding: '8px 10px', border: '1px solid #cbd5e1', borderRadius: '6px', fontSize: '13px', outline: 'none' }}
                />
              </div>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
              <div>
                <label style={{ display: 'block', fontSize: '12.5px', fontWeight: '700', color: '#475569', marginBottom: '6px' }}>Phí lưu kho / Phụ phí</label>
                <input
                  type="number"
                  value={restockingFee}
                  onChange={(e) => setRestockingFee(e.target.value)}
                  style={{ width: '100%', padding: '8px 10px', border: '1px solid #cbd5e1', borderRadius: '6px', fontSize: '13px', outline: 'none' }}
                />
              </div>
              <div>
                <label style={{ display: 'block', fontSize: '12.5px', fontWeight: '700', color: '#475569', marginBottom: '6px' }}>Ảnh chứng minh (URL)</label>
                <input
                  type="text"
                  placeholder="URL cách nhau bằng dấu phẩy..."
                  value={imageUrls}
                  onChange={(e) => setImageUrls(e.target.value)}
                  style={{ width: '100%', padding: '8px 10px', border: '1px solid #cbd5e1', borderRadius: '6px', fontSize: '13px', outline: 'none' }}
                />
              </div>
            </div>

            <div>
              <label style={{ display: 'block', fontSize: '12.5px', fontWeight: '700', color: '#475569', marginBottom: '6px' }}>Ghi chú phiếu trả</label>
              <input
                type="text"
                placeholder="Nhập lý do trả hàng..."
                value={note}
                onChange={(e) => setNote(e.target.value)}
                style={{ width: '100%', padding: '8px 10px', border: '1px solid #cbd5e1', borderRadius: '6px', fontSize: '13px', outline: 'none' }}
              />
            </div>

            {/* INTERACTIVE PRODUCTS TABLE (REPLACES RAW JSON TEXTAREA) */}
            <div style={{ borderTop: '1px solid #f1f5f9', paddingTop: '14px', marginTop: '6px' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '10px' }}>
                <span style={{ fontSize: '13px', fontWeight: '800', color: '#334155' }}>Danh sách sản phẩm xuất trả</span>
                <button
                  type="button"
                  onClick={handleAddManualRow}
                  style={{ padding: '5px 12px', background: '#eff6ff', color: '#2563eb', border: '1px solid #bfdbfe', borderRadius: '6px', fontSize: '12px', fontWeight: '700', cursor: 'pointer' }}
                >
                  + Thêm dòng sản phẩm
                </button>
              </div>

              <div style={{ maxHeight: '300px', overflowY: 'auto', border: '1px solid #e2e8f0', borderRadius: '8px', background: '#fafafa' }}>
                <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '12px', textAlign: 'left' }}>
                  <thead style={{ background: '#f1f5f9', position: 'sticky', top: 0 }}>
                    <tr>
                      <th style={{ padding: '8px', width: '30px' }}> Chọn</th>
                      <th style={{ padding: '8px', minWidth: '150px' }}>Sản phẩm</th>
                      <th style={{ padding: '8px', width: '70px' }}>SL Trả</th>
                      <th style={{ padding: '8px', width: '90px' }}>Giá trả</th>
                      <th style={{ padding: '8px', width: '90px' }}>Lý do</th>
                      <th style={{ padding: '8px', width: '90px' }}>Mã lô</th>
                      <th style={{ padding: '8px', width: '40px', textAlign: 'center' }}>Xóa</th>
                    </tr>
                  </thead>
                  <tbody>
                    {items.length === 0 ? (
                      <tr>
                        <td colSpan="7" style={{ padding: '24px', textAlign: 'center', color: '#94a3b8' }}>
                          Chưa có sản phẩm nào. Vui lòng nhập Mã phiếu nhập để tải hoặc thêm thủ công.
                        </td>
                      </tr>
                    ) : (
                      items.map((item) => (
                        <tr key={item.id} style={{ borderBottom: '1px solid #e2e8f0', background: item.selected ? '#ffffff' : '#f8fafc' }}>
                          <td style={{ padding: '8px', textAlign: 'center' }}>
                            <input
                              type="checkbox"
                              checked={item.selected}
                              onChange={(e) => handleUpdateItemField(item.id, 'selected', e.target.checked)}
                              style={{ accentColor: '#ea580c' }}
                            />
                          </td>
                          <td style={{ padding: '6px 8px' }}>
                            <input
                              type="text"
                              value={item.productName}
                              placeholder="Tên sản phẩm..."
                              onChange={(e) => handleUpdateItemField(item.id, 'productName', e.target.value)}
                              style={{ width: '100%', padding: '4px 6px', border: '1px solid #cbd5e1', borderRadius: '4px', fontSize: '11.5px' }}
                            />
                            <div style={{ display: 'flex', gap: '4px', marginTop: '4px' }}>
                              <input
                                type="number"
                                placeholder="Prod ID"
                                value={item.productId}
                                onChange={(e) => handleUpdateItemField(item.id, 'productId', e.target.value)}
                                style={{ width: '50%', padding: '2px 4px', border: '1px solid #cbd5e1', borderRadius: '4px', fontSize: '10px' }}
                              />
                              <input
                                type="number"
                                placeholder="Var ID"
                                value={item.variantId}
                                onChange={(e) => handleUpdateItemField(item.id, 'variantId', e.target.value)}
                                style={{ width: '50%', padding: '2px 4px', border: '1px solid #cbd5e1', borderRadius: '4px', fontSize: '10px' }}
                              />
                            </div>
                          </td>
                          <td style={{ padding: '6px 8px' }}>
                            <input
                              type="number"
                              min="1"
                              max={item.importedQty}
                              value={item.quantity}
                              onChange={(e) => handleUpdateItemField(item.id, 'quantity', Number(e.target.value))}
                              style={{ width: '100%', padding: '4px 6px', border: '1px solid #cbd5e1', borderRadius: '4px', fontSize: '11.5px' }}
                            />
                            {item.importedQty && item.importedQty !== 999 && (
                              <span style={{ fontSize: '9.5px', color: '#64748b', display: 'block', marginTop: '2px' }}>Max: {item.importedQty}</span>
                            )}
                          </td>
                          <td style={{ padding: '6px 8px' }}>
                            <input
                              type="number"
                              min="0"
                              value={item.returnPrice}
                              onChange={(e) => handleUpdateItemField(item.id, 'returnPrice', Number(e.target.value))}
                              style={{ width: '100%', padding: '4px 6px', border: '1px solid #cbd5e1', borderRadius: '4px', fontSize: '11.5px' }}
                            />
                          </td>
                          <td style={{ padding: '6px 8px' }}>
                            <select
                              value={item.reasonCode}
                              onChange={(e) => handleUpdateItemField(item.id, 'reasonCode', e.target.value)}
                              style={{ width: '100%', padding: '4px 6px', border: '1px solid #cbd5e1', borderRadius: '4px', fontSize: '11.5px', background: '#fff' }}
                            >
                              <option value="DAMAGED">Hỏng QC</option>
                              <option value="EXPIRED">Hết hạn</option>
                              <option value="OVERSTOCK">Dư thừa</option>
                              <option value="OTHER">Lý do khác</option>
                            </select>
                          </td>
                          <td style={{ padding: '6px 8px' }}>
                            <input
                              type="text"
                              value={item.batchNumber}
                              onChange={(e) => handleUpdateItemField(item.id, 'batchNumber', e.target.value)}
                              style={{ width: '100%', padding: '4px 6px', border: '1px solid #cbd5e1', borderRadius: '4px', fontSize: '11.5px' }}
                            />
                          </td>
                          <td style={{ padding: '6px 8px', textAlign: 'center' }}>
                            <button
                              type="button"
                              onClick={() => handleRemoveRow(item.id)}
                              style={{ border: 'none', background: 'none', color: '#dc2626', cursor: 'pointer', fontWeight: 'bold', fontSize: '14px' }}
                            >
                              ×
                            </button>
                          </td>
                        </tr>
                      ))
                    )}
                  </tbody>
                </table>
              </div>
            </div>

            {/* SUBMIT BUTTON */}
            <button
              onClick={saveReturn}
              disabled={loadingImport}
              style={{ width: '100%', padding: '12px', background: '#ea580c', color: '#ffffff', border: 'none', borderRadius: '8px', fontSize: '14px', fontWeight: '800', cursor: 'pointer', marginTop: '14px', boxShadow: '0 4px 12px rgba(234,88,12,0.15)' }}
            >
              Tạo bản nháp phiếu trả
            </button>

          </div>
        </div>

      </div>

      {/* FOOTER */}
      <footer style={{ textAlign: 'center', marginTop: '30px', padding: '16px 0', borderTop: '1px solid #cbd5e1', fontSize: '12px', color: '#94a3b8' }}>
        © 2026 ToyStore Admin Panel
      </footer>
    </section>
  );
}

export default AdminSupplierReturnPage;
