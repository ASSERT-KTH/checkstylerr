//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference
// Implementation, v2.2.8-b130911.1802
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2018.12.17 at 04:13:52 PM PST
//

package org.geoserver.mapml.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Java class for anonymous complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{}head" minOccurs="0"/&gt;
 *         &lt;element ref="{}body"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
    name = "",
    propOrder = {"head", "body"}
)
@XmlRootElement(name = "mapml", namespace = "http://www.w3.org/1999/xhtml/")
public class Mapml {

    @XmlElement(required = true, namespace = "http://www.w3.org/1999/xhtml/")
    protected HeadContent head;

    @XmlElement(required = true, namespace = "http://www.w3.org/1999/xhtml/")
    protected BodyContent body;

    /**
     * Gets the value of the head property.
     *
     * @return possible object is {@link HeadContent }
     */
    public HeadContent getHead() {
        return head;
    }

    /**
     * Sets the value of the head property.
     *
     * @param value allowed object is {@link HeadContent }
     */
    public void setHead(HeadContent value) {
        this.head = value;
    }

    /**
     * Gets the value of the body property.
     *
     * @return possible object is {@link BodyContent }
     */
    public BodyContent getBody() {
        return body;
    }

    /**
     * Sets the value of the body property.
     *
     * @param value allowed object is {@link BodyContent }
     */
    public void setBody(BodyContent value) {
        this.body = value;
    }
}
