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
import java.io.Reader;

/**
 * @version $Id: 7ebc06558d010e601be594b52cffabe16c369e1d $
 * @since 6.2M1
 */
public class DefaultReaderInputSource implements ReaderInputSource
{
    private final Reader reader;

    private final boolean close;

    public DefaultReaderInputSource(Reader reader)
    {
        this(reader, false);
    }

    /**
     * @since 9.0RC1
     */
    public DefaultReaderInputSource(Reader reader, boolean close)
    {
        this.reader = reader;
        this.close = close;
    }

    @Override
    public boolean restartSupported()
    {
        return false;
    }

    @Override
    public Reader getReader()
    {
        return this.reader;
    }

    @Override
    public void close() throws IOException
    {
        if (this.close) {
            this.reader.close();
        }
    }

    @Override
    public String toString()
    {
        return getReader().toString();
    }
}
