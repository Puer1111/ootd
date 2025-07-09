document.addEventListener('DOMContentLoaded', () => {
    const setupMenuToggle = (toggleId, submenuId) => {
        const toggleElement = document.getElementById(toggleId);
        const submenuElement = document.getElementById(submenuId);

        if (toggleElement && submenuElement) {
            toggleElement.addEventListener('click', (event) => {
                // Prevent the default anchor behavior if it's a span or other non-link element
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