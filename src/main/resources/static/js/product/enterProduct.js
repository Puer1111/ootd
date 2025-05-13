
document.addEventListener('DOMContentLoaded', function() {

    // api 전역 호출
    window.api = window.api || {};
    import('../api/app.js')
    .then(module => {
        window.api = module.api;
        // modal initial
        window.api.modal.init();

        // brand 카테고리 가져오기.
        window.api.brand.lookup();

        // 이미지 업로드
        window.api.utils.uploadFile();
    })
});