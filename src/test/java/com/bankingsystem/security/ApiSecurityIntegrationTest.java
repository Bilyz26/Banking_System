package com.bankingsystem.security;

import com.bankingsystem.bootstrap.BankingSystemApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = BankingSystemApplication.class)
@ActiveProfiles("secure")
class ApiSecurityIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUpMockMvc() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    void requiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/accounts/{accountId}", UUID.randomUUID()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"));
    }

    @Test
    void allowsAnonymousHealthProbeButProtectsMetrics() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));

        mockMvc.perform(get("/actuator/metrics"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"));

        mockMvc.perform(get("/actuator/metrics")
                        .with(scope("banking.monitor")))
                .andExpect(status().isOk());
    }

    @Test
    void allowsAnonymousAccessToReviewedApiDocumentation() throws Exception {
        mockMvc.perform(get("/openapi/banking-api.json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi").value("3.1.0"));

        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().isOk());
    }

    @Test
    void rejectsTokenWithoutRequiredScope() throws Exception {
        mockMvc.perform(get("/api/v1/accounts/{accountId}", UUID.randomUUID())
                        .with(scope("banking.write")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("INSUFFICIENT_SCOPE"));
    }

    @Test
    void readScopeReachesAccountQuery() throws Exception {
        mockMvc.perform(get("/api/v1/accounts/{accountId}", UUID.randomUUID())
                        .with(scope("banking.read")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ACCOUNT_NOT_FOUND"));
    }

    @Test
    void readScopeReachesCustomerQuery() throws Exception {
        mockMvc.perform(get("/api/v1/customers/{customerId}", UUID.randomUUID())
                        .with(scope("banking.read")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CUSTOMER_NOT_FOUND"));
    }

    @Test
    void readScopeReachesTransactionHistoryQuery() throws Exception {
        mockMvc.perform(get(
                        "/api/v1/accounts/{accountId}/transactions",
                        UUID.randomUUID())
                        .with(scope("banking.read")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ACCOUNT_NOT_FOUND"));
    }

    @Test
    void writeScopeReachesMoneyOperationWithoutCsrfToken() throws Exception {
        mockMvc.perform(post(
                        "/api/v1/accounts/{accountId}/deposits",
                        UUID.randomUUID())
                        .with(scope("banking.write"))
                        .header("Idempotency-Key", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 10.00,
                                  "currencyCode": "USD",
                                  "description": "Authorized deposit"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ACCOUNT_NOT_FOUND"));
    }

    @Test
    void adminScopeCanCreateCustomerButCannotTransferMoney() throws Exception {
        mockMvc.perform(post("/api/v1/customers")
                        .with(scope("banking.admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fullName": "Security Test",
                                  "emailAddress": "security-%s@example.com"
                                }
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/transfers")
                        .with(scope("banking.admin"))
                        .header("Idempotency-Key", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("INSUFFICIENT_SCOPE"));
    }

    @Test
    void adminScopeReachesCustomerUpdateWhileWriteScopeIsRejected() throws Exception {
        String request = """
                {
                  "fullName": "Updated Customer",
                  "emailAddress": "updated@example.com"
                }
                """;

        mockMvc.perform(put("/api/v1/customers/{customerId}", UUID.randomUUID())
                        .with(scope("banking.write"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("INSUFFICIENT_SCOPE"));

        mockMvc.perform(put("/api/v1/customers/{customerId}", UUID.randomUUID())
                        .with(scope("banking.admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CUSTOMER_NOT_FOUND"));
    }

    private static org.springframework.test.web.servlet.request.RequestPostProcessor scope(
            String scope) {
        return jwt().authorities(new SimpleGrantedAuthority("SCOPE_" + scope));
    }
}
