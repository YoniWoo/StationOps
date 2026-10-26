package altn72.tpfilrouge.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class ConfigurationHorloge {
    @Bean
    public Clock horloge()
    {
        return Clock.system(ZoneId.of("Europe/Paris"));
    }
}
