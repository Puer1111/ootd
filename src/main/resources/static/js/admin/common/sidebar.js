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