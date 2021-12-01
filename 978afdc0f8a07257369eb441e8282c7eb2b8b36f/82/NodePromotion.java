/*******************************************************************************
 * Copyright (c) 2016 Kiel University and others.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.elk.alg.layered.intermediate;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.elk.alg.layered.graph.LEdge;
import org.eclipse.elk.alg.layered.graph.LGraph;
import org.eclipse.elk.alg.layered.graph.LNode;
import org.eclipse.elk.alg.layered.graph.Layer;
import org.eclipse.elk.alg.layered.options.LayeredOptions;
import org.eclipse.elk.alg.layered.options.NodePromotionStrategy;
import org.eclipse.elk.core.alg.ILayoutProcessor;
import org.eclipse.elk.core.util.IElkProgressMonitor;
import org.eclipse.elk.core.util.Pair;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * An approach that is implementing the node promotion heuristic of Nikola S. Nikolov and Alexandre
 * Tarassov and added a few more options for handling and stopping the promotion earlier.
 * 
 * The algorithm is described in
 * <ul>
 * <li>Nikola S. Nikolov, Alexandre Tarassov, and Jürgen Branke. 2005. In search for efficient
 * heuristics for minimum-width graph layering with consideration of dummy nodes. J. Exp.
 * Algorithmics 10, Article 2.7 (December 2005). DOI=10.1145/1064546.1180618
 * http://doi.acm.org/10.1145/1064546.1180618.</li>
 * </ul>
 * 
 * Note that the term 'width' here refers to the maximum number of nodes in any layer.
 * 
 * The goal of the node promotion is to achieve a layering with less dummy nodes. For this purpose
 * the original graph nodes are promoted recursively and the promotion is applied, if and only if
 * this reduces the determined count of dummy nodes. A promotion takes a node and puts it on a
 * higher layer, if there is a predecessor on that layer this node will be promoted as well and so
 * on. After that the effect of the promotion is evaluated, if the number of dummy nodes decreases
 * the promotion is applied. Furthermore there are different strategies to handle the node
 * promotion.
 * <ul>
 * <li>{@link LayeredOptions#NODE_PROMOTION}: {@link Enum} that contains the handling strategies and
 * enables or disables the node promotion. For more information look at
 * {@link NodePromotionStrategy}.</li>
 * <li>{@link LayeredOptions#NODE_PROMOTION_BOUNDARY}: Sets an upper bound for the iterations of the
 * node promotion. Is calculated by a percentage of the number of all nodes or of the number of only
 * the dummy nodes. The algorithm stops if this boundary is reached, even if it's still possible to
 * promote nodes.</li>
 * </ul>
 */
public class NodePromotion implements ILayoutProcessor<LGraph> {

    /** The layered graph to which the promotion is applied. */
    private LGraph masterGraph;

    /** Holds all nodes of the graph that have incoming edges. */
    private List<LNode> nodesWithIncomingEdges;

    /** Stores all nodes of the graph. */
    private List<LNode> nodes;

    /**
     * Holds for each layer the current number of original and dummy nodes inside it. The index in
     * the list stands for the index of the layer.
     */
    private List<Integer> currentWidth;

    /**
     * Holds for each layer the current approximated width in pixels of the original and dummy nodes
     * inside it. The index in the list stands for the index of the layer.
     */
    private List<Double> currentWidthPixel;

    /**
     * Contains the number of the layer for each node. For a node with the ID i layer[i] holds the
     * index of the layer to which it is currently assigned.
     */
    private int[] layers;

    /**
     * Contains three sets of precalculated information for all nodes, where the node
     * with the ID i accesses the results:
     * <ul>
     * <li>degreeDiff[2][i]: Count of outgoing edges of node i.</li>
     * <li>degreeDiff[1][i]: Count of incoming edges of node i.</li>
     * <li>degreeDiff[0][i]: The difference between the count of outgoing and incoming edges for
     * node i.</li>
     * </ul>
     */
    private int[][] degreeDiff;

    /** Stores the maximal accepted width of the graph before processing for later comparison. */
    private int maxWidth;

    /**
     * Stores the approximated maximal accepted width in pixels of the graph before processing for
     * later comparison.
     */
    private double maxWidthPixel;

    /** Holds current number of dummy nodes in the graph. */
    private int dummyNodeCount;

    /** Is the current height of the graph. */
    private int maxHeight;

    /**
     * Approximated pixels that have to be added to the width (in pixels) for better estimation of
     * the actual width of the graph.
     */
    private double nodeSizeAffix;

    /** Approximated size in pixels for a dummy node. */
    private double dummySize;

    /** The strategy which is used for the node promotion. */
    private NodePromotionStrategy promotionStrategy;

    @Override
    public void process(final LGraph layeredGraph, final IElkProgressMonitor progressMonitor) {

        progressMonitor.begin("Node promotion heuristic", 1);

        masterGraph = layeredGraph;

        precalculateAndSetInformation();

        promotionStrategy = layeredGraph.getProperty(LayeredOptions.LAYERING_NODE_PROMOTION_STRATEGY);

        // If the promotion strategy is set to DUMMYNODE_PERCENTAGE or NODECOUNT_PERCENTAGE this
        // value will have an effect on the termination criterion of the node promotion.
        int promoteUntil = masterGraph.getProperty(LayeredOptions.LAYERING_NODE_PROMOTION_MAX_ITERATIONS).intValue();

        // Dummy function that's got no other choice but to say "it's true".
        Function<Pair<Integer, Integer>, Boolean> funFunction = (pair) -> true;

        switch (promotionStrategy) {
        case NIKOLOV:
            promotionMagic(funFunction);
            break;
        case NIKOLOV_PIXEL:
            promotionMagic(funFunction);
            break;
        case NIKOLOV_IMPROVED:
            promotionStrategy = NodePromotionStrategy.NO_BOUNDARY;
            promotionMagic(funFunction);
            // Determine if the max width of original plus dummy nodes is bigger than before the
            // promotion. If so, use a more cautious style of promotion.
            int newMaxWidth = 0;
            for (Integer martha : currentWidth) {
                newMaxWidth = Math.max(newMaxWidth, martha);
            }
            if (newMaxWidth > maxWidth) { // maximal width exceeded
                promotionStrategy = NodePromotionStrategy.NIKOLOV;
                promotionMagic(funFunction);
            }
            break;
        case NIKOLOV_IMPROVED_PIXEL:
            promotionStrategy = NodePromotionStrategy.NO_BOUNDARY;
            promotionMagic(funFunction);
            // Determine if the approximated max width of original plus dummy nodes in pixels is
            // bigger than before the promotion. If so, use a more cautious style of promotion.
            double newMaxWidthPixel = 0.0;
            for (Double donna : currentWidthPixel) {
                newMaxWidthPixel = Math.max(newMaxWidthPixel, donna);
            }
            if (newMaxWidthPixel > maxWidthPixel) { // maximal width exceeded
                promotionStrategy = NodePromotionStrategy.NIKOLOV_PIXEL;
                promotionMagic(funFunction);
            }
            break;
        case NODECOUNT_PERCENTAGE:
            // Set maximal number of iterations till stopping the node promotion depending on the
            // number of nodes in the graph. After the number of iterations is reached it's forced
            // to stop. Terminating criterion is handed to the promotion algorithm in the form of an
            // anonymous function that checks if the criterion is fulfilled. (It's possible the
            // number is never reached.)
            // SUPPRESS CHECKSTYLE NEXT MagicNumber
            int promoteUntilN = (int) Math.ceil(layers.length * promoteUntil / 100.0); 
            promotionMagic(pair -> pair.getSecond() < promoteUntilN);
            break;
        case DUMMYNODE_PERCENTAGE:
            // Calculate number of dummy nodes the algorithm shall ideally reduce until it's forced
            // to stop and hand it to the promotion algorithm in the form of an anonymous function
            // that checks if the criterion is fulfilled. (It's possible the number is never
            // reached.)
            // SUPPRESS CHECKSTYLE NEXT MagicNumber
            int promoteUntilD = (int) Math.ceil(dummyNodeCount * promoteUntil / 100.0);
            promotionMagic(pair -> pair.getFirst() < promoteUntilD);
            break;
        default:
            promotionMagic(funFunction);
            break;
        }

        setNewLayering(layeredGraph);

        progressMonitor.done();
    }

    /**
     * Helper method for calculating needed information for the heuristic. Sets IDs for layers and
     * nodes to grant an easier access. Also calculates the difference for each node between its
     * incoming and outgoing edges and approximates the width in pixels for each (dummy) node and
     * layer as well as the estimated width (original plus dummy nodes).
     */
    private void precalculateAndSetInformation() {

        // The sizes (in pixels) are approximated, because the layering isn't finite in this phase
        // and there might some more changes about where which edge and node might end up at last.
        // Calculate approximative addition of space for a node.
        nodeSizeAffix = masterGraph.getProperty(LayeredOptions.SPACING_NODE_NODE);
        // And calculate an approximative size of a dummy node inside the graph.
        dummySize = masterGraph.getProperty(LayeredOptions.SPACING_EDGE_NODE_BETWEEN_LAYERS);

        maxHeight = masterGraph.getLayers().size();
        int layerID = maxHeight - 1;
        int nodeID = 0;
        maxWidth = 0;
        maxWidthPixel = 0.0;
        currentWidth = Lists.newArrayList(new Integer[maxHeight]);
        currentWidthPixel = Lists.newArrayList(new Double[maxHeight]);

        // Set IDs for all layers and nodes.
        // Layer IDs are reversed for easier handling in the heuristic.
        // Determine maximum width of all layers and fill list of the width of all current layers
        // with information at corresponding position in the list (original plus dummy nodes as well
        // as their approximated width in pixels).
        for (Layer layer : masterGraph.getLayers()) {
            layer.id = layerID;
            for (LNode node : layer.getNodes()) {
                node.id = nodeID;
                nodeID++;
            }
            layerID--;
        }

        layers = new int[nodeID];
        degreeDiff = new int[nodeID][3]; // SUPPRESS CHECKSTYLE MagicNumber
        nodes = Lists.newArrayList();
        nodesWithIncomingEdges = Lists.newArrayList();
        int dummyBaggage = 0; // Will contain number of dummy nodes between the layers.
        dummyNodeCount = 0;

        // Calculate difference and determine all nodes with incoming edges.
        for (Layer layer : masterGraph) {
            layerID = layer.id;
            int incoming = 0;
            int outcoming = 0;
            int layerSize = layer.getNodes().size();
            double layerSizePixel = 0;

            for (LNode node : layer.getNodes()) {
                nodeID = node.id;
                layers[nodeID] = node.getLayer().id;
                layerSizePixel += node.getSize().y + nodeSizeAffix; // Accumulate width of every
                                                                    // node.
                int inDegree = Iterables.size(node.getIncomingEdges());
                int outDegree = Iterables.size(node.getOutgoingEdges());
                degreeDiff[nodeID][0] = outDegree - inDegree;
                degreeDiff[nodeID][1] = inDegree;
                degreeDiff[nodeID][2] = outDegree;
                incoming += inDegree; // accumulate all incoming edges of the layer
                outcoming += outDegree; // and all outgoing edges
                if (inDegree > 0) {
                    nodesWithIncomingEdges.add(node);
                }
                nodes.add(node);
            }

            dummyBaggage -= incoming; // Edges that end here don't create dummy nodes in this layer.
            int nodesNdummies = layerSize + dummyBaggage; // The actual width considering original
                                                          // plus dummy nodes.
            layerSizePixel += dummyBaggage * dummySize; // Add width of the dummy nodes in the
                                                        // layer.

            currentWidth.set(layerID, nodesNdummies); // Setting information...
            currentWidthPixel.set(layerID, layerSizePixel);
            maxWidth = Math.max(maxWidth, nodesNdummies);
            maxWidthPixel = Math.max(maxWidthPixel, layerSizePixel);
            dummyNodeCount += dummyBaggage; // Calculate number of dummy nodes in the graph.
            dummyBaggage += outcoming; // All outgoing edges of this layer might create dummy nodes
                                       // in other layers, so it's necessary to keep them in mind.
        }
    }

    /**
     * Method that handles the promotion. Contains the outer loop originally proposed by Nikolov et
     * al. but checks for more criteria.
     * 
     * @param funky
     *            A function that's used to predict if the algorithm should stop after the running
     *            iteration of the algorithm. If there is a boundary given it potentially stops the
     *            promotion before there are no more iterations possible.
     */
    private void promotionMagic(final Function<Pair<Integer, Integer>, Boolean> funky) {
        int promotions; // Indicator if there have been promotions applied in a run.
        boolean promotionFlag;
        int iterationCounter = 0; // Counts the number of iterations over all nodes.
        int reducedDummies = 0; // Counts the number of already exterminated dummy nodes.

        int[] layeringBackup = Arrays.copyOf(layers, layers.length); // Setting up backups..
        int dummyBackup = dummyNodeCount;
        int heightBackup = maxHeight;
        List<Integer> currentWidthBackup = currentWidth;
        List<Double> currentWidthPixelBackup = currentWidthPixel;

        do {
            promotions = 0;
            // Start promotion for all nodes with incoming edges.
            for (LNode node : nodesWithIncomingEdges) {
                Pair<Integer, Boolean> promotionPair = promoteNode(node);

                // Strategies NIKOLOV and NIKOLOV_PIXEL are considerate about the actual width while
                // the nodes are promoted, therefore they are able to cancel a legitimate promotion
                // because of an exceeding of the maximal accepted width.
                boolean apply = true;
                if (promotionStrategy == NodePromotionStrategy.NIKOLOV
                        || promotionStrategy == NodePromotionStrategy.NIKOLOV_PIXEL) {
                    apply = promotionPair.getSecond();
                }

                // Promotion is valid and will be applied.
                if (promotionPair.getFirst() < 0 && apply) {
                    promotions++;
                    layeringBackup = Arrays.copyOf(layers, layers.length);
                    dummyNodeCount = dummyNodeCount + promotionPair.getFirst();
                    reducedDummies += dummyBackup - dummyNodeCount;
                    dummyBackup = dummyNodeCount + promotionPair.getFirst();
                    heightBackup = maxHeight;
                    currentWidthBackup = Lists.newArrayList(currentWidth);
                    currentWidthPixelBackup = Lists.newArrayList(currentWidthPixel);
                } else { // Promotion is invalid and the last valid state will be restored.
                    layers = Arrays.copyOf(layeringBackup, layeringBackup.length);
                    dummyNodeCount = dummyBackup;
                    currentWidth = Lists.newArrayList(currentWidthBackup);
                    currentWidthPixel = Lists.newArrayList(currentWidthPixelBackup);
                    maxHeight = heightBackup;
                }
            }
            iterationCounter++;
            promotionFlag = promotions != 0 && // Stopping criterium of Nikolov et al..
                    // Added criterion for earlier
                    // stopping depending on reduced
                    // dummies or number of
                    // iterations..
                    funky.apply(Pair.of(reducedDummies, iterationCounter)); 
            
        } while (promotionFlag);

    }

    /**
     * Node promotion heuristic of the paper. Works on an array of integers which represents the
     * nodes and their position within the layers to avoid difficulties while creating and deleting
     * new layers over the course of the discarded promotions.
     * 
     * @param node
     *            Node that shall be promoted.
     * @return A Pair consisting of an Integer and a Boolean.
     *         <ol>
     *         <li>The Integer is the estimated difference of dummy nodes after applying the node
     *         promotion. A negative value indicates a reduction of dummy nodes.</li>
     *         <li>The Boolean gives information about whether the maximal accepted width has been
     *         exceeded while promoting the given node or not.</li>
     *         </ol>
     */
    private Pair<Integer, Boolean> promoteNode(final LNode node) {
        boolean maxWidthNotExceeded = true;
        int dummydiff = 0;
        int nodeLayerPos = layers[node.id];
        double nodeSize = node.getSize().y + nodeSizeAffix;
        int dummiesBuilt = degreeDiff[node.id][2];

        // Calculating the current width of the layer the node came from by subtracting its size
        // from the size of this layer and adding its outgoing edges to this layer because of the
        // then resulting new dummy nodes.
        currentWidth.set(nodeLayerPos, currentWidth.get(nodeLayerPos) - 1 + dummiesBuilt);
        currentWidthPixel.set(nodeLayerPos, currentWidthPixel.get(nodeLayerPos) - nodeSize
                + dummiesBuilt * dummySize);

        // Calculate index of the layer for the promoted node.
        nodeLayerPos++;
        if (nodeLayerPos >= maxHeight) {
            maxHeight++;
            currentWidth.add(1);
            currentWidthPixel.add(nodeSize);
        } else {
            // Calculating the current width of the layer the node is promoted to by adding its size
            // to the size of this layer and subtracting its incoming edges to this layer due to the
            // resulting potentially reduced dummy nodes. If it's a new layer only the size itself will
            // be added to it.
            int dummiesReduced = degreeDiff[node.id][1];
            currentWidth.set(nodeLayerPos, currentWidth.get(nodeLayerPos) + 1 - dummiesReduced);
            currentWidthPixel.set(nodeLayerPos, currentWidthPixel.get(nodeLayerPos) + nodeSize
                    - dummiesReduced * dummySize);
        }

        // Shall the width be considered while promoting a node, it's necessary to check, if the
        // promotion of the node exceeds the max width of the previous layer or the new layer. If
        // so, the promotion will not be applied.
        if ((promotionStrategy == NodePromotionStrategy.NIKOLOV 
                        && (currentWidth.get(nodeLayerPos) > maxWidth 
                                || currentWidth.get(nodeLayerPos - 1) > maxWidth))
                || (promotionStrategy == NodePromotionStrategy.NIKOLOV_PIXEL 
                        && (currentWidthPixel.get(nodeLayerPos) > maxWidthPixel 
                                || currentWidthPixel.get(nodeLayerPos - 1) > maxWidthPixel))) {
            maxWidthNotExceeded = false;
        }

        // Set new position of the node in the layering by promoting preceding nodes in the above
        // neighboring layer recursively and calculating the difference of dummy nodes.
        for (LEdge edge : node.getIncomingEdges()) {
            LNode masterNode = edge.getSource().getNode();
            if (layers[masterNode.id] == nodeLayerPos) {
                Pair<Integer, Boolean> promotion = promoteNode(masterNode);
                dummydiff = dummydiff + promotion.getFirst();
                maxWidthNotExceeded = maxWidthNotExceeded && promotion.getSecond();
            }
        }

        layers[node.id] = nodeLayerPos;
        dummydiff = dummydiff + degreeDiff[node.id][0];

        return new Pair<Integer, Boolean>(dummydiff, maxWidthNotExceeded);
    }

    /**
     * Helper method for setting the calculated and potentially improved layering after the node
     * promotion heuristic is finished.
     * 
     * @param layeredGraph
     *            The graph to which the calculated new layering is applied.
     */
    private void setNewLayering(final LGraph layeredGraph) {
        // The laLaLayer, born in the far away laLaLand, traveled here to be of special use for
        // our layering. It is added to the layeredGraph with reversed IDs so they fit to the IDs
        // stored in the nodes but can also be properly assigned compliant with the layering used in
        // ELK Layered.
        List<Layer> layList = Lists.newArrayList();
        for (int i = 0; i <= maxHeight; i++) {
            Layer laLaLayer = new Layer(layeredGraph);
            laLaLayer.id = maxHeight - i;
            layList.add(laLaLayer);
        }

        // Assign all nodes to the beforehand created (laLa)layers.
        for (LNode node : nodes) {
            node.setLayer(layList.get(maxHeight - layers[node.id]));
        }

        // One Loop to exterminate all deceiving layers that don't contain any nodes!
        Iterator<Layer> layerIt = layList.iterator();
        while (layerIt.hasNext()) {
            Layer possiblyEvilLayer = layerIt.next();
            if (possiblyEvilLayer.getNodes().isEmpty()) {
                layerIt.remove();
            }
        }
        layeredGraph.getLayers().clear();
        layeredGraph.getLayers().addAll(layList);
    }
}
