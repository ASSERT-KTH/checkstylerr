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
 */
@Getter
@EqualsAndHashCode
@Internal
public class RangeExpression implements Criterion {

    private final String propertyName;
    private final Object rangeValueA;
    private final Object rangeValueB;
    private final RangeLocation rangeLocation;

    public RangeExpression(String propertyName, Object rangeValueA,
            Object rangeValueB, RangeLocation rangeLocation) {
        this.propertyName = propertyName;
        this.rangeValueA = rangeValueA;
        this.rangeValueB = rangeValueB;
        this.rangeLocation = rangeLocation;
    }

    public RangeExpression(String propertyName, Object rangeValueA,
            RangeLocation rangeLocation) {
        this.propertyName = propertyName;
        this.rangeValueA = rangeValueA;
        this.rangeValueB = null;
        this.rangeLocation = rangeLocation;
    }

    public void applyExpression(DocumentName documentName, QueryString qs) {
        String rva = String.valueOf(this.rangeValueA);
        if (this.rangeValueB != null) {
            String rvb = String.valueOf(this.rangeValueB);
            qs.put(documentName.get().concat(this.propertyName),
                    this.rangeLocation.toMatchString(rva, rvb));
        } else {
            qs.put(documentName.get().concat(this.propertyName), this.rangeLocation.toMatchString(rva));
        }
    }

}
