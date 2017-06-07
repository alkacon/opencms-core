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

import org.opencms.i18n.CmsEncoder;
import org.opencms.main.CmsException;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

/**
 * HTML report output to be used for import / export / publish operations
 * in the entire OpenCms system.<p>
 *
 * @since 6.0.0
 */
public class CmsHtmlReport extends A_CmsReport {

    /** Constant for a HTML linebreak with added "real" line break. */
    protected static final String LINEBREAK = "<br>";

    /**
     * Constant for a HTML linebreak with added "real" line break-
     * traditional style for report threads that still use XML templates for their output.
     */
    protected static final String LINEBREAK_TRADITIONAL = "<br>\\n";

    /** The list of report objects e.g. String, CmsPageLink, Exception ... */
    private List<Object> m_content;

    /**
     * Counter to remember what is already shown,
     * indicates the next index of the m_content list that has to be reported.
     */
    private int m_indexNext;

    /** Flag to indicate if an exception should be displayed long or short. */
    private boolean m_showExceptionStackTrace;

    /** If set to <code>true</code> nothing is kept in memory. */
    private boolean m_transient;

    /** Boolean flag indicating whether this report should generate HTML or JavaScript output. */
    private boolean m_writeHtml;

    /**
     * Constructs a new report using the provided locale for the output language.<p>
     *
     * @param locale the locale to use for the output language
     * @param siteRoot the site root of the user who started this report (may be <code>null</code>)
     */
    public CmsHtmlReport(Locale locale, String siteRoot) {

        this(locale, siteRoot, false, false);
    }

    /**
     * Constructs a new report using the provided locale for the output language.<p>
     *
     * @param locale the locale to use for the output language
     * @param siteRoot the site root of the user who started this report (may be <code>null</code>)
     * @param writeHtml if <code>true</code>, this report should generate HTML instead of JavaScript output
     * @param isTransient If set to <code>true</code> nothing is kept in memory
     */
    public CmsHtmlReport(Locale locale, String siteRoot, boolean writeHtml, boolean isTransient) {

        init(locale, siteRoot);
        m_content = new ArrayList<Object>(256);
        m_showExceptionStackTrace = true;
        m_writeHtml = writeHtml;
        m_transient = isTransient;
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
            if ((obj instanceof String) || (obj instanceof StringBuffer)) {
                result.append(obj);
            } else if (obj instanceof Throwable) {
                result.append(getExceptionElement((Throwable)obj));
            }
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

        StringBuffer buf = null;

        if (!m_writeHtml) {
            value = CmsStringUtil.escapeJavaScript(value);
            switch (format) {
                case FORMAT_HEADLINE:
                    buf = new StringBuffer();
                    buf.append("aH('");
                    buf.append(value);
                    buf.append("'); ");
                    break;
                case FORMAT_WARNING:
                    buf = new StringBuffer();
                    buf.append("aW('");
                    buf.append(value);
                    buf.append("'); ");
                    addWarning(value);
                    break;
                case FORMAT_ERROR:
                    buf = new StringBuffer();
                    buf.append("aE('");
                    buf.append(value);
                    buf.append("'); ");
                    addError(value);
                    break;
                case FORMAT_NOTE:
                    buf = new StringBuffer();
                    buf.append("aN('");
                    buf.append(value);
                    buf.append("'); ");
                    break;
                case FORMAT_OK:
                    buf = new StringBuffer();
                    buf.append("aO('");
                    buf.append(value);
                    buf.append("'); ");
                    break;
                case FORMAT_DEFAULT:
                default:
                    buf = new StringBuffer();
                    buf.append("a('");
                    buf.append(value);
                    buf.append("'); ");
            }
            // the output lines get split back into single lines on the client-side.
            // thus, a separate JavaScript call has to be added here to tell the
            // client that we want a linebreak here...
            if (value.trim().endsWith(getLineBreak())) {
                buf.append("aB(); ");
            }
            m_content.add(buf.toString());
        } else {
            switch (format) {
                case FORMAT_HEADLINE:
                    buf = new StringBuffer();
                    buf.append("<span class='head'>");
                    buf.append(value);
                    buf.append("</span>");
                    break;
                case FORMAT_WARNING:
                    buf = new StringBuffer();
                    buf.append("<span class='warn'>");
                    buf.append(value);
                    buf.append("</span>");
                    addWarning(value);
                    break;
                case FORMAT_ERROR:
                    buf = new StringBuffer();
                    buf.append("<span class='err'>");
                    buf.append(value);
                    buf.append("</span>");
                    addError(value);
                    break;
                case FORMAT_NOTE:
                    buf = new StringBuffer();
                    buf.append("<span class='note'>");
                    buf.append(value);
                    buf.append("</span>");
                    break;
                case FORMAT_OK:
                    buf = new StringBuffer();
                    buf.append("<span class='ok'>");
                    buf.append(value);
                    buf.append("</span>");
                    break;
                case FORMAT_DEFAULT:
                default:
                    buf = new StringBuffer(value);
            }
            if (value.trim().endsWith(getLineBreak())) {
                buf.append("\n");
            }
            m_content.add(buf.toString());
        }
        setLastEntryTime(System.currentTimeMillis());
    }

    /**
     * @see org.opencms.report.I_CmsReport#println()
     */
    public void println() {

        print(getLineBreak());
    }

    /**
     * @see org.opencms.report.I_CmsReport#println(java.lang.Throwable)
     */
    public synchronized void println(Throwable t) {

        addError(t.getMessage());
        m_content.add(t);
        setLastEntryTime(System.currentTimeMillis());
    }

    /**
     * Returns the correct line break notation depending on the output style of this report.
     *
     * @return the correct line break notation
     */
    protected String getLineBreak() {

        return m_writeHtml ? LINEBREAK_TRADITIONAL : LINEBREAK;
    }

    /**
     * Output helper method to format a reported {@link Throwable} element.<p>
     *
     * This method ensures that exception stack traces are properly escaped
     * when they are added to the report.<p>
     *
     * There is a member variable {@link #m_showExceptionStackTrace} in this
     * class that controls if the stack track is shown or not.
     * In a later version this might be configurable on a per-user basis.<p>
     *
     * @param throwable the exception to format
     *
     * @return the formatted StringBuffer
     */
    private StringBuffer getExceptionElement(Throwable throwable) {

        StringBuffer buf = new StringBuffer(256);

        if (!m_writeHtml) {
            if (m_showExceptionStackTrace) {
                buf.append("aT('");
                buf.append(getMessages().key(Messages.RPT_EXCEPTION_0));
                String exception = CmsEncoder.escapeXml(CmsException.getStackTraceAsString(throwable));
                StringBuffer excBuffer = new StringBuffer(exception.length() + 50);
                StringTokenizer tok = new StringTokenizer(exception, "\r\n");
                while (tok.hasMoreTokens()) {
                    excBuffer.append(tok.nextToken());
                    excBuffer.append(getLineBreak());
                }
                buf.append(CmsStringUtil.escapeJavaScript(excBuffer.toString()));
                buf.append("'); ");
            } else {
                buf.append("aT('");
                buf.append(getMessages().key(Messages.RPT_EXCEPTION_0));
                buf.append(CmsStringUtil.escapeJavaScript(throwable.toString()));
                buf.append("'); ");
            }
            m_content.add(buf);
        } else {
            if (m_showExceptionStackTrace) {
                buf.append("<span class='throw'>");
                buf.append(getMessages().key(Messages.RPT_EXCEPTION_0));
                String exception = CmsEncoder.escapeXml(CmsException.getStackTraceAsString(throwable));
                StringBuffer excBuffer = new StringBuffer(exception.length() + 50);
                StringTokenizer tok = new StringTokenizer(exception, "\r\n");
                while (tok.hasMoreTokens()) {
                    excBuffer.append(tok.nextToken());
                    excBuffer.append(getLineBreak());
                }
                buf.append(excBuffer.toString());
                buf.append("</span>");
            } else {
                buf.append("<span class='throw'>");
                buf.append(getMessages().key(Messages.RPT_EXCEPTION_0));
                buf.append(throwable.toString());
                buf.append("</span>");
                buf.append(getLineBreak());
            }
        }
        return buf;
    }
}