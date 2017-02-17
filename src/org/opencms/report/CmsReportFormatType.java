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

import java.util.Map;

import com.google.common.collect.Maps;

/**
 * Enum representing report entry types.<p>
 */
public enum CmsReportFormatType {
    /** Default format. */
    fmtDefault("FORMAT_DEFAULT", I_CmsReport.FORMAT_DEFAULT),

    /** Error format. */
    fmtError("FORMAT_ERROR", I_CmsReport.FORMAT_ERROR),

    /** Exception format. */
    fmtException("EXCEPTION", -2),

    /** Headline format. */
    fmtHeadline("FORMAT_HEADLINE", I_CmsReport.FORMAT_HEADLINE),

    /** Newline format. */
    fmtNewline("NEWLINE", -1),

    /** Note format. */
    fmtNote("FORMAT_NOTE", I_CmsReport.FORMAT_NOTE),

    /** 'OK' format. */
    fmtOk("FORMAT_OK", I_CmsReport.FORMAT_OK),

    /** Warning format. */
    fmtWarning("FORMAT_WARNING", I_CmsReport.FORMAT_WARNING);

    /** Enum values by id. */
    private static Map<Integer, CmsReportFormatType> m_byId = Maps.newHashMap();

    /** Enum values by format name. */
    private static Map<String, CmsReportFormatType> m_byName = Maps.newHashMap();

    /** The format id. */
    private int m_id;

    /** The format name. */
    private String m_name;

    /**
     * Creates a new instance.<p>
     *
     * @param formatName the format name
     * @param formatId the format id
     */
    private CmsReportFormatType(String formatName, int formatId) {
        m_name = formatName;
        m_id = formatId;
    }

    static {
        for (CmsReportFormatType type : values()) {
            m_byId.put(Integer.valueOf(type.getFormatId()), type);
            m_byName.put(type.getFormatName(), type);
        }
    }

    /**
     * Gets the format enum by its id.<p>
     *
     * @param id the format id
     * @return the format enum
     */
    public static CmsReportFormatType byId(int id) {

        return m_byId.get(Integer.valueOf(id));
    }

    /**
     * Gets the format enum by its format naem.<p>
     *
     * @param name the format name
     * @return the format enum value
     */
    public static CmsReportFormatType byName(String name) {

        return m_byName.get(name);
    }

    /**
     * Gets the format id.<p>
     *
     * @return the format id
     */
    public int getFormatId() {

        return m_id;
    }

    /**
     * Gets the format name.<p>
     *
     * @return the format name
     */
    public String getFormatName() {

        return m_name;
    }

}
