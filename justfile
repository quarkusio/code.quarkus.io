alias deps := build-deps

build-deps:
  ./mvnw clean install -N;./mvnw clean install -f web-deps
dev:
  ./mvnw quarkus:dev -f base
build:
  ./mvnw clean install -Dlib -Dcommunity-app
start:
  java -jar community-app/target/quarkus-app/quarkus-run.jar