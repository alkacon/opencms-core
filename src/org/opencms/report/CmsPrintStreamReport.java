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

import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.workplace.CmsWorkplace;

import java.io.PrintStream;
import java.util.Locale;

/**
 * Stream report where the output is streamed to the given print stream instance.<p>
 *
 * Keep in mind that you are resposible for closing the stream calling the {@link #close()}
 * method when the report is no longer used.<p>
 *
 * @since 6.5.5
 */
public class CmsPrintStreamReport extends CmsHtmlReport {

    /** The print stream to write the output to. */
    private PrintStream m_printStream;

    /** If set to <code>true</code> the report will write html code. */
    private boolean m_writeHtml;

    /**
     * Constructs a new report using the provided locale for the output language.<p>
     *
     * @param printStream the print stream to write the output to
     * @param locale the locale to use for the output language
     * @param writeHtml decides if the report should write clear text or html code
     */
    public CmsPrintStreamReport(PrintStream printStream, Locale locale, boolean writeHtml) {

        super(locale, null, true, true);
        m_printStream = printStream;
        m_writeHtml = writeHtml;
    }

    /**
     * Closes the print stream.<p>
     *
     * Has to be called after the report has finished.<p>
     */
    public void close() {

        if (m_printStream != null) {
            m_printStream.close();
            m_printStream = null;
        }
    }

    /**
     * Finishes the report, closing the stream.<p>
     */
    public void finish() {

        if (m_writeHtml) {
            m_printStream.println(CmsWorkplace.generatePageEndExtended());
        }
        close();
    }

    /**
     * @see org.opencms.report.I_CmsReport#getReportUpdate()
     */
    @Override
    public synchronized String getReportUpdate() {

        return "";
    }

    /**
     * @see org.opencms.report.A_CmsReport#print(java.lang.String, int)
     */
    @Override
    public synchronized void print(String value, int format) {

        if (m_writeHtml) {
            super.print(value, format);
            m_printStream.print(super.getReportUpdate());
            return;
        }
        StringBuffer buf;
        switch (format) {
            case FORMAT_HEADLINE:
                buf = new StringBuffer();
                buf.append("------ ");
                buf.append(value);
                m_printStream.print(buf);
                break;
            case FORMAT_WARNING:
                buf = new StringBuffer();
                buf.append("!!! ");
                buf.append(value);
                m_printStream.print(buf);
                addWarning(value);
                break;
            case FORMAT_ERROR:
                buf = new StringBuffer();
                buf.append("!!! ");
                buf.append(value);
                m_printStream.print(buf);
                addError(value);
                break;
            case FORMAT_NOTE:
            case FORMAT_OK:
            case FORMAT_DEFAULT:
            default:
                m_printStream.print(value);
        }
        setLastEntryTime(System.currentTimeMillis());
    }

    /**
     * @see org.opencms.report.I_CmsReport#println()
     */
    @Override
    public synchronized void println() {

        if (m_writeHtml) {
            super.println();
            m_printStream.print(super.getReportUpdate());
            setLastEntryTime(System.currentTimeMillis());
            return;
        }
        m_printStream.println();
        setLastEntryTime(System.currentTimeMillis());
    }

    /**
     * @see org.opencms.report.I_CmsReport#println(java.lang.Throwable)
     */
    @Override
    public synchronized void println(Throwable t) {

        if (m_writeHtml) {
            super.println(t);
            m_printStream.print(super.getReportUpdate());
            setLastEntryTime(System.currentTimeMillis());
            return;
        }
        StringBuffer buf = new StringBuffer();
        buf.append(getMessages().key(Messages.RPT_EXCEPTION_0));
        buf.append(t.getMessage());
        println(new String(buf), FORMAT_ERROR);
        t.printStackTrace(m_printStream);
        setLastEntryTime(System.currentTimeMillis());
    }

    /**
     * Starts the report.<p>
     */
    public void start() {

        if (m_writeHtml) {
            try {
                m_printStream.println(
                    CmsWorkplace.generatePageStartExtended(
                        OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserGuest()),
                        OpenCms.getSystemInfo().getDefaultEncoding()));
            } catch (CmsException e) {
                // ignore
            }
        }
    }

    /**
     * @see org.opencms.report.CmsHtmlReport#getLineBreak()
     */
    @Override
    protected String getLineBreak() {

        return LINEBREAK;
    }
}