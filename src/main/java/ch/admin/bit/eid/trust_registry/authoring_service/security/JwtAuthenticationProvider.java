/*
 * SPDX-FileCopyrightText: 2025 Swiss Confederation
 *
 * SPDX-License-Identifier: MIT
 */

package ch.admin.bit.eid.trust_registry.authoring_service.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyType;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import java.text.ParseException;
import java.util.Date;
import java.util.regex.Pattern;

import static org.springframework.util.StringUtils.hasText;

/**
 * Authentication provider that validates a JWT token against the configured allowedKeySet.
 */
@RequiredArgsConstructor
public class JwtAuthenticationProvider implements AuthenticationProvider {
    private static final Pattern BEARER_REGEX = Pattern.compile("^bearer (?<token>.*)", Pattern.CASE_INSENSITIVE);
    private final JWKSet allowedKeySet;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        var bearerToken = (String) authentication.getPrincipal();
        if (!hasText(bearerToken)) {
            throw new BadCredentialsException("No Authorization header found");
        }
        var matches = BEARER_REGEX.matcher(bearerToken);
        if (!matches.find()) {
            throw new BadCredentialsException("No Bearer token found");
        }
        var jwt = matches.group("token");
        var kid = validateJwtAndExtractKeyId(jwt, allowedKeySet);
        return new JwtAuthentication(kid);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return PreAuthenticatedAuthenticationToken.class.isAssignableFrom(authentication);
    }

    /**
     * Validates the jwt and returns the keyid of the key used to sign the jwt.
     */
    private static String validateJwtAndExtractKeyId(String jwt, JWKSet allowedKeySet) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(jwt);
            JWSHeader jwtHeader = signedJWT.getHeader();
            JWK matchingKey = allowedKeySet.getKeyByKeyId(jwtHeader.getKeyID());
            if (matchingKey == null) {
                throw new BadCredentialsException("Bearer token not signed with an allowed key");
            }
            var kty = matchingKey.getKeyType();
            if (!signedJWT.verify(buildVerifier(kty, matchingKey))) {
                throw new BadCredentialsException("Request JWT verification failed");
            }
            if (new Date().after(signedJWT.getJWTClaimsSet().getExpirationTime())) {
                throw new BadCredentialsException("Request JWT expired");
            }
            return signedJWT.getHeader().getKeyID();
        } catch (ParseException | JOSEException e) {
            throw new BadCredentialsException("Bearer token is an invalid jwt");
        }
    }

    private static JWSVerifier buildVerifier(KeyType kty, JWK key) throws JOSEException {
        if (KeyType.EC.equals(kty)) {
            return new ECDSAVerifier(key.toECKey().toPublicJWK());
        } else if (KeyType.RSA.equals(kty)) {
            return new RSASSAVerifier(key.toRSAKey().toPublicJWK());
        }
        throw new JOSEException("Unsupported Key Type %s".formatted(kty));
    }
}
