//
// --------------------------------------------------------------------------
//  Gurux Ltd
// 
//
//
// Filename:        $HeadURL$
//
// Version:         $Revision$,
//                  $Date$
//                  $Author$
//
// Copyright (c) Gurux Ltd
//
//---------------------------------------------------------------------------
//
//  DESCRIPTION
//
// This file is a part of Gurux Device Framework.
//
// Gurux Device Framework is Open Source software; you can redistribute it
// and/or modify it under the terms of the GNU General Public License 
// as published by the Free Software Foundation; version 2 of the License.
// Gurux Device Framework is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of 
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
// See the GNU General Public License for more details.
//
// More information of Gurux products: http://www.gurux.org
//
// This code is licensed under the GNU General Public License v2. 
// Full text may be retrieved at http://www.gnu.org/licenses/gpl-2.0.txt
//---------------------------------------------------------------------------

package gurux.dlms.objects.enums;

/**
 * Enumerates token status codes.<br/>
 * Online help:<br/>
 * http://www.gurux.fi/Gurux.DLMS.Objects.GXDLMSTokenGateway
 */
public enum TokenStatusCode {
    /**
     * Token format result OK.
     */
    FORMAT_OK,
    /**
     * Authentication result OK.
     */
    AUTHENTICATION_OK,
    /**
     * Validation result OK.
     */
    VALIDATION_OK,
    /**
     * Token execution result OK.
     */
    TOKEN_EXECUTION_OK,
    /**
     * Token format failure.
     */
    TOKEN_FORMAT_FAILURE,
    /**
     * Authentication failure.
     */
    AUTHENTICATION_FAILURE,
    /**
     * Validation result failure.
     */
    VALIDATION_RESULT_FAILURE,
    /**
     * Token execution result failure.
     */
    TOKEN_EXECUTION_RESULT_FAILURE,
    /**
     * Token received and not yet processed.
     */
    TOKEN_RECEIVED;

    /**
     * Get integer value for enumerator.
     * 
     * @return Enumerator integer value.
     */
    public int getValue() {
        return this.ordinal();
    }

    /**
     * Returns enumerator value from an integer value.
     * 
     * @param value
     *            Integer value.
     * @return Enumeration value.
     */
    public static TokenStatusCode forValue(int value) {
        return values()[value];
    }
}