import { createContext, useState, useEffect, useContext } from 'react';
import { authAPI } from '../services/api';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(localStorage.getItem('authToken'));
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Load user data from localStorage on mount
    if (token) {
      const userData = JSON.parse(localStorage.getItem('userData') || '{}');
      setUser(userData);
    }
    setLoading(false);
  }, [token]);

  const login = async (emailOrUsername, password) => {
    try {
      const response = await authAPI.login(emailOrUsername, password);
      console.log('Login response:', response.data);
      
      // Backend returns 'accessToken', not 'token'
      const { accessToken, userId, username, email } = response.data;

      if (!accessToken) {
        throw new Error('No token received from server');
      }

      localStorage.setItem('authToken', accessToken);
      localStorage.setItem('userData', JSON.stringify({ userId, username, email }));

      setToken(accessToken);
      setUser({ userId, username, email });

      return response.data;
    } catch (error) {
      console.error('Login error:', error);
      throw new Error(error.response?.data?.message || error.message || 'Login failed');
    }
  };

  const signup = async (email, username, password) => {
    try {
      const response = await authAPI.signup(email, username, password);
      const { accessToken, userId } = response.data;

      if (!accessToken) {
        throw new Error('No token received from server');
      }

      localStorage.setItem('authToken', accessToken);
      localStorage.setItem('userData', JSON.stringify({ userId, username, email }));

      setToken(accessToken);
      setUser({ userId, username, email });

      return response.data;
    } catch (error) {
      throw new Error(error.response?.data?.message || error.message || 'Signup failed');
    }
  };

  const logout = () => {
    localStorage.removeItem('authToken');
    localStorage.removeItem('userData');
    setToken(null);
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, token, login, signup, logout, loading }}>
      {children}
    </AuthContext.Provider>
  );
};

// THIS WAS MISSING - EXPORT useAuth HOOK
export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

export default AuthContext;