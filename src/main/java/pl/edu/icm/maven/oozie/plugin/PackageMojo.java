package pl.edu.icm.maven.oozie.plugin;

import static org.twdata.maven.mojoexecutor.MojoExecutor.artifactId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.configuration;
import static org.twdata.maven.mojoexecutor.MojoExecutor.element;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.goal;
import static org.twdata.maven.mojoexecutor.MojoExecutor.groupId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.plugin;
import static org.twdata.maven.mojoexecutor.MojoExecutor.version;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

@Mojo(name = "package", requiresDependencyResolution = ResolutionScope.COMPILE)
public class PackageMojo extends AbstractOozieMojo {

	@Override
	public void execute() throws MojoExecutionException {
		super.execute();
		packageWorkflow();
		packageJob();
	}

	private void packageJob() throws MojoExecutionException {

		if (!jobPackage) {
			getLog().info("Ozzie job package has not been created.");
			return;
		}

		try {
			copyFileFromClasspathToFileSystem("assemblies/jobPackage.xml",
					buildDirectory + "/assemblies/jobPackage.xml");
		} catch (IOException e) {
			throw new MojoExecutionException("Assembly has not been copied", e);
		}

		executeMojo(
				plugin(groupId("org.apache.maven.plugins"),
						artifactId("maven-assembly-plugin"),
						version(OoziePluginConstants.MAVEN_ASSEMBLY_PLUGIN_VERSION)),
				goal("single"),
				configuration(element(
						"descriptors",
						element("descriptor",
								"${project.build.directory}/assemblies/jobPackage.xml"))),
				environment);

	}

	private void packageWorkflow() throws MojoExecutionException {

		try {
			copyFileFromClasspathToFileSystem("assemblies/workflowPackage.xml",
					buildDirectory + "/assemblies/workflowPackage.xml");
		} catch (IOException e) {
			throw new MojoExecutionException("Assembly has not been copied", e);
		}

		executeMojo(
				plugin(groupId("org.apache.maven.plugins"),
						artifactId("maven-assembly-plugin"),
						version(OoziePluginConstants.MAVEN_ASSEMBLY_PLUGIN_VERSION)),
				goal("single"),
				configuration(element(
						"descriptors",
						element("descriptor",
								"${project.build.directory}/assemblies/workflowPackage.xml"))),
				environment);

	}

	private void copyFileFromClasspathToFileSystem(String src, String dst)
			throws IOException {
		InputStream is = this.getClass().getClassLoader()
				.getResourceAsStream(src);
		File fileDst = new File(dst);
		FileUtils.copyInputStreamToFile(is, fileDst);
	}
}
