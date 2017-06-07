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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.workplace.list;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Simple formatter for time.<p>
 *
 * @since 6.0.0
 */
public class CmsListTimeFormatter implements I_CmsListFormatter {

    /** Time style. */
    private int m_timeStyle;

    /**
     * Default constructor.<p>
     *
     * Use medium style.<p>
     */
    public CmsListTimeFormatter() {

        m_timeStyle = DateFormat.MEDIUM;
    }

    /**
     * Customizable constructor.<p>
     *
     * @param timeStyle the style for the time part
     *
     * @see DateFormat
     */
    public CmsListTimeFormatter(int timeStyle) {

        m_timeStyle = timeStyle;
    }

    /**
     * @see org.opencms.workplace.list.I_CmsListFormatter#format(java.lang.Object, java.util.Locale)
     */
    public String format(Object data, Locale locale) {

        if ((data == null) || !(data instanceof Date)) {
            return "";
        }
        DateFormat timeFormat = DateFormat.getTimeInstance(m_timeStyle);
        return timeFormat.format(data);
    }
}
