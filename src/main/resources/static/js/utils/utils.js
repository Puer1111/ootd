export const utils = {
    constructor() {
        this.selectedFiles = [];
        this.previewArea = document.getElementById('preview-area');

        // 폼 제출 이벤트 리스너 추가
        const form = document.getElementById('product-form');
        form.addEventListener('submit', (e) => {
            e.preventDefault();
            this.submitForm();
        });
    },

    handleFileSelect(e) {
        const files = Array.from(e.target.files);

        // selectedFiles 배열에 파일 추가
        files.forEach(file => {
            this.selectedFiles.push(file);
        });

        // 프리뷰 표시
        this.showPreviews(files);

        // 클래스 토글
        if (this.selectedFiles.length > 0) {
            this.previewArea.classList.add('has-images');
        } else {
            this.previewArea.classList.remove('has-images');
        }
    },

    // 프리뷰 기능 초기화
    init(fileInputId = 'fileInput', previewAreaId = 'preview-area') {
        this.fileInput = document.getElementById(fileInputId);
        this.previewArea = document.getElementById(previewAreaId);
        this.selectedFiles = []; // 초기화 시 배열 초기화

        if (!this.fileInput || !this.previewArea) {
            console.error('파일 입력 또는 프리뷰 영역을 찾을 수 없습니다.');
            return;
        }

        // handleFileSelect 함수를 이벤트 리스너로 등록
        this.fileInput.addEventListener('change', (e) => this.handleFileSelect(e));

        // 폼 제출 이벤트 리스너 추가
        const form = document.getElementById('product-form');
        if (form) {
            form.addEventListener('submit', (e) => {
                e.preventDefault();
                this.submitForm();
            });
        }
    },

    // 여러 파일의 프리뷰 표시
    showPreviews(files) {
        // 각 파일에 대해 프리뷰 생성
        Array.from(files).forEach((file, index) => {
            if (file.type.startsWith('image/')) {
                // selectedFiles의 현재 길이를 기준으로 인덱스 설정
                const actualIndex = this.selectedFiles.length - files.length + index;
                this.createPreview(file, actualIndex);
            }
        });
    },

    // 단일 프리뷰 생성
    createPreview(file, index) {
        const reader = new FileReader();

        reader.onload = (e) => {
            const div = document.createElement('div');
            div.className = 'preview-item';
            div.setAttribute('data-index', index);

            div.innerHTML = `
                <img src="${e.target.result}" alt="preview ${index}" class="preview-image">
                <button class="remove-btn" type="button">×</button>
            `;

            const removeBtn = div.querySelector('.remove-btn');
            removeBtn.addEventListener('click', () => this.removePreview(index));

            this.previewArea.appendChild(div);
        };

        reader.readAsDataURL(file);
    },

    // 특정 프리뷰 제거
    removePreview(index) {
        // selectedFiles에서 제거
        this.selectedFiles.splice(index, 1);

        // DOM에서 제거
        const previewItem = this.previewArea.querySelector(`[data-index="${index}"]`);
        if (previewItem) {
            previewItem.remove();
        }

        // 인덱스 재정렬
        this.reindexPreviews();

        // 클래스 업데이트
        if (this.selectedFiles.length === 0) {
            this.previewArea.classList.remove('has-images');
        }
    },

    // 프리뷰 인덱스 재정렬
    reindexPreviews() {
        const previews = this.previewArea.querySelectorAll('.preview-item');
        previews.forEach((preview, i) => {
            preview.setAttribute('data-index', i);
        });
    },

    // 이미지 form 제출 함수
    submitForm() {
        const formData = new FormData();
        const form = document.getElementById('product-form');

        // 다른 폼 필드들 추가
        const inputs = form.querySelectorAll('input[type="text"], input[type="number"], select, textarea');
        inputs.forEach(input => {
            if (input.name && input.value) {
                formData.append(input.name, input.value);
            }
        });

        // 이미지 파일들 추가
        console.log('Selected files count:', this.selectedFiles.length);
        this.selectedFiles.forEach((file, index) => {
            console.log(`Adding file ${index}:`, file.name);
            formData.append('images', file);
        });

        // FormData 내용 확인
        for (let [key, value] of formData.entries()) {
            console.log(key, value);
        }

        fetch('/enter/product', {
            method: 'POST',
            body: formData
        })
            .then(response => response.json())
            .then(data => {
                console.log('Success:', data);
                if (data.redirectUrl) {
                    window.location.href = data.redirectUrl;
                }
            })
            .catch(error => {
                console.error('Error:', error);
            });
    }
}