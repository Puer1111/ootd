document.addEventListener('DOMContentLoaded', function() {
    const form = document.getElementById('signupForm');
    const username = document.getElementById('username');
    const email = document.getElementById('email');
    const name = document.getElementById('name');
    const phone = document.getElementById('phone');
    const password = document.getElementById('password');
    const confirmPassword = document.getElementById('confirmPassword');
    const formMessage = document.getElementById('form-message');

    // 오류 메시지 표시 함수
    function showError(input, message) {
        const formGroup = input.parentElement;
        formGroup.classList.add('show-error');
        const errorElement = formGroup.querySelector('.error-message');
        errorElement.textContent = message;
    }

    // 오류 메시지 초기화 함수
    function clearError(input) {
        const formGroup = input.parentElement;
        formGroup.classList.remove('show-error');
    }

    // 입력 필드 검증 함수
    function validateInput(input, validationFn, errorMessage) {
        if (!validationFn(input.value.trim())) {
            showError(input, errorMessage);
            return false;
        } else {
            clearError(input);
            return true;
        }
    }

    // 이메일 검증 함수
    function isValidEmail(email) {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return emailRegex.test(email);
    }

    // 전화번호 검증 함수
    function isValidPhone(phone) {
        const phoneRegex = /^010-\d{4}-\d{4}$/;
        return phoneRegex.test(phone);
    }

    // 전화번호 자동 포맷팅
    phone.addEventListener('input', function() {
        let value = this.value.replace(/[^0-9]/g, '');
        if (value.length >= 3 && value.length <= 7) {
            value = value.replace(/(\d{3})(\d+)/, '$1-$2');
        } else if (value.length > 7) {
            value = value.replace(/(\d{3})(\d{4})(\d+)/, '$1-$2-$3');
        }
        this.value = value;
    });

    // 비밀번호 강도 검증 함수
    function isValidPassword(password) {
        return password.length >= 6; // 최소 6자 이상
    }

    // 각 필드 검증 이벤트 등록
    username.addEventListener('blur', function() {
        validateInput(username, value => value.length >= 2, '닉네임은 2자 이상이어야 합니다.');
    });

    email.addEventListener('blur', function() {
        validateInput(email, isValidEmail, '유효한 이메일 주소를 입력해주세요.');
    });

    name.addEventListener('blur', function() {
        validateInput(name, value => value.length >= 1, '이름을 입력해주세요.');
    });

    phone.addEventListener('blur', function() {
        validateInput(phone, isValidPhone, '010-1234-5678 형식으로 입력해주세요.');
    });

    password.addEventListener('blur', function() {
        validateInput(password, isValidPassword, '비밀번호는 최소 6자 이상이어야 합니다.');
    });

    confirmPassword.addEventListener('blur', function() {
        validateInput(confirmPassword, value => value === password.value, '비밀번호가 일치하지 않습니다.');
    });

    // 폼 제출 처리
    form.addEventListener('submit', function(e) {
        e.preventDefault();

        // 전체 폼 검증
        const isUsernameValid = validateInput(username, value => value.length >= 2, '닉네임은 2자 이상이어야 합니다.');
        const isEmailValid = validateInput(email, isValidEmail, '유효한 이메일 주소를 입력해주세요.');
        const isNameValid = validateInput(name, value => value.length >= 1, '이름을 입력해주세요.');
        const isPhoneValid = validateInput(phone, isValidPhone, '010-1234-5678 형식으로 입력해주세요.');
        const isPasswordValid = validateInput(password, isValidPassword, '비밀번호는 최소 6자 이상이어야 합니다.');
        const isConfirmPasswordValid = validateInput(confirmPassword, value => value === password.value, '비밀번호가 일치하지 않습니다.');

        // 모든 검증이 통과되면 폼 제출
        if (isUsernameValid && isEmailValid && isNameValid && isPhoneValid && isPasswordValid && isConfirmPasswordValid) {
            // 폼 데이터를 JSON으로 변환
            const formData = {
                username: username.value.trim(),
                email: email.value.trim(),
                name: name.value.trim(),
                phone: phone.value.trim(),
                password: password.value
            };

            // AJAX를 사용하여 폼 제출
            fetch('/api/auth/signup', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(formData)
            })
                .then(response => response.json())
                .then(data => {
                    if (data.message && data.message.includes('성공')) {
                        // 성공 메시지 표시
                        formMessage.textContent = '회원가입이 완료되었습니다! 로그인 페이지로 이동합니다.';
                        formMessage.classList.add('success-message');

                        // 잠시 후 로그인 페이지로 이동
                        setTimeout(() => {
                            window.location.href = '/login';
                        }, 2000);
                    } else {
                        // 오류 메시지 표시
                        formMessage.textContent = data.message || '회원가입 중 오류가 발생했습니다.';
                        formMessage.classList.add('error-message');
                    }
                })
                .catch(error => {
                    formMessage.textContent = '서버 오류가 발생했습니다. 나중에 다시 시도해주세요.';
                    formMessage.classList.add('error-message');
                    console.error('Error:', error);
                });
        }
    });
});