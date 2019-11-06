package io.quarkus.code

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoCollection
import io.quarkus.code.model.ShortUrl
import org.bson.Document
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject
import com.mongodb.client.model.Filters.eq

@ApplicationScoped
open class UrlRepository {
    @Inject
    lateinit var mongoClient: MongoClient

    open fun getById(id: String): ShortUrl? {
        return findByField("_id", id)
    }

    open fun getByUrl(url: String): ShortUrl? {
        return findByField("url", url)
    }

    open fun save(shortUrl: ShortUrl) {
        val document = Document()
                .append("_id", shortUrl.id)
                .append("url", shortUrl.url)
        getCollection().insertOne(document)
    }

    private fun getCollection(): MongoCollection<Document> {
        return mongoClient.getDatabase("shorten").getCollection("urls")
    }

    private fun findByField(field: String, value: String): ShortUrl? {
        val document = getCollection().find(eq(field, value)).first()
        document?.let {
            return ShortUrl(document["_id"].toString(), document["url"] as String)
        }
        return null
    }
}