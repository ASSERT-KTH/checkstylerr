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
package de.tor.tribes.util.parser;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.TroopAmountFixed;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.FightReport;
import de.tor.tribes.types.ext.Tribe;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.ui.windows.DSWorkbenchMainFrame;
import de.tor.tribes.ui.windows.NotifierFrame;
import de.tor.tribes.util.BuildingSettings;
import de.tor.tribes.util.ServerSettings;
import de.tor.tribes.util.SilentParserInterface;
import de.tor.tribes.util.report.ReportManager;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Torridity
 * @author extremeCrazyCoder
 */
public class ReportParser implements SilentParserInterface {
    //TODO rework this Code
    private static Logger logger = LogManager.getLogger("ReportParser");

    @Override
    public boolean parse(String pData) {
        try {
            FightReport r = parseReport(pData);

            if (!r.isValid()) {
                throw new Exception("No valid report data found");
            }
            r.fillMissingSpyInformation();
            ReportManager.getSingleton().addManagedElement(r);
            try {
                DSWorkbenchMainFrame.getSingleton().showSuccess("DS Workbench hat einen Kampfbericht erfolgreich eingelesen und in das Berichtset 'default' übertragen.");
            } catch (Exception e) {
                NotifierFrame.doNotification("DS Workbench hat einen Kampfbericht erfolgreich eingelesen und in das Berichtset 'default' übertragen.", NotifierFrame.NOTIFY_INFO);
            }
            return true;
        } catch (Exception e) {
            //no valid report data found
            logger.info("No valid report data found.", e);
        }
        return false;
    }

    private FightReport parseReport(String pData) {
        StringTokenizer t = new StringTokenizer(pData, "\n");
        boolean luckPart = false;
        boolean attackerPart = false;
        boolean defenderPart = false;
        boolean troopsOnTheWayPart = false;
        boolean troopsOutside = false;
        boolean haveTime = false;

        boolean searchChurch = ServerSettings.getSingleton().isChurch();
        int serverTroopCount = DataHolder.getSingleton().getUnits().size();
        FightReport result = new FightReport();
        String winString = null;
        while (t.hasMoreTokens()) {
            String line = t.nextToken().trim();
            logger.debug("Line: " + line);
            
            if (line.startsWith(getVariable("report.fight.time"))) {
                logger.debug("Found send line");
                line = line.replaceAll(getVariable("report.fight.time"), "").trim();
                
                SimpleDateFormat f;
                if (ServerSettings.getSingleton().isMillisArrival()) {
                    f = new SimpleDateFormat(getVariable("report.date.format.ms"));
                } else {
                    f = new SimpleDateFormat(getVariable("report.date.format"));
                }
                try {
                    Date d = f.parse(line);
                    result.setTimestamp(d.getTime());
                    logger.debug("  Sent: " + f.format(new Date(result.getTimestamp())));
                    haveTime = true;
                    continue;
                } catch (Exception e) {
                    result.setTimestamp(0L);
                    logger.debug(" Failed to parse sent");
                }
            }
            
            if (line.contains(getVariable("report.has.won"))) {
                logger.debug("Found 'won' line: " + line);
                winString = line;
                continue;
            }
            
            if (line.contains(getVariable("report.spy"))) {
                logger.debug("Found 'spied' line: " + line);
                winString = line;
                continue;
            }
            
            if (line.startsWith(getVariable("report.att.luck"))) {
                logger.debug("Found luck line");
                luckPart = true;
                continue;
            }
            
            if (line.startsWith(getVariable("report.moral"))) {
                logger.debug("Found moral line");
                line = line.replaceAll(getVariable("report.moral") + ":", "").trim().replaceAll("%", "");
                if (line.contains(getVariable("report.att.player"))) {
                    //Opera only -.-
                    logger.debug(" Special moral handling (Opera)");
                    attackerPart = true;
                    int attackerPos = line.indexOf(getVariable("report.att.player"));
                    String attacker = line.substring(attackerPos).replaceAll(getVariable("report.att.player") + ":", "").trim();
                    logger.debug(" Found attacker: " + attacker);
                    result.setAttacker(DataHolder.getSingleton().getTribeByName(attacker));
                    line = line.substring(0, attackerPos);
                }
                try {
                    double moral = Double.parseDouble(line);
                    logger.debug(" Set moral to " + moral);
                    result.setMoral(moral);
                } catch (Exception ignored) {
                }
                continue;
            }
            
            if (line.contains(getVariable("report.att.player"))) {
                attackerPart = true;
                line = line.replaceAll(getVariable("report.att.player") + ":", "").trim();
                logger.debug("Found attacker in normal mode: " + line);
                result.setAttacker(DataHolder.getSingleton().getTribeByName(line));
                continue;
            }
            
            if (line.startsWith(getVariable("report.village.1")) ||
                    line.startsWith(getVariable("report.village.2")) ||
                    line.startsWith(getVariable("report.village.3"))) {
                line = line.replaceAll(getVariable("report.village.1") + ":", "");
                line = line.replaceAll(getVariable("report.village.2") + ":", "");
                line = line.replaceAll(getVariable("report.village.3") + ":", "").trim();
                logger.debug("Found village: " + line);
                if (attackerPart) {
                    logger.debug(" Use village as source");
                    result.setSourceVillage(VillageParser.parseSingleLine(line));
                } else if (defenderPart) {
                    logger.debug(" Use village as target");
                    result.setTargetVillage(VillageParser.parseSingleLine(line));
                }
                continue;
            }
            
            if (line.startsWith(getVariable("report.num"))) {
                line = line.replaceAll(getVariable("report.num") + ":", "").trim();
                //fix for some browsers using space instead of tab
                while(line.indexOf("  ") != -1)
                    line = line.replaceAll("  ", " ");
                line = line.replaceAll(" ", "\t");
                
                logger.debug("Found amount line: '" + line + "'");
                if (attackerPart) {
                    logger.debug(" Use amount for attacker");
                    String[] troops = line.split("\t");
                    //hack for militia servers (militia is not shown for attacker)
                    if (troops.length == serverTroopCount || troops.length == serverTroopCount - 1) {
                        result.setAttackers(parseUnits(troops));
                    } else {
                        logger.debug(" Line seems NOT valid (" + troops.length + " != " + serverTroopCount + ")");
                    }
                } else if (defenderPart) {
                    logger.debug(" Use amount for defender");
                    String[] troops = line.split("\t");
                    if (troops.length == serverTroopCount) {
                        result.setDefenders(parseUnits(troops));
                    } else {
                        logger.debug(" Line seems NOT valid (" + troops.length + " != " + serverTroopCount + ")");
                    }
                }
                continue;
            }
            
            if (line.startsWith(getVariable("report.loss"))) {
                line = line.replaceAll(getVariable("report.loss") + ":", "").trim();
                //fix for some browsers using space instead of tab
                while(line.indexOf("  ") != -1)
                    line = line.replaceAll("  ", " ");
                line = line.replaceAll(" ", "\t");
                
                logger.debug("Found losses line: '" + line + "'");
                if (attackerPart) {
                    logger.debug(" Use losses for attacker");
                    String[] troops = line.split("\t");
                    if (troops.length == serverTroopCount || troops.length == serverTroopCount - 1) {
                        //hack for militia servers (militia is not shown for attacker)
                        logger.debug(" Line seems valid");
                        result.setDiedAttackers(parseUnits(troops));
                        attackerPart = false;
                    } else {
                        logger.debug(" Line seems NOT valid (" + troops.length + " != " + serverTroopCount + ")");
                    }
                } else if (defenderPart) {
                    logger.debug(" Use losses for defender");
                    String[] troops = line.split("\t");
                    if (troops.length == serverTroopCount) {
                        logger.debug(" Line seems valid");
                        result.setDiedDefenders(parseUnits(troops));
                    } else {
                        logger.debug(" Line seems NOT valid (" + troops.length + " != " + serverTroopCount + ")");
                    }

                }
                continue;
            }
            
            if (line.startsWith(getVariable("report.defender.player"))) {
                defenderPart = true;
                line = line.replaceAll(getVariable("report.defender.player") + ":", "").trim();
                logger.debug("Found defender line: '" + line + "'");
                result.setDefender(DataHolder.getSingleton().getTribeByName(line));
                continue;
            }
            
            if (line.contains(getVariable("report.spy.res"))) {
                logger.debug("Found spyed resources");
                String resources = line.replaceAll(getVariable("report.spy.res") + ":", "").trim();
                String[] spyedResources = resources.split(" ");
                //TODO handle only one or two resources (zero resources aren't displayed
                try {
                    int wood = Integer.parseInt(spyedResources[0].replaceAll("\\.", ""));
                    int clay = Integer.parseInt(spyedResources[1].replaceAll("\\.", ""));
                    int iron = Integer.parseInt(spyedResources[2].replaceAll("\\.", ""));
                    result.setSpyedResources(wood, clay, iron);
                    logger.debug("Successfully set spyed resources to " + wood + "/" + clay + "/" + iron);
                } catch (Exception e) {
                    logger.debug("Failed to set spyed resources from " + resources);
                    //no spyed resources
                }
                continue;
            }
            
            if (line.contains(getVariable("report.haul"))) {
                logger.debug("Found haul");
                String haul = line.replaceAll(getVariable("report.haul") + ":", "").trim();
                String[] hauledResource = haul.split(" ");
                try {
                    int wood = 0;
                    int clay = 0;
                    int iron = 0;
                    try {
                        wood = Integer.parseInt(hauledResource[0].replaceAll("\\.", ""));
                        clay = Integer.parseInt(hauledResource[1].replaceAll("\\.", ""));
                        iron = Integer.parseInt(hauledResource[2].replaceAll("\\.", ""));
                    } catch (NumberFormatException ignored) {
                    }
                    result.setHaul(wood, clay, iron);
                    logger.debug("Successfully set haul to " + wood + "/" + clay + "/" + iron);
                    continue;
                } catch (Exception e) {
                    logger.debug("Failed to set haul from " + haul);
                    //no haul
                }
            }

            for(int i = 0; i < BuildingSettings.BUILDING_NAMES.length; i++) {
                if (line.contains(getVariable("report.buildings." + BuildingSettings.BUILDING_NAMES[i]))) {
                    logger.debug("Parse " + BuildingSettings.BUILDING_NAMES[i]);
                    int val = parseIntFromReportTable(line, getVariable(
                            "report.buildings." + BuildingSettings.BUILDING_NAMES[i]));
                    if (val != -1) {
                        logger.debug("Got " + BuildingSettings.BUILDING_NAMES[i] + " level " + val);
                        result.setBuilding(i, val);
                        break;
                    } else {
                        logger.debug("No valid " + BuildingSettings.BUILDING_NAMES[i] + " level from " + line);
                    }
                }
            }
            
            if (searchChurch && line.contains(getVariable("report.buildings.first.church"))) {
                logger.debug("Try adding first church");
                try {
                    result.setBuilding(BuildingSettings.getBuildingIdByName("church"), 2);
                    continue;
                } catch (Exception e) {
                    logger.debug("Failed to add first church");
                }
            }
            
            if (line.startsWith(getVariable("report.damage.ram"))) {
                line = line.replaceAll(getVariable("report.damage.ram") + ":", "")
                        .replaceAll(getVariable("report.damage.wall"), "")
                        .replaceAll(getVariable("report.damage.to"), "").trim();
                logger.debug("Found wall line");
                StringTokenizer wallT = new StringTokenizer(line, " \t");
                try {
                    result.setWallBefore(Byte.parseByte(wallT.nextToken()));
                    result.setWallAfter(Byte.parseByte(wallT.nextToken()));
                    continue;
                } catch (Exception e) {
                    result.setWallBefore((byte) -1);
                    result.setWallAfter((byte) -1);
                }
            }
            
            if (line.startsWith(getVariable("report.damage.kata"))) {
                //Schaden durch Katapultbeschuss: Wall beschädigt von Level 8 auf Level 7
                line = line.replaceAll(getVariable("report.damage.kata") + ":", "")
                        .replaceAll(getVariable("report.damage.level"), "")
                        .replaceAll(getVariable("report.damage.to"), "").trim();
                logger.debug("Found cata line");
                //Wall beschädigt von 8 auf 7
                StringTokenizer cataT = new StringTokenizer(line, " ");
                String target = cataT.nextToken().trim();
                try {
                    byte buildingBefore = Byte.parseByte(cataT.nextToken());
                    byte buildingAfter = Byte.parseByte(cataT.nextToken());
                    if(getBuildingId(target) != -1) {
                        result.setAimedBuildingId(getBuildingId(target));
                        result.setBuildingBefore(buildingBefore);
                        result.setBuildingAfter(buildingAfter);
                    }
                    continue;
                } catch (Exception ignored) {
                }
            }
            
            if (line.startsWith(getVariable("report.acceptance.1"))) {
                line = line.replaceAll(getVariable("report.acceptance.1") + ":", "").trim()
                        .replaceAll(getVariable("report.acceptance.2"), "")
                        .replaceAll(getVariable("report.acceptance.3"), "");
                logger.debug("Found acceptance line");

                StringTokenizer acceptT = new StringTokenizer(line, " \t");
                try {
                    result.setAcceptanceBefore(Byte.parseByte(acceptT.nextToken()));
                    result.setAcceptanceAfter(Byte.parseByte(acceptT.nextToken()));
                } catch (Exception e) {
                    result.setAcceptanceBefore((byte) 100);
                    result.setAcceptanceAfter((byte) 100);
                }
                continue;
            }
            
            if (line.startsWith(getVariable("report.ontheway")) ||
                    line.startsWith(getVariable("report.ontheway_spy"))) {
                troopsOnTheWayPart = true;
                logger.debug("Found troops on the way line");
                continue;
            }
            
            if (line.startsWith(getVariable("report.outside"))) {
                troopsOutside = true;
                logger.debug("Found troops outside line");
                continue;
            }
            
            if (line.startsWith(getVariable("report.hidden"))) {
                if (attackerPart) {
                    String[] unknownAttackers = new String[serverTroopCount];
                    for (int i = 0; i < serverTroopCount; i++) {
                        unknownAttackers[i] = "-1";
                    }
                    result.setAttackers(parseUnits(unknownAttackers));
                    result.setDiedAttackers(parseUnits(unknownAttackers));
                    attackerPart = false;
                } else {
                    String[] unknownDefender = new String[serverTroopCount];
                    for (int i = 0; i < serverTroopCount; i++) {
                        unknownDefender[i] = "-1";
                    }
                    result.setDefenders(parseUnits(unknownDefender));
                    result.setDiedDefenders(parseUnits(unknownDefender));
                    defenderPart = false;
                }
                continue;
            }
            
            if (line.startsWith(getVariable("report.full.destruction"))) {
                logger.debug("Found full destruction line");
                defenderPart = false;
                String[] unknownDefender = new String[serverTroopCount];
                for (int i = 0; i < serverTroopCount; i++) {
                    unknownDefender[i] = "-1";
                }
                result.setDefenders(parseUnits(unknownDefender));
                result.setDiedDefenders(parseUnits(unknownDefender));
                continue;
            }
            
            if (!haveTime) {
                logger.debug("Parse sent date");
                line = line.trim();
                SimpleDateFormat f = new SimpleDateFormat(getVariable("report.date.format"));
                try {
                    Date d = f.parse(line);
                    result.setTimestamp(d.getTime());
                    logger.debug("  Sent: " + f.format(new Date(result.getTimestamp())));
                    haveTime = true;
                    continue;
                } catch (Exception e) {
                    result.setTimestamp(0L);
                }
            }

            if (troopsOnTheWayPart) {
                //fix for some browsers using space instead of tab
                while(line.indexOf("  ") != -1)
                    line = line.replaceAll("  ", " ");
                line = line.replaceAll(" ", "\t");
                
                logger.debug("Found possible on the way troop line: '" + line + "'");
                String[] troops = line.split("\t");
                if (troops.length == serverTroopCount || troops.length == serverTroopCount - 1) {
                    troopsOnTheWayPart = false;
                    result.setDefendersOnTheWay(parseUnits(troops));
                }
            }
            
            if (troopsOutside) {
                try {
                    Village v = VillageParser.parseSingleLine(line);
                    if (v == null) {
                        throw new Exception();
                    }
                    line = line.substring(line.indexOf("\t")).trim();
                    String[] troops = line.split("\t");
                    if (troops.length == serverTroopCount) {
                        result.addDefendersOutside(v, parseUnits(troops));
                    }
                } catch (Exception e) {
                    //no additional troops outside
                    troopsOutside = false;
                }
            }
            
            if (luckPart) {
                if (line.indexOf("%") > 0) {
                    luckPart = false;
                    try {
                        // System.out.println("LuckLine " + line);
                        double luck = Double.parseDouble(line
                                .replaceAll(getVariable("report.badluck"), "")
                                .replaceAll(getVariable("report.luck"), "")
                                .replaceAll("%", "").trim());
                        //System.out.println("L " + luck);
                        result.setLuck(luck);
                    } catch (Exception e) {
                        result.setLuck(0.0);
                    }
                }
            }
        }
        Tribe attacker = result.getAttacker();
        Tribe defender = result.getDefender();
        if (winString == null) {
            logger.debug("No winString found. Guessing winner based on surviving Defenders");
            result.setWon(result.getSurvivingDefenders().getTroopSum() == 0);
        } else {
            if (attacker != null && defender != null) {
                if (winString.contains(getVariable("report.win.win"))) {
                    result.setWon(winString.contains(attacker.getName()));
                    logger.debug("'won' set to " + result.isWon() + " due to winString " + winString + " and attacker " + attacker.getName());
                } else if (winString.contains(getVariable("report.spy"))) {
                    result.setWon(winString.indexOf(attacker.getName()) < winString.indexOf(defender.getName()));
                    logger.debug("'won' set to " + result.isWon() + " due to winString " + winString + " and attacker " + attacker.getName());
                }
            } else {
                logger.debug("Either attacker or defender is null. Leaving isWon as it is.");
            }
        }
        return result;
    }

    private int getBuildingId(String translatedBuilding) {
        for(int i = 0; i < BuildingSettings.BUILDING_NAMES.length; i++) {
            if(translatedBuilding.equals(getVariable("report.buildings." + BuildingSettings.BUILDING_NAMES[i])))
                return i;
        }
        logger.error("Could not find Building " + translatedBuilding);
        return -1;
    }

    private TroopAmountFixed parseUnits(String[] pUnits) {
        TroopAmountFixed units = new TroopAmountFixed(0);
        List<UnitHolder> allUnits = DataHolder.getSingleton().getUnits();
        for (int i = 0; i < pUnits.length; i++) {
                units.setAmountForUnit(allUnits.get(i), Integer.parseInt(pUnits[i]));
        }
        
        return units;
    }

    public int parseIntFromReportTable(String pLine, String pName) {
        try {
            String val = pLine.trim().replaceAll(pName + "(.*?)(\\d+)", "$2").trim();
            return Integer.parseInt(val);
        } catch (Exception e) {
            return -1;
        }
    }
    
    private String getVariable(String pProperty) {
        return ParserVariableManager.getSingleton().getProperty(pProperty);
    }
}
