package io.quarkus.code

import io.quarkus.code.model.CodeQuarkusExtension
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import io.quarkus.code.model.Config
import io.quarkus.code.model.QuarkusProject
import io.quarkus.code.services.CodeQuarkusConfigManager
import io.quarkus.code.services.QuarkusExtensionCatalog
import io.quarkus.code.services.QuarkusProjectCreator
import io.quarkus.runtime.StartupEvent
import org.eclipse.microprofile.openapi.annotations.Operation
import org.eclipse.microprofile.openapi.annotations.media.Content
import org.eclipse.microprofile.openapi.annotations.media.Schema
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.util.*
import javax.enterprise.event.Observes
import javax.inject.Inject
import javax.validation.Valid
import javax.ws.rs.BeanParam
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType.APPLICATION_JSON
import javax.ws.rs.core.Response
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.analytics.AnalyticsScopes
import java.io.File
import java.io.InputStreamReader
import java.util.*


@Path("/")
class CodeQuarkusResource {

    @Inject
    lateinit var configManager: CodeQuarkusConfigManager

    @Inject
    lateinit var extensionCatalog: QuarkusExtensionCatalog

    @Inject
    lateinit var projectCreator: QuarkusProjectCreator

    @GET
    @Path("/config")
    @Produces(APPLICATION_JSON)
    @Operation(summary = "Get the Quarkus Launcher configuration (DEPRECATED to '/v1/...')", hidden = true)
    fun config(): Config {
        return configManager.getConfig()
    }

    fun onStart(@Observes ev: StartupEvent) {
        val extensionsResource = CodeQuarkusResource::class.java.getResource("/quarkus/extensions.json")
                ?: throw IOException("missing extensions.json file")
        extensions = extensionsResource.readBytes()

        val inputStreamReader = InputStreamReader(CodeQuarkusResource::class.java.getResourceAsStream("/login.json"))
        val JSON_FACTORY = JacksonFactory()
        val DATA_STORE_DIR = File(System.getProperty("user.home"), ".store/analitics")
        val DATA_STORE_FACTORY = FileDataStoreFactory(DATA_STORE_DIR);
        val clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, inputStreamReader)
        val flow = GoogleAuthorizationCodeFlow.Builder(
                GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, clientSecrets,
                Collections.singleton(AnalyticsScopes.ANALYTICS_EDIT)).setDataStoreFactory(
                DATA_STORE_FACTORY).build()
        var credential = AuthorizationCodeInstalledApp(flow, LocalServerReceiver()).authorize("user")
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