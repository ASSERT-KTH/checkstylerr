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

import com.piksel.sequoia.annotations.Internal;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * An expression {@link Criterion} used during selection.
 * 
 * <p>This expression is <em>simple</em> because it produces
 * a basic equality criteria, as opposed to other more intricate
 * expressions such as range expressions or text search 
 * expressions.
 */
@Internal
@Getter
@EqualsAndHashCode
public class SimpleExpression implements Criterion {

    private final String propertyName;
    private final Object value;
    private final Operator op;

    public SimpleExpression(String propertyName, Object value, Operator op) {
        this.propertyName = propertyName;
        this.value = value;
        this.op = op;
    }

    public void applyExpression(DocumentName documentName, QueryString qs) {
        qs.put(documentName.get().concat(this.propertyName), String.valueOf(this.value));
    }

}
