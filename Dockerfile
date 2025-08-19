# Production runtime for Spring Boot (Java 17)
FROM eclipse-temurin:17-jre-alpine

# Create non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring

WORKDIR /app

# Copy built jar from Gradle output (placed by CI step './gradlew bootJar')
# If multiple jars exist, the newest will be chosen at build time by the builder.
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} /app/app.jar

EXPOSE 8080
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]