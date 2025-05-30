export const colors ={
    getColorSelect() {
        return document.getElementById('product-color');
    },

    register(colorName){
        fetch('/api/register/colors', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                colorName: colorName
            })
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error('서버 응답이 올바르지 않습니다.');
                }
                return response.json();
            })
            .then(data => {
                const newOption = document.createElement('option');
                const colorSelect = this.getColorSelect();
                newOption.value = data.colorsNo;  // 서버에서 반환된 ID
                newOption.textContent = colorName;
                colorSelect.appendChild(newOption);

                // 새로 추가된 브랜드 선택
                colorSelect.value = data.colorsNo;

                // 성공 메시지
                alert(`'${colorName}' 색깔이 추가되었습니다.`);
            })
            .catch(error => {
                alert('색깔 추가 중 오류가 발생했습니다: ' + error.message);
            });
    },

    lookupColors(){
        const colorSelect = this.getColorSelect();
        fetch("/api/lookup/colors")
            .then(response => response.json())
                .then(colors => {
                    // 카테고리 옵션 추가
                    colors.forEach(color => {
                        const option = document.createElement('option');
                        option.value = color.colorNo;
                        option.textContent = color.colorName;
                        colorSelect.appendChild(option);
                    });
                })
                .catch(error => console.error('색깔 목록을 가져오는 중 오류 발생:', error));
    },
    updateSelectedInfo() {
        const colorSelect = this.getColorSelect();
        const selectedCount = colorSelect.selectedOptions.length;
        const selectedList = document.getElementById('selected-list');

        // 선택 개수 업데이트
        document.getElementById('selected-count').textContent = selectedCount;

        // 선택 목록 업데이트
        selectedList.innerHTML = '';
        // 오타 수정: select → colorSelect
        Array.from(colorSelect.selectedOptions).forEach(option => {
            const li = document.createElement('li');
            li.textContent = `${option.text}`;
            selectedList.appendChild(li);
        });
    },

    test() {
        const select = this.getColorSelect();
        // bind를 사용하여 this 컨텍스트 유지
        select.addEventListener('change', this.updateSelectedInfo.bind(this));
    },
}