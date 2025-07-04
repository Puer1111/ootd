export const brandAPI = {
    getBrandSelect() {
        return document.getElementById('brand-select');
    },

    // brand 등록 함수
    async register(brandName,brandLogo,brandWeb) {
        const brandSelect = this.getBrandSelect();
        try {
            const response = await fetch('/api/register/brands', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    brandName: brandName,
                    brandLogoUrl: brandLogo,
                    brandWebSite: brandWeb
                })
            });

            if (!response.ok) {
                let errorMessage = '브랜드 추가 중 오류가 발생했습니다.';
                try {
                    const errorJson = await response.json();
                    if (errorJson && errorJson.message) {
                        errorMessage = errorJson.message;
                    } else {
                        errorMessage = '이미 등록 된 브랜드 입니다.';
                    }
                } catch (jsonParseError) {
                    // JSON 파싱 실패 시, 텍스트로 시도
                    const errorText = await response.text();
                    if (errorText) {
                        errorMessage = errorText;
                    } else {
                        errorMessage = '서버 응답이 올바르지 않습니다.';
                    }
                }
                throw new Error(errorMessage);
            }

            const data = await response.json();

            // 새 브랜드를 선택 옵션에 추가
            const newOption = document.createElement('option');
            const brandId = String(data.brandNo);

            newOption.value = brandId;
            newOption.textContent = brandName;
            brandSelect.appendChild(newOption);

            // 새로 추가된 브랜드 선택
            brandSelect.value = data.brandNo;

            // 성공 메시지
            alert(`'${brandName}' 브랜드가 추가되었습니다.`);

        } catch (error) {
            alert(error.message);
        }
    },

    lookupBrand(){
        const brandSelect = this.getBrandSelect();
        fetch('/api/lookup/brands')
            .then(response => response.json())
            .then(brandList => {
                // 브랜드 옵션 추가
                brandList.forEach(brand => {
                    const option = document.createElement('option');
                    option.value = brand.brandNo;
                    option.textContent = brand.brandName;
                    brandSelect.appendChild(option);
                });
            })
            .catch(error => console.error('브랜드 목록을 가져오는 중 오류 발생:', error));
    }
}
