window.IMP = null;

document.addEventListener("DOMContentLoaded", function () {
    if (window.IMP) {
        console.log("✅ 아임포트 로드 완료");
        window.IMP.init("imp68486865");
    } else {
        console.error("🚨 아임포트 로드 실패: window.IMP가 정의되지 않음");
    }
    // 🆕 주문 정보 로드 및 페이지 초기화
    loadOrderInfo();
});

// 🆕 주문 정보 로드 함수
function loadOrderInfo() {
    const orderInfoStr = sessionStorage.getItem('orderInfo');
    if (orderInfoStr) {
        const orderInfo = JSON.parse(orderInfoStr);
        console.log('로드된 주문 정보:', orderInfo);

        // 페이지에 주문 정보 표시
        displayOrderInfo(orderInfo);
    } else {
        console.warn('주문 정보가 없습니다.');
        // 기본값 또는 에러 처리
        displayDefaultInfo();
    }
}
// 🆕 주문 정보 화면 표시
function displayOrderInfo(orderInfo) {
    const productNameEl = document.getElementById('productName');
    const productPriceEl = document.getElementById('productPrice');
    const quantityEl = document.getElementById('quantity');
    const totalPriceEl = document.getElementById('totalPrice');
    const salePrcentEl = document.getElementById('salePercent');

    if (productNameEl) productNameEl.textContent = orderInfo.productName || 'Test';
    if (productPriceEl) productPriceEl.textContent = orderInfo.unitPrice || 5000;
    if (quantityEl) quantityEl.textContent = orderInfo.quantity || 1;
    if (totalPriceEl) totalPriceEl.textContent = orderInfo.totalPrice || 5000;
    if (salePrcentEl) salePrcentEl.textContent = 0; // 할인율은 기본 0
}

// 🆕 기본 정보 표시 (주문 정보가 없을 때)
function displayDefaultInfo() {
    const productNameEl = document.getElementById('productName');
    const productPriceEl = document.getElementById('productPrice');
    const quantityEl = document.getElementById('quantity');
    const totalPriceEl = document.getElementById('totalPrice');

    if (productNameEl) productNameEl.textContent = 'Test Product';
    if (productPriceEl) productPriceEl.textContent = 5000;
    if (quantityEl) quantityEl.textContent = 1;
    if (totalPriceEl) totalPriceEl.textContent = 5000;
}

function getItem(){
    const quantity = document.getElementById('quantity').innerText;
    const productName = document.getElementById('productName').innerText;
    const productPrice = document.getElementById('productPrice').innerText;
    const salePercent = document.getElementById('salePercent').innerText;
    const totalPrice = document.getElementById('totalPrice').innerText;
    return {
        quantity: quantity,
        productName: productName,
        productPrice:productPrice,
        salePercent: salePercent,
        totalPrice: totalPrice
    };
}
async function createOrder(){
    const item = getItem();

    // 🆕 실제 주문 정보 사용
    const orderInfo = JSON.parse(sessionStorage.getItem('orderInfo') || '{}');

    const orderResponse = await fetch("/orders", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
            // 🔥 실제 주문 데이터 사용
            orderId: orderInfo.orderId, // 이미 생성된 주문 ID 사용
            quantity: item.quantity,
            merchantUid: "merchant_" + new Date().getTime(),
            productName: item.productName,
            productPrice: item.productPrice,
            salePercent: item.salePercent || 0,
            totalPrice: item.totalPrice,
        })
    });
    return await orderResponse.json();
}

async function requestPay() {
    const data = await createOrder();
    console.log("🔍 createOrder 응답 데이터:", data);
    console.log("🔍 orderId 값:", data.orderId);
    console.log("🔍 data 타입:", typeof data);

    if (!data) {
        alert("결제 데이터를 불러오는 중입니다. 잠시 후 다시 시도해주세요.");
        return;
    }

    // 🔥 필요한 변수들 정의
    const quantity = parseInt(document.getElementById('quantity')?.textContent || 1);
    const unitPrice = parseInt(document.getElementById('productPrice')?.textContent || 0);
    const totalPrice = parseInt(document.getElementById('totalPrice')?.textContent || 0);

    IMP.request_pay({
            pg: "html5_inicis.INIpayTest", // KG이니시스
            pay_method: "card",
            merchant_uid: data.merchantUid,
            name: data.productName,
            // 🔥 실제 총 금액 사용 (테스트는 100원)
            amount: 100, // 실제 운영시: totalPrice
            buyer_email:"Hello@naver.com" ,
            buyer_name:"홍길동",
            buyer_tel: "01012345678",
        },
        async function (rsp) {
            if (rsp.success) {
                console.log("✅ 결제 성공, imp_uid:", rsp.imp_uid);
                try {
                    // 🔥 결제 검증 요청
                    const validationResponse = await fetch(`/validation/${rsp.imp_uid}`, {
                        method: "POST",
                        headers: {"Content-Type": "application/json"},
                        body: JSON.stringify({
                            imp_uid: rsp.imp_uid,
                        }),
                    });

                    const validationResult = await validationResponse.json();

                    if (validationResult == null) {
                        alert(`결제 검증 실패: ${validationResult.error}`);
                        return;
                    }

                    console.log("결제검증이 완료되었습니다!");

                    // 🔥 결제 정보 저장 요청
                    const buyerInfo = {
                        orderId: data.orderId,
                        impUid: rsp.imp_uid,
                        productName: data.productName,
                        payMethod: "card",
                        merchantUid: rsp.merchant_uid,
                        totalPrice: parseInt(rsp.paid_amount, 10),
                        email: rsp.buyer_email,
                        phone: rsp.buyer_tel,
                        userName: rsp.buyer_name,
                        orderDate: data.orderDate,
                    };

                    console.log("✅ 최종 저장할 buyerInfo:", buyerInfo);

                    const saveResponse = await fetch("/payments/save", {
                        method: "POST",
                        headers: {"Content-Type": "application/json"},
                        body: JSON.stringify(buyerInfo),
                    });

                    if (saveResponse.ok) {
                        console.log("저장이 완료되었습니다");
                        alert("결제가 완료되었습니다!\n주문 내역에서 확인 가능합니다.");

                        // 🆕 주문 정보 정리
                        sessionStorage.removeItem('orderInfo');

                        // 🆕 주문 내역으로 이동
                        location.href = "/order-history";
                    } else {
                        console.error("🚨 결제 정보 저장 실패:");
                        alert(`결제 정보 저장 실패:`);
                    }

                } catch (error) {
                    console.error("🚨 결제 처리 중 오류 발생:", error.message);
                    alert("결제 처리 중 오류가 발생했습니다. 다시 시도해주세요.");
                }

            } else {
                console.error("🚨 결제 실패:", rsp.error_msg);
                alert(`결제가 실패했습니다: ${rsp.error_msg}`);
            }
        });
}

document.querySelector(".apply-button").addEventListener("click", async function () {
    await requestPay();
});

async function cancelPay() {
    const orderNo = prompt("예약 번호를 입력해주세요").trim();
    if (!orderNo) {
        alert("올바른 예약 번호를 입력해야 합니다.");
        return;
    }

    try {
        // 서버에서 imp_uid를 가져옴.
        const response = await fetch("/api/getImpUid", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify( {orderNo} ) // JSON 형식으로 데이터 전송
        });

        if (!response.ok) {
            throw new Error("예약 정보를 가져오는데 실패했습니다.");
        }

        const responseData = await response.json();
        const imp_uid = responseData.impUid;

        console.log("잘 왔니??" , imp_uid);
        if (imp_uid) {
            const isConfirmed = confirm("정말 취소 하시겠습니까?");
            if (isConfirmed) {
                // 결제 취소 요청 (POST 요청)
                const cancelResponse = await fetch(`/payments/cancel/${imp_uid}`, {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json"
                    },
                    body: JSON.stringify({
                        reason: "고객 요청으로 취소" // 취소 사유 추가
                    })
                });
                if (!cancelResponse.ok) {
                    throw new Error("결제 취소에 실패했습니다.");
                }
                alert("취소가 완료되었습니다!");
            }
        } else {
            alert("유효한 예약 번호가 아닙니다.");
        }
    } catch (error) {
        alert(error.message);
    }
}

document.querySelector(".cancel-button").addEventListener("click", async function () {
    await cancelPay();
});