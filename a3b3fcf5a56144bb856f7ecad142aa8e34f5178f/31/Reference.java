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

import javax.validation.constraints.Pattern;

import org.apache.commons.lang.StringUtils;
import org.hibernate.validator.constraints.NotBlank;

import com.google.api.client.util.Key;
import com.piksel.sequoia.annotations.PublicEvolving;
import com.piksel.sequoia.clientsdk.validation.Validatable;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * A Reference is an extension of a string and represents a global friendly
 * identifier that is under client control. It composed of the tenant owner name
 * and the resource name separated by a colon.
 */

@Getter
@Setter
@EqualsAndHashCode
@PublicEvolving
public class Reference implements Validatable {

    @Key
    @NotBlank
    @Pattern(regexp = Identifier.REGEXP_PATTERN)
    private String name;

    @Key
    @NotBlank
    @Pattern(regexp = Identifier.REGEXP_PATTERN)
    private String owner;

    private Reference(String owner, String name) {
        this.owner = owner;
        this.name = name;
    }

    protected Reference() {

    }

    /**
     * Get the Reference as String representation.
     */
    public String toString() {
        return name != null ? owner + ":" + name : "";
    }

    public static Reference fromOwnerAndName(String owner, String name) {
        if (StringUtils.isBlank(owner) || StringUtils.isBlank(name)) {
            return null;
        }
        return new Reference(owner, name);
    }

    public static Reference fromReference(String reference) {
        if (reference.split(":").length != 2) {
            throw new IllegalArgumentException("Illegal reference");
        }
        return new Reference(reference.split(":")[0], reference.split(":")[1]);
    }

}
