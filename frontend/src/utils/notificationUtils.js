const titleTranslations = {
  'Order created': 'Đơn hàng đã được tạo',
  'Order status changed': 'Trạng thái đơn hàng đã thay đổi',
  'Order cancelled': 'Đơn hàng đã bị hủy',
  'Payment completed': 'Thanh toán thành công',
  'Payment failed': 'Thanh toán thất bại',
  'Payment refunded': 'Đã hoàn tiền thanh toán',
  'Shipment created': 'Đã tạo đơn vận chuyển',
  'Shipment in transit': 'Đơn hàng đang được vận chuyển',
  'Shipment delivered': 'Giao hàng thành công',
  'Shipment returned': 'Đơn vận chuyển đã được trả lại',
  'Shipment cancelled': 'Đơn vận chuyển đã bị hủy',
  'Return status changed': 'Yêu cầu trả hàng đã được cập nhật',
  'Return refunded': 'Đã hoàn tiền trả hàng',
  'Review replied': 'Đánh giá đã có phản hồi',
  'Review status changed': 'Đánh giá đã được cập nhật',
  'Cart item expiring': 'Sản phẩm trong giỏ sắp hết hạn',
  'System announcement': 'Thông báo từ hệ thống',
};

export function getNotificationEnumCode(value) {
  if (value && typeof value === 'object') return value.code || value.name || '';
  return String(value || '');
}

export function getNotificationTitle(item) {
  return titleTranslations[item?.title] || item?.title || 'Thông báo mới';
}

export function getNotificationMessage(message) {
  return String(message || '')
    .replace(/Payment for order #(\d+) was completed successfully\./gi, 'Thanh toán cho đơn hàng #$1 đã hoàn tất thành công.')
    .replace(/Payment for order #(\d+) failed\./gi, 'Thanh toán cho đơn hàng #$1 thất bại.')
    .replace(/Refund for order #(\d+) was completed\./gi, 'Hoàn tiền cho đơn hàng #$1 đã hoàn tất.');
}

export function resolveNotificationTarget(item) {
  const referenceType = getNotificationEnumCode(item?.referenceType);
  const referenceId = item?.referenceId;

  if (referenceType === 'ORDER' && referenceId) return `/orders/${referenceId}`;
  if (referenceType === 'PAYMENT') return '/payments';
  if (referenceType === 'SHIPMENT') return '/shipments';
  if (referenceType === 'CUSTOMER_RETURN') return '/returns';
  if (referenceType === 'REVIEW') return '/reviews/me';
  if (referenceType === 'CART') return '/cart';

  const actionUrl = String(item?.actionUrl || '').trim();
  if (!actionUrl || !actionUrl.startsWith('/')) return '/notifications';
  if (actionUrl.startsWith('/customer-returns/')) return '/returns';
  if (/^\/shipments\/\d+/.test(actionUrl)) return '/shipments';
  if (/^\/payments\/\d+/.test(actionUrl)) return '/payments';
  if (/^\/reviews\/\d+/.test(actionUrl)) return '/reviews/me';
  return actionUrl;
}
