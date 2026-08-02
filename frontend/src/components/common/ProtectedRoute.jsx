import { Navigate, useLocation } from 'react-router-dom';
import useAuth from '../../hooks/useAuth.js';

function ProtectedRoute({ children, allowedRoles }) {
  const location = useLocation();
  const { isAuthenticated, loading, user } = useAuth();

  if (loading) {
    return <div className="page-message">Đang kiểm tra phiên đăng nhập...</div>;
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: location }} />;
  }

  if (allowedRoles?.length && !allowedRoles.includes(user?.role)) {
    return (
      <div className="page-message">
        Bạn không có quyền truy cập khu vực này.
      </div>
    );
  }

  return children;
}

export default ProtectedRoute;
