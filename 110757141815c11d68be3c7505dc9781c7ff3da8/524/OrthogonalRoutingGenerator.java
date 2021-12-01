/*******************************************************************************
 * Copyright (c) 2010, 2019 Kiel University and others.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.elk.alg.layered.p5edges.orthogonal;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.elk.alg.layered.DebugUtil;
import org.eclipse.elk.alg.layered.graph.LGraph;
import org.eclipse.elk.alg.layered.graph.LNode;
import org.eclipse.elk.alg.layered.graph.LPort;
import org.eclipse.elk.alg.layered.options.InternalProperties;
import org.eclipse.elk.alg.layered.options.PortType;
import org.eclipse.elk.core.options.PortSide;
import org.eclipse.elk.core.util.IElkProgressMonitor;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Edge routing implementation that creates orthogonal bend points. Inspired by:
 * <ul>
 *   <li>Georg Sander. Layout of directed hypergraphs with orthogonal hyperedges. In
 *     <i>Proceedings of the 11th International Symposium on Graph Drawing (GD '03)</i>,
 *     volume 2912 of LNCS, pp. 381-386. Springer, 2004.</li>
 *   <li>Giuseppe di Battista, Peter Eades, Roberto Tamassia, Ioannis G. Tollis,
 *     <i>Graph Drawing: Algorithms for the Visualization of Graphs</i>,
 *     Prentice Hall, New Jersey, 1999 (Section 9.4, for cycle breaking in the
 *     hyperedge segment graph)
 * </ul>
 *
 * <p>This is a generic implementation that can be applied to all four routing directions.
 * Usually, edges will be routed from west to east. However, with northern and southern
 * external ports, this changes: edges are routed from south to north and north to south,
 * respectively. To support these different requirements, the routing direction-related
 * code is factored out into {@link IRoutingDirectionStrategy routing strategies}.</p>
 *
 * <p>When instantiating a new routing generator, the concrete directional strategy must be
 * specified. Once that is done, {@link #routeEdges(LGraph, List, int, List, double)}
 * is called repeatedly to route edges between given lists of nodes.</p>
 */
public final class OrthogonalRoutingGenerator {

    ///////////////////////////////////////////////////////////////////////////////
    // Constants and Variables

    /** differences below this tolerance value are treated as zero. */
    public static final double TOLERANCE = 1e-3;

    /** factor for edge spacing used to determine the conflict threshold. */
    private static final double CONFL_THRESH_FACTOR = 0.2;
    /** weight penalty for conflicts of horizontal line segments. */
    private static final int CONFLICT_PENALTY = 16;

    /** routing direction strategy. */
    private final AbstractRoutingDirectionStrategy routingStrategy;
    /** spacing between edges. */
    private final double edgeSpacing;
    /** threshold at which conflicts of horizontal line segments are detected. */
    private final double conflictThreshold;
    /** prefix of debug output files. */
    private final String debugPrefix;


    ///////////////////////////////////////////////////////////////////////////////
    // Constructor

    /**
     * Constructs a new instance.
     *
     * @param direction the direction edges should point at.
     * @param edgeSpacing the space between edges.
     * @param debugPrefix prefix of debug output files, or {@code null} if no debug output should
     *                    be generated.
     */
    public OrthogonalRoutingGenerator(final RoutingDirection direction, final double edgeSpacing,
            final String debugPrefix) {

        this.routingStrategy = direction.strategy();
        this.edgeSpacing = edgeSpacing;
        this.conflictThreshold = CONFL_THRESH_FACTOR * edgeSpacing;
        this.debugPrefix = debugPrefix;
    }


    ///////////////////////////////////////////////////////////////////////////////
    // Edge Routing

    /**
     * Route edges between the given layers.
     *
     * @param monitor the progress monitor we're using.
     * @param layeredGraph the layered graph.
     * @param sourceLayerNodes the left layer. May be {@code null}.
     * @param sourceLayerIndex the source layer's index. Ignored if there is no source layer.
     * @param targetLayerNodes the right layer. May be {@code null}.
     * @param startPos horizontal position of the first routing slot
     * @return the number of routing slots for this layer
     */
    public int routeEdges(final IElkProgressMonitor monitor, final LGraph layeredGraph,
            final Iterable<LNode> sourceLayerNodes, final int sourceLayerIndex, final Iterable<LNode> targetLayerNodes,
            final double startPos) {

        Map<LPort, HyperEdgeSegment> portToEdgeSegmentMap = Maps.newHashMap();
        List<HyperEdgeSegment> edgeSegments = Lists.newArrayList();

        // create hyperedge segments for eastern output ports of the left layer and for western output ports of the
        // right layer
        createHyperEdgeSegments(
                sourceLayerNodes, routingStrategy.getSourcePortSide(), edgeSegments, portToEdgeSegmentMap);
        createHyperEdgeSegments(
                targetLayerNodes, routingStrategy.getTargetPortSide(), edgeSegments, portToEdgeSegmentMap);

        // create dependencies for the hypernode ordering graph
        for (int firstIdx = 0; firstIdx < edgeSegments.size() - 1; firstIdx++) {
            HyperEdgeSegment firstSegment = edgeSegments.get(firstIdx);
            for (int secondIdx = firstIdx + 1; secondIdx < edgeSegments.size(); secondIdx++) {
                HyperEdgeSegment secondSegment = edgeSegments.get(secondIdx);
                createDependency(firstSegment, secondSegment, conflictThreshold);
            }
        }

        // write the full dependency graph to an output file
        // elkjs-exclude-start
        if (debugPrefix != null && monitor.isLoggingEnabled()) {
            DebugUtil.logDebugGraph(
                    monitor,
                    layeredGraph,
                    sourceLayerNodes == null ? 0 : sourceLayerIndex + 1,
                    edgeSegments,
                    debugPrefix,
                    "full");
        }
        // elkjs-exclude-end

        // break cycles
        HyperEdgeCycleBreaker.breakCycles(edgeSegments, layeredGraph.getProperty(InternalProperties.RANDOM));

        // write the acyclic dependency graph to an output file
        // elkjs-exclude-start
        if (debugPrefix != null && monitor.isLoggingEnabled()) {
            DebugUtil.logDebugGraph(
                    monitor,
                    layeredGraph,
                    sourceLayerNodes == null ? 0 : sourceLayerIndex + 1,
                    edgeSegments,
                    debugPrefix,
                    "acyclic");
        }
        // elkjs-exclude-end

        // assign ranks to the hypernodes
        topologicalNumbering(edgeSegments);

        // set bend points with appropriate coordinates
        int rankCount = -1;
        for (HyperEdgeSegment node : edgeSegments) {
            // Hypernodes that are just straight lines don't take up a slot and don't need bend points
            if (Math.abs(node.getStartPos() - node.getEndPos()) < TOLERANCE) {
                continue;
            }

            rankCount = Math.max(rankCount, node.getRoutingSlot());

            routingStrategy.calculateBendPoints(node, startPos, edgeSpacing);
        }

        // release the created resources
        routingStrategy.clearCreatedJunctionPoints();
        return rankCount + 1;
    }


    ///////////////////////////////////////////////////////////////////////////////
    // Hyper Node Graph Creation

    /**
     * Creates hypernodes for the given layer.
     *
     * @param nodes the layer. May be {@code null}, in which case nothing happens.
     * @param portSide side of the output ports for whose outgoing edges hypernodes should
     *                 be created.
     * @param hyperNodes list the created hypernodes should be added to.
     * @param portToHyperNodeMap map from ports to hypernodes that should be filled.
     */
    private void createHyperEdgeSegments(final Iterable<LNode> nodes, final PortSide portSide,
            final List<HyperEdgeSegment> hyperNodes, final Map<LPort, HyperEdgeSegment> portToHyperNodeMap) {

        if (nodes != null) {
            for (LNode node : nodes) {
                for (LPort port : node.getPorts(PortType.OUTPUT, portSide)) {
                    HyperEdgeSegment hyperNode = portToHyperNodeMap.get(port);
                    if (hyperNode == null) {
                        hyperNode = new HyperEdgeSegment(routingStrategy);
                        hyperNodes.add(hyperNode);
                        hyperNode.addPortPositions(port, portToHyperNodeMap);
                    }
                }
            }
        }
    }

    /**
     * Create a dependency between the two given hypernodes, if one is needed.
     *
     * @param hn1 first hypernode
     * @param hn2 second hypernode
     * @param minDiff the minimal difference between horizontal line segments to avoid a conflict
     */
    private static void createDependency(final HyperEdgeSegment hn1, final HyperEdgeSegment hn2, final double minDiff) {
        // check if at least one of the two nodes is just a straight line; those don't
        // create dependencies since they don't take up a slot
        if (Math.abs(hn1.getStartPos() - hn1.getEndPos()) < TOLERANCE
                || Math.abs(hn2.getStartPos() - hn2.getEndPos()) < TOLERANCE) {
            return;
        }

        // compare number of conflicts for both variants
        int conflicts1 = countConflicts(hn1.getTargetPosis(), hn2.getSourcePosis(), minDiff);
        int conflicts2 = countConflicts(hn2.getTargetPosis(), hn1.getSourcePosis(), minDiff);

        // compare number of crossings for both variants
        int crossings1 = countCrossings(hn1.getTargetPosis(), hn2.getStartPos(), hn2.getEndPos())
                + countCrossings(hn2.getSourcePosis(), hn1.getStartPos(), hn1.getEndPos());
        int crossings2 = countCrossings(hn2.getTargetPosis(), hn1.getStartPos(), hn1.getEndPos())
                + countCrossings(hn1.getSourcePosis(), hn2.getStartPos(), hn2.getEndPos());

        int depValue1 = CONFLICT_PENALTY * conflicts1 + crossings1;
        int depValue2 = CONFLICT_PENALTY * conflicts2 + crossings2;

        if (depValue1 < depValue2) {
            // create dependency from first hypernode to second one
            new SegmentDependency(hn1, hn2, depValue2 - depValue1);
        } else if (depValue1 > depValue2) {
            // create dependency from second hypernode to first one
            new SegmentDependency(hn2, hn1, depValue1 - depValue2);
        } else if (depValue1 > 0 && depValue2 > 0) {
            // create two dependencies with zero weight
            new SegmentDependency(hn1, hn2, 0);
            new SegmentDependency(hn2, hn1, 0);
        }
    }

    /**
     * Counts the number of conflicts for the given lists of positions.
     *
     * @param posis1 sorted list of positions
     * @param posis2 sorted list of positions
     * @param minDiff minimal difference between two positions
     * @return number of positions that overlap
     */
    private static int countConflicts(final List<Double> posis1, final List<Double> posis2,
            final double minDiff) {

        int conflicts = 0;

        if (!posis1.isEmpty() && !posis2.isEmpty()) {
            Iterator<Double> iter1 = posis1.iterator();
            Iterator<Double> iter2 = posis2.iterator();
            double pos1 = iter1.next();
            double pos2 = iter2.next();
            boolean hasMore = true;

            do {
                if (pos1 > pos2 - minDiff && pos1 < pos2 + minDiff) {
                    conflicts++;
                }

                if (pos1 <= pos2 && iter1.hasNext()) {
                    pos1 = iter1.next();
                } else if (pos2 <= pos1 && iter2.hasNext()) {
                    pos2 = iter2.next();
                } else {
                    hasMore = false;
                }
            } while (hasMore);
        }

        return conflicts;
    }

    /**
     * Counts the number of crossings for a given list of positions.
     *
     * @param posis sorted list of positions
     * @param start start of the critical area
     * @param end end of the critical area
     * @return number of positions in the critical area
     */
    private static int countCrossings(final List<Double> posis, final double start, final double end) {
        int crossings = 0;
        for (double pos : posis) {
            if (pos > end) {
                break;
            } else if (pos >= start) {
                crossings++;
            }
        }
        return crossings;
    }


    ///////////////////////////////////////////////////////////////////////////////
    // Topological Ordering

    /**
     * Perform a topological numbering of the given hypernodes.
     *
     * @param nodes list of hypernodes
     */
    private static void topologicalNumbering(final List<HyperEdgeSegment> nodes) {
        // determine sources, targets, incoming count and outgoing count; targets are only
        // added to the list if they only connect westward ports (that is, if all their
        // horizontal segments point to the right)
        List<HyperEdgeSegment> sources = Lists.newArrayList();
        List<HyperEdgeSegment> rightwardTargets = Lists.newArrayList();
        for (HyperEdgeSegment node : nodes) {
            node.setInWeight(node.getIncomingDependencies().size());
            node.setOutWeight(node.getOutgoingDependencies().size());

            if (node.getInWeight() == 0) {
                sources.add(node);
            }

            if (node.getOutWeight() == 0 && node.getSourcePosis().size() == 0) {
                rightwardTargets.add(node);
            }
        }

        int maxRank = -1;

        // assign ranks using topological numbering
        while (!sources.isEmpty()) {
            HyperEdgeSegment node = sources.remove(0);
            for (SegmentDependency dep : node.getOutgoingDependencies()) {
                HyperEdgeSegment target = dep.getTarget();
                target.setRoutingSlot(Math.max(target.getRoutingSlot(), node.getRoutingSlot() + 1));
                maxRank = Math.max(maxRank, target.getRoutingSlot());

                target.setInWeight(target.getInWeight() - 1);
                if (target.getInWeight() == 0) {
                    sources.add(target);
                }
            }
        }

        /* If we stopped here, hyper nodes that don't have any horizontal segments pointing
         * leftward would be ranked just like every other hyper node. This would move back
         * edges too far away from their target node. To remedy that, we move all hyper nodes
         * with horizontal segments only pointing rightwards as far right as possible.
         */
        if (maxRank > -1) {
            // assign all target nodes with horizontal segments pointing to the right the
            // rightmost rank
            for (HyperEdgeSegment node : rightwardTargets) {
                node.setRoutingSlot(maxRank);
            }

            // let all other segments with horizontal segments pointing rightwards move as
            // far right as possible
            while (!rightwardTargets.isEmpty()) {
                HyperEdgeSegment node = rightwardTargets.remove(0);

                // The node only has connections to western ports
                for (SegmentDependency dep : node.getIncomingDependencies()) {
                    HyperEdgeSegment source = dep.getSource();
                    if (source.getSourcePosis().size() > 0) {
                        continue;
                    }

                    source.setRoutingSlot(Math.min(source.getRoutingSlot(), node.getRoutingSlot() - 1));

                    source.setOutWeight(source.getOutWeight() - 1);
                    if (source.getOutWeight() == 0) {
                        rightwardTargets.add(source);
                    }
                }
            }
        }
    }

}
