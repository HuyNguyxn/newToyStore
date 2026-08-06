import { useEffect, useState } from 'react';
import { NavLink, useLocation } from 'react-router-dom';
import { getAdminMenuBadges } from '../../services/adminBadgeService.js';

// Reordered Menu Groups according to user request (No emojis/icons)
const navGroups = [
  {
    id: 'operations_hub',
    title: 'TRUNG TÂM VẬN HÀNH',
    items: [
      { to: '/admin/statistics', label: 'Báo cáo & Thống kê', requiredRoles: ['MANAGER', 'ADMIN'] },
      { to: '/admin/notifications', label: 'Thông báo từ hệ thống', requiredRoles: ['MANAGER', 'ADMIN'] },
      { to: '/admin/users', label: 'Người dùng & Phân quyền', requiredRoles: ['ADMIN'] },
      { to: '/admin/return-inspection', label: 'Kiểm định QC hàng trả', requiredRoles: ['STAFF', 'MANAGER', 'ADMIN'] },
      { to: '/admin/moderation', label: 'Kiểm duyệt nội dung', requiredRoles: ['STAFF', 'MANAGER', 'ADMIN'] },
      { to: '/admin/logistics', label: 'Trung tâm Logistics', isHighlight: true, requiredRoles: ['STAFF', 'MANAGER', 'ADMIN'] },
    ],
  },
  {
    id: 'catalog_inventory',
    title: 'QUẢN LÝ KHO & HÀNG HÓA',
    items: [
      { to: '/admin/categories', label: 'Danh mục sản phẩm', requiredRoles: ['STAFF', 'MANAGER', 'ADMIN'] },
      { to: '/admin/products', label: 'Sản phẩm & Biến thể', requiredRoles: ['STAFF', 'MANAGER', 'ADMIN'] },
      { to: '/admin/inventory', label: 'Quản lý Tồn kho', badgeKey: 'lowStockVariants', requiredRoles: ['STAFF', 'MANAGER', 'ADMIN'] },
      { to: '/admin/uploads', label: 'Thư viện hình ảnh', requiredRoles: ['STAFF', 'MANAGER', 'ADMIN'] },
    ],
  },
  {
    id: 'sales_b2c',
    title: 'QUẢN LÝ BÁN HÀNG',
    items: [
      { to: '/admin/orders', label: 'Đơn bán hàng', badgeKey: 'pendingOrders', requiredRoles: ['STAFF', 'MANAGER', 'ADMIN'] },
      { to: '/admin/returns', label: 'Trả hàng từ Khách', badgeKey: 'pendingCustomerReturns', requiredRoles: ['STAFF', 'MANAGER', 'ADMIN'] },
      { to: '/admin/payments', label: 'Thanh toán Khách hàng', requiredRoles: ['STAFF', 'MANAGER', 'ADMIN'] },
      { to: '/admin/refunds', label: 'Hoàn tiền', requiredRoles: ['STAFF', 'MANAGER', 'ADMIN'] },
      { to: '/admin/promotions', label: 'Khuyến mãi & Giảm giá', requiredRoles: ['MANAGER', 'ADMIN'] },
      { to: '/admin/reviews', label: 'Đánh giá Khách hàng', requiredRoles: ['STAFF', 'MANAGER', 'ADMIN'] },
    ],
  },
  {
    id: 'purchasing_b2b',
    title: 'QUẢN LÝ NHÀ CUNG CẤP & MUA HÀNG',
    items: [
      { to: '/admin/suppliers', label: 'Danh sách Nhà cung cấp', requiredRoles: ['STAFF', 'MANAGER', 'ADMIN'] },
      { to: '/admin/imports', label: 'Nhập hàng vào kho', requiredRoles: ['STAFF', 'MANAGER', 'ADMIN'] },
      { to: '/admin/supplier-returns', label: 'Xuất trả Nhà cung cấp', badgeKey: 'pendingSupplierReturns', requiredRoles: ['STAFF', 'MANAGER', 'ADMIN'] },
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
      return saved ? JSON.parse(saved) : { operations_hub: true, catalog_inventory: true, sales_b2c: true, purchasing_b2b: true };
    } catch {
      return { operations_hub: true, catalog_inventory: true, sales_b2c: true, purchasing_b2b: true };
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
    <aside className="admin-sidebar" style={{ width: '270px', background: '#0b1120', color: '#f8fafc', flexShrink: 0, display: 'flex', flexDirection: 'column' }}>
      
      {/* LOGO-ONLY HEADER (Ảnh 2: Chỉ hiển thị logo và cho logo rõ hơn) */}
      <div className="admin-brand" style={{ padding: '20px 0', borderBottom: '1px solid #1e293b', display: 'flex', justifyContent: 'center', alignItems: 'center', background: '#0b1120' }}>
        <img
          src="/toystore-assets/logo.png"
          alt="ToyStore Logo"
          style={{
            width: '64px',
            height: '64px',
            objectFit: 'contain',
            filter: 'brightness(1.15) drop-shadow(0 4px 12px rgba(234,88,12,0.4))',
            borderRadius: '50%',
          }}
        />
      </div>

      {/* SEARCH FILTER BAR */}
      <div style={{ padding: '10px 14px', borderBottom: '1px solid #1e293b' }}>
        <input
          type="text"
          placeholder="Tìm nhanh menu..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          style={{ width: '100%', padding: '6px 10px', background: '#1e293b', color: '#f8fafc', border: '1px solid #334155', borderRadius: '6px', fontSize: '13px', outline: 'none' }}
        />
      </div>

      {/* NAVIGATION ACCORDION GROUPS */}
      <nav className="admin-nav" style={{ flex: 1, overflowY: 'auto', padding: '8px 10px' }} aria-label="Điều hướng quản trị">
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
            <div key={group.id} style={{ marginBottom: '8px' }}>
              {/* GROUP ACCORDION HEADER (Chữ to hơn, khít hơn) */}
              <div
                onClick={() => toggleGroup(group.id)}
                style={{
                  display: 'flex',
                  justify: 'space-between',
                  alignItems: 'center',
                  padding: '6px 8px',
                  cursor: 'pointer',
                  fontSize: '12.5px',
                  fontWeight: '900',
                  color: '#94a3b8',
                  textTransform: 'uppercase',
                  letterSpacing: '0.5px',
                  borderRadius: '4px',
                  userSelect: 'none',
                  transition: 'background 0.2s',
                }}
              >
                <span>{group.title}</span>
                <span style={{ fontSize: '10px', transform: isOpen ? 'rotate(90deg)' : 'rotate(0deg)', transition: 'transform 0.2s' }}>▶</span>
              </div>

              {/* GROUP ITEMS (Khít lại, chữ to hơn, không logo) */}
              {isOpen && (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '2px', marginTop: '2px', paddingLeft: '4px' }}>
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
                          padding: '6px 10px',
                          borderRadius: '6px',
                          fontSize: '14px',
                          fontWeight: isActive ? '800' : '600',
                          color: isActive ? '#ffffff' : item.isHighlight ? '#fdba74' : '#e2e8f0',
                          background: isActive ? '#ea580c' : item.isHighlight ? 'rgba(234,88,12,0.12)' : 'transparent',
                          textDecoration: 'none',
                          borderLeft: isActive ? '3px solid #ffedd5' : item.isHighlight ? '3px solid #ea580c' : '3px solid transparent',
                          transition: 'all 0.12s ease',
                        })}
                      >
                        <span>{item.label}</span>

                        {/* BADGE COUNTER */}
                        {badgeCount > 0 && (
                          <span
                            style={{
                              background: '#ef4444',
                              color: '#ffffff',
                              borderRadius: '10px',
                              padding: '1px 6px',
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
