import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { sampleOrders } from '../../data/sampleData.js';
import { getAdminOrders } from '../../services/orderService.js';
import { getStatisticsOverview } from '../../services/statisticsService.js';
import { formatDateTime, formatPrice, getOrderStatusLabel } from '../../utils/formatters.js';

const fallbackOverview = {
  kpis: [
    { code: 'NET_REVENUE', label: 'Net revenue', value: 12500000, changePercent: 12.5 },
    { code: 'CREATED_ORDER_COUNT', label: 'Created orders', value: 18, changePercent: 4.2 },
    { code: 'SUCCESSFUL_ORDER_COUNT', label: 'Successful orders', value: 11, changePercent: 8.1 },
    { code: 'CANCELLED_ORDER_COUNT', label: 'Cancelled orders', value: 2, changePercent: -3.4 },
  ],
  inventory: {
    lowStockVariantCount: 7,
  },
  revenueTrend: [
    { period: 'Mon', netRevenue: 1200000, orderCount: 4 },
    { period: 'Tue', netRevenue: 2100000, orderCount: 7 },
    { period: 'Wed', netRevenue: 1800000, orderCount: 6 },
    { period: 'Thu', netRevenue: 3200000, orderCount: 9 },
    { period: 'Fri', netRevenue: 2600000, orderCount: 8 },
    { period: 'Sat', netRevenue: 4100000, orderCount: 12 },
    { period: 'Sun', netRevenue: 3000000, orderCount: 10 },
  ],
  orderStatus: [
    { code: 'PENDING', label: 'Pending', count: 18 },
    { code: 'CONFIRMED', label: 'Confirmed', count: 11 },
    { code: 'SHIPPED', label: 'Shipped', count: 9 },
    { code: 'COMPLETED', label: 'Completed', count: 24 },
    { code: 'CANCELLED', label: 'Cancelled', count: 2 },
  ],
  paymentStatus: [
    { code: 'PENDING', label: 'Pending', count: 3 },
    { code: 'FAILED', label: 'Failed', count: 1 },
  ],
};

function AdminDashboardPage() {
  const [overview, setOverview] = useState(null);
  const [recentOrders, setRecentOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [notice, setNotice] = useState('');

  useEffect(() => {
    let active = true;
    const params = buildOverviewParams();

    Promise.all([
      getStatisticsOverview(params),
      getAdminOrders({ page: 0, size: 5, sort: 'createdAt,desc' }),
    ])
      .then(([overviewResult, orderResult]) => {
        if (!active) {
          return;
        }

        setOverview(overviewResult);
        setRecentOrders(orderResult.content || []);
      })
      .catch(() => {
        if (!active) {
          return;
        }

        setOverview(fallbackOverview);
        setRecentOrders(sampleOrders);
        setNotice('Backend chưa sẵn sàng, đang hiển thị dashboard mẫu để xem giao diện. Dữ liệu mẫu này không tính vào số liệu thực tế.');
      })
      .finally(() => {
        if (active) {
          setLoading(false);
        }
      });

    return () => {
      active = false;
    };
  }, []);

  const statCards = useMemo(() => buildStatCards(overview), [overview]);
  const alertCards = useMemo(() => buildAlertCards(overview), [overview]);
  const revenueBars = useMemo(() => buildRevenueBars(overview?.revenueTrend || []), [overview]);
  const orderSegments = useMemo(() => buildStatusSegments(overview?.orderStatus || []), [overview]);

  return (
    <div className="admin-dashboard">
      <section className="admin-dashboard__hero">
        <div>
          <p>Overview</p>
          <h2>Store health at a glance</h2>
          <span>Focus on money, orders, inventory, and customer trust first.</span>
        </div>
        <Link to="/admin/orders">View orders</Link>
      </section>

      {notice && <div className="form-alert form-alert--soft">{notice}</div>}

      <section className="admin-stat-grid">
        {statCards.map((card) => (
          <article className={`admin-stat-card admin-stat-card--${card.tone}`} key={card.label}>
            <span>{card.label}</span>
            <strong>{card.value}</strong>
            {card.change && <small>{card.change}</small>}
          </article>
        ))}
      </section>

      <section className="admin-dashboard-grid">
        <div className="admin-panel">
          <div className="admin-panel__heading">
            <h3>Recent orders</h3>
            <Link to="/admin/orders">See all</Link>
          </div>

          <div className="admin-table">
            <div className="admin-table__row admin-table__row--head">
              <span>Order</span>
              <span>Date</span>
              <span>Total</span>
              <span>Status</span>
            </div>
            {loading && (
              <div className="admin-table__row">
                <span>Loading</span>
                <span>Fetching recent orders...</span>
                <span>-</span>
                <span>-</span>
              </div>
            )}
            {!loading && recentOrders.map((order) => (
              <div className="admin-table__row" key={order.id}>
                <span>#{order.id}</span>
                <span>{formatDateTime(order.createdAt)}</span>
                <span>{formatPrice(order.totalAmount)}</span>
                <span>{getOrderStatusLabel(order.status)}</span>
              </div>
            ))}
            {!loading && recentOrders.length === 0 && (
              <div className="admin-table__row">
                <span>Empty</span>
                <span>No recent orders</span>
                <span>-</span>
                <span>-</span>
              </div>
            )}
          </div>
        </div>

        <div className="admin-panel">
          <div className="admin-panel__heading">
            <h3>Alerts</h3>
            <span>Needs attention</span>
          </div>

          <div className="admin-alert-grid">
            {alertCards.map((alert) => (
              <article className={`admin-alert-card admin-alert-card--${alert.tone}`} key={alert.title}>
                <span>{alert.label}</span>
                <strong>{alert.value}</strong>
                <p>{alert.title}</p>
                <small>{alert.hint}</small>
              </article>
            ))}
          </div>
        </div>
      </section>

      <section className="admin-dashboard-grid">
        <div className="admin-panel">
          <div className="admin-panel__heading">
            <h3>Revenue trend</h3>
            <span>Last 7 days</span>
          </div>

          <div className="css-bar-chart">
            {revenueBars.map((point) => (
              <div className="css-bar-chart__item" key={point.period}>
                <div className="css-bar-chart__track">
                  <span style={{ height: `${point.height}%` }} title={point.valueLabel} />
                </div>
                <strong>{point.period}</strong>
                <small>{point.shortValue}</small>
              </div>
            ))}
          </div>
        </div>

        <div className="admin-panel">
          <div className="admin-panel__heading">
            <h3>Order status</h3>
            <span>Current period</span>
          </div>

          <div className="css-segment-chart">
            <div className="css-segment-chart__bar">
              {orderSegments.map((segment) => (
                <span
                  className={`css-segment-chart__segment css-segment-chart__segment--${segment.tone}`}
                  style={{ width: `${segment.percent}%` }}
                  title={`${segment.label}: ${segment.count}`}
                  key={segment.code}
                />
              ))}
            </div>

            <div className="css-segment-chart__legend">
              {orderSegments.map((segment) => (
                <div key={segment.code}>
                  <span className={`legend-dot legend-dot--${segment.tone}`} />
                  <strong>{segment.label}</strong>
                  <small>{segment.count}</small>
                </div>
              ))}
            </div>
          </div>
        </div>
      </section>
    </div>
  );
}

function buildOverviewParams() {
  const today = new Date();
  const from = new Date(today);
  from.setDate(today.getDate() - 6);

  return {
    from: toIsoDate(from),
    to: toIsoDate(today),
    timezone: 'Asia/Ho_Chi_Minh',
    groupBy: 'DAY',
    compareWithPreviousPeriod: 'true',
    topLimit: '5',
    lowStockThreshold: '5',
  };
}

function buildStatCards(overview) {
  const kpis = overview?.kpis || [];
  const netRevenue = findKpi(kpis, 'NET_REVENUE');
  const createdOrders = findKpi(kpis, 'CREATED_ORDER_COUNT');
  const successfulOrders = findKpi(kpis, 'SUCCESSFUL_ORDER_COUNT');
  const cancelledOrders = findKpi(kpis, 'CANCELLED_ORDER_COUNT');

  return [
    {
      label: 'Net revenue',
      value: formatPrice(netRevenue?.value),
      change: formatChange(netRevenue?.changePercent),
      tone: 'primary',
    },
    {
      label: 'Created orders',
      value: String(Math.round(createdOrders?.value || 0)),
      change: formatChange(createdOrders?.changePercent),
      tone: 'warning',
    },
    {
      label: 'Successful orders',
      value: String(Math.round(successfulOrders?.value || 0)),
      change: formatChange(successfulOrders?.changePercent),
      tone: 'info',
    },
    {
      label: 'Low stock variants',
      value: String(overview?.inventory?.lowStockVariantCount || 0),
      change: cancelledOrders ? `${Math.round(cancelledOrders.value || 0)} cancelled` : '',
      tone: 'danger',
    },
  ];
}

function buildAlertCards(overview) {
  const paymentPending = findStatusCount(overview?.paymentStatus, 'PENDING');
  const paymentFailed = findStatusCount(overview?.paymentStatus, 'FAILED');
  const cancelledOrders = findStatusCount(overview?.orderStatus, 'CANCELLED');
  const lowStock = overview?.inventory?.lowStockVariantCount || 0;

  return [
    {
      label: 'Payment',
      title: 'Pending payments',
      value: String(paymentPending),
      hint: 'Check VNPAY/COD payments that still need confirmation.',
      tone: paymentPending > 0 ? 'warning' : 'success',
    },
    {
      label: 'Inventory',
      title: 'Low stock variants',
      value: String(lowStock),
      hint: 'Prepare import notes before popular toys run out.',
      tone: lowStock > 0 ? 'danger' : 'success',
    },
    {
      label: 'Payment',
      title: 'Failed payments',
      value: String(paymentFailed),
      hint: 'Review failed transactions before customers retry.',
      tone: paymentFailed > 0 ? 'danger' : 'success',
    },
    {
      label: 'Order',
      title: 'Cancelled orders',
      value: String(cancelledOrders),
      hint: 'Watch cancellation reasons for service or stock issues.',
      tone: cancelledOrders > 0 ? 'info' : 'success',
    },
  ];
}

function buildRevenueBars(trend) {
  const points = trend.length > 0 ? trend : fallbackOverview.revenueTrend;
  const maxRevenue = Math.max(...points.map((point) => point.netRevenue || 0), 1);

  return points.map((point) => ({
    period: point.period,
    height: Math.max(8, Math.round(((point.netRevenue || 0) / maxRevenue) * 100)),
    valueLabel: formatPrice(point.netRevenue),
    shortValue: compactMoney(point.netRevenue),
  }));
}

function buildStatusSegments(statusCounts) {
  const counts = statusCounts.length > 0 ? statusCounts : fallbackOverview.orderStatus;
  const total = counts.reduce((sum, item) => sum + (item.count || 0), 0) || 1;

  return counts.map((item) => ({
    code: item.code,
    label: item.label || item.code,
    count: item.count || 0,
    percent: Math.max(4, Math.round(((item.count || 0) / total) * 100)),
    tone: statusTone(item.code),
  }));
}

function findKpi(kpis, code) {
  return kpis.find((kpi) => kpi.code === code);
}

function findStatusCount(items = [], code) {
  return items.find((item) => item.code === code)?.count || 0;
}

function formatChange(changePercent) {
  if (changePercent === null || changePercent === undefined) {
    return '';
  }

  const sign = changePercent > 0 ? '+' : '';
  return `${sign}${changePercent}% vs previous period`;
}

function toIsoDate(value) {
  return value.toISOString().slice(0, 10);
}

function compactMoney(value = 0) {
  if (value >= 1000000) {
    return `${Math.round(value / 100000) / 10}M`;
  }

  if (value >= 1000) {
    return `${Math.round(value / 1000)}K`;
  }

  return String(Math.round(value));
}

function statusTone(code) {
  const tones = {
    PENDING: 'warning',
    CONFIRMED: 'info',
    SHIPPED: 'primary',
    COMPLETED: 'success',
    CANCELLED: 'danger',
  };

  return tones[code] || 'primary';
}

export default AdminDashboardPage;
