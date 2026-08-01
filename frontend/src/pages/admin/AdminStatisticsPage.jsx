import { useMemo, useState } from 'react';
import {
  getPaymentFailureReasons,
  getCustomerSummary,
  getCustomerTrend,
  getInventoryMovements,
  getProfitMarginReport,
  getRefundReasons,
  getRefundByProduct,
  getRevenueByCategory,
  getRevenueByPaymentMethod,
  getRevenueByProduct,
  getRevenueByPromotion,
  getShipmentsByProvider,
  getShipmentsByRegion,
  getShipmentFailureReasons,
  getStatisticsOverview,
  getTopSellingProducts,
  getTopSpendingCustomers,
} from '../../services/statisticsService.js';
import { formatDateTime, formatPrice } from '../../utils/formatters.js';

const quickRanges = [
  { code: 'TODAY', label: 'Hom nay', days: 0 },
  { code: 'YESTERDAY', label: 'Hom qua', days: 1, yesterday: true },
  { code: 'LAST_7_DAYS', label: '7 ngay gan nhat', days: 6 },
  { code: 'LAST_30_DAYS', label: '30 ngay gan nhat', days: 29 },
  { code: 'THIS_MONTH', label: 'Thang nay', thisMonth: true },
  { code: 'LAST_MONTH', label: 'Thang truoc', lastMonth: true },
];

const emptyDetails = {
  revenueByCategory: [],
  revenueByProduct: [],
  revenueByPaymentMethod: [],
  revenueByPromotion: [],
  topSpendingCustomers: [],
  paymentFailureReasons: [],
  refundReasons: [],
  shipmentsByProvider: [],
  shipmentFailureReasons: [],
  customerSummary: [],
  customerTrend: [],
  refundByProduct: [],
  shipmentsByRegion: [],
  inventoryMovements: [],
  profitMargin: [],
};

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
  const [details, setDetails] = useState(emptyDetails);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const groupedKpis = useMemo(() => {
    const kpis = overview?.kpis || [];
    return {
      revenue: kpis.filter((kpi) => hasCode(kpi, ['REVENUE', 'REFUND', 'AVERAGE_ORDER_VALUE'])),
      operations: kpis.filter((kpi) => hasCode(kpi, ['ORDER', 'SOLD', 'CUSTOMER'])),
      rates: kpis.filter((kpi) => hasCode(kpi, ['RATE'])),
      promotion: kpis.filter((kpi) => hasCode(kpi, ['PROMOTION'])),
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
      const topParams = { ...params, limit: filters.topLimit };
      const [
        overviewResult,
        topResult,
        revenueByCategory,
        revenueByProduct,
        revenueByPaymentMethod,
        revenueByPromotion,
        topSpendingCustomers,
        customerSummary,
        customerTrend,
        paymentFailureReasons,
        refundReasons,
        refundByProduct,
        shipmentsByProvider,
        shipmentsByRegion,
        shipmentFailureReasons,
        inventoryMovements,
        profitMargin,
      ] = await Promise.all([
        getStatisticsOverview(params),
        getTopSellingProducts(topParams),
        getRevenueByCategory(params),
        getRevenueByProduct(topParams),
        getRevenueByPaymentMethod(params),
        getRevenueByPromotion(params),
        getTopSpendingCustomers(params),
        getCustomerSummary(params),
        getCustomerTrend(params),
        getPaymentFailureReasons(params),
        getRefundReasons(params),
        getRefundByProduct(params),
        getShipmentsByProvider(params),
        getShipmentsByRegion(params),
        getShipmentFailureReasons(params),
        getInventoryMovements(params),
        getProfitMarginReport(params),
      ]);

      setOverview(overviewResult);
      setTopSelling(asArray(topResult));
      setDetails({
        revenueByCategory: asArray(revenueByCategory),
        revenueByProduct: asArray(revenueByProduct),
        revenueByPaymentMethod: asArray(revenueByPaymentMethod),
        revenueByPromotion: asArray(revenueByPromotion),
        topSpendingCustomers: asArray(topSpendingCustomers),
        customerSummary: asArray(customerSummary),
        customerTrend: asArray(customerTrend),
        paymentFailureReasons: asArray(paymentFailureReasons),
        refundReasons: asArray(refundReasons),
        refundByProduct: asArray(refundByProduct),
        shipmentsByProvider: asArray(shipmentsByProvider),
        shipmentsByRegion: asArray(shipmentsByRegion),
        shipmentFailureReasons: asArray(shipmentFailureReasons),
        inventoryMovements: asArray(inventoryMovements),
        profitMargin: asArray(profitMargin),
      });
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
          <span>Dashboard tong hop KPI, doanh thu, khach hang, payment, refund, shipment va ton kho.</span>
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
        <button type="submit" disabled={loading}>{loading ? 'Loading...' : 'Load all statistics'}</button>
      </form>

      {error && <div className="form-alert">{error}</div>}

      {overview && (
        <>
          <div className="admin-detail-summary">
            <p><strong>Period:</strong> {overview.period?.from} → {overview.period?.to}</p>
            <p><strong>Generated at:</strong> {formatDateTime(overview.generatedAt)}</p>
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

      <section>
        <h3 className="admin-section-title">Revenue breakdown</h3>
        <div className="admin-statistics-panel-grid">
          <BreakdownPanel title="Revenue by category" subtitle="Danh muc tao doanh thu cao nhat" rows={details.revenueByCategory} />
          <PaymentMethodPanel rows={details.revenueByPaymentMethod} title="Revenue by payment method" />
          <BreakdownPanel title="Revenue by promotion" subtitle="Doanh thu theo chuong trinh khuyen mai" rows={details.revenueByPromotion} />
        </div>
      </section>

      <section>
        <h3 className="admin-section-title">Product and customer ranking</h3>
        <div className="admin-statistics-panel-grid">
          <TopProductPanel title="Top selling products" rows={topSelling} />
          <TopProductPanel title="Revenue by product" rows={details.revenueByProduct} />
          <ProfitMarginPanel rows={details.profitMargin} />
          <BreakdownPanel title="Top spending customers" subtitle="Khach hang chi tieu cao nhat" rows={details.topSpendingCustomers} />
          <BreakdownPanel title="Customer summary" subtitle="Khach moi, khach co don, khach mua lap lai" rows={details.customerSummary} amountLabel="Value" />
          <BreakdownPanel title="Customer trend" subtitle="Khach hang moi theo ngay" rows={details.customerTrend} amountLabel="Value" />
        </div>
      </section>

      <section>
        <h3 className="admin-section-title">Risk and operations</h3>
        <div className="admin-statistics-panel-grid">
          <BreakdownPanel title="Payment failure reasons" subtitle="Ly do thanh toan that bai" rows={details.paymentFailureReasons} amountLabel="Amount" />
          <BreakdownPanel title="Refund by reason" subtitle="Ly do hoan tien/tra tien" rows={details.refundReasons} amountLabel="Refund" />
          <BreakdownPanel title="Refund by product" subtitle="Tien refund duoc phan bo theo san pham" rows={details.refundByProduct} amountLabel="Refund" />
          <BreakdownPanel title="Shipment by provider" subtitle="Don van chuyen theo nha giao hang" rows={details.shipmentsByProvider} amountLabel="Amount" />
          <BreakdownPanel title="Shipment by region" subtitle="Don van chuyen theo khu vuc giao hang" rows={details.shipmentsByRegion} amountLabel="Shipping fee" />
          <BreakdownPanel title="Shipment failure reasons" subtitle="Ly do giao hang that bai" rows={details.shipmentFailureReasons} amountLabel="Amount" />
          <BreakdownPanel title="Inventory movements" subtitle="Nhap kho va xuat kho trong ky" rows={details.inventoryMovements} amountLabel="Value" />
        </div>
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
      <EmptyAware rows={rows}>
        {(rows || []).map((row) => <div className="summary-line" key={row.code}><span>{row.label || row.code}</span><strong>{row.count}</strong></div>)}
      </EmptyAware>
    </article>
  );
}

function PaymentMethodPanel({ rows, title = 'Methods' }) {
  return (
    <article className="admin-api-console">
      <div className="admin-panel__heading"><div><p>Payment</p><h2>{title}</h2></div></div>
      <EmptyAware rows={rows}>
        {(rows || []).map((row) => (
          <MetricRow
            key={row.method || row.code}
            label={`${row.method || row.label || row.code} · ${row.sharePercent || 0}%`}
            value={formatPrice(row.amount)}
            percent={row.sharePercent}
            meta={`${row.transactionCount || row.count || 0} transactions`}
          />
        ))}
      </EmptyAware>
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

function BreakdownPanel({ title, subtitle, rows, amountLabel = 'Revenue' }) {
  return (
    <article className="admin-api-console">
      <div className="admin-panel__heading"><div><p>{subtitle}</p><h2>{title}</h2></div></div>
      <EmptyAware rows={rows}>
        {(rows || []).map((row) => (
          <MetricRow
            key={row.code || row.label}
            label={row.label || row.code || 'Unknown'}
            value={formatPrice(row.amount)}
            percent={row.sharePercent}
            meta={`${amountLabel}: ${formatPrice(row.amount)} · Count: ${row.count || 0}`}
          />
        ))}
      </EmptyAware>
    </article>
  );
}

function TopProductPanel({ title, rows }) {
  return (
    <article className="admin-resource-table admin-resource-table--compact">
      <div className="admin-panel__heading"><div><p>Ranking</p><h2>{title}</h2></div></div>
      <div className="admin-resource-table__head" style={{ gridTemplateColumns: '60px 1fr 100px 120px 90px' }}>
        <span>Rank</span><span>Product</span><span>Sold</span><span>Revenue</span><span>Orders</span>
      </div>
      <EmptyAware rows={rows}>
        {(rows || []).map((product, index) => (
          <div className="admin-resource-table__row" style={{ gridTemplateColumns: '60px 1fr 100px 120px 90px' }} key={product.productId || index}>
            <span>#{index + 1}</span>
            <span>{product.productName || product.label || '-'}</span>
            <span>{product.soldQuantity || 0}</span>
            <span>{formatPrice(product.grossRevenue || product.amount || 0)}</span>
            <span>{product.orderCount || product.count || 0}</span>
          </div>
        ))}
      </EmptyAware>
    </article>
  );
}

function ProfitMarginPanel({ rows }) {
  return (
    <article className="admin-resource-table admin-resource-table--compact">
      <div className="admin-panel__heading"><div><p>Finance</p><h2>Profit / margin report</h2></div></div>
      <div className="admin-resource-table__head" style={{ gridTemplateColumns: '1fr 100px 120px 120px 100px' }}>
        <span>Product</span><span>Sold</span><span>Revenue</span><span>Profit</span><span>Margin</span>
      </div>
      <EmptyAware rows={rows}>
        {(rows || []).map((product, index) => (
          <div className="admin-resource-table__row" style={{ gridTemplateColumns: '1fr 100px 120px 120px 100px' }} key={product.productId || index}>
            <span>{product.productName || '-'}</span>
            <span>{product.soldQuantity || 0}</span>
            <span>{formatPrice(product.revenue || 0)}</span>
            <span>{formatPrice(product.grossProfit || 0)}</span>
            <span>{product.marginPercent || 0}%</span>
          </div>
        ))}
      </EmptyAware>
    </article>
  );
}

function MetricRow({ label, value, percent = 0, meta }) {
  const width = Math.max(0, Math.min(100, Number(percent) || 0));
  return (
    <div className="admin-metric-row">
      <div className="summary-line">
        <span>{label}</span>
        <strong>{value}</strong>
      </div>
      <div className="admin-metric-row__bar"><i style={{ width: `${width}%` }} /></div>
      {meta && <small>{meta}</small>}
    </div>
  );
}

function EmptyAware({ rows, children }) {
  if (!rows?.length) {
    return <div className="admin-empty-mini">Chua co du lieu trong khoang thoi gian nay.</div>;
  }

  return children;
}

function asArray(value) {
  return Array.isArray(value) ? value : [];
}

function hasCode(kpi, tokens) {
  const code = String(kpi.code || '');
  return tokens.some((token) => code.includes(token));
}

export default AdminStatisticsPage;
