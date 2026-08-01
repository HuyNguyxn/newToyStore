import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import BackLink from '../../components/common/BackLink.jsx';
import { cancelCustomerReturn, disputeCustomerReturn, getCustomerReturns, updateCustomerReturnInfo } from '../../services/customerReturnService.js';
import { formatPrice } from '../../utils/formatters.js';

function ReturnListPage() {
  const [returns, setReturns] = useState([]);
  const [selected, setSelected] = useState(null);
  const [filters, setFilters] = useState({ status: '', orderId: '' });
  const [reasonNote, setReasonNote] = useState('');
  const [disputeReason, setDisputeReason] = useState('');
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  useEffect(() => { loadReturns(); }, []);

  async function loadReturns() {
    try {
      const result = await getCustomerReturns({ ...filters, page: 0, size: 20, sort: 'createdAt,desc' });
      setReturns(result.content || []);
    } catch (err) {
      setError(err.message || 'Khong the tai yeu cau tra hang.');
    }
  }

  function selectReturn(item) {
    setSelected(item);
    setReasonNote('');
    setDisputeReason('');
  }

  async function doAction(action, success) {
    setError('');
    setMessage('');
    try {
      const result = await action();
      setSelected(result || selected);
      setMessage(success);
      await loadReturns();
    } catch (err) {
      setError(err.message || 'Thao tac return that bai.');
    }
  }

  return (
    <section className="container profile-page">
      <BackLink fallback="/profile" label="Quay lai tai khoan" />
      <div className="customer-panel-hero"><div><p>After-sales</p><h2>Yeu cau tra hang cua toi</h2><span>Theo doi trang thai, cap nhat thong tin hoac mo tranh chap neu can.</span></div><Link className="login-link" to="/returns/new">Tao yeu cau moi</Link></div>
      {error && <div className="form-alert">{error}</div>}{message && <div className="form-alert form-alert--success">{message}</div>}
      <form className="admin-filter" onSubmit={(e) => { e.preventDefault(); loadReturns(); }}>{Object.keys(filters).map((field) => <label key={field}>{field}<input value={filters[field]} onChange={(e) => setFilters((current) => ({ ...current, [field]: e.target.value }))} /></label>)}<button type="submit">Filter</button></form>

      <div className="admin-crud-grid">
        <div className="admin-resource-table">
          <div className="admin-resource-table__head" style={{ gridTemplateColumns: '80px 100px 150px 150px 160px' }}><span>ID</span><span>Order</span><span>Status</span><span>Refund</span><span>Actions</span></div>
          {returns.map((item) => <div className="admin-resource-table__row" style={{ gridTemplateColumns: '80px 100px 150px 150px 160px' }} key={item.id}><span>{item.id}</span><span>{item.orderId}</span><span>{item.status?.code || item.status}</span><span>{formatPrice(item.totalRefundAmount)}</span><span className="admin-resource-table__actions"><button type="button" onClick={() => selectReturn(item)}>Detail</button><button type="button" className="is-danger" onClick={() => doAction(() => cancelCustomerReturn(item.id), 'Da huy yeu cau tra hang.')}>Cancel</button></span></div>)}
        </div>

        <aside className="admin-api-console">
          <div className="admin-panel__heading"><div><p>Selected</p><h2>{selected ? `Return #${selected.id}` : 'Chon return'}</h2></div></div>
          {selected && (
            <>
              <div className="admin-detail-summary">
                <p><strong>Order:</strong> #{selected.orderId}</p>
                <p><strong>Status:</strong> {selected.status?.displayName || selected.status?.code || selected.status}</p>
                <p><strong>Refund:</strong> {formatPrice(selected.totalRefundAmount)}</p>
              </div>
              <div className="admin-line-items">
                <strong>Proof images</strong>
                <div className="admin-mini-gallery">{(selected.proofImages || []).map((url) => <img key={url} src={url} alt="Return proof" />)}</div>
              </div>
              <div className="admin-line-items">
                <strong>Items</strong>
                {(selected.items || []).map((item) => <div className="admin-log-list__item" key={item.id || item.orderItemId}><strong>Order item #{item.orderItemId}</strong><p>Qty {item.quantity} · {item.reasonCode || item.reason} · {formatPrice(item.expectedRefundAmount || item.refundAmount || 0)}</p></div>)}
              </div>
              <form className="admin-line-items" onSubmit={(e) => { e.preventDefault(); doAction(() => updateCustomerReturnInfo(selected.id, reasonNote), 'Da cap nhat thong tin return.'); }}>
                <label>New reason note<input value={reasonNote} onChange={(e) => setReasonNote(e.target.value)} required /></label>
                <button type="submit">Update info</button>
              </form>
              <form className="admin-line-items" onSubmit={(e) => { e.preventDefault(); doAction(() => disputeCustomerReturn(selected.id, disputeReason), 'Da mo tranh chap return.'); }}>
                <label>Dispute reason<input value={disputeReason} onChange={(e) => setDisputeReason(e.target.value)} required /></label>
                <button type="submit">Open dispute</button>
              </form>
              <div className="admin-line-items">
                <strong>History</strong>
                {(selected.histories || []).map((history) => <div className="admin-log-list__item" key={history.id || `${history.status}-${history.createdAt}`}><strong>{history.status}</strong><p>{history.note || history.message || 'Cap nhat return'}</p></div>)}
              </div>
            </>
          )}
        </aside>
      </div>
    </section>
  );
}

export default ReturnListPage;
