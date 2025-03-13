/*
 * SPDX-FileCopyrightText: 2025 Swiss Confederation
 *
 * SPDX-License-Identifier: MIT
 */

package ch.admin.bit.eid.trust_registry.authoring_service.it;

import ch.admin.bit.eid.trust_registry.authoring_service.security.JWTHelper;
import com.jayway.jsonpath.JsonPath;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
@AutoConfigureMockMvc
class DataStoreEntityControllerTest {

    private static final String ENTRY_BASE_URL = "/api/v1/entry/";
    private static final String VC_BASE_URL = "/api/v1/truststatement/";

    @Autowired
    protected MockMvc mvc;

    String createDatastoreEntry() throws Exception {
        var datastoreEntryResponse = mvc
                .perform(post(ENTRY_BASE_URL)
                        .header("Authorization", String.format("Bearer %s", JWTHelper.createBearerToken())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.status").value("SETUP"))
                .andExpect(jsonPath("$.files").isNotEmpty())
                .andExpect(jsonPath("$.files", Matchers.aMapWithSize(1)))
                .andExpect(jsonPath("$.files.TrustStatementV1").exists())
                .andExpect(jsonPath("$.files.TrustStatementV1.isConfigured").value(Boolean.FALSE))
                .andReturn()
                .getResponse()
                .getContentAsString();

        return JsonPath.parse(datastoreEntryResponse).read("$.id");
    }

    @Test
    void testCreateEntry_expired_authorization() throws Exception {
        var datastoreEntryResponse = mvc
                .perform(post(ENTRY_BASE_URL)
                        .header("Authorization", String.format("Bearer %s", JWTHelper.createBearerToken(-1))))
                .andExpect(status().isForbidden());
    }

    @Test
    void testCreateEntry_missing_authorization() throws Exception {
        var datastoreEntryResponse = mvc
                .perform(post(ENTRY_BASE_URL))
                .andExpect(status().isForbidden());
    }

    @Test
    void testCreateEntry_response() throws Exception {
        createDatastoreEntry();
    }

    @Test
    void testPutEntry_validTS_response() throws Exception {
        var datastoreEntryId = createDatastoreEntry();

        var trustStatement =
                "eyJ0eXAiOiJ2YytzZC1qd3QiLCJhbGciOiJFUzI1NiJ9.eyJpc3MiOiJkaWQ6dGR3OmFzZHNhZGFzZWQiLCJfc2QiOlsiRDFESWtTX1dhZEFDNEJYcXdiY1BMVnhyb2hmTldMVk9YRnF0TW0yN2l1ZyIsIkdQRzVfZ2lOS1FUbEloQzgwZ2owVHJEVXNwdGRVMG9aR0JueWVheHZ4U0kiLCJKdWJiUndmc3RXRmFyem04eGNPREtqR2t2dWMwNXd4cmJDSVI4ZzdQcE00Il0sIm5iZiI6MTcyMjQ5OTIwMCwiZXhwIjoxNzY3MTY4MDAwLCJpYXQiOjE3MjU2MTcxNzcsIl9zZF9hbGciOiJzaGEtMjU2In0.2fwQmQO-E1M-ZDbuWCu8MyRxDnd9-pGGwGMmTKgw-bj0ghX3orjP2N9tVReJX0f9D8bTY6pkbhb0k-cYzy-6WA~WyIySDlQaTRMeHBnMG1oa05KWjFuNXVBIiwib3JnTmFtZSIseyJlbiI6IkpvaG4gU21pdGhcdTAwMjdzIFNtaXRoZXJ5IiwiZGUtQ0giOiJKb2huIFNtaXRoXHUwMDI3cyBTY2htaWRlcmVpIn1d~WyJVcU9XUlBIbjVWSElaWTg4TTVWd2pBIiwicHJlZkxhbmciLCJlbiJd~WyJVbnlUVTVndTFIcm02QjBxajRmY253IiwibG9nb1VyaSIseyJlbiI6ImRhdGE6aW1hZ2UvcG5nO2Jhc2U2NCxpVkIuLi4ifV0~";
        mvc
                .perform(put(VC_BASE_URL + datastoreEntryId)
                        .header("Authorization", String.format("Bearer %s", JWTHelper.createBearerToken()))
                        .content(trustStatement))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.files").isNotEmpty())
                .andExpect(jsonPath("$.files", Matchers.aMapWithSize(1)))
                .andExpect(jsonPath("$.files.TrustStatementV1").exists())
                .andExpect(jsonPath("$.files.TrustStatementV1.isConfigured").value(Boolean.TRUE));

        mvc
                .perform(get(ENTRY_BASE_URL + datastoreEntryId)
                        .header("Authorization", String.format("Bearer %s", JWTHelper.createBearerToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.files").isNotEmpty())
                .andExpect(jsonPath("$.files", Matchers.aMapWithSize(1)))
                .andExpect(jsonPath("$.files.TrustStatementV1").exists())
                .andExpect(jsonPath("$.files.TrustStatementV1.isConfigured").value(Boolean.TRUE));
    }

    @Test
    void testCheckEntry_statusIsdisabled_response() throws Exception {
        var datastoreEntryId = createDatastoreEntry();

        mvc
                .perform(
                        patch(ENTRY_BASE_URL + datastoreEntryId)
                                .contentType("application/json")
                                .content(
                                        """
                                                {
                                                  "status": "DISABLED"
                                                }
                                                """
                                )
                                .header("Authorization", String.format("Bearer %s", JWTHelper.createBearerToken()))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.status").value("DISABLED"));

        mvc
                .perform(get(ENTRY_BASE_URL + datastoreEntryId)
                        .header("Authorization", String.format("Bearer %s", JWTHelper.createBearerToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.status").value("DISABLED"));
    }

    @Test
    void testCheckEntry_statusIsdeactivated_response() throws Exception {
        var datastoreEntryId = createDatastoreEntry();

        mvc
                .perform(
                        patch(ENTRY_BASE_URL + datastoreEntryId)
                                .contentType("application/json")
                                .content(
                                        """
                                                {
                                                  "status": "DEACTIVATED"
                                                }
                                                """
                                )
                                .header("Authorization", String.format("Bearer %s", JWTHelper.createBearerToken()))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.status").value("DEACTIVATED"));

        mvc
                .perform(get(ENTRY_BASE_URL + datastoreEntryId)
                        .header("Authorization", String.format("Bearer %s", JWTHelper.createBearerToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.status").value("DEACTIVATED"));
    }
}
