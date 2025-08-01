document.addEventListener('DOMContentLoaded', () => {
    const setupMenuToggle = (toggleId, submenuId) => {
        const toggleElement = document.getElementById(toggleId);
        const submenuElement = document.getElementById(submenuId);

        if (toggleElement && submenuElement) {
            toggleElement.addEventListener('click', (event) => {

                if (event.target.tagName !== 'A') {
                    event.preventDefault();
                }
                submenuElement.classList.toggle('active');
            });
        }
    };

    setupMenuToggle('coupon-management-toggle', 'coupon-submenu');
    setupMenuToggle('product-management-toggle', 'product-submenu');
});