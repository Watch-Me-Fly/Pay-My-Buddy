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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    // manage authorizations
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // return http.build(); with a connection form
        return http
                .authorizeHttpRequests(auth -> {
                    // Allow anyone to sign up
                    auth.requestMatchers("/register").permitAll();
                    auth.requestMatchers("/user").hasRole("USER");
                    // limit access if not connected
                    auth.anyRequest().authenticated();
                })
                // login
                .formLogin(form -> form
                        .loginPage("/login")
                        .permitAll())
                // logout
                 .logout(logout ->
                        logout.logoutUrl("/logout")
                                .logoutSuccessUrl("/logout?true"))
                .build();
    }

    // encrypt passwords
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
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