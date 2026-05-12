package com.sgsits.campusfix.config;

import com.sgsits.campusfix.repository.UserRepository;
import com.sgsits.campusfix.util.JwtUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.config.annotation.*;
import java.io.IOException;
import java.util.List;

@Configuration @EnableWebSecurity @EnableMethodSecurity @EnableAsync @RequiredArgsConstructor
public class AppConfig implements WebMvcConfigurer {
    private final JwtUtil jwt;
    private final UserRepository userRepo;
    @Value("${app.cors.origins}") private String origins;
    @Value("${app.upload.dir}")   private String uploadDir;

    @Bean public BCryptPasswordEncoder encoder() { return new BCryptPasswordEncoder(12); }

    @Bean public SecurityFilterChain chain(HttpSecurity http) throws Exception {
        return http.csrf(c -> c.disable()).cors(c -> c.configurationSource(cors()))
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(a -> a
                // FIX: WhatsApp webhook must be public (WATI/Twilio posts here without auth)
                .requestMatchers("/api/auth/**", "/uploads/**", "/actuator/health",
                                 "/api/whatsapp/webhook").permitAll()
                .anyRequest().authenticated())
            .addFilterBefore(jwtFilter(), UsernamePasswordAuthenticationFilter.class)
            .build();
    }

    @Bean public OncePerRequestFilter jwtFilter() {
        return new OncePerRequestFilter() {
            @Override protected void doFilterInternal(HttpServletRequest req,
                    HttpServletResponse res, FilterChain chain) throws ServletException, IOException {
                String h = req.getHeader("Authorization");
                if (h != null && h.startsWith("Bearer ")) {
                    String t = h.substring(7);
                    if (jwt.valid(t)) userRepo.findByEmail(jwt.email(t)).ifPresent(u -> {
                        var auth = new UsernamePasswordAuthenticationToken(
                            u.getEmail(), null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + u.getRole().toUpperCase()))
                        );
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    });
                }
                chain.doFilter(req, res);
            }
        };
    }

    @Bean public CorsConfigurationSource cors() {
        var cfg = new CorsConfiguration();
        cfg.setAllowedOriginPatterns(List.of(origins.split(",")));
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setAllowCredentials(true);
        var src = new UrlBasedCorsConfigurationSource();
        src.registerCorsConfiguration("/**", cfg);
        return src;
    }

    @Override public void addResourceHandlers(ResourceHandlerRegistry r) {
        r.addResourceHandler("/uploads/**").addResourceLocations("file:" + uploadDir + "/");
    }
}
