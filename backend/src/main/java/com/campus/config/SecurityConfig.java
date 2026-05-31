package com.campus.config;

import com.campus.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
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
    private final JwtAuthenticationFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/login").permitAll()
                .requestMatchers("/api/sys/**").hasRole("admin")

                // 教务: 课程增删改→teacher/admin, 查看→所有人
                .requestMatchers(HttpMethod.GET, "/api/edu/courses", "/api/edu/courses/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/edu/courses", "/api/edu/courses/**").hasAnyRole("admin", "teacher")
                .requestMatchers(HttpMethod.PUT, "/api/edu/courses/**").hasAnyRole("admin", "teacher")
                .requestMatchers(HttpMethod.DELETE, "/api/edu/courses/**").hasAnyRole("admin", "teacher")
                .requestMatchers(HttpMethod.GET, "/api/edu/selections").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/edu/selections/course/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/edu/selections").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/edu/selections/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/edu/courses/teacher").authenticated()
                // 学期: 查看→所有人, 增删改→admin
                .requestMatchers(HttpMethod.GET, "/api/edu/semesters", "/api/edu/semesters/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/edu/semesters").hasRole("admin")
                .requestMatchers(HttpMethod.PUT, "/api/edu/semesters/**").hasRole("admin")
                .requestMatchers(HttpMethod.DELETE, "/api/edu/semesters/**").hasRole("admin")
                // 成绩: 查看→所有人, 录入→teacher/admin
                .requestMatchers(HttpMethod.GET, "/api/edu/grades", "/api/edu/grades/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/edu/grades").hasAnyRole("admin", "teacher")

                // 通知+指南: 查看→所有人, 增删改→teacher/admin
                .requestMatchers(HttpMethod.GET, "/api/admin/notifications", "/api/admin/notifications/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/admin/notifications").hasAnyRole("admin", "teacher")
                .requestMatchers(HttpMethod.PUT, "/api/admin/notifications/**").hasAnyRole("admin", "teacher")
                .requestMatchers(HttpMethod.DELETE, "/api/admin/notifications/**").hasAnyRole("admin", "teacher")
                .requestMatchers(HttpMethod.GET, "/api/admin/guides", "/api/admin/guides/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/admin/guides").hasRole("admin")
                .requestMatchers(HttpMethod.PUT, "/api/admin/guides/**").hasRole("admin")
                .requestMatchers(HttpMethod.DELETE, "/api/admin/guides/**").hasRole("admin")
                .requestMatchers("/api/admin/leaves/**").authenticated()

                .requestMatchers("/api/life/canteens/**", "/api/life/canteen-reviews/**").authenticated()
                .requestMatchers("/api/life/card-recharge/**").hasRole("student")
                .requestMatchers("/api/life/lost-found/**").authenticated()
                .requestMatchers("/api/club/**").authenticated()

                // 成长: 签到创建+评语→teacher/admin, 查看→所有人
                .requestMatchers(HttpMethod.POST, "/api/growth/checkin").hasAnyRole("teacher", "admin")
                .requestMatchers(HttpMethod.POST, "/api/growth/evaluation").hasAnyRole("teacher", "admin")
                .requestMatchers("/api/growth/**").authenticated()

                .requestMatchers(HttpMethod.POST, "/api/message/announcement").hasAnyRole("teacher", "admin")
                .requestMatchers("/api/message/**").authenticated()
                .requestMatchers("/api/ai/**").authenticated()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .cors(cors -> {});
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
