//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.11.12 at 07:16:43 PM CET 
//


package pl.edu.icm.maven.oozie.plugin.pigscripts.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for oozieMavenPluginType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="oozieMavenPluginType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="main-project-pig" type="{}mainProjectPigType" minOccurs="0"/&gt;
 *         &lt;element name="deps-project-pig" type="{}depsProjectPigType" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "oozieMavenPluginType", propOrder = {
    "mainProjectPig",
    "depsProjectPig"
})
public class OozieMavenPluginType {

    @XmlElement(name = "main-project-pig")
    protected MainProjectPigType mainProjectPig;
    @XmlElement(name = "deps-project-pig")
    protected DepsProjectPigType depsProjectPig;

    /**
     * Gets the value of the mainProjectPig property.
     * 
     * @return
     *     possible object is
     *     {@link MainProjectPigType }
     *     
     */
    public MainProjectPigType getMainProjectPig() {
        return mainProjectPig;
    }

    /**
     * Sets the value of the mainProjectPig property.
     * 
     * @param value
     *     allowed object is
     *     {@link MainProjectPigType }
     *     
     */
    public void setMainProjectPig(MainProjectPigType value) {
        this.mainProjectPig = value;
    }

    /**
     * Gets the value of the depsProjectPig property.
     * 
     * @return
     *     possible object is
     *     {@link DepsProjectPigType }
     *     
     */
    public DepsProjectPigType getDepsProjectPig() {
        return depsProjectPig;
    }

    /**
     * Sets the value of the depsProjectPig property.
     * 
     * @param value
     *     allowed object is
     *     {@link DepsProjectPigType }
     *     
     */
    public void setDepsProjectPig(DepsProjectPigType value) {
        this.depsProjectPig = value;
    }

}
