package com.piksel.sequoia.clientsdk.resource;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.api.client.util.Key;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
class Meta extends AbstractMeta{

    @Key
    private List<String> ignorefields;

    @Key
    private Map<String, Map<String, Integer>> facetCount;
    
    @Key
    private Map<String, Collection<LinkedMeta>> linked;

}
