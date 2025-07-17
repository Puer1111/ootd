const AuthManager = {
    setToken: function(token) {

        localStorage.setItem('token', token);
    },

    getToken: function() {
        return localStorage.getItem('token');
    },

    removeToken: function() {
        localStorage.removeItem('token');
        // ✅ 키 통일: 'token'으로 변경
        localStorage.setItem('auth_token', auth_token);
    },

    getToken: function() {
        // ✅ 키 통일: 'token'으로 변경
        return localStorage.getItem('auth_token');
    },

    removeToken: function() {
        // ✅ 키 통일: 'token'으로 변경
        localStorage.removeItem('auth_token');
        // 기존 키도 삭제 (혹시 남아있을 경우)

        localStorage.removeItem('auth_token');
    },

    isLoggedIn: function() {
        return this.getToken() !== null;
    },

    redirectToLogin: function() {
        window.location.href = '/login';
    }
};

document.addEventListener('DOMContentLoaded', function() {
    // 필수 요소들 존재 확인
    const requiredElements = [
        'loading', 'userInfo', 'errorMessage', 'logoutBtn', 'passwordChangeForm'
    ];

    const missingElements = requiredElements.filter(id => !document.getElementById(id));

    if (missingElements.length > 0) {
        console.error('필수 HTML 요소가 없습니다:', missingElements);
        return;
    }

    // 초기화
    loadUserInfo();
    loadUserStats();

    // 이벤트 리스너 등록
    setupEventListeners();
});

function setupEventListeners() {
    // 로그아웃 버튼
    const logoutBtn = document.getElementById('logoutBtn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', logout);
    }

    // 비밀번호 변경 폼
    const passwordChangeForm = document.getElementById('passwordChangeForm');
    if (passwordChangeForm) {
        passwordChangeForm.addEventListener('submit', changePassword);
    }

    // 좋아요 상품목록 메뉴
    const likedProductsMenu = document.querySelector('.menu-item[onclick="goToLikedProducts()"]');
    if (likedProductsMenu) {
        likedProductsMenu.addEventListener('click', function(e) {
            e.preventDefault();
            console.log('좋아요 상품목록 클릭됨!');
            goToLikedProducts();
        });
        likedProductsMenu.removeAttribute('onclick');
    }

    // 모든 메뉴 아이템에 대한 이벤트 리스너 추가
    setupMenuEventListeners();
}

function setupMenuEventListeners() {
    // 메뉴 아이템들에 대한 이벤트 리스너 설정
    const menuItems = [
        { selector: '.menu-item[onclick*="goToCart"]', handler: goToCart },
        { selector: '.menu-item[onclick*="goToOrderHistory"]', handler: goToOrderHistory },
        { selector: '.menu-item[onclick*="goToCancelHistory"]', handler: goToCancelHistory },
        { selector: '.menu-item[onclick*="goToMyReviews"]', handler: goToMyReviews }
    ];

    menuItems.forEach(item => {
        const element = document.querySelector(item.selector);
        if (element) {
            element.addEventListener('click', function(e) {
                e.preventDefault();
                item.handler();
            });
            element.removeAttribute('onclick');
        }
    });
}

function togglePersonalInfo() {
    const detail = document.getElementById('personalInfoDetail');
    if (!detail) {
        console.error('personalInfoDetail 요소를 찾을 수 없습니다.');
        return;
    }

    const isVisible = detail.style.display !== 'none' && detail.style.display !== '';
    detail.style.display = isVisible ? 'none' : 'block';
}

function loadUserInfo() {
    const token = AuthManager.getToken();

    console.log('토큰 확인:', token ? '있음' : '없음');

    if (!token) {
        alert('로그인이 필요합니다.');
        AuthManager.redirectToLogin();
        return;
    }

    const loadingDiv = document.getElementById('loading');
    const userInfoDiv = document.getElementById('userInfo');
    const errorDiv = document.getElementById('errorMessage');

    // 요소 존재 확인
    if (!loadingDiv || !userInfoDiv || !errorDiv) {
        console.error('필수 UI 요소가 없습니다.');
        return;
    }

    // 로딩 상태 표시
    loadingDiv.style.display = 'block';
    userInfoDiv.style.display = 'none';
    errorDiv.style.display = 'none';

    fetch('/api/auth/mypage', {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        }
    })
        .then(response => {
            console.log('마이페이지 API 응답 상태:', response.status);

            if (!response.ok) {
                if (response.status === 401) {
                    throw new Error('UNAUTHORIZED');
                }
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }
            return response.json();
        })
        .then(data => {
            console.log('마이페이지 API 응답 데이터:', data);
            loadingDiv.style.display = 'none';

            if (data.success && data.user) {
                displayUserInfo(data.user);
                userInfoDiv.style.display = 'block';
            } else {
                throw new Error('Invalid response data');
            }
        })
        .catch(error => {
            console.error('마이페이지 로드 에러:', error);
            loadingDiv.style.display = 'none';

            if (error.message === 'UNAUTHORIZED') {
                alert('로그인이 만료되었습니다. 다시 로그인해주세요.');
                AuthManager.removeToken();
                AuthManager.redirectToLogin();
            } else {
                errorDiv.style.display = 'block';
                errorDiv.textContent = '사용자 정보를 불러오는데 실패했습니다.';
            }
        });
}

function displayUserInfo(user) {
    // 사용자 정보 표시 함수 분리
    const userFields = [
        { id: 'userName', value: user.name },
        { id: 'userEmail', value: user.email },
        { id: 'userUsername', value: user.username },
        { id: 'userPhone', value: user.phone }
    ];

    userFields.forEach(field => {
        const element = document.getElementById(field.id);
        if (element) {
            element.textContent = field.value || '정보 없음';
        } else {
            console.warn(`${field.id} 요소를 찾을 수 없습니다.`);
        }
    });
}

function loadUserStats() {
    const token = AuthManager.getToken();

    if (!token) {
        console.log('토큰이 없어 사용자 통계를 로드하지 않습니다.');
        return;
    }

    fetch('/api/auth/user-stats', {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        }
    })
        .then(response => {
            if (response.ok) {
                return response.json();
            }
            throw new Error(`HTTP ${response.status}: 사용자 통계 로드 실패`);
        })
        .then(data => {
            if (data.success) {
                updateUserStats(data);
            } else {
                console.warn('사용자 통계 데이터 형식이 올바르지 않습니다.');
            }
        })
        .catch(error => {
            console.log('사용자 통계 로드 실패:', error);
            // 에러 시 기본값 유지
        });
}

function updateUserStats(data) {
    const statsFields = [
        { id: 'user-points', value: data.points || 0 },
        { id: 'user-coupons', value: data.coupons || 0 },
        { id: 'user-reviews', value: data.reviewCount || 0 }
    ];

    statsFields.forEach(field => {
        const element = document.getElementById(field.id);
        if (element) {
            element.textContent = field.value;
        }
    });
}

function showPasswordModal() {
    const modal = document.getElementById('passwordModal');
    if (!modal) {
        console.error('passwordModal 요소를 찾을 수 없습니다.');
        return;
    }
    modal.style.display = 'block';
    clearPasswordForm();
}

function closePasswordModal() {
    const modal = document.getElementById('passwordModal');
    if (modal) {
        modal.style.display = 'none';
    }
    clearPasswordForm();
}

function clearPasswordForm() {
    const formFields = ['currentPassword', 'newPassword', 'confirmPassword'];
    const messageDiv = document.getElementById('passwordFormMessage');

    formFields.forEach(fieldId => {
        const field = document.getElementById(fieldId);
        if (field) {
            field.value = '';
        }
    });

    if (messageDiv) {
        messageDiv.textContent = '';
        messageDiv.className = 'form-message';
    }

    document.querySelectorAll('.error-message').forEach(elem => {
        elem.textContent = '';
        if (elem.parentElement) {
            elem.parentElement.classList.remove('show-error');
        }
    });
}

function changePassword(e) {
    e.preventDefault();

    const currentPassword = document.getElementById('currentPassword')?.value;
    const newPassword = document.getElementById('newPassword')?.value;
    const confirmPassword = document.getElementById('confirmPassword')?.value;
    const messageDiv = document.getElementById('passwordFormMessage');

    if (!messageDiv) {
        console.error('passwordFormMessage 요소를 찾을 수 없습니다.');
        return;
    }

    // 입력 유효성 검사
    if (!currentPassword || !newPassword || !confirmPassword) {
        showPasswordMessage(messageDiv, '모든 필드를 입력해주세요.', 'error');
        return;
    }

    if (newPassword !== confirmPassword) {
        showPasswordMessage(messageDiv, '새 비밀번호와 확인 비밀번호가 일치하지 않습니다.', 'error');
        return;
    }

    if (newPassword.length < 6) {
        showPasswordMessage(messageDiv, '새 비밀번호는 최소 6자 이상이어야 합니다.', 'error');
        return;
    }

    if (newPassword === currentPassword) {
        showPasswordMessage(messageDiv, '새 비밀번호는 현재 비밀번호와 달라야 합니다.', 'error');
        return;
    }

    const token = AuthManager.getToken();

    if (!token) {
        showPasswordMessage(messageDiv, '로그인이 필요합니다.', 'error');
        return;
    }

    fetch('/api/auth/change-password', {
        method: 'POST',
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            currentPassword: currentPassword,
            newPassword: newPassword,
            confirmPassword: confirmPassword
        })
    })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                showPasswordMessage(messageDiv, data.message, 'success');
                setTimeout(() => {
                    closePasswordModal();
                    alert('비밀번호가 성공적으로 변경되었습니다.');
                }, 1000);
            } else {
                showPasswordMessage(messageDiv, data.message || '비밀번호 변경에 실패했습니다.', 'error');
            }
        })
        .catch(error => {
            console.error('비밀번호 변경 에러:', error);
            showPasswordMessage(messageDiv, '비밀번호 변경 중 오류가 발생했습니다.', 'error');
        });
}

function showPasswordMessage(messageDiv, message, type) {
    messageDiv.textContent = message;
    messageDiv.className = `form-message ${type === 'error' ? 'error-message' : 'success-message'}`;
}

function logout() {
    if (confirm('로그아웃 하시겠습니까?')) {
        AuthManager.removeToken();
        AuthManager.redirectToLogin();
    }
}

function goToLogin() {
    AuthManager.removeToken();
    AuthManager.redirectToLogin();
}

// 네비게이션 함수들
function goToCart() {
    window.location.href = '/cart';
}

function goToLikedProducts() {
    window.location.href = '/liked-products';
}

function goToOrderHistory() {
    window.location.href = '/order-history';
}

function goToCancelHistory() {
    window.location.href = '/cancel-history';
}

function goToMyReviews() {
    window.location.href = '/api/reviews/my-reviews';
}

// 모달 외부 클릭 시 닫기
window.onclick = function(event) {
    const modal = document.getElementById('passwordModal');
    if (event.target === modal) {
        closePasswordModal();
    }
}

// 디버깅을 위한 함수
function debugInfo() {
    console.log('=== 마이페이지 디버그 정보 ===');
    console.log('토큰:', AuthManager.getToken() ? '존재' : '없음');
    console.log('필수 요소 존재 여부:');

    const requiredElements = [
        'loading', 'userInfo', 'errorMessage', 'logoutBtn',
        'passwordChangeForm', 'passwordModal'
    ];

    requiredElements.forEach(id => {
        const element = document.getElementById(id);
        console.log(`- ${id}: ${element ? '존재' : '없음'}`);
    });

    // 현재 페이지 URL 확인
    console.log('현재 URL:', window.location.href);

    // 네트워크 상태 확인
    console.log('온라인 상태:', navigator.onLine);
}

// 페이지 로드 완료 후 디버그 정보 출력 (개발 환경에서만)
if (window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1') {
    window.addEventListener('load', debugInfo);
}