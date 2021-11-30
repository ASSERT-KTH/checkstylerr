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

import java.util.Arrays;

public interface MetadataLockValue {

    enum MetadataLockStatusValue implements MetadataLockValue {
        LOCKED
    }

    enum MetadataLockActionValue implements MetadataLockValue {
        LOCK,
        UNLOCK,
        OVERRIDE
    }

    static MetadataLockValue valueOf(String metadataLockValue) {
        MetadataLockValue value;

        MetadataLockStatusValue[] statuses = MetadataLockStatusValue.values();
        boolean isStatus = Arrays.stream(statuses).anyMatch(status -> status.toString().equals(metadataLockValue));
        if (isStatus) {
            value = MetadataLockStatusValue.valueOf(metadataLockValue);
        } else {
            value = MetadataLockActionValue.valueOf(metadataLockValue);
        }

        return value;
    }
}
