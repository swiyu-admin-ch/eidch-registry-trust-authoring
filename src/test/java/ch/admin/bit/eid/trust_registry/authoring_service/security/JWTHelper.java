/*
 * SPDX-FileCopyrightText: 2025 Swiss Confederation
 *
 * SPDX-License-Identifier: MIT
 */

package ch.admin.bit.eid.trust_registry.authoring_service.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class JWTHelper {
    public static final String privateKey = """
               	{
            "kty":"EC",
            "d":"L5IOdH7GqpjqxeXRaQZvYNFs2qPdMVdNR1ohV0gjYVc",
            "crv":"P-256",
            "kid":"testkey",
            "x":"_gHQsZT-CB_KvIfpvJsDxVSXkuwRJsuof-oMihcupQU",
            "y":"71y_zEPAglUXBghaBxypTAzlNx57KNY9lv8LTbPkmZA"
            }""";

    public static String createBearerToken(long validitySeconds) throws ParseException, JOSEException {
        ECKey ecJWK = ECKey.parse(privateKey);
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .expirationTime(
                        Date.from(Instant.now().plus(validitySeconds, ChronoUnit.SECONDS))
                ).build();
        JWSHeader jwsHeader = new JWSHeader.Builder(JWSAlgorithm.ES256).keyID("testkey").build();
        SignedJWT jwt = new SignedJWT(jwsHeader, claimsSet);
        jwt.sign(new ECDSASigner(ecJWK));
        return jwt.serialize();
    }


    public static String createBearerToken() throws ParseException, JOSEException {
        return createBearerToken(2);
    }

    public static JWKSet jwkSet()  {
        try {
            return JWKSet.parse("""
                    {
                          "keys":[
                            {
                              "kty":"EC",
                              "crv":"P-256",
                              "kid":"testkey",
                              "x":"_gHQsZT-CB_KvIfpvJsDxVSXkuwRJsuof-oMihcupQU",
                              "y":"71y_zEPAglUXBghaBxypTAzlNx57KNY9lv8LTbPkmZA"
                            }
                          ]
                        }
                    """);
        } catch (ParseException e) {
            throw new IllegalStateException("failed to parse jwkset", e);
        }
    }
}
