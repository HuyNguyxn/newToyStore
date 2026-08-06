import { useEffect, useState } from 'react';
import { useOutletContext } from 'react-router-dom';
import {
  createShipmentForOrder,
  deleteShipment,
  executeShipmentAction,
  getShipmentTrackingLogs,
  getShipments,
} from '../../services/adminLogisticsService.js';
import { formatDateTime } from '../../utils/formatters.js';

const shipmentActions = [
  { code: 'HAND_OVER_TO_CARRIER', label: 'Bàn giao cho hãng vận chuyển' },
  { code: 'MARK_DELIVERED', label: 'Xác nhận giao thành công' },
  { code: 'REPORT_DELIVERY_FAILED', label: 'Báo cáo giao hàng thất bại' },
  { code: 'RETRY_DELIVERY', label: 'Giao hàng lại' },
  { code: 'RETURN_TO_WAREHOUSE', label: 'Trả hàng về kho' },
  { code: 'CANCEL_SHIPMENT', label: 'Hủy đơn vận chuyển' },
];

function getShipmentStatusInfo(status) {
  const statusStr = String(status || '').toUpperCase();
  if (statusStr === 'DELIVERED') {
    return { label: 'Đã giao thành công', bg: '#d1fae5', color: '#10b981', border: '#a7f3d0' };
  }
  if (statusStr === 'PENDING') {
    return { label: 'Chờ lấy hàng', bg: '#fef3c7', color: '#d97706', border: '#fde68a' };
  }
  if (statusStr === 'IN_TRANSIT' || statusStr === 'PICKED_UP') {
    return { label: 'Đang giao hàng', bg: '#eff6ff', color: '#2563eb', border: '#bfdbfe' };
  }
  if (statusStr === 'FAILED') {
    return { label: 'Giao thất bại', bg: '#fee2e2', color: '#dc2626', border: '#fecaca' };
  }
  if (statusStr === 'CANCELLED') {
    return { label: 'Đã hủy', bg: '#f1f5f9', color: '#64748b', border: '#cbd5e1' };
  }
  return { label: statusStr, bg: '#f1f5f9', color: '#475569', border: '#cbd5e1' };
}

function AdminLogisticsPage() {
  const { userRole } = useOutletContext();
  const canDelete = userRole === 'MANAGER' || userRole === 'ADMIN';

  const [shipments, setShipments] = useState([]);
  const [selected, setSelected] = useState(null);
  const [logs, setLogs] = useState([]);
  const [filters, setFilters] = useState({ orderId: '', status: '', trackingCode: '' });
  const [createOrderId, setCreateOrderId] = useState('');
  const [actionForm, setActionForm] = useState({ action: 'HAND_OVER_TO_CARRIER', reason: '', location: 'Kho trung tâm' });
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    loadShipments();
  }, []);

  async function loadShipments() {
    setLoading(true);
    setError('');
    try {
      const result = await getShipments({
        orderId: filters.orderId || undefined,
        status: filters.status || undefined,
        trackingCode: filters.trackingCode || undefined,
        page: 0,
        size: 50,
        sort: 'createdAt,desc',
      });
      setShipments(result.content || result || []);
    } catch (err) {
      setError(err.message || 'Không thể tải danh sách vận chuyển.');
      setShipments([]);
    } finally {
      setLoading(false);
    }
  }

  async function doAction(action, successMsg) {
    setError('');
    setMessage('');
    try {
      await action();
      setMessage(successMsg);
      await loadShipments();
      setSelected(null);
      setLogs([]);
    } catch (err) {
      setError(err.message || 'Thao tác vận chuyển thất bại.');
    }
  }

  async function selectShipment(shipment) {
    setSelected(shipment);
    const nextAction = shipment.nextActions?.[0]?.code || shipment.allowedActions?.[0] || 'HAND_OVER_TO_CARRIER';
    setActionForm((current) => ({ ...current, action: nextAction }));
    try {
      const result = await getShipmentTrackingLogs(shipment.id, { page: 0, size: 20 });
      setLogs(result.content || result || []);
    } catch {
      setLogs([]);
    }
  }

  const handleClearFilters = () => {
    setFilters({ orderId: '', status: '', trackingCode: '' });
    setTimeout(() => {
      loadShipments();
    }, 50);
  };

  return (
    <section style={{ padding: '24px', background: '#f8fafc', minHeight: '100vh', fontFamily: 'system-ui, -apple-system, sans-serif' }}>
      
      {/* HEADER ROW */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
        <h1 style={{ fontSize: '18px', fontWeight: '800', color: '#0f172a', margin: 0, textTransform: 'uppercase', letterSpacing: '0.5px' }}>
          Quản lý Vận chuyển (Logistics)
        </h1>
        <div style={{ background: '#fff7ed', color: '#ea580c', border: '1px solid #ffedd5', padding: '6px 16px', borderRadius: '20px', fontSize: '13px', fontWeight: '700' }}>
          Vận đơn: {shipments.length}
        </div>
      </div>

      {/* ALERTS */}
      {error && <div style={{ background: '#fef2f2', color: '#dc2626', border: '1px solid #fecaca', padding: '10px 14px', borderRadius: '8px', marginBottom: '16px', fontSize: '13px', fontWeight: '700' }}>{error}</div>}
      {message && <div style={{ background: '#f0fdf4', color: '#16a34a', border: '1px solid #bbf7d0', padding: '10px 14px', borderRadius: '8px', marginBottom: '16px', fontSize: '13px', fontWeight: '700' }}>{message}</div>}

      {/* FILTER BAR */}
      <form
        onSubmit={(e) => {
          e.preventDefault();
          loadShipments();
        }}
        style={{ background: '#ffffff', padding: '14px 16px', borderRadius: '12px', border: '1px solid #e2e8f0', marginBottom: '16px', display: 'flex', gap: '10px', alignItems: 'center', flexWrap: 'wrap' }}
      >
        <div style={{ flex: '1', minWidth: '160px' }}>
          <input
            type="text"
            placeholder="Mã đơn hàng..."
            value={filters.orderId}
            onChange={(e) => setFilters({ ...filters, orderId: e.target.value })}
            style={{ width: '100%', padding: '9px 12px', border: '1px solid #cbd5e1', borderRadius: '8px', fontSize: '13px', outline: 'none' }}
          />
        </div>

        <div style={{ flex: '1', minWidth: '160px' }}>
          <input
            type="text"
            placeholder="Mã vận đơn (Tracking Code)..."
            value={filters.trackingCode}
            onChange={(e) => setFilters({ ...filters, trackingCode: e.target.value })}
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
            <option value="PENDING">Chờ lấy hàng (PENDING)</option>
            <option value="PICKED_UP">Đã lấy hàng (PICKED_UP)</option>
            <option value="IN_TRANSIT">Đang giao hàng (IN_TRANSIT)</option>
            <option value="DELIVERED">Đã giao thành công (DELIVERED)</option>
            <option value="FAILED">Giao thất bại (FAILED)</option>
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

      {/* OPERATIONS CONSOLE GRIDS */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '20px', marginBottom: '20px' }}>
        
        {/* Create Shipment Card */}
        <div style={{ background: '#ffffff', borderRadius: '12px', border: '1px solid #e2e8f0', padding: '20px' }}>
          <h3 style={{ fontSize: '14.5px', fontWeight: '800', color: '#0f172a', borderBottom: '1px solid #f1f5f9', paddingBottom: '10px', margin: '0 0 14px 0' }}>
            Tạo vận đơn theo Đơn hàng
          </h3>
          <form
            onSubmit={(e) => {
              e.preventDefault();
              doAction(() => createShipmentForOrder(createOrderId), 'Tạo vận đơn giao hàng thành công.');
            }}
            style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}
          >
            <div>
              <label style={{ display: 'block', fontSize: '12.5px', fontWeight: '700', color: '#475569', marginBottom: '6px' }}>Mã đơn hàng *</label>
              <input
                type="text"
                placeholder="Nhập mã đơn hàng (ví dụ: 1)..."
                value={createOrderId}
                onChange={(e) => setCreateOrderId(e.target.value)}
                required
                style={{ width: '100%', padding: '8px 10px', border: '1px solid #cbd5e1', borderRadius: '6px', fontSize: '13px', outline: 'none' }}
              />
            </div>
            <button
              type="submit"
              style={{ width: '100%', padding: '10px', background: '#16a34a', color: '#ffffff', border: 'none', borderRadius: '6px', fontSize: '13px', fontWeight: '700', cursor: 'pointer', marginTop: '4px' }}
            >
              Tạo vận đơn
            </button>
          </form>
        </div>

        {/* Update Shipment Action Card */}
        <div style={{ background: '#ffffff', borderRadius: '12px', border: '1px solid #e2e8f0', padding: '20px' }}>
          <h3 style={{ fontSize: '14.5px', fontWeight: '800', color: '#0f172a', borderBottom: '1px solid #f1f5f9', paddingBottom: '10px', margin: '0 0 14px 0' }}>
            Cập nhật Vận đơn {selected ? `#${selected.id}` : ''}
          </h3>
          <form
            onSubmit={(e) => {
              e.preventDefault();
              doAction(() => executeShipmentAction(selected.id, actionForm), 'Cập nhật tiến trình vận chuyển thành công.');
            }}
            style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}
          >
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px' }}>
              <div>
                <label style={{ display: 'block', fontSize: '12.5px', fontWeight: '700', color: '#475569', marginBottom: '6px' }}>Thao tác</label>
                <select
                  value={actionForm.action}
                  onChange={(e) => setActionForm((c) => ({ ...c, action: e.target.value }))}
                  style={{ width: '100%', padding: '8px 10px', border: '1px solid #cbd5e1', borderRadius: '6px', fontSize: '13px', outline: 'none', background: '#fff' }}
                >
                  {(selected?.nextActions?.length ? selected.nextActions : shipmentActions).map((action) => (
                    <option key={action.code} value={action.code}>
                      {action.label || action.displayName || action.code}
                    </option>
                  ))}
                </select>
              </div>
              <div>
                <label style={{ display: 'block', fontSize: '12.5px', fontWeight: '700', color: '#475569', marginBottom: '6px' }}>Địa điểm</label>
                <input
                  type="text"
                  value={actionForm.location}
                  onChange={(e) => setActionForm((c) => ({ ...c, location: e.target.value }))}
                  style={{ width: '100%', padding: '8px 10px', border: '1px solid #cbd5e1', borderRadius: '6px', fontSize: '13px', outline: 'none' }}
                />
              </div>
            </div>
            <div>
              <label style={{ display: 'block', fontSize: '12.5px', fontWeight: '700', color: '#475569', marginBottom: '6px' }}>Lý do / Mô tả chi tiết</label>
              <input
                type="text"
                placeholder="Nhập ghi chú chi tiết..."
                value={actionForm.reason}
                onChange={(e) => setActionForm((c) => ({ ...c, reason: e.target.value }))}
                style={{ width: '100%', padding: '8px 10px', border: '1px solid #cbd5e1', borderRadius: '6px', fontSize: '13px', outline: 'none' }}
              />
            </div>
            <button
              type="submit"
              disabled={!selected}
              style={{
                width: '100%',
                padding: '10px',
                background: selected ? '#ea580c' : '#cbd5e1',
                color: '#ffffff',
                border: 'none',
                borderRadius: '6px',
                fontSize: '13px',
                fontWeight: '700',
                cursor: selected ? 'pointer' : 'default',
                marginTop: '4px',
              }}
            >
              Cập nhật tiến trình
            </button>
          </form>
        </div>

      </div>

      {/* SPLIT LAYOUT WITH SHIPMENT TABLE AND LOGS PANEL */}
      <div style={{ display: 'grid', gridTemplateColumns: selected ? '1.5fr 1fr' : '1fr', gap: '20px', alignItems: 'start' }}>
        
        {/* Table List */}
        <div style={{ background: '#ffffff', borderRadius: '12px', border: '1px solid #e2e8f0', overflow: 'visible' }}>
          <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '13px' }}>
            <thead>
              <tr style={{ background: '#f8fafc', color: '#475569', fontWeight: '800', fontSize: '12px', borderBottom: '1px solid #e2e8f0', textTransform: 'uppercase' }}>
                <th style={{ padding: '14px 16px', width: '60px' }}>ID</th>
                <th style={{ padding: '14px 16px', width: '80px' }}>Mã Đơn</th>
                <th style={{ padding: '14px 16px', width: '140px' }}>Trạng thái</th>
                <th style={{ padding: '14px 16px', width: '150px' }}>Đơn vị vận chuyển</th>
                <th style={{ padding: '14px 16px', width: '150px' }}>Mã vận đơn</th>
                <th style={{ padding: '14px 16px', width: '160px', textAlign: 'center' }}>Thao tác</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr>
                  <td colSpan="6" style={{ padding: '36px', textAlign: 'center', color: '#64748b' }}>
                    Đang tải danh sách vận chuyển...
                  </td>
                </tr>
              ) : shipments.length === 0 ? (
                <tr>
                  <td colSpan="6" style={{ padding: '36px', textAlign: 'center', color: '#94a3b8' }}>
                    Không tìm thấy vận đơn nào.
                  </td>
                </tr>
              ) : (
                shipments.map((s, idx) => {
                  const statusInfo = getShipmentStatusInfo(s.status);

                  return (
                    <tr
                      key={s.id}
                      style={{
                        borderBottom: '1px solid #f1f5f9',
                        background: idx % 2 === 0 ? '#ffffff' : '#fafafa',
                      }}
                    >
                      <td style={{ padding: '14px 16px', fontWeight: '600', color: '#334155' }}>
                        #{s.id}
                      </td>
                      <td style={{ padding: '14px 16px', fontWeight: '700', color: '#ea580c' }}>
                        DH{s.orderId}
                      </td>
                      <td style={{ padding: '14px 16px' }}>
                        <span
                          style={{
                            background: statusInfo.bg,
                            color: statusInfo.color,
                            border: `1px solid ${statusInfo.border}`,
                            padding: '3px 10px',
                            borderRadius: '8px',
                            fontSize: '12px',
                            fontWeight: '700',
                            display: 'inline-block',
                          }}
                        >
                          {statusInfo.label}
                        </span>
                      </td>
                      <td style={{ padding: '14px 16px', color: '#334155' }}>
                        {s.carrierName || s.providerCode || 'Chưa nhận'}
                      </td>
                      <td style={{ padding: '14px 16px', color: '#64748b', fontWeight: '600' }}>
                        {s.trackingCode || '-'}
                      </td>
                      <td style={{ padding: '14px 16px', textAlign: 'center' }}>
                        <div style={{ display: 'inline-flex', gap: '6px', justifyContent: 'center' }}>
                          <button
                            type="button"
                            onClick={() => selectShipment(s)}
                            style={{
                              padding: '6px 12px',
                              background: '#ffffff',
                              color: '#475569',
                              border: '1px solid #cbd5e1',
                              borderRadius: '6px',
                              cursor: 'pointer',
                              fontSize: '12.5px',
                              fontWeight: '700',
                            }}
                          >
                            Chi tiết / Log
                          </button>
                          {canDelete && (
                            <button
                              type="button"
                              onClick={() => doAction(() => deleteShipment(s.id), 'Đã xóa vận đơn.')}
                              style={{
                                padding: '6px 12px',
                                background: '#ffffff',
                                color: '#dc2626',
                                border: '1px solid #fecaca',
                                borderRadius: '6px',
                                cursor: 'pointer',
                                fontSize: '12.5px',
                                fontWeight: '700',
                              }}
                            >
                              Xóa
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
        </div>

        {/* LOGS PANEL */}
        {selected && (
          <aside style={{ background: '#ffffff', borderRadius: '12px', border: '1px solid #e2e8f0', padding: '20px', boxShadow: '0 4px 16px rgba(0,0,0,0.02)' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: '1px solid #f1f5f9', paddingBottom: '12px', marginBottom: '14px' }}>
              <h3 style={{ fontSize: '14px', fontWeight: '800', color: '#0f172a', margin: 0 }}>
                Lịch trình Vận đơn #{selected.id}
              </h3>
              <button
                type="button"
                onClick={() => {
                  setSelected(null);
                  setLogs([]);
                }}
                style={{ border: 'none', background: '#f1f5f9', borderRadius: '6px', width: '26px', height: '26px', cursor: 'pointer', fontWeight: '700' }}
              >
                ✕
              </button>
            </div>
            
            <TrackingLogList logs={logs} />
          </aside>
        )}

      </div>

      {/* FOOTER */}
      <footer style={{ textAlign: 'center', marginTop: '30px', padding: '16px 0', borderTop: '1px solid #cbd5e1', fontSize: '12px', color: '#94a3b8' }}>
        © 2026 ToyStore Admin Panel
      </footer>
    </section>
  );
}

function TrackingLogList({ logs }) {
  if (!logs?.length) {
    return <div style={{ textAlign: 'center', color: '#94a3b8', fontSize: '13px', padding: '20px 0' }}>Chưa có nhật ký hành trình.</div>;
  }

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
      {logs.map((log, index) => (
        <div
          key={log.id || index}
          style={{
            background: '#f8fafc',
            border: '1px solid #e2e8f0',
            padding: '12px',
            borderRadius: '8px',
            fontSize: '12.5px',
          }}
        >
          <div style={{ display: 'flex', justifyContent: 'space-between', fontWeight: '700', color: '#1e293b', marginBottom: '4px' }}>
            <span>{getShipmentStatusInfo(log.status).label || log.status}</span>
            <span style={{ fontSize: '11px', color: '#64748b', fontWeight: '400' }}>{log.location || 'N/A'}</span>
          </div>
          <p style={{ margin: 0, color: '#475569', lineHeight: 1.4 }}>
            {log.description || log.note || 'Cập nhật tiến trình vận chuyển.'}
          </p>
          {log.createdAt && (
            <span style={{ fontSize: '10.5px', color: '#94a3b8', display: 'block', marginTop: '4px' }}>
              Thời gian: {formatDateTime(log.createdAt)}
            </span>
          )}
        </div>
      ))}
    </div>
  );
}

export default AdminLogisticsPage;
