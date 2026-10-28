# ---- Stage 1: build the JAR ----
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

# Copy only what dependency resolution needs, so this layer is cached
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw -q dependency:go-offline

# Copy source last, since it changes most often
COPY src src
RUN ./mvnw -q clean package -DskipTests

# ---- Stage 2: slim runtime ----
FROM eclipse-temurin:21-jre
RUN groupadd --system app && useradd --system --gid app app
WORKDIR /app
COPY --from=build /workspace/target/*.jar app.jar
USER app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]