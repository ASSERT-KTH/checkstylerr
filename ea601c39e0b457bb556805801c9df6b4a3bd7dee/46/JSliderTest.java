/*******************************************************************************
 * Copyright (c) 2015 BREDEX GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BREDEX GmbH - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.jubula.examples.extension.swing.test;

import org.eclipse.jubula.client.AUT;
import org.eclipse.jubula.client.AUTAgent;
import org.eclipse.jubula.client.MakeR;
import org.eclipse.jubula.client.exceptions.CheckFailedException;
import org.eclipse.jubula.client.launch.AUTConfiguration;
import org.eclipse.jubula.examples.extension.swing.JSliderComponents;
import org.eclipse.jubula.toolkit.ToolkitInfo;
import org.eclipse.jubula.toolkit.swing.SwingToolkit;
import org.eclipse.jubula.toolkit.swing.config.SwingAUTConfiguration;
import org.eclipse.jubula.tools.AUTIdentifier;
import org.eclipse.jubula.tools.ComponentIdentifier;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/** @author BREDEX GmbH */
@SuppressWarnings("nls")
public class JSliderTest {
    
    /** AUT-Agent host name to use */
    public static final String AGENT_HOST = "localhost"; //$NON-NLS-1$
    /** AUT-Agent port to use */
    public static final int AGENT_PORT = 60000;
    /** the AUT-Agent */
    private AUTAgent m_agent;
    /** the AUT */
    private AUT m_aut;
    /** the JSlider component wrapper */
    private JSliderComponents m_jsc;
    /** the component identifier */
    private static final ComponentIdentifier<?> jSliderIdentifier = MakeR.createCI(
            "rO0ABXNyAD1vcmcuZWNsaXBzZS5qdWJ1bGEudG9vbHMuaW50ZXJuYWwub2JqZWN0cy5Db21wb25lbnRJZGVudGlmaWVyAAAAAAAABAcCAAlaABRtX2VxdWFsT3JpZ2luYWxGb3VuZEQAEW1fbWF0Y2hQZXJjZW50YWdlSQAhbV9udW1iZXJPZk90aGVyTWF0Y2hpbmdDb21wb25lbnRzTAAYbV9hbHRlcm5hdGl2ZURpc3BsYXlOYW1ldAASTGphdmEvbGFuZy9TdHJpbmc7TAAUbV9jb21wb25lbnRDbGFzc05hbWVxAH4AAUwAFW1fY29tcG9uZW50UHJvcGVydGllc3QAD0xqYXZhL3V0aWwvTWFwO0wAEG1faGllcmFyY2h5TmFtZXN0ABBMamF2YS91dGlsL0xpc3Q7TAAMbV9uZWlnaGJvdXJzcQB+AANMABRtX3N1cHBvcnRlZENsYXNzTmFtZXEAfgABeHAAv/AAAAAAAAD/////cHQAE2phdmF4LnN3aW5nLkpTbGlkZXJwc3IAE2phdmEudXRpbC5BcnJheUxpc3R4gdIdmcdhnQMAAUkABHNpemV4cAAAAAV3BAAAAAV0AAZmcmFtZTB0ABdqYXZheC5zd2luZy5KUm9vdFBhbmVfMXQAEG51bGwubGF5ZXJlZFBhbmV0ABBudWxsLmNvbnRlbnRQYW5ldAAVamF2YXguc3dpbmcuSlNsaWRlcl8xeHNxAH4ABgAAAAB3BAAAAAB4cQB+AAU="); //$NON-NLS-1$

    /** prepare */
    @Before
    public void setUp() throws Exception {
        m_agent = MakeR.createAUTAgent(AGENT_HOST, AGENT_PORT);
        m_agent.connect();

        final String autID = "SwingExampleExtensionAUT"; //$NON-NLS-1$
        AUTConfiguration config = new SwingAUTConfiguration(
                "api.aut.conf.swing.extension", //$NON-NLS-1$
                autID, 
                "..\\jre\\bin\\java.exe", //$NON-NLS-1$
                "..\\examples\\", //$NON-NLS-1$ 
                new String[]{
                        "-jar", //$NON-NLS-1$
                        "development\\extension\\AUT\\JSlider.jar" //$NON-NLS-1$
                });

        AUTIdentifier id = m_agent.startAUT(config);
        if (id != null) {
            ToolkitInfo toolkitInformation = SwingToolkit
                    .createToolkitInformation();
            
            m_jsc = new JSliderComponents(toolkitInformation);
            
            m_aut = m_agent.getAUT(id, m_jsc.getToolkitInfo());
            m_aut.connect();
        } else {
            Assert.fail("AUT start has failed!"); //$NON-NLS-1$
        }
    }

    /** the actual test method */
    @Test
    public void testJSliderSpecificAction() throws Exception {
        m_aut.execute(m_jsc.verifyLabelExists(jSliderIdentifier,
                false), 
                "Verify JSlider has no label");

    }
    
    /** the actual test method */
    @Test(expected = CheckFailedException.class)
    public void testExpectedCheckFailed() throws Exception {
        m_aut.execute(m_jsc.verifyLabelExists(jSliderIdentifier,
                true), 
                "Expected failure for label existence verification");
    }

    /** cleanup */
    @After
    public void tearDown() throws Exception {
        m_aut.disconnect();
        m_agent.stopAUT(m_aut.getIdentifier());
        m_agent.disconnect();
    }
}