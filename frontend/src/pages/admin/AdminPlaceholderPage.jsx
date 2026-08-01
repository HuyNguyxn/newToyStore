import { Link, useLocation } from 'react-router-dom';

const moduleCopy = {
  '/admin/products': {
    title: 'Products',
    description: 'Manage product catalog, media, variants, pricing, and display status.',
  },
  '/admin/categories': {
    title: 'Categories',
    description: 'Organize parent and child categories for the storefront menu.',
  },
  '/admin/orders': {
    title: 'Orders',
    description: 'Review order flow, fulfillment status, payment state, and customer notes.',
  },
  '/admin/payments': {
    title: 'Payments',
    description: 'Monitor payment attempts, refunds, gateways, and reconciliation state.',
  },
  '/admin/users': {
    title: 'Users',
    description: 'Manage customer accounts, roles, profile status, and access control.',
  },
  '/admin/promotions': {
    title: 'Promotions',
    description: 'Configure discount campaigns, coupon rules, and active promotion windows.',
  },
  '/admin/suppliers': {
    title: 'Suppliers',
    description: 'Track supplier profiles, contact information, and operating status.',
  },
  '/admin/imports': {
    title: 'Imports',
    description: 'Follow stock import batches, supplier deliveries, and inventory updates.',
  },
  '/admin/logistics': {
    title: 'Logistics',
    description: 'Monitor internal delivery, shipping progress, and handover timeline.',
  },
  '/admin/returns': {
    title: 'Returns',
    description: 'Process customer return requests, refund eligibility, and inspection results.',
  },
  '/admin/reviews': {
    title: 'Reviews',
    description: 'Review customer ratings, comments, attached media, and product feedback.',
  },
  '/admin/moderation': {
    title: 'Moderation',
    description: 'Handle reports, blocked content, and review moderation decisions.',
  },
};

function AdminPlaceholderPage() {
  const location = useLocation();
  const moduleInfo = moduleCopy[location.pathname] || {
    title: 'Admin module',
    description: 'This management area is ready to be connected in the next admin CRUD phase.',
  };

  return (
    <section className="admin-placeholder">
      <p>Admin module</p>
      <h2>{moduleInfo.title}</h2>
      <span>{moduleInfo.description}</span>

      <div className="admin-placeholder__actions">
        <Link to="/admin/dashboard">Back to dashboard</Link>
      </div>
    </section>
  );
}

export default AdminPlaceholderPage;
