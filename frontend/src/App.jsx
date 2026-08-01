import { Navigate, Route, Routes } from 'react-router-dom';
import AdminLayout from './components/admin/AdminLayout.jsx';
import ProtectedRoute from './components/common/ProtectedRoute.jsx';
import CustomerLayout from './components/layout/CustomerLayout.jsx';
import AdminDashboardPage from './pages/admin/AdminDashboardPage.jsx';
import AdminInventoryPage from './pages/admin/AdminInventoryPage.jsx';
import AdminResourcePage from './pages/admin/AdminResourcePage.jsx';
import AdminStatisticsPage from './pages/admin/AdminStatisticsPage.jsx';
import AdminUploadPage from './pages/admin/AdminUploadPage.jsx';
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
        <Route path="products" element={<AdminResourcePage resource="products" />} />
        <Route path="categories" element={<AdminResourcePage resource="categories" />} />
        <Route path="orders" element={<AdminResourcePage resource="orders" />} />
        <Route path="payments" element={<AdminResourcePage resource="payments" />} />
        <Route path="users" element={<AdminResourcePage resource="users" />} />
        <Route path="promotions" element={<AdminResourcePage resource="promotions" />} />
        <Route path="suppliers" element={<AdminResourcePage resource="suppliers" />} />
        <Route path="imports" element={<AdminResourcePage resource="imports" />} />
        <Route path="supplier-returns" element={<AdminResourcePage resource="supplierReturns" />} />
        <Route path="logistics" element={<AdminResourcePage resource="logistics" />} />
        <Route path="returns" element={<AdminResourcePage resource="returns" />} />
        <Route path="reviews" element={<AdminResourcePage resource="reviews" />} />
        <Route path="moderation" element={<AdminResourcePage resource="moderation" />} />
        <Route path="inventory" element={<AdminInventoryPage />} />
        <Route path="statistics" element={<AdminStatisticsPage />} />
        <Route path="uploads" element={<AdminUploadPage />} />
        <Route path="*" element={<Navigate to="dashboard" replace />} />
      </Route>
    </Routes>
  );
}

export default App;
