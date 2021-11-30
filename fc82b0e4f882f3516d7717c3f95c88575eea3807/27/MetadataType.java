package com.piksel.sequoia.clientsdk.resource;

import com.google.common.collect.ImmutableMap;
import com.piksel.sequoia.annotations.PublicEvolving;
import lombok.Data;
import org.apache.commons.lang.StringUtils;

import java.util.Map;
import java.util.Objects;

import static com.piksel.sequoia.clientsdk.resource.MetadataLockFieldValue.LOCKING;
import static com.piksel.sequoia.clientsdk.resource.MetadataLockFieldValue.LOCKS;

@PublicEvolving
@Data
public final class MetadataType {

    private Map<MetadataLockFieldValue, MetadataLockField> fields = ImmutableMap.of(
            LOCKS, new MetadataLockField(),
            LOCKING, new MetadataLockField()
    );


    public MetadataLockField getLockStatuses() {
        return fields.get(LOCKS);
    }

    public MetadataLockField getLockActions() {
        return fields.get(LOCKING);
    }

    public void addLockAction(String pathToField, MetadataLockValue.MetadataLockActionValue action) {
        if (StringUtils.isNotBlank(pathToField)) {
            if (Objects.isNull(fields.get(LOCKING))) fields.put(LOCKING, new MetadataLockField());
            final MetadataLockField metadataLockField = fields.get(LOCKING);
            metadataLockField.addLockValue(pathToField, action);
        }
    }

}
