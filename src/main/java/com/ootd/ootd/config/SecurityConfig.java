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
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/login",
                                "/signup",
                                "/api/auth/login",
                                "/api/auth/signup",
                                "/view/**",
                                "/css/**",
                                "/js/**",
                                "/img/**",
                                "/images/**",
                                "/favicon.ico",
                                "/webjars/**",
                                "/error",
                                "/main",
                                "/mypage",
                                "/liked-products",
                                "/order-history",
                                "/cancel-history",
                                "/api/reviews/my-reviews",
                                "/products/*/after-reviews",
                                "/products/**",
                                "/enter/product",
                                "/api/register/colors",
                                "/api/lookup/colors",
                                "/api/register/category",
                                "/api/search/category",
                                "/api/register/brands",
                                "/api/lookup/brands",
                                "/api/products",
                                "/api/select/product/{productNo}",
                                "/api/products/ranking",
                                "/api/products/ranking/category",
                                "/api/products/recommended",
                                "/api/products/sale",
                                "/api/products/categories/main",
                                "/api/products/categories/sub",
                                "/ranking",
                                "/api/ranking/**",
                                "/goPay",
                                "/orders",
                                "/orders/update",
                                "/payments/save",
                                "/validation/{imp_uid}",
                                "/api/getImpUid",
                                "/payments/cancel/{imp_uid}",
                                "/cart",
                                "/cart/add",
                                "/admin/**",
                                "/api/coupons/**",
                                "/coupon/**",
                                "/api/categories",
                                "/points",
                                "/points/history",
                                "/points/statistics",
                                "/api/points/earn-rate",
                                "/recommended",
                                "/sale",
                                "/api/products/recommended",
                                "/api/products/sale",
                                "/sendImpUid",
                                "/api/promotion/**",
                                "/api/auth/info"
                        ).permitAll()
                        .requestMatchers(
                                "/api/auth/mypage",
                                "/api/auth/change-password",
                                "/api/auth/user-stats",
                                "/api/auth/liked-products",
                                "/api/auth/order-history",
                                "/api/auth/cancel-history",
                                "/api/auth/cancel-order/*",
                                "/products/*/like-info",
                                "/products/*/like",
                                "/products/*/reviews",
                                "/products/*/review",
                                "/products/*/order",
                                "/products/*/order-status",
                                "/products/*/cancel-order",
                                "/products/*/after-review",
                                "/products/*/after-review-permission",
                                "/api/reviews/my-reviews-data",
                                "/api/reviews/**",
                                "/api/points/my-points",
                                "/api/points/history",
                                "/api/points/history/all",
                                "/api/points/use",
                                "/api/points/refund",
                                "/api/points/can-use",
                                "/api/points/statistics",
                                "/api/points/recent",
                                "/api/auth/update-order-payment",
                                "/user-orders/**"
                        ).authenticated()
                        .anyRequest().authenticated()
                )
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                )
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
