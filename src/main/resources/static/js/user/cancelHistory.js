const AuthManager = {
    getToken: function() {
        return localStorage.getItem('auth_token') || localStorage.getItem('token');
    },

    isLoggedIn: function() {
        return this.getToken() !== null;
    },

    removeToken: function() {
        localStorage.removeItem('auth_token');
        sessionStorage.removeItem('auth_token');
        localStorage.removeItem('token');
        sessionStorage.removeItem('token');
    },

    redirectToLogin: function() {
        window.location.href = '/login';
    }
};

document.addEventListener('DOMContentLoaded', function() {
    if (!AuthManager.isLoggedIn()) {
        alert('로그인이 필요합니다.');
        window.location.href = '/login';
        return;
    }
    loadCancelHistory();
});

async function loadCancelHistory() {
    const loadingState = document.getElementById('loading-state');
    const productsContainer = document.getElementById('products-container');
    const emptyState = document.getElementById('empty-state');
    const errorState = document.getElementById('error-state');

    loadingState.style.display = 'block';
    productsContainer.style.display = 'none';
    emptyState.style.display = 'none';
    errorState.style.display = 'none';

    try {
        const token = AuthManager.getToken(); // ← token 변수에 저장
        console.log("📋 취소 내역 조회 시작");

        const response = await fetch('/api/auth/cancel-history', {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`, // ← token 변수 사용!
                'Content-Type': 'application/json'
            }
        });

        if (response.status === 401) {
            AuthManager.removeToken();
            alert('로그인이 만료되었습니다.');
            window.location.href = '/login';
            return;
        }

        if (response.ok) {
            const data = await response.json();
            console.log('📋 취소 내역 데이터:', data);

            if (data.success && data.cancelledProducts) {
                displayCancelHistory(data.cancelledProducts, data.totalCount || 0);
            } else {
                showEmptyState();
            }
        } else {
            throw new Error('Failed to load cancel history');
        }

    } catch (error) {
        console.error('❌ 취소 내역 로드 실패:', error);
        showErrorState();
    } finally {
        loadingState.style.display = 'none';
    }
}

function displayCancelHistory(cancelledOrders, totalCount) {
    const productsContainer = document.getElementById('products-container');
    const productsGrid = document.getElementById('products-grid');
    const totalCountElement = document.getElementById('total-count');

    if (cancelledOrders.length === 0) {
        showEmptyState();
        return;
    }

    if (totalCountElement) {
        totalCountElement.textContent = totalCount;
    }

    const cancelledHtml = cancelledOrders.map(order => `
        <div class="product-card">
            <div class="product-image">
                ${order.imageUrls && order.imageUrls.length > 0
        ? `<img src="${order.imageUrls[0]}" alt="${order.productName}">`
        : '<div class="no-image">이미지 없음</div>'
    }
            </div>
            
            <div class="product-info">
                <div class="product-brand">브랜드: ${order.brandName || 'OOTD'}</div>
                <div class="product-name">${order.productName}</div>
                <div class="product-category">카테고리: ${order.categoryName || '패션'} > ${order.subCategory || '일반'}</div>
                
                <div class="order-summary" style="background: #ff6b6b;">
                    <div class="order-main-info">
                        <div class="quantity-price">
                            <span class="quantity-badge">취소된 상품</span>
                            <span class="total-amount">${order.totalPrice.toLocaleString()}원</span>
                        </div>
                        <div class="unit-price">단가: ${order.price.toLocaleString()}원</div>
                    </div>
                    <div class="order-date-info">
                        <span class="order-date">${formatDate(order.orderDate)}</span>
                        <span class="order-status-badge" style="background: white; color: #ff6b6b;">취소됨</span>
                    </div>
                </div>
            </div>
        </div>
    `).join('');

    productsGrid.innerHTML = cancelledHtml;
    productsContainer.style.display = 'block';
}

function formatDate(dateString) {
    const date = new Date(dateString);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}.${month}.${day}`;
}

function showEmptyState() {
    document.getElementById('empty-state').style.display = 'block';
}

function showErrorState() {
    document.getElementById('error-state').style.display = 'block';
}

function goToProduct(productNo) {
    window.location.href = `/products/${productNo}`;
}