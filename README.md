Banking Application 💳

A backend banking application with a RESTful API built using **Spring Boot**, containerized with **Docker**, deployed on **Kubernetes**, and provisioned with **Terraform** on AWS. 
Supports account creation, deposits, withdrawals, transfers, loans, direct debits and jwt user authentication. 

## Tech Stack

- **Backend:** Java + Spring Boot
- **Database:** PostgreSQL
- **API Security:** Spring Security + JWT
- **Infrastructure as Code:** Terraform
- **Containerization:** Docker
- **Orchestration:** Kubernetes (deployed on AWS EKS)
- **Hosting:** AWS (RDS, EKS, S3, etc.)
- **Testing:** JUnit 5(unit testing), Spring Boot Test(integration testing) and Mockito. 

## Features

- User registration and authentication (JWT)
- Account management: create, view and delete accounts. 
- Transactions: deposit, withdraw, transfer, 
- Direct debits: create and cancel.
- Loans: apply, delete, view, repay monthly or full amount, affordability check.
- Role-based access control(admin,user)
- Docker + Docker Compose for local development
- Fully automated infrastructure with Terraform
- Kubernetes deployment with production-ready manifests


## Getting Started

### Prerequisites
- Docker
- Docker Compose 
- Java 17+ and Maven (if running locally without Docker)
- AWS CLI & Terraform (for cloud provisioning) 
- Kubernetes CLI (kubectl)

### Building and Running the Application locally

1. Clone the repository:
   ` git clone git@github.com:benjaminMugisha/Banking_app.git `

2. ` cd Banking-app `

3. create a .env file:
   ` echo "POSTGRES_USER=postgres 
   POSTGRES_PASSWORD=00000
   POSTGRES_DB=banking" > .env `

4.  ` docker-compose up --build ` build and start the application using Docker Compose:

5. Access the application at ` http://localhost:8080/swagger-ui/index.html ` to view endpoints.

###  **curl examples**

**Register a User and generate a token**
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
-H "Content-Type: application/json" \
-d '{"firstName":"John","lastName":"Doe","email":"john@example.com","password":"password123"}'
```

**create an account**
```bash
curl -X POST http://localhost:8080/api/v1/account/create \
-H "Content-Type: application/json" \
-d '{"accountUsername":"john", "balance": 100}'
```

**get all accounts**
```bash
curl -X GET http://localhost:8080/api/v1/account/all -H "Authorization: Bearer <JWT_TOKEN>" 
```

### Running tests:
` ./mvnw clean test `

### Stopping the Application

Press Ctrl + C in your terminal, or run: ` docker compose down `


### Infrastructure on AWS(Terraform)

Commands:
  ` cd Terraform
   terraform init
   terraform apply -auto-approve `

Terraform provisions:
- VPC + Subnets 
- RDS (PostgreSQL)
- EKS Cluster 
- IAM Roles and Networking 
- Route 53 hosted zone 
- Internet Gateway
- Route Table
- NAT Gateway

to move state from local to s3 uncomment backend.tf then `terraform init` then `yes` when prompted. 


### Deploying on Kubernetes(EKS)

once EKS is ready, deploy your app:

1. `aws eks --region eu-west-1 update-kubeconfig --name my-eks-cluster`
2. `cd ..` 
3. `kubectl apply -f kubernetes/` 
4. `kubectl get svc` to view endpoint to use for testing in curl or postman

### Additional Concepts Implemented

- Global Exception Handling using @ControllerAdvice
- API Error Standardization with custom error objects
- validating request fields
- pagination
- logging

Note: For readability, not all methods include pagination, detailed logging, or granular permissions — but those are demonstrated in key features and would be scaled across in production.


### coming next:
- CI/CD
- React UI

📄 License

MIT License.
Copyright (c) 2025 Benjamin Mugisha

🔗 LinkedIn: https://www.linkedin.com/in/benjamin-mugisha-9b2397299/
🐳 Docker Hub: mugisha99benjamin 
📦 GitHub: github.com/benjaminMugisha
