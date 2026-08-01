import { lazy, Suspense } from 'react';
import { Navigate, Route, Routes } from 'react-router-dom';
import AdminLayout from './components/admin/AdminLayout.jsx';
import ProtectedRoute from './components/common/ProtectedRoute.jsx';
import CustomerLayout from './components/layout/CustomerLayout.jsx';
import AdminCategoryPage from './pages/admin/AdminCategoryPage.jsx';
import AdminDashboardPage from './pages/admin/AdminDashboardPage.jsx';
import AdminImportPage from './pages/admin/AdminImportPage.jsx';
import AdminInventoryPage from './pages/admin/AdminInventoryPage.jsx';
import AdminLogisticsPage from './pages/admin/AdminLogisticsPage.jsx';
import AdminModerationPage from './pages/admin/AdminModerationPage.jsx';
import AdminNotificationPage from './pages/admin/AdminNotificationPage.jsx';
import AdminOrderPage from './pages/admin/AdminOrderPage.jsx';
import AdminPaymentPage from './pages/admin/AdminPaymentPage.jsx';
import AdminProductPage from './pages/admin/AdminProductPage.jsx';
import AdminPromotionPage from './pages/admin/AdminPromotionPage.jsx';
import AdminRefundPage from './pages/admin/AdminRefundPage.jsx';
import AdminReturnInspectionPage from './pages/admin/AdminReturnInspectionPage.jsx';
import AdminReviewModerationPage from './pages/admin/AdminReviewModerationPage.jsx';
import AdminSupplierPage from './pages/admin/AdminSupplierPage.jsx';
import AdminSupplierReturnPage from './pages/admin/AdminSupplierReturnPage.jsx';
import AdminUploadPage from './pages/admin/AdminUploadPage.jsx';
import AdminUserPage from './pages/admin/AdminUserPage.jsx';
import ForgotPasswordPage from './pages/auth/ForgotPasswordPage.jsx';
import LoginPage from './pages/auth/LoginPage.jsx';
import RegisterPage from './pages/auth/RegisterPage.jsx';
import ResetPasswordPage from './pages/auth/ResetPasswordPage.jsx';
import CartPage from './pages/cart/CartPage.jsx';
import CheckoutPage from './pages/cart/CheckoutPage.jsx';
import NotFoundPage from './pages/common/NotFoundPage.jsx';
import HomePage from './pages/home/HomePage.jsx';
import NotificationPage from './pages/notifications/NotificationPage.jsx';
import OrderDetailPage from './pages/orders/OrderDetailPage.jsx';
import OrderListPage from './pages/orders/OrderListPage.jsx';
import PaymentListPage from './pages/payments/PaymentListPage.jsx';
import VnpayReturnPage from './pages/payments/VnpayReturnPage.jsx';
import ProductDetailPage from './pages/products/ProductDetailPage.jsx';
import ProductListPage from './pages/products/ProductListPage.jsx';
import ProfilePage from './pages/profile/ProfilePage.jsx';
import ReturnCreatePage from './pages/returns/ReturnCreatePage.jsx';
import ReturnListPage from './pages/returns/ReturnListPage.jsx';
import ReviewCreatePage from './pages/reviews/ReviewCreatePage.jsx';
import ReviewListPage from './pages/reviews/ReviewListPage.jsx';
import ShipmentListPage from './pages/shipments/ShipmentListPage.jsx';

const AdminStatisticsPage = lazy(() => import('./pages/admin/AdminStatisticsPage.jsx'));

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
              <PaymentListPage />
            </ProtectedRoute>
          )}
        />
        <Route path="/payments/vnpay-return" element={<VnpayReturnPage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/forgot-password" element={<ForgotPasswordPage />} />
        <Route path="/reset-password" element={<ResetPasswordPage />} />
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
        <Route path="*" element={<NotFoundPage />} />
      </Route>

      <Route
        path="/admin"
        element={(
          <ProtectedRoute allowedRoles={['ADMIN']}>
            <AdminLayout />
          </ProtectedRoute>
        )}
      >
        <Route index element={<Navigate to="dashboard" replace />} />
        <Route path="dashboard" element={<AdminDashboardPage />} />
        <Route path="products" element={<AdminProductPage />} />
        <Route path="categories" element={<AdminCategoryPage />} />
        <Route path="orders" element={<AdminOrderPage />} />
        <Route path="payments" element={<AdminPaymentPage />} />
        <Route path="refunds" element={<AdminRefundPage />} />
        <Route path="users" element={<AdminUserPage />} />
        <Route path="promotions" element={<AdminPromotionPage />} />
        <Route path="suppliers" element={<AdminSupplierPage />} />
        <Route path="imports" element={<AdminImportPage />} />
        <Route path="supplier-returns" element={<AdminSupplierReturnPage />} />
        <Route path="logistics" element={<AdminLogisticsPage />} />
        <Route path="returns" element={<AdminReturnInspectionPage />} />
        <Route path="reviews" element={<AdminReviewModerationPage />} />
        <Route path="moderation" element={<AdminModerationPage />} />
        <Route path="notifications" element={<AdminNotificationPage />} />
        <Route path="inventory" element={<AdminInventoryPage />} />
        <Route
          path="statistics"
          element={(
            <Suspense fallback={<div className="admin-empty-mini">Loading statistics dashboard...</div>}>
              <AdminStatisticsPage />
            </Suspense>
          )}
        />
        <Route path="uploads" element={<AdminUploadPage />} />
        <Route path="*" element={<Navigate to="dashboard" replace />} />
      </Route>
    </Routes>
  );
}

export default App;
