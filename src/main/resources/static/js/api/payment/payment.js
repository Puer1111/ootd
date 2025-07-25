window.IMP = null;

// 결제 정보 변수들
let paymentData = {
    productNo: null, // 🆕 상품번호 추가
    productName: "Test Product",
    unitPrice: 100,
    quantity: 1,
    salePercent: 0,
    orderId: null, // 주문 ID
    get discountAmount() {
        return Math.floor(this.unitPrice * this.quantity * this.salePercent / 100);
    },
    get totalPrice() {
        return (this.unitPrice * this.quantity) - this.discountAmount;
    }
};

document.addEventListener("DOMContentLoaded", function () {
    // 아임포트 초기화
    if (window.IMP) {
        console.log("✅ 아임포트 로드 완료");
        window.IMP.init("imp68486865");
    } else {
        console.error("🚨 아임포트 로드 실패: window.IMP가 정의되지 않음");
    }

    // 초기화 함수들 실행
    initializePaymentData();
    setupQuantityControls();
    setupPaymentButton();
    setupCancelButton();
    updateDisplay();
});

// ==================== 데이터 초기화 (수정된 버전) ====================
function initializePaymentData() {
    try {
        const orderInfo = sessionStorage.getItem('orderInfo');
        if (orderInfo) {
            const orderData = JSON.parse(orderInfo);
            paymentData.productNo = orderData.productNo || null;
            paymentData.productName = orderData.productName || "상품명";

            // 🔥 수정: 이미 할인이 적용된 가격을 사용
            paymentData.unitPrice = orderData.unitPrice || orderData.finalUnitPrice || 5000;
            paymentData.quantity = orderData.quantity || 1;
            paymentData.orderId = orderData.orderId;

            // 🔥 중요: 추가 할인 방지를 위해 salePercent를 0으로 고정
            paymentData.salePercent = 0; // 이미 할인된 가격이므로 추가 할인 없음

            console.log("📦 주문 정보 로드:", {
                productNo: paymentData.productNo, // 🆕 추가
                productName: orderData.productName,
                unitPrice: paymentData.unitPrice,
                quantity: paymentData.quantity,
                isActiveSale: orderData.isActiveSale,
                originalSalePercentage: orderData.salePercentage, // 참조용
                appliedSalePercentage: paymentData.salePercent // 실제 적용값 (0)
            });
        } else {
            console.log("📦 기본 결제 데이터 사용");
        }
    } catch (error) {
        console.error("❌ 주문 정보 로드 실패:", error);
    }
}

// ==================== 화면 업데이트 ====================
function updateDisplay() {
    const elements = {
        productName: document.getElementById('productName'),
        productPrice: document.getElementById('productPrice'),
        quantity: document.getElementById('quantity'),
        salePercent: document.getElementById('salePercent'),
        discountAmount: document.getElementById('discountAmount'),
        totalPrice: document.getElementById('totalPrice')
    };

    // 각 요소 업데이트
    if (elements.productName) {
        elements.productName.textContent = paymentData.productName;
    }
    if (elements.productPrice) {
        elements.productPrice.textContent = paymentData.unitPrice.toLocaleString();
    }
    if (elements.quantity) {
        elements.quantity.textContent = paymentData.quantity;
    }
    if (elements.salePercent) {
        elements.salePercent.textContent = paymentData.salePercent;
    }
    if (elements.discountAmount) {
        elements.discountAmount.textContent = paymentData.discountAmount.toLocaleString();
    }
    if (elements.totalPrice) {
        elements.totalPrice.textContent = paymentData.totalPrice.toLocaleString();
    }

    // 수량 버튼 상태 업데이트
    updateQuantityButtonState();

    console.log("💰 결제 정보 업데이트:", {
        productNo: paymentData.productNo, // 🆕 추가
        quantity: paymentData.quantity,
        unitPrice: paymentData.unitPrice,
        totalPrice: paymentData.totalPrice,
        orderId: paymentData.orderId
    });
}

function updateQuantityButtonState() {
    const minusBtn = document.getElementById('minus-btn');
    const plusBtn = document.getElementById('plus-btn');

    if (minusBtn) {
        minusBtn.disabled = paymentData.quantity <= 1;
    }
    if (plusBtn) {
        plusBtn.disabled = paymentData.quantity >= 99;
    }
}

// ==================== 수량 조절 ====================
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

// ==================== 🆕 결제 성공 시 UserOrder 저장 (수정된 버전) ====================
// async function saveUserOrder() {
//     if (!paymentData.productNo) {
//         console.log("❌ 상품번호가 없어 UserOrder 저장을 건너뜁니다.");
//         return null;
//     }
//
//     try {
//         const token = localStorage.getItem('token') || sessionStorage.getItem('token');
//         if (!token) {
//             console.log("❌ 로그인 토큰이 없습니다.");
//             return null;
//         }
//
//         console.log("📦 UserOrder 저장 시작:", {
//             productNo: paymentData.productNo,
//             quantity: paymentData.quantity,
//             totalPrice: paymentData.totalPrice
//         });
//
//         // 🔥 수정: productNo 변수 대신 paymentData.productNo 사용
//         const response = await fetch(`/user-orders/${paymentData.productNo}`, {
//             method: "POST",
//             headers: {
//                 "Content-Type": "application/json",
//                 "Authorization": "Bearer " + token
//             },
//             body: JSON.stringify({
//                 quantity: paymentData.quantity,
//                 totalPrice: paymentData.totalPrice,
//                 unitPrice: paymentData.unitPrice,
//                 isActiveSale: paymentData.salePercent > 0,
//                 salePercentage: paymentData.salePercent
//             })
//         });
//
//         if (response.ok) {
//             const result = await response.json();
//             console.log("✅ UserOrder 저장 성공:", result);
//             return result;
//         } else {
//             const errorText = await response.text();
//             console.error("❌ UserOrder 저장 실패:", response.status, errorText);
//             return null;
//         }
//     } catch (error) {
//         console.error("❌ UserOrder 저장 오류:", error);
//         console.error("❌ 오류 상세:", error.message);
//         return null;
//     }
// }

// ==================== 🆕 결제 완료 후 Order 업데이트 ====================
async function updateOrderPayment(imp_uid,orderId) {

    const sendData = {
        impUid: imp_uid,
        orderId: orderId
    }

    console.log(sendData);
    try {

        const token = localStorage.getItem('token') || sessionStorage.getItem('token');
        if (!token) {
            console.log("❌ 로그인 토큰이 없습니다.");

        };

        if (response.ok) {
            const result = await response.json();
            console.log("✅ 주문 수량 업데이트 성공:", result);
            return result;
        } else {
            console.error("❌ 주문 수량 업데이트 실패:", response.status);

            return null;
        }

        const response = await fetch(`/sendImpUid`,{
            method : "Patch",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify(sendData)


        })
            if (response.ok) {
                return response.json();
            }else{
                throw new Error('Network response was not ok');
            }



    } catch (error) {
        console.error('❌ 결제 정보 업데이트 오류:', error);
        return null;
    }
}

// ==================== 주문 수량 업데이트 ====================


// ==================== 결제 데이터 반환 ====================
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

// ==================== 주문 생성/업데이트 ====================
async function createOrder() {
    const item = getItem();

    const userData = await checkUserId();
    if(!userData){
        console.log("로그인 못가져옴")
        return null;
    }

    // 기존 주문이 있으면 수량 업데이트
    if (item.orderId) {
        console.log("📝 기존 주문 수량 업데이트:", item.orderId);
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

    // 새 주문 생성
    console.log("🆕 새 주문 생성의 사용자 ID 들어오는지 : ", userData.id);
    const orderResponse = await fetch("/orders", {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify({
            userId: userData.id,
            quantity: item.quantity,
            merchantUid: "merchant_" + new Date().getTime(),
            productName: item.productName,
            productPrice: item.productPrice,
            salePercent: item.salePercent,
            totalPrice: item.totalPrice,
        })
    });

    if (orderResponse.ok) {
        const result = await orderResponse.json();
        console.log("✅ 주문 생성 성공:", result);
        return result;
    } else {
        console.error("❌ 주문 생성 실패:", orderResponse.status);
        return null;
    }
    //
    // return await orderResponse.json();
}

// =================== 유저 ID 확인 ==================
async function checkUserId() {
    try {
        const token = localStorage.getItem('auth_token') || localStorage.getItem('token');
        const response = await fetch("/api/auth/info", {
            headers: { "Authorization": `Bearer ${token}` }
        });
        if (!response.ok) {
            console.error("사용자 정보 조회 실패:", response.status);
            return null; // 실패 시 null을 반환하여 후속 처리를 막습니다.
        }
        return await response.json();
    } catch (error) {
        // 네트워크 오류 등 예외 발생 시
        console.error("사용자 ID 조회 중 예외 발생:", error);
        return null;
    }
}

// ==================== 결제 요청 ====================
async function requestPay() {
    const data = await createOrder();
    console.log("🔍 createOrder 응답 데이터:", data);

    if (!data) {
        alert("결제 데이터를 불러오는 중입니다. 잠시 후 다시 시도해주세요.");
        return;
    }

    const currentItem = getItem();

    const userData = await checkUserId();

    console.log("userData 확인: " + userData);
    // 결제 요청
    IMP.request_pay({
        pg: "html5_inicis.INIpayTest",
        pay_method: "card",
        merchant_uid: data.merchantUid,
        name: currentItem.productName,
        amount: currentItem.totalPrice,

        buyer_email: "Hello@naver.com", // 유저의 데이터로 바꿔야함
        buyer_name: "홍길동",
        buyer_tel: "01012345678",

        buyer_email: userData.email,
        buyer_name: userData.id,
        buyer_tel: userData.phone,

    }, async function (rsp) {
        if (rsp.success) {
            console.log("✅ 결제 성공, imp_uid:", rsp.imp_uid);
            await handlePaymentSuccess(rsp, data, currentItem); // data는 orders |  current 상품의 정보
        } else {
            console.error("🚨 결제 실패:", rsp.error_msg);
            alert(`결제가 실패했습니다: ${rsp.error_msg}`);
        }
    });
}
window.requestPay = requestPay;

// ==================== 결제 성공 처리 ====================
async function handlePaymentSuccess(rsp, data, currentItem) {
    try {
        // 💡 기존 스타일 유지한 결제 완료 메시지
        alert(`결제가 완료되었습니다.\n상품: ${currentItem.productName}\n수량: ${currentItem.quantity}개\n결제금액: ${parseInt(rsp.paid_amount).toLocaleString()}원`);

        // 결제 검증
        const validationResponse = await fetch(`/validation/${rsp.imp_uid}`, {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify({
                imp_uid: rsp.imp_uid,
            }),
        });

        const validationResult = await validationResponse.json();


        if (!validationResult) {
            alert("결제 검증에 실패했습니다.");
            return;
        }

        console.log("✅ 결제검증 완료");



        // const sendImpUid = await fetch(`/sendImpUid`,{
        //     method : "POST",
        //     headers: {"Content-Type": "application/json"},
        //     body: JSON.stringify(sendData)
        //
        // });

        // 🆕 결제 완료 후 Order 업데이트 (impUid 저장)
        // const orderUpdateResult = await updateOrderPayment(rsp.imp_uid, data.orderId);
        // if (orderUpdateResult) {
        //     console.log('✅ 결제 정보 업데이트 완료 - 결제 취소 가능');
        // } else {
        //     console.error('❌ 결제 정보 업데이트 실패 - 결제 취소 불가능');
        // }

        // // 🆕 결제 성공 시 UserOrder 저장
        // const userOrderResult = await saveUserOrder();
        // if (userOrderResult) {
        //     console.log("✅ 주문 내역이 마이페이지에 저장되었습니다.");
        // } else {
        //     console.log("⚠️ 주문 내역 저장에 실패했지만 결제는 완료되었습니다.");
        // }

        // 결제 정보 저장
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
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify(buyerInfo),
        });

        if (saveResponse.ok) {
            console.log("✅ 결제 정보 저장 완료");
            sessionStorage.removeItem('orderInfo'); // 🆕 세션 정리

            // 💡 적립금 안내 포함한 최종 완료 메시지
            const earnPoints = Math.floor(parseInt(rsp.paid_amount) * 0.01);
            const finalMessage = `결제가 완료되었습니다!\n\n${earnPoints}원의 적립금이 지급되었습니다.\n\n주문 내역을 확인하시겠습니까?`;

            if (confirm(finalMessage)) {
                location.href = "/order-history";
            } else {
                location.href = "/";
            }
        } else {
            console.error("🚨 결제 정보 저장 실패");
            alert("결제는 완료되었으나 정보 저장에 실패했습니다. 고객센터에 문의해주세요.");
        }

    } catch (error) {
        console.error("🚨 결제 처리 중 오류:", error);
        alert("결제 처리 중 오류가 발생했습니다. 다시 시도해주세요.");
    }
}

// ==================== 결제 버튼 설정 ====================
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

// ==================== 결제 취소 ====================
async function cancelPay() {
    const orderNo = prompt("예약 번호를 입력해주세요").trim();
    if (!orderNo) {
        alert("올바른 예약 번호를 입력해야 합니다.");
        return;
    }

    try {
        const response = await fetch("/api/getImpUid", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify({orderNo})
        });

        if (!response.ok) {
            throw new Error("예약 정보를 가져오는데 실패했습니다.");
        }

        const responseData = await response.json();
        const imp_uid = responseData.impUid;

        if (imp_uid) {
            const isConfirmed = confirm("정말 취소 하시겠습니까?");
            if (isConfirmed) {
                const cancelResponse = await fetch(`/payments/cancel/${imp_uid}`, {
                    method: "POST",
                    headers: {"Content-Type": "application/json"},
                    body: JSON.stringify({
                        reason: "고객 요청으로 취소"
                    })
                });

                if (!cancelResponse.ok) {
                    throw new Error("결제 취소에 실패했습니다.");
                }

            }
        } else {
            alert("유효한 예약 번호가 아닙니다.");
        }
    } catch (error) {
        alert(error.message);
    }
}

window.cancelPay = cancelPay;

function setupCancelButton() {
    const cancelButton = document.querySelector(".cancel-button");
    if (cancelButton) {
        cancelButton.addEventListener("click", async function () {
            await cancelPay();
        });
    }
}

// ==================== 할인율 업데이트 ====================
function updateSalePercent(percent) {
    if (percent >= 0 && percent <= 100) {
        paymentData.salePercent = percent;
        updateDisplay();
    }
}