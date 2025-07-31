const PointsAPI = {
    getToken: function() {
        return localStorage.getItem('token') || sessionStorage.getItem('token');
    },

    getAuthHeaders: function() {
        const token = this.getToken();
        return {
            'Content-Type': 'application/json',
            'Authorization': token ? 'Bearer ' + token : ''
        };
    },

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

    calculateEarnPoints: function(purchaseAmount, earnRate = 0.01) {
        if (!purchaseAmount || purchaseAmount <= 0) {
            return 0;
        }
        return Math.round(purchaseAmount * earnRate);
    },

    formatNumber: function(number) {
        if (number === null || number === undefined) {
            return '0';
        }
        return number.toLocaleString();
    },

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

window.PointsAPI = PointsAPI;
