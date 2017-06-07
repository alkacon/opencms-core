/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.jsp.util;

import java.util.Date;

/**
 * Common value wrapper class for XML content values and element setting values.<p>
 */
abstract class A_CmsJspValueWrapper {

    /**
     * Returns if the value has been configured.<p>
     *
     * @return <code>true</code> if the value has been configured
     */
    public abstract boolean getExists();

    /**
     * Returns <code>true</code> in case the value is empty, that is either <code>null</code> or an empty String.<p>
     *
     * @return <code>true</code> in case the value is empty
     */
    public abstract boolean getIsEmpty();

    /**
     * Returns <code>true</code> in case the value is empty or whitespace only,
     * that is either <code>null</code> or String that contains only whitespace chars.<p>
     *
     * @return <code>true</code> in case the value is empty or whitespace only
     */
    public abstract boolean getIsEmptyOrWhitespaceOnly();

    /**
     * Returns <code>true</code> in case the value exists and is not empty.<p>
     *
     * @return <code>true</code> in case the value exists and is not empty
     */
    public abstract boolean getIsSet();

    /**
     * Parses the value to boolean.<p>
     *
     * @return the boolean value
     */
    public boolean getToBoolean() {

        return Boolean.parseBoolean(getToString());
    }

    /**
     * Converts a time stamp to a date.<p>
     *
     * @return the date
     */
    public Date getToDate() {

        return CmsJspElFunctions.convertDate(getToString());
    }

    /**
     * Parses the value to a Double.<p>
     *
     * @return the Double value
     */
    public Double getToFloat() {

        return new Double(Double.parseDouble(getToString()));
    }

    /**
     * Parses the value to a Long.<p>
     *
     * @return the Long value
     */
    public Long getToInteger() {

        return new Long(Long.parseLong(getToString()));
    }

    /**
     * Returns the string value.<p>
     *
     * @return the string value
     */
    public String getToString() {

        return toString();
    }
}
