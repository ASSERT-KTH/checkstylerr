/*******************************************************************************
 * Copyright (c) 2004, 2010 BREDEX GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BREDEX GmbH - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.jubula.client.core.model;

import org.eclipse.jubula.client.core.businessprocess.IParamNameMapper;

/**
 * @author BREDEX GmbH
 * @created Jul 16, 2010
 */
public interface IModifiableParameterInterfacePO extends IParameterInterfacePO {
    
    /**
     * Adds a parameter to the parameter list to call for each parameter of
     * specTestCase.
     * 
     * @param type
     *            type of parameter (e.g. String)
     * @param name
     *            name of parameter
     * @param mapper mapper to resolve param names
     * @return The new parameter description
     */
    public abstract IParamDescriptionPO addParameter(String type, String name, 
        IParamNameMapper mapper);
    
    /**
     * Adds a parameter to the parameter list to call for each parameter of
     * specTestCase.
     * 
     * @param type
     *            type of parameter (e.g. String)
     * @param name
     *            name of parameter
     * @param guid of parameter           
     * @param mapper mapper to resolve and persist param names
     * @return The new parameter description
     */
    public IParamDescriptionPO addParameter(String type, String name,
        String guid, IParamNameMapper mapper);
    
    /**
     * Adds a parameter to the node's list of parameters. if <code>always</code>
     * is <code>true</code>, the parameter will be added even if a parameter
     * with <code>userDefName</code> already exists.
     * 
     * @param type
     *            The parameter type
     * @param userDefName
     *            The userdefined name of the parameter
     * @param always
     *            If <code>true</code>, a parameter might be added several
     *            times, if <code>false</code>, it will not be added if the
     *            <code>userDefName</code> already exists
     * @param mapper mapper to resolve param names
     * * @return The new parameter description or null
     */
    public abstract IParamDescriptionPO addParameter(
        String type, String userDefName,
        boolean always, IParamNameMapper mapper);
    
    /**
     * Removes the given parameter from the parameter list. The method also
     * removes the corresponding test data from the own test data manager and
     * from all associated test execution nodes with own (non-referenced) data
     * managers.
     * 
     * {@inheritDoc}
     * 
     * @param uniqueId
     *            the uniqueId of the parameter to be removed.
     */
    public abstract void removeParameter(String uniqueId);
    
    /**
     * Moves the Parameter with the given GUID to the given index.
     * @param guId the GUIS of the parameter whichg is to move.
     * @param index the zero based index to move to.
     */
    public void moveParameter(String guId, int index);
}
