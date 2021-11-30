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
package org.xwiki.filter.internal.output;

import java.io.File;
import java.io.OutputStream;
import java.io.Writer;
import java.lang.reflect.Type;

import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.filter.output.DefaultFileOutputTarget;
import org.xwiki.filter.output.DefaultOutputStreamOutputTarget;
import org.xwiki.filter.output.DefaultWriterOutputTarget;
import org.xwiki.filter.output.OutputTarget;
import org.xwiki.filter.output.StringWriterOutputTarget;
import org.xwiki.properties.converter.AbstractConverter;

/**
 * @version $Id: 70661a45286ddf903cb568b60051db0357594ed9 $
 * @since 6.2M1
 */
@Component
@Singleton
public class OutputTargetConverter extends AbstractConverter<OutputTarget>
{
    @Override
    protected <G extends OutputTarget> G convertToType(Type targetType, Object value)
    {
        if (value == null) {
            return null;
        }

        if (value instanceof OutputTarget) {
            return (G) value;
        }

        OutputTarget outputTarget;

        if (value instanceof OutputStream) {
            outputTarget = new DefaultOutputStreamOutputTarget((OutputStream) value);
        } else if (value instanceof File) {
            outputTarget = new DefaultFileOutputTarget((File) value);
        } else if (value instanceof Writer) {
            outputTarget = new DefaultWriterOutputTarget((Writer) value);
        } else {
            outputTarget = fromString(value.toString());
        }

        return (G) outputTarget;
    }

    private OutputTarget fromString(String target)
    {
        OutputTarget outputTarget;

        // TODO: use some OutputTargetParser instead to make it extensible
        if (target.startsWith("file:")) {
            outputTarget = new DefaultFileOutputTarget(new File(target.substring("file:".length())));
        } else {
            outputTarget = new StringWriterOutputTarget();
        }

        return outputTarget;
    }
}
