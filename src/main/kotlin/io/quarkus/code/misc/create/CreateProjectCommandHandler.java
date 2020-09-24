package io.quarkus.code.misc.create;

import io.quarkus.bootstrap.model.AppArtifactCoords;
import io.quarkus.bootstrap.model.AppArtifactKey;
import io.quarkus.devtools.codestarts.Codestart;
import io.quarkus.devtools.codestarts.CodestartProject;
import io.quarkus.devtools.codestarts.CodestartSpec;
import io.quarkus.devtools.codestarts.Codestarts;
import io.quarkus.devtools.codestarts.QuarkusCodestartData.LegacySupport;
import io.quarkus.devtools.codestarts.QuarkusCodestartInput;
import io.quarkus.devtools.commands.data.QuarkusCommandException;
import io.quarkus.devtools.commands.data.QuarkusCommandInvocation;
import io.quarkus.devtools.commands.data.QuarkusCommandOutcome;
import io.quarkus.devtools.commands.handlers.QuarkusCommandHandler;
import io.quarkus.devtools.project.codegen.ProjectGenerator;
import io.quarkus.platform.descriptor.QuarkusPlatformDescriptor;
import io.quarkus.platform.tools.ToolsUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import static io.quarkus.code.misc.create.CreateProject.CODESTARTS;
import static io.quarkus.code.misc.create.QuarkusCommandHandlers.computeCoordsFromQuery;
import static io.quarkus.devtools.codestarts.QuarkusCodestarts.prepareProject;
import static io.quarkus.devtools.commands.CreateProject.NO_BUILDTOOL_WRAPPER;
import static io.quarkus.devtools.commands.CreateProject.NO_DOCKERFILES;
import static io.quarkus.devtools.commands.CreateProject.NO_EXAMPLES;
import static io.quarkus.devtools.project.codegen.ProjectGenerator.BOM_ARTIFACT_ID;
import static io.quarkus.devtools.project.codegen.ProjectGenerator.BOM_GROUP_ID;
import static io.quarkus.devtools.project.codegen.ProjectGenerator.BOM_VERSION;
import static io.quarkus.devtools.project.codegen.ProjectGenerator.QUARKUS_VERSION;

/**
 * Instances of this class are thread-safe. They create a new project extracting all the necessary properties from an instance
 * of {@link QuarkusCommandInvocation}.
 */
public class CreateProjectCommandHandler implements QuarkusCommandHandler {

    @Override
    public QuarkusCommandOutcome execute(QuarkusCommandInvocation invocation) throws QuarkusCommandException {
        final QuarkusPlatformDescriptor platformDescr = invocation.getPlatformDescriptor();
        invocation.setValue(BOM_GROUP_ID, platformDescr.getBomGroupId());
        invocation.setValue(BOM_ARTIFACT_ID, platformDescr.getBomArtifactId());
        invocation.setValue(QUARKUS_VERSION, platformDescr.getQuarkusVersion());
        invocation.setValue(BOM_VERSION, platformDescr.getBomVersion());
        final Set<String> extensionsQuery = invocation.getValue(ProjectGenerator.EXTENSIONS, Collections.emptySet());

        final Properties quarkusProps = ToolsUtils.readQuarkusProperties(platformDescr);
        quarkusProps.forEach((k, v) -> {
            String name = k.toString().replace("-", "_");
            if (!invocation.hasValue(name)) {
                invocation.setValue(k.toString().replace("-", "_"), v.toString());
            }
        });

        final List<AppArtifactKey> extensionsToAdd = computeCoordsFromQuery(invocation, extensionsQuery).stream()
                .map(AppArtifactCoords::getKey)
                .collect(Collectors.toList());

        try {
            Map<String, Object> platformData = new HashMap<>();
            if (platformDescr.getMetadata().get("maven") != null) {
                platformData.put("maven", platformDescr.getMetadata().get("maven"));
            }
            if (platformDescr.getMetadata().get("gradle") != null) {
                platformData.put("gradle", platformDescr.getMetadata().get("gradle"));
            }
            final QuarkusCodestartInput input = QuarkusCodestartInput.builder(platformDescr)
                    .addExtensions(extensionsToAdd)
                    .buildTool(invocation.getQuarkusProject().getBuildTool())
                    .addCodestarts(invocation.getValue(CODESTARTS, new HashSet<>()))
                    .noExamples(invocation.getValue(NO_EXAMPLES, false))
                    .noBuildToolWrapper(invocation.getValue(NO_BUILDTOOL_WRAPPER, false))
                    .noDockerfiles(invocation.getValue(NO_DOCKERFILES, false))
                    .addData(platformData)
                    .addData(LegacySupport.convertFromLegacy(invocation.getValues()))
                    .build();
            invocation.log().info(
                    "Generating Quarkus Codestart Project with data: " + input.getCodestartInput().getData().toString());
            final CodestartProject codestartProject = prepareProject(input);
            invocation.log().info("Codestarts: " + codestartProject.getCodestarts().stream().map(Codestart::getSpec)
                    .map(CodestartSpec::getName).collect(Collectors.joining(", ")));
            Codestarts.generateProject(codestartProject, invocation.getQuarkusProject().getProjectDirPath());
        } catch (IOException e) {
            throw new QuarkusCommandException("Failed to create project", e);
        }
        return QuarkusCommandOutcome.success();
    }
}
