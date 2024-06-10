package com.benjamin.Banking_app.Security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
    @Autowired
    MyUserDetailsService myUserDetailservice;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
            return httpSecurity
                    .csrf(AbstractHttpConfigurer::disable) //csrf is a protection from a common attack method
                    .authorizeHttpRequests(registry -> {
                        registry.requestMatchers("/home", "/register/**").permitAll();
                        registry.requestMatchers("/admin/**").hasRole("ADMIN");
                        registry.requestMatchers("/user/**").hasRole("USER");
                        registry.anyRequest().authenticated();
                       // registry.anyRequest().hasRole("ADMIN");
                    })
                    .formLogin(httpSecurityFormLoginConfigurer ->{
                            httpSecurityFormLoginConfigurer.
                                    loginPage("/login").
                                    permitAll();
                    })
                    .build();
        }

    @Bean
    public UserDetailsService userDetailsService(){
        return myUserDetailservice;
    }
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();//for loading users from the db or any other dao
        provider.setUserDetailsService(myUserDetailservice);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

