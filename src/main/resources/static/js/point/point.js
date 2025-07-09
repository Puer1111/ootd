/**
 * 적립금 페이지 메인 JavaScript
 * 경로: src/main/resources/static/js/point/points.js
 */

// ==================== 전역 변수 ====================
let userPointsData = {
    totalPoints: 0,
    availablePoints: 0,
    usedPoints: 0
};

let pointsHistory = [];
let currentPage = 0;
const pageSize = 20;

// ==================== 페이지 초기화 ====================
document.addEventListener('DOMContentLoaded', function() {
    console.log('적립금 페이지 초기화 시작');

    // 토큰 확인
    const token = PointsAPI.getToken();
    if (!token) {
        showLoginRequired();
        return;
    }

    // 페이지 초기화
    initializePage();
});

/**
 * 페이지 초기화
 */
async function initializePage() {
    try {
        showLoading(true);

        // 적립금 정보 로드
        await loadPointsInfo();

        // 적립금 내역 로드
        await loadPointsHistory();

        // 적립금 통계 로드
        await loadPointsStatistics();

        // 이벤트 리스너 설정
        setupEventListeners();

        showLoading(false);

    } catch (error) {
        console.error('페이지 초기화 실패:', error);
        showError('페이지를 불러오는 중 오류가 발생했습니다.');
        showLoading(false);
    }
}

// ==================== 적립금 정보 로드 ====================

/**
 * 적립금 정보 로드
 */
async function loadPointsInfo() {
    try {
        const response = await PointsAPI.getMyPoints();

        if (response.success) {
            userPointsData = {
                totalPoints: response.totalPoints,
                availablePoints: response.availablePoints,
                usedPoints: response.usedPoints
            };

            updatePointsDisplay();
            console.log('적립금 정보 로드 완료:', userPointsData);
        }
    } catch (error) {
        console.error('적립금 정보 로드 실패:', error);
        throw error;
    }
}

/**
 * 적립금 표시 업데이트
 */
function updatePointsDisplay() {
    const elements = {
        totalPoints: document.getElementById('total-points'),
        availablePoints: document.getElementById('available-points'),
        usedPoints: document.getElementById('used-points'),
        availablePointsMain: document.getElementById('available-points-main')
    };

    if (elements.totalPoints) {
        elements.totalPoints.textContent = PointsAPI.formatNumber(userPointsData.totalPoints);
    }
    if (elements.availablePoints) {
        elements.availablePoints.textContent = PointsAPI.formatNumber(userPointsData.availablePoints);
    }
    if (elements.usedPoints) {
        elements.usedPoints.textContent = PointsAPI.formatNumber(userPointsData.usedPoints);
    }
    if (elements.availablePointsMain) {
        elements.availablePointsMain.textContent = PointsAPI.formatNumber(userPointsData.availablePoints);
    }
}

// ==================== 적립금 내역 로드 ====================

/**
 * 적립금 내역 로드
 */
async function loadPointsHistory(page = 0) {
    try {
        const response = await PointsAPI.getPointHistory(page, pageSize);

        if (response.success) {
            pointsHistory = response.history;
            currentPage = response.currentPage;

            displayPointsHistory();
            updatePagination(response);
            console.log(`적립금 내역 로드 완료: ${pointsHistory.length}개 항목`);
        }
    } catch (error) {
        console.error('적립금 내역 로드 실패:', error);
        showError('적립금 내역을 불러올 수 없습니다.');
    }
}

/**
 * 적립금 내역 표시
 */
function displayPointsHistory() {
    const historyContainer = document.getElementById('points-history-list');
    if (!historyContainer) return;

    if (pointsHistory.length === 0) {
        historyContainer.innerHTML = `
            <div class="empty-history">
                <div class="empty-icon">📋</div>
                <h3>적립금 내역이 없습니다</h3>
                <p>첫 번째 적립금을 받아보세요!</p>
            </div>
        `;
        return;
    }

    const historyHtml = pointsHistory.map(history => `
        <div class="history-item ${history.points > 0 ? 'earn' : 'use'}">
            <div class="history-main">
                <div class="history-type">
                    <span class="type-badge ${getTypeBadgeClass(history.pointType)}">
                        ${getTypeDisplayName(history.pointType)}
                    </span>
                </div>
                <div class="history-description">
                    ${history.description || '설명 없음'}
                </div>
            </div>
            <div class="history-details">
                <div class="history-points ${history.points > 0 ? 'positive' : 'negative'}">
                    ${history.points > 0 ? '+' : ''}${PointsAPI.formatNumber(history.points)}원
                </div>
                <div class="history-date">
                    ${PointsAPI.formatDate(history.createdAt)}
                </div>
            </div>
        </div>
    `).join('');

    historyContainer.innerHTML = historyHtml;
}

/**
 * 타입 배지 클래스 반환
 */
function getTypeBadgeClass(pointType) {
    const typeClasses = {
        'EARN_PURCHASE': 'earn-purchase',
        'EARN_SIGNUP': 'earn-signup',
        'EARN_EVENT': 'earn-event',
        'EARN_ADMIN': 'earn-admin',
        'USE_PURCHASE': 'use-purchase',
        'USE_CANCEL': 'use-cancel',
        'REFUND': 'refund',
        'EXPIRE': 'expire'
    };
    return typeClasses[pointType] || 'default';
}

/**
 * 타입 표시명 반환
 */
function getTypeDisplayName(pointType) {
    const typeNames = {
        'EARN_PURCHASE': '구매 적립',
        'EARN_SIGNUP': '가입 적립',
        'EARN_EVENT': '이벤트 적립',
        'EARN_ADMIN': '관리자 지급',
        'USE_PURCHASE': '구매 사용',
        'USE_CANCEL': '취소 환원',
        'REFUND': '환불',
        'EXPIRE': '만료'
    };
    return typeNames[pointType] || pointType;
}

// ==================== 적립금 통계 로드 ====================

/**
 * 적립금 통계 로드
 */
async function loadPointsStatistics() {
    try {
        const response = await PointsAPI.getPointsStatistics();

        if (response.success) {
            displayPointsStatistics(response.statistics);
            console.log('적립금 통계 로드 완료');
        }
    } catch (error) {
        console.error('적립금 통계 로드 실패:', error);
        // 통계는 필수가 아니므로 에러를 표시하지 않음
    }
}

/**
 * 적립금 통계 표시
 */
function displayPointsStatistics(statistics) {
    const elements = {
        totalEarned: document.getElementById('total-earned'),
        totalUsedHistory: document.getElementById('total-used-history'),
        purchaseEarnCount: document.getElementById('purchase-earn-count'),
        purchaseUseCount: document.getElementById('purchase-use-count')
    };

    if (elements.totalEarned && statistics.totalEarned !== undefined) {
        elements.totalEarned.textContent = PointsAPI.formatNumber(statistics.totalEarned);
    }
    if (elements.totalUsedHistory && statistics.totalUsedHistory !== undefined) {
        elements.totalUsedHistory.textContent = PointsAPI.formatNumber(statistics.totalUsedHistory);
    }
    if (elements.purchaseEarnCount && statistics.purchaseEarnCount !== undefined) {
        elements.purchaseEarnCount.textContent = statistics.purchaseEarnCount;
    }
    if (elements.purchaseUseCount && statistics.purchaseUseCount !== undefined) {
        elements.purchaseUseCount.textContent = statistics.purchaseUseCount;
    }
}

// ==================== 페이지네이션 ====================

/**
 * 페이지네이션 업데이트
 */
function updatePagination(response) {
    const paginationContainer = document.getElementById('pagination');
    if (!paginationContainer) return;

    const totalPages = response.totalPages;
    const currentPage = response.currentPage;
    const hasNext = response.hasNext;
    const hasPrevious = response.hasPrevious;

    if (totalPages <= 1) {
        paginationContainer.style.display = 'none';
        return;
    }

    paginationContainer.style.display = 'block';

    let paginationHtml = '';

    // 이전 페이지 버튼
    if (hasPrevious) {
        paginationHtml += `<button class="page-btn" onclick="loadPointsHistory(${currentPage - 1})">이전</button>`;
    }

    // 페이지 번호들
    const startPage = Math.max(0, currentPage - 2);
    const endPage = Math.min(totalPages - 1, currentPage + 2);

    for (let i = startPage; i <= endPage; i++) {
        const isActive = i === currentPage;
        paginationHtml += `
            <button class="page-btn ${isActive ? 'active' : ''}" 
                    onclick="loadPointsHistory(${i})">${i + 1}</button>
        `;
    }

    // 다음 페이지 버튼
    if (hasNext) {
        paginationHtml += `<button class="page-btn" onclick="loadPointsHistory(${currentPage + 1})">다음</button>`;
    }

    paginationContainer.innerHTML = paginationHtml;
}

// ==================== 이벤트 리스너 ====================

/**
 * 이벤트 리스너 설정
 */
function setupEventListeners() {
    // 새로고침 버튼
    const refreshBtn = document.getElementById('refresh-btn');
    if (refreshBtn) {
        refreshBtn.addEventListener('click', function() {
            refreshPage();
        });
    }

    // 필터 버튼들
    const filterBtns = document.querySelectorAll('.filter-btn');
    filterBtns.forEach(btn => {
        btn.addEventListener('click', function() {
            const filterType = this.dataset.filter;
            filterHistory(filterType);
        });
    });

    // 기간 선택 버튼들
    const periodBtns = document.querySelectorAll('.period-btn');
    periodBtns.forEach(btn => {
        btn.addEventListener('click', function() {
            const days = parseInt(this.dataset.days);
            loadRecentActivity(days);
        });
    });
}

// ==================== 페이지 기능 ====================

/**
 * 페이지 새로고침
 */
async function refreshPage() {
    try {
        showLoading(true);
        await loadPointsInfo();
        await loadPointsHistory(0);
        await loadPointsStatistics();
        showLoading(false);
        showSuccess('페이지가 새로고침되었습니다.');
    } catch (error) {
        console.error('페이지 새로고침 실패:', error);
        showError('새로고침 중 오류가 발생했습니다.');
        showLoading(false);
    }
}

/**
 * 내역 필터링
 */
function filterHistory(filterType) {
    const filterBtns = document.querySelectorAll('.filter-btn');
    filterBtns.forEach(btn => {
        btn.classList.remove('active');
        if (btn.dataset.filter === filterType) {
            btn.classList.add('active');
        }
    });

    let filteredHistory;

    switch (filterType) {
        case 'all':
            filteredHistory = pointsHistory;
            break;
        case 'earn':
            filteredHistory = pointsHistory.filter(h => h.points > 0);
            break;
        case 'use':
            filteredHistory = pointsHistory.filter(h => h.points < 0);
            break;
        default:
            filteredHistory = pointsHistory;
    }

    // 임시로 필터된 내역을 저장하고 표시
    const originalHistory = pointsHistory;
    pointsHistory = filteredHistory;
    displayPointsHistory();
    pointsHistory = originalHistory;
}

/**
 * 최근 활동 로드
 */
async function loadRecentActivity(days) {
    try {
        const response = await PointsAPI.getRecentActivity(days);

        if (response.success) {
            // 기간 버튼 활성화 상태 변경
            const periodBtns = document.querySelectorAll('.period-btn');
            periodBtns.forEach(btn => {
                btn.classList.remove('active');
                if (parseInt(btn.dataset.days) === days) {
                    btn.classList.add('active');
                }
            });

            // 최근 활동 표시
            displayRecentActivity(response.recentActivity, days);
        }
    } catch (error) {
        console.error('최근 활동 로드 실패:', error);
        showError('최근 활동을 불러올 수 없습니다.');
    }
}

/**
 * 최근 활동 표시
 */
function displayRecentActivity(recentActivity, days) {
    const recentContainer = document.getElementById('recent-activity');
    if (!recentContainer) return;

    if (recentActivity.length === 0) {
        recentContainer.innerHTML = `
            <div class="empty-activity">
                <p>최근 ${days}일간 적립금 활동이 없습니다.</p>
            </div>
        `;
        return;
    }

    const activityHtml = recentActivity.slice(0, 5).map(activity => `
        <div class="activity-item ${activity.points > 0 ? 'earn' : 'use'}">
            <div class="activity-description">${activity.description}</div>
            <div class="activity-points ${activity.points > 0 ? 'positive' : 'negative'}">
                ${activity.points > 0 ? '+' : ''}${PointsAPI.formatNumber(activity.points)}원
            </div>
            <div class="activity-date">${PointsAPI.formatDate(activity.createdAt)}</div>
        </div>
    `).join('');

    recentContainer.innerHTML = `
        <div class="recent-header">
            <h4>최근 ${days}일 활동 (${recentActivity.length}건)</h4>
        </div>
        <div class="recent-list">
            ${activityHtml}
        </div>
    `;
}

// ==================== UI 상태 관리 ====================

/**
 * 로딩 상태 표시/숨김
 */
function showLoading(show) {
    const loadingElement = document.getElementById('loading');
    if (loadingElement) {
        loadingElement.style.display = show ? 'block' : 'none';
    }
}

/**
 * 에러 메시지 표시
 */
function showError(message) {
    const errorElement = document.getElementById('error-message');
    if (errorElement) {
        errorElement.textContent = message;
        errorElement.style.display = 'block';
        setTimeout(() => {
            errorElement.style.display = 'none';
        }, 5000);
    } else {
        alert('오류: ' + message);
    }
}

/**
 * 성공 메시지 표시
 */
function showSuccess(message) {
    const successElement = document.getElementById('success-message');
    if (successElement) {
        successElement.textContent = message;
        successElement.style.display = 'block';
        setTimeout(() => {
            successElement.style.display = 'none';
        }, 3000);
    } else {
        // 성공 메시지를 위한 임시 알림 생성
        const notification = document.createElement('div');
        notification.className = 'success-notification';
        notification.textContent = message;
        notification.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            background: #4CAF50;
            color: white;
            padding: 15px 20px;
            border-radius: 5px;
            z-index: 1000;
            box-shadow: 0 2px 10px rgba(0,0,0,0.2);
        `;

        document.body.appendChild(notification);

        setTimeout(() => {
            document.body.removeChild(notification);
        }, 3000);
    }
}

/**
 * 로그인 필요 상태 표시
 */
function showLoginRequired() {
    const mainContent = document.getElementById('main-content');
    if (mainContent) {
        mainContent.innerHTML = `
            <div class="login-required">
                <div class="login-icon">🔒</div>
                <h2>로그인이 필요합니다</h2>
                <p>적립금을 확인하려면 로그인해주세요.</p>
                <a href="/login" class="btn btn-primary">로그인하기</a>
            </div>
        `;
    }
}

// ==================== 유틸리티 함수 ====================

/**
 * 적립금 계산기 표시
 */
function showPointCalculator() {
    const modal = document.getElementById('point-calculator-modal');
    if (modal) {
        modal.style.display = 'block';

        // 계산기 로직
        const calculateBtn = document.getElementById('calculate-btn');
        const amountInput = document.getElementById('amount-input');
        const resultElement = document.getElementById('calculation-result');

        if (calculateBtn && amountInput && resultElement) {
            calculateBtn.onclick = function() {
                const amount = parseInt(amountInput.value) || 0;
                const earnPoints = PointsAPI.calculateEarnPoints(amount);
                resultElement.textContent = `${PointsAPI.formatNumber(amount)}원 구매 시 ${PointsAPI.formatNumber(earnPoints)}원 적립`;
            };
        }
    }
}

/**
 * 모달 닫기
 */
function closeModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.style.display = 'none';
    }
}

/**
 * 적립률 정보 로드 및 표시
 */
async function loadEarnRateInfo() {
    try {
        const response = await PointsAPI.getEarnRate();

        if (response.success) {
            const earnRateElement = document.getElementById('earn-rate-info');
            if (earnRateElement) {
                earnRateElement.innerHTML = `
                    <div class="earn-rate-display">
                        <h4>현재 적립률</h4>
                        <div class="rate-value">${response.earnRatePercent}%</div>
                        <div class="rate-example">
                            ${response.example.description}
                        </div>
                    </div>
                `;
            }
        }
    } catch (error) {
        console.error('적립률 정보 로드 실패:', error);
    }
}

// ==================== 내보내기 ====================

// 전역 함수로 내보내기 (HTML에서 직접 호출 가능)
window.refreshPage = refreshPage;
window.filterHistory = filterHistory;
window.loadRecentActivity = loadRecentActivity;
window.showPointCalculator = showPointCalculator;
window.closeModal = closeModal;
window.loadPointsHistory = loadPointsHistory;