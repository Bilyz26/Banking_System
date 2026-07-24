package com.bankingsystem.architecture;

import com.bankingsystem.bootstrap.BankingSystemApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = BankingSystemApplication.class)
class SwaggerUiIntegrationTest {

    private static final Path REVIEWED_CONTRACT =
            Path.of("docs", "openapi", "banking-api.json");

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUpMockMvc() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void servesReviewedOpenApiContractWithoutRuntimeDrift() throws Exception {
        String servedContract = mockMvc.perform(get("/openapi/banking-api.json"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode reviewed = objectMapper.readTree(Files.readString(REVIEWED_CONTRACT));
        JsonNode served = objectMapper.readTree(servedContract);

        assertEquals(reviewed, served);
    }

    @Test
    void exposesSwaggerUi() throws Exception {
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        org.hamcrest.Matchers.containsString("SwaggerUIBundle")))
                .andExpect(content().string(
                        org.hamcrest.Matchers.containsString(
                                "url: \"/openapi/banking-api.json\"")));

        mockMvc.perform(get(
                        "/webjars/swagger-ui/{version}/swagger-ui-bundle.js",
                        "5.32.2"))
                .andExpect(status().isOk());
    }
}
