import { useEffect, useState } from 'react';
import { cancelAdminPayment, deleteAdminPayment, getAdminPaymentDetails, getAdminPaymentRefunds, getAdminPayments, markPaymentFailed, markPaymentSucceeded } from '../../services/adminPaymentService.js';
import { formatDateTime, formatPrice } from '../../utils/formatters.js';

function AdminPaymentPage() {
  const [payments, setPayments] = useState([]);
  const [selected, setSelected] = useState(null);
  const [refunds, setRefunds] = useState([]);
  const [filters, setFilters] = useState({ status: '', method: '', orderId: '' });
  const [providerTransactionId, setProviderTransactionId] = useState('');
  const [reason, setReason] = useState('Updated from admin dashboard');
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  useEffect(() => { loadPayments(); }, []);

  async function loadPayments() {
    try {
      const result = await getAdminPayments({ ...filters, page: 0, size: 20, sort: 'createdAt,desc' });
      setPayments(result.content || []);
    } catch (err) {
      setError(err.message || 'Kh?ng th? t?i payments.');
    }
  }

  async function selectPayment(payment) {
    setSelected(payment);
    try {
      const [detail, refundPage] = await Promise.all([
        getAdminPaymentDetails(payment.id),
        getAdminPaymentRefunds(payment.id, { page: 0, size: 10 }),
      ]);
      setSelected(detail);
      setRefunds(refundPage.content || []);
    } catch {
      setRefunds([]);
    }
  }

  async function doAction(action, success) {
    setError('');
    setMessage('');
    try {
      const result = await action();
      setMessage(success);
      if (result) setSelected(result);
      await loadPayments();
    } catch (err) {
      setError(err.message || 'Thao t?c payment th?t b?i.');
    }
  }

  return (
    <section className="admin-resource">
      <div className="admin-resource__hero"><div><p>Admin Finance</p><h2>Payments</h2><span>Monitor payments, manual state updates, and related refund requests.</span></div></div>
      {error && <div className="form-alert">{error}</div>}{message && <div className="form-alert form-alert--success">{message}</div>}
      <form className="admin-filter" onSubmit={(e) => { e.preventDefault(); loadPayments(); }}>{Object.keys(filters).map((field) => <label key={field}>{field}<input value={filters[field]} onChange={(e) => setFilters((current) => ({ ...current, [field]: e.target.value }))} /></label>)}<button type="submit">Filter</button></form>

      <div className="admin-crud-grid">
        <div className="admin-resource-table">
          <div className="admin-resource-table__head" style={{ gridTemplateColumns: '70px 90px 120px 130px 130px 180px 170px' }}><span>ID</span><span>Order</span><span>Method</span><span>Status</span><span>Amount</span><span>Created</span><span>Actions</span></div>
          {payments.map((payment) => <div className="admin-resource-table__row" style={{ gridTemplateColumns: '70px 90px 120px 130px 130px 180px 170px' }} key={payment.id}><span>{payment.id}</span><span>{payment.orderId}</span><span>{payment.method}</span><span>{payment.status}</span><span>{formatPrice(payment.amount)}</span><span>{formatDateTime(payment.createdAt)}</span><span className="admin-resource-table__actions"><button type="button" onClick={() => selectPayment(payment)}>Manage</button><button type="button" className="is-danger" onClick={() => doAction(() => deleteAdminPayment(payment.id), '?? x?a payment.')}>Delete</button></span></div>)}
        </div>

        <aside className="admin-api-console">
          <div className="admin-panel__heading"><div><p>Selected</p><h2>{selected ? `Payment #${selected.id}` : 'Choose a payment'}</h2></div></div>
          {selected && (
            <>
              <div className="admin-detail-summary">
                <p><strong>Status:</strong> {selected.status}</p>
                <p><strong>Method:</strong> {selected.method}</p>
                <p><strong>Amount:</strong> {formatPrice(selected.amount)}</p>
              </div>
              <label>Provider transaction ID<input value={providerTransactionId} onChange={(e) => setProviderTransactionId(e.target.value)} /></label>
              <label>Reason<input value={reason} onChange={(e) => setReason(e.target.value)} /></label>
              <div className="admin-resource-table__actions">
                <button type="button" onClick={() => doAction(() => markPaymentSucceeded(selected.id, providerTransactionId), '?? mark payment succeeded.')}>Succeed</button>
                <button type="button" className="is-danger" onClick={() => doAction(() => markPaymentFailed(selected.id, reason), '?? mark payment failed.')}>Fail</button>
                <button type="button" className="is-danger" onClick={() => doAction(() => cancelAdminPayment(selected.id, reason), '?? cancel payment.')}>Cancel</button>
              </div>
              <div className="admin-line-items">
                <strong>Refund requests</strong>
                {refunds.length === 0 && <div className="empty-state">Chua co refund request.</div>}
                {refunds.map((refund) => <div className="admin-log-list__item" key={refund.id}><strong>Refund #{refund.id} · {refund.status}</strong><p>{formatPrice(refund.amount)} · {refund.reason || 'No reason'}</p></div>)}
              </div>
            </>
          )}
        </aside>
      </div>
    </section>
  );
}

export default AdminPaymentPage;
