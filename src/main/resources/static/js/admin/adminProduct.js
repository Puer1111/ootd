const sections = {
    'product-list-section': document.getElementById('product-list-section'),
    'product-register-section': document.getElementById('product-register-section')
};

const showSection = (sectionId) => {
    Object.values(sections).forEach(section => {
        if (section) section.style.display = 'none';
    });
    if (sections[sectionId]) {
        sections[sectionId].style.display = 'block';
    }
};

// --- 사이드바 메뉴 이벤트 ---
const productManagementToggle = document.getElementById('product-management-toggle');
const productSubmenu = document.getElementById('product-submenu');

if (productManagementToggle && productSubmenu) {
    // Get all clickable elements in the sidebar for deactivation
    const allSidebarClickables = document.querySelectorAll('.admin-sidebar a, .admin-sidebar span');

    const deactivateAllActiveStates = () => {
        allSidebarClickables.forEach(el => {
            el.classList.remove('active');
        });
        // Also ensure all submenus are hidden, except the one being toggled if it's a parent
        document.querySelectorAll('.admin-sidebar .submenu').forEach(submenu => {
            submenu.classList.remove('active');
        });
    };

    productManagementToggle.addEventListener('click', (event) => {
        event.preventDefault();
        const clickedElement = event.target;

        // Check if the clicked element is the span itself or a child of the li (but not an 'a' tag)
        const isToggleSpan = clickedElement.tagName === 'SPAN' && clickedElement.closest('#product-management-toggle');
        const isSubmenuLink = clickedElement.tagName === 'A' && clickedElement.closest('#product-submenu');

        if (isToggleSpan) {
            // If the span (toggle) is clicked
            const isActive = productSubmenu.classList.contains('active');
            deactivateAllActiveStates(); // Deactivate all others first
            productSubmenu.classList.toggle('active', !isActive); // Toggle submenu visibility
            clickedElement.classList.toggle('active', !isActive); // Toggle active state of the span
        } else if (isSubmenuLink) {
            // If a submenu link is clicked
            deactivateAllActiveStates(); // Deactivate all others first
            clickedElement.classList.add('active'); // Activate the clicked link
            productManagementToggle.querySelector('span').classList.add('active'); // Activate the parent span
            productSubmenu.classList.add('active'); // Ensure submenu stays open

            const targetSectionId = clickedElement.getAttribute('data-target-section');
            showSection(targetSectionId);

            if (targetSectionId === 'product-register-section') {
                resetForm(); // Reset form when navigating to register section
            }
        }
        // If clicked outside the span or a link, do nothing (e.g., clicking on the li padding)
    });
}

// --- 초기화 ---
loadProducts();
showSection('product-list-section'); // 초기 화면은 조회/관리

// --- API 및 유틸리티 초기화 ---
initializeApiAndUtils();

// --- 폼 제출 이벤트 ---
document.getElementById('submit-btn').addEventListener('click', function (e) {
    e.preventDefault();
    submitFormWithAjax();
});

// --- 세일 설정 이벤트 리스너 ---
document.addEventListener('DOMContentLoaded', function() {
    initializeSaleEvents();
});

let allProductsData = []; // 모든 상품 데이터를 저장할 전역 변수

// --- 상품 목록 로드 (세일 정보 포함) ---
function loadProducts() {
    fetch('/admin/select/product')
        .then(response => response.json())
        .then(products => {
            allProductsData = products; // 데이터 저장
            const tableBody = document.getElementById('product-table').querySelector('tbody');
            tableBody.innerHTML = ''; // 기존 내용을 비웁니다.

            products.forEach(product => {
                // 1. 메인 상품 정보 행 생성
                const productRow = document.createElement('tr');
                productRow.classList.add('product-row');

                // 세일 정보 표시
                let saleInfo = '';
                if (product.isActiveSale && product.salePercentage) {
                    saleInfo = `<span class="sale-badge">${product.salePercentage}% OFF</span>`;
                }

                productRow.innerHTML = `
                    <td>
                        <button class="toggle-options-btn">▼</button>
                    </td>
                    <td>${product.productNo}</td>
                    <td><img src="${product.imageUrls && product.imageUrls.length > 0 ? product.imageUrls[0] : '/img/common/default.png'}" alt="${product.productName}" width="50"></td>
                    <td>${product.productName}</td>
                    <td>${product.brandName}</td>
                    <td>${product.subCategory}</td>
                    <td>${product.price.toLocaleString()}원</td>
                    <td>${saleInfo}</td>
                    <td>
                        <button class="btn-edit" data-id="${product.productNo}">수정</button>
                        <button class="btn-delete" data-id="${product.productNo}">삭제</button>
                    </td>
                `;
                tableBody.appendChild(productRow);

                // 2. 상세 옵션 정보를 담을 행 생성 (기본적으로 숨김)
                const optionsRow = document.createElement('tr');
                optionsRow.classList.add('options-row');
                optionsRow.style.display = 'none'; // 기본적으로 숨김

                // 상세 옵션 테이블 HTML 생성
                let optionsTableHtml = `
                    <td colspan="9">
                        <table class="options-table">
                            <thead>
                                <tr>
                                    <th>색상</th>
                                    <th>사이즈</th>
                                    <th>재고</th>
                                    <th>상태</th>
                                </tr>
                            </thead>
                            <tbody>
                `;
                (product.options || []).forEach(option => {
                    optionsTableHtml += `
                        <tr>
                            <td>${option?.colorName ?? 'N/A'}</td>
                            <td>${option?.size ?? 'N/A'}</td>
                            <td>${option?.inventory ?? 0}</td>
                            <td>${option?.status ?? 'N/A'}</td>
                        </tr>
                    `;
                });
                optionsTableHtml += '</tbody></table></td>';
                optionsRow.innerHTML = optionsTableHtml;
                tableBody.appendChild(optionsRow);
            });

            // 이벤트 리스너 등록
            addEventListeners();
        })
        .catch(error => console.error('Error loading products:', error));
}

function addEventListeners() {
    // 펼치기/접기 버튼 이벤트
    document.querySelectorAll('.toggle-options-btn').forEach(button => {
        button.addEventListener('click', event => {
            const btn = event.currentTarget;
            const productRow = btn.closest('tr');
            const optionsRow = productRow.nextElementSibling;

            if (optionsRow && optionsRow.classList.contains('options-row')) {
                const isVisible = optionsRow.style.display !== 'none';
                optionsRow.style.display = isVisible ? 'none' : 'table-row';
                btn.textContent = isVisible ? '▼' : '▲';
            }
        });
    });

    // 수정 및 삭제 버튼 이벤트
    document.querySelectorAll('.btn-edit').forEach(button => {
        button.addEventListener('click', handleEdit);
    });
    document.querySelectorAll('.btn-delete').forEach(button => {
        button.addEventListener('click', handleDelete);
    });
}

// --- 상품 삭제 처리 ---
function handleDelete(event) {
    const productId = event.target.dataset.id;
    if (confirm(`정말로 상품 ID ${productId}를 삭제하시겠습니까?`)) {
        fetch(`/admin/delete/products/${productId}`, {method: 'DELETE'})
            .then(response => {
                if (response.ok) {
                    alert('상품이 삭제되었습니다.');
                    loadProducts(); // 목록 새로고침
                } else {
                    throw new Error('상품 삭제에 실패했습니다.');
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert(error.message);
            });
    }
}

// --- 상품 수정 처리 (세일 정보 포함) ---
function handleEdit(event) {
    const productNo = event.target.dataset.id;
    const productData = allProductsData.find(p => p.productNo == productNo);

    if (!productData) {
        alert('상품 정보를 찾을 수 없습니다.');
        return;
    }

    // 폼 초기화
    resetForm();

    // 폼에 기본 데이터 채우기
    document.getElementById('productNo').value = productData.productNo;
    document.getElementById('productName').value = productData.productName;
    document.getElementById('description').value = productData.description;

    // 세일 정보 설정
    if (productData.isActiveSale) {
        document.getElementById('isSale').checked = true;
        document.querySelector('.sale-percentage').style.display = 'block';
        if (productData.salePercentage) {
            document.getElementById('salePercentage').value = productData.salePercentage;
            updateSalePreview(); // 미리보기 업데이트
        }
    }

    // 브랜드 및 카테고리 설정 (비동기적으로 로드될 수 있으므로 약간의 지연 후 설정)
    setTimeout(() => {
        const brandSelect = document.getElementById('brand-select');
        if (Array.from(brandSelect.options).some(opt => opt.text === productData.brandName)) {
            // 옵션의 텍스트 값으로 찾기
            const brandOption = Array.from(brandSelect.options).find(opt => opt.text === productData.brandName);
            if(brandOption) brandSelect.value = brandOption.value;
        }

        // 카테고리 설정 로직 (구현 필요)
        // 예: document.getElementById('categoryChoiceSecond').value = productData.categoryNo;
    }, 500); // 0.5초 지연

    // 이미지 미리보기 채우기
    const previewArea = document.getElementById('preview-area');
    previewArea.innerHTML = '';
    if (productData.imageUrls && productData.imageUrls.length > 0) {
        productData.imageUrls.forEach(url => {
            const img = document.createElement('img');
            img.src = url;
            previewArea.appendChild(img);
        });
    }

    // 옵션 정보 채우기
    const sizesContainer = document.getElementById('sizesContainer');
    sizesContainer.innerHTML = '';
    productData.options.forEach(option => {
        const sizeItem = document.createElement('div');
        sizeItem.classList.add('size-item');
        sizeItem.innerHTML = `
            <div><input type="text" name="productOption.size[]" value="${option.size}" ></div>
            <div><input type="number" name="product.price[]" value="${productData.price}"></div>
            <div><input type="text" name="productOption.colorName[]" value="${option.colorName}" ></div>
            <div><input type="number" name="productOption.inventory[]" value="${option.inventory}"></div>
            <div><input type="text" name="productOption.status[]" value="${option.status}"></div>
            <button type="button" class="size-remove-btn">-</button>
        `;
        sizesContainer.appendChild(sizeItem);
    });

    // UI 변경
    document.getElementById('form-title').textContent = '상품 수정';
    document.getElementById('submit-btn').textContent = '수정하기';
    showSection('product-register-section');
}

// --- 폼 초기화 ---
function resetForm() {
    const form = document.getElementById('product-form');
    form.reset();
    document.getElementById('productNo').value = '';
    document.getElementById('form-title').textContent = '상품 등록';
    document.getElementById('submit-btn').textContent = '등록하기';
    document.getElementById('preview-area').innerHTML = '';
    document.getElementById('sizesContainer').innerHTML = '';

    // 세일 설정 초기화
    document.getElementById('isSale').checked = false;
    document.querySelector('.sale-percentage').style.display = 'none';
    document.getElementById('salePercentage').value = '';
    document.getElementById('salePreview').style.display = 'none';
}

// --- 폼 제출 (등록/수정) - 세일 정보 포함 ---
function submitFormWithAjax() {
    const form = document.getElementById('product-form');
    const formData = new FormData(form);
    const productId = document.getElementById('productNo').value;

    // 세일 정보 추가
    const isSale = document.getElementById('isSale').checked;
    const salePercentage = document.getElementById('salePercentage').value;

    if (isSale && salePercentage) {
        formData.append('isActiveSale', 'true');
        formData.append('salePercentage', salePercentage);
    } else {
        formData.append('isActiveSale', 'false');
    }

    const url = productId ? `/admin/update/products/${productId}` : '/admin/insert/products';
    const method = productId ? 'PUT' : 'POST';

    fetch(url, {
        method: method,
        body: formData
    })
        .then(response => {
            if (response.ok) {
                return response.json();
            }
            throw new Error('Network response was not ok');
        })
        .then(data => {
            console.log('Success:', data);
            alert(`상품이 성공적으로 ${productId ? '수정' : '등록'}되었습니다.`);
            resetForm();
            loadProducts();
            showSection('product-list-section');
        })
        .catch(error => {
            console.error('Error:', error);
            alert(`처리 중 오류가 발생했습니다.`);
        });
}

// --- 세일 설정 초기화 ---
function initializeSaleEvents() {
    const isSaleCheckbox = document.getElementById('isSale');
    const salePercentageDiv = document.querySelector('.sale-percentage');
    const salePercentageInput = document.getElementById('salePercentage');

    // 세일 체크박스 변경 이벤트
    isSaleCheckbox.addEventListener('change', function() {
        if (this.checked) {
            salePercentageDiv.style.display = 'block';
        } else {
            salePercentageDiv.style.display = 'none';
            document.getElementById('salePreview').style.display = 'none';
            salePercentageInput.value = '';
        }
    });

    // 세일 퍼센티지 입력 이벤트
    salePercentageInput.addEventListener('input', function() {
        const percentage = parseInt(this.value) || 0;

        if (percentage < 0) {
            this.value = 0;
            return;
        }
        if (percentage > 100) {
            this.value = 100;
            return;
        }

        updateSalePreview();
    });

    // 가격 입력 필드 변경 시 미리보기 업데이트
    document.addEventListener('input', function(e) {
        if (e.target.matches('input[name="product.price[]"]')) {
            updateSalePreview();
        }
    });
}

// --- 세일 미리보기 업데이트 ---
function updateSalePreview() {
    const salePercentageInput = document.getElementById('salePercentage');
    const salePreview = document.getElementById('salePreview');
    const originalPriceText = document.getElementById('originalPriceText');
    const salePriceText = document.getElementById('salePriceText');

    const percentage = parseInt(salePercentageInput.value) || 0;

    if (percentage > 0) {
        // 사이즈 컨테이너에서 첫 번째 가격 가져오기
        const priceInputs = document.querySelectorAll('input[name="product.price[]"]');
        let originalPrice = 0;

        if (priceInputs.length > 0) {
            originalPrice = parseInt(priceInputs[0].value) || 0;
        }

        if (originalPrice > 0) {
            const salePrice = Math.round(originalPrice * (100 - percentage) / 100);

            originalPriceText.textContent = originalPrice.toLocaleString();
            salePriceText.textContent = salePrice.toLocaleString();
            salePreview.style.display = 'block';
        } else {
            salePreview.style.display = 'none';
        }
    } else {
        salePreview.style.display = 'none';
    }
}

// --- API 및 유틸리티 초기화 ---
function initializeApiAndUtils() {
    window.api = window.api || {};
    import('../api/app.js')
        .then(module => {
            window.api = module.api;

            // brand 카테고리 가져오기.
            window.api.brand.lookupBrand();

            // 사이즈
            window.api.size.init();
            window.api.size.bindAddSizeButton();

            // modal initial
            window.api.modal.init();

            // category 가져오기
            window.api.category.init();
            window.api.category.lookupByMain();

            // product-color 가져오기
            window.api.colors.lookupColors();

            // 이미지 업로드
            window.api.utils.init('fileInput', 'preview-area');

            // 세일 이벤트 초기화
            initializeSaleEvents();
        });
}