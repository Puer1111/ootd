document.addEventListener('DOMContentLoaded', function () {
    const quantity = document.getElementById('quantity');
    const addCartBtn = document.getElementById('add-cart-btn');
    addCartBtn.addEventListener('click', function () {
        const productInfo = extractProductFromDOM();
        fetch('/cart/add', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    no: productInfo.productNo,
                    name: productInfo.productName,
                    price: productInfo.price,
                    quantity: quantity.value,
                    imageUrls: productInfo.imageUrl
                })
            }
        )
    })
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
    return element ? element.value : '';
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
