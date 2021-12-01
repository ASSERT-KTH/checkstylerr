/*******************************************************************************
 * Copyright (c) 2012, 2015 Kiel University and others.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.elk.alg.layered.components;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.elk.alg.layered.graph.LGraph;
import org.eclipse.elk.alg.layered.graph.LNode;
import org.eclipse.elk.alg.layered.options.LayeredOptions;
import org.eclipse.elk.core.math.KVector;

/**
 * A simple graph placer that places components into rows, trying to make the result fit a configurable
 * aspect ratio. This graph placer does not pay attention to external port connections and should not
 * be used in the presence of such connections.
 * 
 * <p>This is the first algorithm implemented to place the different connected components of a graph,
 * and was formerly the implementation of the {@link ComponentsProcessor#combine(List)} method.</p>
 * 
 * <p>The target graph must not be contained in the list of components, except if there is only
 * one component.</p>
 * 
 * @author msp
 * @author cds
 */
final class SimpleRowGraphPlacer extends AbstractGraphPlacer {
    
    /**
     * {@inheritDoc}
     */
    public void combine(final List<LGraph> components, final LGraph target) {
        if (components.size() == 1) {
            LGraph source = components.get(0);
            if (source != target) {
                target.getLayerlessNodes().clear();
                moveGraph(target, source, 0, 0);
                target.copyProperties(source);
                target.getPadding().copy(source.getPadding());
                target.getSize().x = source.getSize().x;
                target.getSize().y = source.getSize().y;
            }
            return;
        } else if (components.isEmpty()) {
            target.getLayerlessNodes().clear();
            target.getSize().x = 0;
            target.getSize().y = 0;
            return;
        }
        assert !components.contains(target);
        
        // assign priorities
        for (LGraph graph : components) {
            int priority = 0;
            for (LNode node : graph.getLayerlessNodes()) {
                priority += node.getProperty(LayeredOptions.PRIORITY);
            }
            graph.id = priority;
        }

        // sort the components by their priority and size
        Collections.sort(components, new Comparator<LGraph>() {
            public int compare(final LGraph graph1, final LGraph graph2) {
                int prio = graph2.id - graph1.id;
                if (prio == 0) {
                    double size1 = graph1.getSize().x * graph1.getSize().y;
                    double size2 = graph2.getSize().x * graph2.getSize().y;
                    return Double.compare(size1, size2);
                }
                return prio;
            }
        });
        
        LGraph firstComponent = components.get(0);
        target.getLayerlessNodes().clear();
        target.copyProperties(firstComponent);
        
        // determine the maximal row width by the maximal box width and the total area
        double maxRowWidth = 0.0f;
        double totalArea = 0.0f;
        for (LGraph graph : components) {
            KVector size = graph.getSize();
            maxRowWidth = Math.max(maxRowWidth, size.x);
            totalArea += size.x * size.y;
        }
        maxRowWidth = Math.max(maxRowWidth, (float) Math.sqrt(totalArea)
                * target.getProperty(LayeredOptions.ASPECT_RATIO));
        double componentSpacing = target.getProperty(LayeredOptions.SPACING_COMPONENT_COMPONENT);

        // place nodes iteratively into rows
        double xpos = 0, ypos = 0, highestBox = 0, broadestRow = componentSpacing;
        for (LGraph graph : components) {
            KVector size = graph.getSize();
            if (xpos + size.x > maxRowWidth) {
                // place the graph into the next row
                xpos = 0;
                ypos += highestBox + componentSpacing;
                highestBox = 0;
            }
            KVector offset = graph.getOffset();
            offsetGraph(graph, xpos + offset.x, ypos + offset.y);
            offset.reset();
            broadestRow = Math.max(broadestRow, xpos + size.x);
            highestBox = Math.max(highestBox, size.y);
            xpos += size.x + componentSpacing;
        }
        
        target.getSize().x = broadestRow;
        target.getSize().y = ypos + highestBox;
        
        // if compaction is desired, do so!
        if (firstComponent.getProperty(LayeredOptions.COMPACTION_CONNECTED_COMPONENTS)) {
            ComponentsCompactor compactor = new ComponentsCompactor();
            compactor.compact(components, target.getSize(), componentSpacing);

            // the compaction algorithm places components absolutely,
            // therefore we have to use the final drawing's offset
            for (LGraph h : components) {
                h.getOffset().reset().add(compactor.getOffset());
            }

            // set the new graph size
            target.getSize().reset().add(compactor.getGraphSize());
        }

        // finally move the components to the combined graph
        moveGraphs(target, components, 0, 0);
    }

}
