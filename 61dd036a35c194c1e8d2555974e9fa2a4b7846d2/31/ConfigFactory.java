package com.ctrip.apollo.spi;

import com.ctrip.apollo.Config;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public interface ConfigFactory {
   public Config create(String namespace);
}
