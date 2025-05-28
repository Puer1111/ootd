export const product = {
    submitAsJSON(){
    const form = document.getElementById('product-form');
    const formData = new FormData(form);
    const select = document.getElementById('product-color');

    // 선택된 색상들
    const selectedColors = Array.from(select.selectedOptions).map(opt => opt.value);

    // FormData를 객체로 변환
    const data = {
        productName: formData.get('productName'),
        brandName: formData.get('brandName'),
        price: formData.get('price'),
        category: formData.get('category'),
        colorsNo: selectedColors  // 배열로 전송
    };

    console.log('JSON 데이터:', data);

    fetch('/enter/product', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
    })
        .then(response => response.json())
        .then(result => {
            console.log('결과:', result);
            if (result.redirectUrl) {
                window.location.href = "/"

            }
        });
    }
}
