package com.piksel.sequoia.clientsdk.resource;

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
