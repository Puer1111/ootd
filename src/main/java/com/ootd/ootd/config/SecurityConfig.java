package com.ootd.ootd.config;

import com.ootd.ootd.security.JwtAuthenticationFilter;
import com.ootd.ootd.security.JwtTokenProvider;
import com.ootd.ootd.service.auth.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 설정 클래스
 * - JWT 기반 인증
 * - 정적 리소스 및 인증 예외 경로 허용
 * - CSRF, 폼 로그인, HTTP Basic 비활성화
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;

    public SecurityConfig(JwtTokenProvider jwtTokenProvider,
                          @Lazy CustomUserDetailsService customUserDetailsService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.customUserDetailsService = customUserDetailsService;
    }

    /**
     * 보안 필터 체인 설정
     * - 경로 별 인증/권한 설정
     * - JWT 필터 추가
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 비활성화 (JWT 기반 API 보안 방식)
                .csrf(AbstractHttpConfigurer::disable)

                // 요청 경로 별 인가 정책 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",                  // 루트
                                "/login",             // 로그인 페이지
                                "/signup",            // 회원가입 페이지
                                "/api/auth/login",    // 로그인 API
                                "/api/auth/signup",   // 회원가입 API
                                "/view/**",           // View 페이지
                                "/css/**",            // 정적 CSS
                                "/js/**",             // 정적 JS
                                "/img/**",            // 이미지
                                "/webjars/**",        // 웹 자원
                                "/error",             // 에러 페이지
                                "/main",
                                "/mypage",
                                "/liked-products",    // 좋아요 상품목록 페이지
                                "/order-history",      // 가벼운 주문하기
                                "/cancel-history",    // 취소 내역 페이지
                                // 상품
                                "/products/**",
                                "/enter",              // 상품 등록
                                "/enter/product",       // 상품- 등록 후 페이지
                                "/api/register/colors", // 상품-색깔 등록
                                "/api/lookup/colors",  // 상품-색깔 조회
                                "/api/register/category", // 상품-카테고리-등록
                                "/api/lookup/category", // 상품-카테고리-조회
                                "/api/register/brands", // 상품-브랜드-등록
                                "/api/lookup/brands", // 상품-브랜드-조회
                                "/goPay",                // 결제 테스트
                                "/orders",               // 주문 상품 등록
                                "/payments/save",   // 결제 저장
                                "/validation/{imp_uid}", // 결제 검증
                                "/api/getImpUid",        // 고객 번호 조회
                                "/payments/cancel/{imp_uid}", // 결제 취소
                                "/cart" , // 장바구니 페이지
                                "/cart/add" // 장바구니 담기


                        ).permitAll()                // 인증 없이 접근 허용

                        // ✅ 로그인이 필요한 경로들
                        .requestMatchers(
                                "/api/auth/mypage",
                                "/api/auth/change-password",
                                "/products/*/like-info",
                                "/products/*/reviews",
                                "/products/*/review",
                                "/products/*/like",
                                "/products/*/order",          // 주문하기 API
                                "/products/*/order-status",   // 주문 상태 확인 API
                                "/products/*/cancel-order",   // 주문 취소 API
                                "/api/auth/liked-products",
                                "/api/auth/order-history",    // 주문 내역 API
                                "/api/auth/cancel-history",   // 취소 내역 API
                                "/api/auth/cancel-order/*"    // 주문 취소 API (ID별)
                        ).authenticated()           // JWT 인증 필요

                        .anyRequest().authenticated() // 나머지 요청은 인증 필요
                )
                // 기본 로그인/HTTP Basic 인증 비활성화
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)

                // 로그아웃 설정
                .logout(logout -> logout
                        .logoutUrl("/logout")  // 로그아웃 요청 경로
                        .logoutSuccessUrl("/login") // 로그아웃 후 이동 경로
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                )

                // JWT 인증 필터 등록 (기존 인증 필터 앞에 위치)
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * JWT 인증 필터 등록
     * - 요청 시 JWT 토큰 유효성 검사 및 사용자 인증 처리
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtTokenProvider, customUserDetailsService);
    }

    /**
     * 비밀번호 암호화를 위한 PasswordEncoder 빈 등록
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}