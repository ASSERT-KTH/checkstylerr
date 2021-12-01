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
package de.tor.tribes.util.bb;

import de.tor.tribes.types.SOSRequest;
import de.tor.tribes.types.ext.Village;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Torridity
 */
public class SosListFormatter extends BasicFormatter<SOSRequest> {

    private static final String[] VARIABLES = new String[] {LIST_START, LIST_END, ELEMENT_COUNT, ELEMENT_ID};
    private static final String STANDARD_TEMPLATE = new SOSRequest().getStandardTemplate();
    private static final String TEMPLATE_PROPERTY = "sos.list.bbexport.template";

    @Override
    public String getPropertyKey() {
        return TEMPLATE_PROPERTY;
    }

    @Override
    public String getStandardTemplate() {
        return STANDARD_TEMPLATE;
    }

    @Override
    public String formatElements(List<SOSRequest> pElements, boolean pExtended) {
        StringBuilder b = new StringBuilder();
        int cnt = 1;
        NumberFormat f = getNumberFormatter(pElements.size());
        String beforeList = getHeader();
        String listItemTemplate = getLineTemplate();
        String afterList = getFooter();
        String replacedStart = StringUtils.replaceEach(beforeList, new String[] {ELEMENT_COUNT}, new String[] {f.format(pElements.size())});
        b.append(replacedStart);
        for (SOSRequest s : pElements) {
            for(Village target: s.getTargets()) {
                String[] replacements = s.getReplacementsForTarget(target, pExtended);
                String itemLine = StringUtils.replaceEach(listItemTemplate, s.getBBVariables(), replacements);
                itemLine = StringUtils.replaceEach(itemLine, new String[] {ELEMENT_ID, ELEMENT_COUNT}, new String[] {f.format(cnt), f.format(pElements.size())});
                b.append(itemLine).append("\n").append("\n");
            }
            cnt++;
        }
        String replacedEnd = StringUtils.replaceEach(afterList, new String[] {ELEMENT_COUNT}, new String[] {f.format(pElements.size())});
        b.append(replacedEnd);
        return b.toString();
    }

    @Override
    public String[] getTemplateVariables() {
        List<String> vars = new LinkedList<>();
        Collections.addAll(vars, VARIABLES);
        Collections.addAll(vars, new SOSRequest().getBBVariables());
        return vars.toArray(new String[vars.size()]);
    }
    
    @Override
    public Class<SOSRequest> getConvertableType() {
        return SOSRequest.class;
    }
}
