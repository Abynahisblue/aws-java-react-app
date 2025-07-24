# Stage 1: Build React frontend
FROM public.ecr.aws/docker/library/node:16-alpine AS frontend-builder
WORKDIR /app/webapp-frontend
COPY webapp-frontend/package*.json ./
RUN npm install
COPY webapp-frontend/ ./
RUN npm run build

# Stage 2: Build Java backend
FROM public.ecr.aws/docker/library/maven:3.9.9-amazoncorretto-21 AS backend-builder
WORKDIR /app/webapp
COPY webapp/pom.xml .
RUN mvn dependency:go-offline
COPY webapp/src ./src
# Copy frontend build to backend's static resources location
COPY --from=frontend-builder /app/webapp-frontend/build ./src/main/resources/static
RUN mvn package -DskipTests

# Final image
FROM public.ecr.aws/amazoncorretto/amazoncorretto:21
WORKDIR /app
COPY --from=backend-builder /app/webapp/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]