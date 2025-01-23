# Use an official OpenJDK runtime as a parent image
FROM openjdk:17-jdk-slim

# Set the working directory inside the container
WORKDIR /SpringBootApplication\ 3

# Copy the packaged JAR file into the container
# Replace 'target/my-app.jar' with the actual path to your JAR file
COPY target/SpringBootApplication-0.0.1-SNAPSHOT.jar SpringBootApplication-0.0.1-SNAPSHOT.jar

# Expose the port your Spring Boot application listens on
EXPOSE 8080

# Set the entry point to run the JAR file
ENTRYPOINT ["java", "-jar", "app.jar"]