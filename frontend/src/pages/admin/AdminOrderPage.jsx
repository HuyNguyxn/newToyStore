import { useEffect, useState } from 'react';
import { cancelAdminOrder, completeAdminOrder, confirmAdminOrder, deleteAdminOrder, getAdminOrderDetails, getAdminOrders, shipAdminOrder, updateAdminOrderShipping } from '../../services/adminOrderService.js';
import { formatDateTime, formatPrice } from '../../utils/formatters.js';

function AdminOrderPage() {
  const [orders, setOrders] = useState([]);
  const [selected, setSelected] = useState(null);
  const [filters, setFilters] = useState({ status: '', userId: '' });
  const [actionNote, setActionNote] = useState('Updated from admin dashboard');
  const [shippingForm, setShippingForm] = useState({ newAddress: '', note: '' });
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  useEffect(() => { loadOrders(); }, []);

  async function loadOrders() {
    try {
      const result = await getAdminOrders({ ...filters, page: 0, size: 20, sort: 'createdAt,desc' });
      setOrders(result.content || []);
    } catch (err) {
      setError(err.message || 'Kh?ng th? t?i orders.');
    }
  }

  async function selectOrder(order) {
    setSelected(order);
    setShippingForm({ newAddress: order.shippingAddress || '', note: '' });
    try {
      const result = await getAdminOrderDetails(order.id);
      setSelected(result);
      setShippingForm({ newAddress: result.shippingAddress || '', note: '' });
    } catch {
      // Keep table row data if detail endpoint is not available for any reason.
    }
  }

  async function doAction(action, success) {
    setError('');
    setMessage('');
    try {
      const result = await action();
      setMessage(success);
      if (result) setSelected(result);
      await loadOrders();
    } catch (err) {
      setError(err.message || 'Thao t?c order th?t b?i.');
    }
  }

  return (
    <section className="admin-resource">
      <div className="admin-resource__hero"><div><p>Admin Workflow</p><h2>Orders</h2><span>Manage order lifecycle, shipping address, and order detail inspection.</span></div></div>
      {error && <div className="form-alert">{error}</div>}{message && <div className="form-alert form-alert--success">{message}</div>}
      <form className="admin-filter" onSubmit={(e) => { e.preventDefault(); loadOrders(); }}>{Object.keys(filters).map((field) => <label key={field}>{field}<input value={filters[field]} onChange={(e) => setFilters((current) => ({ ...current, [field]: e.target.value }))} /></label>)}<button type="submit">Filter</button></form>

      <div className="admin-crud-grid">
        <div className="admin-resource-table">
          <div className="admin-resource-table__head" style={{ gridTemplateColumns: '70px 90px 130px 140px 140px 180px 160px' }}><span>ID</span><span>User</span><span>Status</span><span>Payment</span><span>Total</span><span>Created</span><span>Actions</span></div>
          {orders.map((order) => <div className="admin-resource-table__row" style={{ gridTemplateColumns: '70px 90px 130px 140px 140px 180px 160px' }} key={order.id}><span>{order.id}</span><span>{order.userId}</span><span>{order.status}</span><span>{order.paymentStatus}</span><span>{formatPrice(order.totalAmount)}</span><span>{formatDateTime(order.createdAt)}</span><span className="admin-resource-table__actions"><button type="button" onClick={() => selectOrder(order)}>Manage</button><button type="button" className="is-danger" onClick={() => doAction(() => deleteAdminOrder(order.id), '?? x?a order.')}>Delete</button></span></div>)}
        </div>

        <aside className="admin-api-console">
          <div className="admin-panel__heading"><div><p>Selected</p><h2>{selected ? `Order #${selected.id}` : 'Choose an order'}</h2></div></div>
          {selected && (
            <>
              <div className="admin-detail-summary">
                <p><strong>Status:</strong> {selected.status}</p>
                <p><strong>Total:</strong> {formatPrice(selected.totalAmount)}</p>
                <p><strong>Address:</strong> {selected.shippingAddress || '-'}</p>
              </div>
              <label>Action note<input value={actionNote} onChange={(e) => setActionNote(e.target.value)} /></label>
              <div className="admin-resource-table__actions">
                <button type="button" onClick={() => doAction(() => confirmAdminOrder(selected.id, actionNote), '?? confirm order.')}>Confirm</button>
                <button type="button" onClick={() => doAction(() => shipAdminOrder(selected.id, actionNote), '?? ship order.')}>Ship</button>
                <button type="button" onClick={() => doAction(() => completeAdminOrder(selected.id, actionNote), '?? complete order.')}>Complete</button>
                <button type="button" className="is-danger" onClick={() => doAction(() => cancelAdminOrder(selected.id, actionNote), '?? cancel order.')}>Cancel</button>
              </div>
              <div className="admin-line-items">
                <strong>Items</strong>
                {(selected.items || []).map((item) => <div className="admin-log-list__item" key={item.id}><strong>{item.productName}</strong><p>{item.variantAttributesSnapshot || 'Default'} x {item.quantity} · {formatPrice((item.price || 0) * item.quantity)}</p></div>)}
              </div>
              <form className="admin-line-items" onSubmit={(e) => { e.preventDefault(); doAction(() => updateAdminOrderShipping(selected.id, shippingForm), '?? c?p nh?t ??a ch? giao h?ng.'); }}>
                <label>New address<input value={shippingForm.newAddress} onChange={(e) => setShippingForm((current) => ({ ...current, newAddress: e.target.value }))} required /></label>
                <label>Shipping note<input value={shippingForm.note} onChange={(e) => setShippingForm((current) => ({ ...current, note: e.target.value }))} /></label>
                <button type="submit">Update shipping address</button>
              </form>
            </>
          )}
        </aside>
      </div>
    </section>
  );
}

export default AdminOrderPage;
