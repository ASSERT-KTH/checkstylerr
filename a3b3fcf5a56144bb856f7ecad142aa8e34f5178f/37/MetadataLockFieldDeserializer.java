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

import com.google.gson.*;
import com.piksel.sequoia.clientsdk.resource.MetadataLockField;
import com.piksel.sequoia.clientsdk.resource.MetadataLockValue;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Type;
import java.util.Map;

@Slf4j
public class MetadataLockFieldDeserializer implements JsonDeserializer<MetadataLockField> {

    @Override
    public MetadataLockField deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        MetadataLockField metadataLockField = new MetadataLockField();
        if (json.isJsonObject()) {
            JsonObject jsonObject = json.getAsJsonObject();
            jsonObject.entrySet().forEach(entry -> addStatus(metadataLockField, entry));
            return metadataLockField;
        }
        return null;
    }

    private void addStatus(MetadataLockField metadataLockField, Map.Entry<String, JsonElement> entry) {
        try {
            metadataLockField.addLockValue(entry.getKey(), MetadataLockValue.valueOf(entry.getValue().getAsString()));
        } catch (IllegalArgumentException e) {
            log.error("Error deserializing the json into a MetadataLockField", e);
        }
    }
}
