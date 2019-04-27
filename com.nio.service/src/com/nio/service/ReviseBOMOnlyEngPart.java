
package com.nio.service;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for reviseBOMOnlyEngPart complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="reviseBOMOnlyEngPart">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="engPartID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="crID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "reviseBOMOnlyEngPart", propOrder = {
    "engPartID",
    "crID"
})
public class ReviseBOMOnlyEngPart {

    protected String engPartID;
    protected String crID;

    /**
     * Gets the value of the engPartID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEngPartID() {
        return engPartID;
    }

    /**
     * Sets the value of the engPartID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEngPartID(String value) {
        this.engPartID = value;
    }

    /**
     * Gets the value of the crID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCrID() {
        return crID;
    }

    /**
     * Sets the value of the crID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCrID(String value) {
        this.crID = value;
    }

}
