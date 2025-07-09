/**
 * 적립금 관련 API 호출 함수들
 * 경로: src/main/resources/static/js/point/pointsAPI.js
 */

// ==================== 토큰 관리 ====================
const PointsAPI = {

    // JWT 토큰 가져오기
    getToken: function() {
        return localStorage.getItem('token') || sessionStorage.getItem('token');
    },

    // 인증 헤더 생성
    getAuthHeaders: function() {
        const token = this.getToken();
        return {
            'Content-Type': 'application/json',
            'Authorization': token ? 'Bearer ' + token : ''
        };
    },

    // ==================== 적립금 조회 API ====================

    /**
     * 내 적립금 정보 조회
     * @returns {Promise<Object>} 적립금 정보
     */
    getMyPoints: async function() {
        try {
            const response = await fetch('/api/points/my-points', {
                method: 'GET',
                headers: this.getAuthHeaders()
            });

            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.message || '적립금 조회에 실패했습니다');
            }

            return data;
        } catch (error) {
            console.error('적립금 조회 실패:', error);
            throw error;
        }
    },

    /**
     * 적립금 내역 조회 (페이지네이션)
     * @param {number} page - 페이지 번호
     * @param {number} size - 페이지 크기
     * @returns {Promise<Object>} 적립금 내역
     */
    getPointHistory: async function(page = 0, size = 20) {
        try {
            const response = await fetch(`/api/points/history?page=${page}&size=${size}`, {
                method: 'GET',
                headers: this.getAuthHeaders()
            });

            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.message || '적립금 내역 조회에 실패했습니다');
            }

            return data;
        } catch (error) {
            console.error('적립금 내역 조회 실패:', error);
            throw error;
        }
    },

    /**
     * 전체 적립금 내역 조회
     * @returns {Promise<Object>} 전체 적립금 내역
     */
    getAllPointHistory: async function() {
        try {
            const response = await fetch('/api/points/history/all', {
                method: 'GET',
                headers: this.getAuthHeaders()
            });

            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.message || '적립금 내역 조회에 실패했습니다');
            }

            return data;
        } catch (error) {
            console.error('전체 적립금 내역 조회 실패:', error);
            throw error;
        }
    },

    // ==================== 적립금 사용 API ====================

    /**
     * 적립금 사용
     * @param {number} points - 사용할 적립금
     * @param {string} description - 사용 사유
     * @param {number} orderId - 주문 ID (선택사항)
     * @returns {Promise<Object>} 사용 결과
     */
    usePoints: async function(points, description = '상품 구매', orderId = null) {
        try {
            const requestData = {
                points: points,
                description: description
            };

            if (orderId) {
                requestData.orderId = orderId;
            }

            const response = await fetch('/api/points/use', {
                method: 'POST',
                headers: this.getAuthHeaders(),
                body: JSON.stringify(requestData)
            });

            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.message || '적립금 사용에 실패했습니다');
            }

            return data;
        } catch (error) {
            console.error('적립금 사용 실패:', error);
            throw error;
        }
    },

    /**
     * 적립금 사용 가능 여부 확인
     * @param {number} points - 확인할 적립금
     * @returns {Promise<Object>} 사용 가능 여부
     */
    canUsePoints: async function(points) {
        try {
            const response = await fetch(`/api/points/can-use?points=${points}`, {
                method: 'GET',
                headers: this.getAuthHeaders()
            });

            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.message || '적립금 확인에 실패했습니다');
            }

            return data;
        } catch (error) {
            console.error('적립금 사용 가능 여부 확인 실패:', error);
            throw error;
        }
    },

    /**
     * 적립금 환원 (결제 실패 시)
     * @param {number} points - 환원할 적립금
     * @param {string} description - 환원 사유
     * @param {number} orderId - 주문 ID (선택사항)
     * @returns {Promise<Object>} 환원 결과
     */
    refundPoints: async function(points, description = '결제 실패로 인한 적립금 환원', orderId = null) {
        try {
            const requestData = {
                points: points,
                description: description
            };

            if (orderId) {
                requestData.orderId = orderId;
            }

            const response = await fetch('/api/points/refund', {
                method: 'POST',
                headers: this.getAuthHeaders(),
                body: JSON.stringify(requestData)
            });

            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.message || '적립금 환원에 실패했습니다');
            }

            return data;
        } catch (error) {
            console.error('적립금 환원 실패:', error);
            throw error;
        }
    },

    // ==================== 적립금 정보 API ====================

    /**
     * 적립금 통계 조회
     * @returns {Promise<Object>} 적립금 통계
     */
    getPointsStatistics: async function() {
        try {
            const response = await fetch('/api/points/statistics', {
                method: 'GET',
                headers: this.getAuthHeaders()
            });

            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.message || '적립금 통계 조회에 실패했습니다');
            }

            return data;
        } catch (error) {
            console.error('적립금 통계 조회 실패:', error);
            throw error;
        }
    },

    /**
     * 최근 적립금 활동 조회
     * @param {number} days - 조회할 일수
     * @returns {Promise<Object>} 최근 활동 내역
     */
    getRecentActivity: async function(days = 7) {
        try {
            const response = await fetch(`/api/points/recent?days=${days}`, {
                method: 'GET',
                headers: this.getAuthHeaders()
            });

            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.message || '최근 활동 조회에 실패했습니다');
            }

            return data;
        } catch (error) {
            console.error('최근 활동 조회 실패:', error);
            throw error;
        }
    },

    /**
     * 적립률 정보 조회
     * @returns {Promise<Object>} 적립률 정보
     */
    getEarnRate: async function() {
        try {
            const response = await fetch('/api/points/earn-rate', {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.message || '적립률 정보 조회에 실패했습니다');
            }

            return data;
        } catch (error) {
            console.error('적립률 정보 조회 실패:', error);
            throw error;
        }
    },

    // ==================== 유틸리티 함수 ====================

    /**
     * 적립금 계산 (클라이언트 사이드)
     * @param {number} purchaseAmount - 구매 금액
     * @param {number} earnRate - 적립률 (기본값: 0.01)
     * @returns {number} 적립될 금액
     */
    calculateEarnPoints: function(purchaseAmount, earnRate = 0.01) {
        if (!purchaseAmount || purchaseAmount <= 0) {
            return 0;
        }
        return Math.round(purchaseAmount * earnRate);
    },

    /**
     * 숫자를 천 단위 구분자로 포맷팅
     * @param {number} number - 포맷팅할 숫자
     * @returns {string} 포맷팅된 문자열
     */
    formatNumber: function(number) {
        if (number === null || number === undefined) {
            return '0';
        }
        return number.toLocaleString();
    },

    /**
     * 날짜를 사용자 친화적 형식으로 포맷팅
     * @param {string} dateString - 날짜 문자열
     * @returns {string} 포맷팅된 날짜
     */
    formatDate: function(dateString) {
        const date = new Date(dateString);
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        const hours = String(date.getHours()).padStart(2, '0');
        const minutes = String(date.getMinutes()).padStart(2, '0');

        return `${year}.${month}.${day} ${hours}:${minutes}`;
    }
};

// 전역에서 사용할 수 있도록 window 객체에 할당
window.PointsAPI = PointsAPI;