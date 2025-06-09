package com.ootd.ootd.utils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class CookieUtils {
    // http 쿠키 값 찾기.
    public static String getCookieValue(HttpServletRequest request, String cookieName) {
        // 1. request에서 모든 쿠키 배열 가져오기
        try {
            jakarta.servlet.http.Cookie[] cookies = request.getCookies();

            if (cookies != null) {
                for (jakarta.servlet.http.Cookie cookie : cookies) {
                    if (cookieName.equals(cookie.getName())) {
                        String value = cookie.getValue();
                        // 빈 문자열 체크
                        return (value != null && !value.trim().isEmpty()) ? value : null;
                    }
                }
            }
        } catch (Exception e) {
            // 로그 기록
            log.error("쿠키 읽기 실패: " + cookieName, e);
        }
        return null;
    };

    public static void setCookie(HttpServletResponse response, String name, String value, int maxAge) {
        // 개발자가 직접 구현한 메서드
        Cookie cookie = new Cookie(name, value);  // ← 여기서 Java 기본 Cookie 클래스 사용
        cookie.setMaxAge(maxAge);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
    }
}

