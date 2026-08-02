import { useEffect, useState } from 'react';
import { deleteAdminUser, getAdminUserDetails, getAdminUsers, lockAdminUser, unlockAdminUser, updateAdminUserRole, updateAdminUserStatus } from '../../services/adminUserService.js';
import { formatDateTime } from '../../utils/formatters.js';

const roles = ['CUSTOMER', 'STAFF', 'MANAGER', 'ADMIN'];
const statuses = ['UNVERIFIED', 'ACTIVE', 'LOCKED'];

function AdminUserPage() {
  const [users, setUsers] = useState([]);
  const [selected, setSelected] = useState(null);
  const [filters, setFilters] = useState({ keyword: '', role: '', status: '' });
  const [role, setRole] = useState('CUSTOMER');
  const [status, setStatus] = useState('ACTIVE');
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  useEffect(() => { loadUsers(); }, []);

  async function loadUsers() {
    try {
      const result = await getAdminUsers({ ...filters, page: 0, size: 20, sort: 'createdAt,desc' });
      setUsers(result.content || []);
    } catch (err) {
      setError(err.message || 'Kh?ng th? t?i users.');
    }
  }

  async function selectUser(user) {
    setSelected(user);
    setRole(user.role || 'CUSTOMER');
    setStatus(user.status || 'ACTIVE');
    try {
      const result = await getAdminUserDetails(user.id);
      setSelected(result);
      setRole(result.role || 'CUSTOMER');
      setStatus(result.status || 'ACTIVE');
    } catch {
      // Use row data if detail request fails.
    }
  }

  async function doAction(action, success) {
    setError('');
    setMessage('');
    try {
      const result = await action();
      setMessage(success);
      if (result) setSelected(result);
      await loadUsers();
    } catch (err) {
      setError(err.message || 'Thao t?c user th?t b?i.');
    }
  }

  return (
    <section className="admin-resource">
      <div className="admin-resource__hero"><div><p>Admin CRM</p><h2>Users</h2><span>Manage account role, status, lock state, and customer profile detail.</span></div></div>
      {error && <div className="form-alert">{error}</div>}{message && <div className="form-alert form-alert--success">{message}</div>}
      <form className="admin-filter" onSubmit={(e) => { e.preventDefault(); loadUsers(); }}>{Object.keys(filters).map((field) => <label key={field}>{field}<input value={filters[field]} onChange={(e) => setFilters((current) => ({ ...current, [field]: e.target.value }))} /></label>)}<button type="submit">Filter</button></form>

      <div className="admin-crud-grid">
        <div className="admin-resource-table">
          <div className="admin-resource-table__head" style={{ gridTemplateColumns: '70px 1fr 1fr 110px 120px 170px 160px' }}><span>ID</span><span>Email</span><span>Name</span><span>Role</span><span>Status</span><span>Created</span><span>Actions</span></div>
          {users.map((user) => <div className="admin-resource-table__row" style={{ gridTemplateColumns: '70px 1fr 1fr 110px 120px 170px 160px' }} key={user.id}><span>{user.id}</span><span>{user.email}</span><span>{user.fullName || '-'}</span><span>{user.role}</span><span>{user.status}</span><span>{formatDateTime(user.createdAt)}</span><span className="admin-resource-table__actions"><button type="button" onClick={() => selectUser(user)}>Manage</button><button type="button" className="is-danger" onClick={() => doAction(() => deleteAdminUser(user.id), '?? x?a user.')}>Delete</button></span></div>)}
        </div>

        <aside className="admin-api-console">
          <div className="admin-panel__heading"><div><p>Selected</p><h2>{selected ? selected.email : 'Choose a user'}</h2></div></div>
          {selected && (
            <>
              <div className="admin-detail-summary">
                <p><strong>ID:</strong> {selected.id}</p>
                <p><strong>Full name:</strong> {selected.fullName || '-'}</p>
                <p><strong>Phone:</strong> {selected.phoneNumber || '-'}</p>
              </div>
              <div className="admin-api-console__row">
                <label>Role<select value={role} onChange={(e) => setRole(e.target.value)}>{roles.map((item) => <option value={item} key={item}>{item}</option>)}</select></label>
                <label>Status<select value={status} onChange={(e) => setStatus(e.target.value)}>{statuses.map((item) => <option value={item} key={item}>{item}</option>)}</select></label>
              </div>
              <div className="admin-resource-table__actions">
                <button type="button" onClick={() => doAction(() => updateAdminUserRole(selected.id, role), '?? c?p nh?t role user.')}>Update role</button>
                <button type="button" onClick={() => doAction(() => updateAdminUserStatus(selected.id, status), '?? c?p nh?t status user.')}>Update status</button>
                <button type="button" className="is-danger" onClick={() => doAction(() => lockAdminUser(selected.id), '?? lock user.')}>Lock</button>
                <button type="button" onClick={() => doAction(() => unlockAdminUser(selected.id), '?? unlock user.')}>Unlock</button>
              </div>
              <div className="admin-line-items">
                <strong>Addresses</strong>
                {(!selected.addresses || selected.addresses.length === 0) && <div className="empty-state">Chua co address.</div>}
                {(selected.addresses || []).map((address) => <div className="admin-log-list__item" key={address.id}><strong>{address.receiverName || 'Address'} {address.defaultAddress ? '· Default' : ''}</strong><p>{address.phoneNumber || ''} · {address.fullAddress || address.addressLine || '-'}</p></div>)}
              </div>
            </>
          )}
        </aside>
      </div>
    </section>
  );
}

export default AdminUserPage;
