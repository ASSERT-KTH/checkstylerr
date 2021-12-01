package com.formulasearchengine.mathosphere.pomlp.gouldi;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * @author Andre Greiner-Petter
 */
public class JsonGouldiIdentifierDefinienBean {
    private String name;
    private JsonGouldiWikidataDefinienBean[] definiens;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public JsonGouldiWikidataDefinienBean[] getDefiniens() {
        return definiens;
    }

    public void setDefiniens(JsonGouldiWikidataDefinienBean[] definiens) {
        this.definiens = definiens;
    }
}
