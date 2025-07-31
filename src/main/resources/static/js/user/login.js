document.addEventListener('DOMContentLoaded', function() {
    const form = document.getElementById('loginForm');
    const email = document.getElementById('email');
    const password = document.getElementById('password');
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

    email.addEventListener('blur', function() {
        validateInput(email, isValidEmail, '유효한 이메일 주소를 입력해주세요.');
    });

    password.addEventListener('blur', function() {
        validateInput(password, value => value.length >= 1, '비밀번호를 입력해주세요.');
    });

    function accessMypage() {
         const token = localStorage.getItem('auth_token');


        if (!token) {
            alert('로그인이 필요합니다.');
            return;
        }

        fetch('/api/auth/mypage', {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        })
            .then(response => {
                if (response.ok) {
                    // 마이페이지로 이동
                    window.location.href = '/mypage';
                } else {
                    throw new Error('마이페이지 접근 실패');
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert('마이페이지 접근에 실패했습니다. 다시 로그인해주세요.');
                localStorage.removeItem('auth_token');
                window.location.href = '/login';
            });
    }

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
                    console.log('로그인 응답:', data);

                    if (data.success) {
                        formMessage.textContent = '로그인 성공! 메인 페이지로 이동합니다.';
                        formMessage.className = 'form-message success-message';

                        if (data.token) {
                            // localStorage.setItem('token', data.token);
                             localStorage.setItem('auth_token', data.token);

                            console.log('토큰 저장 완료:', data.token);
                        }

                        setTimeout(() => {
                            window.location.href = '/';
                        }, 1000);
                    } else {
                        formMessage.textContent = data.message || '이메일 또는 비밀번호가 올바르지 않습니다.';
                        formMessage.className = 'form-message error-message';
                    }
                })
                .catch(error => {
                    formMessage.textContent = '서버 오류가 발생했습니다. 나중에 다시 시도해주세요.';
                    formMessage.className = 'form-message error-message';
                    console.error('Error:', error);
                });
        }
    });
});