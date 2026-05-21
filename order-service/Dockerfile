FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /opt/app
COPY pom.xml .
COPY common ./common
COPY order-service/pom.xml ./order-service/pom.xml
COPY order-service/src ./order-service/src
RUN mvn clean package -P order-service -DskipTests

FROM eclipse-temurin:21-jre AS runner
WORKDIR /opt/app
COPY --from=builder /opt/app/order-service/target/order-service.jar .
ENTRYPOINT ["java", "-jar", "order-service.jar"]