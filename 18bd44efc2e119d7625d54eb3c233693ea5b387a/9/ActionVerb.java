/*
 * Copyright 2012-2013 inBloom, Inc. and its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.slc.sli.ingestion;

import java.io.Serializable;

/**
 * Various Action Types that will be allowed in the schema
 *
 * @author ayarmak
 *
 */
public enum ActionVerb implements Serializable {
    POST("POST"), PUT("PUT"), DELETE("DELETE"), CASCADE_DELETE("CASCADE_DELETE", true), MOVE("MOVE"), PATCH("PATCH"),
    UNKNOWN("UNKNOWN"), NONE( "NONE");

    private final boolean isCascade;
    private final String text;

    private ActionVerb(String word) {
        this.text = word;
        this.isCascade = false;
    }

    private ActionVerb(String word, boolean doCascade) {
        this.text = word;
        this.isCascade = doCascade;
    }

    public boolean doCascade() {
        return isCascade;
    }

    public String getText() {
        return text;
    }

    public boolean doDelete() {
        return ( this == DELETE || this == CASCADE_DELETE ) ? true : false ;
    }

}
