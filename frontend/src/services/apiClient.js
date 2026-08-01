const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';
const TOKEN_KEY = 'newToyStoreToken';

export function getStoredToken() {
  return localStorage.getItem(TOKEN_KEY);
}

export function storeToken(token) {
  localStorage.setItem(TOKEN_KEY, token);
}

export function clearStoredToken() {
  localStorage.removeItem(TOKEN_KEY);
}

export async function apiClient(endpoint, options = {}) {
  const token = getStoredToken();
  const isFormData = options.body instanceof FormData;
  const headers = isFormData
    ? { ...options.headers }
    : {
        'Content-Type': 'application/json',
        ...options.headers,
      };

  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  const response = await fetch(`${API_BASE_URL}${endpoint}`, {
    ...options,
    headers,
  });

  if (response.status === 401) {
    clearStoredToken();
    throw {
      status: 401,
      message: 'Phien dang nhap da het han. Vui long dang nhap lai.',
    };
  }

  if (response.status === 204) {
    return null;
  }

  const contentType = response.headers.get('content-type') || '';
  const data = contentType.includes('application/json') ? await response.json() : await response.text();

  if (!response.ok) {
    throw typeof data === 'object'
      ? data
      : { status: response.status, message: data || 'Da xay ra loi. Vui long thu lai.' };
  }

  return data;
}
