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
    checkAuthAndLoadReviews();
});

function checkAuthAndLoadReviews() {
    if (!AuthManager.isLoggedIn()) {
        alert('로그인이 필요합니다.');
        window.location.href = '/login';
        return;
    }

    loadMyReviews();
}

async function loadMyReviews() {
    const loadingState = document.getElementById('loading-state');
    const reviewsContainer = document.getElementById('reviews-container');
    const emptyState = document.getElementById('empty-state');
    const errorState = document.getElementById('error-state');

    // 로딩 표시
    loadingState.style.display = 'block';
    reviewsContainer.style.display = 'none';
    emptyState.style.display = 'none';
    errorState.style.display = 'none';

    try {
        const token = AuthManager.getToken();
        // 컨트롤러 경로와 일치하도록 수정
        const response = await fetch('/api/reviews/my-reviews-data', {
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
            console.log('내 리뷰 데이터:', data);

            if (data.success && data.reviews) {
                displayMyReviews(data.reviews, data.totalCount || 0);
            } else {
                showEmptyState();
            }
        } else {
            throw new Error('Failed to load my reviews');
        }

    } catch (error) {
        console.error('리뷰 로드 실패:', error);
        showErrorState();
    } finally {
        loadingState.style.display = 'none';
    }
}

function displayMyReviews(reviews, totalCount) {
    const reviewsContainer = document.getElementById('reviews-container');
    const reviewsGrid = document.getElementById('reviews-grid');
    const totalCountElement = document.getElementById('total-count');

    if (reviews.length === 0) {
        showEmptyState();
        return;
    }

    // 총 개수 업데이트
    if (totalCountElement) {
        totalCountElement.textContent = totalCount;
    }

    // 리뷰 목록 HTML 생성
    const reviewsHtml = reviews.map(review => `
        <div class="review-card" data-product-no="${review.productNo}">
            <div class="review-header">
                <div class="product-info">
                    ${review.productImageUrls && review.productImageUrls.length > 0
        ? `<img src="${review.productImageUrls[0]}" alt="${review.productName}" class="product-thumb">`
        : '<div class="no-image-thumb">이미지 없음</div>'
    }
                    <div class="product-details">
                        <div class="product-name" onclick="goToProduct(${review.productNo})">${review.productName}</div>
                        <div class="review-date">${formatDate(review.createdAt)}</div>
                    </div>
                </div>
                <div class="review-rating">
                    ${'★'.repeat(review.rating)}${'☆'.repeat(5 - review.rating)}
                    <span class="rating-number">${review.rating}/5</span>
                </div>
            </div>
            
            <div class="review-content">
                <p>${escapeHtml(review.content)}</p>
            </div>
            
            <div class="review-actions">
                <button class="btn btn-outline" onclick="goToProduct(${review.productNo})">상품 보기</button>
                <button class="btn btn-danger" onclick="deleteReview(${review.id}, '${review.productName}')">리뷰 삭제</button>
            </div>
        </div>
    `).join('');

    reviewsGrid.innerHTML = reviewsHtml;
    reviewsContainer.style.display = 'block';
}

function formatDate(dateString) {
    const date = new Date(dateString);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}.${month}.${day}`;
}

function escapeHtml(text) {
    const map = {
        '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#039;'
    };
    return text.replace(/[&<>"']/g, function (m) {
        return map[m];
    });
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

async function deleteReview(reviewId, productName) {
    if (!confirm(`'${productName}'에 대한 리뷰를 삭제하시겠습니까?`)) {
        return;
    }

    try {
        const token = AuthManager.getToken();
        // 컨트롤러 경로와 일치하도록 수정 (/api/reviews/{reviewId})
        const response = await fetch(`/api/reviews/${reviewId}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        const data = await response.json();

        if (response.ok && data.success) {
            alert('리뷰가 삭제되었습니다.');
            // 페이지 새로고침하여 업데이트된 목록 표시
            loadMyReviews();
        } else {
            alert(data.message || '리뷰 삭제에 실패했습니다.');
        }

    } catch (error) {
        console.error('리뷰 삭제 실패:', error);
        alert('오류가 발생했습니다. 다시 시도해주세요.');
    }
}