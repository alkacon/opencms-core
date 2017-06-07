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

package org.opencms.gwt.shared;

import com.google.common.collect.ComparisonChain;
import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * A bean containing a date, both as a 'long' value and a user-readable string representation.<p>
 */
public class CmsClientDateBean implements IsSerializable, Comparable<Object> {

    /** The actual date value. */
    protected long m_date;

    /** The user-readable date string. */
    protected String m_dateText;

    /**
     * Creates a new instance.<p>
     *
     * @param date the date value
     * @param dateText the user-readable date string
     */
    public CmsClientDateBean(long date, String dateText) {

        m_dateText = dateText;
        m_date = date;
    }

    /**
     * Empty default constructor.<p>
     */
    protected CmsClientDateBean() {

    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object other) {

        if (!(other instanceof CmsClientDateBean)) {
            return -1;
        }
        return ComparisonChain.start().compare(m_date, ((CmsClientDateBean)other).m_date).result();
    }

    /**
     * Returns the date.<p>
     *
     * @return the date
     */
    public long getDate() {

        return m_date;
    }

    /**
     * Returns the dateText.<p>
     *
     * @return the dateText
     */
    public String getDateText() {

        return m_dateText;
    }
}
