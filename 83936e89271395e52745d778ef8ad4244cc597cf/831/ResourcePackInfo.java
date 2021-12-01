package io.gomint.server.player;

import java.util.HashMap;
import java.util.Map;

/**
 * @author geNAZt
 * @version 1.0
 */
public class ResourcePackInfo {

    private Map<String, String> loadedResourcePacks = new HashMap<>();

    public void addResourcePack( String id, String packName ) {
        this.loadedResourcePacks.put( id, packName );
    }
}
