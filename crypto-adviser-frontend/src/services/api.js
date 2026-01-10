import axios from 'axios';

const API_BASE_URL = 'http://localhost:5051';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('authToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
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
  
  validateToken: () =>
    api.get('/api/auth/validate'),
};

export const userAPI = {
  getProfile: () =>
    api.get('/api/users/profile'),
  
  updateProfile: (data) =>
    api.put('/api/users/profile', data),
};

export const recommendationAPI = {
  generate: (profileData) =>
    api.post('/api/v1/recommendations/generate', profileData),
  
  generateAsync: (profileData) =>
    api.post('/api/v1/recommendations/generate-async', profileData),
  
  getHistory: () =>
    api.get('/api/v1/recommendations/history'),
  
  getHistoryByCrypto: (cryptocurrency) =>
    api.get(`/api/v1/recommendations/history/${cryptocurrency}`),
  
  getRecent: (days = 30) =>
    api.get(`/api/v1/recommendations/recent?days=${days}`),
  
  getById: (id) =>
    api.get(`/api/v1/recommendations/${id}`),
};

export default api;