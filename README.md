🧠 Architecture Overview – Payment Initiation System
The system is built with Clean Architecture and Domain-Driven Design (DDD) for maintainability, 
separation of concerns, and strict enforcement of business rules. I adopted the CQRS pattern 
to allow scalability and future flexibility—especially if reads are to be offloaded to a replica DB.

A custom Command Bus was implemented to decouple execution logic and avoid third-party CQRS libraries 
like Axon, which may pose future risks (e.g., licensing, vulnerabilities). This also enables support 
for cross-cutting concerns like validation, logging, retries, and eventual consistency via outbox 
patterns when needed.

🔐 Security 
Security was considered from the start:
Encryption was not included for simplicity requirement
Input validation prevents injection attacks and unnecessary processing.
NamedParameterJdbcTemplate ensures SQL injection protection.

📊 Data Model & Persistence
Entities are designed to be event-driven, incorporating domain events to notify relevant bounded 
contexts of changes. Database indexes are strategically applied for fast data access, balancing 
read performance with write efficiency. All tables are linked via merchant_id as a foreign key, 
with transaction and settlement tables sharing an additional relationship using batch id.

🧾 Endpoint Design Overview

🚀 /initiate-transaction
- Validates idempotency to avoid duplicate processing.
- Verifies merchant status and available balance.
- Locks amount in DB to prevent double spending.
- Suspends merchant after 5 insufficient fund attempts.
- Logs all transactions to the database (success/failure) for audit and retry purposes.
Note: The reason for handling locking amount logic on the object rather than the database was to reduce writes
and give room for easy modification of business rules 


📄 /merchant/transactions
- Implements pagination to reduce DB load.
- Validates date range and merchant existence.
- Logs queries and returns filtered, paginated results via DTO mappers.

💵 /settlements
- Validates merchant.
- Fetches unsettled successful transactions (limit applied).
- Groups by currency (warns on multiple).
- Computes total amount and fees.
- Creates a settlement batch and updates transaction states.
- Returns batch summary.

🛠️ Other Design Notes
- Tables are indexed where needed, carefully balancing read vs write performance.

🧩 Future Enhancements & Assumptions
- Encryption for payload security.
- Outbox pattern for retries and notifications.
- Background workers for settlements, refunds, and retries.
- Caching with Redis.
- Grafana/Prometheus for metrics
- Elasticsearch/Kibana for centralized logging
- Health checks and circuit breakers.
- Microservice Context: Assumes deployment within a microservice architecture, leveraging external components like:
  - Load balancers (e.g., Kubernetes) for scalability.
  - API Gateways for security (decryption, rate limiting) and routing.
  - Dedicated external notification and fund transfer services.
  - An external authentication service.
