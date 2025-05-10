export const brandAPI = {
    // brand 등록 함수
    register(brandName) {
        fetch('/api/register/brands', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                brandName: brandName
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
                const brandSelect = document.getElementById('brand-select');

                newOption.value = data.brandId;  // 서버에서 반환된 ID
                newOption.textContent = brandName;
                brandSelect.appendChild(newOption);

                // 새로 추가된 브랜드 선택
                brandSelect.value = data.brandId;

                // 성공 메시지
                alert(`'${brandName}' 브랜드가 추가되었습니다.`);
            })
            .catch(error => {
                alert('브랜드 추가 중 오류가 발생했습니다: ' + error.message);
            });
    }
}
