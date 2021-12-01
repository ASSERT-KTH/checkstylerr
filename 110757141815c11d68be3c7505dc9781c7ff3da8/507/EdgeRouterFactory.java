/*******************************************************************************
 * Copyright (c) 2015 Kiel University and others.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.elk.alg.layered.p5edges;

import java.util.EnumMap;
import java.util.Map;

import org.eclipse.elk.alg.layered.LayeredPhases;
import org.eclipse.elk.alg.layered.graph.LGraph;
import org.eclipse.elk.alg.layered.p5edges.splines.SplineEdgeRouter;
import org.eclipse.elk.core.alg.ILayoutPhase;
import org.eclipse.elk.core.alg.ILayoutPhaseFactory;
import org.eclipse.elk.core.options.EdgeRouting;

/**
 * Factory for edge routers. This factory is necessary since the {@link EdgeRouting} enumeration is
 * defined outside of ELK Layered and can thus not be made into a factory.
 * 
 * @author cds
 */
public final class EdgeRouterFactory implements ILayoutPhaseFactory<LayeredPhases, LGraph> {
    
    /** the cache prevents us from always instantiating new factories. */
    private static Map<EdgeRouting, EdgeRouterFactory> factoryCache = new EnumMap<>(EdgeRouting.class);
    /** the edge routing this factory uses to decide which implementation to return. */
    private EdgeRouting edgeRoutingStrategy;
    
    
    /**
     * Creates a new factory for the given edge routing strategy. The strategy decides which edge router
     * implementation the factory returns.
     * 
     * @param edgeRoutingStrategy the edge routing strategy.
     * @return the edge router factory.
     */
    public static EdgeRouterFactory factoryFor(final EdgeRouting edgeRoutingStrategy) {
        if (!factoryCache.containsKey(edgeRoutingStrategy)) {
            EdgeRouterFactory factory = new EdgeRouterFactory();
            factory.edgeRoutingStrategy = edgeRoutingStrategy;
            factoryCache.put(edgeRoutingStrategy, factory);
        }
        return factoryCache.get(edgeRoutingStrategy);
    }
    
    /**
     * {@inheritDoc}
     */
    public ILayoutPhase<LayeredPhases, LGraph> create() {
        switch (edgeRoutingStrategy) {
        case POLYLINE:
            return new PolylineEdgeRouter();
            
        case SPLINES:
            return new SplineEdgeRouter();
            
        default:
            return new OrthogonalEdgeRouter();
        }
    }

}
