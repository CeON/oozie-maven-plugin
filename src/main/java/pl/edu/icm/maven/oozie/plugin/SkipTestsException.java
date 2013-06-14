package pl.edu.icm.maven.oozie.plugin;

import org.apache.maven.plugin.MojoExecutionException;

/**
 *
 * @author acz
 */
public class SkipTestsException extends MojoExecutionException {

    public SkipTestsException(String message) {
        super(message);
    }
}
