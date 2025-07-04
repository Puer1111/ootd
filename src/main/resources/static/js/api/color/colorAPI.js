export const colors ={

    async register(colorName) {
        try {
            const response = await fetch('/api/register/colors', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    colorName: colorName
                })
            });

            if (!response.ok) {
                let errorMessage = '색깔 추가 중 오류가 발생했습니다.';
                try {
                    const errorJson = await response.json();
                    if (errorJson && errorJson.message) {
                        errorMessage = errorJson.message;
                    } else {
                        errorMessage = '서버에서 알 수 없는 형식의 오류 응답을 받았습니다.';
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

            const colorSelects = this.getColorSelect(); // 모든 select 요소 가져오기

            colorSelects.forEach(colorSelect => { // 각 select 요소에 새 옵션 추가
                const newOption = document.createElement('option');
                newOption.value = data.colorsNo;  // 서버에서 반환된 ID
                newOption.textContent = colorName;
                colorSelect.appendChild(newOption);

                // 새로 추가된 색깔을 현재 select에서 선택 (선택 사항)
                colorSelect.value = data.colorsNo;
            });

            // 성공 메시지
            alert(`'${colorName}' 색깔이 추가되었습니다.`);

        } catch (error) {
            alert('색깔 추가 중 오류가 발생했습니다: ' + error.message);
        }
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
        return document.querySelectorAll('select[name="colorsNo"]');  // name으로 변경
    },

}