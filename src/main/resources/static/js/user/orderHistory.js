// 토큰 관리
const AuthManager = {
    getToken: function() {
        return localStorage.getItem('token');
    },

    isLoggedIn: function() {
        return this.getToken() !== null;
    },

    removeToken: function() {
        localStorage.removeItem('token');
        sessionStorage.removeItem('token');
    }
};

document.addEventListener('DOMContentLoaded', function() {
    checkAuthAndLoadHistory();
});

function checkAuthAndLoadHistory() {
    if (!AuthManager.isLoggedIn()) {
        alert('로그인이 필요합니다.');
        window.location.href = '/login';
        return;
    }

    loadOrderHistory();
}

async function loadOrderHistory() {
    const loadingState = document.getElementById('loading-state');
    const productsContainer = document.getElementById('products-container');
    const emptyState = document.getElementById('empty-state');
    const errorState = document.getElementById('error-state');

    // 로딩 표시
    loadingState.style.display = 'block';
    productsContainer.style.display = 'none';
    emptyState.style.display = 'none';
    errorState.style.display = 'none';

    try {
        const token = AuthManager.getToken();
        const response = await fetch('/api/auth/order-history', {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
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
            console.log('주문 내역 데이터:', data);

            if (data.success && data.orderedProducts) {
                displayOrderHistory(data.orderedProducts, data.totalCount || 0);
            } else {
                showEmptyState();
            }
        } else {
            throw new Error('Failed to load order history');
        }

    } catch (error) {
        console.error('주문 내역 로드 실패:', error);
        showErrorState();
    } finally {
        loadingState.style.display = 'none';
    }
}

// 주문 내역 표시 함수
function displayOrderHistory(orders, totalCount) {
    const productsContainer = document.getElementById('products-container');
    const productsGrid = document.getElementById('products-grid');
    const totalCountElement = document.getElementById('total-count');

    if (orders.length === 0) {
        showEmptyState();
        return;
    }

    // 총 개수 업데이트
    if (totalCountElement) {
        totalCountElement.textContent = totalCount;
    }

    // 주문 목록 HTML 생성
    const ordersHtml = orders.map(order => `
        <div class="product-card" data-product-no="${order.productNo}">
            <div class="product-image" onclick="goToProduct(${order.productNo})">
                ${order.imageUrls && order.imageUrls.length > 0
        ? `<img src="${order.imageUrls[0]}" alt="${order.productName}">`
        : '<div class="no-image">이미지 없음</div>'
    }
            </div>
            
            <div class="product-info">
                <div class="product-brand">브랜드: ${order.brandName || 'OOTD'}</div>
                <div class="product-name" onclick="goToProduct(${order.productNo})">${order.productName}</div>
                <div class="product-category">카테고리: ${order.categoryName || '패션'} > ${order.subCategory || '일반'}</div>
                
                <!-- 주문 정보 섹션 -->
                <div class="order-summary">
                    <div class="order-main-info">
                        <div class="quantity-price">
                            <span class="quantity-badge">${order.quantity || 1}개 주문</span>
                            <span class="total-amount">${(order.totalPrice || (order.price * (order.quantity || 1))).toLocaleString()}원</span>
                        </div>
                        <div class="unit-price">단가: ${(order.price || 0).toLocaleString()}원</div>
                    </div>
                    <div class="order-date-info">
                        <span class="order-date">${order.orderDate ? formatDate(order.orderDate) : '정보 없음'}</span>
                        <span class="order-status-badge">${order.orderStatus || '주문완료'}</span>
                    </div>
                </div>
                
                <div class="product-actions">
                    <button class="btn btn-outline" onclick="goToProduct(${order.productNo})">상품 보기</button>
                    <button class="btn btn-danger" onclick="cancelOrderFromHistory(${order.orderId || order.productNo})">주문 취소</button>
                </div>
            </div>
        </div>
    `).join('');

    productsGrid.innerHTML = ordersHtml;
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

async function cancelOrderFromHistory(orderId) {
    if (!confirm('정말로 주문을 취소하시겠습니까?')) {
        return;
    }

    try {
        const token = AuthManager.getToken();
        const response = await fetch(`/api/auth/cancel-order/${orderId}`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        const data = await response.json();

        if (response.ok && data.success) {
            alert(data.message || '주문이 취소되었습니다.');
            // 페이지 새로고침하여 업데이트된 목록 표시
            loadOrderHistory();
        } else {
            alert(data.message || '주문 취소에 실패했습니다.');
        }

    } catch (error) {
        console.error('주문 취소 실패:', error);
        alert('오류가 발생했습니다. 다시 시도해주세요.');
    }
}