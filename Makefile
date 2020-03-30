dev-backend:
	mvn compile quarkus:dev

dev-backend-only:
	mvn compile quarkus:dev -Pbackend-only

dev-web:
	cd src/main/frontend && yarn && yarn start

dev:
	make -j2 dev-backend-only dev-web

test-web:
	cd src/main/frontend && yarn && yarn test:i

update-web-snapshots:
	cd src/main/frontend && yarn && yarn test -u

debug:
	mvn compile quarkus:dev -Ddebug -Dsuspend

clean-all:
	mvn clean

clean:
	rm -rf ./target

native:
	mvn package -Pnative -DskipTests

ext-add:
	mvn quarkus:add-extension -Dextensions="$(ID)"

ext-list:
	mvn quarkus:list-extensions
