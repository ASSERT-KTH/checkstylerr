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
package de.tor.tribes.types;

import de.tor.tribes.control.ManageableType;
import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.TroopAmountFixed;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.php.LuckViewInterface;
import de.tor.tribes.php.UnitTableInterface;
import de.tor.tribes.types.ext.Barbarians;
import de.tor.tribes.types.ext.InvalidTribe;
import de.tor.tribes.types.ext.Tribe;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.util.BBSupport;
import de.tor.tribes.util.BuildingSettings;
import de.tor.tribes.util.xml.JDomUtils;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;

/**
 *
 * @author Torridity
 */
public class FightReport extends ManageableType implements Comparable<FightReport>, BBSupport {

    private final static String[] VARIABLES = new String[]{"%ATTACKER%", "%SOURCE%", "%DEFENDER%", "%TARGET%", "%SEND_TIME%", "%RESULT%", "%LUCK%", "%MORALE%", "%ATTACKER_TROOPS%",
        "%DEFENDER_TROOPS%", "%DEFENDERS_OUTSIDE%", "%DEFENDERS_EN_ROUTE%", "%LOYALITY_CHANGE%", "%WALL_CHANGE%", "%BUILDING_CHANGE%"};
    private final static String STANDARD_TEMPLATE = "[quote][i][b]Betreff:[/b][/i] %ATTACKER% greift %TARGET% an\n[i][b]Gesendet:[/b][/i] %SEND_TIME%\n[size=16]%RESULT%[/size]\n"
            + "[b]Glück:[/b] %LUCK%\n[b]Moral:[/b] %MORALE%\n\n[b]Angreifer:[/b] %ATTACKER%\n[b]Dorf:[/b] %SOURCE%\n%ATTACKER_TROOPS%\n\n[b]Verteidiger:[/b] %DEFENDER%\n"
            + "[b]Dorf:[/b] %TARGET%\n %DEFENDER_TROOPS%\n\n%DEFENDERS_OUTSIDE%\n%DEFENDERS_EN_ROUTE%\n%LOYALITY_CHANGE%\n%WALL_CHANGE%\n%BUILDING_CHANGE%[/quote]";

    @Override
    public String[] getBBVariables() {
        return VARIABLES;
    }

    @Override
    public String[] getReplacements(boolean pExtended) {
        String attackerVal = attacker.toBBCode();
        String targetVal = targetVillage.toBBCode();
        SimpleDateFormat d = new SimpleDateFormat("dd.MM.yy HH:mm:ss");
        String sendDateVal = d.format(new Date(timestamp));
        String resultVal = (won) ? "Der Angreifer hat gewonnen" : "Der Verteidiger hat gewonnen";

        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(1);
        nf.setMinimumFractionDigits(1);

        String luckVal = "[img]" + LuckViewInterface.createLuckIndicator(luck) + "[/img] " + nf.format(luck) + "%";
        nf.setMinimumFractionDigits(0);
        nf.setMinimumFractionDigits(0);
        String moraleVal = nf.format(moral) + " %";

        String sourceVal = sourceVillage.toBBCode();
        String attackerTroopsVal = (areAttackersHidden())
                ? "Durch Besitzer des Berichts verborgen"
                : "[img]" + UnitTableInterface.createAttackerUnitTableLink(attackers, diedAttackers) + "[/img]";

        String defenderVal = defender.toBBCode();
        String defenderTroopsVal = (wasLostEverything())
                ? "Keiner deiner Kämpfer ist lebend zurückgekehrt.\nEs konnten keine Informationen über die Truppenstärke des Gegners erlangt werden."
                : "[img]" + UnitTableInterface.createDefenderUnitTableLink(defenders, diedDefenders) + "[/img]";

        String troopsEnRouteVal = (whereDefendersOnTheWay())
                ? "[b]Truppen des Verteidigers, die unterwegs waren[/b]\n\n" + "[img]" + UnitTableInterface.createAttackerUnitTableLink(defendersOnTheWay) + "[/img]"
                : "";
        String troopsOutsideVal = "";
        if (whereDefendersOutside()) {
            Set<Village> targetKeys = defendersOutside.keySet();
            for (Village target: targetKeys) {
                troopsOutsideVal += target.toBBCode() + "\n\n";
                troopsOutsideVal += "[img]" + UnitTableInterface.createAttackerUnitTableLink(defendersOutside.get(target)) + "[/img]\n\n";
            }
        }

        String loyalityChangeVal = (wasSnobAttack())
                ? "[b]Veränderung der Zustimmung:[/b] Zustimmung gesunken von " + nf.format(getAcceptanceBefore()) + " auf " + getAcceptanceAfter()
                : "";

        String wallChangeVal = (wasWallDamaged())
                ? "[b]Schaden durch Rammen:[/b] Wall beschädigt von Level " + getWallBefore() + " auf Level " + getWallAfter()
                : "";
        //TODO use building names from Translation file
        String cataChangeVal = (wasBuildingDamaged())
                ? "[b]Schaden durch Katapultbeschuss:[/b] " + BuildingSettings.BUILDING_NAMES[aimedBuildingId] + " beschädigt von Level " + getBuildingBefore() + " auf Level " + getBuildingAfter()
                : "";
        return new String[]{attackerVal, sourceVal, defenderVal, targetVal, sendDateVal, resultVal, luckVal, moraleVal, attackerTroopsVal, defenderTroopsVal, troopsOutsideVal, troopsEnRouteVal, loyalityChangeVal, wallChangeVal, cataChangeVal};
    }

    @Override
    public String getStandardTemplate() {
        return STANDARD_TEMPLATE;
    }
    
    public enum status {
        LOST_NOTHING,
        WON_WITH_LOSSES,
        LOST_EVERYTHING,
        SPY,
        HIDDEN
    }
    
    private Logger logger = LogManager.getLogger("FightReport");
    
    private boolean won = false;
    private long timestamp = 0;
    private double luck = 0.0;
    private double moral = 100.0;
    private Tribe attacker = null;
    private Village sourceVillage = null;
    private TroopAmountFixed attackers = null;
    private TroopAmountFixed diedAttackers = null;
    private Tribe defender = null;
    private Village targetVillage = null;
    private TroopAmountFixed defenders = null;
    private TroopAmountFixed diedDefenders = null;
    private HashMap<Village, TroopAmountFixed> defendersOutside = null;
    private TroopAmountFixed defendersOnTheWay = null;
    private int wallBefore = -1;
    private int wallAfter = -1;
    private int aimedBuildingId = -1;
    private int buildingBefore = -1;
    private int buildingAfter = -1;
    private int acceptanceBefore = 100;
    private int acceptanceAfter = 100;
    private int[] spyedResources = null;
    private int[] haul = null;
    private int[] buildingLevels = null;
    
    public final int SPY_LEVEL_NONE = 0;
    public final int SPY_LEVEL_RESOURCES = 1;
    public final int SPY_LEVEL_BUILDINGS = 2;
    public final int SPY_LEVEL_OUTSIDE = 3;
    private int spyLevel = SPY_LEVEL_NONE;

    public FightReport() {
        attackers = new TroopAmountFixed();
        diedAttackers = new TroopAmountFixed();
        defenders = new TroopAmountFixed();
        diedDefenders = new TroopAmountFixed();
        defendersOutside = new HashMap<>();
        defendersOnTheWay = new TroopAmountFixed();
        
        buildingLevels = new int[BuildingSettings.BUILDING_NAMES.length];
        Arrays.fill(buildingLevels, -1);
    }

    public static String toInternalRepresentation(FightReport pReport) {
        return JDomUtils.toShortString(pReport.toXml("report"));
    }

    public static FightReport fromInternalRepresentation(String pLine) {
        FightReport r = new FightReport();
        try {
            Document d = JDomUtils.getDocument(pLine);
            r.loadFromXml((Element) JDomUtils.getNodes(d, "report").get(0));
            return r;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void loadFromXml(Element pElement) {
        try {
            this.timestamp = Long.parseLong(pElement.getChildText("timestamp"));
            this.moral = Double.parseDouble(pElement.getChildText("moral"));
            this.luck = Double.parseDouble(pElement.getChildText("luck"));
            //attacker stuff
            Element attackerElement = pElement.getChild("attacker");
            Element defenderElement = pElement.getChild("defender");
            int source = Integer.parseInt(attackerElement.getChildText("src"));
            this.sourceVillage = DataHolder.getSingleton().getVillagesById().get(source);

            int attackerId = Integer.parseInt(attackerElement.getChildText("id"));
            Tribe attElement = DataHolder.getSingleton().getTribes().get(attackerId);
            if (attElement != null) {
                setAttacker(attElement);
            } else {
                if (attackerId != -666 && this.sourceVillage != null && this.sourceVillage.getTribe() != null) {
                    setAttacker(this.sourceVillage.getTribe());
                } else {
                    setAttacker(InvalidTribe.getSingleton());
                }
            }

            int target = Integer.parseInt(defenderElement.getChildText("trg"));
            this.targetVillage = DataHolder.getSingleton().getVillagesById().get(target);
            int defenderId = Integer.parseInt(defenderElement.getChildText("id"));
            Tribe defendingTribe = DataHolder.getSingleton().getTribes().get(defenderId);
            if (defendingTribe != null) {
                setDefender(defendingTribe);
            } else {
                if (defenderId > 0 && this.targetVillage != null && this.targetVillage.getTribe() != null) {
                    setDefender(this.targetVillage.getTribe());
                } else {
                    if (defenderId == -666) {
                        setDefender(InvalidTribe.getSingleton());
                    } else {
                        setDefender(Barbarians.getSingleton());
                    }
                }
            }

            this.attackers = new TroopAmountFixed(attackerElement.getChild("before"));
            this.diedAttackers = new TroopAmountFixed(attackerElement.getChild("died"));
            this.defenders = new TroopAmountFixed(defenderElement.getChild("before"));
            this.diedDefenders = new TroopAmountFixed(defenderElement.getChild("died"));
            try {
                this.defendersOnTheWay = new TroopAmountFixed(defenderElement.getChild("otw"));
            } catch (Exception ignored) {
            }

            Element dDefendersOutside = null;
            try {
                dDefendersOutside = defenderElement.getChild("outside");
            } catch (Exception ignored) {
            }

            this.defendersOutside = new HashMap<>();
            if (dDefendersOutside != null) {
                for (Element e : (List<Element>) JDomUtils.getNodes(dDefendersOutside, "support")) {
                    try {
                        int villageId = e.getAttribute("id").getIntValue();
                        Village v = DataHolder.getSingleton().getVillagesById().get(villageId);
                        if(v != null) {
                            TroopAmountFixed unitsInvillage = this.defendersOutside.get(v);
                            if (unitsInvillage == null) {
                                unitsInvillage = new TroopAmountFixed(e);
                            } else {
                                unitsInvillage.addAmount(new TroopAmountFixed(e));
                            }
                            this.defendersOutside.put(v, unitsInvillage);
                        }
                    } catch(Exception ex) {
                        logger.debug("cannot read defenders outside", ex);
                    }
                }
            }

            try {
                Element e = pElement.getChild("wall");
                if(e != null) {
                    this.wallBefore = Byte.parseByte(e.getAttributeValue("before"));
                    this.wallAfter = Byte.parseByte(e.getAttributeValue("after"));
                }
            } catch (Exception e) {
                this.wallBefore = -1;
                this.wallAfter = -1;
            }
            try {
                Element e = pElement.getChild("building");
                if(e != null) {
                    this.aimedBuildingId =  Byte.parseByte(e.getAttributeValue("target"));
                    this.buildingBefore = Byte.parseByte(e.getAttributeValue("before"));
                    this.buildingAfter = Byte.parseByte(e.getAttributeValue("after"));
                }
            } catch (Exception e) {
                this.buildingBefore = -1;
                this.buildingAfter = -1;
                logger.debug("cannot read building damage", e);
            }
            try {
                Element e = pElement.getChild("acceptance");
                if(e != null) {
                    this.acceptanceBefore = Byte.parseByte(e.getAttributeValue("before"));
                    this.acceptanceAfter = Byte.parseByte(e.getAttributeValue("after"));
                }
            } catch (Exception e) {
                this.acceptanceBefore = 100;
                this.acceptanceAfter = 100;
                logger.debug("cannot read acceptance", e);
            }
            try {
                Element e = pElement.getChild("spyBuildings");
                
                for(int i = 0; i < this.buildingLevels.length; i++) {
                    this.buildingLevels[i] = Integer.parseInt(e.getAttributeValue(
                            BuildingSettings.BUILDING_NAMES[i]));
                }
            } catch (Exception e) {
                logger.debug("Failed to read buildings", e);
            }
            try {
                this.spyLevel = Integer.parseInt(pElement.getChildText("spyLevel"));
            } catch (Exception e) {
                logger.debug("Failed to read spy Level", e);
            }
            
            try {
                Element haulElm = pElement.getChild("haul");
                if(haulElm != null) {
                    this.haul = new int[3];
                    this.haul[0] = Integer.parseInt(haulElm.getAttributeValue("wood"));
                    this.haul[1] = Integer.parseInt(haulElm.getAttributeValue("clay"));
                    this.haul[2] = Integer.parseInt(haulElm.getAttributeValue("iron"));
                }
            } catch (Exception e) {
                logger.debug("Failed to read haul information", e);
                this.haul = null;
            }
            
            try {
                Element spyElm = pElement.getChild("spy");
                if(spyElm != null) {
                    this.spyedResources = new int[3];
                    this.spyedResources[0] = Integer.parseInt(spyElm.getAttributeValue("wood"));
                    this.spyedResources[1] = Integer.parseInt(spyElm.getAttributeValue("clay"));
                    this.spyedResources[2] = Integer.parseInt(spyElm.getAttributeValue("iron"));
                }
            } catch (Exception e) {
                logger.debug("Failed to read spyed resources", e);
                this.spyedResources = null;
            }
            
            try {
                this.won = Boolean.parseBoolean(pElement.getChildText("won"));
            } catch (Exception e) {
                logger.debug("Failed to read won Level", e);
            }
        } catch (Exception e) {
            logger.warn("failed to fully read the report", e);
        }
    }

    @Override
    public Element toXml(String elementName) {
        Element report = new Element(elementName);
        
        try {
            //general part
            report.addContent(new Element("timestamp").setText(Long.toString(timestamp)));
            report.addContent(new Element("moral").setText(Double.toString(moral)));
            report.addContent(new Element("luck").setText(Double.toString(luck)));
            
            //attacker part
            Element attackerE = new Element("attacker");
            attackerE.addContent(new Element("id").setText(Integer.toString(attacker.getId())));
            attackerE.addContent(new Element("src").setText(Integer.toString(sourceVillage.getId())));
            attackerE.addContent(attackers.toXml("before"));
            attackerE.addContent(diedAttackers.toXml("died"));
            report.addContent(attackerE);

            //defender part
            Element defenderE = new Element("defender");
            defenderE.addContent(new Element("id").setText(Integer.toString(defender.getId())));
            defenderE.addContent(new Element("trg").setText(Integer.toString(targetVillage.getId())));
            defenderE.addContent(defenders.toXml("before"));
            defenderE.addContent(diedDefenders.toXml("died"));
            
            if (whereDefendersOnTheWay()) {
                defenderE.addContent(defendersOnTheWay.toXml("otw"));
            }
            if (whereDefendersOutside()) {
                Element outsideE = new Element("outside");
                for (Village target: defendersOutside.keySet()) {
                    Element defOutside = defendersOutside.get(target).toXml("support");
                    defOutside.setAttribute("id", Integer.toString(target.getId()));
                    outsideE.addContent(defOutside);
                }
                defenderE.addContent(outsideE);
            }
            report.addContent(defenderE);
            
            if (wasWallDamaged()) {
                Element wall = new Element("wall");
                wall.setAttribute("before", Integer.toString(getWallBefore()));
                wall.setAttribute("after", Integer.toString(getWallAfter()));
                report.addContent(wall);
            }
            if (wasBuildingDamaged()) {
                Element building = new Element("building");
                building.setAttribute("target", Integer.toString(aimedBuildingId));
                building.setAttribute("before", Integer.toString(getBuildingBefore()));
                building.setAttribute("after", Integer.toString(getBuildingAfter()));
                report.addContent(building);
            }
            if (wasSnobAttack()) {
                Element building = new Element("acceptance");
                building.setAttribute("before", Integer.toString(getAcceptanceBefore()));
                building.setAttribute("after", Integer.toString(getAcceptanceAfter()));
                report.addContent(building);
            }

            if (haul != null) {
                Element haulE = new Element("haul");
                haulE.setAttribute("wood", Integer.toString(haul[0]));
                haulE.setAttribute("clay", Integer.toString(haul[1]));
                haulE.setAttribute("iron", Integer.toString(haul[2]));
                report.addContent(haulE);
            }

            if (spyedResources != null) {
                Element spy = new Element("spy");
                spy.setAttribute("wood", Integer.toString(spyedResources[0]));
                spy.setAttribute("clay", Integer.toString(spyedResources[1]));
                spy.setAttribute("iron", Integer.toString(spyedResources[2]));
                report.addContent(spy);
            }

            Element spyBuildings = new Element("spyBuildings");
            for(int i = 0; i < buildingLevels.length; i++) {
                spyBuildings.setAttribute(BuildingSettings.BUILDING_NAMES[i], Integer.toString(buildingLevels[i]));
            }
            report.addContent(spyBuildings);
            
            report.addContent(new Element("spyLevel").setText(Integer.toString(spyLevel)));
            report.addContent(new Element("won").setText(Boolean.toString(won)));
            return report;
        } catch (Exception e) {
            logger.error("Exception during generating XML", e);
            return null;
        }
    }

    /**
     * @return the attacker
     */
    public Tribe getAttacker() {
        return attacker;
    }

    /**
     * @param attacker the attacker to set
     */
    public void setAttacker(Tribe attacker) {
        if (attacker == null) {
            this.attacker = Barbarians.getSingleton();
        } else {
            this.attacker = attacker;
        }
    }

    /**
     * @return the sourceVillage
     */
    public Village getSourceVillage() {
        return sourceVillage;
    }

    /**
     * @param sourceVillage the sourceVillage to set
     */
    public void setSourceVillage(Village sourceVillage) {
        this.sourceVillage = sourceVillage;
    }

    /**
     * @return the attackers
     */
    public TroopAmountFixed getAttackers() {
        return attackers;
    }

    /**
     * @param attackers the attackers to set
     */
    public void setAttackers(TroopAmountFixed attackers) {
        this.attackers = attackers;
    }

    /**
     * @return the diedAttackers
     */
    public TroopAmountFixed getDiedAttackers() {
        return diedAttackers;
    }

    /**
     * @param diedAttackers the diedAttackers to set
     */
    public void setDiedAttackers(TroopAmountFixed diedAttackers) {
        this.diedAttackers = diedAttackers;
    }

    public TroopAmountFixed getSurvivingAttackers() {
        TroopAmountFixed result = null;
        if (!areAttackersHidden() && attackers != null && diedAttackers != null) {
            result = (TroopAmountFixed) attackers.clone();
            result.removeAmount(diedAttackers);
        }
        return result;
    }

    /**
     * @return the defender
     */
    public Tribe getDefender() {
        return defender;
    }

    /**
     * @param defender the defender to set
     */
    public void setDefender(Tribe defender) {
        if (defender == null) {
            this.defender = Barbarians.getSingleton();
        } else {
            this.defender = defender;
        }
    }

    /**
     * @return the targetVillage
     */
    public Village getTargetVillage() {
        return targetVillage;
    }

    /**
     * @param targetVillage the targetVillage to set
     */
    public void setTargetVillage(Village targetVillage) {
        this.targetVillage = targetVillage;
    }

    public void setSpyedResources(int pWood, int pClay, int pIron) {
        spyedResources = new int[]{pWood, pClay, pIron};
    }

    public int[] getSpyedResources() {
        return spyedResources;
    }

    public void setHaul(int pWood, int pClay, int pIron) {
        haul = new int[]{pWood, pClay, pIron};
    }

    public int[] getHaul() {
        return haul;
    }

    /**
     * @return the defenders
     */
    public TroopAmountFixed getDefenders() {
        return defenders;
    }

    /**
     * @param defenders the defenders to set
     */
    public void setDefenders(TroopAmountFixed defenders) {
        this.defenders = defenders;
    }

    /**
     * @return the diedDefenders
     */
    public TroopAmountFixed getDiedDefenders() {
        return diedDefenders;
    }

    public TroopAmountFixed getSurvivingDefenders() {
        TroopAmountFixed result = null;
        if (!wasLostEverything() && defenders != null && diedDefenders != null) {
            result = (TroopAmountFixed) defenders.clone();
            result.removeAmount(diedDefenders);
        }
        return result;
    }

    public boolean hasSurvivedDefenders() {
        return (getSurvivingDefenders().getTroopPopCount() != 0);
    }

    /**
     * @param diedDefenders the diedDefenders to set
     */
    public void setDiedDefenders(TroopAmountFixed diedDefenders) {
        this.diedDefenders = diedDefenders;
    }

    public void addDefendersOutside(Village pVillage, TroopAmountFixed pDefenders) {
        defendersOutside.put(pVillage, pDefenders);
    }

    public boolean wasLostEverything() {
        //defenders are set to -1 if no information on them could be achieved as result of a total loss
        return !defenders.containsInformation();
    }

    public boolean isSimpleSnobAttack() {
        if (!wasSnobAttack()) {
            //acceptance reduced, must be snob
            return false;
        }
        return (attackers.getTroopSum() < 1000);
    }

    //@TODO configurable guess
    public int guessType() {
        if (wasSnobAttack() || isSimpleSnobAttack()) {
            //acceptance reduced, must be snob
            return Attack.SNOB_TYPE;
        }

        if (areAttackersHidden()) {
            //attackers hidden, no info possible
            return Attack.NO_TYPE;
        }

        boolean isSnobAttack = false;
        int attackerCount = 0;
        int spyCount = 0;
        if (attackers != null) {
            attackerCount = attackers.getTroopSum();
            if (attackers.getAmountForUnit("snob") >= 1) {
                isSnobAttack = true;
            }
            if (attackers.getAmountForUnit("spy") >= 1) {
                spyCount = attackers.getAmountForUnit("spy");
            }
        }
        if (isSnobAttack) {
            //snob joined attack but no acceptance was reduces
            return Attack.SNOB_TYPE;
        }

        double spyPerc = 100.0 * (double) spyCount / (double) attackerCount;

        if (spyPerc > 50.0) {
            //only spies joined the attack
            return Attack.SPY_TYPE;
        }

        if (attackerCount < 500) {
            return Attack.FAKE_TYPE;
        }

        return Attack.CLEAN_TYPE;
    }

    public boolean wasLostNothing() {
        if (areAttackersHidden()) {
            return false;
        }
        return diedAttackers.getTroopSum() == 0;
    }

    public boolean areAttackersHidden() {
        return !attackers.containsInformation();
    }

    public boolean whereDefendersOnTheWay() {
        return (defendersOnTheWay != null && defendersOnTheWay.getTroopSum() != 0);
    }

    public boolean whereDefendersOutside() {
        return (defendersOutside != null && !defendersOutside.isEmpty());
    }

    /**
     * @return the defendersOutside
     */
    public TroopAmountFixed getDefendersOnTheWay() {
        return defendersOnTheWay;
    }

    /**
     * @return the defendersOutside
     */
    public HashMap<Village, TroopAmountFixed> getDefendersOutside() {
        return defendersOutside;
    }

    /**
     * @param defendersOnTheWay the defendersOnTheWay to set
     */
    public void setDefendersOnTheWay(TroopAmountFixed defendersOnTheWay) {
        this.defendersOnTheWay = defendersOnTheWay;
    }

    /**
     * @return the wallBefore
     */
    public int getWallBefore() {
        return wallBefore;
    }

    /**
     * @param wallBefore the wallBefore to set
     */
    public void setWallBefore(int wallBefore) {
        this.wallBefore = wallBefore;
    }

    /**
     * @return the wallAfter
     */
    public int getWallAfter() {
        return wallAfter;
    }

    /**
     * @param wallAfter the wallAfter to set
     */
    public void setWallAfter(int wallAfter) {
        this.wallAfter = wallAfter;
    }

    /**
     * @return the aimedBuilding
     */
    public int getAimedBuildingId() {
        return aimedBuildingId;
    }

    /**
     * @param aimedBuilding the aimedBuilding to set
     */
    public void setAimedBuildingId(int pAimedBuildingId) {
        this.aimedBuildingId = pAimedBuildingId;
    }

    /**
     * @return the buildingBefore
     */
    public int getBuildingBefore() {
        return buildingBefore;
    }

    /**
     * @param buildingBefore the buildingBefore to set
     */
    public void setBuildingBefore(int buildingBefore) {
        this.buildingBefore = buildingBefore;
    }

    /**
     * @return the buildingAfter
     */
    public int getBuildingAfter() {
        return buildingAfter;
    }

    /**
     * @param buildingAfter the buildingAfter to set
     */
    public void setBuildingAfter(int buildingAfter) {
        this.buildingAfter = buildingAfter;
    }

    /**
     * @return the acceptanceBefore
     */
    public int getAcceptanceBefore() {
        return acceptanceBefore;
    }

    /**
     * @param acceptanceBefore the acceptanceBefore to set
     */
    public void setAcceptanceBefore(int acceptanceBefore) {
        this.acceptanceBefore = acceptanceBefore;
    }

    /**
     * @return the acceptanceAfter
     */
    public int getAcceptanceAfter() {
        return acceptanceAfter;
    }

    /**
     * @param acceptanceAfter the acceptanceAfter to set
     */
    public void setAcceptanceAfter(int acceptanceAfter) {
        this.acceptanceAfter = acceptanceAfter;
    }

    public boolean wasWallDamaged() {
        return (getWallBefore() > 0);
    }

    public boolean wasBuildingDamaged() {
        return (getBuildingBefore() > 0);
    }

    public boolean isSpyReport() {
        if (wasLostEverything()) {
            return false;
        }
        boolean spySurvived = false;
        TroopAmountFixed survivingAtt = getSurvivingAttackers();
        for (UnitHolder unit: DataHolder.getSingleton().getUnits()) {
            if (unit.getPlainName().equals("spy")) {
                if (survivingAtt.getAmountForUnit(unit) > 0) {
                    spySurvived = true;
                }
            } else {
                if (survivingAtt == null || survivingAtt.getAmountForUnit(unit) > 0) {
                    //something else survived too or the attackers are hidden
                    return false;
                }
            }
        }
        return spySurvived;
    }

    public int getDestroyedWallLevels() {
        if (wasWallDamaged()) {
            return getWallBefore() - getWallAfter();
        }
        return 0;
    }

    public int getDestroyedBuildingLevels() {
        if (wasBuildingDamaged()) {
            return getBuildingBefore() - getBuildingAfter();
        }
        return 0;
    }

    public boolean wasSnobAttack() {
        return getAcceptanceAfter() < getAcceptanceBefore();
    }

    public boolean wasConquered() {
        return (getAcceptanceAfter() <= 0);
    }

    /**
     * @return the won
     */
    public boolean isWon() {
        return won;
    }

    /**
     * @param won the won to set
     */
    public void setWon(boolean won) {
        this.won = won;
    }

    /**
     * @return the luck
     */
    public double getLuck() {
        return luck;
    }

    /**
     * @param luck the luck to set
     */
    public void setLuck(double luck) {
        this.luck = luck;
    }

    /**
     * @return the moral
     */
    public double getMoral() {
        return moral;
    }

    /**
     * @param moral the moral to set
     */
    public void setMoral(double moral) {
        this.moral = moral;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean isValid() {
        return (attacker != null
                && sourceVillage != null
                && attackers != null
                && diedAttackers != null
                && defender != null
                && targetVillage != null
                && defenders != null
                && diedDefenders != null);
    }

    public int getVillageEffects() {
        int effect = 0;
        if (wasWallDamaged()) {
            effect += 1;
        }
        if (wasBuildingDamaged()) {
            effect += 2;
        }
        if (wasConquered()) {
            effect += 4;
        }
        return effect;
    }

    public Integer getComparableValue() {
        if (areAttackersHidden()) {
            //grey report
            return 4;
        } else if (isSpyReport()) {
            //blue report
            return 2;
        } else if (wasLostEverything()) {
            //red report
            return 3;
        } else if (wasLostNothing()) {
            //green report
            return 0;
        } else {
            //yellow report
            return 1;
        }
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        SimpleDateFormat f = new SimpleDateFormat("dd.MM.yy HH:mm");
        result.append("Gesendet: ").append(f.format(new Date(timestamp))).append("\n");
        result.append(won ? "Gewonnen\n" : "Verloren\n");

        if (isSpyReport()) {
            result.append("Farbe: Blau\n");
        } else if (wasLostEverything()) {
            result.append("Farbe: Rot\n");
        } else if (wasLostNothing()) {
            result.append("Farbe: Grün\n");
        } else {
            result.append("Farbe: Gelb\n");
        }
        result.append("Moral: ").append(moral).append("\n");
        result.append("Glück: ").append(luck).append("\n");
        result.append("Angreifer: ").append(attacker).append("\n");
        result.append("Herkunft: ").append(sourceVillage).append("\n");
        String sAttackers = "";
        String sAttackersDied = "";
        String sDefenders = "";
        String sDefendersDied = "";

        if (areAttackersHidden()) {
            sAttackers = "Verborgen\n";
            sAttackersDied = "Verborgen\n";
        } else {
            for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
                sAttackers += attackers.getAmountForUnit(unit) + " ";
                sAttackersDied += diedAttackers.getAmountForUnit(unit) + " ";
            }
            sAttackers = sAttackers.trim() + "\n";
            sAttackersDied = sAttackersDied.trim() + "\n";
        }

        if (wasLostEverything()) {
            sDefenders = "Keine Informationen\n";
            sDefendersDied = "Keine Informationen\n";
        } else {
            for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
                sDefenders += defenders.getAmountForUnit(unit) + " ";
                sDefendersDied += diedDefenders.getAmountForUnit(unit) + " ";
            }
            sDefenders = sDefenders.trim() + "\n";
            sDefendersDied = sDefendersDied.trim() + "\n";
        }

        result.append("Anzahl: ").append(sAttackers);
        result.append("Verluste: ").append(sAttackersDied);
        result.append("Verteidiger: ").append(defender).append("\n");
        result.append("Ziel: ").append(targetVillage).append("\n");
        result.append("Anzahl: ").append(sDefenders);
        result.append("Verluste: ").append(sDefendersDied);

        if (wasConquered()) {
            if (whereDefendersOutside()) {
                Set<Village> villageKeys = defendersOutside.keySet();
                for (Village v: villageKeys) {
                    if (v != null) {
                        TroopAmountFixed troops = defendersOutside.get(v);
                        if (troops != null) {
                            result.append(" -> ").append(v).append(" ");
                            for (UnitHolder u : DataHolder.getSingleton().getUnits()) {
                                result.append(troops.getAmountForUnit(u)).append(" ");
                            }
                        }
                        result.append("\n");
                    }
                }
            }
        }


        if (wasWallDamaged()) {
            result.append("Wall zerstört von Stufe ").append(getWallBefore()).append(" auf ").append(getWallAfter()).append("\n");
        }
        if (wasBuildingDamaged()) {
            result.append(BuildingSettings.BUILDING_NAMES[aimedBuildingId]).append(" zerstört von Stufe ").append(getBuildingBefore()).append(" auf ").append(getBuildingAfter()).append("\n");
        }
        if (wasSnobAttack()) {
            result.append("Zustimmung gesenkt von ").append(getAcceptanceBefore()).append(" auf ").append(getAcceptanceAfter()).append("\n");
        }
        return result.toString();
    }

    @Override
    public int compareTo(FightReport o) {
        return getComparableValue().compareTo(o.getComparableValue());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof FightReport)) {
            return false;
        }

        FightReport theOther = (FightReport) obj;
        return hashCode() == theOther.hashCode();
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 53 * hash + (this.won ? 1 : 0);
        hash = 53 * hash + (int) (this.timestamp ^ (this.timestamp >>> 32));
        hash = 53 * hash + (int) (Double.doubleToLongBits(this.luck) ^ (Double.doubleToLongBits(this.luck) >>> 32));
        hash = 53 * hash + (int) (Double.doubleToLongBits(this.moral) ^ (Double.doubleToLongBits(this.moral) >>> 32));
        hash = 53 * hash + (this.attacker != null ? this.attacker.hashCode() : 0);
        hash = 53 * hash + (this.sourceVillage != null ? this.sourceVillage.hashCode() : 0);
        hash = 53 * hash + (this.attackers != null ? this.attackers.hashCode() : 0);
        hash = 53 * hash + (this.diedAttackers != null ? this.diedAttackers.hashCode() : 0);
        hash = 53 * hash + (this.defender != null ? this.defender.hashCode() : 0);
        hash = 53 * hash + (this.targetVillage != null ? this.targetVillage.hashCode() : 0);
        hash = 53 * hash + (this.defenders != null ? this.defenders.hashCode() : 0);
        hash = 53 * hash + (this.diedDefenders != null ? this.diedDefenders.hashCode() : 0);
        hash = 53 * hash + (this.defendersOutside != null ? this.defendersOutside.hashCode() : 0);
        hash = 53 * hash + (this.defendersOnTheWay != null ? this.defendersOnTheWay.hashCode() : 0);
        hash = 53 * hash + this.wallBefore;
        hash = 53 * hash + this.wallAfter;
        hash = 53 * hash + this.aimedBuildingId;
        hash = 53 * hash + this.buildingBefore;
        hash = 53 * hash + this.buildingAfter;
        hash = 53 * hash + this.acceptanceBefore;
        hash = 53 * hash + this.acceptanceAfter;
        hash = 53 * hash + Arrays.hashCode(this.spyedResources);
        hash = 53 * hash + Arrays.hashCode(this.haul);
        for(int i = 0; i < this.buildingLevels.length; i++)
            hash = 53 * hash + this.buildingLevels[i];
        return hash;
    }
    
    /*
        This method fills buildings that had not been spyed with zero,
        because buildings with level 0 are not shown by DS
    */
    public void fillMissingSpyInformation() {
        logger.debug(JDomUtils.toShortString(toXml("report")));
        if (spyedResources != null) {
            if(spyedResources[0] != 0) spyLevel = SPY_LEVEL_RESOURCES;
            if(spyedResources[1] != 0) spyLevel = SPY_LEVEL_RESOURCES;
            if(spyedResources[2] != 0) spyLevel = SPY_LEVEL_RESOURCES;
        }
        
        for(int i = 0; i < buildingLevels.length; i++) {
            if(buildingLevels[i] != -1)
                spyLevel = SPY_LEVEL_BUILDINGS;
        }
        
        if(whereDefendersOnTheWay() && spyLevel == SPY_LEVEL_BUILDINGS) {
            //Some Buildings e.g. main cannot be zero
            //outside Troops can only be spyed if buildings were spyed too
            spyLevel = SPY_LEVEL_OUTSIDE;
        }

        //set wall destruction (works also without spying)
        if (wallAfter != -1 && spyLevel < SPY_LEVEL_BUILDINGS) {
            buildingLevels[BuildingSettings.getBuildingIdByName("wall")] = wallAfter;
        }

        switch (spyLevel) {
            case SPY_LEVEL_OUTSIDE:
            case SPY_LEVEL_BUILDINGS:
                for(int i = 0; i < this.buildingLevels.length; i++)
                    if(this.buildingLevels[i] == -1) this.buildingLevels[i] = 0;
            case SPY_LEVEL_RESOURCES:
                if(spyedResources == null)
                    spyedResources = new int[]{0, 0, 0};
            default:
        }
        logger.debug(JDomUtils.toShortString(toXml("report")));
    }

    public void setDefendersOutside(HashMap<Village, TroopAmountFixed> pDefendersOutside) {
        this.defendersOutside = pDefendersOutside;
    }

    public void setBuilding(int pBuildingId, int pLevel) {
        buildingLevels[pBuildingId] = pLevel;
    }
    
    public int getBuilding(int pBuilding) {
        return buildingLevels[pBuilding];
    }

    public int getSpyLevel() {
        return spyLevel;
    }
    
    public status getStatus() {
        if (areAttackersHidden()) return status.HIDDEN;
        if (isSpyReport()) return status.SPY;
        if (wasLostEverything()) return status.LOST_EVERYTHING;
        if (wasLostNothing()) return status.LOST_NOTHING;
        return status.WON_WITH_LOSSES;
    }
}
