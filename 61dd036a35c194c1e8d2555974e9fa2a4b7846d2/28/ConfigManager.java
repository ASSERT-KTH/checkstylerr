package com.ctrip.apollo.internals;

import com.ctrip.apollo.Config;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public interface ConfigManager {
   public Config getConfig(String namespace);
}
