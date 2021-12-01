/*
 * Copyright (c) 2009-2011 TIBCO Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.genxdm.bridgekit.axes;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.genxdm.Model;
import org.genxdm.exceptions.PreCondition;


final class IteratorChildAxisElements<N> implements Iterator<N>
{
    private N m_pending;
    private final Model<N> m_navigator;

    public IteratorChildAxisElements(final N origin, final Model<N> navigator)
    {
        m_navigator = PreCondition.assertArgumentNotNull(navigator);
        m_pending = getNextElement(navigator.getFirstChild(origin), navigator);
    }

    public boolean hasNext()
    {
        return (null != m_pending);
    }

    public N next() throws NoSuchElementException
    {
        if (m_pending != null)
        {
            final N last = m_pending;
            m_pending = getNextElement(m_navigator.getNextSibling(m_pending), m_navigator);
            return last;
        }
        else
        {
            // The iteration has no more elements.
            throw new NoSuchElementException();
        }
    }

    public void remove()
    {
        throw new UnsupportedOperationException();
    }

    private static <N> N getNextElement(final N origin, final Model<N> navigator)
    {
        N candidate = origin;
        while (null != candidate)
        {
            if (navigator.isElement(candidate))
            {
                return candidate;
            }
            else
            {
                candidate = navigator.getNextSibling(candidate);
            }
        }
        return null;
    }
}
