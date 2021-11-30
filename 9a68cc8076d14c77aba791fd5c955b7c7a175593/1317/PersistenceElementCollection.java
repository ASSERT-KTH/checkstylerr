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
 * PersistenceElementCollection.java
 *
 * Created on March 6, 2000, 2:20 PM
 */

package com.sun.jdo.api.persistence.model.jdo.impl;

import java.util.*;
import java.beans.PropertyVetoException;

import com.sun.jdo.api.persistence.model.ModelException;
import com.sun.jdo.api.persistence.model.ModelVetoException;
import com.sun.jdo.api.persistence.model.jdo.*;

/**
 *
 * @author raccah
 * @version %I%
 */
public class PersistenceElementCollection
{
    /** Owner of the collection. */
    private PersistenceElementImpl _owner;

    /** Elements of the collection. */
    private PersistenceElement[] _elements;

    /** Array template for typed returns */
    private Object[] _template;

    /** Property name. */
    private String _propertyName;

    /** Create new PersistenceElementCollection with no owner, property, or
     * template.  This constructor should only be used for cloning and
     * archiving.
     */
    public PersistenceElementCollection ()
    {
        this(null, null, null);
    }

    /** Creates new PersistenceElementCollection */
    public PersistenceElementCollection (PersistenceElementImpl owner,
        String propertyName, Object[] template)
    {
        _owner = owner;
        _propertyName = propertyName;
        _template = template;
    }

    /** Change the set of elements.
     * @param elements the new elements
     * @param action {@link com.sun.jdo.api.persistence.model.jdo.PersistenceElement.Impl#ADD},
     * {@link com.sun.jdo.api.persistence.model.jdo.PersistenceElement.Impl#REMOVE}, or
     * {@link com.sun.jdo.api.persistence.model.jdo.PersistenceElement.Impl#SET}
     * @exception ModelException if impossible
     */
    public void changeElements (PersistenceElement[] elements, int action)
        throws ModelException
    {
        changeElements(Arrays.asList(elements), action);
    }

    /** Change the set of elements.
     * @param elements the new elements
     * @param action {@link com.sun.jdo.api.persistence.model.jdo.PersistenceElement.Impl#ADD},
     * {@link com.sun.jdo.api.persistence.model.jdo.PersistenceElement.Impl#REMOVE}, or
     * {@link com.sun.jdo.api.persistence.model.jdo.PersistenceElement.Impl#SET}
     * @exception ModelException if impossible
     */
    public void changeElements (List elements, int action)
        throws ModelException
    {
        boolean changed = false;

        try
        {
            PersistenceElement[] oldElements = getElements();
            int oldLength = (oldElements == null) ? 0 : oldElements.length;
            int newLength = (elements == null) ? 0 : elements.size();
            List list = null;

            switch (action)
            {
                case PersistenceElement.Impl.SET:
                    list = elements;
                    changed = true;
                    break;
                case PersistenceElement.Impl.ADD:
                    if (newLength > 0)
                    {
                        list = ((oldLength == 0) ? new ArrayList() :
                            new ArrayList(Arrays.asList(oldElements)));
                        list.addAll(elements);
                        changed = true;
                    }
                    break;
                case PersistenceElement.Impl.REMOVE:
                    if ((newLength > 0) && (oldLength > 0))
                    {
                        list = new ArrayList(Arrays.asList(oldElements));
                        list.removeAll(elements);
                        changed = true;
                    }
                    break;
            }
            if (changed)
            {
                try
                {
                    _owner.fireVetoableChange(_propertyName, null, null);
                    _elements = (PersistenceElement[])list.toArray(_template);
                }
                catch (PropertyVetoException e)
                {
                    throw new ModelVetoException(e);
                }
            }
        }
        finally
        {
            if (changed)
                _owner.firePropertyChange(_propertyName, null, null);
        }
    }

    /** Returns the collection of elements maintained by this holder in the form
     * of an array.
     * @return the elements maintained by this collection
     */
    public PersistenceElement[] getElements () { return _elements; }

    /** Returns the element with the supplied name from the collection of
     * elements maintained by this collection.
     * @param name the name to match
     * @return the element with the supplied name, <code>null</code> if none
     * exists
     */
    public PersistenceElement getElement (String name)
    {
        PersistenceElement[] elements = getElements();
        int i, count = ((elements != null) ? elements.length : 0);

        for (i = 0; i < count; i++)
        {
            PersistenceElement element = elements[i];

            if (name.equals(element.getName()))
                return element;
        }

        return null;
    }

    //=============== extra methods needed for xml archiver ==============

    /** Returns the owner of this collection.  This method should only
     * be used internally and for cloning and archiving.
     * @return the owner of this collection
     */
    public PersistenceElementImpl getOwner () { return _owner; }

    /** Set the owner of this collection to the supplied implementation.
     * This method should only be used internally and for cloning and
     * archiving.
     * @param owner the owner of this collection
     */
    public void setOwner (PersistenceElementImpl owner)
    {
        _owner = owner;
    }

    /** Returns the template for the array of this collection.  This method
     * should only be used internally and for cloning and archiving.
     * @return the typed template of this collection
     */
    public Object[] getTemplate () { return _template; }

    /** Set the template for the array of this collection to the supplied
     * array.  This template is used so the array returned by getElements is
     * properly typed.  This method should only be used internally and
     * for cloning and archiving.
     * @param template the typed template of this collection
     */
    public void setTemplate (Object[] template) { _template = template; }

    /** Returns the property name of this collection.  This method
     * should only be used internally and for cloning and archiving.
     * @return the property name for this collection
     */
    public String getPropertyName () { return _propertyName; }

    /** Set the property name of this collection to the supplied name.
     * This name is used to generate the correct property change event on
     * changes to the collection.  This method should only be used
     * internally and for cloning and archiving.
     * @param propertyName the property name for this collection
     */
    public void setPropertyName (String propertyName)
    {
        _propertyName = propertyName;
    }

    /** Set the collection of elements maintained by this holder to the
     * supplied array.  This method should only be used internally and for
     * cloning and archiving.
     * @param elements the collection of elements maintained by this holder
     */
    public void setElements (PersistenceElement[] elements)
    {
        _elements = elements;
    }
}
