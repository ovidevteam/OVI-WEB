# DHY Backend

Spring Boot 3 service powering the “Hệ thống Quản lý Phản ánh” platform for BV Đại học Y Hà Nội.

## Modules

| Module | Highlights |
| --- | --- |
| Auth | JWT login + change password |
| Users | Handler directory exposure |
| Departments/Doctors | CRUD with role-based access |
| Feedbacks | Ticket lifecycle (create, assign, process, history) |
| Ratings | Doctor ratings, statistics, completed-feedback listing |
| Reports | Dashboard, department/doctor KPIs, monthly stats, image-heavy report |
| Uploads | Local storage for feedback/process images |

## Tech Stack

- Spring Boot 3 / Java 21
- Spring Security + JWT
- Spring Data JPA (PostgreSQL)
- Lombok, Validation API

## Run

```bash
# from dhy_be/
./mvnw spring-boot:run
```

Environment variables:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5434/dhypostgres
spring.datasource.username=admin
spring.datasource.password=postgres
app.storage.base-dir=uploads
app.storage.public-base-url=/uploads
```

## Role Matrix (summary)

| Endpoint group | Roles |
| --- | --- |
| Departments/Doctors CRUD | ADMIN (& LEADER read) |
| Feedback search/detail | ADMIN, LEADER, RECEIVER, HANDLER, VIEWER |
| Feedback create | ADMIN, RECEIVER |
| Feedback process | HANDLER |
| Ratings create | ADMIN, LEADER, HANDLER |
| Reports | ADMIN, LEADER |
| File uploads | ADMIN + RECEIVER/HANDLER |

Refer to `03-TECHNICAL-ARCHITECTURE*.md` for deeper BA specs.





