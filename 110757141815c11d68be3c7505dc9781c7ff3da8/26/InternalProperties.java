/*******************************************************************************
 * Copyright (c) 2013, 2015 Kiel University and others.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.elk.alg.mrtree.options;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.eclipse.elk.alg.mrtree.graph.TEdge;
import org.eclipse.elk.alg.mrtree.graph.TNode;
import org.eclipse.elk.core.math.KVector;
import org.eclipse.elk.graph.properties.IProperty;
import org.eclipse.elk.graph.properties.Property;

/**
 * Property definitions for the T layouter.
 * 
 * @author sor
 * @author sgu
 */
public final class InternalProperties {

    /** The original object from which a graph element was created. */
    public static final IProperty<Object> ORIGIN = new Property<Object>("origin");

    /** Random number generator for the algorithm. */
    public static final IProperty<Random> RANDOM = new Property<Random>("random");

    /** The longest path length of a node in the nesting tree of a compound graph. */
    public static final IProperty<Integer> DEPTH = new Property<Integer>("DEPTH", 0);

    /** The maximal fan out of a node in the nesting tree of a compound graph. */
    public static final IProperty<Integer> FAN = new Property<Integer>("FAN", 0);
    
    /** The number of descendants of the node. */
    public static final IProperty<Integer> DESCENDANTS = new Property<Integer>("DESCENDANTS", 0);

    /** This is a root. */
    public static final IProperty<Boolean> ROOT = new Property<Boolean>("ROOT", false);

    /** the lefthand node at the same level. */
    public static final IProperty<TNode> LEFTNEIGHBOR = new Property<TNode>("LEFTNEIGHBOR", null);

    /** the righthand node at the same level. */
    public static final IProperty<TNode> RIGHTNEIGHBOR = new Property<TNode>("RIGHTNEIGHBOR", null);

    /** the lefthand node at the same level with the same parent. */
    public static final IProperty<TNode> LEFTSIBLING = new Property<TNode>("LEFTSIBLING", null);

    /** the righthand node with the same parent. */
    public static final IProperty<TNode> RIGHTSIBLING = new Property<TNode>("RIGHTSIBLING", null);

    /** This is a dummy node. */
    public static final IProperty<Boolean> DUMMY = new Property<Boolean>("DUMMY", false);

    /** The level out of a node in the nesting tree of a compound graph. */
    public static final IProperty<Integer> LEVEL = new Property<Integer>("LEVEL", 0);

    /** A list containing all edges that were marked to remove in the treeing phase. */
    public static final IProperty<List<TEdge>> REMOVABLE_EDGES = new Property<List<TEdge>>(
            "REMOVABLE_EDGES", new LinkedList<TEdge>());

    /** The x coordinate of the node in the level. */
    public static final IProperty<Integer> XCOOR = new Property<Integer>("XCOOR", 0);

    /** The y coordinate of the node in the level. */
    public static final IProperty<Integer> YCOOR = new Property<Integer>("YCOOR", 0);

    /** The y height of the nodes level. */
    public static final IProperty<Double> LEVELHEIGHT = new Property<Double>("LEVELHEIGHT", 0d);

    /**
     * Id of of a real node. This Indicates the block by the most significant letters and level of
     * the node by the length of the string.
     */
    public static final IProperty<String> ID = new Property<String>("ID", "");

    /** Is the node a root of a tree. */
    public static final IProperty<Integer> POSITION = new Property<Integer>("POSITION", 0);

    /** A preliminary x-coordinate. */
    public static final IProperty<Double> PRELIM = new Property<Double>("PRELIM", 0d);

    /** A modifier x-coordinate. */
    public static final IProperty<Double> MODIFIER = new Property<Double>("MODIFIER", 0d);

    /** Upper left corner of the graph's bounding box. */
    public static final IProperty<KVector> BB_UPLEFT = new Property<KVector>("boundingBox.upLeft");
    /** Lower right corner of the graph's bounding box. */
    public static final IProperty<KVector> BB_LOWRIGHT = new Property<KVector>(
            "boundingBox.lowRight");

    /** Hidden default constructor. */
    private InternalProperties() {
    }

}
