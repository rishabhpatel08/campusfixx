FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY backend/pom.xml .
RUN mvn dependency:go-offline -q
ARG CACHE_BUST=11
COPY backend/src ./src
RUN mvn clean package -DskipTests -q

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/campusfix-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["sh","-c","DB_JDBC=$(echo $DATABASE_URL | sed 's|postgres://|jdbc:postgresql://|'); java -Xms128m -Xmx400m -DSPRING_DATASOURCE_URL=$DB_JDBC -jar app.jar"]
