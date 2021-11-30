package extension.junit;
/*******************************************************************************
 * Copyright (c) 2021 BREDEX GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BREDEX GmbH - initial API and implementation and/or initial documentation
 *******************************************************************************/

import org.eclipse.jubula.autagent.Embedded;
import org.eclipse.jubula.client.AUTAgent;

/**
 * This class can be used to start the AUT Agent and the AUT. This is needed
 * @author BREDEX GmbH
 *
 */
public class StartAgentAndAUT {

	public static void main(String[] args) {
		AUTAgent agent = Embedded.INSTANCE.agent();
		agent.connect();
		agent.startAUT(AUTs.SIMPLEADDER.getConfig());
		agent.disconnect();

	}

}
