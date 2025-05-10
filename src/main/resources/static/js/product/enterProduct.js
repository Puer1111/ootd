
document.addEventListener('DOMContentLoaded', function() {

    // api 전역 호출
    window.api = window.api || {};
    import('../api/app.js')
    .then(module => {
        window.api = module.api;
        // modal initial
        window.api.modal.init();
    })

    // fetch('/api/lookup/brands')
    //     .then(response => response.json())
    //     .then(brandNames => {
    //         // 브랜드 옵션 추가
    //         brandNames.forEach(brandName => {
    //             const option = document.createElement('option');
    //             option.value = brandName;
    //             option.textContent = brandName;
    //             brandSelect.appendChild(option);
    //         });
    //     })
    //     .catch(error => console.error('브랜드 목록을 가져오는 중 오류 발생:', error));
});