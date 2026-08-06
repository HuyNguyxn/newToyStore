import { useEffect, useMemo, useRef, useState } from 'react';
import {
  deleteAdminUser,
  getAdminUserDetails,
  getAdminUsers,
  lockAdminUser,
  unlockAdminUser,
  updateAdminUserRole,
  updateAdminUserStatus,
} from '../../services/adminUserService.js';
import { formatDateTime } from '../../utils/formatters.js';

const roleOptions = [
  { value: 'CUSTOMER', label: 'Khách hàng', bg: '#f1f5f9', color: '#475569', border: '#cbd5e1' },
  { value: 'STAFF', label: 'Nhân viên', bg: '#eff6ff', color: '#2563eb', border: '#bfdbfe' },
  { value: 'MANAGER', label: 'Quản lý', bg: '#fffbebfb', color: '#d97706', border: '#fef3c7' },
  { value: 'ADMIN', label: 'Admin', bg: '#fef2f2', color: '#dc2626', border: '#fecaca' },
];

const statusOptions = [
  { value: 'ACTIVE', label: '• Hoạt động', bg: '#f0fdf4', color: '#16a34a', border: '#bbf7d0' },
  { value: 'LOCKED', label: '• Đã khóa', bg: '#fef2f2', color: '#dc2626', border: '#fecaca' },
  { value: 'UNVERIFIED', label: '• Chờ xác thực', bg: '#fffbebfb', color: '#d97706', border: '#fef3c7' },
];

/* Helper to generate user avatar initials and background color */
const avatarColors = [
  'linear-gradient(135deg, #06b6d4, #0891b2)',
  'linear-gradient(135deg, #3b82f6, #1d4ed8)',
  'linear-gradient(135deg, #a855f7, #7e22ce)',
  'linear-gradient(135deg, #f97316, #c2410c)',
  'linear-gradient(135deg, #ef4444, #b91c1c)',
  'linear-gradient(135deg, #10b981, #047857)',
];

function getUserInitials(name, email) {
  if (name && name.trim()) {
    const parts = name.trim().split(' ');
    if (parts.length >= 2) {
      return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
    }
    return name.slice(0, 2).toUpperCase();
  }
  if (email && email.trim()) {
    return email.slice(0, 2).toUpperCase();
  }
  return 'US';
}

function getAvatarColor(id) {
  const index = Math.abs(Number(id) || 0) % avatarColors.length;
  return avatarColors[index];
}

function AdminUserPage() {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedUser, setSelectedUser] = useState(null);
  const [loadingDetails, setLoadingDetails] = useState(false);
  const [activeMenuId, setActiveMenuId] = useState(null);

  // Filters State
  const [filters, setFilters] = useState({ keyword: '', role: '', status: '' });
  const [selectedIds, setSelectedIds] = useState([]);

  // Role/Status Modal Edit State
  const [editRoleModalUser, setEditRoleModalUser] = useState(null);
  const [selectedRole, setSelectedRole] = useState('CUSTOMER');

  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [actionLoading, setActionLoading] = useState(false);

  const menuRef = useRef(null);

  useEffect(() => {
    loadUsers();
  }, []);

  // Trigger backend fetch automatically when filters change with 300ms debounce
  useEffect(() => {
    const timer = setTimeout(() => {
      loadUsers();
    }, 300);
    return () => clearTimeout(timer);
  }, [filters.keyword, filters.role, filters.status]);

  useEffect(() => {
    function handleClickOutside(e) {
      if (menuRef.current && !menuRef.current.contains(e.target)) {
        setActiveMenuId(null);
      }
    }
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  async function loadUsers() {
    setLoading(true);
    setError('');
    try {
      const params = { page: 0, size: 100, sort: 'createdAt,desc' };
      if (filters.keyword.trim()) params.keyword = filters.keyword.trim();
      if (filters.role) params.role = filters.role;
      if (filters.status) params.status = filters.status;

      const result = await getAdminUsers(params);
      let list = result?.content || result || [];
      setUsers(list);
    } catch (err) {
      setError(err?.message || 'Không thể tải danh sách người dùng.');
      setUsers([]);
    } finally {
      setLoading(false);
    }
  }

  /* Combined Instant Real-time Filtered Users */
  const displayUsers = useMemo(() => {
    return users.filter((u) => {
      // 1. Search Keyword (Name, Email, Phone)
      if (filters.keyword.trim()) {
        const kw = filters.keyword.trim().toLowerCase();
        const nameMatch = (u.fullName || '').toLowerCase().includes(kw);
        const emailMatch = (u.email || '').toLowerCase().includes(kw);
        const phoneMatch = (u.phoneNumber || '').toLowerCase().includes(kw);
        if (!nameMatch && !emailMatch && !phoneMatch) return false;
      }
      // 2. Role Filter
      if (filters.role && u.role !== filters.role) {
        return false;
      }
      // 3. Status Filter
      if (filters.status && u.status !== filters.status) {
        return false;
      }
      return true;
    });
  }, [users, filters]);

  /* Stats calculation */
  const totalUsersCount = users.length;
  const adminCount = users.filter((u) => u.role === 'ADMIN').length;
  const managerCount = users.filter((u) => u.role === 'MANAGER').length;
  const staffCount = users.filter((u) => u.role === 'STAFF').length;
  const activeCount = users.filter((u) => u.status === 'ACTIVE' || !u.status).length;
  const customerCount = users.filter((u) => u.role === 'CUSTOMER' || !u.role).length;

  /* Handle Select All Checkboxes */
  function handleSelectAll(e) {
    if (e.target.checked) {
      setSelectedIds(users.map((u) => u.id));
    } else {
      setSelectedIds([]);
    }
  }

  function handleSelectOne(id) {
    setSelectedIds((prev) =>
      prev.includes(id) ? prev.filter((i) => i !== id) : [...prev, id]
    );
  }

  /* Open User Details Modal */
  async function openUserDetails(user) {
    setActiveMenuId(null);
    setLoadingDetails(true);
    setSelectedUser(user);
    setError('');
    try {
      const details = await getAdminUserDetails(user.id);
      setSelectedUser(details);
    } catch (err) {
      // Keep basic row data if detail fetch fails
    } finally {
      setLoadingDetails(false);
    }
  }

  /* Actions: Lock / Unlock / Delete / Update Role */
  async function handleToggleLock(user) {
    setActiveMenuId(null);
    setError('');
    setMessage('');
    setActionLoading(true);
    try {
      const isLocked = user.status === 'LOCKED';
      if (isLocked) {
        await unlockAdminUser(user.id);
        setMessage(`Đã mở khóa tài khoản ${user.email}!`);
      } else {
        await lockAdminUser(user.id);
        setMessage(`Đã khóa tài khoản ${user.email}!`);
      }
      loadUsers();
    } catch (err) {
      setError(err?.message || 'Thao tác thất bại.');
    } finally {
      setActionLoading(false);
    }
  }

  async function handleDeleteUser(user) {
    setActiveMenuId(null);
    if (!window.confirm(`Bạn có chắc chắn muốn xóa tài khoản "${user.email}"?`)) return;
    setError('');
    setMessage('');
    setActionLoading(true);
    try {
      await deleteAdminUser(user.id);
      setMessage(`Đã xóa người dùng ${user.email} khỏi hệ thống!`);
      loadUsers();
    } catch (err) {
      setError(err?.message || 'Xóa người dùng thất bại.');
    } finally {
      setActionLoading(false);
    }
  }

  async function handleSaveUserRole(e) {
    e.preventDefault();
    if (!editRoleModalUser) return;
    setError('');
    setMessage('');
    setActionLoading(true);
    try {
      await updateAdminUserRole(editRoleModalUser.id, selectedRole);
      setMessage(`Đã cập nhật vai trò của ${editRoleModalUser.email} thành ${selectedRole}!`);
      setEditRoleModalUser(null);
      loadUsers();
    } catch (err) {
      setError(err?.message || 'Cập nhật vai trò thất bại.');
    } finally {
      setActionLoading(false);
    }
  }

  return (
    <section style={{ padding: '24px', background: '#f8fafc', minHeight: '100vh', fontFamily: 'system-ui, -apple-system, sans-serif' }}>
      
      {/* ALERTS */}
      {error && <div style={{ background: '#fef2f2', color: '#dc2626', border: '1px solid #fecaca', padding: '10px 14px', borderRadius: '10px', marginBottom: '16px', fontSize: '13px', fontWeight: '700' }}>{error}</div>}
      {message && <div style={{ background: '#f0fdf4', color: '#16a34a', border: '1px solid #bbf7d0', padding: '10px 14px', borderRadius: '10px', marginBottom: '16px', fontSize: '13px', fontWeight: '700' }}>{message}</div>}

      {/* HEADER ROW IN IMAGE 3 STYLE */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
        <h1 style={{ fontSize: '18px', fontWeight: '800', color: '#0f172a', margin: 0, textTransform: 'uppercase', letterSpacing: '0.5px' }}>
          Quản lý người dùng
        </h1>
        <div style={{ background: '#fff7ed', color: '#ea580c', border: '1px solid #ffedd5', padding: '6px 16px', borderRadius: '20px', fontSize: '13px', fontWeight: '700' }}>
          Tổng người dùng: {totalUsersCount}
        </div>
      </div>



      {/* ═══════════════════════════════════════════════════════════════════
         ROW 1: 3 SUMMARY STATS CARDS (NEAT & CLEAN TYPOGRAPHY)
         ═══════════════════════════════════════════════════════════════════ */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))', gap: '14px', marginBottom: '16px' }}>
        
        {/* CARD 1: TOTAL USERS */}
        <div style={{ background: '#ffffff', borderRadius: '12px', border: '1px solid #e2e8f0', padding: '16px 20px' }}>
          <div style={{ fontSize: '11px', fontWeight: '800', color: '#64748b', letterSpacing: '0.6px', textTransform: 'uppercase', marginBottom: '6px' }}>
            TỔNG NGƯỜI DÙNG
          </div>
          <div style={{ fontSize: '32px', fontWeight: '900', color: '#0f172a', lineHeight: 1.1, marginBottom: '6px' }}>
            {totalUsersCount}
          </div>
          <div style={{ fontSize: '12px', color: '#16a34a', fontWeight: '700' }}>
            {activeCount} tài khoản đang hoạt động
          </div>
        </div>

        {/* CARD 2: ADMINS & STAFF */}
        <div style={{ background: '#ffffff', borderRadius: '12px', border: '1px solid #e2e8f0', padding: '16px 20px' }}>
          <div style={{ fontSize: '11px', fontWeight: '800', color: '#64748b', letterSpacing: '0.6px', textTransform: 'uppercase', marginBottom: '6px' }}>
            QUẢN TRỊ & NHÂN VIÊN
          </div>
          <div style={{ fontSize: '32px', fontWeight: '900', color: '#0f172a', lineHeight: 1.1, marginBottom: '6px' }}>
            {adminCount + managerCount + staffCount}
          </div>
          <div style={{ fontSize: '12px', color: '#2563eb', fontWeight: '700' }}>
            {adminCount} Admin · {managerCount} Quản lý · {staffCount} Nhân viên
          </div>
        </div>

        {/* CARD 3: CUSTOMERS */}
        <div style={{ background: '#ffffff', borderRadius: '12px', border: '1px solid #e2e8f0', padding: '16px 20px' }}>
          <div style={{ fontSize: '11px', fontWeight: '800', color: '#64748b', letterSpacing: '0.6px', textTransform: 'uppercase', marginBottom: '6px' }}>
            KHÁCH HÀNG
          </div>
          <div style={{ fontSize: '32px', fontWeight: '900', color: '#0f172a', lineHeight: 1.1, marginBottom: '6px' }}>
            {customerCount}
          </div>
          <div style={{ fontSize: '12px', color: '#d97706', fontWeight: '700' }}>
            Tài khoản khách hàng thành viên
          </div>
        </div>

      </div>

      {/* ═══════════════════════════════════════════════════════════════════
         ROW 2: SEARCH & FILTER BAR (CLEAN & ALIGNED)
         ═══════════════════════════════════════════════════════════════════ */}
      <form
        onSubmit={(e) => { e.preventDefault(); loadUsers(); }}
        style={{ background: '#ffffff', padding: '14px 16px', borderRadius: '12px', border: '1px solid #e2e8f0', marginBottom: '16px', display: 'flex', gap: '10px', alignItems: 'center', flexWrap: 'wrap' }}
      >
        {/* Search Input */}
        <div style={{ flex: '2', minWidth: '260px' }}>
          <input
            type="text"
            placeholder="Tìm theo tên, email hoặc số điện thoại..."
            value={filters.keyword}
            onChange={(e) => setFilters((c) => ({ ...c, keyword: e.target.value }))}
            style={{ width: '100%', padding: '9px 12px', border: '1px solid #cbd5e1', borderRadius: '8px', fontSize: '13px', outline: 'none' }}
          />
        </div>

        {/* Role Select */}
        <div style={{ flex: '1', minWidth: '150px' }}>
          <select
            value={filters.role}
            onChange={(e) => setFilters((c) => ({ ...c, role: e.target.value }))}
            style={{ width: '100%', padding: '9px 12px', border: '1px solid #cbd5e1', borderRadius: '8px', fontSize: '13px', outline: 'none', background: '#fff' }}
          >
            <option value="">Tất cả vai trò</option>
            {roleOptions.map((r) => (
              <option key={r.value} value={r.value}>{r.label}</option>
            ))}
          </select>
        </div>

        {/* Status Select */}
        <div style={{ flex: '1', minWidth: '150px' }}>
          <select
            value={filters.status}
            onChange={(e) => setFilters((c) => ({ ...c, status: e.target.value }))}
            style={{ width: '100%', padding: '9px 12px', border: '1px solid #cbd5e1', borderRadius: '8px', fontSize: '13px', outline: 'none', background: '#fff' }}
          >
            <option value="">Tất cả trạng thái</option>
            {statusOptions.map((s) => (
              <option key={s.value} value={s.value}>{s.label}</option>
            ))}
          </select>
        </div>

        {/* Action Buttons */}
        <div style={{ display: 'flex', gap: '8px' }}>
          <button
            type="submit"
            style={{ padding: '9px 18px', background: '#ea580c', color: '#ffffff', border: 'none', borderRadius: '8px', fontSize: '13px', fontWeight: '700', cursor: 'pointer' }}
          >
            Lọc
          </button>
          <button
            type="button"
            onClick={() => setFilters({ keyword: '', role: '', status: '' })}
            style={{ padding: '9px 14px', background: '#f1f5f9', color: '#475569', border: '1px solid #cbd5e1', borderRadius: '8px', fontSize: '13px', fontWeight: '700', cursor: 'pointer' }}
          >
            Xóa lọc
          </button>
        </div>
      </form>

      {/* ═══════════════════════════════════════════════════════════════════
         ROW 3: USERS DATA TABLE (CLEAN & NEAT ALIGNMENT)
         ═══════════════════════════════════════════════════════════════════ */}
      <div style={{ background: '#ffffff', borderRadius: '12px', border: '1px solid #e2e8f0', overflow: 'visible' }}>
        <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '13px' }}>
          <thead>
            <tr style={{ background: '#f8fafc', color: '#475569', fontWeight: '800', fontSize: '12px', borderBottom: '1px solid #e2e8f0', textTransform: 'uppercase' }}>
              <th style={{ padding: '14px 12px', width: '40px', textAlign: 'center' }}>
                <input
                  type="checkbox"
                  onChange={handleSelectAll}
                  checked={users.length > 0 && selectedIds.length === users.length}
                />
              </th>
              <th style={{ padding: '14px 16px' }}>NGƯỜI DÙNG</th>
              <th style={{ padding: '14px 16px', width: '130px' }}>VAI TRÒ</th>
              <th style={{ padding: '14px 16px', width: '140px' }}>TRẠNG THÁI</th>
              <th style={{ padding: '14px 16px', width: '140px' }}>SỐ ĐIỆN THOẠI</th>
              <th style={{ padding: '14px 16px', width: '150px' }}>NGÀY THAM GIA</th>
              <th style={{ padding: '14px 16px', width: '80px', textAlign: 'center' }}>THAO TÁC</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr>
                <td colSpan="7" style={{ padding: '36px', textAlign: 'center', color: '#64748b' }}>
                  Đang tải danh sách người dùng...
                </td>
              </tr>
            ) : displayUsers.length === 0 ? (
              <tr>
                <td colSpan="7" style={{ padding: '36px', textAlign: 'center', color: '#94a3b8' }}>
                  Không tìm thấy người dùng nào phù hợp.
                </td>
              </tr>
            ) : (
              displayUsers.map((user, idx) => {
                const initials = getUserInitials(user.fullName, user.email);
                const bgGradient = getAvatarColor(user.id);
                const roleConfig = roleOptions.find((r) => r.value === (user.role || 'CUSTOMER')) || roleOptions[0];
                const statusConfig = statusOptions.find((s) => s.value === (user.status || 'ACTIVE')) || statusOptions[0];
                const isMenuOpen = activeMenuId === user.id;

                return (
                  <tr
                    key={user.id}
                    style={{
                      borderBottom: '1px solid #f1f5f9',
                      background: idx % 2 === 0 ? '#ffffff' : '#fafafa',
                      transition: 'background 0.15s ease',
                    }}
                  >
                    {/* CHECKBOX */}
                    <td style={{ padding: '12px', textAlign: 'center' }}>
                      <input
                        type="checkbox"
                        checked={selectedIds.includes(user.id)}
                        onChange={() => handleSelectOne(user.id)}
                      />
                    </td>

                    {/* USER (AVATAR + NAME + EMAIL) */}
                    <td style={{ padding: '12px 16px' }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                        <div
                          style={{
                            width: '36px',
                            height: '36px',
                            borderRadius: '50%',
                            background: bgGradient,
                            color: '#ffffff',
                            fontWeight: '800',
                            fontSize: '12px',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            flexShrink: 0,
                          }}
                        >
                          {initials}
                        </div>
                        <div style={{ overflow: 'hidden' }}>
                          <div style={{ fontWeight: '700', color: '#0f172a', fontSize: '13.5px', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                            {user.fullName || 'Chưa đặt tên'}
                          </div>
                          <div style={{ fontSize: '12px', color: '#64748b', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                            {user.email}
                          </div>
                        </div>
                      </div>
                    </td>

                    {/* ROLE BADGE */}
                    <td style={{ padding: '12px 16px' }}>
                      <span
                        style={{
                          background: roleConfig.bg,
                          color: roleConfig.color,
                          border: `1px solid ${roleConfig.border}`,
                          padding: '3px 10px',
                          borderRadius: '10px',
                          fontSize: '12px',
                          fontWeight: '700',
                          display: 'inline-block',
                        }}
                      >
                        {roleConfig.label}
                      </span>
                    </td>

                    {/* STATUS BADGE WITH DOT */}
                    <td style={{ padding: '12px 16px' }}>
                      <span
                        style={{
                          background: statusConfig.bg,
                          color: statusConfig.color,
                          border: `1px solid ${statusConfig.border}`,
                          padding: '3px 10px',
                          borderRadius: '10px',
                          fontSize: '12px',
                          fontWeight: '700',
                          display: 'inline-block',
                        }}
                      >
                        {statusConfig.label}
                      </span>
                    </td>

                    {/* PHONE NUMBER */}
                    <td style={{ padding: '12px 16px', color: '#334155', fontWeight: '600' }}>
                      {user.phoneNumber || '—'}
                    </td>

                    {/* JOINED DATE */}
                    <td style={{ padding: '12px 16px', color: '#64748b', fontSize: '12.5px' }}>
                      {user.createdAt ? formatDateTime(user.createdAt) : '—'}
                    </td>

                    {/* ACTION (3 DOTS MENU) */}
                    <td style={{ padding: '12px 16px', textAlign: 'center', position: 'relative' }}>
                      <button
                        type="button"
                        onClick={(e) => {
                          e.stopPropagation();
                          setActiveMenuId(isMenuOpen ? null : user.id);
                        }}
                        style={{
                          background: isMenuOpen ? '#e2e8f0' : 'transparent',
                          border: 'none',
                          borderRadius: '6px',
                          width: '30px',
                          height: '30px',
                          cursor: 'pointer',
                          fontSize: '18px',
                          fontWeight: '900',
                          color: '#475569',
                          display: 'inline-flex',
                          alignItems: 'center',
                          justify: 'center',
                        }}
                      >
                        ⋮
                      </button>

                      {/* POPUP ACTION MENU */}
                      {isMenuOpen && (
                        <div
                          ref={menuRef}
                          style={{
                            position: 'absolute',
                            top: 'calc(100% + 4px)',
                            right: '16px',
                            background: '#ffffff',
                            border: '1px solid #e2e8f0',
                            borderRadius: '12px',
                            boxShadow: '0 8px 24px rgba(0,0,0,0.12)',
                            zIndex: 100,
                            minWidth: '180px',
                            padding: '4px',
                            textAlign: 'left',
                          }}
                        >
                          <div
                            onClick={() => openUserDetails(user)}
                            style={{
                              padding: '8px 12px',
                              borderRadius: '6px',
                              fontSize: '13px',
                              fontWeight: '600',
                              color: '#0f172a',
                              cursor: 'pointer',
                            }}
                            onMouseEnter={(e) => (e.currentTarget.style.background = '#f1f5f9')}
                            onMouseLeave={(e) => (e.currentTarget.style.background = 'transparent')}
                          >
                            Xem chi tiết
                          </div>

                          <div
                            onClick={() => {
                              setActiveMenuId(null);
                              setEditRoleModalUser(user);
                              setSelectedRole(user.role || 'CUSTOMER');
                            }}
                            style={{
                              padding: '8px 12px',
                              borderRadius: '6px',
                              fontSize: '13px',
                              fontWeight: '600',
                              color: '#2563eb',
                              cursor: 'pointer',
                            }}
                            onMouseEnter={(e) => (e.currentTarget.style.background = '#eff6ff')}
                            onMouseLeave={(e) => (e.currentTarget.style.background = 'transparent')}
                          >
                            Đổi vai trò
                          </div>

                          <div
                            onClick={() => handleToggleLock(user)}
                            style={{
                              padding: '8px 12px',
                              borderRadius: '6px',
                              fontSize: '13px',
                              fontWeight: '600',
                              color: user.status === 'LOCKED' ? '#16a34a' : '#d97706',
                              cursor: 'pointer',
                            }}
                            onMouseEnter={(e) => (e.currentTarget.style.background = '#fffbebfb')}
                            onMouseLeave={(e) => (e.currentTarget.style.background = 'transparent')}
                          >
                            {user.status === 'LOCKED' ? 'Mở khóa tài khoản' : 'Khóa tài khoản'}
                          </div>

                          <div style={{ height: '1px', background: '#f1f5f9', margin: '4px 0' }} />

                          <div
                            onClick={() => handleDeleteUser(user)}
                            style={{
                              padding: '8px 12px',
                              borderRadius: '6px',
                              fontSize: '13px',
                              fontWeight: '600',
                              color: '#dc2626',
                              cursor: 'pointer',
                            }}
                            onMouseEnter={(e) => (e.currentTarget.style.background = '#fef2f2')}
                            onMouseLeave={(e) => (e.currentTarget.style.background = 'transparent')}
                          >
                            Xóa tài khoản
                          </div>
                        </div>
                      )}
                    </td>
                  </tr>
                );
              })
            )}
          </tbody>
        </table>
      </div>

      {/* ═══════════════════════════════════════════════════════════════════
         MODAL 1: VIEW USER DETAILS MODAL
         ═══════════════════════════════════════════════════════════════════ */}
      {selectedUser && (
        <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, background: 'rgba(0,0,0,0.4)', zIndex: 1000, display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '20px' }}>
          <div style={{ background: '#ffffff', borderRadius: '16px', padding: '24px 28px', width: '100%', maxWidth: '560px', maxHeight: '90vh', overflowY: 'auto', boxShadow: '0 16px 40px rgba(0,0,0,0.15)' }}>
            
            {/* Header */}
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px', paddingBottom: '12px', borderBottom: '1px solid #f1f5f9' }}>
              <h3 style={{ fontSize: '18px', fontWeight: '800', color: '#0f172a', margin: 0 }}>
                Chi Tiết Người Dùng #{selectedUser.id}
              </h3>
              <button type="button" onClick={() => setSelectedUser(null)} style={{ border: 'none', background: '#f1f5f9', borderRadius: '6px', width: '28px', height: '28px', cursor: 'pointer', fontWeight: '700' }}>✕</button>
            </div>

            {loadingDetails ? (
              <div style={{ padding: '24px', textAlign: 'center', color: '#64748b' }}>Đang tải thông tin...</div>
            ) : (
              <>
                {/* Profile Header */}
                <div style={{ display: 'flex', alignItems: 'center', gap: '14px', background: '#f8fafc', border: '1px solid #e2e8f0', borderRadius: '12px', padding: '16px', marginBottom: '16px' }}>
                  <div style={{ width: '48px', height: '48px', borderRadius: '50%', background: getAvatarColor(selectedUser.id), color: '#fff', fontSize: '18px', fontWeight: '800', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                    {getUserInitials(selectedUser.fullName, selectedUser.email)}
                  </div>
                  <div>
                    <h4 style={{ margin: 0, fontSize: '16px', fontWeight: '800', color: '#0f172a' }}>
                      {selectedUser.fullName || 'Chưa đặt tên'}
                    </h4>
                    <span style={{ fontSize: '13px', color: '#64748b' }}>{selectedUser.email}</span>
                  </div>
                </div>

                {/* Info Fields */}
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px', marginBottom: '16px' }}>
                  <div style={{ background: '#f8fafc', padding: '10px 14px', borderRadius: '10px' }}>
                    <span style={{ fontSize: '12px', color: '#64748b', fontWeight: '600', display: 'block' }}>Số điện thoại</span>
                    <strong style={{ fontSize: '13.5px', color: '#0f172a' }}>{selectedUser.phoneNumber || '—'}</strong>
                  </div>
                  <div style={{ background: '#f8fafc', padding: '10px 14px', borderRadius: '10px' }}>
                    <span style={{ fontSize: '12px', color: '#64748b', fontWeight: '600', display: 'block' }}>Vai trò</span>
                    <strong style={{ fontSize: '13.5px', color: '#2563eb' }}>{selectedUser.role || 'CUSTOMER'}</strong>
                  </div>
                  <div style={{ background: '#f8fafc', padding: '10px 14px', borderRadius: '10px' }}>
                    <span style={{ fontSize: '12px', color: '#64748b', fontWeight: '600', display: 'block' }}>Trạng thái</span>
                    <strong style={{ fontSize: '13.5px', color: selectedUser.status === 'LOCKED' ? '#dc2626' : '#16a34a' }}>{selectedUser.status || 'ACTIVE'}</strong>
                  </div>
                  <div style={{ background: '#f8fafc', padding: '10px 14px', borderRadius: '10px' }}>
                    <span style={{ fontSize: '12px', color: '#64748b', fontWeight: '600', display: 'block' }}>Ngày tạo</span>
                    <strong style={{ fontSize: '13px', color: '#334155' }}>{selectedUser.createdAt ? formatDateTime(selectedUser.createdAt) : '—'}</strong>
                  </div>
                </div>

                {/* Addresses Section */}
                <h4 style={{ fontSize: '13.5px', fontWeight: '800', color: '#0f172a', marginBottom: '8px' }}>Danh sách địa chỉ giao hàng</h4>
                {(!selectedUser.addresses || selectedUser.addresses.length === 0) ? (
                  <div style={{ padding: '14px', background: '#f8fafc', borderRadius: '10px', color: '#94a3b8', fontSize: '12.5px', textAlign: 'center', marginBottom: '20px' }}>Chưa có địa chỉ nào lưu trên hệ thống.</div>
                ) : (
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '8px', marginBottom: '20px' }}>
                    {selectedUser.addresses.map((addr) => (
                      <div key={addr.id} style={{ background: '#ffffff', border: '1px solid #e2e8f0', padding: '10px 14px', borderRadius: '10px' }}>
                        <div style={{ fontWeight: '700', color: '#0f172a', fontSize: '13px' }}>
                          {addr.receiverName} {addr.defaultAddress && <span style={{ background: '#eff6ff', color: '#2563eb', padding: '2px 6px', borderRadius: '4px', fontSize: '11px' }}>Mặc định</span>}
                        </div>
                        <div style={{ fontSize: '12px', color: '#64748b', marginTop: '3px' }}>
                          {addr.phoneNumber} · {addr.fullAddress || addr.addressLine}
                        </div>
                      </div>
                    ))}
                  </div>
                )}

                {/* Close Button */}
                <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
                  <button type="button" onClick={() => setSelectedUser(null)} style={{ padding: '9px 20px', background: '#0284c7', color: '#fff', border: 'none', borderRadius: '10px', fontWeight: '700', cursor: 'pointer', fontSize: '13px' }}>
                    Đóng
                  </button>
                </div>
              </>
            )}
          </div>
        </div>
      )}

      {/* ═══════════════════════════════════════════════════════════════════
         MODAL 2: EDIT USER ROLE MODAL
         ═══════════════════════════════════════════════════════════════════ */}
      {editRoleModalUser && (
        <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, background: 'rgba(0,0,0,0.4)', zIndex: 1000, display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '20px' }}>
          <form onSubmit={handleSaveUserRole} style={{ background: '#ffffff', borderRadius: '16px', padding: '24px', width: '100%', maxWidth: '400px', boxShadow: '0 16px 40px rgba(0,0,0,0.15)' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '14px', paddingBottom: '10px', borderBottom: '1px solid #f1f5f9' }}>
              <h3 style={{ fontSize: '16px', fontWeight: '800', color: '#0f172a', margin: 0 }}>
                Cập Nhật Vai Trò (Role)
              </h3>
              <button type="button" onClick={() => setEditRoleModalUser(null)} style={{ border: 'none', background: '#f1f5f9', borderRadius: '6px', width: '28px', height: '28px', cursor: 'pointer', fontWeight: '700' }}>✕</button>
            </div>

            <div style={{ background: '#f8fafc', border: '1px solid #e2e8f0', padding: '10px 12px', borderRadius: '8px', fontSize: '12.5px', color: '#334155', fontWeight: '600', marginBottom: '16px' }}>
              Tài khoản: {editRoleModalUser.email}
            </div>

            <div style={{ marginBottom: '20px' }}>
              <label style={{ fontSize: '12.5px', fontWeight: '700', color: '#0f172a', display: 'block', marginBottom: '6px' }}>Chọn vai trò mới:</label>
              <select
                value={selectedRole}
                onChange={(e) => setSelectedRole(e.target.value)}
                style={{ width: '100%', padding: '10px 14px', border: '1px solid #cbd5e1', borderRadius: '10px', fontSize: '13px', fontWeight: '600' }}
              >
                {roleOptions.map((r) => (
                  <option key={r.value} value={r.value}>{r.label} ({r.value})</option>
                ))}
              </select>
            </div>

            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '8px' }}>
              <button type="button" onClick={() => setEditRoleModalUser(null)} style={{ padding: '8px 16px', background: '#f1f5f9', color: '#475569', border: '1px solid #cbd5e1', borderRadius: '8px', fontSize: '13px', fontWeight: '600', cursor: 'pointer' }}>
                Hủy
              </button>
              <button type="submit" disabled={actionLoading} style={{ padding: '8px 20px', background: '#ea580c', color: '#ffffff', border: 'none', borderRadius: '8px', fontSize: '13px', fontWeight: '700', cursor: 'pointer' }}>
                {actionLoading ? 'Đang lưu...' : 'Lưu Thay Đổi'}
              </button>
            </div>
          </form>
        </div>
      )}

    </section>
  );
}

export default AdminUserPage;
