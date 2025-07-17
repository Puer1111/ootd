export const size = {
    // 사용 가능한 사이즈 목록
    availableSizes: {
        top: [
            {id: 'XS', name: 'XS'},
            {id: 'S', name: 'S'},
            {id: 'M', name: 'M'},
            {id: 'L', name: 'L'},
            {id: 'XL', name: 'XL'},
            {id: 'XXL', name: 'XXL'}
        ],
        shoes: [
            {id: 220, name: '220'},
            {id: 225, name: '225'},
            {id: 230, name: '230'},
            {id: 235, name: '235'},
            {id: 240, name: '240'},
            {id: 245, name: '245'},
            {id: 250, name: '250'},
            {id: 255, name: '255'},
            {id: 260, name: '260'},
            {id: 265, name: '265'},
            {id: 270, name: '270'},
            {id: 275, name: '275'},
            {id: 280, name: '280'}
        ],
        bottom: [
            {id: 26, name: '26'},
            {id: 27, name: '27'},
            {id: 28, name: '28'},
            {id: 29, name: '29'},
            {id: 30, name: '30'},
            {id: 31, name: '31'},
            {id: 32, name: '32'},
            {id: 33, name: '33'},
            {id: 34, name: '34'},
            {id: 36, name: '36'},
            {id: 38, name: '38'}
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
        const mainCategory = document.getElementById('categoryChoiceFirst');

        mainCategory.addEventListener('change', (e) => {
            this.updateAllSizeSelects();
        });

        // 첫 번째 사이즈 아이템 추가
        this.addSizeItem();

        // html 생성.
        this._generateSizeItemHTML();

        // 이벤트 위임으로 동적 이벤트 처리
        this._setupEventDelegation();

        this.updateAllSizeSelects();

    },

    // 사이즈 아이템 추가
    addSizeItem() {
        const container = document.querySelector(this.config.containerSelector);
        if (!container) {
            console.error('Size container not found:', this.config.containerSelector);
            return null;
        }
        // size div 를 생성함.
        const itemId = ++this._sizeItemCount;
        const sizeItem = document.createElement('div');
        sizeItem.className = 'size-item';
        sizeItem.id = `sizeItem${itemId}`;
        sizeItem.dataset.itemId = itemId.toString();

        sizeItem.innerHTML = this._generateSizeItemHTML(itemId);
        container.appendChild(sizeItem);

        this.updateAllSizeSelects();
        this.updateAllColorSelects();

        if (window.api.colors) {
            window.api.colors.lookupColors();
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

    updateSizeOptions() {
        const categorySelect = document.getElementById('categoryChoiceFirst');
        const mainCategory = categorySelect.value;
        let transMain;
        if (mainCategory === "상의") {
            transMain = "top";
        } else if (mainCategory === "하의") {
            transMain = "bottom";
        } else {
            transMain = "shoes";
        }

        if (!transMain || !this.availableSizes[transMain]) {
            return '';
        }

        return this.availableSizes[transMain]
            .map(size => `<option value="${size.id}">${size.name}</option>`)
            .join('');
    },

    updateAllSizeSelects() {
        const sizeOptions = this.updateSizeOptions(); // HTML 문자열 받기
        const sizeSelects = document.querySelectorAll('select[name="size"]');
        sizeSelects.forEach(select => {
            select.innerHTML = `<option value="">선택</option>${sizeOptions}`;
        });
    },

    updateAllColorSelects(){
        window.api.colors.lookupColors();
    },

    // HTML 생성 함수 (SKU 제거, 4개 컬럼 정렬)
    _generateSizeItemHTML(itemId) {
        const sizeOptions = this.updateSizeOptions();
        return `
        <div>
            <select name="size" required></select>
        </div>
        <div>
            <input type="number" name="price" id="product-price" placeholder="가격을 입력해주세요" min="0" required>
        </div>
        <div>
            <select name="colorsNo" id="product-color">
                <option value="ChoiceColor"> 색깔 선택 </option>
            </select>
            <span style="margin-left:5px; flex:2;"><button type="button" id="add-color-btn" style="width:30px; height:30px;">+</button></span>
        </div>
        <div>
            <input type="number" name="inventory" placeholder="10" min="0" required id="inventory">
        </div>
        <div>
            <select name="status">
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

};