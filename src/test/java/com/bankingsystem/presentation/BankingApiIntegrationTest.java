package com.bankingsystem.presentation;

import com.bankingsystem.bootstrap.BankingSystemApplication;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = BankingSystemApplication.class)
class BankingApiIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUpMockMvc() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void completesCustomerAccountAndMoneyOperationJourney() throws Exception {
        String sourceCustomerId = createCustomer("Grace Hopper", "grace@example.com");
        String destinationCustomerId = createCustomer("Edsger Dijkstra", "edsger@example.com");
        String sourceAccountId = openAccount(sourceCustomerId);
        String destinationAccountId = openAccount(destinationCustomerId);
        String lifecycleAccountId = openAccount(sourceCustomerId);

        mockMvc.perform(post("/api/v1/accounts/{accountId}/deposits", sourceAccountId)
                        .header("Idempotency-Key", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 100.00,
                                  "currencyCode": "USD",
                                  "description": "Opening deposit"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(100.00))
                .andExpect(jsonPath("$.currencyCode").value("USD"))
                .andExpect(jsonPath("$.ledgerEntryId").isNotEmpty());

        mockMvc.perform(post("/api/v1/transfers")
                        .header("Idempotency-Key", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sourceAccountId": "%s",
                                  "destinationAccountId": "%s",
                                  "amount": 30.00,
                                  "currencyCode": "USD",
                                  "description": "Test transfer"
                                }
                                """.formatted(sourceAccountId, destinationAccountId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sourceBalance").value(70.00))
                .andExpect(jsonPath("$.destinationBalance").value(30.00))
                .andExpect(jsonPath("$.debitEntryId").isNotEmpty())
                .andExpect(jsonPath("$.creditEntryId").isNotEmpty());

        mockMvc.perform(post("/api/v1/accounts/{accountId}/withdrawals", destinationAccountId)
                        .header("Idempotency-Key", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 10.00,
                                  "currencyCode": "USD",
                                  "description": "Cash withdrawal"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(20.00));

        mockMvc.perform(get("/api/v1/accounts/{accountId}", sourceAccountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(70.00))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
        mockMvc.perform(get("/api/v1/accounts/{accountId}", destinationAccountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(20.00));

        changeStatus(lifecycleAccountId, "freeze", "FROZEN");
        changeStatus(lifecycleAccountId, "unfreeze", "ACTIVE");
        changeStatus(lifecycleAccountId, "close", "CLOSED");
    }

    @Test
    void returnsStableErrorsForInvalidInputAndMissingAccount() throws Exception {
        mockMvc.perform(post("/api/v1/accounts/{accountId}/deposits",
                        "4261d99d-9ba9-45e0-b55a-c7250f305e05")
                        .header("Idempotency-Key", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 0,
                                  "currencyCode": "usd"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors.amount").exists())
                .andExpect(jsonPath("$.fieldErrors.currencyCode").exists());

        mockMvc.perform(get("/api/v1/accounts/{accountId}",
                        "4261d99d-9ba9-45e0-b55a-c7250f305e05"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ACCOUNT_NOT_FOUND"));

        mockMvc.perform(get("/api/v1/accounts/not-a-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"));
    }

    @Test
    void replaysAnIdenticalDepositAndRejectsKeyReuse() throws Exception {
        String customerId = createCustomer("Barbara Liskov", "barbara@example.com");
        String accountId = openAccount(customerId);
        UUID idempotencyKey = UUID.randomUUID();
        String deposit = """
                {
                  "amount": 25.00,
                  "currencyCode": "USD",
                  "description": "Retry-safe deposit"
                }
                """;

        MvcResult firstResponse = mockMvc.perform(
                        post("/api/v1/accounts/{accountId}/deposits", accountId)
                                .header("Idempotency-Key", idempotencyKey)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(deposit))
                .andExpect(status().isOk())
                .andReturn();

        mockMvc.perform(post("/api/v1/accounts/{accountId}/deposits", accountId)
                        .header("Idempotency-Key", idempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(deposit))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ledgerEntryId").value(
                        JsonPath.read(
                                firstResponse.getResponse().getContentAsString(),
                                "$.ledgerEntryId").toString()))
                .andExpect(jsonPath("$.balance").value(25.00));

        mockMvc.perform(post("/api/v1/accounts/{accountId}/deposits", accountId)
                        .header("Idempotency-Key", idempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(deposit.replace("25.00", "30.00")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("IDEMPOTENCY_KEY_REUSED"));
    }

    private String createCustomer(String fullName, String emailAddress) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fullName": "%s",
                                  "emailAddress": "%s"
                                }
                                """.formatted(fullName, emailAddress)))
                .andExpect(status().isCreated())
                .andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.customerId");
    }

    private String openAccount(String ownerId) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ownerId": "%s",
                                  "currencyCode": "USD"
                                }
                                """.formatted(ownerId)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.containsString(
                        "/api/v1/accounts/")))
                .andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.accountId");
    }

    private void changeStatus(
            String accountId,
            String action,
            String expectedStatus) throws Exception {
        mockMvc.perform(post("/api/v1/accounts/{accountId}/{action}", accountId, action))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(accountId))
                .andExpect(jsonPath("$.status").value(expectedStatus));
    }
}
