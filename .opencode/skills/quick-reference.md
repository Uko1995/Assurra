# EaaS Quick Reference Card

## 🚀 Common Patterns at a Glance

### 1. Controller Endpoint Template
```java
@RestController
@RequestMapping("/api/v1/escrow")
@RequiredArgsConstructor
@Validated
public class EscrowController {
    
    private final EscrowService escrowService;
    
    @PostMapping
    public ResponseEntity<ApiResponse<EscrowResponse>> create(
            @Valid @RequestBody CreateEscrowRequest request,
            @RequestHeader("X-Idempotency-Key") @NotBlank String idempotencyKey,
            @RequestHeader("X-User-Id") String userId) {
        
        EscrowResponse response = escrowService.create(request, idempotencyKey, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Created", response));
    }
    
    @GetMapping("/{reference}")
    @Cacheable(value = "escrow", key = "#reference")
    public ResponseEntity<ApiResponse<EscrowResponse>> get(@PathVariable String reference) {
        return ResponseEntity.ok(ApiResponse.success(escrowService.get(reference)));
    }
}
```

### 2. Service Implementation Template
```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class EscrowService {
    
    private final EscrowRepository escrowRepository;
    private final RabbitTemplate rabbitTemplate;
    
    @Transactional
    @CacheEvict(value = "escrow", key = "#result.reference")
    public EscrowResponse create(CreateEscrowRequest request, String idempotencyKey, String userId) {
        // 1. Check idempotency
        // 2. Validate business rules
        // 3. Save entity
        // 4. Publish event
        // 5. Return DTO
    }
    
    @CircuitBreaker(name = "database")
    public EscrowResponse get(String reference) {
        return escrowRepository.findByReference(reference)
            .map(this::toDTO)
            .orElseThrow(() -> new NotFoundException("Escrow not found: " + reference));
    }
}
```

### 3. Repository with JOIN FETCH
```java
@Repository
public interface EscrowRepository extends JpaRepository<EscrowTransaction, UUID> {
    
    // ❌ Avoid - causes N+1
    Optional<EscrowTransaction> findByReference(String reference);
    
    // ✅ Good - single query with all data
    @EntityGraph(attributePaths = {"customer", "merchant", "stateHistory"})
    Optional<EscrowTransaction> findWithDetailsByReference(String reference);
    
    // ✅ Good - JPQL with JOIN FETCH
    @Query("""
        SELECT e FROM EscrowTransaction e 
        JOIN FETCH e.customer 
        LEFT JOIN FETCH e.merchant 
        WHERE e.status = :status
        """)
    List<EscrowTransaction> findByStatusWithDetails(@Param("status") EscrowStatus status);
    
    // ✅ Good - pagination with count query
    @Query(value = "SELECT e FROM EscrowTransaction e WHERE e.merchantId = :merchantId",
           countQuery = "SELECT COUNT(e) FROM EscrowTransaction e WHERE e.merchantId = :merchantId")
    Page<EscrowTransaction> findByMerchantId(@Param("merchantId") UUID merchantId, Pageable pageable);
}
```

### 4. Entity with Encryption & Optimistic Locking
```java
@Entity
@Table(name = "merchant_profiles")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MerchantProfile {
    
    @Id
    @UuidGenerator
    private UUID id;
    
    @Column(nullable = false)
    private String businessName;
    
    @Convert(converter = AesEncryptionConverter.class)
    private String bankAccountNumber;
    
    @Convert(converter = AesEncryptionConverter.class)
    private String bvn;
    
    @Version
    private Long version;
    
    private boolean isActive = true;
    
    // Business method instead of setter
    public void deactivate() {
        this.isActive = false;
    }
}
```

### 5. Exception Handler Template
```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex, WebRequest request) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), "NOT_FOUND", request);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, WebRequest request) {
        List<FieldError> errors = ex.getBindingResult().getFieldErrors().stream()
            .map(e -> new FieldError(e.getField(), e.getDefaultMessage(), e.getRejectedValue()))
            .collect(Collectors.toList());
        
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Validation failed", "VALIDATION_ERROR", request, errors);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, WebRequest request) {
        log.error("Unhandled exception", ex);  // Log full details
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 
            "An unexpected error occurred", "INTERNAL_ERROR", request);
    }
    
    private ResponseEntity<ErrorResponse> buildErrorResponse(
            HttpStatus status, String message, String code, WebRequest request, List<FieldError> errors) {
        ErrorResponse response = ErrorResponse.builder()
            .status("error")
            .message(message)
            .errorCode(code)
            .errors(errors)
            .timestamp(Instant.now())
            .path(request.getDescription(false))
            .build();
        return ResponseEntity.status(status).body(response);
    }
}
```

### 6. Test Template
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class EscrowControllerTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");
    
    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
    }
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    void shouldCreateEscrow() {
        CreateEscrowRequest request = new CreateEscrowRequest();
        request.setAmount(new BigDecimal("50000"));
        // ...
        
        ResponseEntity<ApiResponse<EscrowResponse>> response = restTemplate.exchange(
            "/api/v1/escrow",
            HttpMethod.POST,
            new HttpEntity<>(request, headers()),
            new ParameterizedTypeReference<>() {}
        );
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }
}
```

---

## ⚠️ Anti-Patterns Checklist

| Anti-Pattern | Issue | Solution |
|--------------|-------|----------|
| `escrowRepo.findAll()` + loop | N+1 queries | Use `@EntityGraph` or `JOIN FETCH` |
| `@Autowired` field | Hard to test, hidden dependencies | Constructor injection |
| `@Data` on entity | Mutable state, no business logic | `@Getter` + `@Builder` |
| Returning entity from controller | Exposes internal structure | Use DTOs |
| Raw SQL queries | SQL injection risk | Use JPA/JPQL |
| `double` for money | Precision loss | `BigDecimal` |
| `new Date()` | Deprecated, timezone issues | `Instant.now()` |
| Synchronous REST calls | Blocks threads | Use `@Async` or events |
| Catching `Exception` broadly | Hides bugs | Catch specific exceptions |
| No transaction | Inconsistent data | `@Transactional` |

---

## 🔍 Debugging Tips

### Check for N+1 Queries
```yaml
# application.yml
spring:
  jpa:
    show-sql: true
    properties:
      hibernate:
        format_sql: true
```
Look for: Multiple similar queries in logs (one per entity in collection)

### Enable Request Timing
```java
@Component
public class TimingInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, Object handler) {
        request.setAttribute("startTime", System.currentTimeMillis());
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        long duration = System.currentTimeMillis() - (Long) request.getAttribute("startTime");
        log.info("{} {} took {}ms", request.getMethod(), request.getRequestURI(), duration);
    }
}
```

### Redis Cache Stats
```java
@EventListener
public void logCacheStats(ApplicationReadyEvent event) {
    Cache escrowCache = cacheManager.getCache("escrow");
    // Check hit/miss rates in Redis CLI: INFO stats
}
```

---

## 📊 Performance Targets

| Metric | Target | Alert Threshold |
|--------|--------|-----------------|
| API Response Time (p95) | < 300ms | > 500ms |
| Database Query Time | < 50ms | > 100ms |
| Cache Hit Rate | > 80% | < 70% |
| Test Coverage | > 80% | < 75% |
| Error Rate | < 1% | > 5% |

---

## 🔗 Key Dependencies

```xml
<!-- Essential dependencies for every service -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-amqp</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-circuitbreaker-resilience4j</artifactId>
</dependency>
```

---

**Keep this reference handy while coding!** 🚀
