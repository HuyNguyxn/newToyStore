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
  const code = typeof status === 'object' ? (status?.code || status?.name || '') : String(status || '');
  const statusStr = code.toUpperCase().replace(/\s+/g, '_');
  switch (statusStr) {
    case 'PENDING_PICKUP':
      return { label: 'Chờ lấy hàng', bg: '#fffbeb', color: '#d97706', border: '#fde68a' };
    case 'IN_TRANSIT':
      return { label: 'Đang giao hàng', bg: '#eff6ff', color: '#2563eb', border: '#bfdbfe' };
    case 'DELIVERY_FAILED':
      return { label: 'Giao thất bại', bg: '#fee2e2', color: '#dc2626', border: '#fecaca' };
    case 'SHIPPING_FAILED':
      return { label: 'Giao vận thất bại', bg: '#fff1f2', color: '#e11d48', border: '#fecdd3' };
    case 'DELIVERED':
      return { label: 'Giao thành công', bg: '#ecfdf5', color: '#059669', border: '#a7f3d0' };
    case 'RETURNED':
      return { label: 'Đã hoàn về kho', bg: '#f3e8ff', color: '#7c3aed', border: '#e9d5ff' };
    case 'CANCELLED':
      return { label: 'Đã hủy', bg: '#f1f5f9', color: '#64748b', border: '#e2e8f0' };
    default:
      return { label: 'Chờ lấy hàng', bg: '#fffbeb', color: '#d97706', border: '#fde68a' };
  }
}

function getShipmentTypeInfo(type) {
  const typeCode = typeof type === 'object' ? (type?.code || type?.name || '') : String(type || '');
  const typeStr = typeCode.toUpperCase();
  switch (typeStr) {
    case 'FORWARD':
      return { label: 'Giao hàng đi', bg: '#fff8f3', color: '#ea580c', border: '#ffedd5' };
    case 'CUSTOMER_RETURN':
      return { label: 'Khách hoàn trả', bg: '#eff6ff', color: '#2563eb', border: '#dbeafe' };
    case 'SUPPLIER_RETURN':
      return { label: 'Xuất trả NCC', bg: '#f3e8ff', color: '#9333ea', border: '#e9d5ff' };
    default:
      return { label: 'Giao hàng đi', bg: '#fff8f3', color: '#ea580c', border: '#ffedd5' };
  }
}

function formatCarrierName(carrier) {
  const c = String(carrier || '').toLowerCase();
  if (c.includes('self') || c.includes('tự')) return 'Tự giao hàng (Bưu cục Shop)';
  if (c.includes('ghn')) return 'Giao Hàng Nhanh (GHN)';
  if (c.includes('ghtk')) return 'Giao Hàng Tiết Kiệm (GHTK)';
  if (c.includes('vnp')) return 'VNPost';
  return carrier || 'Tự giao hàng (Bưu cục Shop)';
}

function formatActionLabel(action) {
  if (!action) return 'Bàn giao cho hãng vận chuyển';
  const code = (typeof action === 'object' ? (action.code || action.name || action.label || action.displayName || '') : String(action || '')).toUpperCase().replace(/\s+/g, '_');
  
  if (code.includes('HAND_OVER') || code.includes('CARRIER')) return 'Bàn giao cho hãng vận chuyển';
  if (code.includes('CANCEL')) return 'Hủy đơn vận chuyển';
  if (code.includes('MARK_DELIVERED') || code.includes('CONFIRM_DELIVERY')) return 'Xác nhận giao thành công';
  if (code.includes('REPORT') || code.includes('FAILED')) return 'Báo cáo giao thất bại';
  if (code.includes('RETRY')) return 'Giao hàng lại';
  if (code.includes('RETURN')) return 'Trả hàng về kho';
  
  return typeof action === 'object' ? (action.label || action.displayName || action.code) : action;
}

function formatLocationName(loc) {
  if (!loc) return 'Bưu cục Shop';
  const l = String(loc).toLowerCase();
  if (l.includes('warehouse')) return 'Kho hàng Shop';
  if (l.includes('hub')) return 'Bưu cục trung chuyển';
  if (l.includes('sorting')) return 'Trung tâm phân loại';
  return loc;
}

function formatLogDescriptionText(desc) {
  if (!desc) return 'Cập nhật tiến trình vận chuyển.';
  const d = String(desc).toLowerCase();
  if (d.includes('created and waiting for pickup') || d.includes('waiting for pickup')) return 'Đã tạo vận đơn, đang chờ bưu tá lấy hàng.';
  if (d.includes('handed over') || d.includes('hand over')) return 'Đã bàn giao cho đơn vị vận chuyển.';
  if (d.includes('in transit')) return 'Bưu kiện đang trên đường vận chuyển.';
  if (d.includes('delivered successfully') || d.includes('delivered')) return 'Đơn hàng đã được giao thành công.';
  if (d.includes('failed')) return 'Giao hàng không thành công.';
  if (d.includes('returned')) return 'Bưu kiện đã hoàn về kho.';
  if (d.includes('cancelled')) return 'Đơn vận chuyển đã bị hủy.';
  return desc;
}

// Helper to check internal test shipment orders
function isInternalTestOrder(item) {
  if (!item) return false;
  
  // 1. If user role is CUSTOMER, it is ALWAYS a real business customer shipment!
  const userRole = String(item.user?.role || item.userRole || '').toUpperCase();
  if (userRole === 'CUSTOMER') return false;

  // 2. Check direct user ID of the buyer (Seed Admin/Staff IDs 1 and 2 only)
  const uid = Number(item.userId || item.user?.id || item.customerId || 0);
  if (uid === 1 || uid === 2) return true;

  // 3. Check test seed order IDs 1, 2, 3
  const oid = Number(item.orderId || item.order?.id || 0);
  if (oid === 1 || oid === 2 || oid === 3) return true;

  // 4. Check buyer user role if attached
  if (['ADMIN', 'STAFF', 'MANAGER'].includes(userRole)) return true;

  // 5. Check buyer email if attached
  const email = String(item.user?.email || item.customerEmail || item.email || '').toLowerCase();
  if (email.includes('admin@') || email.includes('staff@') || email.includes('manager@') || email.includes('@toystore.internal')) {
    return true;
  }

  // 6. Check explicit test note on shipment
  const note = String(item.notes || item.description || '').toLowerCase();
  if (note.includes('đơn test') || note.includes('thử nghiệm nội bộ') || note.includes('[test]')) {
    return true;
  }

  return false;
}

function AdminLogisticsPage() {
  const context = useOutletContext() || {};
  const userRole = context.userRole || 'STAFF';
  const canDelete = true;

  const [activeTab, setActiveTab] = useState('FORWARD'); // 'FORWARD', 'CUSTOMER_RETURN', 'SUPPLIER_RETURN', or 'ALL'
  const [shipments, setShipments] = useState([]);
  const [selected, setSelected] = useState(null);
  const [logs, setLogs] = useState([]);
  const [filters, setFilters] = useState({
    orderId: '',
    status: '',
    trackingCode: '',
    customerReturnId: '',
    supplierReturnId: '',
  });
  const [createOrderId, setCreateOrderId] = useState('');
  const [actionForm, setActionForm] = useState({ action: 'HAND_OVER_TO_CARRIER', reason: '', location: '' });
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [totalShipments, setTotalShipments] = useState(0);

  const displayShipments = shipments;

  useEffect(() => {
    loadShipments(currentPage);
  }, [currentPage, activeTab]);

  async function loadShipments(pageToLoad = currentPage) {
    setLoading(true);
    setError('');
    try {
      const result = await getShipments({
        shipmentType: activeTab !== 'ALL' ? activeTab : undefined,
        orderId: filters.orderId || undefined,
        status: filters.status || undefined,
        trackingCode: filters.trackingCode || undefined,
        customerReturnId: filters.customerReturnId || undefined,
        supplierReturnId: filters.supplierReturnId || undefined,
        page: pageToLoad,
        size: 50,
        sort: 'createdAt,desc',
      });
      setShipments(result.content || result || []);
      setTotalPages(result.totalPages || 1);
      setTotalShipments(Number(result?.totalElements ?? (Array.isArray(result) ? result.length : 0)));
    } catch (err) {
      setError(err.message || 'Không thể tải danh sách vận chuyển.');
      setShipments([]);
      setTotalShipments(0);
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
    setFilters({ orderId: '', status: '', trackingCode: '', shipmentType: '', customerReturnId: '', supplierReturnId: '' });
    setCurrentPage(0);
    setTimeout(() => {
      loadShipments(0);
    }, 50);
  };

  return (
    <section style={{ padding: '24px', background: '#f8fafc', minHeight: '100vh', fontFamily: 'system-ui, -apple-system, sans-serif' }}>
      
      {/* HEADER ROW WITH MODE SWITCHER */}
      <div style={{ background: 'linear-gradient(135deg, #fff8f3 0%, #fff1f2 100%)', border: '1px solid #ffedd5', padding: '16px 24px', borderRadius: '16px', marginBottom: '20px', boxShadow: '0 4px 12px rgba(234,88,12,0.04)', display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '16px' }}>
        <div>
          <h1 style={{ fontSize: '20px', fontWeight: '900', color: '#9a3412', margin: 0, textTransform: 'uppercase', letterSpacing: '0.5px' }}>
            Quản lý Vận chuyển & Logistics
          </h1>
          <div style={{ fontSize: '13px', color: '#15803d', fontWeight: '800', marginTop: '4px' }}>
            Đang hiển thị dữ liệu vận đơn theo bộ lọc backend
          </div>
        </div>
      </div>

      {/* DEDICATED LOGISTICS FLOW CATEGORY TABS */}
      <div style={{ display: 'flex', gap: '10px', marginBottom: '16px', flexWrap: 'wrap' }}>
        <button
          type="button"
          onClick={() => {
            setActiveTab('FORWARD');
            setCurrentPage(0);
            setSelected(null);
            setLogs([]);
          }}
          style={{
            padding: '10px 20px',
            borderRadius: '10px',
            border: activeTab === 'FORWARD' ? '2px solid #ea580c' : '1px solid #cbd5e1',
            background: activeTab === 'FORWARD' ? '#fff7ed' : '#ffffff',
            color: activeTab === 'FORWARD' ? '#ea580c' : '#475569',
            fontSize: '13.5px',
            fontWeight: '800',
            cursor: 'pointer',
            boxShadow: activeTab === 'FORWARD' ? '0 4px 12px rgba(234,88,12,0.12)' : 'none',
            transition: 'all 0.2s ease',
          }}
        >
          📦 Đơn giao hàng đi (Bán lẻ)
        </button>
        <button
          type="button"
          onClick={() => {
            setActiveTab('CUSTOMER_RETURN');
            setCurrentPage(0);
            setSelected(null);
            setLogs([]);
          }}
          style={{
            padding: '10px 20px',
            borderRadius: '10px',
            border: activeTab === 'CUSTOMER_RETURN' ? '2px solid #2563eb' : '1px solid #cbd5e1',
            background: activeTab === 'CUSTOMER_RETURN' ? '#eff6ff' : '#ffffff',
            color: activeTab === 'CUSTOMER_RETURN' ? '#2563eb' : '#475569',
            fontSize: '13.5px',
            fontWeight: '800',
            cursor: 'pointer',
            boxShadow: activeTab === 'CUSTOMER_RETURN' ? '0 4px 12px rgba(37,99,235,0.12)' : 'none',
            transition: 'all 0.2s ease',
          }}
        >
          🔄 Khách hàng Đổi / Trả hàng
        </button>
        <button
          type="button"
          onClick={() => {
            setActiveTab('SUPPLIER_RETURN');
            setCurrentPage(0);
            setSelected(null);
            setLogs([]);
          }}
          style={{
            padding: '10px 20px',
            borderRadius: '10px',
            border: activeTab === 'SUPPLIER_RETURN' ? '2px solid #9333ea' : '1px solid #cbd5e1',
            background: activeTab === 'SUPPLIER_RETURN' ? '#f3e8ff' : '#ffffff',
            color: activeTab === 'SUPPLIER_RETURN' ? '#9333ea' : '#475569',
            fontSize: '13.5px',
            fontWeight: '800',
            cursor: 'pointer',
            boxShadow: activeTab === 'SUPPLIER_RETURN' ? '0 4px 12px rgba(147,51,234,0.12)' : 'none',
            transition: 'all 0.2s ease',
          }}
        >
          🏭 Xuất trả Nhà cung cấp
        </button>
        <button
          type="button"
          onClick={() => {
            setActiveTab('ALL');
            setCurrentPage(0);
            setSelected(null);
            setLogs([]);
          }}
          style={{
            padding: '10px 20px',
            borderRadius: '10px',
            border: activeTab === 'ALL' ? '2px solid #0f172a' : '1px solid #cbd5e1',
            background: activeTab === 'ALL' ? '#f1f5f9' : '#ffffff',
            color: activeTab === 'ALL' ? '#0f172a' : '#475569',
            fontSize: '13.5px',
            fontWeight: '800',
            cursor: 'pointer',
            transition: 'all 0.2s ease',
          }}
        >
          🌐 Tất cả vận đơn
        </button>
      </div>

      {/* ALERTS */}
      {error && <div style={{ background: '#fef2f2', color: '#dc2626', border: '1px solid #fecaca', padding: '10px 14px', borderRadius: '8px', marginBottom: '16px', fontSize: '13px', fontWeight: '700' }}>{error}</div>}
      {message && <div style={{ background: '#f0fdf4', color: '#16a34a', border: '1px solid #bbf7d0', padding: '10px 14px', borderRadius: '8px', marginBottom: '16px', fontSize: '13px', fontWeight: '700' }}>{message}</div>}

      {/* FILTER BAR */}
      <form
        onSubmit={(e) => {
          e.preventDefault();
          setCurrentPage(0);
          loadShipments(0);
        }}
        style={{ background: '#ffffff', padding: '16px', borderRadius: '12px', border: '1px solid #e2e8f0', marginBottom: '16px', display: 'flex', gap: '10px', alignItems: 'center', flexWrap: 'wrap' }}
      >
        <div style={{ flex: '1', minWidth: '160px' }}>
          <select
            value={filters.status}
            onChange={(e) => setFilters({ ...filters, status: e.target.value })}
            style={{ width: '100%', padding: '9px 12px', border: '1px solid #cbd5e1', borderRadius: '8px', fontSize: '13px', outline: 'none', background: '#fff' }}
          >
            <option value="">Tất cả trạng thái</option>
            <option value="PENDING_PICKUP">Chờ lấy hàng</option>
            <option value="IN_TRANSIT">Đang giao hàng</option>
            <option value="DELIVERED">Giao thành công</option>
            <option value="DELIVERY_FAILED">Giao thất bại</option>
            <option value="RETURNED">Đã hoàn về kho</option>
            <option value="CANCELLED">Đã hủy</option>
          </select>
        </div>

        <div style={{ flex: '1', minWidth: '120px' }}>
          <input
            type="text"
            placeholder="Mã đơn..."
            value={filters.orderId}
            onChange={(e) => setFilters({ ...filters, orderId: e.target.value })}
            style={{ width: '100%', padding: '9px 12px', border: '1px solid #cbd5e1', borderRadius: '8px', fontSize: '13px', outline: 'none' }}
          />
        </div>

        <div style={{ flex: '1', minWidth: '120px' }}>
          <input
            type="text"
            placeholder="Mã khách trả..."
            value={filters.customerReturnId}
            onChange={(e) => setFilters({ ...filters, customerReturnId: e.target.value })}
            style={{ width: '100%', padding: '9px 12px', border: '1px solid #cbd5e1', borderRadius: '8px', fontSize: '13px', outline: 'none' }}
          />
        </div>

        <div style={{ flex: '1', minWidth: '120px' }}>
          <input
            type="text"
            placeholder="Mã trả NCC..."
            value={filters.supplierReturnId}
            onChange={(e) => setFilters({ ...filters, supplierReturnId: e.target.value })}
            style={{ width: '100%', padding: '9px 12px', border: '1px solid #cbd5e1', borderRadius: '8px', fontSize: '13px', outline: 'none' }}
          />
        </div>

        <div style={{ flex: '1', minWidth: '180px' }}>
          <input
            type="text"
            placeholder="Mã vận đơn (Tracking Code)..."
            value={filters.trackingCode}
            onChange={(e) => setFilters({ ...filters, trackingCode: e.target.value })}
            style={{ width: '100%', padding: '9px 12px', border: '1px solid #cbd5e1', borderRadius: '8px', fontSize: '13px', outline: 'none' }}
          />
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
            Xóa bộ lọc
          </button>
        </div>
      </form>

      {/* OPERATIONS CONSOLE GRIDS */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '20px', marginBottom: '20px' }}>
        
        {/* Create Shipment Card */}
        <div style={{ background: '#ffffff', borderRadius: '12px', border: '1px solid #e2e8f0', padding: '20px' }}>
          <h3 style={{ fontSize: '14.5px', fontWeight: '800', color: '#0f172a', borderBottom: '1px solid #f1f5f9', paddingBottom: '10px', margin: '0 0 14px 0' }}>
            Tạo vận đơn thủ công (Đơn hàng lẻ)
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
                placeholder="Nhập mã đơn hàng..."
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
              Tạo vận đơn chiều đi
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
                  {(selected?.nextActions?.length ? selected.nextActions : shipmentActions).map((action) => {
                    const actionCode = typeof action === 'object' ? action.code : action;
                    return (
                      <option key={actionCode} value={actionCode}>
                        {formatActionLabel(action)}
                      </option>
                    );
                  })}
                </select>
              </div>
              <div>
                <label style={{ display: 'block', fontSize: '12.5px', fontWeight: '700', color: '#475569', marginBottom: '6px' }}>Địa điểm cập nhật</label>
                <input
                  type="text"
                  value={actionForm.location}
                  onChange={(e) => setActionForm((c) => ({ ...c, location: e.target.value }))}
                  style={{ width: '100%', padding: '8px 10px', border: '1px solid #cbd5e1', borderRadius: '6px', fontSize: '13px', outline: 'none' }}
                />
              </div>
            </div>
            <div>
              <label style={{ display: 'block', fontSize: '12.5px', fontWeight: '700', color: '#475569', marginBottom: '6px' }}>Lý do / Ghi chú chi tiết</label>
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
              Cập nhật trạng thái bưu cục
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
                <th style={{ padding: '14px 16px', width: '70px' }}>ID</th>
                <th style={{ padding: '14px 16px', width: '110px' }}>Phân loại</th>
                <th style={{ padding: '14px 16px', width: '120px' }}>Tham chiếu nguồn</th>
                <th style={{ padding: '14px 16px', width: '130px' }}>Trạng thái</th>
                <th style={{ padding: '14px 16px', width: '140px' }}>Hãng vận chuyển</th>
                <th style={{ padding: '14px 16px', width: '160px' }}>Mã vận đơn</th>
                <th style={{ padding: '14px 16px', width: '150px', textAlign: 'center' }}>Thao tác</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr>
                  <td colSpan="7" style={{ padding: '36px', textAlign: 'center', color: '#64748b' }}>
                    Đang tải danh sách vận chuyển...
                  </td>
                </tr>
              ) : displayShipments.length === 0 ? (
                <tr>
                  <td colSpan="7" style={{ padding: '36px', textAlign: 'center', color: '#94a3b8' }}>
                    Không có vận đơn phù hợp với bộ lọc hiện tại.
                  </td>
                </tr>
              ) : (
                displayShipments.map((s, idx) => {
                  const statusInfo = getShipmentStatusInfo(s.status);
                  const typeInfo = getShipmentTypeInfo(s.shipmentType);

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
                      <td style={{ padding: '14px 16px' }}>
                        <span
                          style={{
                            background: typeInfo.bg,
                            color: typeInfo.color,
                            border: `1px solid ${typeInfo.border}`,
                            padding: '3px 8px',
                            borderRadius: '6px',
                            fontSize: '11.5px',
                            fontWeight: '700',
                          }}
                        >
                          {s.shipmentTypeDisplayName || typeInfo.label}
                        </span>
                      </td>
                      <td style={{ padding: '14px 16px', fontWeight: '700' }}>
                        {s.orderId ? (
                          <span style={{ color: '#ea580c' }}>DH{s.orderId}</span>
                        ) : s.customerReturnId ? (
                          <span style={{ color: '#2563eb' }}>K-TRA #{s.customerReturnId}</span>
                        ) : s.supplierReturnId ? (
                          <span style={{ color: '#9333ea' }}>NCC-TRA #{s.supplierReturnId}</span>
                        ) : (
                          <span style={{ color: '#94a3b8' }}>-</span>
                        )}
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
                      <td style={{ padding: '14px 16px', color: '#334155', fontWeight: '600' }}>
                        {formatCarrierName(s.providerDisplayName || s.carrierName || s.providerCode)}
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
                            Hành trình
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
          
          <div style={{ padding: '16px', display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: '10px', borderTop: '1px solid #e2e8f0', background: '#f8fafc', flexWrap: 'wrap' }}>
            <span style={{ fontSize: '13px', color: '#64748b', fontWeight: 700 }}>
              Hiển thị {displayShipments.length} / {totalShipments} vận đơn (Trang {currentPage + 1} / {totalPages || 1})
            </span>
            {totalPages > 1 && (
              <div style={{ display: 'flex', gap: '10px' }}>
              <button
                type="button"
                onClick={() => setCurrentPage((p) => Math.max(0, p - 1))}
                disabled={currentPage === 0}
                style={{ padding: '6px 12px', borderRadius: '6px', border: '1px solid #cbd5e1', background: currentPage === 0 ? '#f1f5f9' : '#ffffff', color: currentPage === 0 ? '#94a3b8' : '#334155', cursor: currentPage === 0 ? 'not-allowed' : 'pointer' }}
              >
                Trang trước
              </button>
              <button
                type="button"
                onClick={() => setCurrentPage((p) => Math.min(totalPages - 1, p + 1))}
                disabled={currentPage >= totalPages - 1}
                style={{ padding: '6px 12px', borderRadius: '6px', border: '1px solid #cbd5e1', background: currentPage >= totalPages - 1 ? '#f1f5f9' : '#ffffff', color: currentPage >= totalPages - 1 ? '#94a3b8' : '#334155', cursor: currentPage >= totalPages - 1 ? 'not-allowed' : 'pointer' }}
              >
                Trang sau
              </button>
              </div>
            )}
          </div>
        </div>

        {/* LOGS PANEL */}
        {selected && (
          <aside style={{ background: '#ffffff', borderRadius: '12px', border: '1px solid #e2e8f0', padding: '20px', boxShadow: '0 4px 16px rgba(0,0,0,0.02)' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: '1px solid #f1f5f9', paddingBottom: '12px', marginBottom: '14px' }}>
              <h3 style={{ fontSize: '14px', fontWeight: '800', color: '#0f172a', margin: 0 }}>
                Chi tiết Vận đơn #{selected.id}
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

            {/* DETAILED SPECIFICATIONS OF SHIPMENT */}
            <div style={{ marginBottom: '16px', background: '#f8fafc', padding: '12px', borderRadius: '8px', fontSize: '12.5px', border: '1px solid #e2e8f0' }}>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '8px', color: '#475569' }}>
                <div><strong>Người nhận:</strong> {selected.recipientName || 'Chưa rõ'}</div>
                <div><strong>Điện thoại:</strong> {selected.recipientPhone || 'Ẩn/Không có'}</div>
                <div style={{ gridColumn: 'span 2' }}><strong>Địa chỉ:</strong> {selected.shippingAddressSnapshot}</div>
                {selected.codAmount > 0 && (
                  <div style={{ gridColumn: 'span 2', color: '#ea580c', fontWeight: '700' }}>
                    Thu hộ COD: {selected.codAmount.toLocaleString()} VND
                  </div>
                )}
              </div>
            </div>
            
            <h4 style={{ fontSize: '13px', fontWeight: '750', color: '#334155', margin: '0 0 10px 0' }}>Lịch trình vận đơn</h4>
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
            <span style={{ fontSize: '11px', color: '#64748b', fontWeight: '600' }}>{formatLocationName(log.location)}</span>
          </div>
          <p style={{ margin: 0, color: '#475569', lineHeight: 1.4 }}>
            {formatLogDescriptionText(log.description || log.note)}
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
