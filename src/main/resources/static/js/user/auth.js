// 공통 인증 관리 JavaScript (static/js/auth.js)

// 토큰 관리 함수들
const AuthManager = {
    // 토큰 저장
    setToken: function(token) {
        localStorage.setItem('auth_token', token);
    },

    // 토큰 가져오기
    getToken: function() {
        return localStorage.getItem('auth_token');
    },

    // 토큰 제거
    removeToken: function() {
        localStorage.removeItem('auth_token');
    },

    // 토큰 유효성 확인
    isLoggedIn: function() {
        return this.getToken() !== null;
    },

    // 인증이 필요한 API 요청을 위한 공통 함수
    authenticatedFetch: function(url, options = {}) {
        const token = this.getToken();

        if (!token) {
            this.redirectToLogin();
            return Promise.reject(new Error('No token available'));
        }

        // 기본 헤더 설정
        const defaultHeaders = {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
        };

        // 옵션 병합
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

    // 권한 없음 처리
    handleUnauthorized: function() {
        alert('로그인이 만료되었습니다. 다시 로그인해주세요.');
        this.removeToken();
        this.redirectToLogin();
    },

    // 로그인 페이지로 리다이렉트
    redirectToLogin: function() {
        window.location.href = '/login';
    },

    // 로그아웃
    logout: function() {
        if (confirm('로그아웃 하시겠습니까?')) {
            this.removeToken();
            this.redirectToLogin();
        }
    }
};

// 페이지 로드 시 토큰 확인 (인증이 필요한 페이지에서 사용)
function checkAuthOnPageLoad() {
    if (!AuthManager.isLoggedIn()) {
        alert('로그인이 필요합니다.');
        AuthManager.redirectToLogin();
    }
}

// 전역에서 사용할 수 있도록 window 객체에 추가
window.AuthManager = AuthManager;
window.checkAuthOnPageLoad = checkAuthOnPageLoad;