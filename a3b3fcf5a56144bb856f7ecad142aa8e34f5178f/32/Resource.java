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

import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.NotBlank;

import com.google.api.client.util.Key;
import com.piksel.sequoia.annotations.PublicEvolving;
import com.piksel.sequoia.clientsdk.validation.PutValidation;
import com.piksel.sequoia.clientsdk.validation.Validatable;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@PublicEvolving
@Getter
@ToString
public class Resource implements Validatable {

    @Key
    @Pattern(regexp = Identifier.REGEXP_PATTERN)
    private String name;

    @Key
    @NotBlank
    @Pattern(regexp = Identifier.REGEXP_PATTERN)
    private String owner;

    @Key
    private Reference ref;

    @Key
    @Setter
    private String title;

    @Key
    @Setter
    private String createdAt;

    @Key
    @Setter
    private String createdBy;

    @Key
    @Setter
    private String updatedAt;

    @Key
    @Setter
    private String updatedBy;

    @Key
    @Setter
    private String deletedAt;

    @Key
    @Setter
    private String deletedBy;

    @Key
    @Setter
    private String reapAt;

    @Key
    @NotBlank(groups = PutValidation.class)
    @Setter
    private String version;

    @Key
    @Setter
    private List<String> tags;

    @Key
    @Setter
    private Map<String, String> alternativeIdentifiers;

    @Key
    @Setter
    private ResourceMap custom;

    @Key
    @Setter
    private Map<String, Double> scores;

    @Key
    @Setter
    private MetadataType metadata;


    public void setOwner(String owner) {
        this.owner = owner;
        initRef();
        this.ref.setOwner(owner);
    }

    public void setName(String name) {
        this.name = name;
        initRef();
        this.ref.setName(name);
    }

    private void initRef() {
        if (Objects.isNull(ref)) {
            ref = new Reference();
        }
    }

}
