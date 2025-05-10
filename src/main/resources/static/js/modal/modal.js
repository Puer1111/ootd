export const modal = {
    // 공통으로 사용할 DOM 요소들을 저장할 객체
    elements: {},

    // 요소 초기화 함수
    initElements() {
        this.elements = {
            modalElement: document.getElementById('common-modal'),
            modalTitle: document.getElementById('modal-title'),
            modalInput: document.getElementById('modal-input'),
            saveBtn: document.getElementById('save-btn'),
            closeBtn: document.querySelector('.close'),
            cancelBtn: document.getElementById('cancel-btn'),
            addBrandBtn: document.getElementById('add-brand-btn'),
            addCategoryBtn: document.getElementById('add-category-btn')
        };
    },

    open(event) {
        if (!this.elements.modalElement) {
            this.initElements();
        }

        const btnId = event.currentTarget.id;
        const { modalElement, modalTitle, modalInput, saveBtn } = this.elements;

        modalElement.style.display = 'block';
        modalInput.value = '';
        modalInput.focus();

        if (btnId === 'add-brand-btn') {
            modalTitle.textContent = '새 브랜드 추가';
            modalInput.placeholder = '브랜드명 입력';
            saveBtn.dataset.type = 'brand'; // 제출 버튼에 타입 지정
            modalInput.name = "brand";
        } else if (btnId === 'add-category-btn') {
            modalTitle.textContent = '새 카테고리 추가';
            modalInput.placeholder = '카테고리명 입력';
            saveBtn.dataset.type = 'category'; // 제출 버튼에 타입 지정
            modalInput.name = "category";
        }
    },

    close() {
        // 아직 요소가 초기화되지 않았다면 초기화
        if (!this.elements.modalElement) {
            this.initElements();
        }

        this.elements.modalElement.style.display = 'none';
    },

    init() {
        // 요소 초기화
        this.initElements();
        const { addBrandBtn, addCategoryBtn, closeBtn, cancelBtn, modalElement, saveBtn } = this.elements;

        // 브랜드/카테고리 추가 버튼
        if (addBrandBtn) {
            addBrandBtn.addEventListener('click', (event) => this.open(event));
        }

        if (addCategoryBtn) {
            addCategoryBtn.addEventListener('click', (event) => this.open(event));
        }

        // 닫기 버튼 이벤트
        if (closeBtn) {
            closeBtn.addEventListener('click', () => this.close());
        }

        // 취소 버튼 이벤트
        if (cancelBtn) {
            cancelBtn.addEventListener('click', () => this.close());
        }

        // 모달 외부 클릭 이벤트
        window.addEventListener('click', (event) => {
            if (event.target === modalElement) {
                this.close();
            }
        });

        // 저장 버튼 이벤트
        if (saveBtn) {
            saveBtn.addEventListener('click', () => this.save());
        }
    },

    save() {
        // 아직 요소가 초기화되지 않았다면 초기화
        if (!this.elements.modalElement) {
            this.initElements();
        }

        const { modalInput, saveBtn } = this.elements;
        const inputValue = modalInput.value.trim();
        const type = saveBtn.dataset.type;

        if (inputValue === '' || inputValue === ' ') {
            alert(`${type === 'brand' ? '브랜드' : '카테고리'}명을 입력해주세요.`);
            return;
        }

        // 타입에 따라 적절한 API 호출
        if (type === 'brand') {
            window.api.brand.register(inputValue)

        } else if (type === 'category') {
            window.api.category.register(inputValue)
        }
    }
};