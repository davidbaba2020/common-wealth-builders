# Commonwealth Builders Management System

A comprehensive Spring Boot application for managing wealth-building operations including user management, payment processing, expense tracking, and financial reporting with role-based access control.

## Table of Contents

- [Features](#features)
- [Technology Stack](#technology-stack)
- [System Architecture](#system-architecture)
- [Role-Based Access Control](#role-based-access-control)
- [Installation](#installation)
- [Configuration](#configuration)
- [API Documentation](#api-documentation)
- [Usage Examples](#usage-examples)
- [Security](#security)
- [Testing](#testing)
- [Deployment](#deployment)
- [Contributing](#contributing)
- [License](#license)

---

## Features

### 🔐 Authentication & Authorization
- JWT-based authentication
- Role-based access control (RBAC)
- Password encryption with BCrypt
- Password reset functionality
- Session management

### 👥 User Management
- User registration (TECH_ADMIN only)
- User profile management
- Account enable/disable
- User search functionality
- Comprehensive user tracking

### 💰 Payment Management
- Payment submission by users
- Payment verification workflow (FIN_ADMIN)
- Payment status tracking (PENDING, VERIFIED, REJECTED, CANCELLED)
- Payment search and filtering
- Payment history

### 💳 Expense Management
- Expense creation and tracking (FIN_ADMIN)
- Expense approval workflow
- Category-based organization
- Expense search and reporting
- Vendor management

### 📊 Financial Reporting
- Financial summary reports
- User contribution reports
- Expense reports
- Payment reports
- Monthly summary reports
- PDF generation for all reports
- Email distribution
- WhatsApp notifications

### 🔍 Audit Trail
- Comprehensive activity logging
- User action tracking
- Module-based filtering
- Searchable audit logs
- Timestamp tracking

### 🎭 Role Management
- Dynamic role creation (SUPER_ADMIN)
- Role assignment/revocation (TECH_ADMIN)
- Permission management
- Role-based endpoint access

---

## Technology Stack

### Backend
- **Java 17** - Programming language
- **Spring Boot 4.0.2** - Application framework
- **Spring Security** - Authentication & authorization
- **Spring Data JPA** - Database operations
- **PostgreSQL** - Primary database
- **JWT (JJWT 0.11.5)** - Token-based authentication
- **MapStruct 1.5.5** - DTO mapping

### Documentation
- **SpringDoc OpenAPI 2.3.0** - API documentation (Swagger)

### Reporting & Communication
- **iText7 8.0.2** - PDF generation
- **Thymeleaf** - Email templates
- **Spring Mail** - Email functionality
- **Twilio SDK 9.14.1** - WhatsApp integration

### Build & Development
- **Maven** - Dependency management
- **Lombok 1.18.32** - Boilerplate reduction
- **Spring DevTools** - Development utilities

---

## System Architecture
```
┌─────────────────────────────────────────────────────────────┐
│                     Client Layer                            │
│  (Web Browser, Mobile App, API Consumer)                   │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│                  API Gateway Layer                          │
│  - JWT Authentication Filter                                │
│  - Rate Limiting                                            │
│  - Request Validation                                       │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│                  Controller Layer                           │
│  ┌──────────────┬──────────────┬──────────────┬──────────┐ │
│  │ Auth         │ User         │ Payment      │ Expense  │ │
│  │ Controller   │ Controller   │ Controller   │Controller│ │
│  ├──────────────┼──────────────┼──────────────┼──────────┤ │
│  │ Role         │ Report       │ AuditTrail   │          │ │
│  │ Controller   │ Controller   │ Controller   │          │ │
│  └──────────────┴──────────────┴──────────────┴──────────┘ │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│                   Service Layer                             │
│  - Business Logic                                           │
│  - Transaction Management                                   │
│  - Data Validation                                          │
│  - External Service Integration                             │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│                 Repository Layer                            │
│  - JPA Repositories                                         │
│  - Custom Queries                                           │
│  - Database Interactions                                    │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│                  PostgreSQL Database                        │
│  - User Data                                                │
│  - Payments & Expenses                                      │
│  - Audit Logs                                               │
│  - Roles & Permissions                                      │
└─────────────────────────────────────────────────────────────┘

External Services:
┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│  SMTP Server │  │    Twilio    │  │  File System │
│   (Email)    │  │  (WhatsApp)  │  │  (PDF Files) │
└──────────────┘  └──────────────┘  └──────────────┘
```

---

## Role-Based Access Control

### User Roles

| Role | Description | Access Level |
|------|-------------|--------------|
| **SUPER_ADMIN** | System administrator with full access | All operations |
| **TECH_ADMIN** | Technical administrator | User management, roles, audit logs |
| **FIN_ADMIN** | Financial administrator | Payments, expenses, reports |
| **USER** | Regular system user | Own profile, create payments |

### Permission Matrix

| Feature | SUPER_ADMIN | TECH_ADMIN | FIN_ADMIN | USER |
|---------|-------------|------------|-----------|------|
| **Authentication** |
| Login | ✅ | ✅ | ✅ | ✅ |
| Change Own Password | ✅ | ✅ | ✅ | ✅ |
| Reset Password | ✅ | ✅ | ✅ | ✅ |
| **User Management** |
| Register New User | ✅ | ✅ | ❌ | ❌ |
| View All Users | ✅ | ✅ | ❌ | ❌ |
| View User Details | ✅ | ✅ | ❌ | ❌ |
| Enable/Disable User | ✅ | ✅ | ❌ | ❌ |
| Delete User | ✅ | ❌ | ❌ | ❌ |
| View Own Profile | ✅ | ✅ | ✅ | ✅ |
| **Role Management** |
| Create Role | ✅ | ❌ | ❌ | ❌ |
| View Roles | ✅ | ✅ | ❌ | ❌ |
| Update Role | ✅ | ❌ | ❌ | ❌ |
| Delete Role | ✅ | ❌ | ❌ | ❌ |
| Assign Role | ✅ | ✅ | ❌ | ❌ |
| Revoke Role | ✅ | ✅ | ❌ | ❌ |
| **Payment Management** |
| Create Payment | ✅ | ✅ | ✅ | ✅ |
| View All Payments | ✅ | ❌ | ✅ | ❌ |
| View Own Payments | ✅ | ✅ | ✅ | ✅ |
| Verify Payment | ✅ | ❌ | ✅ | ❌ |
| Reject Payment | ✅ | ❌ | ✅ | ❌ |
| Cancel Own Payment | ✅ | ✅ | ✅ | ✅ |
| **Expense Management** |
| Create Expense | ✅ | ❌ | ✅ | ❌ |
| View Expenses | ✅ | ❌ | ✅ | ❌ |
| Update Expense | ✅ | ❌ | ✅ | ❌ |
| Delete Expense | ✅ | ❌ | ❌ | ❌ |
| Approve Expense | ✅ | ❌ | ✅ | ❌ |
| **Financial Reports** |
| Generate Reports | ✅ | ❌ | ✅ | ❌ |
| Download PDF Reports | ✅ | ❌ | ✅ | ❌ |
| Send Email Reports | ✅ | ❌ | ✅ | ❌ |
| Send WhatsApp Reports | ✅ | ❌ | ✅ | ❌ |
| **Audit Trail** |
| View Audit Logs | ✅ | ✅ | ❌ | ❌ |
| Search Audit Logs | ✅ | ✅ | ❌ | ❌ |

---

## Installation

### Prerequisites

- Java 17 or higher
- Maven 3.8+
- PostgreSQL 14+
- SMTP server credentials (for email)
- Twilio account (for WhatsApp - optional)

### Step 1: Clone the Repository
```bash
git clone https://github.com/davidbaba2020/common-wealth-builders.git
cd common-wealth-builders
```

### Step 2: Database Setup

Create a PostgreSQL database:
```sql
CREATE DATABASE commonwealth_db;
CREATE USER commonwealth_user WITH PASSWORD 'your_secure_password';
GRANT ALL PRIVILEGES ON DATABASE commonwealth_db TO commonwealth_user;
```

### Step 3: Configure Environment Variables

Create a `.env` file in the project root or set system environment variables:
```properties
# Database Configuration
DB_URL=jdbc:postgresql://localhost:5432/commonwealth_db
DB_USERNAME=commonwealth_user
DB_PASSWORD=your_secure_password

# JWT Configuration
JWT_SECRET=your-super-secret-jwt-key-min-256-bits-long
JWT_EXPIRATION=86400000

# Email Configuration
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-gmail-app-password
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587

# Twilio Configuration (Optional)
TWILIO_ACCOUNT_SID=your-twilio-account-sid
TWILIO_AUTH_TOKEN=your-twilio-auth-token
TWILIO_WHATSAPP_NUMBER=+14155238886

# Application Configuration
SERVER_PORT=8080
APPLICATION_NAME=Commonwealth Builders
APPLICATION_VERSION=1.0.0
```

### Step 4: Update application.yaml

Edit `src/main/resources/application.yaml`:
```yaml
spring:
  application:
    name: commonwealth-builders

  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    driver-class-name: org.postgresql.Driver

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true

  mail:
    host: ${MAIL_HOST:smtp.gmail.com}
    port: ${MAIL_PORT:587}
    username: ${MAIL_USERNAME}
    password: ${MAIL_PASSWORD}
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
            required: true

jwt:
  secret: ${JWT_SECRET}
  expiration: ${JWT_EXPIRATION:86400000}

twilio:
  account-sid: ${TWILIO_ACCOUNT_SID:}
  auth-token: ${TWILIO_AUTH_TOKEN:}
  whatsapp-number: ${TWILIO_WHATSAPP_NUMBER:}

application:
  name: ${APPLICATION_NAME:Commonwealth Builders}
  version: ${APPLICATION_VERSION:1.0.0}

server:
  port: ${SERVER_PORT:8080}

springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
    tags-sorter: alpha
    operations-sorter: alpha
```

### Step 5: Build the Project
```bash
mvn clean install
```

### Step 6: Run the Application
```bash
mvn spring-boot:run
```

Or run the JAR file:
```bash
java -jar target/common-wealth-builders-0.0.1-SNAPSHOT.jar
```

The application will start on `http://localhost:8080`

---

## Configuration

### Gmail App Password Setup

1. Go to your Google Account settings
2. Enable 2-Factor Authentication
3. Go to Security > App Passwords
4. Generate a new app password for "Mail"
5. Use this password in `MAIL_PASSWORD` environment variable

### Twilio WhatsApp Setup

1. Sign up for a Twilio account at https://www.twilio.com
2. Get a WhatsApp-enabled phone number
3. Configure WhatsApp sandbox or get approval for production
4. Use your Account SID, Auth Token, and WhatsApp number in environment variables

---

## API Documentation

### Swagger UI

Once the application is running, access the interactive API documentation:
```
http://localhost:8080/swagger-ui.html
```

### OpenAPI JSON

Access the raw OpenAPI specification:
```
http://localhost:8080/v3/api-docs
```

### API Base URL
```
http://localhost:8080/v1
```

### Authentication

All protected endpoints require a JWT token in the Authorization header:
```
Authorization: Bearer <your-jwt-token>
```

---

## Usage Examples

### 1. User Registration Flow

**Step 1: TECH_ADMIN logs in**
```bash
curl -X POST http://localhost:8080/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "tech.admin@example.com",
    "password": "AdminPass123!"
  }'
```

**Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": 1,
      "email": "tech.admin@example.com",
      "roles": ["ROLE_TECH_ADMIN"]
    }
  },
  "timestamp": "2026-02-05T10:30:00Z"
}
```

**Step 2: TECH_ADMIN registers new user**
```bash
curl -X POST http://localhost:8080/v1/auth/register \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -d '{
    "firstName": "Jane",
    "lastName": "Doe",
    "email": "jane.doe@example.com",
    "password": "SecurePass123!",
    "phoneNumber": "+2348012345678"
  }'
```

### 2. Payment Workflow

**Step 1: User creates payment**
```bash
curl -X POST http://localhost:8080/v1/payments \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer USER_JWT_TOKEN" \
  -d '{
    "amount": 50000.00,
    "paymentMethod": "BANK_TRANSFER",
    "referenceNumber": "TXN123456789",
    "description": "Monthly contribution",
    "paymentDate": "2026-02-05T10:30:00"
  }'
```

**Step 2: FIN_ADMIN reviews pending payments**
```bash
curl -X GET "http://localhost:8080/v1/payments/pending?page=0&size=10" \
  -H "Authorization: Bearer FIN_ADMIN_JWT_TOKEN"
```

**Step 3: FIN_ADMIN verifies payment**
```bash
curl -X PUT http://localhost:8080/v1/payments/1/verify \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer FIN_ADMIN_JWT_TOKEN" \
  -d '{
    "remarks": "Payment verified successfully",
    "verifiedAmount": 50000.00
  }'
```

### 3. Financial Reporting

**Generate and download financial summary**
```bash
curl -X POST http://localhost:8080/v1/reports/financial-summary/download \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer FIN_ADMIN_JWT_TOKEN" \
  -d '{
    "startDate": "2026-01-01T00:00:00",
    "endDate": "2026-01-31T23:59:59"
  }' \
  --output financial_summary.pdf
```

**Send report via email**
```bash
curl -X POST http://localhost:8080/v1/reports/financial-summary/send-email \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer FIN_ADMIN_JWT_TOKEN" \
  -d '{
    "startDate": "2026-01-01T00:00:00",
    "endDate": "2026-01-31T23:59:59",
    "email": "board@example.com",
    "recipientName": "Board of Directors",
    "subject": "January 2026 Financial Summary"
  }'
```

---

## Security

### Authentication
- JWT tokens expire after 24 hours (configurable)
- Passwords hashed using BCrypt (strength 12)
- Token refresh mechanism

### Authorization
- Role-based access control (RBAC)
- Method-level security with `@PreAuthorize`
- Fine-grained permissions

### Data Protection
- SQL injection prevention via JPA
- XSS protection
- CSRF protection for state-changing operations
- HTTPS recommended for production

### Audit Logging
- All user actions logged
- IP address tracking
- User agent tracking
- Timestamp tracking

---

## Testing

### Run All Tests
```bash
mvn test
```

### Run Specific Test Class
```bash
mvn test -Dtest=PaymentServiceTest
```

### Integration Tests
```bash
mvn verify
```

### Test Coverage
```bash
mvn jacoco:report
```

View coverage report at: `target/site/jacoco/index.html`

---

## Deployment

### Production Build
```bash
mvn clean package -DskipTests
```

### Docker Deployment

**Dockerfile:**
```dockerfile
FROM openjdk:17-jdk-slim
VOLUME /tmp
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
EXPOSE 8080
```

**Build and Run:**
```bash
docker build -t commonwealth-builders .
docker run -p 8080:8080 \
  -e DB_URL=jdbc:postgresql://host.docker.internal:5432/commonwealth_db \
  -e DB_USERNAME=commonwealth_user \
  -e DB_PASSWORD=your_password \
  -e JWT_SECRET=your_jwt_secret \
  commonwealth-builders
```

### Docker Compose

**docker-compose.yml:**
```yaml
version: '3.8'

services:
  postgres:
    image: postgres:14
    environment:
      POSTGRES_DB: commonwealth_db
      POSTGRES_USER: commonwealth_user
      POSTGRES_PASSWORD: your_password
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      DB_URL: jdbc:postgresql://postgres:5432/commonwealth_db
      DB_USERNAME: commonwealth_user
      DB_PASSWORD: your_password
      JWT_SECRET: your_jwt_secret
      MAIL_USERNAME: your_email@gmail.com
      MAIL_PASSWORD: your_app_password
    depends_on:
      - postgres

volumes:
  postgres_data:
```

**Run:**
```bash
docker-compose up -d
```

---

## Project Structure
```
commonwealth-builders/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── common_wealth_builders/
│   │   │           ├── config/                 # Configuration classes
│   │   │           │   ├── OpenApiConfig.java
│   │   │           │   ├── SecurityConfig.java
│   │   │           │   └── JpaAuditingConfig.java
│   │   │           ├── controller/             # REST controllers
│   │   │           │   ├── AuthController.java
│   │   │           │   ├── UserController.java
│   │   │           │   ├── PaymentController.java
│   │   │           │   ├── ExpenseController.java
│   │   │           │   ├── RoleController.java
│   │   │           │   ├── ReportController.java
│   │   │           │   └── AuditTrailController.java
│   │   │           ├── dto/                    # Data Transfer Objects
│   │   │           │   ├── request/
│   │   │           │   └── response/
│   │   │           ├── entity/                 # JPA entities
│   │   │           │   ├── User.java
│   │   │           │   ├── Payment.java
│   │   │           │   ├── Expense.java
│   │   │           │   ├── Role.java
│   │   │           │   └── AuditTrail.java
│   │   │           ├── repository/             # JPA repositories
│   │   │           ├── service/                # Service interfaces
│   │   │           │   └── impl/               # Service implementations
│   │   │           ├── security/               # Security components
│   │   │           │   ├── JwtUtil.java
│   │   │           │   └── JwtAuthenticationFilter.java
│   │   │           ├── exception/              # Exception handling
│   │   │           └── enums/                  # Enum types
│   │   └── resources/
│   │       ├── application.yaml                # Application configuration
│   │       └── templates/
│   │           └── email/                      # Email templates
│   └── test/                                   # Test classes
├── pom.xml                                     # Maven configuration
├── README.md                                   # This file
├── .gitignore
└── .env.example                                # Environment variables template
```

---

## API Endpoints Summary

### Authentication
- `POST /v1/auth/login` - Login
- `POST /v1/auth/register` - Register new user (TECH_ADMIN)
- `POST /v1/auth/change-password` - Change password
- `POST /v1/auth/reset-password` - Reset password
- `DELETE /v1/auth/delete/{userId}` - Delete user (SUPER_ADMIN)

### User Management
- `GET /v1/users` - Get all users (TECH_ADMIN)
- `GET /v1/users/{id}` - Get user by ID (TECH_ADMIN)
- `GET /v1/users/profile` - Get current user profile
- `GET /v1/users/search` - Search users (TECH_ADMIN)
- `POST /v1/users/{id}/enable` - Enable user (TECH_ADMIN)
- `POST /v1/users/{id}/disable` - Disable user (TECH_ADMIN)

### Payment Management
- `POST /v1/payments` - Create payment
- `GET /v1/payments` - Get all payments (FIN_ADMIN)
- `GET /v1/payments/{id}` - Get payment by ID
- `GET /v1/payments/user/{userId}` - Get user payments
- `GET /v1/payments/pending` - Get pending payments (FIN_ADMIN)
- `PUT /v1/payments/{id}/verify` - Verify payment (FIN_ADMIN)
- `PUT /v1/payments/{id}/reject` - Reject payment (FIN_ADMIN)
- `PUT /v1/payments/{id}/cancel` - Cancel payment
- `GET /v1/payments/search` - Search payments (FIN_ADMIN)

### Expense Management
- `POST /v1/expenses` - Create expense (FIN_ADMIN)
- `GET /v1/expenses` - Get all expenses (FIN_ADMIN)
- `GET /v1/expenses/{id}` - Get expense by ID (FIN_ADMIN)
- `PUT /v1/expenses/{id}` - Update expense (FIN_ADMIN)
- `DELETE /v1/expenses/{id}` - Delete expense (SUPER_ADMIN)
- `POST /v1/expenses/{id}/approve` - Approve expense (FIN_ADMIN)
- `GET /v1/expenses/pending` - Get pending expenses (FIN_ADMIN)
- `GET /v1/expenses/search` - Search expenses (FIN_ADMIN)

### Role Management
- `POST /v1/roles` - Create role (SUPER_ADMIN)
- `GET /v1/roles` - Get all roles (TECH_ADMIN)
- `GET /v1/roles/{id}` - Get role by ID (TECH_ADMIN)
- `PUT /v1/roles/{id}` - Update role (SUPER_ADMIN)
- `DELETE /v1/roles/{id}` - Delete role (SUPER_ADMIN)
- `POST /v1/roles/assign` - Assign role (TECH_ADMIN)
- `POST /v1/roles/revoke` - Revoke role (TECH_ADMIN)
- `GET /v1/roles/user/{userId}` - Get user roles (TECH_ADMIN)

### Financial Reports
- `POST /v1/reports/financial-summary` - Generate financial summary (FIN_ADMIN)
- `POST /v1/reports/financial-summary/download` - Download PDF (FIN_ADMIN)
- `POST /v1/reports/financial-summary/send-email` - Send via email (FIN_ADMIN)
- `POST /v1/reports/financial-summary/send-whatsapp` - Send via WhatsApp (FIN_ADMIN)
- `GET /v1/reports/user-contribution/{userId}` - User contribution report (FIN_ADMIN)
- `GET /v1/reports/user-contribution/{userId}/download` - Download PDF (FIN_ADMIN)
- `POST /v1/reports/expenses` - Generate expense report (FIN_ADMIN)
- `POST /v1/reports/expenses/download` - Download PDF (FIN_ADMIN)
- `POST /v1/reports/payments` - Generate payment report (FIN_ADMIN)
- `POST /v1/reports/payments/download` - Download PDF (FIN_ADMIN)
- `GET /v1/reports/monthly` - Generate monthly report (FIN_ADMIN)

### Audit Trail
- `GET /v1/audit-trail` - Get all audit logs (TECH_ADMIN)
- `GET /v1/audit-trail/user/{userId}` - Get user audit logs (TECH_ADMIN)
- `GET /v1/audit-trail/module/{module}` - Get module audit logs (TECH_ADMIN)
- `GET /v1/audit-trail/search` - Search audit logs (TECH_ADMIN)

---

## Troubleshooting

### Common Issues

**1. Database Connection Error**
```
Error: Connection refused - could not connect to database
```
**Solution:** Verify PostgreSQL is running and credentials are correct in application.yaml

**2. JWT Token Expired**
```
401 Unauthorized - Token has expired
```
**Solution:** Login again to get a new token

**3. Email Not Sending**
```
Error: Authentication failed
```
**Solution:** Use Gmail App Password instead of regular password

**4. Port Already in Use**
```
Error: Port 8080 is already in use
```
**Solution:** Change port in application.yaml or kill process using port 8080

**5. Maven Build Failure**
```
Error: Could not resolve dependencies
```
**Solution:** Run `mvn clean install -U` to force update dependencies

---

## Contributing

We welcome contributions! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### Coding Standards

- Follow Java naming conventions
- Write unit tests for new features
- Document public APIs with JavaDoc
- Keep methods focused and small
- Use meaningful variable names

---

## License

This project is proprietary software. All rights reserved.

---

## Support

For support, email support@commonwealthbuilders.com or open an issue in the GitHub repository.

---

## Changelog

### Version 1.0.0 (2026-02-05)
- Initial release
- User management with RBAC
- Payment processing and verification
- Expense management and approval
- Financial reporting with PDF, email, and WhatsApp
- Comprehensive audit trail
- Swagger API documentation

---

## Acknowledgments

- Spring Boot team for the excellent framework
- iText for PDF generation capabilities
- Twilio for WhatsApp integration
- All contributors to this project

---

**Built with ❤️ by Commonwealth Builders Team**