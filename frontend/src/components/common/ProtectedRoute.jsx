import { Navigate, useLocation } from 'react-router-dom';
import useAuth from '../../hooks/useAuth.js';

function ProtectedRoute({ children }) {
  const location = useLocation();
  const { isAuthenticated, loading } = useAuth();

  if (loading) {
    return <div className="page-message">Dang kiem tra phien dang nhap...</div>;
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: location }} />;
  }

  return children;
}

export default ProtectedRoute;
