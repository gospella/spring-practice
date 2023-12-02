package com.example.springpractice.task3.conf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Веб конфигурация
 *
 * @author Klim Ross
 * @since 2023.12.02
 */
@Configuration
public class WebSecurityConfigurer {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public InMemoryUserDetailsManager userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails user = User.withUsername("spring")
                .password(passwordEncoder.encode("secret"))
                .roles("ADMIN")
                .build();
        return new InMemoryUserDetailsManager(user);
    }
}