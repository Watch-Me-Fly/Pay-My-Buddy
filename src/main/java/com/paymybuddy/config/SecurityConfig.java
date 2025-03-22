package com.paymybuddy.config;

import com.paymybuddy.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    // Manage authorizations
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> { auth
                        // Public access ------------------------------------------
                        .requestMatchers(
                                "/assets/css/**",
                                "/assets/js/**",
                                "/assets/images/**",
                                "/assets/icons/**").permitAll()
                        // landing page
                        .requestMatchers("/", "/index.html").permitAll()
                        // allow anyone to signup or login
                        .requestMatchers( "/signup.html", "/login.html").permitAll()
                        // Restricted access  ------------------------------------
                        .requestMatchers("/users/**", "/user/**").authenticated()
                        // access profile only when logged-in
                        .requestMatchers("/user/profile.html").authenticated()
                        // get relations and transactions if logged-in
                        .requestMatchers("/user/transactions.html", "/user/relations.html").authenticated()
                        // limit access if not connected
                        .anyRequest().authenticated();
                })
                // login
                .formLogin(login -> login
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/user/profile", true)
                        .permitAll()
                )
                // logout
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                    ).build();
    }

    // encrypt passwords
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    // manage authentifications
    @Bean
    public AuthenticationManager authenticationManager(
            CustomUserDetailsService customUserDetailService,
            HttpSecurity http,
            BCryptPasswordEncoder bCryptPasswordEncoder) throws Exception {

        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);

        authenticationManagerBuilder
                .userDetailsService(customUserDetailService)
                .passwordEncoder(bCryptPasswordEncoder);

        return authenticationManagerBuilder.build();
    }
}