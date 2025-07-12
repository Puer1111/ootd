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

// 랭킹 페이지 상태 관리
let rankingState = {
    sortOrder: 'reviews',    // 'reviews', 'likes', 'rating'
    mainCategory: 'all',     // 'all', 'top', 'bottom', 'shoes'
    subCategory: 'all',      // 하위 카테고리
    period: 'all',           // 'all', 'week', 'month', 'year'
    viewMode: 'grid',        // 'grid', 'list'
    showLikedOnly: false,
    currentPage: 1,
    itemsPerPage: 20,
    allProducts: [],         // 전체 상품 데이터
    filteredProducts: [],    // 필터링된 상품 데이터
    userLikedProducts: new Set() // 사용자가 좋아요한 상품 ID들
};

document.addEventListener('DOMContentLoaded', function() {
    // URL 파라미터 파싱
    parseURLParameters();

    loadUserLikedProducts();
    loadRankingData();

    // 초기 UI 상태 설정
    updateFilterButtonsFromState();
});

// URL 파라미터 파싱
function parseURLParameters() {
    const urlParams = new URLSearchParams(window.location.search);

    if (urlParams.has('sort')) {
        rankingState.sortOrder = urlParams.get('sort');
    }

    if (urlParams.has('category')) {
        rankingState.mainCategory = urlParams.get('category');
    }

    if (urlParams.has('subCategory')) {
        rankingState.subCategory = urlParams.get('subCategory');
    }

    if (urlParams.has('period')) {
        rankingState.period = urlParams.get('period');
    }

    if (urlParams.has('view')) {
        rankingState.viewMode = urlParams.get('view');
    }

    if (urlParams.has('page')) {
        rankingState.currentPage = parseInt(urlParams.get('page')) || 1;
    }
}

// 현재 상태에 따라 필터 버튼 업데이트
function updateFilterButtonsFromState() {
    // 정렬 버튼 업데이트
    document.querySelectorAll('.sort-btn').forEach(btn => {
        btn.classList.remove('active');
    });
    const activeSortBtn = document.querySelector(`[data-sort="${rankingState.sortOrder}"]`);
    if (activeSortBtn) {
        activeSortBtn.classList.add('active');
    }

    // 카테고리 버튼 업데이트
    document.querySelectorAll('.category-btn').forEach(btn => {
        btn.classList.remove('active');
    });
    const activeCategoryBtn = document.querySelector(`[data-category="${rankingState.mainCategory}"]`);
    if (activeCategoryBtn) {
        activeCategoryBtn.classList.add('active');
    }

    // 기간 버튼 업데이트
    document.querySelectorAll('.period-btn').forEach(btn => {
        btn.classList.remove('active');
    });
    const activePeriodBtn = document.querySelector(`[data-period="${rankingState.period}"]`);
    if (activePeriodBtn) {
        activePeriodBtn.classList.add('active');
    }

    // 뷰 모드 버튼 업데이트
    document.querySelectorAll('.view-btn').forEach(btn => {
        btn.classList.remove('active');
    });
    const activeViewBtn = document.querySelector(`[data-view="${rankingState.viewMode}"]`);
    if (activeViewBtn) {
        activeViewBtn.classList.add('active');
    }

    // 하위 카테고리 로드
    if (rankingState.mainCategory !== 'all') {
        loadSubCategories(rankingState.mainCategory);
    }
}

// URL 업데이트
function updateURL() {
    const params = new URLSearchParams();

    if (rankingState.sortOrder !== 'reviews') {
        params.set('sort', rankingState.sortOrder);
    }

    if (rankingState.mainCategory !== 'all') {
        params.set('category', rankingState.mainCategory);
    }

    if (rankingState.subCategory !== 'all') {
        params.set('subCategory', rankingState.subCategory);
    }

    if (rankingState.period !== 'all') {
        params.set('period', rankingState.period);
    }

    if (rankingState.viewMode !== 'grid') {
        params.set('view', rankingState.viewMode);
    }

    if (rankingState.currentPage !== 1) {
        params.set('page', rankingState.currentPage);
    }

    const newURL = params.toString() ? `${window.location.pathname}?${params.toString()}` : window.location.pathname;
    window.history.pushState(null, '', newURL);
}

// ✅ 새로운 랭킹 API 사용
async function loadRankingData() {
    showLoading(true);

    try {
        let url = '/api/ranking/products';
        let params = new URLSearchParams();

        // 카테고리 필터링
        if (rankingState.subCategory !== 'all') {
            url = '/api/ranking/products/category';
            params.append('subCategory', rankingState.subCategory);
        } else if (rankingState.mainCategory !== 'all') {
            url = '/api/ranking/products/category';
            params.append('mainCategory', rankingState.mainCategory);
        }

        if (params.toString()) {
            url += '?' + params.toString();
        }

        const response = await fetch(url);
        const data = await response.json();

        if (data.success) {
            let products = data.products || [];

            // 클라이언트에서 정렬 처리
            switch (rankingState.sortOrder) {
                case 'likes':
                    products.sort((a, b) => (b.likeCount || 0) - (a.likeCount || 0));
                    break;
                case 'rating':
                    products.sort((a, b) => {
                        const ratingA = a.averageRating || 0;
                        const ratingB = b.averageRating || 0;
                        if (ratingB !== ratingA) {
                            return ratingB - ratingA;
                        }
                        return (b.reviewCount || 0) - (a.reviewCount || 0);
                    });
                    break;
                // 'reviews'는 이미 API에서 정렬되어 옴
            }

            // 클라이언트에서 페이지네이션 처리
            const totalProducts = products.length;
            const totalPages = Math.ceil(totalProducts / rankingState.itemsPerPage);
            const startIndex = (rankingState.currentPage - 1) * rankingState.itemsPerPage;
            const endIndex = Math.min(startIndex + rankingState.itemsPerPage, totalProducts);

            rankingState.allProducts = products.slice(startIndex, endIndex);

            applyFilters();
            renderRankingProducts();
            renderPagination(totalPages, rankingState.currentPage, totalProducts);
            updateSectionTitle();
        } else {
            console.error('랭킹 데이터 로드 실패:', data.message);
            showEmptyState();
        }
    } catch (error) {
        console.error('랭킹 데이터 로드 에러:', error);
        showEmptyState();
    } finally {
        showLoading(false);
    }
}

// 하위 카테고리 로드
async function loadSubCategories(mainCategory) {
    try {
        const response = await fetch(`/api/ranking/categories/sub?mainCategory=${mainCategory}`);
        const data = await response.json();

        if (data.success) {
            const subCategoriesContainer = document.getElementById('sub-categories');
            const subCategoryFilters = document.getElementById('sub-category-filters');

            if (subCategoriesContainer && data.categories.length > 0) {
                // 전체 버튼 추가
                let html = '<button class="sub-category-btn' + (rankingState.subCategory === 'all' ? ' active' : '') + '" data-sub-category="all" onclick="filterBySubCategory(\'all\')">전체</button>';

                // 하위 카테고리 버튼들 추가
                data.categories.forEach(category => {
                    const isActive = rankingState.subCategory === category ? ' active' : '';
                    html += `<button class="sub-category-btn${isActive}" data-sub-category="${category}" onclick="filterBySubCategory('${category}')">${category}</button>`;
                });

                subCategoriesContainer.innerHTML = html;
                if (subCategoryFilters) {
                    subCategoryFilters.style.display = 'block';
                }
            } else if (subCategoryFilters) {
                subCategoryFilters.style.display = 'none';
            }
        }
    } catch (error) {
        console.error('하위 카테고리 로드 실패:', error);
    }
}

// 필터 적용
function applyFilters() {
    let filtered = [...rankingState.allProducts];

    // 좋아요 필터 적용
    if (rankingState.showLikedOnly) {
        filtered = filtered.filter(product =>
            rankingState.userLikedProducts.has(product.productNo.toString())
        );
    }

    rankingState.filteredProducts = filtered;
}

// 랭킹 상품 렌더링
function renderRankingProducts() {
    const rankingGrid = document.getElementById('ranking-grid');
    const noProductsDiv = document.getElementById('no-products');
    const noLikedDiv = document.getElementById('no-liked-products');

    // 상태 초기화
    if (noProductsDiv) noProductsDiv.style.display = 'none';
    if (noLikedDiv) noLikedDiv.style.display = 'none';

    if (rankingState.filteredProducts.length === 0) {
        if (rankingGrid) rankingGrid.innerHTML = '';
        if (rankingState.showLikedOnly && noLikedDiv) {
            noLikedDiv.style.display = 'block';
        } else if (noProductsDiv) {
            noProductsDiv.style.display = 'block';
        }
        return;
    }

    if (!rankingGrid) return;

    // 뷰 모드에 따라 클래스 설정
    rankingGrid.className = `ranking-grid${rankingState.viewMode === 'list' ? ' list-view' : ''}`;

    let html = '';
    rankingState.filteredProducts.forEach((product, index) => {
        const isLiked = rankingState.userLikedProducts.has(product.productNo.toString());
        const displayPrice = (product.isActiveSale && product.salePrice) ? product.salePrice : product.price;
        const originalPrice = (product.isActiveSale && product.price !== product.salePrice) ? product.price : null;

        // 랭킹 순위 계산 (페이지네이션 고려)
        const rank = (rankingState.currentPage - 1) * rankingState.itemsPerPage + index + 1;
        const rankClass = rank <= 3 ? `rank-${rank}` : 'rank-other';

        html += `
            <div class="ranking-card${rankingState.viewMode === 'list' ? ' list-view' : ''}" data-product-no="${product.productNo}" onclick="goToProduct(this)">
                <div class="ranking-badge ${rankClass}">${rank}</div>
                
                ${product.isRecommended ? '<div class="badge recommended-badge">추천</div>' : ''}
                ${product.isActiveSale ? `<div class="badge sale-badge">${product.salePercentage || 0}% OFF</div>` : ''}
                
                <button class="like-btn-card ${isLiked ? 'liked' : ''}" 
                        data-product-no="${product.productNo}" 
                        onclick="toggleLikeFromRanking(event, this)">
                    <span class="heart">${isLiked ? '♥' : '♡'}</span>
                </button>

                <div class="card-image">
                    ${product.imageUrls && product.imageUrls.length > 0
            ? `<img src="${product.imageUrls[0]}" alt="${product.productName}">`
            : '<span>이미지 없음</span>'}
                </div>

                <div class="card-content">
                    <div class="card-subtitle">${product.brandName || '브랜드명'}</div>
                    <div class="card-title">${product.productName || '상품명'}</div>
                    <div class="card-tag">${product.subCategory || '카테고리'}</div>
                    
                    <div class="card-price">
                        ${originalPrice ? `<span class="original-price">${originalPrice.toLocaleString()}원</span>` : ''}
                        <span class="current-price">${displayPrice ? displayPrice.toLocaleString() : '0'}원</span>
                    </div>

                    <div class="card-stats">
                        <div class="stat-item">
                            <span class="icon heart-icon">♥</span>
                            <span>${product.likeCount || 0}</span>
                        </div>
                        <div class="stat-item">
                            <span class="icon star-icon">★</span>
                            <span>${product.averageRating ? product.averageRating.toFixed(1) : '0.0'}</span>
                        </div>
                        <div class="stat-item">
                            <span class="icon review-icon">📝</span>
                            <span>리뷰 ${product.reviewCount || 0}개</span>
                        </div>
                    </div>
                </div>
            </div>
        `;
    });

    rankingGrid.innerHTML = html;
}

// 페이지네이션 렌더링
function renderPagination(totalPages, currentPage, totalProducts) {
    const paginationContainer = document.getElementById('pagination-container');
    const pageNumbers = document.getElementById('page-numbers');
    const prevBtn = document.getElementById('prev-page');
    const nextBtn = document.getElementById('next-page');

    if (!totalPages || totalPages <= 1) {
        if (paginationContainer) paginationContainer.style.display = 'none';
        return;
    }

    if (paginationContainer) paginationContainer.style.display = 'flex';

    // 이전/다음 버튼 상태
    if (prevBtn) prevBtn.disabled = currentPage <= 1;
    if (nextBtn) nextBtn.disabled = currentPage >= totalPages;

    // 페이지 번호 생성
    if (pageNumbers) {
        let pagesHtml = '';
        const startPage = Math.max(1, currentPage - 2);
        const endPage = Math.min(totalPages, currentPage + 2);

        for (let i = startPage; i <= endPage; i++) {
            const isActive = i === currentPage ? ' active' : '';
            pagesHtml += `<div class="page-number${isActive}" onclick="goToPage(${i})">${i}</div>`;
        }

        pageNumbers.innerHTML = pagesHtml;
    }
}

// 정렬 방식 변경
function changeSortOrder(sortOrder) {
    document.querySelectorAll('.sort-btn').forEach(btn => {
        btn.classList.remove('active');
    });
    const activeBtn = document.querySelector(`[data-sort="${sortOrder}"]`);
    if (activeBtn) {
        activeBtn.classList.add('active');
    }

    rankingState.sortOrder = sortOrder;
    rankingState.currentPage = 1; // 페이지 리셋
    updateURL();
    loadRankingData();
}

// 카테고리 필터링
function filterByCategory(category) {
    document.querySelectorAll('.category-btn').forEach(btn => {
        btn.classList.remove('active');
    });
    const activeBtn = document.querySelector(`[data-category="${category}"]`);
    if (activeBtn) {
        activeBtn.classList.add('active');
    }

    rankingState.mainCategory = category;
    rankingState.subCategory = 'all';
    rankingState.currentPage = 1; // 페이지 리셋

    // 하위 카테고리 로드
    if (category !== 'all') {
        loadSubCategories(category);
    } else {
        const subCategoryFilters = document.getElementById('sub-category-filters');
        if (subCategoryFilters) {
            subCategoryFilters.style.display = 'none';
        }
    }

    updateURL();
    loadRankingData();
}

// 하위 카테고리 필터링
function filterBySubCategory(subCategory) {
    document.querySelectorAll('.sub-category-btn').forEach(btn => {
        btn.classList.remove('active');
    });
    const activeBtn = document.querySelector(`[data-sub-category="${subCategory}"]`);
    if (activeBtn) {
        activeBtn.classList.add('active');
    }

    rankingState.subCategory = subCategory;
    rankingState.currentPage = 1; // 페이지 리셋
    updateURL();
    loadRankingData();
}

// 기간 필터링 (현재는 UI만 제공, 실제 기능은 추후 구현 가능)
function filterByPeriod(period) {
    document.querySelectorAll('.period-btn').forEach(btn => {
        btn.classList.remove('active');
    });
    const activeBtn = document.querySelector(`[data-period="${period}"]`);
    if (activeBtn) {
        activeBtn.classList.add('active');
    }

    rankingState.period = period;
    rankingState.currentPage = 1; // 페이지 리셋
    updateURL();
    // 현재는 기간 필터링 API가 없으므로 데이터 다시 로드하지 않음
    // loadRankingData(); // 추후 API 구현 시 활성화
}

// 뷰 모드 변경
function changeView(viewMode) {
    document.querySelectorAll('.view-btn').forEach(btn => {
        btn.classList.remove('active');
    });
    const activeBtn = document.querySelector(`[data-view="${viewMode}"]`);
    if (activeBtn) {
        activeBtn.classList.add('active');
    }

    rankingState.viewMode = viewMode;
    updateURL();
    renderRankingProducts(); // 다시 렌더링
}

// 페이지 이동
function goToPage(page) {
    rankingState.currentPage = page;
    updateURL();
    loadRankingData();

    // 상단으로 스크롤
    window.scrollTo({ top: 0, behavior: 'smooth' });
}

// 페이지 변경 (이전/다음)
function changePage(direction) {
    const newPage = rankingState.currentPage + direction;
    if (newPage >= 1) {
        goToPage(newPage);
    }
}

// 섹션 제목 업데이트
function updateSectionTitle() {
    const titleElement = document.getElementById('section-title');
    if (!titleElement) return;

    let title = '';

    switch (rankingState.sortOrder) {
        case 'reviews':
            title = '리뷰 랭킹';
            break;
        case 'likes':
            title = '좋아요 랭킹';
            break;
        case 'rating':
            title = '평점 랭킹';
            break;
        default:
            title = '상품 랭킹';
    }

    // 카테고리 정보 추가
    if (rankingState.subCategory !== 'all') {
        title += ` - ${rankingState.subCategory}`;
    } else if (rankingState.mainCategory !== 'all') {
        const categoryNames = {
            'top': '상의',
            'bottom': '하의',
            'shoes': '신발'
        };
        title += ` - ${categoryNames[rankingState.mainCategory] || rankingState.mainCategory}`;
    }

    // 기간 정보 추가 (현재는 UI만)
    if (rankingState.period !== 'all') {
        const periodNames = {
            'week': '주간',
            'month': '월간',
            'year': '연간'
        };
        title = `${periodNames[rankingState.period]} ${title}`;
    }

    titleElement.textContent = title;
}

// 좋아요 필터 토글
function toggleLikedFilter() {
    if (!AuthManager.isLoggedIn()) {
        alert('로그인이 필요합니다.');
        window.location.href = '/login';
        return;
    }

    rankingState.showLikedOnly = !rankingState.showLikedOnly;
    const filterBtn = document.getElementById('filter-liked-btn');

    if (filterBtn) {
        if (rankingState.showLikedOnly) {
            filterBtn.classList.add('active');
            filterBtn.innerHTML = '<span class="heart-icon">♥</span> 전체 보기';
        } else {
            filterBtn.classList.remove('active');
            filterBtn.innerHTML = '<span class="heart-icon">♥</span> 좋아요만 보기';
        }
    }

    applyFilters();
    renderRankingProducts();
}

// 필터 초기화
function resetFilters() {
    rankingState.sortOrder = 'reviews';
    rankingState.mainCategory = 'all';
    rankingState.subCategory = 'all';
    rankingState.period = 'all';
    rankingState.currentPage = 1;
    rankingState.showLikedOnly = false;

    updateFilterButtonsFromState();
    updateURL();
    loadRankingData();

    // 좋아요 필터 버튼 초기화
    const filterBtn = document.getElementById('filter-liked-btn');
    if (filterBtn) {
        filterBtn.classList.remove('active');
        filterBtn.innerHTML = '<span class="heart-icon">♥</span> 좋아요만 보기';
    }
}

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
                rankingState.userLikedProducts = new Set(data.likedProducts.map(p => p.productNo.toString()));
            }
        })
        .catch(error => {
            console.log('좋아요 상품 로드 실패 (로그인 안 됨 또는 오류):', error);
        });
}

// 랭킹에서 좋아요 토글
function toggleLikeFromRanking(event, button) {
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
                    rankingState.userLikedProducts.add(productNo);
                    button.classList.add('liked');
                    heart.textContent = '♥';
                } else {
                    // 좋아요 취소
                    rankingState.userLikedProducts.delete(productNo);
                    button.classList.remove('liked');
                    heart.textContent = '♡';

                    // 좋아요 필터링 중이고 좋아요가 취소되면 다시 필터링
                    if (rankingState.showLikedOnly) {
                        applyFilters();
                        renderRankingProducts();
                    }
                }

                // 좋아요 수 업데이트
                const productIndex = rankingState.allProducts.findIndex(p => p.productNo.toString() === productNo);
                if (productIndex !== -1) {
                    rankingState.allProducts[productIndex].likeCount = data.likeCount;

                    // 좋아요 순 정렬인 경우 데이터 다시 로드
                    if (rankingState.sortOrder === 'likes') {
                        loadRankingData();
                    } else {
                        renderRankingProducts();
                    }
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

// 로딩 상태 표시
function showLoading(show) {
    const loadingState = document.getElementById('loading-state');
    const rankingGrid = document.getElementById('ranking-grid');

    if (show) {
        if (loadingState) loadingState.style.display = 'block';
        if (rankingGrid) rankingGrid.style.display = 'none';
    } else {
        if (loadingState) loadingState.style.display = 'none';
        if (rankingGrid) rankingGrid.style.display = 'grid';
    }
}

// 빈 상태 표시
function showEmptyState() {
    const rankingGrid = document.getElementById('ranking-grid');
    const noProductsDiv = document.getElementById('no-products');

    if (rankingGrid) rankingGrid.innerHTML = '';
    if (noProductsDiv) noProductsDiv.style.display = 'block';
}

// 상품 상세 페이지로 이동
function goToProduct(element) {
    const productNo = element.getAttribute('data-product-no');
    if (productNo) {
        location.href = '/products/' + productNo;
    }
}

// 뒤로가기
function goBack() {
    if (window.history.length > 1) {
        window.history.back();
    } else {
        window.location.href = '/';
    }
}

// 윈도우의 popstate 이벤트 처리 (뒤로가기/앞으로가기)
window.addEventListener('popstate', function(event) {
    parseURLParameters();
    updateFilterButtonsFromState();
    loadRankingData();
});