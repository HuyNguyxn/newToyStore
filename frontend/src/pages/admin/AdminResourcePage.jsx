import { useEffect, useMemo, useState } from 'react';
import {
  adminResourceConfigs,
  getAdminResource,
  runAdminAction,
  runAdminJsonRequest,
} from '../../services/adminService.js';
import { formatDateTime, formatPrice } from '../../utils/formatters.js';

function AdminResourcePage({ resource }) {
  const config = adminResourceConfigs[resource];
  const [items, setItems] = useState([]);
  const [pageInfo, setPageInfo] = useState({ number: 0, totalPages: 1, totalElements: 0 });
  const [filters, setFilters] = useState({});
  const [selectedItem, setSelectedItem] = useState(null);
  const [loading, setLoading] = useState(true);
  const [actingKey, setActingKey] = useState('');
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [consoleRequest, setConsoleRequest] = useState({
    method: 'GET',
    endpoint: config.endpoint,
    bodyText: '',
  });
  const [consoleResponse, setConsoleResponse] = useState('');
  const [consoleLoading, setConsoleLoading] = useState(false);

  const pageParams = useMemo(() => ({
    ...config.defaultParams,
    ...filters,
  }), [config, filters]);
  const tableGridStyle = {
    gridTemplateColumns: `repeat(${config.columns.length}, minmax(130px, 1fr)) minmax(220px, 1.5fr)`,
  };

  useEffect(() => {
    loadData(0);
    setConsoleRequest({
      method: 'GET',
      endpoint: config.endpoint,
      bodyText: '',
    });
    setConsoleResponse('');
  }, [resource]);

  if (!config) {
    return <div className="page-message">Admin module khong ton tai.</div>;
  }

  function normalizePage(result) {
    if (Array.isArray(result)) {
      return {
        content: result,
        number: 0,
        totalPages: 1,
        totalElements: result.length,
      };
    }

    return {
      content: result?.content || [],
      number: result?.number || 0,
      totalPages: result?.totalPages || 1,
      totalElements: result?.totalElements || result?.content?.length || 0,
    };
  }

  function loadData(nextPage = pageInfo.number) {
    setLoading(true);
    setMessage('');
    setError('');

    getAdminResource(config.endpoint, { ...pageParams, page: nextPage })
      .then((result) => {
        const next = normalizePage(result);
        setItems(next.content);
        setPageInfo({
          number: next.number,
          totalPages: next.totalPages,
          totalElements: next.totalElements,
        });
      })
      .catch((err) => {
        setItems([]);
        setError(err.message || 'Khong the tai du lieu quan tri.');
      })
      .finally(() => setLoading(false));
  }

  function handleFilterChange(field, value) {
    setFilters((current) => ({ ...current, [field]: value }));
  }

  function handleFilterSubmit(event) {
    event.preventDefault();
    loadData(0);
  }

  async function handleAction(action, item) {
    const confirmed = !action.danger || window.confirm(`Xac nhan ${action.label.toLowerCase()} item #${item.id}?`);
    if (!confirmed) {
      return;
    }

    const key = `${action.label}-${item.id}`;
    setActingKey(key);
    setMessage('');
    setError('');

    try {
      await runAdminAction({
        endpoint: action.endpoint(item),
        method: action.method,
        body: typeof action.body === 'function' ? action.body(item) : action.body,
      });
      setMessage(`Da thuc hien: ${action.label} #${item.id}.`);
      loadData(pageInfo.number);
    } catch (err) {
      setError(err.message || `Khong the thuc hien ${action.label}.`);
    } finally {
      setActingKey('');
    }
  }

  async function handleConsoleSubmit(event) {
    event.preventDefault();
    setConsoleLoading(true);
    setMessage('');
    setError('');
    setConsoleResponse('');

    try {
      if (consoleRequest.bodyText.trim()) {
        JSON.parse(consoleRequest.bodyText);
      }

      const result = await runAdminJsonRequest(consoleRequest);
      setConsoleResponse(result === null ? 'No content' : JSON.stringify(result, null, 2));
      setMessage('Admin API request thanh cong.');
      loadData(pageInfo.number);
    } catch (err) {
      setError(err.message || 'Admin API request that bai. Kiem tra endpoint, method hoac JSON body.');
      if (err && typeof err === 'object') {
        setConsoleResponse(JSON.stringify(err, null, 2));
      }
    } finally {
      setConsoleLoading(false);
    }
  }

  function renderCell(item, column) {
    const value = item?.[column];

    if (value === null || value === undefined || value === '') {
      return '-';
    }

    if (column.toLowerCase().includes('amount') || column.toLowerCase().includes('price')) {
      return formatPrice(value);
    }

    if (column.toLowerCase().includes('date') || column.endsWith('At')) {
      return formatDateTime(value);
    }

    if (typeof value === 'object') {
      return JSON.stringify(value);
    }

    return String(value);
  }

  if (loading) {
    return <div className="page-message">Dang tai {config.title.toLowerCase()}...</div>;
  }

  return (
    <section className="admin-resource">
      <div className="admin-resource__hero">
        <div>
          <p>Admin API</p>
          <h2>{config.title}</h2>
          <span>{config.description}</span>
        </div>
        <strong>{pageInfo.totalElements} records</strong>
      </div>

      <form className="admin-filter" onSubmit={handleFilterSubmit}>
        {(config.filters || []).map((field) => (
          <label key={field}>
            {field}
            <input
              value={filters[field] || ''}
              onChange={(event) => handleFilterChange(field, event.target.value)}
              placeholder={`Filter by ${field}`}
            />
          </label>
        ))}
        <button type="submit">Filter</button>
        <button type="button" onClick={() => { setFilters({}); setTimeout(() => loadData(0), 0); }}>
          Reset
        </button>
      </form>

      {error && <div className="form-alert">{error}</div>}
      {message && <div className="form-alert form-alert--success">{message}</div>}

      <form className="admin-api-console" onSubmit={handleConsoleSubmit}>
        <div className="admin-panel__heading">
          <div>
            <p>Advanced API Console</p>
            <h2>Run admin endpoint</h2>
          </div>
        </div>

        <div className="admin-api-console__row">
          <label>
            Method
            <select
              value={consoleRequest.method}
              onChange={(event) => setConsoleRequest((current) => ({ ...current, method: event.target.value }))}
            >
              <option>GET</option>
              <option>POST</option>
              <option>PUT</option>
              <option>PATCH</option>
              <option>DELETE</option>
            </select>
          </label>

          <label>
            Endpoint
            <input
              value={consoleRequest.endpoint}
              onChange={(event) => setConsoleRequest((current) => ({ ...current, endpoint: event.target.value }))}
              placeholder="/products"
              required
            />
          </label>
        </div>

        <label>
          JSON body
          <textarea
            value={consoleRequest.bodyText}
            onChange={(event) => setConsoleRequest((current) => ({ ...current, bodyText: event.target.value }))}
            placeholder='{"name":"Example"}'
            rows="5"
          />
        </label>

        <button type="submit" disabled={consoleLoading}>
          {consoleLoading ? 'Running...' : 'Run API'}
        </button>

        {consoleResponse && <pre>{consoleResponse}</pre>}
      </form>

      <div className="admin-resource-table">
        <div className="admin-resource-table__head" style={tableGridStyle}>
          {(config.columns || []).map((column) => <span key={column}>{column}</span>)}
          <span>actions</span>
        </div>

        {items.map((item) => (
          <div className="admin-resource-table__row" style={tableGridStyle} key={item.id || JSON.stringify(item)}>
            {(config.columns || []).map((column) => (
              <span key={column}>{renderCell(item, column)}</span>
            ))}
            <span className="admin-resource-table__actions">
              <button type="button" onClick={() => setSelectedItem(item)}>View</button>
              {(config.actions || []).map((action) => (
                <button
                  type="button"
                  key={action.label}
                  className={action.danger ? 'is-danger' : ''}
                  disabled={actingKey === `${action.label}-${item.id}`}
                  onClick={() => handleAction(action, item)}
                >
                  {action.label}
                </button>
              ))}
            </span>
          </div>
        ))}
      </div>

      {items.length === 0 && <div className="empty-state">Chua co du lieu de hien thi.</div>}

      <div className="pagination-bar">
        <button type="button" disabled={pageInfo.number <= 0} onClick={() => loadData(pageInfo.number - 1)}>
          Truoc
        </button>
        <span>Trang {pageInfo.number + 1} / {pageInfo.totalPages}</span>
        <button
          type="button"
          disabled={pageInfo.number + 1 >= pageInfo.totalPages}
          onClick={() => loadData(pageInfo.number + 1)}
        >
          Sau
        </button>
      </div>

      {selectedItem && (
        <div className="admin-detail-modal" role="dialog" aria-modal="true">
          <div className="admin-detail-modal__panel">
            <div className="admin-panel__heading">
              <div>
                <p>Record detail</p>
                <h2>{config.title} #{selectedItem.id || 'detail'}</h2>
              </div>
              <button type="button" onClick={() => setSelectedItem(null)}>Close</button>
            </div>
            <pre>{JSON.stringify(selectedItem, null, 2)}</pre>
          </div>
        </div>
      )}
    </section>
  );
}

export default AdminResourcePage;
