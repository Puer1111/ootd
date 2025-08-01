export const categoryAPI = {
    getCategorySelect() {
        return {
            first: document.getElementById('categoryChoiceFirst'),
            second: document.getElementById('categoryChoiceSecond')
        }
    },
    // category 등록 함수
    async register(mainCategory, subCategory) {
        const categorySelect = this.getCategorySelect().second;
        try {
            const response = await fetch('/api/register/category', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    mainCategory: mainCategory,
                    subCategory: subCategory
                })
            });

            if (!response.ok) {
                let errorMessage = '카테고리 추가 중 오류가 발생했습니다.';
                try {
                    const errorJson = await response.json();
                    if (errorJson && errorJson.message) {
                        errorMessage = errorJson.message;
                    } else {
                        errorMessage = '이미 등록 된 카테고리 입니다.';
                    }
                } catch (jsonParseError) {

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

            // 새 카테고리 선택 옵션에 추가
            const newOption = document.createElement('option');
            newOption.value = data.categoryNo;  // 서버에서 반환된 ID
            newOption.textContent = data.subCategory;
            categorySelect.appendChild(newOption);


            categorySelect.value = data.categoryNo;


            alert(`'${data.subCategory}' 카테고리가 추가되었습니다.`);

        } catch (error) {
            alert(error.message);
        }
    },

    lookupByMain() {
        const mainCategory = this.getCategorySelect().first.value;
        const categorySelect = this.getCategorySelect().second;

        fetch(`/api/search/category?mainCategory=${mainCategory}`, {
            method: 'GET'
        })
            .then(response => response.json())
            .then(subCategories => {
                this.clearSecondCategory();

                subCategories.forEach(Category => {
                    const option = document.createElement('option');
                    option.value = Category.categoryNo;
                    option.textContent = Category.subCategory;
                    categorySelect.appendChild(option);
                });
            })
            .catch(error => console.error('카테고리 목록을 가져오는 중 오류 발생:', error));
    },


    // 첫 번째 카테고리 변경 이벤트 바인딩
    bindCategoryChange() {
        const categorySelects = this.getCategorySelect();
        if (categorySelects.first) {
            categorySelects.first.addEventListener('change', this.handleCategoryChange.bind(this));
        }
    },

// 카테고리 변경 핸들러
    handleCategoryChange() {
        const categorySelects = this.getCategorySelect();
        const selectedValue = categorySelects.first.value;

        // 두 번째 카테고리 초기화
        this.clearSecondCategory();

        if (selectedValue) {
            this.lookupByMain()

        }
    },

// 두 번째 카테고리 초기화
    clearSecondCategory() {
        const secondSelect = this.getCategorySelect().second;
        // 기본 옵션만 남기고 모든 옵션 제거
        secondSelect.innerHTML = '<option value="">카테고리 선택</option>';
    },

// 초기화
    init() {
        this.bindCategoryChange();
        this.handleCategoryChange();
    }

}