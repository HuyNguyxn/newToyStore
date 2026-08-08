import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import BackLink from '../../components/common/BackLink.jsx';
import { cancelCustomerReturn, disputeCustomerReturn, getCustomerReturns, updateCustomerReturnInfo } from '../../services/customerReturnService.js';
import { formatDateTime, formatPrice } from '../../utils/formatters.js';

const getReturnStatusBadge = (statusObj) => {
  const code = (typeof statusObj === 'string' ? statusObj : statusObj?.code || statusObj?.name || '').toUpperCase();
  const displayName = statusObj?.displayName;

  if (code.includes('REQUESTED') || code.includes('PENDING')) {
    return { label: displayName || 'Chờ shop tiếp nhận', bg: '#fef3c7', color: '#b45309', icon: '⏳' };
  }
  if (code.includes('APPROVED') || code.includes('ACCEPTED') || code.includes('IN_PROGRESS')) {
    return { label: displayName || 'Đã chấp nhận · Đang xử lý', bg: '#eff6ff', color: '#1d4ed8', icon: '📦' };
  }
  if (code.includes('REFUNDED') || code.includes('COMPLETED')) {
    return { label: displayName || 'Đã hoàn tiền thành công', bg: '#dcfce7', color: '#15803d', icon: '💰' };
  }
  if (code.includes('REJECTED') || code.includes('CANCELLED')) {
    return { label: displayName || 'Bị từ chối / Đã hủy', bg: '#fee2e2', color: '#b91c1c', icon: '❌' };
  }
  if (code.includes('DISPUTED')) {
    return { label: displayName || 'Đang tranh chấp CSKH', bg: '#f3e8ff', color: '#7e22ce', icon: '⚖️' };
  }
  return { label: displayName || code || 'Đang cập nhật', bg: '#f1f5f9', color: '#475569', icon: '📌' };
};

const returnReasonLabels = {
  CHANGED_MIND: 'Đổi ý / Không còn nhu cầu 🔄',
  CUSTOMER_CHANGED_MIND: 'Đổi ý / Không còn nhu cầu 🔄',
  DEFECTIVE: 'Sản phẩm bị lỗi / Hỏng do nhà sản xuất 💔',
  DAMAGED: 'Sản phẩm bị hỏng / Vỡ 💔',
  DAMAGED_IN_TRANSIT: 'Bưu kiện hư hỏng do vận chuyển 🚚',
  WRONG_ITEM: 'Giao sai sản phẩm / Khác mẫu mã ❌',
  NOT_AS_DESCRIBED: 'Sản phẩm không đúng như mô tả 📝',
};

const getReasonLabel = (reasonVal) => {
  if (!reasonVal) return 'Đổi ý / Lý do khác 🔄';
  if (typeof reasonVal === 'object') {
    if (reasonVal.description) return reasonVal.description;
    reasonVal = reasonVal.code || reasonVal.name || '';
  }
  const codeStr = String(reasonVal).toUpperCase();
  if (returnReasonLabels[codeStr]) return returnReasonLabels[codeStr];
  if (codeStr.includes('MIND') || codeStr.includes('CHANGE')) return 'Đổi ý / Không nhu cầu 🔄';
  if (codeStr.includes('DEFECT') || codeStr.includes('ERROR')) return 'Sản phẩm bị lỗi nhà sản xuất 💔';
  if (codeStr.includes('WRONG')) return 'Giao sai sản phẩm / Mẫu mã ❌';
  if (codeStr.includes('DAMAGE')) return 'Bưu kiện bị hư hỏng khi vận chuyển 🚚';
  return codeStr;
};

function ReturnListPage() {
  const [returnsList, setReturnsList] = useState([]);
  const [selected, setSelected] = useState(null);
  const [filters, setFilters] = useState({ status: '', orderId: '' });

  // Action Inputs
  const [reasonNote, setReasonNote] = useState('');
  const [disputeReason, setDisputeReason] = useState('');

  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    loadReturns();
  }, []);

  async function loadReturns() {
    setLoading(true);
    setError('');
    try {
      const cleanFilters = {};
      if (filters.status) cleanFilters.status = filters.status;
      if (filters.orderId) cleanFilters.orderId = filters.orderId;

      const result = await getCustomerReturns({ ...cleanFilters, page: 0, size: 50, sort: 'createdAt,desc' });
      const list = result.content || (Array.isArray(result) ? result : []);
      setReturnsList(list);
    } catch (err) {
      setError(err.message || 'Không thể tải danh sách yêu cầu đổi trả.');
    } finally {
      setLoading(false);
    }
  }

  function selectReturn(item) {
    setSelected(item);
    setReasonNote(item.reasonNote || '');
    setDisputeReason('');
    setMessage('');
    setError('');
  }

  async function doAction(actionFn, successMsg) {
    setError('');
    setMessage('');
    setActionLoading(true);
    try {
      const result = await actionFn();
      setSelected(result || selected);
      setMessage(successMsg);
      await loadReturns();
    } catch (err) {
      setError(err.message || 'Thao tác đổi trả thất bại.');
    } finally {
      setActionLoading(false);
    }
  }

  return (
    <div className="container" style={{ padding: '24px 16px', maxWidth: '1200px', margin: '0 auto', fontFamily: 'system-ui, -apple-system, sans-serif' }}>
      <BackLink fallback="/profile" label="Quay lại tài khoản" />

      {/* HERO BANNER */}
      <div
        style={{
          background: 'linear-gradient(135deg, #ffffff 0%, #fff7ed 50%, #ffedd5 100%)',
          borderRadius: '20px',
          padding: '28px 32px',
          color: '#0f172a',
          marginBottom: '24px',
          border: '1px solid #fed7aa',
          boxShadow: '0 10px 25px rgba(234, 88, 12, 0.08)',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          flexWrap: 'wrap',
          gap: '20px',
          marginTop: '12px',
        }}
      >
        <div>
          <div style={{ display: 'inline-flex', alignItems: 'center', gap: '8px', background: '#ffedd5', color: '#ea580c', padding: '4px 12px', borderRadius: '20px', fontSize: '12px', fontWeight: '800', textTransform: 'uppercase', letterSpacing: '0.6px', marginBottom: '10px' }}>
            🔄 HẬU MÃI & HOÀN TIỀN
          </div>
          <h1 style={{ fontSize: '26px', fontWeight: '900', color: '#0f172a', margin: '0 0 6px 0', letterSpacing: '-0.5px' }}>
            Yêu cầu Đổi / Trả hàng của tôi
          </h1>
          <p style={{ margin: 0, color: '#475569', fontSize: '14px', fontWeight: '500' }}>
            Theo dõi tiến độ xử lý, cập nhật thông tin hoàn tiền hoặc khiếu nại CSKH
          </p>
        </div>

        <Link
          to="/returns/new"
          style={{
            padding: '12px 22px',
            fontSize: '14px',
            fontWeight: '800',
            color: '#ffffff',
            background: 'linear-gradient(135deg, #ea580c 0%, #c2410c 100%)',
            textDecoration: 'none',
            borderRadius: '14px',
            boxShadow: '0 6px 20px rgba(234, 88, 12, 0.25)',
            display: 'inline-flex',
            alignItems: 'center',
            gap: '8px',
            transition: 'all 0.2s ease',
          }}
        >
          🔄 Tạo yêu cầu mới
        </Link>
      </div>

      {error && (
        <div style={{ background: '#fef2f2', border: '1px solid #fecaca', color: '#dc2626', padding: '14px 18px', borderRadius: '12px', marginBottom: '20px', fontWeight: '600', fontSize: '14px' }}>
          ⚠️ {error}
        </div>
      )}

      {message && (
        <div style={{ background: '#f0fdf4', border: '1px solid #bbf7d0', color: '#15803d', padding: '14px 18px', borderRadius: '12px', marginBottom: '20px', fontWeight: '700', fontSize: '14px' }}>
          {message}
        </div>
      )}

      {/* FILTER & SEARCH BAR */}
      <div style={{ background: '#ffffff', borderRadius: '16px', padding: '16px 20px', border: '1px solid #e2e8f0', marginBottom: '24px', display: 'flex', gap: '16px', alignItems: 'center', flexWrap: 'wrap' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', flex: 1, minWidth: '220px' }}>
          <span style={{ fontSize: '13px', fontWeight: '800', color: '#475569' }}>Lọc Trạng Thái:</span>
          <select
            value={filters.status}
            onChange={(e) => setFilters((prev) => ({ ...prev, status: e.target.value }))}
            style={{ padding: '8px 12px', borderRadius: '10px', border: '1px solid #cbd5e1', fontSize: '13px', fontWeight: '700', color: '#0f172a', flex: 1 }}
          >
            <option value="">Tất cả trạng thái</option>
            <option value="REQUESTED">Chờ tiếp nhận</option>
            <option value="APPROVED">Đã chấp nhận</option>
            <option value="REFUNDED">Đã hoàn tiền</option>
            <option value="REJECTED">Từ chối / Hủy</option>
            <option value="DISPUTED">Đang tranh chấp</option>
          </select>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', flex: 1, minWidth: '220px' }}>
          <span style={{ fontSize: '13px', fontWeight: '800', color: '#475569' }}>Mã Đơn Hàng:</span>
          <input
            type="text"
            placeholder="Ví dụ: 17"
            value={filters.orderId}
            onChange={(e) => setFilters((prev) => ({ ...prev, orderId: e.target.value }))}
            style={{ padding: '8px 12px', borderRadius: '10px', border: '1px solid #cbd5e1', fontSize: '13px', fontWeight: '600', flex: 1 }}
          />
        </div>

        <button
          onClick={loadReturns}
          style={{ padding: '9px 18px', borderRadius: '10px', background: '#0f172a', color: '#ffffff', border: 'none', fontSize: '13px', fontWeight: '800', cursor: 'pointer' }}
        >
          🔍 Tìm kiếm
        </button>
      </div>

      {/* MAIN CONTENT GRID */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(340px, 1fr))', gap: '24px', alignItems: 'start' }}>
        
        {/* LEFT COLUMN: RETURN CARDS LIST */}
        <div style={{ background: '#ffffff', borderRadius: '20px', padding: '20px', border: '1px solid #e2e8f0', boxShadow: '0 4px 20px rgba(0,0,0,0.03)' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px', paddingBottom: '12px', borderBottom: '1px solid #f1f5f9' }}>
            <h2 style={{ fontSize: '17px', fontWeight: '800', color: '#0f172a', margin: 0, display: 'flex', alignItems: 'center', gap: '8px' }}>
              📦 Danh sách yêu cầu ({returnsList.length})
            </h2>
            <button
              onClick={loadReturns}
              style={{ background: '#f8fafc', border: '1px solid #cbd5e1', borderRadius: '8px', padding: '4px 10px', fontSize: '12px', fontWeight: '700', color: '#475569', cursor: 'pointer' }}
            >
              🔄 Tải lại
            </button>
          </div>

          {loading ? (
            <div style={{ textAlign: 'center', padding: '40px 0', color: '#94a3b8' }}>
              Đang tải danh sách đổi trả...
            </div>
          ) : returnsList.length === 0 ? (
            <div style={{ textAlign: 'center', padding: '48px 20px', background: '#f8fafc', borderRadius: '16px', border: '1px dashed #cbd5e1' }}>
              <span style={{ fontSize: '40px', display: 'block', marginBottom: '8px' }}>📦</span>
              <h3 style={{ fontSize: '16px', fontWeight: '800', color: '#334155', margin: '0 0 4px 0' }}>Chưa có yêu cầu đổi trả</h3>
              <p style={{ fontSize: '13px', color: '#64748b', margin: '0 0 16px 0' }}>Bạn chưa tạo yêu cầu hoàn trả nào cho các sản phẩm đã mua.</p>
              <Link to="/returns/new" style={{ display: 'inline-block', background: '#ea580c', color: '#fff', padding: '8px 16px', borderRadius: '10px', fontSize: '13px', fontWeight: '800', textDecoration: 'none' }}>
                🔄 Tạo yêu cầu ngay
              </Link>
            </div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
              {returnsList.map((item) => {
                const isSelected = selected?.id === item.id;
                const statusBadge = getReturnStatusBadge(item.status);

                return (
                  <div
                    key={item.id}
                    onClick={() => selectReturn(item)}
                    style={{
                      padding: '16px',
                      borderRadius: '16px',
                      background: isSelected ? 'linear-gradient(135deg, #fff7ed 0%, #ffedd5 100%)' : '#ffffff',
                      border: isSelected ? '2px solid #ea580c' : '1px solid #e2e8f0',
                      boxShadow: isSelected ? '0 6px 16px rgba(234, 88, 12, 0.15)' : '0 2px 6px rgba(0,0,0,0.02)',
                      cursor: 'pointer',
                      transition: 'all 0.2s ease',
                    }}
                  >
                    {/* TOP ROW */}
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '8px' }}>
                      <span style={{ fontSize: '14px', fontWeight: '900', color: '#0f172a' }}>
                        Yêu cầu #RTRN-{item.id}
                      </span>
                      <span
                        style={{
                          background: statusBadge.bg,
                          color: statusBadge.color,
                          padding: '3px 8px',
                          borderRadius: '12px',
                          fontSize: '11px',
                          fontWeight: '800',
                        }}
                      >
                        {statusBadge.icon} {statusBadge.label}
                      </span>
                    </div>

                    {/* ORDER & REFUND AMOUNT */}
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '10px' }}>
                      <span style={{ fontSize: '12.5px', color: '#64748b', fontWeight: '600' }}>
                        Đơn hàng: <strong style={{ color: '#ea580c' }}>#DH{item.orderId}</strong>
                      </span>
                      <span style={{ fontSize: '15px', fontWeight: '900', color: '#16a34a' }}>
                        {formatPrice(item.totalRefundAmount || 0)}
                      </span>
                    </div>

                    {/* FOOTER ACTIONS */}
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', fontSize: '12px', color: '#94a3b8', paddingTop: '8px', borderTop: '1px solid rgba(0,0,0,0.04)' }}>
                      <span>🕒 {formatDateTime(item.createdAt)}</span>
                      <div style={{ display: 'flex', gap: '8px' }}>
                        <button
                          type="button"
                          onClick={(e) => { e.stopPropagation(); selectReturn(item); }}
                          style={{ background: '#eff6ff', border: '1px solid #bfdbfe', color: '#2563eb', borderRadius: '6px', padding: '3px 8px', fontSize: '11.5px', fontWeight: '800', cursor: 'pointer' }}
                        >
                          🔍 Chi tiết
                        </button>
                        {String(item.status?.code || item.status).includes('REQUESTED') && (
                          <button
                            type="button"
                            onClick={(e) => {
                              e.stopPropagation();
                              if (window.confirm('Bạn muốn hủy yêu cầu đổi trả này?')) {
                                doAction(() => cancelCustomerReturn(item.id), 'Đã hủy yêu cầu thành công.');
                              }
                            }}
                            style={{ background: '#fef2f2', border: '1px solid #fecaca', color: '#dc2626', borderRadius: '6px', padding: '3px 8px', fontSize: '11.5px', fontWeight: '800', cursor: 'pointer' }}
                          >
                            ❌ Hủy
                          </button>
                        )}
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>

        {/* RIGHT COLUMN: RETURN DETAILS & ACTIONS PANEL */}
        <div style={{ background: '#ffffff', borderRadius: '20px', padding: '24px', border: '1px solid #e2e8f0', boxShadow: '0 4px 20px rgba(0,0,0,0.03)', position: 'sticky', top: '90px' }}>
          {!selected ? (
            <div style={{ textAlign: 'center', padding: '60px 20px' }}>
              <span style={{ fontSize: '48px', display: 'block', marginBottom: '12px' }}>🔄</span>
              <h3 style={{ fontSize: '17px', fontWeight: '800', color: '#1e293b', margin: '0 0 6px 0' }}>
                Chọn một yêu cầu để xem chi tiết
              </h3>
              <p style={{ fontSize: '13.5px', color: '#64748b', margin: 0, maxWidth: '280px', marginLeft: 'auto', marginRight: 'auto' }}>
                Bấm vào bất kỳ yêu cầu đổi trả nào ở danh sách bên trái để theo dõi tiến độ xử lý hoặc bổ sung thông tin.
              </p>
            </div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
              
              {/* HEADER */}
              <div style={{ paddingBottom: '14px', borderBottom: '1px solid #f1f5f9', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <div>
                  <span style={{ fontSize: '11.5px', fontWeight: '800', color: '#ea580c', textTransform: 'uppercase' }}>CHI TIẾT ĐỔI TRẢ</span>
                  <h3 style={{ fontSize: '18px', fontWeight: '900', color: '#0f172a', margin: '2px 0 0 0' }}>
                    Mã #RTRN-{selected.id}
                  </h3>
                </div>
                {selected.status && (() => {
                  const badge = getReturnStatusBadge(selected.status);
                  return (
                    <span style={{ background: badge.bg, color: badge.color, padding: '4px 10px', borderRadius: '12px', fontSize: '11.5px', fontWeight: '800' }}>
                      {badge.icon} {badge.label}
                    </span>
                  );
                })()}
              </div>

              {/* SUMMARY STAT CARD */}
              <div style={{ background: '#f8fafc', padding: '14px', borderRadius: '14px', border: '1px solid #e2e8f0', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <div>
                  <span style={{ fontSize: '12px', color: '#64748b', fontWeight: '600' }}>Mã đơn hàng:</span>
                  <div style={{ fontSize: '14.5px', fontWeight: '800', color: '#0f172a' }}>#DH{selected.orderId}</div>
                </div>
                <div style={{ textAlign: 'right' }}>
                  <span style={{ fontSize: '12px', color: '#64748b', fontWeight: '600' }}>Tổng hoàn tiền:</span>
                  <div style={{ fontSize: '16px', fontWeight: '900', color: '#16a34a' }}>{formatPrice(selected.totalRefundAmount || 0)}</div>
                </div>
              </div>

              {/* PROOF IMAGES GALLERY (IF ANY) */}
              {selected.proofImages && selected.proofImages.length > 0 && (
                <div>
                  <span style={{ fontSize: '13px', fontWeight: '800', color: '#0f172a', display: 'block', marginBottom: '8px' }}>
                    📸 Ảnh minh chứng thực tế ({selected.proofImages.length})
                  </span>
                  <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
                    {selected.proofImages.map((url, idx) => (
                      <a key={idx} href={url} target="_blank" rel="noopener noreferrer" style={{ display: 'block', width: '65px', height: '65px', borderRadius: '10px', overflow: 'hidden', border: '1px solid #cbd5e1' }}>
                        <img src={url} alt="Proof" style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                      </a>
                    ))}
                  </div>
                </div>
              )}

              {/* RETURNED ITEMS LIST */}
              <div>
                <span style={{ fontSize: '13px', fontWeight: '800', color: '#0f172a', display: 'block', marginBottom: '8px' }}>
                  📦 Sản phẩm hoàn trả ({selected.items?.length || 0})
                </span>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                  {(selected.items || []).map((it, idx) => (
                    <div key={idx} style={{ padding: '10px 12px', background: '#f8fafc', borderRadius: '10px', border: '1px solid #f1f5f9', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                      <div>
                        <div style={{ fontSize: '13.5px', fontWeight: '800', color: '#0f172a' }}>
                          Sản phẩm #{it.productId || it.orderItemId} (x{it.quantity})
                        </div>
                        <span style={{ fontSize: '11.5px', color: '#ea580c', fontWeight: '700' }}>
                          Lý do: {getReasonLabel(it.reasonCode || it.reason)}
                        </span>
                      </div>
                      <div style={{ fontSize: '13.5px', fontWeight: '800', color: '#16a34a' }}>
                        {formatPrice(it.expectedRefundAmount || it.refundAmount || 0)}
                      </div>
                    </div>
                  ))}
                </div>
              </div>

              {/* UPDATE REASON NOTE FORM */}
              <div style={{ background: '#ffffff', borderRadius: '14px', border: '1px solid #e2e8f0', padding: '14px' }}>
                <span style={{ fontSize: '13px', fontWeight: '800', color: '#0f172a', display: 'block', marginBottom: '6px' }}>
                  ✏️ Ghi chú / Cập nhật thông tin bổ sung
                </span>
                <input
                  type="text"
                  placeholder="Nhập ghi chú mới cho Shop..."
                  value={reasonNote}
                  onChange={(e) => setReasonNote(e.target.value)}
                  style={{ width: '100%', padding: '8px 12px', borderRadius: '8px', border: '1px solid #cbd5e1', fontSize: '13px', marginBottom: '8px', boxSizing: 'border-box' }}
                />
                <button
                  type="button"
                  disabled={actionLoading || !reasonNote.trim()}
                  onClick={() => doAction(() => updateCustomerReturnInfo(selected.id, reasonNote), 'Đã cập nhật ghi chú thành công.')}
                  style={{ padding: '6px 14px', background: '#ea580c', color: '#fff', border: 'none', borderRadius: '8px', fontSize: '12px', fontWeight: '800', cursor: 'pointer' }}
                >
                  Lưu ghi chú
                </button>
              </div>

              {/* DISPUTE FORM (IF APPLICABLE) */}
              <div style={{ background: '#fcf5ff', borderRadius: '14px', border: '1px solid #e9d5ff', padding: '14px' }}>
                <span style={{ fontSize: '13px', fontWeight: '800', color: '#6b21a8', display: 'block', marginBottom: '6px' }}>
                  ⚖️ Mở khiếu nại CSKH (Nếu chưa hài lòng)
                </span>
                <input
                  type="text"
                  placeholder="Nhập lý do khiếu nại xử lý đổi trả..."
                  value={disputeReason}
                  onChange={(e) => setDisputeReason(e.target.value)}
                  style={{ width: '100%', padding: '8px 12px', borderRadius: '8px', border: '1px solid #d8b4fe', fontSize: '13px', marginBottom: '8px', boxSizing: 'border-box' }}
                />
                <button
                  type="button"
                  disabled={actionLoading || !disputeReason.trim()}
                  onClick={() => doAction(() => disputeCustomerReturn(selected.id, disputeReason), 'Đã mở tranh chấp thành công.')}
                  style={{ padding: '6px 14px', background: '#7e22ce', color: '#fff', border: 'none', borderRadius: '8px', fontSize: '12px', fontWeight: '800', cursor: 'pointer' }}
                >
                  Mở tranh chấp
                </button>
              </div>

              {/* PROCESSING HISTORIES TIMELINE */}
              {selected.histories && selected.histories.length > 0 && (
                <div>
                  <span style={{ fontSize: '13px', fontWeight: '800', color: '#0f172a', display: 'block', marginBottom: '8px' }}>
                    📜 Lịch sử tiến trình xử lý ({selected.histories.length})
                  </span>
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '8px', borderLeft: '2px solid #ea580c', paddingLeft: '12px', marginLeft: '4px' }}>
                    {selected.histories.map((h, idx) => (
                      <div key={idx} style={{ position: 'relative' }}>
                        <div style={{ fontSize: '12.5px', fontWeight: '800', color: '#0f172a' }}>
                          {h.status || 'Cập nhật'}
                        </div>
                        <p style={{ margin: '2px 0 0 0', fontSize: '12px', color: '#475569' }}>
                          {h.note || h.message || 'Cập nhật trạng thái đổi trả'}
                        </p>
                        <span style={{ fontSize: '10.5px', color: '#94a3b8' }}>
                          {h.createdAt ? formatDateTime(h.createdAt) : ''}
                        </span>
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

export default ReturnListPage;
