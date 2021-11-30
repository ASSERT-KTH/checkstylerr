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
package org.xwiki.extension.repository.aether.internal.components;

import org.codehaus.plexus.logging.AbstractLoggerManager;
import org.slf4j.Logger;

/**
 * @version $Id: 7225188805dcfa2d7435e33315b424a57a2d545c $
 * @since 4.0M1
 */
public class XWikiLoggerManager extends AbstractLoggerManager
{
    private XWikiLogger logger;

    public XWikiLoggerManager(Logger logger)
    {
        this.logger = new XWikiLogger(logger);
    }

    @Override
    public int getActiveLoggerCount()
    {
        return 1;
    }

    @Override
    public org.codehaus.plexus.logging.Logger getLoggerForComponent(String arg0, String arg1)
    {
        return this.logger;
    }

    @Override
    public int getThreshold()
    {
        return this.logger.getThreshold();
    }

    @Override
    public void returnComponentLogger(String arg0, String arg1)
    {
    }

    @Override
    public void setThreshold(int arg0)
    {
    }

    @Override
    public void setThresholds(int arg0)
    {
    }
}
