/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.siddhi.api.condition.where.logical;


import org.wso2.siddhi.api.condition.ExpirableCondition;
import org.wso2.siddhi.api.condition.where.WhereCondition;
import org.wso2.siddhi.api.condition.Condition;

import java.util.Map;

/**
 * 
 * OrCondition  class handles the logical OR conditions
 */
public class OrCondition extends ExpirableCondition implements WhereCondition {
    private Condition leftCondition;
    private Condition rightCondition;


    /**
     * @param leftCondition  one of the condition to be evaluated
     * @param rightCondition one of the condition to be evaluated
     */
    public OrCondition(Condition leftCondition, Condition rightCondition) {
        this.leftCondition = leftCondition;
        this.rightCondition = rightCondition;
    }

    /**
     * get the left condition
     *
     * @return the left condition
     */
    public Condition getLeftCondition() {
        return leftCondition;
    }

    /**
     * get the right condition
     *
     * @return the right condition
     */
    public Condition getRightCondition() {
        return rightCondition;
    }

    @Override
    public WhereCondition getNewInstance(Map<String, String> referenceConversion) {
        return (WhereCondition) new OrCondition(((WhereCondition)leftCondition).getNewInstance(referenceConversion),((WhereCondition)rightCondition).getNewInstance(referenceConversion)).within(getLifeTime());
}
}