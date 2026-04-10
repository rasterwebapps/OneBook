---
name: add-rest-endpoint
description: >-
  Scaffold a complete REST API endpoint for OneBook following the layered architecture:
  DTO records, Spring Data JPA repository, service with business logic, REST controller,
  and JUnit 5 unit tests.
---

# Add REST Endpoint

Scaffold a complete REST API endpoint following OneBook's layered architecture.

## When to Use

- Adding a new CRUD API for a domain entity
- Adding a new business operation endpoint
- Extending an existing controller with new operations

## Steps

### 1. Create DTO Records

Create request and response DTOs as Java records in the appropriate `dto/` package:

```java
// Request DTO — with validation annotations
public record Create{Entity}Request(
    @NotBlank String fieldName,
    @NotNull Long referenceId,
    @Positive BigDecimal amount,
    String optionalField
) {}

// Response DTO — never expose JPA entities
public record {Entity}Response(
    Long id,
    String fieldName,
    BigDecimal amount,
    Instant createdAt
) {}
```

**Rules:**
- ALWAYS use Java records for DTOs
- ALWAYS add `@NotBlank`, `@NotNull`, `@Positive` validation annotations
- ALWAYS use `BigDecimal` for monetary amounts
- NEVER expose JPA entities directly

### 2. Create Repository

Create a Spring Data JPA repository interface:

```java
public interface {Entity}Repository extends JpaRepository<{Entity}, Long> {
    List<{Entity}> findByTenantId(String tenantId);
    Optional<{Entity}> findByIdAndTenantId(Long id, String tenantId);
    // Add domain-specific queries
}
```

**Rules:**
- ALL queries MUST include `tenantId` parameter
- Use derived query methods where possible
- Use `@Query` with JPQL for complex queries

### 3. Create Service

Implement business logic in a `@Service` class:

```java
@Service
public class {Entity}Service {
    private final {Entity}Repository repository;

    public {Entity}Service({Entity}Repository repository) {
        this.repository = repository;  // Constructor injection, all fields final
    }

    public {Entity}Response create(String tenantId, Create{Entity}Request request) {
        // 1. Validate business rules
        // 2. Create entity
        // 3. Save to repository
        // 4. Return DTO (never entity)
    }
}
```

**Rules:**
- Constructor-based dependency injection (all fields `final`)
- For accounting: 3-level validation (service → trigger → exception)
- For amounts: `BigDecimal` with `RoundingMode.HALF_UP`
- Throw domain-specific exceptions

### 4. Create Controller

Create REST endpoints returning DTOs:

```java
@RestController
@RequestMapping("/api/{domain}")
public class {Entity}Controller {
    private final {Entity}Service service;

    public {Entity}Controller({Entity}Service service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<{Entity}Response> create(
            @RequestParam String tenantId,
            @Valid @RequestBody Create{Entity}Request request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(service.create(tenantId, request));
    }

    @GetMapping
    public ResponseEntity<List<{Entity}Response>> list(@RequestParam String tenantId) {
        return ResponseEntity.ok(service.findAll(tenantId));
    }
}
```

**HTTP Status Codes:**
- `200 OK` — GET/PUT success
- `201 Created` — POST success
- `204 No Content` — DELETE success
- `400 Bad Request` — Validation errors
- `404 Not Found` — Resource not found

### 5. Create Unit Tests

Create JUnit 5 tests for the service layer:

```java
@ExtendWith(MockitoExtension.class)
class {Entity}ServiceTest {
    @Mock private {Entity}Repository repository;
    @InjectMocks private {Entity}Service service;

    @Test
    void create_validRequest_returnsResponse() { /* ... */ }

    @Test
    void create_duplicateCode_throwsException() { /* ... */ }

    @Test
    void findAll_withTenantId_returnsFilteredList() { /* ... */ }
}
```

**Test naming:** `methodName_condition_expectedResult`

### 6. Verify

```bash
cd backend && ./gradlew compileJava   # Compilation check
cd backend && ./gradlew test          # All tests pass
```

## References

- Backend agent: `.github/agents/backend.agent.md`
- API docs: `docs/technical/api-documentation.md`
- API contracts: `docs/technical/api-contracts.md`
