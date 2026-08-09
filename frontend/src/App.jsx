import { lazy, Suspense } from 'react';
import { Navigate, Route, Routes } from 'react-router-dom';
import AdminLayout from './components/admin/AdminLayout.jsx';
import ProtectedRoute from './components/common/ProtectedRoute.jsx';
import useAuth from './hooks/useAuth.js';
import CustomerLayout from './components/layout/CustomerLayout.jsx';
import AdminCategoryPage from './pages/admin/AdminCategoryPage.jsx';
import AdminDashboardPage from './pages/admin/AdminDashboardPage.jsx';
import AdminImportPage from './pages/admin/AdminImportPage.jsx';
import AdminInventoryPage from './pages/admin/AdminInventoryPage.jsx';
import AdminLogisticsPage from './pages/admin/AdminLogisticsPage.jsx';
import AdminModerationPage from './pages/admin/AdminModerationPage.jsx';
import AdminNotificationPage from './pages/admin/AdminNotificationPage.jsx';
import AdminOrderPage from './pages/admin/AdminOrderPage.jsx';
import AdminCustomerPaymentPage from './pages/admin/AdminCustomerPaymentPage.jsx';
import AdminProductPage from './pages/admin/AdminProductPage.jsx';
import AdminPromotionPage from './pages/admin/AdminPromotionPage.jsx';
import AdminCustomerRefundPage from './pages/admin/AdminCustomerRefundPage.jsx';
import AdminReturnInspectionPage from './pages/admin/AdminReturnInspectionPage.jsx';
import AdminReviewModerationPage from './pages/admin/AdminReviewModerationPage.jsx';
import AdminSupplierPage from './pages/admin/AdminSupplierPage.jsx';
import AdminSupplierPaymentPage from './pages/admin/AdminSupplierPaymentPage.jsx';
import AdminSupplierReturnPage from './pages/admin/AdminSupplierReturnPage.jsx';
import AdminUploadPage from './pages/admin/AdminUploadPage.jsx';
import AdminUserPage from './pages/admin/AdminUserPage.jsx';
import ForgotPasswordPage from './pages/auth/ForgotPasswordPage.jsx';
import LoginPage from './pages/auth/LoginPage.jsx';
import RegisterPage from './pages/auth/RegisterPage.jsx';
import ResetPasswordPage from './pages/auth/ResetPasswordPage.jsx';
import VerifyEmailPage from './pages/auth/VerifyEmailPage.jsx';
import CartPage from './pages/cart/CartPage.jsx';
import CheckoutPage from './pages/cart/CheckoutPage.jsx';
import NotFoundPage from './pages/common/NotFoundPage.jsx';
import HomePage from './pages/home/HomePage.jsx';
import NotificationPage from './pages/notifications/NotificationPage.jsx';
import OrderDetailPage from './pages/orders/OrderDetailPage.jsx';
import OrderListPage from './pages/orders/OrderListPage.jsx';
import CustomerPaymentListPage from './pages/customer-payments/CustomerPaymentListPage.jsx';
import CustomerVnpayReturnPage from './pages/customer-payments/CustomerVnpayReturnPage.jsx';
import ProductDetailPage from './pages/products/ProductDetailPage.jsx';
import ProductListPage from './pages/products/ProductListPage.jsx';
import ProfilePage from './pages/profile/ProfilePage.jsx';
import ReturnCreatePage from './pages/returns/ReturnCreatePage.jsx';
import ReturnListPage from './pages/returns/ReturnListPage.jsx';
import ReviewCreatePage from './pages/reviews/ReviewCreatePage.jsx';
import ReviewListPage from './pages/reviews/ReviewListPage.jsx';
import ShipmentListPage from './pages/shipments/ShipmentListPage.jsx';
import ReturnPolicyPage from './pages/policy/ReturnPolicyPage.jsx';
import PrivacyPolicyPage from './pages/policy/PrivacyPolicyPage.jsx';

const AdminStatisticsPage = lazy(() => import('./pages/admin/AdminStatisticsPage.jsx'));

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
        <Route path="dashboard" element={<AdminDashboardPage />} />
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
              <Suspense fallback={<div className="admin-empty-mini">Đang tải trang thống kê...</div>}>
                <AdminStatisticsPage />
              </Suspense>
            </ProtectedRoute>
          )}
        />
        <Route path="uploads" element={<AdminUploadPage />} />
        <Route path="*" element={<AdminDefaultRedirect />} />
      </Route>
    </Routes>
  );
}

export default App;
