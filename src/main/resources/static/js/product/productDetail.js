// 기존 캐러셸 관련 변수들
let currentSlide = 0;
let totalSlides = 0;

// 새로운 좋아요/리뷰 관련 변수들
let currentProductNo = 0;
let selectedRating = 0;
let isLoggedIn = false;

document.addEventListener('DOMContentLoaded', function () {
    // 기존 캐러셸 및 탭 초기화
    initializeCarousel();
    initializeTabs();

    // 새로운 기능들 초기화
    initializeProductInteraction();
});

// === 기존 캐러셸 기능들 ===
function initializeCarousel() {
    const slides = document.querySelectorAll('.carousel-slide');
    totalSlides = slides.length;

    console.log('=== 캐러셸 디버깅 ===');
    console.log('총 슬라이드 수:', totalSlides);
    console.log('슬라이드 요소들:', slides);

    slides.forEach((slide, index) => {
        console.log(`슬라이드 ${index}:`, slide);
        console.log(`- data-slide:`, slide.getAttribute('data-slide'));
        console.log(`- active class:`, slide.classList.contains('active'));
        const img = slide.querySelector('img');
        if (img) {
            console.log(`- 이미지 src:`, img.src);
        }
    });

    if (totalSlides <= 1) {
        const controls = document.querySelector('.carousel-controls');
        const indicators = document.querySelector('.carousel-indicators');
        const thumbnails = document.querySelector('.image-thumbnails');

        if (controls) controls.style.display = 'none';
        if (indicators) indicators.style.display = 'none';
        if (thumbnails) thumbnails.style.display = 'none';

        console.log('이미지가 1개 이하여서 컨트롤 숨김');
    } else {
        console.log('이미지가 여러개 - 캐러셸 활성화');
    }
}

function nextSlide() {
    console.log('다음 슬라이드:', currentSlide, '->', (currentSlide + 1) % totalSlides);
    if (totalSlides <= 1) return;
    currentSlide = (currentSlide + 1) % totalSlides;
    updateCarousel();
}

function prevSlide() {
    console.log('이전 슬라이드:', currentSlide, '->', (currentSlide - 1 + totalSlides) % totalSlides);
    if (totalSlides <= 1) return;
    currentSlide = (currentSlide - 1 + totalSlides) % totalSlides;
    updateCarousel();
}

function goToSlide(slideIndex) {
    console.log('슬라이드 이동:', currentSlide, '->', slideIndex);
    if (slideIndex >= 0 && slideIndex < totalSlides) {
        currentSlide = slideIndex;
        updateCarousel();
    }
}

function goToSlideFromThumbnail(thumbnail) {
    const slideIndex = parseInt(thumbnail.getAttribute('data-slide'));
    console.log('썸네일에서 슬라이드 이동:', slideIndex);
    goToSlide(slideIndex);
}

function updateCarousel() {
    console.log('캐러셸 업데이트 - 현재 슬라이드:', currentSlide);

    const slides = document.querySelectorAll('.carousel-slide');
    const indicators = document.querySelectorAll('.indicator');
    const thumbnails = document.querySelectorAll('.thumbnail');

    slides.forEach((slide, index) => {
        const isActive = index === currentSlide;
        slide.classList.toggle('active', isActive);
        if (isActive) {
            console.log(`슬라이드 ${index} 활성화됨`);
        }
    });

    indicators.forEach((indicator, index) => {
        indicator.classList.toggle('active', index === currentSlide);
    });

    thumbnails.forEach((thumbnail, index) => {
        thumbnail.classList.toggle('active', index === currentSlide);
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

    // 페이지 초기화
    initializePage();
    loadLikeInfo();
    loadReviews();
    setupReviewForm();
}

// JWT 토큰 가져오기
function getJwtToken() {
    const token = localStorage.getItem('token') || sessionStorage.getItem('token');
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

// 좋아요 토글 (기존 함수 대체)
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
            method: 'POST', headers: {
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
            method: 'POST', headers: {
                'Authorization': 'Bearer ' + token, 'Content-Type': 'application/json'
            }, body: JSON.stringify({
                rating: selectedRating, content: content
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

// 수량 조절
document.addEventListener('DOMContentLoaded', function() {
    const minusBtn = document.getElementById('minus');
    const plusBtn = document.getElementById('plus');
    const quantity = document.getElementById('quantity');

    minusBtn.onclick = function(){
        let current = parseInt(quantity.value) || 1;
        if(current > 1){
            quantity.value = current - 1;
        }
    };

    plusBtn.onclick = function(){
        let current = parseInt(quantity.value) || 1;
        quantity.value = current + 1;
    };
});
