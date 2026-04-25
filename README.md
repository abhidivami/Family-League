# Family League

A Spring Boot REST API for running an IPL prediction game. Users predict match outcomes (toss, winner, POTM), accumulate points, and compete on a leaderboard. An admin manages the league structure and publishes results.

## Prerequisites

| Tool | Version |
|---|---|
| Java | 17+ |
| Maven | via `./mvnw` (included) |
| PostgreSQL | 14+ |

## Quick Start

### 1. Clone

```bash
git clone https://github.com/abhidivami/Family-League.git
cd FamilyLeague
```

### 2. Create the database

```bash
psql -U postgres -c "CREATE DATABASE \"family-league\";"
```

### 3. Configure

```bash
cp src/main/resources/application.properties.example src/main/resources/application.properties
```

Open `application.properties` and set your values:

```properties
spring.datasource.username=your_db_user
spring.datasource.password=your_db_password

# Generate with: openssl rand -hex 32
app.jwt.secret=your_64_char_hex_secret
```

Or export environment variables instead (recommended for production):

```bash
export DB_URL=jdbc:postgresql://localhost:5432/family-league
export DB_USERNAME=your_db_user
export DB_PASSWORD=your_db_password
export JWT_SECRET=$(openssl rand -hex 32)
```

### 4. Run

```bash
./mvnw spring-boot:run
```

Flyway runs automatically on startup and applies all migrations. The API is ready at `http://localhost:8080`.

### 5. Explore the API

- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- OpenAPI JSON: [http://localhost:8080/api-docs](http://localhost:8080/api-docs)
- Health check: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

---

## First-time Setup (Admin + Data)

### Register users

```bash
# Create a regular user
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"rahul","password":"Rahul@123","email":"rahul@example.com","displayName":"Rahul Sharma"}'

# Create the admin user
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"Admin@123","email":"admin@example.com","displayName":"IPL Admin"}'
```

Promote the admin in the database:

```sql
UPDATE users SET role = 'ROLE_ADMIN' WHERE username = 'admin';
```

### Get a token

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"Admin@123"}'
```

Use the `accessToken` from the response as `Authorization: Bearer <token>` on all subsequent requests.

---

## Typical Admin Flow

```
Create League → Create Season → Add Teams → Add Players → Add Players to Season Teams
    → Activate Season → Schedule Matches → Set Playing XI
    → [Users submit predictions]
    → Publish Match Result  ← triggers async scoring
    → Close Season          ← triggers season prediction scoring
```

---

## Scoring Rules

| Prediction | Points |
|---|---|
| Correct toss winner | 1 |
| Correct match winner | 1 |
| Correct POTM | 1 |
| Correct team final position (season) | 1 per position |

IPL-style standings: Win = 2 pts, Tie / No Result = 1 pt, Loss = 0 pts.

---

## Tech Stack

- **Java 25** · **Spring Boot 4** · **Spring Security 7** (JWT, BCrypt)
- **PostgreSQL** · **Spring Data JPA** · **Hibernate 7** · **Flyway 11**
- **SpringDoc OpenAPI 2** · **Spring Boot Actuator**

See [ARCHITECTURE.md](ARCHITECTURE.md) for the full domain model, API reference, and async flow details.
