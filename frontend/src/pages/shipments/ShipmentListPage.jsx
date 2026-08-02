import { useEffect, useState } from 'react';
import { getMyShipments, getShipmentDetails, getShipmentTrackingLogs } from '../../services/shipmentService.js';
import { formatDateTime, formatPrice } from '../../utils/formatters.js';

function ShipmentListPage() {
  const [shipments, setShipments] = useState([]);
  const [selected, setSelected] = useState(null);
  const [logs, setLogs] = useState([]);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  useEffect(() => { loadShipments(); }, []);

  async function loadShipments() {
    try {
      const result = await getMyShipments({ page: 0, size: 20, sort: 'createdAt,desc' });
      setShipments(result.content || []);
    } catch (err) {
      setError(err.message || 'Kh?ng th? t?i v?n chuy?n.');
    }
  }

  async function selectShipment(shipment) {
    setError('');
    setMessage('');
    setSelected(shipment);
    try {
      const [detail, logPage] = await Promise.all([
        getShipmentDetails(shipment.id),
        getShipmentTrackingLogs(shipment.id, { page: 0, size: 30, sort: 'createdAt,asc' }),
      ]);
      setSelected(detail);
      setLogs(logPage.content || []);
    } catch (err) {
      setLogs([]);
      setMessage('?ang hi?n th? th?ng tin v?n chuy?n t? danh s?ch.');
    }
  }

  return (
    <section className="container profile-page">
      <div className="admin-resource__hero"><div><p>Delivery</p><h2>Theo doi van chuyen</h2><span>Xem shipment noi bo, tracking code va lich su giao hang.</span></div></div>
      {error && <div className="form-alert">{error}</div>}{message && <div className="form-alert form-alert--success">{message}</div>}
      <div className="admin-crud-grid">
        <div className="admin-resource-table">
          <div className="admin-resource-table__head" style={{ gridTemplateColumns: '80px 100px 150px 150px 150px 140px' }}><span>ID</span><span>Order</span><span>Status</span><span>Tracking</span><span>Fee</span><span>Actions</span></div>
          {shipments.map((shipment) => <div className="admin-resource-table__row" style={{ gridTemplateColumns: '80px 100px 150px 150px 150px 140px' }} key={shipment.id}><span>{shipment.id}</span><span>{shipment.orderId}</span><span>{shipment.status?.code || shipment.status}</span><span>{shipment.trackingCode || '-'}</span><span>{formatPrice(shipment.shippingFee)}</span><span className="admin-resource-table__actions"><button type="button" onClick={() => selectShipment(shipment)}>Tracking</button></span></div>)}
        </div>

        <aside className="admin-api-console">
          <div className="admin-panel__heading"><div><p>Selected</p><h2>{selected ? `Shipment #${selected.id}` : 'Chon shipment'}</h2></div></div>
          {selected && (
            <>
              <div className="admin-detail-summary">
                <p><strong>Order:</strong> #{selected.orderId}</p>
                <p><strong>Status:</strong> {selected.status?.displayName || selected.status?.code || selected.status}</p>
                <p><strong>Address:</strong> {selected.shippingAddressSnapshot || '-'}</p>
                <p><strong>COD:</strong> {formatPrice(selected.codAmount)}</p>
              </div>
              <div className="admin-line-items">
                <strong>Shipment items</strong>
                {(selected.items || []).map((item) => <div className="admin-log-list__item" key={item.id || item.productId}><strong>{item.productName || `Product #${item.productId}`}</strong><p>Qty {item.quantity} · {item.variantAttributesSnapshot || 'Default'}</p></div>)}
              </div>
              <div className="admin-line-items">
                <strong>Tracking timeline</strong>
                {logs.length === 0 && <div className="empty-state">Chua co tracking log.</div>}
                {logs.map((log) => <div className="admin-log-list__item" key={log.id || `${log.status}-${log.createdAt}`}><strong>{log.status?.displayName || log.status?.code || log.status}</strong><span>{log.location || 'No location'} · {formatDateTime(log.createdAt)}</span><p>{log.description || log.note || 'Cap nhat van chuyen'}</p></div>)}
              </div>
            </>
          )}
        </aside>
      </div>
    </section>
  );
}

export default ShipmentListPage;
