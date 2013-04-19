package pl.edu.icm.maven.oozie.plugin;

import static org.twdata.maven.mojoexecutor.MojoExecutor.artifactId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.configuration;
import static org.twdata.maven.mojoexecutor.MojoExecutor.element;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executionEnvironment;
import static org.twdata.maven.mojoexecutor.MojoExecutor.goal;
import static org.twdata.maven.mojoexecutor.MojoExecutor.groupId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.name;
import static org.twdata.maven.mojoexecutor.MojoExecutor.plugin;
import static org.twdata.maven.mojoexecutor.MojoExecutor.version;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.twdata.maven.mojoexecutor.MojoExecutor.ExecutionEnvironment;

@Mojo(name = "package", requiresDependencyResolution = ResolutionScope.COMPILE)
public class PackageMojo extends AbstractMojo {

	private static final String MAVEN_DEPENDENCY_PLUGIN_VERSION = "2.7";
	private static final String MAVEN_CLEAN_PLUGIN_VERSION = "2.5";
	private static final String MAVEN_RESOURCES_PLUGIN_VERSION = "2.6";
	private static final String MAVEN_ASSEMBLY_PLUGIN_VERSION = "2.3";

	@Component
	private MavenProject mavenProject;

	@Component
	private MavenSession mavenSession;

	@Component
	private BuildPluginManager pluginManager;

	@Parameter(property = "package.assemblyDescriptor", defaultValue = "src/main/assembly/assembly.xml")
	private String assemblyDescriptor;

	@Parameter(property = "package.oozieDirectory", defaultValue = "src/main/oozie/")
	private String oozieDirectory;

	public void execute() throws MojoExecutionException {

		ExecutionEnvironment environment = executionEnvironment(mavenProject,
				mavenSession, pluginManager);

		executeMojo(
				plugin(groupId("org.apache.maven.plugins"),
						artifactId("maven-dependency-plugin"),
						version(MAVEN_DEPENDENCY_PLUGIN_VERSION)),
				goal("unpack-dependencies"),
				configuration(
						element(name("outputDirectory"),
								"${project.build.directory}/oozie-wf-tmp/"),
						element(name("includeClassifiers"), "oozie-wf"),
						element(name("excludeTransitive"), "true")),
				environment);

		/*
		 * This step can be omitted when the following problem with "exclude"
		 * option for tar.gz files http://jira.codehaus.org/browse/MDEP-242 for
		 * maven-dependency-plugin is resolved. Instead the 'exclude' option
		 * should be used in previous step.
		 */
		executeMojo(
				plugin(groupId("org.apache.maven.plugins"),
						artifactId("maven-clean-plugin"),
						version(MAVEN_CLEAN_PLUGIN_VERSION)),
				goal("clean"),
				configuration(
						element(name("excludeDefaultDirectories"), "true"),
						element(name("filesets"),
								element(name("fileset"),
										element(name("directory"),
												"${project.build.directory}/oozie-wf-tmp/"),
										element(name("includes"),
												element(name("include"),
														"**/lib/*"),
												element(name("include"),
														"**/lib/"))))),
				environment);

		executeMojo(
				plugin(groupId("org.apache.maven.plugins"),
						artifactId("maven-dependency-plugin"),
						version(MAVEN_DEPENDENCY_PLUGIN_VERSION)),
				goal("copy-dependencies"),
				configuration(
						element(name("outputDirectory"),
								"${project.build.directory}/oozie-wf-tmp/lib/"),
						element(name("excludeClassifiers"), "oozie-wf"),
						element(name("excludeScope"), "provided")),
				environment);

		executeMojo(
				plugin(groupId("org.apache.maven.plugins"),
						artifactId("maven-resources-plugin"),
						version(MAVEN_RESOURCES_PLUGIN_VERSION)),
				goal("copy-resources"),
				configuration(
						element("outputDirectory",
								"${project.build.directory}/oozie-wf-tmp/"),
						element("resources",
								element("resource",
										element("directory", oozieDirectory),
										element("filtering", "true")))

				), environment);

		executeMojo(
				plugin(groupId("org.apache.maven.plugins"),
						artifactId("maven-assembly-plugin"),
						version(MAVEN_ASSEMBLY_PLUGIN_VERSION)),
				goal("single"),
				configuration(element("descriptors",
						element("descriptor", assemblyDescriptor))),
				environment);

		executeMojo(
				plugin(groupId("org.apache.maven.plugins"),
						artifactId("maven-clean-plugin"),
						version(MAVEN_CLEAN_PLUGIN_VERSION)),
				goal("clean"),
				configuration(
						element(name("excludeDefaultDirectories"), "true"),
						element(name("filesets"),
								element(name("fileset"),
										element(name("directory"),
												"${project.build.directory}/oozie-wf-tmp/"),
										element(name("includes"),
												element(name("include"), "**"))))),
				environment);
	}
}
