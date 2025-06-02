package com.yaxim.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yaxim.global.auth.CookieService;
import com.yaxim.global.auth.jwt.JwtAuthEntryPoint;
import com.yaxim.global.auth.jwt.JwtAuthenticationFilter;
import com.yaxim.global.auth.jwt.JwtProvider;
import com.yaxim.global.auth.oauth2.CustomOidcUserService;
import com.yaxim.global.auth.oauth2.OAuth2FailureHandler;
import com.yaxim.global.auth.oauth2.OAuth2SuccessHandler;
import com.yaxim.global.error.ErrorHandleFilter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
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
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtProvider jwtProvider;
    private final ObjectMapper objectMapper;
    private final CookieService cookieService;
    private final CustomOidcUserService oidcUserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final OAuth2FailureHandler oAuth2FailureHandler;

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setCacheSeconds(60);
        messageSource.setDefaultLocale(Locale.KOREA);
        messageSource.setUseCodeAsDefaultMessage(true);
        return messageSource;
    }

    @Bean(name = "messageSourceAccessor")
    public MessageSourceAccessor messageSourceAccessor(MessageSource messageSource) {
        return new MessageSourceAccessor(messageSource, Locale.getDefault());
    }

    @Bean
    public LocaleResolver localeResolver() {
        CustomLocaleResolver customLocaleResolver = new CustomLocaleResolver();
        customLocaleResolver.setDefaultLocale(Locale.KOREA);
        return customLocaleResolver;
    }

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
        lci.setParamName("lang");
        return lci;
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtProvider, cookieService);
    }

    @Bean
    public JwtAuthEntryPoint jwtAuthEntryPoint() {
        return new JwtAuthEntryPoint();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public ErrorHandleFilter errorHandleFilter() {
        return new ErrorHandleFilter(objectMapper, messageSource());
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowCredentials(true);
        config.addAllowedOriginPattern("*");
//        config.setAllowedOrigins(List.of("http://localhost:3000")); // Todo CORS 에러 Test 후 "*" 와 정확한 url 표기 중 하나 고르기
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           HandlerMappingIntrospector introspector
//            , ErrorHandleFilter errorHandleFilter
    ) throws Exception {
        return http
                .cors(httpSecurityCorsConfigurer -> corsConfigurationSource())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(
                        SessionCreationPolicy.STATELESS))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers("/webjars/**", "/swagger-resources/**", "/error", "/favicon.ico").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/login/**").permitAll()
                        .requestMatchers("/user/**").permitAll()
                        .requestMatchers("/auth/reissue").permitAll()
                        .requestMatchers("/projects/**").permitAll() // 🔥 테스트용 임시 추가
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(errorHandleFilter(), JwtAuthenticationFilter.class)
                .oauth2Login(oauth ->
                        oauth.userInfoEndpoint(c ->
                                c.oidcUserService(oidcUserService)
                                )
                                .successHandler(oAuth2SuccessHandler)
                                .failureHandler(oAuth2FailureHandler)
                )
                .build();
    }

    public static class CustomLocaleResolver extends AcceptHeaderLocaleResolver {

        String[] mLanguageCode = new String[]{"ko", "en"};
        List<Locale> mLocales = Arrays.asList(new Locale("en"), new Locale("es"), new Locale("ko"));

        @Override
        public Locale resolveLocale(HttpServletRequest request) {
            // 언어팩 변경
            String acceptLanguage = request.getHeader(HttpHeaders.ACCEPT_LANGUAGE);
            if (acceptLanguage == null || acceptLanguage.isEmpty()) {
                return Locale.getDefault();
            }
            List<Locale.LanguageRange> list = Locale.LanguageRange.parse(
                    request.getHeader("Accept-Language"));

            mLocales = new ArrayList<>();
            for (String code : mLanguageCode) {
                mLocales.add(new Locale(code));
            }
            return Locale.lookup(list, mLocales);
        }
    }
}
