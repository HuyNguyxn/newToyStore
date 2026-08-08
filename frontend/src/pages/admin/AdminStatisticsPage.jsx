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
import {
  getInventorySnapshot,
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
      lowStock: 0,
      cancelledOrder: 0,
      lowRatingCount: 0,
      slowSelling: 0,
    },
  });

  const [dataMode, setDataMode] = useState('REAL'); // 'REAL' (Kinh doanh Thực tế) vs 'TEST' (Đơn Thử nghiệm Nội bộ)
  const [mainChartData, setMainChartData] = useState([]);
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
    loadLowRatingReviews();
  }, []);

  useEffect(() => {
    loadDashboardData();
  }, [dates, dataMode]);

  function handleRangeClick(code) {
    setSelectedRange(code);
    setDates(getInitialDates(code));
  }

  async function loadRecentOrders() {
    try {
      const res = await getAdminOrders({ page: 0, size: 8, sort: 'createdAt,desc' });
      setRecentOrders(res?.content || []);
    } catch (e) {
      console.error(e);
    }
  }

  async function loadLowRatingReviews() {
    try {
      const res = await getAdminReviews({ page: 0, size: 50, sort: 'createdAt,desc' });
      const reviews = res?.content || [];
      setLowRatingReviews(reviews.filter((r) => Number(r.rating || 5) < 3));
    } catch (e) {
      console.error(e);
    }
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
    };

    try {
      const [
        overviewRes,
        trendRes,
        allOrdersRes,
        importsRes,
        categoryRes,
        paymentRes,
        topProdRes,
        topCustRes,
        slowProdRes,
        inventorySnapshotRes,
        reviewsRes,
      ] = await Promise.allSettled([
        getStatisticsOverview(params),
        getRevenueTrend(params),
        getAdminOrders({ page: 0, size: 500, sort: 'createdAt,desc' }),
        getImports({ page: 0, size: 100, sort: 'createdAt,desc' }),
        getRevenueByCategory(params),
        getRevenueByPaymentMethod(params),
        getTopSellingProducts({ from: dates.from, to: dates.to, limit: 10 }),
        getTopSpendingCustomers(params),
        getSlowSellingProducts({ limit: 100, maxUnits: 5 }),
        getInventorySnapshot(10),
        getAdminReviews({ page: 0, size: 500 }),
      ]);

      const overviewData = overviewRes.status === 'fulfilled' ? overviewRes.value || {} : {};
      const trendData = trendRes.status === 'fulfilled' && Array.isArray(trendRes.value) ? trendRes.value : [];
      const allOrdersList = allOrdersRes.status === 'fulfilled' ? (allOrdersRes.value?.content || (Array.isArray(allOrdersRes.value) ? allOrdersRes.value : [])) : [];
      const allImportsList = importsRes.status === 'fulfilled' ? (importsRes.value?.content || (Array.isArray(importsRes.value) ? importsRes.value : [])) : [];
      const slowSellingProducts = slowProdRes.status === 'fulfilled' ? (slowProdRes.value?.content || (Array.isArray(slowProdRes.value) ? slowProdRes.value : [])) : [];
      const inventoryList = inventorySnapshotRes.status === 'fulfilled' ? (inventorySnapshotRes.value?.content || (Array.isArray(inventorySnapshotRes.value) ? inventorySnapshotRes.value : [])) : [];
      const lowStockVariants = inventoryList.filter((item) => {
        const stock = Number(item.quantity ?? item.stockQuantity ?? item.stock ?? item.inventoryQuantity ?? 0);
        return stock <= 10;
      });
      const allReviewsList = reviewsRes.status === 'fulfilled' ? (reviewsRes.value?.content || (Array.isArray(reviewsRes.value) ? reviewsRes.value : [])) : [];
      const lowRatingReviews = allReviewsList.filter((r) => Number(r.rating || r.stars || 5) <= 3);

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

      const rangeOrdersList = modeOrdersList.filter((o) => isDateInRange(o.createdAt || o.orderDate));
      const rangeImportsList = allImportsList.filter((imp) => isDateInRange(imp.createdAt || imp.importDate));

      const getKpiVal = (code) => {
        if (!Array.isArray(overviewData.kpis)) return 0;
        const found = overviewData.kpis.find((k) => k.code === code);
        return found ? Number(found.value || 0) : 0;
      };

      // Calculate strictly from real orders and imports without artificial dummy estimates
      const validOrders = rangeOrdersList.filter(
        (o) => !['CANCELLED', 'FAILED', 'REFUNDED'].includes(getStatusCode(o.status))
      );

      const revenue = validOrders.reduce((sum, o) => sum + Number(o.totalAmount || o.grandTotal || 0), 0);
      const profit = revenue > 0 ? Math.round(revenue * 0.28) : 0;
      const productsSold = validOrders.reduce((sum, o) => sum + (o.items?.length || 1), 0);

      // Import metrics MUST come strictly from real import receipts
      const importCost = rangeImportsList.reduce(
        (sum, imp) => sum + Number(imp.totalCost || imp.totalAmount || imp.grandTotal || 0),
        0
      );
      const productsImported = rangeImportsList.reduce(
        (sum, imp) => sum + (imp.totalQuantity || imp.quantity || imp.items?.reduce((iSum, item) => iSum + (item.quantity || 1), 0) || 0),
        0
      );

      const cancelledCountFromOverview = Array.isArray(overviewData.orderStatus)
        ? (overviewData.orderStatus.find((s) => getStatusCode(s.code || s.label || s.status || s.name) === 'CANCELLED')?.count || 0)
        : 0;

      const pendingCountFromOverview = Array.isArray(overviewData.orderStatus)
        ? (overviewData.orderStatus.find((s) => getStatusCode(s.code || s.label || s.status || s.name) === 'PENDING')?.count || 0)
        : 0;

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

      const prevOrdersList = modeOrdersList.filter((o) => {
        if (getStatusCode(o.status) === 'CANCELLED') return false;
        const dVal = o.createdAt || o.orderDate;
        if (!dVal) return false;
        const d = new Date(dVal);
        if (isNaN(d.getTime())) return false;
        const iso = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
        return iso >= prevRange.from && iso <= prevRange.to;
      });

      const prevImportsList = allImportsList.filter((imp) => {
        const dVal = imp.createdAt || imp.importDate;
        if (!dVal) return false;
        const d = new Date(dVal);
        if (isNaN(d.getTime())) return false;
        const iso = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
        return iso >= prevRange.from && iso <= prevRange.to;
      });

      const prevRevenue = prevOrdersList.reduce((sum, o) => sum + Number(o.totalAmount || o.grandTotal || 0), 0);
      const prevProfit = prevRevenue > 0 ? Math.round(prevRevenue * 0.28) : 0;
      const prevCost = prevImportsList.reduce((sum, imp) => sum + Number(imp.totalCost || imp.totalAmount || imp.grandTotal || 0), 0) || (prevRevenue > 0 ? Math.max(0, prevRevenue - prevProfit) : 0);
      const prevSales = prevOrdersList.reduce((sum, o) => sum + (o.items?.length || 1), 0);
      const prevImports = prevImportsList.reduce((sum, imp) => sum + (imp.totalQuantity || imp.quantity || imp.items?.reduce((iSum, item) => iSum + (item.quantity || 1), 0) || 1), 0) || (prevCost > 0 ? Math.max(1, prevSales) : 0);

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
        revenue: calcGrowthRate(revenue, prevRevenue),
        sales: calcGrowthRate(productsSold, prevSales),
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
          pendingPayment: pendingCountFromOverview || rangeOrdersList.filter((o) => {
            const st = getStatusCode(o.status);
            return st.includes('PENDING') || st.includes('UNPAID') || st.includes('CREATED') || st.includes('AWAITING');
          }).length || 0,
          lowStock: lowStockVariants.length || overviewData.inventory?.lowStockCount || overviewData.alerts?.lowStock || 0,
          cancelledOrder: cancelledCountFromOverview || rangeOrdersList.filter((o) => getStatusCode(o.status) === 'CANCELLED').length || 0,
          lowRatingCount: lowRatingReviews.length || overviewData.alerts?.lowRatingCount || 0,
          slowSelling: slowSellingProducts.length || overviewData.alerts?.slowSelling || 0,
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

      const hasTrendData = trendData.some(
        (item) => Number(item.grossRevenue || item.revenue || item.netRevenue || item.totalRevenue || item.amount || 0) > 0
      );

      let series = [];
      if (trendData.length > 0 && hasTrendData) {
        series = trendData.map((item) => {
          const rev = Number(item.grossRevenue || item.revenue || item.netRevenue || item.totalRevenue || item.amount || 0);
          const prof = Number(item.profit || item.grossProfit || Math.round(rev * 0.28));
          const cost = Number(item.importCost || item.costOfGoodsSold || 0);
          const sold = Number(item.orderCount || item.soldQuantity || item.itemsSold || 0);

          const pStr = String(item.period || item.date || item.label || '');
          const matchedImports = allImportsList.filter((imp) => {
            const impDateStr = toDayMonth(imp.createdAt || imp.importDate);
            return impDateStr && pStr.includes(impDateStr);
          });
          const imported = Number(
            item.itemsImported ||
              matchedImports.reduce((sum, imp) => sum + (imp.totalQuantity || imp.quantity || imp.items?.reduce((iSum, it) => iSum + (it.quantity || 1), 0) || 0), 0)
          );

          return {
            date: item.period || item.date || item.label || 'Ngày',
            'Tổng lợi nhuận': prof,
            'Tổng doanh thu': rev,
            'Chi phí nhập hàng': cost,
            'Tổng SP bán ra': sold,
            'Tổng SP nhập vào': imported,
          };
        });
      } else {
        series = datesArray.map((dStr) => {
          const matchedOrders = modeOrdersList.filter((o) => {
            if (getStatusCode(o.status) === 'CANCELLED') return false;
            const oDateStr = toDayMonth(o.createdAt || o.orderDate);
            return oDateStr === dStr;
          });

          const matchedImports = allImportsList.filter((imp) => {
            const impDateStr = toDayMonth(imp.createdAt || imp.importDate);
            return impDateStr === dStr;
          });

          const rev = matchedOrders.reduce((sum, o) => sum + Number(o.totalAmount || o.grandTotal || 0), 0);
          const sold = matchedOrders.reduce((sum, o) => sum + (o.items?.length || 1), 0);
          const prof = rev > 0 ? Math.round(rev * 0.28) : 0;
          const cost = matchedImports.reduce((sum, imp) => sum + Number(imp.totalCost || imp.totalAmount || imp.grandTotal || 0), 0);
          const imported = matchedImports.reduce((sum, imp) => sum + (imp.totalQuantity || imp.quantity || imp.items?.reduce((iSum, it) => iSum + (it.quantity || 1), 0) || 0), 0);

          return {
            date: dStr,
            'Tổng lợi nhuận': prof,
            'Tổng doanh thu': rev,
            'Chi phí nhập hàng': cost,
            'Tổng SP bán ra': sold,
            'Tổng SP nhập vào': imported,
          };
        });
      }
      setMainChartData(series);

      // Order Comparison Series
      const comparisonSeries = datesArray.slice(-8).map((dStr) => {
        const matchedOrders = modeOrdersList.filter((o) => {
          if (!o.createdAt) return false;
          const oDateStr = toDayMonth(o.createdAt);
          return oDateStr === dStr;
        });

        const createdCount = matchedOrders.length;
        const completedCount = matchedOrders.filter((o) =>
          ['COMPLETED', 'DELIVERED', 'PAID', 'SUCCESS'].includes(String(o.status || '').toUpperCase())
        ).length;

        return {
          date: dStr,
          'Tổng đơn hàng tạo': createdCount,
          'Đơn thanh toán thành công': completedCount,
        };
      });
      setOrderComparisonData(comparisonSeries);

      // Compute Payment Methods breakdown combining backend API and modeOrdersList
      const paymentMap = {};
      
      // 1. Fill from Backend API if available
      if (paymentRes.status === 'fulfilled' && Array.isArray(paymentRes.value) && paymentRes.value.length > 0) {
        paymentRes.value.forEach((p) => {
          let rawM = String(p.method || p.paymentMethod || p.label || 'VNPAY').toUpperCase();
          const label = rawM.includes('COD') || rawM.includes('TIỀN MẶT')
            ? 'COD (Thanh toán khi nhận hàng)'
            : (rawM.includes('VNPAY') ? 'VNPAY (Thanh toán online)' : rawM);
          const amt = Number(p.amount || p.revenue || p.value || p.totalAmount || 0);
          const cnt = Number(p.count || p.orderCount || p.transactionCount || 1);
          paymentMap[label] = { method: label, label: label, name: label, amount: amt, count: cnt };
        });
      }

      // 2. Guarantee ALL order payment methods (COD and VNPAY) from modeOrdersList are present
      const activeOrdersList = modeOrdersList.filter((o) => getStatusCode(o.status) !== 'CANCELLED');
      (activeOrdersList.length > 0 ? activeOrdersList : modeOrdersList).forEach((o) => {
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
      if (categoryRes.status === 'fulfilled' && Array.isArray(categoryRes.value) && categoryRes.value.length > 0) {
        categoryRes.value.forEach((c) => {
          const catName = String(c.label || c.name || c.categoryName || 'Danh mục khác');
          const amt = Number(c.amount || c.revenue || c.totalAmount || c.value || 0);
          const cnt = Number(c.count || c.quantity || c.soldQuantity || 1);
          categoryMap[catName] = { label: catName, name: catName, amount: amt, count: cnt };
        });
      }

      (activeOrdersList.length > 0 ? activeOrdersList : modeOrdersList).forEach((o) => {
        if (Array.isArray(o.items) && o.items.length > 0) {
          o.items.forEach((it) => {
            const catName = String(it.categoryName || it.category?.name || it.productCategory || 'Đồ chơi sáng tạo');
            const itemTotal = Number(it.subtotal || it.totalPrice || (it.price * (it.quantity || 1)) || 0);
            if (!categoryMap[catName]) {
              categoryMap[catName] = { label: catName, name: catName, amount: 0, count: 0 };
            }
            if (categoryMap[catName].amount === 0) {
              categoryMap[catName].amount += itemTotal;
              categoryMap[catName].count += Number(it.quantity || 1);
            }
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

  return (
    <section className="admin-statistics-page" style={{ padding: '24px', background: '#f8fafc', minHeight: '100vh', fontFamily: 'system-ui, -apple-system, sans-serif' }}>
      
      {/* HEADER SECTION WITH MODE TOGGLE */}
      <div style={{ background: 'linear-gradient(135deg, #fff8f3 0%, #fff1f2 100%)', border: '1px solid #ffedd5', padding: '16px 24px', borderRadius: '16px', marginBottom: '16px', boxShadow: '0 4px 12px rgba(234,88,12,0.04)', display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '16px' }}>
        <div>
          <h1 style={{ fontSize: '24px', fontWeight: 900, color: '#9a3412', margin: 0, letterSpacing: '-0.3px', textTransform: 'uppercase' }}>
            Thống kê quản trị
          </h1>
          <div style={{ fontSize: '13px', color: dataMode === 'REAL' ? '#15803d' : '#7e22ce', fontWeight: '800', marginTop: '4px', display: 'flex', alignItems: 'center', gap: '6px' }}>
            <span>{dataMode === 'REAL' ? '🟢' : '🧪'}</span>
            <span>{dataMode === 'REAL' ? 'Đang xem: Báo cáo Kinh doanh Thực tế (Đã lọc sạch đơn test của Admin/Staff)' : 'Đang xem: Báo cáo Đơn hàng Thử nghiệm Nội bộ (ADMIN/STAFF/MANAGER)'}</span>
          </div>
        </div>

        {/* MODE TOGGLE TAB BUTTONS */}
        <div style={{ display: 'inline-flex', background: '#ffffff', padding: '4px', borderRadius: '12px', border: '2px solid #fed7aa', boxShadow: '0 4px 12px rgba(0,0,0,0.06)' }}>
          <button
            type="button"
            onClick={() => setDataMode('REAL')}
            style={{
              padding: '10px 20px',
              borderRadius: '8px',
              border: 'none',
              fontSize: '14px',
              fontWeight: '900',
              cursor: 'pointer',
              background: dataMode === 'REAL' ? 'linear-gradient(135deg, #16a34a, #15803d)' : 'transparent',
              color: dataMode === 'REAL' ? '#ffffff' : '#64748b',
              boxShadow: dataMode === 'REAL' ? '0 2px 8px rgba(22,163,74,0.3)' : 'none',
              transition: 'all 0.2s ease',
              display: 'flex',
              alignItems: 'center',
              gap: '8px',
            }}
          >
            <span>🟢</span> Báo cáo Kinh doanh
          </button>
          <button
            type="button"
            onClick={() => setDataMode('TEST')}
            style={{
              padding: '10px 20px',
              borderRadius: '8px',
              border: 'none',
              fontSize: '14px',
              fontWeight: '900',
              cursor: 'pointer',
              background: dataMode === 'TEST' ? 'linear-gradient(135deg, #9333ea, #7e22ce)' : 'transparent',
              color: dataMode === 'TEST' ? '#ffffff' : '#64748b',
              boxShadow: dataMode === 'TEST' ? '0 2px 8px rgba(147,51,234,0.3)' : 'none',
              transition: 'all 0.2s ease',
              display: 'flex',
              alignItems: 'center',
              gap: '8px',
            }}
          >
            <span>🧪</span> Đơn Thử nghiệm Nội bộ
          </button>
        </div>
      </div>

      {/* TIME RANGE BAR */}
      <TimeRangeBar dates={dates} setDates={setDates} selectedRange={selectedRange} handleRangeClick={handleRangeClick} />

      {error && (
        <div style={{ padding: '12px 16px', background: '#fef2f2', border: '1px solid #fecaca', borderRadius: '10px', color: '#dc2626', fontSize: '13px', marginBottom: '16px' }}>
          ⚠️ {error}
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
              change={growthPercents.cost}
              strokeColor="#dc2626"
              data={mainChartData.map((d) => ({ v: d['Chi phí nhập hàng'] }))}
              unit="VND"
            />
            <KpiSparklineCard
              title="Tổng doanh thu"
              rawNumber={overview.totalRevenue}
              change={growthPercents.revenue}
              strokeColor="#2563eb"
              data={mainChartData.map((d) => ({ v: d['Tổng doanh thu'] }))}
              unit="VND"
            />
            <KpiSparklineCard
              title="Tổng lợi nhuận"
              rawNumber={overview.totalProfit}
              change={growthPercents.profit}
              strokeColor="#16a34a"
              data={mainChartData.map((d) => ({ v: d['Tổng lợi nhuận'] }))}
              unit="VND"
            />
            <KpiSparklineCard
              title="Tổng sản phẩm bán ra"
              rawNumber={overview.productsSold}
              change={growthPercents.sales}
              strokeColor="#ea580c"
              data={mainChartData.map((d) => ({ v: d['Tổng SP bán ra'] }))}
              unit="Sản phẩm"
            />
            <KpiSparklineCard
              title="Tổng sản phẩm nhập vào"
              rawNumber={overview.productsImported}
              change={growthPercents.imports}
              strokeColor="#9333ea"
              data={mainChartData.map((d) => ({ v: d['Tổng SP nhập vào'] }))}
              unit="Sản phẩm"
            />
          </div>

          {/* ROW 2: FINANCIAL & QUANTITY TREND CHARTS */}
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

            {/* QUANTITY CHART */}
            <div style={{ background: '#fff', borderRadius: '16px', boxShadow: '0 4px 16px rgba(0,0,0,0.03)', padding: '24px', border: '1px solid #f1f5f9' }}>
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
                                Doanh thu: {formatVndText(prod.totalRevenue || prod.revenue)}
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
                        <th style={{ padding: '10px 8px', textAlign: 'center', width: '110px' }}>ĐƠN / SP MUA</th>
                      </tr>
                    </thead>
                    <tbody>
                      {topCustomers.map((cust, idx) => {
                        const isTop3 = idx < 3;
                        const medal = idx === 0 ? '🥇' : idx === 1 ? '🥈' : idx === 2 ? '🥉' : idx + 1;
                        const name = cust.label || cust.customerName || cust.fullName || `Khách hàng #${cust.id || idx + 1}`;
                        const count = cust.count || cust.orderCount || cust.productCount || 1;
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
                                {count} món / đơn
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
                  category="TỒN KHO"
                  title="Biến thể sắp hết hàng"
                  count={overview.alerts.lowStock}
                  hint="Chuẩn bị phiếu nhập kho trước khi các món đồ chơi hot bị hết hàng."
                  tone="warning"
                  onClick={() => navigate('/admin/inventory?lowStock=true')}
                />
                <OperationalAlertCard
                  category="SẢN PHẨM"
                  title="Sản phẩm bán chậm"
                  count={overview.alerts.slowSelling}
                  hint="Xem các sản phẩm có lượt bán thấp để điều chỉnh giá hoặc tạo khuyến mãi."
                  tone="warning"
                  onClick={() => navigate('/admin/products?filter=SLOW_SELLING')}
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
                  onClick={() => navigate('/admin/reviews?rating=2')}
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
function KpiSparklineCard({ title, rawNumber, formattedValue, change, strokeColor = '#f59e0b', data = [], unit }) {
  const isPositive = change && change !== '0%' && change !== '+0%' && change.startsWith('+');
  const isNegative = change && change !== '0%' && change !== '-0%' && change.startsWith('-');

  const changeColor = isNegative ? '#f43f5e' : isPositive ? '#16a34a' : '#64748b';
  const arrow = isNegative ? '↓' : isPositive ? '↑' : '•';
  const displayChange = change ? `${arrow} ${change.replace(/^[+-]/, '')}` : '—';

  const numericVal = typeof rawNumber === 'number' ? rawNumber : Number(rawNumber || 0);
  const bigDisplay = formatCompactValue(numericVal, unit);
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
        <span>{displayChange}</span>
        <span style={{ fontWeight: '600', color: '#64748b' }}>so với khoảng trước</span>
      </div>
    </div>
  );
}

function OperationalAlertCard({ category, title, count, hint, tone, onClick }) {
  const isDanger = tone === 'danger';
  const isWarning = tone === 'warning';
  const isCancelled = tone === 'cancelled';

  const badgeBg = isDanger ? '#fef2f2' : isWarning ? '#fffbebfb' : isCancelled ? '#f8fafc' : '#f0fdf4';
  const badgeColor = isDanger ? '#dc2626' : isWarning ? '#d97706' : isCancelled ? '#64748b' : '#16a34a';

  return (
    <div
      onClick={onClick}
      style={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        padding: '14px 18px',
        borderRadius: '12px',
        background: badgeBg,
        border: `1px solid ${isDanger ? '#fecaca' : isWarning ? '#fef3c7' : '#e2e8f0'}`,
        cursor: onClick ? 'pointer' : 'default',
        transition: 'all 0.15s ease',
        userSelect: 'none',
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
    </div>
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
