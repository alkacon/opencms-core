/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/report/CmsHtmlReport.java,v $
 * Date   : $Date: 2005/07/28 15:53:10 $
 * Version: $Revision: 1.30 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
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
 * @author Alexander Kandzior 
 * @author Thomas Weckert  
 * @author Jan Baudisch 
 * 
 * @version $Revision: 1.30 $ 
 * 
 * @since 6.0.0 
 */
public class CmsHtmlReport extends A_CmsReport {

    /** Constant for a HTML linebreak with added "real" line break. */
    private static final String LINEBREAK = "<br>";

    /** 
     * Constant for a HTML linebreak with added "real" line break- 
     * traditional style for report threads that still use XML templates for their output.
     */
    private static final String LINEBREAK_TRADITIONAL = "<br>\\n";

    /** The list of report objects e.g. String, CmsPageLink, Exception ... */
    private List m_content;

    /**
     * Counter to remember what is already shown,
     * indicates the next index of the m_content list that has to be reported.
     */
    private int m_indexNext;

    /** Flag to indicate if an exception should be displayed long or short. */
    private boolean m_showExceptionStackTracke;

    /** Boolean flag indicating whether this report should generate HTML or JavaScript output. */
    private boolean m_writeHtml;

    /**
     * Constructs a new report using the provided locale for the output language.<p>
     * 
     * @param locale the locale to use for the output language
     * @param siteRoot the site root of the user who started this report (may be <code>null</code>)
     */
    public CmsHtmlReport(Locale locale, String siteRoot) {

        this(locale, siteRoot, false);
    }

    /**
     * Constructs a new report using the provided locale for the output language.<p>
     *  
     * @param locale the locale to use for the output language
     * @param siteRoot the site root of the user who started this report (may be <code>null</code>)
     * @param writeHtml if <code>true</code>, this report should generate HTML instead of JavaScript output
     */
    protected CmsHtmlReport(Locale locale, String siteRoot, boolean writeHtml) {

        init(locale, siteRoot);
        m_content = new ArrayList(256);
        m_showExceptionStackTracke = true;
        m_writeHtml = writeHtml;
    }

    /**
     * @see org.opencms.report.I_CmsReport#getReportUpdate()
     */
    public synchronized String getReportUpdate() {

        StringBuffer result = new StringBuffer();
        int indexEnd = m_content.size();
        for (int i = m_indexNext; i < indexEnd; i++) {
            Object obj = m_content.get(i);
            if (obj instanceof String || obj instanceof StringBuffer) {
                result.append(obj);
            } else if (obj instanceof Throwable) {
                result.append(getExceptionElement((Throwable)obj));
            }
        }
        m_indexNext = indexEnd;

        return result.toString();
    }

    /**
     * @see org.opencms.report.A_CmsReport#print(java.lang.String, int)
     */
    public synchronized void print(String value, int format) {

        value = CmsStringUtil.escapeJavaScript(value);
        StringBuffer buf;

        if (!m_writeHtml) {
            switch (format) {
                case FORMAT_HEADLINE:
                    buf = new StringBuffer();
                    buf.append("aH('");
                    buf.append(value);
                    buf.append("'); ");
                    m_content.add(buf);
                    break;
                case FORMAT_WARNING:
                    buf = new StringBuffer();
                    buf.append("aW('");
                    buf.append(value);
                    buf.append("'); ");
                    m_content.add(buf);
                    break;
                case FORMAT_ERROR:
                    buf = new StringBuffer();
                    buf.append("aE('");
                    buf.append(value);
                    buf.append("'); ");
                    m_content.add(buf);
                    addError(value);
                    break;
                case FORMAT_NOTE:
                    buf = new StringBuffer();
                    buf.append("aN('");
                    buf.append(value);
                    buf.append("'); ");
                    m_content.add(buf);
                    break;
                case FORMAT_OK:
                    buf = new StringBuffer();
                    buf.append("aO('");
                    buf.append(value);
                    buf.append("'); ");
                    m_content.add(buf);
                    break;
                case FORMAT_DEFAULT:
                default:
                    buf = new StringBuffer();
                    buf.append("a('");
                    buf.append(value);
                    buf.append("'); ");
                    m_content.add(buf);
            }

            // the output lines get split back into single lines on the client-side.
            // thus, a separate JavaScript call has to be added here to tell the
            // client that we want a linebreak here...
            if (value.trim().endsWith(getLineBreak())) {
                buf.append("aB(); ");
            }
        } else {
            // TODO remove this code when all reports are switched from XML templates to JSP pages
            switch (format) {
                case FORMAT_HEADLINE:
                    buf = new StringBuffer();
                    buf.append("<span class='head'>");
                    buf.append(value);
                    buf.append("</span>");
                    m_content.add(buf);
                    break;
                case FORMAT_WARNING:
                    buf = new StringBuffer();
                    buf.append("<span class='warn'>");
                    buf.append(value);
                    buf.append("</span>");
                    m_content.add(buf);
                    break;
                case FORMAT_ERROR:
                    buf = new StringBuffer();
                    buf.append("<span class='err'>");
                    buf.append(value);
                    buf.append("</span>");
                    m_content.add(buf);
                    addError(value);
                    break;
                case FORMAT_NOTE:
                    buf = new StringBuffer();
                    buf.append("<span class='note'>");
                    buf.append(value);
                    buf.append("</span>");
                    m_content.add(buf);
                    break;
                case FORMAT_OK:
                    buf = new StringBuffer();
                    buf.append("<span class='ok'>");
                    buf.append(value);
                    buf.append("</span>");
                    m_content.add(buf);
                    break;
                case FORMAT_DEFAULT:
                default:
                    m_content.add(value);
            }
        }
    }

    /**
     * @see org.opencms.report.I_CmsReport#println()
     */
    public synchronized void println() {

        this.print(getLineBreak());
    }

    /**
     * @see org.opencms.report.I_CmsReport#println(java.lang.Throwable)
     */
    public synchronized void println(Throwable t) {

        m_content.add(t);
    }

    /**
     * Returns the corrent linebreak notation depending on the output style of thsi report.
     * 
     * @return the corrent linebreak notation
     */
    protected String getLineBreak() {

        return m_writeHtml ? LINEBREAK_TRADITIONAL : LINEBREAK;
    }

    /**
     * Output helper method to format a reported <code>Throwable</code> element.<p>
     * 
     * This method ensures that exception stack traces are properly escaped
     * when they are added to the report.<p>
     * 
     * There is a member variable {@link #m_showExceptionStackTracke} in this
     * class that controls if the stack track is shown or not.
     * In a later version this might be configurable on a per-user basis.<p>
     *      
     * @param throwable the exception to format
     * @return the formatted StringBuffer
     */
    private StringBuffer getExceptionElement(Throwable throwable) {

        StringBuffer buf = new StringBuffer();

        if (!m_writeHtml) {
            if (m_showExceptionStackTracke) {
                buf.append("aT('");
                buf.append(Messages.get().key(getLocale(), Messages.RPT_EXCEPTION_0, null));
                String exception = CmsEncoder.escapeXml(CmsException.getStackTraceAsString(throwable));
                exception = CmsStringUtil.substitute(exception, "\\", "\\\\");
                StringTokenizer tok = new StringTokenizer(exception, "\r\n");
                while (tok.hasMoreTokens()) {
                    buf.append(tok.nextToken());
                    buf.append(getLineBreak());
                }
                buf.append("'); ");
                m_content.add(buf);
            } else {
                buf.append("aT('");
                buf.append(Messages.get().key(getLocale(), Messages.RPT_EXCEPTION_0, null));
                buf.append(throwable.toString());
                buf.append("'); ");
                m_content.add(buf);
            }
        } else {
            if (m_showExceptionStackTracke) {
                buf.append("<span class='throw'>");
                buf.append(Messages.get().key(getLocale(), Messages.RPT_EXCEPTION_0, null));
                String exception = CmsEncoder.escapeXml(CmsException.getStackTraceAsString(throwable));
                exception = CmsStringUtil.substitute(exception, "\\", "\\\\");
                StringTokenizer tok = new StringTokenizer(exception, "\r\n");
                while (tok.hasMoreTokens()) {
                    buf.append(tok.nextToken());
                    buf.append(getLineBreak());
                }
                buf.append("</span>");
            } else {
                buf.append("<span class='throw'>");
                buf.append(Messages.get().key(getLocale(), Messages.RPT_EXCEPTION_0, null));
                buf.append(throwable.toString());
                buf.append("</span>");
                buf.append(getLineBreak());
            }
        }

        return buf;
    }

}