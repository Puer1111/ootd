// 토큰 관리
const AuthManager = {
    getToken: function() {
        return localStorage.getItem('token');
    },

    removeToken: function() {
        localStorage.removeItem('token');
        localStorage.removeItem('auth_token');
    },

    redirectToLogin: function() {
        window.location.href = '/login';
    }
};

document.addEventListener('DOMContentLoaded', function() {
    loadLikedProducts();
});

function loadLikedProducts() {
    const token = AuthManager.getToken();

    if (!token) {
        AuthManager.redirectToLogin();
        return;
    }

    // 로딩 상태 표시
    showLoadingState();

    fetch('/api/auth/liked-products', {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        }
    })
        .then(response => {
            if (!response.ok) {
                if (response.status === 401) {
                    throw new Error('UNAUTHORIZED');
                }
                throw new Error('Network response was not ok');
            }
            return response.json();
        })
        .then(data => {
            console.log('좋아요 상품 데이터:', data);

            if (data.success) {
                displayProducts(data.likedProducts, data.totalCount);
            } else {
                showErrorState();
            }
        })
        .catch(error => {
            console.error('좋아요 상품 로드 에러:', error);

            if (error.message === 'UNAUTHORIZED') {
                alert('로그인이 만료되었습니다. 다시 로그인해주세요.');
                AuthManager.removeToken();
                AuthManager.redirectToLogin();
            } else {
                showErrorState();
            }
        });
}

function showLoadingState() {
    document.getElementById('loading-state').style.display = 'block';
    document.getElementById('products-container').style.display = 'none';
    document.getElementById('empty-state').style.display = 'none';
    document.getElementById('error-state').style.display = 'none';
}

function showErrorState() {
    document.getElementById('loading-state').style.display = 'none';
    document.getElementById('products-container').style.display = 'none';
    document.getElementById('empty-state').style.display = 'none';
    document.getElementById('error-state').style.display = 'block';
}

function displayProducts(products, totalCount) {
    // 총 개수 업데이트
    document.getElementById('total-count').textContent = totalCount;

    // 로딩 상태 숨기기
    document.getElementById('loading-state').style.display = 'none';

    if (!products || products.length === 0) {
        // 빈 상태 표시
        document.getElementById('empty-state').style.display = 'block';
        document.getElementById('products-container').style.display = 'none';
        return;
    }

    // 상품 목록 표시
    const productsGrid = document.getElementById('products-grid');
    productsGrid.innerHTML = '';

    products.forEach(product => {
        const productCard = createProductCard(product);
        productsGrid.appendChild(productCard);
    });

    document.getElementById('products-container').style.display = 'block';
    document.getElementById('empty-state').style.display = 'none';
}

function createProductCard(product) {
    const card = document.createElement('div');
    card.className = 'product-card';
    card.setAttribute('data-product-no', product.productNo);

    // 이미지 URL 처리
    const imageUrl = (product.imageUrls && product.imageUrls.length > 0)
        ? product.imageUrls[0]
        : '/images/no-image.png';

    // 가격 포맷팅
    const formattedPrice = new Intl.NumberFormat('ko-KR').format(product.price);

    card.innerHTML = `
        <button class="unlike-btn" onclick="toggleLike(event, ${product.productNo})">
            <span class="heart">♥</span>
        </button>
        
        <img src="${imageUrl}" 
             alt="${product.productName}" 
             class="product-image"
             onerror="this.src='/images/no-image.png'">
        
        <div class="product-info">
            <div class="product-brand">${product.brandName || '브랜드명'}</div>
            <div class="product-name">${product.productName}</div>
            <div class="product-category">${product.subCategory || '카테고리'}</div>
            <div class="product-price">${formattedPrice}원</div>
            
            <div class="product-stats">
                <div class="stat-item">
                    <span class="heart-icon">♥</span>
                    <span>${product.likeCount || 0}</span>
                </div>
                <div class="stat-item">
                    <span class="star-icon">★</span>
                    <span>리뷰 ${product.reviewCount || 0}개</span>
                </div>
            </div>
        </div>
    `;

    // 카드 클릭 이벤트 (좋아요 버튼 제외)
    card.addEventListener('click', function(e) {
        if (!e.target.closest('.unlike-btn')) {
            goToProduct(product.productNo);
        }
    });

    return card;
}

function goToProduct(productNo) {
    window.location.href = `/products/${productNo}`;
}

function toggleLike(event, productNo) {
    event.stopPropagation(); // 카드 클릭 이벤트 방지

    const token = AuthManager.getToken();

    if (!token) {
        alert('로그인이 필요합니다.');
        AuthManager.redirectToLogin();
        return;
    }

    const button = event.currentTarget;

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
                if (!data.isLiked) {
                    // 좋아요 취소됨 - 카드 제거
                    const productCard = button.closest('.product-card');
                    productCard.style.opacity = '0';
                    productCard.style.transform = 'scale(0.9)';

                    setTimeout(() => {
                        productCard.remove();
                        updateTotalCount();
                    }, 300);

                    // 모든 카드가 제거되었는지 확인
                    setTimeout(() => {
                        const remainingCards = document.querySelectorAll('.product-card');
                        if (remainingCards.length === 0) {
                            document.getElementById('products-container').style.display = 'none';
                            document.getElementById('empty-state').style.display = 'block';
                        }
                    }, 400);
                }
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

function updateTotalCount() {
    const remainingCards = document.querySelectorAll('.product-card');
    document.getElementById('total-count').textContent = remainingCards.length;
}