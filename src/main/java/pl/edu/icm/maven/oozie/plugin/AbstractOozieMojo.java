package pl.edu.icm.maven.oozie.plugin;

import static org.twdata.maven.mojoexecutor.MojoExecutor.executionEnvironment;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.twdata.maven.mojoexecutor.MojoExecutor.ExecutionEnvironment;

public abstract class AbstractOozieMojo extends AbstractMojo {

	@Component
	protected MavenProject mavenProject;

	@Component
	protected MavenSession mavenSession;

	@Component
	protected BuildPluginManager pluginManager;

	protected String buildDirectory;

	protected ExecutionEnvironment environment;

	@Parameter(property = "package.oozieDirectory", defaultValue = "src/main/oozie/")
	protected String oozieDirectory;

	@Parameter(property = "skipTests", defaultValue = "false")
	protected boolean skipTests;

	@Parameter(property = "skipITs", defaultValue = "false")
	protected boolean skipITs;

	@Parameter(property = "IT.env", defaultValue = "local")
	protected String envIT;

	@Parameter(property = "jobPackage", defaultValue = "false")
	protected boolean jobPackage;

	@Parameter(property = "skipCleanIT", defaultValue = "false")
	protected boolean skipCleanIT;

	@Override
	public void execute() throws MojoExecutionException {
		environment = executionEnvironment(mavenProject, mavenSession,
				pluginManager);
		buildDirectory = mavenProject.getBuild().getDirectory();
	}

}
