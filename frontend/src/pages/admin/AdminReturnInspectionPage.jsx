import { useEffect, useState } from 'react';
import {
  finalizeCustomerReturnRefund,
  getCustomerReturns,
  inspectCustomerReturn,
  receiveCustomerReturn,
  requireCustomerReturnInfo,
  resolveCustomerReturnDispute,
} from '../../services/adminReturnService.js';
import { formatDateTime, formatPrice } from '../../utils/formatters.js';

// Status styling helper with rich modern color palette
function getReturnStatusInfo(status) {
  const code = status && typeof status === 'object' ? status.code : status;
  const statusStr = String(code || '').toUpperCase();

  switch (statusStr) {
    case 'COMPLETED':
    case 'SUCCESS':
    case 'SUCCEEDED':
    case 'RESOLVED':
      return { label: 'Đã hoàn thành', bg: '#ecfdf5', color: '#059669', border: '#a7f3d0' };
    case 'PENDING':
    case 'REQUESTED':
      return { label: 'Chờ tiếp nhận', bg: '#fffbe6', color: '#d97706', border: '#ffe58f' };
    case 'RECEIVED':
      return { label: 'Đã tiếp nhận kho', bg: '#eff6ff', color: '#2563eb', border: '#bfdbfe' };
    case 'INSPECTED':
      return { label: 'Đã kiểm định', bg: '#f3e8ff', color: '#7e22ce', border: '#d8b4fe' };
    case 'REJECTED':
    case 'FAILED':
      return { label: 'Từ chối yêu cầu', bg: '#fef2f2', color: '#dc2626', border: '#fecaca' };
    case 'RETURNING':
      return { label: 'Đang vận chuyển hoàn', bg: '#fff7ed', color: '#c2410c', border: '#ffedd5' };
    case 'SHIPPING_FAILED':
      return { label: 'Giao vận thất bại', bg: '#fff1f2', color: '#e11d48', border: '#fecdd3' };
    case 'APPROVED':
      return { label: 'Đã duyệt trả hàng', bg: '#f0f9ff', color: '#0284c7', border: '#bae6fd' };
    case 'NEEDS_MORE_INFO':
      return { label: 'Cần bổ sung thông tin', bg: '#fff7ed', color: '#ea580c', border: '#ffedd5' };
    case 'INSPECTED_OK':
      return { label: 'QC Đạt (Nhập kho)', bg: '#ecfdf5', color: '#059669', border: '#a7f3d0' };
    case 'INSPECTED_FAILED':
      return { label: 'QC Không đạt', bg: '#fef2f2', color: '#dc2626', border: '#fecaca' };
    case 'DISPUTED':
      return { label: 'Đang tranh chấp CSKH', bg: '#f3e8ff', color: '#7c3aed', border: '#ddd6fe' };
    case 'REFUND_PENDING':
      return { label: 'Đang chờ hoàn tiền', bg: '#fff7ed', color: '#c2410c', border: '#fed7aa' };
    case 'REFUNDED':
      return { label: 'Đã hoàn tiền', bg: '#ecfdf5', color: '#059669', border: '#a7f3d0' };
    case 'REPLACED':
      return { label: 'Đã đổi hàng', bg: '#eff6ff', color: '#2563eb', border: '#bfdbfe' };
    case 'CANCELLED':
      return { label: 'Đã hủy yêu cầu', bg: '#f1f5f9', color: '#64748b', border: '#e2e8f0' };
    default:
      return { label: statusStr || 'Chờ xử lý', bg: '#f1f5f9', color: '#475569', border: '#cbd5e1' };
  }
}

function AdminReturnInspectionPage() {
  const [customerReturns, setCustomerReturns] = useState([]);
  const [customerTotalElements, setCustomerTotalElements] = useState(0);
  const [selectedCustomerReturn, setSelectedCustomerReturn] = useState(null);

  const [customerForm, setCustomerForm] = useState({
    adminMessage: '',
    isPassed: true,
    qcNote: '',
    isApproved: true,
    resolutionNote: '',
    refundNote: '',
  });

  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  // Pagination states
  const [customerPage, setCustomerPage] = useState(0);
  const [customerTotalPages, setCustomerTotalPages] = useState(1);

  // Filter states for Customer Returns
  const [customerFilters, setCustomerFilters] = useState({
    status: 'all',
    orderId: '',
  });

  const [activeCustomerFilters, setActiveCustomerFilters] = useState({
    status: 'all',
    orderId: '',
  });

  useEffect(() => {
    loadCustomerData();
  }, [customerPage, activeCustomerFilters]);

  async function loadCustomerData() {
    setLoading(true);
    setError('');
    try {
      const filters = { page: customerPage, size: 10, sort: 'createdAt,desc' };
      if (activeCustomerFilters.status && activeCustomerFilters.status !== 'all') {
        filters.status = activeCustomerFilters.status;
      }
      if (activeCustomerFilters.orderId) {
        filters.orderId = activeCustomerFilters.orderId.trim();
      }

      const res = await getCustomerReturns(filters);
      setCustomerReturns(res?.content || res || []);
      setCustomerTotalElements(Number(res?.totalElements ?? (Array.isArray(res) ? res.length : 0)));
      setCustomerTotalPages(res?.totalPages || 1);
    } catch (err) {
      setError(err?.message || 'Không thể tải danh sách phiếu trả hàng khách hàng.');
    } finally {
      setLoading(false);
    }
  }

  function handleFilterCustomer() {
    setCustomerPage(0);
    setActiveCustomerFilters({ ...customerFilters });
  }

  function handleClearFilterCustomer() {
    const defaultFilters = { status: 'all', orderId: '' };
    setCustomerFilters(defaultFilters);
    setActiveCustomerFilters(defaultFilters);
    setCustomerPage(0);
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
      await loadCustomerData();
      setSelectedCustomerReturn(null);
    } catch (err) {
      setError(err?.message || 'Thao tác kiểm định thất bại. Vui lòng kiểm tra lại.');
    }
  }

  const selectedItem = selectedCustomerReturn;

  return (
    <section style={{ padding: '28px', background: '#f8fafc', minHeight: '100vh', fontFamily: 'Inter, system-ui, -apple-system, sans-serif' }}>
      
      {/* HERO HEADER BANNER */}
      <div
        style={{
          background: 'linear-gradient(135deg, #0f172a 0%, #1e293b 100%)',
          borderRadius: '24px',
          padding: '24px 32px',
          marginBottom: '28px',
          color: '#ffffff',
          boxShadow: '0 10px 30px rgba(15,23,42,0.12)',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          flexWrap: 'wrap',
          gap: '20px',
        }}
      >
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '6px' }}>
            <h1 style={{ fontSize: '22px', fontWeight: '900', margin: 0, letterSpacing: '-0.5px' }}>
              Trung tâm Kiểm định Hàng trả (QC)
            </h1>
          </div>
          <p style={{ margin: 0, fontSize: '13.5px', color: '#94a3b8', fontWeight: '500' }}>
            Quản lý tiếp nhận hoàn kho, kiểm tra chất lượng sản phẩm & giải quyết bồi thường khách hàng / nhà cung cấp
          </p>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
          <button
            type="button"
            onClick={() => {
              loadCustomerData();
            }}
            style={{
              background: 'rgba(255,255,255,0.1)',
              color: '#ffffff',
              border: '1px solid rgba(255,255,255,0.2)',
              padding: '10px 18px',
              borderRadius: '14px',
              fontSize: '13px',
              fontWeight: '800',
              cursor: 'pointer',
              display: 'inline-flex',
              alignItems: 'center',
              gap: '8px',
              backdropFilter: 'blur(10px)',
              transition: 'all 0.2s ease',
            }}
            onMouseEnter={(e) => {
              e.currentTarget.style.background = '#ea580c';
              e.currentTarget.style.borderColor = '#ea580c';
            }}
            onMouseLeave={(e) => {
              e.currentTarget.style.background = 'rgba(255,255,255,0.1)';
              e.currentTarget.style.borderColor = 'rgba(255,255,255,0.2)';
            }}
          >
            Làm mới dữ liệu
          </button>
        </div>
      </div>

      {/* ALERTS */}
      {error && (
        <div
          style={{
            background: '#fef2f2',
            color: '#dc2626',
            border: '1px solid #fecaca',
            padding: '14px 18px',
            borderRadius: '16px',
            marginBottom: '20px',
            fontSize: '13.5px',
            fontWeight: '700',
            display: 'flex',
            alignItems: 'center',
            gap: '10px',
            boxShadow: '0 4px 12px rgba(220,38,38,0.06)',
          }}
        >
          <div style={{ flex: 1 }}>{error}</div>
          <button onClick={() => setError('')} style={{ border: 'none', background: 'transparent', cursor: 'pointer', color: '#dc2626', fontWeight: '800' }}>✕</button>
        </div>
      )}

      {message && (
        <div
          style={{
            background: '#f0fdf4',
            color: '#16a34a',
            border: '1px solid #bbf7d0',
            padding: '14px 18px',
            borderRadius: '16px',
            marginBottom: '20px',
            fontSize: '13.5px',
            fontWeight: '700',
            display: 'flex',
            alignItems: 'center',
            gap: '10px',
            boxShadow: '0 4px 12px rgba(22,163,74,0.06)',
          }}
        >
          <div style={{ flex: 1 }}>{message}</div>
          <button onClick={() => setMessage('')} style={{ border: 'none', background: 'transparent', cursor: 'pointer', color: '#16a34a', fontWeight: '800' }}>✕</button>
        </div>
      )}

      {/* MAIN TWO-COLUMN GRID */}
      <div style={{ display: 'grid', gridTemplateColumns: selectedItem ? '1fr 420px' : '1fr', gap: '28px', alignItems: 'start' }}>
        
        {/* LEFT COLUMN: TABLE PANEL */}
        <div>
          {(
            <div style={{ background: '#ffffff', padding: '24px', borderRadius: '24px', border: '1px solid #f1f5f9', boxShadow: '0 4px 20px rgba(0,0,0,0.02)' }}>
              
              {/* FILTER BAR */}
              <div style={{ display: 'flex', gap: '12px', marginBottom: '20px', alignItems: 'center', flexWrap: 'wrap' }}>
                <div style={{ position: 'relative', flex: '1', minWidth: '200px' }}>
                  <input
                    type="text"
                    placeholder="Tìm theo Mã ĐH (VD: 16, DH16)..."
                    value={customerFilters.orderId}
                    onChange={(e) => setCustomerFilters({ ...customerFilters, orderId: e.target.value })}
                    onKeyDown={(e) => e.key === 'Enter' && handleFilterCustomer()}
                    style={{
                      width: '100%',
                      padding: '10px 14px',
                      borderRadius: '12px',
                      border: '1px solid #cbd5e1',
                      fontSize: '13px',
                      outline: 'none',
                      boxSizing: 'border-box',
                    }}
                  />
                </div>

                <select
                  value={customerFilters.status}
                  onChange={(e) => setCustomerFilters({ ...customerFilters, status: e.target.value })}
                  style={{
                    padding: '10px 14px',
                    borderRadius: '12px',
                    border: '1px solid #cbd5e1',
                    fontSize: '13px',
                    outline: 'none',
                    background: '#fff',
                    fontWeight: '600',
                  }}
                >
                  <option value="all">Tất cả trạng thái</option>
                  <option value="REQUESTED">Chờ shop tiếp nhận</option>
                  <option value="NEEDS_MORE_INFO">Cần bổ sung thông tin</option>
                  <option value="APPROVED">Đã duyệt chấp nhận</option>
                  <option value="RETURNING">Đang vận chuyển hoàn về</option>
                  <option value="RECEIVED">Đã tiếp nhận hàng tại kho</option>
                  <option value="INSPECTED_OK">Kiểm định QC Đạt</option>
                  <option value="INSPECTED_FAILED">Kiểm định QC Không đạt</option>
                  <option value="DISPUTED">Đang tranh chấp CSKH</option>
                  <option value="REFUND_PENDING">Đang chờ hoàn tiền</option>
                  <option value="REFUNDED">Đã hoàn tiền thành công</option>
                  <option value="REJECTED">Từ chối yêu cầu</option>
                </select>

                <button
                  type="button"
                  onClick={handleFilterCustomer}
                  style={{
                    background: '#2563eb',
                    color: '#ffffff',
                    border: 'none',
                    padding: '10px 18px',
                    borderRadius: '12px',
                    fontSize: '13px',
                    fontWeight: '800',
                    cursor: 'pointer',
                    boxShadow: '0 4px 12px rgba(37,99,235,0.2)',
                  }}
                >
                  Lọc kết quả
                </button>

                <button
                  type="button"
                  onClick={handleClearFilterCustomer}
                  style={{
                    background: '#f1f5f9',
                    color: '#475569',
                    border: '1px solid #cbd5e1',
                    padding: '10px 16px',
                    borderRadius: '12px',
                    fontSize: '13px',
                    fontWeight: '700',
                    cursor: 'pointer',
                  }}
                >
                  Đặt lại
                </button>
              </div>

              {/* TABLE */}
              <div style={{ overflowX: 'auto', borderRadius: '16px', border: '1px solid #f1f5f9' }}>
                <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '13px' }}>
                  <thead>
                    <tr style={{ background: '#f8fafc', color: '#475569', fontWeight: '800', fontSize: '12px', borderBottom: '1px solid #e2e8f0', textTransform: 'uppercase', letterSpacing: '0.5px' }}>
                      <th style={{ padding: '14px 16px', width: '80px' }}>Phiếu #</th>
                      <th style={{ padding: '14px 16px', width: '110px' }}>Đơn hàng</th>
                      <th style={{ padding: '14px 16px', width: '160px' }}>Trạng thái QC</th>
                      <th style={{ padding: '14px 16px' }}>Ngày tạo yêu cầu</th>
                      <th style={{ padding: '14px 16px', width: '130px', textAlign: 'center' }}>Hành động</th>
                    </tr>
                  </thead>
                  <tbody>
                    {customerReturns.length === 0 ? (
                      <tr>
                        <td colSpan="5" style={{ padding: '32px', textAlign: 'center', color: '#94a3b8', fontSize: '14px' }}>
                          🍃 Chưa tìm thấy phiếu trả hàng nào phù hợp với bộ lọc.
                        </td>
                      </tr>
                    ) : (
                      customerReturns.map((item) => {
                        const statusInfo = getReturnStatusInfo(item.status);
                        const isSelected = selectedCustomerReturn?.id === item.id;

                        return (
                          <tr
                            key={item.id}
                            onClick={() => {
                              setSelectedCustomerReturn(item);
                            }}
                            style={{
                              borderBottom: '1px solid #f1f5f9',
                              background: isSelected ? '#fff7ed' : '#ffffff',
                              cursor: 'pointer',
                              transition: 'all 0.15s ease',
                            }}
                          >
                            <td style={{ padding: '14px 16px', fontWeight: '800', color: '#0f172a' }}>
                              #{item.id}
                            </td>
                            <td style={{ padding: '14px 16px' }}>
                              <span style={{ fontWeight: '900', color: '#ea580c', background: '#fff7ed', border: '1px solid #ffedd5', padding: '3px 8px', borderRadius: '8px', fontSize: '12px' }}>
                                DH{item.orderId}
                              </span>
                            </td>
                            <td style={{ padding: '14px 16px' }}>
                              <span
                                style={{
                                  background: statusInfo.bg,
                                  color: statusInfo.color,
                                  border: `1px solid ${statusInfo.border}`,
                                  padding: '4px 10px',
                                  borderRadius: '20px',
                                  fontSize: '12px',
                                  fontWeight: '800',
                                  display: 'inline-flex',
                                  alignItems: 'center',
                                }}
                              >
                                <span>{statusInfo.label}</span>
                              </span>
                            </td>
                            <td style={{ padding: '14px 16px', color: '#64748b', fontWeight: '500' }}>
                              {formatDateTime(item.createdAt)}
                            </td>
                            <td style={{ padding: '14px 16px', textAlign: 'center' }}>
                              <button
                                type="button"
                                style={{
                                  padding: '6px 14px',
                                  background: isSelected ? '#ea580c' : '#ffffff',
                                  color: isSelected ? '#ffffff' : '#ea580c',
                                  border: '1px solid #ea580c',
                                  borderRadius: '10px',
                                  cursor: 'pointer',
                                  fontSize: '12.5px',
                                  fontWeight: '800',
                                  transition: 'all 0.15s ease',
                                }}
                              >
                                {isSelected ? 'Đang chọn' : 'Kiểm định'}
                              </button>
                            </td>
                          </tr>
                        );
                      })
                    )}
                  </tbody>
                </table>
              </div>

              {/* PAGINATION */}
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '20px', padding: '0 4px' }}>
                <span style={{ fontSize: '13px', color: '#64748b', fontWeight: '600' }}>
                  Hiển thị {customerReturns.length} / {customerTotalElements} kết quả (Trang {customerPage + 1} / {customerTotalPages || 1})
                </span>

                <div style={{ display: 'flex', gap: '8px' }}>
                  <button
                    disabled={customerPage === 0}
                    onClick={() => setCustomerPage((p) => p - 1)}
                    style={{
                      padding: '8px 16px',
                      borderRadius: '10px',
                      border: '1px solid #cbd5e1',
                      background: customerPage === 0 ? '#f1f5f9' : '#ffffff',
                      color: customerPage === 0 ? '#94a3b8' : '#0f172a',
                      fontWeight: '700',
                      cursor: customerPage === 0 ? 'not-allowed' : 'pointer',
                    }}
                  >
                    ◄ Trang trước
                  </button>

                  <button
                    disabled={customerPage >= customerTotalPages - 1}
                    onClick={() => setCustomerPage((p) => p + 1)}
                    style={{
                      padding: '8px 16px',
                      borderRadius: '10px',
                      border: '1px solid #cbd5e1',
                      background: customerPage >= customerTotalPages - 1 ? '#f1f5f9' : '#ffffff',
                      color: customerPage >= customerTotalPages - 1 ? '#94a3b8' : '#0f172a',
                      fontWeight: '700',
                      cursor: customerPage >= customerTotalPages - 1 ? 'not-allowed' : 'pointer',
                    }}
                  >
                    Trang sau ►
                  </button>
                </div>
              </div>

            </div>
          )}
        </div>

        {/* RIGHT COLUMN: SMART QC INSPECTION PANEL */}
        {selectedItem && (
          <aside
            style={{
              background: '#ffffff',
              borderRadius: '24px',
              border: '1px solid #e2e8f0',
              padding: '24px',
              boxShadow: '0 12px 32px rgba(0,0,0,0.06)',
              position: 'sticky',
              top: '24px',
            }}
          >
            {/* PANEL HEADER */}
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px', paddingBottom: '14px', borderBottom: '1px solid #f1f5f9' }}>
              <div>
                <span style={{ fontSize: '11px', fontWeight: '900', color: '#ea580c', textTransform: 'uppercase', letterSpacing: '0.5px' }}>
                  Bảng kiểm định Khách hàng
                </span>
                <h3 style={{ fontSize: '18px', fontWeight: '900', color: '#0f172a', margin: '2px 0 0 0' }}>
                  Phiếu #{selectedItem.id} {`(ĐH${selectedCustomerReturn.orderId})`}
                </h3>
              </div>

              <button
                type="button"
                onClick={() => {
                  setSelectedCustomerReturn(null);
                }}
                style={{
                  border: 'none',
                  background: '#f1f5f9',
                  color: '#64748b',
                  borderRadius: '50%',
                  width: '32px',
                  height: '32px',
                  cursor: 'pointer',
                  fontWeight: '800',
                  fontSize: '14px',
                }}
              >
                ✕
              </button>
            </div>

            {/* CUSTOMER INSPECTION ACTIONS */}
            {selectedCustomerReturn && (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
                
                {/* STEP 1: WAREHOUSE RECEIPT */}
                <div style={{ background: '#f8fafc', borderRadius: '16px', padding: '16px', border: '1px solid #e2e8f0' }}>
                  <div style={{ fontSize: '13px', fontWeight: '800', color: '#2563eb', marginBottom: '10px', display: 'flex', alignItems: 'center', gap: '6px' }}>
                    <span>Bước 1: Tiếp nhận hàng về kho</span>
                  </div>

                  <div style={{ marginBottom: '10px' }}>
                    <label style={{ display: 'block', fontSize: '11.5px', fontWeight: '700', color: '#64748b', marginBottom: '4px' }}>
                      Ghi chú / Nhắn tin khách hàng
                    </label>
                    <input
                      type="text"
                      placeholder="Ví dụ: Vui lòng bổ sung video mở gói hàng..."
                      value={customerForm.adminMessage}
                      onChange={(e) => updateCustomerForm('adminMessage', e.target.value)}
                      style={{ width: '100%', padding: '8px 12px', border: '1px solid #cbd5e1', borderRadius: '10px', fontSize: '12.5px', outline: 'none', boxSizing: 'border-box' }}
                    />
                  </div>

                  <div style={{ display: 'flex', gap: '8px' }}>
                    <button
                      type="button"
                      onClick={() => runAction(() => receiveCustomerReturn(selectedCustomerReturn.id), 'Đã tiếp nhận hàng về kho thành công!')}
                      style={{
                        flex: 1,
                        padding: '10px',
                        background: '#2563eb',
                        color: '#ffffff',
                        border: 'none',
                        borderRadius: '10px',
                        fontSize: '12.5px',
                        fontWeight: '800',
                        cursor: 'pointer',
                        boxShadow: '0 4px 12px rgba(37,99,235,0.2)',
                      }}
                    >
                      Đã nhận về kho
                    </button>
                    <button
                      type="button"
                      onClick={() => runAction(() => requireCustomerReturnInfo(selectedCustomerReturn.id, customerForm.adminMessage), 'Đã gửi yêu cầu bổ sung thông tin.')}
                      style={{
                        padding: '10px 14px',
                        background: '#ffffff',
                        color: '#475569',
                        border: '1px solid #cbd5e1',
                        borderRadius: '10px',
                        fontSize: '12.5px',
                        fontWeight: '700',
                        cursor: 'pointer',
                      }}
                    >
                      Yêu cầu ảnh
                    </button>
                  </div>
                </div>

                {/* STEP 2: QC INSPECTION RESULT */}
                <div style={{ background: '#f8fafc', borderRadius: '16px', padding: '16px', border: '1px solid #e2e8f0' }}>
                  <div style={{ fontSize: '13px', fontWeight: '800', color: '#059669', marginBottom: '10px', display: 'flex', alignItems: 'center', gap: '6px' }}>
                    <span>Bước 2: Đánh giá chất lượng (QC)</span>
                  </div>

                  <textarea
                    rows="3"
                    placeholder="Nhập ghi chú chi tiết về tình trạng sản phẩm hoàn trả..."
                    value={customerForm.qcNote}
                    onChange={(e) => updateCustomerForm('qcNote', e.target.value)}
                    style={{
                      width: '100%',
                      padding: '10px',
                      border: '1px solid #cbd5e1',
                      borderRadius: '10px',
                      fontSize: '12.5px',
                      outline: 'none',
                      resize: 'none',
                      marginBottom: '10px',
                      boxSizing: 'border-box',
                    }}
                  />

                  <div style={{ display: 'flex', gap: '8px' }}>
                    <button
                      type="button"
                      onClick={() => runAction(() => inspectCustomerReturn(selectedCustomerReturn.id, { isPassed: true, qcNote: customerForm.qcNote }), 'Đã xác nhận QC Đạt (Cho phép nhập kho & hoàn tiền)!')}
                      style={{
                        flex: 1,
                        padding: '10px',
                        background: '#16a34a',
                        color: '#ffffff',
                        border: 'none',
                        borderRadius: '10px',
                        fontSize: '12.5px',
                        fontWeight: '800',
                        cursor: 'pointer',
                        boxShadow: '0 4px 12px rgba(22,163,74,0.2)',
                      }}
                    >
                      QC ĐẠT
                    </button>

                    <button
                      type="button"
                      onClick={() => runAction(() => inspectCustomerReturn(selectedCustomerReturn.id, { isPassed: false, qcNote: customerForm.qcNote }), 'Đã ghi nhận QC Không đạt (Từ chối).')}
                      style={{
                        flex: 1,
                        padding: '10px',
                        background: '#ffffff',
                        color: '#dc2626',
                        border: '1px solid #fecaca',
                        borderRadius: '10px',
                        fontSize: '12.5px',
                        fontWeight: '800',
                        cursor: 'pointer',
                      }}
                    >
                      Không đạt QC
                    </button>
                  </div>
                </div>

                {/* STEP 3: DISPUTE RESOLUTION */}
                <div style={{ background: '#faf5ff', borderRadius: '16px', padding: '16px', border: '1px solid #f3e8ff' }}>
                  <div style={{ fontSize: '13px', fontWeight: '800', color: '#7c3aed', marginBottom: '10px', display: 'flex', alignItems: 'center', gap: '6px' }}>
                    <span>Bước 3: Tranh chấp & Khiếu nại</span>
                  </div>

                  <div style={{ display: 'flex', alignItems: 'center', marginBottom: '8px' }}>
                    <input
                      type="checkbox"
                      id="disputeCheck"
                      checked={customerForm.isApproved}
                      onChange={(e) => updateCustomerForm('isApproved', e.target.checked)}
                      style={{ width: '16px', height: '16px', cursor: 'pointer', accentColor: '#7c3aed' }}
                    />
                    <label htmlFor="disputeCheck" style={{ fontSize: '12.5px', fontWeight: '700', color: '#4c1d95', marginLeft: '8px', cursor: 'pointer' }}>
                      Chấp nhận phương án bồi thường cho khách hàng
                    </label>
                  </div>

                  <textarea
                    rows="2"
                    placeholder="Nội dung giải quyết khiếu nại..."
                    value={customerForm.resolutionNote}
                    onChange={(e) => updateCustomerForm('resolutionNote', e.target.value)}
                    style={{ width: '100%', padding: '8px 10px', border: '1px solid #ddd6fe', borderRadius: '10px', fontSize: '12.5px', outline: 'none', resize: 'none', marginBottom: '8px', boxSizing: 'border-box' }}
                  />

                  <button
                    type="button"
                    onClick={() => runAction(() => resolveCustomerReturnDispute(selectedCustomerReturn.id, { isApproved: customerForm.isApproved, resolutionNote: customerForm.resolutionNote }), 'Đã cập nhật phương án tranh chấp.')}
                    style={{ width: '100%', padding: '10px', background: '#7c3aed', color: '#ffffff', border: 'none', borderRadius: '10px', fontSize: '12.5px', fontWeight: '800', cursor: 'pointer', boxShadow: '0 4px 12px rgba(124,58,237,0.2)' }}
                  >
                    Giải quyết tranh chấp
                  </button>
                </div>

                {/* STEP 4: REFUND FINALIZATION */}
                <div style={{ background: '#ecfdf5', borderRadius: '16px', padding: '16px', border: '1px solid #a7f3d0' }}>
                  <div style={{ fontSize: '13px', fontWeight: '800', color: '#059669', marginBottom: '10px', display: 'flex', alignItems: 'center', gap: '6px' }}>
                    <span>Bước 4: Hoàn tiền đơn hàng</span>
                  </div>

                  <textarea
                    rows="2"
                    placeholder="Ghi chú hoàn tiền..."
                    value={customerForm.refundNote}
                    onChange={(e) => updateCustomerForm('refundNote', e.target.value)}
                    style={{ width: '100%', padding: '8px 10px', border: '1px solid #a7f3d0', borderRadius: '10px', fontSize: '12.5px', outline: 'none', resize: 'none', marginBottom: '8px', boxSizing: 'border-box' }}
                  />

                  <button
                    type="button"
                    onClick={() => runAction(() => finalizeCustomerReturnRefund(selectedCustomerReturn.id, { refundNote: customerForm.refundNote }), 'Đã tạo yêu cầu hoàn tiền. Trạng thái sẽ hoàn tất khi giao dịch thành công.')}
                    style={{ width: '100%', padding: '10px', background: '#059669', color: '#ffffff', border: 'none', borderRadius: '10px', fontSize: '13px', fontWeight: '900', cursor: 'pointer', boxShadow: '0 4px 12px rgba(5,150,105,0.2)' }}
                  >
                    TẠO YÊU CẦU HOÀN TIỀN
                  </button>
                </div>

              </div>
            )}
          </aside>
        )}
      </div>

    </section>
  );
}

export default AdminReturnInspectionPage;
