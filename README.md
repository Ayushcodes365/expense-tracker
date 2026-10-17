# Expense Tracker (Spring Boot)

A small expense tracker with a REST API, an H2 database and a simple web UI.

**Stack:** Java 17, Spring Boot 3.3, Spring Web, Spring Data JPA, Bean Validation, H2.

## Run

```bash
mvn spring-boot:run
```

Then open http://localhost:8080 for the UI. Data is stored in `./data/expensedb` so it survives restarts.
The H2 console is at http://localhost:8080/h2-console (JDBC URL `jdbc:h2:file:./data/expensedb`, user `sa`, empty password).

## Test

```bash
mvn test
```

## API

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/expenses` | List expenses (newest first). Optional filters: `category`, `from`, `to` (ISO dates) |
| GET | `/api/expenses/{id}` | Get one expense |
| POST | `/api/expenses` | Create an expense |
| PUT | `/api/expenses/{id}` | Update an expense |
| DELETE | `/api/expenses/{id}` | Delete an expense |
| GET | `/api/expenses/summary` | Total, count, totals by category and by month. Optional `from`, `to` |

Categories: `FOOD, TRANSPORT, HOUSING, UTILITIES, ENTERTAINMENT, HEALTH, SHOPPING, EDUCATION, OTHER`.

### Example

```bash
curl -X POST localhost:8080/api/expenses \
  -H "Content-Type: application/json" \
  -d '{"title":"Lunch","amount":12.50,"category":"FOOD","date":"2026-10-17","notes":"Cafe"}'

curl "localhost:8080/api/expenses?category=FOOD&from=2026-10-01"
curl localhost:8080/api/expenses/summary
```

Validation errors return HTTP 400 with an `errors` map; a missing id returns 404.

## Project layout

```
src/main/java/com/example/expensetracker
├── ExpenseTrackerApplication.java
├── controller/   ExpenseController
├── service/      ExpenseService
├── repository/   ExpenseRepository
├── model/        Expense, Category
├── dto/          ExpenseRequest, SummaryResponse
└── exception/    ResourceNotFoundException, GlobalExceptionHandler
src/main/resources/static/index.html   (web UI)
```

## Ideas for next steps

- Swap H2 for PostgreSQL/MySQL (change the datasource URL and add the driver).
- Add user accounts with Spring Security.
- Add monthly budgets and alerts.
