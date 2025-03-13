package ch.admin.bit.eid.trust_registry.authoring_service.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;


/**
 * Provides the bearer token from the Authorization header to AuthenticationProvider implementation.
 */
@Slf4j
public class SimpleBearerTokenAuthenticationFilter extends AbstractPreAuthenticatedProcessingFilter {
    private static final String AUTH_TOKEN_HEADER_NAME = "Authorization";

    public SimpleBearerTokenAuthenticationFilter(AuthenticationManager authenticationManager) {
        setAuthenticationManager(authenticationManager);
    }

    @Override
    protected String getPreAuthenticatedPrincipal(HttpServletRequest request) {
        return request.getHeader(AUTH_TOKEN_HEADER_NAME);
    }

    @Override
    protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
        return "N/A"; // the filter does not provide an roles etc, it is just about authenticated or not
    }
}