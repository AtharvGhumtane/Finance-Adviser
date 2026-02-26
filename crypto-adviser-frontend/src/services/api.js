import axios from 'axios';

const API_BASE_URL = 'http://localhost:5051';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: { 'Content-Type': 'application/json' },
});

api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('authToken');
    if (token) config.headers.Authorization = `Bearer ${token}`;
    return config;
  },
  (error) => Promise.reject(error)
);

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('authToken');
      localStorage.removeItem('userData');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export const authAPI = {
  login: (emailOrUsername, password) =>
    api.post('/api/auth/login', { emailOrUsername, password }),
  signup: (email, username, password) =>
    api.post('/api/auth/signup', { email, username, password }),
  validateToken: () => api.get('/api/auth/validate'),
};

export const userAPI = {
  getProfile: () => api.get('/api/users/profile'),
  updateProfile: (data) => api.put('/api/users/profile', data),
};

export const recommendationAPI = {
  generate: (data) => api.post('/api/v1/recommendations/generate', data),
  generateAsync: (data) => api.post('/api/v1/recommendations/generate-async', data),
  getHistory: () => api.get('/api/v1/recommendations/history'),
  getHistoryByCrypto: (c) => api.get(`/api/v1/recommendations/history/${c}`),
  getRecent: (days = 30) => api.get(`/api/v1/recommendations/recent?days=${days}`),
  getById: (id) => api.get(`/api/v1/recommendations/${id}`),
};

// Tax Optimizer Service
export const taxAPI = {
  optimize: (data) => api.post('/api/v1/tax/optimize', data),
  compareRegimes: (data) => api.post('/api/v1/tax/compare-regimes', data),
  hraExemption: (data) => api.post('/api/v1/tax/hra-exemption', data),
  getAnalyses: (userId) => api.get(`/api/v1/tax/analyses/${userId}`),
};

// Credit Card Trap Service
export const creditAPI = {
  analyze: (data) => api.post('/api/v1/credit/analyze', data),
  quickTrapCheck: (data) => api.post('/api/v1/credit/quick-trap-check', data),
  riskOnly: (data) => api.post('/api/v1/credit/risk-only', data),
  getAnalyses: (userId) => api.get(`/api/v1/credit/analyses/user/${userId}`),
};

export default api;