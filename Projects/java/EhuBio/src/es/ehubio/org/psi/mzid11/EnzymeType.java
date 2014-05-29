//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.05.29 at 05:27:31 PM CEST 
//


package es.ehubio.org.psi.mzid11;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * The details of an individual cleavage enzyme should be provided by giving a regular expression or a CV term if a "standard" enzyme cleavage has been performed. 
 * 
 * <p>Java class for EnzymeType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="EnzymeType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://psidev.info/psi/pi/mzIdentML/1.1}IdentifiableType">
 *       &lt;sequence>
 *         &lt;element name="SiteRegexp" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="EnzymeName" type="{http://psidev.info/psi/pi/mzIdentML/1.1}ParamListType" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="nTermGain">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *             &lt;pattern value="[A-Za-z0-9 ]+"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="cTermGain">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *             &lt;pattern value="[A-Za-z0-9 ]+"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="semiSpecific" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="missedCleavages" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="minDistance">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}int">
 *             &lt;minInclusive value="1"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EnzymeType", propOrder = {
    "siteRegexp",
    "enzymeName"
})
public class EnzymeType
    extends IdentifiableType
{

    @XmlElement(name = "SiteRegexp")
    protected String siteRegexp;
    @XmlElement(name = "EnzymeName")
    protected ParamListType enzymeName;
    @XmlAttribute(name = "nTermGain")
    protected String nTermGain;
    @XmlAttribute(name = "cTermGain")
    protected String cTermGain;
    @XmlAttribute(name = "semiSpecific")
    protected Boolean semiSpecific;
    @XmlAttribute(name = "missedCleavages")
    protected Integer missedCleavages;
    @XmlAttribute(name = "minDistance")
    protected Integer minDistance;

    /**
     * Gets the value of the siteRegexp property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSiteRegexp() {
        return siteRegexp;
    }

    /**
     * Sets the value of the siteRegexp property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSiteRegexp(String value) {
        this.siteRegexp = value;
    }

    /**
     * Gets the value of the enzymeName property.
     * 
     * @return
     *     possible object is
     *     {@link ParamListType }
     *     
     */
    public ParamListType getEnzymeName() {
        return enzymeName;
    }

    /**
     * Sets the value of the enzymeName property.
     * 
     * @param value
     *     allowed object is
     *     {@link ParamListType }
     *     
     */
    public void setEnzymeName(ParamListType value) {
        this.enzymeName = value;
    }

    /**
     * Gets the value of the nTermGain property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNTermGain() {
        return nTermGain;
    }

    /**
     * Sets the value of the nTermGain property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNTermGain(String value) {
        this.nTermGain = value;
    }

    /**
     * Gets the value of the cTermGain property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCTermGain() {
        return cTermGain;
    }

    /**
     * Sets the value of the cTermGain property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCTermGain(String value) {
        this.cTermGain = value;
    }

    /**
     * Gets the value of the semiSpecific property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isSemiSpecific() {
        return semiSpecific;
    }

    /**
     * Sets the value of the semiSpecific property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setSemiSpecific(Boolean value) {
        this.semiSpecific = value;
    }

    /**
     * Gets the value of the missedCleavages property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getMissedCleavages() {
        return missedCleavages;
    }

    /**
     * Sets the value of the missedCleavages property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setMissedCleavages(Integer value) {
        this.missedCleavages = value;
    }

    /**
     * Gets the value of the minDistance property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getMinDistance() {
        return minDistance;
    }

    /**
     * Sets the value of the minDistance property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setMinDistance(Integer value) {
        this.minDistance = value;
    }

}
