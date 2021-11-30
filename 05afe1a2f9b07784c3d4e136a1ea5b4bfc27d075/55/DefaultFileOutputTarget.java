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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @version $Id: 7a014b0cecd01372df06b6b69a813f84447a95aa $
 * @since 6.2M1
 */
public class DefaultFileOutputTarget extends AbstractOutputStreamOutputTarget implements FileOutputTarget
{
    private File file;

    public DefaultFileOutputTarget(File file)
    {
        this.file = file;
    }

    @Override
    public File getFile()
    {
        return this.file;
    }

    @Override
    protected OutputStream openStream() throws IOException
    {
        return new FileOutputStream(this.file);
    }
}
