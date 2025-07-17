// 기존 캐러셸 관련 변수들
let currentSlide = 0;
let totalSlides = 0;

// 새로운 좋아요/리뷰 관련 변수들
let currentProductNo = 0;
let selectedRating = 0;
let isLoggedIn = false;

// 🆕 세일 관련 전역 변수 추가
let isActiveSale = false;
let salePrice = 0;
let originalPrice = 0;

document.addEventListener('DOMContentLoaded', function () {
    // 기존 캐러셸 및 탭 초기화
    initializeCarousel();
    initializeTabs();

    // 새로운 기능들 초기화
    initializeProductInteraction();

    // 수량 조절 초기화 추가
    initializeQuantityControls();

    // 쿠폰 div 초기화 추가
    initializeCouponDiv();
});

// === 🔧 수정된 캐러셸 기능들 ===
function initializeCarousel() {
    const slides = document.querySelectorAll('.carousel-slide');
    const thumbnails = document.querySelectorAll('.thumbnail');

    totalSlides = slides.length;
    currentSlide = 0;

    console.log('=== 캐러셸 초기화 ===');
    console.log('총 슬라이드 수:', totalSlides);

    if (totalSlides <= 1) {
        // 이미지가 1개 이하면 컨트롤 숨김
        const controls = document.querySelector('.carousel-controls');
        const thumbnailsContainer = document.querySelector('.image-thumbnails');

        if (controls) controls.style.display = 'none';
        if (thumbnailsContainer) thumbnailsContainer.style.display = 'none';

        console.log('이미지가 1개 이하여서 컨트롤 숨김');
        return;
    }

    // 썸네일 클릭 이벤트 등록
    thumbnails.forEach((thumbnail, index) => {
        thumbnail.addEventListener('click', () => {
            console.log('썸네일 클릭:', index);
            goToSlide(index);
        });
    });

    // 초기 슬라이드 설정
    updateCarousel();

    console.log('캐러셸 초기화 완료');
}

function nextSlide() {
    if (totalSlides <= 1) return;

    currentSlide = (currentSlide + 1) % totalSlides;
    console.log('다음 슬라이드로 이동:', currentSlide);
    updateCarousel();
}

function prevSlide() {
    if (totalSlides <= 1) return;

    currentSlide = (currentSlide - 1 + totalSlides) % totalSlides;
    console.log('이전 슬라이드로 이동:', currentSlide);
    updateCarousel();
}

function goToSlide(slideIndex) {
    if (slideIndex < 0 || slideIndex >= totalSlides) return;

    currentSlide = slideIndex;
    console.log('슬라이드 이동:', currentSlide);
    updateCarousel();
}

// 🔧 썸네일에서 호출하는 함수 (HTML에서 onclick으로 호출)
function goToSlideFromThumbnail(thumbnail) {
    const slideIndex = parseInt(thumbnail.getAttribute('data-slide'));
    console.log('썸네일에서 슬라이드 이동:', slideIndex);
    goToSlide(slideIndex);
}

function updateCarousel() {
    const slides = document.querySelectorAll('.carousel-slide');
    const thumbnails = document.querySelectorAll('.thumbnail');

    console.log('캐러셸 업데이트 - 현재 슬라이드:', currentSlide);

    // 모든 슬라이드 비활성화
    slides.forEach((slide, index) => {
        slide.classList.remove('active');
        if (index === currentSlide) {
            slide.classList.add('active');
            console.log(`슬라이드 ${index} 활성화`);
        }
    });

    // 모든 썸네일 비활성화
    thumbnails.forEach((thumbnail, index) => {
        thumbnail.classList.remove('active');
        if (index === currentSlide) {
            thumbnail.classList.add('active');
            console.log(`썸네일 ${index} 활성화`);
        }
    });
}

function initializeTabs() {
    const hash = window.location.hash.substring(1);
    if (hash && ['info', 'reviews'].includes(hash)) {
        showTab(hash);
    }
}

function showTab(tabName) {
    console.log('탭 전환:', tabName);

    document.querySelectorAll('.tab-btn').forEach(btn => {
        btn.classList.remove('active');
    });

    document.querySelectorAll('.tab-content').forEach(content => {
        content.classList.remove('active');
    });

    const selectedBtn = document.querySelector(`.tab-btn[onclick="showTab('${tabName}')"]`);
    const selectedContent = document.getElementById(`${tabName}-tab`);

    if (selectedBtn && selectedContent) {
        selectedBtn.classList.add('active');
        selectedContent.classList.add('active');

        if (history.pushState) {
            history.pushState(null, null, `#${tabName}`);
        }
    }
}

function goBack() {
    if (window.history.length > 1) {
        window.history.back();
    } else {
        window.location.href = '/';
    }
}

// 키보드 이벤트
document.addEventListener('keydown', function (e) {
    if (e.key === 'ArrowLeft') {
        prevSlide();
    } else if (e.key === 'ArrowRight') {
        nextSlide();
    }
});

// === 새로운 좋아요/리뷰 기능들 ===

function initializeProductInteraction() {
    // 상품 번호 가져오기
    const productNoElement = document.getElementById('product-no');
    if (productNoElement) {
        currentProductNo = parseInt(productNoElement.textContent);
        console.log('현재 상품 번호:', currentProductNo);
    }

    // 🆕 세일 정보 초기화
    initializeSaleInfo();

    // 페이지 초기화
    initializePage();
    loadLikeInfo();
    loadReviews();
    setupReviewForm();
}

// 🆕 세일 정보 초기화 함수
function initializeSaleInfo() {
    const isActiveSaleElement = document.getElementById('is-active-sale');
    const salePriceElement = document.getElementById('sale-price');
    const originalPriceElement = document.getElementById('original-price');

    if (isActiveSaleElement) {
        isActiveSale = isActiveSaleElement.value === 'true';
    }

    if (salePriceElement && salePriceElement.value) {
        salePrice = parseInt(salePriceElement.value);
    }

    if (originalPriceElement) {
        originalPrice = parseInt(originalPriceElement.value);
    }

    console.log('세일 정보 초기화:', {
        isActiveSale: isActiveSale,
        salePrice: salePrice,
        originalPrice: originalPrice
    });

    // 초기 총 가격 설정
    updateTotalPrice();
}

// 🆕 현재 적용 가격 반환 (세일 중이면 세일가, 아니면 원가)
function getCurrentPrice() {
    if (isActiveSale && salePrice > 0) {
        return salePrice;
    }
    return originalPrice;
}

// JWT 토큰 가져오기
function getJwtToken() {
    const token = localStorage.getItem('auth_token') || sessionStorage.getItem('auth_token');
    console.log('JWT 토큰:', token ? '있음' : '없음');
    return token;
}

// 페이지 초기화
function initializePage() {
    const token = getJwtToken();
    isLoggedIn = !!token;

    console.log('로그인 상태:', isLoggedIn);

    const writeBtn = document.getElementById('write-review-btn');
    const loginMessage = document.getElementById('login-required-message');

    if (isLoggedIn && writeBtn) {
        writeBtn.style.display = 'inline-block';
        console.log('리뷰 작성 버튼 표시');
    } else if (!isLoggedIn && loginMessage) {
        loginMessage.style.display = 'block';
        console.log('로그인 메시지 표시');
    }
}

// 좋아요 정보 로드
async function loadLikeInfo() {
    try {
        const token = getJwtToken();
        const headers = {};
        if (token) {
            headers['Authorization'] = 'Bearer ' + token;
        }

        console.log('좋아요 정보 로드 시작:', `/products/${currentProductNo}/like-info`);

        const response = await fetch(`/products/${currentProductNo}/like-info`, {
            headers: headers
        });

        console.log('좋아요 정보 응답 상태:', response.status);

        if (response.ok) {
            const data = await response.json();
            console.log('좋아요 정보:', data);

            const likeCountElement = document.getElementById('like-count');
            if (likeCountElement) {
                likeCountElement.textContent = data.likeCount;
            }

            if (data.isLoggedIn && data.isLiked) {
                const likeBtn = document.getElementById('like-btn');
                const heartIcon = document.getElementById('heart-icon');
                if (likeBtn) likeBtn.classList.add('liked');
                if (heartIcon) heartIcon.textContent = '♥';
            }
        } else {
            console.error('좋아요 정보 로드 실패:', response.status, response.statusText);
        }
    } catch (error) {
        console.error('좋아요 정보 로드 실패:', error);
    }
}

// 좋아요 토글
async function toggleLike() {
    console.log('좋아요 토글 시작, 로그인 상태:', isLoggedIn);

    if (!isLoggedIn) {
        alert('로그인 후 좋아요를 눌러주세요!');
        window.location.href = '/login?redirect=' + encodeURIComponent(window.location.pathname);
        return;
    }

    try {
        const token = getJwtToken();
        console.log('좋아요 요청 전송:', `/products/${currentProductNo}/like`);

        const response = await fetch(`/products/${currentProductNo}/like`, {
            method: 'POST',
            headers: {
                'Authorization': 'Bearer ' + token
            }
        });

        console.log('좋아요 응답 상태:', response.status);

        if (response.status === 401) {
            alert('로그인이 만료되었습니다. 다시 로그인해주세요.');
            localStorage.removeItem('token');
            sessionStorage.removeItem('token');
            window.location.href = '/login?redirect=' + encodeURIComponent(window.location.pathname);
            return;
        }

        if (response.ok) {
            const data = await response.json();
            console.log('좋아요 응답 데이터:', data);

            const likeCountElement = document.getElementById('like-count');
            if (likeCountElement) {
                likeCountElement.textContent = data.likeCount;
            }

            const likeBtn = document.getElementById('like-btn');
            const heartIcon = document.getElementById('heart-icon');

            if (data.isLiked) {
                if (likeBtn) likeBtn.classList.add('liked');
                if (heartIcon) heartIcon.textContent = '♥';
            } else {
                if (likeBtn) likeBtn.classList.remove('liked');
                if (heartIcon) heartIcon.textContent = '♡';
            }
        } else {
            const errorData = await response.text();
            console.error('좋아요 실패:', response.status, errorData);
            alert('좋아요 처리에 실패했습니다.');
        }
    } catch (error) {
        console.error('좋아요 토글 실패:', error);
        alert('오류가 발생했습니다. 다시 시도해주세요.');
    }
}

// 리뷰 목록 로드
async function loadReviews() {
    try {
        console.log('리뷰 목록 로드 시작:', `/products/${currentProductNo}/reviews`);

        const response = await fetch(`/products/${currentProductNo}/reviews`);

        console.log('리뷰 응답 상태:', response.status);

        if (response.ok) {
            const data = await response.json();
            console.log('리뷰 데이터:', data);

            // 리뷰 수 업데이트
            const reviewCountElement = document.getElementById('review-count');
            const reviewCountTabElement = document.getElementById('review-count-tab');
            const avgRatingElement = document.getElementById('avg-rating');

            if (reviewCountElement) reviewCountElement.textContent = data.reviewCount;
            if (reviewCountTabElement) reviewCountTabElement.textContent = data.reviewCount;
            if (avgRatingElement) avgRatingElement.textContent = data.avgRating;

            displayReviews(data.reviews);
        }
    } catch (error) {
        console.error('리뷰 로드 실패:', error);
    }
}

// 리뷰 목록 표시
function displayReviews(reviews) {
    const reviewsList = document.getElementById('reviews-list');
    const noReviews = document.getElementById('no-reviews');

    if (!reviewsList) return;

    if (reviews.length === 0) {
        if (noReviews) noReviews.style.display = 'block';
        return;
    }

    if (noReviews) noReviews.style.display = 'none';

    const reviewsHtml = reviews.map(review => `
        <div class="review-item">
            <div class="review-header">
                <div class="review-rating">${'★'.repeat(review.rating)}${'☆'.repeat(5 - review.rating)}</div>
            </div>
            <div class="review-content">${escapeHtml(review.content)}</div>
        </div>
    `).join('');

    reviewsList.innerHTML = `<div id="no-reviews" style="display: none;">
        <p>아직 작성된 리뷰가 없습니다.</p>
        <p>첫 번째 리뷰를 작성해보세요!</p>
    </div>${reviewsHtml}`;
}

// HTML 이스케이프 함수
function escapeHtml(text) {
    const map = {
        '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#039;'
    };
    return text.replace(/[&<>"']/g, function (m) {
        return map[m];
    });
}

// 리뷰 폼 설정
function setupReviewForm() {
    const writeBtn = document.getElementById('write-review-btn');
    const formContainer = document.getElementById('review-form-container');
    const submitBtn = document.getElementById('submit-review-btn');
    const cancelBtn = document.getElementById('cancel-review-btn');
    const stars = document.querySelectorAll('.rating-input .star');

    if (writeBtn && formContainer) {
        writeBtn.addEventListener('click', () => {
            console.log('리뷰 작성 버튼 클릭');
            formContainer.style.display = 'block';
            writeBtn.style.display = 'none';
        });
    }

    if (cancelBtn && formContainer && writeBtn) {
        cancelBtn.addEventListener('click', () => {
            formContainer.style.display = 'none';
            writeBtn.style.display = 'inline-block';
            resetReviewForm();
        });
    }

    // 별점 선택
    stars.forEach(star => {
        star.addEventListener('click', () => {
            selectedRating = parseInt(star.dataset.rating);
            console.log('선택된 별점:', selectedRating);
            updateStarDisplay();
        });
    });

    if (submitBtn) {
        submitBtn.addEventListener('click', submitReview);
    }
}

// 별점 표시 업데이트
function updateStarDisplay() {
    const stars = document.querySelectorAll('.rating-input .star');
    stars.forEach((star, index) => {
        if (index < selectedRating) {
            star.classList.add('active');
        } else {
            star.classList.remove('active');
        }
    });
}

// 리뷰 작성
async function submitReview() {
    if (selectedRating === 0) {
        alert('별점을 선택해주세요.');
        return;
    }

    const contentElement = document.getElementById('review-content');
    if (!contentElement) return;

    const content = contentElement.value.trim();
    if (!content) {
        alert('리뷰 내용을 입력해주세요.');
        return;
    }

    try {
        const token = getJwtToken();
        console.log('리뷰 작성 요청:', `/products/${currentProductNo}/review`);

        const response = await fetch(`/products/${currentProductNo}/review`, {
            method: 'POST',
            headers: {
                'Authorization': 'Bearer ' + token,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                rating: selectedRating,
                content: content
            })
        });

        console.log('리뷰 작성 응답 상태:', response.status);

        if (response.status === 401) {
            alert('로그인이 만료되었습니다. 다시 로그인해주세요.');
            localStorage.removeItem('token');
            sessionStorage.removeItem('token');
            window.location.href = '/login?redirect=' + encodeURIComponent(window.location.pathname);
            return;
        }

        if (response.ok) {
            const data = await response.json();
            console.log('리뷰 작성 성공:', data);
            alert('리뷰가 작성되었습니다.');

            // 폼 숨기기 및 초기화
            const formContainer = document.getElementById('review-form-container');
            const writeBtn = document.getElementById('write-review-btn');

            if (formContainer) formContainer.style.display = 'none';
            if (writeBtn) writeBtn.style.display = 'inline-block';

            resetReviewForm();

            // 리뷰 목록 새로고침
            loadReviews();
        } else {
            const data = await response.json();
            console.error('리뷰 작성 실패:', data);
            alert(data.message || '리뷰 작성에 실패했습니다.');
        }
    } catch (error) {
        console.error('리뷰 작성 실패:', error);
        alert('오류가 발생했습니다. 다시 시도해주세요.');
    }
}

// 리뷰 폼 초기화
function resetReviewForm() {
    selectedRating = 0;
    const contentElement = document.getElementById('review-content');
    if (contentElement) {
        contentElement.value = '';
    }

    document.querySelectorAll('.rating-input .star').forEach(star => {
        star.classList.remove('active');
    });
}

// === 수량 조절 기능 ===

// 현재 선택된 수량 가져오기
function getSelectedQuantity() {
    const quantityInput = document.getElementById('quantity');
    return quantityInput ? parseInt(quantityInput.value) || 1 : 1;
}

// 총 가격 업데이트 (수정된 버전)
function updateTotalPrice() {
    const quantityInput = document.getElementById('quantity');
    const totalPriceElement = document.getElementById('total-price');

    if (quantityInput && totalPriceElement) {
        const quantity = parseInt(quantityInput.value) || 1;
        const currentUnitPrice = getCurrentPrice(); // 🆕 세일 가격 반영
        const totalPrice = currentUnitPrice * quantity;

        totalPriceElement.textContent = totalPrice.toLocaleString() + '원';

        console.log('가격 업데이트:', {
            quantity: quantity,
            unitPrice: currentUnitPrice,
            totalPrice: totalPrice,
            isActiveSale: isActiveSale
        });
    }
}

// 수량 조절 함수
function initializeQuantityControls() {
    const minusBtn = document.getElementById('minus');
    const plusBtn = document.getElementById('plus');
    const quantityInput = document.getElementById('quantity');

    if (minusBtn && plusBtn && quantityInput) {
        // 기본값 설정
        quantityInput.value = 1;
        updateTotalPrice();

        // 마이너스 버튼
        minusBtn.onclick = function () {
            let current = parseInt(quantityInput.value) || 1;
            if (current > 1) {
                quantityInput.value = current - 1;
                updateTotalPrice();
            }
        };

        // 플러스 버튼
        plusBtn.onclick = function () {
            let current = parseInt(quantityInput.value) || 1;
            if (current < 99) { // 최대 99개 제한
                quantityInput.value = current + 1;
                updateTotalPrice();
            }
        };

        // 직접 입력 시 유효성 검사
        quantityInput.addEventListener('input', function () {
            let value = parseInt(this.value);
            if (isNaN(value) || value < 1) {
                this.value = 1;
            } else if (value > 99) {
                this.value = 99;
            }
            updateTotalPrice();
        });

        // 포커스 아웃 시 값 정리
        quantityInput.addEventListener('blur', function () {
            if (!this.value || parseInt(this.value) < 1) {
                this.value = 1;
                updateTotalPrice();
            }
        });
    }
}

// 🆕 수정된 주문하기 함수 (세일 가격 정보를 정확히 전달)
async function orderProductWithQuantity() {
    console.log('주문하기 버튼 클릭, 로그인 상태:', isLoggedIn);

    if (!isLoggedIn) {
        alert('로그인 후 주문해주세요!');
        window.location.href = '/login?redirect=' + encodeURIComponent(window.location.pathname);
        return;
    }

    // 현재 선택된 수량 가져오기
    const quantity = getSelectedQuantity();
    const productName = document.getElementById('product-name').textContent;
    const unitPrice = getCurrentPrice(); // 이미 세일이 적용된 최종 단가
    const totalPrice = unitPrice * quantity;

    try {
        // 🔥 수정: 결제 페이지에서 추가 할인이 적용되지 않도록 salePercentage를 0으로 설정
        const orderInfo = {
            productNo: currentProductNo,
            productName: productName,
            unitPrice: unitPrice, // 이미 세일이 적용된 가격
            quantity: quantity,
            totalPrice: totalPrice,
            isActiveSale: isActiveSale,
            salePercentage: 0, // 🔥 중요: 이미 할인된 가격이므로 추가 할인 방지
            originalPrice: originalPrice, // 원래 가격 정보는 참조용으로만 전달
            finalUnitPrice: unitPrice // 최종 단가 명시
        };

        sessionStorage.setItem('orderInfo', JSON.stringify(orderInfo));
        console.log('주문 정보 세션 저장:', orderInfo);

        // 결제 페이지로 바로 이동
        if (confirm('결제를 진행하시겠습니까?')) {
            window.location.href = '/goPay';
        }

    } catch (error) {
        console.error('주문 처리 실패:', error);
        alert('오류가 발생했습니다. 다시 시도해주세요.');
    }
}

// 기존 orderProduct 함수도 유지 (하위 호환성)
async function orderProduct() {
    return orderProductWithQuantity();
}

// 장바구니에 담기 (세일 가격 적용 수정)
async function addToCartWithQuantity() {
    const quantity = getSelectedQuantity();
    const productName = document.getElementById('product-name').textContent;
    const productPrice = getCurrentPrice(); // 🆕 세일 가격 적용

    // 첫 번째 이미지 URL 가져오기
    const firstImage = document.querySelector('.product-image');
    const imageUrl = firstImage ? firstImage.src : '';

    const cartData = {
        productNo: currentProductNo,
        productName: productName,
        price: productPrice, // 🆕 세일 가격 적용
        originalPrice: originalPrice, // 🆕 원가 정보도 전송
        isActiveSale: isActiveSale, // 🆕 세일 여부 전송
        quantity: quantity,
        imageUrls: imageUrl
    };

    try {
        const response = await fetch('/cart/add', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(cartData)
        });

        const data = await response.json();

        if (response.ok && data.success) {
            let alertMessage = `장바구니에 추가되었습니다!\n\n상품: ${productName}\n수량: ${quantity}개`;

            if (isActiveSale) {
                alertMessage += `\n세일가: ${productPrice.toLocaleString()}원 (원가: ${originalPrice.toLocaleString()}원)`;
            } else {
                alertMessage += `\n가격: ${productPrice.toLocaleString()}원`;
            }

            alert(alertMessage);

            // 장바구니로 이동할지 묻기
            if (confirm('장바구니를 확인하시겠습니까?')) {
                window.location.href = '/cart';
            }
        } else {
            alert(data.message || '장바구니 추가에 실패했습니다.');
        }
    } catch (error) {
        console.error('장바구니 추가 실패:', error);
        alert('오류가 발생했습니다. 다시 시도해주세요.');
    }
}

function initializeCouponDiv() {
    const couponDiv = document.querySelector('.getSaleCoupon');
    couponDiv.style.display = "block";
    couponDiv.addEventListener('click', function () {
        const link = this.querySelector('a');
        if (link) {
            window.location.href = link.href;
        }
    });
    const response = fetch("/api/auth/check-Pay");
    const result = response.json();
    if (result.ok) {
        couponDiv.style.display="none";
    }
}