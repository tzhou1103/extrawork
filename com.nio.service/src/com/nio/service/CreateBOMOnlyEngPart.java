
package com.nio.service;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for createBOMOnlyEngPart complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="createBOMOnlyEngPart">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="partType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="owning_user" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="owning_group" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="crNum" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "createBOMOnlyEngPart", propOrder = {
    "partType",
    "owningUser",
    "owningGroup",
    "crNum"
})
public class CreateBOMOnlyEngPart {

    protected String partType;
    @XmlElement(name = "owning_user")
    protected String owningUser;
    @XmlElement(name = "owning_group")
    protected String owningGroup;
    protected String crNum;

    /**
     * Gets the value of the partType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPartType() {
        return partType;
    }

    /**
     * Sets the value of the partType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPartType(String value) {
        this.partType = value;
    }

    /**
     * Gets the value of the owningUser property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOwningUser() {
        return owningUser;
    }

    /**
     * Sets the value of the owningUser property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOwningUser(String value) {
        this.owningUser = value;
    }

    /**
     * Gets the value of the owningGroup property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOwningGroup() {
        return owningGroup;
    }

    /**
     * Sets the value of the owningGroup property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOwningGroup(String value) {
        this.owningGroup = value;
    }

    /**
     * Gets the value of the crNum property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCrNum() {
        return crNum;
    }

    /**
     * Sets the value of the crNum property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCrNum(String value) {
        this.crNum = value;
    }

}
