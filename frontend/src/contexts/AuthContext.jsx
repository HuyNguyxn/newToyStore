import { createContext, useEffect, useMemo, useReducer } from 'react';
import { clearStoredToken, getStoredToken, storeToken } from '../services/apiClient.js';
import { getCurrentUser, loginUser, registerUser, updateCurrentUser } from '../services/authService.js';

const AuthContext = createContext(null);

const initialState = {
  user: null,
  token: getStoredToken(),
  loading: Boolean(getStoredToken()),
  error: null,
};

function authReducer(state, action) {
  switch (action.type) {
    case 'AUTH_LOADING':
      return { ...state, loading: true, error: null };
    case 'AUTH_SUCCESS':
      return {
        ...state,
        user: action.payload.user,
        token: action.payload.token,
        loading: false,
        error: null,
      };
    case 'AUTH_PROFILE_SUCCESS':
      return { ...state, user: action.payload, loading: false, error: null };
    case 'AUTH_ERROR':
      return { ...state, loading: false, error: action.payload };
    case 'AUTH_IDLE':
      return { ...state, loading: false, error: null };
    case 'AUTH_LOGOUT':
      return { user: null, token: null, loading: false, error: null };
    default:
      return state;
  }
}

export function AuthProvider({ children }) {
  const [state, dispatch] = useReducer(authReducer, initialState);

  useEffect(() => {
    if (!state.token) {
      return;
    }

    let active = true;

    getCurrentUser()
      .then((user) => {
        if (active) {
          dispatch({ type: 'AUTH_PROFILE_SUCCESS', payload: user });
        }
      })
      .catch((error) => {
        clearStoredToken();
        if (active) {
          dispatch({ type: 'AUTH_LOGOUT' });
          dispatch({ type: 'AUTH_ERROR', payload: error.message || 'Kh?ng th? t?i th?ng tin t?i kho?n.' });
        }
      });

    return () => {
      active = false;
    };
  }, []);

  async function login(credentials) {
    dispatch({ type: 'AUTH_LOADING' });
    try {
      const response = await loginUser(credentials);
      storeToken(response.accessToken);
      dispatch({
        type: 'AUTH_SUCCESS',
        payload: {
          token: response.accessToken,
          user: response.user,
        },
      });
      return response.user;
    } catch (error) {
      dispatch({ type: 'AUTH_ERROR', payload: error?.message || 'Đăng nhập thất bại.' });
      throw error;
    }
  }

  async function register(payload) {
    dispatch({ type: 'AUTH_LOADING' });
    try {
      const user = await registerUser(payload);
      dispatch({ type: 'AUTH_IDLE' });
      return user;
    } catch (error) {
      dispatch({ type: 'AUTH_ERROR', payload: error?.message || 'Đăng ký thất bại.' });
      throw error;
    }
  }

  async function updateProfile(payload) {
    dispatch({ type: 'AUTH_LOADING' });
    try {
      const user = await updateCurrentUser(payload);
      dispatch({ type: 'AUTH_PROFILE_SUCCESS', payload: user });
      return user;
    } catch (error) {
      dispatch({ type: 'AUTH_ERROR', payload: error?.message || 'Cập nhật tài khoản thất bại.' });
      throw error;
    }
  }

  function setUserProfile(userProfile) {
    if (userProfile) {
      dispatch({ type: 'AUTH_PROFILE_SUCCESS', payload: userProfile });
    }
  }

  function logout() {
    clearStoredToken();
    dispatch({ type: 'AUTH_LOGOUT' });
  }

  const value = useMemo(() => ({
    ...state,
    isAuthenticated: Boolean(state.token && state.user),
    isAdmin: state.user?.role === 'ADMIN',
    isStaff: ['STAFF', 'MANAGER', 'ADMIN'].includes(state.user?.role),
    login,
    register,
    updateProfile,
    setUserProfile,
    logout,
  }), [state]);

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
}

export default AuthContext;
