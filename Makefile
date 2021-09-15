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
	cd frontend && yarn && yarn start

dev-lib:
	cd library && yarn && yarn run link;
	cd frontend && yarn && yarn run link-library;
	cd library && yarn run watch;

test-frontend:
	cd frontend && yarn && yarn test

build-frontend:
	cd frontend && yarn && yarn build

start-frontend:
	cd frontend && yarn run run

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



