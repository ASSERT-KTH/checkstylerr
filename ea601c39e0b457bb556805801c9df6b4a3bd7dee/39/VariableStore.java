/*******************************************************************************
 * Copyright (c) 2014 BREDEX GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BREDEX GmbH - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.jubula.qa.api.converter.target.rcp;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @created 08.12.2014
 */
public class VariableStore {
    /** the logger */
    private static Logger log = LoggerFactory.getLogger(VariableStore.class);
    
    private static VariableStore instance;
    
    private Map<String, String> variablePool = new HashMap<String, String>();
    
    /**
     * Storing variables
     */
    private VariableStore() {
        //empty
    }
    
    public static VariableStore getInstance() {
        if (instance == null) {
            instance = new VariableStore();
        }
        return instance;
    }
    
    public void addVariable(String name, String value) {
        variablePool.put(name, value);
    }
    
    public String getValue(String variableName) {
        String value = variablePool.get(variableName);
        if (value == null) {
            log.error("Variable '" + variableName + "' was not set in variable pool."); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return value;
    }
}