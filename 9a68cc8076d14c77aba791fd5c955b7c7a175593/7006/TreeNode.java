/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.flashlight.datatree;

import java.util.Collection;
import java.util.List;

/**
 * TreeNode maintains all the Runtime Monitoring Data
 * @author Harpreet Singh
 */
public interface TreeNode extends TreeElement {


    public String getCategory ();
    public void setCategory (String category);

    public boolean isEnabled ();
    public void setEnabled (boolean enabled);

    public void setDescription (String description);
    public String getDescription ();

    // Children utility methods
    public TreeNode addChild (TreeNode newChild);
    public void removeChild(TreeNode oldChild);
    public void setParent (TreeNode parent);
    public TreeNode getParent ();

    /**
     *
     * @return complete dotted name to this node
     */
    public String getCompletePathName ();

    public boolean hasChildNodes ();

    /**
     *
     * @return Collection<TreeNode> collection of children
     */
    public Collection<TreeNode> getChildNodes ();

    /**
     *
     * @return Collection<TreeNode> collection of children
     */
    public Collection<TreeNode> getEnabledChildNodes ();

    /**
     *
     * @param completeName dotted name to the node
     * @return TreeNode uniquely identified tree node. Null if no matching tree node.
     */

    public TreeNode getNode (String completeName);

    /**
     * Performs a depth first traversal of the tree. Returns all the nodes in the
     * tree unless ignoreDisabled flag is turned on.
     * @param ignoreDisabled will ignore a disabled node and its children
     * @return List<TreeNode> lists all nodes under the current sub tree.
     */

    public List<TreeNode> traverse (boolean ignoreDisabled);
    /**
     *
     * Returns all nodes that match the given Regex pattern as specified by the
     * <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/util/regex/Pattern.html"> Pattern</a>  class.
     * Admin CLI in GlassFish v2 did not use Pattern's specified in java.util.Pattern. It had
     * a simpler mechanism where * was equivalent to .* from {@linkplain java.util.regex.Pattern}
     * If the V2Compatible flag is turned on, then the pattern is considered a v2 pattern.
     * @param pattern Find a node that matches the pattern. By default pattern should follow the conventions
     * outlined by the java.util.regex.Pattern class.
     * @param ignoreDisabled will ignore a disabled node and its children
     * @param gfv2Compatible in this mode, * has the same meaning as <i>.*</i> in the Pattern class.
     * The implementation should consider pattern as a v2 pattern.
     * @return
     */
    public List<TreeNode> getNodes (String pattern, boolean ignoreDisabled, boolean gfv2Compatible);

    /**
     * Behaves as {@link #getNodes (String, boolean, boolean) with ignoreDisabled set to true
     * and gfV2Compatible set to true
     * Pattern is considered to be a GFV2 Compatible Pattern
     */
    public List<TreeNode> getNodes (String pattern);

    public TreeNode getChild(String childName);

    /**
     * Get the "parent" matching the given pattern.
     * E.g "server.jvm.memory.maxheapsize-count" is the parent of
     * "server.jvm.memory.maxheapsize-count-count"
     * Note that in V3 the latter will NOT be found with getNodes()
     *
     * @param pattern Find a node that matches the pattern. By default pattern should follow the conventions
     * outlined by the java.util.regex.Pattern class.
     * @return The parent node if found otherwise null.
     */
    TreeNode getPossibleParentNode(String pattern);
}
