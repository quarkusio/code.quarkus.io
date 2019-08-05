package io.quarkus.dup.cli.commands;

import io.quarkus.cli.commands.writer.ProjectWriter;
import io.quarkus.dup.generators.ProjectGeneratorRegistry;
import io.quarkus.dup.generators.rest.BasicRestProjectGenerator;
import io.quarkus.maven.utilities.MojoUtils;
import io.quarkus.maven.utilities.MojoUtils.*;
import io.quarkus.templates.BuildTool;
import io.quarkus.templates.SourceType;
import org.apache.maven.model.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

import static io.quarkus.dup.generators.ProjectGenerator.*;
import static io.quarkus.maven.utilities.MojoUtils.*;

/**
 * @author <a href="mailto:stalep@gmail.com">Ståle Pedersen</a>
 */
public class CreateProject {

    private static final String POM_PATH = "pom.xml";
    private ProjectWriter writer;
    private String groupId;
    private String artifactId;
    private String version = getPluginVersion();
    private SourceType sourceType = SourceType.JAVA;
    private BuildTool buildTool = BuildTool.MAVEN;
    private String className;

    private Model model;

    public CreateProject(final ProjectWriter writer) {
        this.writer = writer;
    }

    public CreateProject groupId(String groupId) {
        this.groupId = groupId;
        return this;
    }

    public CreateProject artifactId(String artifactId) {
        this.artifactId = artifactId;
        return this;
    }

    public CreateProject version(String version) {
        this.version = version;
        return this;
    }

    public CreateProject sourceType(SourceType sourceType) {
        this.sourceType = sourceType;
        return this;
    }

    public CreateProject className(String className) {
        this.className = className;
        return this;
    }

    public CreateProject buildTool(BuildTool buildTool) {
        this.buildTool = buildTool;
        return this;
    }

    public Model getModel() {
        return model;
    }

    public boolean doCreateProject(final Map<String, Object> context) throws IOException {
        if (!writer.init()) {
            return false;
        }

        MojoUtils.getAllProperties().forEach((k, v) -> context.put(k.replace("-", "_"), v));

        context.put(PROJECT_GROUP_ID, groupId);
        context.put(PROJECT_ARTIFACT_ID, artifactId);
        context.put(PROJECT_VERSION, version);
        context.put(QUARKUS_VERSION, getPluginVersion());
        context.put(SOURCE_TYPE, sourceType);
        context.put(ADDITIONAL_GITIGNORE_ENTRIES, buildTool.getGitIgnoreEntries());

        if (className != null) {
            className = sourceType.stripExtensionFrom(className);
            int idx = className.lastIndexOf('.');
            if (idx >= 0) {
                final String packageName = className.substring(0, idx);
                className = className.substring(idx + 1);
                context.put(PACKAGE_NAME, packageName);
            }
            context.put(CLASS_NAME, className);
        }

        ProjectGeneratorRegistry.get(BasicRestProjectGenerator.NAME).generate(writer, context);

        final byte[] pom = writer.getContent(POM_PATH);
        model = MojoUtils.readPom(new ByteArrayInputStream(pom));
        addVersionProperty(model);
        addBom(model);
        addMainPluginConfig(model);
        addNativeProfile(model);
        ByteArrayOutputStream pomOutputStream = new ByteArrayOutputStream();
        MojoUtils.write(model, pomOutputStream);
        writer.write(POM_PATH, pomOutputStream.toString("UTF-8"));

        return true;
    }

    private void addBom(Model model) {
        boolean hasBom = false;
        DependencyManagement dm = model.getDependencyManagement();
        if (dm == null) {
            dm = new DependencyManagement();
            model.setDependencyManagement(dm);
        } else {
            hasBom = dm.getDependencies().stream()
                    .anyMatch(d -> d.getGroupId().equals(getPluginGroupId()) &&
                            d.getArtifactId().equals(getBomArtifactId()));
        }

        if (!hasBom) {
            Dependency bom = new Dependency();
            bom.setGroupId(getPluginGroupId());
            bom.setArtifactId(getBomArtifactId());
            bom.setVersion(QUARKUS_VERSION_PROPERTY);
            bom.setType("pom");
            bom.setScope("import");

            dm.addDependency(bom);
        }
    }

    private void addNativeProfile(Model model) {
        final boolean match = model.getProfiles().stream().anyMatch(p -> p.getId().equals("native"));
        if (!match) {
            PluginExecution exec = new PluginExecution();
            exec.addGoal("native-image");
            exec.setConfiguration(configuration(new Element("enableHttpUrlHandler", "true")));

            Plugin plg = plugin(getPluginGroupId(), getPluginArtifactId(), QUARKUS_VERSION_PROPERTY);
            plg.addExecution(exec);

            BuildBase buildBase = new BuildBase();
            buildBase.addPlugin(plg);

            Profile profile = new Profile();
            profile.setId("native");
            profile.setBuild(buildBase);

            final Activation activation = new Activation();
            final ActivationProperty property = new ActivationProperty();
            property.setName("native");

            activation.setProperty(property);
            profile.setActivation(activation);
            model.addProfile(profile);
        }
    }

    private void addMainPluginConfig(Model model) {
        if (!hasPlugin(model)) {
            Build build = createBuildSectionIfRequired(model);
            Plugin plugin = plugin(getPluginGroupId(), getPluginArtifactId(), QUARKUS_VERSION_PROPERTY);
            if (isParentPom(model)) {
                addPluginManagementSection(model, plugin);
                //strip the quarkusVersion off
                plugin = plugin(getPluginGroupId(), getPluginArtifactId());
            }
            PluginExecution pluginExec = new PluginExecution();
            pluginExec.addGoal("build");
            plugin.addExecution(pluginExec);
            build.getPlugins().add(plugin);
        }
    }

    private boolean hasPlugin(final Model model) {
        List<Plugin> plugins = null;
        final Build build = model.getBuild();
        if (build != null) {
            if (isParentPom(model)) {
                final PluginManagement management = build.getPluginManagement();
                if (management != null) {
                    plugins = management.getPlugins();
                }
            } else {
                plugins = build.getPlugins();
            }
        }
        return plugins != null && build.getPlugins()
                .stream()
                .anyMatch(p -> p.getGroupId().equalsIgnoreCase(getPluginGroupId()) &&
                        p.getArtifactId().equalsIgnoreCase(getPluginArtifactId()));
    }

    private void addPluginManagementSection(Model model, Plugin plugin) {
        if (model.getBuild() != null && model.getBuild().getPluginManagement() != null) {
            if (model.getBuild().getPluginManagement().getPlugins() == null) {
                model.getBuild().getPluginManagement().setPlugins(new ArrayList<>());
            }
            model.getBuild().getPluginManagement().getPlugins().add(plugin);
        }
    }

    private Build createBuildSectionIfRequired(Model model) {
        Build build = model.getBuild();
        if (build == null) {
            build = new Build();
            model.setBuild(build);
        }
        if (build.getPlugins() == null) {
            build.setPlugins(new ArrayList<>());
        }
        return build;
    }

    private void addVersionProperty(Model model) {
        Properties properties = model.getProperties();
        if (properties == null) {
            properties = new Properties();
            model.setProperties(properties);
        }
        properties.putIfAbsent("quarkus.version", getPluginVersion());
    }

    private boolean isParentPom(Model model) {
        return "pom".equals(model.getPackaging());
    }

    public static SourceType determineSourceType(Set<String> extensions) {
        return extensions.stream().anyMatch(e -> e.toLowerCase().contains("kotlin"))
                ? SourceType.KOTLIN
                : SourceType.JAVA;
    }
}
