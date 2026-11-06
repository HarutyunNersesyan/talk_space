FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY . .
RUN mvn -B -DskipTests package
FROM eclipse-temurin:17-jre
WORKDIR /app
RUN groupadd --system talkspace && useradd --system --gid talkspace talkspace && mkdir -p logs images && chown -R talkspace:talkspace /app
COPY --from=build /build/talkspace-app/target/talkspace-app.jar app.jar
COPY --chown=talkspace:talkspace images/ ./images/
USER talkspace
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
