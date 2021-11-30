/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

/**
 * SqlTime.java
 *
 */

package com.sun.jdo.spi.persistence.support.sqlstore.sco;

import java.io.ObjectStreamException;

import com.sun.jdo.spi.persistence.support.sqlstore.PersistenceCapable;
import com.sun.jdo.spi.persistence.support.sqlstore.StateManager;
import com.sun.jdo.spi.persistence.support.sqlstore.SCO;
import com.sun.jdo.spi.persistence.support.sqlstore.SCODate;
import com.sun.jdo.spi.persistence.support.sqlstore.PersistenceManager;

/**
 * A mutable 2nd class object date.
 * @author Marina Vatkina
 * @version 1.0
 * @see     java.sql.Time
 */
public class SqlTime
    extends java.sql.Time
    implements SCODate
{
    private transient PersistenceCapable owner;

    private transient String fieldName;

    /**
     * Creates a <code>SqlTime</code> object that represents the time at which
     * it was allocated. Assigns owning object and field name
     * @param owner the owning object
     * @param fieldName the owning field name
     */
    public SqlTime(Object owner, String fieldName)
    {
    super(0);
    if (owner instanceof PersistenceCapable)
        {
                this.owner = (PersistenceCapable)owner;
        this.fieldName = fieldName;
        }
    }

    /**
     * Creates a <code>SqlTime</code> object that represents the given time
     * in milliseconds. Assigns owning object and field name
     * @param owner     the owning object
     * @param fieldName the owning field name
     * @param date     the number of milliseconds
     */
    public SqlTime(Object owner, String fieldName, long date)
    {
    super(date);
    if (owner instanceof PersistenceCapable)
        {
                this.owner = (PersistenceCapable)owner;
        this.fieldName = fieldName;
        }
    }

    /**
     * Sets the <tt>SqlTime</tt> object to represent a point in time that is
     * <tt>time</tt> milliseconds after January 1, 1970 00:00:00 GMT.
     *
     * @param   time   the number of milliseconds.
     * @see     java.sql.Time
     */
    public void setTime(long time) {
    this.makeDirty();
    super.setTime(time);
    }

    /**
     * Creates and returns a copy of this object.
     *
     * <P>Mutable Second Class Objects are required to provide a public
     * clone method in order to allow for copying PersistenceCapable
     * objects. In contrast to Object.clone(), this method must not throw a
     * CloneNotSupportedException.
     */
    public Object clone()
    {
        SqlTime obj = (SqlTime) super.clone();

        obj.owner = null;
        obj.fieldName = null;

        return obj;
    }

    /** -----------Depricated Methods------------------*/

    /**
     * Sets the hour of this <tt>SqlTime</tt> object to the specified value.
     *
     * @param   hours   the hour value.
     * @see     java.util.Calendar
     * @see     java.sql.Time
     * @deprecated As of JDK version 1.1,
     * replaced by <code>Calendar.set(Calendar.HOUR_OF_DAY, int hours)</code>.
     */
    public void setHours(int hours) {
    this.makeDirty();
        super.setHours(hours);
    }

    /**
     * Sets the minutes of this <tt>SqlTime</tt> object to the specified value.
     *
     * @param   minutes   the value of the minutes.
     * @see     java.util.Calendar
     * @see     java.sql.Time
     * @deprecated As of JDK version 1.1,
     * replaced by <code>Calendar.set(Calendar.MINUTE, int minutes)</code>.
     */
    public void setMinutes(int minutes) {
    this.makeDirty();
        super.setMinutes(minutes);
    }

    /**
     * Sets the seconds of this <tt>SqlTime</tt> to the specified value.
     *
     * @param   seconds   the seconds value.
     * @see     java.util.Calendar
     * @see     java.sql.Time
     * @deprecated As of JDK version 1.1,
     * replaced by <code>Calendar.set(Calendar.SECOND, int seconds)</code>.
     */
    public void setSeconds(int seconds) {
    this.makeDirty();
        super.setSeconds(seconds);
    }

    /** ---------------- internal methods ------------------- */

    /**
     * Creates and returns a copy of this object without resetting the owner and field value.
     *
     */
    public Object cloneInternal()
    {
        return super.clone();
    }

    /**
     * Sets the <tt>SqlTime</tt> object without notification of the Owner
     * field. Used internaly to populate date from DB
     *
     * @param   time   the number of milliseconds.
     * @see     java.sql.Time
     */
    public void setTimeInternal(long time) {
    super.setTime(time);
    }

    /**
     * Nullifies references to the owner Object and Field
     * NOTE: This method should be called under the locking of
     * the owener' state manager.
     */
    public void unsetOwner()
    {
        this.owner = null;
        this.fieldName = null;
    }

    /**
     * Returns the owner object of the SCO instance
     *
     * @return owner object
     */
    public Object getOwner()
    {
        return this.owner;
    }

    /**
     * Returns the field name
     *
     * @return field name as java.lang.String
     */
    public String getFieldName()
    {
        return this.fieldName;
    }

    /**
     * Marks object dirty
     */
    public StateManager makeDirty()
    {
        if (owner != null)
        {
            StateManager stateManager = owner.jdoGetStateManager();

            if (stateManager != null)
            {
                PersistenceManager pm = (PersistenceManager) stateManager.getPersistenceManagerInternal();

                pm.acquireShareLock();

                try
                {
                    synchronized (stateManager)
                    {
                        //
                        // Need to recheck owner because it could be set to
                        // null before we lock the stateManager.
                        //
                        if (owner != null)
                        {
                            stateManager.makeDirty(fieldName);
                            return stateManager;
                        }
                    }
                }
                finally
                {
                    pm.releaseShareLock();
                }
            }
        }
        return null;
     }

    /**
     * Apply changes (no-op)
     */
    public void applyUpdates(StateManager sm, boolean modified)
    {
    }

    /**
     * Use java.sql.Time as the designated object to be used when writing
     * this object to the stream.
     *
     * @return java.sql.Time that represents the same value.
     * @throws ObjectStreamException.
     */
    Object writeReplace() throws ObjectStreamException
    {
        return new java.sql.Time(getTime());
    }
}
