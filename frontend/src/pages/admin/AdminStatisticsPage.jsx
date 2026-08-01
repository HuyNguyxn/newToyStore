import { useMemo, useState } from 'react';
import {
  Bar,
  BarChart,
  CartesianGrid,
  Cell,
  ComposedChart,
  Legend,
  Line,
  LineChart,
  Pie,
  PieChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts';
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

const chartColors = ['#f97316', '#2563eb', '#22c55e', '#ef4444', '#a855f7', '#14b8a6', '#f59e0b', '#64748b'];

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
          <span>Mỗi bảng thống kê có bộ lọc riêng và biểu đồ riêng để nhìn dữ liệu rõ hơn.</span>
        </div>
        <strong>Recharts dashboard</strong>
      </div>

      <OverviewStatisticsPanel />

      <section>
        <h3 className="admin-section-title">Revenue breakdown</h3>
        <div className="admin-statistics-panel-grid">
          <BreakdownStatisticPanel
            title="Revenue by category"
            subtitle="Danh mục tạo doanh thu cao nhất"
            chartType="bar"
            loader={(params) => getRevenueByCategory(params)}
          />
          <BreakdownStatisticPanel
            title="Revenue by payment method"
            subtitle="Tỷ trọng theo phương thức thanh toán"
            chartType="pie"
            loader={(params) => getRevenueByPaymentMethod(params)}
            labelSelector={(row) => row.method || row.label || row.code}
            countSelector={(row) => row.transactionCount || row.count || 0}
          />
          <BreakdownStatisticPanel
            title="Revenue by promotion"
            subtitle="Doanh thu theo chương trình khuyến mãi"
            chartType="bar"
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
            valueKey="grossRevenue"
          />
          <ProfitMarginStatisticPanel />
          <BreakdownStatisticPanel
            title="Top spending customers"
            subtitle="Khách hàng chi tiêu cao nhất"
            chartType="bar"
            loader={(params) => getTopSpendingCustomers(params)}
          />
          <BreakdownStatisticPanel
            title="Customer summary"
            subtitle="Khách mới, khách có đơn, khách mua lặp lại"
            chartType="bar"
            valueFormatter={(value) => `${value}`}
            loader={(params) => getCustomerSummary(params)}
          />
          <BreakdownStatisticPanel
            title="Customer trend"
            subtitle="Khách hàng mới theo thời gian"
            chartType="line"
            valueFormatter={(value) => `${value}`}
            loader={(params) => getCustomerTrend(params)}
          />
        </div>
      </section>

      <section>
        <h3 className="admin-section-title">Risk and operations</h3>
        <div className="admin-statistics-panel-grid">
          <BreakdownStatisticPanel
            title="Payment failure reasons"
            subtitle="Lý do thanh toán thất bại"
            chartType="bar"
            valueFormatter={(value) => `${value}`}
            loader={(params) => getPaymentFailureReasons(params)}
          />
          <BreakdownStatisticPanel
            title="Refund by reason"
            subtitle="Lý do hoàn tiền"
            chartType="pie"
            loader={(params) => getRefundReasons(params)}
          />
          <BreakdownStatisticPanel
            title="Refund by product"
            subtitle="Tiền refund được phân bổ theo sản phẩm"
            chartType="bar"
            loader={(params) => getRefundByProduct(params)}
          />
          <BreakdownStatisticPanel
            title="Shipment by provider"
            subtitle="Đơn giao hàng theo nhà vận chuyển"
            chartType="pie"
            loader={(params) => getShipmentsByProvider(params)}
          />
          <BreakdownStatisticPanel
            title="Shipment by region"
            subtitle="Đơn giao hàng theo khu vực"
            chartType="bar"
            loader={(params) => getShipmentsByRegion(params)}
          />
          <BreakdownStatisticPanel
            title="Shipment failure reasons"
            subtitle="Lý do giao hàng thất bại"
            chartType="bar"
            valueFormatter={(value) => `${value}`}
            loader={(params) => getShipmentFailureReasons(params)}
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
            <StatisticPanelHeading title="Revenue trend" subtitle="Biểu đồ đường theo doanh thu thuần" />
            <RevenueLineChart rows={overview.revenueTrend || []} />
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
            <BreakdownChart
              rows={overview.paymentMethods || []}
              chartType="pie"
              labelSelector={(row) => row.method || row.label || row.code}
              valueSelector={(row) => row.amount || 0}
              valueFormatter={formatPrice}
            />
            <InventoryOverviewPanel inventory={overview.inventory} />
          </div>
        </>
      )}
    </>
  );
}

function BreakdownStatisticPanel({
  title,
  subtitle,
  loader,
  chartType,
  valueFormatter = formatPrice,
  labelSelector = (row) => row.label || row.code || 'Unknown',
  valueSelector = (row) => row.amount ?? row.count ?? 0,
  countSelector = (row) => row.count || 0,
}) {
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
      <BreakdownChart
        rows={data}
        chartType={chartType}
        labelSelector={labelSelector}
        valueSelector={valueSelector}
        valueFormatter={valueFormatter}
      />
      <EmptyAware rows={data}>
        {data.map((row) => (
          <MetricRow
            key={row.code || row.label || row.method}
            label={labelSelector(row)}
            value={valueFormatter(valueSelector(row))}
            percent={row.sharePercent}
            meta={`Count: ${countSelector(row)}`}
          />
        ))}
      </EmptyAware>
    </article>
  );
}

function TopProductStatisticPanel({ title, subtitle, loader, valueKey = 'soldQuantity' }) {
  const { data, filters, updateField, chooseQuickRange, load, loading, error } = useStatisticLoader(loader, []);
  const valueFormatter = valueKey === 'grossRevenue' ? formatPrice : (value) => `${value} sold`;

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
      <BreakdownChart
        rows={data}
        chartType="bar"
        labelSelector={(row) => row.productName || row.label || 'Product'}
        valueSelector={(row) => row[valueKey] || row.amount || 0}
        valueFormatter={valueFormatter}
      />
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
      <StatisticPanelHeading title="Profit / margin report" subtitle="Cột đôi cost/net revenue kết hợp line net profit" />
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
      <ProfitMarginChart rows={data} />
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
      <StatisticPanelHeading title="Inventory movements" subtitle="Nhập, xuất, chênh lệch và snapshot tồn kho" />
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
      <BreakdownChart
        rows={data}
        chartType="bar"
        labelSelector={(row) => row.label || row.code}
        valueSelector={(row) => Math.abs(row.quantity || 0)}
        valueFormatter={(value) => `${value} units`}
      />
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

function RevenueLineChart({ rows }) {
  const data = asArray(rows);
  if (!data.length) return <div className="admin-empty-mini">Chưa có dữ liệu biểu đồ.</div>;

  return (
    <ChartFrame>
      <LineChart data={data}>
        <CartesianGrid strokeDasharray="3 3" />
        <XAxis dataKey="period" />
        <YAxis tickFormatter={compactMoney} />
        <Tooltip formatter={(value) => formatPrice(value)} />
        <Legend />
        <Line type="monotone" dataKey="grossRevenue" name="Gross revenue" stroke="#f97316" strokeWidth={3} dot={{ r: 4 }} />
        <Line type="monotone" dataKey="netRevenue" name="Net revenue" stroke="#2563eb" strokeWidth={3} dot={{ r: 4 }} />
        <Line type="monotone" dataKey="refundAmount" name="Refund" stroke="#ef4444" strokeWidth={2} dot={{ r: 3 }} />
      </LineChart>
    </ChartFrame>
  );
}

function ProfitMarginChart({ rows }) {
  const data = asArray(rows).slice(0, 10).map((row) => ({
    ...row,
    shortName: shorten(row.productName || `#${row.productId}`, 14),
  }));

  if (!data.length) return <div className="admin-empty-mini">Chưa có dữ liệu biểu đồ.</div>;

  return (
    <ChartFrame>
      <ComposedChart data={data}>
        <CartesianGrid strokeDasharray="3 3" />
        <XAxis dataKey="shortName" />
        <YAxis yAxisId="money" tickFormatter={compactMoney} />
        <YAxis yAxisId="percent" orientation="right" tickFormatter={(value) => `${value}%`} />
        <Tooltip formatter={(value, name) => String(name).includes('Margin') ? `${value}%` : formatPrice(value)} />
        <Legend />
        <Bar yAxisId="money" dataKey="cost" name="Cost / nhập vào" fill="#ef4444" radius={[8, 8, 0, 0]} />
        <Bar yAxisId="money" dataKey="netRevenue" name="Net revenue / bán ra" fill="#22c55e" radius={[8, 8, 0, 0]} />
        <Line yAxisId="money" type="monotone" dataKey="netProfit" name="Net profit trend" stroke="#2563eb" strokeWidth={3} />
        <Line yAxisId="percent" type="monotone" dataKey="marginPercent" name="Margin %" stroke="#a855f7" strokeWidth={2} />
      </ComposedChart>
    </ChartFrame>
  );
}

function BreakdownChart({ rows, chartType, labelSelector, valueSelector, valueFormatter }) {
  const data = asArray(rows).slice(0, 10).map((row, index) => ({
    name: shorten(labelSelector(row), 18),
    value: Number(valueSelector(row)) || 0,
    raw: row,
    fill: chartColors[index % chartColors.length],
  })).filter((row) => row.value > 0);

  if (!data.length) return <div className="admin-empty-mini">Chưa có dữ liệu biểu đồ.</div>;

  if (chartType === 'pie') {
    return (
      <ChartFrame compact>
        <PieChart>
          <Tooltip formatter={(value) => valueFormatter(value)} />
          <Legend />
          <Pie
            data={data}
            dataKey="value"
            nameKey="name"
            innerRadius={42}
            outerRadius={82}
            paddingAngle={3}
            activeShape={ActivePieSlice}
          >
            {data.map((entry) => <Cell key={entry.name} fill={entry.fill} />)}
          </Pie>
        </PieChart>
      </ChartFrame>
    );
  }

  if (chartType === 'line') {
    return (
      <ChartFrame compact>
        <LineChart data={data}>
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis dataKey="name" />
          <YAxis tickFormatter={compactNumber} />
          <Tooltip formatter={(value) => valueFormatter(value)} />
          <Line type="monotone" dataKey="value" name="Value" stroke="#2563eb" strokeWidth={3} />
        </LineChart>
      </ChartFrame>
    );
  }

  return (
    <ChartFrame compact>
      <BarChart data={data}>
        <CartesianGrid strokeDasharray="3 3" />
        <XAxis dataKey="name" />
        <YAxis tickFormatter={compactNumber} />
        <Tooltip formatter={(value) => valueFormatter(value)} />
        <Bar dataKey="value" name="Value" radius={[8, 8, 0, 0]}>
          {data.map((entry) => <Cell key={entry.name} fill={entry.fill} />)}
        </Bar>
      </BarChart>
    </ChartFrame>
  );
}

function ActivePieSlice(props) {
  const { cx, cy, innerRadius, outerRadius, startAngle, endAngle, fill, payload, percent, value } = props;
  return (
    <g className="recharts-active-slice">
      <Pie
        data={[payload]}
        dataKey="value"
        cx={cx}
        cy={cy - 6}
        innerRadius={innerRadius}
        outerRadius={outerRadius + 8}
        startAngle={startAngle}
        endAngle={endAngle}
        fill={fill}
        isAnimationActive={false}
      />
      <text x={cx} y={cy} textAnchor="middle" fill="#0f172a" fontSize="12" fontWeight="800">
        {payload.name}
      </text>
      <text x={cx} y={cy + 18} textAnchor="middle" fill="#64748b" fontSize="11">
        {`${(percent * 100).toFixed(1)}% · ${compactNumber(value)}`}
      </text>
    </g>
  );
}

function InventoryOverviewPanel({ inventory }) {
  const rows = [
    { label: 'Stock', amount: inventory?.stockQuantity || 0 },
    { label: 'Reserved', amount: inventory?.reservedQuantity || 0 },
    { label: 'Available', amount: inventory?.availableQuantity || 0 },
    { label: 'Low stock', amount: inventory?.lowStockVariantCount || 0 },
  ];

  return (
    <article className="admin-api-console">
      <StatisticPanelHeading title="Stock overview" subtitle="Inventory" />
      <BreakdownChart
        rows={rows}
        chartType="bar"
        labelSelector={(row) => row.label}
        valueSelector={(row) => row.amount}
        valueFormatter={(value) => `${value}`}
      />
      <div className="summary-line"><span>Stock quantity</span><strong>{inventory?.stockQuantity || 0}</strong></div>
      <div className="summary-line"><span>Reserved quantity</span><strong>{inventory?.reservedQuantity || 0}</strong></div>
      <div className="summary-line"><span>Available quantity</span><strong>{inventory?.availableQuantity || 0}</strong></div>
      <div className="summary-line"><span>Low stock variants</span><strong>{inventory?.lowStockVariantCount || 0}</strong></div>
    </article>
  );
}

function ChartFrame({ children, compact = false }) {
  return (
    <div className={`admin-chart-frame ${compact ? 'admin-chart-frame--compact' : ''}`}>
      <ResponsiveContainer width="100%" height={compact ? 260 : 340}>
        {children}
      </ResponsiveContainer>
    </div>
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

function StatusPanel({ title, rows }) {
  return (
    <article className="admin-api-console">
      <StatisticPanelHeading title={title} subtitle="Status" />
      <BreakdownChart
        rows={rows || []}
        chartType="pie"
        labelSelector={(row) => row.label || row.code}
        valueSelector={(row) => row.count}
        valueFormatter={(value) => `${value}`}
      />
      <EmptyAware rows={rows}>
        {(rows || []).map((row) => <div className="summary-line" key={row.code}><span>{row.label || row.code}</span><strong>{row.count}</strong></div>)}
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

function shorten(value, maxLength) {
  const text = String(value || '');
  return text.length > maxLength ? `${text.slice(0, maxLength)}...` : text;
}

function compactMoney(value) {
  if (Math.abs(value) >= 1000000) return `${Math.round(value / 1000000)}M`;
  if (Math.abs(value) >= 1000) return `${Math.round(value / 1000)}K`;
  return value;
}

function compactNumber(value) {
  if (Math.abs(value) >= 1000000) return `${Math.round(value / 1000000)}M`;
  if (Math.abs(value) >= 1000) return `${Math.round(value / 1000)}K`;
  return value;
}

export default AdminStatisticsPage;
