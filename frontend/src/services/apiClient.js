const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';
const TOKEN_KEY = 'newToyStoreToken';
const API_REQUEST_STARTED = 'new-toy-store:api-request-started';
const API_REQUEST_FINISHED = 'new-toy-store:api-request-finished';
let requestSequence = 0;

function notifyRequest(type, detail) {
  window.dispatchEvent(new CustomEvent(type, { detail }));
}

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
  const requestId = ++requestSequence;
  const method = options.method || 'GET';
  notifyRequest(API_REQUEST_STARTED, { requestId, endpoint, method });

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

  try {
    const response = await fetch(`${API_BASE_URL}${endpoint}`, {
      ...options,
      headers,
    });

    if (response.status === 204) {
      return null;
    }

    const contentType = response.headers.get('content-type') || '';
    const data = contentType.includes('application/json') ? await response.json() : await response.text();

    if (!response.ok) {
      if (response.status === 401 && token) {
        clearStoredToken();
      }

      if (typeof data === 'object' && data !== null) {
        throw data;
      }

      const fallbackMessages = {
        401: token
          ? 'Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.'
          : 'Email hoặc mật khẩu không chính xác.',
        403: 'Bạn không có quyền thực hiện thao tác này.',
      };

      throw {
        status: response.status,
        message: data || fallbackMessages[response.status] || 'Đã xảy ra lỗi. Vui lòng thử lại.',
      };
    }

    return data;
  } finally {
    notifyRequest(API_REQUEST_FINISHED, { requestId, endpoint, method });
  }
}
