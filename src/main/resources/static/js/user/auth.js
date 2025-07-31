const AuthManager = {
    setToken: function(auth_token) {
        localStorage.setItem('auth_token', auth_token);
    },

    getToken: function() {
        return localStorage.getItem('auth_token');
    },

    removeToken: function() {
        localStorage.removeItem('auth_token');
    },

    isLoggedIn: function() {
        return this.getToken() !== null;
    },

    authenticatedFetch: function(url, options = {}) {
        const token = this.getToken();

        if (!token) {
            this.redirectToLogin();
            return Promise.reject(new Error('No token available'));
        }

        const defaultHeaders = {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
        };

        const mergedOptions = {
            ...options,
            headers: {
                ...defaultHeaders,
                ...(options.headers || {})
            }
        };

        return fetch(url, mergedOptions)
            .then(response => {
                if (response.status === 401) {
                    // 토큰이 만료되었거나 유효하지 않음
                    this.handleUnauthorized();
                    throw new Error('UNAUTHORIZED');
                }
                return response;
            });
    },

    handleUnauthorized: function() {
        alert('로그인이 만료되었습니다. 다시 로그인해주세요.');
        this.removeToken();
        this.redirectToLogin();
    },

    redirectToLogin: function() {
        window.location.href = '/login';
    },

    logout: function() {
        if (confirm('로그아웃 하시겠습니까?')) {
            this.removeToken();
            this.redirectToLogin();
        }
    }
};

function checkAuthOnPageLoad() {
    if (!AuthManager.isLoggedIn()) {
        alert('로그인이 필요합니다.');
        AuthManager.redirectToLogin();
    }
}

window.AuthManager = AuthManager;
window.checkAuthOnPageLoad = checkAuthOnPageLoad;