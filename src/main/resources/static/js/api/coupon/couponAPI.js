document.addEventListener('DOMContentLoaded', () => {
    const issueCouponBtn = document.getElementById('issue-coupon-btn');
    let availableCoupons = []; // 조회된 쿠폰 목록을 저장할 전역 변수

    if (issueCouponBtn) {
        issueCouponBtn.addEventListener('click', async () => {
            if (!AuthManager.isLoggedIn()) {
                alert('로그인이 필요합니다.');
                AuthManager.redirectToLogin();
                return;
            }

            const couponId = issueCouponBtn.dataset.couponId;
            if (!couponId) {
                alert('쿠폰 정보를 찾을 수 없습니다.');
                return;
            }

            try {
                const response = await AuthManager.authenticatedFetch(`/api/coupons/${couponId}/issue`, {
                    method: 'POST',
                });

                if (response.ok) {
                    alert('쿠폰이 발급되었습니다!');
                    window.location.reload();
                } else {
                    const errorData = await response.json();
                    alert('쿠폰 발급에 실패했습니다.');
                }
            } catch (error) {
                console.error('Error issuing coupon:', error);
                alert('이미 발급받은 쿠폰입니다.');
            }
        });
    }

    const modalCouponBtn = document.getElementById('check-available-coupon');
    const couponModal = document.getElementById('coupon-modal');
    const couponModalClose = document.querySelector('.coupon-modal-close');
    const couponListContainer = document.getElementById('coupon-list-container');
    const salePercentElement = document.getElementById('salePercent');
    const discountAmountElement = document.getElementById('discountAmount');
    const totalPriceElement = document.getElementById('totalPrice');
    const productPriceElement = document.getElementById('productPrice');
    const quantityElement = document.getElementById('quantity');

    // 결제 금액 업데이트 함수 (payment.js의 로직과 유사하게 구현)
    function updatePaymentDetails() {
        const productPrice = parseFloat(productPriceElement.textContent);
        const quantity = parseInt(quantityElement.textContent);
        const currentSalePercent = parseFloat(salePercentElement.textContent);

        const originalTotal = productPrice * quantity;
        const discountAmount = originalTotal * (currentSalePercent / 100);
        const finalPrice = originalTotal - discountAmount;

        discountAmountElement.textContent = discountAmount.toFixed(0); // 소수점 제거
        totalPriceElement.textContent = finalPrice.toFixed(0); // 소수점 제거
    }

    if (modalCouponBtn) {
        modalCouponBtn.addEventListener('click', async () => {
            if (!AuthManager.isLoggedIn()) {
                alert('로그인이 필요합니다.');
                AuthManager.redirectToLogin();
                return;
            }
            try {
                const response = await AuthManager.authenticatedFetch('/api/coupons/lookup');
                if (response.ok) {
                    availableCoupons = await response.json(); // 조회된 쿠폰 목록 저장
                    couponListContainer.innerHTML = ''; // 기존 목록 초기화
                    if (availableCoupons.length > 0) {
                        availableCoupons.forEach(coupon => {
                            const couponItemWrapper = document.createElement('div');
                            couponItemWrapper.classList.add('coupon-item');

                            const couponNameDiv = document.createElement('div');
                            couponNameDiv.textContent = `쿠폰 이름: ${coupon.couponName}`;

                            const discountArea = document.createElement('div');
                            discountArea.classList.add('discount-area');
                            discountArea.dataset.discountRate = coupon.discountRate; // 할인율을 dataset에 저장

                            const couponDiscountSpan = document.createElement('span');
                            couponDiscountSpan.textContent = `${coupon.discountRate}%`;
                            couponDiscountSpan.classList.add('coupon-discount-text');

                            const radioInput = document.createElement('input');
                            radioInput.type = 'radio';
                            radioInput.name = 'selectedCoupon';
                            radioInput.value = coupon.couponId;
                            radioInput.id = `couponRadio_${coupon.couponId}`;

                            discountArea.appendChild(couponDiscountSpan);
                            discountArea.appendChild(radioInput);

                            // discountArea 클릭 시 radioInput 선택 및 할인율 업데이트
                            discountArea.addEventListener('click', (event) => {
                                radioInput.checked = true;
                                const rate = parseFloat(event.currentTarget.dataset.discountRate);
                                console.log('couponAPI.js: Discount area clicked. Updating salePercent with:', rate);
                                window.updateSalePercent(rate);
                            });

                            const couponRemainDaysDiv = document.createElement('div');
                            couponRemainDaysDiv.textContent = `남은기간: ${coupon.remainingDays}일`;

                            couponItemWrapper.appendChild(discountArea);
                            couponItemWrapper.appendChild(couponNameDiv);
                            couponItemWrapper.appendChild(couponRemainDaysDiv);

                            couponListContainer.appendChild(couponItemWrapper);
                        });
                    } else {
                        couponListContainer.textContent = '사용 가능한 쿠폰이 없습니다.';
                    }
                } else {
                    const errorData = await response.json();
                    alert(`쿠폰 조회 실패: ${errorData.message}`);
                }
            } catch (error) {
                console.error('Error fetching coupons:', error);
                alert('쿠폰 조회 중 오류가 발생했습니다.');
            }
            couponModal.style.display = 'block'; // 모달 열기
        });
    }

    if (couponModalClose) {
        couponModalClose.addEventListener('click', () => {
            couponModal.style.display = 'none'; // 모달 닫기
        });
    }

    window.addEventListener('click', (event) => {
        if (event.target === couponModal) {
            couponModal.style.display = 'none'; // 모달 외부 클릭 시 닫기
        }
    });
});
