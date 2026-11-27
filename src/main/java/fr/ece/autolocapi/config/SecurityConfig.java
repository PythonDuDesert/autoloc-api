package fr.ece.autolocapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http)
            throws Exception {http.csrf(csrf -> csrf.disable()).authorizeHttpRequests(auth -> auth
                        // Frontend
                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/static/**",
                                "/favicon.ico"
                        ).permitAll()
                        // Actuator
                        .requestMatchers(
                                "/actuator/health",
                                "/actuator/prometheus"
                        ).permitAll()
                        // Authentification publique
                        .requestMatchers("/api/public/**").permitAll()
                        // Administration
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        // Données de gestion (données personnelles, finances) : ADMIN uniquement.
                        // L inscription est publique : sans ces règles, tout nouveau compte USER
                        // pourrait lire la liste complète des clients.
                        .requestMatchers(
                                "/api/clients/**",
                                "/api/agences/**",
                                "/api/contrats/**",
                                "/api/paiements/**",
                                "/api/maintenances/**",
                                "/api/reservations/**"
                        ).hasRole("ADMIN")
                        // Catalogue véhicules : lecture pour tous, écriture ADMIN
                        .requestMatchers(HttpMethod.POST, "/api/vehicules/**", "/api/equipements/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/vehicules/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/vehicules/**", "/api/equipements/**").hasRole("ADMIN")
                        // Confirmer / terminer une réservation : ADMIN (l annulation reste ouverte à USER)
                        .requestMatchers(HttpMethod.PATCH, "/api/reservations/*/statut").hasRole("ADMIN")
                        // API authentifiée
                        .requestMatchers("/api/**").hasAnyRole("USER", "ADMIN")
                        .anyRequest().authenticated()
                )
                .httpBasic(basic -> basic.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)));

        return http.build();
    }
}