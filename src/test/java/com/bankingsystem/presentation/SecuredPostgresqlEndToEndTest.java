package com.bankingsystem.presentation;

import com.bankingsystem.bootstrap.BankingSystemApplication;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = BankingSystemApplication.class)
@ActiveProfiles({"postgresql", "secure"})
@Testcontainers(disabledWithoutDocker = true)
class SecuredPostgresqlEndToEndTest {

    @Container
    private static final PostgreSQLContainer POSTGRESQL =
            new PostgreSQLContainer("postgres:17-alpine")
                    .withDatabaseName("banking_e2e_test")
                    .withUsername("banking_test")
                    .withPassword("banking_test");

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private MockMvc mockMvc;

    @DynamicPropertySource
    static void registerPostgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRESQL::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRESQL::getUsername);
        registry.add("spring.datasource.password", POSTGRESQL::getPassword);
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
        jdbcTemplate.update("DELETE FROM ledger_entries");
        jdbcTemplate.update("DELETE FROM accounts");
        jdbcTemplate.update("DELETE FROM customers");
    }

    @Test
    void completesSecuredBankingJourneyAgainstMigratedPostgresql() throws Exception {
        String sourceCustomerId = createCustomer("Katherine Johnson", "katherine@example.com");
        String destinationCustomerId = createCustomer("Dorothy Vaughan", "dorothy@example.com");
        String sourceAccountId = openAccount(sourceCustomerId);
        String destinationAccountId = openAccount(destinationCustomerId);

        deposit(sourceAccountId, "125.00");
        transfer(sourceAccountId, destinationAccountId, "45.00");

        mockMvc.perform(get("/api/v1/accounts/{accountId}", sourceAccountId)
                        .with(token("banking.read")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(80.00));
        mockMvc.perform(get("/api/v1/accounts/{accountId}", destinationAccountId)
                        .with(token("banking.read")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(45.00));

        mockMvc.perform(get("/api/v1/accounts/{accountId}/transactions", sourceAccountId)
                        .with(token("banking.read")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions.length()").value(2))
                .andExpect(jsonPath("$.transactions[0].type").value("TRANSFER_DEBIT"))
                .andExpect(jsonPath("$.transactions[1].type").value("DEPOSIT"));
        mockMvc.perform(get(
                        "/api/v1/accounts/{accountId}/transactions",
                        destinationAccountId)
                        .with(token("banking.read")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions.length()").value(1))
                .andExpect(jsonPath("$.transactions[0].type").value("TRANSFER_CREDIT"));

        Integer migrationCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM flyway_schema_history WHERE success",
                Integer.class);
        assertTrue(migrationCount != null && migrationCount > 0);
        assertTrue(Boolean.TRUE.equals(jdbcTemplate.queryForObject(
                "SELECT version() LIKE 'PostgreSQL%'",
                Boolean.class)));
    }

    private String createCustomer(String fullName, String emailAddress) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/customers")
                        .with(token("banking.admin"))
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

    private String openAccount(String customerId) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/accounts")
                        .with(token("banking.admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ownerId": "%s",
                                  "currencyCode": "USD"
                                }
                                """.formatted(customerId)))
                .andExpect(status().isCreated())
                .andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.accountId");
    }

    private void deposit(String accountId, String amount) throws Exception {
        mockMvc.perform(post("/api/v1/accounts/{accountId}/deposits", accountId)
                        .with(token("banking.write"))
                        .header("Idempotency-Key", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": %s,
                                  "currencyCode": "USD",
                                  "description": "Secured E2E deposit"
                                }
                                """.formatted(amount)))
                .andExpect(status().isOk());
    }

    private void transfer(
            String sourceAccountId,
            String destinationAccountId,
            String amount) throws Exception {
        mockMvc.perform(post("/api/v1/transfers")
                        .with(token("banking.write"))
                        .header("Idempotency-Key", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sourceAccountId": "%s",
                                  "destinationAccountId": "%s",
                                  "amount": %s,
                                  "currencyCode": "USD",
                                  "description": "Secured E2E transfer"
                                }
                                """.formatted(
                                        sourceAccountId,
                                        destinationAccountId,
                                        amount)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sourceBalance").value(80.00))
                .andExpect(jsonPath("$.destinationBalance").value(45.00));
    }

    private static RequestPostProcessor token(String scope) {
        return jwt()
                .jwt(jwt -> jwt
                        .subject("banking-e2e-operator")
                        .claim("client_id", "banking-system-e2e")
                        .claim("scope", scope))
                .authorities(new SimpleGrantedAuthority("SCOPE_" + scope));
    }
}
