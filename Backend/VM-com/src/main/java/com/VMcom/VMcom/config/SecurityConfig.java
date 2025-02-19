package com.VMcom.VMcom.config;


import com.VMcom.VMcom.services.AppUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {


    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AppUserService appUserService;
    private final LogoutHandler logoutHandler;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    @Value("${application.security.cors.allowed-origins}")
    private String allowedOrigins;

    private static final String[] WHITE_LIST_URL = {

            //Products
            "/api/v1/product/getAll",
            "/api/v1/product/get/**",
            "/api/v1/product/products",
            "/api/v1/product/totalAmountOfPagesAndProducts",
            "/api/v1/product/get/category/**",
            "/api/v1/product/productCategory/getAll",
            "/api/v1/product/productCategory/update/**",
            "/api/v1/product/images/**",
            //Security
            "/api/v1/auth/authenticate",
            "/api/v1/auth/register",
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests((requests) -> {
                    requests
                            .requestMatchers(WHITE_LIST_URL).permitAll()

                            //Admin
                            .requestMatchers("/api/v1/product/add").hasAuthority("ROLE_ADMIN")
                            .requestMatchers("/api/v1/product/update/**").hasAuthority("ROLE_ADMIN")
                            .requestMatchers("/api/v1/product/delete/**").hasAuthority("ROLE_ADMIN")
                            .requestMatchers("/api/v1/product/productCategory/add").hasAuthority("ROLE_ADMIN")
                            .requestMatchers("/api/v1/product/productCategory/delete/**").hasAuthority("ROLE_ADMIN")
                            .requestMatchers("/api/v1/product/productSpecificationLine/**").hasAuthority("ROLE_ADMIN")
                            .requestMatchers("/api/v1/product/add/productPhoto").hasAuthority("ROLE_ADMIN")
                            .requestMatchers("/api/v1/admin/appUserOrder/**").hasAuthority("ROLE_ADMIN")
                            .requestMatchers("/api/v1/admin/appUserOrders").hasAuthority("ROLE_ADMIN")


                            //User

                            //Admin and User
                            .requestMatchers("/api/v1/address").hasAnyAuthority("ROLE_ADMIN", "ROLE_USER")
                            .requestMatchers("/api/v1/address/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_USER")
                            .requestMatchers("/api/v1/addresses").hasAnyAuthority("ROLE_ADMIN", "ROLE_USER")
                            .requestMatchers("/api/v1/appUser").hasAnyAuthority("ROLE_ADMIN", "ROLE_USER")
                            .requestMatchers("/api/v1/shopCart").hasAnyAuthority("ROLE_ADMIN", "ROLE_USER")
                            .requestMatchers("/api/v1/shopCartLine").hasAnyAuthority("ROLE_ADMIN", "ROLE_USER")
                            .requestMatchers("/api/v1/appUserOrder").hasAnyAuthority("ROLE_ADMIN", "ROLE_USER")
                            .requestMatchers("/api/v1/appUserOrder/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_USER")
                            .requestMatchers("/api/v1/appUserOrders").hasAnyAuthority("ROLE_ADMIN", "ROLE_USER")
                            .requestMatchers("/api/v1/auth/change-password").hasAnyAuthority("ROLE_ADMIN", "ROLE_USER")
                            .requestMatchers("/api/v1/auth/refresh-token").hasAnyAuthority("ROLE_ADMIN", "ROLE_USER")
                            .anyRequest()
                            .authenticated();
                })
                .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .accessDeniedHandler(customAccessDeniedHandler)
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                        )
                .logout(logout -> logout
                .logoutUrl("/api/v1/logout")
                .addLogoutHandler(logoutHandler)
                .logoutSuccessHandler((request, response, authentication) -> {
                    SecurityContextHolder.clearContext();
                })
                );



        return (SecurityFilterChain)http.build();
    }


    @Bean
    public AuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(appUserService);
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
       return configuration.getAuthenticationManager();
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.applyPermitDefaultValues();
        configuration.setAllowedOrigins(List.of(allowedOrigins));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD","PATCH"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }


}
