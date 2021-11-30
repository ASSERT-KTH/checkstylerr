package com.piksel.sequoia.clientsdk.resource;

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

import org.apache.commons.lang3.StringUtils;

public enum MetadataLockFieldValue {
    LOCKS("locks"),
    LOCKING("locking");

    private final String field;

    MetadataLockFieldValue(String field) {
        this.field = field;
    }

    @Override
    public String toString() {
        return field;
    }

    public static MetadataLockFieldValue fromString(String fieldName) {
        if (StringUtils.isNotBlank(fieldName)) {
            for (MetadataLockFieldValue item : MetadataLockFieldValue.values()) {
                if (fieldName.equalsIgnoreCase(item.field)) {
                    return item;
                }
            }
        }
        return null;
    }

}
