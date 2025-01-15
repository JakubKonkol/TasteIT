#FROM openjdk:21
#LABEL authors="jakubkonkol"
#
#WORKDIR /app
#
#COPY .mvn/ .mvn
#COPY mvnw pom.xml ./
#RUN chmod +x mvnw
#RUN ./mvnw dependency:go-offline
#
#COPY src ./src
#
#RUN ./mvnw package -DskipTests
#
#CMD ["./mvnw", "spring-boot:run"]

FROM eclipse-temurin:21-jdk-alpine as build
WORKDIR /app

# Dodajemy potrzebne narzędzia
RUN apk add --no-cache dos2unix

# Kopiujemy pliki Maven wrapper
COPY mvnw .
COPY .mvn .mvn
# Konwertujemy końce linii i nadajemy uprawnienia
RUN dos2unix mvnw && chmod +x mvnw

# Kopiujemy pom.xml i pobieramy zależności
COPY pom.xml .
RUN ./mvnw dependency:go-offline

# Kopiujemy kod źródłowy
COPY src src

# Budujemy aplikację
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]