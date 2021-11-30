package com.piksel.sequoia.clientsdk.criteria;

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

import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.apache.commons.lang.StringUtils.isNotBlank;

import com.piksel.sequoia.annotations.PublicEvolving;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@PublicEvolving
/**
 * Wrapper the name of the document on which apply search criteria. Provides a method to include the name into a criteria with the
 * suitable separator.
 * Use it when you want indicate the name of the document in the search criteria.
 *
 * Usage:
 *  DocumentName rootName = new DocumentName("root");
 *  queryString.put(rootName.get().concat("fieldName"),"value");
 *
 *  It will be translated into
 *      root.fieldName=value
 */
public class DocumentName {
    private final static String SEPARATOR = ".";
    private String name;

    public String get() {
        return isNotBlank(name) ?
                name.concat(SEPARATOR)
                : EMPTY;
    }
}
