# ===============================
# Stage 1: Build
# ===============================
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
RUN chmod +x mvnw

RUN ./mvnw dependency:go-offline -B

COPY src src
RUN ./mvnw package -DskipTests -B


# ===============================
# Stage 2: Runtime
# ===============================
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

ENV PORT=10000
ENV SPRING_PROFILES_ACTIVE=render

EXPOSE ${PORT}

ENTRYPOINT ["sh", "-c", "java -jar app.jar --spring.profiles.active=${SPRING_PROFILES_ACTIVE} --server.port=${PORT}"]