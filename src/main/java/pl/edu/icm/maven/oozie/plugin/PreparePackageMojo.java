package pl.edu.icm.maven.oozie.plugin;

import org.apache.maven.artifact.Artifact;
import static org.twdata.maven.mojoexecutor.MojoExecutor.artifactId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.configuration;
import static org.twdata.maven.mojoexecutor.MojoExecutor.element;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.goal;
import static org.twdata.maven.mojoexecutor.MojoExecutor.groupId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.name;
import static org.twdata.maven.mojoexecutor.MojoExecutor.plugin;
import static org.twdata.maven.mojoexecutor.MojoExecutor.version;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.shared.dependency.tree.DependencyNode;
import org.apache.maven.shared.dependency.tree.DependencyTreeBuilderException;

@Mojo(name = "prepare-package", requiresDependencyResolution = ResolutionScope.COMPILE)
public class PreparePackageMojo extends AbstractOozieMojo {

    @Override
    public void execute() throws MojoExecutionException {
        super.execute();
        preparePackageWorkflow();
        preparePackageJob();
    }

    private void preparePackageJob() throws MojoExecutionException {

        if (!jobPackage && (skipTests || skipITs)) {
            getLog().info("Ozzie job package has not been prepared.");
            return;
        }

        DependencyNode dependencyTree;
        try {
            dependencyTree = dependencyTreeBuilder.buildDependencyTree(mavenProject, localRepository, null);
        } catch (DependencyTreeBuilderException ex) {
            throw new MojoExecutionException("Failed to build dependency tree", ex);
        }
        unpackWorkflows("${project.build.directory}/" + OoziePluginConstants.OOZIE_WF_PREPARE_PACKAGE_DIR, dependencyTree);

        /*
         * This step can be omitted when the following problem with "exclude"
         * option for tar.gz files http://jira.codehaus.org/browse/MDEP-242 for
         * maven-dependency-plugin is resolved. Instead the 'exclude' option
         * should be used in previous step.
         */
        executeMojo(
                plugin(groupId("org.apache.maven.plugins"),
                artifactId("maven-clean-plugin"),
                version(OoziePluginConstants.MAVEN_CLEAN_PLUGIN_VERSION)),
                goal("clean"),
                configuration(
                element(name("excludeDefaultDirectories"), "true"),
                element(name("filesets"),
                element(name("fileset"),
                element(name("directory"),
                "${project.build.directory}/"
                + OoziePluginConstants.OOZIE_WF_PREPARE_PACKAGE_DIR),
                element(name("includes"),
                element(name("include"),
                "**/lib/*"),
                element(name("include"),
                "**/lib/"))))),
                environment);

        executeMojo(
                plugin(groupId("org.apache.maven.plugins"),
                artifactId("maven-dependency-plugin"),
                version(OoziePluginConstants.MAVEN_DEPENDENCY_PLUGIN_VERSION)),
                goal("copy-dependencies"),
                configuration(
                element(name("outputDirectory"),
                "${project.build.directory}/"
                + OoziePluginConstants.OOZIE_WF_PREPARE_PACKAGE_DIR
                + "/lib/"),
                element(name("excludeClassifiers"),
                OoziePluginConstants.OOZIE_WF_CLASSIFIER),
                element(name("excludeScope"), "provided")), environment);

    }

    private void preparePackageWorkflow() throws MojoExecutionException {

        executeMojo(
                plugin(groupId("org.apache.maven.plugins"),
                artifactId("maven-resources-plugin"),
                version(OoziePluginConstants.MAVEN_RESOURCES_PLUGIN_VERSION)),
                goal("copy-resources"),
                configuration(
                element("outputDirectory",
                "${project.build.directory}/"
                + OoziePluginConstants.OOZIE_WF_PREPARE_PACKAGE_DIR),
                element("resources",
                element("resource",
                element("directory", oozieDirectory),
                element("filtering", String.valueOf(filtering))))), environment);
    }
    
    private void unpackWorkflows(String outputDirectory, DependencyNode dependencyTree) throws MojoExecutionException {

        for (DependencyNode childNode : dependencyTree.getChildren()) {
            Artifact af = childNode.getArtifact();
            if (OoziePluginConstants.OOZIE_WF_CLASSIFIER.equals(af.getClassifier())) {

                executeMojo(
                        plugin(groupId("org.apache.maven.plugins"),
                        artifactId("maven-dependency-plugin"),
                        version(OoziePluginConstants.MAVEN_DEPENDENCY_PLUGIN_VERSION)),
                        goal("unpack-dependencies"),
                        configuration(
                            element(name("outputDirectory"), outputDirectory),
                            element(name("includeArtifactIds"), af.getArtifactId())
                        ),
                        environment);

                unpackWorkflows(outputDirectory + "/" + af.getGroupId() + "-" + af.getArtifactId(), childNode);
            }
        }
    }
}