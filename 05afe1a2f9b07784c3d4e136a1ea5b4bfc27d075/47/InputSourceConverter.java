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
package org.xwiki.filter.internal.input;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.URL;

import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.filter.input.DefaultFileInputSource;
import org.xwiki.filter.input.DefaultInputStreamInputSource;
import org.xwiki.filter.input.DefaultReaderInputSource;
import org.xwiki.filter.input.DefaultURLInputSource;
import org.xwiki.filter.input.InputSource;
import org.xwiki.filter.input.StringInputSource;
import org.xwiki.properties.converter.AbstractConverter;
import org.xwiki.properties.converter.ConversionException;

/**
 * @version $Id: f06c7b285895a29e3331fd35cdcd02ad915f06df $
 * @since 6.2M1
 */
@Component
@Singleton
public class InputSourceConverter extends AbstractConverter<InputSource>
{
    @Override
    protected <G extends InputSource> G convertToType(Type targetType, Object value)
    {
        if (value == null) {
            return null;
        }

        if (value instanceof InputSource) {
            return (G) value;
        }

        InputSource inputSource;

        if (value instanceof InputStream) {
            inputSource = new DefaultInputStreamInputSource((InputStream) value);
        } else if (value instanceof File) {
            inputSource = new DefaultFileInputSource((File) value);
        } else if (value instanceof Reader) {
            inputSource = new DefaultReaderInputSource((Reader) value);
        } else if (value instanceof URL) {
            inputSource = new DefaultURLInputSource((URL) value);
        } else {
            inputSource = fromString(value.toString());
        }

        return (G) inputSource;
    }

    private InputSource fromString(String source)
    {
        InputSource inputSource;

        // TODO: use some InputSourceParser instead to make it extensible
        if (source.startsWith("url:")) {
            try {
                inputSource = new DefaultURLInputSource(new URL(source.substring("url:".length())));
            } catch (Exception e) {
                throw new ConversionException("Failed to create input source for URL [" + source + "]", e);
            }
        } else if (source.startsWith("file:")) {
            inputSource = new DefaultFileInputSource(new File(source.substring("file:".length())));
        } else if (source.startsWith("string:")) {
            inputSource = new DefaultFileInputSource(new File(source.substring("string:".length())));
        } else if (source.startsWith("resource:")) {
            ClassLoader classloader = Thread.currentThread().getContextClassLoader();
            URL resource = classloader.getResource(source.substring("resource:".length()));
            inputSource = new DefaultURLInputSource(resource);
        } else {
            inputSource = new StringInputSource(source);
        }

        return inputSource;
    }
}
