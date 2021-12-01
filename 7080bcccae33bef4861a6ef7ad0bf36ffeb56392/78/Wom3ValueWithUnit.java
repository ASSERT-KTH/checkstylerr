/**
 * Copyright 2011 The Open Source Research Group,
 *                University of Erlangen-Nürnberg
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package org.sweble.wom3;

import java.io.Serializable;

/**
 * An HTML value associated with its unit of measurement.
 */
public interface Wom3ValueWithUnit
		extends
			Cloneable,
			Serializable
{
	/**
	 * Get the unit of measurement of this value.
	 * 
	 * @return The unit.
	 */
	public Wom3Unit getUnit();

	/**
	 * Get the actual value.
	 * 
	 * @return The value.
	 */
	public float getValue();

	/**
	 * Get the actual value rounded to an integer.
	 * 
	 * @return The value as integer.
	 */
	public int getIntValue();
}
