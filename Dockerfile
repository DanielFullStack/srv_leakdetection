# Stage 1: Build
FROM maven:3.8.4-openjdk-17 AS build
COPY src /usr/src/app/src
COPY pom.xml /usr/src/app
RUN mvn -f /usr/src/app/pom.xml clean package -DskipTests
RUN ls -la /usr/src/app/target

# Stage 2: Run
FROM openjdk:17-alpine
COPY --from=build /usr/src/app/target/srv_leakdetection-0.0.1-SNAPSHOT.jar /usr/app/srv_leakdetection.jar

WORKDIR /usr/app
ENTRYPOINT ["java", "-jar", "/usr/app/srv_leakdetection.jar"]