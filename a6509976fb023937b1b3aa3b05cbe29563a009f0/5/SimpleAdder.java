package org.eclipse.jubula.examples.api.swing;
/*******************************************************************************
 * Copyright (c) 2019 BREDEX GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BREDEX GmbH - initial API and implementation and/or initial documentation
 *******************************************************************************/

import org.eclipse.jubula.toolkit.base.components.handler.GraphicsComponentActionHandler;
import org.eclipse.jubula.toolkit.concrete.components.ButtonComponent;
import org.eclipse.jubula.toolkit.concrete.components.TextComponent;
import org.eclipse.jubula.toolkit.concrete.components.TextInputComponent;
import org.eclipse.jubula.toolkit.concrete.components.handler.TextComponentActionHandler;
import org.eclipse.jubula.toolkit.concrete.components.handler.TextInputComponentActionHandler;
import org.eclipse.jubula.toolkit.enums.ValueSets.InteractionMode;
import org.eclipse.jubula.toolkit.enums.ValueSets.Operator;
import org.eclipse.jubula.toolkit.swing.SwingComponents;
import org.eclipse.jubula.tools.ComponentIdentifier;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import extension.junit.JubulaJunitExtension;

/** @author BREDEX GmbH */
@ExtendWith(JubulaJunitExtension.class)
public class SimpleAdder {
	/** the value1 */
	private static TextInputComponentActionHandler value1;
	/** the value2 */
	private static TextInputComponentActionHandler value2;
	/** the button */
	private static GraphicsComponentActionHandler button;
	/** the result */
	private static TextComponentActionHandler result;

	/** global prepare */
	@BeforeAll
	public static void loadObjectMapping() throws Exception {
		ComponentIdentifier<TextInputComponent> val1Id = OM.value1;
		ComponentIdentifier<TextInputComponent> val2Id = OM.value2;
		ComponentIdentifier<ButtonComponent> buttonId = OM.equalsButton;
		ComponentIdentifier<TextComponent> sumId = OM.resultField;

		value1 = SwingComponents.createJTextComponentActionHandler(val1Id);
		value2 = SwingComponents.createJTextComponentActionHandler(val2Id);
		button = SwingComponents.createAbstractButtonActionHandler(buttonId);
		result = SwingComponents.createJLabelActionHandler(sumId);
	}

	/** the actual test method */
	@Test
	public void testTestFirstSimpleAdderSteps() throws Exception {

		final int firstValue = 17;
		for (int i = 1; i < 5; i++) {
			value1.replaceText(String.valueOf(firstValue));
			value2.replaceText(String.valueOf(i));
			button.click(1, InteractionMode.primary);
			result.checkText(String.valueOf(firstValue + i), Operator.equals);
		}
	}
}