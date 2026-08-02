import { useState } from 'react';
import {
  deletePaymentRefund,
  getPaymentRefunds,
  processPaymentRefund,
  rejectPaymentRefund,
  requestPaymentRefund,
} from '../../services/adminRefundService.js';
import { formatDateTime, formatPrice } from '../../utils/formatters.js';

function AdminRefundPage() {
  const [paymentId, setPaymentId] = useState('');
  const [refunds, setRefunds] = useState([]);
  const [form, setForm] = useState({ amount: '', method: 'COD', reason: '' });
  const [rejectReason, setRejectReason] = useState('Rejected from admin dashboard');
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  async function loadRefunds(event) {
    event?.preventDefault();
    if (!paymentId) return;
    setLoading(true);
    setError('');
    try {
      const result = await getPaymentRefunds(paymentId, { page: 0, size: 20, sort: 'createdAt,desc' });
      setRefunds(result.content || []);
    } catch (err) {
      setRefunds([]);
      setError(err.message || 'Kh?ng th? t?i danh s?ch refund.');
    } finally {
      setLoading(false);
    }
  }

  async function handleCreateRefund(event) {
    event.preventDefault();
    setLoading(true);
    setError('');
    setMessage('');
    try {
      await requestPaymentRefund(paymentId, {
        amount: Number(form.amount),
        method: form.method,
        reason: form.reason.trim(),
      });
      setMessage('?? t?o y?u c?u refund.');
      setForm({ amount: '', method: 'COD', reason: '' });
      await loadRefunds();
    } catch (err) {
      setError(err.message || 'T?o refund th?t b?i.');
    } finally {
      setLoading(false);
    }
  }

  async function handleAction(action) {
    setLoading(true);
    setError('');
    setMessage('');
    try {
      await action();
      setMessage('?? c?p nh?t refund.');
      await loadRefunds();
    } catch (err) {
      setError(err.message || 'Thao t?c refund th?t b?i.');
    } finally {
      setLoading(false);
    }
  }

  return (
    <section className="admin-resource">
      <div className="admin-resource__hero">
        <div>
          <p>Admin Workflow</p>
          <h2>Order / Payment Refund</h2>
          <span>Create refund requests for payments, then process, reject, or delete refund records.</span>
        </div>
        <strong>{refunds.length} refunds</strong>
      </div>

      <form className="admin-filter" onSubmit={loadRefunds}>
        <label>
          paymentId
          <input value={paymentId} onChange={(event) => setPaymentId(event.target.value)} required />
        </label>
        <button type="submit" disabled={loading}>{loading ? 'Loading...' : 'Load refunds'}</button>
      </form>

      {error && <div className="form-alert">{error}</div>}
      {message && <div className="form-alert form-alert--success">{message}</div>}

      <form className="admin-api-console" onSubmit={handleCreateRefund}>
        <div className="admin-panel__heading">
          <div>
            <p>Create refund</p>
            <h2>Refund request</h2>
          </div>
        </div>
        <div className="admin-api-console__row">
          <label>Amount<input type="number" value={form.amount} onChange={(event) => setForm((current) => ({ ...current, amount: event.target.value }))} required /></label>
          <label>Method<select value={form.method} onChange={(event) => setForm((current) => ({ ...current, method: event.target.value }))}><option>COD</option><option>VNPAY</option><option>MANUAL</option></select></label>
        </div>
        <label>Reason<input value={form.reason} onChange={(event) => setForm((current) => ({ ...current, reason: event.target.value }))} required maxLength="255" /></label>
        <button type="submit" disabled={loading || !paymentId}>Create refund</button>
      </form>

      <form className="admin-filter" onSubmit={(event) => event.preventDefault()}>
        <label>
          Reject reason
          <input value={rejectReason} onChange={(event) => setRejectReason(event.target.value)} />
        </label>
      </form>

      <div className="admin-resource-table">
        <div className="admin-resource-table__head" style={{ gridTemplateColumns: '80px 120px 120px 130px 140px 160px 260px' }}>
          <span>ID</span><span>Payment</span><span>Order</span><span>Method</span><span>Amount</span><span>Status</span><span>Actions</span>
        </div>
        {refunds.map((refund) => (
          <div className="admin-resource-table__row" style={{ gridTemplateColumns: '80px 120px 120px 130px 140px 160px 260px' }} key={refund.id}>
            <span>{refund.id}</span>
            <span>{refund.paymentId}</span>
            <span>{refund.orderId}</span>
            <span>{refund.method}</span>
            <span>{formatPrice(refund.amount)}</span>
            <span>{refund.status} {refund.createdAt ? `(${formatDateTime(refund.createdAt)})` : ''}</span>
            <span className="admin-resource-table__actions">
              <button type="button" onClick={() => handleAction(() => processPaymentRefund(refund.id))}>Process</button>
              <button type="button" className="is-danger" onClick={() => handleAction(() => rejectPaymentRefund(refund.id, rejectReason))}>Reject</button>
              <button type="button" className="is-danger" onClick={() => handleAction(() => deletePaymentRefund(refund.id))}>Delete</button>
            </span>
          </div>
        ))}
      </div>
    </section>
  );
}

export default AdminRefundPage;
