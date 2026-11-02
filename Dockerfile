FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml ./
COPY src ./src
RUN mvn -B -DskipTests package
FROM eclipse-temurin:17-jre
WORKDIR /app
RUN groupadd --system talkspace && useradd --system --gid talkspace talkspace && mkdir -p logs images && chown -R talkspace:talkspace /app
COPY --from=build /build/target/talk_space-0.0.1-SNAPSHOT.jar app.jar
COPY --chown=talkspace:talkspace images/ ./images/
USER talkspace
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
