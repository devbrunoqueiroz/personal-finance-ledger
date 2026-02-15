# ---------- BUILD STAGE ----------
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

# copia só o pom primeiro (cache de deps)
COPY pom.xml .
RUN mvn -q -e -B -DskipTests dependency:go-offline

# agora copia o resto
COPY src ./src

# build
RUN mvn -q -e -B -DskipTests package


# ---------- RUNTIME STAGE ----------
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]