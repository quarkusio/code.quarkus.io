alias deps := build-deps

build-deps:
  ./mvnw clean install -N;./mvnw clean install -f web-deps
dev:
  ./mvnw quarkus:dev -f base
build:
  ./mvnw clean install -Dlib -Dcommunity-app -DskipTests
start:
  java -jar community-app/target/quarkus-app/quarkus-run.jar
generate-search-parser:
  npx peggy base/search-parser/search.pegjs --format es -o base/src/main/resources/web/lib/core/search/parser.js