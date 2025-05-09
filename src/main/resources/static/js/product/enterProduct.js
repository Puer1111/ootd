document.addEventListener('DOMContentLoaded', function() {
    // 요소들 가져오기
    const brandSelect = document.getElementById('brand-select');
    const addBrandBtn = document.getElementById('add-brand-btn');
    const modal = document.getElementById('brand-modal');
    const closeBtn = document.querySelector('.close');
    const cancelBtn = document.getElementById('cancel-btn');
    const saveBrandBtn = document.getElementById('save-brand-btn');
    const newBrandInput = document.getElementById('new-brand');

    // 모달 열기
    addBrandBtn.addEventListener('click', function() {
        modal.style.display = 'block';
        newBrandInput.value = ''; // 입력 필드 초기화
        newBrandInput.focus();
    });

    // 모달 닫기 함수
    function closeModal() {
        modal.style.display = 'none';
    }

    // X 버튼 클릭 시 모달 닫기
    closeBtn.addEventListener('click', closeModal);

    // 취소 버튼 클릭 시 모달 닫기
    cancelBtn.addEventListener('click', closeModal);

    // 창 바깥 클릭 시 모달 닫기
    window.addEventListener('click', function(event) {
        if (event.target === modal) {
            closeModal();
        }
    });

    // 저장 버튼 클릭 시 새 브랜드 추가
    saveBrandBtn.addEventListener('click', function() {
        const brandName = newBrandInput.value.trim();

        if (brandName === '') {
            alert('브랜드명을 입력해주세요.');
            return;
        }

        // fetch API를 사용한 AJAX 요청
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
                newOption.value = data.brandId;  // 서버에서 반환된 ID
                newOption.textContent = brandName;
                brandSelect.appendChild(newOption);

                // 새로 추가된 브랜드 선택
                brandSelect.value = data.brandId;

                // 모달 닫기
                closeModal();

                // 성공 메시지
                alert(`'${brandName}' 브랜드가 추가되었습니다.`);
            })
            .catch(error => {
                alert('브랜드 추가 중 오류가 발생했습니다: ' + error.message);
            });
    });

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
});