package com.piksel.sequoia.clientsdk.resource;

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
