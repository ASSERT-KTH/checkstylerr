/*******************************************************************************
 * Copyright (c) 2018 Kiel University and others.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.elk.core.debug.grandom.ui;

import com.google.inject.Injector;
import org.eclipse.core.runtime.Platform;
import org.eclipse.elk.core.debug.grandom.ui.internal.GrandomActivator;
import org.eclipse.xtext.ui.guice.AbstractGuiceAwareExecutableExtensionFactory;
import org.osgi.framework.Bundle;

/**
 * This class was generated. Customizations should only happen in a newly
 * introduced subclass. 
 */
public class GRandomExecutableExtensionFactory extends AbstractGuiceAwareExecutableExtensionFactory {

	@Override
	protected Bundle getBundle() {
		return Platform.getBundle(GrandomActivator.PLUGIN_ID);
	}
	
	@Override
	protected Injector getInjector() {
		GrandomActivator activator = GrandomActivator.getInstance();
		return activator != null ? activator.getInjector(GrandomActivator.ORG_ECLIPSE_ELK_CORE_DEBUG_GRANDOM_GRANDOM) : null;
	}

}
