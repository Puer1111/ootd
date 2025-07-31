const AuthManager = {
    getToken: function() {
        return localStorage.getItem('auth_token') || sessionStorage.getItem('auth_token');
    },

    isLoggedIn: function() {
        return this.getToken() !== null;
    },

    removeToken: function() {
        localStorage.removeItem('token');
        sessionStorage.removeItem('token');
    }
};

let currentState = {
    sortOrder: 'reviews',
    mainCategory: 'all',
    subCategory: 'all',
    showLikedOnly: false,
    allProducts: [],
    filteredProducts: [],
    userLikedProducts: new Set()
};

document.addEventListener('DOMContentLoaded', function() {
    console.log('🚀 메인페이지 로드 시작');
    checkLoginStatus();
    loadUserLikedProducts();
    updateLikeButtons();
});

function checkLoginStatus() {
    const token = AuthManager.getToken();
    const notLoggedIn = document.getElementById('not-logged-in');
    const loggedIn = document.getElementById('logged-in');

    console.log('🔍 로그인 상태 확인:', token ? '로그인됨' : '비로그인');

    if (token) {
        if (notLoggedIn) notLoggedIn.style.display = 'none';
        if (loggedIn) loggedIn.style.display = 'flex';
        getUserInfo(token);
    } else {
        if (notLoggedIn) notLoggedIn.style.display = 'flex';
        if (loggedIn) loggedIn.style.display = 'none';
    }
}

async function getUserInfo(token) {
    try {
        const response = await fetch('/api/auth/mypage', {
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (response.ok) {
            const data = await response.json();
            const usernameSpan = document.getElementById('username');
            if (usernameSpan && data.user) {
                usernameSpan.textContent = data.user.name || data.user.username || '사용자';
            }
        } else if (response.status === 401) {
            console.log('⚠️ 토큰 만료로 로그아웃 처리');
            AuthManager.removeToken();
            checkLoginStatus();
        }
    } catch (error) {
        console.error('❌ 사용자 정보 로드 실패:', error);
    }
}

function logout() {
    if (confirm('로그아웃 하시겠습니까?')) {
        AuthManager.removeToken();
        alert('로그아웃되었습니다.');
        checkLoginStatus();
        currentState.userLikedProducts.clear();
        updateLikeButtons();
        if (currentState.showLikedOnly) {
            toggleLikedFilter();
        }
    }
}

async function loadUserLikedProducts() {
    if (!AuthManager.isLoggedIn()) {
        console.log('💡 비로그인 상태 - 좋아요 목록 로드 스킵');
        return;
    }

    const token = AuthManager.getToken();
    console.log('💖 좋아요 상품 목록 로드 시작');

    try {
        const response = await fetch('/api/auth/liked-products', {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (response.ok) {
            const data = await response.json();
            console.log('✅ 좋아요 상품 목록 로드 성공:', data);

            if (data.success && data.likedProducts) {
                currentState.userLikedProducts = new Set(
                    data.likedProducts.map(p => p.productNo.toString())
                );
                console.log('💖 사용자 좋아요 상품 ID들:', currentState.userLikedProducts);
                updateLikeButtons();
            }
        } else if (response.status === 401) {
            console.log('⚠️ 토큰 만료로 좋아요 목록 로드 실패');
            AuthManager.removeToken();
            checkLoginStatus();
        }
    } catch (error) {
        console.log('⚠️ 좋아요 상품 로드 실패 (네트워크 오류):', error);
    }
}

function updateLikeButtons() {
    const likeButtons = document.querySelectorAll('.like-btn-card');
    console.log('🔄 좋아요 버튼 상태 업데이트 시작. 버튼 개수:', likeButtons.length);

    likeButtons.forEach(button => {
        const productNo = button.dataset.productNo;
        const heart = button.querySelector('.heart');

        if (currentState.userLikedProducts.has(productNo)) {
            button.classList.add('liked');
            heart.textContent = '♥';
            console.log(`💖 상품 ${productNo}: 좋아요됨`);
        } else {
            button.classList.remove('liked');
            heart.textContent = '♡';
            console.log(`🤍 상품 ${productNo}: 좋아요 안됨`);
        }
    });
}

async function toggleLikeFromMain(event, button) {
    event.stopPropagation();

    console.log('💖 좋아요 버튼 클릭됨!');

    if (!AuthManager.isLoggedIn()) {
        alert('로그인이 필요합니다.');
        window.location.href = '/login';
        return;
    }

    const productNo = button.dataset.productNo;
    const token = AuthManager.getToken();
    const heart = button.querySelector('.heart');

    console.log('💖 좋아요 토글 시작 - 상품번호:', productNo);

    button.disabled = true;
    button.style.opacity = '0.5';

    try {
        const response = await fetch(`/products/${productNo}/like`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        console.log('📡 좋아요 API 응답 상태:', response.status);

        if (response.status === 401) {
            alert('로그인이 만료되었습니다. 다시 로그인해주세요.');
            AuthManager.removeToken();
            checkLoginStatus();
            return;
        }

        if (response.ok) {
            const data = await response.json();
            console.log('✅ 좋아요 토글 성공:', data);

            if (data.isLiked) {
                currentState.userLikedProducts.add(productNo);
                button.classList.add('liked');
                heart.textContent = '♥';
                console.log(`💖 상품 ${productNo}: 좋아요 추가됨`);
            } else {
                currentState.userLikedProducts.delete(productNo);
                button.classList.remove('liked');
                heart.textContent = '♡';
                console.log(`🤍 상품 ${productNo}: 좋아요 취소됨`);

                if (currentState.showLikedOnly) {
                    const card = button.closest('.card');
                    if (card) {
                        card.style.display = 'none';
                    }
                }
            }

            const card = button.closest('.card');
            const likeCountElement = card.querySelector('.stat-item .heart-icon + span');
            if (likeCountElement) {
                likeCountElement.textContent = data.likeCount;
            }

        } else {
            const errorData = await response.json();
            console.error('❌ 좋아요 토글 실패:', errorData);
            alert(errorData.message || '오류가 발생했습니다.');
        }
    } catch (error) {
        console.error('❌ 좋아요 토글 네트워크 에러:', error);
        alert('네트워크 오류가 발생했습니다. 다시 시도해주세요.');
    } finally {
        button.disabled = false;
        button.style.opacity = '1';
    }
}

function toggleLikedFilter() {
    if (!AuthManager.isLoggedIn()) {
        alert('로그인이 필요합니다.');
        window.location.href = '/login';
        return;
    }

    currentState.showLikedOnly = !currentState.showLikedOnly;
    const filterBtn = document.getElementById('filter-liked-btn');
    const allCards = document.querySelectorAll('.card');
    const noLikedDiv = document.getElementById('no-liked-products');

    console.log('🔍 좋아요 필터 토글:', currentState.showLikedOnly ? '좋아요만 보기' : '전체 보기');

    if (currentState.showLikedOnly) {
        filterBtn.classList.add('active');
        filterBtn.innerHTML = '<span class="heart-icon">♥</span> 전체 보기';

        let visibleCount = 0;
        allCards.forEach(card => {
            const productNo = card.dataset.productNo;
            if (currentState.userLikedProducts.has(productNo)) {
                card.style.display = 'block';
                visibleCount++;
            } else {
                card.style.display = 'none';
            }
        });

        if (visibleCount === 0) {
            noLikedDiv.style.display = 'block';
        } else {
            noLikedDiv.style.display = 'none';
        }
    } else {
        filterBtn.classList.remove('active');
        filterBtn.innerHTML = '<span class="heart-icon">♥</span> 좋아요만 보기';

        allCards.forEach(card => {
            card.style.display = 'block';
        });
        noLikedDiv.style.display = 'none';
    }
}

function goToProduct(element) {
    const productNo = element.getAttribute('data-product-no');
    if (productNo) {
        console.log('🔗 상품 상세로 이동:', productNo);
        location.href = '/products/' + productNo;
    }
}

function filterByCategory(category) {
    console.log('📁 카테고리 필터:', category);
}

function filterBySubCategory(subCategory) {
    console.log('📂 하위 카테고리 필터:', subCategory);
}

function changeSortOrder(sortOrder) {
    console.log('🔄 정렬 변경:', sortOrder);
}

document.addEventListener('DOMContentLoaded', () => {
    const carousels = document.querySelectorAll('.carousel-container');

    carousels.forEach(carousel => {
        const productsGrid = carousel.querySelector('.products-grid');
        const prevBtn = carousel.querySelector('.prev');
        const nextBtn = carousel.querySelector('.next');

        if (!productsGrid || !prevBtn || !nextBtn) {
            return;
        }

        let currentIndex = 0;
        const totalCards = productsGrid.querySelectorAll('.card').length;

        function getCardsToShow() {
            const containerWidth = carousel.offsetWidth;
            const cardWidth = 250 + 32;
            return Math.floor(containerWidth / cardWidth);
        }

        function updateCarousel() {
            const cardsToShow = getCardsToShow();
            const cardElement = productsGrid.querySelector('.card');
            if (!cardElement) return;

            const cardWidth = cardElement.offsetWidth;
            const cardMargin = parseInt(window.getComputedStyle(cardElement).marginRight);
            const totalCardWidth = cardWidth + cardMargin;

            const newTransform = -currentIndex * totalCardWidth;
            productsGrid.style.transform = `translateX(${newTransform}px)`;

            prevBtn.disabled = currentIndex === 0;
            nextBtn.disabled = currentIndex >= totalCards - cardsToShow;
        }

        prevBtn.addEventListener('click', () => {
            if (currentIndex > 0) {
                currentIndex--;
                updateCarousel();
            }
        });

        nextBtn.addEventListener('click', () => {
            const cardsToShow = getCardsToShow();
            if (currentIndex < totalCards - cardsToShow) {
                currentIndex++;
                updateCarousel();
            }
        });

        window.addEventListener('resize', updateCarousel);

        if (totalCards > 0) {
            updateCarousel();
        }
    });
});