export const utils = {
    // 프리뷰 생성
    createPreviewFromURL(url, options = {}) {
        const preview = this.createPreviewElement(url, options);
        return preview;
    },

    // 파일을 GCS에 업로드 후 프리뷰 생성
    async createPreview(file, options = {}) {
        try {
            // 업로드 중 로딩 표시
            const loadingElement = this.createLoadingElement();
            const container = options.container || document.body;
            container.appendChild(loadingElement);

            // GCS에 파일 업로드
            const gcsUrl = await this.uploadToGCS(file);

            // 로딩 제거하고 프리뷰 생성
            loadingElement.remove();
            return this.createPreviewFromURL(gcsUrl, options);

        } catch (error) {
            throw error;
        }
    },
    // 업로드 받기
    async uploadToGCS(file) {
        const formData = new FormData();
        formData.append('file', file);

        try {
            const response = await fetch('/api/upload', {
                method: 'POST',
                body: formData
            });

            const data = await response.json();
            return data.publicUrl; // GCS 공개 URL
        } catch (error) {
            throw new Error('파일 업로드 실패: ' + error.message);
        }
    },

    createPreviewElement(src, { className = 'preview-item', size = 'medium' } = {}) {
        const div = document.createElement('div');
        div.className = className;
        div.innerHTML = `
            <img src="${src}" alt="preview" class="preview-image ${size}">
            <button class="remove-btn" type="button">×</button>
        `;
        return div;
    },

    createLoadingElement() {
        const div = document.createElement('div');
        div.className = 'loading-spinner';
        div.innerHTML = '업로드 중...';
        return div;
    },
    uploadFile(){
        document.getElementById('fileInput').addEventListener('change', async (event) => {
            const file = event.target.files[0];

            if (file) {
                try {
                    // createPreview가 시작점
                    const preview = await window.api.utils.createPreview(file, {
                        container: document.getElementById('preview-area'),
                        className: 'product-image-preview',
                        size: 'medium'
                    });

                    // 업로드 성공 후 처리
                    console.log('Preview element created:', preview);

                } catch (error) {
                    console.error('Upload failed:', error);
                    alert('파일 업로드에 실패했습니다.');
                }
            }
        });
    }
}