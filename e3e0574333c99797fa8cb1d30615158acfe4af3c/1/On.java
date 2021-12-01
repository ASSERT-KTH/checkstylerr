/*
 * Copyright 2014 jmrozanec
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cronutils.model.field.expression;

import com.cronutils.model.field.value.IntegerFieldValue;
import com.cronutils.model.field.value.SpecialChar;
import com.cronutils.model.field.value.SpecialCharFieldValue;
import com.cronutils.utils.Preconditions;
import com.cronutils.utils.StringUtils;
import org.apache.commons.lang3.Validate;

import static com.cronutils.utils.Preconditions.checkArgument;

public class On extends FieldExpression {

    private static final long serialVersionUID = 8746471281123327324L;
    private static final int DEFAULT_NTH_VALUE = -1;
    private final IntegerFieldValue time;
    private final IntegerFieldValue nth;
    private final SpecialCharFieldValue specialChar;

    public On(final SpecialCharFieldValue specialChar) {
        this(new IntegerFieldValue(DEFAULT_NTH_VALUE), specialChar);
    }

    public On(final IntegerFieldValue time) {
        this(time, new SpecialCharFieldValue(SpecialChar.NONE));
    }

    public On(final IntegerFieldValue time, final SpecialCharFieldValue specialChar) {
        this(time, specialChar, new IntegerFieldValue(-1));
        checkArgument(!specialChar.getValue().equals(SpecialChar.HASH), "value missing for a#b cron expression");
    }

    public On(final IntegerFieldValue time, final SpecialCharFieldValue specialChar, final IntegerFieldValue nth) {
        Preconditions.checkNotNull(time, "time must not be null");
        Preconditions.checkNotNull(specialChar, "special char must not null");
        Preconditions.checkNotNull(nth, "nth value must not be null");

        this.time = time;
        this.specialChar = specialChar;
        this.nth = nth;
    }

    public IntegerFieldValue getTime() {
        return time;
    }

    public IntegerFieldValue getNth() {
        return nth;
    }

    public SpecialCharFieldValue getSpecialChar() {
        return specialChar;
    }

    @Override
    public String asString() {
        switch (specialChar.getValue()) {
            case NONE:
                return getTime().toString();
            case HASH:
                return String.format("%s#%s", getTime(), getNth());
            case W:
                return isDefault(getTime()) ? "W" : String.format("%sW", getTime());
            case L:
                return isDefault(getTime()) ? "L" + getNthStringRepresentation() : String.format("%sL", getTime());
            default:
                return specialChar.toString();
        }
    }

    private String getNthStringRepresentation() {
        return isDefault(getNth()) ? StringUtils.EMPTY : String.format("-%s", getNth());
    }

    private boolean isDefault(final IntegerFieldValue fieldValue) {
        return fieldValue.getValue() == DEFAULT_NTH_VALUE;
    }
}
