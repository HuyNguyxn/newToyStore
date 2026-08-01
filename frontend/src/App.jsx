import { Route, Routes } from 'react-router-dom';
import AdminLayout from './components/admin/AdminLayout.jsx';
import ProtectedRoute from './components/common/ProtectedRoute.jsx';
import CustomerLayout from './components/layout/CustomerLayout.jsx';
import AdminDashboardPage from './pages/admin/AdminDashboardPage.jsx';
import LoginPage from './pages/auth/LoginPage.jsx';
import RegisterPage from './pages/auth/RegisterPage.jsx';
import CartPage from './pages/cart/CartPage.jsx';
import CheckoutPage from './pages/cart/CheckoutPage.jsx';
import HomePage from './pages/home/HomePage.jsx';
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
        <Route
          path="/profile"
          element={(
            <ProtectedRoute>
              <ProfilePage />
            </ProtectedRoute>
          )}
        />
      </Route>

      <Route
        path="/admin"
        element={(
          <ProtectedRoute allowedRoles={['ADMIN']}>
            <AdminLayout />
          </ProtectedRoute>
        )}
      >
        <Route path="dashboard" element={<AdminDashboardPage />} />
      </Route>
    </Routes>
  );
}

export default App;
