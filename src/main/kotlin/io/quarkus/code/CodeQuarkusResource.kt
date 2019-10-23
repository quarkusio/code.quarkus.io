package io.quarkus.code

import io.quarkus.code.model.CodeQuarkusExtension
import io.quarkus.code.model.Config
import io.quarkus.code.model.QuarkusProject
import io.quarkus.code.services.CodeQuarkusConfigManager
import io.quarkus.code.services.QuarkusExtensionCatalog
import io.quarkus.code.services.QuarkusProjectCreator
import org.eclipse.microprofile.openapi.annotations.Operation
import org.eclipse.microprofile.openapi.annotations.media.Content
import org.eclipse.microprofile.openapi.annotations.media.Schema
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse
import javax.inject.Inject
import javax.json.bind.JsonbBuilder
import javax.validation.Valid
import javax.ws.rs.BeanParam
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType.APPLICATION_JSON
import javax.ws.rs.core.MediaType.TEXT_PLAIN
import javax.ws.rs.core.Response

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

@Path("/")
class CodeQuarkusResource {

    @Inject
    lateinit var configManager: CodeQuarkusConfigManager

    @Inject
    lateinit var extensionCatalog: QuarkusExtensionCatalog

    @Inject
    lateinit var projectCreator: QuarkusProjectCreator

    private val httpClient = OkHttpClient()

    @GET
    @Path("/config")
    @Produces(APPLICATION_JSON)
    @Operation(summary = "Get the Quarkus Launcher configuration (DEPRECATED to '/v1/...')", hidden = true)
    fun config(): Config {
        return configManager.getConfig()
    }


    @GET
    @Path("/extensions")
    @Produces(APPLICATION_JSON)
    @Operation(summary = "Get the Quarkus Launcher list of Quarkus extensions (DEPRECATED to '/v1/...')")
    @APIResponse(
            responseCode = "200",
            description = "List of Quarkus extensions",
            content = [Content(
                    mediaType = APPLICATION_JSON,
                    schema = Schema(implementation = CodeQuarkusExtension::class)
            )]
    )
    fun extensions(): List<CodeQuarkusExtension> {
        return extensionCatalog.extensions
    }

    @GET
    @Path("/shorten")
    @Produces(TEXT_PLAIN)
    @Operation(summary = "Create a short url based on the parameters")
    fun shorten(@Valid @BeanParam project: QuarkusProject): Response {
        val JSON = "application/json; charset=utf-8".toMediaType()
        val body = """
        {
                "group_guid": "$bitlyGroupId",
                "long_url": "https://code.quarkus.io/api/download?g=${project.groupId}&a=${project.artifactId}&v=${project.version}&c=${project.className}&e=${project.extensions}"
        }
        """.toRequestBody(JSON)

        val request = Request.Builder()
                .url("https://api-ssl.bitly.com/v4/shorten")
                .addHeader("Authorization", "Bearer $bitlyAccessToken")
                .post(body)
                .build()

        httpClient.newCall(request).execute().use { response ->

                if (!response.isSuccessful) throw IOException("Unexpected code $response")

                val jsonb = JsonbBuilder.create()
                val obj = jsonb.fromJson(response.body!!.string(), BitlyResponse::class.java)

                return Response.ok(obj.link).build()
        }
    }

    @GET
    @Path("/download")
    @Produces("application/zip")
    @Operation(summary = "Download a custom Quarkus application with the provided settings (DEPRECATED to '/v1/...')")
    fun download(@Valid @BeanParam project: QuarkusProject): Response {
        return Response
                .ok(projectCreator.create(project))
                .type("application/zip")
                .header("Content-Disposition", "attachment; filename=\"${project.artifactId}.zip\"")
                .build()
    }
}