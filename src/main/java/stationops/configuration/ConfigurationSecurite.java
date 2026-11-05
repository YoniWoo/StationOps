package stationops.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class ConfigurationSecurite {
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception
    {
        http
                .authorizeHttpRequests(autorisations -> autorisations
                        .requestMatchers("/login", "/error").permitAll()
                        // la page de connexion a besoin du css et des polices avant d'etre connecté
                        .requestMatchers("/css/**", "/js/**", "/fonts/**").permitAll()
                        .requestMatchers("/supervision", "/supervision/**")
                            .hasRole("RESPONSABLE_EXPLOITATION")
                        .requestMatchers(HttpMethod.POST,
                                "/incidents/*/affectation",
                                "/incidents/*/resolution")
                            .hasRole("RESPONSABLE_EXPLOITATION")
                        .requestMatchers("/incidents/*/evaluation")
                            .hasRole("RESPONSABLE_EXPLOITATION")
                        .anyRequest().authenticated()
                )
                .formLogin(connexion -> connexion
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/login?error")
                        .permitAll()
                )
                .logout(deconnexion -> deconnexion
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                );
        return http.build();
    }
}
