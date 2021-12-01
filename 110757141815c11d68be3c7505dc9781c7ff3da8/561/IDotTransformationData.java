/*******************************************************************************
 * Copyright (c) 2015 Kiel University and others.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.elk.alg.graphviz.dot.transform;

import java.util.List;

import org.eclipse.elk.graph.properties.IPropertyHolder;

/**
 * Interface around the layout format service's {@code TransformationData} class. This is required to
 * eliminate the dependency on the format service.
 * 
 * @param <S> source graph type
 * @param <T> target graph type
 * @author cds
 */
public interface IDotTransformationData<S, T> extends IPropertyHolder {
    
    /**
     * Set the original source graph.
     * 
     * @param srcGraph the source graph
     */
    void setSourceGraph(S srcGraph);
    
    /**
     * Returns the original source graph.
     * 
     * @return the source graph
     */
    S getSourceGraph();
    
    /**
     * Returns the transformed target graphs.
     * 
     * @return the target graphs
     */
    List<T> getTargetGraphs();
    
    /**
     * Report a log message.
     * 
     * @param msg a user friendly message
     */
    void log(String msg);
    
    /**
     * Returns the reported log messages.
     * 
     * @return the log messages
     */
    Iterable<String> getMessages();
}
