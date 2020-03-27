dev:
	mvn compile quarkus:dev

start-web:
	cd src/main/frontend && yarn && yarn start

dev-web:
	make -j2 dev start-web

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
