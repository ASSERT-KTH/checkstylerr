/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.plugins.diskpersistence;


/**
 * Persist the cache data to disk.
 *
 * The code in this class is totally not thread safe it is the resonsibility
 * of the cache using this persistence listener to handle the concurrency.
 *
 * @version        $Revision: 254 $
 * @author <a href="mailto:fbeauregard@pyxis-tech.com">Francois Beauregard</a>
 * @author <a href="mailto:abergevin@pyxis-tech.com">Alain Bergevin</a>
 * @author <a href="&#109;a&#105;&#108;&#116;&#111;:chris&#64;swebtec.&#99;&#111;&#109;">Chris Miller</a>
 */
public class DiskPersistenceListener extends AbstractDiskPersistenceListener {
    private static final String CHARS_TO_CONVERT = "./\\ :;\"\'_?";

    /**
    * Build cache file name for the specified cache entry key.
    *
    * @param key   Cache Entry Key.
    * @return char[] file name.
    */
    protected char[] getCacheFileName(String key) {
        if ((key == null) || (key.length() == 0)) {
            throw new IllegalArgumentException("Invalid key '" + key + "' specified to getCacheFile.");
        }

        char[] chars = key.toCharArray();

        StringBuffer sb = new StringBuffer(chars.length + 8);

        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            int pos = CHARS_TO_CONVERT.indexOf(c);
 
            if (pos >= 0) {
                sb.append('_');
                sb.append(i);
            } else {
                sb.append(c);
            }
        }

        char[] fileChars = new char[sb.length()];
        sb.getChars(0, fileChars.length, fileChars, 0);
        return fileChars;
    }
}
