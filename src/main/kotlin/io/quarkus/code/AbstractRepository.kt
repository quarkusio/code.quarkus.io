package io.quarkus.code


import io.quarkus.code.model.ShortUrl
import software.amazon.awssdk.services.dynamodb.model.*
import java.util.HashMap
import software.amazon.awssdk.services.dynamodb.model.AttributeValue


abstract class AbstractRepository {

    private val tableName: String
        get() = "shortUrls"

    protected fun scanRequest(): ScanRequest {
        return ScanRequest.builder().tableName(tableName)
                .attributesToGet(ID_COL, URL_COL).build()
    }

    protected fun putRequest(shortUrl: ShortUrl): PutItemRequest {
        val item = HashMap<String, AttributeValue>()
        item[ID_COL] = AttributeValue.builder().s(shortUrl.id).build()
        item[URL_COL] = AttributeValue.builder().s(shortUrl.url).build()

        return PutItemRequest.builder()
                .tableName(tableName)
                .item(item)
                .build()
    }

    protected fun queryRequest(url: String): ScanRequest {
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

    protected fun getRequest(name: String): GetItemRequest {
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
}