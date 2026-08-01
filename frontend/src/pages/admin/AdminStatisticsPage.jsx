import { useState } from 'react';
import { getStatisticsOverview, getTopSellingProducts } from '../../services/statisticsService.js';
import { formatPrice } from '../../utils/formatters.js';

function AdminStatisticsPage() {
  const [filters, setFilters] = useState({
    from: '',
    to: '',
    timezone: 'Asia/Ho_Chi_Minh',
    groupBy: 'DAY',
    topLimit: '10',
    lowStockThreshold: '5',
  });
  const [overview, setOverview] = useState(null);
  const [topSelling, setTopSelling] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  function updateField(field, value) {
    setFilters((current) => ({ ...current, [field]: value }));
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setLoading(true);
    setError('');

    try {
      const params = Object.fromEntries(Object.entries(filters).filter(([, value]) => value !== ''));
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
          <p>Admin API</p>
          <h2>Statistics</h2>
          <span>Run overview statistics and top-selling product reports.</span>
        </div>
        <strong>{topSelling.length} top products</strong>
      </div>

      <form className="admin-filter" onSubmit={handleSubmit}>
        {Object.keys(filters).map((field) => (
          <label key={field}>
            {field}
            <input
              value={filters[field]}
              onChange={(event) => updateField(field, event.target.value)}
              placeholder={field}
            />
          </label>
        ))}
        <button type="submit" disabled={loading}>{loading ? 'Loading...' : 'Load statistics'}</button>
      </form>

      {error && <div className="form-alert">{error}</div>}

      {overview && (
        <div className="admin-dashboard-grid">
          {(overview.kpis || []).map((kpi) => (
            <article className="admin-stat-card" key={kpi.code || kpi.label}>
              <span>{kpi.label || kpi.code}</span>
              <strong>{typeof kpi.value === 'number' && String(kpi.code).includes('REVENUE') ? formatPrice(kpi.value) : kpi.value}</strong>
              {kpi.changeRate !== undefined && <small>{kpi.changeRate}% vs previous</small>}
            </article>
          ))}
        </div>
      )}

      <div className="admin-resource-table">
        <div
          className="admin-resource-table__head"
          style={{ gridTemplateColumns: 'repeat(6, minmax(130px, 1fr))' }}
        >
          <span>productId</span>
          <span>productName</span>
          <span>soldQuantity</span>
          <span>netRevenue</span>
          <span>orderCount</span>
          <span>rank</span>
        </div>
        {topSelling.map((product, index) => (
          <div
            className="admin-resource-table__row"
            style={{ gridTemplateColumns: 'repeat(6, minmax(130px, 1fr))' }}
            key={product.productId || index}
          >
            <span>{product.productId || '-'}</span>
            <span>{product.productName || '-'}</span>
            <span>{product.soldQuantity || 0}</span>
            <span>{formatPrice(product.netRevenue || product.revenue || 0)}</span>
            <span>{product.orderCount || 0}</span>
            <span>{product.rank || index + 1}</span>
          </div>
        ))}
      </div>
    </section>
  );
}

export default AdminStatisticsPage;
