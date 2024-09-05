FROM maven:3-eclipse-temurin-17 AS build

# Set the working directory in the container
WORKDIR /app

# Copy relevant files
COPY pom.xml /app/
COPY src /app/src

# Build the application
RUN mvn clean install

FROM keycloak/keycloak:latest

COPY --from=build /app/target/*.jar /opt/keycloak/providers