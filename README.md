Overview
This is a simple Spring Boot banking application that uses PostgreSQL as its database. The application is fully Dockerized
and can be run using Docker Compose.


## Getting Started

### Prerequisites
- Docker
- Docker Compose

### Building and Running the Application

1. Clone the repository:
   git clone git@github.com:benjaminMugisha/Banking_app.git
   
2. Navigate to the project directory:
   cd Banking-app

3. Build and start the application using Docker Compose:
   docker-compose up --build
   
4. Access the application at `http://localhost:8080`.


### Database Connection
This application uses PostgreSQL, and in the `docker-compose.yml` file, PostgreSQL is mapped to port `5433` on your local machine.
This means that while PostgreSQL inside the Docker container is running on the default port `5432`, it is exposed on port `5433` on the host machine.


### Changing the Port (Optional)
If port 5433 is already in use, you can modify the port mapping in the docker-compose.yml file:
ports:
- "your-new-port:5432"
  Then, update the connection string in application.properties:
spring.datasource.url=jdbc:postgresql://localhost:your-new-port/Banking


### Stopping the Application

To stop the containers, press `Ctrl+C` in the terminal where `docker-compose up` is running, or use:
docker-compose down
