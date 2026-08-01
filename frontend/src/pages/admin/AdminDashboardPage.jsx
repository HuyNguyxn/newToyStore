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
};

const alerts = [
  '3 VNPAY payments are still pending.',
  '7 product variants are close to out of stock.',
  '5 reviews need moderation before publishing.',
  '2 return requests need staff inspection.',
];

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
        setNotice('Backend chua san sang, dang hien thi dashboard mau.');
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

          <div className="admin-alert-list">
            {alerts.map((alert) => (
              <div className="admin-alert" key={alert}>
                {alert}
              </div>
            ))}
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

function findKpi(kpis, code) {
  return kpis.find((kpi) => kpi.code === code);
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

export default AdminDashboardPage;
