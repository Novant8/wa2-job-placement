# Use an official Gradle image to build the application
FROM gradle:jdk17 AS build

# Set the working directory in the container
WORKDIR /app

# Copy Gradle wrapper and properties to cache dependencies
COPY gradle /app/gradle
COPY gradlew /app/

# Copy the project files
COPY build.gradle.kts settings.gradle.kts /app/
COPY src /app/src

# Build the application
ENV SPRING_PROFILES_ACTIVE=docker
RUN ./gradlew clean build -x test

# Use an official OpenJDK runtime as a parent image
FROM eclipse-temurin:17

# Set the working directory in the container
WORKDIR /app

# Copy the jar files from the build stage
COPY --from=build /app/build/libs/*.jar app.jar

ENV SPRING_PROFILES_ACTIVE=docker

# Set the entry point to run the jar file
ENTRYPOINT ["java", "-jar", "/app/app.jar"]