package io.quarkus.code.rest;

import com.google.common.base.Strings;
import io.quarkus.code.config.CodeQuarkusConfig;
import io.quarkus.code.config.GitHubConfig;
import io.quarkus.code.config.SegmentConfig;
import io.quarkus.code.model.*;
import io.quarkus.code.service.PlatformInfo;
import io.quarkus.code.service.PlatformService;
import io.quarkus.code.service.QuarkusProjectService;
import io.quarkus.devtools.commands.data.QuarkusCommandException;
import io.quarkus.registry.catalog.PlatformCatalog;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.reactive.NoCache;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import static java.util.Collections.emptyList;
import static java.util.function.Predicate.not;

@Path("/")
public class CodeQuarkusResource {

    private static final Logger LOG = Logger.getLogger(CodeQuarkusResource.class.getName());
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss' GMT'");
    private static final String LAST_MODIFIED_HEADER = "Last-Modified";

    @Inject
    private CodeQuarkusConfig config;

    @Inject
    private SegmentConfig segmentConfig;

    @Inject
    private GitHubConfig gitHubConfig;

    @Inject
    private PlatformService platformService;

    @Inject
    private QuarkusProjectService projectCreator;

    @Inject
    private ManagedExecutor exec;

    @GET
    @Path("/config")
    @Produces(MediaType.APPLICATION_JSON)
    @NoCache
    @Operation(summary = "Get the Quarkus Launcher configuration", hidden = true)
    public Uni<PublicConfig> config() {
        PublicConfig publicConfig = new PublicConfig.Builder()
                .environment(config.environment().orElse("dev"))
                .segmentWriteKey(segmentConfig.writeKey().filter(not(Strings::isNullOrEmpty)).orElse(null))
                .sentryDSN(config.sentryFrontendDSN().filter(not(Strings::isNullOrEmpty)).orElse(null))
                .quarkusPlatformVersion(config.quarkusPlatformVersion().orElse("unknown"))
                .quarkusDevtoolsVersion(config.quarkusDevtoolsVersion().orElse("unknown"))
                .quarkusVersion(config.quarkusPlatformVersion().orElse("unknown"))
                .gitHubClientId(gitHubConfig.clientId().filter(not(Strings::isNullOrEmpty)).orElse(null))
                .gitCommitId(config.gitCommitId().orElse("unknown"))
                .build();
        return Uni.createFrom().item(publicConfig);
    }

    @GET
    @Path("/platforms")
    @Produces(MediaType.APPLICATION_JSON)
    @NoCache
    @Operation(summary = "Get all available platforms")
    @Tag(name = "Platform", description = "Platform related endpoints")
    @APIResponse(responseCode = "200", description = "All available platforms", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = PlatformCatalog.class)))
    public Uni<Response> platforms() {
        PlatformCatalog platformCatalog = platformService.platformCatalog();
        String lastUpdated = platformService.cacheLastUpdated().format(FORMATTER);
        Response response = Response.ok(platformCatalog)
                .header(LAST_MODIFIED_HEADER, lastUpdated)
                .build();
        return Uni.createFrom().item(response);
    }

    @GET
    @Path("/streams")
    @Produces(MediaType.APPLICATION_JSON)
    @NoCache
    @Operation(summary = "Get all available streams")
    @Tag(name = "Platform", description = "Platform related endpoints")
    @APIResponse(responseCode = "200", description = "All available streams", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = Stream.class, type = SchemaType.ARRAY)))
    public Uni<Response> streams() {
        List<Stream> streamKeys = platformService.streams();
        String lastUpdated = platformService.cacheLastUpdated().format(FORMATTER);
        Response response = Response.ok(streamKeys)
                .header(LAST_MODIFIED_HEADER, lastUpdated)
                .build();
        return Uni.createFrom().item(response);
    }

    @GET
    @Path("/extensions")
    @Produces(MediaType.APPLICATION_JSON)
    @NoCache
    @Operation(operationId = "extensions", summary = "Get the Quarkus Launcher list of Quarkus extensions")
    @Tag(name = "Extensions", description = "Extension related endpoints")
    @APIResponse(responseCode = "200", description = "List of Quarkus extensions", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CodeQuarkusExtension.class, type = SchemaType.ARRAY)))
    public Uni<Response> extensions(
            @QueryParam("platformOnly") @DefaultValue("true") boolean platformOnly,
            @QueryParam("id") String extensionId) {
        List<CodeQuarkusExtension> extensions = platformService.recommendedCodeQuarkusExtensions();
        return extensions(platformOnly, extensions, extensionId);
    }

    @GET
    @Path("/extensions/stream/{streamKey}")
    @Produces(MediaType.APPLICATION_JSON)
    @NoCache
    @Operation(operationId = "extensionsForStream", summary = "Get the Quarkus Launcher list of Quarkus extensions")
    @Tag(name = "Extensions", description = "Extension related endpoints")
    @APIResponse(responseCode = "200", description = "List of Quarkus extensions for a certain stream", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CodeQuarkusExtension.class, type = SchemaType.ARRAY)))
    public Uni<Response> extensionsForStream(
            @PathParam("streamKey") String streamKey,
            @QueryParam("platformOnly") @DefaultValue("true") boolean platformOnly,
            @QueryParam("id") String extensionId) {
        List<CodeQuarkusExtension> extensions = platformService.codeQuarkusExtensions(streamKey);
        return extensions(platformOnly, extensions, extensionId);
    }

    @GET
    @Path("/presets")
    @Produces(MediaType.APPLICATION_JSON)
    @NoCache
    @Operation(operationId = "presets", summary = "Get the Quarkus Launcher list of Presets")
    @Tag(name = "Presets", description = "Preset related endpoints")
    @APIResponse(responseCode = "200", description = "List of Presets", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = Preset.class, type = SchemaType.ARRAY)))
    public Uni<Response> presets() {
        return presets(platformService.recommendedPlatformInfo().extensionsById());
    }

    @GET
    @Path("/presets/stream/{streamKey}")
    @Produces(MediaType.APPLICATION_JSON)
    @NoCache
    @Operation(operationId = "presetsForStream", summary = "Get the Quarkus Launcher list of Presets")
    @Tag(name = "Presets", description = "Preset related endpoints")
    @APIResponse(responseCode = "200", description = "List of Presets for a certain stream", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = Preset.class, type = SchemaType.ARRAY)))
    public Uni<Response> presetsForStream(
            @PathParam("streamKey") String streamKey) {

        final Map<String, ExtensionRef> extensionsById = platformService.platformInfo(streamKey).extensionsById();
        return presets(extensionsById);
    }

    private Uni<Response> presets(Map<String, ExtensionRef> extensionsById) {
        String lastUpdated = platformService.cacheLastUpdated().format(FORMATTER);
        final List<Preset> presets = getAllPresets().stream()
                .filter(p -> p.extensions().stream().allMatch(extensionsById::containsKey))
                .toList();
        Response response = Response.ok(presets)
                .header(LAST_MODIFIED_HEADER, lastUpdated)
                .build();
        return Uni.createFrom().item(response);
    }

    List<Preset> getAllPresets() {

        List<Preset> presets = new ArrayList<>(config.useDefaultPresets() ? platformService.presets() : emptyList());

        config.customPresets().ifPresent(customConfigs -> customConfigs.stream()
                .map(pc -> new Preset(pc.key(), pc.title(), pc.icon(), pc.extensions()))
                .forEach(presets::add));

        return presets;
    }

    private Uni<Response> extensions(
            boolean platformOnly,
            List<CodeQuarkusExtension> extensions,
            String extensionId) {
        List<CodeQuarkusExtension> extensionsFiltered = extensions;
        if (platformOnly) {
            extensionsFiltered = extensionsFiltered.stream()
                    .filter(CodeQuarkusExtension::platform)
                    .collect(Collectors.toList());
        }
        if (extensionId != null) {
            extensionsFiltered = extensionsFiltered.stream()
                    .filter(extension -> extension.id().equals(extensionId))
                    .collect(Collectors.toList());
        }
        String lastUpdated = platformService.cacheLastUpdated().format(FORMATTER);
        Response response = Response.ok(extensionsFiltered)
                .header(LAST_MODIFIED_HEADER, lastUpdated)
                .build();
        return Uni.createFrom().item(response);
    }

    @POST
    @Path("/project")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Prepare a Quarkus application project to be downloaded")
    @Tag(name = "Project", description = "Project creation endpoints")
    public Uni<CreatedProject> project(@Valid ProjectDefinition projectDefinition) {
        List<NameValuePair> params = new ArrayList<>();
        if (projectDefinition != null) {
            if (projectDefinition.streamKey() != null) {
                params.add(new BasicNameValuePair("S", projectDefinition.streamKey()));
            }
            if (!ProjectDefinition.DEFAULT_GROUPID.equals(projectDefinition.groupId())) {
                params.add(new BasicNameValuePair("g", projectDefinition.groupId()));
            }
            if (!ProjectDefinition.DEFAULT_ARTIFACTID.equals(projectDefinition.artifactId())) {
                params.add(new BasicNameValuePair("a", projectDefinition.artifactId()));
            }
            if (!ProjectDefinition.DEFAULT_VERSION.equals(projectDefinition.version())) {
                params.add(new BasicNameValuePair("v", projectDefinition.version()));
            }
            if (!ProjectDefinition.DEFAULT_BUILDTOOL.equals(projectDefinition.buildTool())) {
                params.add(new BasicNameValuePair("b", projectDefinition.buildTool()));
            }
            if (projectDefinition.javaVersion() != null) {
                params.add(new BasicNameValuePair("j", projectDefinition.javaVersion().toString()));
            }
            if (projectDefinition.noCode() != ProjectDefinition.DEFAULT_NO_CODE
                    || projectDefinition.noExamples() != ProjectDefinition.DEFAULT_NO_CODE) {
                params.add(new BasicNameValuePair("nc", String.valueOf(!ProjectDefinition.DEFAULT_NO_CODE)));
            }
            if (!projectDefinition.extensions().isEmpty()) {
                projectDefinition.extensions().forEach(extension -> params.add(new BasicNameValuePair("e", extension)));
            }
            if (projectDefinition.path() != null) {
                params.add(new BasicNameValuePair("p", projectDefinition.path()));
            }
            if (projectDefinition.className() != null) {
                params.add(new BasicNameValuePair("c", projectDefinition.className()));
            }
        }
        String queryString = URLEncodedUtils.format(params, StandardCharsets.UTF_8);
        String path = params.isEmpty() ? "/d" : "/d?" + queryString;
        if (path.length() > 1900) {
            throw new BadRequestException(
                    Response.status(Response.Status.BAD_REQUEST)
                            .entity("The path is too long. Choose a sensible amount of extensions.")
                            .type(MediaType.TEXT_PLAIN)
                            .build());
        }
        return Uni.createFrom().item(new CreatedProject(path));
    }

    @GET
    @Path("/download")
    @Produces("application/zip")
    @Operation(operationId = "downloadForStream", summary = "Download a custom Quarkus application with the provided settings")
    @Tag(name = "Download", description = "Download endpoints")
    @Blocking
    public Response getDownload(@Valid @BeanParam ProjectDefinitionQuery query) throws IOException, QuarkusCommandException {
        return download(query.toProjectDefinition());
    }

    @POST
    @Path("/download")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces("application/zip")
    @Operation(summary = "Download a custom Quarkus application with the provided settings")
    @Tag(name = "Download", description = "Download endpoints")
    @Blocking
    public Response download(@Valid ProjectDefinition projectDefinition) throws IOException, QuarkusCommandException {
        ProjectDefinition p = projectDefinition != null ? projectDefinition : ProjectDefinition.of();
        PlatformInfo platformInfo = platformService.platformInfo(p.streamKey());
        return Response.ok(projectCreator.create(platformInfo, p))
                .type("application/zip")
                .header("Content-Disposition", "attachment; filename=\"" + p.artifactId() + ".zip\"")
                .build();
    }
}
