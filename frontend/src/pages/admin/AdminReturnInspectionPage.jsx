import { useEffect, useState } from 'react';
import {
  finalizeCustomerReturnRefund,
  getCustomerReturns,
  getSupplierReturns,
  inspectCustomerReturn,
  inspectSupplierReturn,
  receiveCustomerReturn,
  requireCustomerReturnInfo,
  resolveCustomerReturnDispute,
} from '../../services/adminReturnService.js';
import { formatDateTime, formatPrice } from '../../utils/formatters.js';

// Status styling helper
function getReturnStatusInfo(status) {
  const code = status && typeof status === 'object' ? status.code : status;
  const statusStr = String(code || '').toUpperCase();
  if (statusStr === 'COMPLETED' || statusStr === 'SUCCESS' || statusStr === 'SUCCEEDED' || statusStr === 'RESOLVED') {
    return { label: 'Đã hoàn thành', bg: '#d1fae5', color: '#10b981', border: '#a7f3d0' };
  }
  if (statusStr === 'PENDING' || statusStr === 'REQUESTED') {
    return { label: 'Chờ tiếp nhận', bg: '#fef3c7', color: '#d97706', border: '#fde68a' };
  }
  if (statusStr === 'RECEIVED') {
    return { label: 'Đã tiếp nhận', bg: '#eff6ff', color: '#2563eb', border: '#bfdbfe' };
  }
  if (statusStr === 'INSPECTED') {
    return { label: 'Đã kiểm định', bg: '#faf5ff', color: '#9333ea', border: '#e9d5ff' };
  }
  if (statusStr === 'REJECTED' || statusStr === 'FAILED') {
    return { label: 'Từ chối', bg: '#fee2e2', color: '#dc2626', border: '#fecaca' };
  }
  return { label: statusStr, bg: '#f1f5f9', color: '#475569', border: '#cbd5e1' };
}

function AdminReturnInspectionPage() {
  const [customerReturns, setCustomerReturns] = useState([]);
  const [supplierReturns, setSupplierReturns] = useState([]);
  const [selectedCustomerReturn, setSelectedCustomerReturn] = useState(null);
  const [selectedSupplierReturn, setSelectedSupplierReturn] = useState(null);
  
  // Interactive inspection items list for Supplier Returns
  const [supplierInspectionItems, setSupplierInspectionItems] = useState([]);

  const [customerForm, setCustomerForm] = useState({
    adminMessage: 'Vui lòng cung cấp thêm hình ảnh chứng minh trả hàng.',
    isPassed: true,
    qcNote: 'Đã kiểm định QC đạt yêu cầu.',
    isApproved: true,
    resolutionNote: 'Đã thống nhất giải pháp xử lý.',
    refundNote: 'Hoàn tiền cho khách hàng.',
  });

  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    loadData();
  }, []);

  async function loadData() {
    setLoading(true);
    setError('');

    try {
      const [custRes, suppRes] = await Promise.allSettled([
        getCustomerReturns({ page: 0, size: 20, sort: 'createdAt,desc' }),
        getSupplierReturns({ page: 0, size: 20, sort: 'createdAt,desc' }),
      ]);

      if (custRes.status === 'fulfilled') {
        setCustomerReturns(custRes.value?.content || custRes.value || []);
      } else {
        setCustomerReturns([]);
      }

      if (suppRes.status === 'fulfilled') {
        setSupplierReturns(suppRes.value?.content || suppRes.value || []);
      } else {
        setSupplierReturns([]);
      }
    } catch (err) {
      setError(err?.message || 'Không thể tải danh sách phiếu trả hàng.');
    } finally {
      setLoading(false);
    }
  }

  function handleSelectSupplierReturn(item) {
    setSelectedSupplierReturn(item);
    setSelectedCustomerReturn(null);
    
    // Map items from return object into interactive inspection rows in state
    const mapped = (item.items || []).map((p) => ({
      itemId: p.id,
      productName: p.productName || `Sản phẩm ID: ${p.productId}`,
      quantity: p.quantity,
      acceptedQuantity: p.quantity,
      discrepancyReason: '',
    }));
    setSupplierInspectionItems(mapped);
  }

  function handleUpdateInspectionItem(itemId, field, value) {
    setSupplierInspectionItems((current) =>
      current.map((item) => (item.itemId === itemId ? { ...item, [field]: value } : item))
    );
  }

  function updateCustomerForm(field, value) {
    setCustomerForm((current) => ({ ...current, [field]: value }));
  }

  async function runAction(action, successMessage) {
    setError('');
    setMessage('');
    try {
      await action();
      setMessage(successMessage);
      await loadData();
      setSelectedCustomerReturn(null);
      setSelectedSupplierReturn(null);
      setSupplierInspectionItems([]);
    } catch (err) {
      setError(err?.message || 'Thao tác kiểm định thất bại. Vui lòng kiểm tra lại.');
    }
  }

  return (
    <section style={{ padding: '24px', background: '#f8fafc', minHeight: '100vh', fontFamily: 'system-ui, -apple-system, sans-serif' }}>
      
      {/* HEADER ROW */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
        <h1 style={{ fontSize: '18px', fontWeight: '800', color: '#0f172a', margin: 0, textTransform: 'uppercase', letterSpacing: '0.5px' }}>
          Kiểm định hàng trả
        </h1>
        <button
          onClick={loadData}
          style={{ background: '#ffffff', color: '#475569', border: '1px solid #cbd5e1', padding: '6px 14px', borderRadius: '8px', fontSize: '13px', fontWeight: '700', cursor: 'pointer' }}
        >
          Làm mới dữ liệu
        </button>
      </div>

      {/* ALERTS */}
      {error && <div style={{ background: '#fef2f2', color: '#dc2626', border: '1px solid #fecaca', padding: '10px 14px', borderRadius: '8px', marginBottom: '16px', fontSize: '13px', fontWeight: '700' }}>{error}</div>}
      {message && <div style={{ background: '#f0fdf4', color: '#16a34a', border: '1px solid #bbf7d0', padding: '10px 14px', borderRadius: '8px', marginBottom: '16px', fontSize: '13px', fontWeight: '700' }}>{message}</div>}

      {/* TWO-COLUMN GRID */}
      <div style={{ display: 'grid', gridTemplateColumns: selectedSupplierReturn ? '1fr 1.2fr' : '1fr 360px', gap: '24px', alignItems: 'start' }}>
        
        {/* TABLES CONTAINER */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
          
          {/* Customer Returns Table */}
          <div style={{ background: '#ffffff', padding: '20px', borderRadius: '12px', border: '1px solid #e2e8f0' }}>
            <h3 style={{ fontSize: '15px', fontWeight: '800', marginBottom: '14px', color: '#0f172a', borderBottom: '1px solid #f1f5f9', paddingBottom: '10px' }}>
              Phiếu yêu cầu trả hàng từ Khách hàng
            </h3>
            
            <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '13px' }}>
              <thead>
                <tr style={{ background: '#f8fafc', color: '#475569', fontWeight: '800', fontSize: '12px', borderBottom: '1px solid #e2e8f0', textTransform: 'uppercase' }}>
                  <th style={{ padding: '10px 12px', width: '70px' }}>Mã</th>
                  <th style={{ padding: '10px 12px', width: '90px' }}>Đơn hàng</th>
                  <th style={{ padding: '10px 12px', width: '130px' }}>Trạng thái</th>
                  <th style={{ padding: '10px 12px' }}>Ngày yêu cầu</th>
                  <th style={{ padding: '10px 12px', width: '110px', textAlign: 'center' }}>Thao tác</th>
                </tr>
              </thead>
              <tbody>
                {customerReturns.length === 0 ? (
                  <tr>
                    <td colSpan="5" style={{ padding: '20px', textAlign: 'center', color: '#94a3b8' }}>
                      Chưa có phiếu trả hàng nào từ khách hàng.
                    </td>
                  </tr>
                ) : (
                  customerReturns.map((item, idx) => {
                    const statusInfo = getReturnStatusInfo(item.status);
                    return (
                      <tr key={item.id} style={{ borderBottom: '1px solid #f1f5f9', background: idx % 2 === 0 ? '#ffffff' : '#fafafa' }}>
                        <td style={{ padding: '10px 12px', fontWeight: '600' }}>#{item.id}</td>
                        <td style={{ padding: '10px 12px', fontWeight: '700', color: '#ea580c' }}>DH{item.orderId}</td>
                        <td style={{ padding: '10px 12px' }}>
                          <span style={{ background: statusInfo.bg, color: statusInfo.color, border: `1px solid ${statusInfo.border}`, padding: '2px 8px', borderRadius: '6px', fontSize: '11.5px', fontWeight: '700', display: 'inline-block' }}>
                            {statusInfo.label}
                          </span>
                        </td>
                        <td style={{ padding: '10px 12px', color: '#64748b' }}>{formatDateTime(item.createdAt)}</td>
                        <td style={{ padding: '10px 12px', textAlign: 'center' }}>
                          <button
                            type="button"
                            onClick={() => {
                              setSelectedCustomerReturn(item);
                              setSelectedSupplierReturn(null);
                              setSupplierInspectionItems([]);
                            }}
                            style={{ padding: '5px 10px', background: '#ffffff', color: '#ea580c', border: '1px solid #ffedd5', borderRadius: '6px', cursor: 'pointer', fontSize: '12px', fontWeight: '700' }}
                          >
                            Xử lý
                          </button>
                        </td>
                      </tr>
                    );
                  })
                )}
              </tbody>
            </table>
          </div>

          {/* Supplier Returns Table */}
          <div style={{ background: '#ffffff', padding: '20px', borderRadius: '12px', border: '1px solid #e2e8f0' }}>
            <h3 style={{ fontSize: '15px', fontWeight: '800', marginBottom: '14px', color: '#0f172a', borderBottom: '1px solid #f1f5f9', paddingBottom: '10px' }}>
              Yêu cầu trả hàng Nhà cung cấp
            </h3>
            
            <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '13px' }}>
              <thead>
                <tr style={{ background: '#f8fafc', color: '#475569', fontWeight: '800', fontSize: '12px', borderBottom: '1px solid #e2e8f0', textTransform: 'uppercase' }}>
                  <th style={{ padding: '10px 12px', width: '70px' }}>Mã</th>
                  <th style={{ padding: '10px 12px', width: '110px' }}>Mã NCC</th>
                  <th style={{ padding: '10px 12px', width: '130px' }}>Trạng thái</th>
                  <th style={{ padding: '10px 12px' }}>Ngày tạo</th>
                  <th style={{ padding: '10px 12px', width: '110px', textAlign: 'center' }}>Thao tác</th>
                </tr>
              </thead>
              <tbody>
                {supplierReturns.length === 0 ? (
                  <tr>
                    <td colSpan="5" style={{ padding: '20px', textAlign: 'center', color: '#94a3b8' }}>
                      Chưa có yêu cầu trả nhà cung cấp nào.
                    </td>
                  </tr>
                ) : (
                  supplierReturns.map((item, idx) => {
                    const statusInfo = getReturnStatusInfo(item.status);
                    return (
                      <tr key={item.id} style={{ borderBottom: '1px solid #f1f5f9', background: idx % 2 === 0 ? '#ffffff' : '#fafafa' }}>
                        <td style={{ padding: '10px 12px', fontWeight: '600' }}>#{item.id}</td>
                        <td style={{ padding: '10px 12px', fontWeight: '600', color: '#334155' }}>NCC{item.supplierId}</td>
                        <td style={{ padding: '10px 12px' }}>
                          <span style={{ background: statusInfo.bg, color: statusInfo.color, border: `1px solid ${statusInfo.border}`, padding: '2px 8px', borderRadius: '6px', fontSize: '11.5px', fontWeight: '700', display: 'inline-block' }}>
                            {statusInfo.label}
                          </span>
                        </td>
                        <td style={{ padding: '10px 12px', color: '#64748b' }}>{formatDateTime(item.createdAt)}</td>
                        <td style={{ padding: '10px 12px', textAlign: 'center' }}>
                          <button
                            type="button"
                            onClick={() => handleSelectSupplierReturn(item)}
                            style={{ padding: '5px 10px', background: '#ffffff', color: '#9333ea', border: '1px solid #f3e8ff', borderRadius: '6px', cursor: 'pointer', fontSize: '12px', fontWeight: '700' }}
                          >
                            Kiểm định
                          </button>
                        </td>
                      </tr>
                    );
                  })
                )}
              </tbody>
            </table>
          </div>

        </div>

        {/* DETAILS ASIDE CONSOLE */}
        <aside style={{ background: '#ffffff', borderRadius: '12px', border: '1px solid #e2e8f0', padding: '20px', boxShadow: '0 4px 16px rgba(0,0,0,0.02)' }}>
          <h3 style={{ fontSize: '15px', fontWeight: '800', color: '#0f172a', borderBottom: '1px solid #f1f5f9', paddingBottom: '10px', margin: '0 0 16px 0' }}>
            Bảng kiểm định nhanh
          </h3>

          {/* CUSTOMER RETURN QC FORM */}
          {selectedCustomerReturn && (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <span style={{ fontSize: '13.5px', fontWeight: '700', color: '#ea580c' }}>Khách hàng #{selectedCustomerReturn.id}</span>
                <button
                  type="button"
                  onClick={() => setSelectedCustomerReturn(null)}
                  style={{ border: 'none', background: '#f1f5f9', borderRadius: '4px', width: '22px', height: '22px', cursor: 'pointer', fontWeight: '700' }}
                >
                  ✕
                </button>
              </div>

              <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                <div>
                  <label style={{ display: 'block', fontSize: '12px', fontWeight: '700', color: '#475569', marginBottom: '6px' }}>Thông điệp Admin</label>
                  <input
                    value={customerForm.adminMessage}
                    onChange={(e) => updateCustomerForm('adminMessage', e.target.value)}
                    style={{ width: '100%', padding: '8px 10px', border: '1px solid #cbd5e1', borderRadius: '6px', fontSize: '12.5px', outline: 'none' }}
                  />
                </div>

                <button
                  type="button"
                  onClick={() => runAction(() => requireCustomerReturnInfo(selectedCustomerReturn.id, customerForm.adminMessage), 'Đã gửi yêu cầu thêm thông tin hình ảnh.')}
                  style={{ width: '100%', padding: '9px', background: '#f1f5f9', color: '#475569', border: '1px solid #cbd5e1', borderRadius: '6px', fontSize: '12.5px', fontWeight: '700', cursor: 'pointer' }}
                >
                  Yêu cầu thêm thông tin
                </button>
                <button
                  type="button"
                  onClick={() => runAction(() => receiveCustomerReturn(selectedCustomerReturn.id), 'Đã ghi nhận tiếp nhận hàng trả tại kho.')}
                  style={{ width: '100%', padding: '10px', background: '#2563eb', color: '#ffffff', border: 'none', borderRadius: '6px', fontSize: '13px', fontWeight: '800', cursor: 'pointer' }}
                >
                  Đã tiếp nhận hàng về kho
                </button>

                <div style={{ borderTop: '1px solid #f1f5f9', paddingTop: '12px', marginTop: '6px' }}>
                  <label style={{ display: 'block', fontSize: '12px', fontWeight: '700', color: '#475569', marginBottom: '6px' }}>Kết quả kiểm định chất lượng (QC)</label>
                  <textarea
                    rows="3"
                    value={customerForm.qcNote}
                    onChange={(e) => updateCustomerForm('qcNote', e.target.value)}
                    style={{ width: '100%', padding: '8px 10px', border: '1px solid #cbd5e1', borderRadius: '6px', fontSize: '12.5px', outline: 'none', resize: 'none', marginBottom: '8px' }}
                  />
                  <div style={{ display: 'flex', gap: '8px' }}>
                    <button
                      type="button"
                      onClick={() => runAction(() => inspectCustomerReturn(selectedCustomerReturn.id, { isPassed: true, qcNote: customerForm.qcNote }), 'Đã QC đạt yêu cầu.')}
                      style={{ flex: 1, padding: '9px', background: '#16a34a', color: '#ffffff', border: 'none', borderRadius: '6px', fontSize: '12.5px', fontWeight: '700', cursor: 'pointer' }}
                    >
                      Đạt QC
                    </button>
                    <button
                      type="button"
                      onClick={() => runAction(() => inspectCustomerReturn(selectedCustomerReturn.id, { isPassed: false, qcNote: customerForm.qcNote }), 'Đã QC từ chối.')}
                      style={{ flex: 1, padding: '9px', background: '#ffffff', color: '#dc2626', border: '1px solid #fecaca', borderRadius: '6px', fontSize: '12.5px', fontWeight: '700', cursor: 'pointer' }}
                    >
                      Không đạt QC
                    </button>
                  </div>
                </div>
              </div>
            </div>
          )}

          {/* INTERACTIVE SUPPLIER INSPECTION FORM (REPLACES RAW JSON TEXTAREA) */}
          {selectedSupplierReturn && (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <span style={{ fontSize: '13.5px', fontWeight: '700', color: '#9333ea' }}>Đồng kiểm Nhà cung cấp #{selectedSupplierReturn.id}</span>
                <button
                  type="button"
                  onClick={() => {
                    setSelectedSupplierReturn(null);
                    setSupplierInspectionItems([]);
                  }}
                  style={{ border: 'none', background: '#f1f5f9', borderRadius: '4px', width: '22px', height: '22px', cursor: 'pointer', fontWeight: '700' }}
                >
                  ✕
                </button>
              </div>

              <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                
                {/* Table of items to inspect */}
                <div style={{ border: '1px solid #e2e8f0', borderRadius: '8px', background: '#fafafa', overflow: 'hidden' }}>
                  <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '12px', textAlign: 'left' }}>
                    <thead style={{ background: '#f1f5f9' }}>
                      <tr>
                        <th style={{ padding: '8px' }}>Sản phẩm</th>
                        <th style={{ padding: '8px', width: '60px', textAlign: 'right' }}>SL Trả</th>
                        <th style={{ padding: '8px', width: '70px' }}>SL Nhận</th>
                        <th style={{ padding: '8px', minWidth: '120px' }}>Lý do chênh lệch</th>
                      </tr>
                    </thead>
                    <tbody>
                      {supplierInspectionItems.length === 0 ? (
                        <tr>
                          <td colSpan="4" style={{ padding: '16px', textAlign: 'center', color: '#94a3b8' }}>
                            Phiếu trả không có sản phẩm.
                          </td>
                        </tr>
                      ) : (
                        supplierInspectionItems.map((item) => (
                          <tr key={item.itemId} style={{ borderBottom: '1px solid #e2e8f0', background: '#ffffff' }}>
                            <td style={{ padding: '8px', fontWeight: '600', color: '#334155' }}>
                              {item.productName}
                            </td>
                            <td style={{ padding: '8px', textAlign: 'right', fontWeight: '700' }}>
                              {item.quantity}
                            </td>
                            <td style={{ padding: '6px 8px' }}>
                              <input
                                type="number"
                                min="0"
                                max={item.quantity}
                                value={item.acceptedQuantity}
                                onChange={(e) => handleUpdateInspectionItem(item.itemId, 'acceptedQuantity', Number(e.target.value))}
                                style={{ width: '100%', padding: '4px 6px', border: '1px solid #cbd5e1', borderRadius: '4px', fontSize: '11.5px' }}
                              />
                            </td>
                            <td style={{ padding: '6px 8px' }}>
                              <input
                                type="text"
                                placeholder="Ghi lý do..."
                                value={item.discrepancyReason}
                                onChange={(e) => handleUpdateInspectionItem(item.itemId, 'discrepancyReason', e.target.value)}
                                style={{ width: '100%', padding: '4px 6px', border: '1px solid #cbd5e1', borderRadius: '4px', fontSize: '11.5px' }}
                              />
                            </td>
                          </tr>
                        ))
                      )}
                    </tbody>
                  </table>
                </div>

                <button
                  type="button"
                  disabled={supplierInspectionItems.length === 0}
                  onClick={() =>
                    runAction(
                      () =>
                        inspectSupplierReturn(selectedSupplierReturn.id, {
                          items: supplierInspectionItems.map((item) => ({
                            itemId: item.itemId,
                            acceptedQuantity: item.acceptedQuantity,
                            discrepancyReason: item.discrepancyReason.trim(),
                          })),
                        }),
                      'Đã hoàn tất đồng kiểm hàng nhận về kho thành công.'
                    )
                  }
                  style={{ width: '100%', padding: '11px', background: '#9333ea', color: '#ffffff', border: 'none', borderRadius: '6px', fontSize: '13px', fontWeight: '800', cursor: 'pointer', marginTop: '6px' }}
                >
                  Xác nhận kết quả đồng kiểm QC
                </button>
              </div>
            </div>
          )}

          {!selectedCustomerReturn && !selectedSupplierReturn && (
            <div style={{ textAlign: 'center', color: '#94a3b8', fontSize: '13px', padding: '24px 0' }}>
              Vui lòng chọn 1 phiếu trả hàng từ danh sách bên trái để thực hiện kiểm QC / xử lý nhanh.
            </div>
          )}
        </aside>

      </div>

      {/* FOOTER */}
      <footer style={{ textAlign: 'center', marginTop: '30px', padding: '16px 0', borderTop: '1px solid #cbd5e1', fontSize: '12px', color: '#94a3b8' }}>
        © 2026 ToyStore Admin Panel
      </footer>
    </section>
  );
}

export default AdminReturnInspectionPage;
