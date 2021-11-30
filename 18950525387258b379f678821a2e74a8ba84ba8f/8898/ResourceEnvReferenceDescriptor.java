/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package com.sun.enterprise.deployment;

import com.sun.enterprise.deployment.util.DOLUtils;

/**
 * I am an object representing a dependency on a resource environment
 * @author Kenneth Saks
 */

public class ResourceEnvReferenceDescriptor extends EnvironmentProperty implements NamedDescriptor, ResourceEnvReference {

    static private final int NULL_HASH_CODE = Integer.valueOf(1).hashCode();

    private String refType;

    private boolean isManagedBean = false;
    private ManagedBeanDescriptor managedBeanDesc;

    private static final String SESSION_CTX_TYPE = "jakarta.ejb.SessionContext";
    private static final String MDB_CTX_TYPE ="jakarta.ejb.MessageDrivenContext";
    private static final String EJB_CTX_TYPE ="jakarta.ejb.EJBContext";
    private static final String EJB_TIMER_SERVICE_TYPE
        = "jakarta.ejb.TimerService";
    private static final String VALIDATION_VALIDATOR ="jakarta.validation.Validator";
    private static final String VALIDATION_VALIDATOR_FACTORY ="jakarta.validation.ValidatorFactory";

    private static final String CDI_BEAN_MANAGER_TYPE = "jakarta.enterprise.inject.spi.BeanManager";

    public ResourceEnvReferenceDescriptor() {
    }

    public ResourceEnvReferenceDescriptor(String name, String description, String refType) {
        super(name, "", description);
        this.refType = refType;
    }

    public void setRefType(String refType) {
        this.refType = refType;

    }

    public String getRefType() {
        return this.refType;
    }

    public String getInjectResourceType() {
        return getRefType();
    }

    public void setInjectResourceType(String refType) {
        setRefType(refType);
    }

    public void setIsManagedBean(boolean flag) {
        isManagedBean = flag;
    }

    public boolean isManagedBean() {
        return isManagedBean;
    }

    public void setManagedBeanDescriptor(ManagedBeanDescriptor desc) {
        managedBeanDesc = desc;
    }

    public ManagedBeanDescriptor getManagedBeanDescriptor() {
        return managedBeanDesc;
    }

   /**
    * Return the jndi name of the destination to which I refer.
    */
    public String getJndiName() {
        String jndiName = this.getValue();
        if (! jndiName.equals("")) {
            return jndiName;
        }
        if (mappedName != null && ! mappedName.equals("")) {
            return mappedName;
        }
        return lookupName;
    }

   /**
    * Sets the jndi name of the destination to which I refer
    */
    public void setJndiName(String jndiName) {
        this.setValue(jndiName);
    }

    public boolean isEJBContext() {
        return (getRefType().equals(SESSION_CTX_TYPE) ||
                getRefType().equals(MDB_CTX_TYPE) ||
                getRefType().equals(EJB_CTX_TYPE) ||
                getRefType().equals(EJB_TIMER_SERVICE_TYPE));
    }

    public boolean isValidator() {
        return (getRefType().equals(VALIDATION_VALIDATOR));
    }

    public boolean isValidatorFactory() {
        return (getRefType().equals(VALIDATION_VALIDATOR_FACTORY));
    }

    public boolean isCDIBeanManager() {
        return (getRefType().equals(CDI_BEAN_MANAGER_TYPE));
    }

    public boolean isConflict(ResourceReferenceDescriptor other) {
        return (getName().equals(other.getName())) &&
            (!DOLUtils.equals(getType(), other.getType())
            || isConflictResourceGroup(other));
    }

    /* Equality on name. */
    public boolean equals(Object object) {
        if (object instanceof ResourceEnvReference) {
            ResourceEnvReference destReference = (ResourceEnvReference) object;
            return destReference.getName().equals(this.getName());
        }
        return false;
    }

    public int hashCode() {
        int result = NULL_HASH_CODE;
        String name = getName();
        if (name != null) {
            result += name.hashCode();
        }
        return result;
    }
    /**
     * Performs the same check as in ResourceReferenceDescriptor
     */
    public void checkType() {
        if (refType == null) {
            if (isBoundsChecking()) {
                throw new IllegalArgumentException(localStrings.getLocalString(
                        "enterprise.deployment.exceptiontypenotallowedpropertytype",
                        "{0} is not an allowed property value type",
                        new Object[]{"null"}));
            }
        }
        if (refType != null) {
            try {
                Class.forName(refType, true, Thread.currentThread().getContextClassLoader());
            } catch (Throwable t) {
                if (isBoundsChecking()) {
                    throw new IllegalArgumentException(localStrings.getLocalString(
                            "enterprise.deployment.exceptiontypenotallowedpropertytype",
                            "{0} is not an allowed property value type",
                            new Object[]{refType}));
                } else {
                    return;
                }
            }
        }
    }
}
