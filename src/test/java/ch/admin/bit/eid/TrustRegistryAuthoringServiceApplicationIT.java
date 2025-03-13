package ch.admin.bit.eid;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TrustRegistryAuthoringServiceApplicationIT {
    @LocalServerPort
    int port;

    @Test
    void testHealth() throws URISyntaxException, IOException, InterruptedException {
        try (var client = HttpClient.newHttpClient()) {
            var url = "http://localhost:%s/actuator/health/readiness".formatted(port);
            var request = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .GET()
                    .build();
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.body()).isEqualTo("{\"status\":\"UP\"}");
        }
    }

    @Test
    void test403() throws URISyntaxException, IOException, InterruptedException {
        try (var client = HttpClient.newHttpClient()) {
            var url = "http://localhost:%s/api/v1/entry/123".formatted(port);
            var request = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .GET()
                    .build();
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertThat(response.statusCode()).isEqualTo(403);
        }
    }
}