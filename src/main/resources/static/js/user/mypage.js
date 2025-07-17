const AuthManager = {
    setToken: function(token) {
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
    loadUserInfo();
    loadUserStats(); // 🆕 사용자 통계 로드 추가
    document.getElementById('logoutBtn').addEventListener('click', logout);
    document.getElementById('passwordChangeForm').addEventListener('submit', changePassword);

    // ✅ 좋아요 상품목록 이벤트 리스너 추가
    const likedProductsMenu = document.querySelector('.menu-item[onclick="goToLikedProducts()"]');
    if (likedProductsMenu) {
        likedProductsMenu.addEventListener('click', function() {
            console.log('좋아요 상품목록 클릭됨!');
            goToLikedProducts();
        });
        // onclick 속성 제거 (중복 방지)
        likedProductsMenu.removeAttribute('onclick');
    }
});

function togglePersonalInfo() {
    const detail = document.getElementById('personalInfoDetail');
    const isVisible = detail.style.display !== 'none' && detail.style.display !== '';

    if (isVisible) {
        detail.style.display = 'none';
    } else {
        detail.style.display = 'block';
    }
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
                throw new Error('Network response was not ok');
            }
            return response.json();
        })
        .then(data => {
            console.log('마이페이지 API 응답 데이터:', data);
            loadingDiv.style.display = 'none';

            if (data.success && data.user) {
                document.getElementById('userName').textContent = data.user.name || '정보 없음';
                document.getElementById('userEmail').textContent = data.user.email || '정보 없음';
                document.getElementById('userUsername').textContent = data.user.username || '정보 없음';
                document.getElementById('userPhone').textContent = data.user.phone || '정보 없음';

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
            }
        });
}

// 🆕 사용자 통계 정보 로드 (적립금, 쿠폰, 후기 개수)
function loadUserStats() {
    const token = AuthManager.getToken();

    if (!token) return;

    // 후기 개수 가져오기
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
            throw new Error('Failed to load user stats');
        })
        .then(data => {
            if (data.success) {
                // 적립금 (기본값 0)
                const pointsElement = document.getElementById('user-points');
                if (pointsElement) {
                    pointsElement.textContent = data.points || 0;
                }

                // 쿠폰 (기본값 0)
                const couponsElement = document.getElementById('user-coupons');
                if (couponsElement) {
                    couponsElement.textContent = data.coupons || 0;
                }

                // 후기 개수 (내가 쓴 리뷰 개수)
                const reviewsElement = document.getElementById('user-reviews');
                if (reviewsElement) {
                    reviewsElement.textContent = data.reviewCount || 0;
                }
            }
        })
        .catch(error => {
            console.log('사용자 통계 로드 실패 (로그인 안 됨 또는 오류):', error);
            // 에러 시 기본값 유지
        });
}

function showPasswordModal() {
    document.getElementById('passwordModal').style.display = 'block';
    clearPasswordForm();
}

function closePasswordModal() {
    document.getElementById('passwordModal').style.display = 'none';
    clearPasswordForm();
}

function clearPasswordForm() {
    document.getElementById('currentPassword').value = '';
    document.getElementById('newPassword').value = '';
    document.getElementById('confirmPassword').value = '';
    document.getElementById('passwordFormMessage').textContent = '';
    document.getElementById('passwordFormMessage').className = 'form-message';

    document.querySelectorAll('.error-message').forEach(elem => {
        elem.textContent = '';
        elem.parentElement.classList.remove('show-error');
    });
}

function changePassword(e) {
    e.preventDefault();

    const currentPassword = document.getElementById('currentPassword').value;
    const newPassword = document.getElementById('newPassword').value;
    const confirmPassword = document.getElementById('confirmPassword').value;
    const messageDiv = document.getElementById('passwordFormMessage');

    if (!currentPassword || !newPassword || !confirmPassword) {
        messageDiv.textContent = '모든 필드를 입력해주세요.';
        messageDiv.className = 'form-message error-message';
        return;
    }

    if (newPassword !== confirmPassword) {
        messageDiv.textContent = '새 비밀번호와 확인 비밀번호가 일치하지 않습니다.';
        messageDiv.className = 'form-message error-message';
        return;
    }

    if (newPassword.length < 6) {
        messageDiv.textContent = '새 비밀번호는 최소 6자 이상이어야 합니다.';
        messageDiv.className = 'form-message error-message';
        return;
    }

    const token = AuthManager.getToken();

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
                messageDiv.textContent = data.message;
                messageDiv.className = 'form-message success-message';

                setTimeout(() => {
                    closePasswordModal();
                    alert('비밀번호가 성공적으로 변경되었습니다.');
                }, 1000);
            } else {
                messageDiv.textContent = data.message;
                messageDiv.className = 'form-message error-message';
            }
        })
        .catch(error => {
            messageDiv.textContent = '비밀번호 변경 중 오류가 발생했습니다.';
            messageDiv.className = 'form-message error-message';
        });
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

// 🆕 장바구니로 이동
function goToCart() {
    window.location.href = '/cart';
}

// 네비게이션 함수들
function goToLikedProducts() {
    window.location.href = '/liked-products';
}

function goToOrderHistory() {
    window.location.href = '/order-history';
}

function goToCancelHistory() {
    window.location.href = '/cancel-history';
}

// 🆕 내가 쓴 리뷰 페이지로 이동
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
