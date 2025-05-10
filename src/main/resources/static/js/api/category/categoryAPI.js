export const categoryAPI = {
    // category 등록 함수
    register(category) {
        const categorySelect = document.getElementById('category-select');
        fetch('/api/register/category', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                category: category
            })
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error('서버 응답이 올바르지 않습니다.');
                }
                return response.json();
            })
            .then(data => {
                // 새 브랜드를 선택 옵션에 추가
                const newOption = document.createElement('option');
                newOption.value = data.categoryId;  // 서버에서 반환된 ID
                newOption.textContent = category;
                categorySelect.appendChild(newOption);

                // 새로 추가된 브랜드 선택
                categorySelect.value = data.categoryId;

                // 성공 메시지
                alert(`'${categoryName}' 브랜드가 추가되었습니다.`);
            })
            .catch(error => {
                alert('브랜드 추가 중 오류가 발생했습니다: ' + error.message);
            });
    }
}
