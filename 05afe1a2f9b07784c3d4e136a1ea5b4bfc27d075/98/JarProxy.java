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
package org.xwiki.classloader.internal.protocol.jar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilePermission;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessController;
import java.security.Permission;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import edu.emory.mathcs.util.classloader.ResourceUtils;
import edu.emory.mathcs.util.io.RedirectibleInput;
import edu.emory.mathcs.util.io.RedirectingInputStream;

/**
 * Implementation of {@link JarURLConnection.JarOpener} that caches downloaded JAR files in a local file system.
 * <p>
 * Originally written by Dawid Kurzyniec and released to the public domain, as explained at
 * http://creativecommons.org/licenses/publicdomain
 * </p>
 * <p>
 * Source: http://dcl.mathcs.emory.edu/php/loadPage.php?content=util/features.html#classloading
 * </p>
 *
 * @see JarURLConnection
 * @see JarURLStreamHandler
 * @version $Id: ca356d8a24e0865bf26b6a5ab5419126dada91da $
 * @since 2.0.1
 * @deprecated since 12.5RC1
 */
@Deprecated
public class JarProxy implements JarURLConnection.JarOpener
{
    private final Map<URL, CachedJarFile> cache = new HashMap<>();

    @SuppressWarnings("resource")
    @Override
    public JarFile openJarFile(JarURLConnection conn) throws IOException
    {
        URL url = conn.getJarFileURL();
        CachedJarFile result;
        synchronized (this.cache) {
            result = this.cache.get(url);
        }
        if (result != null) {
            SecurityManager security = System.getSecurityManager();
            if (security != null) {
                security.checkPermission(result.perm);
            }
            return result;
        }

        // we have to download and open the JAR; yet it may be a local file
        try {
            URI uri = new URI(url.toString());
            if (ResourceUtils.isLocalFile(uri)) {
                File file = new File(uri);
                Permission perm = new FilePermission(file.getAbsolutePath(), "read");
                result = new CachedJarFile(file, perm, false);
            }
        } catch (URISyntaxException e) {
            // apparently not a local file
        }

        if (result == null) {
            final URLConnection jarconn = url.openConnection();

            // set up the properties based on the JarURLConnection
            jarconn.setAllowUserInteraction(conn.getAllowUserInteraction());
            jarconn.setDoInput(conn.getDoInput());
            jarconn.setDoOutput(conn.getDoOutput());
            jarconn.setIfModifiedSince(conn.getIfModifiedSince());

            Map<String, List<String>> map = conn.getRequestProperties();
            for (Map.Entry<String, List<String>> entry : map.entrySet()) {
                StringBuilder value = new StringBuilder();
                for (String str : entry.getValue()) {
                    value.append(',').append(str);
                }
                if (value.length() >= 1) {
                    jarconn.setRequestProperty(entry.getKey(), value.substring(1));
                }
            }

            jarconn.setUseCaches(conn.getUseCaches());

            try (InputStream in = getJarInputStream(jarconn)) {
                result = AccessController.doPrivileged(new PrivilegedExceptionAction<CachedJarFile>()
                {
                    @Override
                    public CachedJarFile run() throws IOException
                    {
                        File file = File.createTempFile("jar_cache", "");
                        try (FileOutputStream out = new FileOutputStream(file)) {
                            RedirectibleInput r = new RedirectingInputStream(in, false, false);
                            int len = r.redirectAll(out);
                            out.flush();
                            if (len == 0) {
                                // e.g. HttpURLConnection: "NOT_MODIFIED"
                                return null;
                            }
                        }
                        return new CachedJarFile(file, jarconn.getPermission(), true);

                    }
                });
            } catch (PrivilegedActionException pae) {
                throw (IOException) pae.getException();
            }
        }

        // if no input came (e.g. due to NOT_MODIFIED), do not cache
        if (result == null) {
            return null;
        }

        // optimistic locking
        synchronized (this.cache) {
            CachedJarFile asyncResult = this.cache.get(url);
            if (asyncResult != null) {
                // some other thread already retrieved the file; return w/o
                // security check since we already succeeded in getting past it
                result.closeCachedFile();
                return asyncResult;
            }
            this.cache.put(url, result);
            return result;
        }
    }

    protected InputStream getJarInputStream(URLConnection conn) throws IOException
    {
        return conn.getInputStream();
    }

    protected void clear()
    {
        Map<URL, CachedJarFile> cache;
        synchronized (this.cache) {
            cache = new HashMap<>(this.cache);
            this.cache.clear();
        }
        for (CachedJarFile jfile : cache.values()) {
            try {
                jfile.closeCachedFile();
            } catch (IOException e) {
                // best-effort
            }
        }
    }

    @Override
    protected void finalize() throws java.lang.Throwable
    {
        try {
            clear();
        } finally {
            super.finalize();
        }
    }

    private static class CachedJarFile extends JarFile
    {
        final Permission perm;

        CachedJarFile(File file, Permission perm, boolean tmp) throws IOException
        {
            super(file, true, ZipFile.OPEN_READ | (tmp ? ZipFile.OPEN_DELETE : 0));
            this.perm = perm;
        }

        @Override
        public Manifest getManifest() throws IOException
        {
            Manifest orig = super.getManifest();
            if (orig == null) {
                return null;
            }
            // make sure the original manifest is not modified
            Manifest man = new Manifest();
            man.getMainAttributes().putAll(orig.getMainAttributes());
            for (Map.Entry<String, Attributes> entry : orig.getEntries().entrySet()) {
                man.getEntries().put(entry.getKey(), new Attributes(entry.getValue()));
            }
            return man;
        }

        @Override
        public ZipEntry getEntry(String name)
        {
            // super.getJarEntry() would result in stack overflow
            return super.getEntry(name);
        }

        @Override
        protected void finalize() throws IOException
        {
            closeCachedFile();
        }

        protected void closeCachedFile() throws IOException
        {
            super.close();
        }

        @Override
        public void close() throws IOException
        {
            // no op; do NOT close file while still in cache
        }
    }
}
