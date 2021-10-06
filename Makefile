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

build-lib:
	cd library && yarn && yarn build

test-lib:
	cd library && yarn test

tag-lib:
	cd library && yarn run tag

link-lib:
	cd library && yarn && yarn build && yarn run link;
	cd frontend && yarn run link-library;

unlink-lib:
	cd frontend && yarn && yarn run unlink-library;
	cd library && yarn && yarn run unlink;


dev-frontend:
	cd frontend && yarn && yarn start

watch-lib:
	make build-lib
	cd library && yarn run watch;

dev-lib:
	make -j2 watch-lib dev-frontend

test-frontend:
	cd frontend && yarn && yarn test

build-frontend:
	cd frontend && yarn && yarn build

start-frontend:
	cd frontend && yarn run run

docker-build-frontend:
	cd frontend && docker build -f docker/Dockerfile.multistage -t quay.io/quarkus/code-quarkus-frontend .

compose:
	docker-compose up

deploy-openshift:
	cd openshift && ./deploy.sh;



