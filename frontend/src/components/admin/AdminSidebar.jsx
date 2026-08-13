import { useEffect, useState } from 'react';
import { NavLink, useLocation } from 'react-router-dom';
import { getAdminMenuBadges } from '../../services/adminBadgeService.js';

// Reordered Menu Groups according to user request (No emojis/icons)
const navGroups = [
  {
    id: 'operations_hub',
    title: 'TRUNG TÂM VẬN HÀNH',
    items: [
      { to: '/admin/statistics', label: 'Báo cáo & Thống kê', requiredRoles: ['ADMIN'] },
      { to: '/admin/accounting', label: 'Kế toán & Dòng tiền', requiredRoles: ['MANAGER', 'ADMIN'] },
      { to: '/admin/notifications', label: 'Thông báo từ hệ thống', requiredRoles: ['ADMIN'] },
      { to: '/admin/users', label: 'Người dùng & Phân quyền', requiredRoles: ['ADMIN'] },
      { to: '/admin/return-inspection', label: 'Kiểm định QC hàng trả', requiredRoles: ['STAFF', 'MANAGER', 'ADMIN'] },
      { to: '/admin/moderation', label: 'Kiểm duyệt nội dung', requiredRoles: ['STAFF', 'MANAGER', 'ADMIN'] },
      { to: '/admin/logistics', label: 'Trung tâm Logistics', requiredRoles: ['STAFF', 'MANAGER', 'ADMIN'] },
    ],
  },
  {
    id: 'catalog_inventory',
    title: 'QUẢN LÝ KHO & HÀNG HÓA',
    items: [
      { to: '/admin/categories', label: 'Danh mục sản phẩm', requiredRoles: ['STAFF', 'MANAGER', 'ADMIN'] },
      { to: '/admin/products', label: 'Sản phẩm & Biến thể', requiredRoles: ['STAFF', 'MANAGER', 'ADMIN'] },
      { to: '/admin/inventory', label: 'Quản lý Kho & Lô hàng', badgeKey: 'pendingImportNotes', requiredRoles: ['STAFF', 'MANAGER', 'ADMIN'] },
      { to: '/admin/uploads', label: 'Thư viện hình ảnh', requiredRoles: ['STAFF', 'MANAGER', 'ADMIN'] },
    ],
  },
  {
    id: 'sales_b2c',
    title: 'QUẢN LÝ BÁN HÀNG',
    items: [
      { to: '/admin/orders', label: 'Đơn bán hàng', badgeKey: 'pendingOrders', requiredRoles: ['STAFF', 'MANAGER', 'ADMIN'] },
      { to: '/admin/returns', label: 'Yêu cầu trả hàng của khách hàng', badgeKey: 'pendingCustomerReturns', requiredRoles: ['STAFF', 'MANAGER', 'ADMIN'] },
      { to: '/admin/payments', label: 'Thanh toán Khách hàng', requiredRoles: ['STAFF', 'MANAGER', 'ADMIN'] },
      { to: '/admin/refunds', label: 'Hoàn tiền', requiredRoles: ['STAFF', 'MANAGER', 'ADMIN'] },
      { to: '/admin/promotions', label: 'Khuyến mãi & Giảm giá', requiredRoles: ['ADMIN'] },
      { to: '/admin/reviews', label: 'Đánh giá Khách hàng', requiredRoles: ['STAFF', 'MANAGER', 'ADMIN'] },
    ],
  },
  {
    id: 'purchasing_b2b',
    title: 'QUẢN LÝ NHÀ CUNG CẤP & MUA HÀNG',
    items: [
      { to: '/admin/suppliers', label: 'Danh sách Nhà cung cấp', requiredRoles: ['STAFF', 'MANAGER', 'ADMIN'] },
      { to: '/admin/imports', label: 'Tạo phiếu Nhập hàng', requiredRoles: ['STAFF', 'MANAGER', 'ADMIN'] },
      { to: '/admin/supplier-payments', label: 'Thanh toán Nhà cung cấp', requiredRoles: ['MANAGER', 'ADMIN'] },
      { to: '/admin/supplier-returns', label: 'Trả hàng Nhà cung cấp', badgeKey: 'pendingSupplierReturns', requiredRoles: ['STAFF', 'MANAGER', 'ADMIN'] },
    ],
  },
];

/* Helper Component: Process Canvas to Erase All White Background Pixels & Zoom in on Inner Circular Emblem */
function TransparentSidebarLogo({ src = '/toystore-assets/logo.png', size = 250 }) {
  const [cleanSrc, setCleanSrc] = useState(src);

  useEffect(() => {
    const img = new Image();
    img.src = src;
    img.onload = () => {
      const canvas = document.createElement('canvas');
      canvas.width = img.width;
      canvas.height = img.height;
      const ctx = canvas.getContext('2d');
      ctx.drawImage(img, 0, 0);

      const imgData = ctx.getImageData(0, 0, canvas.width, canvas.height);
      const data = imgData.data;

      // Erase white / near-white background pixels (RGB > 215)
      for (let i = 0; i < data.length; i += 4) {
        const r = data[i];
        const g = data[i + 1];
        const b = data[i + 2];
        if (r > 215 && g > 215 && b > 215) {
          data[i + 3] = 0; // Make pixel transparent
        }
      }

      ctx.putImageData(imgData, 0, 0);
      setCleanSrc(canvas.toDataURL('image/png'));
    };
  }, [src]);

  return (
    <div
      style={{
        width: `${size}px`,
        height: `${size}px`,
        borderRadius: '50%',
        overflow: 'hidden',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        filter: 'brightness(1.2) drop-shadow(0 6px 16px rgba(234,88,12,0.45))',
      }}
    >
      <img
        src={cleanSrc}
        alt="ToyStore Logo"
        style={{
          width: '100%',
          height: '100%',
          objectFit: 'contain',
          transform: 'scale(1.42)', // Zoom in on the inner circular emblem graphics itself
        }}
      />
    </div>
  );
}

function AdminSidebar({ userRole = 'ADMIN' }) {
  const location = useLocation();
  const [searchTerm, setSearchTerm] = useState('');
  const [badges, setBadges] = useState({
    pendingOrders: 0,
    pendingCustomerReturns: 0,
    pendingSupplierReturns: 0,
    pendingImportNotes: 0,
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
      
      {/* LOGO-ONLY HEADER (Phóng to 250px khít tràn phủ các phần màu đen) */}
      <div className="admin-brand" style={{ padding: '8px 0', borderBottom: '1px solid #1e293b', display: 'flex', justifyContent: 'center', alignItems: 'center', background: '#0b1120' }}>
        <TransparentSidebarLogo size={250} />
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

      {/* NAVIGATION ACCORDION GROUPS (Flex Column - Tightly Packed) */}
      <nav style={{ flex: 1, overflowY: 'auto', padding: '8px 10px', display: 'flex', flexDirection: 'column', gap: '2px' }} aria-label="Điều hướng quản trị">
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
            <div key={group.id} style={{ marginBottom: '4px' }}>
              {/* GROUP ACCORDION HEADER (Chữ to hơn, khít rịt) */}
              <div
                onClick={() => toggleGroup(group.id)}
                style={{
                  display: 'flex',
                  justify: 'space-between',
                  alignItems: 'center',
                  padding: '5px 8px',
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
