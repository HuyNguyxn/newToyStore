import { useEffect, useState } from 'react';
import { NavLink, useLocation } from 'react-router-dom';
import { getAdminMenuBadges } from '../../services/adminBadgeService.js';

// Hybrid Navigation Configuration (Option 3)
const navGroups = [
  {
    id: 'sales_b2c',
    title: 'QUẢN LÝ BÁN HÀNG (B2C)',
    icon: '🛍️',
    items: [
      { to: '/admin/orders', label: 'Đơn bán hàng', icon: '📦', badgeKey: 'pendingOrders', requiredRoles: ['STAFF', 'MANAGER', 'ADMIN'] },
      { to: '/admin/returns', label: 'Trả hàng từ Khách', icon: '↩️', badgeKey: 'pendingCustomerReturns', requiredRoles: ['STAFF', 'MANAGER', 'ADMIN'] },
      { to: '/admin/payments', label: 'Thanh toán Khách hàng', icon: '💳', requiredRoles: ['STAFF', 'MANAGER', 'ADMIN'] },
      { to: '/admin/refunds', label: 'Hoàn tiền', icon: '↩', requiredRoles: ['STAFF', 'MANAGER', 'ADMIN'] },
      { to: '/admin/promotions', label: 'Khuyến mãi & Giảm giá', icon: '🏷️', requiredRoles: ['MANAGER', 'ADMIN'] },
      { to: '/admin/reviews', label: 'Đánh giá Khách hàng', icon: '★', requiredRoles: ['STAFF', 'MANAGER', 'ADMIN'] },
    ],
  },
  {
    id: 'purchasing_b2b',
    title: 'MUA HÀNG & NCC (B2B)',
    icon: '🏢',
    items: [
      { to: '/admin/suppliers', label: 'Nhà cung cấp', icon: '🏭', requiredRoles: ['STAFF', 'MANAGER', 'ADMIN'] },
      { to: '/admin/imports', label: 'Nhập hàng vào kho', icon: '📥', requiredRoles: ['STAFF', 'MANAGER', 'ADMIN'] },
      { to: '/admin/supplier-returns', label: 'Xuất trả Nhà cung cấp', icon: '🔄', badgeKey: 'pendingSupplierReturns', requiredRoles: ['STAFF', 'MANAGER', 'ADMIN'] },
    ],
  },
  {
    id: 'catalog_inventory',
    title: 'HÀNG HÓA & KHO BÃI',
    icon: '📦',
    items: [
      { to: '/admin/categories', label: 'Danh mục sản phẩm', icon: '▣', requiredRoles: ['STAFF', 'MANAGER', 'ADMIN'] },
      { to: '/admin/products', label: 'Sản phẩm & Biến thể', icon: '▤', requiredRoles: ['STAFF', 'MANAGER', 'ADMIN'] },
      { to: '/admin/inventory', label: 'Quản lý Tồn kho', icon: '▨', badgeKey: 'lowStockVariants', requiredRoles: ['STAFF', 'MANAGER', 'ADMIN'] },
      { to: '/admin/uploads', label: 'Thư viện hình ảnh', icon: '🖼️', requiredRoles: ['STAFF', 'MANAGER', 'ADMIN'] },
    ],
  },
  {
    id: 'operations_hub',
    title: 'TRUNG TÂM VẬN HÀNH',
    icon: '⚙️',
    items: [
      { to: '/admin/logistics', label: 'Trung tâm Logistics', icon: '⇄', isHighlight: true, requiredRoles: ['STAFF', 'MANAGER', 'ADMIN'] },
      { to: '/admin/return-inspection', label: 'Kiểm định QC hàng trả', icon: '🔍', requiredRoles: ['STAFF', 'MANAGER', 'ADMIN'] },
      { to: '/admin/statistics', label: 'Báo cáo & Thống kê', icon: '📊', requiredRoles: ['MANAGER', 'ADMIN'] },
      { to: '/admin/users', label: 'Người dùng & Phân quyền', icon: '👥', requiredRoles: ['ADMIN'] },
      { to: '/admin/moderation', label: 'Kiểm duyệt nội dung', icon: '◈', requiredRoles: ['STAFF', 'MANAGER', 'ADMIN'] },
      { to: '/admin/notifications', label: 'Thông báo hệ thống', icon: '🔔', requiredRoles: ['MANAGER', 'ADMIN'] },
    ],
  },
];

function AdminSidebar({ userRole = 'ADMIN' }) {
  const location = useLocation();
  const [searchTerm, setSearchTerm] = useState('');
  const [badges, setBadges] = useState({
    pendingOrders: 0,
    pendingCustomerReturns: 0,
    pendingSupplierReturns: 0,
    lowStockVariants: 0,
  });

  // Accordion open states initialized from localStorage memory
  const [openGroups, setOpenGroups] = useState(() => {
    try {
      const saved = localStorage.getItem('admin_sidebar_groups');
      return saved ? JSON.parse(saved) : { sales_b2c: true, purchasing_b2b: true, catalog_inventory: true, operations_hub: true };
    } catch {
      return { sales_b2c: true, purchasing_b2b: true, catalog_inventory: true, operations_hub: true };
    }
  });

  // Load menu badge counters
  const fetchBadges = async () => {
    const data = await getAdminMenuBadges();
    if (data) setBadges(data);
  };

  useEffect(() => {
    fetchBadges();
    const interval = setInterval(fetchBadges, 30000); // Polling every 30s

    // Instant Refresh Listener
    const handleInstantRefresh = () => fetchBadges();
    window.addEventListener('refresh-admin-badges', handleInstantRefresh);

    return () => {
      clearInterval(interval);
      window.removeEventListener('refresh-admin-badges', handleInstantRefresh);
    };
  }, []);

  // Save expanded states to localStorage
  useEffect(() => {
    try {
      localStorage.setItem('admin_sidebar_groups', JSON.stringify(openGroups));
    } catch (e) {
      console.error(e);
    }
  }, [openGroups]);

  // Auto-expand group containing current route
  useEffect(() => {
    const currentPath = location.pathname;
    navGroups.forEach((group) => {
      if (group.items.some((item) => item.to === currentPath)) {
        setOpenGroups((prev) => ({ ...prev, [group.id]: true }));
      }
    });
  }, [location.pathname]);

  const toggleGroup = (groupId) => {
    setOpenGroups((prev) => ({ ...prev, [groupId]: !prev[groupId] }));
  };

  // Filter items by search term if typed
  const normalizedSearch = searchTerm.trim().toLowerCase();

  return (
    <aside className="admin-sidebar" style={{ width: '260px', background: '#0f172a', color: '#f8fafc', flexShrink: 0, display: 'flex', flexDirection: 'column' }}>
      
      {/* BRAND HEADER */}
      <div className="admin-brand" style={{ padding: '16px 20px', borderBottom: '1px solid #1e293b', display: 'flex', alignItems: 'center', gap: '12px' }}>
        <img src="/toystore-assets/logo.png" alt="ToyStore" style={{ width: '32px', height: '32px', objectFit: 'contain' }} />
        <div>
          <strong style={{ fontSize: '16px', color: '#ea580c', display: 'block', fontWeight: '900', letterSpacing: '0.5px' }}>TOYSTORE ERP</strong>
          <small style={{ fontSize: '11px', color: '#94a3b8' }}>Hệ thống Quản trị Doanh nghiệp</small>
        </div>
      </div>

      {/* SEARCH FILTER BAR */}
      <div style={{ padding: '12px 16px', borderBottom: '1px solid #1e293b' }}>
        <input
          type="text"
          placeholder="🔍 Tìm nhanh menu..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          style={{ width: '100%', padding: '6px 10px', background: '#1e293b', color: '#f8fafc', border: '1px solid #334155', borderRadius: '6px', fontSize: '12px', outline: 'none' }}
        />
      </div>

      {/* NAVIGATION ACCORDION GROUPS */}
      <nav className="admin-nav" style={{ flex: 1, overflowY: 'auto', padding: '12px 10px' }} aria-label="Điều hướng quản trị">
        {navGroups.map((group) => {
          // Filter items by role and search query
          const visibleItems = group.items.filter((item) => {
            const hasRole = !item.requiredRoles || item.requiredRoles.includes(userRole);
            const matchesSearch = !normalizedSearch || item.label.toLowerCase().includes(normalizedSearch) || group.title.toLowerCase().includes(normalizedSearch);
            return hasRole && matchesSearch;
          });

          if (visibleItems.length === 0) return null;

          const isOpen = normalizedSearch ? true : openGroups[group.id];

          return (
            <div key={group.id} style={{ marginBottom: '14px' }}>
              {/* GROUP ACCORDION HEADER */}
              <div
                onClick={() => toggleGroup(group.id)}
                style={{
                  display: 'flex',
                  justify: 'space-between',
                  alignItems: 'center',
                  padding: '8px 10px',
                  cursor: 'pointer',
                  fontSize: '11px',
                  fontWeight: '800',
                  color: '#94a3b8',
                  textTransform: 'uppercase',
                  letterSpacing: '0.6px',
                  borderRadius: '6px',
                  userSelect: 'none',
                  transition: 'background 0.2s',
                }}
              >
                <span>{group.icon} {group.title}</span>
                <span style={{ fontSize: '10px', transform: isOpen ? 'rotate(90deg)' : 'rotate(0deg)', transition: 'transform 0.2s' }}>▶</span>
              </div>

              {/* GROUP ITEMS */}
              {isOpen && (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '2px', marginTop: '4px', paddingLeft: '6px' }}>
                  {visibleItems.map((item) => {
                    const badgeCount = item.badgeKey ? badges[item.badgeKey] || 0 : 0;

                    return (
                      <NavLink
                        key={item.to}
                        to={item.to}
                        className={({ isActive }) => (isActive ? 'is-active' : '')}
                        style={({ isActive }) => ({
                          display: 'flex',
                          alignItems: 'center',
                          justify: 'space-between',
                          padding: '8px 12px',
                          borderRadius: '8px',
                          fontSize: '13px',
                          fontWeight: isActive ? '700' : '500',
                          color: isActive ? '#ffffff' : item.isHighlight ? '#fdba74' : '#cbd5e1',
                          background: isActive ? '#ea580c' : item.isHighlight ? 'rgba(234,88,12,0.1)' : 'transparent',
                          textDecoration: 'none',
                          borderLeft: isActive ? '3px solid #ffedd5' : item.isHighlight ? '3px solid #ea580c' : '3px solid transparent',
                          transition: 'all 0.15s ease',
                        })}
                      >
                        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                          <span style={{ fontSize: '14px' }}>{item.icon}</span>
                          <span>{item.label}</span>
                        </div>

                        {/* BADGE COUNTER */}
                        {badgeCount > 0 && (
                          <span
                            style={{
                              background: '#ef4444',
                              color: '#ffffff',
                              borderRadius: '10px',
                              padding: '2px 7px',
                              fontSize: '11px',
                              fontWeight: '800',
                              boxShadow: '0 2px 6px rgba(239,68,68,0.4)',
                            }}
                          >
                            {badgeCount}
                          </span>
                        )}
                      </NavLink>
                    );
                  })}
                </div>
              )}
            </div>
          );
        })}
      </nav>

    </aside>
  );
}

export default AdminSidebar;
