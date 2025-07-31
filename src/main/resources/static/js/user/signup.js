document.addEventListener('DOMContentLoaded', function() {
    const form = document.getElementById('signupForm');
    const username = document.getElementById('username');
    const email = document.getElementById('email');
    const name = document.getElementById('name');
    const phone = document.getElementById('phone');
    const password = document.getElementById('password');
    const confirmPassword = document.getElementById('confirmPassword');
    const formMessage = document.getElementById('form-message');

    function showError(input, message) {
        const formGroup = input.parentElement;
        formGroup.classList.add('show-error');
        const errorElement = formGroup.querySelector('.error-message');
        errorElement.textContent = message;
    }

    function clearError(input) {
        const formGroup = input.parentElement;
        formGroup.classList.remove('show-error');
    }

    function validateInput(input, validationFn, errorMessage) {
        if (!validationFn(input.value.trim())) {
            showError(input, errorMessage);
            return false;
        } else {
            clearError(input);
            return true;
        }
    }

    function isValidEmail(email) {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return emailRegex.test(email);
    }

    function isValidPhone(phone) {
        const phoneRegex = /^010-\d{4}-\d{4}$/;
        return phoneRegex.test(phone);
    }

    phone.addEventListener('input', function() {
        let value = this.value.replace(/[^0-9]/g, '');
        if (value.length >= 3 && value.length <= 7) {
            value = value.replace(/(\d{3})(\d+)/, '$1-$2');
        } else if (value.length > 7) {
            value = value.replace(/(\d{3})(\d{4})(\d+)/, '$1-$2-$3');
        }
        this.value = value;
    });

    function isValidPassword(password) {
        return password.length >= 6; // 최소 6자 이상
    }

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

    form.addEventListener('submit', function(e) {
        e.preventDefault();

        const isUsernameValid = validateInput(username, value => value.length >= 2, '닉네임은 2자 이상이어야 합니다.');
        const isEmailValid = validateInput(email, isValidEmail, '유효한 이메일 주소를 입력해주세요.');
        const isNameValid = validateInput(name, value => value.length >= 1, '이름을 입력해주세요.');
        const isPhoneValid = validateInput(phone, isValidPhone, '010-1234-5678 형식으로 입력해주세요.');
        const isPasswordValid = validateInput(password, isValidPassword, '비밀번호는 최소 6자 이상이어야 합니다.');
        const isConfirmPasswordValid = validateInput(confirmPassword, value => value === password.value, '비밀번호가 일치하지 않습니다.');

        if (isUsernameValid && isEmailValid && isNameValid && isPhoneValid && isPasswordValid && isConfirmPasswordValid) {
            const formData = {
                username: username.value.trim(),
                email: email.value.trim(),
                name: name.value.trim(),
                phone: phone.value.trim(),
                password: password.value
            };

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
                        formMessage.textContent = '회원가입이 완료되었습니다! 로그인 페이지로 이동합니다.';
                        formMessage.classList.add('success-message');

                        setTimeout(() => {
                            window.location.href = '/login';
                        }, 2000);
                    } else {
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