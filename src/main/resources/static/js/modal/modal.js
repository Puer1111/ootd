

    export const modal = {

        open(event) {
            const modalElement = document.getElementById('common-modal');
            const modalTitle = document.getElementById('modal-title');
            const saveBtn = document.getElementById('save-btn');
            const modalInput = document.getElementById('modal-input');

            const btnId = event.currentTarget.id;

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
        close(){
            const modalElement = document.getElementById('common-modal');
            modalElement.style.display = 'none';
        },
        init() {
            const saveBtn = document.getElementById('save-btn');
            const addBrandBtn = document.getElementById('add-brand-btn');
            const addCategoryBtn = document.getElementById('add-category-btn');
            const modalElement = document.getElementById('common-modal');

            if (addBrandBtn) {
                addBrandBtn.addEventListener('click', (event) => this.open(event));
            }

            if (addCategoryBtn) {
                addCategoryBtn.addEventListener('click', (event) => this.open(event));
            }

            // x 버튼 닫기 이벤트 등록
            const closeBtn = document.querySelector('.close');
            closeBtn.addEventListener('click', this.close);

            // 취소 버튼 클릭 시 닫기
            const cancelBtn = document.getElementById('cancel-btn');
            if (cancelBtn) {
                cancelBtn.addEventListener('click', this.close);
            }

            // 모달 외부 클릭 시 닫기 (선택적)
            window.addEventListener('click', (event) => {
                if (event.target === modalElement) {
                    this.close();
                }
            });
            saveBtn.addEventListener('click',() =>this.save());
        },
        save(){
            const modalInput = document.getElementById('modal-input');
            const saveBtn = document.getElementById('save-btn');

            const inputValue = modalInput.value.trim();
            const type = saveBtn.dataset.type;

            if(inputValue === ' '){
                alert(`${type === 'brand' ? '브랜드' : '카테고리'}명을 입력해주세요.`);
                return;
            }
            // 타입에 따라 적절한 API 호출
            if (type === 'brand') {
                api.brand.register(inputValue)
            } else if (type === 'category') {
                api.category.register(inputValue)
            }
        }
    }
