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

import de.tor.tribes.types.ext.Barbarians;
import de.tor.tribes.types.ext.Tribe;
import de.tor.tribes.types.ext.Village;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Torridity
 */
public class TribeUtils {
    
    public static Tribe[] getTribeByVillage(Village pVillage, boolean pUseBarbarians, Comparator<Tribe> pComparator) {
        return getTribeByVillage(new Village[]{pVillage}, pUseBarbarians, pComparator);
    }

    public static Tribe[] getTribeByVillage(Village[] pVillages, boolean pUseBarbarians, Comparator<Tribe> pComparator) {
        List<Tribe> tribes = new LinkedList<>();
        
        for (Village v : pVillages) {
            Tribe t = v.getTribe();
            if (pUseBarbarians || !t.equals(Barbarians.getSingleton())) {
                if (!tribes.contains(t)) {
                    tribes.add(t);
                }
            }
        }
        
        if (pComparator != null) {
            Collections.sort(tribes, pComparator);
        }
        
        return tribes.toArray(new Tribe[tribes.size()]);
    }    
}
