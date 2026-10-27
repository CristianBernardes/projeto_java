package br.com.projetojava.auth.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security configuration.
 *
 * <p>Defines the security filter chain with JWT-based stateless authentication. Public endpoints
 * are explicitly listed; all others require a valid JWT. {@link RestAuthenticationEntryPoint} and
 * {@link RestAccessDeniedHandler} ensure {@code 401}/{@code 403} rejections use the same {@code
 * ErrorResponse} contract as every other API error.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtAuthFilter jwtAuthFilter;
  private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
  private final RestAccessDeniedHandler restAccessDeniedHandler;

  /**
   * Configures the security filter chain.
   *
   * <p>Rules:
   *
   * <ul>
   *   <li>Auth endpoints ({@code /api/v1/auth/**}) — public
   *   <li>{@code GET /api/v1/products} and {@code GET /api/v1/products/{id}} — public (read-only)
   *   <li>Swagger UI and API docs — public
   *   <li>Actuator health/info — public
   *   <li>All other requests — require valid JWT
   * </ul>
   *
   * @param http {@link HttpSecurity} to configure
   * @return configured {@link SecurityFilterChain}
   * @throws Exception if configuration fails
   */
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .exceptionHandling(
            e ->
                e.authenticationEntryPoint(restAuthenticationEntryPoint)
                    .accessDeniedHandler(restAccessDeniedHandler))
        .authorizeHttpRequests(
            auth ->
                auth
                    // Auth endpoints — public
                    .requestMatchers("/api/v1/auth/**")
                    .permitAll()
                    // Product read — public
                    .requestMatchers(HttpMethod.GET, "/api/v1/products", "/api/v1/products/**")
                    .permitAll()
                    // Swagger UI — public
                    .requestMatchers(
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/api-docs",
                        "/api-docs/**",
                        "/v3/api-docs",
                        "/v3/api-docs/**")
                    .permitAll()
                    // Actuator — public for health/info only
                    .requestMatchers(HttpMethod.GET, "/actuator/health", "/actuator/info")
                    .permitAll()
                    // Block all other actuator access
                    .requestMatchers("/actuator/**")
                    .denyAll()
                    // Everything else requires authentication
                    .anyRequest()
                    .authenticated())
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  /**
   * Provides the BCrypt password encoder bean.
   *
   * @return {@link BCryptPasswordEncoder} instance
   */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
