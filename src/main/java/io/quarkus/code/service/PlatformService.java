package io.quarkus.code.service;

import io.quarkus.code.config.ExtensionProcessorConfig;
import io.quarkus.code.misc.QuarkusExtensionUtils;
import io.quarkus.code.model.CodeQuarkusExtension;
import io.quarkus.devtools.project.QuarkusProjectHelper;
import io.quarkus.registry.ExtensionCatalogResolver;
import io.quarkus.registry.RegistryResolutionException;
import io.quarkus.registry.catalog.ExtensionCatalog;
import io.quarkus.registry.catalog.Platform;
import io.quarkus.registry.catalog.PlatformCatalog;
import io.quarkus.registry.catalog.PlatformRelease;
import io.quarkus.registry.catalog.PlatformStream;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.scheduler.Scheduled;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;
import java.util.logging.Logger;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class PlatformService {
    private static final Logger LOG = Logger.getLogger(PlatformService.class.getName());

    @Inject
    public ExtensionProcessorConfig extensionProcessorConfig;

    private final ExtensionCatalogResolver catalogResolver = QuarkusProjectHelper.getCatalogResolver();
    public Map<String,List<CodeQuarkusExtension>> streamCatalogMap = new HashMap<>();
    public PlatformCatalog platformCatalog;
    public LocalDateTime lastUpdated;
    public static final String SEPARATOR = ":";

    public ExtensionProcessorConfig getExtensionProcessorConfig() {
        return this.extensionProcessorConfig;
    }

    public void setExtensionProcessorConfig(ExtensionProcessorConfig extensionProcessorConfig) {
        this.extensionProcessorConfig = extensionProcessorConfig;
    }

    public Map<String,List<CodeQuarkusExtension>> getStreamCatalogMap() {
        return this.streamCatalogMap;
    }

    public void setStreamCatalogMap(Map<String,List<CodeQuarkusExtension>> streamCatalogMap) {
        this.streamCatalogMap = streamCatalogMap;
    }

    public PlatformCatalog getPlatformCatalog() {
        return this.platformCatalog;
    }

    public void setPlatformCatalog(PlatformCatalog platformCatalog) {
        this.platformCatalog = platformCatalog;
    }

    public LocalDateTime getLastUpdated() {
        return this.lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public void onStart(@Observes StartupEvent e) {
        this.reloadCatalogs();
    }

    @Scheduled(cron = "{reload.catalogs.cron.expr}")
    public void reloadCatalogs() {
        try {
            this.platformCatalog = this.catalogResolver.resolvePlatformCatalog();
            populateExtensionCatalogMaps();
            this.lastUpdated = LocalDateTime.now((ZoneId)ZoneOffset.UTC);
        } catch (RegistryResolutionException e) {
            LOG.warning("Could not reload catalogs [" + e.getMessage() + "]");
        }

    }

    public List<CodeQuarkusExtension> getExtensionCatalog() {
        String defaultPlatformKey = this.getPlatformCatalog().getRecommendedPlatform().getPlatformKey();
        String defaultStreamId = this.getPlatformCatalog().getRecommendedPlatform().getRecommendedStream().getId();
        return this.getExtensionCatalog(defaultPlatformKey,defaultStreamId);
    }

    public List<CodeQuarkusExtension> getExtensionCatalog(String platformKey,String streamId){
        String key = createStreamKey(platformKey,streamId);
        return this.getExtensionCatalogForStream(key);
    }

    public List<CodeQuarkusExtension> getExtensionCatalogForStream(String stream) {
        if(this.streamCatalogMap.containsKey(stream)){
            return this.streamCatalogMap.get(stream);
        }
        return null;
    }

    public Set<String> getStreamKeys(){
        return this.streamCatalogMap.keySet();
    }

    private final void populateExtensionCatalogMaps() throws RegistryResolutionException {

        this.streamCatalogMap.clear();
        PlatformCatalog platformCatalog = this.getPlatformCatalog();
        Collection<Platform> platforms = platformCatalog.getPlatforms();

        for(Platform platform:platforms){
            for(PlatformStream stream:platform.getStreams()){
                // Stream Map
                PlatformRelease recommendedRelease = stream.getRecommendedRelease();
                ExtensionCatalog extensionCatalog = this.catalogResolver.resolveExtensionCatalog(recommendedRelease.getMemberBoms());
                List<CodeQuarkusExtension> codeQuarkusExtensions = QuarkusExtensionUtils.processExtensions(extensionCatalog, this.getExtensionProcessorConfig());
                String platformKey = platform.getPlatformKey();
                String streamId = stream.getId();
                String key = this.createStreamKey(platformKey, streamId);
                streamCatalogMap.put(key, codeQuarkusExtensions);
            }
        }
    }

    private final String createStreamKey(String platformKey, String streamId) {
        return platformKey + SEPARATOR + streamId;
    }
}
