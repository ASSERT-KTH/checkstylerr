/*******************************************************************************
 * Copyright (c) 2011, 2015 Kiel University and others.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.elk.alg.force.graph;

import org.eclipse.elk.core.math.KVector;
import org.eclipse.elk.graph.properties.MapPropertyHolder;

/**
 * A particle in the force graph, that is an object that can attract or repulse other particles.
 * 
 * @author owo
 * @author msp
 */
public abstract class FParticle extends MapPropertyHolder {
    
    /** the serial version UID. */
    private static final long serialVersionUID = -6264474302326798066L;
    
    /** Position of this particle. */
    private KVector position = new KVector();
    /** Width and height of graphical representation. */
    private KVector size = new KVector();

    /**
     * Returns the position vector of this particle.
     * 
     * @return the position vector
     */
    public final KVector getPosition() {
        return position;
    }

    /**
     * Returns the size of this particle.
     * 
     * @return the dimension vector
     */
    public final KVector getSize() {
        return size;
    }
    
    /**
     * Calculate radius for this particle.
     * 
     * @return radius of smallest circle surrounding shape of p
     */
    public double getRadius() {
        return size.length() / 2;
    }

}
