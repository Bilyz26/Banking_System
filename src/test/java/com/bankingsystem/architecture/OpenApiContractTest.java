package com.bankingsystem.architecture;

import com.bankingsystem.account.presentation.AccountResponse;
import com.bankingsystem.account.presentation.AccountStatusResponse;
import com.bankingsystem.account.presentation.MoneyOperationRequest;
import com.bankingsystem.account.presentation.MoneyOperationResponse;
import com.bankingsystem.account.presentation.OpenAccountRequest;
import com.bankingsystem.bootstrap.BankingSystemApplication;
import com.bankingsystem.customer.presentation.CreateCustomerRequest;
import com.bankingsystem.customer.presentation.CreateCustomerResponse;
import com.bankingsystem.ledger.presentation.AccountTransactionPageResponse;
import com.bankingsystem.ledger.presentation.AccountTransactionResponse;
import com.bankingsystem.shared.presentation.ApiError;
import com.bankingsystem.transfer.presentation.TransferMoneyRequest;
import com.bankingsystem.transfer.presentation.TransferMoneyResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = BankingSystemApplication.class)
class OpenApiContractTest {

    private static final Path CONTRACT =
            Path.of("docs", "openapi", "banking-api.json");
    private static final Set<String> HTTP_METHODS =
            Set.of("get", "post", "put", "patch", "delete");
    private static final Map<Class<?>, String> DTO_SCHEMAS = Map.ofEntries(
            Map.entry(CreateCustomerRequest.class, "CreateCustomerRequest"),
            Map.entry(CreateCustomerResponse.class, "Customer"),
            Map.entry(OpenAccountRequest.class, "OpenAccountRequest"),
            Map.entry(AccountResponse.class, "Account"),
            Map.entry(AccountStatusResponse.class, "AccountStatus"),
            Map.entry(MoneyOperationRequest.class, "MoneyOperationRequest"),
            Map.entry(MoneyOperationResponse.class, "MoneyOperationResponse"),
            Map.entry(TransferMoneyRequest.class, "TransferRequest"),
            Map.entry(TransferMoneyResponse.class, "TransferResponse"),
            Map.entry(AccountTransactionResponse.class, "Transaction"),
            Map.entry(AccountTransactionPageResponse.class, "TransactionPage"),
            Map.entry(ApiError.class, "ApiError"));

    @Autowired
    @Qualifier("requestMappingHandlerMapping")
    private RequestMappingHandlerMapping handlerMapping;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void documentsEveryVersionedApiOperationWithUniqueOperationIds()
            throws IOException {
        JsonNode contract = readContract();

        assertEquals("3.1.0", contract.path("openapi").textValue());
        assertEquals(applicationOperations(), contractOperations(contract));
    }

    @Test
    void keepsDocumentedSchemaFieldsAlignedWithRestDtos() throws IOException {
        JsonNode contract = readContract();

        DTO_SCHEMAS.forEach((dtoType, schemaName) ->
                assertEquals(
                        recordComponentNames(dtoType),
                        schemaPropertyNames(contract, schemaName),
                        () -> dtoType.getSimpleName()
                                + " differs from OpenAPI schema " + schemaName));
    }

    private JsonNode readContract() throws IOException {
        return objectMapper.readTree(Files.readString(CONTRACT));
    }

    private Set<String> applicationOperations() {
        Set<String> operations = new HashSet<>();

        handlerMapping.getHandlerMethods().forEach((mapping, handler) -> {
            if (!handler.getBeanType().getPackageName().startsWith("com.bankingsystem")) {
                return;
            }
            for (String path : mapping.getPatternValues()) {
                if (!path.startsWith("/api/v1")) {
                    continue;
                }
                for (RequestMethod method : methods(mapping)) {
                    operations.add(operation(
                            method.name().toLowerCase(Locale.ROOT),
                            path.substring("/api/v1".length())));
                }
            }
        });

        return operations;
    }

    private static Set<RequestMethod> methods(RequestMappingInfo mapping) {
        Set<RequestMethod> methods = mapping.getMethodsCondition().getMethods();
        assertTrue(!methods.isEmpty(), "API handlers must declare an HTTP method");
        return methods;
    }

    private static Set<String> contractOperations(JsonNode contract) {
        Set<String> operations = new HashSet<>();
        Set<String> operationIds = new HashSet<>();

        contract.path("paths").properties().forEach(pathEntry ->
                pathEntry.getValue().properties().forEach(methodEntry -> {
                    if (!HTTP_METHODS.contains(methodEntry.getKey())) {
                        return;
                    }
                    String operationId =
                            methodEntry.getValue().path("operationId").textValue();
                    assertTrue(
                            operationId != null && !operationId.isBlank(),
                            () -> "Missing operationId for "
                                    + methodEntry.getKey() + " " + pathEntry.getKey());
                    assertTrue(
                            operationIds.add(operationId),
                            () -> "Duplicate operationId: " + operationId);
                    operations.add(operation(methodEntry.getKey(), pathEntry.getKey()));
                }));

        return operations;
    }

    private static Set<String> recordComponentNames(Class<?> dtoType) {
        assertTrue(dtoType.isRecord(), () -> dtoType.getName() + " must be a record");
        return Arrays.stream(dtoType.getRecordComponents())
                .map(component -> component.getName())
                .collect(Collectors.toUnmodifiableSet());
    }

    private static Set<String> schemaPropertyNames(
            JsonNode contract,
            String schemaName) {
        JsonNode schema = contract.path("components").path("schemas").path(schemaName);
        assertTrue(!schema.isMissingNode(), () -> "Missing schema: " + schemaName);

        Set<String> properties = new HashSet<>();
        collectSchemaProperties(contract, schema, properties);
        return Set.copyOf(properties);
    }

    private static void collectSchemaProperties(
            JsonNode contract,
            JsonNode schema,
            Set<String> properties) {
        schema.path("properties").propertyNames().forEach(properties::add);
        schema.path("allOf").forEach(component -> {
            JsonNode reference = component.path("$ref");
            if (reference.isTextual()) {
                String schemaName = reference.textValue()
                        .substring(reference.textValue().lastIndexOf('/') + 1);
                collectSchemaProperties(
                        contract,
                        contract.path("components").path("schemas").path(schemaName),
                        properties);
            } else {
                collectSchemaProperties(contract, component, properties);
            }
        });
    }

    private static String operation(String method, String path) {
        return method.toUpperCase(Locale.ROOT) + " " + path;
    }
}
