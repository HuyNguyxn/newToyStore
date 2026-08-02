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

const statisticViewOptions = [
  { value: 'OVERVIEW', label: 'Tổng quan cửa hàng' },
  { value: 'REVENUE_TREND', label: 'Biểu đồ doanh thu' },
  { value: 'REVENUE_CATEGORY', label: 'Doanh thu theo danh mục' },
  { value: 'REVENUE_PAYMENT', label: 'Doanh thu theo phương thức thanh toán' },
  { value: 'REVENUE_PROMOTION', label: 'Doanh thu theo khuyến mãi' },
  { value: 'TOP_PRODUCTS', label: 'Sản phẩm bán chạy' },
  { value: 'PRODUCT_REVENUE', label: 'Doanh thu theo sản phẩm' },
  { value: 'PROFIT_MARGIN', label: 'Lợi nhuận / biên lợi nhuận' },
  { value: 'TOP_CUSTOMERS', label: 'Khách hàng chi tiêu cao' },
  { value: 'CUSTOMER_SUMMARY', label: 'Tổng quan khách hàng' },
  { value: 'CUSTOMER_TREND', label: 'Xu hướng khách hàng' },
  { value: 'PAYMENT_FAILURE', label: 'Lý do thanh toán thất bại' },
  { value: 'REFUND_REASON', label: 'Lý do hoàn tiền' },
  { value: 'REFUND_PRODUCT', label: 'Hoàn tiền theo sản phẩm' },
  { value: 'SHIPMENT_PROVIDER', label: 'Vận chuyển theo nhà giao hàng' },
  { value: 'SHIPMENT_REGION', label: 'Vận chuyển theo khu vực' },
  { value: 'SHIPMENT_FAILURE', label: 'Lý do giao hàng thất bại' },
  { value: 'INVENTORY_MOVEMENT', label: 'Biến động tồn kho' },
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

function buildParams(filters) {
  return Object.fromEntries(Object.entries(filters).filter(([, value]) => value !== '' && value !== false));
}

function AdminStatisticsPage() {
  const [selectedView, setSelectedView] = useState('OVERVIEW');
  const selectedOption = statisticViewOptions.find((option) => option.value === selectedView) || statisticViewOptions[0];

  return (
    <section className="admin-resource">
      <div className="admin-resource__hero">
        <div>
          <p>THỐNG KÊ QUẢN TRỊ</p>
          <h2>Tổng quan và biểu đồ cửa hàng</h2>
          <span>Mỗi nhóm thống kê có bộ lọc riêng. Chọn một biểu đồ để tập trung xem đúng dữ liệu cần phân tích.</span>
        </div>
        <strong>{selectedOption.label}</strong>
      </div>

      <StatisticViewSelector selectedView={selectedView} onSelect={setSelectedView} />
      <SelectedStatisticView selectedView={selectedView} />
    </section>
  );
}

function StatisticViewSelector({ selectedView, onSelect }) {
  return (
    <section className="admin-statistics-switcher">
      <label>
        <span>Chọn biểu đồ / báo cáo</span>
        <select value={selectedView} onChange={(event) => onSelect(event.target.value)}>
          {statisticViewOptions.map((option) => (
            <option key={option.value} value={option.value}>{option.label}</option>
          ))}
        </select>
      </label>
      <p>Chọn “Tổng quan cửa hàng” để xem dashboard chung. Chọn biểu đồ cụ thể thì trang chỉ hiển thị đúng biểu đồ đó.</p>
    </section>
  );
}

function SelectedStatisticView({ selectedView }) {
  if (selectedView === 'OVERVIEW') return <OverviewStatisticsPanel />;
  if (selectedView === 'REVENUE_TREND') return <RevenueTrendStatisticPanel />;
  if (selectedView === 'PROFIT_MARGIN') return <ProfitMarginStatisticPanel />;
  if (selectedView === 'INVENTORY_MOVEMENT') return <InventoryMovementStatisticPanel />;
  if (selectedView === 'TOP_PRODUCTS') {
    return (
      <TopProductStatisticPanel
        title="Sản phẩm bán chạy"
        subtitle="Sản phẩm bán chạy theo số lượng"
        loader={(params, filters) => getTopSellingProducts({ ...params, limit: filters.topLimit })}
      />
    );
  }
  if (selectedView === 'PRODUCT_REVENUE') {
    return (
      <TopProductStatisticPanel
        title="Doanh thu theo sản phẩm"
        subtitle="Những sản phẩm tạo doanh thu cao"
        loader={(params, filters) => getRevenueByProduct({ ...params, limit: filters.topLimit })}
        valueKey="grossRevenue"
      />
    );
  }

  const config = getBreakdownStatisticConfig(selectedView);
  if (!config) return <OverviewStatisticsPanel />;

  return <BreakdownStatisticPanel {...config} />;
}

function getBreakdownStatisticConfig(selectedView) {
  const configs = {
    REVENUE_CATEGORY: {
      title: 'Doanh thu theo danh mục',
      subtitle: 'Danh mục tạo doanh thu cao nhất',
      chartType: 'bar',
      loader: (params) => getRevenueByCategory(params),
    },
    REVENUE_PAYMENT: {
      title: 'Doanh thu theo phương thức thanh toán',
      subtitle: 'Tỷ trọng doanh thu theo COD, VNPAY và các phương thức khác',
      chartType: 'pie',
      loader: (params) => getRevenueByPaymentMethod(params),
      labelSelector: (row) => row.method || row.label || row.code,
      countSelector: (row) => row.transactionCount || row.count || 0,
    },
    REVENUE_PROMOTION: {
      title: 'Doanh thu theo khuyến mãi',
      subtitle: 'Doanh thu được tạo ra từ từng chương trình khuyến mãi',
      chartType: 'bar',
      loader: (params) => getRevenueByPromotion(params),
    },
    TOP_CUSTOMERS: {
      title: 'Khách hàng chi tiêu cao',
      subtitle: 'Những khách hàng có tổng chi tiêu cao nhất',
      chartType: 'bar',
      loader: (params) => getTopSpendingCustomers(params),
    },
    CUSTOMER_SUMMARY: {
      title: 'Tổng quan khách hàng',
      subtitle: 'Khách mới, khách có đơn và khách mua lặp lại',
      chartType: 'bar',
      valueFormatter: (value) => `${value}`,
      loader: (params) => getCustomerSummary(params),
    },
    CUSTOMER_TREND: {
      title: 'Xu hướng khách hàng',
      subtitle: 'Lượng khách hàng mới theo thời gian',
      chartType: 'line',
      valueFormatter: (value) => `${value}`,
      loader: (params) => getCustomerTrend(params),
    },
    PAYMENT_FAILURE: {
      title: 'Lý do thanh toán thất bại',
      subtitle: 'Những nguyên nhân làm giao dịch thanh toán không thành công',
      chartType: 'bar',
      valueFormatter: (value) => `${value}`,
      loader: (params) => getPaymentFailureReasons(params),
    },
    REFUND_REASON: {
      title: 'Lý do hoàn tiền',
      subtitle: 'Tỷ trọng các nguyên nhân dẫn tới hoàn tiền',
      chartType: 'pie',
      loader: (params) => getRefundReasons(params),
    },
    REFUND_PRODUCT: {
      title: 'Hoàn tiền theo sản phẩm',
      subtitle: 'Số tiền hoàn lại được phân bổ theo sản phẩm',
      chartType: 'bar',
      loader: (params) => getRefundByProduct(params),
    },
    SHIPMENT_PROVIDER: {
      title: 'Vận chuyển theo nhà giao hàng',
      subtitle: 'Số đơn giao hàng theo từng đơn vị vận chuyển',
      chartType: 'pie',
      loader: (params) => getShipmentsByProvider(params),
    },
    SHIPMENT_REGION: {
      title: 'Vận chuyển theo khu vực',
      subtitle: 'Số đơn giao hàng theo từng khu vực',
      chartType: 'bar',
      loader: (params) => getShipmentsByRegion(params),
    },
    SHIPMENT_FAILURE: {
      title: 'Lý do giao hàng thất bại',
      subtitle: 'Những nguyên nhân khiến giao hàng không thành công',
      chartType: 'bar',
      valueFormatter: (value) => `${value}`,
      loader: (params) => getShipmentFailureReasons(params),
    },
  };

  return configs[selectedView];
}

function RevenueTrendStatisticPanel() {
  const { data, filters, updateField, chooseQuickRange, load, loading, error } = useStatisticLoader(
    (params) => getStatisticsOverview(params),
    null
  );

  return (
    <article className="admin-api-console">
      <StatisticPanelHeading title="Biểu đồ doanh thu" subtitle="Đường doanh thu theo thời gian" />
      <StatisticFilter
        compact
        title="Bộ lọc doanh thu"
        filters={filters}
        loading={loading}
        onSubmit={load}
        onField={updateField}
        onQuick={chooseQuickRange}
        buttonText="Tải biểu đồ"
      />
      {error && <div className="form-alert">{error}</div>}
      <RevenueLineChart rows={data?.revenueTrend || []} />
    </article>
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
        title="Bộ lọc tổng quan"
        subtitle="Bộ lọc riêng cho tổng quan cửa hàng"
        filters={filters}
        loading={loading}
        onSubmit={load}
        onField={updateField}
        onQuick={chooseQuickRange}
        buttonText="Tải tổng quan"
      />
      {error && <div className="form-alert">{error}</div>}

      {overview && (
        <>
          <div className="admin-detail-summary">
            <p><strong>Khoảng thời gian:</strong> {overview.period?.from} → {overview.period?.to}</p>
            <p><strong>Tạo lúc:</strong> {formatDateTime(overview.generatedAt)}</p>
            <p><strong>Múi giờ:</strong> {overview.period?.timezone}</p>
            <p><strong>Nhóm theo:</strong> yêu cầu {overview.period?.requestedGroupBy}, áp dụng {overview.period?.appliedGroupBy}</p>
            <p><strong>Đã tự điều chỉnh:</strong> {overview.period?.groupByAdjusted ? 'Có' : 'Không'} · <strong>So sánh:</strong> {overview.period?.compareWithPreviousPeriod ? 'Có' : 'Không'}</p>
          </div>

          <KpiSection title="Chỉ số doanh thu" items={groupedKpis.revenue} />
          <KpiSection title="Chỉ số vận hành" items={groupedKpis.operations} />
          <KpiSection title="Chỉ số tỷ lệ" items={groupedKpis.rates} />
          <KpiSection title="Chỉ số khuyến mãi" items={groupedKpis.promotion} />

          <section className="admin-api-console">
            <StatisticPanelHeading title="Xu hướng doanh thu" subtitle="Biểu đồ đường theo doanh thu thuần" />
            <RevenueLineChart rows={overview.revenueTrend || []} />
          </section>

          <div className="admin-dashboard-grid">
            <StatusPanel title="Trạng thái đơn hàng" rows={overview.orderStatus} />
            <StatusPanel title="Trạng thái thanh toán" rows={overview.paymentStatus} />
            <StatusPanel title="Trạng thái hoàn tiền" rows={overview.refundStatus} />
            <StatusPanel title="Trạng thái vận chuyển" rows={overview.shipmentStatus} />
            <StatusPanel title="Trạng thái người dùng" rows={overview.userStatus} />
            <StatusPanel title="Trạng thái sản phẩm" rows={overview.productStatus} />
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
        buttonText="Tải dữ liệu"
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
        buttonText="Tải dữ liệu"
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
      <StatisticPanelHeading title="Báo cáo lợi nhuận / biên lợi nhuận" subtitle="Cột đôi chi phí/doanh thu thuần kết hợp đường lợi nhuận thuần" />
      <StatisticFilter
        compact
        title="Bộ lọc lợi nhuận"
        filters={filters}
        loading={loading}
        onSubmit={load}
        onField={updateField}
        onQuick={chooseQuickRange}
        buttonText="Tải dữ liệu"
      />
      {error && <div className="form-alert">{error}</div>}
      <ProfitMarginChart rows={data} />
      <div className="admin-resource-table__head" style={{ gridTemplateColumns: '1.2fr 70px 110px 100px 110px 110px 90px' }}>
        <span>Sản phẩm</span><span>Đã bán</span><span>Gộp</span><span>Hoàn tiền</span><span>Thuần</span><span>Lợi nhuận</span><span>Biên LN</span>
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
      <StatisticPanelHeading title="Biến động tồn kho" subtitle="Nhập, xuất, chênh lệch và snapshot tồn kho" />
      <StatisticFilter
        compact
        title="Bộ lọc biến động tồn kho"
        filters={filters}
        loading={loading}
        onSubmit={load}
        onField={updateField}
        onQuick={chooseQuickRange}
        buttonText="Tải dữ liệu"
      />
      {error && <div className="form-alert">{error}</div>}
      <BreakdownChart
        rows={data}
        chartType="bar"
        labelSelector={(row) => row.label || row.code}
        valueSelector={(row) => Math.abs(row.quantity || 0)}
        valueFormatter={(value) => `${value} đơn vị`}
      />
      <div className="admin-resource-table__head" style={{ gridTemplateColumns: '110px 1fr 100px 120px' }}>
        <span>Loại</span><span>Biến động</span><span>Số lượng</span><span>Giá trị</span>
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
        <Line type="monotone" dataKey="grossRevenue" name="Doanh thu gộp" stroke="#f97316" strokeWidth={3} dot={{ r: 4 }} />
        <Line type="monotone" dataKey="netRevenue" name="Doanh thu thuần" stroke="#2563eb" strokeWidth={3} dot={{ r: 4 }} />
        <Line type="monotone" dataKey="refundAmount" name="Hoàn tiền" stroke="#ef4444" strokeWidth={2} dot={{ r: 3 }} />
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
        <Bar yAxisId="money" dataKey="cost" name="Chi phí / nhập vào" fill="#ef4444" radius={[8, 8, 0, 0]} />
        <Bar yAxisId="money" dataKey="netRevenue" name="Doanh thu thuần / bán ra" fill="#22c55e" radius={[8, 8, 0, 0]} />
        <Line yAxisId="money" type="monotone" dataKey="netProfit" name="Xu hướng lợi nhuận thuần" stroke="#2563eb" strokeWidth={3} />
        <Line yAxisId="percent" type="monotone" dataKey="marginPercent" name="Biên lợi nhuận %" stroke="#a855f7" strokeWidth={2} />
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
    { label: 'Tồn kho', amount: inventory?.stockQuantity || 0 },
    { label: 'Đã giữ', amount: inventory?.reservedQuantity || 0 },
    { label: 'Có thể bán', amount: inventory?.availableQuantity || 0 },
    { label: 'Sắp hết hàng', amount: inventory?.lowStockVariantCount || 0 },
  ];

  return (
    <article className="admin-api-console">
      <StatisticPanelHeading title="Tổng quan tồn kho" subtitle="Tồn kho" />
      <BreakdownChart
        rows={rows}
        chartType="bar"
        labelSelector={(row) => row.label}
        valueSelector={(row) => row.amount}
        valueFormatter={(value) => `${value}`}
      />
      <div className="summary-line"><span>Tổng tồn kho</span><strong>{inventory?.stockQuantity || 0}</strong></div>
      <div className="summary-line"><span>Đã giữ hàng</span><strong>{inventory?.reservedQuantity || 0}</strong></div>
      <div className="summary-line"><span>Có thể bán</span><strong>{inventory?.availableQuantity || 0}</strong></div>
      <div className="summary-line"><span>Biến thể sắp hết hàng</span><strong>{inventory?.lowStockVariantCount || 0}</strong></div>
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
        <label>Từ ngày<input type="date" value={filters.from} onChange={(event) => onField('from', event.target.value)} /></label>
        <label>Đến ngày<input type="date" value={filters.to} onChange={(event) => onField('to', event.target.value)} /></label>
      </div>
      <div className="admin-api-console__row">
        <label>Múi giờ<input value={filters.timezone} onChange={(event) => onField('timezone', event.target.value)} /></label>
        <label>Nhóm theo
          <select value={filters.groupBy} onChange={(event) => onField('groupBy', event.target.value)}>
            <option value="AUTO">Tự động</option>
            <option value="DAY">Theo ngày</option>
            <option value="WEEK">Theo tuần</option>
            <option value="MONTH">Theo tháng</option>
            <option value="QUARTER">Theo quý</option>
            <option value="YEAR">Theo năm</option>
          </select>
        </label>
      </div>
      <div className="admin-api-console__row">
        <label>Mốc ngày đơn hàng
          <select value={filters.dateField} onChange={(event) => onField('dateField', event.target.value)}>
            <option value="CREATED_AT">Ngày tạo đơn</option>
            <option value="COMPLETED_AT">Ngày hoàn tất</option>
            <option value="CANCELLED_AT">Ngày hủy đơn</option>
          </select>
        </label>
        <label>Số dòng top<input type="number" min="1" max="50" value={filters.topLimit} onChange={(event) => onField('topLimit', event.target.value)} /></label>
      </div>
      <div className="admin-api-console__row">
        <label>Ngưỡng sắp hết hàng<input type="number" min="0" value={filters.lowStockThreshold} onChange={(event) => onField('lowStockThreshold', event.target.value)} /></label>
        <label className="inline-check">
          <input type="checkbox" checked={filters.compareWithPreviousPeriod} onChange={(event) => onField('compareWithPreviousPeriod', event.target.checked)} />
          So sánh kỳ trước
        </label>
      </div>
      <button type="submit" disabled={loading}>{loading ? 'Đang tải...' : buttonText}</button>
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
      {kpi.changePercent !== null && kpi.changePercent !== undefined && <small>{kpi.changePercent}% so với kỳ trước</small>}
    </article>
  );
}

function StatusPanel({ title, rows }) {
  return (
    <article className="admin-api-console">
      <StatisticPanelHeading title={title} subtitle="Trạng thái" />
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

