package io.quarkus.code

import io.quarkus.code.services.UrlRepositoryMock
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import io.restassured.config.RedirectConfig.redirectConfig
import io.restassured.config.RestAssuredConfig.newConfig
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import javax.inject.Inject

@QuarkusTest
class ShortUrlResourceTest {

    @Inject
    lateinit var urlRepository: UrlRepositoryMock

    @Test
    @DisplayName("Should return a short url for specified download parameters")
    fun testShortDownloadUrl() {
        given()
                .queryParam("g", "ch.nerdin.blog")
                .`when`().get("/s")
        .then()
                .statusCode(200)
        assertThat(urlRepository.shortUrls.isEmpty(), `is`(false))
        assertThat(urlRepository.shortUrls[0].url, `is`("https://code.quarkus.io/api/download?g=ch.nerdin.blog&a=code-with-quarkus&v=1.0.0-SNAPSHOT&c=org.acme.ExampleResource&e=[]"))
    }

    @Test
    @DisplayName("Should return the shortUrl for a given id")
    fun testShortUrlById() {
        given()
                .config(newConfig().redirect(redirectConfig().followRedirects(false)))
                .`when`().get("/s/${UrlRepositoryMock.ID}")
        .then()
                .statusCode(303)
                .header("location", `is`("http://blog.nerdin.ch/"))
    }
}