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

import java.lang.reflect.ParameterizedType;

import javax.inject.Inject;

import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.component.util.ReflectionUtils;
import org.xwiki.filter.FilterDescriptorManager;
import org.xwiki.filter.FilterException;

/**
 * @param <F> the type of the filter supported by this {@link InputFilterStream}
 * @param <P> the type of the properties bean
 * @version $Id: 3b806608849189449e45b8ec63fd2efb4f2a2e29 $
 * @since 6.2M1
 */
public abstract class AbstractBeanInputFilterStream<P, F> implements BeanInputFilterStream<P>, Initializable
{
    @Inject
    private FilterDescriptorManager filterDescriptorManager;

    protected Class<F> filterType;

    protected P properties;

    public AbstractBeanInputFilterStream()
    {
    }

    public AbstractBeanInputFilterStream(FilterDescriptorManager filterDescriptorManager, P properties)
        throws FilterException
    {
        this.filterDescriptorManager = filterDescriptorManager;
        setProperties(properties);
    }

    @Override
    public void setProperties(P properties) throws FilterException
    {
        this.properties = properties;
    }

    @Override
    public void initialize() throws InitializationException
    {
        // Get the type of the internal filter
        ParameterizedType genericType =
            (ParameterizedType) ReflectionUtils.resolveType(AbstractBeanInputFilterStream.class, getClass());
        this.filterType = ReflectionUtils.getTypeClass(genericType.getActualTypeArguments()[1]);
    }

    @Override
    public void read(Object filter) throws FilterException
    {
        F proxyFilter = this.filterDescriptorManager.createFilterProxy(filter, this.filterType);

        read(filter, proxyFilter);
    }

    protected abstract void read(Object filter, F proxyFilter) throws FilterException;
}
