// 토큰 관리
const AuthManager = {
    getToken: function() {
        return localStorage.getItem('token') || sessionStorage.getItem('token');
    },

    isLoggedIn: function() {
        return this.getToken() !== null;
    },

    removeToken: function() {
        localStorage.removeItem('token');
        sessionStorage.removeItem('token');
    }
};

// 전역 상태
let userLikedProducts = new Set();
let saleProducts = [];

document.addEventListener('DOMContentLoaded', function() {
    console.log('🔥 세일 상품 페이지 로드');
    loadUserLikedProducts();
    loadSaleProducts();
});

// 사용자 좋아요 목록 로드
async function loadUserLikedProducts() {
    if (!AuthManager.isLoggedIn()) {
        console.log('💡 비로그인 상태 - 좋아요 목록 로드 스킵');
        return;
    }

    try {
        const token = AuthManager.getToken();
        const response = await fetch('/api/auth/liked-products', {
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (response.ok) {
            const data = await response.json();
            if (data.success && data.likedProducts) {
                userLikedProducts = new Set(
                    data.likedProducts.map(p => p.productNo.toString())
                );
                console.log('💖 좋아요 목록 로드 완료');
            }
        }
    } catch (error) {
        console.log('⚠️ 좋아요 목록 로드 실패:', error);
    }
}

// 세일 상품 목록 로드
async function loadSaleProducts() {
    showLoading(true);

    try {
        console.log('🔥 세일 상품 API 호출 시작');
        const response = await fetch('/api/products/sale');
        const data = await response.json();

        console.log('📡 세일 상품 API 응답:', data);

        if (data.success && data.products) {
            saleProducts = data.products;
            renderProducts();
            updateStats();

            console.log(`✅ 세일 상품 ${saleProducts.length}개 로드 완료`);
        } else {
            console.error('❌ 세일 상품 로드 실패:', data.message);
            showEmptyState();
        }
    } catch (error) {
        console.error('❌ 세일 상품 로드 에러:', error);
        showEmptyState();
    } finally {
        showLoading(false);
    }
}

// 상품 목록 렌더링
function renderProducts() {
    const productsGrid = document.getElementById('products-grid');
    const noProductsDiv = document.getElementById('no-products');

    if (saleProducts.length === 0) {
        productsGrid.innerHTML = '';
        noProductsDiv.style.display = 'block';
        return;
    }

    noProductsDiv.style.display = 'none';

    let html = '';
    saleProducts.forEach(product => {
        const isLiked = userLikedProducts.has(product.productNo.toString());
        const imageUrl = product.imageUrls && product.imageUrls.length > 0
            ? product.imageUrls[0]
            : '/img/common/no-image.png';

        // 세일 정보 계산
        const originalPrice = product.price;
        const salePrice = product.salePrice || product.price;
        const salePercentage = product.salePercentage || 0;
        const savings = originalPrice - salePrice;

        html += `
            <div class="card promotion-card" data-product-no="${product.productNo}" onclick="goToProduct(this)">
                <!-- 세일 배지 -->
                <div class="product-badge sale">
                    <span>🔥 세일</span>
                </div>

                <!-- 할인율 -->
                ${salePercentage > 0 ? `
                    <div class="discount-info">
                        ${salePercentage}% OFF
                    </div>
                ` : ''}

                <!-- 좋아요 버튼 -->
                <button class="like-btn-card ${isLiked ? 'liked' : ''}" 
                        data-product-no="${product.productNo}" 
                        onclick="toggleLikeFromMain(event, this)">
                    <span class="heart">${isLiked ? '♥' : '♡'}</span>
                </button>

                <div class="card-image">
                    <img src="${imageUrl}" alt="${product.productName}">
                </div>

                <div class="card-content">
                    <div class="card-subtitle">${product.brandName || '브랜드명'}</div>
                    <div class="card-title">${product.productName}</div>
                    <div class="card-tag">${product.subCategory || '카테고리'}</div>
                    
                    <div class="card-price sale-price">
                        ${salePrice < originalPrice ? `
                            <span class="original-price">${originalPrice.toLocaleString()}원</span>
                            <span class="current-price">${salePrice.toLocaleString()}원</span>
                            ${savings > 0 ? `<span class="savings-amount">${savings.toLocaleString()}원 할인</span>` : ''}
                        ` : `
                            <span class="current-price">${originalPrice.toLocaleString()}원</span>
                        `}
                    </div>

                    <div class="card-stats">
                        <div class="stat-item">
                            <span class="icon heart-icon">♥</span>
                            <span>${product.likeCount}</span>
                        </div>
                        <div class="stat-item">
                            <span class="icon star-icon">★</span>
                            <span>리뷰 ${product.reviewCount}개</span>
                        </div>
                    </div>
                </div>
            </div>
        `;
    });

    productsGrid.innerHTML = html;
}

// 통계 정보 업데이트
function updateStats() {
    const saleCountElement = document.getElementById('sale-count');
    const avgDiscountElement = document.getElementById('avg-discount');
    const totalSavingsElement = document.getElementById('total-savings');

    if (saleCountElement) {
        saleCountElement.textContent = saleProducts.length;
    }

    if (saleProducts.length > 0) {
        // 평균 할인율 계산
        const productsWithDiscount = saleProducts.filter(p => p.salePercentage > 0);
        const avgDiscount = productsWithDiscount.length > 0
            ? (productsWithDiscount.reduce((sum, p) => sum + p.salePercentage, 0) / productsWithDiscount.length)
            : 0;

        // 최대 할인 금액
        const maxSavings = Math.max(...saleProducts.map(p => {
            const savings = p.price - (p.salePrice || p.price);
            return savings > 0 ? savings : 0;
        }));

        if (avgDiscountElement) {
            avgDiscountElement.textContent = Math.round(avgDiscount) + '%';
        }

        if (totalSavingsElement) {
            totalSavingsElement.textContent = maxSavings.toLocaleString() + '원';
        }
    }
}

// 좋아요 토글
async function toggleLikeFromMain(event, button) {
    event.stopPropagation();

    if (!AuthManager.isLoggedIn()) {
        alert('로그인이 필요합니다.');
        window.location.href = '/login';
        return;
    }

    const productNo = button.dataset.productNo;
    const token = AuthManager.getToken();
    const heart = button.querySelector('.heart');

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

        if (response.status === 401) {
            alert('로그인이 만료되었습니다. 다시 로그인해주세요.');
            AuthManager.removeToken();
            window.location.href = '/login';
            return;
        }

        if (response.ok) {
            const data = await response.json();

            if (data.isLiked) {
                userLikedProducts.add(productNo);
                button.classList.add('liked');
                heart.textContent = '♥';
            } else {
                userLikedProducts.delete(productNo);
                button.classList.remove('liked');
                heart.textContent = '♡';
            }

            // 좋아요 수 업데이트
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

// 상품 상세 페이지로 이동
function goToProduct(element) {
    const productNo = element.getAttribute('data-product-no');
    if (productNo) {
        console.log('🔗 상품 상세로 이동:', productNo);
        location.href = '/products/' + productNo;
    }
}

// 로딩 상태 표시
function showLoading(show) {
    const loadingState = document.getElementById('loading-state');
    const productsGrid = document.getElementById('products-grid');

    if (show) {
        loadingState.style.display = 'block';
        productsGrid.style.display = 'none';
    } else {
        loadingState.style.display = 'none';
        productsGrid.style.display = 'grid';
    }
}

// 빈 상태 표시
function showEmptyState() {
    const productsGrid = document.getElementById('products-grid');
    const noProductsDiv = document.getElementById('no-products');

    productsGrid.innerHTML = '';
    productsGrid.style.display = 'none';
    noProductsDiv.style.display = 'block';
}