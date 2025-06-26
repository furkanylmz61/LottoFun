# LottoFun - Lottery System API

A comprehensive lottery management system built with Spring Boot that provides a complete lottery experience including user registration, ticket purchasing, automated draws, and prize distribution.

## 🚀 Features

### Core Functionality
- **User Authentication & Authorization**: JWT-based security system with user registration and login
- **Lottery Ticket System**: Purchase tickets by selecting 5 unique numbers from 1-49
- **Automated Draw System**: Scheduled draws that execute automatically every minute
- **Prize Distribution**: Multi-tier prize system based on matching numbers
- **Balance Management**: Virtual wallet system for users with automatic prize payouts
- **Draw History**: Complete history of all past draws with results

### Technical Features
- **RESTful API**: Well-documented REST endpoints with OpenAPI/Swagger documentation
- **Real-time Processing**: Automated draw execution and result processing
- **Database Integration**: PostgreSQL database with JPA/Hibernate
- **Security**: JWT authentication with Spring Security
- **Validation**: Input validation for all user inputs
- **Exception Handling**: Comprehensive error handling and custom exceptions
- **Scheduled Tasks**: Automated draw scheduling and execution

## 🏗️ Architecture

The application follows a clean architecture pattern with clear separation of concerns:

```
├── presentation/           # Controllers and DTOs
│   ├── controller/        # REST API endpoints
│   ├── dto/              # Data Transfer Objects
│   └── validation/       # Custom validators
├── service/              # Business logic layer
├── entity/               # JPA entities
├── infrastructure/       # Infrastructure concerns
│   ├── repository/       # Data access layer
│   ├── security/         # Security configuration
│   └── configuration/    # Application configuration
├── exception/            # Custom exceptions
├── config/               # Spring configuration
└── util/                 # Utility classes
```

## 🎯 Prize System

The lottery uses a 5-number system (1-49) with the following prize tiers:

| Matched Numbers | Prize Amount |
|-----------------|--------------|
| 5 matches       | $1,000,000   |
| 4 matches       | $1,000       |
| 3 matches       | $100         |
| 2 matches       | $10          |
| 0-1 matches     | No prize     |

## 📋 Prerequisites

- **Java 17** or higher
- **Maven 3.6+**
- **PostgreSQL 12+**
- **Git**


## 📚 API Documentation

### Swagger UI
Once the application is running, access the interactive API documentation at:
```
http://localhost:8080/swagger-ui/html
```

### Authentication Endpoints

#### Register User
```http
POST /api/auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe"
}
```

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}
```

### Ticket Operations

#### Purchase Ticket
```http
POST /api/ticket/purchase
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "drawId": 1,
  "selectedNumbers": [7, 14, 21, 28, 35]
}
```

#### Get Ticket Details
```http
GET /api/ticket/{ticketId}
Authorization: Bearer <jwt-token>
```

### Draw Operations

#### Get Active Draw
```http
GET /api/draw/active
Authorization: Bearer <jwt-token>
```

#### Get Draw History
```http
GET /api/draw/history?page=0&size=10&direction=desc
Authorization: Bearer <jwt-token>
```

### User Operations

#### Get User Profile
```http
GET /api/user/profile
Authorization: Bearer <jwt-token>
```

#### Get User Tickets
```http
GET /api/user/tickets?page=0&size=10
Authorization: Bearer <jwt-token>
```

#### Claim Prize
```http
POST /api/user/claim-ticket/{ticketId}
Authorization: Bearer <jwt-token>
```

## 🎮 How to Use

### 1. Register and Login
- Create a new user account via `/api/auth/register`
- Login to receive a JWT token
- Each new user starts with $1,000 balance

### 2. Purchase Tickets
- Get the active draw ID from `/api/draw/active`
- Purchase tickets by selecting 5 unique numbers (1-49)
- Each ticket costs $10

### 3. Check Results
- Draws execute automatically every minute
- Check your ticket status via `/api/ticket/{ticketId}`
- View draw history to see winning numbers

### 4. Claim Prizes
- Winning tickets can be claimed via `/api/user/claim-ticket/{ticketId}`
- Prize money is automatically added to your balance

## 🔧 Configuration

### Application Configuration
Key configuration properties in `application.yml`:

```yaml
# Lottery Settings
lottery:
  ticket:
    price: 10.00
    max-numbers: 5
    min-number: 1
    max-number: 49
  draw:
    frequencyMinutes: 1
    processingBatchSize: 1000
  prizes:
    jackpot: 1000000.00
    high: 1000.00
    medium: 100.00
    low: 10.00
```


## 📊 Database Schema

### Key Entities

#### Users
- User account information
- Balance management
- Relationship to tickets

#### Draws
- Draw scheduling and status
- Winning numbers
- Prize pool information

#### Tickets
- User ticket purchases
- Selected numbers
- Prize calculation results

### Entity Relationships
- User → Tickets (One-to-Many)
- Draw → Tickets (One-to-Many)
- Unique constraint on (user, draw, selected_numbers)

## 🔐 Security

### Authentication
- JWT-based stateless authentication
- Password encoding with BCrypt
- Token expiration handling

### Authorization
- Protected endpoints require valid JWT token
- User can only access their own data
- Role-based access control ready for extension

### Input Validation
- Custom validators for lottery numbers
- Bean validation for all DTOs
- SQL injection prevention via JPA

## 🚀 Production Deployment

### Environment Variables for Production
```bash
# Database
DB_HOST=your-production-db-host
DB_PORT=5432
DB_NAME=lottofun_prod
DB_USERNAME=lottofun_prod_user
DB_PASSWORD=secure-production-password

# JWT Security
JWT_SECRET=your-super-secure-256-bit-secret-key
JWT_EXPIRATION=86400000

# Application
SPRING_PROFILES_ACTIVE=prod
```

### Docker Deployment (Optional)
```dockerfile
FROM openjdk:17-jdk-slim
COPY target/lottofun-*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### Performance Considerations
- Database connection pooling
- Batch processing for large operations
- Pagination for large result sets
- Scheduled task optimization

## 🐛 Troubleshooting

### Common Issues

1. **Database Connection Issues**
   - Verify PostgreSQL is running
   - Check environment variables
   - Ensure database exists and user has permissions

2. **JWT Token Issues**
   - Ensure JWT_SECRET is at least 256 bits
   - Check token expiration
   - Verify Authorization header format

3. **Scheduling Issues**
   - Check system time synchronization
   - Verify `@EnableScheduling` annotation
   - Monitor application logs for scheduled tasks

