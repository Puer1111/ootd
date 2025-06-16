export const modal = {
    // 공통으로 사용할 DOM 요소들을 저장할 객체
    elements: {},

    // 요소 초기화 함수
    initElements() {
        this.elements = {
            modalElement: document.getElementById('common-modal'),
            modalTitle: document.getElementById('modal-title'),
            typeInputTitle: document.getElementById('modal-input-title'), // 모달창 제목에 대한 내용을 입력.
            typeInputContent1: document.getElementById('modal-input-content-1'), // 모달창 내용입력
            typeInputContent2: document.getElementById('modal-input-content-2'), // 모달창 내용입력
            typeContentDiv: document.getElementById('type-content-div'),
            saveBtn: document.getElementById('save-btn'),
            closeBtn: document.querySelector('.close'),
            cancelBtn: document.getElementById('cancel-btn'),
            addBrandBtn: document.getElementById('add-brand-btn'),
            addCategoryBtn: document.getElementById('add-category-btn'),
            addColorBtn: document.getElementById('add-color-btn'),
            addSizeBtn:document.getElementById('add-size-btn'),
            addSizeOption: document.getElementById('product-size'),
            categoryFirst: document.getElementById('categoryChoiceFirst')
        };
    },

    open(event) {
        if (!this.elements.modalElement) {
            this.initElements();
        }

        const btnId = event.currentTarget.id;
        const {categoryFirst, modalElement, modalTitle, typeInputTitle, saveBtn,addSizeOption,
            typeInputContent1,typeInputContent2,typeContentDiv
        } = this.elements;

        modalElement.style.display = 'block';
        typeContentDiv.style.display='none';
        addSizeOption.style.display='none';
        typeInputTitle.value = '';
        typeInputTitle.focus();

        if (btnId === 'add-brand-btn') {
            modalTitle.textContent = '새 브랜드 추가';
            typeInputTitle.placeholder = '브랜드명 입력';
            saveBtn.dataset.type = 'brand'; // 제출 버튼에 타입 지정
            typeInputTitle.name = "brand";
            typeContentDiv.style.display='block';
            typeInputContent1.placeholder= '브랜드 로고 등록'
            typeInputContent1.dataset.type='brandLogoUrl';
            typeInputContent1.name='brandLogoUrl';
            typeInputContent2.placeholder='브랜드 사이트 등록';
            typeInputContent2.dataset.type='brandWebsite';
            typeInputContent2.name='brandWebsite';

        }
        else if (btnId === 'add-category-btn') {
            modalTitle.textContent = '새 카테고리 추가';
            typeInputTitle.placeholder = '카테고리명 입력';
            saveBtn.dataset.type = 'category'; // 제출 버튼에 타입 지정
            typeInputTitle.name = "category";
            // typeContentDiv.style.display='block';
            addSizeOption.innerHTML = '<option value="choiceSize">-- 카테고리 선택 --</option>';
            addSizeOption.style.display='block';
            Array.from(categoryFirst.options).forEach(option => {
                const newOption = new Option(option.text, option.value);
                addSizeOption.appendChild(newOption);
            });

        }
        else if (btnId === 'add-color-btn'){
            modalTitle.textContent = '새 색깔 추가';
            typeInputTitle.placeholder = ' 색깔명 입력';
            saveBtn.dataset.type = 'color';
            typeInputTitle.name = "color";
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
        const { addBrandBtn, addCategoryBtn, addColorBtn, closeBtn, cancelBtn, modalElement, saveBtn } = this.elements;

        // 브랜드/카테고리 추가 버튼
        if (addBrandBtn) {
            addBrandBtn.addEventListener('click', (event) => this.open(event));
        }

        if (addCategoryBtn) {
            addCategoryBtn.addEventListener('click', (event) => this.open(event));
        }

        if (addColorBtn){
            addColorBtn.addEventListener('click', (event) => this.open(event));
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

        const { typeInputTitle, saveBtn, typeInputContent1,typeInputContent2 } = this.elements;
        const inputTitleValue = typeInputTitle.value.trim();
        const inputContentValue1 = typeInputContent1.value.trim();
        const inputContentValue2 = typeInputContent2.value.trim();
        const type = saveBtn.dataset.type;

        if (inputTitleValue === '' ) {
            return;
        }

        // 타입에 따라 적절한 API 호출
        if (type === 'brand') {
            window.api.brand.register(inputTitleValue,inputContentValue1,inputContentValue2)
        } else if (type === 'category') {
            window.api.category.register(inputTitleValue)
        } else if( type === 'color'){
            window.api.colors.register(inputTitleValue)
        }
    }
};