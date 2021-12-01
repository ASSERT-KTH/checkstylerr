/*******************************************************************************
 * Copyright (c) 2017 Kiel University and others.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.elk.alg.common;

import java.util.List;

import com.google.common.collect.Lists;

/**
* A tree of T.
* @param <T> the type
*/
public class Tree<T> {
   // CHECKSTYLEOFF VisibilityModifier
   /** The contained node. */
   public final T node;
   /** The subtrees. */
   public List<Tree<T>> children;
   // CHECKSTYLEON VisibilityModifier
   
   /**
    * Constructor.
    * @param n the contained node
    */
   public Tree(final T n) {
       node = n;
       children = Lists.newArrayList();
   }
}