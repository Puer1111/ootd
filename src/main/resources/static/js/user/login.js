// 로그인 성공 후 토큰 저장 및 마이페이지 접근을 위한 개선된 JavaScript

document.addEventListener('DOMContentLoaded', function() {
    const form = document.getElementById('loginForm');
    const email = document.getElementById('email');
    const password = document.getElementById('password');
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

    // 각 필드 검증 이벤트 등록
    email.addEventListener('blur', function() {
        validateInput(email, isValidEmail, '유효한 이메일 주소를 입력해주세요.');
    });

    password.addEventListener('blur', function() {
        validateInput(password, value => value.length >= 1, '비밀번호를 입력해주세요.');
    });

    // 마이페이지 접근 함수 (JWT 토큰 포함)
    function accessMypage() {
        // ✅ 키 통일: 'token'으로 변경
        const token = localStorage.getItem('token');

        if (!token) {
            alert('로그인이 필요합니다.');
            return;
        }

        // JWT 토큰을 Authorization 헤더에 포함하여 요청
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
                // ✅ 키 통일: 'token'으로 변경
                localStorage.removeItem('token');
                window.location.href = '/login';
            });
    }

    // 폼 제출 처리
    form.addEventListener('submit', function(e) {
        e.preventDefault();

        // 전체 폼 검증
        const isEmailValid = validateInput(email, isValidEmail, '유효한 이메일 주소를 입력해주세요.');
        const isPasswordValid = validateInput(password, value => value.length >= 1, '비밀번호를 입력해주세요.');

        // 모든 검증이 통과되면 폼 제출
        if (isEmailValid && isPasswordValid) {
            // 폼 데이터를 JSON으로 변환
            const formData = {
                email: email.value.trim(),
                password: password.value
            };

            // AJAX를 사용하여 폼 제출
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
                        // 성공 메시지 표시
                        formMessage.textContent = '로그인 성공! 메인 페이지로 이동합니다.';
                        formMessage.className = 'form-message success-message';

                        // ✅ 토큰 저장 - 키 통일: 'token'으로 변경
                        if (data.token) {
                            localStorage.setItem('token', data.token);
                            console.log('토큰 저장 완료:', data.token);
                        }

                        // ✅ 메인 페이지로 이동 (마이페이지 대신)
                        setTimeout(() => {
                            window.location.href = '/';
                        }, 1000);
                    } else {
                        // 오류 메시지 표시
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