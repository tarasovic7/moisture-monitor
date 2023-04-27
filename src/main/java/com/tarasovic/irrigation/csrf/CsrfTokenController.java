package com.tarasovic.irrigation.csrf;

import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CsrfTokenController {

    @GetMapping("/api/csrf-token")
    public ResponseEntity<?> getCsrfToken(CsrfToken csrfToken) {
        return ResponseEntity.ok()
                .header(csrfToken.getHeaderName(), csrfToken.getToken())
                .build();
    }
}
