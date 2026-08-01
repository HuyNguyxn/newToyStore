import { useMemo, useState } from 'react';
import { getStatisticsOverview, getTopSellingProducts } from '../../services/statisticsService.js';
import { formatDateTime, formatPrice } from '../../utils/formatters.js';

const quickRanges = [
  { code: 'TODAY', label: 'Hom nay', days: 0 },
  { code: 'YESTERDAY', label: 'Hom qua', days: 1, yesterday: true },
  { code: 'LAST_7_DAYS', label: '7 ngay gan nhat', days: 6 },
  { code: 'LAST_30_DAYS', label: '30 ngay gan nhat', days: 29 },
  { code: 'THIS_MONTH', label: 'Thang nay', thisMonth: true },
  { code: 'LAST_MONTH', label: 'Thang truoc', lastMonth: true },
];

function toDateInput(date) {
  return date.toISOString().slice(0, 10);
}

function applyQuickRange(option) {
  const now = new Date();
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());

  if (option.yesterday) {
    const yesterday = new Date(today);
    yesterday.setDate(yesterday.getDate() - 1);
    return { from: toDateInput(yesterday), to: toDateInput(yesterday) };
  }

  if (option.thisMonth) {
    return { from: toDateInput(new Date(today.getFullYear(), today.getMonth(), 1)), to: toDateInput(today) };
  }

  if (option.lastMonth) {
    const from = new Date(today.getFullYear(), today.getMonth() - 1, 1);
    const to = new Date(today.getFullYear(), today.getMonth(), 0);
    return { from: toDateInput(from), to: toDateInput(to) };
  }

  const from = new Date(today);
  from.setDate(from.getDate() - option.days);
  return { from: toDateInput(from), to: toDateInput(today) };
}

function AdminStatisticsPage() {
  const [filters, setFilters] = useState({
    from: '',
    to: '',
    timezone: 'Asia/Ho_Chi_Minh',
    groupBy: 'AUTO',
    dateField: 'CREATED_AT',
    compareWithPreviousPeriod: false,
    topLimit: '10',
    lowStockThreshold: '5',
  });
  const [overview, setOverview] = useState(null);
  const [topSelling, setTopSelling] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const groupedKpis = useMemo(() => {
    const kpis = overview?.kpis || [];
    return {
      revenue: kpis.filter((kpi) => String(kpi.code).includes('REVENUE') || String(kpi.code).includes('REFUND') || String(kpi.code).includes('AVERAGE_ORDER_VALUE')),
      operations: kpis.filter((kpi) => String(kpi.code).includes('ORDER') || String(kpi.code).includes('SOLD') || String(kpi.code).includes('CUSTOMER')),
      rates: kpis.filter((kpi) => String(kpi.code).includes('RATE')),
      promotion: kpis.filter((kpi) => String(kpi.code).includes('PROMOTION')),
    };
  }, [overview]);

  function updateField(field, value) {
    setFilters((current) => ({ ...current, [field]: value }));
  }

  function chooseQuickRange(option) {
    setFilters((current) => ({ ...current, ...applyQuickRange(option) }));
  }

  function buildParams() {
    return Object.fromEntries(Object.entries(filters).filter(([, value]) => value !== '' && value !== false));
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setLoading(true);
    setError('');

    try {
      const params = buildParams();
      const [overviewResult, topResult] = await Promise.all([
        getStatisticsOverview(params),
        getTopSellingProducts({ ...params, limit: filters.topLimit }),
      ]);
      setOverview(overviewResult);
      setTopSelling(Array.isArray(topResult) ? topResult : []);
    } catch (err) {
      setError(err.message || 'Khong the tai thong ke.');
    } finally {
      setLoading(false);
    }
  }

  return (
    <section className="admin-resource">
      <div className="admin-resource__hero">
        <div>
          <p>Admin Analytics</p>
          <h2>Statistics</h2>
          <span>Overview dashboard with shared time range, groupBy, comparison, KPI, trend, status and inventory reports.</span>
        </div>
        <strong>{overview?.generatedAt ? `Updated ${formatDateTime(overview.generatedAt)}` : `${topSelling.length} top products`}</strong>
      </div>

      <form className="admin-api-console" onSubmit={handleSubmit}>
        <div className="admin-panel__heading"><div><p>Shared filter</p><h2>Time range</h2></div></div>
        <div className="admin-quick-ranges">
          {quickRanges.map((option) => <button type="button" key={option.code} onClick={() => chooseQuickRange(option)}>{option.label}</button>)}
        </div>
        <div className="admin-api-console__row">
          <label>From<input type="date" value={filters.from} onChange={(event) => updateField('from', event.target.value)} /></label>
          <label>To<input type="date" value={filters.to} onChange={(event) => updateField('to', event.target.value)} /></label>
        </div>
        <div className="admin-api-console__row">
          <label>Timezone<input value={filters.timezone} onChange={(event) => updateField('timezone', event.target.value)} /></label>
          <label>Group by
            <select value={filters.groupBy} onChange={(event) => updateField('groupBy', event.target.value)}>
              <option value="AUTO">AUTO</option>
              <option value="DAY">DAY</option>
              <option value="WEEK">WEEK</option>
              <option value="MONTH">MONTH</option>
              <option value="QUARTER">QUARTER</option>
              <option value="YEAR">YEAR</option>
            </select>
          </label>
        </div>
        <label>Order date field
          <select value={filters.dateField} onChange={(event) => updateField('dateField', event.target.value)}>
            <option value="CREATED_AT">CREATED_AT</option>
            <option value="COMPLETED_AT">COMPLETED_AT</option>
            <option value="CANCELLED_AT">CANCELLED_AT</option>
          </select>
        </label>
        <div className="admin-api-console__row">
          <label>Top limit<input type="number" min="1" max="20" value={filters.topLimit} onChange={(event) => updateField('topLimit', event.target.value)} /></label>
          <label>Low stock threshold<input type="number" min="0" value={filters.lowStockThreshold} onChange={(event) => updateField('lowStockThreshold', event.target.value)} /></label>
        </div>
        <label className="inline-check">
          <input type="checkbox" checked={filters.compareWithPreviousPeriod} onChange={(event) => updateField('compareWithPreviousPeriod', event.target.checked)} />
          Compare with previous period
        </label>
        <button type="submit" disabled={loading}>{loading ? 'Loading...' : 'Load statistics'}</button>
      </form>

      {error && <div className="form-alert">{error}</div>}

      {overview && (
        <>
          <div className="admin-detail-summary">
            <p><strong>Period:</strong> {overview.period?.from} → {overview.period?.to}</p>
            <p><strong>Timezone:</strong> {overview.period?.timezone}</p>
            <p><strong>Group by:</strong> requested {overview.period?.requestedGroupBy}, applied {overview.period?.appliedGroupBy}</p>
            <p><strong>Adjusted:</strong> {overview.period?.groupByAdjusted ? 'Yes' : 'No'} · <strong>Compare:</strong> {overview.period?.compareWithPreviousPeriod ? 'Yes' : 'No'}</p>
          </div>

          <KpiSection title="Revenue KPIs" items={groupedKpis.revenue} />
          <KpiSection title="Operation KPIs" items={groupedKpis.operations} />
          <KpiSection title="Rate KPIs" items={groupedKpis.rates} />
          <KpiSection title="Promotion KPIs" items={groupedKpis.promotion} />

          <section className="admin-api-console">
            <div className="admin-panel__heading"><div><p>Revenue</p><h2>Trend</h2></div></div>
            <div className="admin-chart-bars">
              {(overview.revenueTrend || []).map((point) => <TrendBar key={point.period} point={point} />)}
            </div>
          </section>

          <div className="admin-dashboard-grid">
            <StatusPanel title="Order status" rows={overview.orderStatus} />
            <StatusPanel title="Payment status" rows={overview.paymentStatus} />
            <StatusPanel title="Refund status" rows={overview.refundStatus} />
            <StatusPanel title="Shipment status" rows={overview.shipmentStatus} />
            <StatusPanel title="User status" rows={overview.userStatus} />
            <StatusPanel title="Product status" rows={overview.productStatus} />
          </div>

          <div className="admin-crud-grid">
            <PaymentMethodPanel rows={overview.paymentMethods} />
            <InventoryPanel inventory={overview.inventory} />
          </div>
        </>
      )}

      <section className="admin-resource-table">
        <div className="admin-resource-table__head" style={{ gridTemplateColumns: '90px 1fr 150px 150px 160px 90px' }}>
          <span>productId</span><span>productName</span><span>soldQuantity</span><span>grossRevenue</span><span>orderCount</span><span>rank</span>
        </div>
        {topSelling.map((product, index) => (
          <div className="admin-resource-table__row" style={{ gridTemplateColumns: '90px 1fr 150px 150px 160px 90px' }} key={product.productId || index}>
            <span>{product.productId || '-'}</span>
            <span>{product.productName || '-'}</span>
            <span>{product.soldQuantity || 0}</span>
            <span>{formatPrice(product.grossRevenue || 0)}</span>
            <span>{product.orderCount || 0}</span>
            <span>{index + 1}</span>
          </div>
        ))}
      </section>
    </section>
  );
}

function KpiSection({ title, items }) {
  if (!items?.length) return null;
  return (
    <section>
      <h3 className="admin-section-title">{title}</h3>
      <div className="admin-dashboard-grid">
        {items.map((kpi) => <KpiCard key={kpi.code || kpi.label} kpi={kpi} />)}
      </div>
    </section>
  );
}

function KpiCard({ kpi }) {
  const isMoney = ['REVENUE', 'REFUND', 'AVERAGE_ORDER_VALUE', 'AMOUNT'].some((token) => String(kpi.code).includes(token));
  return (
    <article className="admin-stat-card">
      <span>{kpi.label || kpi.code}</span>
      <strong>{isMoney ? formatPrice(kpi.value) : kpi.value}</strong>
      {kpi.changePercent !== null && kpi.changePercent !== undefined && <small>{kpi.changePercent}% vs previous</small>}
    </article>
  );
}

function TrendBar({ point }) {
  const max = Math.max(point.grossRevenue || 0, point.netRevenue || 0, point.refundAmount || 0, 1);
  return (
    <div className="admin-chart-bars__item">
      <span>{point.period}</span>
      <div><i style={{ width: `${Math.min(100, ((point.netRevenue || 0) / max) * 100)}%` }} /></div>
      <small>{formatPrice(point.netRevenue)} · {point.orderCount} orders</small>
    </div>
  );
}

function StatusPanel({ title, rows }) {
  return (
    <article className="admin-api-console">
      <div className="admin-panel__heading"><div><p>Status</p><h2>{title}</h2></div></div>
      {(rows || []).map((row) => <div className="summary-line" key={row.code}><span>{row.label || row.code}</span><strong>{row.count}</strong></div>)}
    </article>
  );
}

function PaymentMethodPanel({ rows }) {
  return (
    <article className="admin-api-console">
      <div className="admin-panel__heading"><div><p>Payment</p><h2>Methods</h2></div></div>
      {(rows || []).map((row) => <div className="summary-line" key={row.method}><span>{row.method} · {row.sharePercent}%</span><strong>{formatPrice(row.amount)}</strong></div>)}
    </article>
  );
}

function InventoryPanel({ inventory }) {
  return (
    <article className="admin-api-console">
      <div className="admin-panel__heading"><div><p>Inventory</p><h2>Stock overview</h2></div></div>
      <div className="summary-line"><span>Stock quantity</span><strong>{inventory?.stockQuantity || 0}</strong></div>
      <div className="summary-line"><span>Reserved quantity</span><strong>{inventory?.reservedQuantity || 0}</strong></div>
      <div className="summary-line"><span>Available quantity</span><strong>{inventory?.availableQuantity || 0}</strong></div>
      <div className="summary-line"><span>Low stock variants</span><strong>{inventory?.lowStockVariantCount || 0}</strong></div>
    </article>
  );
}

export default AdminStatisticsPage;
