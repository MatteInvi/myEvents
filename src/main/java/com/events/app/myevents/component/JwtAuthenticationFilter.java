package com.events.app.myevents.component;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.events.app.myevents.Service.JwtService;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    JwtService jwtService;

    @Autowired
    UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // Controlla l'header di autorizzazione
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail; // Il tuo 'username'

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Estrai il token JWT
        jwt = authHeader.substring(7);
        userEmail = jwtService.extractUsername(jwt);

        // Autentica se l'utente è presente e non è già autenticato
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Carica i dettagli dell'utente (dal DB)
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            // Valida il token
            if (jwtService.isTokenValid(jwt, userDetails)) {

                // Crea un oggetto di autenticazione per Spring Security
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities());

                // Imposta i dettagli della richiesta
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request));

                // Imposta l'utente nel SecurityContextHolder
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}