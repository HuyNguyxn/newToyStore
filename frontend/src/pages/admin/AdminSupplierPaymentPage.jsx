import { useEffect, useState } from 'react';
import {
  cancelSupplierPayment,
  createSupplierPaymentFromImport,
  getSupplierPaymentDetails,
  getSupplierPayments,
  recordSupplierPayment,
} from '../../services/adminSupplierPaymentService.js';
import { formatDateTime, formatPrice } from '../../utils/formatters.js';

function enumCode(value) {
  if (value && typeof value === 'object') return String(value.code || value.name || '').toUpperCase();
  return String(value || '').toUpperCase();
}

function statusInfo(status) {
  const code = enumCode(status);
  if (code === 'PAID') return { label: 'Đã thanh toán', bg: '#dcfce7', color: '#15803d', border: '#bbf7d0' };
  if (code === 'PARTIALLY_PAID') return { label: 'Thanh toán một phần', bg: '#e0f2fe', color: '#0369a1', border: '#bae6fd' };
  if (code === 'OVERDUE') return { label: 'Quá hạn', bg: '#fee2e2', color: '#dc2626', border: '#fecaca' };
  if (code === 'CANCELLED') return { label: 'Đã hủy', bg: '#f1f5f9', color: '#64748b', border: '#cbd5e1' };
  return { label: 'Chưa thanh toán', bg: '#fef3c7', color: '#d97706', border: '#fde68a' };
}

function methodText(method) {
  const code = enumCode(method);
  if (code === 'BANK_TRANSFER') return 'Chuyển khoản';
  if (code === 'CASH') return 'Tiền mặt';
  if (code === 'OTHER') return 'Khác';
  return code || 'Chưa rõ';
}

function AdminSupplierPaymentPage() {
  const [payments, setPayments] = useState([]);
  const [selected, setSelected] = useState(null);
  const [filters, setFilters] = useState({ supplierId: '', importNoteId: '', status: '' });
  const [pageInfo, setPageInfo] = useState({ number: 0, totalPages: 1, totalElements: 0 });
  const [createImportNoteId, setCreateImportNoteId] = useState('');
  const [paymentForm, setPaymentForm] = useState({
    amount: '',
    method: 'BANK_TRANSFER',
    referenceCode: '',
    paidDate: new Date().toISOString().slice(0, 10),
    note: '',
  });
  const [cancelReason, setCancelReason] = useState('');
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    loadPayments();
  }, []);

  async function loadPayments(page = pageInfo.number) {
    setLoading(true);
    setError('');
    try {
      const result = await getSupplierPayments({
        supplierId: filters.supplierId || undefined,
        importNoteId: filters.importNoteId || undefined,
        status: filters.status || undefined,
        page,
        size: 20,
        sort: 'createdAt,desc',
      });
      const list = result.content || result || [];
      setPayments(list);
      setPageInfo({
        number: result.number ?? page,
        totalPages: result.totalPages ?? 1,
        totalElements: result.totalElements ?? list.length,
      });
    } catch (err) {
      setPayments([]);
      setPageInfo({ number: 0, totalPages: 1, totalElements: 0 });
      setError(err.message || 'Không thể tải danh sách thanh toán nội bộ.');
    } finally {
      setLoading(false);
    }
  }

  async function selectPayment(payment) {
    setSelected(payment);
    setError('');
    try {
      const detail = await getSupplierPaymentDetails(payment.id);
      setSelected(detail);
      setPaymentForm((current) => ({ ...current, amount: detail.remainingAmount || '' }));
    } catch (err) {
      setError(err.message || 'Không thể tải chi tiết thanh toán nội bộ.');
    }
  }

  async function runAction(action, successMessage) {
    setError('');
    setMessage('');
    try {
      const result = await action();
      setMessage(successMessage);
      if (result?.id) {
        await selectPayment(result);
      }
      await loadPayments(pageInfo.number);
    } catch (err) {
      setError(err.message || 'Thao tác thanh toán nội bộ thất bại.');
    }
  }

  async function handleCreateFromImport(event) {
    event.preventDefault();
    if (!createImportNoteId.trim()) return;
    await runAction(
      () => createSupplierPaymentFromImport(createImportNoteId.trim()),
      'Đã tạo khoản phải trả từ phiếu nhập.'
    );
    setCreateImportNoteId('');
  }

  async function handleRecordPayment(event) {
    event.preventDefault();
    if (!selected) return;
    await runAction(
      () => recordSupplierPayment(selected.id, {
        amount: Number(paymentForm.amount),
        method: paymentForm.method,
        referenceCode: paymentForm.referenceCode || null,
        paidDate: paymentForm.paidDate || null,
        note: paymentForm.note || null,
      }),
      'Đã ghi nhận thanh toán nội bộ.'
    );
  }

  const selectedStatus = enumCode(selected?.status);
  const canPay = selected && !['PAID', 'CANCELLED'].includes(selectedStatus);
  const canCancel = selected && ['PENDING', 'PARTIALLY_PAID', 'OVERDUE'].includes(selectedStatus);

  return (
    <section style={{ padding: 24, background: '#f8fafc', minHeight: '100vh', fontFamily: 'system-ui, -apple-system, sans-serif' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', gap: 16, alignItems: 'flex-start', marginBottom: 20, flexWrap: 'wrap' }}>
        <div>
          <p style={{ margin: '0 0 6px', color: '#ea580c', fontSize: 13, fontWeight: 900, textTransform: 'uppercase' }}>Quản lý mua hàng</p>
          <h1 style={{ margin: 0, color: '#0f172a', fontSize: 26, fontWeight: 900 }}>Thanh toán Nhà cung cấp</h1>
          <p style={{ margin: '8px 0 0', color: '#64748b' }}>Theo dõi công nợ phát sinh từ phiếu nhập và tiền đã chi cho nhà cung cấp.</p>
        </div>
        <form onSubmit={handleCreateFromImport} style={{ display: 'flex', gap: 8, background: '#fff', border: '1px solid #e2e8f0', borderRadius: 12, padding: 10 }}>
          <input
            type="number"
            placeholder="Mã phiếu nhập"
            value={createImportNoteId}
            onChange={(event) => setCreateImportNoteId(event.target.value)}
            style={{ width: 150, border: '1px solid #cbd5e1', borderRadius: 8, padding: '9px 10px' }}
          />
          <button type="submit" style={{ border: 'none', borderRadius: 8, background: '#ea580c', color: '#fff', fontWeight: 900, padding: '0 14px', cursor: 'pointer' }}>
            Tạo công nợ
          </button>
        </form>
      </div>

      {error && <div style={{ background: '#fef2f2', color: '#dc2626', border: '1px solid #fecaca', padding: 12, borderRadius: 8, marginBottom: 12, fontWeight: 800 }}>{error}</div>}
      {message && <div style={{ background: '#f0fdf4', color: '#15803d', border: '1px solid #bbf7d0', padding: 12, borderRadius: 8, marginBottom: 12, fontWeight: 800 }}>{message}</div>}

      <form onSubmit={(event) => { event.preventDefault(); loadPayments(0); }} style={{ display: 'flex', gap: 10, flexWrap: 'wrap', background: '#fff', border: '1px solid #e2e8f0', borderRadius: 12, padding: 14, marginBottom: 16 }}>
        <input type="number" placeholder="Mã nhà cung cấp" value={filters.supplierId} onChange={(event) => setFilters({ ...filters, supplierId: event.target.value })} style={{ flex: 1, minWidth: 160, border: '1px solid #cbd5e1', borderRadius: 8, padding: '10px 12px' }} />
        <input type="number" placeholder="Mã phiếu nhập" value={filters.importNoteId} onChange={(event) => setFilters({ ...filters, importNoteId: event.target.value })} style={{ flex: 1, minWidth: 160, border: '1px solid #cbd5e1', borderRadius: 8, padding: '10px 12px' }} />
        <select value={filters.status} onChange={(event) => setFilters({ ...filters, status: event.target.value })} style={{ flex: 1, minWidth: 190, border: '1px solid #cbd5e1', borderRadius: 8, padding: '10px 12px', background: '#fff' }}>
          <option value="">Tất cả trạng thái</option>
          <option value="PENDING">Chưa thanh toán</option>
          <option value="PARTIALLY_PAID">Thanh toán một phần</option>
          <option value="PAID">Đã thanh toán</option>
          <option value="OVERDUE">Quá hạn</option>
          <option value="CANCELLED">Đã hủy</option>
        </select>
        <button type="submit" style={{ border: 'none', borderRadius: 8, background: '#ea580c', color: '#fff', fontWeight: 900, padding: '0 20px', cursor: 'pointer' }}>Lọc</button>
      </form>

      <div style={{ display: 'grid', gridTemplateColumns: selected ? 'minmax(0, 1fr) 380px' : '1fr', gap: 16 }}>
        <div style={{ background: '#fff', border: '1px solid #e2e8f0', borderRadius: 12, overflow: 'hidden' }}>
          <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: 13 }}>
            <thead>
              <tr style={{ background: '#f8fafc', color: '#334155', textAlign: 'left' }}>
                <th style={{ padding: 14 }}>Mã công nợ</th>
                <th style={{ padding: 14 }}>Nhà cung cấp</th>
                <th style={{ padding: 14 }}>Phiếu nhập</th>
                <th style={{ padding: 14 }}>Tổng tiền</th>
                <th style={{ padding: 14 }}>Còn nợ</th>
                <th style={{ padding: 14 }}>Trạng thái</th>
                <th style={{ padding: 14 }}>Thao tác</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr><td colSpan="7" style={{ padding: 30, textAlign: 'center', color: '#94a3b8' }}>Đang tải dữ liệu...</td></tr>
              ) : payments.length === 0 ? (
                <tr><td colSpan="7" style={{ padding: 30, textAlign: 'center', color: '#94a3b8' }}>Chưa có khoản thanh toán nhà cung cấp phù hợp.</td></tr>
              ) : payments.map((payment) => {
                const info = statusInfo(payment.status);
                return (
                  <tr key={payment.id} style={{ borderTop: '1px solid #f1f5f9' }}>
                    <td style={{ padding: 14, fontWeight: 900, color: '#ea580c' }}>{payment.invoiceCode}</td>
                    <td style={{ padding: 14 }}>{payment.supplierName || `NCC${payment.supplierId}`}</td>
                    <td style={{ padding: 14 }}>PN{payment.importNoteId}</td>
                    <td style={{ padding: 14, fontWeight: 800 }}>{formatPrice(payment.totalAmount)}</td>
                    <td style={{ padding: 14, fontWeight: 900, color: payment.remainingAmount > 0 ? '#dc2626' : '#15803d' }}>{formatPrice(payment.remainingAmount)}</td>
                    <td style={{ padding: 14 }}><span style={{ background: info.bg, color: info.color, border: `1px solid ${info.border}`, borderRadius: 8, padding: '4px 10px', fontWeight: 900 }}>{info.label}</span></td>
                    <td style={{ padding: 14 }}><button type="button" onClick={() => selectPayment(payment)} style={{ border: '1px solid #cbd5e1', background: '#fff', borderRadius: 8, padding: '7px 12px', fontWeight: 800, cursor: 'pointer' }}>Quản lý</button></td>
                  </tr>
                );
              })}
            </tbody>
          </table>
          {pageInfo.totalPages > 1 && (
            <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', gap: 10, padding: '14px 0', borderTop: '1px solid #f1f5f9' }}>
              <button
                type="button"
                disabled={pageInfo.number === 0}
                onClick={() => loadPayments(pageInfo.number - 1)}
                style={{ padding: '7px 14px', border: '1px solid #cbd5e1', borderRadius: 8, background: '#fff', cursor: pageInfo.number === 0 ? 'not-allowed' : 'pointer', fontWeight: 800 }}
              >
                Trang trước
              </button>
              <span style={{ fontSize: 13, fontWeight: 800, color: '#475569' }}>
                Trang {pageInfo.number + 1} / {pageInfo.totalPages}
              </span>
              <button
                type="button"
                disabled={pageInfo.number >= pageInfo.totalPages - 1}
                onClick={() => loadPayments(pageInfo.number + 1)}
                style={{ padding: '7px 14px', border: '1px solid #cbd5e1', borderRadius: 8, background: '#fff', cursor: pageInfo.number >= pageInfo.totalPages - 1 ? 'not-allowed' : 'pointer', fontWeight: 800 }}
              >
                Trang sau
              </button>
            </div>
          )}
        </div>

        {selected && (
          <aside style={{ background: '#fff', border: '1px solid #e2e8f0', borderRadius: 12, padding: 18 }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 14 }}>
              <h2 style={{ margin: 0, fontSize: 18, fontWeight: 900, color: '#0f172a' }}>{selected.invoiceCode}</h2>
              <button type="button" onClick={() => setSelected(null)} style={{ border: 'none', background: '#f1f5f9', borderRadius: 8, width: 30, height: 30, fontWeight: 900, cursor: 'pointer' }}>×</button>
            </div>

            <div style={{ display: 'grid', gap: 8, background: '#f8fafc', border: '1px solid #e2e8f0', borderRadius: 8, padding: 12, marginBottom: 14, fontSize: 13 }}>
              <div>Nhà cung cấp: <strong>{selected.supplierName || `NCC${selected.supplierId}`}</strong></div>
              <div>Phiếu nhập: <strong>PN{selected.importNoteId}</strong></div>
              <div>Tổng tiền: <strong>{formatPrice(selected.totalAmount)}</strong></div>
              <div>Đã trả: <strong>{formatPrice(selected.paidAmount)}</strong></div>
              <div>Còn nợ: <strong style={{ color: '#dc2626' }}>{formatPrice(selected.remainingAmount)}</strong></div>
              <div>Hạn thanh toán: <strong>{selected.dueDate || 'Chưa đặt'}</strong></div>
            </div>

            {canPay && (
              <form onSubmit={handleRecordPayment} style={{ display: 'grid', gap: 10, marginBottom: 16 }}>
                <input type="number" min="0.01" step="0.01" placeholder="Số tiền chi trả" value={paymentForm.amount} onChange={(event) => setPaymentForm({ ...paymentForm, amount: event.target.value })} style={{ border: '1px solid #cbd5e1', borderRadius: 8, padding: 10 }} />
                <select value={paymentForm.method} onChange={(event) => setPaymentForm({ ...paymentForm, method: event.target.value })} style={{ border: '1px solid #cbd5e1', borderRadius: 8, padding: 10, background: '#fff' }}>
                  <option value="BANK_TRANSFER">Chuyển khoản</option>
                  <option value="CASH">Tiền mặt</option>
                  <option value="OTHER">Khác</option>
                </select>
                <input type="text" placeholder="Mã tham chiếu" value={paymentForm.referenceCode} onChange={(event) => setPaymentForm({ ...paymentForm, referenceCode: event.target.value })} style={{ border: '1px solid #cbd5e1', borderRadius: 8, padding: 10 }} />
                <input type="date" value={paymentForm.paidDate} onChange={(event) => setPaymentForm({ ...paymentForm, paidDate: event.target.value })} style={{ border: '1px solid #cbd5e1', borderRadius: 8, padding: 10 }} />
                <input type="text" placeholder="Ghi chú" value={paymentForm.note} onChange={(event) => setPaymentForm({ ...paymentForm, note: event.target.value })} style={{ border: '1px solid #cbd5e1', borderRadius: 8, padding: 10 }} />
                <button type="submit" style={{ border: 'none', borderRadius: 8, background: '#16a34a', color: '#fff', fontWeight: 900, padding: 11, cursor: 'pointer' }}>Ghi nhận thanh toán</button>
              </form>
            )}

            {canCancel && (
              <div style={{ display: 'grid', gap: 8, marginBottom: 16 }}>
                <input type="text" placeholder="Lý do hủy" value={cancelReason} onChange={(event) => setCancelReason(event.target.value)} style={{ border: '1px solid #cbd5e1', borderRadius: 8, padding: 10 }} />
                <button type="button" onClick={() => runAction(() => cancelSupplierPayment(selected.id, cancelReason), 'Đã hủy khoản thanh toán nội bộ.')} style={{ border: '1px solid #fecaca', borderRadius: 8, background: '#fff', color: '#dc2626', fontWeight: 900, padding: 10, cursor: 'pointer' }}>Hủy công nợ</button>
              </div>
            )}

            <h3 style={{ fontSize: 14, fontWeight: 900, color: '#0f172a' }}>Lịch sử chi trả</h3>
            {(selected.transactions || []).length === 0 ? (
              <p style={{ color: '#94a3b8', fontSize: 13 }}>Chưa có lần chi trả nào.</p>
            ) : (
              <div style={{ display: 'grid', gap: 8 }}>
                {selected.transactions.map((transaction) => (
                  <div key={transaction.id} style={{ border: '1px solid #e2e8f0', borderRadius: 8, padding: 10, background: '#f8fafc', fontSize: 13 }}>
                    <strong>{formatPrice(transaction.amount)}</strong>
                    <div>{methodText(transaction.method)} - {transaction.paidDate || formatDateTime(transaction.createdAt)}</div>
                    {transaction.referenceCode && <div>Mã tham chiếu: {transaction.referenceCode}</div>}
                    {transaction.note && <div>Ghi chú: {transaction.note}</div>}
                  </div>
                ))}
              </div>
            )}
          </aside>
        )}
      </div>
    </section>
  );
}

export default AdminSupplierPaymentPage;
