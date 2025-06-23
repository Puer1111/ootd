// 토큰 관리
const AuthManager = {
    getToken: function() {
        return localStorage.getItem('token');
    },

    isLoggedIn: function() {
        return this.getToken() !== null;
    }
};

let showLikedOnly = false;
let userLikedProducts = new Set(); // 사용자가 좋아요한 상품 ID들

document.addEventListener('DOMContentLoaded', function() {
    loadUserLikedProducts();
});

// 사용자가 좋아요한 상품들 불러오기
function loadUserLikedProducts() {
    if (!AuthManager.isLoggedIn()) {
        return;
    }

    const token = AuthManager.getToken();

    fetch('/api/auth/liked-products', {
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
            throw new Error('Failed to load liked products');
        })
        .then(data => {
            if (data.success && data.likedProducts) {
                // 좋아요한 상품 ID들을 Set에 저장
                userLikedProducts = new Set(data.likedProducts.map(p => p.productNo.toString()));
                updateLikeButtons();
            }
        })
        .catch(error => {
            console.log('좋아요 상품 로드 실패 (로그인 안 됨 또는 오류):', error);
        });
}

// 좋아요 버튼 상태 업데이트
function updateLikeButtons() {
    const likeButtons = document.querySelectorAll('.like-btn-card');

    likeButtons.forEach(button => {
        const productNo = button.dataset.productNo;
        const heart = button.querySelector('.heart');

        if (userLikedProducts.has(productNo)) {
            button.classList.add('liked');
            heart.textContent = '♥';
        } else {
            button.classList.remove('liked');
            heart.textContent = '♡';
        }
    });
}

// 메인 페이지에서 좋아요 토글
function toggleLikeFromMain(event, button) {
    event.stopPropagation(); // 카드 클릭 이벤트 방지

    if (!AuthManager.isLoggedIn()) {
        alert('로그인이 필요합니다.');
        window.location.href = '/login';
        return;
    }

    const productNo = button.dataset.productNo;
    const token = AuthManager.getToken();
    const heart = button.querySelector('.heart');

    // 버튼 비활성화 (중복 클릭 방지)
    button.disabled = true;

    fetch(`/products/${productNo}/like`, {
        method: 'POST',
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        }
    })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                if (data.isLiked) {
                    // 좋아요 추가
                    userLikedProducts.add(productNo);
                    button.classList.add('liked');
                    heart.textContent = '♥';
                } else {
                    // 좋아요 취소
                    userLikedProducts.delete(productNo);
                    button.classList.remove('liked');
                    heart.textContent = '♡';

                    // 좋아요 필터링 중이고 좋아요가 취소되면 카드 숨기기
                    if (showLikedOnly) {
                        const card = button.closest('.card');
                        card.classList.add('hidden');
                        checkIfNoLikedProducts();
                    }
                }

                // 좋아요 수 업데이트 (카드의 통계 부분)
                const card = button.closest('.card');
                const likeCountSpan = card.querySelector('.heart-icon').nextElementSibling;
                likeCountSpan.textContent = data.likeCount;

            } else {
                alert(data.message || '오류가 발생했습니다.');
            }
        })
        .catch(error => {
            console.error('좋아요 토글 에러:', error);
            alert('오류가 발생했습니다. 다시 시도해주세요.');
        })
        .finally(() => {
            button.disabled = false;
        });
}

// 좋아요 필터 토글
function toggleLikedFilter() {
    if (!AuthManager.isLoggedIn()) {
        alert('로그인이 필요합니다.');
        window.location.href = '/login';
        return;
    }

    showLikedOnly = !showLikedOnly;
    const filterBtn = document.getElementById('filter-liked-btn');
    const cards = document.querySelectorAll('.card');
    const noLikedDiv = document.getElementById('no-liked-products');

    if (showLikedOnly) {
        // 좋아요만 보기 모드
        filterBtn.classList.add('active');
        filterBtn.innerHTML = '<span class="heart-icon">♥</span> 전체 보기';

        let hasLikedProducts = false;

        cards.forEach(card => {
            const productNo = card.dataset.productNo;
            if (userLikedProducts.has(productNo)) {
                card.classList.remove('hidden');
                hasLikedProducts = true;
            } else {
                card.classList.add('hidden');
            }
        });

        // 좋아요한 상품이 없으면 메시지 표시
        if (!hasLikedProducts) {
            noLikedDiv.style.display = 'block';
        } else {
            noLikedDiv.style.display = 'none';
        }

    } else {
        // 전체 보기 모드
        filterBtn.classList.remove('active');
        filterBtn.innerHTML = '<span class="heart-icon">♥</span> 좋아요만 보기';

        cards.forEach(card => {
            card.classList.remove('hidden');
        });

        noLikedDiv.style.display = 'none';
    }
}

// 좋아요한 상품이 없는지 확인
function checkIfNoLikedProducts() {
    if (!showLikedOnly) return;

    const visibleCards = document.querySelectorAll('.card:not(.hidden)');
    const noLikedDiv = document.getElementById('no-liked-products');

    if (visibleCards.length === 0) {
        noLikedDiv.style.display = 'block';
    } else {
        noLikedDiv.style.display = 'none';
    }
}

// 기존 goToProduct 함수 (상품 상세 페이지로 이동)
function goToProduct(element) {
    const productNo = element.getAttribute('data-product-no');
    if (productNo) {
        location.href = '/products/' + productNo;
    }
}