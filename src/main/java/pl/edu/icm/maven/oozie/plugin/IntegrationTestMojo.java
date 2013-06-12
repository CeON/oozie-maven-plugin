package pl.edu.icm.maven.oozie.plugin;

import static org.twdata.maven.mojoexecutor.MojoExecutor.artifactId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.configuration;
import static org.twdata.maven.mojoexecutor.MojoExecutor.element;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executionEnvironment;
import static org.twdata.maven.mojoexecutor.MojoExecutor.goal;
import static org.twdata.maven.mojoexecutor.MojoExecutor.groupId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.plugin;
import static org.twdata.maven.mojoexecutor.MojoExecutor.version;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

@Mojo(name = "integration-test", requiresDependencyResolution = ResolutionScope.TEST)
public class IntegrationTestMojo extends AbstractOozieMojo {

	public void execute() throws MojoExecutionException {

		environment = executionEnvironment(mavenProject, mavenSession,
				pluginManager);

		executeMojo(
				plugin(groupId("org.apache.maven.plugins"),
						artifactId("maven-failsafe-plugin"),
						version(OoziePluginConstants.MAVEN_FAILSAFE_PLUGIN_VERSION)),
				goal("integration-test"),
				configuration(
						element("skipTests", String.valueOf(skipTests)),
						element("skipITs", String.valueOf(skipITs)),
						element("systemPropertyVariables",
								element("envIT", envIT))
				), environment);

	}
}
