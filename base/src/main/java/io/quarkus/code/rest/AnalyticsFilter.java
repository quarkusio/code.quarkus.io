package io.quarkus.code.rest;

import io.quarkus.code.model.ProjectDefinition;
import io.quarkus.code.service.PlatformInfo;
import io.quarkus.code.service.PlatformService;
import io.quarkus.code.service.SegmentAnalyticsService;
import io.quarkus.maven.dependency.ArtifactCoords;
import io.quarkus.maven.dependency.ArtifactKey;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Provider;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Provider
public class AnalyticsFilter implements ContainerRequestFilter {

    private static final Logger LOG = Logger.getLogger(AnalyticsFilter.class.getName());

    @Inject
    private Instance<SegmentAnalyticsService> analyticsService;

    @Inject
    private Instance<PlatformService> platformService;

    @Context
    private HttpServerRequest httpServerRequest;

    @Context
    private UriInfo info;

    @Override
    public void filter(ContainerRequestContext context) {
        try {
            MultivaluedMap<String, String> queryParams = info.getQueryParameters();
            String path = info.getPath();
            String source = queryParams.getFirst("cn") != null ? queryParams.getFirst("cn")
                    : context.getHeaderString("Client-Name") != null ? context.getHeaderString("Client-Name") : "unknown";
            String url = info.getRequestUri().toString();
            String userAgent = context.getHeaders().getFirst(HttpHeaders.USER_AGENT);
            String referer = context.getHeaders().getFirst("Referer");
            String remoteAddr = httpServerRequest.remoteAddress() != null ? httpServerRequest.remoteAddress().hostAddress()
                    : null;
            String uuid = remoteAddr != null ? UUID.nameUUIDFromBytes(remoteAddr.getBytes()).toString()
                    : UUID.randomUUID().toString();
            String appAction = null;
            if (path.startsWith("/download")) {
                appAction = "App Download";
            } else if (path.startsWith("/github/project")) {
                appAction = "App Push to GitHub";
            }
            if (appAction != null) {
                try {
                    Map<String, Object> w = readWatchedData(context);

                    Set<String> extensions = (Set<String>) w.get("extensions");
                    if (extensions != null && extensions.size() < 20) {
                        for (String id : extensions) {
                            Map<String, Object> props;
                            if (id.split(":").length == 2) {
                                ArtifactKey key = ArtifactKey.fromString(id);
                                props = Map.of("extension", key.toGacString());
                            } else {
                                ArtifactCoords coords = ArtifactCoords.fromString(id);
                                props = Map.of("extension", coords.getKey().toGacString(), "extensionVersion",
                                        coords.getVersion());
                            }
                            Map<String, Object> trackMap = new HashMap<>(w);
                            trackMap.remove("extensions");
                            trackMap.putAll(props);
                            analyticsService.get()
                                    .track("Extension Used", trackMap, source, path, url, userAgent, referer, uuid);
                        }
                        analyticsService.get().track(appAction, w, source, path, url, userAgent, referer, uuid);
                    }
                } catch (IllegalStateException e) {
                    LOG.log(Level.FINE, "Error while extracting extension list from request", e);
                }
            }
            analyticsService.get().track("Api Call", Map.of(), source, path, url, userAgent, referer, uuid);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error while generating/sending an analytic event", e);
        }
    }

    private Map<String, Object> readWatchedData(ContainerRequestContext context) throws IOException {
        MultivaluedMap<String, String> queryParams = context.getUriInfo().getQueryParameters();
        Set<String> extensions;
        String buildTool;
        String streamKey;
        String javaVersion;
        boolean noCode;
        PlatformInfo recommendedPlatformInfo = platformService.get().recommendedPlatformInfo();
        if ("POST".equals(context.getMethod())) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(context.getEntityStream()));
            String text = reader.lines().collect(Collectors.joining(System.lineSeparator()));
            context.setEntityStream(new ByteArrayInputStream(text.getBytes()));
            if (!text.isBlank()) {
                JsonObject json = new JsonObject(text);
                Set<String> rawExtensions = json.getJsonArray("extensions", new JsonArray()).stream().map(Object::toString)
                        .collect(Collectors.toSet());
                extensions = recommendedPlatformInfo.checkAndMergeExtensions(rawExtensions);
                buildTool = json.getString("buildTool", ProjectDefinition.DEFAULT_BUILDTOOL);
                javaVersion = json.getString("javaVersion");
                streamKey = json.getString("streamKey");
                noCode = json.getBoolean("noCode", ProjectDefinition.DEFAULT_NO_CODE);
            } else {
                extensions = Collections.emptySet();
                buildTool = ProjectDefinition.DEFAULT_BUILDTOOL;
                streamKey = null;
                javaVersion = null;
                noCode = ProjectDefinition.DEFAULT_NO_CODE;
            }
        } else {
            extensions = recommendedPlatformInfo
                    .checkAndMergeExtensions(new HashSet<>(queryParams.getOrDefault("e", List.of())));
            buildTool = queryParams.containsKey("b") ? queryParams.getFirst("b") : ProjectDefinition.DEFAULT_BUILDTOOL;
            streamKey = queryParams.getFirst("S");
            javaVersion = queryParams.containsKey("j") ? queryParams.getFirst("j") : null;
            noCode = queryParams.containsKey("nc") ? Boolean.parseBoolean(queryParams.getFirst("nc"))
                    : ProjectDefinition.DEFAULT_NO_CODE;
        }
        String resolvedStreamKey = platformService.get().platformInfo(streamKey).stream().key();
        Map<String, Object> data = new HashMap<>();
        data.put("buildTool", buildTool);
        data.put("extensions", extensions);
        data.put("streamKey", resolvedStreamKey);
        data.put("javaVersion", javaVersion);
        data.put("noCode", noCode);
        return data;
    }
}