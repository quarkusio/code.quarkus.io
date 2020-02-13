dev:
	./mvnw compile quarkus:dev

start-web:
	cd src/main/frontend && yarn start

dev-web:
	make -j2 dev start-web

debug:
	./mvnw compile quarkus:dev -Ddebug -Dsuspend

clean:
	./mvnw clean

native:
	./mvnw package -Pnative -DskipTests

ext-add:
	./mvnw quarkus:add-extension -Dextensions="$(ID)"

ext-list:
	./mvnw quarkus:list-extensions
