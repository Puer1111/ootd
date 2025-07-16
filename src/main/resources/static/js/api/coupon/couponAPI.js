document.addEventListener('DOMContentLoaded', () => {
    const issueCouponBtn = document.getElementById('issue-coupon-btn');

    console.log('AuthManager.getToken() 호출 결과:', AuthManager.getToken());
    console.log('localStorage.getItem(\'auth_token\') 직접 호출 결과:', localStorage.getItem('auth_token'));
    console.log('AuthManager.isLoggedIn() 호출 결과:', AuthManager.isLoggedIn());

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
                    // const result = await response.json();
                    alert( '쿠폰이 발급되었습니다!');
                    window.location.reload(); 
                } else {
                    const errorData = await response.json();
                    alert('쿠폰 발급에 실패했습니다.');
                }
            } catch (error) {
                console.error('Error issuing coupon:', error);
                alert('오류가 발생했습니다. 다시 시도해주세요.');
            }
        });
    }
});