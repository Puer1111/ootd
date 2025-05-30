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
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Spring Security 설정 클래스
 * - JWT 기반 인증
 * - 정적 리소스 및 인증 예외 경로 허용
 * - CSRF, 폼 로그인, HTTP Basic 비활성화
 */
@Configuration
// @EnableWebSecurity(debug = true)  // 디버깅 활성화
@EnableWebSecurity  // 디버깅 제거
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;

    public SecurityConfig(JwtTokenProvider jwtTokenProvider,
                          @Lazy CustomUserDetailsService customUserDetailsService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.customUserDetailsService = customUserDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 비활성화
                .csrf(AbstractHttpConfigurer::disable)

                // CORS 설정 (필요한 경우)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

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
                                "/mypage",  // 페이지 접근은 허용
                                "/go"       // 페이지 접근은 허용
                                // 상품
                                "/enter",              // 상품 등록
                                "/enter/product",       // 상품- 등록 후 페이지
                                "/api/register/colors", // 상품-색깔 등록
                                "/api/lookup/colors",  // 상품-색깔 조회
                                "/api/register/category", // 상품-카테고리-등록
                                "/api/lookup/category", // 상품-카테고리-조회
                                "/api/register/brands" , // 상품-브랜드-등록
                                "/api/lookup/brands", // 상품-브랜드-조회
                                "/goPay"            // 결제 테스트
                        ).permitAll()                // 인증 없이 접근 허용
                        // /api/auth/mypage와 /mypage는 인증이 필요하도록 변경
                        .anyRequest().authenticated() // 나머지 요청은 인증 필요

                // 세션 관리 - JWT 사용하므로 STATELESS로 설정
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 기본 로그인/HTTP Basic 인증 비활성화
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)

                // 로그아웃 설정
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                )

                // JWT 인증 필터 등록
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtTokenProvider, customUserDetailsService);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // CORS 설정 (필요한 경우)
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}