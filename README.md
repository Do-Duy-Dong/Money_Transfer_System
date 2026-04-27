E-Wallet
A production-style digital wallet REST API built with Java Spring Boot 3.4.0. Users can register accounts, link external bank accounts, perform OTP-verified deposits and withdrawals, and transfer funds between internal wallets. The service integrates with the Mock Bank API via HMAC-signed requests.

Features
JWT Authentication — Stateless login with access token + refresh token rotation.
Internal Fund Transfers — OTP-verified wallet-to-wallet transfers. Each transaction has signature, idempotency key,status. Use optimistic locking to prevent data race condition.
Deposit & Withdrawal — OTP-verified deposit from / withdrawal to a linked bank account.
Async Callback Handling — Receives transaction result callbacks from Mock Bank and updates transaction status.
Background Jobs (JobRunr) — Scheduled jobs to:
Auto-retry PENDING transactions every 30 minutes.
Schedule a 2-minute delayed retry for individual pending transactions.
Email Notifications
Redis Cache — OTP records and session data stored in Redis for fast lookup and TTL-based expiry.
Transaction History — Paginated view of all past transactions and statistics.
Tech Stack
Technology

Java 17
Spring Boot 3.4.0
PostgreSQL (Spring Data JPA / Hibernate)
Redis
Spring Security + JWT
JobRunr 7.2.0
Spring Retry + Spring AOP
Spring Boot Mail + Thymeleaf
Lombok, ModelMapper 3.1.1
Docker
Prerequisites
JDK 17+
Maven 3.8+
Docker & Docker Compose
A running instance of Mock Bank API (for deposit/withdraw features)
Mock Bank base URL
mockbank.base-url=http://localhost:8081

# Wallet base URL
wallet.base-url=http://localhost:8080
Integration with Mock Bank
This service communicates with the Mock Bank API for deposit and withdrawal operations using:

Partner Token — Issued by Mock Bank during bank-linking. Sent as X-Partner-Token header.
HMAC-SHA256 Signature — Computed from requestId + walletNo + bankNo + amount using a shared secret key. Sent as X-Signature header.
The signature ensures every transaction request is tamper-proof and cannot be replayed.
