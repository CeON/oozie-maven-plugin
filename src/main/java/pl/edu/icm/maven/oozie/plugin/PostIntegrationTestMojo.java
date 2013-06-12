package pl.edu.icm.maven.oozie.plugin;

import java.io.IOException;

import org.apache.hadoop.fs.Path;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

@Mojo(name = "post-integration-test", requiresDependencyResolution = ResolutionScope.TEST)
public class PostIntegrationTestMojo extends AbstractIntegrationTestMojo {

    @Override
    public void execute() throws MojoExecutionException {

        if (skipTests || skipITs) {
            getLog().info("Tests are skipped");
            return;
        }

        super.execute();

        if (!skipCleanIT) {
            Path hdfsWorkingDirPath = new Path(hdfsWorkingDirURI);
            try {
                hdfsFS.delete(hdfsWorkingDirPath, true);
            } catch (IOException e) {
                throw new MojoExecutionException(
                        "A problem has occured during deleting "
                        + hdfsWorkingDirPath.toUri() + " from HDFS "
                        + hdfsURI.toString(), e);
            }
        } else {
            getLog().info(
                    "Working directory " + hdfsWorkingDirURI + " within HDFS "
                    + hdfsURI.toString() + " has not been deleted.");
        }

    }
}