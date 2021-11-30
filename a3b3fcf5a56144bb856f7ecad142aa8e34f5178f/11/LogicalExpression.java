package com.piksel.sequoia.clientsdk.criteria;

/*-
 * #%L
 * Sequoia Java Client SDK
 * %%
 * Copyright (C) 2018 Piksel
 * %%
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
 * #L%
 */

import static java.util.Arrays.copyOf;

import com.piksel.sequoia.annotations.Internal;
import com.piksel.sequoia.annotations.PublicEvolving;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
@PublicEvolving
@Internal
public class LogicalExpression implements Criterion {

    private final String propertyName;
    private final Object[] values;
    private final LogicalLocation logicalLocation;

    public LogicalExpression(String propertyName, Object[] values,
            LogicalLocation logicalLocation) {
        this.propertyName = propertyName;
        this.values = copyOf(values, values.length);
        this.logicalLocation = logicalLocation;
    }

    public Object[] getValues() {
        return copyOf(values, values.length);
    }

    public void applyExpression(DocumentName documentName, QueryString qs) {
        qs.put(documentName.get().concat(propertyName),
                logicalLocation.toMatchString((String[]) getValues()));
    }

}
