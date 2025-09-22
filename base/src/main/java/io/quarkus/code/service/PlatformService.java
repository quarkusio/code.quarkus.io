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
import java.util.regex.Pattern;

import io.quarkus.code.model.Preset;
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
import jakarta.enterprise.inject.Instance;
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

import static io.quarkus.code.misc.QuarkusExtensionUtils.processExtensions;
import static io.quarkus.devtools.project.JavaVersion.getCompatibleLTSVersions;
import static io.quarkus.platform.catalog.processor.CatalogProcessor.getMinimumJavaVersion;
import static io.quarkus.platform.catalog.processor.CatalogProcessor.getRecommendedJavaVersion;

@Singleton
public class PlatformService {

    public static final List<Preset> DEFAULT_PRESETS = List.of(
            // Some presets are duplicated to support platforms before and after the Big Reactive Renaming
            new Preset("rest-service", "REST service",
                    "https://raw.githubusercontent.com/quarkusio/code.quarkus.io/main/base/assets/icons/presets/rest.svg",
                    List.of("io.quarkus:quarkus-rest")),
            new Preset("rest-service", "REST service",
                    "https://raw.githubusercontent.com/quarkusio/code.quarkus.io/main/base/assets/icons/presets/rest.svg",
                    List.of("io.quarkus:quarkus-resteasy-reactive")),
            new Preset("db-service", "REST service with database",
                    "https://raw.githubusercontent.com/quarkusio/code.quarkus.io/main/base/assets/icons/presets/db-service.svg",
                    List.of("io.quarkus:quarkus-rest", "io.quarkus:quarkus-rest-jackson",
                            "io.quarkus:quarkus-hibernate-orm-panache", "io.quarkus:quarkus-jdbc-postgresql")),
            new Preset("db-service", "REST service with database",
                    "https://raw.githubusercontent.com/quarkusio/code.quarkus.io/main/base/assets/icons/presets/db-service.svg",
                    List.of("io.quarkus:quarkus-resteasy-reactive", "io.quarkus:quarkus-resteasy-reactive-jackson",
                            "io.quarkus:quarkus-hibernate-orm-panache", "io.quarkus:quarkus-jdbc-postgresql")),
            new Preset("event-driven-kafka", "Event driven service with Kafka",
                    "https://raw.githubusercontent.com/quarkusio/code.quarkus.io/main/base/assets/icons/presets/event-driven-kafka.svg",
                    List.of("io.quarkus:quarkus-messaging-kafka")),
            new Preset("event-driven-kafka", "Event driven service with Kafka",
                    "https://raw.githubusercontent.com/quarkusio/code.quarkus.io/main/base/assets/icons/presets/event-driven-kafka.svg",
                    List.of("io.quarkus:quarkus-smallrye-reactive-messaging-kafka")),
            new Preset("cli", "Command-line tool",
                    "https://raw.githubusercontent.com/quarkusio/code.quarkus.io/main/base/assets/icons/presets/cli.svg",
                    List.of("io.quarkus:quarkus-picocli")),
            new Preset("webapp-mvc", "Web app with Model-View-Controller",
                    "https://raw.githubusercontent.com/quarkusio/code.quarkus.io/main/base/assets/icons/presets/webapp-mvc.svg",
                    List.of("io.quarkiverse.renarde:quarkus-renarde", "io.quarkiverse.web-bundler:quarkus-web-bundler")),
            new Preset("webapp-npm", "Web app with NPM UI",
                    "https://raw.githubusercontent.com/quarkusio/code.quarkus.io/main/base/assets/icons/presets/webapp-npm.svg",
                    List.of("io.quarkus:quarkus-rest", "io.quarkus:quarkus-rest-jackson",
                            "io.quarkiverse.quinoa:quarkus-quinoa")),
            new Preset("webapp-npm", "Web app with NPM UI",
                    "https://raw.githubusercontent.com/quarkusio/code.quarkus.io/main/base/assets/icons/presets/webapp-npm.svg",
                    List.of("io.quarkus:quarkus-resteasy-reactive", "io.quarkus:quarkus-resteasy-reactive-jackson",
                            "io.quarkiverse.quinoa:quarkus-quinoa")),
            new Preset("webapp-qute", "Web app with ServerSide Rendering",
                    "https://raw.githubusercontent.com/quarkusio/code.quarkus.io/main/base/assets/icons/presets/webapp-qute.svg",
                    List.of("io.quarkiverse.qute.web:quarkus-qute-web", "io.quarkiverse.web-bundler:quarkus-web-bundler")),
            new Preset("ai-infused", "AI Infused service",
                    "https://raw.githubusercontent.com/quarkusio/code.quarkus.io/main/base/assets/icons/presets/ai-infused.svg",
                    List.of("io.quarkiverse.langchain4j:quarkus-langchain4j-openai",
                            "io.quarkiverse.langchain4j:quarkus-langchain4j-easy-rag")));
    public static final Pattern NUMERIC_PATTERN = Pattern.compile("\\d+");

    @Inject
    private PlatformConfig platformConfig;

    @Inject
    private QuarkusProjectService projectService;

    @Inject
    Instance<PlatformOverride> platformOverride;

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

    public List<Preset> presets() {
        return platformOverride.isResolvable() ? platformOverride.get().presets() : DEFAULT_PRESETS;
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

    private PlatformOverride getPlatformOverride() {
        return platformOverride.isResolvable() ? platformOverride.get() : PlatformOverride.DEFAULT_PLATFORM_OVERRIDE;
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
                List<CodeQuarkusExtension> codeQuarkusExtensions = processExtensions(extensionCatalog,
                        getPlatformOverride());
                String platformKey = platform.getPlatformKey();
                String streamId = stream.getId();
                String streamKey = createStreamKey(platformKey, streamId);
                boolean lts = (boolean) stream.getMetadata().get("lts");
                String minimumJavaVersion = getMinimumJavaVersion(extensionCatalog);

                SortedSet<Integer> compatibleJavaLTSVersions = getCompatibleLTSVersions(
                        new JavaVersion(minimumJavaVersion));
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
                if (!codeQuarkusExtensions.isEmpty()) {
                    streams.add(streamInfo);
                    updatedStreamCatalogMap.put(streamKey, platformInfo);
                } else {
                    LOG.warning("No extension found for streamKey: %s (skipping)".formatted(streamKey));
                }
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
                throw new IllegalStateException("No extension found in the stream: " + entry.getKey());
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

    static String getStreamStatus(String version) {
        for (String part : version.split("[.-]")) {
            if (part.equalsIgnoreCase("redhat")) {
                return "FINAL";
            }
            if (!part.isBlank() && !isNumeric(part)) {
                return part.toUpperCase();
            }
        }
        return "FINAL";
    }

    private static boolean isNumeric(String part) {
        return NUMERIC_PATTERN.matcher(part).matches();
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
