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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateErrorListener;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;

/**
 * HTML report output to be used for import / export / publish operations
 * in the entire OpenCms system.<p>
 *
 * @since 6.0.0
 */
public class CmsVaadinHtmlReport extends A_CmsReport {

    /**
     * Represents a single report entry.<p>
     */
    class ReportEntry {

        /** Either a string, or an exception. */
        private Object m_message;

        /** String indicating the report type. */
        private String m_type;

        /**
         * Creates a new instance.<p>
         *
         * @param type the entry type
         * @param message the message (either a string or an exception)
         */
        public ReportEntry(String type, Object message) {
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
        public String getType() {

            return m_type;
        }

    }

    /** Format constant for newlines. */
    public static final int FORMAT_NEWLINE = -1;

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsVaadinHtmlReport.class);

    /** The list of report objects e.g. String, CmsPageLink, Exception ... */
    private List<Object> m_content;

    /**
     * Counter to remember what is already shown,
     * indicates the next index of the m_content list that has to be reported.
     */
    private int m_indexNext;

    /** The log report to send output to. */
    private CmsLogReport m_logReport;

    /** The StringTemplate group used by this report. */
    private StringTemplateGroup m_templateGroup;

    /** If set to <code>true</code> nothing is kept in memory. */
    private boolean m_transient;

    /** Boolean flag indicating whether this report should generate HTML or JavaScript output. */
    private boolean m_writeHtml;

    /**
     * Constructs a new report using the provided locale for the output language.<p>
     *
     * @param locale the locale to use for the output language
     * @param siteRoot the site root of the user who started this report (may be <code>null</code>)
     * @param writeHtml if <code>true</code>, this report should generate HTML instead of JavaScript output
     * @param isTransient If set to <code>true</code> nothing is kept in memory
     * @param logChannel the log channel to send the report output to (or null if this shouldn't be done)
     */
    public CmsVaadinHtmlReport(
        Locale locale,
        String siteRoot,
        boolean writeHtml,
        boolean isTransient,
        Object logChannel) {

        init(locale, siteRoot);
        if (logChannel != null) {
            m_logReport = new CmsLogReport(locale, logChannel);
        }
        m_content = new ArrayList<Object>(256);
        m_writeHtml = writeHtml;
        m_transient = isTransient;
        try (InputStream stream = CmsVaadinHtmlReport.class.getResourceAsStream("report.st")) {
            try {
                m_templateGroup = new StringTemplateGroup(
                    new InputStreamReader(stream, "UTF-8"),
                    DefaultTemplateLexer.class,
                    new StringTemplateErrorListener() {

                        @SuppressWarnings("synthetic-access")
                        public void error(String arg0, Throwable arg1) {

                            LOG.error(arg0 + ": " + arg1.getMessage(), arg1);
                        }

                        @SuppressWarnings("synthetic-access")
                        public void warning(String arg0) {

                            LOG.warn(arg0);

                        }
                    });
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } catch (IOException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Constructs a new report using the provided locale for the output language.<p>
     *
     * @param locale the locale to use for the output language
     * @param siteRoot the site root of the user who started this report (may be <code>null</code>)
     * @param logChannel the log channel to send the report output to (or null if this shouldn't be done)
     */
    public CmsVaadinHtmlReport(Locale locale, String siteRoot, Object logChannel) {

        this(locale, siteRoot, false, false, logChannel);
    }

    /**
     * @see org.opencms.report.I_CmsReport#getReportUpdate()
     */
    public synchronized String getReportUpdate() {

        StringBuffer result = new StringBuffer();
        int indexEnd = m_content.size();
        for (int i = m_indexNext; i < indexEnd; i++) {
            int pos = m_transient ? 0 : i;
            Object obj = m_content.get(pos);
            ReportEntry entry = (ReportEntry)obj;

            StringTemplate template = m_templateGroup.getInstanceOf(entry.getType());
            boolean needsParam = template.getFormalArguments().get("message") != null;
            if (needsParam) {
                template.setAttribute("message", entry.getMessage());
            }
            result.append(template.toString());
            if (m_transient) {
                m_content.remove(m_indexNext);
            }
        }
        m_indexNext = m_transient ? 0 : indexEnd;
        return result.toString();
    }

    /**
     * Returns if the report writes html or javascript code.<p>
     *
     * @return <code>true</code> if the report writes html, and <code>false</code> if the report writes javascript code
     */
    public boolean isWriteHtml() {

        return m_writeHtml;
    }

    /**
     * @see org.opencms.report.A_CmsReport#print(java.lang.String, int)
     */
    @Override
    public synchronized void print(String value, int format) {

        if (m_logReport != null) {
            m_logReport.print(value, format);
        }

        String[] names = I_CmsReport.FORMAT_NAMES;
        String formatName = null;
        if (format == FORMAT_NEWLINE) {
            formatName = "NEWLINE";
        } else if (format < names.length) {
            formatName = names[format];
        }
        if (formatName != null) {
            ReportEntry message = new ReportEntry(formatName, value);
            m_content.add(message);
            setLastEntryTime(System.currentTimeMillis());
        }
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
        m_content.add(new ReportEntry("EXCEPTION", t));
        setLastEntryTime(System.currentTimeMillis());
    }

}