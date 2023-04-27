package com.tarasovic.irrigation.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;

@EnableWebSecurity
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .authorizeHttpRequests(auth -> {
                            auth.requestMatchers("/charts").permitAll();
                            auth.requestMatchers("/battery").permitAll();
                            auth.requestMatchers("/api/**")
                                    .access(new WebExpressionAuthorizationManager("(hasIpAddress('192.168.0.0/16') or hasIpAddress('0:0:0:0:0:0:0:1')) and hasRole('PICO')"));
                            auth.anyRequest().authenticated();
                        }
                )
                .csrf() //https://docs.spring.io/spring-security/reference/features/exploits/csrf.html
                .csrfTokenRepository(csrfTokenRepository())
                .and()
                .httpBasic(Customizer.withDefaults())
                .build();
    }

    private CsrfTokenRepository csrfTokenRepository() {
        HttpSessionCsrfTokenRepository csrfTokenRepository = new HttpSessionCsrfTokenRepository();
        csrfTokenRepository.setHeaderName("X-CSRF-Token");
        return csrfTokenRepository;
    }

    @Bean
    public UserDetailsService userDetailsService() {
        //encoding password -> PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder(); encoder.encode()
        UserDetails user =
                User.withUsername("raspberry-pico")
                        .password("{bcrypt}$2a$10$v2opgVrNU/jbqT1hKyb2VO44dcgalu2s0Lo93sg3ZeYALm95bLAfW")
                        .roles("PICO")
                        .build();

        return new InMemoryUserDetailsManager(user);
    }
}
