# Stage 1: Build the Spring Boot app using Maven
FROM maven:3.9.9-eclipse-temurin-17 AS build


# Set working directory inside container
WORKDIR /build

# Copy only pom.xml first to cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the source code and build the jar
COPY src ./src
#RUN mvn clean package -DskipTests

# Stage 2: Run the app using JDK
FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy the jar from build stage
COPY target/jwt-0.0.1-SNAPSHOT.jar app.jar

# Expose port
EXPOSE 8080

# Command to run the app
CMD ["java", "-jar", "app.jar"]
