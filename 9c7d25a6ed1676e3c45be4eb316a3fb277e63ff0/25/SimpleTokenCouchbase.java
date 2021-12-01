package org.gluu.couchbase.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.gluu.persist.model.base.CustomAttribute;
import org.gluu.persist.annotation.@AttributesList;
import org.gluu.persist.annotation.LdapCustomObjectClass;
import org.gluu.persist.annotation.LdapDN;
import org.gluu.persist.annotation.LdapEntry;
import org.gluu.persist.annotation.LdapObjectClass;
import org.gluu.util.StringHelper;

/**
 * Created by eugeniuparvan on 1/12/17.
 */
@Entry
@ObjectClass(values = {"top", "oxAuthToken"})
public class SimpleTokenCouchbase implements Serializable {

    private static final long serialVersionUID = 6726419630327625172L;

    @AttributesList(name = "name", value = "values", sortByName = true)
    private List<CustomAttribute> customAttributes = new ArrayList<CustomAttribute>();

    @DN
    private String dn;

    @CustomObjectClass
    private String[] customObjectClasses;

    public String getDn() {
        return dn;
    }

    public void setDn(String dn) {
        this.dn = dn;
    }

    public List<CustomAttribute> getCustomAttributes() {
        return customAttributes;
    }

    public void setCustomAttributes(List<CustomAttribute> customAttributes) {
        this.customAttributes = customAttributes;
    }

    public String getAttribute(String ldapAttribute) {
        String attribute = null;
        if (ldapAttribute != null && !ldapAttribute.isEmpty()) {
            for (CustomAttribute customAttribute : customAttributes) {
                if (customAttribute.getName().equals(ldapAttribute)) {
                    attribute = customAttribute.getValue();
                    break;
                }
            }
        }

        return attribute;
    }

    public List<String> getAttributeValues(String ldapAttribute) {
        List<String> values = null;
        if (ldapAttribute != null && !ldapAttribute.isEmpty()) {
            for (CustomAttribute customAttribute : customAttributes) {
                if (StringHelper.equalsIgnoreCase(customAttribute.getName(), ldapAttribute)) {
                    values = customAttribute.getValues();
                    break;
                }
            }
        }

        return values;
    }

    public String[] getCustomObjectClasses() {
        return customObjectClasses;
    }

    public void setCustomObjectClasses(String[] customObjectClasses) {
        this.customObjectClasses = customObjectClasses;
    }

}
