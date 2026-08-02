export function formatPrice(value) {
  const amount = Number(value || 0);
  return new Intl.NumberFormat('vi-VN').format(amount) + 'd';
}

export function getProductPrice(product) {
  if (!product) {
    return 0;
  }

  const firstVariant = product.variants?.[0];
  return firstVariant?.discountedPrice || firstVariant?.price || product.basePrice || product.price || 0;
}

export function getProductOriginalPrice(product) {
  if (!product) {
    return 0;
  }

  const firstVariant = product.variants?.[0];
  return firstVariant?.price || product.basePrice || product.oldPrice || 0;
}

export function getProductStatusLabel(product) {
  if (!product) {
    return '';
  }

  if (product.status === 'Đang bán') {
    return product.status;
  }

  if (product.purchasable || product.status === 'ACTIVE') {
    return 'Đang bán';
  }

  if (product.status === 'OUT_OF_STOCK') {
    return 'Hết hàng';
  }

  return product.status || 'Tạm ẩn';
}

export function formatDateTime(value) {
  if (!value) {
    return '';
  }

  return new Date(value).toLocaleString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
}

export function getOrderStatusLabel(status) {
  const labels = {
    PENDING: 'Chờ xác nhận',
    CONFIRMED: 'Đã xác nhận',
    SHIPPED: 'Đang giao',
    COMPLETED: 'Hoàn thành',
    PARTIALLY_REFUNDED: 'Hoàn một phần',
    FULLY_REFUNDED: 'Hoàn toàn bộ',
    CANCELLED: 'Đã hủy',
  };

  return labels[status] || status || '';
}

export function getPaymentStatusLabel(status) {
  const labels = {
    PENDING: 'Chờ thanh toán',
    SUCCEEDED: 'Thành công',
    FAILED: 'Thất bại',
    CANCELLED: 'Đã hủy',
    EXPIRED: 'Hết hạn',
    REFUND_PENDING: 'Chờ hoàn tiền',
    REFUNDED: 'Đã hoàn tiền',
    REFUND_FAILED: 'Hoàn tiền thất bại',
  };

  return labels[status] || status || '';
}
