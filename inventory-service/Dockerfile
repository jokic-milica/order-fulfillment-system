FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /opt/app
COPY pom.xml .
COPY common ./common
COPY inventory-service/pom.xml ./inventory-service/pom.xml
COPY inventory-service/src ./inventory-service/src
RUN mvn clean package -P inventory-service -DskipTests

FROM eclipse-temurin:21-jre AS runner
WORKDIR /opt/app
COPY --from=builder /opt/app/inventory-service/target/inventory-service.jar .
ENTRYPOINT ["java", "-jar", "inventory-service.jar"]