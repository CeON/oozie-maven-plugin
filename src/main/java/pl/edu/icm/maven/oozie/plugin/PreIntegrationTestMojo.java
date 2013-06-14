package pl.edu.icm.maven.oozie.plugin;

import java.io.IOException;
import org.apache.hadoop.fs.Path;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

@Mojo(name = "pre-integration-test", requiresDependencyResolution = ResolutionScope.TEST)
public class PreIntegrationTestMojo extends AbstractIntegrationTestMojo {

	@Override
	public void execute() throws MojoExecutionException {

		if (skipTests || skipITs) {
			getLog().info("Tests are skipped");
			return;
		}

                try {
                    super.execute();
                } catch (SkipTestsException ex) {
                    getLog().info("Phase pre-integration-test skipped: " + ex.getMessage());
                    return;
                }

		try {
			Path src = new Path(buildDirectory + "/"
					+ OoziePluginConstants.OOZIE_WF_PREPARE_PACKAGE_DIR);

			Path hdfsWorkingDirPath = new Path(hdfsWorkingDirURI);
			if (hdfsFS.exists(hdfsWorkingDirPath)) {
				throw new MojoExecutionException("Path "
						+ hdfsWorkingDirPath.toUri() + " exists within HDFS "
						+ hdfsURI.toString());
			}

			Path dst = new Path(hdfsWorkingDirURI + "/" + wfDir);
			hdfsFS.copyFromLocalFile(src, dst);
		} catch (IOException e) {
			throw new MojoExecutionException(
					"A problem has occured during copying workflow job to HDFS "
							+ hdfsURI.toString(), e);
		}
	}
}
