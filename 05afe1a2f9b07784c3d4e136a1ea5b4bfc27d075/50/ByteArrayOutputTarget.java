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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.lang3.ArrayUtils;

/**
 * @version $Id: 09089b4bbe3c68d5ff74ffb51eb9064c9255108b $
 * @since 6.2M1
 */
public class ByteArrayOutputTarget extends AbstractOutputStreamOutputTarget
{
    @Override
    protected OutputStream openStream() throws IOException
    {
        return new ByteArrayOutputStream();
    }

    @Override
    public void close() throws IOException
    {
        // Useless
    }

    public byte[] toByteArray()
    {
        return this.outputStream != null ? ((ByteArrayOutputStream) this.outputStream).toByteArray()
            : ArrayUtils.EMPTY_BYTE_ARRAY;
    }
}
