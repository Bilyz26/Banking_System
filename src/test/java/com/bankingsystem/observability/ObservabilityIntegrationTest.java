package com.bankingsystem.observability;

import com.bankingsystem.bootstrap.BankingSystemApplication;
import com.bankingsystem.shared.presentation.CorrelationIdFilter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(
        classes = BankingSystemApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ObservabilityIntegrationTest {

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @LocalServerPort
    private int port;

    @Test
    void exposesHealthAndAvailabilityProbes() throws Exception {
        assertHealthy("/actuator/health");
        assertHealthy("/actuator/health/liveness");
        assertHealthy("/actuator/health/readiness");
    }

    @Test
    void exposesHttpMetricsForPrometheusAndDiagnostics() throws Exception {
        send(request("/api/v1/accounts/" + UUID.randomUUID()).build());

        HttpResponse<String> metrics =
                send(request("/actuator/metrics/http.server.requests").build());
        assertEquals(200, metrics.statusCode());
        assertTrue(metrics.body().contains("\"name\":\"http.server.requests\""));

        HttpResponse<String> prometheus =
                send(request("/actuator/prometheus").build());
        assertEquals(200, prometheus.statusCode());
        assertTrue(prometheus.body().contains("http_server_requests"));
    }

    @Test
    void echoesSafeCorrelationIdAndReplacesUnsafeValue() throws Exception {
        HttpResponse<String> safeResponse = send(request("/actuator/health")
                .header(CorrelationIdFilter.HEADER_NAME, "client-request_42")
                .build());
        assertEquals(
                "client-request_42",
                safeResponse.headers()
                        .firstValue(CorrelationIdFilter.HEADER_NAME)
                        .orElseThrow());

        HttpResponse<String> unsafeResponse = send(request("/actuator/health")
                .header(CorrelationIdFilter.HEADER_NAME, "unsafe value")
                .build());
        String replacement = unsafeResponse.headers()
                .firstValue(CorrelationIdFilter.HEADER_NAME)
                .orElseThrow();
        assertNotEquals("unsafe value", replacement);
        UUID.fromString(replacement);
    }

    private void assertHealthy(String path) throws Exception {
        HttpResponse<String> response = send(request(path).build());
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("\"status\":\"UP\""));
    }

    private HttpRequest.Builder request(String path) {
        return HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .GET();
    }

    private HttpResponse<String> send(HttpRequest request) throws Exception {
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
