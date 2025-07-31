document.addEventListener('DOMContentLoaded', function() {
    loadRecommendedProducts();
});

function loadRecommendedProducts() {
    showLoadingState();

    fetch('/api/promotion/recommended')
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: 추천 상품을 불러올 수 없습니다.`);
            }
            return response.json();
        })
        .then(data => {
            if (data.success && data.products) {
                displayProductsByCategory(data.products);
                updateStats(data.products);
            } else {
                displayProductsByCategory([]);
            }
        })
        .catch(error => {
            displayProductsByCategory([]);
        })
        .finally(() => {
            hideLoadingState();
        });
}

function displayProductsByCategory(products) {
    const categories = {
        tops: products.filter(product => isTopCategory(product)),
        bottoms: products.filter(product => isBottomCategory(product)),
        shoes: products.filter(product => isShoeCategory(product))
    };

    displayCategoryProducts('tops', categories.tops);
    displayCategoryProducts('bottoms', categories.bottoms);
    displayCategoryProducts('shoes', categories.shoes);

    updateCategoryCount('top', categories.tops.length);
    updateCategoryCount('bottom', categories.bottoms.length);
    updateCategoryCount('shoes', categories.shoes.length);
}

function isTopCategory(product) {
    const topCategories = ['상의', '티셔츠', '셔츠', '블라우스', '니트', '후드', '재킷', '코트', '조끼', '탑', 'top'];
    const productName = product.productName ? product.productName.toLowerCase() : '';

    return topCategories.some(category =>
        product.mainCategory?.toLowerCase().includes(category.toLowerCase()) ||
        product.subCategory?.toLowerCase().includes(category.toLowerCase()) ||
        product.categoryName?.toLowerCase().includes(category.toLowerCase()) ||
        productName.includes('셔츠') || productName.includes('티셔츠') ||
        productName.includes('블라우스') || productName.includes('니트') ||
        productName.includes('후드') || productName.includes('재킷')
    );
}

function isBottomCategory(product) {
    const bottomCategories = ['하의', '바지', '팬츠', '청바지', '슬랙스', '반바지', '치마', '스커트', '레깅스', '팬츠', 'bottom'];
    const productName = product.productName ? product.productName.toLowerCase() : '';

    return bottomCategories.some(category =>
        product.mainCategory?.toLowerCase().includes(category.toLowerCase()) ||
        product.subCategory?.toLowerCase().includes(category.toLowerCase()) ||
        product.categoryName?.toLowerCase().includes(category.toLowerCase()) ||
        productName.includes('바지') || productName.includes('팬츠') ||
        productName.includes('치마') || productName.includes('스커트') ||
        productName.includes('레깅스') || productName.includes('청바지')
    );
}

function isShoeCategory(product) {
    const shoeCategories = ['신발', '운동화', '구두', '부츠', '샌들', '슬리퍼', '하이힐', '스니커즈', 'shoes'];
    const productName = product.productName ? product.productName.toLowerCase() : '';

    return shoeCategories.some(category =>
        product.mainCategory?.toLowerCase().includes(category.toLowerCase()) ||
        product.subCategory?.toLowerCase().includes(category.toLowerCase()) ||
        product.categoryName?.toLowerCase().includes(category.toLowerCase()) ||
        productName.includes('신발') || productName.includes('운동화') ||
        productName.includes('구두') || productName.includes('부츠') ||
        productName.includes('샌들') || productName.includes('스니커즈')
    );
}

function displayCategoryProducts(categoryType, products) {
    const gridId = categoryType === 'tops' ? 'tops-grid' :
        categoryType === 'bottoms' ? 'bottoms-grid' : 'shoes-grid';

    const grid = document.getElementById(gridId);

    if (!grid) {
        return;
    }

    grid.innerHTML = '';

    if (products.length === 0) {
        grid.innerHTML = `
            <div class="no-category-products">
                <p>해당 카테고리의 추천 상품이 없습니다.</p>
            </div>
        `;
        return;
    }

    products.forEach((product, index) => {
        try {
            const productCard = createHorizontalProductCard(product);
            grid.appendChild(productCard);
        } catch (error) {}
    });
}

function createHorizontalProductCard(product) {
    const card = document.createElement('div');
    card.className = 'product-card-horizontal';
    card.setAttribute('data-product-no', product.productNo);

    const imageUrl = (product.imageUrls && product.imageUrls.length > 0)
        ? product.imageUrls[0]
        : '/images/no-image.png';

    const formattedPrice = new Intl.NumberFormat('ko-KR').format(product.price);

    card.innerHTML = `
        ${product.isRecommended ? '<div class="product-badge recommended">추천</div>' : ''}
        ${product.isSale ? '<div class="discount-info">-' + (product.salePercentage || 0) + '%</div>' : ''}
        
        <img src="${imageUrl}" 
             alt="${product.productName}" 
             class="product-image"
             onerror="this.src='/images/no-image.png'">
        
        <div class="product-info">
            <div class="product-brand">${product.brandName || '브랜드명'}</div>
            <div class="product-name">${product.productName}</div>
            
            ${product.isSale && product.salePrice ? `
                <div class="card-price sale-price">
                    <div class="original-price">${formattedPrice}원</div>
                    <div class="current-price">${new Intl.NumberFormat('ko-KR').format(product.salePrice)}원</div>
                </div>
            ` : `
                <div class="product-price">${formattedPrice}원</div>
            `}
            
            <div class="product-stats">
                <div class="stat-item">
                    <span class="heart-icon">♥</span>
                    <span>${product.likeCount || 0}</span>
                </div>
                <div class="stat-item">
                    <span class="star-icon">★</span>
                    <span>${product.reviewCount || 0}</span>
                </div>
            </div>
        </div>
    `;

    card.addEventListener('click', function() {
        goToProduct(product.productNo);
    });

    return card;
}

function createViewMoreButton(categoryType) {
    const button = document.createElement('a');
    button.className = 'view-more-btn';
    button.href = `/products/category/${categoryType}`;
    button.innerHTML = `
        <div>
            <div style="font-size: 2rem; margin-bottom: 0.5rem;">→</div>
            <div>더보기</div>
        </div>
    `;
    return button;
}

function updateCategoryCount(categoryType, count) {
    const countElement = document.getElementById(`${categoryType}-count`);
    if (countElement) {
        countElement.textContent = count;
    }
}

function updateStats(products) {
    const recommendedCountElement = document.getElementById('recommended-count');
    if (recommendedCountElement) {
        recommendedCountElement.textContent = products.length;
    }
}

function showLoadingState() {
    const loadingState = document.getElementById('loading-state');
    if (loadingState) {
        loadingState.style.display = 'block';
    }
}

function hideLoadingState() {
    const loadingState = document.getElementById('loading-state');
    if (loadingState) {
        loadingState.style.display = 'none';
    }
}

function goToProduct(productNo) {
    window.location.href = `/products/${productNo}`;
}

document.querySelectorAll('.horizontal-grid').forEach(grid => {
    let isDown = false;
    let startX;
    let scrollLeft;

    grid.addEventListener('mousedown', (e) => {
        isDown = true;
        startX = e.pageX - grid.offsetLeft;
        scrollLeft = grid.scrollLeft;
    });

    grid.addEventListener('mouseleave', () => {
        isDown = false;
    });

    grid.addEventListener('mouseup', () => {
        isDown = false;
    });

    grid.addEventListener('mousemove', (e) => {
        if (!isDown) return;
        e.preventDefault();
        const x = e.pageX - grid.offsetLeft;
        const walk = (x - startX) * 2;
        grid.scrollLeft = scrollLeft - walk;
    });
});
