document.addEventListener('DOMContentLoaded', function () {

    // api 전역 호출
    window.api = window.api || {};
    import('../api/app.js')
        .then( module => {
            window.api = module.api;
            // modal initial
            window.api.modal.init();

            // brand 카테고리 가져오기.
            window.api.brand.lookupBrand();

            // 사이즈
            window.api.size.init();
            window.api.size.bindAddSizeButton();

            // category 가져오기
            window.api.category.init();
            window.api.category.lookupCategory();

            // product-color 가져오기
            window.api.colors.lookupColors();

            // 이미지 업로드
            window.api.utils.init('fileInput', 'preview-area');

            // 폼 제출
            document.getElementById('submit-btn').addEventListener('click', function (e) {
                submitFormWithAjax(); // Ajax 함수 호출
            });

        })
});

function submitFormWithAjax() {
    const form = document.getElementById('product-form');
    const formData = new FormData();

    // 일반 필드들 추가
    formData.append('brandName', document.getElementById('brand-select').value);
    formData.append('productName', document.getElementById('productName').value);
    formData.append('subCategory', document.getElementById('categoryChoiceSecond').value);
    formData.append('price', document.getElementById('product-price').value);
    formData.append('description',document.getElementById('description').value);
    formData.append('')
    // Multiple select 처리
    const colorSelect = document.getElementById('product-color');
    Array.from(colorSelect.selectedOptions).forEach(option => {
        formData.append('colorsNo', option.value);
    });

    // 파일들 추가
    const fileInput = document.getElementById('fileInput');

    if (fileInput.files.length === 0) {
        console.log('파일이 선택되지 않았습니다.');
    }
    Array.from(fileInput.files).forEach(file => {
        formData.append('images', file);
    });

    // Ajax 전송
    fetch('/enter/product', {
        method: 'POST',
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
            // 성공 처리
            alert('상품이 성공적으로 등록되었습니다.');
            form.reset();
        })
        .catch(error => {
            console.error('Error:', error);
            alert('등록 중 오류가 발생했습니다.');
        });
}
