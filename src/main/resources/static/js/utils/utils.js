export const utils = {
    selectedFiles: [],
    fileInput: null,
    previewArea: null,
    placeholderCreated: false, // placeholder 생성 여부 추적

    // 프리뷰 기능 초기화
    init(fileInputId = 'fileInput', previewAreaId = 'preview-area') {
        this.fileInput = document.getElementById(fileInputId);
        this.previewArea = document.getElementById(previewAreaId);
        this.selectedFiles = []; // 초기화 시 배열 초기화
        this.placeholderCreated = false; // 초기화

        if (!this.fileInput || !this.previewArea) {
            console.error('파일 입력 또는 프리뷰 영역을 찾을 수 없습니다.');
            return;
        }

        // placeholder HTML 추가 (한 번만)
        this.createPlaceholder();

        // handleFileSelect 함수를 이벤트 리스너로 등록
        this.fileInput.addEventListener('change', (e) => this.handleFileSelect(e));

        // 드래그 앤 드롭 이벤트 추가
        this.setupDragAndDrop();

        // 폼 제출 이벤트 리스너 추가
        const form = document.getElementById('product-form');
        if (form) {
            form.addEventListener('submit', (e) => {
                e.preventDefault();
                this.submitForm();
            });
        }

        console.log('이미지 업로드 초기화 완료');
    },

    // placeholder 생성 (한 번만 실행되도록 수정)
    createPlaceholder() {
        // 이미 생성되었다면 실행하지 않음
        if (this.placeholderCreated || this.previewArea.querySelector('.upload-placeholder')) {
            return;
        }

        // 기존 내용 완전히 제거
        this.previewArea.innerHTML = '';

        // placeholder HTML 생성
        const placeholderDiv = document.createElement('div');
        placeholderDiv.className = 'upload-placeholder';
        placeholderDiv.id = 'uploadPlaceholder';

        placeholderDiv.innerHTML = `
            <svg class="upload-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                      d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12"/>
            </svg>
            <div class="upload-text">이미지를 선택하세요</div>
        `;

        this.previewArea.appendChild(placeholderDiv);
        this.placeholderCreated = true; // 생성 완료 표시

        console.log('Placeholder 생성 완료');
    },

    // 드래그 앤 드롭 설정
    setupDragAndDrop() {
        const boundary = document.querySelector('.product-img-boundary');
        if (!boundary) return;

        boundary.addEventListener('dragover', (e) => {
            e.preventDefault();
            boundary.style.borderColor = '#007bff';
            boundary.style.backgroundColor = '#f0f8ff';
        });

        boundary.addEventListener('dragleave', (e) => {
            e.preventDefault();
            if (this.selectedFiles.length > 0) {
                boundary.style.borderColor = '#28a745';
                boundary.style.backgroundColor = '#f8fff8';
            } else {
                boundary.style.borderColor = '#ddd';
                boundary.style.backgroundColor = '#fafafa';
            }
        });

        boundary.addEventListener('drop', (e) => {
            e.preventDefault();

            const files = Array.from(e.dataTransfer.files).filter(file =>
                file.type.startsWith('image/')
            );

            if (files.length > 0) {
                // 기존 파일들에 새 파일들 추가
                files.forEach(file => {
                    this.selectedFiles.push(file);
                });

                this.showPreviews(files);
                this.updateUIState();
            }

            // 스타일 복원
            if (this.selectedFiles.length > 0) {
                boundary.style.borderColor = '#28a745';
                boundary.style.backgroundColor = '#f8fff8';
            } else {
                boundary.style.borderColor = '#ddd';
                boundary.style.backgroundColor = '#fafafa';
            }
        });
    },

    handleFileSelect(e) {
        const files = Array.from(e.target.files);

        // 이미지 파일만 필터링
        const imageFiles = files.filter(file => file.type.startsWith('image/'));

        // selectedFiles 배열에 파일 추가
        imageFiles.forEach(file => {
            this.selectedFiles.push(file);
        });

        // 프리뷰 표시
        this.showPreviews(imageFiles);

        // UI 상태 업데이트
        this.updateUIState();

        console.log('선택된 파일들:', this.selectedFiles);
    },

    // UI 상태 업데이트 (placeholder 안전하게 제어)
    updateUIState() {
        const placeholder = this.previewArea.querySelector('#uploadPlaceholder');
        const boundary = document.querySelector('.product-img-boundary');

        // placeholder가 없으면 다시 생성
        if (!placeholder) {
            this.placeholderCreated = false;
            this.createPlaceholder();
            return this.updateUIState(); // 재귀 호출로 다시 실행
        }

        if (this.selectedFiles.length > 0) {
            // 파일이 있을 때: placeholder 숨기기
            placeholder.style.display = 'none';
            placeholder.classList.add('hidden');
            if (boundary) boundary.classList.add('has-files');
        } else {
            // 파일이 없을 때: placeholder 보이기
            placeholder.style.display = '';
            placeholder.classList.remove('hidden');
            if (boundary) boundary.classList.remove('has-files');
        }

        console.log('UI 상태 업데이트:', this.selectedFiles.length > 0 ? '파일 있음' : '파일 없음');
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
                <button class="remove-btn" type="button" data-file-index="${index}">×</button>
            `;

            const removeBtn = div.querySelector('.remove-btn');

            // 삭제 버튼 이벤트
            removeBtn.addEventListener('click', (e) => {
                e.preventDefault();
                e.stopPropagation();
                e.stopImmediatePropagation();
                this.removePreview(index);
                return false;
            });

            removeBtn.addEventListener('mousedown', (e) => {
                e.preventDefault();
                e.stopPropagation();
                e.stopImmediatePropagation();
            });

            this.previewArea.appendChild(div);
        };

        reader.readAsDataURL(file);
    },

    // 특정 프리뷰 제거
    removePreview(index) {
        console.log('삭제 버튼 클릭됨, index:', index);

        // selectedFiles에서 제거
        this.selectedFiles.splice(index, 1);

        // DOM에서 해당 미리보기 제거
        const previewItem = this.previewArea.querySelector(`[data-index="${index}"]`);
        if (previewItem) {
            previewItem.remove();
        }

        // 인덱스 재정렬
        this.reindexPreviews();

        // UI 상태 업데이트 (placeholder 복원 포함)
        this.updateUIState();

        // input 초기화 (필요시)
        if (this.selectedFiles.length === 0) {
            this.fileInput.value = '';
        }

        console.log('파일 삭제 후 남은 파일들:', this.selectedFiles);
    },

    // 프리뷰 인덱스 재정렬
    reindexPreviews() {
        const previews = this.previewArea.querySelectorAll('.preview-item');
        previews.forEach((preview, i) => {
            preview.setAttribute('data-index', i);

            const removeBtn = preview.querySelector('.remove-btn');
            if (removeBtn) {
                removeBtn.setAttribute('data-file-index', i);

                // 기존 이벤트 리스너 제거 후 재등록
                removeBtn.replaceWith(removeBtn.cloneNode(true));
                const newRemoveBtn = preview.querySelector('.remove-btn');

                newRemoveBtn.addEventListener('click', (e) => {
                    e.preventDefault();
                    e.stopPropagation();
                    e.stopImmediatePropagation();
                    this.removePreview(i);
                    return false;
                });

                newRemoveBtn.addEventListener('mousedown', (e) => {
                    e.preventDefault();
                    e.stopPropagation();
                    e.stopImmediatePropagation();
                });
            }
        });
    },

    // 선택된 파일들 가져오기
    getSelectedFiles() {
        return this.selectedFiles;
    },

    // 모든 파일 초기화 (placeholder 안전하게 복원)
    clearAllFiles() {
        console.log('모든 파일 초기화 시작');

        // 선택된 파일들 초기화
        this.selectedFiles = [];

        // 모든 미리보기 아이템만 제거 (placeholder는 유지)
        const previews = this.previewArea.querySelectorAll('.preview-item');
        previews.forEach(preview => preview.remove());

        // placeholder가 없으면 다시 생성
        if (!this.previewArea.querySelector('#uploadPlaceholder')) {
            this.placeholderCreated = false;
            this.createPlaceholder();
        }

        // UI 상태 업데이트
        this.updateUIState();

        // input 초기화
        if (this.fileInput) {
            this.fileInput.value = '';
        }

        console.log('모든 파일이 초기화되었습니다.');
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

        // 유효성 검증
        if (this.selectedFiles.length === 0) {
            alert('이미지를 선택해주세요.');
            return;
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
                } else {
                    alert('상품이 성공적으로 등록되었습니다!');
                    this.clearAllFiles(); // 성공 후 파일 초기화
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert('등록 중 오류가 발생했습니다: ' + error.message);
            });
    }
};