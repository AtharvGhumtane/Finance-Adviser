import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const ProtectedRoute = ({ children }) => {
  const { token, loading } = useAuth();

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-[#0A0E27]">
        <div className="text-center">
          <div className="relative w-20 h-20 mx-auto mb-6">
            <div className="absolute inset-0 border-4 border-yellow-500/20 rounded-full"></div>
            <div className="absolute inset-0 border-4 border-yellow-400 border-t-transparent rounded-full animate-spin"></div>
          </div>
          <p className="text-gray-400 text-lg">Loading...</p>
        </div>
      </div>
    );
  }

  return token ? children : <Navigate to="/login" replace />;
};

export default ProtectedRoute;