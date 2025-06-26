export const categoryAPI = {
    getCategorySelect() {
        return {
            first: document.getElementById('categoryChoiceFirst'),
            second: document.getElementById('categoryChoiceSecond')
        }
    },
    // category 등록 함수
    register(mainCategory, subCategory) {
        const categorySelect = this.getCategorySelect().second;
        fetch('/api/register/category', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                mainCategory: mainCategory,
                subCategory: subCategory
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
                newOption.value = data.categoryNo;  // 서버에서 반환된 ID
                newOption.textContent = data.subCategory;
                categorySelect.appendChild(newOption);

                // 새로 추가된 카테고리 선택
                categorySelect.value = data.categoryNo;

                // 성공 메시지
                alert(`'${data.subCategory}' 카테고리가 추가되었습니다.`);
            })
            .catch(error => {
                alert('카테고리 추가 중 오류가 발생했습니다: ' + error.message);
            });
    },


    // lookupAllCategory() {
    //     const categorySelect = this.getCategorySelect().second;
    //     fetch('/api/lookup/category')
    //         .then(response => response.json())
    //         .then(subCategories => {
    //             // 카테고리 옵션 추가
    //             subCategories.forEach(Category => {
    //                 const option = document.createElement('option');
    //                 option.value = Category.categoryNo;
    //                 option.textContent = Category.subCategory;
    //                 categorySelect.appendChild(option);
    //             });
    //         })
    //         .catch(error => console.error('카테고리 목록을 가져오는 중 오류 발생:', error));
    // },

    lookupByMain() {
        const mainCategory = this.getCategorySelect().first.value;
        const categorySelect = this.getCategorySelect().second;
        const formData = new FormData();

        formData.append('mainCategory',mainCategory);
        fetch('/api/search/category', {
            method: 'POST',
            body: formData
        })
            .then(response => response.json())
            .then(subCategories => {
                this.clearSecondCategory();

                // 카테고리 옵션 추가
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

        // 선택된 첫 번째 카테고리에 따라 두 번째 카테고리 옵션 생성
        if (selectedValue) {
            this.lookupByMain()
            // this.populateSecondCategory(this.categoryData[selectedValue]);
        }
    },

// 두 번째 카테고리 초기화
    clearSecondCategory() {
        const secondSelect = this.getCategorySelect().second;
        // 기본 옵션만 남기고 모든 옵션 제거
        secondSelect.innerHTML = '<option value="">카테고리 선택</option>';
    },

// 두 번째 카테고리 옵션 추가
//     populateSecondCategory(categories) {
//         const secondSelect = this.getCategorySelect().second;
//
//         categories.forEach(category => {
//             const option = document.createElement('option');
//             option.value = category.value;
//             option.textContent = category.text;
//             secondSelect.appendChild(option);
//         });
//     },

// 초기화
    init() {
        this.bindCategoryChange();
        this.handleCategoryChange();
    }

}
