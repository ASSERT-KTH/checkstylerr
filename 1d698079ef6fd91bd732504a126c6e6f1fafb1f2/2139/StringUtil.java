/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides common utility methods for handling strings.
 *
 * @author <a href="&#109;a&#105;&#108;&#116;&#111;:chris&#64;swebtec.&#99;&#111;&#109;">Chris Miller</a>
 */
public class StringUtil {
    
    private StringUtil() {
    }
	
    /**
     * Splits a string into substrings based on the supplied delimiter
     * character. Each extracted substring will be trimmed of leading
     * and trailing whitespace.
     *
     * @param str The string to split
     * @param delimiter The character that delimits the string
     * @return A string array containing the resultant substrings
     */
    public static final List split(String str, char delimiter) {
        // return no groups if we have an empty string
        if ((str == null) || "".equals(str)) {
            return new ArrayList();
        }

        ArrayList parts = new ArrayList();
        int currentIndex;
        int previousIndex = 0;

        while ((currentIndex = str.indexOf(delimiter, previousIndex)) > 0) {
            String part = str.substring(previousIndex, currentIndex).trim();
            parts.add(part);
            previousIndex = currentIndex + 1;
        }

        parts.add(str.substring(previousIndex, str.length()).trim());

        return parts;
    }
    
    /**
     * @param s the string to be checked
     * @return true if the string parameter contains at least one element 
     */
    public static final boolean hasLength(String s) {
    	return (s != null) && (s.length() > 0);
    }
    
    /**
     * @param s the string to be checked
     * @return true if the string parameter is null or doesn't contain any element
     * @since 2.4 
     */
    public static final boolean isEmpty(String s) {
        return (s == null) || (s.length() == 0);
    }
    
}
