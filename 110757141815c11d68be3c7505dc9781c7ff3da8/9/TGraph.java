/*******************************************************************************
 * Copyright (c) 2013, 2015 Kiel University and others.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.elk.alg.mrtree.graph;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.elk.graph.properties.MapPropertyHolder;

/**
 * A graph for the T layouter. It consists of node, edges and labels.
 * 
 * @author sor
 * @author sgu
 */
public class TGraph extends MapPropertyHolder {

    /** the serial version UID. */
    private static final long serialVersionUID = 1L;

    /** All nodes of this graph. */
    private LinkedList<TNode> nodes = new LinkedList<TNode>();
    /** All edges of this graph. */
    private LinkedList<TEdge> edges = new LinkedList<TEdge>();
   
    /**
     * Default constructor that creates an empty graph.
     * 
     */
    public TGraph() {
        this.nodes = new LinkedList<TNode>();
        this.edges = new LinkedList<TEdge>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        String tmp = null;
        for (TNode tNode : nodes) {
            tmp += tNode.toString() + "\n";
        }
        for (TEdge tEdge : edges) {
            tmp += tEdge.toString() + "\n";
        }
        return tmp;
    }

    /**
     * Returns the list of edges for this graph.
     * 
     * @return the edges
     */
    public List<TEdge> getEdges() {
        return edges;
    }

    /**
     * Returns the list of nodes for this graph.
     * 
     * @return the nodes
     */
    public List<TNode> getNodes() {
        return nodes;
    }

}
