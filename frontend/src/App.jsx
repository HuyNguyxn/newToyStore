import { lazy, Suspense } from 'react';
import { Navigate, Route, Routes } from 'react-router-dom';
import AdminLayout from './components/admin/AdminLayout.jsx';
import ProtectedRoute from './components/common/ProtectedRoute.jsx';
import useAuth from './hooks/useAuth.js';
import CustomerLayout from './components/layout/CustomerLayout.jsx';
const AdminCategoryPage = lazy(() => import('./pages/admin/AdminCategoryPage.jsx'));
const AdminImportPage = lazy(() => import('./pages/admin/AdminImportPage.jsx'));
const AdminInventoryPage = lazy(() => import('./pages/admin/AdminInventoryPage.jsx'));
const AdminLogisticsPage = lazy(() => import('./pages/admin/AdminLogisticsPage.jsx'));
const AdminModerationPage = lazy(() => import('./pages/admin/AdminModerationPage.jsx'));
const AdminNotificationPage = lazy(() => import('./pages/admin/AdminNotificationPage.jsx'));
const AdminOrderPage = lazy(() => import('./pages/admin/AdminOrderPage.jsx'));
const AdminCustomerPaymentPage = lazy(() => import('./pages/admin/AdminCustomerPaymentPage.jsx'));
const AdminProductPage = lazy(() => import('./pages/admin/AdminProductPage.jsx'));
const AdminPromotionPage = lazy(() => import('./pages/admin/AdminPromotionPage.jsx'));
const AdminCustomerRefundPage = lazy(() => import('./pages/admin/AdminCustomerRefundPage.jsx'));
const AdminReturnInspectionPage = lazy(() => import('./pages/admin/AdminReturnInspectionPage.jsx'));
const AdminReviewModerationPage = lazy(() => import('./pages/admin/AdminReviewModerationPage.jsx'));
const AdminSupplierPage = lazy(() => import('./pages/admin/AdminSupplierPage.jsx'));
const AdminSupplierPaymentPage = lazy(() => import('./pages/admin/AdminSupplierPaymentPage.jsx'));
const AdminSupplierReturnPage = lazy(() => import('./pages/admin/AdminSupplierReturnPage.jsx'));
const AdminUploadPage = lazy(() => import('./pages/admin/AdminUploadPage.jsx'));
const AdminUserPage = lazy(() => import('./pages/admin/AdminUserPage.jsx'));
const AdminAccountingPage = lazy(() => import('./pages/admin/AdminAccountingPage.jsx'));
const AdminStatisticsPage = lazy(() => import('./pages/admin/AdminStatisticsPage.jsx'));
const ForgotPasswordPage = lazy(() => import('./pages/auth/ForgotPasswordPage.jsx'));
const LoginPage = lazy(() => import('./pages/auth/LoginPage.jsx'));
const RegisterPage = lazy(() => import('./pages/auth/RegisterPage.jsx'));
const ResetPasswordPage = lazy(() => import('./pages/auth/ResetPasswordPage.jsx'));
const VerifyEmailPage = lazy(() => import('./pages/auth/VerifyEmailPage.jsx'));
const CartPage = lazy(() => import('./pages/cart/CartPage.jsx'));
const CheckoutPage = lazy(() => import('./pages/cart/CheckoutPage.jsx'));
const NotFoundPage = lazy(() => import('./pages/common/NotFoundPage.jsx'));
const HomePage = lazy(() => import('./pages/home/HomePage.jsx'));
const NotificationPage = lazy(() => import('./pages/notifications/NotificationPage.jsx'));
const OrderDetailPage = lazy(() => import('./pages/orders/OrderDetailPage.jsx'));
const OrderListPage = lazy(() => import('./pages/orders/OrderListPage.jsx'));
const CustomerPaymentListPage = lazy(() => import('./pages/customer-payments/CustomerPaymentListPage.jsx'));
const CustomerVnpayReturnPage = lazy(() => import('./pages/customer-payments/CustomerVnpayReturnPage.jsx'));
const ProductDetailPage = lazy(() => import('./pages/products/ProductDetailPage.jsx'));
const ProductListPage = lazy(() => import('./pages/products/ProductListPage.jsx'));
const ProfilePage = lazy(() => import('./pages/profile/ProfilePage.jsx'));
const ReturnCreatePage = lazy(() => import('./pages/returns/ReturnCreatePage.jsx'));
const ReturnListPage = lazy(() => import('./pages/returns/ReturnListPage.jsx'));
const ReviewCreatePage = lazy(() => import('./pages/reviews/ReviewCreatePage.jsx'));
const ReviewListPage = lazy(() => import('./pages/reviews/ReviewListPage.jsx'));
const ShipmentListPage = lazy(() => import('./pages/shipments/ShipmentListPage.jsx'));
const ReturnPolicyPage = lazy(() => import('./pages/policy/ReturnPolicyPage.jsx'));
const PrivacyPolicyPage = lazy(() => import('./pages/policy/PrivacyPolicyPage.jsx'));

function RouteLoadingFallback() {
  return <div className="route-loading-fallback" role="status">Đang tải trang...</div>;
}

// Role-based default redirect for admin panel
function AdminDefaultRedirect() {
  const { user } = useAuth();
  // STAFF can't access statistics, redirect to products
  if (user?.role === 'STAFF') {
    return <Navigate to="/admin/products" replace />;
  }
  // MANAGER & ADMIN go to statistics
  return <Navigate to="/admin/statistics" replace />;
}

function App() {
  return (
    <Suspense fallback={<RouteLoadingFallback />}>
      <Routes>
      <Route element={<CustomerLayout />}>
        <Route path="/" element={<HomePage />} />
        <Route path="/products" element={<ProductListPage />} />
        <Route path="/products/category/:categoryId" element={<ProductListPage />} />
        <Route path="/products/:id" element={<ProductDetailPage />} />
        <Route
          path="/cart"
          element={(
            <ProtectedRoute>
              <CartPage />
            </ProtectedRoute>
          )}
        />
        <Route
          path="/checkout"
          element={(
            <ProtectedRoute>
              <CheckoutPage />
            </ProtectedRoute>
          )}
        />
        <Route
          path="/orders"
          element={(
            <ProtectedRoute>
              <OrderListPage />
            </ProtectedRoute>
          )}
        />
        <Route
          path="/orders/:id"
          element={(
            <ProtectedRoute>
              <OrderDetailPage />
            </ProtectedRoute>
          )}
        />
        <Route
          path="/payments"
          element={(
            <ProtectedRoute>
              <CustomerPaymentListPage />
            </ProtectedRoute>
          )}
        />
        <Route path="/payment" element={<Navigate to="/payments" replace />} />
        <Route path="/payments/vnpay-return" element={<CustomerVnpayReturnPage />} />
        <Route path="/customer-payments" element={<Navigate to="/payments" replace />} />
        <Route path="/customer-payments/vnpay-return" element={<Navigate to="/payments/vnpay-return" replace />} />
        <Route path="/vnpay-return" element={<CustomerVnpayReturnPage />} />
        <Route path="/payment/vnpay-return" element={<CustomerVnpayReturnPage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/forgot-password" element={<ForgotPasswordPage />} />
        <Route path="/reset-password" element={<ResetPasswordPage />} />
        <Route path="/verify-email" element={<VerifyEmailPage />} />
        <Route
          path="/profile"
          element={(
            <ProtectedRoute>
              <ProfilePage />
            </ProtectedRoute>
          )}
        />
        <Route
          path="/notifications"
          element={(
            <ProtectedRoute>
              <NotificationPage />
            </ProtectedRoute>
          )}
        />
        <Route
          path="/returns/new"
          element={(
            <ProtectedRoute>
              <ReturnCreatePage />
            </ProtectedRoute>
          )}
        />
        <Route
          path="/returns"
          element={(
            <ProtectedRoute>
              <ReturnListPage />
            </ProtectedRoute>
          )}
        />
        <Route
          path="/returns/me"
          element={(
            <ProtectedRoute>
              <ReturnListPage />
            </ProtectedRoute>
          )}
        />
        <Route
          path="/reviews/new"
          element={(
            <ProtectedRoute>
              <ReviewCreatePage />
            </ProtectedRoute>
          )}
        />
        <Route
          path="/reviews/me"
          element={(
            <ProtectedRoute>
              <ReviewListPage />
            </ProtectedRoute>
          )}
        />
        <Route
          path="/shipments"
          element={(
            <ProtectedRoute>
              <ShipmentListPage />
            </ProtectedRoute>
          )}
        />
        <Route path="/policy/returns" element={<ReturnPolicyPage />} />
        <Route path="/policy/privacy" element={<PrivacyPolicyPage />} />
        <Route path="*" element={<NotFoundPage />} />
      </Route>

      <Route
        path="/admin"
        element={(
          <ProtectedRoute allowedRoles={['STAFF', 'MANAGER', 'ADMIN']}>
            <AdminLayout />
          </ProtectedRoute>
        )}
      >
        <Route index element={<AdminDefaultRedirect />} />
        <Route path="dashboard" element={<AdminDefaultRedirect />} />
        <Route path="products" element={<AdminProductPage />} />
        <Route path="categories" element={<AdminCategoryPage />} />
        <Route path="orders" element={<AdminOrderPage />} />
        <Route path="payments" element={<AdminCustomerPaymentPage />} />
        <Route path="customer-payments" element={<Navigate to="/admin/payments" replace />} />
        <Route path="refunds" element={<AdminCustomerRefundPage />} />
        <Route path="customer-refunds" element={<Navigate to="/admin/refunds" replace />} />
        <Route
          path="users"
          element={(
            <ProtectedRoute allowedRoles={['ADMIN']}>
              <AdminUserPage />
            </ProtectedRoute>
          )}
        />
        <Route
          path="promotions"
          element={(
            <ProtectedRoute allowedRoles={['MANAGER', 'ADMIN']}>
              <AdminPromotionPage />
            </ProtectedRoute>
          )}
        />
        <Route path="suppliers" element={<AdminSupplierPage />} />
        <Route path="imports" element={<AdminImportPage />} />
        <Route path="supplier-payments" element={<AdminSupplierPaymentPage />} />
        <Route
          path="accounting"
          element={(
            <ProtectedRoute allowedRoles={['MANAGER', 'ADMIN']}>
              <AdminAccountingPage />
            </ProtectedRoute>
          )}
        />
        <Route path="supplier-returns" element={<AdminSupplierReturnPage />} />
        <Route path="logistics" element={<AdminLogisticsPage />} />
        <Route path="returns" element={<AdminReturnInspectionPage />} />
        <Route path="return-inspection" element={<AdminReturnInspectionPage />} />
        <Route path="reviews" element={<AdminReviewModerationPage />} />
        <Route path="moderation" element={<AdminModerationPage />} />
        <Route
          path="notifications"
          element={(
            <ProtectedRoute allowedRoles={['MANAGER', 'ADMIN']}>
              <AdminNotificationPage />
            </ProtectedRoute>
          )}
        />
        <Route path="inventory" element={<AdminInventoryPage />} />
        <Route
          path="statistics"
          element={(
            <ProtectedRoute allowedRoles={['MANAGER', 'ADMIN']}>
              <AdminStatisticsPage />
            </ProtectedRoute>
          )}
        />
        <Route path="uploads" element={<AdminUploadPage />} />
        <Route path="*" element={<AdminDefaultRedirect />} />
      </Route>
      </Routes>
    </Suspense>
  );
}

export default App;
