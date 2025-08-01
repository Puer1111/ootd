document.addEventListener('DOMContentLoaded', function () {
    const quantity = document.getElementById('quantity');
    const addCartBtn = document.getElementById('add-cart-btn');

    addCartBtn.addEventListener('click', function (e) {
        e.preventDefault();

        const productInfo = extractProductFromDOM();

        fetch('/cart/add', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                productNo: productInfo.productNo,

                productName: productInfo.productName,
                price: productInfo.price,

                imageUrls: productInfo.imageUrl
            })
        })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    alert('장바구니에 추가되었습니다!');
                } else {
                    alert('추가 실패: ' + data.message);
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert('오류가 발생했습니다.');
            });
    });
});

function extractProductFromDOM() {
    const product = {
        productNo: this.getValueById('product-no'),
        productName: this.getTextById('product-name'),
        price: this.getPriceFromText('product-price'),
        imageUrl: this.getImageSrc('product-image'),
    };

    console.log('추출된 상품 정보:', product);
    return product;
}

function getTextById(id) {
    const element = document.getElementById(id);
    return element ? element.textContent.trim() : '';
}

function getValueById(id) {
    const element = document.getElementById(id);
    return element ? element.innerText : '';
}

function getImageSrc(id) {
    const element = document.getElementById(id);
    return element ? element.src : '';
}

function getPriceFromText(id) {
    const element = document.getElementById(id);
    if (element) {
        // "5,000원" → 5000 숫자로 변환
        const priceText = element.textContent.replace(/[^0-9]/g, '');
        return parseInt(priceText) || 0;
    }
    return 0;
}
