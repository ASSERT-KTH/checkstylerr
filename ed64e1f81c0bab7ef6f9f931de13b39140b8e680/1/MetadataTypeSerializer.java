package com.piksel.sequoia.clientsdk.resource.json;

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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.piksel.sequoia.clientsdk.resource.MetadataType;

import java.lang.reflect.Type;
import java.util.Objects;

public class MetadataTypeSerializer implements JsonSerializer<MetadataType> {

    @Override
    public JsonElement serialize(MetadataType src, Type typeOfSrc, JsonSerializationContext context) {
        if (isEmptyMetadataType(src)) {
            return null;
        }

        JsonElement fields = context.serialize(src.getFields());

        JsonObject metadataType = new JsonObject();
        metadataType.add("fields", fields);
        return metadataType;
    }

    private boolean isEmptyMetadataType(MetadataType metadataType) {
        return  Objects.isNull(metadataType) ||
                Objects.isNull(metadataType.getFields()) ||
                metadataType.getFields().isEmpty() ||
                (lockActionsIsEmpty(metadataType) && lockStatusesIsEmpty(metadataType));
    }

    private boolean lockStatusesIsEmpty(MetadataType metadataType) {
        return Objects.isNull(metadataType.getLockStatuses()) || metadataType.getLockStatuses().getLockValues().isEmpty();
    }

    private boolean lockActionsIsEmpty(MetadataType metadataType) {
        return Objects.isNull(metadataType.getLockActions()) || metadataType.getLockActions().getLockValues().isEmpty();
    }
}
