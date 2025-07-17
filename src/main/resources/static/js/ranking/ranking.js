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

// 랭킹 페이지 상태 관리
let rankingState = {
    sortOrder: 'reviews',    // 'reviews', 'likes', 'rating'
    mainCategory: 'all',     // 'all', 'top', 'bottom', 'shoes'
    subCategory: 'all',      // 하위 카테고리
    viewMode: 'grid',        // 'grid', 'list'
    showLikedOnly: false,    // 🆕 좋아요 많은 상품만 보기 (모든 사용자용)
    currentPage: 1,
    itemsPerPage: 10,
    allProducts: [],         // 전체 상품 데이터
    filteredProducts: [],    // 필터링된 상품 데이터
    userLikedProducts: new Set(), // 사용자가 좋아요한 상품 ID들 (로그인한 경우만)
    likeThreshold: 1         // 🆕 좋아요 임계값 (이 수치 이상인 상품만 표시)
};

document.addEventListener('DOMContentLoaded', function() {
    // URL 파라미터 파싱
    parseURLParameters();

    // 좋아요 상품 로드 (로그인된 경우만)
    loadUserLikedProducts();

    // 랭킹 데이터 로드
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

    if (urlParams.has('view')) {
        rankingState.viewMode = urlParams.get('view');
    }

    if (urlParams.has('page')) {
        rankingState.currentPage = parseInt(urlParams.get('page')) || 1;
    }

    // 🆕 좋아요 필터 URL 파라미터 추가
    if (urlParams.has('liked')) {
        rankingState.showLikedOnly = urlParams.get('liked') === 'true';
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

    // 뷰 모드 버튼 업데이트
    document.querySelectorAll('.view-btn').forEach(btn => {
        btn.classList.remove('active');
    });
    const activeViewBtn = document.querySelector(`[data-view="${rankingState.viewMode}"]`);
    if (activeViewBtn) {
        activeViewBtn.classList.add('active');
    }

    // 🆕 좋아요 필터 버튼 업데이트
    const filterBtn = document.getElementById('filter-liked-btn');
    if (filterBtn) {
        if (rankingState.showLikedOnly) {
            filterBtn.classList.add('active');
            filterBtn.innerHTML = '<span class="heart-icon">♥</span> 전체 보기';
        } else {
            filterBtn.classList.remove('active');
            filterBtn.innerHTML = '<span class="heart-icon">♥</span> 좋아요 많은 상품만';
        }
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

    if (rankingState.viewMode !== 'grid') {
        params.set('view', rankingState.viewMode);
    }

    if (rankingState.currentPage !== 1) {
        params.set('page', rankingState.currentPage);
    }

    // 🆕 좋아요 필터 URL 파라미터 추가
    if (rankingState.showLikedOnly) {
        params.set('liked', 'true');
    }

    const newURL = params.toString() ? `${window.location.pathname}?${params.toString()}` : window.location.pathname;
    window.history.pushState(null, '', newURL);
}

// 🆕 랭킹 데이터 로드 (DB에서 정렬 완료된 데이터 받기)
async function loadRankingData() {
    showLoading(true);

    try {
        let url = '/api/ranking/products';
        let params = new URLSearchParams();

        // 🆕 정렬 기준을 sortBy 파라미터로 전달
        params.append('sortBy', rankingState.sortOrder);

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

        console.log('🔍 API 요청:', url);
        console.log('🔍 정렬 기준:', rankingState.sortOrder);
        console.log('🔍 카테고리:', rankingState.mainCategory, rankingState.subCategory);

        const response = await fetch(url);

        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }

        const data = await response.json();
        console.log('🔍 API 응답:', data);

        if (data.success) {
            let products = data.products || [];

            rankingState.allProducts = products;

            applyFilters();
            renderRankingProducts();
            renderPagination();
            updateSectionTitle();
        } else {
            console.error('랭킹 데이터 로드 실패:', data.message);
            showEmptyState();
        }
    } catch (error) {
        console.error('랭킹 데이터 로드 에러:', error);

        // API 실패 시 더미 데이터 사용
        console.log('API 실패 - 더미 데이터를 사용합니다.');
        loadDummyData();
    } finally {
        showLoading(false);
    }
}

// 개발용 더미 데이터 로드
function loadDummyData() {
    const dummyProducts = [
        {
            productNo: 1,
            productName: "베이직 티셔츠",
            brandName: "브랜드A",
            price: 29000,
            salePrice: 25000,
            isActiveSale: true,
            salePercentage: 14,
            isRecommended: true,
            mainCategory: "상의",
            subCategory: "티셔츠",
            imageUrls: ["https://via.placeholder.com/300x300/667eea/ffffff?text=티셔츠"],
            likeCount: 150,
            reviewCount: 45,
            averageRating: 4.5
        },
        {
            productNo: 2,
            productName: "데님 팬츠",
            brandName: "브랜드B",
            price: 59000,
            isActiveSale: false,
            isRecommended: false,
            mainCategory: "하의",
            subCategory: "청바지",
            imageUrls: ["https://via.placeholder.com/300x300/764ba2/ffffff?text=청바지"],
            likeCount: 89,
            reviewCount: 32,
            averageRating: 4.2
        },
        {
            productNo: 3,
            productName: "스니커즈",
            brandName: "브랜드C",
            price: 89000,
            salePrice: 69000,
            isActiveSale: true,
            salePercentage: 22,
            isRecommended: true,
            mainCategory: "신발",
            subCategory: "운동화",
            imageUrls: ["https://via.placeholder.com/300x300/28a745/ffffff?text=운동화"],
            likeCount: 203,
            reviewCount: 78,
            averageRating: 4.7
        },
        {
            productNo: 4,
            productName: "오버핏 후드",
            brandName: "브랜드D",
            price: 65000,
            salePrice: 52000,
            isActiveSale: true,
            salePercentage: 20,
            isRecommended: false,
            mainCategory: "상의",
            subCategory: "후드",
            imageUrls: ["https://via.placeholder.com/300x300/dc3545/ffffff?text=후드"],
            likeCount: 124,
            reviewCount: 56,
            averageRating: 4.3
        },
        {
            productNo: 5,
            productName: "체크 셔츠",
            brandName: "브랜드E",
            price: 39000,
            isActiveSale: false,
            isRecommended: true,
            mainCategory: "상의",
            subCategory: "셔츠",
            imageUrls: ["https://via.placeholder.com/300x300/6c757d/ffffff?text=셔츠"],
            likeCount: 98,
            reviewCount: 34,
            averageRating: 4.1
        },
        {
            productNo: 6,
            productName: "와이드 슬랙스",
            brandName: "브랜드F",
            price: 75000,
            salePrice: 60000,
            isActiveSale: true,
            salePercentage: 20,
            isRecommended: false,
            mainCategory: "하의",
            subCategory: "슬랙스",
            imageUrls: ["https://via.placeholder.com/300x300/ffc107/ffffff?text=슬랙스"],
            likeCount: 87,
            reviewCount: 29,
            averageRating: 3.9
        },
        {
            productNo: 7,
            productName: "첼시 부츠",
            brandName: "브랜드G",
            price: 159000,
            isActiveSale: false,
            isRecommended: true,
            mainCategory: "신발",
            subCategory: "부츠",
            imageUrls: ["https://via.placeholder.com/300x300/6f42c1/ffffff?text=부츠"],
            likeCount: 156,
            reviewCount: 67,
            averageRating: 4.6
        },
        {
            productNo: 8,
            productName: "미니멀 니트",
            brandName: "브랜드H",
            price: 89000,
            salePrice: 71000,
            isActiveSale: true,
            salePercentage: 20,
            isRecommended: true,
            mainCategory: "상의",
            subCategory: "니트",
            imageUrls: ["https://via.placeholder.com/300x300/20c997/ffffff?text=니트"],
            likeCount: 134,
            reviewCount: 89,
            averageRating: 4.8
        },
        // 🆕 좋아요 적은 상품들 추가 (테스트용)
        {
            productNo: 9,
            productName: "기본 슬랙스",
            brandName: "브랜드I",
            price: 45000,
            isActiveSale: false,
            isRecommended: false,
            mainCategory: "하의",
            subCategory: "슬랙스",
            imageUrls: ["https://via.placeholder.com/300x300/6c757d/ffffff?text=기본슬랙스"],
            likeCount: 25,
            reviewCount: 12,
            averageRating: 3.5
        },
        {
            productNo: 10,
            productName: "일반 운동화",
            brandName: "브랜드J",
            price: 79000,
            isActiveSale: false,
            isRecommended: false,
            mainCategory: "신발",
            subCategory: "운동화",
            imageUrls: ["https://via.placeholder.com/300x300/adb5bd/ffffff?text=일반운동화"],
            likeCount: 15,
            reviewCount: 8,
            averageRating: 3.2
        }
    ];

    // 카테고리 필터링 적용
    let filteredProducts = dummyProducts;

    if (rankingState.subCategory !== 'all') {
        filteredProducts = dummyProducts.filter(product => product.subCategory === rankingState.subCategory);
    } else if (rankingState.mainCategory !== 'all') {
        const categoryMap = {
            'top': '상의',
            'bottom': '하의',
            'shoes': '신발'
        };
        const categoryName = categoryMap[rankingState.mainCategory];
        if (categoryName) {
            filteredProducts = dummyProducts.filter(product => product.mainCategory === categoryName);
        }
    }

    // 정렬 적용
    switch (rankingState.sortOrder) {
        case 'likes':
            filteredProducts.sort((a, b) => (b.likeCount || 0) - (a.likeCount || 0));
            break;
        case 'rating':
            filteredProducts.sort((a, b) => {
                const ratingA = a.averageRating || 0;
                const ratingB = b.averageRating || 0;
                if (ratingB !== ratingA) {
                    return ratingB - ratingA;
                }
                return (b.reviewCount || 0) - (a.reviewCount || 0);
            });
            break;
        case 'reviews':
        default:
            filteredProducts.sort((a, b) => (b.reviewCount || 0) - (a.reviewCount || 0));
            break;
    }

    rankingState.allProducts = filteredProducts;
    applyFilters();
    renderRankingProducts();
    renderPagination(1, 1, filteredProducts.length);
    updateSectionTitle();
}

// 하위 카테고리 로드
async function loadSubCategories(mainCategory) {
    try {
        const response = await fetch(`/api/ranking/categories/sub?mainCategory=${mainCategory}`);

        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }

        const data = await response.json();

        if (data.success) {
            const subCategoriesContainer = document.getElementById('sub-categories');
            const subCategoryFilters = document.getElementById('sub-category-filters');

            if (subCategoriesContainer && data.categories && data.categories.length > 0) {
                let html = '<button class="sub-category-btn' + (rankingState.subCategory === 'all' ? ' active' : '') + '" data-sub-category="all" onclick="filterBySubCategory(\'all\')">전체</button>';

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

        const dummySubCategories = {
            'top': ['티셔츠', '셔츠', '후드', '니트'],
            'bottom': ['청바지', '슬랙스', '반바지', '트레이닝'],
            'shoes': ['운동화', '구두', '부츠', '샌들']
        };

        const categories = dummySubCategories[mainCategory] || [];

        if (categories.length > 0) {
            const subCategoriesContainer = document.getElementById('sub-categories');
            const subCategoryFilters = document.getElementById('sub-category-filters');

            let html = '<button class="sub-category-btn' + (rankingState.subCategory === 'all' ? ' active' : '') + '" data-sub-category="all" onclick="filterBySubCategory(\'all\')">전체</button>';

            categories.forEach(category => {
                const isActive = rankingState.subCategory === category ? ' active' : '';
                html += `<button class="sub-category-btn${isActive}" data-sub-category="${category}" onclick="filterBySubCategory('${category}')">${category}</button>`;
            });

            if (subCategoriesContainer) {
                subCategoriesContainer.innerHTML = html;
            }
            if (subCategoryFilters) {
                subCategoryFilters.style.display = 'block';
            }
        }
    }
}

// 필터 적용
function applyFilters() {
    let filtered = [...rankingState.allProducts];

    // 🆕 좋아요 필터 적용 - 모든 사용자용 (좋아요 수가 많은 상품만)
    if (rankingState.showLikedOnly) {
        console.log(`좋아요 ${rankingState.likeThreshold}개 이상 상품만 필터링 중...`);

        filtered = filtered.filter(product => {
            const likeCount = product.likeCount || 0;
            const isPopular = likeCount >= rankingState.likeThreshold;
            console.log(`상품 ${product.productNo}(${product.productName}): 좋아요 ${likeCount}개 - ${isPopular ? '인기상품' : '일반상품'}`);
            return isPopular;
        });

        console.log('좋아요 필터링 후 상품 수:', filtered.length);
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

    // 페이지네이션 적용
    const totalProducts = rankingState.filteredProducts.length;
    const totalPages = Math.ceil(totalProducts / rankingState.itemsPerPage);
    const startIndex = (rankingState.currentPage - 1) * rankingState.itemsPerPage;
    const endIndex = Math.min(startIndex + rankingState.itemsPerPage, totalProducts);

    const productsToShow = rankingState.filteredProducts.slice(startIndex, endIndex);

    if (productsToShow.length === 0) {
        if (rankingGrid) rankingGrid.innerHTML = '';
        if (rankingState.showLikedOnly && noLikedDiv) {
            noLikedDiv.style.display = 'block';
        } else if (noProductsDiv) {
            noProductsDiv.style.display = 'block';
        }
        const paginationContainer = document.getElementById('pagination-container');
        if (paginationContainer) paginationContainer.style.display = 'none';
        return;
    }

    if (!rankingGrid) return;

    rankingGrid.className = `ranking-grid${rankingState.viewMode === 'list' ? ' list-view' : ''}`;

    let html = '';
    productsToShow.forEach((product, index) => {
        const isLiked = rankingState.userLikedProducts.has(product.productNo.toString());
        const displayPrice = (product.isActiveSale && product.salePrice) ? product.salePrice : product.price;
        const originalPrice = (product.isActiveSale && product.price !== product.salePrice) ? product.price : null;

        const globalIndex = rankingState.filteredProducts.findIndex(p => p.productNo === product.productNo);
        const rank = globalIndex + 1;
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
            ? `<img src="${product.imageUrls[0]}" alt="${product.productName}" onerror="this.style.display='none'; this.nextElementSibling.style.display='block';">`
            : ''}
                    <span style="${product.imageUrls && product.imageUrls.length > 0 ? 'display:none' : ''}">이미지 없음</span>
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

    renderPagination(totalPages, rankingState.currentPage, totalProducts);
}

// 페이지네이션 렌더링
function renderPagination(totalPages, currentPage, totalProducts) {
    const paginationContainer = document.getElementById('pagination-container');
    const pageNumbers = document.getElementById('page-numbers');
    const prevBtn = document.getElementById('prev-page');
    const nextBtn = document.getElementById('next-page');

    if (totalPages === undefined) {
        totalProducts = rankingState.filteredProducts.length;
        totalPages = Math.ceil(totalProducts / rankingState.itemsPerPage);
        currentPage = rankingState.currentPage;
    }

    if (!totalPages || totalPages <= 1) {
        if (paginationContainer) paginationContainer.style.display = 'none';
        return;
    }

    if (paginationContainer) paginationContainer.style.display = 'flex';

    if (prevBtn) prevBtn.disabled = currentPage <= 1;
    if (nextBtn) nextBtn.disabled = currentPage >= totalPages;

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
    rankingState.currentPage = 1;
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
    rankingState.currentPage = 1;

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
    rankingState.currentPage = 1;
    updateURL();
    loadRankingData();
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
    renderRankingProducts();
}

// 페이지 이동
function goToPage(page) {
    rankingState.currentPage = page;
    updateURL();
    renderRankingProducts();

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

    // 🆕 좋아요 필터 정보 추가
    if (rankingState.showLikedOnly) {
        title += ` (인기상품만)`;
    }

    titleElement.textContent = title;
}

// 🆕 좋아요 필터 토글 - 모든 사용자용 (인기상품 필터)
function toggleLikedFilter() {
    rankingState.showLikedOnly = !rankingState.showLikedOnly;
    rankingState.currentPage = 1; // 페이지 리셋

    const filterBtn = document.getElementById('filter-liked-btn');

    if (filterBtn) {
        if (rankingState.showLikedOnly) {
            filterBtn.classList.add('active');
            filterBtn.innerHTML = '<span class="heart-icon">♥</span> 전체 보기';
            console.log(`좋아요 ${rankingState.likeThreshold}개 이상 상품만 보기 활성화`);
        } else {
            filterBtn.classList.remove('active');
            filterBtn.innerHTML = '<span class="heart-icon">♥</span> 좋아요 많은 상품만';
            console.log('전체 보기 활성화');
        }
    }

    applyFilters();
    renderRankingProducts();
    updateSectionTitle(); // 🆕 제목도 업데이트
    updateURL(); // URL 업데이트
}

// 필터 초기화
function resetFilters() {
    rankingState.sortOrder = 'reviews';
    rankingState.mainCategory = 'all';
    rankingState.subCategory = 'all';
    rankingState.currentPage = 1;
    rankingState.showLikedOnly = false;

    updateFilterButtonsFromState();
    updateURL();
    loadRankingData();

    // 좋아요 필터 버튼 초기화
    const filterBtn = document.getElementById('filter-liked-btn');
    if (filterBtn) {
        filterBtn.classList.remove('active');
        filterBtn.innerHTML = '<span class="heart-icon">♥</span> 좋아요 많은 상품만';
    }
}

// 사용자가 좋아요한 상품들 불러오기 (로그인한 경우만)
function loadUserLikedProducts() {
    if (!AuthManager.isLoggedIn()) {
        console.log('로그인하지 않음 - 개인 좋아요 상품 로드 생략');
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
            throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        })
        .then(data => {
            if (data.success && data.likedProducts) {
                rankingState.userLikedProducts = new Set(
                    data.likedProducts.map(product => product.productNo.toString())
                );
                console.log('개인 좋아요 상품 로드 완료:', rankingState.userLikedProducts.size + '개');

                // 좋아요 상품 로드 완료 후 화면 다시 렌더링
                if (rankingState.allProducts.length > 0) {
                    renderRankingProducts();
                }
            }
        })
        .catch(error => {
            console.log('개인 좋아요 상품 로드 실패:', error.message);
            // 실패해도 계속 진행 (로그인하지 않았을 수 있음)
        });
}

// 랭킹에서 좋아요 토글 (로그인한 사용자만)
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
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }
            return response.json();
        })
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
                }

                // 좋아요 수 업데이트
                const productIndex = rankingState.allProducts.findIndex(p => p.productNo.toString() === productNo);
                if (productIndex !== -1) {
                    rankingState.allProducts[productIndex].likeCount = data.likeCount || 0;

                    // 좋아요 순 정렬인 경우 데이터 다시 로드
                    if (rankingState.sortOrder === 'likes') {
                        loadRankingData();
                    } else {
                        // 좋아요 수 변경 후 현재 상품의 좋아요 수 업데이트
                        const productIndex = rankingState.allProducts.findIndex(p => p.productNo.toString() === productNo);
                        if (productIndex !== -1) {
                            rankingState.allProducts[productIndex].likeCount = data.likeCount || 0;
                        }

                        // 필터 다시 적용 (인기상품 필터가 활성화된 경우 재필터링)
                        applyFilters();
                        renderRankingProducts();
                    }
                }

            } else {
                alert(data.message || '오류가 발생했습니다.');
            }
        })
        .catch(error => {
            console.error('좋아요 토글 에러:', error);

            // 개발 중이므로 임시로 클라이언트에서만 처리
            console.log('개발 중 - 클라이언트에서만 좋아요 상태 변경');

            if (rankingState.userLikedProducts.has(productNo)) {
                rankingState.userLikedProducts.delete(productNo);
                button.classList.remove('liked');
                heart.textContent = '♡';
            } else {
                rankingState.userLikedProducts.add(productNo);
                button.classList.add('liked');
                heart.textContent = '♥';
            }

            // 🆕 임시 좋아요 수 업데이트 (실제로는 서버에서 받아야 함)
            const productIndex = rankingState.allProducts.findIndex(p => p.productNo.toString() === productNo);
            if (productIndex !== -1) {
                if (rankingState.userLikedProducts.has(productNo)) {
                    rankingState.allProducts[productIndex].likeCount = (rankingState.allProducts[productIndex].likeCount || 0) + 1;
                } else {
                    rankingState.allProducts[productIndex].likeCount = Math.max(0, (rankingState.allProducts[productIndex].likeCount || 0) - 1);
                }
            }

            // 인기상품 필터가 활성화된 경우 재필터링
            if (rankingState.showLikedOnly) {
                applyFilters();
                renderRankingProducts();
            }
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