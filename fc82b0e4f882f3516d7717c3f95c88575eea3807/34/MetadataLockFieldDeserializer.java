package com.piksel.sequoia.clientsdk.resource.json;

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
