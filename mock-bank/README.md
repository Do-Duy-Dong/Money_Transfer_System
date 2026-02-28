# Mock Bank API

A minimal, production-style **mock banking REST API** built with **Java Spring Boot 3.2.0**. It simulates a real bank's partner integration layer — allowing external services (such as [User Service](../user-service/)) to link accounts, perform deposits and withdrawals, and query transaction status via a secure, HMAC-signed protocol.

---

## Features

- **Partner Linking** — Issues a `token` and `secretKey` to a registered partner service.
- **Deposit** — Credits a bank account balance on behalf of a partner wallet.
- **Withdrawal** — Debits a bank account balance on behalf of a partner wallet.
- **HMAC-SHA256 Signature Verification** — Every transaction request is verified using a per-partner secret key to prevent tampering and replay attacks.
- **Idempotency** — Duplicate requests with the same `requestId` and type return the original result without reprocessing.
- **Callback** — Partners can query pending transaction status via the callback endpoint.
- **In-Memory Database** — Uses **H2** for zero-setup local development and testing.
- **Global Exception Handling** — Consistent error responses for all failure cases.

---

## Tech Stack

Technology                         
- Java 17                            
- Spring Boot 3.2.0                   
- H2 (in-memory)        
- Spring Boot Validation              
- Lombok                              

The API will be available at **`http://localhost:8081`** 

### H2 DB
- 3 bank account will be created whenever run project


