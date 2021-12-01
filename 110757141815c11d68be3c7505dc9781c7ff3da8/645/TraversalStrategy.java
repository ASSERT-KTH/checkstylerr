/*******************************************************************************
 * Copyright (c) 2017 Kiel University and others.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.elk.alg.common.compaction.options;

/**
 * Possible traversal orders (with an implicit cost function) for placing polyominoes on an infinite square planar grid.
 */
public enum TraversalStrategy {
    // TODO finalize choices and comment on each one
    SPIRAL,
    LINE_BY_LINE,
    MANHATTAN,
    JITTER,
    QUADRANTS_LINE_BY_LINE,
    QUADRANTS_MANHATTAN,
    QUADRANTS_JITTER,
    COMBINE_LINE_BY_LINE_MANHATTAN,
    COMBINE_JITTER_MANHATTAN
}
