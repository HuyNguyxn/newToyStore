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
