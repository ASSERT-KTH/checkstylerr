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
package org.eclipse.jubula.examples.api.hanoi.javafx;

import java.util.Locale;

import org.eclipse.jubula.client.AUT;
import org.eclipse.jubula.client.AUTAgent;
import org.eclipse.jubula.client.MakeR;
import org.eclipse.jubula.client.launch.AUTConfiguration;
import org.eclipse.jubula.toolkit.base.components.GraphicsComponent;
import org.eclipse.jubula.toolkit.enums.ValueSets.InteractionMode;
import org.eclipse.jubula.toolkit.enums.ValueSets.Modifier;
import org.eclipse.jubula.toolkit.enums.ValueSets.Unit;
import org.eclipse.jubula.toolkit.javafx.JavafxComponents;
import org.eclipse.jubula.toolkit.javafx.config.JavaFXAUTConfiguration;
import org.eclipse.jubula.tools.AUTIdentifier;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/** @author BREDEX GmbH */
public class TowersOfHanoi {
	
    /** AUT-Agent host name to use */
    public static final String AGENT_HOST = "localhost"; //$NON-NLS-1$
    /** AUT-Agent port to use */
    public static final int AGENT_PORT = 60000;
    
    /** area1 */
    private static GraphicsComponent area1;
    /** area2 */
    private static GraphicsComponent area2;
    /** area3 */
    private static GraphicsComponent area3;
    
    /** the button */
    private static GraphicsComponent resetButton;
    
    /** the AUT-Agent */
    private AUTAgent m_agent;
    /** the AUT */
    private AUT m_aut;

    /** global prepare */
    @BeforeClass
    public static void loadObjectMapping() throws Exception {
        area1 = JavafxComponents.createImageView(OM.Area1);
        area2 = JavafxComponents.createImageView(OM.Area2);
        area3 = JavafxComponents.createImageView(OM.Area3);
        resetButton = JavafxComponents.createButton(OM.ResetButton);
    }

    /** prepare */
    @Before
    public void setUp() throws Exception {
        m_agent = getAUTAgentInstance();
        m_agent.connect();

        final String autID = "Towers_Of_Hanoi"; //$NON-NLS-1$
        AUTConfiguration config = new JavaFXAUTConfiguration(
                "api.aut.conf.towersofhanoi.javafx", //$NON-NLS-1$
                autID,
                "//path/to/JRE", //$NON-NLS-1$
                "//aut/working/dir", //$NON-NLS-1$ 
                new String[]{"-jar", "hanoi.jar"} //$NON-NLS-1$ //$NON-NLS-2$
                );

        AUTIdentifier id = m_agent.startAUT(config);
        if (id != null) {
            m_aut = m_agent.getAUT(id, JavafxComponents.getToolkitInformation());
            m_aut.connect();
        } else {
            Assert.fail("AUT start has failed!"); //$NON-NLS-1$
        }
    }

    /** the actual test method */
    @Test
    public void testMoveTower() throws Exception {
    	
        Thread.sleep(3000);
        
        /** RESET GAME  */
        m_aut.execute(
        		resetButton.click(
        				1, InteractionMode.primary), null);
        
        /** PLAY GAME */
        moveDiscs(6, area1, area2, area3);

        Thread.sleep(3000);
    }
    
    /**
     * Moves certain amount of discs from one area to another
     * @param i amount of discs
     * @param source source area
     * @param help help area
     * @param target target area
     */
	public void moveDiscs(
			int i,
			GraphicsComponent source, 
			GraphicsComponent help,
			GraphicsComponent target) {
		if (i > 0) {
    		moveDiscs(i-1, source, target, help);
    		executeDragAndDrop(source, target);    		
    		moveDiscs(i-1, help, source, target);
    	}
    }
	
	/**
	 * Executes a Drag&Drop-Action
	 * @param dragSource source
	 * @param dropTarget target
	 */
	private void executeDragAndDrop(GraphicsComponent dragSource,
			GraphicsComponent dropTarget) {
		m_aut.execute(dragSource.drag(InteractionMode.primary,
				new Modifier[] { Modifier.none }, 50, Unit.percent, 50,
				Unit.percent), null);
		m_aut.execute(dropTarget.drop(50, Unit.percent, 50, Unit.percent, 100), null);
	}

    /** cleanup */
    @After
    public void tearDown() throws Exception {
        m_aut.disconnect();
        m_agent.stopAUT(m_aut.getIdentifier());
        m_agent.disconnect();
    }

    /**
     * @return an AUT-Agent instance
     */
    protected AUTAgent getAUTAgentInstance() {
        return MakeR.createAUTAgent(AGENT_HOST, AGENT_PORT);
    }
    
}