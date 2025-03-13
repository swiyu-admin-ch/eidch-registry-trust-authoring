/*
 * SPDX-FileCopyrightText: 2025 Swiss Confederation
 *
 * SPDX-License-Identifier: MIT
 */

package ch.admin.bit.eid.trust_registry.authoring_service.config;

import com.nimbusds.jose.jwk.JWKSet;
import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.IncompatibleConfigurationException;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import java.text.ParseException;

@Slf4j
@Configuration
@Validated
@Data
@ConfigurationProperties(prefix = "application")
public class ApplicationProperties {
    
    /**
     * If set to true the service expects all
     * writing message bodies to be encoded as JWT
     */
    @NotNull
    private boolean enableJwtAuthentication;

    /**
     * If enableJWTAuthentication is set,
     * the JWTs will be checked against the public keys
     * stored in this json web key set.
     * Is expected to be a map with the key "keys"
     * <a href="https://datatracker.ietf.org/doc/html/rfc7517#appendix-A.1">Example how the JWKS is expected</a>
     */
    private String authenticationJwks;

    private JWKSet allowedKeySet;

    @PostConstruct
    public void init() {
        try {
            if (enableJwtAuthentication) {
                allowedKeySet = JWKSet.parse(authenticationJwks);
            }
        } catch (ParseException e) {
            log.error("Provided Allow JWKSet can not be parsed! %s".formatted(authenticationJwks));
            throw new IncompatibleConfigurationException("Provided Allow JWKSet can not be parsed! %s".formatted(authenticationJwks));
        }
    }
}
