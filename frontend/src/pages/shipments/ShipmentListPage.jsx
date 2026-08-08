import { useEffect, useState } from 'react';
import { getMyShipments, getShipmentDetails, getShipmentTrackingLogs } from '../../services/shipmentService.js';
import { formatDateTime, formatPrice } from '../../utils/formatters.js';

/* Helper function for friendly Vietnamese status badges */
const getShipmentStatusBadge = (statusObj) => {
  const raw = (typeof statusObj === 'string' ? statusObj : statusObj?.code || statusObj?.name || statusObj?.displayName || '').toUpperCase();

  if (raw.includes('DELIVERED') || raw.includes('COMPLETED') || raw.includes('SUCCESS')) {
    return { label: 'Đã giao hàng thành công', bg: '#dcfce7', color: '#15803d', icon: '✅' };
  }
  if (raw.includes('PICKED_UP') || raw.includes('IN_TRANSIT') || raw.includes('DELIVERING') || raw.includes('SHIPPED')) {
    return { label: 'Đang trên đường giao', bg: '#dbeafe', color: '#1d4ed8', icon: '🚚' };
  }
  if (raw.includes('PENDING') || raw.includes('CREATED') || raw.includes('PROCESSING')) {
    return { label: 'Chờ lấy hàng / Xử lý', bg: '#fef3c7', color: '#b45309', icon: '📦' };
  }
  if (raw.includes('CANCELLED') || raw.includes('FAILED')) {
    return { label: 'Giao thất bại / Đã hủy', bg: '#fee2e2', color: '#b91c1c', icon: '❌' };
  }
  return { label: 'Đang cập nhật', bg: '#f1f5f9', color: '#475569', icon: '📌' };
};

const formatLocationName = (loc) => {
  if (!loc) return '';
  if (loc === 'Warehouse') return 'Kho hàng Shop';
  if (loc === 'Hub') return 'Bưu cục trung chuyển';
  return loc;
};

const formatLogDescriptionText = (desc) => {
  if (!desc) return 'Cập nhật tiến trình vận chuyển';
  if (desc.includes('created and waiting for pickup')) return 'Đã tạo vận đơn, đang chờ bưu tá lấy hàng';
  if (desc.includes('handed over to carrier') || desc.includes('Handed over')) return 'Đã bàn giao cho đơn vị vận chuyển';
  if (desc.includes('delivered successfully') || desc.includes('delivered')) return 'Giao hàng thành công tới người nhận';
  if (desc.includes('cancelled')) return 'Vận đơn đã bị hủy';
  return desc;
};

function ShipmentListPage() {
  const [shipments, setShipments] = useState([]);
  const [selected, setSelected] = useState(null);
  const [logs, setLogs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [detailLoading, setDetailLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    loadShipments();
  }, []);

  async function loadShipments() {
    setLoading(true);
    setError('');
    try {
      const result = await getMyShipments({ page: 0, size: 50, sort: 'createdAt,desc' });
      const list = result.content || (Array.isArray(result) ? result : []);
      setShipments(list);

      // Auto select the first shipment if available
      if (list.length > 0) {
        selectShipment(list[0]);
      }
    } catch (err) {
      setError(err.message || 'Không thể tải danh sách vận chuyển.');
    } finally {
      setLoading(false);
    }
  }

  async function selectShipment(shipment) {
    setError('');
    setSelected(shipment);
    setDetailLoading(true);
    try {
      const [detail, logPage] = await Promise.all([
        getShipmentDetails(shipment.id).catch(() => shipment),
        getShipmentTrackingLogs(shipment.id, { page: 0, size: 50, sort: 'createdAt,asc' }).catch(() => ({ content: [] })),
      ]);
      setSelected(detail || shipment);
      setLogs(logPage.content || (Array.isArray(logPage) ? logPage : []));
    } catch (err) {
      setLogs([]);
    } finally {
      setDetailLoading(false);
    }
  }

  // Calculate statistics
  const deliveredCount = shipments.filter(s => {
    const c = String(s.status?.code || s.status || '').toUpperCase();
    return c.includes('DELIVERED') || c.includes('COMPLETED') || c.includes('SUCCESS');
  }).length;

  const deliveringCount = shipments.filter(s => {
    const c = String(s.status?.code || s.status || '').toUpperCase();
    return c.includes('IN_TRANSIT') || c.includes('DELIVERING') || c.includes('PICKED_UP') || c.includes('SHIPPED');
  }).length;

  return (
    <div className="container" style={{ padding: '24px 16px', maxWidth: '1200px', margin: '0 auto', fontFamily: 'system-ui, -apple-system, sans-serif' }}>
      {/* HERO BANNER - LIGHT MODE SANG TRỌNG */}
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
        }}
      >
        <div>
          <div style={{ display: 'inline-flex', alignItems: 'center', gap: '8px', background: '#ffedd5', color: '#ea580c', padding: '5px 14px', borderRadius: '20px', fontSize: '12px', fontWeight: '800', textTransform: 'uppercase', letterSpacing: '0.6px', marginBottom: '10px', boxShadow: '0 2px 6px rgba(234,88,12,0.1)' }}>
            🚚 HỆ THỐNG GIAO HÀNG
          </div>
          <h1 style={{ fontSize: '26px', fontWeight: '900', color: '#0f172a', margin: '0 0 6px 0', letterSpacing: '-0.5px' }}>
            Theo dõi vận chuyển đơn hàng
          </h1>
          <p style={{ margin: 0, color: '#475569', fontSize: '14px', fontWeight: '600' }}>
            Tra cứu chi tiết hành trình vận chuyển, mã vận đơn và cập nhật thời gian thực
          </p>
        </div>

        {/* SUMMARY STATS */}
        <div style={{ display: 'flex', gap: '12px' }}>
          <div style={{ background: '#ffffff', border: '1px solid #e2e8f0', padding: '10px 18px', borderRadius: '14px', textAlign: 'center', boxShadow: '0 2px 8px rgba(0,0,0,0.03)' }}>
            <span style={{ fontSize: '20px', fontWeight: '900', color: '#0f172a', display: 'block' }}>{shipments.length}</span>
            <span style={{ fontSize: '11px', color: '#64748b', fontWeight: '700', textTransform: 'uppercase' }}>Tổng kiện hàng</span>
          </div>
          <div style={{ background: '#eff6ff', border: '1px solid #bfdbfe', padding: '10px 18px', borderRadius: '14px', textAlign: 'center', boxShadow: '0 2px 8px rgba(37,99,235,0.06)' }}>
            <span style={{ fontSize: '20px', fontWeight: '900', color: '#2563eb', display: 'block' }}>{deliveringCount}</span>
            <span style={{ fontSize: '11px', color: '#1e40af', fontWeight: '700', textTransform: 'uppercase' }}>Đang vận chuyển</span>
          </div>
          <div style={{ background: '#f0fdf4', border: '1px solid #bbf7d0', padding: '10px 18px', borderRadius: '14px', textAlign: 'center', boxShadow: '0 2px 8px rgba(22,163,74,0.06)' }}>
            <span style={{ fontSize: '20px', fontWeight: '900', color: '#16a34a', display: 'block' }}>{deliveredCount}</span>
            <span style={{ fontSize: '11px', color: '#15803d', fontWeight: '700', textTransform: 'uppercase' }}>Đã giao thành công</span>
          </div>
        </div>
      </div>

      {error && (
        <div style={{ background: '#fef2f2', border: '1px solid #fecaca', color: '#dc2626', padding: '14px 18px', borderRadius: '12px', marginBottom: '20px', fontWeight: '600', fontSize: '14px' }}>
          ⚠️ {error}
        </div>
      )}

      {/* MAIN CONTENT GRID: 55% LEFT / 45% RIGHT */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(340px, 1fr))', gap: '24px', alignItems: 'start' }}>
        
        {/* LEFT COLUMN: LIST OF SHIPMENTS */}
        <div style={{ background: '#ffffff', borderRadius: '20px', padding: '20px', border: '1px solid #e2e8f0', boxShadow: '0 4px 20px rgba(0,0,0,0.03)' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px', paddingBottom: '12px', borderBottom: '1px solid #f1f5f9' }}>
            <h2 style={{ fontSize: '17px', fontWeight: '800', color: '#0f172a', margin: 0, display: 'flex', alignItems: 'center', gap: '8px' }}>
              📦 Danh sách kiện hàng ({shipments.length})
            </h2>
            <button
              onClick={loadShipments}
              style={{ background: '#f8fafc', border: '1px solid #cbd5e1', borderRadius: '8px', padding: '4px 10px', fontSize: '12px', fontWeight: '700', color: '#475569', cursor: 'pointer', transition: 'all 0.15s' }}
            >
              🔄 Tải lại
            </button>
          </div>

          {loading ? (
            <div style={{ textAlign: 'center', padding: '40px 0', color: '#94a3b8' }}>
              <div className="spinner" style={{ margin: '0 auto 12px' }}></div>
              Đang tải thông tin vận chuyển...
            </div>
          ) : shipments.length === 0 ? (
            <div style={{ textAlign: 'center', padding: '48px 20px', background: '#f8fafc', borderRadius: '16px', border: '1px dashed #cbd5e1' }}>
              <span style={{ fontSize: '40px', display: 'block', marginBottom: '8px' }}>🚚</span>
              <h3 style={{ fontSize: '16px', fontWeight: '800', color: '#334155', margin: '0 0 4px 0' }}>Chưa có kiện hàng nào</h3>
              <p style={{ fontSize: '13px', color: '#64748b', margin: 0 }}>Các đơn hàng của bạn khi bắt đầu vận chuyển sẽ xuất hiện tại đây.</p>
            </div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
              {shipments.map((shipment) => {
                const isSelected = selected?.id === shipment.id;
                const statusInfo = getShipmentStatusBadge(shipment.status);
                const orderText = shipment.orderId ? `#DH${shipment.orderId}` : `Đơn #${shipment.id}`;
                const trackingText = shipment.trackingCode || `SHIP-${shipment.id}`;

                return (
                  <div
                    key={shipment.id}
                    onClick={() => selectShipment(shipment)}
                    style={{
                      padding: '16px',
                      borderRadius: '14px',
                      background: isSelected ? 'linear-gradient(135deg, #fff7ed 0%, #ffedd5 100%)' : '#ffffff',
                      border: isSelected ? '2px solid #ea580c' : '1px solid #e2e8f0',
                      boxShadow: isSelected ? '0 6px 16px rgba(234, 88, 12, 0.15)' : '0 2px 6px rgba(0,0,0,0.02)',
                      cursor: 'pointer',
                      transition: 'all 0.2s cubic-bezier(0.4, 0, 0.2, 1)',
                      position: 'relative',
                    }}
                    onMouseEnter={(e) => {
                      if (!isSelected) {
                        e.currentTarget.style.borderColor = '#fdba74';
                        e.currentTarget.style.transform = 'translateY(-2px)';
                      }
                    }}
                    onMouseLeave={(e) => {
                      if (!isSelected) {
                        e.currentTarget.style.borderColor = '#e2e8f0';
                        e.currentTarget.style.transform = 'translateY(0)';
                      }
                    }}
                  >
                    {/* CARD HEADER */}
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '8px' }}>
                      <span style={{ fontSize: '14px', fontWeight: '800', color: '#0f172a' }}>
                        Đơn hàng <strong style={{ color: '#ea580c' }}>{orderText}</strong>
                      </span>
                      <span
                        style={{
                          background: statusInfo.bg,
                          color: statusInfo.color,
                          padding: '4px 10px',
                          borderRadius: '20px',
                          fontSize: '11.5px',
                          fontWeight: '800',
                          display: 'inline-flex',
                          alignItems: 'center',
                          gap: '4px',
                        }}
                      >
                        {statusInfo.icon} {statusInfo.label}
                      </span>
                    </div>

                    {/* CARD DETAILS */}
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', fontSize: '12.5px', color: '#64748b' }}>
                      <div>
                        <span>Mã vận đơn: </span>
                        <strong style={{ color: '#334155', fontFamily: 'monospace', fontSize: '12px' }}>{trackingText}</strong>
                      </div>
                      <div>
                        <span>Phí giao: </span>
                        <strong style={{ color: '#0f172a' }}>{formatPrice(shipment.shippingFee || 0)}</strong>
                      </div>
                    </div>

                    {shipment.createdAt && (
                      <div style={{ marginTop: '6px', fontSize: '11.5px', color: '#94a3b8' }}>
                        🕒 Ngày tạo: {formatDateTime(shipment.createdAt)}
                      </div>
                    )}
                  </div>
                );
              })}
            </div>
          )}
        </div>

        {/* RIGHT COLUMN: DETAILED TRACKING TIMELINE & INFO */}
        <div style={{ background: '#ffffff', borderRadius: '20px', padding: '24px', border: '1px solid #e2e8f0', boxShadow: '0 4px 20px rgba(0,0,0,0.03)', position: 'sticky', top: '90px' }}>
          {!selected ? (
            <div style={{ textAlign: 'center', padding: '60px 20px' }}>
              <span style={{ fontSize: '48px', display: 'block', marginBottom: '12px' }}>📍</span>
              <h3 style={{ fontSize: '17px', fontWeight: '800', color: '#1e293b', margin: '0 0 6px 0' }}>
                Chọn kiện hàng để xem lộ trình
              </h3>
              <p style={{ fontSize: '13.5px', color: '#64748b', margin: 0, maxWidth: '280px', marginLeft: 'auto', marginRight: 'auto' }}>
                Bấm vào bất kỳ kiện hàng nào ở danh sách bên trái để xem lịch sử vận chuyển chi tiết.
              </p>
            </div>
          ) : (
            <>
              {/* SELECTED SHIPMENT HEADER */}
              <div style={{ paddingBottom: '16px', marginBottom: '20px', borderBottom: '1px solid #f1f5f9' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                  <div>
                    <span style={{ fontSize: '12px', fontWeight: '800', color: '#ea580c', textTransform: 'uppercase', letterSpacing: '0.5px' }}>
                      CHI TIẾT VẬN CHUYỂN
                    </span>
                    <h2 style={{ fontSize: '20px', fontWeight: '900', color: '#0f172a', margin: '2px 0 0 0' }}>
                      Kiện hàng #{selected.id}
                    </h2>
                  </div>
                  {selected.status && (() => {
                    const statusInfo = getShipmentStatusBadge(selected.status);
                    return (
                      <span
                        style={{
                          background: statusInfo.bg,
                          color: statusInfo.color,
                          padding: '6px 12px',
                          borderRadius: '20px',
                          fontSize: '12px',
                          fontWeight: '800',
                          display: 'inline-flex',
                          alignItems: 'center',
                          gap: '5px',
                        }}
                      >
                        {statusInfo.icon} {statusInfo.label}
                      </span>
                    );
                  })()}
                </div>
              </div>

              {/* OVERVIEW INFO CARDS */}
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px', marginBottom: '20px' }}>
                <div style={{ background: '#f8fafc', padding: '12px 14px', borderRadius: '12px', border: '1px solid #f1f5f9' }}>
                  <span style={{ fontSize: '11px', color: '#64748b', fontWeight: '700', textTransform: 'uppercase' }}>Đơn hàng liên quan</span>
                  <p style={{ margin: '4px 0 0 0', fontSize: '14px', fontWeight: '800', color: '#ea580c' }}>
                    #DH{selected.orderId}
                  </p>
                </div>
                <div style={{ background: '#f8fafc', padding: '12px 14px', borderRadius: '12px', border: '1px solid #f1f5f9' }}>
                  <span style={{ fontSize: '11px', color: '#64748b', fontWeight: '700', textTransform: 'uppercase' }}>Tiền thu hộ (COD)</span>
                  <p style={{ margin: '4px 0 0 0', fontSize: '14px', fontWeight: '800', color: '#16a34a' }}>
                    {formatPrice(selected.codAmount || 0)}
                  </p>
                </div>
              </div>

              {/* SHIPPING ADDRESS SNAPSHOT */}
              {selected.shippingAddressSnapshot && (
                <div style={{ background: '#fff7ed', border: '1px solid #ffedd5', padding: '12px 14px', borderRadius: '12px', marginBottom: '20px' }}>
                  <span style={{ fontSize: '11.5px', color: '#c2410c', fontWeight: '800', display: 'flex', alignItems: 'center', gap: '4px' }}>
                    📍 Địa chỉ giao hàng:
                  </span>
                  <p style={{ margin: '4px 0 0 0', fontSize: '13px', color: '#431407', fontWeight: '600', lineHeight: '1.4' }}>
                    {selected.shippingAddressSnapshot}
                  </p>
                </div>
              )}

              {/* TRACKING TIMELINE */}
              <div style={{ marginBottom: '20px' }}>
                <h3 style={{ fontSize: '15px', fontWeight: '800', color: '#0f172a', marginBottom: '14px', display: 'flex', alignItems: 'center', gap: '6px' }}>
                  🚩 Nhật ký hành trình vận chuyển
                </h3>

                {detailLoading ? (
                  <div style={{ padding: '20px', textAlign: 'center', color: '#94a3b8' }}>Đang tải nhật ký hành trình...</div>
                ) : logs.length === 0 ? (
                  <div style={{ padding: '20px', background: '#f8fafc', borderRadius: '12px', textAlign: 'center', color: '#64748b', fontSize: '13px' }}>
                    📦 Đơn hàng đang được đóng gói chuẩn bị giao cho đơn vị vận chuyển.
                  </div>
                ) : (
                  <div style={{ position: 'relative', paddingLeft: '24px' }}>
                    {/* Vertical Timeline Line */}
                    <div style={{ position: 'absolute', top: '10px', bottom: '10px', left: '7px', width: '2px', background: '#e2e8f0' }} />

                    {logs.map((log, index) => {
                      const isLatest = index === logs.length - 1 || index === 0;
                      
                      let statusBadge = getShipmentStatusBadge(log.status);
                      const desc = (log.description || log.note || '').toLowerCase();
                      if (desc.includes('bàn giao') || desc.includes('carrier') || desc.includes('trên đường')) {
                        statusBadge = { label: 'Đang trên đường giao', bg: '#dbeafe', color: '#1d4ed8', icon: '🚚' };
                      } else if (desc.includes('tạo') || desc.includes('created') || desc.includes('chờ bưu tá')) {
                        statusBadge = { label: 'Chờ lấy hàng / Xử lý', bg: '#fef3c7', color: '#b45309', icon: '📦' };
                      } else if (desc.includes('thành công') || desc.includes('delivered')) {
                        statusBadge = { label: 'Đã giao hàng thành công', bg: '#dcfce7', color: '#15803d', icon: '✅' };
                      }

                      return (
                        <div key={log.id || index} style={{ position: 'relative', marginBottom: '18px' }}>
                          {/* Timeline Dot */}
                          <div
                            style={{
                              position: 'absolute',
                              left: '-24px',
                              top: '2px',
                              width: '14px',
                              height: '14px',
                              borderRadius: '50%',
                              background: isLatest ? '#ea580c' : '#cbd5e1',
                              border: isLatest ? '3px solid #ffedd5' : '3px solid #ffffff',
                              boxShadow: isLatest ? '0 0 0 2px #ea580c' : 'none',
                            }}
                          />

                          {/* Log Content */}
                          <div>
                            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', flexWrap: 'wrap' }}>
                              <span style={{ fontSize: '13.5px', fontWeight: '800', color: isLatest ? '#ea580c' : '#1e293b' }}>
                                {statusBadge.label}
                              </span>
                              {log.createdAt && (
                                <span style={{ fontSize: '11.5px', color: '#94a3b8', fontWeight: '600' }}>
                                  • {formatDateTime(log.createdAt)}
                                </span>
                              )}
                            </div>

                            {(log.description || log.note || log.location) && (
                              <p style={{ margin: '3px 0 0 0', fontSize: '12.5px', color: '#475569', lineHeight: '1.4' }}>
                                {log.location ? `📍 ${formatLocationName(log.location)}: ` : ''}{formatLogDescriptionText(log.description || log.note)}
                              </p>
                            )}
                          </div>
                        </div>
                      );
                    })}
                  </div>
                )}
              </div>

              {/* PRODUCTS IN SHIPMENT */}
              {Array.isArray(selected.items) && selected.items.length > 0 && (
                <div>
                  <h3 style={{ fontSize: '15px', fontWeight: '800', color: '#0f172a', marginBottom: '10px' }}>
                    🛍️ Sản phẩm trong kiện hàng ({selected.items.length})
                  </h3>
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                    {selected.items.map((item, idx) => (
                      <div key={item.id || idx} style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '10px 12px', background: '#f8fafc', borderRadius: '10px', border: '1px solid #f1f5f9' }}>
                        <span style={{ fontSize: '13px', fontWeight: '700', color: '#334155' }}>
                          {item.productName || `Sản phẩm #${item.productId}`}
                        </span>
                        <span style={{ fontSize: '12px', fontWeight: '800', color: '#ea580c', background: '#fff7ed', padding: '2px 8px', borderRadius: '6px' }}>
                          x{item.quantity || 1}
                        </span>
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </>
          )}
        </div>
      </div>
    </div>
  );
}

export default ShipmentListPage;
