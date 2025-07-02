document.addEventListener('DOMContentLoaded', async function () {
    try {
        const response = await fetch('/api/categories');
        const categories = await response.json();
        const select = document.getElementById('subCategory');

        categories.forEach(category => {
            const option = document.createElement('option');
            option.value = category.subCategory;
            option.textContent = category.subCategory;
            select.appendChild(option);
        });
    } catch (error) {
        console.error('Error fetching categories:', error);
        alert('카테고리 목록을 불러오는 데 실패했습니다.');
    }
});

document.getElementById('coupon-form').addEventListener('submit', async function (e) {
    e.preventDefault();

    const formData = new FormData(this);
    const data = {};
    formData.forEach((value, key) => {
        data[key] = value;
    });

    try {
        const response = await fetch('/api/coupon/insert', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data)
        });

        if (response.ok) {
            alert('쿠폰이 성공적으로 등록되었습니다.');
            window.location.reload();
        } else {
            alert('쿠폰 등록에 실패했습니다.');
        }
    } catch (error) {
        console.error('Error:', error);
        alert('오류가 발생했습니다.');
    }
});
