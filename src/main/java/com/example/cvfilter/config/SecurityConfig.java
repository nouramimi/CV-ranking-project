package com.example.cvfilter.config;

import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);
    private static final String[] swaggerWhiteListApi = {
            "api/v1/auth/**",
            "v3/api-docs/**",
            "v3/apis-docs.yaml",
            "/swagger-ui/**",
            "swagger-ui.html"
    };
    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, CustomCorsConfiguration customCorsConfiguration) throws Exception {
        logger.info("Configuring Security Filter Chain");

        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> {
                    logger.debug("Configuring authorization rules");
                    auth
                            .requestMatchers("/api/auth/**","/api/job-offers/getAll", "/api/job-offers/getById/**","/error").permitAll()

                            .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                            .requestMatchers(swaggerWhiteListApi).permitAll()

                            .requestMatchers("/api/cv/upload/**", "/api/cv/user/applications").hasRole("USER")
                            .requestMatchers("/api/job-offers/**").hasAnyRole("ADMIN", "HR_MANAGER")
                            .requestMatchers( "/api/cv/**").hasAnyRole("ADMIN", "HR_MANAGER")
                            .requestMatchers("/api/companies/**", "/api/hr-managers/**").hasRole("ADMIN")
                            .anyRequest().authenticated();
                })
                .cors(c -> c.configurationSource(customCorsConfiguration))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            logger.warn("Authentication failed for request: {} - {}",
                                    request.getRequestURI(), authException.getMessage());
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                                    "Authentication required");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            logger.warn("Access denied for request: {} - {}",
                                    request.getRequestURI(), accessDeniedException.getMessage());
                            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                                    "Access denied");
                        })
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationManager authManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}