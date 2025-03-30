package com.paymybuddy.config;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);
    private final CustomFailureAuthenticationHandler failHandler = new CustomFailureAuthenticationHandler();
    private final CustomSuccessAuthenticationHandler successHandler = new CustomSuccessAuthenticationHandler();

    // Manage authorizations
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        logger.info("Security Filter Chain");
         http
            .csrf(AbstractHttpConfigurer::disable)
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
                     .requestMatchers( "/users/signup", "/login").permitAll()
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
             .formLogin(form -> form
                     .loginPage("/login")
                     .loginProcessingUrl("/login")
                     .defaultSuccessUrl("/user/profile.html")
                     .permitAll()
                     // for a more user-friendly feedback behaviour
                     .failureHandler(failHandler)
                     .successHandler(successHandler)
             )
            // logout
            .logout(logout -> logout
                    .logoutUrl("/logout")
                    .logoutSuccessUrl("/")
                    .invalidateHttpSession(true)
                    .deleteCookies("JSESSIONID")
                    .permitAll()
            );

        return http.build();
    }

    // encrypt passwords
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        logger.info("Password Encoder");
        return new BCryptPasswordEncoder();
    }

    // manage authentifications
    @Bean
    public AuthenticationManager authenticationManager(
            UserDetailsService userDetailsService,
            BCryptPasswordEncoder bCryptPasswordEncoder) throws Exception {

        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(bCryptPasswordEncoder);

        return new ProviderManager(authProvider);
    }

}