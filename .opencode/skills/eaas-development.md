# EaaS Development Standards & Guidelines

## Critical Rules (MUST FOLLOW)

When generating or modifying code for the EaaS (Escrow as a Service) platform, you MUST adhere to these standards. No exceptions.

---

## 1. Query Optimization - Zero Tolerance for N+1

### Rules
- **ALWAYS** use `JOIN FETCH` or `@EntityGraph` for related data
- **ALWAYS** use `@BatchSize` for collections that need lazy loading
- **NEVER** loop through entities to fetch relationships
- **VERIFY** with query logs - check for multiple similar queries

### ❌ BAD - N+1 Problem
```java
// Generates N+1 queries!
List<Escrow> escrows = escrowRepository.findAll();
escrows.forEach(e -> {
    System.out.println(e.getCustomer().getFullName()); // Query per escrow
    e.getStateHistory().size(); // Another query per escrow
});
```

### ✅ GOOD - Single Query with JOIN FETCH
```java
@Query("""
    SELECT e FROM EscrowTransaction e 
    JOIN FETCH e.customer 
    LEFT JOIN FETCH e.merchant
    WHERE e.status = :status
    """)
List<EscrowTransaction> findByStatusWithDetails(@Param("status") EscrowStatus status);
```

### ✅ GOOD - EntityGraph Approach
```java
@EntityGraph(attributePaths = {"customer", "merchant", "stateHistory"})
Optional<EscrowTransaction> findWithDetailsByReference(String reference);
```

### ✅ GOOD - Batch Size for Collections
```java
@Entity
public class EscrowTransaction {
    @OneToMany(mappedBy = "escrow")
    @BatchSize(size = 20)  // Loads 20 collections in 1 query
    private List<EscrowStateHistory> stateHistory;
}
```

---

## 2. Performance - Sub-300ms Response Time

### Rules
- **Cache hot data** in Redis (escrow by reference, user profiles, fee configs)
- **Use async processing** for non-critical operations (notifications, audit logs)
- **Implement pagination** - never return unbounded lists
- **Database indexing** - verify indexes on frequently queried columns
- **Connection pooling** - configure HikariCP appropriately

### Caching Pattern
```java
@Service
@RequiredArgsConstructor
public class EscrowService {
    private final EscrowRepository escrowRepository;
    private final RedisTemplate<String, EscrowDTO> redisTemplate;
    
    @Cacheable(value = "escrow", key = "#reference", unless = "#result == null")
    public EscrowDTO getEscrow(String reference) {
        return escrowRepository.findByReference(reference)
            .map(this::mapToDTO)
            .orElseThrow(() -> new NotFoundException("Escrow not found"));
    }
    
    @CacheEvict(value = "escrow", key = "#reference")
    @Transactional
    public EscrowDTO updateEscrow(String reference, UpdateRequest request) {
        // Update logic
    }
}
```

### Async Processing Pattern
```java
@Service
@RequiredArgsConstructor
public class NotificationService {
    private final RabbitTemplate rabbitTemplate;
    
    @Async
    public CompletableFuture<Void> sendNotificationAsync(String eventType, Object data) {
        rabbitTemplate.convertAndSend("escrow.events", eventType, data);
        return CompletableFuture.completedFuture(null);
    }
}
```

### Pagination Pattern
```java
@GetMapping
public Page<EscrowResponse> listEscrows(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(required = false) EscrowStatus status) {
    
    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
    return escrowService.findByStatus(status, pageable);
}
```

---

## 3. Security - Defense in Depth

### Rules
- **Validate ALL inputs** with @Valid and custom validators
- **Encrypt PII** - BVN, bank accounts at rest (AES-256)
- **Never expose stack traces** in production error responses
- **Use proper authentication** - JWT or API Key on every endpoint
- **Authorization checks** - @PreAuthorize with role verification
- **Audit logging** - Immutable logs for every state change
- **SQL injection prevention** - Use parameterized queries only

### Input Validation
```java
@PostMapping
public ResponseEntity<ApiResponse<EscrowResponse>> createEscrow(
        @Valid @RequestBody CreateEscrowRequest request,
        @RequestHeader("X-Idempotency-Key") @NotBlank String idempotencyKey) {
    // Implementation
}

@Data
public class CreateEscrowRequest {
    @NotNull(message = "Merchant ID is required")
    private UUID merchantId;
    
    @NotBlank(message = "Product description is required")
    @Size(min = 10, max = 500, message = "Description must be 10-500 characters")
    private String productDescription;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1000.00", message = "Minimum amount is ₦1,000")
    private BigDecimal amount;
}
```

### PII Encryption
```java
@Entity
public class MerchantProfile {
    @Convert(converter = AesEncryptionConverter.class)
    private String bankAccountNumber;
    
    @Convert(converter = AesEncryptionConverter.class)
    private String bvn;
}

@Component
public class AesEncryptionConverter implements AttributeConverter<String, String> {
    @Value("${encryption.key}")
    private String encryptionKey;
    
    @Override
    public String convertToDatabaseColumn(String attribute) {
        // AES-256 encryption
    }
    
    @Override
    public String convertToEntityAttribute(String dbData) {
        // AES-256 decryption
    }
}
```

### Secure Error Handling
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, WebRequest request) {
        // Log full stack trace internally
        log.error("Unhandled exception", ex);
        
        // Return generic message to client
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponse("An unexpected error occurred", "INTERNAL_ERROR"));
    }
}
```

### Audit Logging
```java
@EventListener
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
public void onEscrowStateChanged(EscrowStateChangedEvent event) {
    AuditLog log = AuditLog.builder()
        .entityType("ESCROW")
        .entityId(event.getEscrowId())
        .action("STATUS_CHANGED")
        .fromStatus(event.getFromStatus())
        .toStatus(event.getToStatus())
        .performedBy(event.getUserId())
        .metadata(event.getMetadata())
        .createdAt(Instant.now())
        .build();
    
    auditLogRepository.save(log);
}
```

---

## 4. Error Handling - Consistent & Safe

### Rules
- **Use structured error responses** with consistent format
- **Appropriate HTTP status codes**:
  - 400 - Bad Request (validation errors)
  - 401 - Unauthorized (missing/invalid auth)
  - 403 - Forbidden (insufficient permissions)
  - 404 - Not Found (resource doesn't exist)
  - 409 - Conflict (business rule violation)
  - 429 - Too Many Requests (rate limiting)
  - 500 - Internal Server Error (unexpected failures)
- **Idempotency** - Handle duplicate requests gracefully

### Error Response Structure
```java
@Data
@Builder
public class ErrorResponse {
    private String status;           // "error"
    private String message;          // Human-readable message
    private String errorCode;        // Machine-readable code
    private List<FieldError> errors; // Field-level validation errors
    private Instant timestamp;
    private String path;
    
    @Data
    public static class FieldError {
        private String field;
        private String message;
        private Object rejectedValue;
    }
}
```

### Exception Handler Example
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex, 
            WebRequest request) {
        
        List<FieldError> fieldErrors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> new FieldError(
                error.getField(),
                error.getDefaultMessage(),
                error.getRejectedValue()
            ))
            .collect(Collectors.toList());
        
        ErrorResponse response = ErrorResponse.builder()
            .status("error")
            .message("Validation failed")
            .errorCode("VALIDATION_ERROR")
            .errors(fieldErrors)
            .timestamp(Instant.now())
            .path(request.getDescription(false))
            .build();
        
        return ResponseEntity.badRequest().body(response);
    }
    
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse.builder()
                .status("error")
                .message(ex.getMessage())
                .errorCode("NOT_FOUND")
                .timestamp(Instant.now())
                .build());
    }
}
```

---

## 5. Testing - Non-Negotiable

### Rules
- **Unit tests** for every service class (business logic)
- **Integration tests** for controllers with @SpringBootTest
- **Test coverage minimum 80%** for new code
- **Use TestContainers** for database-dependent tests
- **Mock external dependencies** (Interswitch, SendGrid)

### Unit Test Example
```java
@ExtendWith(MockitoExtension.class)
class FeeCalculationServiceTest {
    
    @InjectMocks
    private FeeCalculationService feeService;
    
    @Test
    void shouldCalculateFeeWithinRange() {
        // Given
        BigDecimal amount = new BigDecimal("100000");
        
        // When
        FeeBreakdown result = feeService.calculateFee(amount);
        
        // Then
        assertThat(result.getEscrowFee()).isEqualTo(new BigDecimal("1500.00"));
        assertThat(result.getMerchantAmount()).isEqualTo(new BigDecimal("98500.00"));
        assertThat(result.getTotalCharge()).isEqualTo(new BigDecimal("101500.00"));
    }
    
    @Test
    void shouldApplyMinimumFee() {
        // Given
        BigDecimal amount = new BigDecimal("10000"); // 1.5% = 150, but min is 500
        
        // When
        FeeBreakdown result = feeService.calculateFee(amount);
        
        // Then
        assertThat(result.getEscrowFee()).isEqualTo(new BigDecimal("500.00"));
    }
}
```

### Integration Test Example
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class EscrowControllerIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
        .withDatabaseName("escrow_test")
        .withUsername("test")
        .withPassword("test");
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    void shouldCreateEscrow() {
        // Given
        CreateEscrowRequest request = new CreateEscrowRequest();
        request.setMerchantId(UUID.randomUUID());
        request.setAmount(new BigDecimal("50000"));
        request.setProductDescription("Test product description");
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + getTestToken());
        headers.set("X-Idempotency-Key", UUID.randomUUID().toString());
        
        // When
        ResponseEntity<ApiResponse<EscrowResponse>> response = restTemplate.exchange(
            "/api/v1/escrow",
            HttpMethod.POST,
            new HttpEntity<>(request, headers),
            new ParameterizedTypeReference<>() {}
        );
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getData().getReference()).startsWith("TXN-");
    }
}
```

---

## 6. Code Style - Spring Boot Best Practices

### Rules
- **Constructor injection only** - never field injection
- **Package structure**: `com.uko.eaas.{service}/controller/service/repository/model/config`
- **Use Lombok wisely** - @Getter, @Builder, @RequiredArgsConstructor (avoid @Data on entities)
- **Separate DTOs** - RequestDTO, ResponseDTO, never expose entities
- **Immutable entities** - no setters, use builder pattern
- **Naming conventions**:
  - Classes: PascalCase (EscrowService)
  - Methods/fields: camelCase (createEscrow)
  - Constants: UPPER_SNAKE_CASE (MAX_RETRY_ATTEMPTS)

### Proper Dependency Injection
```java
@Service
@RequiredArgsConstructor  // Generates constructor with all final fields
public class EscrowService {
    private final EscrowRepository escrowRepository;
    private final FeeCalculationService feeService;
    private final RabbitTemplate rabbitTemplate;
    private final RedisTemplate<String, Object> redisTemplate;
    
    // No @Autowired needed - constructor injection handles it
}
```

### DTO Pattern
```java
// Request DTO - for input validation
@Data
public class CreateEscrowRequest {
    @NotNull
    private UUID merchantId;
    
    @NotBlank
    @Size(min = 10)
    private String productDescription;
    
    @NotNull
    @DecimalMin("1000")
    private BigDecimal amount;
}

// Response DTO - for API output
@Data
@Builder
public class EscrowResponse {
    private String reference;
    private BigDecimal amount;
    private BigDecimal escrowFee;
    private BigDecimal merchantAmount;
    private EscrowStatus status;
    private Instant createdAt;
}

// Entity - never exposed directly
@Entity
@Table(name = "escrow_transactions")
@Getter  // Only getter, no setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class EscrowTransaction {
    @Id
    private UUID id;
    
    private String reference;
    private BigDecimal amount;
    
    // Business methods instead of setters
    public void markAsFunded() {
        this.status = EscrowStatus.FUNDED;
        this.fundedAt = Instant.now();
    }
}
```

---

## 7. Database Best Practices

### Rules
- **Use transactions** - @Transactional on service methods
- **Optimistic locking** - @Version for concurrent updates
- **Soft deletes only** - never DELETE, use `is_active` flag
- **Decimal for money** - BigDecimal (DECIMAL(15,2))
- **Foreign keys** - enforce referential integrity
- **Indexing** - index foreign keys and frequently queried columns

### Transaction Management
```java
@Service
@RequiredArgsConstructor
public class EscrowService {
    
    @Transactional
    public EscrowResponse createEscrow(CreateEscrowRequest request) {
        // All database operations in this method are atomic
        EscrowTransaction escrow = escrowRepository.save(buildEscrow(request));
        escrowStateHistoryRepository.save(buildHistory(escrow));
        auditLogService.logEscrowCreated(escrow);
        
        return mapToResponse(escrow);
    }
    
    @Transactional(readOnly = true)  // Optimization for read-only
    public EscrowResponse getEscrow(String reference) {
        return escrowRepository.findByReference(reference)
            .map(this::mapToResponse)
            .orElseThrow(() -> new NotFoundException("Escrow not found"));
    }
}
```

### Optimistic Locking
```java
@Entity
public class EscrowTransaction {
    @Version
    private Long version;
    
    // Other fields...
}

// Spring will throw OptimisticLockingFailureException 
// if concurrent modification detected
```

### Soft Delete Pattern
```java
@Entity
public class MerchantProfile {
    private boolean isActive = true;
    
    public void deactivate() {
        this.isActive = false;
        this.deactivatedAt = Instant.now();
    }
}

// Repository method
@Query("SELECT m FROM MerchantProfile m WHERE m.isActive = true")
List<MerchantProfile> findAllActive();
```

---

## 8. Microservice Patterns

### Rules
- **Idempotency keys** - required for all POST/PUT operations
- **Circuit breakers** - use Resilience4j for external calls
- **Exponential backoff** - for retries
- **Timeouts** - set on all external calls (5s default)
- **Event-driven** - use RabbitMQ for async operations
- **Database per service** - no shared databases
- **Health checks** - /actuator/health on every service

### Idempotency Pattern
```java
@Service
@RequiredArgsConstructor
public class EscrowService {
    private final IdempotencyKeyRepository idempotencyRepository;
    private final EscrowRepository escrowRepository;
    
    @Transactional
    public EscrowResponse createEscrow(CreateEscrowRequest request, String idempotencyKey) {
        // Check if already processed
        Optional<IdempotencyRecord> existing = idempotencyRepository.findByKey(idempotencyKey);
        if (existing.isPresent()) {
            log.info("Returning cached response for idempotency key: {}", idempotencyKey);
            return existing.get().getResponse();
        }
        
        // Process request
        EscrowTransaction escrow = escrowRepository.save(buildEscrow(request));
        EscrowResponse response = mapToResponse(escrow);
        
        // Cache response
        idempotencyRepository.save(new IdempotencyRecord(idempotencyKey, response));
        
        return response;
    }
}
```

### Circuit Breaker Pattern
```java
@Service
@RequiredArgsConstructor
public class PaymentService {
    private final InterswitchClient interswitchClient;
    
    @CircuitBreaker(name = "interswitch", fallbackMethod = "processPaymentFallback")
    @Retry(name = "interswitch")
    @TimeLimiter(name = "interswitch")
    public PaymentResponse processPayment(PaymentRequest request) {
        return interswitchClient.charge(request);
    }
    
    public PaymentResponse processPaymentFallback(PaymentRequest request, Exception ex) {
        log.error("Interswitch payment failed, using fallback", ex);
        // Queue for retry or return error
        throw new PaymentProcessingException("Payment service temporarily unavailable");
    }
}
```

### Event Publishing Pattern
```java
@Service
@RequiredArgsConstructor
public class EscrowStateMachine {
    private final RabbitTemplate rabbitTemplate;
    
    public void transitionToFunded(EscrowTransaction escrow) {
        escrow.markAsFunded();
        escrowRepository.save(escrow);
        
        // Publish event for other services
        EscrowFundedEvent event = EscrowFundedEvent.builder()
            .escrowId(escrow.getId())
            .reference(escrow.getReference())
            .amount(escrow.getAmount())
            .merchantId(escrow.getMerchantId())
            .fundedAt(Instant.now())
            .build();
        
        rabbitTemplate.convertAndSend("escrow.events", "escrow.funded", event);
    }
}
```

---

## Quick Reference: Common Violations

### ❌ NEVER DO THIS

```java
// 1. N+1 Query
List<Escrow> escrows = escrowRepo.findAll();
escrows.forEach(e -> e.getCustomer().getEmail()); // N queries!

// 2. Exposing Entity
@GetMapping("/{id}")
public EscrowTransaction getEscrow(@PathVariable UUID id) { // Never return entity!
    return escrowRepo.findById(id).orElseThrow();
}

// 3. No Input Validation
@PostMapping
public void create(String email, BigDecimal amount) { // No validation!

// 4. Synchronous External Call
String result = restTemplate.getForObject(url, String.class); // Blocks thread!

// 5. Hardcoded Secrets
String apiKey = "sk_live_123456"; // NEVER!

// 6. No Transaction
public void transferFunds(Account from, Account to, BigDecimal amount) {
    from.debit(amount);  // If this succeeds...
    to.credit(amount);   // ...but this fails, money is lost!
}

// 7. Field Injection
@Autowired  // BAD!
private EscrowRepository escrowRepository;

// 8. @Data on Entity
@Data  // Generates setters - allows external modification!
@Entity
public class EscrowTransaction { }
```

---

## Pre-Submission Checklist

Before submitting ANY code, verify:

- [ ] **No N+1 queries** - checked with Hibernate `show-sql=true`
- [ ] **Response time < 300ms** - tested with `@Timed` or manual timing
- [ ] **All inputs validated** - `@Valid` or manual validation present
- [ ] **PII encrypted** - BVN, bank accounts use `@Convert` encryption
- [ ] **Error handling** - covers all exception paths with proper HTTP codes
- [ ] **Unit tests** - written for all new service methods
- [ ] **No hardcoded secrets** - use `@Value` or environment variables
- [ ] **Database migration** - Flyway script included if schema changed
- [ ] **API documentation** - OpenAPI annotations added to controllers
- [ ] **Audit logging** - business events logged for compliance
- [ ] **Circuit breaker** - added for external service calls
- [ ] **Idempotency** - key handling for mutating operations

---

## Technology Stack (Strict Adherence)

### MUST Use
- Java 21 (LTS)
- Spring Boot 3.2+
- PostgreSQL 15+
- Redis 7+
- RabbitMQ 3.12+
- Maven 3.9+
- Docker & Docker Compose

### MUST NOT Use
- Lombok `@Data` on entities (use `@Getter` + `@Builder`)
- Field injection (`@Autowired` on fields)
- Raw SQL (use JPA/JPQL only)
- Synchronous calls between services (use events)
- Shared databases between services
- `double` or `float` for money (use `BigDecimal`)
- `java.util.Date` (use `java.time.Instant` or `LocalDateTime`)

---

**REMINDER: These rules are enforced on EVERY code generation. Quality is non-negotiable.**
