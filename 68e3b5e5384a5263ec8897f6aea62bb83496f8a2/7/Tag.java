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
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.ui.ImageManager;
import de.tor.tribes.util.BBSupport;
import de.tor.tribes.util.StringHelper;
import de.tor.tribes.util.bb.VillageListFormatter;
import de.tor.tribes.util.xml.JDomUtils;
import java.awt.Color;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;

/**
 *
 * @author Charon
 */
public class Tag extends ManageableType implements Comparable<Tag>, BBSupport {

    public static final Comparator<Tag> CASE_INSENSITIVE_ORDER = new CaseInsensitiveTagComparator();
    public static final Comparator<Tag> SIZE_ORDER = new SizeComparator();
    private final static String[] VARIABLES = new String[]{"%NAME%", "%VILLAGE_LIST%", "%VILLAGE_COUNT%", "%COLOR%", "%ICON%"};
    public final static String STANDARD_TEMPLATE = "[u][color=\"%COLOR%\"][b]%NAME%[/b][/color][/u]\n"
            + "%ICON%\n"
            + "Dörfer: %VILLAGE_COUNT%\n"
            + "[quote]%VILLAGE_LIST%[/quote]";
    private String sName = null;
    private List<Integer> mVillageIDs = new LinkedList<>();
    //-1 means no icon
    private TagMapMarker mapMarker = null;
    private boolean showOnMap = true;
    
    private static Logger logger = LogManager.getLogger("Tag");

    @Override
    public String[] getBBVariables() {
        return VARIABLES;
    }

    @Override
    public String[] getReplacements(boolean pExtended) {
        String nameVal = sName;
        List<Village> villages = new LinkedList<>();
        for (Integer id : getVillageIDs()) {
            Village v = DataHolder.getSingleton().getVillagesById().get(id);
            if (v != null) {
                villages.add(v);
            }
        }
        String villageListVal = new VillageListFormatter().formatElements(villages, pExtended);
        String villageCountVal = Integer.toString(getVillageIDs().size());
        String colorVal = "";
        if (getTagColor() != null) {
            colorVal = Integer.toHexString(getTagColor().getRGB());
            colorVal = "#" + colorVal.substring(2, colorVal.length());
        } else {
            colorVal = Integer.toHexString(Color.BLACK.getRGB());
            colorVal = "#" + colorVal.substring(2, colorVal.length());
        }
        String iconVal = "";
        if (getTagIcon() != -1) {
            /*  UnitHolder u = DataHolder.getSingleton().getUnits().get(getTagIcon());
            if (u != null) {
            iconVal = "[unit]" + u.getPlainName() + "[/unit]";
            }*/
            iconVal = "[img]" + ImageManager.getNoteImageURLOnServer(getTagIcon()) + "[/img]";

        }

        return new String[]{nameVal, villageListVal, villageCountVal, colorVal, iconVal};
    }

    @Override
    public void loadFromXml(Element pElement) {
        try {
            String name = URLDecoder.decode(pElement.getChild("name").getTextTrim(), "UTF-8");
            boolean bShowOnMap = Boolean.parseBoolean(pElement.getAttributeValue("shownOnMap"));
            this.sName = name;
            showOnMap = bShowOnMap;
            try {
                Element color = pElement.getChild("color");
                int r = color.getAttribute("r").getIntValue();
                int g = color.getAttribute("g").getIntValue();
                int b = color.getAttribute("b").getIntValue();
                setTagColor(new Color(r, g, b));
            } catch (Exception e) {
                setTagColor(null);
            }

            try {
                Element icon = pElement.getChild("icon");
                setTagIcon(Integer.parseInt(icon.getText()));
            } catch (Exception e) {
                setTagIcon(-1);
            }

            for (Element e : (List<Element>) JDomUtils.getNodes(pElement, "villages/village")) {
                tagVillage(Integer.parseInt(e.getValue()));
            }
        } catch (Exception ignored) {
        }
    }

    public Tag() {
        this.mapMarker = new TagMapMarker();

    }

    /**Default constructor*/
    public Tag(String pName, boolean pShowOnMap) {
        this.sName = pName;
        showOnMap = pShowOnMap;
        this.mapMarker = new TagMapMarker();
    }

    /**Get the tag name
     * @return String Name of this tag
     */
    public String getName() {
        return sName;
    }

    /**Set the tag name
     * @param pName Name of this tag
     */
    public final void setName(String pName) {
        this.sName = pName;
    }

    /**Get the map marker of this tag
     * @return TagMapMarker Map marker of this tag
     */
    public TagMapMarker getMapMarker() {
        return mapMarker;
    }

    /**Tag the village with the ID 'pVillageID' by this tag
     * @param pVillageID ID of the village to tag
     */
    public void tagVillage(Integer pVillageID) {
        if (!mVillageIDs.contains(pVillageID)) {
            mVillageIDs.add(pVillageID);
        }
    }

    /**Remove this tag from the village with the ID 'pVillageID'
     *@param pVillageID ID of the village to untag
     */
    public void untagVillage(Integer pVillageID) {
        mVillageIDs.remove(pVillageID);
    }

    /**Get the list of IDs of villages tagged by this tag
     * @return List<Integer> List of tagged villages IDs
     */
    public List<Integer> getVillageIDs() {
        return mVillageIDs;
    }
  
    /**Check whether this tag tags the village with the ID 'pVillageID' or not
     * @param pVillageID ID of the village to check
     * @return boolean TRUE=Tag tags the village
     */
    public boolean tagsVillage(int pVillageID) {
        return mVillageIDs.contains(pVillageID);
    }

    /**Remove all tagged villages*/
    public void clearTaggedVillages() {
        mVillageIDs.clear();
    }

    /**Set whether to render villages tagged by this tag or not
     * @param pValue TRUE=render villages tagged by this tag
     */
    public final void setShowOnMap(boolean pValue) {
        showOnMap = pValue;
    }

    /**Check whether villages tagged by this tag are rendered or not
     * @return boolean TRUE=tagges villages are rendered
     */
    public boolean isShowOnMap() {
        return showOnMap;
    }

    @Override
    public String toString() {
        return sName;
    }

    /**Convert this tag into its XML representation
     * @return String String that contains the XML representation
     */
    @Override
    public Element toXml(String elementName) {
        Element tag = new Element(elementName);
        try {
            tag.setAttribute("shownOnMap", Boolean.toString(showOnMap));
            
            tag.addContent(new Element("name").setText(URLEncoder.encode(sName, "UTF-8")));
            
            Color c = getTagColor();
            if (c != null) {
                Element color = new Element("color");
                color.setAttribute("r", Integer.toString(c.getRed()));
                color.setAttribute("g", Integer.toString(c.getGreen()));
                color.setAttribute("b", Integer.toString(c.getBlue()));
                tag.addContent(color);
            }
            tag.addContent(new Element("icon").setText(Integer.toString(getTagIcon())));
            Element villages = new Element("villages");
            for (Integer i : mVillageIDs) {
                villages.addContent(new Element("village").setText(Integer.toString(i)));
            }
            tag.addContent(villages);
        } catch (Exception e) {
            logger.error("Exception during generating XML", e);
            return null;
        }
        return tag;
    }

    /**Get the color of the associated TagMapMarker
     * @return Color the TagMapMarker's color
     */
    public Color getTagColor() {
        return mapMarker.getTagColor();
    }

    /**Set the color of the associated TagMapMarker
     * @param tagColor the TagMapMarker's color
     */
    public void setTagColor(Color tagColor) {
        mapMarker.setTagColor(tagColor);
    }

    /**Get the icon's ID of the associated TagMapMarker
     * @return the tagIcon
     */
    public int getTagIcon() {
        return mapMarker.getTagIcon();
    }

    /**Set the icon's ID of the associated TagMapMarker
     * @param tagIcon the tagIcon to set
     */
    public void setTagIcon(int tagIcon) {
        mapMarker.setTagIcon(tagIcon);
    }

    /**Set the associated TagMapMarker
     * @param mapMarker the mapMarker to set
     */
    public final void setMapMarker(TagMapMarker mapMarker) {
        this.mapMarker = mapMarker;
    }

    private static class CaseInsensitiveTagComparator implements Comparator<Tag>, java.io.Serializable {
        // use serialVersionUID from JDK 1.2.2 for interoperability

        private static final long serialVersionUID = 8575799808933029326L;

        @Override
        public int compare(Tag s1, Tag s2) {
            return StringHelper.compareByStringRepresentations(s1, s2);
        }
    }

    private static class SizeComparator implements Comparator<Tag>, java.io.Serializable {
        // use serialVersionUID from JDK 1.2.2 for interoperability

        private static final long serialVersionUID = 8575799808933029326L;

        @Override
        public int compare(Tag s1, Tag s2) {
            return Integer.compare(s2.getName().length(), s1.getName().length());
        }
    }

    @Override
    public int compareTo(Tag o) {
        return CASE_INSENSITIVE_ORDER.compare(this, o);
    }
}
