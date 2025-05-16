export const categoryAPI = {
    getCategorySelect() {
        return document.getElementById('category-select')
    },
    // category 등록 함수
    register(category) {
        const categorySelect = this.getCategorySelect();
        fetch('/api/register/category', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                categoryName: category
            })
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error('서버 응답이 올바르지 않습니다.');
                }
                return response.json();
            })
            .then(data => {
                // 새 카테고리 선택 옵션에 추가
                const newOption = document.createElement('option');
                newOption.value = data.categoryName;  // 서버에서 반환된 ID
                newOption.textContent = data.categoryName;
                categorySelect.appendChild(newOption);

                // 새로 추가된 카테고리 선택
                categorySelect.value = data.categoryNo;

                // 성공 메시지
                alert(`'${data.categoryName}' 카테고리가 추가되었습니다.`);
            })
            .catch(error => {
                alert('카테고리 추가 중 오류가 발생했습니다: ' + error.message);
            });
    },
    lookupCategory() {
        const categorySelect = this.getCategorySelect();
        fetch('/api/lookup/category')
            .then(response => response.json())
            .then(categoryNames => {
                // 카테고리 옵션 추가
                categoryNames.forEach(categoryName => {
                    const option = document.createElement('option');
                    option.value = categoryName;
                    option.textContent = categoryName;
                    categorySelect.appendChild(option);
                });
            })
            .catch(error => console.error('카테고리 목록을 가져오는 중 오류 발생:', error));
    }

}
