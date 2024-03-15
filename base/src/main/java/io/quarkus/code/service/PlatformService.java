package io.quarkus.code.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import io.quarkus.code.model.ProjectDefinition;
import io.quarkus.code.model.Stream;
import io.quarkus.devtools.commands.data.QuarkusCommandException;
import io.quarkus.devtools.project.JavaVersion;
import io.quarkus.devtools.project.QuarkusProjectHelper;
import io.quarkus.logging.Log;
import io.quarkus.registry.Constants;
import io.quarkus.registry.ExtensionCatalogResolver;
import io.quarkus.registry.catalog.PlatformRelease;
import io.quarkus.runtime.LaunchMode;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import io.quarkus.code.config.PlatformConfig;
import io.quarkus.code.model.CodeQuarkusExtension;
import io.quarkus.registry.RegistryResolutionException;
import io.quarkus.registry.catalog.ExtensionCatalog;
import io.quarkus.registry.catalog.Platform;
import io.quarkus.registry.catalog.PlatformCatalog;
import io.quarkus.registry.catalog.PlatformStream;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.scheduler.Scheduled;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;

import static io.quarkus.code.misc.QuarkusExtensionUtils.processExtensions;
import static io.quarkus.devtools.project.JavaVersion.getCompatibleLTSVersions;
import static io.quarkus.platform.catalog.processor.CatalogProcessor.getMinimumJavaVersion;
import static io.quarkus.platform.catalog.processor.CatalogProcessor.getRecommendedJavaVersion;

@Singleton
public class PlatformService {

    @Inject
    private PlatformConfig platformConfig;

    @Inject
    private QuarkusProjectService projectService;

    private final ExtensionCatalogResolver catalogResolver;
    private final AtomicReference<PlatformServiceCache> platformServiceCacheRef = new AtomicReference<>();

    public void onStart(@Observes StartupEvent e) {
        reload();
    }

    public PlatformService() throws RegistryResolutionException {
        this.catalogResolver = QuarkusProjectHelper.getCatalogResolver();
    }

    @Scheduled(cron = "{io.quarkus.code.quarkus-platforms.reload-cron-expr}")
    public void reload() {
        try {
            reloadPlatformServiceCache();
        } catch (RegistryResolutionException e) {
            Log.warnf(e, "Could not resolve catalogs [%s]", e.getLocalizedMessage());
        } catch (Exception e) {
            Log.warnf(e, "Could not reload catalogs [%s]", e.getLocalizedMessage());
        }
    }

    public boolean isLoaded() {
        return platformServiceCacheRef.get() != null && !recommendedCodeQuarkusExtensions().isEmpty();
    }

    public PlatformServiceCache platformsCache() {
        PlatformServiceCache cache = platformServiceCacheRef.get();
        if (cache == null) {
            throw new IllegalStateException("Platforms cache must not be used if not loaded");
        }
        return cache;
    }

    public PlatformInfo recommendedPlatformInfo() {
        return platformInfo(null);
    }

    public LocalDateTime cacheLastUpdated() {
        return platformsCache().cacheLastUpdated();
    }

    public PlatformCatalog platformCatalog() {
        return platformsCache().platformCatalog();
    }

    public List<CodeQuarkusExtension> recommendedCodeQuarkusExtensions() {
        return codeQuarkusExtensions(recommendedStreamKey());
    }

    public String recommendedStreamKey() {
        return platformsCache().recommendedStreamKey();
    }

    public List<Stream> streams() {
        return platformServiceCacheRef.get().streams();
    }

    public Set<String> streamKeys() {
        return platformServiceCacheRef.get().streamCatalogMap().keySet();
    }

    public List<CodeQuarkusExtension> codeQuarkusExtensions(String platformKey, String streamId) {
        String key = createStreamKey(platformKey, streamId);
        return codeQuarkusExtensions(key);
    }

    public List<CodeQuarkusExtension> codeQuarkusExtensions(String streamKey) {
        return platformInfo(streamKey).codeQuarkusExtensions();
    }

    public PlatformInfo platformInfo(String platformKey, String streamId) {
        String key = createStreamKey(platformKey, streamId);
        return platformInfo(key);
    }

    public PlatformInfo platformInfo(String streamKey) {
        String normalizedStreamKey = normalizeStreamKey(streamKey);
        Map<String, PlatformInfo> streamCatalogMap = platformServiceCacheRef.get().streamCatalogMap();
        if (streamCatalogMap.containsKey(normalizedStreamKey)) {
            return streamCatalogMap.get(normalizedStreamKey);
        } else {
            throw new IllegalArgumentException("Invalid streamKey: " + streamKey);
        }
    }

    private void reloadPlatformServiceCache() throws RegistryResolutionException, IOException, QuarkusCommandException {
        catalogResolver.clearRegistryCache();
        PlatformCatalog platformCatalog;
        if (platformConfig.getRegistryId().isEmpty()) {
            platformCatalog = catalogResolver.resolvePlatformCatalog();
        } else {
            platformCatalog = catalogResolver.resolvePlatformCatalogFromRegistry(platformConfig.getRegistryId().get());
        }
        Map<String, PlatformInfo> updatedStreamCatalogMap = new HashMap<>();
        if (platformCatalog == null || platformCatalog.getMetadata() == null
                || platformCatalog.getPlatforms() == null) {
            throw new RuntimeException("Platform catalog not found");
        }

        String platformTimestamp = platformCatalog.getMetadata().get(Constants.LAST_UPDATED).toString();
        if (platformTimestamp.isBlank()) {
            throw new RuntimeException("Platform last updated date is empty");
        }
        if (platformServiceCacheRef.get() != null
                && platformServiceCacheRef.get().platformTimestamp().equals(platformTimestamp)) {
            LOG.log(Level.INFO, "The platform cache is up to date with the registry");
            return;
        }
        Collection<Platform> platforms = platformCatalog.getPlatforms();
        List<Stream> streams = new ArrayList<>();
        for (Platform platform : platforms) {
            for (PlatformStream stream : platform.getStreams()) {
                PlatformRelease recommendedRelease = stream.getRecommendedRelease();
                ExtensionCatalog extensionCatalog = catalogResolver
                        .resolveExtensionCatalog(recommendedRelease.getMemberBoms());
                List<CodeQuarkusExtension> codeQuarkusExtensions = processExtensions(extensionCatalog);
                String platformKey = platform.getPlatformKey();
                String streamId = stream.getId();
                String streamKey = createStreamKey(platformKey, streamId);
                boolean lts = (boolean) stream.getMetadata().get("lts");
                String minimumJavaVersion = getMinimumJavaVersion(extensionCatalog);

                SortedSet<Integer> compatibleJavaLTSVersions = getCompatibleLTSVersions(
                        new JavaVersion(minimumJavaVersion));
                if (platformKey.equals("com.redhat.quarkus.platform")) {
                    // Hack to remove 21 support from code.quarkus.redhat.com
                    compatibleJavaLTSVersions.remove(21);
                }
                int recommendedJavaVersion = Optional.ofNullable(getRecommendedJavaVersion(extensionCatalog))
                        .map(Integer::parseInt).orElse(compatibleJavaLTSVersions.stream().findFirst().orElseThrow());
                String quarkusCoreVersion = stream.getRecommendedRelease().getQuarkusCoreVersion();
                boolean recommended = stream.getId().equals(platform.getRecommendedStream().getId());
                final String platformVersion = stream.getRecommendedRelease().getVersion().toString();
                Stream streamInfo = Stream.builder()
                        .key(streamKey)
                        .quarkusCoreVersion(quarkusCoreVersion)
                        .javaCompatibility(
                                new Stream.JavaCompatibility(compatibleJavaLTSVersions, recommendedJavaVersion))
                        .lts(lts)
                        .platformVersion(platformVersion)
                        .recommended(recommended)
                        .status(getStreamStatus(quarkusCoreVersion))
                        .build();
                PlatformInfo platformInfo = new PlatformInfo(
                        platformKey,
                        streamInfo,
                        quarkusCoreVersion,
                        platformVersion,
                        recommended,
                        codeQuarkusExtensions,
                        extensionCatalog);
                streams.add(streamInfo);
                updatedStreamCatalogMap.put(streamKey, platformInfo);
            }
        }
        PlatformServiceCache newCache = new PlatformServiceCache(
                createStreamKey(
                        platformCatalog.getRecommendedPlatform().getPlatformKey(),
                        platformCatalog.getRecommendedPlatform().getRecommendedStream().getId()),
                streams,
                platformCatalog,
                updatedStreamCatalogMap,
                LocalDateTime.now(ZoneOffset.UTC),
                platformTimestamp);

        checkNewCache(newCache);

        platformServiceCacheRef.set(newCache);
        Log.infof("""
                PlatformService cache has been reloaded successfully:
                platform timestamp: %s
                recommended stream key: %s (core: %s, platform: %s)
                recommended stream extensions: %d
                available streams: %s
                """.stripIndent(),
                platformTimestamp,
                recommendedStreamKey(),
                recommendedPlatformInfo().quarkusCoreVersion(),
                recommendedPlatformInfo().platformVersion(),
                recommendedCodeQuarkusExtensions().size(),
                String.join(", ", updatedStreamCatalogMap.keySet()));
    }

    private void checkNewCache(PlatformServiceCache newCache) throws IOException, QuarkusCommandException {
        // Only replace the existing values if we successfully fetched new values
        if (newCache.streamCatalogMap().isEmpty()) {
            throw new RuntimeException("No stream found");
        }

        // Check streams
        if (!newCache.streamCatalogMap().containsKey(newCache.recommendedStreamKey())) {
            throw new RuntimeException(
                    "Recommended stream not found in stream catalog: " + newCache.recommendedStreamKey());
        }

        if (LaunchMode.current().isDevOrTest()) {
            // skip the rest of the checks in dev/test mode
            return;
        }

        for (Map.Entry<String, PlatformInfo> entry : newCache.streamCatalogMap().entrySet()) {
            if (entry.getValue().codeQuarkusExtensions().isEmpty()) {
                throw new RuntimeException("No extension found in the stream: " + entry.getKey());
            }
            projectService.createTmp(
                    entry.getValue(),
                    ProjectDefinition.builder().streamKey(entry.getKey())
                            .extensions(Set.of("resteasy", "resteasy-jackson", "hibernate-validator")).build(),
                    false,
                    true);

            Set<String> extensions = entry.getValue().extensionsById().containsKey("io.quarkus:quarkus-rest")
                    ? Set.of("rest", "rest-jackson", "hibernate-validator")
                    : Set.of("resteasy-reactive", "resteasy-reactive-jackson", "hibernate-validator");

            projectService.createTmp(
                    entry.getValue(),
                    ProjectDefinition.builder().streamKey(entry.getKey())
                            .extensions(extensions)
                            .build(),
                    false,
                    true);
            projectService.createTmp(
                    entry.getValue(),
                    ProjectDefinition.builder().streamKey(entry.getKey()).extensions(Set.of("spring-web")).build(),
                    false,
                    true);
        }
    }

    private String getStreamStatus(String quarkusCoreVersion) {
        String qualifier = new DefaultArtifactVersion(quarkusCoreVersion).getQualifier();
        return qualifier == null || qualifier.isBlank() ? "FINAL" : qualifier.toUpperCase();
    }

    private String createStreamKey(String platformKey, String streamId) {
        return platformKey + SEPARATOR + streamId;
    }

    private String normalizeStreamKey(String streamKey) {
        if (streamKey == null) {
            return recommendedStreamKey();
        }
        return streamKey.contains(":") ? streamKey
                : createStreamKey(recommendedPlatformInfo().platformKey(), streamKey);
    }

    private static final Logger LOG = Logger.getLogger(PlatformService.class.getName());
    private static final String SEPARATOR = ":";

    public record PlatformServiceCache(
            String recommendedStreamKey,
            List<Stream> streams,
            PlatformCatalog platformCatalog,
            Map<String, PlatformInfo> streamCatalogMap,
            LocalDateTime cacheLastUpdated,
            String platformTimestamp) {
    }
}