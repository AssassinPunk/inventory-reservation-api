# Packages the jar built by Gradle (./gradlew bootJar locally, ./gradlew build in CI)
FROM eclipse-temurin:21-jre
WORKDIR /app

# Don't run as root inside the container
RUN useradd --system --uid 1001 spring
USER spring

COPY build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]