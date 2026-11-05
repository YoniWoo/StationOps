package stationops.api;

import stationops.dto.JetonCsrfDto;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Hidden
@RestController
public class CsrfApiControleur {

    @GetMapping("/api/csrf")
    public JetonCsrfDto consulterJeton(CsrfToken csrfToken)
    {
        return new JetonCsrfDto(csrfToken.getHeaderName(), csrfToken.getToken());
    }
}