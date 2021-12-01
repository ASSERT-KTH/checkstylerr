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

import de.tor.tribes.util.Constants;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;

/**
 * @author Torridity
 */
public class ColoredCoutdownCellRenderer extends DefaultTableRenderer {

    private static final int MINUTE = (1000 * 60);

    public ColoredCoutdownCellRenderer() {
        super();
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        try {
            JLabel renderComponent = ((JLabel) c);
            long diff = (Long) value;

            long five_minutes = 5 * MINUTE;
            long ten_minutes = 10 * MINUTE;
            Color color = null;
            if (row % 2 == 0) {
                color = Constants.DS_ROW_A;
            } else {
                color = Constants.DS_ROW_B;
            }
            renderComponent.setText(DurationFormatUtils.formatDuration(diff, "HHH:mm:ss.SSS", true));

            if (diff <= 0) {
                //value is expired, stroke result
                renderComponent.setText("<html><nobr><s>" + renderComponent.getText() + "</s></nobr></html>");
            } else if (diff <= ten_minutes && diff > five_minutes) {
                float ratio = (float) (diff - five_minutes) / (float) five_minutes;
                Color c1 = Color.YELLOW;
                Color c2 = Color.GREEN;
                int red = (int) (c2.getRed() * ratio + c1.getRed() * (1 - ratio));
                int green = (int) (c2.getGreen() * ratio + c1.getGreen() * (1 - ratio));
                int blue = (int) (c2.getBlue() * ratio + c1.getBlue() * (1 - ratio));
                color = new Color(red, green, blue);
            } else if (diff <= five_minutes) {
                float ratio = (float) diff / (float) five_minutes;
                Color c1 = Color.RED;
                Color c2 = Color.YELLOW;
                int red = (int) (c2.getRed() * ratio + c1.getRed() * (1 - ratio));
                int green = (int) (c2.getGreen() * ratio + c1.getGreen() * (1 - ratio));
                int blue = (int) (c2.getBlue() * ratio + c1.getBlue() * (1 - ratio));
                color = new Color(red, green, blue);
            }
            if (isSelected) {
                color = c.getBackground();
            }
            renderComponent.setOpaque(true);
            renderComponent.setBackground(color);
            JPanel panel = new JPanel(new GridBagLayout());
            panel.add(renderComponent, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, 1, new Insets(0, 0, 0, 0), 0, 0));
            return panel;
        } catch (Exception e) {
            return c;
        }
    }
}
