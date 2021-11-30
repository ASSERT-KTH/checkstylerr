package com.piksel.sequoia.clientsdk.resource;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.piksel.sequoia.annotations.Public;

/**
 * Marks that the field is a direct relationship.
 */
@Retention(RetentionPolicy.RUNTIME)
@Public
public @interface DirectRelationship {
    
    /**
     * 
     */
    String ref();

    String relationship();
    
}