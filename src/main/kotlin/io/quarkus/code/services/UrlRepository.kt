package io.quarkus.code.services

import io.quarkus.code.model.ShortUrl
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest
import software.amazon.awssdk.services.dynamodb.model.ScanRequest
import java.util.HashMap
import java.util.stream.Collectors
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject


@ApplicationScoped
open class UrlRepository {
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
        dynamoDbClient.putItem(putRequest(shortUrl))
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

    private fun scanRequest(): ScanRequest {
        return ScanRequest.builder().tableName(tableName)
                .attributesToGet(ID_COL, URL_COL).build()
    }

    private fun putRequest(shortUrl: ShortUrl): PutItemRequest {
        val item = HashMap<String, AttributeValue>()
        item[ID_COL] = AttributeValue.builder().s(shortUrl.id).build()
        item[URL_COL] = AttributeValue.builder().s(shortUrl.url).build()

        return PutItemRequest.builder()
                .tableName(tableName)
                .item(item)
                .build()
    }

    private fun queryRequest(url: String): ScanRequest {
        val values = HashMap<String, AttributeValue>()
        values[":url"] = AttributeValue.builder().s(url).build()
        val attributesNames = HashMap<String, String>()
        attributesNames["#$URL_COL"] = URL_COL

        return ScanRequest.builder()
                .tableName(tableName)
                .filterExpression("#$URL_COL = :url")
                .expressionAttributeNames(attributesNames)
                .expressionAttributeValues(values)
                .build()
    }

    private fun getRequest(name: String): GetItemRequest {
        val key = HashMap<String, AttributeValue>()
        key[ID_COL] = AttributeValue.builder().s(name).build()

        return GetItemRequest.builder()
                .tableName(tableName)
                .key(key)
                .attributesToGet(ID_COL, URL_COL)
                .build()
    }

    companion object {
        const val ID_COL = "shortUrlId"
        const val URL_COL = "shortUrlUrl"
    }

    private val tableName: String
        get() = "shortUrls"
}