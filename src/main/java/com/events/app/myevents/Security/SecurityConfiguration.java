package com.events.app.myevents.Security;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import static org.springframework.security.config.Customizer.withDefaults;

import com.events.app.myevents.component.JwtAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration {

    @Autowired
    JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http

                .authorizeHttpRequests(requests -> requests
                        .requestMatchers("/", "/css/*", "/js/*", "/img/**", "/user/confirm", "/user/create",
                                "/user/passwordRecovery", "/user/reset-password", "/error/**", "/api/auth/**")
                        .permitAll()
                        .requestMatchers("/api/events/**", "/api/invited/**", "/user/**", "/event/**", "/invited/**",
                                "/photo/**")
                        .hasAnyAuthority("ADMIN", "USER_VERIFIED")
                        .requestMatchers(HttpMethod.POST, "/**", "/invited/**", "/photo/upload/invite", "/event/**")
                        .hasAnyAuthority("ADMIN", "USER_VERIFIED")

                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true)
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll())

                .csrf(csrf -> csrf.disable())
                  .cors(withDefaults())

                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

        @Bean
    CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // 1. Permetti le credenziali (necessario per l'header Authorization con JWT)
        config.setAllowCredentials(true);

        // 2. Specifica i domini del tuo frontend
        // Quando sei in sviluppo
        config.addAllowedOrigin("http://10.0.2.2:8080"); // Per emulatore Android
        config.addAllowedOrigin("http://localhost:3000"); // Per local react
        // Quando sei su Render.com
        // Ricorda di cambiare "URL_FRONEND_RENDER" con l'URL effettivo del tuo sito
        // React su Render
        config.addAllowedOrigin("http://127.0.0.1:5500/"); 

        // 3. Permetti tutti i metodi HTTP
        config.addAllowedMethod("*");

        // 4. Permetti tutti gli header (cruciale per l'header Authorization)
        config.addAllowedHeader("*");

        // Applica questa configurazione a tutti i percorsi
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }


    @Bean
    UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://127.0.0.1:5500/", "http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();

    }

    @Bean
    DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    DatabaseUserDetailsService userDetailsService() {
        return new DatabaseUserDetailsService();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();

    }

}
