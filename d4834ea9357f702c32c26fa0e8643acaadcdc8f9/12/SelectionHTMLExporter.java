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
package de.tor.tribes.util.html;

import de.tor.tribes.io.ServerManager;
import de.tor.tribes.types.ext.Ally;
import de.tor.tribes.types.ext.BarbarianAlly;
import de.tor.tribes.types.ext.Barbarians;
import de.tor.tribes.types.ext.NoAlly;
import de.tor.tribes.types.ext.Tribe;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.EscapeChars;
import de.tor.tribes.util.GlobalOptions;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 *
 * @author Charon
 */
public class SelectionHTMLExporter {

    private static Logger logger = LogManager.getLogger("SelectionHTMLExporter");
    private static String HEADER = "";
    private static String FOOTER = "";
    private static String ALLY_BLOCK = "";
    private static String TRIBE_BLOCK = "";
    private static String VILLAGE_BLOCK = "";
    private static boolean TEMPLATE_ERROR = false;
    //header variables
    private static final String CREATOR = "$CREATOR";
    private static final String SERVER = "$SERVER";
    private static final String PLANNAME = "$PLANNAME";
    private static final String ATTACK_COUNT = "$ATTACK_COUNT";
    //ally block variables
    private static final String ALLY_DIV_ID = "$ALLY_DIV_ID";
    private static final String ALLY_TAB_ID = "$ALLY_TAB_ID";
    private static final String ALLY_NAME = "$ALLY_NAME";
    private static final String ALLY_MEMBER = "$ALLY_MEMBER";
    private static final String ALLY_VILLAGES = "$ALLY_VILLAGES";
    private static final String ALLY_POINTS = "$ALLY_POINTS";
    private static final String ALLY_OFF = "$ALLY_OFF";
    private static final String ALLY_DEFF = "$ALLY_DEFF";
    private static final String ALLY_GUEST_LINK = "$ALLY_GUEST_LINK";
    private static final String ALLY_DSREAL_LINK = "$ALLY_DSREAL_LINK";
    private static final String ALLY_TWPLUS_LINK = "$ALLY_TWPLUS_LINK";
    private static final String TRIBE_DATA = "$TRIBE_DATA";
    //tribe block variables
    private static final String TRIBE_DIV_ID = "$TRIBE_DIV_ID";
    private static final String TRIBE_TAB_ID = "$TRIBE_TAB_ID";
    private static final String TRIBE_NAME = "$TRIBE_NAME";
    private static final String TRIBE_VILLAGES = "$TRIBE_VILLAGES";
    private static final String TRIBE_POINTS = "$TRIBE_POINTS";
    private static final String TRIBE_OFF = "$TRIBE_OFF";
    private static final String TRIBE_DEFF = "$TRIBE_DEFF";
    private static final String TRIBE_GUEST_LINK = "$TRIBE_GUEST_LINK";
    private static final String TRIBE_DSREAL_LINK = "$TRIBE_DSREAL_LINK";
    private static final String TRIBE_TWPLUS_LINK = "$TRIBE_TWPLUS_LINK";
    private static final String VILLAGE_DATA = "$VILLAGE_DATA";
    //village block variables
    private static final String VILLAGE_DIV_ID = "$VILLAGE_DIV_ID";
    private static final String VILLAGE_TAB_ID = "$VILLAGE_TAB_ID";
    private static final String VILLAGE_NAME = "$VILLAGE_NAME";
    private static final String VILLAGE_POINTS = "$VILLAGE_POINTS";
    private static final String VILLAGE_CONTINENT = "$VILLAGE_CONTINENT";
    private static final String VILLAGE_INGAME_LINK = "$VILLAGE_INGAME_LINK";
    //footer variables
    private static final String VERSION = "$VERSION";
    private static final String CREATION_DATE = "$CREATION_DATE";

    static {
        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(SelectionHTMLExporter.class.getResourceAsStream("/de/tor/tribes/tmpl/selection_header.tmpl")));
            String line = "";
            while ((line = r.readLine()) != null) {
                HEADER += line + "\n";
            }
            r.close();
            r = new BufferedReader(new InputStreamReader(SelectionHTMLExporter.class.getResourceAsStream("/de/tor/tribes/tmpl/selection_ally.tmpl")));
            line = "";
            while ((line = r.readLine()) != null) {
                ALLY_BLOCK += line + "\n";
            }
            r.close();
            r = new BufferedReader(new InputStreamReader(SelectionHTMLExporter.class.getResourceAsStream("/de/tor/tribes/tmpl/selection_tribe.tmpl")));
            line = "";
            while ((line = r.readLine()) != null) {
                TRIBE_BLOCK += line + "\n";
            }
            r.close();
            r = new BufferedReader(new InputStreamReader(SelectionHTMLExporter.class.getResourceAsStream("/de/tor/tribes/tmpl/selection_village.tmpl")));
            line = "";
            while ((line = r.readLine()) != null) {
                VILLAGE_BLOCK += line + "\n";
            }
            r.close();
            r = new BufferedReader(new InputStreamReader(SelectionHTMLExporter.class.getResourceAsStream("/de/tor/tribes/tmpl/selection_footer.tmpl")));
            line = "";
            while ((line = r.readLine()) != null) {
                FOOTER += line + "\n";
            }
            r.close();
        } catch (Exception e) {
            logger.error("Failed to read templates", e);
            TEMPLATE_ERROR = true;
        }
    }

    public static void doExport(File pHtmlFile, List<Village> pVillages) {
        if (TEMPLATE_ERROR) {
            logger.warn("Skip writing HTML file due to TEMPLATE_ERROR flag");
            return;
        }

        String server = GlobalOptions.getSelectedServer();
        String guestBaseURL = ServerManager.getServerURL(server) + "/";
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(0);
        StringBuilder result = new StringBuilder();

        // <editor-fold defaultstate="collapsed" desc=" build header">
        String h = HEADER;
        result.append(h);
        // </editor-fold>

        Collections.sort(pVillages, Village.ALLY_TRIBE_VILLAGE_COMPARATOR);
        HashMap<Ally, HashMap<Tribe, List<Village>>> data = new HashMap<>();
        for (Village v : pVillages) {
            Tribe t = v.getTribe();
            Ally a = null;
            if (t == null) {
                t = Barbarians.getSingleton();
                a = BarbarianAlly.getSingleton();
            } else {
                a = t.getAlly();
                if (a == null) {
                    a = NoAlly.getSingleton();
                }
            }
            HashMap<Tribe, List<Village>> allyData = data.get(a);
            if (allyData == null) {
                allyData = new HashMap<>();
                data.put(a, allyData);
            }

            List<Village> tribeData = allyData.get(t);
            if (tribeData == null) {
                tribeData = new LinkedList<>();
                allyData.put(t, tribeData);
            }
            tribeData.add(v);
        }
        for(Ally a: data.keySet()) {
            String allyBlock = ALLY_BLOCK;
            //add general data

            allyBlock = allyBlock.replace(ALLY_DIV_ID, "ALLYDIV" + a.getId());
            allyBlock = allyBlock.replace(ALLY_TAB_ID, "ALLYTAB" + a.getId());
            allyBlock = allyBlock.replace(ALLY_NAME, EscapeChars.forHTML(a.getName()) + " [" + EscapeChars.forHTML(a.getTag()) + "]");

            allyBlock = allyBlock.replace(ALLY_MEMBER, nf.format(a.getMembers()));
            allyBlock = allyBlock.replace(ALLY_VILLAGES, nf.format(a.getVillages()));
            allyBlock = allyBlock.replace(ALLY_POINTS, nf.format(a.getPoints()) + " (" + nf.format(a.getRank()) + ")");
            //add bash data
            double off = 0;
            double deff = 0;
            for (Tribe t : a.getTribes()) {
                off += t.getKillsAtt();
                deff += t.getKillsDef();
            }
            allyBlock = allyBlock.replace(ALLY_OFF, nf.format(off));
            allyBlock = allyBlock.replace(ALLY_DEFF, nf.format(deff));
            if (!a.equals(BarbarianAlly.getSingleton()) && !a.equals(NoAlly.getSingleton())) {
                //add info links
                String allyGuest = guestBaseURL;
                allyGuest += "guest.php?screen=info_ally&id=" + a.getId();
                allyBlock = allyBlock.replace(ALLY_GUEST_LINK, "<a href=\"" + allyGuest + "\" target=\"_blank\">Gastzugang</a>");
                
                String dsRealLink = "http://dsreal.de/index.php?tool=akte&mode=ally&world=" + server + "&id=" + a.getId();
                allyBlock = allyBlock.replace(ALLY_DSREAL_LINK, "<a href=\"" + dsRealLink + "\" target=\"_blank\">DS Real</a>");
                String twPlusLink = "http://" + server + ".twplus.org/file/ally/" + a.getId() + "/";
                allyBlock = allyBlock.replace(ALLY_TWPLUS_LINK, "<a href=\"" + twPlusLink + "\" target=\"_blank\">TWPlus</a>");
            } else {
                //no addional information for barbarian or no ally
                allyBlock = allyBlock.replace(ALLY_GUEST_LINK, "");
                allyBlock = allyBlock.replace(ALLY_DSREAL_LINK, "");
                allyBlock = allyBlock.replace(ALLY_TWPLUS_LINK, "");
            }
            //build tribe data
            String tribeBlocks = "";
            HashMap<Tribe, List<Village>> tribeData = data.get(a);
            for(Tribe t: tribeData.keySet()) {
                //build new tribe block
                String tribeBlock = TRIBE_BLOCK;
                tribeBlock = tribeBlock.replace(TRIBE_DIV_ID, "TRIBEDIV" + t.getId());
                tribeBlock = tribeBlock.replace(TRIBE_TAB_ID, "TRIBETAB" + t.getId());
                tribeBlock = tribeBlock.replace(TRIBE_NAME, EscapeChars.forHTML(t.getName()));
                tribeBlock = tribeBlock.replace(TRIBE_VILLAGES, nf.format(t.getVillages()));
                tribeBlock = tribeBlock.replace(TRIBE_POINTS, nf.format(t.getPoints()) + " (" + nf.format(t.getRank()) + ")");
                tribeBlock = tribeBlock.replace(TRIBE_OFF, nf.format(t.getKillsAtt()) + " (" + nf.format(t.getRankAtt()) + ")");
                tribeBlock = tribeBlock.replace(TRIBE_DEFF, nf.format(t.getKillsDef()) + " (" + nf.format(t.getRankDef()) + ")");
                //add info links
                if (!t.equals(Barbarians.getSingleton())) {
                    String tribeGuest = guestBaseURL;
                    tribeGuest += "guest.php?screen=info_player&id=" + t.getId();
                    tribeBlock = tribeBlock.replace(TRIBE_GUEST_LINK, "<a href=\"" + tribeGuest + "\" target=\"_blank\">Gastzugang</a>");
                    
                    String dsRealLink = "http://dsreal.de/index.php?tool=akte&mode=player&world=" + server + "&id=" + t.getId();
                    tribeBlock = tribeBlock.replace(TRIBE_DSREAL_LINK, "<a href=\"" + dsRealLink + "\" target=\"_blank\">DS Real</a>");
                    String twPlusLink = "http://" + server + ".twplus.org/file/player/" + t.getId() + "/";
                    tribeBlock = tribeBlock.replace(TRIBE_TWPLUS_LINK, "<a href=\"" + twPlusLink + "\" target=\"_blank\">TWPlus</a>");
                } else {
                    //no additional information for barbarians
                    tribeBlock = tribeBlock.replace(TRIBE_GUEST_LINK, "");
                    tribeBlock = tribeBlock.replace(TRIBE_DSREAL_LINK, "");
                    tribeBlock = tribeBlock.replace(TRIBE_TWPLUS_LINK, "");
                }

                //build village blocks for current tribe
                String villageBlocks = "";
                for (Village v : tribeData.get(t)) {
                    //build new village block
                    String villageBlock = VILLAGE_BLOCK;
                    villageBlock = villageBlock.replace(VILLAGE_DIV_ID, "VILLAGEDIV" + v.getId());
                    villageBlock = villageBlock.replace(VILLAGE_TAB_ID, "VILLAGETAB" + v.getId());
                    villageBlock = villageBlock.replace(VILLAGE_NAME, EscapeChars.forHTML(v.toString()));
                    villageBlock = villageBlock.replace(VILLAGE_POINTS, nf.format(v.getPoints()));
                    villageBlock = villageBlock.replace(VILLAGE_CONTINENT, "K" + v.getContinent());

                    String villageURL = guestBaseURL + "guest.php?screen=map&x=" + v.getX() + "&y=" + v.getY();
                    villageBlock = villageBlock.replace(VILLAGE_INGAME_LINK, "<a href=\"" + villageURL + "\" target=\"_blank\">Gastzugang</a>");
                    villageBlocks += villageBlock;
                }
                //put village blocks in current tribe block
                tribeBlock = tribeBlock.replace(VILLAGE_DATA, villageBlocks);
                tribeBlocks += tribeBlock;
            }
            //put tribe blocks in current ally block
            allyBlock = allyBlock.replace(TRIBE_DATA, tribeBlocks);
            //appen ally to result
            result.append(allyBlock);
        }

        // <editor-fold defaultstate="collapsed" desc=" build footer">
        String foot = FOOTER;
        foot = foot.replace(VERSION, Double.toString(Constants.VERSION) + Constants.VERSION_ADDITION);

        SimpleDateFormat f = new SimpleDateFormat("dd.MM.yyyy 'um' HH:mm:ss 'Uhr'");

        foot = foot.replace(CREATION_DATE, f.format(new Date()));
        result.append(foot);
        // </editor-fold>

        try {
            FileWriter w = new FileWriter(pHtmlFile);
            w.write(result.toString());
            w.flush();
            w.close();
        } catch (Exception e) {
            logger.error("Failed to write HTML selection to file", e);
        }
    }
}
