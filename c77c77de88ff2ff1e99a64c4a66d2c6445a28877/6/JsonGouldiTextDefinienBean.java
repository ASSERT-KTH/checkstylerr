package com.formulasearchengine.mathosphere.pomlp.gouldi;

/**
 * @author Andre Greiner-Petter
 */
public class JsonGouldiTextDefinienBean extends JsonGouldiWikidataDefinienBean{
    private String discription;

    @Override
    public String getDiscription() {
        return discription;
    }

    @Override
    public void setDiscription(String discription) {
        this.discription = discription;
    }
}
