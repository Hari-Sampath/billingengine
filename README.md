# Billing Engine

A minimal billing system built with Spring Boot and the Stripe API. Handles charging customers, storing transaction records, and automatically emailing customers when a payment fails. Built as a learning project to get hands-on with Spring Boot, JPA, and third-party API integration — this was my first Spring Boot project.

There's also a small dashboard (plain HTML/JS, no framework) for viewing customers and their transaction history, charging customers manually, and a basic login gate.

## What it does

- Creates a Stripe customer and processes a test-mode charge synchronously (no webhooks)
- Stores every transaction attempt — customer, amount, success/failure — in Postgres
- Runs a daily job that finds failed payments and emails the customer about it, without sending duplicates
- Pulls the live customer list from Stripe rather than relying on a local copy, so it stays accurate
- Locks the whole thing behind a single admin login

## Stack

- Java 17, Spring Boot, Maven
- Spring Data JPA + PostgreSQL
- Stripe Java SDK (test mode, legacy Charges API — see "Known limitations" below)
- Spring Boot Mail (Gmail SMTP)
- Lombok
- Vanilla HTML/CSS/JS frontend, no build step

## Setup

### 1. Database

Create the Postgres database once:

```sql
CREATE DATABASE billing_engine;
```

Hibernate handles the schema from there (`ddl-auto=update`).

### 2. Stripe

Grab a test-mode secret key from `dashboard.stripe.com/test/apikeys`.

### 3. Gmail SMTP

Regular Gmail passwords don't work with SMTP anymore. You need an **App Password**: Google Account → Security → 2-Step Verification → App Passwords.

### 4. `application.properties`

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/billing_engine
spring.datasource.username=postgres
spring.datasource.password=your_db_password
spring.jpa.hibernate.ddl-auto=update

stripe.api.key=sk_test_your_key_here

spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your_email@gmail.com
spring.mail.password=your_16_char_app_password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

admin.email=admin@yourdomain.local
admin.password=change_this
```

Don't commit this file with real credentials in it. It's in `.gitignore` for a reason.

### 5. Run it

```bash
mvn spring-boot:run
```

Then open `frontend/index.html` directly in a browser — no server needed for it, it just talks to the Spring Boot app at `localhost:8080`.

## Testing charges

Stripe gives you tokens that simulate specific outcomes, so you don't need real card numbers:

| Token | Result |
|---|---|
| `tok_visa` | Successful charge |
| `tok_chargeDeclined` | Generic decline |
| `tok_chargeDeclinedInsufficientFunds` | Insufficient funds |
| `tok_chargeDeclinedExpiredCard` | Expired card |
| `tok_chargeDeclinedIncorrectCvc` | Incorrect CVC |

Full list at [stripe.com/docs/testing](https://docs.stripe.com/testing).

## Project structure

```
com.example.billingengine/
├── config/        # Stripe setup, CORS, auth interceptor, admin seeding
├── controller/     # REST endpoints
├── dto/            # Request/response shapes
├── entity/         # JPA entities
├── repository/     # Spring Data repositories
├── scheduler/       # Daily failed-payment emails, weekly token cleanup
└── service/        # Business logic
```

## Testing the scheduled email manually

The failure-email job runs once a day by default. To test it without waiting:

1. Temporarily change the cron in `FailedPaymentScheduler.java` to `"*/30 * * * * *"` (every 30 seconds)
2. Generate a failed transaction (charge with `tok_chargeDeclined`)
3. Check your inbox within 30–60 seconds
4. Set the cron back to `"0 0 9 * * *"` when done

## Known limitations

This is an MVP, not a production system. Things I know are missing if this ever needed to handle real money:

- **Legacy Charges API, not PaymentIntents.** Works fine for test-mode simulation but doesn't support 3D Secure/SCA, which most real cards require. Migrating to PaymentIntents + Stripe.js (for client-side card tokenization) would be the real next step.
- **No webhooks.** Fine for a synchronous MVP, but a real integration needs them to handle async payment confirmations, disputes, and refunds.
- **Homegrown auth.** Single hardcoded admin, in-memory tokens with a fixed expiry, no Spring Security. Works for a demo, wouldn't want this guarding anything real.
- **Secrets in a properties file.** Should be environment variables in any real deployment.
- **No automated tests yet.**

## Why some of these design choices

- **Charges API instead of PaymentIntents**: kept things synchronous and avoided needing the Stripe CLI or handling webhook signatures — a deliberate simplification for a first Spring Boot project, not an oversight.
- **Stripe as the source of truth for customers, Postgres for transaction history**: charges made through this app were never reliably attached to Stripe customer records (an account-level restriction I hit early on), so transaction history has to come from the local ledger instead of Stripe's Charge API.
- **In-memory auth tokens**: fine for a single admin user; would need to move to persistent, revocable tokens before supporting more than one person logging in.
