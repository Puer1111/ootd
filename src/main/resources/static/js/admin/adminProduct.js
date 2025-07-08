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
const sidebarLinks = document.querySelectorAll('.admin-sidebar a[data-target-section]');
sidebarLinks.forEach(link => {
    link.addEventListener('click', (event) => {
        event.preventDefault();
        const targetSectionId = link.getAttribute('data-target-section');
        showSection(targetSectionId);

        if (targetSectionId === 'product-register-section') {
            resetForm(); // 등록 폼으로 전환 시 폼 초기화
        }
    });
});

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


let allProductsData = []; // 모든 상품 데이터를 저장할 전역 변수

// --- 상품 목록 로드 ---
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
                    <td colspan="8">
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
                product.options.forEach(option => {
                    optionsTableHtml += `
                        <tr>
                            <td>${option.colorName}</td>
                            <td>${option.size}</td>
                            <td>${option.inventory}</td>
                            <td>${option.status}</td>
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

// --- 상품 수정 처리 ---
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
    document.getElementById('description').value = productData.description; // DTO에 description이 있어야 함
    
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
        // window.api.size.addSizeItem 함수가 옵션 데이터를 받아 채울 수 있도록 수정 필요
        // 임시로 직접 DOM 생성
        const sizeItem = document.createElement('div');
        sizeItem.classList.add('size-item');
        sizeItem.innerHTML = `
            <div><input type="text" name="productOption.size[]" value="${option.size}" readonly></div>
            <div><input type="number" name="product.price[]" value="${productData.price}"></div>
            <div><input type="number" name="productOption.inventory[]" value="${option.inventory}"></div>
            <div><input type="text" name="productOption.status[]" value="${option.status}"></div>
            <div><input type="text" name="productOption.colorName[]" value="${option.colorName}" readonly></div>
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
}

// --- 폼 제출 (등록/수정) ---
function submitFormWithAjax() {
    const form = document.getElementById('product-form');
    const formData = new FormData(form);
    const productId = document.getElementById('productNo').value;

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
        });
}
