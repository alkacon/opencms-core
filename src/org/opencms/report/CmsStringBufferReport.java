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

package org.opencms.report;

import java.util.Locale;

/**
 * Report class used to write the output of a report to a StringBuffer.<p>
 *
 * It stores everything and generates no output.
 * After the report is finished, you can access to result of the
 * report using the {@link #toString()} method.<p>
 *
 * @since 6.0.0
 */
public class CmsStringBufferReport extends A_CmsReport {

    /** The StringBuffer to write to. */
    private StringBuffer m_strBuf;

    /**
     * Constructs a new report using the provided locale for the output language.<p>
     *
     * @param locale the locale to use for the output language
     */
    public CmsStringBufferReport(Locale locale) {

        init(locale, null);

        m_strBuf = new StringBuffer();
    }

    /**
     * @see org.opencms.report.I_CmsReport#getReportUpdate()
     */
    public String getReportUpdate() {

        return "";
    }

    /**
     * @see org.opencms.report.A_CmsReport#print(java.lang.String, int)
     */
    @Override
    public void print(String value, int format) {

        switch (format) {
            case FORMAT_HEADLINE:
            case FORMAT_WARNING:
                addWarning(value);
                m_strBuf.append(value);
                break;
            case FORMAT_ERROR:
                addError(value);
                m_strBuf.append(value);
                break;
            case FORMAT_NOTE:
            case FORMAT_OK:
            case FORMAT_DEFAULT:
            default:
                m_strBuf.append(value);
        }
        setLastEntryTime(System.currentTimeMillis());
    }

    /**
     * @see org.opencms.report.I_CmsReport#println()
     */
    public void println() {

        m_strBuf.append("\n");
    }

    /**
     * @see org.opencms.report.I_CmsReport#println(java.lang.Throwable)
     */
    public void println(Throwable t) {

        print(getMessages().key(Messages.RPT_EXCEPTION_0), FORMAT_WARNING);
        println(t.getMessage(), FORMAT_ERROR);

        StackTraceElement[] stackTrace = t.getStackTrace();
        for (int i = 0; i < stackTrace.length; i++) {
            StackTraceElement element = stackTrace[i];
            println(element.toString());
        }
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return m_strBuf.toString();
    }
}