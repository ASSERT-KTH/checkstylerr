package com.piksel.sequoia.clientsdk.criteria;

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
