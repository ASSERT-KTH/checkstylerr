/*******************************************************************************
 * Copyright (c) 2014 BREDEX GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BREDEX GmbH - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.jubula.examples.extension.javafx.toolkit.provider;

import java.net.URL;
import java.util.ResourceBundle;

import org.eclipse.jubula.examples.extension.javafx.toolkit.Activator;
import org.eclipse.jubula.toolkit.common.AbstractToolkitProvider;
import org.eclipse.jubula.toolkit.common.utils.ToolkitUtils;

public class MyToolkitProvider extends AbstractToolkitProvider {
	/** the bundle location */
    public static final String BUNDLE = "org.eclipse.jubula.examples.extension.javafx.toolkit.i18n.i18n"; //$NON-NLS-1$

    /** {@inheritDoc} */
    public URL getComponentConfigurationFileURL() {
        return ToolkitUtils.getURL(Activator.getDefault().getBundle(),
                COMP_CONFIG_PATH);
    }
    
    /** {@inheritDoc} */
    public ResourceBundle getResourceBundle() {
        return ResourceBundle.getBundle(BUNDLE);
    }
}
