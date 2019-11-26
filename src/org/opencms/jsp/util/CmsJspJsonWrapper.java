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

import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;

/**
 * Wrapper for accessing JSON in JSPs.
 */
public class CmsJspJsonWrapper {

    /** The wrapped value. */
    private Object m_value;

    /**
     * Creates a new wrapper.
     *
     * @param value the value to wrap
     */
    public CmsJspJsonWrapper(Object value) {

        m_value = value;
    }

    /**
     * Gets unindented, single line JSON text for the wrapped value.
     *
     * @return the JSON text
     */
    public String getCompact() {

        try {
            return JSONObject.valueToString(m_value);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets JSON text in indented format.
     *
     * @return the indented JSON
     */
    public String getPretty() {

        try {
            return JSONObject.valueToString(m_value, 4, 0);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return getCompact();
    }

}
