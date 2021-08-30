dev-api:
	cd api && mvn compile quarkus:dev

test-api:
	cd api && mvn clean test -DskipITs

start-api:
	java -jar api/target/quarkus-app/quarkus-run.jar

it-api:
	cd api && mvn clean verify

build-api:
	cd api && mvn clean install -DskipTests

docker-build-api:
	cd api && docker build -f src/main/docker/Dockerfile.multistage -t quay.io/quarkus/code-quarkus-api .

dev-frontend:
	cd frontend && npm install && npm start

dev-lib:
	cd frontend && npm run link-library;
	cd library && npm run watch;

test-frontend:
	cd frontend && npm install && npm test

build-frontend:
	cd frontend && yarn && yarn build

start-frontend:
	cd frontend && npm run run

docker-build-frontend:
	cd frontend && docker build -f docker/Dockerfile.multistage -t quay.io/quarkus/code-quarkus-frontend .

dev:
	make -j3 dev-api dev-lib dev-frontend

build:
	make -j2 build-api build-frontend

start:
	make -j2 start-api start-frontend

compose:
	docker-compose up

deploy-openshift:
	cd openshift && ./deploy.sh;



