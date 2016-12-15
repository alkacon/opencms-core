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

import org.opencms.main.CmsLog;

import java.util.Locale;

/**
 * Report class used for the logfile.<p>
 *
 * This prints all messages in the logfile at INFO level.<p>
 *
 * @since 6.0.0
 */
public class CmsLogReport extends A_CmsReport {

    /** The buffer to write the log messages to. */
    private StringBuffer m_buffer;

    /** The class name to use for the logger. */
    private Object m_channel;

    /**
     * Constructs a new report using the provided locale for the output language,
     * using the provided Java class for the log channel.<p>
     *
     * @param locale the locale to use for the report output messages
     * @param channel the log channel
     */
    public CmsLogReport(Locale locale, Class<?> channel) {
        this(locale, (Object)channel);

    }

    /**
     * Constructs a new report using the provided locale for the output language,
     * using the provided Java class for the log channel.<p>
     *
     * @param locale the locale to use for the report output messages
     * @param channel the log channel (usually a string with the package name, or a class)
     */
    public CmsLogReport(Locale locale, Object channel) {

        init(locale, null);
        m_buffer = new StringBuffer();
        if (channel == null) {
            channel = CmsLogReport.class;
        }
        m_channel = channel;
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
    public synchronized void print(String value, int format) {

        switch (format) {
            case FORMAT_HEADLINE:
                m_buffer.append("[ ");
                m_buffer.append(value);
                m_buffer.append(" ]");
                break;
            case FORMAT_WARNING:
                m_buffer.append("!!! ");
                m_buffer.append(value);
                m_buffer.append(" !!!");
                addWarning(value);
                break;
            case FORMAT_ERROR:
                m_buffer.append("!!! ");
                m_buffer.append(value);
                m_buffer.append(" !!!");
                addError(value);
                break;
            case FORMAT_NOTE:
            case FORMAT_OK:
            case FORMAT_DEFAULT:
            default:
                m_buffer.append(value);
        }
        setLastEntryTime(System.currentTimeMillis());
    }

    /**
     * @see org.opencms.report.I_CmsReport#println()
     */
    public synchronized void println() {

        if (CmsLog.getLog(m_channel).isInfoEnabled()) {
            CmsLog.getLog(m_channel).info(m_buffer.toString());
        }
        m_buffer = new StringBuffer();
        setLastEntryTime(System.currentTimeMillis());
    }

    /**
     * @see org.opencms.report.I_CmsReport#println(java.lang.Throwable)
     */
    public synchronized void println(Throwable t) {

        if (CmsLog.getLog(m_channel).isInfoEnabled()) {
            StringBuffer message = new StringBuffer();
            message.append(getMessages().key(Messages.RPT_EXCEPTION_0));
            message.append(t.getMessage());
            m_buffer.append(message);
            addError(message.toString());
            CmsLog.getLog(m_channel).info(m_buffer.toString(), t);
        }
        m_buffer = new StringBuffer();
        setLastEntryTime(System.currentTimeMillis());
    }
}