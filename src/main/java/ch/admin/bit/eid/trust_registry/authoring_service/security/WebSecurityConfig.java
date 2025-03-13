/*
 * SPDX-FileCopyrightText: 2025 Swiss Confederation
 *
 * SPDX-License-Identifier: MIT
 */

package ch.admin.bit.eid.trust_registry.authoring_service.security;

import ch.admin.bit.eid.trust_registry.authoring_service.config.ApplicationProperties;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;

@Configuration
@AllArgsConstructor
@EnableWebSecurity
@EnableMethodSecurity
@Slf4j
public class WebSecurityConfig {
    private final ApplicationProperties applicationProperties;


    /**
     * We allow acces to swagger,api-docs and actuator without authentication.
     */
    @Bean
    @Order(1)
    public SecurityFilterChain publicFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatchers(matchers -> matchers.requestMatchers(
                        "/actuator/**",
                        "/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html"))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(requests -> requests.anyRequest().permitAll());
        return http.build();
    }

    @ConditionalOnProperty(name = "application.enable-jwt-authentication", havingValue = "true")
    @Bean
    @Order(2)
    public SecurityFilterChain jwtSecurityEnabledFilterChain(HttpSecurity http) throws Exception {
        var authenticationManager = http.getSharedObject(AuthenticationManagerBuilder.class)
                .authenticationProvider(new JwtAuthenticationProvider(applicationProperties.getAllowedKeySet()))
                .build();

        http
                .securityMatchers(matchers -> matchers.requestMatchers("/api/**"))
                .authenticationManager(authenticationManager)
                .addFilterBefore(new SimpleBearerTokenAuthenticationFilter(authenticationManager), AnonymousAuthenticationFilter.class)
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(requests -> requests.anyRequest().authenticated());
        return http.build();
    }

    @ConditionalOnProperty(name = "application.enable-jwt-authentication", havingValue = "false", matchIfMissing = true)
    @Bean
    @Order(3)
    public SecurityFilterChain jwtSecurityDisabledFilterChain(HttpSecurity http) throws Exception {
        log.warn("Service started with unsecured API");
        http
                .securityMatchers(matchers -> matchers.requestMatchers("/api/**"))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(requests -> requests.anyRequest().permitAll());
        return http.build();
    }
}
