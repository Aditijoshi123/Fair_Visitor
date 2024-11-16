package com.vms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf(httpSecurityCsrfConfigurer -> httpSecurityCsrfConfigurer.disable())
                .authorizeHttpRequests(auth ->{
                    auth.requestMatchers("/admin/**").hasAnyAuthority("ADMIN")
                            .requestMatchers("/gatekeeper/**").hasAnyAuthority("GATEKEEPER")
                            .requestMatchers("/resident/**").hasAnyAuthority("RESIDENT")
                            .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                            .anyRequest()
                            .authenticated();
                })
                .formLogin(Customizer.withDefaults())
                .httpBasic(Customizer.withDefaults());

        return httpSecurity.build();
    }

	public static void main(String[] args) {
		PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		System.out.println(passwordEncoder.encode("aditi@123"));
	}
}
