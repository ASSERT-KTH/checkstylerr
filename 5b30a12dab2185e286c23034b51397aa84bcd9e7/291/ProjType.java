//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference
// Implementation, v2.2.8-b130911.1802
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2018.12.17 at 04:13:52 PM PST
//

package org.geoserver.mapml.xml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

/**
 * Java class for projType.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <p>
 *
 * <pre>
 * &lt;simpleType name="projType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="OSMTILE"/&gt;
 *     &lt;enumeration value="CBMTILE"/&gt;
 *     &lt;enumeration value="APSTILE"/&gt;
 *     &lt;enumeration value="WGS84"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 */
@XmlType(name = "projType")
@XmlEnum
public enum ProjType {
    OSMTILE("OSMTILE", 3857),
    CBMTILE("CBMTILE", 3978),
    APSTILE("APSTILE", 5936),
    @XmlEnumValue("WGS84")
    WGS_84("WGS84", 4326);
    private final String value;
    public final int epsgCode;

    ProjType(String v, int epsgCode) {
        value = v;
        this.epsgCode = epsgCode;
    }

    public String value() {
        return value;
    }

    public static ProjType fromValue(String v) {
        for (ProjType c : ProjType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
}
