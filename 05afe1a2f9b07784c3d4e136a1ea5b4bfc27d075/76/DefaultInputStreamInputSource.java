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

import org.apache.commons.io.input.CloseShieldInputStream;

/**
 * @version $Id: bae7bf4d153918f6de9057274f76bd0e6d851c5e $
 * @since 6.2M1
 */
public class DefaultInputStreamInputSource implements InputStreamInputSource
{
    private final InputStream inputStream;

    private final boolean close;

    public DefaultInputStreamInputSource(InputStream inputStream)
    {
        this(inputStream, false);
    }

    public DefaultInputStreamInputSource(InputStream inputStream, boolean close)
    {
        this.inputStream = inputStream;
        this.close = close;
    }

    @Override
    public boolean restartSupported()
    {
        return false;
    }

    @Override
    public InputStream getInputStream()
    {
        return this.close ? this.inputStream : new CloseShieldInputStream(this.inputStream);
    }

    @Override
    public void close() throws IOException
    {
        if (this.close) {
            this.inputStream.close();
        }
    }

    @Override
    public String toString()
    {
        return getInputStream().toString();
    }
}
