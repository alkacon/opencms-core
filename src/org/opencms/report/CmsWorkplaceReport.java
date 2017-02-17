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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;

import com.google.common.collect.Lists;

/**
 * Report class for displaying reports to the user in the workplace.<p>
 *
 * The difference to the older CmsHtmlReport class is that this can be used from both the old and the new Vaadin-based workplace, without
 * having to decide when the report is created. Instead, the 'client' requesting report updates has to pass in a report update formatter instance,
 * which is then used to format the report update for each case.
 *
 */
public class CmsWorkplaceReport extends A_CmsReport {

    /** Format constant for newlines. */
    public static final int FORMAT_NEWLINE = -1;

    /** Logger instance for this class. */
    @SuppressWarnings("unused")
    private static final Log LOG = CmsLog.getLog(CmsWorkplaceReport.class);

    /** The list of report objects e.g. String, CmsPageLink, Exception ... */
    private List<Object> m_content;

    /**
     * Counter to remember what is already shown,
     * indicates the next index of the m_content list that has to be reported.
     */
    private int m_indexNext;

    /** The log report to send output to. */
    private CmsLogReport m_logReport;

    /** If set to <code>true</code> nothing is kept in memory. */
    private boolean m_transient;

    /**
     * Constructs a new report using the provided locale for the output language.<p>
     *
     * @param locale the locale to use for the output language
     * @param siteRoot the site root of the user who started this report (may be <code>null</code>)
     * @param isTransient If set to <code>true</code> nothing is kept in memory
     * @param logChannel the log channel to send the report output to (or null if this shouldn't be done)
     */
    public CmsWorkplaceReport(Locale locale, String siteRoot, boolean isTransient, Object logChannel) {

        init(locale, siteRoot);
        if (logChannel != null) {
            m_logReport = new CmsLogReport(locale, logChannel);
        }
        m_content = new ArrayList<Object>(256);
        m_transient = isTransient;

    }

    /**
     * Constructs a new report using the provided locale for the output language.<p>
     *
     * @param locale the locale to use for the output language
     * @param siteRoot the site root of the user who started this report (may be <code>null</code>)
     * @param logChannel the log channel to send the report output to (or null if this shouldn't be done)
     */
    public CmsWorkplaceReport(Locale locale, String siteRoot, Object logChannel) {

        this(locale, siteRoot, false, logChannel);
    }

    /**
     * @see org.opencms.report.I_CmsReport#getReportUpdate()
     */
    public synchronized String getReportUpdate() {

        return getReportUpdate(new CmsClassicJavascriptReportUpdateFormatter(getLocale()));

    }

    /**
     * @see org.opencms.report.A_CmsReport#getReportUpdate(org.opencms.report.I_CmsReportUpdateFormatter)
     */
    @Override
    public synchronized String getReportUpdate(I_CmsReportUpdateFormatter formatter) {

        int indexEnd = m_content.size();
        List<CmsReportUpdateItem> itemsToFormat = Lists.newArrayList();
        for (int i = m_indexNext; i < indexEnd; i++) {
            int pos = m_transient ? 0 : i;
            Object obj = m_content.get(pos);
            CmsReportUpdateItem entry = (CmsReportUpdateItem)obj;
            itemsToFormat.add(entry);
            if (m_transient) {
                m_content.remove(m_indexNext);
            }
        }
        String result = formatter.formatReportUpdate(itemsToFormat);
        m_indexNext = m_transient ? 0 : indexEnd;
        return result;
    }

    /**
     * @see org.opencms.report.A_CmsReport#print(java.lang.String, int)
     */
    @Override
    public synchronized void print(String value, int format) {

        if (m_logReport != null) {
            m_logReport.print(value, format);
        }
        if (format == I_CmsReport.FORMAT_ERROR) {
            addError(value);
        } else if (format == I_CmsReport.FORMAT_WARNING) {
            addWarning(value);
        }
        CmsReportUpdateItem message = new CmsReportUpdateItem(CmsReportFormatType.byId(format), value);
        m_content.add(message);
        setLastEntryTime(System.currentTimeMillis());
    }

    /**
     * @see org.opencms.report.I_CmsReport#println()
     */
    public void println() {

        if (m_logReport != null) {
            m_logReport.println();
        }
        print("", FORMAT_NEWLINE);
    }

    /**
     * @see org.opencms.report.I_CmsReport#println(java.lang.Throwable)
     */
    public synchronized void println(Throwable t) {

        if (m_logReport != null) {
            m_logReport.println(t);
        }
        addError(t.getMessage());
        m_content.add(new CmsReportUpdateItem(CmsReportFormatType.fmtException, t));
        setLastEntryTime(System.currentTimeMillis());
    }

}