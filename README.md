Banking Application 💳

A full-stack banking application with a RESTful API built using Spring Boot.
backend is containerized using Docker, and frontend is deployed separately.

Frontend(Vercel):  
Backend(Railway): https://bankingapp-production-8d32.up.railway.app/

This project also includes IaC Infrastructure-as-Code provisioning for AWS EKS using
terraform and a CI/CD using Github Actions.

This web app simulates a real banking platform with authentication, account management, 
loans, recurring payments(Direct debits) and an administrator control panel.
it focuses on:
    - secure financial operations
    - transactions traceability
    - role-based access control
    - cloud-ready deployment

## Tech Stack

Backend:
  - Java 21 
  - Spring Boot 3
  - Spring Security + JWT
  - JPA/Hibernate
  - PostgreSQL

Frontend:
  - React(Vite)

Infrastructure & Devops:
  - Docker & Docker compose
  - Kubernetes(AWS EKS)
  - Terraform(dev & prod environments) see ./Terraform
  - Github Actions CI/CD see .github/workflows for scripts.

Cloud services AWS:
  - AWS EKS, AWS RDS, AWS S3, AWS DynamoDB, Route53 & Networking.

Testing:
 - Junit
 - Mockito
 - Spring Boot Test

Version control: Git&GitHub


## Core Features

- Auth: User registration & login with JWT + refresh token.
- Accounts: Auto created on registration, deposit, withdraw, transfer via IBAN, Balance tracking per transaction.
- Transactions: Complete history with timestamp, transaction type and resulting balance.
- Direct debits: create, update and cancel recurring payments between accounts.
- Loans: apply with affordability/DTI checks, monthly auto-repayments and early repayment.
- Admin: Create admins, view statistics and manages users, accounts, loans, transactions.
- Security: Role-based access control(USER/ADMIN), JWT filter chain.
- Global exception handling with custom error responses.
- Structured logging and pagination throughout.

## Getting Started locally

### Prerequisites
- Docker & Docker Compose
- Java 17+ and Maven

### Building and Running the Application locally

# (1. Clone the repository:)
   ` git clone git@github.com:benjaminMugisha/Banking_app.git && cd Banking_app `

# 2. create a .env file:
   ` echo "POSTGRES_USER=postgres
   POSTGRES_PASSWORD=00000
   POSTGRES_DB=banking" > .env `

# 3. build and start the application using Docker Compose:
   ` docker-compose up --build `

# 4. to view endpoints visit Swagger UI:
   ` http://localhost:8080/swagger-ui/index.html `

# 5. Run tests:
    ` ./mvnw clean test `

###  ** Sample CURL examples**
Below are example requests to test the API. 
Replace <JWT TOKEN> with the token received after registering or logging in.

**Register a User(returns JWT + refresh token + account details)**
```bash
curl -X POST http://localhost:8080/api/v2/auth/register \
-H "Content-Type: application/json" \
-d '{"firstName":"John","lastName":"Doe",
"balance":500,"email":"john@mybank.com",
"password":"Password123"}'
```
example response:
{
"refreshToken":"...",
"token":"...",
"accountUsername":"john@mybank.com",
"iban": "..."
}

**Login( automatic for user when you registered):**
```bash
curl -X POST http://localhost:8080/api/v2/auth/login \
-H "Content-Type: application/json" \
-d '{"email":"john@mybank.com","password":"Password123"}'
```

**setting up a direct debit**
You have to first Register another user:
```bash
curl -X POST http://localhost:8080/api/v2/auth/register \
-H "Content-Type: application/json" \
-d '{"firstName":"secondary","lastName":"user",
"balance":100,"email":"secondary@mybank.com",
"password":"Password123"}'
```
use the iban you receive from this registration down here:

**Direct Debit setup**
```bash
curl -X POST http://localhost:8080/api/v2/dd/create \
-H "Authorization: Bearer <JWT TOKEN>" \
 -H "Content-Type: application/json" \
 -d '{"toIban":"IBAN", "amount":10}' 
```

**Loan application**
```bash
curl -X POST http://localhost:8080/api/v2/loans/apply \
 -H "Authorization: Bearer <JWT TOKEN>" \
 -H "Content-Type: application/json" \
 -d '{"income":10000, "principal":100, "monthsToRepay":12}'  
```

**View your transaction history**
```bash
curl -X GET http://localhost:8080/api/v2/auth/me/transactions \
 -H "Authorization: Bearer <JWT TOKEN>"
```


**ADMIN ENDPOINTS**
A default admin is created on application startup
```bash
curl -X POST http://localhost:8080/api/v2/auth/login \
-H "Content-Type: application/json" \
-d '{"email":"root@mybank.com","password":"root123"}'
```
copy the returned JWT token

**create a new admin**
```bash
curl -X POST http://localhost:8080/api/v2/auth/create-admin \
-H "Content-Type: application/json" \
-H "Authorization: Bearer <JWT TOKEN"
-d '{
"firstName":"Admin","lastName":"User",
"balance":500,"email":"admin@mybank.com",
"password":"Password123"
}'
```
Only admins can create other admins. This endpoint does not return a JWT so
newly created admins must log in separately.

*bank stats like how many users, accounts, loans, etc...*
``` bash 
curl -X get http://localhost:8080/api/v2/admin/stats \
-H "Authorization: Bearer <JWT TOKEN>"
```

**Get all accounts.**
```bash
curl -X GET http://localhost:8080/api/v2/accounts/all \
 -H "Authorization: Bearer <JWT TOKEN>"
```

**view only active users**
``` bash 
curl -X get http://localhost:8080/api/v2/auth/stats \
-H "Authorization: Bearer <JWT TOKEN>"
```


### Running tests:
` ./mvnw clean test `

### Stopping the Application

Press Ctrl + C in your terminal, or run: ` docker compose down `


### Docker Image, Infrastructure on AWS(Terraform) and Deploying to Kubernetes(EKS):
full infrastructure provisioning for AWS EKS inside /Terraform.

Originally deployed via Github Actions CI/CD pipeline:
 - build Docker image
 - Push to dockerhub
 - Deploy to kubernetes

**This deployment path is currently disabled for cost reasons but remains fully reproducible**
see ./github/workflows.

📄 License
MIT License. Copyright (c) 2026 Benjamin Mugisha

📦 [GitHub](https://github.com/benjaminMugisha/Banking_app)
🔗 [LinkedIn](https://www.linkedin.com/in/benjamin-mugisha-9b2397299/)
🐳 [Docker Hub](https://hub.docker.com/r/mugisha99benjamin/banking_app)
