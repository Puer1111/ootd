export const utils = {
    // 프리뷰 기능 초기화
    init(fileInputId = 'fileInput', previewAreaId = 'preview-area') {
        this.fileInput = document.getElementById(fileInputId);
        this.previewArea = document.getElementById(previewAreaId);

        if (!this.fileInput || !this.previewArea) {
            console.error('파일 입력 또는 프리뷰 영역을 찾을 수 없습니다.');
            return;
        }

        this.fileInput.addEventListener('change', (event) => {
            this.showPreviews(event.target.files);
            const files = Array.from(event.target.files);

            if (files.length > 0) {
                this.previewArea.classList.add('has-images'); // 이미지가 있으면 표시
            } else {
                this.previewArea.classList.remove('has-images'); // 없으면 숨김
            }
        });
    },

    // 여러 파일의 프리뷰 표시
    showPreviews(files) {
        // 기존 프리뷰 모두 제거
        this.clearPreviews();

        // 각 파일에 대해 프리뷰 생성
        Array.from(files).forEach((file, index) => {
            if (file.type.startsWith('image/')) {
                this.createPreview(file, index);
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

            // 삭제 버튼 이벤트 추가
            const removeBtn = div.querySelector('.remove-btn');
            removeBtn.addEventListener('click', () => this.removePreview(index));

            this.previewArea.appendChild(div);
        };

        reader.readAsDataURL(file);
    },

    // 특정 프리뷰 제거
    removePreview(index) {
        const previewItem = this.previewArea.querySelector(`[data-index="${index}"]`);

        if (previewItem) {
            previewItem.remove();
        }

        // 파일 입력에서도 해당 파일 제거
        this.updateFileInput(index);
    },

    // 파일 입력 업데이트
    updateFileInput(removeIndex) {
        const dt = new DataTransfer();
        const files = Array.from(this.fileInput.files);

        files.forEach((file, i) => {
            if (i !== removeIndex) {
                dt.items.add(file);
            }
        });

        this.fileInput.files = dt.files;

        // 남은 프리뷰들의 인덱스 재정렬
        this.reindexPreviews();
    },

    // 프리뷰 인덱스 재정렬
    reindexPreviews() {
        const previews = this.previewArea.querySelectorAll('.preview-item');
        previews.forEach((preview, i) => {
            preview.setAttribute('data-index', i);
        });
    },

    // 모든 프리뷰 제거
    clearPreviews() {
        this.previewArea.innerHTML = '';
    },

    // 현재 선택된 파일들 가져오기
    getFiles() {
        return Array.from(this.fileInput.files);
    }
}