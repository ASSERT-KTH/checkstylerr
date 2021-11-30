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
package org.xwiki.filter;

import java.lang.reflect.ParameterizedType;
import java.util.Map;

import javax.inject.Inject;

import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.component.util.ReflectionUtils;
import org.xwiki.filter.descriptor.DefaultFilterStreamBeanDescriptor;
import org.xwiki.filter.type.FilterStreamType;
import org.xwiki.properties.BeanManager;

/**
 * @param <P> the type of the class containing the parameters of the filter
 * @version $Id: ecf60442a209e2bf2b004bad900ead51d065d405 $
 * @since 6.2M1
 */
public abstract class AbstractBeanFilterStreamFactory<P> extends AbstractFilterStreamFactory implements
    FilterStreamFactory, Initializable
{
    /**
     * The {@link BeanManager} component.
     */
    @Inject
    protected BeanManager beanManager;

    private String name;

    private String description;

    /**
     * Properties bean class used to generate the macro descriptor.
     */
    private Class<P> propertiesBeanClass;

    public AbstractBeanFilterStreamFactory(FilterStreamType type)
    {
        super(type);
    }

    @Override
    public void initialize() throws InitializationException
    {
        // Get bean properties type
        ParameterizedType genericType =
            (ParameterizedType) ReflectionUtils.resolveType(AbstractBeanFilterStreamFactory.class, getClass());
        this.propertiesBeanClass = ReflectionUtils.getTypeClass(genericType.getActualTypeArguments()[0]);

        // Initialize Filter Descriptor.
        DefaultFilterStreamBeanDescriptor descriptor =
            new DefaultFilterStreamBeanDescriptor(getName(), getDescription(),
                this.beanManager.getBeanDescriptor(this.propertiesBeanClass));

        setDescriptor(descriptor);
    }

    protected P createPropertiesBean(Map<String, Object> properties) throws FilterException
    {
        Class<P> beanClass = getPropertiesBeanClass();

        if (beanClass.isInstance(properties)) {
            return (P) properties;
        }

        P parametersBean;
        try {
            parametersBean = beanClass.newInstance();

            this.beanManager.populate(parametersBean, properties);
        } catch (Exception e) {
            throw new FilterException(String.format("Failed to read parameters [%s]", properties), e);
        }

        return parametersBean;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description)
    {
        this.description = description;
    }

    /**
     * @return the description
     */
    public String getDescription()
    {
        return this.description;
    }

    /**
     * @param propertiesBeanClass the parametersBeanClass to set
     */
    public void setPropertiesBeanClass(Class<P> propertiesBeanClass)
    {
        this.propertiesBeanClass = propertiesBeanClass;
    }

    /**
     * @return the properties bean class
     */
    public Class<P> getPropertiesBeanClass()
    {
        return this.propertiesBeanClass;
    }
}
