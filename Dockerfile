

# ---------- build stage ----------
FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace

# Copy Maven wrapper + pom first to leverage Docker layer caching
COPY mvnw pom.xml ./
COPY .mvn .mvn

# Pre-fetch dependencies (faster subsequent builds)
RUN ./mvnw -q -DskipTests dependency:go-offline

# Copy source and build
COPY src src
RUN ./mvnw -q -DskipTests package

# ---------- runtime stage ----------
FROM eclipse-temurin:25-jre
WORKDIR /app

# Copy the built jar (Spring Boot fat jar)
COPY --from=build /workspace/target/*.jar /app/app.jar

# Spring Boot default is 8080; you use /jobsearch as context-path in config
EXPOSE 8080

# Good container defaults (optional but helpful)
ENV JAVA_OPTS=""

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
