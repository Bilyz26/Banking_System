package com.bankingsystem.bootstrap;

import com.bankingsystem.shared.presentation.SecurityErrorResponseWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import tools.jackson.databind.json.JsonMapper;

@Configuration
@Profile("secure")
public class SecurityConfiguration {

    @Bean
    SecurityErrorResponseWriter securityErrorResponseWriter(JsonMapper jsonMapper) {
        return new SecurityErrorResponseWriter(jsonMapper);
    }

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            SecurityErrorResponseWriter errorResponseWriter) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/webjars/swagger-ui/**",
                                "/openapi/banking-api.json")
                        .permitAll()
                        .requestMatchers(
                                "/actuator/health",
                                "/actuator/health/**")
                        .permitAll()
                        .requestMatchers(
                                "/actuator/info",
                                "/actuator/metrics",
                                "/actuator/metrics/**",
                                "/actuator/prometheus")
                        .hasAuthority("SCOPE_banking.monitor")
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/customers/*",
                                "/api/v1/accounts/*",
                                "/api/v1/accounts/*/transactions")
                        .hasAuthority("SCOPE_banking.read")
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/accounts/*/deposits",
                                "/api/v1/accounts/*/withdrawals",
                                "/api/v1/transfers")
                        .hasAuthority("SCOPE_banking.write")
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/customers",
                                "/api/v1/accounts",
                                "/api/v1/accounts/*/freeze",
                                "/api/v1/accounts/*/unfreeze",
                                "/api/v1/accounts/*/close")
                        .hasAuthority("SCOPE_banking.admin")
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/v1/customers/*")
                        .hasAuthority("SCOPE_banking.admin")
                        .anyRequest()
                        .denyAll())
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(errorResponseWriter)
                        .accessDeniedHandler(errorResponseWriter))
                .oauth2ResourceServer(resourceServer -> resourceServer
                        .jwt(Customizer.withDefaults())
                        .authenticationEntryPoint(errorResponseWriter)
                        .accessDeniedHandler(errorResponseWriter))
                .build();
    }
}
