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
package org.xwiki.filter.input;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * @version $Id: e27ebde824aa40b32dd374092b89a50aefe650ca $
 * @since 6.2M1
 */
public class DefaultURLInputSource extends AbstractInputStreamInputSource implements URLInputSource
{
    private final URL url;

    public DefaultURLInputSource(URL url)
    {
        this.url = url;
    }

    @Override
    public URL getURL()
    {
        return this.url;
    }

    @Override
    protected InputStream openStream() throws IOException
    {
        return this.url.openStream();
    }
}
