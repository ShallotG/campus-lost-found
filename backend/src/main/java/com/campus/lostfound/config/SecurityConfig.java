package com.campus.lostfound.config;

import com.campus.lostfound.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> {})
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 认证接口 - 公开
                        .requestMatchers("/api/auth/login", "/api/auth/register").permitAll()
                        // 静态资源 - 公开
                        .requestMatchers("/uploads/**").permitAll()
                        // API文档 - 公开
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/doc.html").permitAll()
                        // 用户自助接口 - 需登录
                        .requestMatchers("/api/users/me/**").authenticated()
                        // 失主端 - 需要 USER 角色
                        .requestMatchers("/api/match-confirm/**").hasAuthority("ROLE_USER")
                        // 管理端 - 需要 STAFF 或 ADMIN 角色
                        .requestMatchers(HttpMethod.POST, "/api/lost-items/**").hasAnyAuthority("ROLE_STAFF", "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/lost-items/**").hasAnyAuthority("ROLE_STAFF", "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/lost-items/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("/api/claims/**").hasAnyAuthority("ROLE_STAFF", "ROLE_ADMIN")
                        .requestMatchers("/api/dashboard/**").hasAnyAuthority("ROLE_STAFF", "ROLE_ADMIN")
                        .requestMatchers("/api/users/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("/api/configs/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("/api/system-logs/**").hasAuthority("ROLE_ADMIN")
                        // 检索接口 - 需认证
                        .requestMatchers("/api/search/**").authenticated()
                        // 其他请求 - 需认证
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}