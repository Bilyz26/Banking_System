package com.bankingsystem.architecture;

import com.bankingsystem.bootstrap.BankingSystemApplication;
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
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

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

    @Autowired
    @Qualifier("requestMappingHandlerMapping")
    private RequestMappingHandlerMapping handlerMapping;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void documentsEveryVersionedApiOperationWithUniqueOperationIds()
            throws IOException {
        JsonNode contract = objectMapper.readTree(Files.readString(CONTRACT));

        assertEquals("3.1.0", contract.path("openapi").textValue());
        assertEquals(applicationOperations(), contractOperations(contract));
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

    private static String operation(String method, String path) {
        return method.toUpperCase(Locale.ROOT) + " " + path;
    }
}
