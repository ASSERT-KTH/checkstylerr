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
package org.xwiki.filter.output;

import java.io.IOException;
import java.io.Writer;

/**
 * @version $Id: 6b33f3ab5b5ff43d7ca90e252e3b5c02472296c4 $
 * @since 6.2M1
 */
public class DefaultWriterOutputTarget implements WriterOutputTarget
{
    private final Writer writer;

    private final boolean close;

    public DefaultWriterOutputTarget(Writer writer)
    {
        this(writer, false);
    }

    /**
     * @since 9.0RC1
     */
    public DefaultWriterOutputTarget(Writer writer, boolean close)
    {
        this.writer = writer;
        this.close = close;
    }

    @Override
    public boolean restartSupported()
    {
        return false;
    }

    @Override
    public Writer getWriter()
    {
        return this.writer;
    }

    @Override
    public void close() throws IOException
    {
        if (this.close) {
            this.writer.close();
        }
    }

    @Override
    public String toString()
    {
        return getWriter().toString();
    }
}
