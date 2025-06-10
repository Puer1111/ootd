(function() {
    // 토큰 확인 함수
    function getToken() {
        // ✅ 키 통일: 'token'으로 변경
        let token = localStorage.getItem('token');
        if (!token) {
            // 기존 키도 확인 (하위 호환성)
            token = localStorage.getItem('auth_token');
        }
        if (!token) {
            // 쿠키에서도 확인
            const cookies = document.cookie.split(';');
            for (let cookie of cookies) {
                const [key, value] = cookie.trim().split('=');
                if (key === 'jwt-token') {
                    token = decodeURIComponent(value);
                    break;
                }
            }
        }
        return token;
    }

    console.log('=== 페이지 로드 전 인증 체크 ===');
    const token = getToken();
    console.log('토큰 존재:', token ? '예' : '아니오');
    if (token) {
        console.log('토큰 앞 20자:', token.substring(0, 20) + '...');
    }

    if (!token) {
        console.log('토큰 없음 - 즉시 리다이렉트');
        alert('로그인이 필요한 서비스입니다.');
        window.location.href = '/login';
        return;
    }

    console.log('토큰 확인됨 - 페이지 로딩 계속');

    // DOM 준비되면 페이지 표시
    function showPage() {
        document.body.classList.add('auth-verified');
        const loadingDiv = document.getElementById('auth-loading');
        if (loadingDiv) {
            loadingDiv.remove();
        }
        console.log('페이지 표시됨');
    }

    // DOM이 준비되면 페이지 표시
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', showPage);
    } else {
        showPage();
    }
})();