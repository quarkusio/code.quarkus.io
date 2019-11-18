package io.quarkus.code

import io.quarkus.code.model.ShortUrl
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import java.util.stream.Collectors
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject


@ApplicationScoped
open class UrlRepository: AbstractRepository() {
    @Inject
    lateinit var dynamoDbClient: DynamoDbClient

    open fun getById(id: String): ShortUrl? {
        return from(dynamoDbClient.getItem(getRequest(id)).item())
    }

    open fun getByUrl(url: String): ShortUrl? {
        val items = dynamoDbClient.scan(queryRequest(url)).items()
        return if (items.isNotEmpty()) {
            from(items.first())
        } else {
            null
        }
    }

    open fun save(shortUrl: ShortUrl): List<ShortUrl> {
        dynamoDbClient.putItem(putRequest(shortUrl));
        return findAll()
    }

    fun findAll(): List<ShortUrl> {
        return dynamoDbClient.scanPaginator(scanRequest()).items().stream()
                .map(({ from(it) }))
        .collect(Collectors.toList())
    }

    fun from(item: Map<String, AttributeValue>?): ShortUrl {
        if (item != null && item.isNotEmpty()) {
            return ShortUrl((item[ID_COL] ?: error("invalid")).s(), (item[URL_COL] ?: error("invalid")).s())
        }
        error("invalid")
    }
}