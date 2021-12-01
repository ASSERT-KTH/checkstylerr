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
 * <p>
 * Possible secondary sorting criteria for the processing order of polyominoes. They are used when polyominoes are
 * equal according to the primary sorting criterion {@code HighLevelSortingCriterion}.
 * </p>
 */
public enum LowLevelSortingCriterion {
    BY_SIZE,
    BY_SIZE_AND_SHAPE
}
