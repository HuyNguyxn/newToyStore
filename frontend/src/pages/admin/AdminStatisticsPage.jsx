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
  { code: 'TODAY', label: 'Hôm nay', days: 0 },
  { code: 'YESTERDAY', label: 'Hôm qua', days: 1, yesterday: true },
  { code: 'LAST_7_DAYS', label: '7 ngày gần nhất', days: 6 },
  { code: 'LAST_30_DAYS', label: '30 ngày gần nhất', days: 29 },
  { code: 'THIS_MONTH', label: 'Tháng này', thisMonth: true },
  { code: 'LAST_MONTH', label: 'Tháng trước', lastMonth: true },
];

const defaultFilters = {
  from: '',
  to: '',
  timezone: 'Asia/Ho_Chi_Minh',
  groupBy: 'AUTO',
  dateField: 'CREATED_AT',
  compareWithPreviousPeriod: false,
  topLimit: '10',
  lowStockThreshold: '5',
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

function buildParams(filters) {
  return Object.fromEntries(Object.entries(filters).filter(([, value]) => value !== '' && value !== false));
}

function AdminStatisticsPage() {
  return (
    <section className="admin-resource">
      <div className="admin-resource__hero">
        <div>
          <p>Admin Analytics</p>
          <h2>Statistics</h2>
          <span>Mỗi bảng thống kê có bộ lọc riêng, cùng cách thao tác nhưng không ảnh hưởng lẫn nhau.</span>
        </div>
        <strong>Independent panels</strong>
      </div>

      <OverviewStatisticsPanel />

      <section>
        <h3 className="admin-section-title">Revenue breakdown</h3>
        <div className="admin-statistics-panel-grid">
          <BreakdownStatisticPanel
            title="Revenue by category"
            subtitle="Danh mục tạo doanh thu cao nhất"
            loader={(params) => getRevenueByCategory(params)}
          />
          <PaymentMethodStatisticPanel
            title="Revenue by payment method"
            subtitle="Doanh thu theo phương thức thanh toán"
            loader={(params) => getRevenueByPaymentMethod(params)}
          />
          <BreakdownStatisticPanel
            title="Revenue by promotion"
            subtitle="Doanh thu theo chương trình khuyến mãi"
            loader={(params) => getRevenueByPromotion(params)}
          />
        </div>
      </section>

      <section>
        <h3 className="admin-section-title">Product and customer ranking</h3>
        <div className="admin-statistics-panel-grid">
          <TopProductStatisticPanel
            title="Top selling products"
            subtitle="Sản phẩm bán chạy theo số lượng"
            loader={(params, filters) => getTopSellingProducts({ ...params, limit: filters.topLimit })}
          />
          <TopProductStatisticPanel
            title="Revenue by product"
            subtitle="Sản phẩm tạo doanh thu cao"
            loader={(params, filters) => getRevenueByProduct({ ...params, limit: filters.topLimit })}
          />
          <ProfitMarginStatisticPanel />
          <BreakdownStatisticPanel
            title="Top spending customers"
            subtitle="Khách hàng chi tiêu cao nhất"
            loader={(params) => getTopSpendingCustomers(params)}
          />
          <BreakdownStatisticPanel
            title="Customer summary"
            subtitle="Khách mới, khách có đơn, khách mua lặp lại"
            loader={(params) => getCustomerSummary(params)}
            amountLabel="Value"
          />
          <BreakdownStatisticPanel
            title="Customer trend"
            subtitle="Khách hàng mới theo thời gian"
            loader={(params) => getCustomerTrend(params)}
            amountLabel="Value"
          />
        </div>
      </section>

      <section>
        <h3 className="admin-section-title">Risk and operations</h3>
        <div className="admin-statistics-panel-grid">
          <BreakdownStatisticPanel
            title="Payment failure reasons"
            subtitle="Lý do thanh toán thất bại"
            loader={(params) => getPaymentFailureReasons(params)}
            amountLabel="Amount"
          />
          <BreakdownStatisticPanel
            title="Refund by reason"
            subtitle="Lý do hoàn tiền"
            loader={(params) => getRefundReasons(params)}
            amountLabel="Refund"
          />
          <BreakdownStatisticPanel
            title="Refund by product"
            subtitle="Tiền refund được phân bổ theo sản phẩm"
            loader={(params) => getRefundByProduct(params)}
            amountLabel="Refund"
          />
          <BreakdownStatisticPanel
            title="Shipment by provider"
            subtitle="Đơn giao hàng theo nhà vận chuyển"
            loader={(params) => getShipmentsByProvider(params)}
            amountLabel="Shipping fee"
          />
          <BreakdownStatisticPanel
            title="Shipment by region"
            subtitle="Đơn giao hàng theo khu vực"
            loader={(params) => getShipmentsByRegion(params)}
            amountLabel="Shipping fee"
          />
          <BreakdownStatisticPanel
            title="Shipment failure reasons"
            subtitle="Lý do giao hàng thất bại"
            loader={(params) => getShipmentFailureReasons(params)}
            amountLabel="Amount"
          />
          <InventoryMovementStatisticPanel />
        </div>
      </section>
    </section>
  );
}

function OverviewStatisticsPanel() {
  const { data: overview, filters, updateField, chooseQuickRange, load, loading, error } = useStatisticLoader(
    (params) => getStatisticsOverview(params),
    null
  );

  const groupedKpis = useMemo(() => {
    const kpis = overview?.kpis || [];
    return {
      revenue: kpis.filter((kpi) => hasCode(kpi, ['REVENUE', 'REFUND', 'AVERAGE_ORDER_VALUE'])),
      operations: kpis.filter((kpi) => hasCode(kpi, ['ORDER', 'SOLD', 'CUSTOMER'])),
      rates: kpis.filter((kpi) => hasCode(kpi, ['RATE'])),
      promotion: kpis.filter((kpi) => hasCode(kpi, ['PROMOTION'])),
    };
  }, [overview]);

  return (
    <>
      <StatisticFilter
        title="Overview filter"
        subtitle="Filter riêng cho dashboard tổng quan"
        filters={filters}
        loading={loading}
        onSubmit={load}
        onField={updateField}
        onQuick={chooseQuickRange}
        buttonText="Load overview"
      />
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
            <PaymentMethodPanel rows={overview.paymentMethods} title="Payment methods" />
            <InventoryPanel inventory={overview.inventory} />
          </div>
        </>
      )}
    </>
  );
}

function BreakdownStatisticPanel({ title, subtitle, loader, amountLabel = 'Revenue' }) {
  const { data, filters, updateField, chooseQuickRange, load, loading, error } = useStatisticLoader(loader, []);

  return (
    <article className="admin-api-console">
      <StatisticPanelHeading title={title} subtitle={subtitle} />
      <StatisticFilter
        compact
        title={`${title} filter`}
        filters={filters}
        loading={loading}
        onSubmit={load}
        onField={updateField}
        onQuick={chooseQuickRange}
        buttonText="Load"
      />
      {error && <div className="form-alert">{error}</div>}
      <EmptyAware rows={data}>
        {data.map((row) => (
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

function PaymentMethodStatisticPanel({ title, subtitle, loader }) {
  const { data, filters, updateField, chooseQuickRange, load, loading, error } = useStatisticLoader(loader, []);

  return (
    <article className="admin-api-console">
      <StatisticPanelHeading title={title} subtitle={subtitle} />
      <StatisticFilter
        compact
        title={`${title} filter`}
        filters={filters}
        loading={loading}
        onSubmit={load}
        onField={updateField}
        onQuick={chooseQuickRange}
        buttonText="Load"
      />
      {error && <div className="form-alert">{error}</div>}
      <PaymentMethodPanel rows={data} title="Methods" embedded />
    </article>
  );
}

function TopProductStatisticPanel({ title, subtitle, loader }) {
  const { data, filters, updateField, chooseQuickRange, load, loading, error } = useStatisticLoader(loader, []);

  return (
    <article className="admin-resource-table admin-resource-table--compact">
      <StatisticPanelHeading title={title} subtitle={subtitle} />
      <StatisticFilter
        compact
        title={`${title} filter`}
        filters={filters}
        loading={loading}
        onSubmit={load}
        onField={updateField}
        onQuick={chooseQuickRange}
        buttonText="Load"
      />
      {error && <div className="form-alert">{error}</div>}
      <div className="admin-resource-table__head" style={{ gridTemplateColumns: '60px 1fr 100px 120px 90px' }}>
        <span>Rank</span><span>Product</span><span>Sold</span><span>Revenue</span><span>Orders</span>
      </div>
      <EmptyAware rows={data}>
        {data.map((product, index) => (
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

function ProfitMarginStatisticPanel() {
  const { data, filters, updateField, chooseQuickRange, load, loading, error } = useStatisticLoader(
    (params) => getProfitMarginReport(params),
    []
  );

  return (
    <article className="admin-resource-table admin-resource-table--compact">
      <StatisticPanelHeading title="Profit / margin report" subtitle="Lãi gộp, refund, doanh thu thuần và biên lợi nhuận" />
      <StatisticFilter
        compact
        title="Profit margin filter"
        filters={filters}
        loading={loading}
        onSubmit={load}
        onField={updateField}
        onQuick={chooseQuickRange}
        buttonText="Load"
      />
      {error && <div className="form-alert">{error}</div>}
      <div className="admin-resource-table__head" style={{ gridTemplateColumns: '1.2fr 70px 110px 100px 110px 110px 90px' }}>
        <span>Product</span><span>Sold</span><span>Gross</span><span>Refund</span><span>Net</span><span>Net profit</span><span>Margin</span>
      </div>
      <EmptyAware rows={data}>
        {data.map((product, index) => (
          <div className="admin-resource-table__row" style={{ gridTemplateColumns: '1.2fr 70px 110px 100px 110px 110px 90px' }} key={product.productId || index}>
            <span>{product.productName || '-'}</span>
            <span>{product.soldQuantity || 0}</span>
            <span>{formatPrice(product.revenue || 0)}</span>
            <span>{formatPrice(product.refundAmount || 0)}</span>
            <span>{formatPrice(product.netRevenue || 0)}</span>
            <span>{formatPrice(product.netProfit || 0)}</span>
            <span>{product.marginPercent || 0}%</span>
          </div>
        ))}
      </EmptyAware>
    </article>
  );
}

function InventoryMovementStatisticPanel() {
  const { data, filters, updateField, chooseQuickRange, load, loading, error } = useStatisticLoader(
    (params) => getInventoryMovements(params),
    []
  );

  return (
    <article className="admin-resource-table admin-resource-table--compact">
      <StatisticPanelHeading title="Inventory movements" subtitle="Nhập, xuất, chênh lệch và snapshot tồn kho hiện tại" />
      <StatisticFilter
        compact
        title="Inventory movement filter"
        filters={filters}
        loading={loading}
        onSubmit={load}
        onField={updateField}
        onQuick={chooseQuickRange}
        buttonText="Load"
      />
      {error && <div className="form-alert">{error}</div>}
      <div className="admin-resource-table__head" style={{ gridTemplateColumns: '110px 1fr 100px 120px' }}>
        <span>Type</span><span>Movement</span><span>Quantity</span><span>Value</span>
      </div>
      <EmptyAware rows={data}>
        {data.map((row) => (
          <div className="admin-resource-table__row" style={{ gridTemplateColumns: '110px 1fr 100px 120px' }} key={row.code}>
            <span>{row.direction}</span>
            <span>
              <strong>{row.label}</strong>
              <small>{row.description}</small>
            </span>
            <span>{row.quantity || 0}</span>
            <span>{formatPrice(row.amount || 0)}</span>
          </div>
        ))}
      </EmptyAware>
    </article>
  );
}

function StatisticFilter({ title, subtitle, filters, loading, onSubmit, onField, onQuick, buttonText, compact = false }) {
  return (
    <form className={`admin-api-console ${compact ? 'admin-api-console--nested' : ''}`} onSubmit={onSubmit}>
      {!compact && <StatisticPanelHeading title={title} subtitle={subtitle} />}
      <div className="admin-quick-ranges">
        {quickRanges.map((option) => <button type="button" key={option.code} onClick={() => onQuick(option)}>{option.label}</button>)}
      </div>
      <div className="admin-api-console__row">
        <label>From<input type="date" value={filters.from} onChange={(event) => onField('from', event.target.value)} /></label>
        <label>To<input type="date" value={filters.to} onChange={(event) => onField('to', event.target.value)} /></label>
      </div>
      <div className="admin-api-console__row">
        <label>Timezone<input value={filters.timezone} onChange={(event) => onField('timezone', event.target.value)} /></label>
        <label>Group by
          <select value={filters.groupBy} onChange={(event) => onField('groupBy', event.target.value)}>
            <option value="AUTO">AUTO</option>
            <option value="DAY">DAY</option>
            <option value="WEEK">WEEK</option>
            <option value="MONTH">MONTH</option>
            <option value="QUARTER">QUARTER</option>
            <option value="YEAR">YEAR</option>
          </select>
        </label>
      </div>
      <div className="admin-api-console__row">
        <label>Order date field
          <select value={filters.dateField} onChange={(event) => onField('dateField', event.target.value)}>
            <option value="CREATED_AT">CREATED_AT</option>
            <option value="COMPLETED_AT">COMPLETED_AT</option>
            <option value="CANCELLED_AT">CANCELLED_AT</option>
          </select>
        </label>
        <label>Top limit<input type="number" min="1" max="50" value={filters.topLimit} onChange={(event) => onField('topLimit', event.target.value)} /></label>
      </div>
      <div className="admin-api-console__row">
        <label>Low stock threshold<input type="number" min="0" value={filters.lowStockThreshold} onChange={(event) => onField('lowStockThreshold', event.target.value)} /></label>
        <label className="inline-check">
          <input type="checkbox" checked={filters.compareWithPreviousPeriod} onChange={(event) => onField('compareWithPreviousPeriod', event.target.checked)} />
          Compare previous period
        </label>
      </div>
      <button type="submit" disabled={loading}>{loading ? 'Loading...' : buttonText}</button>
    </form>
  );
}

function StatisticPanelHeading({ title, subtitle }) {
  return (
    <div className="admin-panel__heading">
      <div>
        <p>{subtitle}</p>
        <h2>{title}</h2>
      </div>
    </div>
  );
}

function useStatisticLoader(loader, initialData) {
  const [filters, setFilters] = useState(defaultFilters);
  const [data, setData] = useState(initialData);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  function updateField(field, value) {
    setFilters((current) => ({ ...current, [field]: value }));
  }

  function chooseQuickRange(option) {
    setFilters((current) => ({ ...current, ...applyQuickRange(option) }));
  }

  async function load(event) {
    event.preventDefault();
    setLoading(true);
    setError('');

    try {
      const result = await loader(buildParams(filters), filters);
      setData(Array.isArray(initialData) ? asArray(result) : result);
    } catch (err) {
      setError(err.message || 'Không thể tải thống kê.');
    } finally {
      setLoading(false);
    }
  }

  return { data, filters, updateField, chooseQuickRange, load, loading, error };
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
      <StatisticPanelHeading title={title} subtitle="Status" />
      <EmptyAware rows={rows}>
        {(rows || []).map((row) => <div className="summary-line" key={row.code}><span>{row.label || row.code}</span><strong>{row.count}</strong></div>)}
      </EmptyAware>
    </article>
  );
}

function PaymentMethodPanel({ rows, title = 'Methods', embedded = false }) {
  const content = (
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
  );

  if (embedded) return content;

  return (
    <article className="admin-api-console">
      <StatisticPanelHeading title={title} subtitle="Payment" />
      {content}
    </article>
  );
}

function InventoryPanel({ inventory }) {
  return (
    <article className="admin-api-console">
      <StatisticPanelHeading title="Stock overview" subtitle="Inventory" />
      <div className="summary-line"><span>Stock quantity</span><strong>{inventory?.stockQuantity || 0}</strong></div>
      <div className="summary-line"><span>Reserved quantity</span><strong>{inventory?.reservedQuantity || 0}</strong></div>
      <div className="summary-line"><span>Available quantity</span><strong>{inventory?.availableQuantity || 0}</strong></div>
      <div className="summary-line"><span>Low stock variants</span><strong>{inventory?.lowStockVariantCount || 0}</strong></div>
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
    return <div className="admin-empty-mini">Chưa có dữ liệu trong khoảng thời gian này.</div>;
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
