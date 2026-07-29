package com.bankingsystem.architecture;

import com.bankingsystem.account.presentation.AccountResponse;
import com.bankingsystem.account.presentation.AccountStatusResponse;
import com.bankingsystem.account.presentation.MoneyOperationRequest;
import com.bankingsystem.account.presentation.MoneyOperationResponse;
import com.bankingsystem.account.presentation.OpenAccountRequest;
import com.bankingsystem.bootstrap.BankingSystemApplication;
import com.bankingsystem.customer.presentation.CreateCustomerRequest;
import com.bankingsystem.customer.presentation.CreateCustomerResponse;
import com.bankingsystem.customer.presentation.CustomerResponse;
import com.bankingsystem.customer.presentation.UpdateCustomerProfileRequest;
import com.bankingsystem.ledger.presentation.AccountTransactionPageResponse;
import com.bankingsystem.ledger.presentation.AccountTransactionResponse;
import com.bankingsystem.shared.presentation.ApiError;
import com.bankingsystem.transfer.presentation.TransferMoneyRequest;
import com.bankingsystem.transfer.presentation.TransferMoneyResponse;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
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
            Map.entry(CustomerResponse.class, "Customer"),
            Map.entry(UpdateCustomerProfileRequest.class, "UpdateCustomerProfileRequest"),
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
    private static final Map<Class<?>, String> REQUEST_SCHEMAS = Map.ofEntries(
            Map.entry(CreateCustomerRequest.class, "CreateCustomerRequest"),
            Map.entry(UpdateCustomerProfileRequest.class, "UpdateCustomerProfileRequest"),
            Map.entry(OpenAccountRequest.class, "OpenAccountRequest"),
            Map.entry(MoneyOperationRequest.class, "MoneyOperationRequest"),
            Map.entry(TransferMoneyRequest.class, "TransferRequest"));

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

    @Test
    void keepsDocumentedTypesFormatsAndEnumsAlignedWithRestDtos()
            throws IOException {
        JsonNode contract = readContract();

        DTO_SCHEMAS.forEach((dtoType, schemaName) -> {
            Map<String, JsonNode> schemaProperties =
                    schemaProperties(contract, namedSchema(contract, schemaName));
            for (RecordComponent component : dtoType.getRecordComponents()) {
                assertCompatibleType(
                        contract,
                        component.getGenericType(),
                        schemaProperties.get(component.getName()),
                        dtoType.getSimpleName() + "." + component.getName());
            }
        });
    }

    @Test
    void keepsDocumentedRequestConstraintsAlignedWithJakartaValidation()
            throws IOException {
        JsonNode contract = readContract();

        REQUEST_SCHEMAS.forEach((requestType, schemaName) -> {
            JsonNode schema = namedSchema(contract, schemaName);
            assertEquals(
                    requiredComponents(requestType),
                    schemaRequiredFields(contract, schema),
                    requestType.getSimpleName() + " required fields");

            Map<String, JsonNode> properties = schemaProperties(contract, schema);
            for (RecordComponent component : requestType.getRecordComponents()) {
                assertDocumentedConstraints(
                        contract,
                        component,
                        properties.get(component.getName()),
                        requestType.getSimpleName() + "." + component.getName());
            }
        });
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

    private static Set<String> requiredComponents(Class<?> requestType) {
        return Arrays.stream(requestType.getRecordComponents())
                .filter(component ->
                        component.getAccessor().isAnnotationPresent(NotNull.class)
                                || component.getAccessor().isAnnotationPresent(NotBlank.class))
                .map(RecordComponent::getName)
                .collect(Collectors.toUnmodifiableSet());
    }

    private static Set<String> schemaPropertyNames(
            JsonNode contract,
            String schemaName) {
        return schemaProperties(contract, namedSchema(contract, schemaName)).keySet();
    }

    private static JsonNode namedSchema(JsonNode contract, String schemaName) {
        JsonNode schema = contract.path("components").path("schemas").path(schemaName);
        assertTrue(!schema.isMissingNode(), () -> "Missing schema: " + schemaName);
        return schema;
    }

    private static Map<String, JsonNode> schemaProperties(
            JsonNode contract,
            JsonNode schema) {
        Map<String, JsonNode> properties = new LinkedHashMap<>();
        collectSchemaProperties(contract, schema, properties);
        return Map.copyOf(properties);
    }

    private static void collectSchemaProperties(
            JsonNode contract,
            JsonNode schema,
            Map<String, JsonNode> properties) {
        schema.path("properties").properties().forEach(property -> {
            JsonNode previous = properties.put(property.getKey(), property.getValue());
            assertTrue(previous == null, () -> "Duplicate schema property: " + property.getKey());
        });
        schema.path("allOf").forEach(component -> {
            collectSchemaProperties(contract, resolveSchema(contract, component), properties);
        });
    }

    private static JsonNode resolveSchema(JsonNode contract, JsonNode schema) {
        JsonNode reference = schema.path("$ref");
        if (!reference.isTextual()) {
            return schema;
        }
        String schemaName =
                reference.textValue().substring(reference.textValue().lastIndexOf('/') + 1);
        return resolveSchema(contract, namedSchema(contract, schemaName));
    }

    private static Set<String> schemaRequiredFields(
            JsonNode contract,
            JsonNode schema) {
        Set<String> required = new HashSet<>();
        collectRequiredFields(contract, schema, required);
        return Set.copyOf(required);
    }

    private static void collectRequiredFields(
            JsonNode contract,
            JsonNode schema,
            Set<String> required) {
        schema.path("required").forEach(field -> required.add(field.textValue()));
        schema.path("allOf").forEach(component ->
                collectRequiredFields(contract, resolveSchema(contract, component), required));
    }

    private static void assertDocumentedConstraints(
            JsonNode contract,
            RecordComponent component,
            JsonNode documentedSchema,
            String fieldName) {
        JsonNode schema = resolveSchema(contract, documentedSchema);

        if (component.getAccessor().isAnnotationPresent(NotBlank.class)) {
            assertEquals(1, schema.path("minLength").intValue(), fieldName + " minLength");
        }

        Size size = component.getAccessor().getAnnotation(Size.class);
        if (size != null) {
            if (size.min() > 0) {
                assertEquals(size.min(), schema.path("minLength").intValue(), fieldName);
            }
            if (size.max() < Integer.MAX_VALUE) {
                assertEquals(size.max(), schema.path("maxLength").intValue(), fieldName);
            }
        }

        Pattern pattern = component.getAccessor().getAnnotation(Pattern.class);
        if (pattern != null) {
            assertEquals(
                    pattern.regexp(),
                    withoutAnchors(schema.path("pattern").textValue()),
                    fieldName + " pattern");
        }

        if (component.getAccessor().isAnnotationPresent(Email.class)) {
            assertEquals("email", schema.path("format").textValue(), fieldName);
        }

        DecimalMin decimalMin = component.getAccessor().getAnnotation(DecimalMin.class);
        if (decimalMin != null) {
            String keyword = decimalMin.inclusive() ? "minimum" : "exclusiveMinimum";
            assertDecimalEquals(
                    new BigDecimal(decimalMin.value()),
                    schema.path(keyword).decimalValue(),
                    fieldName + " " + keyword);
        }

        Digits digits = component.getAccessor().getAnnotation(Digits.class);
        if (digits != null) {
            assertDecimalEquals(
                    BigDecimal.ONE.movePointLeft(digits.fraction()),
                    schema.path("multipleOf").decimalValue(),
                    fieldName + " multipleOf");
            assertDecimalEquals(
                    BigDecimal.TEN.pow(digits.integer()),
                    schema.path("exclusiveMaximum").decimalValue(),
                    fieldName + " exclusiveMaximum");
        }
    }

    private static void assertDecimalEquals(
            BigDecimal expected,
            BigDecimal actual,
            String message) {
        assertTrue(
                expected.compareTo(actual) == 0,
                () -> message + " expected " + expected + " but was " + actual);
    }

    private static String withoutAnchors(String pattern) {
        String normalized = pattern;
        if (normalized.startsWith("^")) {
            normalized = normalized.substring(1);
        }
        if (normalized.endsWith("$")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    private static void assertCompatibleType(
            JsonNode contract,
            Type javaType,
            JsonNode documentedSchema,
            String fieldName) {
        assertTrue(documentedSchema != null, () -> "Missing schema property for " + fieldName);
        JsonNode schema = resolveSchema(contract, documentedSchema);
        Class<?> rawType = rawType(javaType);
        String expectedType = expectedOpenApiType(rawType);

        assertTrue(
                documentedTypes(schema).contains(expectedType),
                () -> fieldName + " expects OpenAPI type " + expectedType
                        + " but documents " + documentedTypes(schema));

        String expectedFormat = expectedFormat(rawType);
        if (expectedFormat != null) {
            assertEquals(expectedFormat, schema.path("format").textValue(), fieldName);
        }

        if (rawType.isEnum()) {
            Set<String> javaValues = Arrays.stream(rawType.getEnumConstants())
                    .map(Object::toString)
                    .collect(Collectors.toUnmodifiableSet());
            Set<String> documentedValues = new HashSet<>();
            schema.path("enum").forEach(value -> documentedValues.add(value.textValue()));
            assertEquals(javaValues, documentedValues, fieldName);
        }

        if (List.class.isAssignableFrom(rawType)) {
            Type itemType = ((ParameterizedType) javaType).getActualTypeArguments()[0];
            assertCompatibleType(contract, itemType, schema.path("items"), fieldName + "[]");
        }
    }

    private static Class<?> rawType(Type javaType) {
        if (javaType instanceof Class<?> type) {
            return type;
        }
        if (javaType instanceof ParameterizedType parameterizedType
                && parameterizedType.getRawType() instanceof Class<?> type) {
            return type;
        }
        throw new IllegalArgumentException("Unsupported REST field type: " + javaType);
    }

    private static String expectedOpenApiType(Class<?> javaType) {
        if (javaType == String.class || javaType == UUID.class
                || javaType == Instant.class || javaType.isEnum()) {
            return "string";
        }
        if (javaType == BigDecimal.class) {
            return "number";
        }
        if (javaType == int.class || javaType == Integer.class) {
            return "integer";
        }
        if (List.class.isAssignableFrom(javaType)) {
            return "array";
        }
        if (Map.class.isAssignableFrom(javaType) || javaType.isRecord()) {
            return "object";
        }
        throw new IllegalArgumentException("Unsupported REST field type: " + javaType.getName());
    }

    private static String expectedFormat(Class<?> javaType) {
        if (javaType == UUID.class) {
            return "uuid";
        }
        if (javaType == Instant.class) {
            return "date-time";
        }
        return null;
    }

    private static Set<String> documentedTypes(JsonNode schema) {
        JsonNode type = schema.path("type");
        if (type.isTextual()) {
            return Set.of(type.textValue());
        }
        Set<String> types = new HashSet<>();
        type.forEach(value -> types.add(value.textValue()));
        return Set.copyOf(types);
    }

    private static String operation(String method, String path) {
        return method.toUpperCase(Locale.ROOT) + " " + path;
    }
}
