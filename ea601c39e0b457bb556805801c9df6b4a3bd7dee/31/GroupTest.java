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
package org.eclipse.jubula.examples.extension.rcp.test;

import java.util.Locale;

import org.eclipse.jubula.client.AUT;
import org.eclipse.jubula.client.AUTAgent;
import org.eclipse.jubula.client.MakeR;
import org.eclipse.jubula.client.exceptions.CheckFailedException;
import org.eclipse.jubula.client.launch.AUTConfiguration;
import org.eclipse.jubula.examples.extension.rcp.GroupComponents;
import org.eclipse.jubula.toolkit.ToolkitInfo;
import org.eclipse.jubula.toolkit.concrete.components.MenuBarComponent;
import org.eclipse.jubula.toolkit.enums.ValueSets.Operator;
import org.eclipse.jubula.toolkit.rcp.config.RCPAUTConfiguration;
import org.eclipse.jubula.toolkit.swt.SwtComponents;
import org.eclipse.jubula.toolkit.swt.SwtToolkit;
import org.eclipse.jubula.tools.AUTIdentifier;
import org.eclipse.jubula.tools.ComponentIdentifier;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/** @author BREDEX GmbH */
@SuppressWarnings("nls")
public class GroupTest {
    
    /** AUT-Agent host name to use */
    public static final String AGENT_HOST = "localhost"; //$NON-NLS-1$
    /** AUT-Agent port to use */
    public static final int AGENT_PORT = 60000;
    /** the AUT-Agent */
    private AUTAgent m_agent;
    /** the AUT */
    private AUT m_aut;
    /** the group component wrapper */
    private GroupComponents m_gc;
    /** the component identifier */
    private static final ComponentIdentifier<?> groupIdentifier = MakeR.createCI(
            "rO0ABXNyAD1vcmcuZWNsaXBzZS5qdWJ1bGEudG9vbHMuaW50ZXJuYWwub2JqZWN0cy5Db21wb25lbnRJZGVudGlmaWVyAAAAAAAABAcCAAlaABRtX2VxdWFsT3JpZ2luYWxGb3VuZEQAEW1fbWF0Y2hQZXJjZW50YWdlSQAhbV9udW1iZXJPZk90aGVyTWF0Y2hpbmdDb21wb25lbnRzTAAYbV9hbHRlcm5hdGl2ZURpc3BsYXlOYW1ldAASTGphdmEvbGFuZy9TdHJpbmc7TAAUbV9jb21wb25lbnRDbGFzc05hbWVxAH4AAUwAFW1fY29tcG9uZW50UHJvcGVydGllc3QAD0xqYXZhL3V0aWwvTWFwO0wAEG1faGllcmFyY2h5TmFtZXN0ABBMamF2YS91dGlsL0xpc3Q7TAAMbV9uZWlnaGJvdXJzcQB+AANMABRtX3N1cHBvcnRlZENsYXNzTmFtZXEAfgABeHAAv/AAAAAAAAD/////cHQAHW9yZy5lY2xpcHNlLnN3dC53aWRnZXRzLkdyb3VwcHNyABNqYXZhLnV0aWwuQXJyYXlMaXN0eIHSHZnHYZ0DAAFJAARzaXpleHAAAAAMdwQAAAAMdAAfb3JnLmVjbGlwc2Uuc3d0LndpZGdldHMuU2hlbGxfMXQAI29yZy5lY2xpcHNlLnN3dC53aWRnZXRzLkNvbXBvc2l0ZV8xcQB+AAlxAH4ACXEAfgAJcQB+AAl0ACNvcmcuZWNsaXBzZS5zd3QuY3VzdG9tLkNUYWJGb2xkZXJfMXQAI29yZy5lY2xpcHNlLnN3dC53aWRnZXRzLkNvbXBvc2l0ZV8ydAA7b3JnLmVjbGlwc2UuanVidWxhLmV4YW1wbGVzLmV4dGVuc2lvbi5yY3AuYXV0LkV4dGVuc2lvblZpZXdxAH4ACXQAFkFVVEV4dGVuc2lvbi5Db21wb3NpdGV0ABlBVVRFeHRlbnNpb24uVmVyc2lvbkdyb3VweHNxAH4ABgAAAAF3BAAAAAF0AB9vcmcuZWNsaXBzZS5zd3Qud2lkZ2V0cy5Hcm91cF8xeHEAfgAF"); //$NON-NLS-1$


    /** prepare */
    @Before
    public void setUp() throws Exception {
        m_agent = MakeR.createAUTAgent(AGENT_HOST, AGENT_PORT);
        m_agent.connect();

        final String autID = "RCPExampleExtensionAUT"; //$NON-NLS-1$
        AUTConfiguration config = new RCPAUTConfiguration(
                "api.aut.conf.rcp.extension", //$NON-NLS-1$
                autID, 
                "AUTs\\SimpleAdder\\rcp\\win32\\win32\\x86\\SimpleAdder.exe", //$NON-NLS-1$
                "..\\examples\\", //$NON-NLS-1$ 
                null, 
                Locale.getDefault());

        AUTIdentifier id = m_agent.startAUT(config);
        if (id != null) {
            ToolkitInfo toolkitInformation = SwtToolkit
                    .createToolkitInformation();
            
            m_gc = new GroupComponents(toolkitInformation);
            
            m_aut = m_agent.getAUT(id, m_gc.getToolkitInfo());
            m_aut.connect();
        } else {
            Assert.fail("AUT start has failed!"); //$NON-NLS-1$
        }
        MenuBarComponent menu = SwtComponents.createMenu();
        m_aut.execute(menu.selectMenuEntryByTextpath(
                "Window.*/Show.*View/Extension.*", Operator.matches)
                , "Open group view");
    }

    /** the actual test method */
    @Test
    public void testGroupSpecificAction() throws Exception {
        m_aut.execute(m_gc.verifyText(groupIdentifier,
                "Bundle name", Operator.matches),
                "Verify group name");
    }
    
    /** the actual test method */
    @Test(expected = CheckFailedException.class)
    public void testExpectedCheckFailed() throws Exception {
        m_aut.execute(m_gc.verifyText(groupIdentifier,
                "Bundle version", Operator.matches),
                "Verify group name is not version");
    }

    /** cleanup */
    @After
    public void tearDown() throws Exception {
        m_aut.disconnect();
        m_agent.stopAUT(m_aut.getIdentifier());
        m_agent.disconnect();
    }
}
