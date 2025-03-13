package ch.admin.bit.eid.trust_registry.authoring_service.security;

import com.nimbusds.jose.JOSEException;
import org.junit.jupiter.api.Test;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import java.text.ParseException;

import static ch.admin.bit.eid.trust_registry.authoring_service.security.JWTHelper.jwkSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class JwtAuthenticationProviderTest {

    JwtAuthenticationProvider provider = new JwtAuthenticationProvider(jwkSet());

    @Test
    void authenticate() throws ParseException, JOSEException {
        // GIVEN
        var bearerToken = String.format("Bearer %s", JWTHelper.createBearerToken());
        var authenticationRequest = new PreAuthenticatedAuthenticationToken( bearerToken, "N/A");
        // WHEN
        var auth = provider.authenticate(authenticationRequest);
        // THEN
        assertThat(auth.getPrincipal()).isEqualTo("testkey");
        assertThat(auth.isAuthenticated()).isTrue();

    }
}