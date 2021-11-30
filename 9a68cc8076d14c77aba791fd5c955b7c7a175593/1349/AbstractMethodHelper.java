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

/*
 * AbstractMethodHelper.java
 *
 * Created on December 20, 2001, 5:30 PM
 */

package com.sun.jdo.spi.persistence.support.ejb.ejbc;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.sun.enterprise.deployment.MethodDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbCMPEntityDescriptor;
import org.glassfish.ejb.deployment.descriptor.PersistenceDescriptor;
import org.glassfish.ejb.deployment.descriptor.QueryDescriptor;

/** This is a helper class which extracts the information needed for method
 * code generation of the concrete bean class.
 *
 * @author Rochelle Raccah
 */
abstract public class AbstractMethodHelper
{
    /** Constant representing a local interface return type. */
    public static final int LOCAL_RETURN = 0;

    /** Constant representing a remote interface return type. */
    public static final int REMOTE_RETURN = 1;

    /** Constant representing no return type. */
    public static final int NO_RETURN = 2;

    private EjbCMPEntityDescriptor _cmpDescriptor;
    private List finders = new ArrayList();
    private List selectors = new ArrayList();
    //private ArrayList otherMethods = new ArrayList();
    private List createMethods = new ArrayList();
    private Map methodNames = new HashMap();

    /** Creates a new instance of AbstractMethodHelper
     * @param descriptor the EjbCMPEntityDescriptor which defines the
     * information for this bean.
     */
    public AbstractMethodHelper (EjbCMPEntityDescriptor descriptor)
    {
        _cmpDescriptor = descriptor;
        categorizeMethods();    // Separate methods into categories.
    }

    /** Gets the EjbCMPEntityDescriptor which defines the
     * information for this bean.
     * @return the EjbCMPEntityDescriptor for the bean specified in the
     * constructor.
     */
    protected EjbCMPEntityDescriptor getDescriptor() { return _cmpDescriptor; }

    /**
     * Reads all known methods and sorts them by name into specific
     * Collections for further processing.
     */
    protected void categorizeMethods ()
    {
        EjbCMPEntityDescriptor descriptor = getDescriptor();
        Iterator iterator = descriptor.getMethodDescriptors().iterator();

        while (iterator.hasNext())
        {
            MethodDescriptor methodDescriptor =
                (MethodDescriptor)iterator.next();
            Method method = methodDescriptor.getMethod(descriptor);
            String methodName = methodDescriptor.getName();

            //if (DEBUG)
            //    System.out.println("Method: " + methodName); // NOI18N

            if (methodName.startsWith(CMPTemplateFormatter.find_))
                finders.add(method);
            else if (methodName.startsWith(CMPTemplateFormatter.ejbSelect_))
                selectors.add(method);
            else if (methodName.startsWith(CMPTemplateFormatter.create_))
                createMethods.add(method);
            else if (methodName.startsWith(CMPTemplateFormatter.get_) ||
                methodName.startsWith(CMPTemplateFormatter.set_))
            {
                ;// skip
            }
            //else
            //    otherMethods.add(method);

            // It is OK to use HashMap here as we won't use it for possible
            // overloaded methods.
            methodNames.put(methodName, method);
        }
    }

    /** Gets the list of finder methods for this bean.
     * @return a list of java.lang.reflect.Method objects which represent
     * the finders for this bean
     */
    public List getFinders () { return finders; }

    // give subclasses a chance to replace the list
    protected void setFinders (List finderList)
    {
        finders = finderList;
    }

    /** Gets the list of selector methods for this bean.
     * @return a list of java.lang.reflect.Method objects which represent
     * the selectors for this bean
     */
    public List getSelectors () { return selectors; }

    // give subclasses a chance to replace the list
    protected void setSelectors (List selectorList)
    {
        selectors = selectorList;
    }

    /** Gets the list of ejb create methods for this bean.
     * @return a list of java.lang.reflect.Method objects which represent
     * the ejb create methods for this bean
     */
    public List getCreateMethods () { return createMethods; }

    // might need this later
    //public List getOtherMethods () { return otherMethods; }

    /** Gets a map of the method names for this bean.  The keys are the
     * method names and the values are the java.lang.reflect.Method objects.
     * These should represent all methods of this bean.
     * @return a map of the method names to java.lang.reflect.Method objects
     * for this bean
     */
    public Map getMethodNames () { return methodNames; }

    /** Gets the name of the local home which corresponds to this bean.
     * @return the name of the local home class
     */
    public String getLocalHome ()
    {
        return getDescriptor().getLocalHomeClassName();
    }

    /** Gets the name of the remote home which corresponds to this bean.
     * @return the name of the remote home class
     */
    public String getRemoteHome ()
    {
        return getDescriptor().getHomeClassName();
    }

    /** Gets the query descriptor associated with the specified method if it
     * exists.
     * @param method the java.lang.reflect.Method object used to find the
     * query string
     * @return a query descriptor for the specified method. Returns
     * <code>null</code> for CMP 1.1 queries.
     */
    protected QueryDescriptor getQueryDescriptor (Method method)
    {
        PersistenceDescriptor persistenceDescriptor =
            getDescriptor().getPersistenceDescriptor();
        return persistenceDescriptor.getQueryFor(method);
    }

    /** Gets the query string associated with the specified method if it
     * exists.
     * @param method the java.lang.reflect.Method object used to find the
     * query string
     * @return a query string for the specified method
     */
    public String getQueryString (Method method)
    {
        QueryDescriptor queryDescriptor = getQueryDescriptor(method);

        return ((queryDescriptor != null) ? queryDescriptor.getQuery() : null);
    }

    /** Gets the return type associated with the specified method if it
     * exists.  If no corresponding query descriptor is found, the value
     * <code>NO_RETURN</code> is returned.
     * @param method the java.lang.reflect.Method object used to find the
     * query return type
     * @return the return type for the specified method, one of
     * {@link #LOCAL_RETURN}, {@link #REMOTE_RETURN}, or {@link #NO_RETURN}
     */
    public int getQueryReturnType (Method method)
    {
        QueryDescriptor queryDescriptor = getQueryDescriptor(method);

        if (queryDescriptor != null)
        {
            if (queryDescriptor.getHasLocalReturnTypeMapping())
                return LOCAL_RETURN;
            if (queryDescriptor.getHasRemoteReturnTypeMapping())
                return REMOTE_RETURN;
        }

        return NO_RETURN;
    }

    /** Returns <code>true</code> if prefetch is enabled for the specified
     * method, <code>false</code> otherwise. Prefetch is enabled by default.
     * @param method the java.lang.reflect.Method object used to find the
     * prefetch setting.
     * @return a boolean representing the prefetch setting
     */
    abstract public boolean isQueryPrefetchEnabled (Method method);

    /** Gets the jdo filter expression associated with the specified method
     * if it exists.  Note that this method should only be used for CMP 1.1 -
     * use {@link #getQueryString} for CMP 2.0.
     * @param method the java.lang.reflect.Method object used to find the
     * query filter
     * @return the jdo filter expression
     */
    abstract public String getJDOFilterExpression (Method method);

    /** Gets the jdo parameter declaration associated with the specified
     * method if it exists.  Note that this method should only be used for
     * CMP 1.1 - use {@link #getQueryString} for CMP 2.0.
     * @param method the java.lang.reflect.Method object used to find the
     * parameter declaration
     * @return the jdo parameter declaration
     */
    abstract public String getJDOParameterDeclaration (Method method);

    /** Gets the jdo variables declaration associated with the specified
     * method if it exists.  Note that this method should only be used for
     * CMP 1.1 - use {@link #getQueryString} for CMP 2.0.
     * @param method the java.lang.reflect.Method object used to find the
     * parameter declaration
     * @return the jdo variables declaration
     */
    abstract public String getJDOVariableDeclaration (Method method);

    /** Gets the jdo ordering specification associated with the specified
     * method if it exists.  Note that this method should only be used for
     * CMP 1.1 - use {@link #getQueryString} for CMP 2.0.
     * @param method the java.lang.reflect.Method object used to find the
     * parameter declaration
     * @return the jdo ordering specification
     */
    abstract public String getJDOOrderingSpecification (Method method);
}
