# calculator-devops

A Calculator REST API built with Spring Boot (Java 21, Maven) and delivered through an automated CI/CD pipeline: GitHub -> Jenkins -> Docker Hub -> Docker Compose (Nginx reverse proxy + API).

Course: CSE 816 Software Production Engineering, mini-project.

## API

| Operation | Request | Example response |
|---|---|---|
| Add | `GET /api/v1/add?a=2&b=3` | `{"operation":"add","a":2.0,"b":3.0,"result":5.0}` |
| Subtract | `GET /api/v1/subtract?a=5&b=3` | `{"operation":"subtract","a":5.0,"b":3.0,"result":2.0}` |
| Multiply | `GET /api/v1/multiply?a=6&b=7` | `{"operation":"multiply","a":6.0,"b":7.0,"result":42.0}` |
| Divide | `GET /api/v1/divide?a=4&b=2` | `{"operation":"divide","a":4.0,"b":2.0,"result":2.0}` |
| Health | `GET /actuator/health` | `{"groups":["liveness","readiness"],"status":"UP"}` |

Invalid or missing parameters, and division by zero, return HTTP 400 with a JSON error:

```json
{"status":400,"error":"Bad Request","message":"Division by zero is not allowed"}
```

## Prerequisites

- JDK 21
- Docker (with Docker Compose v2) for the container steps
- Maven is not required: the Maven Wrapper (`./mvnw`) is included

## Build, test, run locally

```bash
./mvnw clean package        # compile, run tests, build target/*.jar
./mvnw test                 # tests only (JUnit XML in target/surefire-reports/)
./mvnw spring-boot:run      # start on http://localhost:8080
curl "localhost:8080/api/v1/add?a=2&b=3"
```

The test suite has 10 tests: JUnit 5 unit tests for each operation (including divide by zero), MockMvc integration tests against the real endpoints, and a context-load test.

## Run with Docker

```bash
docker build -t calculator-devops:local .
docker run --rm -p 8081:8080 calculator-devops:local
curl "localhost:8081/api/v1/add?a=2&b=3"
```

The image is built in two stages (JDK builds the JAR, a JRE image runs it as a non-root user).
Published image: <https://hub.docker.com/r/mehraan13/calculator-devops>

## Run with Docker Compose

```bash
docker compose up -d --build
docker compose ps                       # app should be "healthy"
curl "localhost:8081/api/v1/multiply?a=6&b=7"
docker compose down
```

Compose starts two services on the network `calc-net`: the API (with a health check on `/actuator/health`) and an Nginx reverse proxy, which is the only service published on the host (port 8081).

## CI/CD pipeline

The `Jenkinsfile` defines a declarative pipeline, triggered by a GitHub webhook on push:

1. **Checkout**: pull the commit
2. **Build**: `./mvnw clean package -DskipTests`
3. **Test**: `./mvnw test`, publish JUnit results
4. **Docker Build**: image tagged with the build number and `latest`
5. **Push**: push to Docker Hub using the Jenkins credential `dockerhub-creds`
6. **Deploy**: `docker compose up -d`, then verify `/actuator/health`

Build status (success/failure) is emailed after every run. No credentials are stored in this repository.

Jenkins setup used: Pipeline script from SCM, a Docker Hub access token stored as a "Username with password" credential (ID `dockerhub-creds`), SMTP configured in Manage Jenkins, and a GitHub webhook pointing to `<jenkins-url>/github-webhook/`.

## Repository structure

```
calculator-devops/
├── src/main/java/com/mehraan/calculator/   # Spring Boot application
├── src/test/java/com/mehraan/calculator/   # JUnit and MockMvc tests
├── pom.xml
├── Dockerfile                              # multi-stage build
├── .dockerignore
├── docker-compose.yml                      # API + Nginx proxy
├── nginx.conf
├── Jenkinsfile
├── .gitignore
└── README.md
```