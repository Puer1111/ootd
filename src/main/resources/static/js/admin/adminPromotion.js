// 전역 변수
let allProducts = [];
let filteredProducts = [];
let recommendedProducts = new Set();
let saleProducts = new Map(); // productNo -> { percentage, price, ... }

// 🆕 페이지네이션 변수 추가
let currentPage = 1;
const itemsPerPage = 10;
let totalPages = 1;

document.addEventListener('DOMContentLoaded', function() {
    loadAllProducts();
    setupEventListeners();
});

// 이벤트 리스너 설정
function setupEventListeners() {
    // 탭 전환
    document.querySelectorAll('.tab-btn').forEach(btn => {
        btn.addEventListener('click', function() {
            const tabName = this.dataset.tab;
            switchTab(tabName);
        });
    });

    // 필터 변경 시
    document.getElementById('filter-type-recommend').addEventListener('change', filterRecommendProducts);
    document.getElementById('filter-type-sale').addEventListener('change', filterSaleProducts);

    // 엔터키로 사용자 정의 세일 적용
    document.addEventListener('keypress', function(e) {
        if (e.target.classList.contains('sale-percentage-input') && e.key === 'Enter') {
            const input = e.target;
            const productNo = input.id.replace('customSale', '');

            // 해당 상품의 원가 찾기
            const product = allProducts.find(p => p.productNo == productNo);
            if (product && input.value) {
                applyCustomSale(parseInt(productNo), input.value, product.price || 0);
            }
        }
    });
}

// 🆕 탭 전환 함수 (페이지 초기화 포함)
function switchTab(tabName) {
    // 탭 버튼 활성화 상태 변경
    document.querySelectorAll('.tab-btn').forEach(btn => {
        btn.classList.remove('active');
    });
    document.querySelector(`[data-tab="${tabName}"]`).classList.add('active');

    // 탭 콘텐츠 표시/숨김
    document.querySelectorAll('.tab-content').forEach(content => {
        content.classList.remove('active');
    });
    document.getElementById(`${tabName}-tab`).classList.add('active');

    // 페이지 초기화
    currentPage = 1;

    // 탭별 데이터 로드
    if (tabName === 'sale') {
        loadSaleData();
    } else if (tabName === 'recommendation') {
        renderRecommendTable();
    }
}

// 모든 상품 로드
async function loadAllProducts() {
    try {
        showLoading('recommend');

        // 모든 상품 정보와 추천 상품 정보를 동시에 가져오기
        const [productsResponse, recommendedResponse] = await Promise.all([
            fetch('/admin/select/product'),
            fetch('/admin/promotion/recommended')
        ]);

        const products = await productsResponse.json();
        const recommendedData = await recommendedResponse.json();

        if (products && Array.isArray(products)) {
            allProducts = products;

            // 추천 상품 정보 저장
            if (recommendedData.success) {
                recommendedProducts = new Set(
                    recommendedData.promotions.map(promo => promo.productNo)
                );
            }

            filteredProducts = [...allProducts];
            renderRecommendTable();
        } else {
            showError('상품 목록을 불러올 수 없습니다.');
        }
    } catch (error) {
        console.error('상품 목록 로드 실패:', error);
        showError('상품 목록을 불러오는 중 오류가 발생했습니다.');
    }
}

// 세일 데이터 로드
async function loadSaleData() {
    try {
        showLoading('sale');

        const [productsResponse, saleResponse] = await Promise.all([
            fetch('/admin/select/product'),
            fetch('/admin/promotion/sale')
        ]);

        const products = await productsResponse.json();
        const saleData = await saleResponse.json();

        if (products && Array.isArray(products)) {
            allProducts = products;

            // 세일 상품 정보 저장
            saleProducts.clear();
            if (saleData.success) {
                saleData.promotions.forEach(promo => {
                    saleProducts.set(promo.productNo, {
                        percentage: promo.salePercentage,
                        originalPrice: promo.originalPrice,
                        salePrice: promo.salePrice,
                        isActiveSale: promo.isActiveSale
                    });
                });
            }

            renderSaleTable();
        } else {
            showError('상품 목록을 불러올 수 없습니다.');
        }
    } catch (error) {
        console.error('세일 데이터 로드 실패:', error);
        showError('세일 데이터를 불러오는 중 오류가 발생했습니다.');
    }
}

// 🆕 추천 상품 테이블 렌더링 (페이지네이션 포함)
function renderRecommendTable() {
    const tbody = document.querySelector('#recommend-table tbody');

    if (filteredProducts.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" class="empty-state">표시할 상품이 없습니다.</td></tr>';
        renderPagination('recommend', 0);
        return;
    }

    // 페이지네이션 계산
    totalPages = Math.ceil(filteredProducts.length / itemsPerPage);
    const startIndex = (currentPage - 1) * itemsPerPage;
    const endIndex = startIndex + itemsPerPage;
    const currentPageProducts = filteredProducts.slice(startIndex, endIndex);

    let html = '';
    currentPageProducts.forEach(product => {
        const isRecommended = recommendedProducts.has(product.productNo);
        const recommendStatus = isRecommended ?
            '<span class="status-badge recommended">추천</span>' :
            '<span class="status-badge not-recommended">미추천</span>';

        const buttonText = isRecommended ? '추천 해제' : '추천';
        const buttonClass = isRecommended ? 'btn-danger' : 'btn-primary';

        html += `
            <tr>
                <td>${product.productNo}</td>
                <td class="product-name" title="${product.productName}">${product.productName}</td>
                <td>${product.brandName || '-'}</td>
                <td>${recommendStatus}</td>
                <td>
                    <button class="btn btn-sm ${buttonClass}" 
                            onclick="toggleRecommendation(${product.productNo}, ${isRecommended})"
                            data-product-no="${product.productNo}">
                        ${buttonText}
                    </button>
                </td>
            </tr>
        `;
    });

    tbody.innerHTML = html;
    renderPagination('recommend', filteredProducts.length);
}

// 🆕 세일 상품 테이블 렌더링 (25, 50, 75 버튼 제거 + 페이지네이션)
function renderSaleTable() {
    const tbody = document.querySelector('#sale-table tbody');

    if (allProducts.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" class="empty-state">표시할 상품이 없습니다.</td></tr>';
        renderPagination('sale', 0);
        return;
    }

    // 페이지네이션 계산
    totalPages = Math.ceil(allProducts.length / itemsPerPage);
    const startIndex = (currentPage - 1) * itemsPerPage;
    const endIndex = startIndex + itemsPerPage;
    const currentPageProducts = allProducts.slice(startIndex, endIndex);

    let html = '';
    currentPageProducts.forEach(product => {
        const saleInfo = saleProducts.get(product.productNo);
        const isOnSale = saleInfo && saleInfo.isActiveSale;

        // 세일 상태 표시
        let saleStatus = '';
        if (isOnSale) {
            saleStatus = `<span class="status-badge on-sale">${saleInfo.percentage}% 세일</span>`;
        } else {
            saleStatus = '<span class="status-badge not-on-sale">세일 안함</span>';
        }

        // 가격 표시
        let priceDisplay = `<span class="price-display">${(product.price || 0).toLocaleString()}원</span>`;
        if (isOnSale && saleInfo.salePrice) {
            priceDisplay = `
                <div class="price-display">
                    <div class="original-price">${(saleInfo.originalPrice || product.price).toLocaleString()}원</div>
                    <div class="sale-price">${saleInfo.salePrice.toLocaleString()}원</div>
                </div>
            `;
        }

        html += `
            <tr>
                <td>${product.productNo}</td>
                <td class="product-name" title="${product.productName}">${product.productName}</td>
                <td>${product.brandName || '-'}</td>
                <td>${priceDisplay}</td>
                <td>${saleStatus}</td>
                <td>
                    <div class="sale-btn-group">
                        <!-- 사용자 정의 퍼센트 입력 필드만 유지 -->
                        <div class="sale-input-group">
                            <input type="number" 
                                   class="sale-percentage-input" 
                                   placeholder="%" 
                                   min="1" 
                                   max="99"
                                   id="customSale${product.productNo}">
                            <button class="sale-apply-btn" 
                                    id="customSaleApply${product.productNo}"
                                    onclick="applyCustomSale(${product.productNo}, document.getElementById('customSale${product.productNo}').value, ${product.price || 0})">
                                적용
                            </button>
                        </div>
                        
                        <!-- 세일 해제 버튼만 유지 -->
                        <button class="sale-off-btn" 
                                onclick="removeSale(${product.productNo})">
                            세일 해제
                        </button>
                    </div>
                </td>
            </tr>
        `;
    });

    tbody.innerHTML = html;
    renderPagination('sale', allProducts.length);
}

// 🆕 페이지네이션 렌더링 함수
function renderPagination(tableType, totalItems) {
    const existingPagination = document.querySelector(`#${tableType}-pagination`);
    if (existingPagination) {
        existingPagination.remove();
    }

    if (totalItems === 0) return;

    totalPages = Math.ceil(totalItems / itemsPerPage);

    if (totalPages <= 1) return; // 페이지가 1개 이하면 페이지네이션 표시 안함

    const table = document.querySelector(`#${tableType}-table`);
    const paginationDiv = document.createElement('div');
    paginationDiv.id = `${tableType}-pagination`;
    paginationDiv.className = 'pagination';

    let paginationHtml = '';

    // 이전 버튼
    paginationHtml += `
        <button class="pagination-btn" ${currentPage === 1 ? 'disabled' : ''} 
                onclick="changePage(${currentPage - 1}, '${tableType}')">
            이전
        </button>
    `;

    // 페이지 번호들
    const startPage = Math.max(1, currentPage - 2);
    const endPage = Math.min(totalPages, currentPage + 2);

    if (startPage > 1) {
        paginationHtml += `<button class="pagination-btn" onclick="changePage(1, '${tableType}')">1</button>`;
        if (startPage > 2) {
            paginationHtml += `<span class="pagination-info">...</span>`;
        }
    }

    for (let i = startPage; i <= endPage; i++) {
        paginationHtml += `
            <button class="pagination-btn ${i === currentPage ? 'active' : ''}" 
                    onclick="changePage(${i}, '${tableType}')">
                ${i}
            </button>
        `;
    }

    if (endPage < totalPages) {
        if (endPage < totalPages - 1) {
            paginationHtml += `<span class="pagination-info">...</span>`;
        }
        paginationHtml += `<button class="pagination-btn" onclick="changePage(${totalPages}, '${tableType}')">${totalPages}</button>`;
    }

    // 다음 버튼
    paginationHtml += `
        <button class="pagination-btn" ${currentPage === totalPages ? 'disabled' : ''} 
                onclick="changePage(${currentPage + 1}, '${tableType}')">
            다음
        </button>
    `;

    // 정보 표시
    const startItem = (currentPage - 1) * itemsPerPage + 1;
    const endItem = Math.min(currentPage * itemsPerPage, totalItems);
    paginationHtml += `<span class="pagination-info">${startItem}-${endItem} / ${totalItems}개</span>`;

    paginationDiv.innerHTML = paginationHtml;
    table.parentNode.insertBefore(paginationDiv, table.nextSibling);
}

// 🆕 페이지 변경 함수
function changePage(newPage, tableType) {
    if (newPage < 1 || newPage > totalPages) return;

    currentPage = newPage;

    if (tableType === 'recommend') {
        renderRecommendTable();
    } else if (tableType === 'sale') {
        renderSaleTable();
    }
}

// 추천 상태 토글
async function toggleRecommendation(productNo, isCurrentlyRecommended) {
    try {
        const button = document.querySelector(`button[data-product-no="${productNo}"]`);
        button.disabled = true;
        button.textContent = '처리 중...';

        const response = await fetch(`/admin/promotion/recommend/${productNo}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                isRecommended: !isCurrentlyRecommended,
                priority: isCurrentlyRecommended ? 0 : 50
            })
        });

        const data = await response.json();

        if (data.success) {
            if (isCurrentlyRecommended) {
                recommendedProducts.delete(productNo);
            } else {
                recommendedProducts.add(productNo);
            }

            renderRecommendTable();
            showSuccess(data.message);
        } else {
            showError(data.message || '추천 설정 중 오류가 발생했습니다.');
            button.disabled = false;
        }
    } catch (error) {
        console.error('추천 설정 실패:', error);
        showError('추천 설정 중 오류가 발생했습니다.');
    }
}

// 사용자 정의 세일 퍼센트 적용 함수
async function applyCustomSale(productNo, percentage, originalPrice) {
    // 입력값 검증
    const salePercentage = parseInt(percentage);

    if (!salePercentage || salePercentage < 1 || salePercentage > 99) {
        showError('1~99 사이의 숫자를 입력해주세요.');
        return;
    }

    try {
        console.log(`사용자 정의 세일 적용: ${productNo}번 상품에 ${salePercentage}% 세일`);

        // 적용 버튼 로딩 상태로 변경
        const applyBtn = document.querySelector(`#customSaleApply${productNo}`);
        if (applyBtn) {
            applyBtn.disabled = true;
            applyBtn.textContent = '적용중...';
        }

        // 기존 setSalePercentage 함수 호출
        await setSalePercentage(productNo, salePercentage, originalPrice);

        // 입력 필드 초기화
        const input = document.getElementById(`customSale${productNo}`);
        if (input) {
            input.value = '';
        }

        // 버튼 상태 복원
        if (applyBtn) {
            applyBtn.disabled = false;
            applyBtn.textContent = '적용';
        }

        showSuccess(`${salePercentage}% 세일이 적용되었습니다.`);

    } catch (error) {
        console.error('사용자 정의 세일 적용 실패:', error);
        showError('세일 적용 중 오류가 발생했습니다.');

        // 버튼 상태 복원
        const applyBtn = document.querySelector(`#customSaleApply${productNo}`);
        if (applyBtn) {
            applyBtn.disabled = false;
            applyBtn.textContent = '적용';
        }
    }
}

// 세일 퍼센티지 설정
async function setSalePercentage(productNo, percentage, originalPrice) {
    try {
        showButtonLoading(productNo, '세일 설정 중...');

        const response = await fetch(`/admin/promotion/sale/${productNo}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                isSale: true,
                salePercentage: percentage,
                originalPrice: originalPrice
            })
        });

        const data = await response.json();

        if (data.success) {
            // 세일 정보 업데이트
            const salePrice = originalPrice - (originalPrice * percentage / 100);
            saleProducts.set(productNo, {
                percentage: percentage,
                originalPrice: originalPrice,
                salePrice: salePrice,
                isActiveSale: true
            });

            renderSaleTable();
            showSuccess(`${percentage}% 세일이 적용되었습니다.`);
        } else {
            showError(data.message || '세일 설정 중 오류가 발생했습니다.');
        }
    } catch (error) {
        console.error('세일 설정 실패:', error);
        showError('세일 설정 중 오류가 발생했습니다.');
    }
}

// 세일 해제
async function removeSale(productNo) {
    try {
        showButtonLoading(productNo, '세일 해제 중...');

        const response = await fetch(`/admin/promotion/sale/${productNo}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                isSale: false,
                salePercentage: null,
                originalPrice: null
            })
        });

        const data = await response.json();

        if (data.success) {
            saleProducts.delete(productNo);
            renderSaleTable();
            showSuccess('세일이 해제되었습니다.');
        } else {
            showError(data.message || '세일 해제 중 오류가 발생했습니다.');
        }
    } catch (error) {
        console.error('세일 해제 실패:', error);
        showError('세일 해제 중 오류가 발생했습니다.');
    }
}

// 만료된 세일 정리
async function cleanupExpiredSales() {
    try {
        const response = await fetch('/admin/promotion/cleanup-expired', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        const data = await response.json();

        if (data.success) {
            loadSaleData(); // 데이터 새로고침
            showSuccess('만료된 세일이 정리되었습니다.');
        } else {
            showError(data.message || '세일 정리 중 오류가 발생했습니다.');
        }
    } catch (error) {
        console.error('세일 정리 실패:', error);
        showError('세일 정리 중 오류가 발생했습니다.');
    }
}

// 추천 상품 필터링
function filterRecommendProducts() {
    const filterType = document.getElementById('filter-type-recommend').value;

    switch(filterType) {
        case 'all':
            filteredProducts = [...allProducts];
            break;
        case 'recommended':
            filteredProducts = allProducts.filter(product =>
                recommendedProducts.has(product.productNo)
            );
            break;
        case 'not-recommended':
            filteredProducts = allProducts.filter(product =>
                !recommendedProducts.has(product.productNo)
            );
            break;
    }

    currentPage = 1; // 필터 변경 시 첫 페이지로
    renderRecommendTable();
}

// 세일 상품 필터링
function filterSaleProducts() {
    const filterType = document.getElementById('filter-type-sale').value;
    let filteredSaleProducts = [];

    switch(filterType) {
        case 'all':
            filteredSaleProducts = [...allProducts];
            break;
        case 'on-sale':
            filteredSaleProducts = allProducts.filter(product => {
                const saleInfo = saleProducts.get(product.productNo);
                return saleInfo && saleInfo.isActiveSale;
            });
            break;
        case 'not-on-sale':
            filteredSaleProducts = allProducts.filter(product => {
                const saleInfo = saleProducts.get(product.productNo);
                return !saleInfo || !saleInfo.isActiveSale;
            });
            break;
    }

    // 임시로 allProducts를 교체해서 렌더링
    const originalProducts = [...allProducts];
    allProducts = filteredSaleProducts;
    currentPage = 1; // 필터 변경 시 첫 페이지로
    renderSaleTable();
    allProducts = originalProducts;
}

// 상품 목록 새로고침
function refreshProductList() {
    currentPage = 1;
    loadAllProducts();
}

// 세일 상품 목록 새로고침
function refreshSaleProductList() {
    currentPage = 1;
    loadSaleData();
}

// 버튼 로딩 상태 표시
function showButtonLoading(productNo, message) {
    const buttons = document.querySelectorAll(`tr:has([onclick*="${productNo}"]) button`);
    buttons.forEach(btn => {
        btn.disabled = true;
        if (btn.textContent !== message) {
            btn.dataset.originalText = btn.textContent;
            btn.textContent = message;
        }
    });
}

// 로딩 표시
function showLoading(tableType) {
    const tbody = document.querySelector(`#${tableType}-table tbody`);
    const colspan = tableType === 'recommend' ? 5 : 6;
    tbody.innerHTML = `<tr><td colspan="${colspan}" class="loading">상품 목록을 불러오는 중...</td></tr>`;
}

// 에러 표시
function showError(message) {
    alert('❌ ' + message);
}

// 성공 메시지 표시
function showSuccess(message) {
    const toast = document.createElement('div');
    toast.className = 'toast toast-success';
    toast.textContent = '✅ ' + message;
    toast.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        background: #4CAF50;
        color: white;
        padding: 12px 20px;
        border-radius: 6px;
        box-shadow: 0 4px 12px rgba(0,0,0,0.15);
        z-index: 1000;
        font-weight: 500;
    `;

    document.body.appendChild(toast);

    setTimeout(() => {
        toast.remove();
    }, 3000);
}