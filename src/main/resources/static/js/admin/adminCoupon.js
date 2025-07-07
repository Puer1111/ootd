// 쿠폰 조회 및 표시 함수
async function retrieveAndDisplayCoupons() {
    try {
        const response = await fetch('/api/coupon/all');
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        const coupons = await response.json();
        const table = document.getElementById('coupon-table');
        if (!table) return; // 테이블이 없으면 함수 종료

        const tableBody = table.querySelector('tbody');
        if (!tableBody) return; // tbody가 없으면 함수 종료

        tableBody.innerHTML = ''; // 기존 데이터 삭제

        coupons.forEach(coupon => {
            const row = tableBody.insertRow();
            const expirationDate = coupon.expirationDate ? new Date(coupon.expirationDate).toLocaleDateString() : 'N/A';
            row.innerHTML = `
                <td>${coupon.id}</td>
                <td>${coupon.couponName}</td>
                <td>${coupon.discountRate}%</td>
                <td>${coupon.quantity}</td>
                <td>${expirationDate}</td>
            `;
        });
    } catch (error) {
        console.error('Error fetching coupons:', error);
        alert('쿠폰 정보를 불러오는 데 실패했습니다.');
    }
}

// 쿠폰 등록을 처리하는 함수
function register(couponData) {

    // 숫자 필드는 숫자로 변환
    couponData.discountRate = parseInt(couponData.discountRate, 10);
    couponData.quantity = parseInt(couponData.quantity, 10);
    couponData.receiveLimit = parseInt(couponData.receiveLimit, 10);
    couponData.usageLimit = parseInt(couponData.usageLimit, 10);

    fetch('/api/coupon/insert', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(couponData),
    })
    .then(response => {
        if (response.ok) {
            alert('쿠폰이 성공적으로 등록되었습니다.');
            window.location.reload(); // 성공 시 페이지 새로고침
        } else {
            // 서버로부터 받은 에러 메시지를 파싱하려는 시도
            response.text().then(text => {
                console.error('Coupon registration failed:', text);
                alert('쿠폰 등록에 실패했습니다. 서버 로그를 확인하세요.');
            });
        }
    })
    .catch(error => {
        console.error('Error during coupon registration:', error);
        alert('쿠폰 등록 중 오류가 발생했습니다.');
    });
}

// 쿠폰 수정을 처리하는 함수
function update(couponData) {
    // 숫자 필드는 숫자로 변환
    couponData.couponId = parseInt(couponData.couponId, 10);
    couponData.discountRate = parseInt(couponData.discountRate, 10);
    couponData.quantity = parseInt(couponData.quantity, 10);
    couponData.receiveLimit = parseInt(couponData.receiveLimit, 10);
    couponData.usageLimit = parseInt(couponData.usageLimit, 10);

    fetch('/api/coupon/update', {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(couponData),
    })
    .then(response => {
        if (response.ok) {
            alert('쿠폰이 성공적으로 수정되었습니다.');
            window.location.reload(); // 성공 시 페이지 새로고침
        } else {
            response.text().then(text => {
                console.error('Coupon modification failed:', text);
                alert('쿠폰 수정에 실패했습니다. 서버 로그를 확인하세요.');
            });
        }
    })
    .catch(error => {
        console.error('Error during coupon modification:', error);
        alert('쿠폰 수정 중 오류가 발생했습니다.');
    });
}

// 쿠폰 삭제를 처리하는 함수
function deleteCoupon(couponId) {
    if (!confirm('정말로 이 쿠폰을 삭제하시겠습니까?')) {
        return;
    }

    fetch('/api/coupon/delete', {
        method: 'DELETE',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ couponId: couponId }),
    })
    .then(response => {
        if (response.ok) {
            alert('쿠폰이 성공적으로 삭제되었습니다.');
            window.location.reload(); // 성공 시 페이지 새로고침
        } else {
            response.text().then(text => {
                console.error('Coupon deletion failed:', text);
                alert('쿠폰 삭제에 실패했습니다. 서버 로그를 확인하세요.');
            });
        }
    })
    .catch(error => {
        console.error('Error during coupon deletion:', error);
        alert('쿠폰 삭제 중 오류가 발생했습니다.');
    });
}

// 섹션 표시/숨김 함수
function showSection(sectionId) {
    const sections = document.querySelectorAll('#coupon-retrieve-section, #coupon-register-section, #coupon-modify-section, #coupon-delete-section');
    sections.forEach(section => {
        if (section.id === sectionId) {
            section.style.display = 'block';
            if (sectionId === 'coupon-retrieve-section') {
                retrieveAndDisplayCoupons();
            }
        } else {
            section.style.display = 'none';
        }
    });
}

// 카테고리 조회 함수
async function lookupCategory(mainCategory, subCategorySelectElement) {
    subCategorySelectElement.innerHTML = '<option value="">-- 소분류 선택 --</option>'; // 기존 옵션 초기화

    if (!mainCategory) {
        return;
    }

    try {
        const response = await fetch(`/api/search/category?mainCategory=${mainCategory}`);
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        const categories = await response.json();

        categories.forEach(category => {
            const option = document.createElement('option');
            option.value = category.subCategory; // 실제 DB에 저장될 값
            option.textContent = category.subCategory; // 사용자에게 보여질 텍스트
            subCategorySelectElement.appendChild(option);
        });
    } catch (error) {
        console.error('Error fetching subcategories:', error);
        alert('소분류 카테고리를 불러오는 데 실패했습니다.');
    }
}

document.addEventListener('DOMContentLoaded', () => {
    // 초기에는 조회 섹션만 보이도록 설정
    showSection('coupon-retrieve-section');

    // 사이드바 메뉴 토글 기능
    const couponManagementToggle = document.getElementById('coupon-management-toggle');
    const couponSubmenu = document.getElementById('coupon-submenu');

    if (couponManagementToggle && couponSubmenu) {
        couponManagementToggle.addEventListener('click', (event) => {
            // 클릭된 요소가 서브메뉴 링크가 아닌 경우에만 토글
            if (event.target.tagName !== 'A') {
                couponSubmenu.classList.toggle('active');
            }
        });

        // 서브메뉴 링크 클릭 시 해당 섹션 표시
        couponSubmenu.querySelectorAll('a').forEach(link => {
            link.addEventListener('click', (event) => {
                event.preventDefault();
                const targetSectionId = event.target.dataset.targetSection;
                showSection(targetSectionId);
            });
        });
    }

    // 쿠폰 등록 폼 처리
    const couponForm = document.getElementById('coupon-form');
    if (couponForm) {
        couponForm.addEventListener('submit', (event) => {
            event.preventDefault();
            const formData = new FormData(couponForm);
            const couponData = Object.fromEntries(formData.entries());
            register(couponData);
        });

        // 등록 폼의 mainCategory 변경 시 subCategory 업데이트
        const mainCategorySelect = couponForm.querySelector('#mainCategory');
        const subCategorySelect = couponForm.querySelector('#subCategory');
        if (mainCategorySelect && subCategorySelect) {
            mainCategorySelect.addEventListener('change', () => {
                lookupCategory(mainCategorySelect.value, subCategorySelect);
            });
        }
    }

    // 쿠폰 수정 폼 처리
    const couponUpdateForm = document.getElementById('coupon-modify-form');
    if (couponUpdateForm) {
        couponUpdateForm.addEventListener('submit', (event) => {
            event.preventDefault();
            const formData = new FormData(couponUpdateForm);
            const couponData = Object.fromEntries(formData.entries());
            update(couponData);
        });

        // 수정 폼의 modifyMainCategory 변경 시 modifySubCategory 업데이트
        const modifyMainCategorySelect = couponUpdateForm.querySelector('#modifyMainCategory');
        const modifySubCategorySelect = couponUpdateForm.querySelector('#modifySubCategory');
        if (modifyMainCategorySelect && modifySubCategorySelect) {
            modifyMainCategorySelect.addEventListener('change', () => {
                lookupCategory(modifyMainCategorySelect.value, modifySubCategorySelect);
            });
        }
    }

    // 쿠폰 삭제 폼 처리
    const couponDeleteForm = document.getElementById('coupon-delete-form');
    if (couponDeleteForm) {
        couponDeleteForm.addEventListener('submit', (event) => {
            event.preventDefault();
            const formData = new FormData(couponDeleteForm);
            const couponId = parseInt(formData.get('couponId'), 10);
            deleteCoupon(couponId);
        });
    }

});
