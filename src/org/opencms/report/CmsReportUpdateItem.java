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

package org.opencms.report;

/**
 * Represents a single report entry.<p>
 */
public class CmsReportUpdateItem {

    /** Either a string, or an exception. */
    private Object m_message;

    /** String indicating the report type. */
    private CmsReportFormatType m_type;

    /**
     * Creates a new instance.<p>
     *
     * @param type the entry type
     * @param message the message (either a string or an exception)
     */
    public CmsReportUpdateItem(CmsReportFormatType type, Object message) {
        m_type = type;
        m_message = message;
    }

    /**
     * Gets the message.<p>
     *
     * The message is either a string or an exception.
     *
     * @return the message
     */
    public Object getMessage() {

        return m_message;
    }

    /**
     * Gets the entry type.<p>
     *
     * @return the entry type
     */
    public CmsReportFormatType getType() {

        return m_type;
    }

}