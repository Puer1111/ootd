export const brandAPI = {
    getBrandSelect() {
        return document.getElementById('brand-select');
    },

    // brand 등록 함수
    register(brandName,brandLogo,brandWeb) {
        fetch('/api/register/brands', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                brandName: brandName,
                brandLogoUrl: brandLogo,
                brandWebSite: brandWeb
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
                const brandSelect = this.getBrandSelect();
                newOption.value = data.brandNo;  // 서버에서 반환된 ID
                newOption.textContent = brandName;
                brandSelect.appendChild(newOption);

                // 새로 추가된 브랜드 선택
                brandSelect.value = data.brandNo;

                // 성공 메시지
                alert(`'${brandName}' 브랜드가 추가되었습니다.`);
            })
            .catch(error => {
                alert('브랜드 추가 중 오류가 발생했습니다: ' + error.message);
            });
    },
    lookupBrand(){
        const brandSelect = this.getBrandSelect();
        fetch('/api/lookup/brands')
            .then(response => response.json())
            .then(brandNames => {
                // 브랜드 옵션 추가
                brandNames.forEach(brandName => {
                    const option = document.createElement('option');
                    option.value = brandName;
                    option.textContent = brandName;
                    brandSelect.appendChild(option);
                });
            })
            .catch(error => console.error('브랜드 목록을 가져오는 중 오류 발생:', error));
    }
}
