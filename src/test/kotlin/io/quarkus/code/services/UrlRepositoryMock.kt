package io.quarkus.code.services

import io.quarkus.code.model.ShortUrl
import io.quarkus.test.Mock
import javax.inject.Singleton

@Mock
@Singleton
open class UrlRepositoryMock: UrlRepository() {
    val shortUrls = ArrayList<ShortUrl>()

    companion object {
        const val ID = "id123"
    }

    override fun getById(id: String): ShortUrl? {
        assert(id == ID)
        return ShortUrl(url = "http://blog.nerdin.ch/")
    }

    override fun getByUrl(url: String): ShortUrl? {
        return null
    }

    override fun save(shortUrl: ShortUrl): List<ShortUrl> {
        this.shortUrls.add(shortUrl)
        return emptyList()
    }
}