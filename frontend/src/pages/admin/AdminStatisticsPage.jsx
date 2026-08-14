import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Area,
  AreaChart,
  Bar,
  BarChart,
  CartesianGrid,
  Cell,
  Legend,
  Line,
  LineChart,
  ComposedChart,
  Pie,
  PieChart,
  ResponsiveContainer,
  Sector,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts';
import { getAdminOrders } from '../../services/adminOrderService.js';
import { getImports } from '../../services/adminImportService.js';
import { getAdminReviews } from '../../services/adminReviewService.js';
import { getAllAdminProducts } from '../../services/adminProductService.js';
import { getCustomerReturns } from '../../services/adminReturnService.js';
import { getAdminMenuBadges } from '../../services/adminBadgeService.js';
import {
  getInventorySnapshot,
  getInventoryCostSummary,
  getRevenueByCategory,
  getRevenueByPaymentMethod,
  getRevenueTrend,
  getSlowSellingProducts,
  getStatisticsOverview,
  getTopSellingProducts,
  getTopSpendingCustomers,
} from '../../services/statisticsService.js';
import { formatDateTime, formatPrice } from '../../utils/formatters.js';

/* ═══════════════════════════════════════════════════════════════════
   DESIGN TOKENS & PALETTES
   ═══════════════════════════════════════════════════════════════════ */
const chartColors = ['#ea580c', '#2563eb', '#16a34a', '#9333ea', '#f59e0b', '#06b6d4', '#ec4899', '#64748b'];

const quickRanges = [
  { code: 'TODAY', label: 'Hôm nay' },
  { code: 'YESTERDAY', label: 'Hôm qua' },
  { code: 'LAST_7_DAYS', label: '7 ngày gần nhất' },
  { code: 'LAST_30_DAYS', label: '30 ngày gần nhất' },
  { code: 'THIS_MONTH', label: 'Tháng này' },
  { code: 'LAST_MONTH', label: 'Tháng trước' },
];

/* ═══════════════════════════════════════════════════════════════════
   UTILITY HELPERS
   ═══════════════════════════════════════════════════════════════════ */
function toDateString(date) {
  const d = new Date(date);
  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

function getInitialDates(code) {
  const now = new Date();
  const todayStr = toDateString(now);

  if (code === 'TODAY') return { from: todayStr, to: todayStr };
  if (code === 'YESTERDAY') {
    const y = new Date(now);
    y.setDate(y.getDate() - 1);
    const yStr = toDateString(y);
    return { from: yStr, to: yStr };
  }
  if (code === 'LAST_7_DAYS') {
    const f = new Date(now);
    f.setDate(f.getDate() - 6);
    return { from: toDateString(f), to: todayStr };
  }
  if (code === 'LAST_30_DAYS') {
    const f = new Date(now);
    f.setDate(f.getDate() - 29);
    return { from: toDateString(f), to: todayStr };
  }
  if (code === 'LAST_MONTH') {
    const first = new Date(now.getFullYear(), now.getMonth() - 1, 1);
    const last = new Date(now.getFullYear(), now.getMonth(), 0);
    return { from: toDateString(first), to: toDateString(last) };
  }
  const firstThisMonth = new Date(now.getFullYear(), now.getMonth(), 1);
  return { from: toDateString(firstThisMonth), to: todayStr };
}

function formatVndText(amount) {
  return formatPrice(amount || 0).replace(/đ$/i, '').trim() + ' VND';
}

function formatStatisticNumber(value) {
  const number = Number(value);
  return Number.isFinite(number)
    ? new Intl.NumberFormat('vi-VN', { maximumFractionDigits: 2 }).format(number)
    : '0';
}

function generateDateSeries(fromStr, toStr) {
  const list = [];
  const start = new Date(fromStr || '2026-07-25');
  const end = new Date(toStr || '2026-08-04');

  for (let d = new Date(start); d <= end; d.setDate(d.getDate() + 1)) {
    const m = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    list.push(`${day}/${m}`);
  }
  return list.length ? list : ['28/07', '29/07', '30/07', '31/07', '01/08', '02/08', '03/08', '04/08'];
}

function formatYMoneyTick(v) {
  if (!v || v === 0) return '0';
  if (v >= 1_000_000_000) {
    const val = v / 1_000_000_000;
    return `${val % 1 === 0 ? val.toFixed(0) : val.toFixed(1)}B`;
  }
  if (v >= 1_000_000) {
    const val = v / 1_000_000;
    return `${val % 1 === 0 ? val.toFixed(0) : val.toFixed(1)}M`;
  }
  if (v >= 1_000) {
    const val = v / 1_000;
    return `${val % 1 === 0 ? val.toFixed(0) : val.toFixed(1)}K`;
  }
  return String(v);
}

function getMoneyTicks(data = []) {
  let max = 0;
  data.forEach((item) => {
    const v1 = Number(item['Tổng doanh thu'] || 0);
    const v2 = Number(item['Tổng lợi nhuận'] || 0);
    const v3 = Number(item['Chi phí nhập hàng'] || 0);
    if (v1 > max) max = v1;
    if (v2 > max) max = v2;
    if (v3 > max) max = v3;
  });
  if (max === 0) return [0, 1000000, 2000000, 3000000, 4000000];
  const stepUnit = max >= 1_000_000_000 ? 250000000 : 500000;
  const step = Math.ceil(max / 4 / stepUnit) * stepUnit || 1000000;
  return [0, step, step * 2, step * 3, step * 4];
}

function getProductTicks(data = []) {
  let max = 0;
  data.forEach((item) => {
    const v1 = Number(item['Tổng SP bán ra'] || 0);
    const v2 = Number(item['Tổng SP nhập vào'] || 0);
    if (v1 > max) max = v1;
    if (v2 > max) max = v2;
  });
  if (max === 0) return [0, 5, 10, 15, 20];
  const step = Math.ceil(max / 4) || 5;
  return [0, step, step * 2, step * 3, step * 4];
}

function buildMacSeries(imports = [], orders = [], from, to) {
  const dates = generateDateSeries(from, to);
  const byDate = new Map(dates.map((date) => [date, {
    date,
    sellingPrice: null,
    importPrice: null,
    mac: null,
    importedQuantity: 0,
    soldQuantity: 0,
    grossMarginGap: null,
    stockQuantity: null,
  }]));

  const dateInfo = (value) => {
    if (!value) return null;
    const d = new Date(value);
    if (Number.isNaN(d.getTime())) return null;
    const iso = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
    return { iso, key: `${String(d.getDate()).padStart(2, '0')}/${String(d.getMonth() + 1).padStart(2, '0')}` };
  };

  const events = [];
  imports.forEach((receipt) => {
    const receiptStatus = typeof receipt.status === 'object'
      ? receipt.status?.code || receipt.status?.name || receipt.status?.status
      : receipt.status;
    if (String(receiptStatus || '').toUpperCase() !== 'COMPLETED') return;
    const date = dateInfo(receipt.completedAt || receipt.updatedAt || receipt.createdAt || receipt.importDate);
    if (!date || date.iso > to) return;
    (receipt.items || []).forEach((item) => {
      const quantity = Number(item.quantity || 0);
      const unitCost = Number(item.importPrice || item.price || 0);
      if (quantity > 0) events.push({ ...date, type: 'IMPORT', quantity, unitCost });
    });
  });

  orders.forEach((order) => {
    const status = typeof order.status === 'object' ? order.status?.code || order.status?.name : order.status;
    const date = dateInfo(getOrderRevenueDate(order));
    if (!date || date.iso > to || !['COMPLETED', 'PARTIALLY_REFUNDED', 'FULLY_REFUNDED'].includes(String(status || '').toUpperCase())) return;
    (order.items || []).forEach((item) => {
      const quantity = Number(item.quantity || 0);
      const unitPrice = Number(item.price || item.unitPrice || 0);
      if (quantity > 0) events.push({ ...date, type: 'SALE', quantity, unitPrice });
    });
  });

  events.sort((a, b) => a.iso.localeCompare(b.iso) || (a.type === 'IMPORT' ? -1 : 1));
  let runningStock = 0;
  let stockValue = 0;
  let runningMac = null;
  let lastSellingPrice = null;
  let openingStock = 0;
  let openingMac = null;
  let openingSellingPrice = null;
  let openingCaptured = false;
  events.forEach((event) => {
    if (!openingCaptured && event.iso >= from) {
      openingStock = runningStock;
      openingMac = runningMac;
      openingSellingPrice = lastSellingPrice;
      openingCaptured = true;
    }
    const point = event.iso >= from ? byDate.get(event.key) : null;
    if (event.type === 'IMPORT') {
      stockValue += event.quantity * event.unitCost;
      runningStock += event.quantity;
      runningMac = runningStock > 0 ? stockValue / runningStock : runningMac;
      if (point) {
        const previousQuantity = point.importedQuantity;
        point.importedQuantity += event.quantity;
        point.importPrice = point.importPrice === null
          ? event.unitCost
          : ((point.importPrice * previousQuantity) + (event.unitCost * event.quantity)) / point.importedQuantity;
      }
    } else {
      lastSellingPrice = event.unitPrice;
      const fulfilledQuantity = Math.min(event.quantity, Math.max(0, runningStock));
      stockValue = Math.max(0, stockValue - fulfilledQuantity * Number(runningMac || 0));
      runningStock = Math.max(0, runningStock - fulfilledQuantity);
      if (point) {
        const previousQuantity = point.soldQuantity;
        point.soldQuantity += event.quantity;
        point.sellingPrice = point.sellingPrice === null
          ? event.unitPrice
          : ((point.sellingPrice * previousQuantity) + (event.unitPrice * event.quantity)) / point.soldQuantity;
      }
    }
    if (point) {
      point.mac = runningMac;
      point.stockQuantity = runningStock;
    }
  });

  let displayedStock = openingCaptured ? openingStock : runningStock;
  runningMac = openingCaptured ? openingMac : runningMac;
  lastSellingPrice = openingCaptured ? openingSellingPrice : lastSellingPrice;
  return dates.map((date) => {
    const point = byDate.get(date);
    if (point.mac !== null) runningMac = point.mac;
    point.mac = runningMac;
    if (point.stockQuantity !== null) displayedStock = point.stockQuantity;
    point.stockQuantity = displayedStock;
    if (point.sellingPrice !== null) lastSellingPrice = point.sellingPrice;
    point.sellingPrice = lastSellingPrice;
    point.grossMarginGap = point.sellingPrice !== null && point.mac !== null
      ? Math.max(0, point.sellingPrice - point.mac)
      : null;
    return point;
  });
}

function getOrderRevenueDate(order = {}) {
  const completedHistory = (order.histories || []).find((history) => {
    const status = history.newStatus || history.currentStatus || history.toStatus || history.status;
    return getStatusCode(status) === 'COMPLETED';
  });
  return completedHistory?.createdAt
    || completedHistory?.occurredAt
    || order.completedAt
    || order.updatedAt
    || order.createdAt
    || order.orderDate;
}

const MacTooltip = ({ active, payload, label }) => {
  if (!active || !payload?.length) return null;
  const point = payload[0]?.payload || {};
  return (
    <div style={{ background: '#fff', border: '1px solid #dbeafe', borderRadius: 10, padding: '10px 12px', boxShadow: '0 8px 18px rgba(15,23,42,.12)', minWidth: 220 }}>
      <div style={{ fontWeight: 800, color: '#334155', marginBottom: 6 }}>Ngày: {label}</div>
      <div style={{ color: '#2563eb' }}>Giá bán: {point.sellingPrice == null ? '—' : formatVndText(point.sellingPrice)}</div>
      <div style={{ color: '#16a34a' }}>MAC: {point.mac == null ? '—' : formatVndText(point.mac)}</div>
      <div style={{ color: '#dc2626' }}>Giá nhập lô: {point.importPrice == null ? '—' : formatVndText(point.importPrice)}</div>
      <div style={{ color: '#64748b', marginTop: 4 }}>Nhập {point.importedQuantity} · Bán {point.soldQuantity}</div>
    </div>
  );
};

function InventoryValueChartCard({ data, dates, products, selectedVariant, setSelectedVariant }) {
  return (
    <div style={{ background: '#fff', borderRadius: '16px', boxShadow: '0 4px 16px rgba(0,0,0,0.03)', padding: '24px', border: '1px solid #dbeafe', marginBottom: '24px' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: 12, flexWrap: 'wrap' }}>
        <div>
          <h2 style={{ fontSize: '18px', fontWeight: '900', color: '#0f172a', margin: 0 }}>Dòng giá trị hàng hóa theo kỳ</h2>
          <span style={{ fontSize: '12px', color: '#64748b' }}>Giá vốn hàng đã bán và giá trị nhập kho đã hoàn tất từ {dates.from} đến {dates.to}. Biểu đồ này luôn phản ánh toàn cửa hàng.</span>
        </div>
        <select value={selectedVariant} onChange={(e) => setSelectedVariant(e.target.value)} style={{ minWidth: 210, padding: '7px 10px', border: '1px solid #bfdbfe', borderRadius: 8, color: '#1e3a8a', fontWeight: 700, background: '#eff6ff' }}>
          <option value="ALL">Thẻ hiện tại: toàn bộ biến thể</option>
          {products.flatMap((product) => (product.variants || []).map((variant) => <option key={variant.id} value={variant.id}>{product.name || `Sản phẩm #${product.id}`} · {variant.type || `Variant #${variant.id}`}</option>))}
        </select>
      </div>
      <div style={{ width: '100%', height: '330px', marginTop: 16 }}><ResponsiveContainer width="100%" height="100%"><ComposedChart data={data} margin={{ top: 8, right: 12, left: -8, bottom: 0 }}>
        <CartesianGrid strokeDasharray="3 3" stroke="#eff6ff" vertical={false} />
        <XAxis dataKey="date" stroke="#334155" fontSize={11} minTickGap={24} />
        <YAxis yAxisId="money" stroke="#334155" fontSize={10} tickFormatter={formatYMoneyTick} />
        <YAxis yAxisId="quantity" orientation="right" stroke="#94a3b8" fontSize={10} allowDecimals={false} />
        <Tooltip />
        <Legend wrapperStyle={{ paddingTop: 8, fontSize: 11 }} />
        <Bar yAxisId="quantity" dataKey="Tổng SP nhập vào" name="Sản phẩm nhập vào" fill="#a78bfa" barSize={9} />
        <Bar yAxisId="quantity" dataKey="Tổng SP bán ra" name="Sản phẩm bán ra" fill="#fb923c" barSize={9} />
        <Line yAxisId="money" type="monotone" dataKey="Chi phí nhập hàng" name="Giá trị nhập kho" stroke="#dc2626" strokeWidth={2.5} dot={false} connectNulls />
        <Line yAxisId="money" type="monotone" dataKey="Giá vốn hàng bán" name="Giá vốn hàng bán" stroke="#16a34a" strokeWidth={2.5} dot={false} connectNulls />
      </ComposedChart></ResponsiveContainer></div>
    </div>
  );
}

const renderCustomDot = (dataKey) => (props) => {
  const { cx, cy, payload, index, stroke } = props;
  const val = payload[dataKey];
  if (val !== null && val !== undefined && (Number(val) > 0 || index === 0)) {
    return <circle key={`dot-${dataKey}-${index}`} cx={cx} cy={cy} r={4} fill={stroke} stroke="#ffffff" strokeWidth={2} />;
  }
  return null;
};

/* Custom Financial AreaChart Tooltip */
const CustomFinancialTooltip = ({ active, payload, label }) => {
  if (active && payload && payload.length) {
    const dStr = label || payload[0]?.payload?.date || '';
    return (
      <div
        style={{
          background: '#ffffff',
          border: '1px solid #e2e8f0',
          borderRadius: '12px',
          padding: '12px 16px',
          boxShadow: '0 10px 25px rgba(0,0,0,0.12)',
          fontFamily: 'system-ui, -apple-system, sans-serif',
          minWidth: '200px',
        }}
      >
        <div style={{ fontSize: '13px', fontWeight: '800', color: '#64748b', marginBottom: '8px', borderBottom: '1px solid #f1f5f9', paddingBottom: '4px' }}>
          📅 Ngày: <strong style={{ color: '#0f172a' }}>{dStr}</strong>
        </div>
        {payload.map((entry, idx) => (
          <div key={idx} style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: '12px', marginTop: '4px' }}>
            <span style={{ fontSize: '12.5px', fontWeight: '700', color: entry.color }}>
              {entry.name}:
            </span>
            <strong style={{ fontSize: '13px', fontWeight: '900', color: entry.color }}>
              {formatVndText(entry.value || 0)}
            </strong>
          </div>
        ))}
      </div>
    );
  }
  return null;
};

/* Custom Quantity LineChart Tooltip */
const CustomQuantityTooltip = ({ active, payload, label }) => {
  if (active && payload && payload.length) {
    const dStr = label || payload[0]?.payload?.date || '';
    return (
      <div
        style={{
          background: '#ffffff',
          border: '1px solid #e2e8f0',
          borderRadius: '12px',
          padding: '12px 16px',
          boxShadow: '0 10px 25px rgba(0,0,0,0.12)',
          fontFamily: 'system-ui, -apple-system, sans-serif',
          minWidth: '210px',
        }}
      >
        <div style={{ fontSize: '13px', fontWeight: '800', color: '#64748b', marginBottom: '8px', borderBottom: '1px solid #f1f5f9', paddingBottom: '4px' }}>
          📅 Ngày: <strong style={{ color: '#0f172a' }}>{dStr}</strong>
        </div>
        {payload.map((entry, idx) => (
          <div key={idx} style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: '12px', marginTop: '4px' }}>
            <span style={{ fontSize: '12.5px', fontWeight: '700', color: entry.color }}>
              {entry.name}:
            </span>
            <strong style={{ fontSize: '13px', fontWeight: '900', color: entry.color }}>
              {entry.value || 0} Sản phẩm
            </strong>
          </div>
        ))}
      </div>
    );
  }
  return null;
};

/* Custom High-End Pie Tooltip */
const CustomPieTooltip = ({ active, payload }) => {
  if (active && payload && payload.length) {
    const data = payload[0].payload;
    const name = data.label || data.method || data.name || 'Chi tiết';
    const amount = Number(data.amount || data.value || data.revenue || 0);
    const count = data.count ?? data.orderCount ?? data.transactionCount ?? null;
    const color = payload[0].color || '#ea580c';

    return (
      <div
        style={{
          background: '#ffffff',
          border: '1px solid #e2e8f0',
          borderRadius: '12px',
          padding: '12px 16px',
          boxShadow: '0 10px 25px rgba(0,0,0,0.12)',
          fontFamily: 'system-ui, -apple-system, sans-serif',
          minWidth: '180px',
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '6px' }}>
          <span style={{ width: '10px', height: '10px', borderRadius: '50%', background: color, display: 'inline-block' }}></span>
          <strong style={{ fontSize: '14.5px', color: '#0f172a', fontWeight: '800' }}>{name}</strong>
        </div>
        <div style={{ fontSize: '13.5px', color: '#16a34a', fontWeight: '800', marginBottom: '4px' }}>
          Doanh thu: {formatVndText(amount)}
        </div>
        {count !== null && count !== undefined && (
          <div style={{ fontSize: '12.5px', color: '#64748b', fontWeight: '700' }}>
            Số lượng: <strong style={{ color: '#ea580c' }}>{count} {data.method ? 'đơn hàng' : 'lượt'}</strong>
          </div>
        )}
      </div>
    );
  }
  return null;
};

/* Crisp Outer Label next to Pie Slices */
const renderCustomPieLabel = (props) => {
  const RADIAN = Math.PI / 180;
  const { cx, cy, midAngle, outerRadius, percent, payload } = props;
  
  if (!percent || percent <= 0) return null;

  const radius = outerRadius + 22;
  const x = cx + radius * Math.cos(-midAngle * RADIAN);
  const y = cy + radius * Math.sin(-midAngle * RADIAN);
  const textAnchor = x > cx ? 'start' : 'end';
  const name = payload.label || payload.method || payload.name || '';
  const pctStr = `${(percent * 100).toFixed(1)}%`;

  return (
    <g>
      <text
        x={x}
        y={y - 4}
        textAnchor={textAnchor}
        fill="#0f172a"
        fontSize={12.5}
        fontWeight={800}
        fontFamily="system-ui, -apple-system, sans-serif"
      >
        {name}
      </text>
      <text
        x={x}
        y={y + 12}
        textAnchor={textAnchor}
        fill="#ea580c"
        fontSize={12}
        fontWeight={800}
        fontFamily="system-ui, -apple-system, sans-serif"
      >
        {pctStr}
      </text>
    </g>
  );
};

/* Sleek Hover Active Slice Expansion */
const renderActiveShape = (props) => {
  const { cx, cy, innerRadius, outerRadius, startAngle, endAngle, fill } = props;
  return (
    <g>
      <Sector
        cx={cx}
        cy={cy}
        innerRadius={innerRadius}
        outerRadius={outerRadius + 8}
        startAngle={startAngle}
        endAngle={endAngle}
        fill={fill}
        style={{ filter: 'drop-shadow(0 6px 14px rgba(0,0,0,0.25))' }}
      />
      <Sector
        cx={cx}
        cy={cy}
        startAngle={startAngle}
        endAngle={endAngle}
        innerRadius={outerRadius + 11}
        outerRadius={outerRadius + 14}
        fill={fill}
        opacity={0.5}
      />
    </g>
  );
};

/* ═══════════════════════════════════════════════════════════════════
   MAIN MASTER OVERVIEW COMPONENT
   ═══════════════════════════════════════════════════════════════════ */
function AdminStatisticsPage() {
  const [selectedRange, setSelectedRange] = useState('LAST_30_DAYS');
  const [dates, setDates] = useState(getInitialDates('LAST_30_DAYS'));

  // Overview stats
  const [overview, setOverview] = useState({
    totalProfit: 0,
    totalRevenue: 0,
    productsSold: 0,
    productsImported: 0,
    alerts: {
      pendingPayment: 0,
      pendingReturn: 0,
      lowStock: 0,
      cancelledOrder: 0,
      lowRatingCount: 0,
      slowSelling: 0,
    },
  });

  const dataMode = 'REAL'; // Backend statistics deliberately include CUSTOMER orders only.
  const [mainChartData, setMainChartData] = useState([]);
  const [inventoryCostSummary, setInventoryCostSummary] = useState({
    currentSellingPrice: 0,
    currentMac: 0,
    latestImportPrice: 0,
    stockQuantity: 0,
    grossMarginPercent: 0,
    available: false,
    hasStock: false,
    hasCompletedImport: false,
    variantCount: 0,
  });
  const [macProducts, setMacProducts] = useState([]);
  const [selectedMacVariant, setSelectedMacVariant] = useState('ALL');
  const [reportMeta, setReportMeta] = useState({ generatedAt: null, period: null, available: false });
  const [orderComparisonData, setOrderComparisonData] = useState([]);
  const [recentOrders, setRecentOrders] = useState([]);
  const [lowRatingReviews, setLowRatingReviews] = useState([]);

  // Kept Specialty Datasets
  const [revenueCategory, setRevenueCategory] = useState([]);
  const [paymentMethods, setPaymentMethods] = useState([]);
  const [topProducts, setTopProducts] = useState([]);
  const [topCustomers, setTopCustomers] = useState([]);

  // 3D Donut Hover States
  const [activeCategory, setActiveCategory] = useState(0);
  const [activePayment, setActivePayment] = useState(0);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    getAllAdminProducts({ sort: 'name,asc' })
      .then((res) => setMacProducts(Array.isArray(res) ? res : []))
      .catch(() => setMacProducts([]));
  }, []);

  useEffect(() => {
    loadDashboardData();
  }, [dates, selectedMacVariant]);

  function handleRangeClick(code) {
    setSelectedRange(code);
    setDates(getInitialDates(code));
  }

  async function loadDashboardData() {
    setLoading(true);
    setError('');

    const params = {
      from: dates.from,
      to: dates.to,
      timezone: 'Asia/Ho_Chi_Minh',
      groupBy: 'AUTO',
      topLimit: 10,
      compareWithPreviousPeriod: true,
    };

    try {
      const [
        overviewRes,
        allOrdersRes,
        importsRes,
        categoryRes,
        paymentRes,
        topProdRes,
        topCustRes,
        slowProdRes,
        inventorySnapshotRes,
        inventoryCostSummaryRes,
        reviewsRes,
        lowRatingOneRes,
        lowRatingTwoRes,
        customerReturnsRes,
        operationalBadgesRes,
      ] = await Promise.allSettled([
        getStatisticsOverview(params),
        getAdminOrders({ page: 0, size: 8, sort: 'createdAt,desc' }),
        getImports({ page: 0, size: 1, sort: 'createdAt,desc' }),
        getRevenueByCategory(params),
        getRevenueByPaymentMethod(params),
        getTopSellingProducts({ from: dates.from, to: dates.to, limit: 10 }),
        getTopSpendingCustomers(params),
        getSlowSellingProducts({ from: dates.from, to: dates.to, timezone: params.timezone, groupBy: params.groupBy, limit: 50, maxUnits: 5 }),
        getInventorySnapshot({ lowStockThreshold: 10 }),
        getInventoryCostSummary(selectedMacVariant),
        getAdminReviews({ page: 0, size: 1 }),
        getAdminReviews({ rating: 1, page: 0, size: 1 }),
        getAdminReviews({ rating: 2, page: 0, size: 1 }),
        getCustomerReturns({ status: 'REQUESTED', page: 0, size: 1, sort: 'createdAt,desc' }),
        getAdminMenuBadges(),
      ]);

      const overviewData = overviewRes.status === 'fulfilled' ? overviewRes.value || {} : {};
      const trendData = Array.isArray(overviewData.revenueTrend) ? overviewData.revenueTrend : [];
      setReportMeta({
        generatedAt: overviewData.generatedAt || null,
        period: overviewData.period || null,
        available: overviewRes.status === 'fulfilled',
      });
      if (overviewRes.status !== 'fulfilled') {
        setError('Không thể tải báo cáo tổng hợp. Các KPI theo kỳ được ẩn để tránh hiển thị số 0 không đúng.');
      }
      const allOrdersList = allOrdersRes.status === 'fulfilled' ? (allOrdersRes.value?.content || (Array.isArray(allOrdersRes.value) ? allOrdersRes.value : [])) : [];
      const allImportsList = importsRes.status === 'fulfilled' ? (importsRes.value?.content || (Array.isArray(importsRes.value) ? importsRes.value : [])) : [];
      const slowSellingProducts = slowProdRes.status === 'fulfilled' ? (slowProdRes.value?.content || (Array.isArray(slowProdRes.value) ? slowProdRes.value : [])) : [];
      const inventoryList = inventorySnapshotRes.status === 'fulfilled' ? (inventorySnapshotRes.value?.content || (Array.isArray(inventorySnapshotRes.value) ? inventorySnapshotRes.value : [])) : [];
      if (inventoryCostSummaryRes.status === 'fulfilled') {
        setInventoryCostSummary({
          currentSellingPrice: Number(inventoryCostSummaryRes.value?.currentSellingPrice || 0),
          currentMac: Number(inventoryCostSummaryRes.value?.currentMac || 0),
          latestImportPrice: Number(inventoryCostSummaryRes.value?.latestImportPrice || 0),
          stockQuantity: Number(inventoryCostSummaryRes.value?.stockQuantity || 0),
          grossMarginPercent: Number(inventoryCostSummaryRes.value?.grossMarginPercent || 0),
          available: true,
          hasStock: Boolean(inventoryCostSummaryRes.value?.hasStock),
          hasCompletedImport: Boolean(inventoryCostSummaryRes.value?.hasCompletedImport),
          variantCount: Number(inventoryCostSummaryRes.value?.variantCount || 0),
        });
      } else {
        setInventoryCostSummary({
          currentSellingPrice: 0,
          currentMac: 0,
          latestImportPrice: 0,
          stockQuantity: 0,
          grossMarginPercent: 0,
          available: false,
          hasStock: false,
          hasCompletedImport: false,
          variantCount: 0,
        });
        setError((current) => current || 'Không thể tải dữ liệu giá vốn và tồn kho hiện tại. Vui lòng kiểm tra phiên bản backend đang chạy.');
      }
      const allReviewsList = reviewsRes.status === 'fulfilled' ? (reviewsRes.value?.content || (Array.isArray(reviewsRes.value) ? reviewsRes.value : [])) : [];
      const lowRatingReviews = allReviewsList.filter((r) => Number(r.rating || r.stars || 5) < 3);
      const lowRatingCountFromBackend =
        (lowRatingOneRes.status === 'fulfilled' ? Number(lowRatingOneRes.value?.totalElements || 0) : 0) +
        (lowRatingTwoRes.status === 'fulfilled' ? Number(lowRatingTwoRes.value?.totalElements || 0) : 0);
      const customerReturnsList = customerReturnsRes.status === 'fulfilled'
        ? (customerReturnsRes.value?.content || (Array.isArray(customerReturnsRes.value) ? customerReturnsRes.value : []))
        : [];
      const operationalBadges = operationalBadgesRes.status === 'fulfilled'
        ? operationalBadgesRes.value || {}
        : {};

      const getStatusCode = (st) => (typeof st === 'object' ? (st?.code || st?.name || st?.status || '') : String(st || '')).toUpperCase();

      // Detect internal test order created by ADMIN, MANAGER, or STAFF
      const isInternalTestOrder = (o) => {
        if (!o) return false;

        // 1. If buyer user role is CUSTOMER, it is ALWAYS a real business customer order!
        const roleStr = String(
          o.user?.role ||
          (Array.isArray(o.user?.roles) ? o.user.roles.join(',') : '') ||
          o.customerRole ||
          o.role ||
          o.userRole ||
          o.customer?.role ||
          ''
        ).toUpperCase();

        if (roleStr === 'CUSTOMER' || roleStr.endsWith('CUSTOMER')) {
          return false;
        }

        // 2. Check direct user ID of the buyer (Seed Admin/Staff IDs 1 and 2 only)
        const uid = Number(o.userId || o.user?.id || o.customerId || 0);
        if (uid === 1 || uid === 2) {
          return true;
        }

        // 3. Internal staff roles (ADMIN, MANAGER, STAFF)
        if (['ADMIN', 'MANAGER', 'STAFF', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_STAFF'].some((r) => roleStr.includes(r))) {
          return true;
        }

        // 4. Check explicit test note on order itself
        const note = String(o.note || o.customerNote || o.description || '').toLowerCase();
        if (note.includes('đơn test') || note.includes('thử nghiệm nội bộ') || note.includes('[test]')) {
          return true;
        }

        return false;
      };

      // Filter orders strictly based on dataMode ('REAL' vs 'TEST')
      const modeOrdersList = allOrdersList.filter((o) => {
        const isTest = isInternalTestOrder(o);
        return dataMode === 'TEST' ? isTest : !isTest;
      });

      setRecentOrders(modeOrdersList.slice(0, 8));

      // Strictly filter orders and imports within the selected date range [dates.from, dates.to]
      const isDateInRange = (dateVal) => {
        if (!dateVal) return false;
        const d = new Date(dateVal);
        if (isNaN(d.getTime())) return false;
        const yyyy = d.getFullYear();
        const mm = String(d.getMonth() + 1).padStart(2, '0');
        const dd = String(d.getDate()).padStart(2, '0');
        const iso = `${yyyy}-${mm}-${dd}`;
        return iso >= dates.from && iso <= dates.to;
      };

      const rangeOrdersList = modeOrdersList.filter((o) => isDateInRange(getOrderRevenueDate(o)));
      const rangeImportsList = allImportsList.filter((imp) => isDateInRange(imp.completedAt || imp.updatedAt || imp.createdAt || imp.importDate));

      const getKpiVal = (code) => {
        if (!Array.isArray(overviewData.kpis)) return 0;
        const found = overviewData.kpis.find((k) => k.code === code);
        return found ? Number(found.value || 0) : 0;
      };

      const getKpiChange = (code) => {
        if (!Array.isArray(overviewData.kpis)) return null;
        const found = overviewData.kpis.find((k) => k.code === code);
        return found && Number.isFinite(Number(found.changePercent)) ? Number(found.changePercent) : null;
      };

      // Calculate strictly from real orders and imports without artificial dummy estimates
      const revenueStatusCodes = ['COMPLETED', 'PARTIALLY_REFUNDED', 'FULLY_REFUNDED'];
      const validOrders = rangeOrdersList.filter((o) => revenueStatusCodes.includes(getStatusCode(o.status)));

      const fallbackRevenue = validOrders.reduce((sum, o) => sum + Number(o.totalAmount || o.grandTotal || 0), 0);
      const backendRevenue = getKpiVal('NET_REVENUE');
      const revenue = dataMode === 'REAL' && overviewRes.status === 'fulfilled' ? backendRevenue : fallbackRevenue;
      const backendProfit = trendData.reduce((sum, point) => sum + Number(point.grossProfit || 0), 0);
      const profit = dataMode === 'REAL' && overviewRes.status === 'fulfilled' ? backendProfit : 0;
      const fallbackSoldQuantity = validOrders.reduce(
        (sum, order) => sum + (order.items || []).reduce((itemSum, item) => itemSum + Number(item.quantity || 0), 0),
        0
      );
      const productsSold = dataMode === 'REAL' && overviewRes.status === 'fulfilled'
        ? getKpiVal('SOLD_QUANTITY')
        : fallbackSoldQuantity;

      // Import metrics MUST come strictly from real import receipts
      const completedRangeImports = rangeImportsList.filter((imp) => getStatusCode(imp.status) === 'COMPLETED');
      const fallbackImportCost = completedRangeImports.reduce(
        (sum, imp) => sum + Number(imp.totalCost || imp.totalAmount || imp.grandTotal || 0), 0
      );
      const fallbackImportedQuantity = completedRangeImports.reduce(
        (sum, imp) => sum + (imp.items || []).reduce((itemSum, item) => itemSum + Number(item.quantity || 0), 0), 0
      );
      const importCost = dataMode === 'REAL' && overviewRes.status === 'fulfilled'
        ? trendData.reduce((sum, point) => sum + Number(point.importCost || 0), 0)
        : fallbackImportCost;
      const productsImported = dataMode === 'REAL' && overviewRes.status === 'fulfilled'
        ? trendData.reduce((sum, point) => sum + Number(point.importedQuantity || 0), 0)
        : fallbackImportedQuantity;

      const cancelledCountFromOverview = Array.isArray(overviewData.orderStatus)
        ? (overviewData.orderStatus.find((s) => getStatusCode(s.code || s.label || s.status || s.name) === 'CANCELLED')?.count || 0)
        : 0;

      const pendingCountFromOverview = Array.isArray(overviewData.paymentStatus)
        ? (overviewData.paymentStatus.find((s) => getStatusCode(s.code || s.label || s.status || s.name) === 'PENDING')?.count || 0)
        : 0;
      const pendingReturnCount = customerReturnsRes.status === 'fulfilled' && Number.isFinite(Number(customerReturnsRes.value?.totalElements))
        ? Number(customerReturnsRes.value.totalElements)
        : customerReturnsList.filter((request) => getStatusCode(request.status) === 'REQUESTED').length;

      // Calculate Previous Period Date Range based on user's exact comparison rules
      const calcPrevDateRange = () => {
        const fromD = new Date(dates.from);
        const toD = new Date(dates.to);
        const diffMs = toD.getTime() - fromD.getTime();
        const numDays = Math.max(1, Math.round(diffMs / (1000 * 60 * 60 * 24)) + 1);

        if (selectedRange === 'THIS_MONTH') {
          const prevMonthFrom = new Date(fromD.getFullYear(), fromD.getMonth() - 1, 1);
          const prevMonthTo = new Date(fromD.getFullYear(), fromD.getMonth(), 0);
          return {
            from: prevMonthFrom.toISOString().split('T')[0],
            to: prevMonthTo.toISOString().split('T')[0],
          };
        }

        if (selectedRange === 'LAST_MONTH') {
          const twoMonthsAgoFrom = new Date(fromD.getFullYear(), fromD.getMonth() - 1, 1);
          const twoMonthsAgoTo = new Date(fromD.getFullYear(), fromD.getMonth(), 0);
          return {
            from: twoMonthsAgoFrom.toISOString().split('T')[0],
            to: twoMonthsAgoTo.toISOString().split('T')[0],
          };
        }

        const pTo = new Date(fromD.getTime() - 24 * 60 * 60 * 1000);
        const pFrom = new Date(fromD.getTime() - numDays * 24 * 60 * 60 * 1000);
        return {
          from: pFrom.toISOString().split('T')[0],
          to: pTo.toISOString().split('T')[0],
        };
      };

      const prevRange = calcPrevDateRange();
      let previousTrendData = [];
      if (dataMode === 'REAL') {
        try {
          const result = await getRevenueTrend({
            ...params,
            from: prevRange.from,
            to: prevRange.to,
          });
          previousTrendData = Array.isArray(result) ? result : [];
        } catch (previousTrendError) {
          console.warn('Không thể tải dữ liệu kỳ trước:', previousTrendError);
        }
      }

      const prevOrdersList = modeOrdersList.filter((o) => {
        if (!revenueStatusCodes.includes(getStatusCode(o.status))) return false;
        const dVal = getOrderRevenueDate(o);
        if (!dVal) return false;
        const d = new Date(dVal);
        if (isNaN(d.getTime())) return false;
        const iso = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
        return iso >= prevRange.from && iso <= prevRange.to;
      });

      const prevImportsList = allImportsList.filter((imp) => {
        const dVal = imp.completedAt || imp.updatedAt || imp.createdAt || imp.importDate;
        if (!dVal) return false;
        const d = new Date(dVal);
        if (isNaN(d.getTime())) return false;
        const iso = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
        return iso >= prevRange.from && iso <= prevRange.to;
      });

      const fallbackPrevRevenue = prevOrdersList.reduce((sum, o) => sum + Number(o.totalAmount || o.grandTotal || 0), 0);
      const prevRevenue = previousTrendData.length
        ? previousTrendData.reduce((sum, point) => sum + Number(point.netRevenue || 0), 0)
        : fallbackPrevRevenue;
      const prevProfit = previousTrendData.reduce((sum, point) => sum + Number(point.grossProfit || 0), 0);
      const completedPrevImports = prevImportsList.filter((imp) => getStatusCode(imp.status) === 'COMPLETED');
      const prevCost = previousTrendData.length
        ? previousTrendData.reduce((sum, point) => sum + Number(point.importCost || 0), 0)
        : completedPrevImports.reduce((sum, imp) => sum + Number(imp.totalCost || imp.totalAmount || imp.grandTotal || 0), 0);
      const prevSales = previousTrendData.length
        ? previousTrendData.reduce((sum, point) => sum + Number(point.soldQuantity || 0), 0)
        : prevOrdersList.reduce((sum, order) => sum + (order.items || []).reduce((itemSum, item) => itemSum + Number(item.quantity || 0), 0), 0);
      const prevImports = previousTrendData.length
        ? previousTrendData.reduce((sum, point) => sum + Number(point.importedQuantity || 0), 0)
        : completedPrevImports.reduce((sum, imp) => sum + (imp.items || []).reduce((itemSum, item) => itemSum + Number(item.quantity || 0), 0), 0);

      // Helper to calculate exact growth % comparing Current Period vs Previous Period
      const calcGrowthRate = (currVal, prevVal) => {
        const c = Number(currVal || 0);
        const p = Number(prevVal || 0);

        if (c === 0 && p === 0) return '0%';
        if (p === 0 && c > 0) return '+100%';
        if (p > 0 && c === 0) return '-100%';

        const pct = Math.round(((c - p) / p) * 100);
        if (pct === 0) return '0%';
        return pct > 0 ? `+${pct}%` : `${pct}%`;
      };

      const computedGrowthPercents = {
        cost: calcGrowthRate(importCost, prevCost),
        profit: calcGrowthRate(profit, prevProfit),
        revenue: getKpiChange('NET_REVENUE') == null
          ? calcGrowthRate(revenue, prevRevenue)
          : `${getKpiChange('NET_REVENUE') > 0 ? '+' : ''}${getKpiChange('NET_REVENUE')}%`,
        sales: getKpiChange('SOLD_QUANTITY') == null
          ? calcGrowthRate(productsSold, prevSales)
          : `${getKpiChange('SOLD_QUANTITY') > 0 ? '+' : ''}${getKpiChange('SOLD_QUANTITY')}%`,
        imports: calcGrowthRate(productsImported, prevImports),
      };

      setOverview({
        totalProfit: profit,
        totalRevenue: revenue,
        importCost,
        productsSold,
        productsImported,
        growthPercents: computedGrowthPercents,
        alerts: {
          pendingPayment: dataMode === 'REAL' && operationalBadgesRes.status === 'fulfilled'
            ? Number(operationalBadges.pendingPayments || 0)
            : dataMode === 'REAL' && overviewRes.status === 'fulfilled'
              ? pendingCountFromOverview
              : rangeOrdersList.filter((o) => {
                  const st = getStatusCode(o.status);
                  return st.includes('PENDING') || st.includes('UNPAID') || st.includes('CREATED') || st.includes('AWAITING');
                }).length,
          pendingReturn: dataMode === 'REAL' && operationalBadgesRes.status === 'fulfilled'
            ? Number(operationalBadges.pendingCustomerReturns || 0)
            : pendingReturnCount,
          lowStock: dataMode === 'REAL' && operationalBadgesRes.status === 'fulfilled'
            ? Number(operationalBadges.lowStockVariants || 0)
            : Number(overviewData.inventory?.lowStockVariantCount || 0),
          cancelledOrder: dataMode === 'REAL' && operationalBadgesRes.status === 'fulfilled'
            ? Number(operationalBadges.cancelledOrders || 0)
            : dataMode === 'REAL' && overviewRes.status === 'fulfilled'
              ? cancelledCountFromOverview
              : rangeOrdersList.filter((o) => getStatusCode(o.status) === 'CANCELLED').length,
          lowRatingCount: dataMode === 'REAL' && operationalBadgesRes.status === 'fulfilled'
            ? Number(operationalBadges.lowRatingReviews || 0)
            : lowRatingCountFromBackend || lowRatingReviews.length,
          slowSelling: dataMode === 'REAL' && operationalBadgesRes.status === 'fulfilled'
            ? Number(operationalBadges.slowSellingProducts || 0)
            : slowSellingProducts.length,
        },
      });

      // Build REAL trend series strictly from Backend API data or fallback orders
      const datesArray = generateDateSeries(dates.from, dates.to);

      const toDayMonth = (dateObjOrStr) => {
        if (!dateObjOrStr) return '';
        const d = new Date(dateObjOrStr);
        if (isNaN(d.getTime())) return '';
        const m = String(d.getMonth() + 1).padStart(2, '0');
        const day = String(d.getDate()).padStart(2, '0');
        return `${day}/${m}`;
      };

      let series = [];
      if (trendData.length > 0 && dataMode === 'REAL') {
        series = trendData.map((item) => {
          const rev = Number(item.netRevenue || 0);
          const prof = Number(item.grossProfit || 0);
          const cost = Number(item.importCost || 0);
          const sold = Number(item.soldQuantity || 0);
          const imported = Number(item.importedQuantity || 0);

          return {
            date: item.period || item.date || item.label || 'Ngày',
            'Tổng lợi nhuận': prof,
            'Tổng doanh thu': rev,
            'Chi phí nhập hàng': cost,
            'Giá vốn hàng bán': Number(item.costOfGoodsSold || 0),
            'Tổng SP bán ra': sold,
            'Tổng SP nhập vào': imported,
            'Tổng đơn hàng tạo': Number(item.createdOrderCount || 0),
            'Đơn thanh toán thành công': Number(item.orderCount || 0),
          };
        });
      } else {
        series = datesArray.map((dStr) => {
          const matchedOrders = modeOrdersList.filter((o) => {
            if (!revenueStatusCodes.includes(getStatusCode(o.status))) return false;
            const oDateStr = toDayMonth(getOrderRevenueDate(o));
            return oDateStr === dStr;
          });

          const matchedImports = allImportsList.filter((imp) => {
            const impDateStr = toDayMonth(imp.completedAt || imp.updatedAt || imp.createdAt || imp.importDate);
            return impDateStr === dStr;
          });

          const rev = matchedOrders.reduce((sum, o) => sum + Number(o.totalAmount || o.grandTotal || 0), 0);
          const sold = matchedOrders.reduce((sum, order) => sum + (order.items || []).reduce((itemSum, item) => itemSum + Number(item.quantity || 0), 0), 0);
          const prof = 0;
          const completedImports = matchedImports.filter((imp) => getStatusCode(imp.status) === 'COMPLETED');
          const cost = completedImports.reduce((sum, imp) => sum + Number(imp.totalCost || imp.totalAmount || imp.grandTotal || 0), 0);
          const imported = completedImports.reduce((sum, imp) => sum + (imp.items || []).reduce((itemSum, item) => itemSum + Number(item.quantity || 0), 0), 0);

          return {
            date: dStr,
            'Tổng lợi nhuận': prof,
            'Tổng doanh thu': rev,
            'Chi phí nhập hàng': cost,
            'Giá vốn hàng bán': 0,
            'Tổng SP bán ra': sold,
            'Tổng SP nhập vào': imported,
            'Tổng đơn hàng tạo': matchedOrders.length,
            'Đơn thanh toán thành công': matchedOrders.length,
          };
        });
      }
      setMainChartData(series);
      // Both series now come from backend aggregation, so they are not limited by an admin page size.
      const comparisonSeries = series.slice(-8).map((point) => ({
        date: point.date,
        'Tổng đơn hàng tạo': Number(point['Tổng đơn hàng tạo'] || 0),
        'Đơn thanh toán thành công': Number(point['Đơn thanh toán thành công'] || 0),
      }));
      setOrderComparisonData(comparisonSeries);

      // Compute Payment Methods breakdown combining backend API and modeOrdersList
      const paymentMap = {};
      
      // 1. Fill from Backend API if available
      const hasBackendPaymentData = dataMode === 'REAL' && paymentRes.status === 'fulfilled' && Array.isArray(paymentRes.value);
      if (hasBackendPaymentData) {
        paymentRes.value.forEach((p) => {
          let rawM = String(p.method || p.paymentMethod || p.label || 'VNPAY').toUpperCase();
          const label = rawM.includes('COD') || rawM.includes('TIỀN MẶT')
            ? 'COD (Thanh toán khi nhận hàng)'
            : (rawM.includes('VNPAY') ? 'VNPAY (Thanh toán online)' : rawM);
          const amt = Number(p.amount || p.revenue || p.value || p.totalAmount || 0);
          const cnt = Number(p.count ?? p.orderCount ?? p.transactionCount ?? 0);
          paymentMap[label] = { method: label, label: label, name: label, amount: amt, count: cnt };
        });
      }

      // 2. Guarantee ALL order payment methods (COD and VNPAY) from modeOrdersList are present
      const activeOrdersList = validOrders;
      if (!hasBackendPaymentData) activeOrdersList.forEach((o) => {
        let rawM = String(o.paymentMethod || o.paymentMethodName || o.payment?.method || 'COD').toUpperCase();
        const label = rawM.includes('VNPAY')
          ? 'VNPAY (Thanh toán online)'
          : (rawM.includes('COD') || rawM.includes('CASH') || rawM.includes('TIỀN MẶT') ? 'COD (Thanh toán khi nhận hàng)' : 'Ví MoMo');
        const amt = Number(o.totalAmount || o.grandTotal || 0);

        if (!paymentMap[label]) {
          paymentMap[label] = { method: label, label: label, name: label, amount: 0, count: 0 };
        }
        paymentMap[label].amount += amt;
        paymentMap[label].count += 1;
      });

      setPaymentMethods(Object.values(paymentMap));

      // Compute Revenue Category breakdown combining backend API and modeOrdersList
      const categoryMap = {};
      const hasBackendCategoryData = dataMode === 'REAL' && categoryRes.status === 'fulfilled' && Array.isArray(categoryRes.value);
      if (hasBackendCategoryData) {
        categoryRes.value.forEach((c) => {
          const catName = String(c.label || c.name || c.categoryName || 'Danh mục khác');
          const amt = Number(c.amount || c.revenue || c.totalAmount || c.value || 0);
          const cnt = Number(c.count ?? c.quantity ?? c.soldQuantity ?? 0);
          categoryMap[catName] = { label: catName, name: catName, amount: amt, count: cnt };
        });
      }

      if (!hasBackendCategoryData) activeOrdersList.forEach((o) => {
        if (Array.isArray(o.items) && o.items.length > 0) {
          o.items.forEach((it) => {
            const catName = String(it.categoryName || it.category?.name || it.productCategory || 'Đồ chơi sáng tạo');
            const itemTotal = Number(it.subtotal || it.totalPrice || (it.price * (it.quantity || 1)) || 0);
            if (!categoryMap[catName]) {
              categoryMap[catName] = { label: catName, name: catName, amount: 0, count: 0 };
            }
            categoryMap[catName].amount += itemTotal;
            categoryMap[catName].count += Number(it.quantity || 0);
          });
        }
      });

      setRevenueCategory(Object.values(categoryMap));

      setTopProducts(topProdRes.status === 'fulfilled' && Array.isArray(topProdRes.value) ? topProdRes.value : []);

      const rawTopCust = topCustRes.status === 'fulfilled' && Array.isArray(topCustRes.value) ? topCustRes.value : [];
      const filteredTopCust = rawTopCust.filter((cust) => {
        const text = String((cust.label || '') + ' ' + (cust.customerName || '') + ' ' + (cust.fullName || '') + ' ' + (cust.email || '')).toLowerCase();
        const isTest = text.includes('admin') || text.includes('manager') || text.includes('staff') || text.includes('test');
        return dataMode === 'TEST' ? isTest : !isTest;
      });
      setTopCustomers(filteredTopCust);
    } catch (err) {
      setError(err?.message || 'Không thể tải dữ liệu thống kê.');
    } finally {
      setLoading(false);
    }
  }

  const growthPercents = useMemo(() => {
    return overview.growthPercents || { cost: '0%', profit: '0%', revenue: '0%', sales: '0%', imports: '0%' };
  }, [overview]);

  const moneyTicks = useMemo(() => getMoneyTicks(mainChartData), [mainChartData]);
  const productTicks = useMemo(() => getProductTicks(mainChartData), [mainChartData]);
  const hasPeriodActivity = useMemo(() => (
    Number(overview.totalRevenue || 0) > 0
    || Number(overview.totalProfit || 0) !== 0
    || Number(overview.importCost || 0) > 0
    || Number(overview.productsSold || 0) > 0
    || Number(overview.productsImported || 0) > 0
  ), [overview]);
  return (
    <section className="admin-statistics-page" style={{ padding: '24px', background: '#f8fafc', minHeight: '100vh', fontFamily: 'system-ui, -apple-system, sans-serif' }}>
      
      {/* HEADER SECTION WITH MODE TOGGLE */}
      <div style={{ background: 'linear-gradient(135deg, #fff8f3 0%, #fff1f2 100%)', border: '1px solid #ffedd5', padding: '16px 24px', borderRadius: '16px', marginBottom: '16px', boxShadow: '0 4px 12px rgba(234,88,12,0.04)', display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '16px' }}>
        <div>
          <h1 style={{ fontSize: '24px', fontWeight: 900, color: '#9a3412', margin: 0, letterSpacing: '-0.3px', textTransform: 'uppercase' }}>
            Thống kê quản trị
          </h1>
          <div style={{ fontSize: '13px', color: '#15803d', fontWeight: '800', marginTop: '4px', display: 'flex', alignItems: 'center', gap: '6px' }}>
            <span>Chỉ tổng hợp giao dịch của khách hàng; dữ liệu thử của Admin/Staff không được tính vào KPI.</span>
          </div>
        </div>

        <span style={{ padding: '8px 12px', borderRadius: 999, background: '#ecfdf5', color: '#15803d', border: '1px solid #bbf7d0', fontSize: 12, fontWeight: 900 }}>Dữ liệu kinh doanh thực</span>
      </div>

      {/* TIME RANGE BAR */}
      <TimeRangeBar dates={dates} setDates={setDates} selectedRange={selectedRange} handleRangeClick={handleRangeClick} />

      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: 12, flexWrap: 'wrap', margin: '-8px 0 16px', padding: '10px 14px', border: '1px solid #e2e8f0', borderRadius: 12, background: '#fff', color: '#64748b', fontSize: 12 }}>
        <span>
          {reportMeta.available
            ? `Dữ liệu ${reportMeta.period?.from || dates.from} → ${reportMeta.period?.to || dates.to} · Nhóm ${reportMeta.period?.appliedGroupBy || 'AUTO'}${reportMeta.period?.groupByAdjusted ? ' (hệ thống tự điều chỉnh)' : ''} · Cập nhật ${formatDateTime(reportMeta.generatedAt)}`
            : 'Chưa xác nhận được thời điểm và phạm vi dữ liệu từ backend.'}
        </span>
        <button type="button" onClick={loadDashboardData} disabled={loading} style={{ border: '1px solid #fed7aa', background: '#fff7ed', color: '#c2410c', borderRadius: 9, padding: '7px 12px', fontWeight: 800, cursor: loading ? 'wait' : 'pointer', opacity: loading ? 0.65 : 1 }}>
          {loading ? 'Đang đồng bộ…' : 'Làm mới dữ liệu'}
        </button>
      </div>

      {error && (
        <div style={{ padding: '12px 16px', background: '#fef2f2', border: '1px solid #fecaca', borderRadius: '10px', color: '#dc2626', fontSize: '13px', marginBottom: '16px' }}>
          ⚠️ {error}
        </div>
      )}

      {!loading && reportMeta.available && !hasPeriodActivity && (
        <div style={{ padding: '12px 16px', background: '#fffbeb', border: '1px solid #fde68a', borderRadius: '10px', color: '#92400e', fontSize: '13px', marginBottom: '16px', fontWeight: 700 }}>
          Khoảng thời gian đã chọn chưa phát sinh đơn hoàn tất hoặc phiếu nhập hoàn tất. Các giá trị 0 bên dưới là kết quả hợp lệ, không phải lỗi tải dữ liệu.
        </div>
      )}

      {loading ? (
        <div style={{ textAlign: 'center', padding: '60px 0', color: '#94a3b8', fontSize: '15px' }}>
          ⏳ Đang tải dữ liệu thống kê từ hệ thống...
        </div>
      ) : (
        <>
          {/* ROW 1: 5 SPARKLINE KPI CARDS MATCHING EXACT LEGEND METRICS AND COLORS */}
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '16px', marginBottom: '24px' }}>
            <KpiSparklineCard
              title="Chi phí nhập hàng"
              rawNumber={overview.importCost}
              formattedValue={reportMeta.available ? undefined : '—'}
              change={growthPercents.cost}
              strokeColor="#dc2626"
              data={mainChartData.map((d) => ({ v: d['Chi phí nhập hàng'] }))}
              unit="VND"
            />
            <KpiSparklineCard
              title="Tổng doanh thu"
              rawNumber={overview.totalRevenue}
              formattedValue={reportMeta.available ? undefined : '—'}
              change={growthPercents.revenue}
              strokeColor="#2563eb"
              data={mainChartData.map((d) => ({ v: d['Tổng doanh thu'] }))}
              unit="VND"
            />
            <KpiSparklineCard
              title="Tổng lợi nhuận"
              rawNumber={overview.totalProfit}
              formattedValue={reportMeta.available ? undefined : '—'}
              change={growthPercents.profit}
              strokeColor="#16a34a"
              data={mainChartData.map((d) => ({ v: d['Tổng lợi nhuận'] }))}
              unit="VND"
            />
            <KpiSparklineCard
              title="Tổng sản phẩm bán ra"
              rawNumber={overview.productsSold}
              formattedValue={reportMeta.available ? undefined : '—'}
              change={growthPercents.sales}
              strokeColor="#ea580c"
              data={mainChartData.map((d) => ({ v: d['Tổng SP bán ra'] }))}
              unit="Sản phẩm"
            />
            <KpiSparklineCard
              title="Tổng sản phẩm nhập vào"
              rawNumber={overview.productsImported}
              formattedValue={reportMeta.available ? undefined : '—'}
              change={growthPercents.imports}
              strokeColor="#9333ea"
              data={mainChartData.map((d) => ({ v: d['Tổng SP nhập vào'] }))}
              unit="Sản phẩm"
            />
          </div>

          {/* ROW 1B: MAC DATA CARDS REQUIRED BY MAC DESIGN */}
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '16px', marginBottom: '24px' }}>
            <KpiSparklineCard title="Giá bán bình quân hiện tại" rawNumber={inventoryCostSummary.currentSellingPrice} formattedValue={inventoryCostSummary.available ? undefined : '—'} comparisonLabel={inventoryCostSummary.hasStock ? 'Bình quân theo tồn kho' : 'Bình quân các biến thể'} strokeColor="#2563eb" data={[{ v: inventoryCostSummary.currentSellingPrice }]} unit="VND" />
            <KpiSparklineCard title="Giá vốn bình quân MAC" rawNumber={inventoryCostSummary.currentMac} formattedValue={inventoryCostSummary.available ? undefined : '—'} comparisonLabel={inventoryCostSummary.hasStock ? 'Bình quân theo tồn kho' : 'Bình quân các biến thể'} strokeColor="#16a34a" data={[{ v: inventoryCostSummary.currentMac }]} unit="VND" />
            <KpiSparklineCard title="Giá nhập lô gần nhất" rawNumber={inventoryCostSummary.latestImportPrice} formattedValue={inventoryCostSummary.available && inventoryCostSummary.hasCompletedImport ? undefined : '—'} comparisonLabel={inventoryCostSummary.hasCompletedImport ? 'Phiếu nhập đã hoàn tất' : 'Chưa có phiếu nhập hoàn tất'} strokeColor="#dc2626" data={[{ v: inventoryCostSummary.latestImportPrice }]} unit="VND" />
            <KpiSparklineCard title="Tổng tồn kho hiện tại" rawNumber={inventoryCostSummary.stockQuantity} formattedValue={inventoryCostSummary.available ? undefined : '—'} comparisonLabel={`${inventoryCostSummary.variantCount} biến thể được tính`} strokeColor="#7c3aed" data={[{ v: inventoryCostSummary.stockQuantity }]} unit="Sản phẩm" />
            <KpiSparklineCard title="Biên lợi nhuận gộp hiện tại" rawNumber={inventoryCostSummary.grossMarginPercent} formattedValue={inventoryCostSummary.available ? `${inventoryCostSummary.grossMarginPercent.toFixed(1)}%` : '—'} comparisonLabel="Theo giá bán và MAC hiện tại" strokeColor="#22c55e" data={[{ v: inventoryCostSummary.grossMarginPercent }]} unit="%" />
          </div>

          <InventoryValueChartCard data={mainChartData} dates={dates} products={macProducts} selectedVariant={selectedMacVariant} setSelectedVariant={setSelectedMacVariant} />

          {/* ROW 2: FINANCIAL TREND CHART */}
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(440px, 1fr))', gap: '24px', marginBottom: '24px' }}>
            {/* FINANCIAL CHART */}
            <div style={{ background: '#fff', borderRadius: '16px', boxShadow: '0 4px 16px rgba(0,0,0,0.03)', padding: '24px', border: '1px solid #f1f5f9' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '18px' }}>
                <div>
                  <h2 style={{ fontSize: '18px', fontWeight: '900', color: '#0f172a', margin: 0, letterSpacing: '-0.3px' }}>
                    Biểu đồ Doanh thu, Chi phí nhập & Lợi nhuận
                  </h2>
                  <span style={{ fontSize: '12px', color: '#94a3b8' }}>Dữ liệu tài chính dòng tiền từ {dates.from} đến {dates.to}</span>
                </div>
                <span style={{ padding: '4px 10px', borderRadius: '8px', background: '#eff6ff', color: '#2563eb', fontSize: '11px', fontWeight: '800', border: '1px solid #bfdbfe' }}>
                  Đơn vị: VND
                </span>
              </div>

              <div style={{ width: '100%', height: '270px' }}>
                <ResponsiveContainer width="100%" height="100%">
                  <AreaChart data={mainChartData} margin={{ top: 10, right: 10, left: -10, bottom: 0 }}>
                    <defs>
                      <linearGradient id="gradRevBeautiful" x1="0" y1="0" x2="0" y2="1">
                        <stop offset="5%" stopColor="#2563eb" stopOpacity={0.35} />
                        <stop offset="95%" stopColor="#2563eb" stopOpacity={0.0} />
                      </linearGradient>
                      <linearGradient id="gradCostBeautiful" x1="0" y1="0" x2="0" y2="1">
                        <stop offset="5%" stopColor="#dc2626" stopOpacity={0.35} />
                        <stop offset="95%" stopColor="#dc2626" stopOpacity={0.0} />
                      </linearGradient>
                      <linearGradient id="gradProfBeautiful" x1="0" y1="0" x2="0" y2="1">
                        <stop offset="5%" stopColor="#16a34a" stopOpacity={0.35} />
                        <stop offset="95%" stopColor="#16a34a" stopOpacity={0.0} />
                      </linearGradient>
                    </defs>
                    <CartesianGrid strokeDasharray="3 3" stroke="#f1f5f9" vertical={false} />
                    <XAxis
                      dataKey="date"
                      stroke="#000000"
                      fontSize={12}
                      tickLine={{ stroke: '#000000' }}
                      tick={{ fill: '#000000', fontWeight: '600' }}
                      interval="preserveStartEnd"
                      minTickGap={25}
                    />
                    <YAxis
                      stroke="#000000"
                      fontSize={11}
                      tickLine={{ stroke: '#000000' }}
                      tick={{ fill: '#000000', fontWeight: '600' }}
                      ticks={moneyTicks}
                      tickFormatter={formatYMoneyTick}
                      allowDecimals={false}
                    />
                    <Tooltip content={<CustomFinancialTooltip />} />
                    <Legend wrapperStyle={{ paddingTop: '10px', fontSize: '13px' }} />
                    <Area
                      type="monotone"
                      dataKey="Tổng doanh thu"
                      stroke="#2563eb"
                      fillOpacity={1}
                      fill="url(#gradRevBeautiful)"
                      strokeWidth={3}
                      connectNulls={false}
                      dot={renderCustomDot('Tổng doanh thu')}
                      activeDot={{ r: 6, strokeWidth: 2, fill: '#ffffff' }}
                      animationDuration={1200}
                    />
                    <Area
                      type="monotone"
                      dataKey="Chi phí nhập hàng"
                      stroke="#dc2626"
                      fillOpacity={1}
                      fill="url(#gradCostBeautiful)"
                      strokeWidth={2.5}
                      connectNulls={false}
                      dot={renderCustomDot('Chi phí nhập hàng')}
                      activeDot={{ r: 6, strokeWidth: 2, fill: '#ffffff' }}
                      animationDuration={1200}
                    />
                    <Area
                      type="monotone"
                      dataKey="Tổng lợi nhuận"
                      stroke="#16a34a"
                      fillOpacity={1}
                      fill="url(#gradProfBeautiful)"
                      strokeWidth={3}
                      connectNulls={false}
                      dot={renderCustomDot('Tổng lợi nhuận')}
                      activeDot={{ r: 6, strokeWidth: 2, fill: '#ffffff' }}
                      animationDuration={1200}
                    />
                  </AreaChart>
                </ResponsiveContainer>
              </div>
            </div>

            {/* QUANTITY CHART REMOVED: its metrics are now part of the MAC chart */}
            <div style={{ display: 'none' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '18px' }}>
                <div>
                  <h2 style={{ fontSize: '18px', fontWeight: '900', color: '#0f172a', margin: 0, letterSpacing: '-0.3px' }}>
                    Biểu đồ Sản phẩm bán ra & Nhập vào (Sản lượng)
                  </h2>
                  <span style={{ fontSize: '12px', color: '#94a3b8' }}>Dữ liệu số lượng sản phẩm xuất nhập từ {dates.from} đến {dates.to}</span>
                </div>
                <span style={{ padding: '4px 10px', borderRadius: '8px', background: '#fff7ed', color: '#ea580c', fontSize: '11px', fontWeight: '800', border: '1px solid #ffedd5' }}>
                  Đơn vị: Sản phẩm
                </span>
              </div>

              <div style={{ width: '100%', height: '270px' }}>
                <ResponsiveContainer width="100%" height="100%">
                  <LineChart data={mainChartData} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
                    <CartesianGrid strokeDasharray="3 3" stroke="#f1f5f9" vertical={false} />
                    <XAxis
                      dataKey="date"
                      stroke="#000000"
                      fontSize={12}
                      tickLine={{ stroke: '#000000' }}
                      tick={{ fill: '#000000', fontWeight: '600' }}
                      interval="preserveStartEnd"
                      minTickGap={25}
                    />
                    <YAxis
                      stroke="#000000"
                      fontSize={11}
                      tickLine={{ stroke: '#000000' }}
                      tick={{ fill: '#000000', fontWeight: '600' }}
                      ticks={productTicks}
                      tickFormatter={(v) => `${v}`}
                      allowDecimals={false}
                    />
                    <Tooltip content={<CustomQuantityTooltip />} />
                    <Legend wrapperStyle={{ paddingTop: '10px', fontSize: '13px' }} />
                    <Line
                      type="monotone"
                      dataKey="Tổng SP bán ra"
                      name="Tổng sản phẩm bán ra"
                      stroke="#ea580c"
                      strokeWidth={3}
                      connectNulls={false}
                      dot={renderCustomDot('Tổng SP bán ra')}
                      activeDot={{ r: 6, strokeWidth: 2, fill: '#ffffff' }}
                      animationDuration={1200}
                    />
                    <Line
                      type="monotone"
                      dataKey="Tổng SP nhập vào"
                      name="Tổng sản phẩm nhập vào"
                      stroke="#9333ea"
                      strokeWidth={2.5}
                      strokeDasharray="5 5"
                      connectNulls={false}
                      dot={renderCustomDot('Tổng SP nhập vào')}
                      activeDot={{ r: 6, strokeWidth: 2, fill: '#ffffff' }}
                      animationDuration={1200}
                    />
                  </LineChart>
                </ResponsiveContainer>
              </div>
            </div>
          </div>

          {/* ROW 3: 3D PIE CHARTS (CATEGORY & PAYMENT METHODS) */}
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(440px, 1fr))', gap: '24px', marginBottom: '24px' }}>
            {/* CATEGORY 3D PIE CHART */}
            <div style={{ background: '#fff', borderRadius: '16px', boxShadow: '0 4px 16px rgba(0,0,0,0.03)', padding: '24px', border: '1px solid #f1f5f9' }}>
              <h2 style={{ fontSize: '18px', fontWeight: '800', color: '#1e293b', marginBottom: '16px' }}>
                Doanh thu theo danh mục sản phẩm (Tỷ lệ % thị phần)
              </h2>
              {revenueCategory.length > 0 ? (
                <div style={{ width: '100%', height: '340px' }}>
                  <ResponsiveContainer width="100%" height="100%">
                    <PieChart>
                      <Pie
                        data={revenueCategory}
                        dataKey="amount"
                        nameKey="label"
                        cx="50%"
                        cy="50%"
                        innerRadius={45}
                        outerRadius={85}
                        label={renderCustomPieLabel}
                        labelLine={{ stroke: '#cbd5e1', strokeWidth: 1.5 }}
                        activeIndex={activeCategory}
                        activeShape={renderActiveShape}
                        onMouseEnter={(_, i) => setActiveCategory(i)}
                        animationDuration={1200}
                      >
                        {revenueCategory.map((entry, index) => (
                          <Cell key={`cell-${index}`} fill={chartColors[index % chartColors.length]} />
                        ))}
                      </Pie>
                      <Tooltip content={<CustomPieTooltip />} />
                      <Legend />
                    </PieChart>
                  </ResponsiveContainer>
                </div>
              ) : (
                <p style={{ textAlign: 'center', padding: '40px', color: '#94a3b8' }}>Chưa có dữ liệu doanh thu theo danh mục từ Backend.</p>
              )}
            </div>

            {/* PAYMENT METHOD 3D PIE CHART */}
            <div style={{ background: '#fff', borderRadius: '16px', boxShadow: '0 4px 16px rgba(0,0,0,0.03)', padding: '24px', border: '1px solid #f1f5f9' }}>
              <h2 style={{ fontSize: '18px', fontWeight: '800', color: '#1e293b', marginBottom: '16px' }}>
                Doanh thu theo phương thức thanh toán (Tỷ lệ % thị phần)
              </h2>
              {paymentMethods.length > 0 ? (
                <div style={{ width: '100%', height: '340px' }}>
                  <ResponsiveContainer width="100%" height="100%">
                    <PieChart>
                      <Pie
                        data={paymentMethods}
                        dataKey="amount"
                        nameKey="method"
                        cx="50%"
                        cy="50%"
                        innerRadius={45}
                        outerRadius={85}
                        label={renderCustomPieLabel}
                        labelLine={{ stroke: '#cbd5e1', strokeWidth: 1.5 }}
                        activeIndex={activePayment}
                        activeShape={renderActiveShape}
                        onMouseEnter={(_, i) => setActivePayment(i)}
                        animationDuration={1200}
                      >
                        {paymentMethods.map((entry, index) => (
                          <Cell key={`cell-${index}`} fill={chartColors[index % chartColors.length]} />
                        ))}
                      </Pie>
                      <Tooltip content={<CustomPieTooltip />} />
                      <Legend />
                    </PieChart>
                  </ResponsiveContainer>
                </div>
              ) : (
                <p style={{ textAlign: 'center', padding: '40px', color: '#94a3b8' }}>Chưa có dữ liệu thanh toán từ Backend.</p>
              )}
            </div>
          </div>

          {/* ROW 4: PROFESSIONAL RANKING TABLES (TOP SELLING PRODUCTS & TOP CUSTOMERS) */}
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(440px, 1fr))', gap: '24px', marginBottom: '24px' }}>
            
            {/* TOP SELLING PRODUCTS RANKING TABLE */}
            <div style={{ background: '#fff', borderRadius: '16px', boxShadow: '0 4px 16px rgba(0,0,0,0.03)', padding: '24px', border: '1px solid #f1f5f9' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '18px' }}>
                <div>
                  <h2 style={{ fontSize: '18px', fontWeight: '900', color: '#0f172a', margin: 0, letterSpacing: '-0.3px' }}>Bảng xếp hạng Sản phẩm bán chạy</h2>
                  <span style={{ fontSize: '12px', color: '#94a3b8' }}>Xếp hạng theo số lượng bán ra & doanh số</span>
                </div>
                <span style={{ padding: '4px 10px', borderRadius: '20px', fontSize: '11px', fontWeight: '800', background: '#fff7ed', color: '#ea580c', border: '1px solid #ffedd5' }}>Top 10</span>
              </div>

              {topProducts.length > 0 ? (
                <div style={{ overflowX: 'auto' }}>
                  <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '13px', textAlign: 'left' }}>
                    <thead>
                      <tr style={{ borderBottom: '2px solid #f1f5f9', color: '#64748b', fontSize: '12px' }}>
                        <th style={{ padding: '10px 8px', width: '50px', textAlign: 'center' }}>HẠNG</th>
                        <th style={{ padding: '10px 8px', width: '60px' }}>ID</th>
                        <th style={{ padding: '10px 8px', width: '56px' }}>ẢNH</th>
                        <th style={{ padding: '10px 8px' }}>TÊN SẢN PHẨM</th>
                        <th style={{ padding: '10px 8px', minWidth: '150px' }}>SỐ LƯỢNG BÁN RA</th>
                      </tr>
                    </thead>
                    <tbody>
                      {topProducts.map((prod, idx) => {
                        const maxQty = topProducts[0]?.soldQuantity || 1;
                        const percent = Math.min(100, Math.max(8, Math.round((prod.soldQuantity / maxQty) * 100)));
                        const isTop3 = idx < 3;
                        const medal = idx === 0 ? '🥇' : idx === 1 ? '🥈' : idx === 2 ? '🥉' : idx + 1;
                        
                        return (
                          <tr
                            key={prod.productId || prod.id || idx}
                            style={{ borderBottom: '1px solid #f8fafc', transition: 'background 0.2s ease', cursor: 'pointer' }}
                            onMouseEnter={(e) => { e.currentTarget.style.background = '#fff8f3'; }}
                            onMouseLeave={(e) => { e.currentTarget.style.background = 'transparent'; }}
                          >
                            {/* HẠNG */}
                            <td style={{ padding: '12px 8px', textAlign: 'center' }}>
                              <span style={{
                                display: 'inline-flex',
                                alignItems: 'center',
                                justifyContent: 'center',
                                width: '32px',
                                height: '32px',
                                borderRadius: '50%',
                                background: idx === 0 ? '#fef3c7' : idx === 1 ? '#f1f5f9' : idx === 2 ? '#ffedd5' : '#f8fafc',
                                fontSize: isTop3 ? '16px' : '13px',
                                fontWeight: '800',
                                color: idx === 0 ? '#d97706' : idx === 1 ? '#475569' : idx === 2 ? '#c2410c' : '#64748b',
                                border: isTop3 ? `1px solid ${idx === 0 ? '#fde68a' : idx === 1 ? '#cbd5e1' : '#fed7aa'}` : '1px solid #e2e8f0',
                              }}>
                                {medal}
                              </span>
                            </td>

                            {/* ID */}
                            <td style={{ padding: '12px 8px', fontWeight: '800', color: '#2563eb', fontSize: '12px' }}>
                              #{prod.productId || prod.id || idx + 1}
                            </td>

                            {/* ẢNH */}
                            <td style={{ padding: '12px 8px' }}>
                              {prod.imageUrl ? (
                                <img src={prod.imageUrl} alt="" style={{ width: '42px', height: '42px', borderRadius: '8px', objectFit: 'cover', border: '1px solid #e2e8f0' }} />
                              ) : (
                                <div style={{ width: '42px', height: '42px', borderRadius: '8px', background: '#ffedd5', color: '#ea580c', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '18px' }}>
                                  📦
                                </div>
                              )}
                            </td>

                            {/* TÊN */}
                            <td style={{ padding: '12px 8px', fontWeight: '700', color: '#0f172a', maxWdith: '180px' }}>
                              <div style={{ whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                                {prod.productName || prod.name || 'Sản phẩm đồ chơi'}
                              </div>
                              <div style={{ fontSize: '11px', color: '#64748b', fontWeight: '500' }}>
                                Doanh thu: {formatVndText(prod.grossRevenue ?? prod.totalRevenue ?? prod.revenue ?? 0)}
                              </div>
                            </td>

                            {/* SỐ LƯỢNG BÁN RA (PROGRESS BAR) */}
                            <td style={{ padding: '12px 8px' }}>
                              <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                                <div style={{ flex: 1, height: '8px', background: '#e2e8f0', borderRadius: '4px', overflow: 'hidden' }}>
                                  <div
                                    style={{
                                      height: '100%',
                                      width: `${percent}%`,
                                      background: idx === 0 ? 'linear-gradient(90deg, #f59e0b, #d97706)' : idx === 1 ? 'linear-gradient(90deg, #3b82f6, #1d4ed8)' : idx === 2 ? 'linear-gradient(90deg, #ea580c, #c2410c)' : 'linear-gradient(90deg, #10b981, #059669)',
                                      borderRadius: '4px',
                                      transition: 'width 0.6s ease',
                                    }}
                                  />
                                </div>
                                <span style={{ fontSize: '12px', fontWeight: '900', color: '#0f172a', minWidth: '45px', textAlign: 'right' }}>
                                  {prod.soldQuantity || 0} SP
                                </span>
                              </div>
                            </td>
                          </tr>
                        );
                      })}
                    </tbody>
                  </table>
                </div>
              ) : (
                <p style={{ textAlign: 'center', padding: '40px', color: '#94a3b8' }}>Chưa có dữ liệu sản phẩm bán chạy từ Backend.</p>
              )}
            </div>

            {/* TOP SPENDING CUSTOMERS RANKING TABLE */}
            <div style={{ background: '#fff', borderRadius: '16px', boxShadow: '0 4px 16px rgba(0,0,0,0.03)', padding: '24px', border: '1px solid #f1f5f9' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '18px' }}>
                <div>
                  <h2 style={{ fontSize: '18px', fontWeight: '900', color: '#0f172a', margin: 0, letterSpacing: '-0.3px' }}>Khách hàng chi tiêu cao (VIP)</h2>
                  <span style={{ fontSize: '12px', color: '#94a3b8' }}>Xếp hạng theo tổng tiền mua sắm tại cửa hàng</span>
                </div>
                <span style={{ padding: '4px 10px', borderRadius: '20px', fontSize: '11px', fontWeight: '800', background: '#f0fdf4', color: '#16a34a', border: '1px solid #bbf7d0' }}>VIP</span>
              </div>

              {topCustomers.length > 0 ? (
                <div style={{ overflowX: 'auto' }}>
                  <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '13px', textAlign: 'left' }}>
                    <thead>
                      <tr style={{ borderBottom: '2px solid #f1f5f9', color: '#64748b', fontSize: '12px' }}>
                        <th style={{ padding: '10px 8px', width: '50px', textAlign: 'center' }}>HẠNG</th>
                        <th style={{ padding: '10px 8px', width: '60px' }}>ID</th>
                        <th style={{ padding: '10px 8px' }}>TÊN KHÁCH HÀNG</th>
                        <th style={{ padding: '10px 8px', textAlign: 'right' }}>CHI TIÊU (VND)</th>
                        <th style={{ padding: '10px 8px', textAlign: 'center', width: '150px' }}>SẢN PHẨM / ĐƠN</th>
                      </tr>
                    </thead>
                    <tbody>
                      {topCustomers.map((cust, idx) => {
                        const isTop3 = idx < 3;
                        const medal = idx === 0 ? '🥇' : idx === 1 ? '🥈' : idx === 2 ? '🥉' : idx + 1;
                        const name = cust.label || cust.customerName || cust.fullName || `Khách hàng #${cust.id || idx + 1}`;
                        const orderCount = Number(cust.orderCount ?? 0);
                        const purchasedQuantity = Number(cust.purchasedQuantity ?? 0);
                        const averageProductsPerOrder = Number(
                          cust.averageProductsPerOrder
                          ?? (orderCount > 0 ? purchasedQuantity / orderCount : 0),
                        );
                        const spent = cust.amount || cust.totalSpent || 0;
                        return (
                          <tr
                            key={cust.customerId || cust.id || idx}
                            style={{ borderBottom: '1px solid #f8fafc', transition: 'background 0.2s ease', cursor: 'pointer' }}
                            onMouseEnter={(e) => { e.currentTarget.style.background = '#f0fdf4'; }}
                            onMouseLeave={(e) => { e.currentTarget.style.background = 'transparent'; }}
                          >
                            {/* HẠNG */}
                            <td style={{ padding: '12px 8px', textAlign: 'center' }}>
                              <span style={{
                                display: 'inline-flex',
                                alignItems: 'center',
                                justifyContent: 'center',
                                width: '32px',
                                height: '32px',
                                borderRadius: '50%',
                                background: idx === 0 ? '#fef3c7' : idx === 1 ? '#f1f5f9' : idx === 2 ? '#ffedd5' : '#f8fafc',
                                fontSize: isTop3 ? '16px' : '13px',
                                fontWeight: '800',
                                color: idx === 0 ? '#d97706' : idx === 1 ? '#475569' : idx === 2 ? '#c2410c' : '#64748b',
                                border: isTop3 ? `1px solid ${idx === 0 ? '#fde68a' : idx === 1 ? '#cbd5e1' : '#fed7aa'}` : '1px solid #e2e8f0',
                              }}>
                                {medal}
                              </span>
                            </td>

                            {/* ID KHÁCH HÀNG */}
                            <td style={{ padding: '12px 8px', fontWeight: '800', color: '#16a34a', fontSize: '12px' }}>
                              #USR-{cust.userId || cust.customerId || cust.id || (idx + 1)}
                            </td>

                            {/* TÊN */}
                            <td style={{ padding: '12px 8px', fontWeight: '700', color: '#0f172a' }}>
                              <div style={{ whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                                {name}
                              </div>
                            </td>

                            {/* SỐ TIỀN CHI TIÊU */}
                            <td style={{ padding: '12px 8px', fontWeight: '900', color: '#16a34a', textAlign: 'right' }}>
                              {formatVndText(spent)}
                            </td>

                            {/* SỐ LƯỢNG MUA */}
                            <td style={{ padding: '12px 8px', textAlign: 'center' }}>
                              <span style={{ padding: '4px 10px', borderRadius: '12px', background: '#e0f2fe', color: '#0369a1', fontSize: '12px', fontWeight: '800' }}>
                                {formatStatisticNumber(averageProductsPerOrder)} sản phẩm / đơn
                              </span>
                            </td>
                          </tr>
                        );
                      })}
                    </tbody>
                  </table>
                </div>
              ) : (
                <p style={{ textAlign: 'center', padding: '40px', color: '#94a3b8' }}>Chưa có dữ liệu khách hàng chi tiêu cao từ Backend.</p>
              )}
            </div>

          </div>

          {/* ROW 5: RECENT ORDERS & OPERATIONAL ALERTS */}
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(440px, 1fr))', gap: '24px' }}>
            {/* RECENT ORDERS TABLE */}
            <div style={{ background: '#fff', borderRadius: '16px', boxShadow: '0 4px 16px rgba(0,0,0,0.03)', padding: '24px', border: '1px solid #f1f5f9' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
                <h2 style={{ fontSize: '18px', fontWeight: '800', color: '#1e293b', margin: 0 }}>Đơn hàng gần đây</h2>
                <span style={{ fontSize: '12px', color: '#94a3b8' }}>Mới nhất</span>
              </div>

              <div style={{ overflowX: 'auto' }}>
                <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '13px', textAlign: 'left' }}>
                  <thead>
                    <tr style={{ borderBottom: '2px solid #f1f5f9', color: '#64748b', fontSize: '12px' }}>
                      <th style={{ padding: '10px 8px' }}>Mã đơn</th>
                      <th style={{ padding: '10px 8px' }}>Khách hàng</th>
                      <th style={{ padding: '10px 8px' }}>Tổng tiền</th>
                      <th style={{ padding: '10px 8px' }}>Ngày tạo</th>
                      <th style={{ padding: '10px 8px', textAlign: 'center' }}>Trạng thái</th>
                    </tr>
                  </thead>
                  <tbody>
                    {recentOrders.map((order) => (
                      <tr key={order.id} style={{ borderBottom: '1px solid #f8fafc' }}>
                        <td style={{ padding: '12px 8px', fontWeight: '700', color: '#2563eb' }}>
                          #{order.id || order.code}
                        </td>
                        <td style={{ padding: '12px 8px', fontWeight: '500' }}>
                          <div style={{ fontWeight: '700', color: '#0f172a' }}>
                            {order.customerName || order.user?.fullName || order.recipientName || 'Khách lẻ'}
                          </div>
                          <div style={{ fontSize: '11px', color: '#64748b', fontWeight: '600' }}>
                            ID Khách: #USR-{order.userId || order.user?.id || order.customerId || order.id}
                          </div>
                        </td>
                        <td style={{ padding: '12px 8px', fontWeight: '700' }}>
                          {formatVndText(order.totalAmount || order.grandTotal)}
                        </td>
                        <td style={{ padding: '12px 8px', color: '#64748b', fontSize: '12px' }}>
                          {formatDateTime(order.createdAt)}
                        </td>
                        <td style={{ padding: '12px 8px', textAlign: 'center' }}>
                          <OrderStatusBadge status={order.status} />
                        </td>
                      </tr>
                    ))}
                    {recentOrders.length === 0 && (
                      <tr>
                        <td colSpan={5} style={{ textAlign: 'center', padding: '24px', color: '#94a3b8' }}>
                          Chưa có đơn hàng nào trong hệ thống.
                        </td>
                      </tr>
                    )}
                  </tbody>
                </table>
              </div>
            </div>

            {/* OPERATIONAL ALERTS */}
            <div style={{ background: '#fff', borderRadius: '16px', boxShadow: '0 4px 16px rgba(0,0,0,0.03)', padding: '24px', border: '1px solid #f1f5f9' }}>
              <div style={{ marginBottom: '16px' }}>
                <h2 style={{ fontSize: '18px', fontWeight: '800', color: '#1e293b', margin: 0 }}>Cảnh báo vận hành cửa hàng</h2>
                <span style={{ fontSize: '12px', color: '#94a3b8' }}>Trạng thái cần chú ý</span>
              </div>

              <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                <OperationalAlertCard
                  category="THANH TOÁN"
                  title="Chờ xác nhận thanh toán"
                  count={overview.alerts.pendingPayment}
                  hint="Kiểm tra các giao dịch VNPAY/COD đang chờ xác nhận."
                  tone="pending"
                  onClick={() => navigate('/admin/payments?status=PENDING')}
                />
                <OperationalAlertCard
                  category="HOÀN TRẢ"
                  title="Yêu cầu hoàn trả hàng"
                  count={overview.alerts.pendingReturn}
                  hint="Kiểm tra các yêu cầu hoàn trả mới và xử lý trong thời hạn cam kết."
                  tone="return"
                  onClick={() => navigate('/admin/returns?status=REQUESTED')}
                />
                <OperationalAlertCard
                  category="TỒN KHO"
                  title="Biến thể sắp hết hàng"
                  count={overview.alerts.lowStock}
                  hint="Chuẩn bị phiếu nhập kho trước khi các món đồ chơi hot bị hết hàng."
                  tone="warning"
                  onClick={() => navigate('/admin/imports?source=LOW_STOCK')}
                />
                <OperationalAlertCard
                  category="SẢN PHẨM"
                  title="Sản phẩm bán chậm"
                  count={overview.alerts.slowSelling}
                  hint="Xem các sản phẩm có lượt bán thấp để điều chỉnh giá hoặc tạo khuyến mãi."
                  tone="warning"
                  onClick={() => navigate('/admin/products?view=SLOW_SELLING')}
                />
                <OperationalAlertCard
                  category="ĐƠN HÀNG"
                  title="Đơn hàng đã hủy"
                  count={overview.alerts.cancelledOrder}
                  hint="Theo dõi lý do hủy đơn để cải thiện dịch vụ hoặc nguồn hàng."
                  tone="cancelled"
                  onClick={() => navigate('/admin/orders?status=CANCELLED')}
                />
                <OperationalAlertCard
                  category="ĐÁNH GIÁ"
                  title="Cảnh báo Đánh giá kém (< 3 sao)"
                  count={overview.alerts.lowRatingCount}
                  hint="Xem ngay phản hồi < 3 sao từ khách hàng để hỗ trợ và xử lý dịch vụ."
                  tone="danger"
                  onClick={() => navigate('/admin/reviews?maxRating=2&hasAdminReplied=false')}
                />
              </div>
            </div>
          </div>
        </>
      )}
    </section>
  );
}

/* ═══════════════════════════════════════════════════════════════════
   SUB-COMPONENTS
   ═══════════════════════════════════════════════════════════════════ */
function TimeRangeBar({ dates, setDates, selectedRange, handleRangeClick }) {
  return (
    <div style={{ background: '#fff', padding: '16px 20px', borderRadius: '16px', boxShadow: '0 2px 10px rgba(0,0,0,0.03)', marginBottom: '24px', border: '1px solid #f1f5f9', display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: '16px' }}>
      <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
        {quickRanges.map((r) => (
          <button
            key={r.code}
            type="button"
            onClick={() => handleRangeClick(r.code)}
            style={{
              padding: '8px 16px',
              borderRadius: '10px',
              border: selectedRange === r.code ? '2px solid #ea580c' : '1px solid #fed7aa',
              fontSize: '13px',
              fontWeight: '700',
              cursor: 'pointer',
              background: selectedRange === r.code ? '#ea580c' : '#fff',
              color: selectedRange === r.code ? '#fff' : '#ea580c',
              transition: 'all 0.2s ease',
            }}
          >
            {r.label}
          </button>
        ))}
      </div>

      <div style={{ display: 'flex', alignItems: 'center', gap: '12px', flexWrap: 'wrap' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
          <span style={{ fontSize: '12px', fontWeight: '700', color: '#64748b' }}>TỪ NGÀY:</span>
          <input
            type="date"
            value={dates.from}
            onChange={(e) => {
              handleRangeClick('CUSTOM');
              setDates((prev) => ({ ...prev, from: e.target.value }));
            }}
            style={{ padding: '6px 10px', border: '1px solid #cbd5e1', borderRadius: '8px', fontSize: '13px', outline: 'none' }}
          />
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
          <span style={{ fontSize: '12px', fontWeight: '700', color: '#64748b' }}>ĐẾN NGÀY:</span>
          <input
            type="date"
            value={dates.to}
            onChange={(e) => {
              handleRangeClick('CUSTOM');
              setDates((prev) => ({ ...prev, to: e.target.value }));
            }}
            style={{ padding: '6px 10px', border: '1px solid #cbd5e1', borderRadius: '8px', fontSize: '13px', outline: 'none' }}
          />
        </div>
      </div>
    </div>
  );
}

function formatCompactValue(val, unit) {
  const num = Number(val || 0);
  if (unit !== 'VND') {
    return num.toLocaleString('vi-VN');
  }
  if (!num || num === 0) return '0 ₫';
  if (num >= 1_000_000_000) return `${(num / 1_000_000_000).toFixed(1)}B ₫`;
  if (num >= 1_000_000) return `${(num / 1_000_000).toFixed(num >= 10_000_000 ? 0 : 1)}M ₫`;
  if (num >= 1_000) return `${(num / 1_000).toFixed(0)}K ₫`;
  return `${num.toLocaleString('vi-VN')} ₫`;
}

/* Top KPI Card: Big Value + Title + Neatly Framed Wave Chart (NO DOTS) + Growth Badge */
function KpiSparklineCard({ title, rawNumber, formattedValue, change, comparisonLabel, strokeColor = '#f59e0b', data = [], unit }) {
  const isPositive = change && change !== '0%' && change !== '+0%' && change.startsWith('+');
  const isNegative = change && change !== '0%' && change !== '-0%' && change.startsWith('-');

  const changeColor = isNegative ? '#f43f5e' : isPositive ? '#16a34a' : '#64748b';
  const arrow = isNegative ? '↓' : isPositive ? '↑' : '•';
  const displayChange = change ? `${arrow} ${change.replace(/^[+-]/, '')}` : '—';

  const numericVal = typeof rawNumber === 'number' ? rawNumber : Number(rawNumber || 0);
  const bigDisplay = formattedValue ?? formatCompactValue(numericVal, unit);
  const gradId = useMemo(() => `sparkGrad-${title.replace(/[^a-zA-Z0-9]/g, '')}`, [title]);

  // Strictly plot 100% real timeline data without artificial mountain peaks or sine waves
  const sparkData = useMemo(() => {
    if (!data || data.length === 0) return [];
    const rawVals = data.map((d) => Number(d.v || 0));

    // For single-day views (Today / Yesterday), plot a clean line from 0 to the day's value
    if (rawVals.length === 1) {
      const val = rawVals[0];
      return [{ v: 0 }, { v: val }];
    }

    // Directly plot actual daily data points from the backend
    return rawVals.map((v) => ({ v }));
  }, [data]);

  return (
    <div
      style={{
        background: '#ffffff',
        borderRadius: '20px',
        border: `1.5px solid ${strokeColor}25`,
        boxShadow: `0 4px 18px ${strokeColor}0d`,
        padding: '20px 16px 16px 16px',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        textAlign: 'center',
        minHeight: '175px',
        justifyContent: 'space-between',
        overflow: 'hidden',
      }}
    >
      {/* BIG VALUE DISPLAY */}
      <div style={{ fontSize: '36px', fontWeight: '900', color: '#0f172a', letterSpacing: '-1px', lineHeight: 1.1 }}>
        {bigDisplay}
      </div>

      {/* TITLE IN METRIC ACCENT COLOR */}
      <div style={{ fontSize: '13.5px', fontWeight: '800', color: strokeColor, marginTop: '2px', letterSpacing: '-0.2px' }}>
        {title}
      </div>

      {/* EMBEDDED SPARKLINE CHART CONTAINER (Contained 100% inside card box) */}
      <div
        style={{
          width: '100%',
          height: '54px',
          margin: '4px 0',
          position: 'relative',
          pointerEvents: 'none',
        }}
      >
        {sparkData.length > 0 && (
          <ResponsiveContainer width="100%" height="100%">
            <AreaChart data={sparkData} margin={{ top: 6, right: 8, left: 8, bottom: 6 }}>
              <defs>
                <linearGradient id={gradId} x1="0" y1="0" x2="0" y2="1">
                  <stop offset="0%" stopColor={strokeColor} stopOpacity={0.4} />
                  <stop offset="100%" stopColor={strokeColor} stopOpacity={0.03} />
                </linearGradient>
              </defs>
              <Area
                type="monotone"
                dataKey="v"
                stroke={strokeColor}
                strokeWidth={2.5}
                fill={`url(#${gradId})`}
                dot={false}
                activeDot={false}
                isAnimationActive={false}
              />
            </AreaChart>
          </ResponsiveContainer>
        )}
      </div>

      {/* GROWTH COMPARISON BADGE */}
      <div style={{ fontSize: '11.5px', fontWeight: '800', color: changeColor, display: 'inline-flex', alignItems: 'center', gap: '4px', background: `${changeColor}14`, padding: '3px 10px', borderRadius: '10px', border: `1px solid ${changeColor}25` }}>
        {!comparisonLabel && <span>{displayChange}</span>}
        <span style={{ fontWeight: '600', color: '#64748b' }}>{comparisonLabel || 'so với khoảng trước'}</span>
      </div>
    </div>
  );
}

function OperationalAlertCard({ category, title, count, hint, tone, onClick }) {
  const isDanger = tone === 'danger';
  const isWarning = tone === 'warning';
  const isCancelled = tone === 'cancelled';
  const isReturn = tone === 'return';

  const badgeBg = isDanger ? '#fef2f2' : isWarning ? '#fffbebfb' : isCancelled ? '#f8fafc' : isReturn ? '#eff6ff' : '#f0fdf4';
  const badgeColor = isDanger ? '#dc2626' : isWarning ? '#d97706' : isCancelled ? '#64748b' : isReturn ? '#2563eb' : '#16a34a';

  return (
    <button
      type="button"
      onClick={onClick}
      style={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        padding: '14px 18px',
        borderRadius: '12px',
        background: badgeBg,
        border: `1px solid ${isDanger ? '#fecaca' : isWarning ? '#fef3c7' : isReturn ? '#bfdbfe' : '#e2e8f0'}`,
        cursor: onClick ? 'pointer' : 'default',
        transition: 'all 0.15s ease',
        userSelect: 'none',
        width: '100%',
        textAlign: 'left',
        font: 'inherit',
      }}
      onMouseEnter={(e) => {
        if (onClick) {
          e.currentTarget.style.transform = 'translateY(-2px)';
          e.currentTarget.style.boxShadow = '0 6px 16px rgba(0,0,0,0.06)';
        }
      }}
      onMouseLeave={(e) => {
        if (onClick) {
          e.currentTarget.style.transform = 'translateY(0)';
          e.currentTarget.style.boxShadow = 'none';
        }
      }}
    >
      <div>
        <div style={{ fontSize: '11px', fontWeight: '800', color: badgeColor, letterSpacing: '0.5px' }}>{category}</div>
        <div style={{ fontSize: '14px', fontWeight: '700', color: '#1e293b', margin: '2px 0' }}>{title}</div>
        <div style={{ fontSize: '12px', color: '#64748b' }}>{hint}</div>
      </div>
      <div style={{ fontSize: '22px', fontWeight: '900', color: badgeColor }}>{count}</div>
    </button>
  );
}

function OrderStatusBadge({ status }) {
  const code = typeof status === 'object' ? (status?.code || status?.name || '') : String(status || '');
  const displayName = typeof status === 'object' ? (status?.displayName || status?.label || '') : '';
  const s = code.toUpperCase();

  let bg = '#f1f5f9';
  let color = '#475569';
  let label = displayName || s;

  if (['COMPLETED', 'DELIVERED', 'PAID', 'SUCCESS'].includes(s)) {
    bg = '#dcfce7';
    color = '#166534';
    if (!displayName) label = 'Thành công';
  } else if (['CONFIRMED', 'CONFIRM'].includes(s)) {
    bg = '#eff6ff';
    color = '#2563eb';
    if (!displayName) label = 'Đã xác nhận';
  } else if (['SHIPPED', 'SHIPPING'].includes(s)) {
    bg = '#f0fdf4';
    color = '#16a34a';
    if (!displayName) label = 'Đang giao';
  } else if (['PENDING', 'PROCESSING', 'UNPAID'].includes(s)) {
    bg = '#fef3c7';
    color = '#92400e';
    if (!displayName) label = 'Chờ xử lý';
  } else if (['CANCELLED', 'FAILED', 'REFUNDED'].includes(s)) {
    bg = '#fee2e2';
    color = '#991b1b';
    if (!displayName) label = 'Đã hủy';
  }

  return (
    <span style={{ padding: '4px 10px', borderRadius: '20px', fontSize: '11px', fontWeight: '800', background: bg, color }}>
      {label}
    </span>
  );
}

export default AdminStatisticsPage;
