export const size = {
    // 사용 가능한 사이즈 목록
    availableSizes: {
        top: [
            { id: 1, name: 'XS' },
            { id: 2, name: 'S' },
            { id: 3, name: 'M' },
            { id: 4, name: 'L' },
            { id: 5, name: 'XL' },
            { id: 6, name: 'XXL' }
        ],
        shoes: [
            { id: 7, name: '220' },
            { id: 8, name: '225' },
            { id: 9, name: '230' },
            { id: 10, name: '235' },
            { id: 11, name: '240' },
            { id: 12, name: '245' },
            { id: 13, name: '250' },
            { id: 14, name: '255' },
            { id: 15, name: '260' },
            { id: 16, name: '265' },
            { id: 17, name: '270' },
            { id: 18, name: '275' },
            { id: 19, name: '280' }
        ],
        pants: [
            { id: 20, name: '26' },
            { id: 21, name: '27' },
            { id: 22, name: '28' },
            { id: 23, name: '29' },
            { id: 24, name: '30' },
            { id: 25, name: '31' },
            { id: 26, name: '32' },
            { id: 27, name: '33' },
            { id: 28, name: '34' },
            { id: 29, name: '36' },
            { id: 30, name: '38' }
        ]
    },

    // 내부 카운터
    _sizeItemCount: 0,

    // 설정 옵션
    config: {
        containerSelector: '#sizesContainer',
        minSizeItems: 1
    },

    // 초기화 함수
    init(containerSelector = '#sizesContainer') {
        this.config.containerSelector = containerSelector;
        this._sizeItemCount = 0;

        // 첫 번째 사이즈 아이템 추가
        this.addSizeItem();

        // 이벤트 위임으로 동적 이벤트 처리
        this._setupEventDelegation();
    },

    // 사이즈 아이템 추가
    addSizeItem() {
        const container = document.querySelector(this.config.containerSelector);
        if (!container) {
            console.error('Size container not found:', this.config.containerSelector);
            return null;
        }

        const itemId = ++this._sizeItemCount;
        const sizeItem = document.createElement('div');
        sizeItem.className = 'size-item';
        sizeItem.id = `sizeItem${itemId}`;
        sizeItem.dataset.itemId = itemId;

        sizeItem.innerHTML = this._generateSizeItemHTML(itemId);
        container.appendChild(sizeItem);

        if (window.colorManager) {
            window.colorManager.lookupColors(itemId);
        }

        return itemId;
    },
    bindAddSizeButton() {
        const addSizeBtn = this.getAddSizeButton();
        if (addSizeBtn) {
            // bind를 사용하여 this 컨텍스트 유지
            addSizeBtn.addEventListener('click', this.handleAddSizeClick.bind(this));
        }
    },

    // 사이즈 추가 버튼 요소 가져오기
    getAddSizeButton() {
        return document.querySelector('.btn-add');
    },

    // 사이즈 추가 클릭 핸들러
    handleAddSizeClick() {
        // 함수가 존재하는지 확인 후 실행
        if (typeof this.addSizeItem === 'function') {
            this.addSizeItem();
        } else {
            console.warn('addSizeItem 함수가 아직 로드되지 않았습니다.');
        }
    },

    // 사이즈 아이템 삭제
    removeSizeItem(itemId) {
        const item = document.getElementById(`sizeItem${itemId}`);
        if (!item) {
            console.warn('Size item not found:', itemId);
            return false;
        }

        // 최소 개수 체크
        const remainingItems = document.querySelectorAll('.size-item').length;
        if (remainingItems <= this.config.minSizeItems) {
            alert(`최소 ${this.config.minSizeItems}개의 사이즈는 등록해야 합니다.`);
            return false;
        }

        item.remove();
        return true;
    },

    // 모든 사이즈 데이터 수집
    collectSizeData() {
        const sizeItems = document.querySelectorAll('.size-item');
        const sizesData = [];

        sizeItems.forEach(item => {
            const itemId = item.dataset.itemId;
            const sizeId = item.querySelector(`select[name="size"]`)?.value;
            const price = item.querySelector(`input[name="product-price"]`)?.value;
            const stock = item.querySelector(`input[name="stock${itemId}"]`)?.value;
            const status = item.querySelector(`select[name="status${itemId}"]`)?.value;

            if (sizeId && price && stock) {
                sizesData.push({
                    size_id: parseInt(sizeId),
                    price: parseFloat(price),
                    stock_quantity: parseInt(stock),
                    status: status || 'available'
                });
            }
        });

        return sizesData;
    },

    // 유효성 검증
    validateSizes() {
        const sizesData = this.collectSizeData();
        const errors = [];

        // 최소 개수 체크
        if (sizesData.length < this.config.minSizeItems) {
            errors.push(`최소 ${this.config.minSizeItems}개의 사이즈를 등록해주세요.`);
        }

        // 중복 사이즈 체크
        const sizeIds = sizesData.map(item => item.size_id);
        const duplicates = sizeIds.filter((item, index) => sizeIds.indexOf(item) !== index);
        if (duplicates.length > 0) {
            errors.push('중복된 사이즈가 있습니다.');
        }

        // 필수 필드 체크
        sizesData.forEach((item, index) => {
            if (!item.size_id) {
                errors.push(`${index + 1}번째 사이즈를 선택해주세요.`);
            }
            if (!item.price || item.price <= 0) {
                errors.push(`${index + 1}번째 가격을 입력해주세요.`);
            }
            if (item.stock_quantity === undefined || item.stock_quantity < 0) {
                errors.push(`${index + 1}번째 재고를 입력해주세요.`);
            }
        });

        return {
            isValid: errors.length === 0,
            errors: errors,
            data: sizesData
        };
    },

    // 중복 사이즈 체크 및 UI 업데이트
    checkDuplicateSizes() {
        const sizeSelects = document.querySelectorAll('select[name^="sizeId"]');
        const selectedSizes = [];
        let hasDuplicate = false;

        sizeSelects.forEach(select => {
            if (select.value) {
                if (selectedSizes.includes(select.value)) {
                    hasDuplicate = true;
                    select.style.borderColor = '#dc3545';
                } else {
                    selectedSizes.push(select.value);
                    select.style.borderColor = '#e1e5e9';
                }
            }
        });

        return !hasDuplicate;
    },

    // // 사이즈 데이터 로드 (편집 모드)
    // loadSizeData(sizesData) {
    //     // 기존 사이즈 아이템들 제거
    //     const container = document.querySelector(this.config.containerSelector);
    //     if (!container) return;
    //
    //     container.innerHTML = '';
    //     this._sizeItemCount = 0;
    //
    //     // 데이터가 없으면 빈 아이템 하나 추가
    //     if (!sizesData || sizesData.length === 0) {
    //         this.addSizeItem();
    //         return;
    //     }
    //
    //     // 데이터 기반으로 사이즈 아이템들 생성
    //     sizesData.forEach(sizeData => {
    //         const itemId = this.addSizeItem();
    //
    //         // 값 설정
    //         setTimeout(() => {
    //             const sizeSelect = document.querySelector(`select[name="size"]`);
    //             const priceInput = document.querySelector(`input[name="product-price"]`);
    //             const stockInput = document.querySelector(`input[name="stock${itemId}"]`);
    //             const statusSelect = document.querySelector(`select[name="status${itemId}"]`);
    //
    //             if (sizeSelect) sizeSelect.value = sizeData.size_id;
    //             if (priceInput) priceInput.value = sizeData.price;
    //             if (stockInput) stockInput.value = sizeData.stock_quantity;
    //             if (statusSelect) statusSelect.value = sizeData.status || 'available';
    //         }, 0);
    //     });
    // },

    // 이벤트 위임 설정 (동적 요소 처리)
    _setupEventDelegation() {
        const container = document.querySelector(this.config.containerSelector);
        if (!container) return;

        // 사이즈 선택 변경 시 중복 체크
        container.addEventListener('change', (e) => {
            if (e.target.name && e.target.name.startsWith('sizeId')) {
                this.checkDuplicateSizes();
            }
        });

        // 삭제 버튼 클릭 시
        container.addEventListener('click', (e) => {
            if (e.target.classList.contains('size-remove-btn') ||
                e.target.closest('.size-remove-btn')) {
                const button = e.target.closest('[onclick]') || e.target;
                const onclickAttr = button.getAttribute('onclick');
                if (onclickAttr) {
                    const itemId = onclickAttr.match(/removeSizeItem\((\d+)\)/)?.[1];
                    if (itemId) {
                        this.removeSizeItem(itemId);
                    }
                }
            }
        });
    },

    // HTML 생성 함수 (SKU 제거, 4개 컬럼 정렬)
        _generateSizeItemHTML(itemId) {
            // const sizeOptions = this.availableSizes
            //     .map(size => `<option value="${size.id}">${size.name}</option>`)
            //     .join('');
            const category = 'top'; // 또는 다른 로직으로 결정
            const sizeOptions = this.availableSizes[category]
                .map(size => `<option value="${size.id}">${size.name}</option>`)
                .join('');
            return `
        <div>
            <select name="size" required>
                <option value="">선택</option>
                ${sizeOptions}
            </select>
        </div>
        <div>
            <input type="number" name="price" id="product-price" placeholder="가격을 입력해주세요" min="0" required>
        </div>
        <div>
            <select name="colorsNo" id="product-color">
                <option value="ChoiceColor">-- 색깔 선택 --</option>
            </select>
        </div>
        <div>
            <input type="number" name="stock${itemId}" placeholder="10" min="0" required>
        </div>
        <div>
            <select name="status${itemId}">
                <option value="available">판매중</option>
                <option value="unavailable">품절</option>
                <option value="discontinued">단종</option>
            </select>
        </div>
        <div>
            <button type="button" class="size-remove-btn" 
                    onclick="size.removeSizeItem(${itemId})" title="삭제">
                🗑️
            </button>
        </div>
    `;

    },

    // 리셋
    reset() {
        const container = document.querySelector(this.config.containerSelector);
        if (container) {
            container.innerHTML = '';
            this._sizeItemCount = 0;
            this.addSizeItem();
        }
    },

    // // 유틸리티: 사이즈 이름으로 ID 찾기
    // getSizeIdByName(sizeName) {
    //     const size = this.availableSizes.find(s => s.name === sizeName);
    //     return size ? size.id : null;
    // },
    //
    // // 유틸리티: 사이즈 ID로 이름 찾기
    // getSizeNameById(sizeId) {
    //     const size = this.availableSizes.find(s => s.id == sizeId);
    //     return size ? size.name : null;
    // },
    //
    // // 설정 변경
    // setConfig(newConfig) {
    //     this.config = { ...this.config, ...newConfig };
    // }
};