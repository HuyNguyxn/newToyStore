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

  if (product.status === 'Dang ban') {
    return product.status;
  }

  if (product.purchasable || product.status === 'ACTIVE') {
    return 'Dang ban';
  }

  if (product.status === 'OUT_OF_STOCK') {
    return 'Het hang';
  }

  return product.status || 'Tam an';
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
    PENDING: 'Cho xac nhan',
    CONFIRMED: 'Da xac nhan',
    SHIPPED: 'Dang giao',
    COMPLETED: 'Hoan thanh',
    PARTIALLY_REFUNDED: 'Hoan mot phan',
    FULLY_REFUNDED: 'Hoan toan bo',
    CANCELLED: 'Da huy',
  };

  return labels[status] || status || '';
}

export function getPaymentStatusLabel(status) {
  const labels = {
    PENDING: 'Cho thanh toan',
    SUCCEEDED: 'Thanh cong',
    FAILED: 'That bai',
    CANCELLED: 'Da huy',
    EXPIRED: 'Het han',
    REFUND_PENDING: 'Cho hoan tien',
    REFUNDED: 'Da hoan tien',
    REFUND_FAILED: 'Hoan tien that bai',
  };

  return labels[status] || status || '';
}
