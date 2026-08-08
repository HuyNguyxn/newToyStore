/**
 * Kiểm tra định dạng số điện thoại Việt Nam hợp lệ (10 chữ số bắt đầu bằng 03, 05, 07, 08, 09 hoặc +84)
 */
export function isValidVietnamesePhoneNumber(phone) {
  if (!phone) return false;
  const cleanPhone = phone.trim().replace(/[\s.-]/g, '');
  return /^(0|\+84)(3|5|7|8|9)[0-9]{8}$/.test(cleanPhone);
}

/**
 * Kiểm tra tính đầy đủ của thông tin cá nhân (Họ tên, Số điện thoại chuẩn Việt Nam & Địa chỉ)
 */
export function isUserProfileComplete(user) {
  if (!user) return false;

  const hasFullName = Boolean(user.fullName && user.fullName.trim().length > 0);
  const hasPhoneNumber = isValidVietnamesePhoneNumber(user.phoneNumber);
  const hasAddress = Boolean(
    (Array.isArray(user.addresses) && user.addresses.length > 0) ||
    (user.detailAddress && user.detailAddress.trim().length > 0) ||
    (user.address && user.address.trim().length > 0)
  );

  return hasFullName && hasPhoneNumber && hasAddress;
}
