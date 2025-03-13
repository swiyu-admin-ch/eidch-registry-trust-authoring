package ch.admin.bit.eid.trust_registry.authoring_service.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;

import static java.util.Collections.emptyList;

/**
 * Simple authentication token that holds just the principal (key id) of the JWT.
 */
public class JwtAuthentication extends AbstractAuthenticationToken {
    /**
     * Principle value, here the key id of the JWT.
     */
    private final String principal;

    public JwtAuthentication(String principal) {
        super(emptyList());
        setAuthenticated(true);
        this.principal = principal;
    }

    @Override
    public String getPrincipal() {
        return principal;
    }

    @Override
    public Object getCredentials() {
        return null;
    }
}
