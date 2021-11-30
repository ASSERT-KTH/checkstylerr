/*******************************************************************************
 * Copyright (c) 2004, 2010 BREDEX GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BREDEX GmbH - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.jubula.autagent.common.desktop;

import java.awt.AWTException;
import java.awt.CheckboxMenuItem;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang.text.StrSubstitutor;
import org.eclipse.jubula.autagent.common.AutStarter;
import org.eclipse.jubula.autagent.common.agent.AutAgent;
import org.eclipse.jubula.autagent.common.gui.ObjectMappingFrame;
import org.eclipse.jubula.autagent.common.i18n.Messages;
import org.eclipse.jubula.communication.internal.Communicator;
import org.eclipse.jubula.communication.internal.Communicator.ConnectionManager;
import org.eclipse.jubula.tools.internal.constants.StringConstants;
import org.eclipse.jubula.tools.internal.registration.AutIdentifier;
import org.eclipse.jubula.tools.internal.utils.EnvironmentUtils;
import org.osgi.framework.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author BREDEX GmbH
 * @created 11.06.2010
 */
public abstract class DesktopIntegration implements PropertyChangeListener {
    /** name of the property which AUT is in OMM */
    private static final String PROP_OBJECT_MAPPING_AUT = "OBJECT_MAPPING_AUT"; //$NON-NLS-1$

    /** if the object agent OM is used the AUT */
    private static AutIdentifier omAut = null;
    
    /** property change support */
    private static PropertyChangeSupport propertyChangedSupport;
    /** the logger */
    private static final Logger LOG = 
            LoggerFactory.getLogger(DesktopIntegration.class);

    
    /** is the system tray supported on this platform */
    private boolean m_isSystraySupported;
    
    /** access to system tray */
    private TrayIcon m_trayIcon;
    
    /** status: port number */
    private int m_port = 0;
    
    /** {@link PopupMenu} for the start of the ObjectMapping*/
    private PopupMenu m_startOMMMenu = null;
    /** {@link PopupMenu} for the stop of the ObjectMapping*/
    private MenuItem m_stopOMMMenu = null;
    
    /** status: connected AUTs */
    private List<String> m_auts = new ArrayList<String>();

    /** version information */
    private Version m_version;
    
    /**
     * create the necessary environment
     * 
     * @param autAgent The AUT Agent monitored by the created object.
     */
    public DesktopIntegration(final AutAgent autAgent) {
        extractVersionInformation();
        ObjectMappingFrame.INSTANCE.setDesktopIntegration(this);
        propertyChangedSupport = new PropertyChangeSupport(this);
        propertyChangedSupport.addPropertyChangeListener(this);
        if (EnvironmentUtils.isMacOS()) {
            // WORKAROUND for hanging SystemTray.isSupported()
            m_isSystraySupported = false;
            return;
        }
        m_isSystraySupported = SystemTray.isSupported();
        if (m_isSystraySupported) {

            SystemTray tray = SystemTray.getSystemTray();
            URL imageURL = DesktopIntegration.class.getResource("/resources/autagent.png"); //$NON-NLS-1$
            Image image = Toolkit.getDefaultToolkit().getImage(imageURL);

            ActionListener exitListener = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    System.exit(0);
                }
            };
            PopupMenu popup = new PopupMenu();
            final CheckboxMenuItem strictModeItem = new CheckboxMenuItem("Strict AUT Management"); //$NON-NLS-1$

            autAgent.addPropertyChangeListener(
                    AutAgent.PROP_KILL_DUPLICATE_AUTS, 
                    new PropertyChangeListener() {
                        @SuppressWarnings("synthetic-access")
                        public void propertyChange(PropertyChangeEvent evt) {
                            Object newValue = evt.getNewValue();
                            if (newValue instanceof Boolean) {
                                boolean isKillDuplicateAuts = 
                                    ((Boolean)newValue).booleanValue();
                                strictModeItem.setState(isKillDuplicateAuts);
                            } else {
                                LOG.error("Expected new value for property to be of type " + Boolean.class.getName()); //$NON-NLS-1$
                            }
                        }
                    });
            boolean isKillDuplicateAuts = autAgent.isKillDuplicateAuts();
            strictModeItem.setState(isKillDuplicateAuts);
            strictModeItem.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    autAgent.setKillDuplicateAuts(
                            e.getStateChange() == ItemEvent.SELECTED);
                }
            });
            MenuItem defaultItem = new MenuItem("Exit"); //$NON-NLS-1$
            defaultItem.addActionListener(exitListener);
            
            popup.add(strictModeItem);
            PopupMenu objectMappingMenu = createObjectMappingPopup();
            popup.add(objectMappingMenu);
            popup.addSeparator();
            popup.add(defaultItem);

            m_trayIcon = new TrayIcon(image, "AUT Agent", popup); //$NON-NLS-1$
            m_trayIcon.setImageAutoSize(true);
            try {
                tray.add(m_trayIcon);
            } catch (AWTException e) {
                m_isSystraySupported = false; // strange but ignorable
            }
        }
    }

    /**
     * extracts the version properties information.
     * the file is copied from an other project therefore it might not be there
     */
    private void extractVersionInformation() {
        InputStream versionInfo = DesktopIntegration.class
                .getResourceAsStream("/resources/version.properties"); //$NON-NLS-1$
        Properties p = new Properties();
        try {
            p.load(versionInfo);
            String versionString = StrSubstitutor.replace(
                    p.getProperty("build.version"), p); //$NON-NLS-1$
            m_version = new Version(versionString);
            versionInfo.close();
        } catch (Exception e) {
            LOG.warn("Version properties could not be read"); //$NON-NLS-1$
        }
    }
    
    /**
     * @return the AUT which is in the agent Mapping Mode or 
     * <code>null</code> id there is no
     */
    public static AutIdentifier getObjectMappingAUT() {
        return omAut;
    }
    
    /**
     * @param autID the AUT which is set to OMM or <code>null</code>
     * if there is no AUT
     */
    public static void setObjectMappingAUT(AutIdentifier autID) {
        AutIdentifier id = omAut;
        omAut = autID;
        ObjectMappingFrame.INSTANCE
            .setOMAutName(autID != null ? autID.getID() : null);
        propertyChangedSupport.firePropertyChange(PROP_OBJECT_MAPPING_AUT,
                id, omAut);
    }

    /**
     * 
     * @return the popup menu with its children
     */
    private PopupMenu createObjectMappingPopup() {
        PopupMenu objectMappingMenu = new PopupMenu(
                Messages.ObjectMappingMenu);
        m_startOMMMenu = new PopupMenu(Messages.StartMenu);
        m_startOMMMenu.setEnabled(false);
        m_stopOMMMenu = new MenuItem(Messages.StopMenu);
        m_stopOMMMenu.setEnabled(false);
        MenuItem settings = new MenuItem(Messages.ObjectMappingOpen);
        settings.addActionListener(new ActionListener() {
            /** show the object mapping windows*/
            public void actionPerformed(ActionEvent e) {
                ObjectMappingFrame.INSTANCE.showObjectMappingPanel();
            }
        });
        objectMappingMenu.add(m_startOMMMenu);
        objectMappingMenu.add(m_stopOMMMenu);
        objectMappingMenu.add(settings);
        return objectMappingMenu;
    }

    /**
     * update the tray icon when the status changes
     */
    private void updateStatus() {
        if (m_isSystraySupported) {
            m_trayIcon.setToolTip(buildToolTip());
        }        
    }
    /**
     * @return info according to status fields
     */
    private String buildToolTip() {
        String version = m_version != null ? m_version.toString()
                : StringConstants.EMPTY;
        StringBuilder tt = new StringBuilder(
                "AUT Agent " + version);  //$NON-NLS-1$
        tt.append(StringConstants.NEWLINE);
        tt.append(" Port used: "); //$NON-NLS-1$
        tt.append(m_port);
        if (!m_auts.isEmpty()) {
            tt.append(StringConstants.NEWLINE);
            tt.append(StringConstants.SPACE);
            tt.append(m_auts.size());
            tt.append(" running AUT"); //$NON-NLS-1$
            if (m_auts.size() > 1) {
                tt.append('s');
            }
            tt.append(StringConstants.COLON);
            
            String omAutId = omAut != null ? omAut.getID() : null;
            for (String aut : m_auts) {
                tt.append(StringConstants.NEWLINE);
                tt.append(StringConstants.SPACE);
                tt.append(aut);
                if (omAutId != null && aut.equals(omAutId)) {
                    tt.append(StringConstants.SPACE);
                    tt.append("[OM]"); //$NON-NLS-1$
                }
            }
        }
        
        return tt.toString();
    }
    
    /**
     * @param port info
     */
    public void setPort(int port) {
        m_port = port;
        updateStatus();
    }
    /**
     * {@inheritDoc}
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(AutAgent.PROP_NAME_AUTS)) {
            AutStarter.getInstance().getCommunicator()
            .getConnectionManager().removePropertyChangedListener(this);
            AutStarter.getInstance().getCommunicator()
            .getConnectionManager().addPropertyChangedListener(this);
            if (evt.getNewValue() instanceof AutIdentifier) {
                AutIdentifier aut = (AutIdentifier)evt.getNewValue();
                m_auts.add(aut.getExecutableName());
                rebuildOMSubMenu();
            }
            if (evt.getOldValue() instanceof AutIdentifier) {
                AutIdentifier aut = (AutIdentifier)evt.getOldValue();
                if (omAut != null && omAut.equals(aut)) {
                    omAut = null;
                }
                m_auts.remove(aut.getExecutableName());
                rebuildOMSubMenu();
            }
        }
        if (evt.getPropertyName().equals(PROP_OBJECT_MAPPING_AUT) 
                || evt.getPropertyName().equals(
                        ConnectionManager.PROP_CONNECTION_CHANGE)) {
            rebuildOMSubMenu();
        }

        updateStatus();
    }

    
    /**
     * rebuilds the sub menus in the object mapping part.
     * This is enabling/disabling the menus and adding the AUTs to the 
     * start and stop sub menus
     */
    private void rebuildOMSubMenu() {
        /** Start Menu Items*/
        m_startOMMMenu.removeAll();
        Communicator clientComm = 
                AutStarter.getInstance().getCommunicator();
        if ((clientComm != null && clientComm.getConnectionManager() != null
                && clientComm.getConnectionManager().getNextState() != 0)) {
            m_startOMMMenu.setEnabled(false);
        } else {
            m_startOMMMenu.setEnabled((omAut == null && m_auts.size() > 0));
        }
        
        Set<AutIdentifier> auts = AutStarter.getInstance()
                .getAgent().getAuts();
        for (AutIdentifier autIdentifier : auts) {
            MenuItem menuItem = new MenuItem(autIdentifier.getID());
            menuItem.addActionListener(createStartListener(autIdentifier));
            m_startOMMMenu.add(menuItem);
        }
        /**  STOP menu items    */
        m_stopOMMMenu.setEnabled(omAut != null);
        ActionListener[] listener = m_stopOMMMenu.getActionListeners();
        for (int i = 0; i < listener.length; i++) {
            m_stopOMMMenu.removeActionListener(listener[i]);
        }
        if (m_stopOMMMenu.isEnabled()) {
            m_stopOMMMenu.addActionListener(createStopListener(omAut));
        }
    }

    /**
     * the actionListener for starting the objectMapping Mode for an AUT
     * @param id {@link AutIdentifier} to identify the AUT
     * @return the {@link ActionListener}
     */
    public abstract ActionListener createStartListener(AutIdentifier id);

    /**
     * the actionListener for stopping the objectMapping Mode for an AUT
     * @param id {@link AutIdentifier} to identify the AUT
     * @return the {@link ActionListener}
     */
    public abstract ActionListener createStopListener(AutIdentifier id);
    
    /**
     * remove the system Tray icon
     */
    public void removeSystemTray() {
        try {
            if (SystemTray.isSupported()) {
                SystemTray tray = SystemTray.getSystemTray();
                tray.remove(m_trayIcon);
            }
        } catch (UnsupportedOperationException e) {
            // Nothing to do
        }
    }
}