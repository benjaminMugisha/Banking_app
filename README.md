Banking Application 💳

A banking application with a RESTful API built using Spring Boot,
containerized using Docker, deployed on Kubernetes (AWS EKS), and provisioned via Terraform on AWS EKS.

Supports full banking operations such as account creation, deposits, withdrawals, transfers, loans,
direct debits, transaction tracking and JWT based authentication.

## Tech Stack

- Backend: Java + Spring Boot
- Database: PostgreSQL
- API Security: Spring Security + JWT
- Infrastructure as Code: Terraform
- Containerization: Docker + Docker Compose
- Orchestration: Kubernetes (on AWS EKS)
- Cloud Hosting: AWS (EKS, RDS, EKS, S3, DynamoDB, etc.)
- Testing: JUnit 5, Spring Boot Test and Mockito.
- CICD: GitHub Actions.

## Features

- User registration and authentication (JWT)
- Account management: create, view and delete accounts.
- Transactions: deposit, withdraw, transfer,
- Direct debits: create and cancel.
- Loans: apply, delete, view, repay monthly or full amount, affordability checks.
- Role-based access control(admin,user)
- Docker + Docker Compose for local development
- Fully automated infrastructure via Terraform
- Kubernetes deployment with autoscaling
- Version control: Git&GitHub
- CICD pipeline using Github Actions to automatically test, Docker build and deploy.
- Global Exception handling with custom error responses.
- Pagination and logging.
- Docker Compose setup for local development


## Getting Started

### Prerequisites
- Docker & Docker Compose
- Java 17+ and Maven
- AWS CLI & Terraform (for cloud provisioning)
- Kubernetes CLI (kubectl)

### Building and Running the Application locally

1. Clone the repository:
   ` git clone git@github.com:benjaminMugisha/Banking_app.git && cd Banking_app `

2. create a .env file:
   ` echo "POSTGRES_USER=postgres
   POSTGRES_PASSWORD=00000
   POSTGRES_DB=banking" > .env `

3. build and start the application using Docker Compose:
   ` docker-compose up --build `

4. to view endpoints visit Swagger UI:
   ` http://localhost:8080/swagger-ui/index.html `

###  ** Sample CURL examples**

**Register a User and generate a token**
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
-H "Content-Type: application/json" \
-d '{"firstName":"John","lastName":"Doe","accountUsername":"johndoe12345",
"balance":"500","email":"john@example.com","password":"password123","role":"ADMIN"}'
```
will return a jwt token, plus your account details.

**Register another user.***
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
-H "Content-Type: application/json" \
-d '{"firstName":"Peter","lastName":"Pan","accountUsername":"peterpan123456",
"balance":"50","email":"peterpan@gmail.com","password":"password123","role":"USER"}'
```

**Get all accounts(Admin only).**
```bash
curl -X GET http://localhost:8080/api/v1/account/all \
 -H "Authorization: Bearer <JWT TOKEN>"
```

**Test transfer**
```bash
curl -X PATCH http://localhost:8080/api/v1/account/user/transfer \
 -H "Authorization: Bearer <JWT TOKEN>" \
 -H "Content-Type: application/json" \
 -d '{"fromAccountId":2, "toAccountId":1, "amount":1.5}'  
```

**View transaction history**
```bash
curl -X GET http://localhost:8080/api/v1/transactions/2 \
 -H "Authorization: Bearer <JWT TOKEN>"
```

### Running tests:
` ./mvnw clean test `

### Stopping the Application

Press Ctrl + C in your terminal, or run: ` docker compose down `



### Docker Image, Infrastructure on AWS(Terraform) and Deploying to Kubernetes(EKS):
This project uses GitHub Actions to automatically build the Docker image and push it to Dockerhub,
deploy the app to AWS EKS and Kubernetes.
No manual deployment steps are required, just push to Github and the CICD pipeline will handle everything.  
see .github/workflows for code and scripts.


### COMING NEXT:
- React UI

📄 License

MIT License.
Copyright (c) 2025 Benjamin Mugisha

🔗 [LinkedIn](https://www.linkedin.com/in/benjamin-mugisha-9b2397299/)
🐳 [Docker Hub](https://hub.docker.com/r/mugisha99benjamin/banking_app)
📦 [GitHub](https://github.com/benjaminMugisha/Banking_app)
