import { Navigate, useLocation } from 'react-router-dom';
import useAuth from '../../hooks/useAuth.js';

function ProtectedRoute({ children, allowedRoles }) {
  const location = useLocation();
  const { isAuthenticated, loading, user } = useAuth();

  if (loading) {
    return <div className="page-message">Dang kiem tra phien dang nhap...</div>;
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: location }} />;
  }

  if (allowedRoles?.length && !allowedRoles.includes(user?.role)) {
    return (
      <div className="page-message">
        Ban khong co quyen truy cap khu vuc nay.
      </div>
    );
  }

  return children;
}

export default ProtectedRoute;
