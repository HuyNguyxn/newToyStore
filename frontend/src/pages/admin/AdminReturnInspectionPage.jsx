import { useEffect, useState } from 'react';
import {
  finalizeCustomerReturnRefund,
  getCustomerReturns,
  getSupplierReturnCriticalAlerts,
  getSupplierReturns,
  inspectCustomerReturn,
  inspectSupplierReturn,
  receiveCustomerReturn,
  requireCustomerReturnInfo,
  resolveCustomerReturnDispute,
} from '../../services/adminReturnService.js';
import { formatPrice } from '../../utils/formatters.js';

function AdminReturnInspectionPage() {
  const [customerReturns, setCustomerReturns] = useState([]);
  const [supplierReturns, setSupplierReturns] = useState([]);
  const [selectedCustomerReturn, setSelectedCustomerReturn] = useState(null);
  const [selectedSupplierReturn, setSelectedSupplierReturn] = useState(null);
  const [customerForm, setCustomerForm] = useState({
    adminMessage: 'Please provide more return evidence.',
    isPassed: true,
    qcNote: 'QC passed from admin dashboard.',
    isApproved: true,
    resolutionNote: 'Resolved from admin dashboard.',
    refundNote: 'Refund finalized from admin dashboard.',
  });
  const [supplierInspectionJson, setSupplierInspectionJson] = useState('{"items":[{"itemId":1,"acceptedQuantity":1,"discrepancyReason":""}]}');
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    loadData();
  }, []);

  async function loadData() {
    setLoading(true);
    setError('');
    try {
      const [customerResult, supplierResult] = await Promise.all([
        getCustomerReturns({ page: 0, size: 10, sort: 'createdAt,desc' }),
        getSupplierReturns({ page: 0, size: 10, sort: 'createdAt,desc' }),
      ]);
      setCustomerReturns(customerResult.content || []);
      setSupplierReturns(supplierResult.content || []);
    } catch (err) {
      setError(err.message || 'Khong the tai du lieu return.');
    } finally {
      setLoading(false);
    }
  }

  function updateCustomerForm(field, value) {
    setCustomerForm((current) => ({ ...current, [field]: value }));
  }

  async function runAction(action, successMessage) {
    setError('');
    setMessage('');
    setLoading(true);
    try {
      await action();
      setMessage(successMessage);
      await loadData();
    } catch (err) {
      setError(err.message || 'Thao tac return that bai.');
    } finally {
      setLoading(false);
    }
  }

  async function loadCriticalAlerts() {
    await runAction(async () => {
      const alerts = await getSupplierReturnCriticalAlerts();
      setSupplierReturns(Array.isArray(alerts) ? alerts : []);
    }, 'Da tai danh sach canh bao SLA.');
  }

  if (loading && customerReturns.length === 0 && supplierReturns.length === 0) {
    return <div className="page-message">Dang tai return inspection...</div>;
  }

  return (
    <section className="admin-resource">
      <div className="admin-resource__hero">
        <div>
          <p>Admin Workflow</p>
          <h2>Return Inspection</h2>
          <span>Inspect customer returns and supplier returns, then finalize refund or supplier deduction workflow.</span>
        </div>
      </div>

      {error && <div className="form-alert">{error}</div>}
      {message && <div className="form-alert form-alert--success">{message}</div>}

      <div className="admin-crud-grid">
        <div className="admin-api-console">
          <div className="admin-panel__heading">
            <div>
              <p>Customer Return</p>
              <h2>{selectedCustomerReturn ? `Return #${selectedCustomerReturn.id}` : 'Select customer return'}</h2>
            </div>
          </div>

          <label>Admin message<input value={customerForm.adminMessage} onChange={(event) => updateCustomerForm('adminMessage', event.target.value)} /></label>
          <label className="inline-check"><input type="checkbox" checked={customerForm.isPassed} onChange={(event) => updateCustomerForm('isPassed', event.target.checked)} /> QC passed</label>
          <label>QC note<input value={customerForm.qcNote} onChange={(event) => updateCustomerForm('qcNote', event.target.value)} /></label>
          <label className="inline-check"><input type="checkbox" checked={customerForm.isApproved} onChange={(event) => updateCustomerForm('isApproved', event.target.checked)} /> Dispute approved</label>
          <label>Resolution note<input value={customerForm.resolutionNote} onChange={(event) => updateCustomerForm('resolutionNote', event.target.value)} /></label>
          <label>Refund note<input value={customerForm.refundNote} onChange={(event) => updateCustomerForm('refundNote', event.target.value)} /></label>

          <div className="admin-resource-table__actions">
            <button type="button" disabled={!selectedCustomerReturn} onClick={() => runAction(() => requireCustomerReturnInfo(selectedCustomerReturn.id, customerForm.adminMessage), 'Da yeu cau bo sung thong tin.')}>Require info</button>
            <button type="button" disabled={!selectedCustomerReturn} onClick={() => runAction(() => receiveCustomerReturn(selectedCustomerReturn.id), 'Da nhan hang return.')}>Receive</button>
            <button type="button" disabled={!selectedCustomerReturn} onClick={() => runAction(() => inspectCustomerReturn(selectedCustomerReturn.id, customerForm), 'Da QC return.')}>Inspect</button>
            <button type="button" disabled={!selectedCustomerReturn} onClick={() => runAction(() => resolveCustomerReturnDispute(selectedCustomerReturn.id, customerForm), 'Da xu ly dispute.')}>Resolve dispute</button>
            <button type="button" disabled={!selectedCustomerReturn} onClick={() => runAction(() => finalizeCustomerReturnRefund(selectedCustomerReturn.id, customerForm.refundNote), 'Da finalize refund.')}>Finalize refund</button>
          </div>
        </div>

        <div className="admin-api-console">
          <div className="admin-panel__heading">
            <div>
              <p>Supplier Return</p>
              <h2>{selectedSupplierReturn ? `Return #${selectedSupplierReturn.id}` : 'Select supplier return'}</h2>
            </div>
          </div>

          <button type="button" onClick={loadCriticalAlerts}>Load SLA critical alerts</button>
          <label>Inspection JSON<textarea rows="8" value={supplierInspectionJson} onChange={(event) => setSupplierInspectionJson(event.target.value)} /></label>
          <button
            type="button"
            disabled={!selectedSupplierReturn}
            onClick={() => runAction(() => inspectSupplierReturn(selectedSupplierReturn.id, JSON.parse(supplierInspectionJson)), 'Da ghi nhan supplier inspection.')}
          >
            Inspect supplier return
          </button>
        </div>
      </div>

      <div className="admin-crud-grid">
        <ReturnTable title="Customer returns" rows={customerReturns} selectedId={selectedCustomerReturn?.id} onSelect={setSelectedCustomerReturn} />
        <ReturnTable title="Supplier returns" rows={supplierReturns} selectedId={selectedSupplierReturn?.id} onSelect={setSelectedSupplierReturn} />
      </div>
    </section>
  );
}

function ReturnTable({ title, rows, selectedId, onSelect }) {
  return (
    <div className="admin-resource-table">
      <div className="admin-resource-table__head" style={{ gridTemplateColumns: '80px 120px 130px 130px 130px' }}>
        <span>{title}</span><span>Order/Supplier</span><span>Status</span><span>Refund</span><span>Action</span>
      </div>
      {rows.map((row) => (
        <div className="admin-resource-table__row" style={{ gridTemplateColumns: '80px 120px 130px 130px 130px' }} key={row.id}>
          <span>{row.id}</span>
          <span>{row.orderId || row.supplierId || '-'}</span>
          <span>{row.status}</span>
          <span>{formatPrice(row.totalRefundAmount)}</span>
          <span className="admin-resource-table__actions">
            <button type="button" className={selectedId === row.id ? 'is-danger' : ''} onClick={() => onSelect(row)}>
              Select
            </button>
          </span>
        </div>
      ))}
    </div>
  );
}

export default AdminReturnInspectionPage;
