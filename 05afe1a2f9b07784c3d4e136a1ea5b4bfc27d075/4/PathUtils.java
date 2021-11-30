/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.extension.internal;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import org.apache.commons.lang3.StringUtils;

/**
 * Various path utilities.
 * 
 * @version $Id: 6b42c6cd047a74fdf7a0cb4805cf5d3d516304c4 $
 * @since 6.4M1
 */
public final class PathUtils
{
    private PathUtils()
    {
        // Utility class
    }

    /**
     * Protect passed String to work with as much filesystems as possible.
     * 
     * @param str the file or directory name to encode
     * @return the encoded name
     * @since 7.1RC1
     */
    public static String encode(String str)
    {
        String encoded;
        try {
            encoded = URLEncoder.encode(str, "UTF-8").replace(".", "%2E").replace("*", "%2A");
        } catch (UnsupportedEncodingException e) {
            // Should never happen

            encoded = str;
        }

        return encoded;
    }

    // Workaround insane WebSphere bug (see https://jira.xwiki.org/browse/XWIKI-13898)
    private static URL fixURL(URL jarURL)
    {
        String jarURLString = jarURL.toExternalForm();
        if (StringUtils.contains(jarURLString, ' ')) {
            jarURLString = jarURLString.replace(" ", "%20");

            try {
                return new URL(jarURLString);
            } catch (MalformedURLException e) {
                // TODO: Log something ?
            }
        }

        return jarURL;
    }

    /**
     * @param descriptorURL the URL to the core extension descriptor
     * @return the URL to the core extension file
     * @throws IOException when failing to access passed URL
     * @since 8.4RC1
     * @since 7.4.6
     */
    public static URL getExtensionURL(URL descriptorURL) throws IOException
    {
        URLConnection connection = descriptorURL.openConnection();

        if (connection instanceof JarURLConnection) {
            return fixURL(((JarURLConnection) connection).getJarFileURL());
        }

        return descriptorURL;
    }
}
