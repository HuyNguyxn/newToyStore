import { useEffect, useState } from 'react';
import { createShipmentForOrder, deleteShipment, executeShipmentAction, getShipmentTrackingLogs, getShipments } from '../../services/adminLogisticsService.js';

const shipmentActions = [
  { code: 'HAND_OVER_TO_CARRIER', label: 'Hand over to carrier' },
  { code: 'MARK_DELIVERED', label: 'Mark delivered' },
  { code: 'REPORT_DELIVERY_FAILED', label: 'Report delivery failed' },
  { code: 'RETRY_DELIVERY', label: 'Retry delivery' },
  { code: 'RETURN_TO_WAREHOUSE', label: 'Return to warehouse' },
  { code: 'CANCEL_SHIPMENT', label: 'Cancel shipment' },
];

function AdminLogisticsPage() {
  const [shipments, setShipments] = useState([]);
  const [selected, setSelected] = useState(null);
  const [logs, setLogs] = useState([]);
  const [filters, setFilters] = useState({ orderId: '', userId: '', status: '', providerCode: '', trackingCode: '' });
  const [createOrderId, setCreateOrderId] = useState('');
  const [actionForm, setActionForm] = useState({ action: 'HAND_OVER_TO_CARRIER', reason: '', location: 'Warehouse' });
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  useEffect(() => { loadShipments(); }, []);

  async function loadShipments() {
    try { const result = await getShipments({ ...filters, page: 0, size: 20, sort: 'createdAt,desc' }); setShipments(result.content || []); } catch (err) { setError(err.message || 'Kh?ng th? t?i logistics.'); }
  }

  async function doAction(action, success) {
    setError(''); setMessage('');
    try { await action(); setMessage(success); await loadShipments(); } catch (err) { setError(err.message || 'Thao t?c logistics th?t b?i.'); }
  }

  async function selectShipment(shipment) {
    setSelected(shipment);
    const nextAction = shipment.nextActions?.[0]?.code || shipment.allowedActions?.[0] || 'HAND_OVER_TO_CARRIER';
    setActionForm((current) => ({ ...current, action: nextAction }));
    try { const result = await getShipmentTrackingLogs(shipment.id, { page: 0, size: 20 }); setLogs(result.content || []); } catch { setLogs([]); }
  }

  return <section className="admin-resource">
    <div className="admin-resource__hero"><div><p>Admin Workflow</p><h2>Logistics</h2><span>Create shipments, execute delivery actions, and inspect tracking logs.</span></div></div>
    {error && <div className="form-alert">{error}</div>}{message && <div className="form-alert form-alert--success">{message}</div>}
    <form className="admin-filter" onSubmit={(e) => { e.preventDefault(); loadShipments(); }}>{Object.keys(filters).map((f) => <label key={f}>{f}<input value={filters[f]} onChange={(e) => setFilters((c) => ({ ...c, [f]: e.target.value }))} /></label>)}<button type="submit">Filter</button></form>
    <div className="admin-crud-grid">
      <form className="admin-api-console" onSubmit={(e) => { e.preventDefault(); doAction(() => createShipmentForOrder(createOrderId), '?? t?o shipment.'); }}>
        <div className="admin-panel__heading"><div><p>Create</p><h2>Shipment by order</h2></div></div>
        <label>Order ID<input value={createOrderId} onChange={(e) => setCreateOrderId(e.target.value)} required /></label>
        <button type="submit">Create shipment</button>
      </form>
      <form className="admin-api-console" onSubmit={(e) => { e.preventDefault(); doAction(() => executeShipmentAction(selected.id, actionForm), '?? execute shipment action.'); }}>
        <div className="admin-panel__heading"><div><p>Action</p><h2>{selected ? `Shipment #${selected.id}` : 'Select shipment'}</h2></div></div>
        <div className="admin-api-console__row">
          <label>Action
            <select value={actionForm.action} onChange={(e) => setActionForm((c) => ({ ...c, action: e.target.value }))}>
              {(selected?.nextActions?.length ? selected.nextActions : shipmentActions).map((action) => (
                <option key={action.code} value={action.code}>{action.label || action.displayName || action.code}</option>
              ))}
            </select>
          </label>
          <label>Location<input value={actionForm.location} onChange={(e) => setActionForm((c) => ({ ...c, location: e.target.value }))} /></label>
        </div>
        <label>Reason<input value={actionForm.reason} onChange={(e) => setActionForm((c) => ({ ...c, reason: e.target.value }))} /></label>
        <button type="submit" disabled={!selected}>Execute action</button>
        <TrackingLogList logs={logs} />
      </form>
    </div>
    <div className="admin-resource-table"><div className="admin-resource-table__head" style={{ gridTemplateColumns: '80px 100px 140px 140px 150px 230px' }}><span>ID</span><span>Order</span><span>Status</span><span>Carrier</span><span>Tracking</span><span>Actions</span></div>{shipments.map((s) => <div className="admin-resource-table__row" style={{ gridTemplateColumns: '80px 100px 140px 140px 150px 230px' }} key={s.id}><span>{s.id}</span><span>{s.orderId}</span><span>{s.status}</span><span>{s.carrierName || s.providerCode}</span><span>{s.trackingCode}</span><span className="admin-resource-table__actions"><button type="button" onClick={() => selectShipment(s)}>Select/logs</button><button type="button" className="is-danger" onClick={() => doAction(() => deleteShipment(s.id), '?? x?a shipment.')}>Delete</button></span></div>)}</div>
  </section>;
}

function TrackingLogList({ logs }) {
  if (!logs?.length) {
    return <div className="empty-state">Chua co tracking log.</div>;
  }

  return (
    <div className="admin-log-list">
      {logs.map((log) => (
        <div className="admin-log-list__item" key={log.id || `${log.status}-${log.createdAt}`}>
          <strong>{log.status}</strong>
          <span>{log.location || 'No location'}</span>
          <p>{log.description || log.note || 'Cap nhat van chuyen'}</p>
        </div>
      ))}
    </div>
  );
}

export default AdminLogisticsPage;
