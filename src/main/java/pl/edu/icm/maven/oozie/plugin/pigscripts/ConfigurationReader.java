package pl.edu.icm.maven.oozie.plugin.pigscripts;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.maven.plugin.logging.Log;
import org.xml.sax.SAXException;

import pl.edu.icm.maven.oozie.plugin.pigscripts.configuration.OozieMavenPluginType;

public class ConfigurationReader {
	
	private String buildDirectory;
	private String[] descriptors;
	private Log log;
	
	public ConfigurationReader(String [] descriptors, Log log, String buildDirectory){
		this.buildDirectory = buildDirectory;
		this.descriptors = descriptors;
		this.log = log;
	}

	public OozieMavenPluginType readConfiguration() {
		
		if(descriptors == null || descriptors.length==0){
			log.info("No configuration descriptor have been found. Default extraction procedure is selected");
			return null;
		}
		try {
			//get schema
			String schemaLang = "http://www.w3.org/2001/XMLSchema";
			SchemaFactory factory = SchemaFactory.newInstance(schemaLang);
		    Schema schema = factory.newSchema(new StreamSource(getClass().getClassLoader().getResourceAsStream("xsd/descriptor-1.1.xsd")));
		    //validate descriptor
		    Validator validator = schema.newValidator();
		    
		    File parent = new File(buildDirectory).getParentFile();
		    
		    validator.validate(new StreamSource(new File(parent, descriptors[0])));
		    //read descriptor
			JAXBContext jaxbContext = JAXBContext.newInstance(OozieMavenPluginType.class);
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			unmarshaller.setSchema(schema);
			InputStream reader = new FileInputStream(new File(parent, descriptors[0]));
			JAXBElement<OozieMavenPluginType> root = unmarshaller.unmarshal(new StreamSource(reader), OozieMavenPluginType.class);
			log.info("Selected configuration descriptor is correct. Advanced extraction procedure is selected");
			return root.getValue();
		} catch (JAXBException e) {
			log.error("The descriptor used is faulty written");
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			log.error("Under the path to descriptor no file has been found");
			e.printStackTrace();
		} catch (SAXException e) {
			log.error("The descriptor used is faulty written");
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		log.info("Some problems occured with selected configuration descriptor. Default extraction procedure is selected");
		return null;
	}
}
