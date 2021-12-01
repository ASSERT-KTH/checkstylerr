/*******************************************************************************
 * Copyright (c) 2017 Kiel University and others.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.elk.alg.common.polyomino;

import java.util.function.BiFunction;

import org.eclipse.elk.alg.common.polyomino.structures.Polyomino;
import org.eclipse.elk.core.util.Pair;

/**
 * <p>
 * Implements one way of computing the successor of a point (x, y) in ℕ x ℕ based on enumerating points in increasing
 * order of the cost function |x|+|y| (That is the Taxicab Metric ) . As the cost function returns the same value for
 * some distinct points, there are different ways to enumerate these points. In this implementation the order of the
 * positions forms a spiral winding in the mathematical positive sense.
 * </p>
 * Example: a 3x3 grid with coordinates </br>
 * {@code (-1,-1) ( 0,-1) ( 1,-1)}</br>
 * {@code (-1, 0) ( 0, 0) ( 1, 0)}</br>
 * {@code (-1, 1) ( 0, 1) ( 1, 1)}</br>
 * will be enumerated in this order (marked by an X) </br>
 * {@code _ _ _}</br>
 * {@code _ X _}</br>
 * {@code _ _ _}</br>
 * </br>
 * {@code _ X _}</br>
 * {@code _ O _}</br>
 * {@code _ _ _}</br>
 * </br>
 * {@code _ O _}</br>
 * {@code _ O X}</br>
 * {@code _ _ _}</br>
 * </br>
 * {@code _ O _}</br>
 * {@code _ O O}</br>
 * {@code _ X _}</br>
 * </br>
 * {@code _ O _}</br>
 * {@code X O O}</br>
 * {@code _ O _}</br>
 * </br>
 */
public class SuccessorManhattan implements BiFunction<Pair<Integer, Integer>, Polyomino, Pair<Integer, Integer>> {

    /**
     * {@inheritDoc}
     */
    @Override
    public Pair<Integer, Integer> apply(final Pair<Integer, Integer> coords, final Polyomino poly) {
        int x = coords.getFirst();
        int y = coords.getSecond();

        int newX = x;
        int newY = y;

        if (x == 0 && y == 0) {
            newY -= 1;
        } else {
            if (x == -1 && y <= 0) {
                newX = 0;
                newY -= 2;
            } else {
                if (x <= 0 && y > 0) {
                    newX -= 1;
                    newY -= 1;
                } else {
                    if (x >= 0 && y < 0) {
                        newX += 1;
                        newY += 1;
                    } else {
                        if (x > 0 && y >= 0) {
                            newX -= 1;
                            newY += 1;
                        } else {
                            newX += 1;
                            newY -= 1;
                        }
                    }
                }
            }
        }

        return new Pair<Integer, Integer>(newX, newY);
    }

}
