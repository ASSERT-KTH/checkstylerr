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
package org.eclipse.jubula.examples.extension.javafx.test;

import org.eclipse.jubula.client.AUT;
import org.eclipse.jubula.client.AUTAgent;
import org.eclipse.jubula.client.MakeR;
import org.eclipse.jubula.client.exceptions.CheckFailedException;
import org.eclipse.jubula.client.launch.AUTConfiguration;
import org.eclipse.jubula.examples.extension.javafx.PieChartComponents;
import org.eclipse.jubula.toolkit.ToolkitInfo;
import org.eclipse.jubula.toolkit.base.components.GraphicsComponent;
import org.eclipse.jubula.toolkit.concrete.components.ButtonComponent;
import org.eclipse.jubula.toolkit.enums.ValueSets.InteractionMode;
import org.eclipse.jubula.toolkit.javafx.JavafxComponents;
import org.eclipse.jubula.toolkit.javafx.JavafxToolkit;
import org.eclipse.jubula.toolkit.javafx.config.JavaFXAUTConfiguration;
import org.eclipse.jubula.tools.AUTIdentifier;
import org.eclipse.jubula.tools.ComponentIdentifier;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/** @author BREDEX GmbH */
@SuppressWarnings("nls")
public class PieChartTest {
    /** the default number of pies within the AUT */
    public static final int DEFAULT_NO_OF_SLICES = 5;
    
    /** AUT-Agent host name to use */
    public static final String AGENT_HOST = "localhost"; //$NON-NLS-1$
    /** AUT-Agent port to use */
    public static final int AGENT_PORT = 60000;
    /** the AUT-Agent */
    private AUTAgent m_agent;
    /** the AUT */
    private AUT m_aut;
    /** the pie chart component wrapper */
    private PieChartComponents m_pcc;
    /** the component identifier */
    private static final ComponentIdentifier<?> pieChartIdentifier = MakeR.createCI(
            "rO0ABXNyAD1vcmcuZWNsaXBzZS5qdWJ1bGEudG9vbHMuaW50ZXJuYWwub2JqZWN0cy5Db21wb25lbnRJZGVudGlmaWVyAAAAAAAABAcCAAlaABRtX2VxdWFsT3JpZ2luYWxGb3VuZEQAEW1fbWF0Y2hQZXJjZW50YWdlSQAhbV9udW1iZXJPZk90aGVyTWF0Y2hpbmdDb21wb25lbnRzTAAYbV9hbHRlcm5hdGl2ZURpc3BsYXlOYW1ldAASTGphdmEvbGFuZy9TdHJpbmc7TAAUbV9jb21wb25lbnRDbGFzc05hbWVxAH4AAUwAFW1fY29tcG9uZW50UHJvcGVydGllc3QAD0xqYXZhL3V0aWwvTWFwO0wAEG1faGllcmFyY2h5TmFtZXN0ABBMamF2YS91dGlsL0xpc3Q7TAAMbV9uZWlnaGJvdXJzcQB+AANMABRtX3N1cHBvcnRlZENsYXNzTmFtZXEAfgABeHAAv/AAAAAAAAD/////cHQAG2phdmFmeC5zY2VuZS5jaGFydC5QaWVDaGFydHBzcgATamF2YS51dGlsLkFycmF5TGlzdHiB0h2Zx2GdAwABSQAEc2l6ZXhwAAAABHcEAAAABHQAFGphdmFmeC5zdGFnZS5TdGFnZV8xdAAUamF2YWZ4LnNjZW5lLlNjZW5lXzF0ABpqYXZhZnguc2NlbmUubGF5b3V0LlZCb3hfMXQACHBpZUNoYXJ0eHNxAH4ABgAAAAF3BAAAAAF0ABpqYXZhZnguc2NlbmUubGF5b3V0LkhCb3hfMXhxAH4ABQ=="); //$NON-NLS-1$
    /** the button */
    private static GraphicsComponent button;

    /** global prepare */
    @BeforeClass
    public static void loadObjectMapping() throws Exception {
        ComponentIdentifier<ButtonComponent> buttonCI = MakeR.createCI(
                "rO0ABXNyAD1vcmcuZWNsaXBzZS5qdWJ1bGEudG9vbHMuaW50ZXJuYWwub2JqZWN0cy5Db21wb25lbnRJZGVudGlmaWVyAAAAAAAABAcCAAlaABRtX2VxdWFsT3JpZ2luYWxGb3VuZEQAEW1fbWF0Y2hQZXJjZW50YWdlSQAhbV9udW1iZXJPZk90aGVyTWF0Y2hpbmdDb21wb25lbnRzTAAYbV9hbHRlcm5hdGl2ZURpc3BsYXlOYW1ldAASTGphdmEvbGFuZy9TdHJpbmc7TAAUbV9jb21wb25lbnRDbGFzc05hbWVxAH4AAUwAFW1fY29tcG9uZW50UHJvcGVydGllc3QAD0xqYXZhL3V0aWwvTWFwO0wAEG1faGllcmFyY2h5TmFtZXN0ABBMamF2YS91dGlsL0xpc3Q7TAAMbV9uZWlnaGJvdXJzcQB+AANMABRtX3N1cHBvcnRlZENsYXNzTmFtZXEAfgABeHAAv/AAAAAAAAD/////cHQAG2phdmFmeC5zY2VuZS5jb250cm9sLkJ1dHRvbnBzcgATamF2YS51dGlsLkFycmF5TGlzdHiB0h2Zx2GdAwABSQAEc2l6ZXhwAAAABXcEAAAABXQAFGphdmFmeC5zdGFnZS5TdGFnZV8xdAAUamF2YWZ4LnNjZW5lLlNjZW5lXzF0ABpqYXZhZnguc2NlbmUubGF5b3V0LlZCb3hfMXQAGmphdmFmeC5zY2VuZS5sYXlvdXQuSEJveF8xdAAKcGx1c0J1dHRvbnhzcQB+AAYAAAABdwQAAAABdAALbWludXNCdXR0b254cQB+AAU="); //$NON-NLS-1$
        
        button = JavafxComponents.createButton(buttonCI);
    }

    /** prepare */
    @Before
    public void setUp() throws Exception {
        m_agent = MakeR.createAUTAgent(AGENT_HOST, AGENT_PORT);
        m_agent.connect();

        final String autID = "JavaFXExampleExtensionAUT"; //$NON-NLS-1$
        AUTConfiguration config = new JavaFXAUTConfiguration(
                "api.aut.conf.javafx.extension", //$NON-NLS-1$
                autID, 
                "..\\jre\\bin\\java.exe", //$NON-NLS-1$
                "..\\examples\\", //$NON-NLS-1$ 
                new String[]{
                        "-jar", //$NON-NLS-1$
                        "development\\extension\\AUT\\PieChart.jar" //$NON-NLS-1$
                });

        AUTIdentifier id = m_agent.startAUT(config);
        if (id != null) {
            ToolkitInfo toolkitInformation = JavafxToolkit
                    .createToolkitInformation();
            
            m_pcc = new PieChartComponents(toolkitInformation);
            
            m_aut = m_agent.getAUT(id, m_pcc.getToolkitInfo());
            m_aut.connect();
        } else {
            Assert.fail("AUT start has failed!"); //$NON-NLS-1$
        }
    }

    /** the actual test method */
    @Test
    public void testPieChartSpecificAction() throws Exception {
        m_aut.execute(m_pcc.checkNumberOfItems(pieChartIdentifier,
                DEFAULT_NO_OF_SLICES), 
                "Verify initial number of slices");

        m_aut.execute(button.click(1, InteractionMode.primary),
                "Increment number of pies");

        m_aut.execute(m_pcc.checkNumberOfItems(pieChartIdentifier,
                DEFAULT_NO_OF_SLICES + 1),
                "Verify incremented number of slices");
    }
    
    /** the actual test method */
    @Test(expected = CheckFailedException.class)
    public void testExpectedCheckFailed() throws Exception {
        m_aut.execute(m_pcc.checkNumberOfItems(pieChartIdentifier,
                DEFAULT_NO_OF_SLICES - 1), 
                "Expected failure for initial number of slice verification");
    }

    /** cleanup */
    @After
    public void tearDown() throws Exception {
        m_aut.disconnect();
        m_agent.stopAUT(m_aut.getIdentifier());
        m_agent.disconnect();
    }
}