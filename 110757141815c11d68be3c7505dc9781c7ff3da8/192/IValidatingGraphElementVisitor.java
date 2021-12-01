/*******************************************************************************
 * Copyright (c) 2016 TypeFox GmbH and others.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.elk.core.validation;

import java.util.Collection;

import org.eclipse.elk.core.util.IGraphElementVisitor;

/**
 * Interface for graph element visitors that do some kind of validation. The validation result can be obtained
 * after visitation has finished.
 * 
 * <p>Note: An instance of such a visitor should not be used more than once because it remembers the
 * issues found during the last run.</p>
 */
public interface IValidatingGraphElementVisitor extends IGraphElementVisitor {
    
    /**
     * The issues found by this validator.
     */
    Collection<GraphIssue> getIssues();

}
