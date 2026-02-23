# Auth Service

Authentication and authorization microservice for managing user authentication, JWT token generation, and security.

## Overview

The Auth Service handles user authentication, JWT token generation/validation, and provides secure endpoints for user registration and login.

## Architecture Overview

### System Architecture
```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │ HTTP/REST
       ▼
┌─────────────────────────────────┐
│      Auth Service (8081)        │
│  ┌──────────────────────────┐   │
│  │  Security Filter Chain   │   │
│  └────────┬─────────────────┘   │
│           ▼                      │
│  ┌──────────────────────────┐   │
│  │   Auth Controller        │   │
│  └────────┬─────────────────┘   │
│           ▼                      │
│  ┌──────────────────────────┐   │
│  │   Auth Service Layer     │   │
│  └────────┬─────────────────┘   │
│           ▼                      │
│  ┌──────────────────────────┐   │
│  │   JWT Utility            │   │
│  └────────┬─────────────────┘   │
│           ▼                      │
│  ┌──────────────────────────┐   │
│  │   User Repository        │   │
│  └────────┬─────────────────┘   │
└───────────┼─────────────────────┘
            ▼
    ┌──────────────┐
    │  MySQL DB    │
    │  (auth_db)   │
    └──────────────┘
```

### Component Responsibilities
- **Security Filter Chain**: JWT validation and authentication
- **Auth Controller**: REST endpoints for authentication operations
- **Auth Service**: Business logic for user registration, login, token management
- **JWT Utility**: Token generation, validation, and parsing
- **User Repository**: Database operations for user entities

### Security Flow
1. User sends credentials to /api/auth/login
2. Service validates credentials against database
3. JWT token generated with user details and roles
4. Token returned to client
5. Client includes token in Authorization header for subsequent requests
6. Other services validate token using shared secret

## Assumptions

### Technical Assumptions
- MySQL database is accessible and properly configured
- JWT secret key is securely stored and shared across all microservices
- Token expiration is set to 24 hours (86400000 ms)
- BCrypt is used for password hashing with default strength (10 rounds)
- All services use the same JWT secret for token validation

### Business Assumptions
- User registration is open (no approval workflow)
- Username is email address (unique identifier)
- Roles are predefined: USER, ADMIN, MANAGER
- Single role per user (no multi-role support)
- No password complexity requirements enforced
- No account lockout mechanism after failed attempts
- Token refresh extends expiration without re-authentication
- No session management or token revocation

### Operational Assumptions
- Service runs on port 8081
- Database schema auto-created via Hibernate DDL
- No external identity provider integration
- Logging is enabled for SQL queries (development mode)
- No rate limiting on authentication endpoints

## Technology Stack

- **Java**: 21
- **Spring Boot**: 4.0.3
- **Spring Security**: JWT-based authentication
- **Spring Data JPA**: Database operations
- **MySQL**: Database
- **Lombok**: Reduce boilerplate code
- **JWT**: io.jsonwebtoken (0.11.5)

## Prerequisites

- JDK 21 or higher
- Maven 3.6+
- MySQL 8.0+

## Dependencies

```xml
- spring-boot-starter-data-jpa
- spring-boot-starter-security
- spring-boot-starter-webmvc
- mysql-connector-j
- lombok
- jjwt-api (0.11.5)
- jjwt-impl (0.11.5)
- jjwt-jackson (0.11.5)
```

## Environment Variables

Create `src/main/resources/application.properties`:

```properties
# Server Configuration
server.port=8081

# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/auth_db
spring.datasource.username=<db_username>
spring.datasource.password=<db_password>
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect

# JWT Configuration
jwt.secret=<your_secret_key>
jwt.expiration=86400000
```

## Setup Instructions

1. **Clone the repository**
   ```bash
   cd auth-service
   ```

2. **Create MySQL database**
   ```sql
   CREATE DATABASE auth_db;
   ```

3. **Configure application.properties**
   - Update database credentials
   - Set JWT secret key

4. **Build the project**
   ```bash
   mvn clean install
   ```

5. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

## API Endpoints

### Authentication

#### Register User
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "user@example.com",
  "password": "password123",
  "role": "USER"
}
```

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "user@example.com",
  "password": "password123"
}

Response:
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "expiresIn": 86400000
}
```

#### Validate Token
```http
POST /api/auth/validate
Authorization: Bearer <token>
```

#### Refresh Token
```http
POST /api/auth/refresh
Authorization: Bearer <token>
```

## Project Structure

```
auth-service/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/company/auth/
│   │   │       ├── config/
│   │   │       ├── controller/
│   │   │       ├── dto/
│   │   │       ├── entity/
│   │   │       ├── repository/
│   │   │       ├── security/
│   │   │       └── service/
│   │   └── resources/
│   │       └── application.properties
│   └── test/
└── pom.xml
```

## Testing Instructions

### Unit Tests
Run all unit tests:
```bash
mvn test
```

Run specific test class:
```bash
mvn test -Dtest=AuthServiceTest
```

### Integration Tests
Run integration tests with test database:
```bash
mvn verify
```

### Test Coverage
Generate test coverage report:
```bash
mvn clean test jacoco:report
```
View report at: `target/site/jacoco/index.html`

### Manual API Testing

#### 1. Register a new user
```bash
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"test@example.com","password":"test123","role":"USER"}'
```

#### 2. Login and get token
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test@example.com","password":"test123"}'
```

#### 3. Validate token
```bash
curl -X POST http://localhost:8081/api/auth/validate \
  -H "Authorization: Bearer <your_token>"
```

#### 4. Refresh token
```bash
curl -X POST http://localhost:8081/api/auth/refresh \
  -H "Authorization: Bearer <your_token>"
```

### Test Data Setup
Create test users:
```sql
USE auth_db;
INSERT INTO users (username, password, role) VALUES 
('admin@example.com', '$2a$10$encrypted_password', 'ADMIN'),
('user@example.com', '$2a$10$encrypted_password', 'USER');
```

### Testing Checklist
- [ ] User registration with valid data
- [ ] User registration with duplicate username
- [ ] Login with valid credentials
- [ ] Login with invalid credentials
- [ ] Token validation with valid token
- [ ] Token validation with expired token
- [ ] Token validation with invalid token
- [ ] Token refresh with valid token
- [ ] Password encryption verification
- [ ] Role-based access control

## Docker Support

```dockerfile
FROM openjdk:21-jdk-slim
WORKDIR /app
COPY target/auth-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
```

Build and run:
```bash
docker build -t auth-service .
docker run -p 8081:8081 auth-service
```

## Security Features

- Password encryption using BCrypt
- JWT token-based authentication
- Token expiration and refresh mechanism
- Role-based access control (RBAC)

## Error Handling

Standard error response format:
```json
{
  "timestamp": "2024-02-22T10:00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid credentials",
  "path": "/api/auth/login"
}
```

## Contributing

1. Create feature branch
2. Commit changes
3. Push to branch
4. Create Pull Request

## License

Proprietary
