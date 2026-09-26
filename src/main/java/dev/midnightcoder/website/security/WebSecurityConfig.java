package dev.midnightcoder.website.security;

import dev.midnightcoder.identity.MidnightUserDetailsService;
import dev.midnightcoder.website.jwt.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author Glabay | The Midnight Coder
 * @project Midnight-Technician
 * @social Discord: Glabay
 * @website <a href="https://midnightcoder.dev">Midnight Coder</a>
 * @since 2026-09-24
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfig implements WebMvcConfigurer {
    private final PasswordEncoder passwordEncoder;
    private final MidnightUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    public WebSecurityConfig(PasswordEncoder passwordEncoder,
                             @Qualifier("midnightUserDetailsService")
                             MidnightUserDetailsService userDetailsService,
                             JwtAuthenticationFilter jwtAuthenticationFilter,
                             CustomAuthenticationEntryPoint customAuthenticationEntryPoint
    ) {
        this.passwordEncoder = passwordEncoder;
        this.userDetailsService = userDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.customAuthenticationEntryPoint = customAuthenticationEntryPoint;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        return http.sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(request -> request
                .requestMatchers(HttpMethod.GET,
                    "/css/**",
                    "/img/**",
                    "/js/**",
                    "/webjars/**"
                ).permitAll()
                .requestMatchers(
                    "/",
                    "/home",
                    "/index",
                    "/error",
                    "/auth/**"
                ).permitAll()
                .requestMatchers(
                    "/dashboard/**"
                ).hasRole("USER")
                .requestMatchers(
                    "/api/**"
                ).authenticated()
                .anyRequest().authenticated()
            )
            .exceptionHandling(exception ->
                                   exception.authenticationEntryPoint(customAuthenticationEntryPoint)
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }

    @Bean
    public MidnightUserDetailsService customUserDetails() {
        return userDetailsService;
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        var provider = new DaoAuthenticationProvider(customUserDetails());
            provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

}
