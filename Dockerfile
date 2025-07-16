# Stage 1: Build the React frontend
FROM public.ecr.aws/docker/library/node:16-alpine AS frontend-builder
WORKDIR /app/webapp-frontend
COPY webapp-frontend/package.json webapp-frontend/package-lock.json ./
RUN npm install
COPY webapp-frontend/ ./
RUN npm run build

# Stage 2: Build the Java backend
FROM public.ecr.aws/docker/library/maven:3.9.9-amazoncorretto-21 AS backend-builder
WORKDIR /app/webapp
COPY webapp/pom.xml .
RUN mvn dependency:go-offline
COPY webapp/src ./src
RUN mvn package -DskipTests

# Stage 3: Create the final image
# Changed the base image to a publicly available Amazon Corretto 21 Alpine JDK image
FROM public.ecr.aws/amazoncorretto/amazoncorretto:21-alpine
WORKDIR /app

# Copy the built backend JAR
COPY --from=backend-builder /app/webapp/target/*.jar app.jar

# Copy the built frontend static files into the location Spring Boot serves them from
COPY --from=frontend-builder /app/webapp-frontend/build /app/src/main/resources/static

# Expose the port the application runs on
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
