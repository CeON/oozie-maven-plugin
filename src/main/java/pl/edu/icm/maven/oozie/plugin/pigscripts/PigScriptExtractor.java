package pl.edu.icm.maven.oozie.plugin.pigscripts;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

import pl.edu.icm.maven.oozie.plugin.pigscripts.configuration.DepsProjectPigType;
import pl.edu.icm.maven.oozie.plugin.pigscripts.configuration.MainProjectPigType;
import pl.edu.icm.maven.oozie.plugin.pigscripts.configuration.OozieMavenPluginType;
import pl.edu.icm.maven.oozie.plugin.pigscripts.configuration.ScriptHandlingType;

public class PigScriptExtractor {

	OozieMavenPluginType pigScriptsConfiguration = null;
	boolean pigScriptsFirstLevel = true;
	Log log = null;
	boolean omp_debbug = true;
	
	public PigScriptExtractor(OozieMavenPluginType pigScriptsConfiguration, Log log, boolean omp_debbug) {
		this.pigScriptsConfiguration = pigScriptsConfiguration;
		this.pigScriptsFirstLevel = true;
		this.log = log;
		this.omp_debbug = omp_debbug;
	}

	public void performExtraction(String globalLibDirectory, String currentTreePosition, Artifact af, File afTmpDir) throws MojoExecutionException{
		if( pigScriptsConfiguration == null){
			performDefaultExtraction(globalLibDirectory, currentTreePosition, af, afTmpDir);
		}else{
			performFancyExtraction(globalLibDirectory, currentTreePosition, af, afTmpDir);
		}
	}
	
	private void performFancyExtraction(String globalLibDirectory,
			String currentTreePosition, Artifact af, File afTmpDir) throws MojoExecutionException {
		
		String[] dirContent;
		if (!afTmpDir.isDirectory() || (dirContent = afTmpDir.list()).length == 0) {
		    throw new MojoExecutionException("unable to get artifact " + af.getGroupId() + ":" + af.getArtifactId());
		}

		for (String artifactFile : dirContent) {
		    String artifactPath = new File(afTmpDir, artifactFile).getPath();
		    try {
		        JarFile jar = new JarFile(artifactPath);
		        Enumeration<? extends JarEntry> entries = jar.entries();
		        int i=1;
		        while (entries.hasMoreElements()) {
		            JarEntry entry = entries.nextElement();
		            String name = entry.getName();
		            
		            if(!name.matches(".*pig")) continue;
		            if(omp_debbug) log.info(">>>>>>>>>>>>file "+i+". >>>>>>>>>>>");
		            i++;
		            
                    MainProjectPigType mppt = pigScriptsConfiguration.getMainProjectPig(); 
                    DepsProjectPigType dppt = pigScriptsConfiguration.getDepsProjectPig(); 
                    log.info("to analyse: "+entry);
                    if(pigScriptsFirstLevel){
                    	if(omp_debbug) log.info("is main pig script?");
                    	if(extractMainScripts(globalLibDirectory, currentTreePosition, jar, entry, name, mppt)){
                    	}else{
                    		if(omp_debbug) log.info("is main pig macro?");
                    		extractMainMacros(globalLibDirectory, currentTreePosition, jar, entry, name, mppt);
                    	}
                    }else{
                    	if(omp_debbug) log.info("is deps pig script");
                    	extractDepsScripts(globalLibDirectory, currentTreePosition, jar, entry, name, dppt);	
                    }
		        }
		    } catch (IOException ex) {
		        throw new MojoExecutionException("unable to unpack jar file " + artifactPath, ex);
		    }
		}
		pigScriptsFirstLevel = false;
	}

	private boolean extractDepsScripts(String globalLibDirectory,
			String currentTreePosition, JarFile jar, JarEntry entry,
			String name, DepsProjectPigType dppt) throws FileNotFoundException, IOException {
		
		if(filterFile(name, dppt.getAllScripts())) return false;
		if(omp_debbug) log.info("uff");
		
		String finalPath = createFinalPath(name, dppt.getAllScripts());
		
		copyScript(currentTreePosition, jar, entry, finalPath);
		log.info("Copying pig script: (src)["+entry+"] (dst)["+finalPath+"]");
		return true;
	}
	
	private boolean extractMainMacros(String globalLibDirectory,
			String currentTreePosition, JarFile jar, JarEntry entry,
			String name, MainProjectPigType mppt) throws FileNotFoundException, IOException {
		
		if(filterFile(name, mppt.getMacros())) return false;
		if(omp_debbug) log.info("uff");
		
		String finalPath = createFinalPath(name, mppt.getMacros());
		
		copyScript(currentTreePosition, jar, entry, finalPath);
		log.info("Copying pig script: (src)["+entry+"] (dst)["+finalPath+"]");
		return true;
	}

	private boolean extractMainScripts(String globalLibDirectory,String currentTreePosition, JarFile jar, JarEntry entry,
			String name, MainProjectPigType mppt) throws IOException,FileNotFoundException {
		
		if(filterFile(name, mppt.getScripts())) return false;
		if(omp_debbug) log.info("uff");
		
		String finalPath = createFinalPath(name, mppt.getScripts());
		
		copyScript(currentTreePosition, jar, entry, finalPath);
		log.info("Copying pig script: (src)["+entry+"] (dst)["+finalPath+"]");
		return true;
	}

	private void copyScript(String currentTreePosition, JarFile jar,
			JarEntry entry, String finalPath) throws IOException,
			FileNotFoundException {
		File target = new File(currentTreePosition,finalPath);		
		FileUtils.forceMkdir(target.getParentFile());	
		
		FileOutputStream output = null;
        try {
            output = new FileOutputStream(target);
            InputStream input = jar.getInputStream(entry);
            IOUtils.copy(input, output);
        } finally {
            IOUtils.closeQuietly(output);
        }
	}

	private String createFinalPath(String name, ScriptHandlingType sht) {
		StringBuilder finalPathBuilder = new StringBuilder();
		String tmp = sht.getTarget();
		if(tmp != null && tmp.length()>0){
			finalPathBuilder.append(tmp);
		}
		tmp = sht.getRoot();
		if(sht.isPreserve()){
			finalPathBuilder.append(name);
		}else{
			finalPathBuilder.append(name.substring(tmp.length()));
		}
		return finalPathBuilder.toString();
	}

	/**
	 * @return true if either name is not present in includes or follows exclude pattern; false otherwise 
	 */ 
	private boolean filterFile(String name, ScriptHandlingType sht) {
		if(omp_debbug) log.info("--------------------------");
		if(omp_debbug) log.info("name: "+name);
		String root = sht.getRoot();
		if(omp_debbug) log.info("root: "+root);
		for( String include : sht.getIncludes().getInclude()){//is in includes?
			if(omp_debbug) log.info(name+".matches("+root+include+")");
			if (name.matches(root+include)) {//in includes
				if(sht.getExcludes() != null) for( String exclude : sht.getExcludes().getExclude()){//is in excludes?
					if(name.matches(exclude)){
						if(omp_debbug) log.info("no_1");
						return true;
					}					
				}
				if(omp_debbug) log.info("yes");
				return false;
			}
		}
		if(omp_debbug) log.info("no_2");
		return true;
	}

	private void performDefaultExtraction(String globalLibDirectory,
			String currentTreePosition, Artifact af, File afTmpDir)
			throws MojoExecutionException {
		String[] dirContent;
		if (!afTmpDir.isDirectory() || (dirContent = afTmpDir.list()).length == 0) {
		    throw new MojoExecutionException("unable to get artifact " + af.getGroupId() + ":" + af.getArtifactId());
		}

		for (String artifactFile : dirContent) {
		    String artifactPath = new File(afTmpDir, artifactFile).getPath();
		    try {
		        JarFile jar = new JarFile(artifactPath);
		        Enumeration<? extends JarEntry> entries = jar.entries();
		        while (entries.hasMoreElements()) {
		            JarEntry entry = entries.nextElement();
		            String name = entry.getName();

		            if (name.matches("pig/.*\\.pig")) {

		                File target;
		                if (name.matches("pig/.*/.*\\.pig")) {
		                    // copy to lib directory in main workflow (not a subworkflow)
		                    target = new File(globalLibDirectory, name);
		                } else {
		                    // copy to workflow directory
		                    target = new File(currentTreePosition, new File(name).getName());
		                }
		                FileUtils.forceMkdir(target.getParentFile());

		                FileOutputStream output = null;
		                try {
		                    output = new FileOutputStream(target);
		                    InputStream input = jar.getInputStream(entry);
		        			log.info("Copying pig script: (src)["+entry+"] (dst)["+target.getPath()+"]");
		                    IOUtils.copy(input, output);
		                } finally {
		                    IOUtils.closeQuietly(output);
		                }
		            }
		        }
		    } catch (IOException ex) {
		        throw new MojoExecutionException("unable to unpack jar file " + artifactPath, ex);
		    }
		}
	}
}
