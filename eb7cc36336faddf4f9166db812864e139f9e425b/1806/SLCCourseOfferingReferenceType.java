//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.01.22 at 01:42:02 PM EST 
//


package org.ed_fi._0100;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * Changed to use a required SLC identity type.
 * 
 * <p>Java class for SLC-CourseOfferingReferenceType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SLC-CourseOfferingReferenceType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="CourseOfferingIdentity" type="{http://ed-fi.org/0100}SLC-CourseOfferingIdentityType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SLC-CourseOfferingReferenceType", propOrder = {
    "courseOfferingIdentity"
})
public class SLCCourseOfferingReferenceType {

    @XmlElement(name = "CourseOfferingIdentity", required = true)
    protected SLCCourseOfferingIdentityType courseOfferingIdentity;

    /**
     * Gets the value of the courseOfferingIdentity property.
     * 
     * @return
     *     possible object is
     *     {@link SLCCourseOfferingIdentityType }
     *     
     */
    public SLCCourseOfferingIdentityType getCourseOfferingIdentity() {
        return courseOfferingIdentity;
    }

    /**
     * Sets the value of the courseOfferingIdentity property.
     * 
     * @param value
     *     allowed object is
     *     {@link SLCCourseOfferingIdentityType }
     *     
     */
    public void setCourseOfferingIdentity(SLCCourseOfferingIdentityType value) {
        this.courseOfferingIdentity = value;
    }

}