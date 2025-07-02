export const colors ={

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
            const colorSelects = this.getColorSelect();
            fetch("/api/lookup/colors")
                .then(response => response.json())
                .then(colors => {
                    colorSelects.forEach(colorSelect => {  // 🔥 각 select마다 반복
                        // 기존 옵션 제거 (중복 방지)
                        colorSelect.innerHTML = '<option value="">-- 색깔 선택 --</option>';

                        colors.forEach(color => {
                            const option = document.createElement('option');
                            option.value = color.colorNo;
                            option.textContent = color.colorName;
                            colorSelect.appendChild(option);  // 🔥 개별 요소에 appendChild
                        });
                    });
                })
                    .catch(error => console.error('색깔 목록을 가져오는 중 오류 발생:', error));
        },

    getColorSelect() {
        return document.querySelector('select[name="colorsNo"]');  // name으로 변경
    },

}