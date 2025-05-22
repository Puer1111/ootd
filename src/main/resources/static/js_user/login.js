document.addEventListener('DOMContentLoaded', function() {
    const form = document.getElementById('loginForm');
    const email = document.getElementById('email');
    const password = document.getElementById('password');
    const formMessage = document.getElementById('form-message'); // 실제로 안먹어서 로그인html안에 애는 선언되야함 (모듈화) 실행시킬려면 객채처럼 묶어야함

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

    email.addEventListener('blur', function() {
        validateInput(email, isValidEmail, '유효한 이메일 주소를 입력해주세요.');
    });

    password.addEventListener('blur', function() {
        validateInput(password, value => value.length >= 1, '비밀번호를 입력해주세요.');
    });

    form.addEventListener('submit', function(e) {
        e.preventDefault();
        const isEmailValid = validateInput(email, isValidEmail, '유효한 이메일 주소를 입력해주세요.');
        const isPasswordValid = validateInput(password, value => value.length >= 1, '비밀번호를 입력해주세요.');

        if (isEmailValid && isPasswordValid) {
            const formData = {
                email: email.value.trim(),
                password: password.value
            };

            fetch('/api/auth/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(formData)
            })
                .then(response => response.json())
                .then(data => {
                    if (data.success) {
                        formMessage.textContent = '로그인 성공! 메인 페이지로 이동합니다.';
                        formMessage.classList.add('success-message');
                        if (data.token) {
                            localStorage.setItem('auth_token', data.token);
                        }
                        setTimeout(() => {
                            window.location.href = '/main';
                        }, 1000);
                    } else {
                        formMessage.textContent = data.message || '이메일 또는 비밀번호가 올바르지 않습니다.';
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
