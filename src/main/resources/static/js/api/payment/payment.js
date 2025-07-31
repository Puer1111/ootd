window.IMP = null;

let paymentData = {
    productNo: null,
    productName: "Test Product",
    unitPrice: 100,
    quantity: 1,
    salePercent: 0,
    orderId: null,
    get discountAmount() {
        return Math.floor(this.unitPrice * this.quantity * this.salePercent / 100);
    },
    get totalPrice() {
        return (this.unitPrice * this.quantity) - this.discountAmount;
    }
};

document.addEventListener("DOMContentLoaded", function () {
    if (window.IMP) {
        window.IMP.init("imp68486865");
    } else {
        console.error("아임포트 로드 실패: window.IMP가 정의되지 않음");
    }

    initializePaymentData();
    setupQuantityControls();
    setupPaymentButton();
    setupCancelButton();
    updateDisplay();
});

function initializePaymentData() {
    try {
        const orderInfo = sessionStorage.getItem('orderInfo');
        if (orderInfo) {
            const orderData = JSON.parse(orderInfo);
            paymentData.productNo = orderData.productNo || null;
            paymentData.productName = orderData.productName || "상품명";
            paymentData.unitPrice = orderData.unitPrice || orderData.finalUnitPrice || 5000;
            paymentData.quantity = orderData.quantity || 1;
            paymentData.orderId = orderData.orderId;
        }
    } catch (error) {
        console.error("주문 정보 로드 실패:", error);
    }
}

function updateDisplay() {
    const elements = {
        productName: document.getElementById('productName'),
        productPrice: document.getElementById('productPrice'),
        quantity: document.getElementById('quantity'),
        salePercent: document.getElementById('salePercent'),
        discountAmount: document.getElementById('discountAmount'),
        totalPrice: document.getElementById('totalPrice')
    };

    if (elements.productName) elements.productName.textContent = paymentData.productName;
    if (elements.productPrice) elements.productPrice.textContent = paymentData.unitPrice.toLocaleString();
    if (elements.quantity) elements.quantity.textContent = paymentData.quantity + "개";
    if (elements.salePercent) elements.salePercent.textContent = paymentData.salePercent + "%";
    if (elements.discountAmount) elements.discountAmount.textContent = paymentData.discountAmount.toLocaleString();
    if (elements.totalPrice) elements.totalPrice.textContent = paymentData.totalPrice.toLocaleString();

    updateQuantityButtonState();
}

function updateQuantityButtonState() {
    const minusBtn = document.getElementById('minus-btn');
    const plusBtn = document.getElementById('plus-btn');

    if (minusBtn) minusBtn.disabled = paymentData.quantity <= 1;
    if (plusBtn) plusBtn.disabled = paymentData.quantity >= 99;
}

function setupQuantityControls() {
    const minusBtn = document.getElementById('minus-btn');
    const plusBtn = document.getElementById('plus-btn');

    if (minusBtn) {
        minusBtn.addEventListener('click', function () {
            if (paymentData.quantity > 1) {
                paymentData.quantity--;
                updateDisplay();
            }
        });
    }

    if (plusBtn) {
        plusBtn.addEventListener('click', function () {
            if (paymentData.quantity < 99) {
                paymentData.quantity++;
                updateDisplay();
            }
        });
    }
}

async function updateOrderPayment(imp_uid, orderId) {
    const sendData = {
        impUid: imp_uid,
        orderId: orderId
    };

    try {
        const token = localStorage.getItem('token') || sessionStorage.getItem('token');
        if (!token) return;

        const response = await fetch(`/sendImpUid`, {
            method: "Patch",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(sendData)
        });

        if (response.ok) {
            return response.json();
        } else {
            throw new Error('Network response was not ok');
        }
    } catch (error) {
        console.error('결제 정보 업데이트 오류:', error);
        return null;
    }
}

function getItem() {
    return {
        quantity: paymentData.quantity,
        productName: paymentData.productName,
        productPrice: paymentData.unitPrice,
        salePercent: paymentData.salePercent,
        totalPrice: paymentData.totalPrice,
        orderId: paymentData.orderId
    };
}

async function createOrder() {
    const item = getItem();
    const userData = await checkUserId();
    if (!userData) return null;

    if (item.orderId) {
        const updateResult = await updateOrderQuantity();
        if (updateResult) {
            return {
                orderId: item.orderId,
                merchantUid: "merchant_" + new Date().getTime(),
                productName: item.productName,
                productPrice: item.productPrice,
                quantity: item.quantity,
                totalPrice: item.totalPrice,
                salePercent: item.salePercent,
                orderDate: new Date().toISOString()
            };
        }
    }

    const orderResponse = await fetch("/orders", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
            userId: userData.id,
            quantity: item.quantity,
            merchantUid: "merchant_" + new Date().getTime(),
            productName: item.productName,
            productPrice: item.productPrice,
            salePercent: item.salePercent,
            totalPrice: item.totalPrice
        })
    });

    if (orderResponse.ok) {
        return await orderResponse.json();
    } else {
        console.error("주문 생성 실패:", orderResponse.status);
        return null;
    }
}

async function checkUserId() {
    try {
        const token = localStorage.getItem('auth_token') || localStorage.getItem('token');
        const response = await fetch("/api/auth/info", {
            headers: { "Authorization": `Bearer ${token}` }
        });
        if (!response.ok) return null;
        return await response.json();
    } catch (error) {
        console.error("사용자 ID 조회 중 예외 발생:", error);
        return null;
    }
}

async function requestPay() {
    const data = await createOrder();
    if (!data) {
        alert("결제 데이터를 불러오는 중입니다. 잠시 후 다시 시도해주세요.");
        return;
    }

    const currentItem = getItem();
    const userData = await checkUserId();

    IMP.request_pay({
        pg: "html5_inicis.INIpayTest",
        pay_method: "card",
        merchant_uid: data.merchantUid,
        name: currentItem.productName,
        amount: currentItem.totalPrice,
        buyer_email: userData.email,
        buyer_name: userData.id,
        buyer_tel: userData.phone
    }, async function (rsp) {
        if (rsp.success) {
            await handlePaymentSuccess(rsp, data, currentItem);
        } else {
            alert(`결제가 실패했습니다: ${rsp.error_msg}`);
        }
    });
}
window.requestPay = requestPay;

async function handlePaymentSuccess(rsp, data, currentItem) {
    try {
        alert(`결제가 완료되었습니다.\n상품: ${currentItem.productName}\n수량: ${currentItem.quantity}개\n결제금액: ${parseInt(rsp.paid_amount).toLocaleString()}원`);

        const validationResponse = await fetch(`/validation/${rsp.imp_uid}`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ imp_uid: rsp.imp_uid })
        });

        const validationResult = await validationResponse.json();
        if (!validationResult) {
            alert("결제 검증에 실패했습니다.");
            return;
        }

        const buyerInfo = {
            orderId: data.orderId,
            impUid: rsp.imp_uid,
            productName: currentItem.productName,
            payMethod: "card",
            merchantUid: rsp.merchant_uid,
            totalPrice: parseInt(rsp.paid_amount, 10),
            email: rsp.buyer_email,
            phone: rsp.buyer_tel,
            userName: rsp.buyer_name,
            orderDate: data.orderDate,
            quantity: currentItem.quantity
        };

        const saveResponse = await fetch("/payments/save", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(buyerInfo)
        });

        if (saveResponse.ok) {
            sessionStorage.removeItem('orderInfo');
            const earnPoints = Math.floor(parseInt(rsp.paid_amount) * 0.01);
            const finalMessage = `결제가 완료되었습니다!\n\n${earnPoints}원의 적립금이 지급되었습니다.\n\n주문 내역을 확인하시겠습니까?`;

            if (confirm(finalMessage)) {
                location.href = "/order-history";
            } else {
                location.href = "/";
            }
        } else {
            alert("결제는 완료되었으나 정보 저장에 실패했습니다. 고객센터에 문의해주세요.");
        }
    } catch (error) {
        alert("결제 처리 중 오류가 발생했습니다. 다시 시도해주세요.");
    }
}

function setupPaymentButton() {
    const paymentButton = document.querySelector(".apply-button");
    if (paymentButton) {
        paymentButton.addEventListener("click", async function () {
            const currentItem = getItem();
            const confirmMessage = `결제를 진행하시겠습니까?\n\n상품: ${currentItem.productName}\n수량: ${currentItem.quantity}개\n할인: ${currentItem.salePercent}%\n결제금액: ${currentItem.totalPrice.toLocaleString()}원`;

            if (confirm(confirmMessage)) {
                await requestPay();
            }
        });
    }
}

function setupCancelButton() {
    const cancelButton = document.querySelector(".cancel-button");
    if (cancelButton) {
        cancelButton.addEventListener("click", async function () {
            await cancelPay();
        });
    }
}

function updateSalePercent(percent) {
    if (percent >= 0 && percent <= 100) {
        paymentData.salePercent = percent;
        updateDisplay();
    }
}
window.updateSalePercent = updateSalePercent;

function updatePaymentDetails() {
    const productPrice = parseFloat(productPriceElement.textContent.replace(/[^0-9.-]+/g, ""));
    const quantity = parseInt(quantityElement.textContent.replace(/[^0-9]+/g, ""));
    const currentSalePercent = parseFloat(salePercentElement.textContent.replace(/[^0-9.-]+/g, ""));

    const originalTotal = productPrice * quantity;
    const discountAmount = originalTotal * (currentSalePercent / 100);
    const finalPrice = originalTotal - discountAmount;

    discountAmountElement.textContent = discountAmount.toFixed(0);
    totalPriceElement.textContent = finalPrice.toFixed(0);
}
