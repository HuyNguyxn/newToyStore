import { useCallback, useEffect, useMemo, useState } from 'react';
import {
  createManualJournalEntry,
  executeAccountingReconciliation,
  getAccountingDashboard,
  getAccountingReconciliationPreview,
  getGeneralLedger,
  getIncomeStatement,
  getJournalEntries,
  getLedgerAccounts,
  getTrialBalance,
  reverseJournalEntry,
} from '../../services/accountingService.js';
import { formatPrice } from '../../utils/formatters.js';

const today = new Date().toISOString().slice(0, 10);
const monthStart = `${today.slice(0, 8)}01`;

const SOURCE_LABELS = {
  OPENING_BALANCE: 'Số dư đầu kỳ',
  OWNER_CAPITAL: 'Vốn chủ sở hữu',
  CUSTOMER_PAYMENT: 'Thu tiền khách hàng',
  CUSTOMER_REFUND: 'Hoàn tiền khách hàng',
  IMPORT_RECEIPT: 'Nhập hàng',
  SUPPLIER_PAYMENT: 'Trả nhà cung cấp',
  OPERATING_EXPENSE: 'Chi phí vận hành',
  FUND_TRANSFER: 'Điều chuyển quỹ',
  MANUAL_ADJUSTMENT: 'Điều chỉnh thủ công',
  REVERSAL: 'Đảo bút toán',
};

const ACCOUNT_TYPE_LABELS = {
  ASSET: 'Tài sản',
  LIABILITY: 'Nợ phải trả',
  EQUITY: 'Vốn chủ sở hữu',
  REVENUE: 'Doanh thu',
  EXPENSE: 'Chi phí',
};

const RECONCILIATION_LABELS = {
  CUSTOMER_PAYMENT: 'Thanh toán khách hàng',
  ORDER_COMPLETION: 'Đơn hàng hoàn tất',
  CUSTOMER_REFUND: 'Hoàn tiền khách hàng',
  IMPORT_RECEIPT: 'Phiếu nhập hoàn tất',
  SUPPLIER_PAYMENT: 'Thanh toán nhà cung cấp',
};

function money(value) {
  return formatPrice(Number(value || 0));
}

function AdminAccountingPage() {
  const [activeView, setActiveView] = useState('overview');
  const [asOf, setAsOf] = useState(today);
  const [from, setFrom] = useState(monthStart);
  const [to, setTo] = useState(today);
  const [minimumReserve, setMinimumReserve] = useState(0);
  const [dashboard, setDashboard] = useState(null);
  const [accounts, setAccounts] = useState([]);
  const [trialBalance, setTrialBalance] = useState(null);
  const [income, setIncome] = useState(null);
  const [reconciliation, setReconciliation] = useState(null);
  const [journalPage, setJournalPage] = useState({ content: [], number: 0, totalPages: 1 });
  const [selectedAccount, setSelectedAccount] = useState('111');
  const [ledgerPage, setLedgerPage] = useState({ content: [], number: 0, totalPages: 1 });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const [manualForm, setManualForm] = useState({
    entryDate: today,
    description: '',
    sourceType: 'MANUAL_ADJUSTMENT',
    sourceReference: '',
    lines: [
      { accountCode: '111', description: '', debitAmount: '', creditAmount: '' },
      { accountCode: '411', description: '', debitAmount: '', creditAmount: '' },
    ],
  });

  const loadOverview = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const [dashboardData, accountData, trialData, incomeData] = await Promise.all([
        getAccountingDashboard({ asOf, minimumReserve }),
        getLedgerAccounts({ asOf }),
        getTrialBalance({ asOf }),
        getIncomeStatement({ from, to }),
      ]);
      setDashboard(dashboardData);
      setAccounts(accountData || []);
      setTrialBalance(trialData);
      setIncome(incomeData);
      try {
        setReconciliation(await getAccountingReconciliationPreview());
      } catch {
        setReconciliation(null);
      }
    } catch (err) {
      setError(err.message || 'Không thể tải dữ liệu kế toán nội bộ.');
    } finally {
      setLoading(false);
    }
  }, [asOf, from, minimumReserve, to]);

  const loadJournals = useCallback(async (page = 0) => {
    setLoading(true);
    setError('');
    try {
      const result = await getJournalEntries({ page, size: 15, sort: 'entryDate,desc' });
      setJournalPage(result);
    } catch (err) {
      setError(err.message || 'Không thể tải sổ nhật ký chung.');
    } finally {
      setLoading(false);
    }
  }, []);

  const loadLedger = useCallback(async (page = 0) => {
    setLoading(true);
    setError('');
    try {
      const result = await getGeneralLedger(selectedAccount, { from, to, page, size: 20 });
      setLedgerPage(result);
    } catch (err) {
      setError(err.message || 'Không thể tải sổ cái tài khoản.');
    } finally {
      setLoading(false);
    }
  }, [from, selectedAccount, to]);

  useEffect(() => { loadOverview(); }, [loadOverview]);
  useEffect(() => { if (activeView === 'journal') loadJournals(0); }, [activeView, loadJournals]);
  useEffect(() => { if (activeView === 'ledger') loadLedger(0); }, [activeView, loadLedger]);

  const debitTotal = useMemo(() => manualForm.lines.reduce((sum, line) => sum + Number(line.debitAmount || 0), 0), [manualForm.lines]);
  const creditTotal = useMemo(() => manualForm.lines.reduce((sum, line) => sum + Number(line.creditAmount || 0), 0), [manualForm.lines]);

  function updateManualLine(index, field, value) {
    setManualForm((current) => ({
      ...current,
      lines: current.lines.map((line, lineIndex) => lineIndex === index ? { ...line, [field]: value } : line),
    }));
  }

  async function submitManualEntry(event) {
    event.preventDefault();
    setError('');
    setMessage('');
    try {
      await createManualJournalEntry({
        ...manualForm,
        lines: manualForm.lines.map((line) => ({
          ...line,
          debitAmount: Number(line.debitAmount || 0),
          creditAmount: Number(line.creditAmount || 0),
        })),
      });
      setMessage('Đã ghi nhận bút toán và cập nhật số dư sổ cái.');
      setManualForm((current) => ({ ...current, description: '', sourceReference: '', lines: current.lines.map((line) => ({ ...line, debitAmount: '', creditAmount: '', description: '' })) }));
      await loadOverview();
      setActiveView('journal');
    } catch (err) {
      setError(err.message || 'Không thể ghi nhận bút toán.');
    }
  }

  async function reverseEntry(id) {
    if (!window.confirm('Bạn xác nhận đảo toàn bộ bút toán này? Bút toán gốc vẫn được giữ để kiểm toán.')) return;
    setError('');
    try {
      await reverseJournalEntry(id);
      setMessage('Đã tạo bút toán đảo, không xóa lịch sử cũ.');
      await Promise.all([loadJournals(journalPage.number || 0), loadOverview()]);
    } catch (err) {
      setError(err.message || 'Không thể đảo bút toán.');
    }
  }

  async function synchronizeAccounting() {
    if (!window.confirm(`Hệ thống tìm thấy ${reconciliation?.detectedCount || 0} nghiệp vụ chưa có trong sổ kế toán. Bạn xác nhận đồng bộ?`)) return;
    setLoading(true);
    setError('');
    setMessage('');
    try {
      const result = await executeAccountingReconciliation();
      setMessage(`Đã tạo ${result.createdCount} bút toán; bỏ qua ${result.skippedCount} nghiệp vụ đã được ghi nhận.`);
      await loadOverview();
    } catch (err) {
      setError(err.message || 'Không thể đồng bộ dữ liệu nghiệp vụ vào sổ kế toán.');
    } finally {
      setLoading(false);
    }
  }

  const metricCards = dashboard ? [
    { label: 'Tổng tiền khả dụng', value: money(dashboard.totalLiquidFunds), tone: 'green' },
    { label: 'Công nợ nhà cung cấp', value: money(dashboard.supplierOutstanding), tone: 'orange' },
    { label: 'Còn lại sau công nợ', value: money(dashboard.availableAfterPayables), tone: dashboard.availableAfterPayables >= 0 ? 'blue' : 'red' },
    { label: 'Lợi nhuận lũy kế', value: money(dashboard.netProfit), tone: dashboard.netProfit >= 0 ? 'violet' : 'red' },
  ] : [];

  return (
    <section className="accounting-page">
      <header className="accounting-header">
        <div>
          <p>TRUNG TÂM VẬN HÀNH</p>
          <h1>Kế toán và dòng tiền nội bộ</h1>
          <span>Theo dõi sổ cái kép; không kết nối hoặc đối soát ngân hàng thật.</span>
        </div>
        <div className="accounting-date-filter">
          <label>Đến ngày<input type="date" value={asOf} onChange={(event) => setAsOf(event.target.value)} /></label>
          <button type="button" onClick={loadOverview}>Cập nhật</button>
        </div>
      </header>

      <nav className="accounting-tabs">
        {[
          ['overview', 'Tổng quan quỹ'], ['journal', 'Nhật ký chung'], ['ledger', 'Sổ cái'],
          ['reports', 'Báo cáo'], ['manual', 'Ghi nhận thủ công'],
        ].map(([value, label]) => (
          <button key={value} type="button" className={activeView === value ? 'is-active' : ''} onClick={() => setActiveView(value)}>{label}</button>
        ))}
      </nav>

      {error && <div className="accounting-alert is-error">{error}</div>}
      {message && <div className="accounting-alert is-success">{message}</div>}
      {loading && <div className="accounting-loading">Đang cập nhật dữ liệu...</div>}

      {activeView === 'overview' && dashboard && (
        <>
          <div className="accounting-metrics">
            {metricCards.map((card) => <article key={card.label} className={`accounting-metric tone-${card.tone}`}><span>{card.label}</span><strong>{card.value}</strong></article>)}
          </div>
          <div className="accounting-grid-two">
            <article className="accounting-panel">
              <h2>Cơ cấu tiền cửa hàng</h2>
              <dl className="accounting-summary-list">
                <div><dt>Tiền mặt</dt><dd>{money(dashboard.cashBalance)}</dd></div>
                <div><dt>Tài khoản thanh toán nội bộ</dt><dd>{money(dashboard.paymentAccountBalance)}</dd></div>
                <div><dt>Giá trị hàng tồn kho</dt><dd>{money(dashboard.inventoryValue)}</dd></div>
                <div><dt>Phải trả trên sổ cái</dt><dd>{money(dashboard.ledgerAccountsPayable)}</dd></div>
              </dl>
            </article>
            <article className="accounting-panel">
              <h2>Khả năng thanh toán nhà cung cấp</h2>
              <dl className="accounting-summary-list">
                <div><dt>Hóa đơn còn mở</dt><dd>{dashboard.openSupplierInvoiceCount}</dd></div>
                <div><dt>Công nợ quá hạn</dt><dd className="danger">{money(dashboard.supplierOverdue)}</dd></div>
                <div><dt>Số hóa đơn quá hạn</dt><dd>{dashboard.overdueSupplierInvoiceCount}</dd></div>
                <div><dt>Có thể chi sau khi giữ quỹ dự phòng</dt><dd className="success">{money(dashboard.safeSupplierPaymentCapacity)}</dd></div>
              </dl>
              <label className="reserve-field">Quỹ dự phòng tối thiểu<input type="number" min="0" value={minimumReserve} onChange={(event) => setMinimumReserve(Number(event.target.value || 0))} /></label>
            </article>
          </div>
          <article className={`accounting-panel accounting-reconciliation ${reconciliation?.detectedCount > 0 ? 'has-missing' : 'is-synced'}`}>
            <div>
              <h2>Đối soát dữ liệu nghiệp vụ</h2>
              <p>
                {reconciliation?.detectedCount > 0
                  ? `Có ${reconciliation.detectedCount} nghiệp vụ lịch sử chưa được ghi vào sổ kế toán.`
                  : 'Sổ kế toán đã đồng bộ với dữ liệu nghiệp vụ.'}
              </p>
              {reconciliation?.groups?.length > 0 && (
                <div className="accounting-reconciliation-groups">
                  {reconciliation.groups.map((group) => (
                    <span key={group.sourceType}>
                      {RECONCILIATION_LABELS[group.sourceType] || SOURCE_LABELS[group.sourceType] || group.sourceType}: <strong>{group.missingCount}</strong>
                    </span>
                  ))}
                </div>
              )}
            </div>
            <button type="button" disabled={!reconciliation?.detectedCount || loading} onClick={synchronizeAccounting}>
              Đồng bộ sổ kế toán
            </button>
          </article>
        </>
      )}

      {activeView === 'journal' && (
        <article className="accounting-panel accounting-table-panel">
          <h2>Sổ nhật ký chung</h2>
          <table className="accounting-table"><thead><tr><th>Số bút toán</th><th>Ngày</th><th>Nguồn</th><th>Diễn giải</th><th>Tổng Nợ</th><th>Trạng thái</th><th>Thao tác</th></tr></thead>
            <tbody>{(journalPage.content || []).map((entry) => <tr key={entry.id}>
              <td><strong>{entry.entryNumber}</strong></td><td>{entry.entryDate}</td><td>{SOURCE_LABELS[entry.sourceType] || entry.sourceType}</td>
              <td>{entry.description}</td><td>{money(entry.totalDebit)}</td><td><span className={`accounting-status ${entry.status === 'POSTED' ? 'posted' : 'reversed'}`}>{entry.status === 'POSTED' ? 'Đã ghi sổ' : 'Đã đảo'}</span></td>
              <td>{entry.status === 'POSTED' && <button className="accounting-link-danger" type="button" onClick={() => reverseEntry(entry.id)}>Đảo bút toán</button>}</td>
            </tr>)}</tbody>
          </table>
          <Pager page={journalPage} onChange={loadJournals} />
        </article>
      )}

      {activeView === 'ledger' && (
        <article className="accounting-panel accounting-table-panel">
          <div className="accounting-panel-heading"><h2>Sổ cái tài khoản</h2><select value={selectedAccount} onChange={(event) => setSelectedAccount(event.target.value)}>{accounts.map((account) => <option key={account.code} value={account.code}>{account.code} - {account.name}</option>)}</select></div>
          <PeriodFilter from={from} to={to} setFrom={setFrom} setTo={setTo} onApply={() => loadLedger(0)} />
          <table className="accounting-table"><thead><tr><th>Ngày</th><th>Số bút toán</th><th>Diễn giải</th><th>Phát sinh Nợ</th><th>Phát sinh Có</th></tr></thead>
            <tbody>{(ledgerPage.content || []).map((line) => <tr key={line.lineId}><td>{line.entryDate}</td><td>{line.entryNumber}</td><td>{line.description}</td><td>{money(line.debitAmount)}</td><td>{money(line.creditAmount)}</td></tr>)}</tbody>
          </table>
          <Pager page={ledgerPage} onChange={loadLedger} />
        </article>
      )}

      {activeView === 'reports' && income && trialBalance && (
        <>
          <PeriodFilter from={from} to={to} setFrom={setFrom} setTo={setTo} onApply={loadOverview} />
          <div className="accounting-grid-two">
            <article className="accounting-panel"><h2>Báo cáo kết quả kinh doanh</h2><dl className="accounting-summary-list">
              <div><dt>Doanh thu bán hàng</dt><dd>{money(income.grossSalesRevenue)}</dd></div><div><dt>Hoàn tiền và giảm trừ</dt><dd>{money(income.salesReturns)}</dd></div>
              <div><dt>Doanh thu thuần</dt><dd>{money(income.netRevenue)}</dd></div><div><dt>Giá vốn hàng bán</dt><dd>{money(income.costOfGoodsSold)}</dd></div>
              <div><dt>Chi phí vận hành</dt><dd>{money(income.operatingExpenses)}</dd></div><div className="total"><dt>Lợi nhuận thuần</dt><dd>{money(income.netProfit)}</dd></div>
            </dl></article>
            <article className="accounting-panel"><h2>Cân đối phát sinh</h2><div className={`trial-balance-state ${trialBalance.balanced ? 'balanced' : 'unbalanced'}`}>{trialBalance.balanced ? 'Sổ đang cân bằng' : 'Sổ đang lệch'}</div><dl className="accounting-summary-list"><div><dt>Tổng phát sinh Nợ</dt><dd>{money(trialBalance.totalDebit)}</dd></div><div><dt>Tổng phát sinh Có</dt><dd>{money(trialBalance.totalCredit)}</dd></div></dl></article>
          </div>
          <article className="accounting-panel accounting-table-panel"><h2>Bảng cân đối tài khoản</h2><table className="accounting-table"><thead><tr><th>Mã</th><th>Tài khoản</th><th>Loại</th><th>Phát sinh Nợ</th><th>Phát sinh Có</th><th>Số dư</th></tr></thead><tbody>{trialBalance.accounts.map((account) => <tr key={account.code}><td>{account.code}</td><td>{account.name}</td><td>{ACCOUNT_TYPE_LABELS[account.accountType]}</td><td>{money(account.totalDebit)}</td><td>{money(account.totalCredit)}</td><td><strong>{money(account.balance)}</strong></td></tr>)}</tbody></table></article>
        </>
      )}

      {activeView === 'manual' && (
        <form className="accounting-panel accounting-manual-form" onSubmit={submitManualEntry}>
          <h2>Ghi nhận bút toán nội bộ</h2>
          <div className="accounting-form-grid"><label>Ngày hạch toán<input type="date" value={manualForm.entryDate} onChange={(event) => setManualForm({ ...manualForm, entryDate: event.target.value })} /></label><label>Loại nghiệp vụ<select value={manualForm.sourceType} onChange={(event) => setManualForm({ ...manualForm, sourceType: event.target.value })}><option value="OPENING_BALANCE">Số dư đầu kỳ</option><option value="OWNER_CAPITAL">Góp vốn</option><option value="OPERATING_EXPENSE">Chi phí vận hành</option><option value="FUND_TRANSFER">Điều chuyển quỹ</option><option value="MANUAL_ADJUSTMENT">Điều chỉnh khác</option></select></label><label>Mã tham chiếu<input required value={manualForm.sourceReference} onChange={(event) => setManualForm({ ...manualForm, sourceReference: event.target.value })} placeholder="VD: OPENING-2026" /></label><label className="wide">Diễn giải<input required value={manualForm.description} onChange={(event) => setManualForm({ ...manualForm, description: event.target.value })} /></label></div>
          <div className="accounting-lines"><div className="accounting-line header"><span>Tài khoản</span><span>Diễn giải</span><span>Nợ</span><span>Có</span><span /></div>{manualForm.lines.map((line, index) => <div className="accounting-line" key={`${index}-${line.accountCode}`}><select value={line.accountCode} onChange={(event) => updateManualLine(index, 'accountCode', event.target.value)}>{accounts.map((account) => <option key={account.code} value={account.code}>{account.code} - {account.name}</option>)}</select><input value={line.description} onChange={(event) => updateManualLine(index, 'description', event.target.value)} /><input type="number" min="0" value={line.debitAmount} onChange={(event) => updateManualLine(index, 'debitAmount', event.target.value)} /><input type="number" min="0" value={line.creditAmount} onChange={(event) => updateManualLine(index, 'creditAmount', event.target.value)} /><button type="button" onClick={() => setManualForm((current) => ({ ...current, lines: current.lines.filter((_, i) => i !== index) }))}>×</button></div>)}</div>
          <div className="accounting-manual-footer"><button type="button" className="secondary" onClick={() => setManualForm((current) => ({ ...current, lines: [...current.lines, { accountCode: '111', description: '', debitAmount: '', creditAmount: '' }] }))}>Thêm dòng</button><div>Tổng Nợ: <strong>{money(debitTotal)}</strong> · Tổng Có: <strong>{money(creditTotal)}</strong></div><button type="submit" disabled={Math.abs(debitTotal - creditTotal) >= 0.01 || debitTotal <= 0}>Ghi sổ</button></div>
        </form>
      )}
    </section>
  );
}

function PeriodFilter({ from, to, setFrom, setTo, onApply }) {
  return <div className="accounting-period-filter"><label>Từ ngày<input type="date" value={from} onChange={(event) => setFrom(event.target.value)} /></label><label>Đến ngày<input type="date" value={to} onChange={(event) => setTo(event.target.value)} /></label><button type="button" onClick={onApply}>Áp dụng</button></div>;
}

function Pager({ page, onChange }) {
  if (!page || page.totalPages <= 1) return null;
  return <div className="accounting-pager"><button type="button" disabled={page.number <= 0} onClick={() => onChange(page.number - 1)}>Trang trước</button><span>Trang {page.number + 1}/{page.totalPages}</span><button type="button" disabled={page.number >= page.totalPages - 1} onClick={() => onChange(page.number + 1)}>Trang sau</button></div>;
}

export default AdminAccountingPage;
