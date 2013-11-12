package pl.edu.icm.maven.oozie.plugin.pigscripts;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

import pl.edu.icm.maven.oozie.plugin.OoziePluginConstants;
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
                    	extractScripts(globalLibDirectory, currentTreePosition, jar, entry, name, mppt.getScripts());
                    }else{
                    	if(omp_debbug) log.info("is deps pig script");
                    	extractScripts(globalLibDirectory, currentTreePosition, jar, entry, name, dppt.getScripts());	
                    }
		        }
		    } catch (IOException ex) {
		        throw new MojoExecutionException("unable to unpack jar file " + artifactPath, ex);
		    }
		}
		pigScriptsFirstLevel = false;
	}

	private boolean extractScripts(String globalLibDirectory,
			String currentTreePosition, JarFile jar, JarEntry entry,
			String name, List<ScriptHandlingType> scriptsList) throws FileNotFoundException, IOException {
		
		if(omp_debbug) log.warn("GLOBAL LIB DIRECTORY: "+globalLibDirectory);
		if(omp_debbug) log.warn("CURRENT TREE POSITION: "+currentTreePosition);
		if(omp_debbug) log.warn("NAME: "+name);
		boolean done = false;
		for(ScriptHandlingType sht : scriptsList){
			if(sht.getSrcProject()!=null && !currentTreePosition.matches(sht.getSrcProject())) continue;
			if(filterFile(name, sht)) continue;
			if(omp_debbug) log.info("uff");
			done=true;
			String startPosition = extractStartPosition(currentTreePosition, sht);
			String finalPath = createFinalPath(globalLibDirectory,name, sht);
			if(omp_debbug) log.warn("FINAL PATH: "+finalPath);
			copyScript(startPosition, jar, entry, finalPath);
		}
		return done;
	}
	
	private String extractStartPosition(String currentTreePosition,
			ScriptHandlingType sht) {
		String startPosition;
		if(omp_debbug) log.warn("sht.getMainDirAsDst() "+sht.getMainDirAsDst());
		if(sht.getMainDirAsDst()==null || sht.getMainDirAsDst()!=null && !sht.getMainDirAsDst()){
			startPosition = currentTreePosition;
		}else{
			String oWfDir = OoziePluginConstants.OOZIE_WF_PREPARE_PACKAGE_DIR;
			int oozieWfStringEnd = currentTreePosition.indexOf(oWfDir)+oWfDir.length();
			startPosition = currentTreePosition.substring(0,oozieWfStringEnd);
		}
		return startPosition;
	}
	
	private String createFinalPath(String globalLibDirectory, String name, ScriptHandlingType sht) {
		StringBuilder finalPathBuilder = new StringBuilder();
		String tmp = sht.getTarget();
		if(tmp != null && tmp.length()>0){
			if(omp_debbug) log.warn("TARGET "+tmp);
			finalPathBuilder.append(tmp);
		}
		tmp = sht.getRoot();
		if(sht.getPreserve()!=null && sht.getPreserve()==true){
			if(omp_debbug) log.warn("[preserve] ROOT "+tmp);
			finalPathBuilder.append(name);
		}else{
			if(omp_debbug) log.warn("[not preserve] ROOT "+name.substring(tmp.length()));
			finalPathBuilder.append(name.substring(tmp.length()));
		}
		return finalPathBuilder.toString();
	}
	

	
	private void copyScript(String currentTreePosition, JarFile jar,
			JarEntry entry, String finalPath) throws IOException,
			FileNotFoundException {

		if(omp_debbug) log.warn("FILE ROOT: "+currentTreePosition);
		
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
        log.info("Copying pig script: (src)["+entry+"] (dst)["+target.getPath()+"]");
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
			if(omp_debbug) log.info(name+".indexOf("+root+") != -1");
			if(omp_debbug) log.info("Pattern.compile("+include+").matcher("+name+").find(rootIndex+"+root+".length())");
			int rootIndex = name.indexOf(root);
			boolean containsRoot = rootIndex!=-1; 
			boolean containsPattern = containsRoot == true ? 
					Pattern.compile(include).matcher(name).find(rootIndex+root.length()) : false;  
			if (containsRoot && containsPattern) {//in includes
				if(sht.getExcludes() != null) for( String exclude : sht.getExcludes().getExclude()){//is in excludes?
					if(Pattern.compile(exclude).matcher(name).find(rootIndex+root.length())){
						if(omp_debbug) log.info("match inclusion and exclusion patterns");
						return true;
					}					
				}
				if(omp_debbug) log.info("match inclusion pattern");
				return false;
			}
		}
		if(omp_debbug) log.info("hasn't match inclusion pattern");
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
