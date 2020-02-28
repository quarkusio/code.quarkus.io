dev:
	mvn compile quarkus:dev

start-web:
	cd src/main/frontend && yarn && yarn start

dev-web:
	make -j2 dev start-web

debug:
	mvn compile quarkus:dev -Ddebug -Dsuspend

clean:
	mvn clean

native:
	mvn package -Pnative -DskipTests

ext-add:
	mvn quarkus:add-extension -Dextensions="$(ID)"

ext-list:
	mvn quarkus:list-extensions
