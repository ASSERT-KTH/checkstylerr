/* 
 * Copyright 2015 Torridity.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.tor.tribes.util;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.ui.LayerOrderConfigurationFrame;
import de.tor.tribes.ui.windows.DSWorkbenchMainFrame;
import java.io.File;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * @author Charon
 */
public class MainShutdownHook extends Thread {

    private static Logger logger = LogManager.getLogger("ShutdownHook");
    private static MainShutdownHook SINGLETON = null;
    
    private boolean hasRun = false;

    public static synchronized MainShutdownHook getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new MainShutdownHook();
        }
        return SINGLETON;
    }

    public MainShutdownHook() {
        setName("ShutdownHook");
        setDaemon(true);
    }

    @Override
    public synchronized void run() {
        if(hasRun) {
            return;
        }
        hasRun = true;
        try {
            logger.info("Performing ShutdownHook");
            if (!DataHolder.getSingleton().isDataValid()) {
                logger.error("Server data seems to be invalid. No user data will be stored!");
                return;
            }
            GlobalOptions.saveUserData();
            GlobalOptions.addProperty("layer.order", LayerOrderConfigurationFrame.getSingleton().getLayerOrder());
            DSWorkbenchMainFrame.getSingleton().storeProperties();
            GlobalOptions.saveProperties();
            GlobalOptions.storeViewStates();
            if (!FileUtils.deleteQuietly(new File("runningFile"))) {
                logger.warn("Failed to remove file 'runningFile'");
            }
            logger.debug("Shutdown finished");
        } catch (Throwable t) {
            logger.error("Shutdown failed", t);
        }
        System.exit(0);
    }
}
