package extension.junit;
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

import org.eclipse.jubula.client.launch.AUTConfiguration;
import org.eclipse.jubula.toolkit.swing.config.SwingAUTConfiguration;

public enum AUTs {

	SIMPLEADDER(new SwingAUTConfiguration("SimpleAdder", "SimpleAdder", "SimpleAdder.cmd", ".\\", new String[0]));
	
	private AUTConfiguration config;

	AUTs(AUTConfiguration config) {
		this.setConfig(config);
		
	}

	public AUTConfiguration getConfig() {
		return config;
	}

	public void setConfig(AUTConfiguration config) {
		this.config = config;
	}
}
