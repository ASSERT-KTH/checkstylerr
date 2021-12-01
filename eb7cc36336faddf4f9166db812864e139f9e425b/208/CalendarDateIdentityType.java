/*
 * Copyright 2012-2013 inBloom, Inc. and its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.04.20 at 03:09:04 PM EDT 
//


package org.slc.sli.sample.entities;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * Encapsulates the possible attributes that can be used to lookup the identity of calendar dates in the school calendar.
 * 
 * <p>Java class for CalendarDateIdentityType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CalendarDateIdentityType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Date" type="{http://www.w3.org/2001/XMLSchema}date"/>
 *         &lt;choice maxOccurs="unbounded">
 *           &lt;element name="StateOrganizationId" type="{http://ed-fi.org/0100}IdentificationCode"/>
 *           &lt;element name="EducationOrgIdentificationCode" type="{http://ed-fi.org/0100}EducationOrgIdentificationCode" maxOccurs="unbounded"/>
 *         &lt;/choice>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CalendarDateIdentityType", propOrder = {
    "date",
    "stateOrganizationIdOrEducationOrgIdentificationCode"
})
public class CalendarDateIdentityType {

    @XmlElement(name = "Date", required = true, type = String.class)
    @XmlJavaTypeAdapter(Adapter2 .class)
    @XmlSchemaType(name = "date")
    protected Calendar date;
    @XmlElements({
        @XmlElement(name = "StateOrganizationId", type = String.class),
        @XmlElement(name = "EducationOrgIdentificationCode", type = EducationOrgIdentificationCode.class)
    })
    protected List<Object> stateOrganizationIdOrEducationOrgIdentificationCode;

    /**
     * Gets the value of the date property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Calendar getDate() {
        return date;
    }

    /**
     * Sets the value of the date property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDate(Calendar value) {
        this.date = value;
    }

    /**
     * Gets the value of the stateOrganizationIdOrEducationOrgIdentificationCode property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the stateOrganizationIdOrEducationOrgIdentificationCode property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getStateOrganizationIdOrEducationOrgIdentificationCode().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * {@link EducationOrgIdentificationCode }
     * 
     * 
     */
    public List<Object> getStateOrganizationIdOrEducationOrgIdentificationCode() {
        if (stateOrganizationIdOrEducationOrgIdentificationCode == null) {
            stateOrganizationIdOrEducationOrgIdentificationCode = new ArrayList<Object>();
        }
        return this.stateOrganizationIdOrEducationOrgIdentificationCode;
    }

}