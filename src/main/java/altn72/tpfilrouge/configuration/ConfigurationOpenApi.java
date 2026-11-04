package altn72.tpfilrouge.configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "StationOps — API REST",
                version = "1.0",
                description = """
                        Consultation des stations et équipements,
                        et résolution des incidents.

                        Une connexion préalable via /login est nécessaire.
                        La résolution est réservée au responsable
                        et nécessite un jeton CSRF valide.
                        """
        )
)
public class ConfigurationOpenApi {
}