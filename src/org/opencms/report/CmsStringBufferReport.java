/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/report/CmsStringBufferReport.java,v $
 * Date   : $Date: 2004/04/01 04:48:02 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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

import java.util.Locale;

/**
 * Report class used to write the output of a report to a StringBuffer.<p>
 * 
 * It stores everything and generates no output. 
 * After the report is finished, you can access to result of the
 * report using the {@link #toString()} method.<p>
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.2 $ $Date: 2004/04/01 04:48:02 $
 */
public class CmsStringBufferReport extends A_CmsReport {

    private StringBuffer m_strBuf;

    /**
     * Creates a new string buffer report.<p>
     * 
     * @param locale the locale to use for the report output messages 
     */
    public CmsStringBufferReport(Locale locale) {

        this(C_BUNDLE_NAME, locale);
    }

    /**
     * Constructs a new string buffer report using the specified locale and resource bundle
     * for the output language.<p>
     * 
     * @param locale the locale to use for the report output messages
     * @param bundleName the name of the resource bundle with localized strings
     */
    public CmsStringBufferReport(String bundleName, Locale locale) {

        init();
        addBundle(bundleName, locale);

        m_strBuf = new StringBuffer();
    }

    /**
     * @see org.opencms.report.I_CmsReport#getReportUpdate()
     */
    public String getReportUpdate() {

        return "";
    }

    /**
     * @see org.opencms.report.I_CmsReport#print(java.lang.String)
     */
    public void print(String value) {

        print(value, C_FORMAT_DEFAULT);
    }

    /**
     * @see org.opencms.report.I_CmsReport#print(java.lang.String, int)
     */
    public void print(String value, int format) {

        switch (format) {
            case C_FORMAT_HEADLINE:
            case C_FORMAT_WARNING:
            case C_FORMAT_NOTE:
            case C_FORMAT_OK:
            case C_FORMAT_DEFAULT:
            default:
                m_strBuf.append(value);
        }
    }

    /**
     * @see org.opencms.report.I_CmsReport#println()
     */
    public void println() {

        m_strBuf.append("\n");
    }

    /**
     * @see org.opencms.report.I_CmsReport#println(java.lang.String)
     */
    public void println(String value) {

        println(value, C_FORMAT_DEFAULT);
    }

    /**
     * @see org.opencms.report.I_CmsReport#println(java.lang.String, int)
     */
    public void println(String value, int format) {

        switch (format) {
            case C_FORMAT_HEADLINE:
            case C_FORMAT_WARNING:
            case C_FORMAT_NOTE:
            case C_FORMAT_OK:
            case C_FORMAT_DEFAULT:
            default:
                m_strBuf.append(value).append("\n");
        }
    }

    /**
     * @see org.opencms.report.I_CmsReport#println(java.lang.Throwable)
     */
    public void println(Throwable t) {

        print(key("report.exception"), C_FORMAT_WARNING);
        println(t.getMessage(), C_FORMAT_WARNING);

        StackTraceElement[] stackTrace = t.getStackTrace();
        for (int i = 0; i < stackTrace.length; i++) {
            StackTraceElement element = stackTrace[i];
            println(element.toString());
        }
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {

        return m_strBuf.toString();
    }

}