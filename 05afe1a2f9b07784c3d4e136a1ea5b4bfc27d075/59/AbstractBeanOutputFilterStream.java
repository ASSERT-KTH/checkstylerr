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

import org.xwiki.filter.FilterException;

/**
 * @param <P> the type of the properties bean
 * @version $Id: c7bad9ee2ecd6c5e8ab09f71b605e5bb9fd03764 $
 * @since 6.2M1
 */
public abstract class AbstractBeanOutputFilterStream<P> implements BeanOutputFilterStream<P>
{
    protected P properties;

    protected Object filter;

    public AbstractBeanOutputFilterStream()
    {

    }

    public AbstractBeanOutputFilterStream(P properties) throws FilterException
    {
        setProperties(properties);
    }

    @Override
    public void setProperties(P properties) throws FilterException
    {
        this.properties = properties;
    }

    @Override
    public Object getFilter() throws FilterException
    {
        if (this.filter == null) {
            this.filter = createFilter();
        }

        return this.filter;
    }

    protected Object createFilter() throws FilterException
    {
        return this;
    }
}
