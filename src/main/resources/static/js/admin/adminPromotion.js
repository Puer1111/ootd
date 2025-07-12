// 전역 변수
let allProducts = [];
let currentPromotions = [];

document.addEventListener('DOMContentLoaded', function() {
    initializePromotionPage();
    loadPromotionList();
    setupEventListeners();
});

// 페이지 초기화
function initializePromotionPage() {
    // 사이드바 메뉴 활성화 처리
    const submenuItems = document.querySelectorAll('[data-target-section]');
    submenuItems.forEach(item => {
        item.addEventListener('click', function(e) {
            e.preventDefault();
            const targetSection = this.getAttribute('data-target-section');
            showSection(targetSection);
        });
    });

    // 기본 섹션 표시
    showSection('promotion-list-section');
}

// 이벤트 리스너 설정
function setupEventListeners() {
    // 필터 변경 시
    document.getElementById('filter-type').addEventListener('change', filterPromotions);

    // 할인율, 원가 입력 시 세일가 자동 계산
    document.getElementById('sale-percentage').addEventListener('input', calculateSalePrice);
    document.getElementById('original-price').addEventListener('input', calculateSalePrice);

    // 엔터키로 검색
    document.getElementById('product-search').addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            searchProducts();
        }
    });

    document.getElementById('sale-product-search').addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            searchProductsForSale();
        }
    });
}

// 섹션 표시
function showSection(sectionId) {
    // 모든 섹션 숨기기
    const sections = document.querySelectorAll('.admin-table-container, #recommended-section, #sale-section');
    sections.forEach(section => {
        section.style.display = 'none';
    });

    // 선택된 섹션 표시
    const targetSection = document.getElementById(sectionId);
    if (targetSection) {
        targetSection.style.display = 'block';

        // 각 섹션별 데이터 로드
        switch(sectionId) {
            case 'promotion-list-section':
                loadPromotionList();
                break;
            case 'recommended-section':
                loadRecommendedProducts();
                break;
            case 'sale-section':
                loadSaleProducts();
                break;
        }
    }
}

// 전체 프로모션 목록 로드
async function loadPromotionList() {
    try {
        showLoading('promotion-table');

        // 모든 상품 정보 가져오기
        const [productsResponse, recommendedResponse, saleResponse] = await Promise.all([
            fetch('/api/products/ranking'),
            fetch('/admin/promotion/recommended'),
            fetch('/admin/promotion/sale')
        ]);

        const productsData = await productsResponse.json();
        const recommendedData = await recommendedResponse.json();
        const saleData = await saleResponse.json();

        if (productsData.success) {
            allProducts = productsData.products;

            // 프로모션 정보 병합
            const recommendedMap = new Map();
            const saleMap = new Map();

            if (recommendedData.success) {
                recommendedData.promotions.forEach(promo => {
                    recommendedMap.set(promo.productNo, promo);
                });
            }

            if (saleData.success) {
                saleData.promotions.forEach(promo => {
                    saleMap.set(promo.productNo, promo);
                });
            }

            // 프로모션 정보가 있는 상품만 필터링
            currentPromotions = allProducts.filter(product => {
                const recommended = recommendedMap.get(product.productNo);
                const sale = saleMap.get(product.productNo);

                if (recommended || sale) {
                    product.promotion = {
                        recommended: recommended,
                        sale: sale
                    };
                    return true;
                }
                return false;
            });

            renderPromotionTable();
        }
    } catch (error) {
        console.error('프로모션 목록 로드 실패:', error);
        showError('프로모션 목록을 불러오는 중 오류가 발생했습니다.');
    }
}

// 프로모션 테이블 렌더링
function renderPromotionTable() {
    const tbody = document.querySelector('#promotion-table tbody');

    if (currentPromotions.length === 0) {
        tbody.innerHTML = '<tr><td colspan="11" class="empty-state">프로모션이 설정된 상품이 없습니다.</td></tr>';
        return;
    }

    let html = '';
    currentPromotions.forEach(product => {
        const recommended = product.promotion?.recommended;
        const sale = product.promotion?.sale;

        html += `
            <tr>
                <td>${product.productNo}</td>
                <td>${product.productName}</td>
                <td>${product.brandName || '-'}</td>
                <td>
                    ${recommended ?
            `<span class="promotion-status active">추천</span>` :
            `<span class="promotion-status inactive">-</span>`
        }
                </td>
                <td>${recommended ? recommended.promotionPriority || 0 : '-'}</td>
                <td>
                    ${sale ?
            `<span class="promotion-status active">세일</span>` :
            `<span class="promotion-status inactive">-</span>`
        }
                </td>
                <td>${sale ? sale.salePercentage + '%' : '-'}</td>
                <td>${sale ? (sale.originalPrice || 0).toLocaleString() + '원' : '-'}</td>
                <td>
                    ${sale ?
            `<span class="sale-price">${(sale.salePrice || 0).toLocaleString()}원</span>` :
            '-'
        }
                </td>
                <td>
                    ${sale && sale.saleStartDate ?
            `${formatDate(sale.saleStartDate)} ~ ${sale.saleEndDate ? formatDate(sale.saleEndDate) : '무제한'}` :
            '-'
        }
                </td>
                <td>
                    <button class="btn btn-sm btn-primary" onclick="editPromotion(${product.productNo})">수정</button>
                    <button class="btn btn-sm btn-danger" onclick="deletePromotion(${product.productNo})">삭제</button>
                </td>
            </tr>
        `;
    });

    tbody.innerHTML = html;
}

// 상품 검색 (추천용)
async function searchProducts() {
    const searchTerm = document.getElementById('product-search').value.trim();
    if (!searchTerm) {
        alert('검색어를 입력해주세요.');
        return;
    }

    try {
        showLoading('search-results');

        const response = await fetch('/api/products/ranking');
        const data = await response.json();

        if (data.success) {
            const filteredProducts = data.products.filter(product =>
                product.productName.toLowerCase().includes(searchTerm.toLowerCase()) ||
                product.productNo.toString().includes(searchTerm)
            );

            renderSearchResults(filteredProducts, 'search-results', 'recommended');
        }
    } catch (error) {
        console.error('상품 검색 실패:', error);
        showError('상품 검색 중 오류가 발생했습니다.');
    }
}

// 상품 검색 (세일용)
async function searchProductsForSale() {
    const searchTerm = document.getElementById('sale-product-search').value.trim();
    if (!searchTerm) {
        alert('검색어를 입력해주세요.');
        return;
    }

    try {
        showLoading('sale-search-results');

        const response = await fetch('/api/products/ranking');
        const data = await response.json();

        if (data.success) {
            const filteredProducts = data.products.filter(product =>
                product.productName.toLowerCase().includes(searchTerm.toLowerCase()) ||
                product.productNo.toString().includes(searchTerm)
            );

            renderSearchResults(filteredProducts, 'sale-search-results', 'sale');
        }
    } catch (error) {
        console.error('상품 검색 실패:', error);
        showError('상품 검색 중 오류가 발생했습니다.');
    }
}

// 검색 결과 렌더링
function renderSearchResults(products, containerId, type) {
    const container = document.getElementById(containerId);

    if (products.length === 0) {
        container.innerHTML = '<div class="empty-state"><p>검색 결과가 없습니다.</p></div>';
        return;
    }

    let html = '';
    products.forEach(product => {
        const imageUrl = product.imageUrls && product.imageUrls.length > 0 ? product.imageUrls[0] : '/img/common/no-image.png';

        html += `
            <div class="search-result-item">
                <img src="${imageUrl}" alt="${product.productName}" class="search-result-image">
                <div class="search-result-info">
                    <h4>${product.productName}</h4>
                    <p>상품번호: ${product.productNo}</p>
                    <p>브랜드: ${product.brandName || '-'}</p>
                    <p>가격: ${(product.price || 0).toLocaleString()}원</p>
                </div>
                <div class="search-result-actions">
                    <button class="btn btn-sm btn-primary" onclick="openPromotionModal(${product.productNo}, '${type}')">
                        ${type === 'recommended' ? '추천 설정' : '세일 설정'}
                    </button>
                </div>
            </div>
        `;
    });

    container.innerHTML = html;
}

// 현재 추천 상품 목록 로드
async function loadRecommendedProducts() {
    try {
        const response = await fetch('/admin/promotion/recommended');
        const data = await response.json();

        if (data.success) {
            renderPromotionList(data.promotions, 'recommended-list', 'recommended');
        }
    } catch (error) {
        console.error('추천 상품 로드 실패:', error);
        showError('추천 상품을 불러오는 중 오류가 발생했습니다.');
    }
}

// 현재 세일 상품 목록 로드
async function loadSaleProducts() {
    try {
        const response = await fetch('/admin/promotion/sale');
        const data = await response.json();

        if (data.success) {
            renderPromotionList(data.promotions, 'sale-list', 'sale');
        }
    } catch (error) {
        console.error('세일 상품 로드 실패:', error);
        showError('세일 상품을 불러오는 중 오류가 발생했습니다.');
    }
}

// 프로모션 목록 렌더링
function renderPromotionList(promotions, containerId, type) {
    const container = document.getElementById(containerId);

    if (promotions.length === 0) {
        container.innerHTML = `<div class="empty-state"><p>설정된 ${type === 'recommended' ? '추천' : '세일'} 상품이 없습니다.</p></div>`;
        return;
    }

    let html = '';
    promotions.forEach(promotion => {
        const badgeClass = type === 'recommended' ? 'recommended' : 'sale';
        const badgeText = type === 'recommended' ? '추천' : '세일';

        html += `
            <div class="promotion-item">
                <div class="promotion-item-header">
                    <span class="promotion-badge ${badgeClass}">${badgeText}</span>
                </div>
                <h4>상품번호: ${promotion.productNo}</h4>
                ${type === 'recommended' ?
            `<p>우선순위: ${promotion.promotionPriority || 0}</p>` :
            `
                    <p>할인율: ${promotion.salePercentage}%</p>
                    <p>원가: ${(promotion.originalPrice || 0).toLocaleString()}원</p>
                    <p>세일가: ${(promotion.salePrice || 0).toLocaleString()}원</p>
                    ${promotion.saleStartDate ? `<p>기간: ${formatDate(promotion.saleStartDate)} ~ ${promotion.saleEndDate ? formatDate(promotion.saleEndDate) : '무제한'}</p>` : ''}
                    `
        }
                <div class="promotion-item-actions">
                    <button class="btn btn-sm btn-warning" onclick="editPromotionDirect(${promotion.productNo}, '${type}')">수정</button>
                    <button class="btn btn-sm btn-danger" onclick="removePromotionDirect(${promotion.productNo}, '${type}')">해제</button>
                </div>
            </div>
        `;
    });

    container.innerHTML = html;
}

// 프로모션 모달 열기
async function openPromotionModal(productNo, type) {
    try {
        // 상품 정보 가져오기
        const productResponse = await fetch(`/api/select/product/${productNo}`);
        const productData = await productResponse.json();

        if (type === 'recommended') {
            document.getElementById('recommended-product-no').value = productNo;
            renderProductInfo(productData, 'recommended-product-info');
            document.getElementById('recommended-modal').style.display = 'block';
        } else {
            document.getElementById('sale-product-no').value = productNo;
            document.getElementById('original-price').value = productData.price || 0;
            renderProductInfo(productData, 'sale-product-info');
            calculateSalePrice();
            document.getElementById('sale-modal').style.display = 'block';
        }
    } catch (error) {
        console.error('상품 정보 로드 실패:', error);
        showError('상품 정보를 불러오는 중 오류가 발생했습니다.');
    }
}

// 상품 정보 렌더링
function renderProductInfo(product, containerId) {
    const container = document.getElementById(containerId);
    const imageUrl = product.imageUrls && product.imageUrls.length > 0 ? product.imageUrls[0] : '/img/common/no-image.png';

    container.innerHTML = `
        <img src="${imageUrl}" alt="${product.productName}">
        <div class="product-info-details">
            <h4>${product.productName}</h4>
            <p>상품번호: ${product.productNo}</p>
            <p>브랜드: ${product.brandName || '-'}</p>
            <p>가격: ${(product.price || 0).toLocaleString()}원</p>
        </div>
    `;
}

// 세일가 자동 계산
function calculateSalePrice() {
    const salePercentage = parseFloat(document.getElementById('sale-percentage').value) || 0;
    const originalPrice = parseFloat(document.getElementById('original-price').value) || 0;

    const salePrice = originalPrice - (originalPrice * salePercentage / 100);
    document.getElementById('calculated-sale-price').textContent = Math.round(salePrice).toLocaleString();
}

// 추천 설정
async function setRecommended() {
    const productNo = document.getElementById('recommended-product-no').value;
    const priority = parseInt(document.getElementById('recommended-priority').value);

    try {
        const response = await fetch(`/admin/promotion/recommend/${productNo}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                isRecommended: true,
                priority: priority
            })
        });

        const data = await response.json();

        if (data.success) {
            alert('추천 상품으로 설정되었습니다.');
            closeModal('recommended-modal');
            loadRecommendedProducts();
            loadPromotionList();
        } else {
            alert(data.message || '추천 설정에 실패했습니다.');
        }
    } catch (error) {
        console.error('추천 설정 실패:', error);
        alert('추천 설정 중 오류가 발생했습니다.');
    }
}

// 추천 해제
async function removeRecommended() {
    const productNo = document.getElementById('recommended-product-no').value;

    if (!confirm('추천 상품에서 해제하시겠습니까?')) {
        return;
    }

    try {
        const response = await fetch(`/admin/promotion/recommend/${productNo}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                isRecommended: false
            })
        });

        const data = await response.json();

        if (data.success) {
            alert('추천 상품에서 해제되었습니다.');
            closeModal('recommended-modal');
            loadRecommendedProducts();
            loadPromotionList();
        } else {
            alert(data.message || '추천 해제에 실패했습니다.');
        }
    } catch (error) {
        console.error('추천 해제 실패:', error);
        alert('추천 해제 중 오류가 발생했습니다.');
    }
}

// 세일 설정
async function setSale() {
    const productNo = document.getElementById('sale-product-no').value;
    const salePercentage = parseInt(document.getElementById('sale-percentage').value);
    const originalPrice = parseInt(document.getElementById('original-price').value);
    const startDate = document.getElementById('sale-start-date').value;
    const endDate = document.getElementById('sale-end-date').value;

    if (!salePercentage || !originalPrice) {
        alert('할인율과 원가를 모두 입력해주세요.');
        return;
    }

    try {
        const requestBody = {
            isSale: true,
            salePercentage: salePercentage,
            originalPrice: originalPrice
        };

        // 날짜가 입력된 경우에만 추가
        if (startDate) requestBody.saleStartDate = startDate;
        if (endDate) requestBody.saleEndDate = endDate;

        const response = await fetch(`/admin/promotion/sale/${productNo}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(requestBody)
        });

        const data = await response.json();

        if (data.success) {
            alert('세일 상품으로 설정되었습니다.');
            closeModal('sale-modal');
            loadSaleProducts();
            loadPromotionList();
        } else {
            alert(data.message || '세일 설정에 실패했습니다.');
        }
    } catch (error) {
        console.error('세일 설정 실패:', error);
        alert('세일 설정 중 오류가 발생했습니다.');
    }
}

// 세일 해제
async function removeSale() {
    const productNo = document.getElementById('sale-product-no').value;

    if (!confirm('세일 상품에서 해제하시겠습니까?')) {
        return;
    }

    try {
        const response = await fetch(`/admin/promotion/sale/${productNo}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                isSale: false
            })
        });

        const data = await response.json();

        if (data.success) {
            alert('세일 상품에서 해제되었습니다.');
            closeModal('sale-modal');
            loadSaleProducts();
            loadPromotionList();
        } else {
            alert(data.message || '세일 해제에 실패했습니다.');
        }
    } catch (error) {
        console.error('세일 해제 실패:', error);
        alert('세일 해제 중 오류가 발생했습니다.');
    }
}

// 프로모션 직접 수정
function editPromotionDirect(productNo, type) {
    openPromotionModal(productNo, type);
}

// 프로모션 직접 해제
async function removePromotionDirect(productNo, type) {
    if (type === 'recommended') {
        document.getElementById('recommended-product-no').value = productNo;
        await removeRecommended();
    } else {
        document.getElementById('sale-product-no').value = productNo;
        await removeSale();
    }
}

// 프로모션 수정 (테이블에서)
async function editPromotion(productNo) {
    try {
        const response = await fetch(`/admin/promotion/${productNo}`);
        const data = await response.json();

        if (data.success && data.promotion) {
            const promotion = data.promotion;

            // 추천과 세일 중 어떤 것을 수정할지 선택
            const choice = prompt('수정할 프로모션을 선택하세요:\n1. 추천\n2. 세일\n번호를 입력하세요:');

            if (choice === '1' && promotion.isRecommended) {
                openPromotionModal(productNo, 'recommended');
            } else if (choice === '2' && promotion.isSale) {
                openPromotionModal(productNo, 'sale');
            } else {
                alert('잘못된 선택이거나 해당 프로모션이 설정되어 있지 않습니다.');
            }
        }
    } catch (error) {
        console.error('프로모션 정보 로드 실패:', error);
        alert('프로모션 정보를 불러오는 중 오류가 발생했습니다.');
    }
}

// 프로모션 삭제 (테이블에서)
async function deletePromotion(productNo) {
    if (!confirm('이 상품의 모든 프로모션을 삭제하시겠습니까?')) {
        return;
    }

    try {
        const response = await fetch(`/admin/promotion/${productNo}`, {
            method: 'DELETE'
        });

        const data = await response.json();

        if (data.success) {
            alert('프로모션이 삭제되었습니다.');
            loadPromotionList();
        } else {
            alert(data.message || '프로모션 삭제에 실패했습니다.');
        }
    } catch (error) {
        console.error('프로모션 삭제 실패:', error);
        alert('프로모션 삭제 중 오류가 발생했습니다.');
    }
}

// 만료된 세일 정리
async function cleanupExpiredSales() {
    if (!confirm('만료된 세일을 정리하시겠습니까?')) {
        return;
    }

    try {
        const response = await fetch('/admin/promotion/cleanup-expired', {
            method: 'POST'
        });

        const data = await response.json();

        if (data.success) {
            alert('만료된 세일이 정리되었습니다.');
            loadPromotionList();
            loadSaleProducts();
        } else {
            alert(data.message || '세일 정리에 실패했습니다.');
        }
    } catch (error) {
        console.error('세일 정리 실패:', error);
        alert('세일 정리 중 오류가 발생했습니다.');
    }
}

// 프로모션 필터링
function filterPromotions() {
    const filterType = document.getElementById('filter-type').value;

    let filteredPromotions = [];

    switch(filterType) {
        case 'all':
            filteredPromotions = [...currentPromotions];
            break;
        case 'recommended':
            filteredPromotions = currentPromotions.filter(p => p.promotion?.recommended);
            break;
        case 'sale':
            filteredPromotions = currentPromotions.filter(p => p.promotion?.sale);
            break;
        case 'both':
            filteredPromotions = currentPromotions.filter(p => p.promotion?.recommended && p.promotion?.sale);
            break;
    }

    // 임시로 currentPromotions 변경
    const originalPromotions = [...currentPromotions];
    currentPromotions = filteredPromotions;
    renderPromotionTable();
    currentPromotions = originalPromotions;
}

// 프로모션 목록 새로고침
function refreshPromotionList() {
    loadPromotionList();
}

// 모달 닫기
function closeModal(modalId) {
    document.getElementById(modalId).style.display = 'none';

    // 폼 초기화
    if (modalId === 'recommended-modal') {
        document.getElementById('recommended-form').reset();
        document.getElementById('recommended-priority').value = 50;
    } else if (modalId === 'sale-modal') {
        document.getElementById('sale-form').reset();
        document.getElementById('calculated-sale-price').textContent = '0';
    }
}

// 로딩 표시
function showLoading(containerId) {
    const container = document.getElementById(containerId);
    if (container) {
        if (containerId.includes('table')) {
            container.querySelector('tbody').innerHTML = '<tr><td colspan="11" class="loading">로딩 중...</td></tr>';
        } else {
            container.innerHTML = '<div class="loading">로딩 중...</div>';
        }
    }
}

// 에러 표시
function showError(message) {
    alert(message);
}

// 날짜 포맷팅
function formatDate(dateString) {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleDateString('ko-KR');
}

// 모달 외부 클릭 시 닫기
window.onclick = function(event) {
    const modals = document.querySelectorAll('.modal');
    modals.forEach(modal => {
        if (event.target === modal) {
            modal.style.display = 'none';
        }
    });
}