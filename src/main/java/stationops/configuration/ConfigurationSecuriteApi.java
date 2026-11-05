package stationops.configuration;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Configuration
public class ConfigurationSecuriteApi {

    @Bean
    @Order(1)
    public SecurityFilterChain securityFilterChainApi(
            HttpSecurity http) throws Exception
    {
        http
                .securityMatcher("/api/**")
                .authorizeHttpRequests(autorisations -> autorisations
                        .requestMatchers(HttpMethod.POST,
                                "/api/incidents/*/resolution")
                        .hasRole("RESPONSABLE_EXPLOITATION")
                        .anyRequest().authenticated())
                .exceptionHandling(erreurs -> erreurs
                        .authenticationEntryPoint(
                                (requete, reponse, exception) ->
                                        reponse.sendRedirect(
                                                requete.getContextPath() + "/login"))
                        .accessDeniedHandler(
                                (requete, reponse, exception) ->
                                        ecrireErreur(reponse, 403, "Accès refusé : droits insuffisants ou jeton CSRF absent ou invalide."))
                )
                .requestCache(cache -> cache.disable());

        return http.build();
    }

    private void ecrireErreur(
            HttpServletResponse reponse,
            int statut,
            String message) throws IOException
    {
        reponse.setStatus(statut);
        reponse.setCharacterEncoding(StandardCharsets.UTF_8.name());
        reponse.setContentType("application/json");

        reponse.getWriter().write(
                "{\"statut\":" + statut
                        + ",\"message\":\"" + message + "\"}");
    }
}