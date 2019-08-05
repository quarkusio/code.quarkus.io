package io.launcher.quarkus

import scala.concurrent.duration._
import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.language.postfixOps

class LauncherQuarkusSimulation extends Simulation {

  val httpProtocol = http
    .baseUrl("https://code.quarkus.io")
    .inferHtmlResources()
    .acceptEncodingHeader("gzip, deflate")
    .acceptLanguageHeader("fr-FR,fr;q=0.9,en-US;q=0.8,en;q=0.7")
    .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.100 Safari/537.36")


  val headers = Map(
    "Accept" -> "*/*",
    "Origin" -> "https://code.quarkus.io")

  val scn = scenario("LauncherQuarkusSimulation")
    // open-page
    .exec(http("index")
    .get("/")
    .headers(headers)
    .resources(http("request_3")
      .get("/static/css/2.f8c7b9a7.chunk.css")
      .headers(headers),
      http("request_4")
        .get("/static/css/main.0231ee40.chunk.css")
        .headers(headers),
      http("request_5")
        .get("/quarkus_icon_rgb_reverse.svg")
        .headers(headers),
      http("request_6")
        .get("/static/js/main.dbab949a.chunk.js")
        .headers(headers),
      http("request_7")
        .get("/static/js/2.8a086a4a.chunk.js")
        .headers(headers),
      http("request_8")
        .get("/api/quarkus/config")
        .headers(headers),
      http("request_9")
        .get("/quarkus_logo_horizontal_rgb_reverse.svg")
        .headers(headers),
      http("request_10")
        .get("/static/media/overpass-bold.d031db25.woff2")
        .headers(headers),
      http("request_11")
        .get("/quarkus_background_header.jpg")
        .headers(headers),
      http("request_12")
        .get("/static/media/overpass-regular.02d9e0ef.woff2")
        .headers(headers),
      http("request_13")
        .get("/static/media/overpass-light.c97e1959.woff2")
        .headers(headers),
      http("request_14")
        .get("/static/media/overpass-semibold.ca834120.woff2")
        .headers(headers),
      http("request_15")
        .get("/quarkus_background_main.jpg")
        .headers(headers)))
    .pause(5)
    // download-app
    .exec(http("download")
    .get("/api/quarkus/download?g=org.example&a=quarkus-app&v=0.0.1-SNAPSHOT&c=org.example.QuarkusApp&e=io.quarkus%3Aquarkus-resteasy&e=io.quarkus%3Aquarkus-undertow&e=io.quarkus%3Aquarkus-smallrye-openapi&e=io.quarkus%3Aquarkus-flyway&e=io.quarkus%3Aquarkus-hibernate-search-elasticsearch&e=io.quarkus%3Aquarkus-hibernate-validator&e=io.quarkus%3Aquarkus-jdbc-postgresql")
    .headers(headers))

  setUp(
    scn.inject(
      rampConcurrentUsers(10) to (50) during (2 minutes),
      constantConcurrentUsers(50) during (2 minutes),
      rampConcurrentUsers(50) to (400) during (2 minutes),
      constantConcurrentUsers(400) during (2 minutes),
    ).protocols(httpProtocol)
  )
  //setUp(scn.inject(constantUsersPerSec(4) during (1 minutes))).protocols(httpProtocol)
}