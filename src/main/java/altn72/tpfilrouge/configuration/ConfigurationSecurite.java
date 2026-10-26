package altn72.tpfilrouge.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
                        .requestMatchers("/supervision", "/supervision/**")
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
