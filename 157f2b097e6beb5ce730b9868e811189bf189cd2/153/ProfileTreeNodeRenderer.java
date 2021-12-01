/* 
 * Copyright 2015 Torridity.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.tor.tribes.ui.renderer;

import de.tor.tribes.types.UserProfile;
import java.awt.Component;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 *
 * @author Torridity
 */
public class ProfileTreeNodeRenderer extends DefaultTreeCellRenderer {

    private ImageIcon server = null;
    private ImageIcon mainProfile = null;
    private ImageIcon uvProfile = null;

    public ProfileTreeNodeRenderer() {
        server = new ImageIcon(ProfileTreeNodeRenderer.class.getResource("/res/server.png"));
        mainProfile = new ImageIcon(ProfileTreeNodeRenderer.class.getResource("/res/profile.png"));
        uvProfile = new ImageIcon(ProfileTreeNodeRenderer.class.getResource("/res/uv_profile.png"));
    }

    public Component getTreeCellRendererComponent(
            JTree tree,
            Object value,
            boolean sel,
            boolean expanded,
            boolean leaf,
            int row,
            boolean hasFocus) {

        super.getTreeCellRendererComponent(
                tree, value, sel,
                expanded, leaf, row,
                hasFocus);

        try {
            DefaultMutableTreeNode node = ((DefaultMutableTreeNode) value);
            if (node.getUserObject() instanceof String) {
                setIcon(server);
            } else if (node.getUserObject() instanceof UserProfile) {
                if (((UserProfile) node.getUserObject()).isUVAccount()) {
                    setIcon(uvProfile);
                } else {
                    setIcon(mainProfile);
                }
            }
        } catch (Exception ignored) {
        }
        return this;
    }
}
