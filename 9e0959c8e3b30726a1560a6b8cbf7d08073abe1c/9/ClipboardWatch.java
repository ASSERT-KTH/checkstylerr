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

import de.tor.tribes.ui.windows.ClockFrame;
import de.tor.tribes.ui.windows.DSWorkbenchMainFrame;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;
import java.util.zip.CRC32;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 *
 * @author Charon
 */
public class ClipboardWatch extends Thread {

    private static Logger logger = LogManager.getLogger("ClipboardMonitor");
    private static ClipboardWatch SINGLETON = null;

    public static synchronized ClipboardWatch getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new ClipboardWatch();
            SINGLETON.start();
        }
        return SINGLETON;
    }

    ClipboardWatch() {
        setName("ClipboardMonitor");
        setDaemon(true);
        setPriority(MIN_PRIORITY);
    }
    private Clip clip = null;

    private synchronized void playNotification() {
        if (!GlobalOptions.getProperties().getBoolean("clipboard.notification")) {
            return;
        }

        Timer t = new Timer("ClipboardNotification", true);
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                if (clip != null) {//reset clip
                    clip.stop();
                    clip.setMicrosecondPosition(0);
                }

                try {
                    if (clip == null) {
                        clip = AudioSystem.getClip();
                        InputStream data = new BufferedInputStream(ClockFrame.class.getResourceAsStream("/res/Ding.wav"));
                        AudioInputStream inputStream = AudioSystem.getAudioInputStream(data);
                        clip.open(inputStream);
                    }
                    clip.start();
                } catch (Exception e) {
                    logger.error("Failed to play notification", e);
                }
            }
        }, 0);
    }

    @Override
    public void run() {
        logger.info("Starting ClipboardMonitor");
        long lastCRC = 0;
        CRC32 c = new CRC32(); //use CRC32 because it is fast
        while (true) {
            if (DSWorkbenchMainFrame.getSingleton().isWatchClipboard()) {
                try {
                    Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
                    String data = (String) t.getTransferData(DataFlavor.stringFlavor);
                    c.reset();
                    c.update(data.getBytes("UTF-8"));
                    long currentCRC = c.getValue();

                    if ((data.length() > 10) && currentCRC != lastCRC) {
                        if (PluginManager.getSingleton().executeReportParser(data)) {
                            //report parsed, clean clipboard
                            logger.info("Report successfully parsed.");
                            playNotification();
                        } else if (PluginManager.getSingleton().executeTroopsParser(data)) {
                            logger.info("Troops successfully parsed.");
                            SystrayHelper.showInfoMessage("Truppen erfolgreich eingelesen");
                            playNotification();
                        } else if (PluginManager.getSingleton().executeGroupParser(data)) {
                            logger.info("Groups successfully parsed.");
                            SystrayHelper.showInfoMessage("Gruppen erfolgreich eingelesen");
                            playNotification();
                        } else if (PluginManager.getSingleton().executeSupportParser(data)) {
                            logger.info("Support successfully parsed.");
                            SystrayHelper.showInfoMessage("Unterstützungen erfolgreich eingelesen");
                            playNotification();
                        } else if (PluginManager.getSingleton().executeNonPAPlaceParser(data)) {
                            logger.info("Place info successfully parsed.");
                            SystrayHelper.showInfoMessage("Truppen aus Versammlungsplatz erfolgreich eingelesen");
                            playNotification();
                        } else if (PluginManager.getSingleton().executeDiplomacyParser(data)) {
                            logger.info("Diplomacy info successfully parsed.");
                            SystrayHelper.showInfoMessage("Kartenmarkierungen aus Diplomatie erfolgreich eingelesen");
                            playNotification();
                        } else if (PluginManager.getSingleton().executeMovementParser(data)) {
                            logger.info("Movements successfully parsed.");
                            SystrayHelper.showInfoMessage("Befehle erfolgreich eingelesen");
                            playNotification();
                        } else if (PluginManager.getSingleton().executeBuildingParser(data)) {
                            logger.info("Buildings successfully parsed.");
                            SystrayHelper.showInfoMessage("Gebäude erfolgreich eingelesen");
                            playNotification();
                        }
                        lastCRC = currentCRC;
                    }
                } catch (Exception e) {
                    //no usable data
                    //  e.printStackTrace();
                }
            } else {
                //clipboard watch is disabled, sleep 9 + 1 seconds
                try {
                    Thread.sleep(9000);
                } catch (Exception ignored) {
                }
            }
            try {
                Thread.sleep(1000);
            } catch (Exception ignored) {
            }
        }
    }
}
