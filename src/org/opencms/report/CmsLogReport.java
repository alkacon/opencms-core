/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/report/CmsLogReport.java,v $
 * Date   : $Date: 2004/08/11 16:52:24 $
 * Version: $Revision: 1.9 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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

import org.opencms.main.OpenCms;
import org.opencms.workplace.I_CmsWpConstants;

import java.util.Locale;

/**
 * Report class used for the logfile.<p>
 * 
 * This prints all messages in the logfile at INFO level.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)  
 * @version $Revision: 1.9 $
 */
public class CmsLogReport extends A_CmsReport {

    /** The buffer to write the log messages to. */
    private StringBuffer m_buffer;

    /** The class name to use for the logger. */
    private Class m_clazz;

    /**
     * Empty default constructor. 
     * 
     * @see java.lang.Object#Object()
     */
    public CmsLogReport() {

        // generate a message object with the default (english) locale
        this(C_BUNDLE_NAME, I_CmsWpConstants.C_DEFAULT_LOCALE);
    }

    /**
     * Constructs a new report using the provided Java class for the logger channel.<p>
     * 
     * @param clazz the the class for the logger channel 
     */
    public CmsLogReport(Class clazz) {

        this(C_BUNDLE_NAME, I_CmsWpConstants.C_DEFAULT_LOCALE, clazz);
    }

    /**
     * Constructs a new report using the provided locale and resource bundle
     * for the output language.<p>
     * 
     * @param locale the locale to use for the report output messages
     * @param bundleName the name of the resource bundle with localized strings
     */
    public CmsLogReport(String bundleName, Locale locale) {

        this(bundleName, locale, CmsLogReport.class);
    }

    /**
     * Constructs a new report using the provided locale and resource bundle
     * for the output language.<p>
     * 
     * @param locale the locale to use for the report output messages
     * @param bundleName the name of the resource bundle with localized strings
     * @param clazz the the class for the logger channel 
     */
    public CmsLogReport(String bundleName, Locale locale, Class clazz) {

        init(locale);
        addBundle(bundleName);
        m_buffer = new StringBuffer();
        if (clazz == null) {
            clazz = CmsLogReport.class;
        }
        m_clazz = clazz;
    }

    /**
     * @see org.opencms.report.I_CmsReport#getReportUpdate()
     */
    public synchronized String getReportUpdate() {

        return "";
    }

    /**
     * @see org.opencms.report.I_CmsReport#print(java.lang.String)
     */
    public synchronized void print(String value) {

        this.print(value, C_FORMAT_DEFAULT);
    }

    /**
     * @see org.opencms.report.I_CmsReport#print(java.lang.String, int)
     */
    public synchronized void print(String value, int format) {

        switch (format) {
            case C_FORMAT_HEADLINE:
                m_buffer.append("[ ");
                m_buffer.append(value);
                m_buffer.append(" ]");
                break;
            case C_FORMAT_WARNING:
                m_buffer.append("!!! ");
                m_buffer.append(value);
                m_buffer.append(" !!!");
                break;
            case C_FORMAT_ERROR:
                m_buffer.append("!!! ");
                m_buffer.append(value);
                m_buffer.append(" !!!");
                addError(value);
                break;
            case C_FORMAT_NOTE:
            case C_FORMAT_OK:
            case C_FORMAT_DEFAULT:
            default:
                m_buffer.append(value);
        }
    }

    /**
     * @see org.opencms.report.I_CmsReport#println()
     */
    public synchronized void println() {

        if (OpenCms.getLog(m_clazz).isInfoEnabled()) {
            OpenCms.getLog(m_clazz).info(m_buffer.toString());
        }
        m_buffer = new StringBuffer();
    }

    /**
     * @see org.opencms.report.I_CmsReport#println(java.lang.String)
     */
    public synchronized void println(String value) {

        this.println(value, C_FORMAT_DEFAULT);
    }

    /**
     * @see org.opencms.report.I_CmsReport#println(java.lang.String, int)
     */
    public synchronized void println(String value, int format) {

        print(value, format);
        println();
    }

    /**
     * @see org.opencms.report.I_CmsReport#println(java.lang.Throwable)
     */
    public synchronized void println(Throwable t) {

        if (OpenCms.getLog(m_clazz).isInfoEnabled()) {
            m_buffer.append(key("report.exception"));
            m_buffer.append(t.getMessage());
            OpenCms.getLog(m_clazz).info(m_buffer.toString(), t);
        }
        m_buffer = new StringBuffer();
    }
}