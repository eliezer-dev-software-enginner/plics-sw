# AGENTS.md - Development Guidelines for Java Desktop ERP

This document provides comprehensive guidelines for AI agents working on this Java Desktop ERP application.

## Build and Development Commands

### Essential Commands
```bash
# Build and test
./gradlew build              # Full build with compilation and tests
./gradlew test               # Run all tests
./gradlew test --tests "TestClass"    # Run specific test class
./gradlew test --tests "*methodName"  # Run specific test method
./gradlew jar                # Build JAR without tests
./gradlew clean              # Clean build artifacts

# Development and distribution
./gradlew run                # Run application in development mode
./gradlew createInstaller    # Generate Linux .deb installer
./gradlew copyDependencies   # Copy runtime dependencies to build/libs
```

### Testing Strategy
- **Framework**: JUnit 5 with Mockito 5.10.0
- **Database**: In-memory SQLite (`jdbc:sqlite::memory:`) for tests
- **Pattern**: Given-When-Then structure with factory test data
- **Setup**: Use `@BeforeEach` for database reset between tests

Example test structure:
```java
@Test
void salvar() throws SQLException {
    // Given
    ProdutoModel p = produtoFake();
    
    // When
    repo.salvar(p);
    
    // Then
    ProdutoModel encontrado = repo.buscarPorCodigoBarras(p.codigoBarras);
    assertNotNull(encontrado);
}
```

## Technology Stack

### Core Technologies
- **Java**: Java 25 (latest)
- **Build**: Gradle 9.2.0 with Kotlin DSL
- **UI**: JavaFX 17 with custom Megalodonte component framework
- **Database**: SQLite with file `erp.db`
- **Architecture**: MVVM with reactive state management

### Key Dependencies
- Megalodonte ecosystem (`megalodonte:*`) for UI components and reactivity
- Ikonli for icons (AntDesign, Entypo packs)
- SQLite JDBC driver for persistence

## Code Style Guidelines

### Naming Conventions
- **Classes**: PascalCase (`ProdutoScreen`, `ProdutoScreenViewModel`)
- **Methods**: camelCase (`salvar()`, `buscarPorCodigoBarras()`)
- **Variables**: camelCase (`codigoBarras`, `precoCompra`)
- **Packages**: lowercase with underscores (`my_app.screens.produtoScreen`)
- **Constants**: UPPER_SNAKE_CASE for static final fields

### Import Organization
```java
// 1. Megalodonte framework (wildcard allowed)
import megalodonte.*;

// 2. JavaFX and Java standard library
import javafx.scene.layout.VBox;
import java.sql.SQLException;
import java.util.List;

// 3. Third-party libraries
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
```

### Code Structure Patterns

#### MVVM Architecture
- **Models**: Entity classes in `my_app.db.models` (`ProdutoModel`, `CategoriaModel`)
- **Views**: Screen classes in `my_app.screens.*` extending `Screen`
- **ViewModels**: State management in `my_app.lifecycle.*` with reactive `State` objects

#### Repository Pattern
- Classes in `my_app.db.repositories` (`ProdutoRepository`, `CategoriaRepository`)
- Methods: `salvar()`, `atualizar()`, `buscarPorId()`, `listarTodos()`, `deletar()`
- Handle `SQLException` appropriately, propagate to service layer

#### Service Layer
- Business logic in `my_app.services` (`ProdutoService`)
- Validation using `IllegalStateException` for business rule violations
- Orchestrate between repositories and handle transactions

### Functional Interfaces
Use functional interfaces for actions and callbacks:
```java
@FunctionalInterface
interface Action {
    void run() throws Exception;
}
```

### Error Handling Patterns

#### Repository Layer
```java
public ProdutoModel salvar(ProdutoModel produto) throws SQLException {
    // Database operations
    return produto;
}
```

#### Service Layer
```java
public void salvar(ProdutoModel produto) throws SQLException {
    if (repo.buscarPorCodigoBarras(produto.codigoBarras) != null) {
        throw new IllegalStateException("Produto já existe");
    }
    repo.salvar(produto);
}
```

#### UI Layer
```java
private void executar(Action action) {
    try {
        action.run();
        IO.println("Operação realizada com sucesso");
    } catch (Exception e) {
        IO.println(e.getMessage());
    }
}
```

## Project Structure

```
my_app/
├── core/                   # Themes and core functionality
├── db/                     # Database layer
│   ├── models/            # Entity models (ProdutoModel, CategoriaModel)
│   └── repositories/      # Data access layer
├── hotreload/             # Development hot reload system
├── lifecycle/             # ViewModels and state management
├── routes/                # Routing configuration
├── screens/               # UI screens
│   ├── authScreen/
│   ├── HomeScreen/
│   ├── produtoScreen/
│   └── components/        # Reusable UI components
└── services/              # Business logic layer
```

## UI Development Guidelines

### Screen Components
- Extend `Screen` base class
- Use builder pattern for UI construction
- Follow declarative approach with Megalodonte components
- Implement proper event handling with lambda expressions

### State Management
- Use `State<T>` objects in ViewModels for reactive updates
- Subscribe to state changes in views for UI updates
- Keep state immutable and create new instances for updates

### Routing
- Define routes in `AppRoutes` class
- Use `RouteParamsAware` interface for parameter injection
- Navigate via `Router.navigate()` with dynamic parameters

## Database Development

### Connection
- Use singleton `DB` class for connection management
- Database file: `erp.db` in project root
- Schema initialization via `DBInitializer`

### Model Requirements
- Implement proper constructors
- Use appropriate data types (`String`, `BigDecimal`, `Integer`)
- Include validation annotations if needed
- Override `toString()` for debugging

## Development Best Practices

### Hot Reload
- Set `devMode = true` in `Main` for development
- Changes to screens and components auto-reload
- Restart required for model/repository changes

### Testing Requirements
- Always write tests for repository methods
- Use in-memory database for test isolation
- Create factory methods for test data (`produtoFake()`)
- Test both success and failure scenarios

### Code Organization
- Keep methods focused and single-purpose
- Use proper access modifiers (private, protected, public)
- Document complex business logic
- Follow Java conventions for method organization (public, then private)

### Security Considerations
- Never hardcode credentials in code
- Use parameterized queries for SQL operations
- Validate input data in service layer
- Handle sensitive data appropriately

## Common Patterns to Follow

### Data Validation
```java
public void validar(ProdutoModel produto) {
    if (produto.codigoBarras == null || produto.codigoBarras.trim().isEmpty()) {
        throw new IllegalStateException("Código de barras é obrigatório");
    }
}
```

### Dependency Injection (Manual)
```java
public class ProdutoScreenViewModel {
    private final ProdutoService service;
    
    public ProdutoScreenViewModel() {
        this.service = new ProdutoService();
    }
}
```

### Factory Pattern for Test Data
```java
private ProdutoModel produtoFake() {
    ProdutoModel p = new ProdutoModel();
    p.codigoBarras = "7891234567890";
    p.descricao = "Produto Teste";
    p.precoCompra = new BigDecimal("10.50");
    return p;
}
```

## Performance Guidelines

- Use connection pooling if scaling beyond SQLite
- Implement lazy loading for large datasets
- Cache frequently accessed reference data
- Optimize database queries with proper indexing

## Git Workflow

- Commit frequently with descriptive messages
- Use feature branches for new functionality
- Ensure all tests pass before committing
- Run `./gradlew build` before pushing changes

## Additional Resources

Refer to existing code in `my_app.screens.produtoScreen` and `my_app.db.repositories.ProdutoRepository` for implementation patterns and best practices.