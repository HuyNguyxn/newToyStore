import { apiClient } from './apiClient.js';

function toQueryString(params = {}) {
  const searchParams = new URLSearchParams();

  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      searchParams.set(key, value);
    }
  });

  return searchParams.toString();
}

export function getAdminResource(endpoint, params = {}) {
  const query = toQueryString(params);
  return apiClient(`${endpoint}${query ? `?${query}` : ''}`);
}

export function runAdminAction({ endpoint, method = 'PATCH', body }) {
  return apiClient(endpoint, {
    method,
    ...(body === undefined ? {} : { body: JSON.stringify(body) }),
  });
}

export function runAdminJsonRequest({ endpoint, method = 'GET', bodyText = '' }) {
  const hasBody = !['GET', 'DELETE'].includes(method) && bodyText.trim();
  return apiClient(endpoint, {
    method,
    ...(hasBody ? { body: bodyText } : {}),
  });
}

export const adminResourceConfigs = {
  products: {
    title: 'Products',
    description: 'Manage catalog, images, variants, pricing, and active selling state.',
    endpoint: '/products',
    defaultParams: { page: 0, size: 10, sort: 'createdAt,desc' },
    filters: ['keyword', 'status'],
    columns: ['id', 'name', 'status', 'categoryName', 'averageRating', 'reviewCount'],
    actions: [
      { label: 'Delete', danger: true, method: 'DELETE', endpoint: (item) => `/products/${item.id}` },
    ],
  },
  categories: {
    title: 'Categories',
    description: 'Manage visible and hidden category tree nodes.',
    endpoint: '/api/categories',
    defaultParams: { page: 0, size: 10 },
    filters: ['keyword', 'status'],
    columns: ['id', 'name', 'slug', 'status', 'parentId', 'productCount'],
    actions: [
      { label: 'Show', endpoint: (item) => `/api/categories/${item.id}/show` },
      { label: 'Hide', endpoint: (item) => `/api/categories/${item.id}/hide` },
      { label: 'Delete', danger: true, method: 'DELETE', endpoint: (item) => `/api/categories/${item.id}` },
    ],
  },
  orders: {
    title: 'Orders',
    description: 'Review order lifecycle and perform staff order actions.',
    endpoint: '/orders/admin/filter',
    defaultParams: { page: 0, size: 10, sort: 'createdAt,desc' },
    filters: ['status', 'userId'],
    columns: ['id', 'userId', 'status', 'totalAmount', 'paymentStatus', 'createdAt'],
    actions: [
      { label: 'Confirm', endpoint: (item) => `/orders/${item.id}/confirm` },
      { label: 'Ship', endpoint: (item) => `/orders/${item.id}/ship` },
      { label: 'Complete', endpoint: (item) => `/orders/${item.id}/complete` },
      { label: 'Cancel', danger: true, endpoint: (item) => `/orders/${item.id}/cancel?note=${encodeURIComponent('Admin cancelled from dashboard')}` },
      { label: 'Delete', danger: true, method: 'DELETE', endpoint: (item) => `/orders/${item.id}` },
    ],
  },
  payments: {
    title: 'Payments',
    description: 'Monitor payment state, manual confirmations, failures, and cancellations.',
    endpoint: '/payments/admin/filter',
    defaultParams: { page: 0, size: 10, sort: 'createdAt,desc' },
    filters: ['status', 'method', 'orderId'],
    columns: ['id', 'orderId', 'method', 'status', 'amount', 'createdAt'],
    actions: [
      { label: 'Succeed', endpoint: (item) => `/payments/${item.id}/succeed`, body: {} },
      { label: 'Fail', danger: true, endpoint: (item) => `/payments/${item.id}/fail`, body: { reason: 'Marked failed from admin dashboard' } },
      { label: 'Cancel', danger: true, endpoint: (item) => `/payments/${item.id}/cancel`, body: { reason: 'Cancelled from admin dashboard' } },
      { label: 'Delete', danger: true, method: 'DELETE', endpoint: (item) => `/payments/${item.id}` },
    ],
  },
  users: {
    title: 'Users',
    description: 'Manage users, account locking, and staff visibility.',
    endpoint: '/users',
    defaultParams: { page: 0, size: 10, sort: 'createdAt,desc' },
    filters: ['keyword', 'role', 'status'],
    columns: ['id', 'email', 'fullName', 'role', 'status', 'createdAt'],
    actions: [
      { label: 'Lock', danger: true, endpoint: (item) => `/users/${item.id}/lock` },
      { label: 'Unlock', endpoint: (item) => `/users/${item.id}/unlock` },
      { label: 'Delete', danger: true, method: 'DELETE', endpoint: (item) => `/users/${item.id}` },
    ],
  },
  promotions: {
    title: 'Promotions',
    description: 'Manage discount campaigns, coupon state, and active windows.',
    endpoint: '/api/v1/promotions',
    defaultParams: { page: 0, size: 10 },
    filters: ['keyword', 'scope', 'active'],
    columns: ['id', 'code', 'name', 'scope', 'active', 'startDate', 'endDate'],
    actions: [
      { label: 'Activate', endpoint: (item) => `/api/v1/promotions/${item.id}/activate` },
      { label: 'Deactivate', danger: true, endpoint: (item) => `/api/v1/promotions/${item.id}/deactivate` },
      { label: 'Delete', danger: true, method: 'DELETE', endpoint: (item) => `/api/v1/promotions/${item.id}` },
    ],
  },
  suppliers: {
    title: 'Suppliers',
    description: 'Manage supplier profile, active state, and recovery.',
    endpoint: '/suppliers',
    defaultParams: { page: 0, size: 10, sort: 'createdAt,desc' },
    filters: ['keyword', 'status'],
    columns: ['id', 'name', 'email', 'phoneNumber', 'status', 'createdAt'],
    actions: [
      { label: 'Restore', endpoint: (item) => `/suppliers/${item.id}/restore` },
      { label: 'Delete', danger: true, method: 'DELETE', endpoint: (item) => `/suppliers/${item.id}` },
    ],
  },
  imports: {
    title: 'Imports',
    description: 'Track import batches and stock receiving workflow.',
    endpoint: '/imports',
    defaultParams: { page: 0, size: 10, sort: 'createdAt,desc' },
    filters: ['status', 'supplierId'],
    columns: ['id', 'supplierId', 'status', 'totalAmount', 'createdAt', 'completedAt'],
    actions: [
      { label: 'Complete', endpoint: (item) => `/imports/${item.id}/complete` },
      { label: 'Cancel', danger: true, endpoint: (item) => `/imports/${item.id}/cancel` },
    ],
  },
  logistics: {
    title: 'Logistics',
    description: 'Monitor internal shipment and delivery state.',
    endpoint: '/shipments/admin/filter',
    defaultParams: { page: 0, size: 10, sort: 'createdAt,desc' },
    filters: ['status', 'orderId'],
    columns: ['id', 'orderId', 'status', 'carrierName', 'trackingCode', 'createdAt'],
    actions: [
      { label: 'Delete', danger: true, method: 'DELETE', endpoint: (item) => `/shipments/${item.id}` },
    ],
  },
  returns: {
    title: 'Customer Returns',
    description: 'Review customer return and refund workflow.',
    endpoint: '/api/returns',
    defaultParams: { page: 0, size: 10, sort: 'createdAt,desc' },
    filters: ['status', 'userId', 'orderId'],
    columns: ['id', 'orderId', 'userId', 'status', 'refundAmount', 'createdAt'],
    actions: [
      { label: 'Cancel', danger: true, endpoint: (item) => `/api/returns/${item.id}/cancel` },
    ],
  },
  reviews: {
    title: 'Reviews',
    description: 'Moderate customer ratings, media attachments, and shop replies.',
    endpoint: '/reviews/admin/all',
    defaultParams: { page: 0, size: 10, sort: 'createdAt,desc' },
    filters: ['status', 'productId', 'userId'],
    columns: ['id', 'productId', 'userFullName', 'rating', 'status', 'createdAt'],
    actions: [],
  },
  moderation: {
    title: 'Moderation Blacklists',
    description: 'Manage blocked words, phrases, and moderation data.',
    endpoint: '/admin/moderation/blacklists',
    defaultParams: { page: 0, size: 10, sort: 'createdAt,desc' },
    filters: ['keyword', 'type'],
    columns: ['id', 'type', 'value', 'reason', 'createdAt', 'deleted'],
    actions: [
      { label: 'Restore', method: 'PUT', endpoint: (item) => `/admin/moderation/blacklists/${item.id}/restore` },
      { label: 'Delete', danger: true, method: 'DELETE', endpoint: (item) => `/admin/moderation/blacklists/${item.id}` },
      { label: 'Hard delete', danger: true, method: 'DELETE', endpoint: (item) => `/admin/moderation/blacklists/${item.id}/hard` },
    ],
  },
};
