# UES Event Finder

A full-stack web application for finding events and venues, built with Spring Boot and Angular.

## Tech Stack

**Backend:**
- Spring Boot 3.2
- Spring Security (JWT authentication)
- Spring Data JPA
- MySQL
- Log4j2
- Maven

**Frontend:**
- Angular 17 (standalone components)
- Bootstrap 5
- Chart.js (analytics charts)

## Prerequisites

- Java 17+
- Node.js 18+ and npm
- MySQL 8+
- Maven 3.6+

## Setup

### 1. Database

Create a MySQL database:
```sql
CREATE DATABASE ues_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. Backend Configuration

Edit `backend/src/main/resources/application.properties`:
```properties
spring.datasource.username=YOUR_DB_USER
spring.datasource.password=YOUR_DB_PASSWORD
spring.mail.username=YOUR_GMAIL
spring.mail.password=YOUR_GMAIL_APP_PASSWORD
```

### 3. Run Backend

```bash
cd backend
mvn spring-boot:run
```

The backend runs on http://localhost:8080

**Default admin credentials:**
- Email: `admin@ues.com`
- Password: `admin123`

### 4. Run Frontend

```bash
cd frontend
npm install
ng serve
```

The frontend runs on http://localhost:4200

## Features

### User Roles
- **Unregistered**: Submit registration request
- **Registered User (ROLE_USER)**: Browse locations/events, write reviews, manage profile
- **Manager (ROLE_MANAGER)**: All user features + manage events/locations, reply to reviews, view analytics
- **Admin (ROLE_ADMIN)**: All features + approve/reject registrations, manage managers

### API Endpoints

| Method | Endpoint | Access |
|--------|----------|--------|
| POST | /api/auth/login | Public |
| POST | /api/auth/register | Public |
| GET | /api/locations | Public |
| GET | /api/locations/{id} | Public |
| POST | /api/locations | ADMIN |
| PUT | /api/locations/{id} | ADMIN, MANAGER |
| DELETE | /api/locations/{id} | ADMIN |
| GET | /api/events | Public |
| GET | /api/events/today | Public |
| POST | /api/events | MANAGER, ADMIN |
| POST | /api/reviews | Authenticated |
| PATCH | /api/reviews/{id}/hide | MANAGER, ADMIN |
| DELETE | /api/reviews/{id} | MANAGER, ADMIN |
| POST | /api/reviews/{id}/comments | Authenticated |
| GET | /api/users/me | Authenticated |
| PUT | /api/users/me | Authenticated |
| POST | /api/users/me/change-password | Authenticated |
| GET | /api/admin/requests | ADMIN |
| POST | /api/admin/requests/{id}/approve | ADMIN |
| POST | /api/admin/requests/{id}/reject | ADMIN |
| POST | /api/admin/locations/{id}/managers | ADMIN |
| DELETE | /api/admin/locations/{id}/managers/{userId} | ADMIN |
| GET | /api/analytics/locations/{id} | MANAGER, ADMIN |

## Project Structure

```
ues-project/
├── backend/           # Spring Boot application
│   ├── pom.xml
│   └── src/main/java/com/ues/
│       ├── model/     # JPA entities
│       ├── repository/# Spring Data repositories
│       ├── service/   # Business logic
│       ├── controller/# REST controllers
│       ├── security/  # JWT auth
│       ├── config/    # Security & Web config
│       └── dto/       # Data transfer objects
└── frontend/          # Angular application
    └── src/app/
        ├── core/      # Services, guards, interceptors
        ├── shared/    # Navbar component
        └── features/  # Feature components
            ├── auth/
            ├── home/
            ├── locations/
            ├── events/
            ├── reviews/
            ├── profile/
            ├── admin/
            └── manager/analytics/
```
