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

import java.io.IOException;
import java.net.URL;
import java.util.Stack;

import org.eclipse.jubula.client.AUT;
import org.eclipse.jubula.client.MakeR;
import org.eclipse.jubula.client.ObjectMapping;
import org.eclipse.jubula.client.exceptions.ActionException;
import org.eclipse.jubula.client.exceptions.CheckFailedException;
import org.eclipse.jubula.client.exceptions.ComponentNotFoundException;
import org.eclipse.jubula.client.exceptions.ConfigurationException;
import org.eclipse.jubula.client.exceptions.ExecutionException;
import org.eclipse.jubula.client.exceptions.ExecutionExceptionHandler;
import org.eclipse.jubula.tools.ComponentIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @created 19.11.2014
 */
public class RuntimeContext {
    /** the logger */
    private static Logger log = LoggerFactory
            .getLogger(RuntimeContext.class);
    
    private static class ContinueExecutionHandler 
        implements ExecutionExceptionHandler {
        /** nesting level counter */
        private Stack<Boolean> m_checkFailedStack = new Stack<Boolean>();
        /** nesting level counter */
        private Stack<Boolean> m_actionErrorStack = new Stack<Boolean>();
        /** nesting level counter */
        private Stack<Boolean> m_compNotFoundStack = new Stack<Boolean>();
        /** nesting level counter */
        private Stack<Boolean> m_configurationErrorStack = new Stack<Boolean>();

        /**
         * @param suppressCheckFailedDefault
         *            whether to suppress CheckFailedExceptions by default
         * @param suppressActionErrorDefault
         *            whether to suppress ActionErrorsExceptions by default
         * @param suppressCompNotFoundDefault
         *            whether to suppress CompNotFoundsExceptions by default
         * @param suppressConfigurationErrorDefault
         *            whether to suppress ConfigurationErrors by default
         */
        public ContinueExecutionHandler(Boolean suppressCheckFailedDefault,
                boolean suppressActionErrorDefault,
                boolean suppressCompNotFoundDefault,
                boolean suppressConfigurationErrorDefault) {
            getCheckFailedStack().push(suppressCheckFailedDefault);
            getActionErrorStack().push(suppressActionErrorDefault);
            getCompNotFoundStack().push(suppressCompNotFoundDefault);
            getConfigurationErrorStack().push(suppressConfigurationErrorDefault);
        }    
        
        /** special handling supports ignoring of check failed exceptions */
        public void handle(ExecutionException arg0) throws ExecutionException {
            if ((arg0 instanceof CheckFailedException)
                    && getCheckFailedStack().peek()) {
                return;
            } else if ((arg0 instanceof ActionException)
                    && getActionErrorStack().peek()) {
                return;
            } else if ((arg0 instanceof ComponentNotFoundException)
                    && getCompNotFoundStack().peek()) {
                return;
            }
            if ((arg0 instanceof ConfigurationException)
                    && getConfigurationErrorStack().peek()) {
                return;
            }
            throw arg0;
        }

        /**
         * @return the checkFailedStack
         */
        public Stack<Boolean> getCheckFailedStack() {
            return m_checkFailedStack;
        }

        /**
         * @return the actionErrorStack
         */
        public Stack<Boolean> getActionErrorStack() {
            return m_actionErrorStack;
        }

        /**
         * @return the compNotFoundStack
         */
        public Stack<Boolean> getCompNotFoundStack() {
            return m_compNotFoundStack;
        }

        /**
         * @return the configurationErrorStack
         */
        public Stack<Boolean> getConfigurationErrorStack() {
            return m_configurationErrorStack;
        }
    }
    
    /** the AUT */
    private AUT m_aut;
    
    /** the object map to use */
    private ObjectMapping om;

    /** the event handler for this runtime context */
    private ContinueExecutionHandler m_eventHandler;

    /**
     * @param aut
     *            the AUT
     * @param suppressCheckFailedDefault
     *            whether to suppress CheckFailedExceptions by default
     * @param suppressActionErrorDefault
     *            whether to suppress ActionErrorsExceptions by default
     * @param suppressCompNotFoundDefault
     *            whether to suppress CompNotFoundsExceptions by default
     * @param suppressConfigurationErrorDefault
     *            whether to suppress ConfigurationErrors by default
     */
    public RuntimeContext(AUT aut, boolean suppressCheckFailedDefault,
            boolean suppressActionErrorDefault,
            boolean suppressCompNotFoundDefault,
            boolean suppressConfigurationErrorDefault) {
        setAUT(aut);
        m_eventHandler = new ContinueExecutionHandler(
                suppressCheckFailedDefault, suppressActionErrorDefault,
                suppressCompNotFoundDefault, suppressConfigurationErrorDefault);
        aut.setHandler(m_eventHandler);
        
        // load object mapping - hint: feel free to adjust
        URL resource = RuntimeContext.class.getClassLoader().getResource(
                "om.properties"); //$NON-NLS-1$
        try {
            om = MakeR.createObjectMapping(resource.openStream());
        } catch (IOException e) {
            log.error(e.getLocalizedMessage(), e);
        }
    }

    /**
     * @return the AUT
     */
    public AUT getAUT() {
        return m_aut;
    }

    /**
     * @param aut the AUT to set
     */
    private void setAUT(AUT aut) {
        m_aut = aut;
    }

    /**
     * Gets a component identifier for a given logical component name 
     * from the object mapping for the AUT
     * @param name the logical component name
     * @return the component identifier
     */
    public ComponentIdentifier getIdentifier(String name) {
        return om.get(name);
    }
    
    /* **************************************************
     * Handling of CheckFailedExceptions
     * ************************************************** */
    
    /**
     * @return the current event stack
     */
    private Stack<Boolean> getCheckFailedStack() {
        return m_eventHandler.getCheckFailedStack();
    }
    
    /**
     * Begins local ignoring of
     * {@link org.eclipse.jubula.client.exceptions.CheckFailedException}.
     * 
     * Call {@link org.eclipse.jubula.qa.api.converter.target.rcp.RuntimeContext.endLocalEventHandling(true, ?, ?, ?)}
     * to end the scope.
     */
    public void beginIgnoreCheckFailed() {
        getCheckFailedStack().push(true);
    }
    
    /**
     * Begins local respecting of
     * {@link org.eclipse.jubula.client.exceptions.CheckFailedException}.
     * 
     * Call {@link org.eclipse.jubula.qa.api.converter.target.rcp.RuntimeContext.endLocalEventHandling(true, ?, ?, ?)}
     * to end the scope.
     */
    public void doNotIgnoreCheckFailed() {
        getCheckFailedStack().push(false);
    }
    
    /* **************************************************
     * Handling of ActionErrors
     * ************************************************** */
    
    /**
     * @return the current event stack for handling action errors
     */
    private Stack<Boolean> getActionErrorStack() {
        return m_eventHandler.getActionErrorStack();
    }
    
    /**
     * Begins local ignoring of
     * {@link org.eclipse.jubula.client.exceptions.ActionException}.
     * 
     * Call {@link org.eclipse.jubula.qa.api.converter.target.rcp.RuntimeContext.endLocalEventHandling(?, true, ?, ?)}
     * to end the scope.
     */
    public void beginIgnoreActionError() {
        getActionErrorStack().push(true);
    }
    
    /**
     * Begins local respecting of
     * {@link org.eclipse.jubula.client.exceptions.ActionException}.
     * 
     * Call {@link org.eclipse.jubula.qa.api.converter.target.rcp.RuntimeContext.endLocalEventHandling(?, true, ?, ?)}
     * to end the scope.
     */
    public void doNotIgnoreActionError() {
        getActionErrorStack().push(false);
    }
    
    /* **************************************************
     * Handling of ComponentNotFoundExceptions
     * ************************************************** */
    
    /**
     * @return the current event stack
     */
    private Stack<Boolean> getCompNotFoundStack() {
        return m_eventHandler.getCompNotFoundStack();
    }
    
    /**
     * Begins local ignoring of
     * {@link org.eclipse.jubula.client.exceptions.ComponentNotFoundException}.
     * 
     * Call {@link org.eclipse.jubula.qa.api.converter.target.rcp.RuntimeContext.endLocalEventHandling(?, ?, true, ?)}
     * to end the scope.
     */
    public void beginIgnoreCompNotFound() {
        getCompNotFoundStack().push(true);
    }
    
    /**
     * Begins local respecting of
     * {@link org.eclipse.jubula.client.exceptions.ComponentNotFoundException}.
     * 
     * Call {@link org.eclipse.jubula.qa.api.converter.target.rcp.RuntimeContext.endLocalEventHandling(?, ?, true, ?)}
     * to end the scope.
     */
    public void doNotIgnoreCompNotFound() {
        getCompNotFoundStack().push(false);
    }
    
    /* **************************************************
     * Handling of CheckFailedExceptions
     * ************************************************** */
    
    /**
     * @return the current event stack
     */
    private Stack<Boolean> getConfigurationErrorStack() {
        return m_eventHandler.getConfigurationErrorStack();
    }
    
    /**
     * Begins local ignoring of
     * {@link org.eclipse.jubula.client.exceptions.ComponentNotFoundException}.
     * 
     * Call {@link org.eclipse.jubula.qa.api.converter.target.rcp.RuntimeContext.endLocalEventHandling(?, ?, ?, true)}
     * to end the scope.
     */
    public void beginIgnoreConfigurationError() {
        getConfigurationErrorStack().push(true);
    }
    
    /**
     * Begins local respecting of
     * {@link org.eclipse.jubula.client.exceptions.ComponentNotFoundException}.
     * 
     * Call {@link org.eclipse.jubula.qa.api.converter.target.rcp.RuntimeContext.endLocalEventHandling(?, ?, ?, true)}
     * to end the scope.
     */
    public void doNotIgnoreConfigurationError() {
        getConfigurationErrorStack().push(false);
    }

    /* **************************************************
     * Handling of ending of local event handling
     * ************************************************** */
    
    /**
     * Ends the scope of local event handling and restores previous state.
     */
    public void endLocalEventHandling(boolean checkFailed, boolean actionError,
            boolean compNotFound, boolean configurationError) {
        if (checkFailed) {
            getCheckFailedStack().pop();
        }
        if (actionError) {
            getActionErrorStack().pop();
        }
        if (compNotFound) {
            getCompNotFoundStack().pop();
        }
        if (configurationError) {
            getConfigurationErrorStack().pop();
        }
    }
}