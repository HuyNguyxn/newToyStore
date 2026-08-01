import { Link } from 'react-router-dom';
import { formatPrice } from '../../utils/formatters.js';

const statCards = [
  { label: 'Revenue today', value: formatPrice(12500000), tone: 'primary' },
  { label: 'Orders pending', value: '18', tone: 'warning' },
  { label: 'Low stock items', value: '7', tone: 'danger' },
  { label: 'Reviews waiting', value: '5', tone: 'info' },
];

const recentOrders = [
  { id: 1024, customer: 'Nguyen Minh Anh', total: 780000, status: 'Pending' },
  { id: 1023, customer: 'Tran Hoang Nam', total: 1250000, status: 'Confirmed' },
  { id: 1022, customer: 'Le Bao Ngoc', total: 430000, status: 'Shipped' },
];

const alerts = [
  '3 VNPAY payments are still pending.',
  '7 product variants are close to out of stock.',
  '5 reviews need moderation before publishing.',
  '2 return requests need staff inspection.',
];

function AdminDashboardPage() {
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

      <section className="admin-stat-grid">
        {statCards.map((card) => (
          <article className={`admin-stat-card admin-stat-card--${card.tone}`} key={card.label}>
            <span>{card.label}</span>
            <strong>{card.value}</strong>
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
              <span>Customer</span>
              <span>Total</span>
              <span>Status</span>
            </div>
            {recentOrders.map((order) => (
              <div className="admin-table__row" key={order.id}>
                <span>#{order.id}</span>
                <span>{order.customer}</span>
                <span>{formatPrice(order.total)}</span>
                <span>{order.status}</span>
              </div>
            ))}
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

export default AdminDashboardPage;
