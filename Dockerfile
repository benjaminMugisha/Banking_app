FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app

COPY target/Banking_app-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
