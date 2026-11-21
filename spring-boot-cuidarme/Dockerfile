# compilação
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
# os testes rodam localmente (./mvnw test)
RUN mvn -B -q package -DskipTests

# execução
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
# credenciais e porta vêm das variáveis de ambiente do serviço de hospedagem:
# DB_URL, DB_USERNAME, DB_PASSWORD, PORT (opcional), MOSTRAR_SQL, CORS_ORIGENS
ENV MOSTRAR_SQL=false
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
