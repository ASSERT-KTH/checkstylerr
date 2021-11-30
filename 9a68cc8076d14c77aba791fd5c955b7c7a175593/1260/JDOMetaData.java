/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jdo.api.persistence.enhancer.meta;


/**
 * Provides the JDO meta information neccessary for byte-code enhancement.
 * <p>
 * <b>Please note: This interface deals with fully qualified names in the
 * JVM notation, that is, with '/' as package separator character&nbsp; (instead
 * of '.').</b>
 * <p>
 * The following convention is used to specify the format of a given name:
 * Something called ...
 * <ul>
 * <li>
 * <i>name</i> represents a non-qualified name (e.g. <code>JDOPersistenceCapableName</code>
 * = "<code>PersistenceCapable</code>")</li>
 * <li>
 * <i>type</i> represents a Java-qualified class name (e.g. <code>JDOPersistenceCapablePath</code>
 * = '<code>com.sun.jdo.spi.persistence.support.sqlstore.PersistenceCapable</code>")</li>
 * <li>
 * <i>path</i> represents a JVM-qualified name (e.g. <code>JDOPersistenceCapablePath</code>
 * = '<code>com/sun/jdo/spi/persistence/support/sqlstore/PersistenceCapable</code>")</li>
 * <li>
 * <i>sig</i> (for <i>signature</i>) represents a JVM-qualified type-signature
 * name (e.g. <code>JDOPersistenceCapableSig</code>
 * = "L<code>com/sun/jdo/spi/persistence/support/sqlstore/PersistenceCapable;</code>")</li>
 * </ul>
 */
//@olsen: new interface
public interface JDOMetaData {

    String JDOExternalPath = "com/sun/jdo/api/persistence/support/";// NOI18N
    String JDOPath = "com/sun/jdo/spi/persistence/support/sqlstore/";// NOI18N

    String JDOPersistenceCapableName = "PersistenceCapable";// NOI18N
    String JDOPersistenceCapablePath = JDOPath + JDOPersistenceCapableName;// NOI18N
    String JDOPersistenceCapableSig = "L" + JDOPersistenceCapablePath + ";";// NOI18N
    String JDOPersistenceCapableType = JDOPersistenceCapablePath.replace('/', '.');

    String javaLangCloneablePath = "java/lang/Cloneable";

    String JDOInstanceCallbacksName = "InstanceCallbacks";// NOI18N
    String JDOInstanceCallbacksPath = JDOPath + JDOInstanceCallbacksName;// NOI18N
    String JDOInstanceCallbacksSig = "L" + JDOInstanceCallbacksPath + ";";// NOI18N
    String JDOInstanceCallbacksType = JDOInstanceCallbacksPath.replace('/', '.');

    String JDOSecondClassObjectBaseName = "SCO";// NOI18N
    String JDOSecondClassObjectBasePath = JDOPath + JDOSecondClassObjectBaseName;// NOI18N
    String JDOSecondClassObjectBaseSig = "L" + JDOSecondClassObjectBasePath + ";";// NOI18N
    String JDOSecondClassObjectBaseType = JDOSecondClassObjectBasePath.replace('/', '.');

    String JDOPersistenceManagerName = "PersistenceManager";// NOI18N
    // we use the external, "public" PersistenceManager interface only
    String JDOPersistenceManagerPath = JDOExternalPath + JDOPersistenceManagerName;// NOI18N
    String JDOPersistenceManagerSig = "L" + JDOPersistenceManagerPath + ";";// NOI18N
    String JDOPersistenceManagerType = JDOPersistenceManagerPath.replace('/', '.');

    String JDOStateManagerName = "StateManager";// NOI18N
    String JDOStateManagerPath = JDOPath + JDOStateManagerName;// NOI18N
    String JDOStateManagerSig = "L" + JDOStateManagerPath + ";";// NOI18N
    String JDOStateManagerType = JDOStateManagerPath.replace('/', '.');

    String JDOStateManagerFieldName = "jdoStateManager";//NOI18N
    String JDOStateManagerFieldType = JDOStateManagerType;
    String JDOStateManagerFieldSig = JDOStateManagerSig;

    String JDOFlagsFieldName = "jdoFlags";//NOI18N
    String JDOFlagsFieldType = "byte";//NOI18N
    String JDOFlagsFieldSig = "B";//NOI18N

    /**
     * Tests whether a class is known to be transient.
     * <P>
     * The following invariant holds:
     *   isTransientClass(classPath)
     *       => !isPersistenceCapableClass(classPath)
     * @param classPath the JVM-qualified name of the class
     * @return true if this class is known to be transient; otherwise false
     */
    boolean isTransientClass(String classPath)
        throws JDOMetaDataUserException, JDOMetaDataFatalError;

    /**
     * Tests whether a class is known to be persistence-capable.
     * <P>
     * The following invariant holds:
     *   isPersistenceCapableClass(classPath)
     *       => !isTransientClass(classPath)
     *          && !isSecondClassObjectType(classPath)
     * @param classPath the JVM-qualified name of the class
     * @return true if this class is persistence-capable; otherwise false
     */
    boolean isPersistenceCapableClass(String classPath)
        throws JDOMetaDataUserException, JDOMetaDataFatalError;

    /**
     * Tests whether a class is known as a persistence-capable root class.
     * <P>
     * @param classPath the JVM-qualified name of the class
     * @return true if this class is persistence-capable and does not
     *         derive from another persistence-capable class; otherwise false
     */
    boolean isPersistenceCapableRootClass(String classPath)
        throws JDOMetaDataUserException, JDOMetaDataFatalError;

    /**
     * Returns the name of the persistence-capable root class of a class.
     * <P>
     * @param classPath the JVM-qualified name of the class
     * @return the name of the least-derived persistence-capable class that
     *         is equal to or a super class of the argument class; if the
     *         argument class is not persistence-capable, null is returned.
     */
    String getPersistenceCapableRootClass(String classPath)
        throws JDOMetaDataUserException, JDOMetaDataFatalError;

    /**
     * Returns the name of the superclass of a class.
     * <P>
     * @param classPath the JVM-qualified name of the class
     * @return the name of the superclass.
     */
    String getSuperClass(String classPath)
        throws JDOMetaDataUserException, JDOMetaDataFatalError;

    /**
     * Tests whether a class is known as type for Second Class Objects.
     * <P>
     * The following invariant holds:
     *   isSecondClassObjectType(classPath)
     *       => !isPersistenceCapableClass(classPath)
     * @param classPath the JVM-qualified name of the type
     * @return true if this type is known for second class objects;
     *         otherwise false
     */
    boolean isSecondClassObjectType(String classPath)
        throws JDOMetaDataUserException, JDOMetaDataFatalError;

    /**
     * Tests whether a class is known as type for Mutable Second Class Objects.
     * <P>
     * @param classPath the JVM-qualified name of the type
     * @return true if this type is known for mutable second class objects;
     *         otherwise false
     */
    boolean isMutableSecondClassObjectType(String classPath)
        throws JDOMetaDataUserException, JDOMetaDataFatalError;

    /**
     * Tests whether a field of a class is known to be persistent.
     * <P>
     * @param classPath the JVM-qualified name of the class
     * @param fieldName the name of the field
     * @return true if this field is known to be persistent; otherwise false
     */
    boolean isPersistentField(String classPath, String fieldName)
        throws JDOMetaDataUserException, JDOMetaDataFatalError;

    /**
     * Tests whether a field of a class is known to be transactional.
     * <P>
     * @param classPath the JVM-qualified name of the class
     * @param fieldName the name of the field
     * @return true if this field is known to be transactional; otherwise false
     */
    boolean isTransactionalField(String classPath, String fieldName)
        throws JDOMetaDataUserException, JDOMetaDataFatalError;

    /**
     * Tests whether a field of a class is known to be Primary Key.
     * <P>
     * @param classPath the JVM-qualified name of the class
     * @param fieldName the name of the field
     * @return true if this field is known to be primary key; otherwise false
     */
    boolean isPrimaryKeyField(String classPath, String fieldName)
        throws JDOMetaDataUserException, JDOMetaDataFatalError;

    /**
     * Tests whether a field of a class is known to be part of the
     * Default Fetch Group.
     * <P>
     * @param classPath the JVM-qualified name of the class
     * @param fieldName the name of the field
     * @return true if this field is known to be part of the
     *         default fetch group; otherwise false
     */
    boolean isDefaultFetchGroupField(String classPath, String fieldName)
        throws JDOMetaDataUserException, JDOMetaDataFatalError;

    /**
     * Returns the unique field index of a declared, persistent field of a
     * class.
     * <P>
     * @param classPath the JVM-qualified name of the class
     * @param fieldName the name of the field
     * @return the non-negative, unique field index
     */
    int getFieldNo(String classPath, String fieldName)
        throws JDOMetaDataUserException, JDOMetaDataFatalError;

    /**
     * Returns an array of field names of all declared persistent and
     * transactional fields of a class.
     * <P>
     * The position of the field names in the result array corresponds
     * to their unique field index as returned by getFieldNo such that
     * these equations holds:
     * <P> getFieldNo(getManagedFields(classPath)[i]) == i
     * <P> getManagedFields(classPath)[getFieldNo(fieldName)] == fieldName
     * <P>
     * @param classPath the JVM-qualified name of the class
     * @return an array of all declared persistent and transactional
     *         fields of a class
     */
    String[] getManagedFields(String classPath)
        throws JDOMetaDataUserException, JDOMetaDataFatalError;
}
