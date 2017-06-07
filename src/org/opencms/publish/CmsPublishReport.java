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

package org.opencms.publish;

import org.opencms.i18n.CmsMessageContainer;
import org.opencms.report.CmsHtmlReport;
import org.opencms.report.CmsPrintStreamReport;
import org.opencms.report.I_CmsReport;
import org.opencms.util.CmsStringUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Locale;

/**
 * Report class used for the publish operations.<p>
 *
 * It stores nothing. It just prints everything to a temporary file.<p>
 *
 * @since 6.5.5
 */
public class CmsPublishReport extends CmsPrintStreamReport {

    /** The output stream. */
    protected ByteArrayOutputStream m_outputStream;

    /** The busy flag to prevent duplicated output. */
    private boolean m_busy;

    /** The original report. */
    private I_CmsReport m_report;

    /**
     * Constructs a new publish report using the provided locale for the output language.<p>
     *
     * @param locale the locale to use for the output language
     *
     */
    protected CmsPublishReport(Locale locale) {

        this(new ByteArrayOutputStream(), locale);
    }

    /**
     * Constructs a new publish report using the provided locale for the output language.<p>
     *
     * @param outputStream the underlying byte array output stream
     * @param locale the locale to use for the output language
     *
     */
    private CmsPublishReport(ByteArrayOutputStream outputStream, Locale locale) {

        super(new PrintStream(outputStream), locale, true);
        init(locale, null);

        m_outputStream = outputStream;
    }

    /**
     * Constructs a new publish report decorating the provided report.<p>
     *
     * @param report the report to decorate
     */
    private CmsPublishReport(I_CmsReport report) {

        this(new ByteArrayOutputStream(), report.getLocale());
        m_report = report;
        if (report instanceof CmsHtmlReport) {
            if (((CmsHtmlReport)report).isWriteHtml()) {
                try {
                    m_outputStream.write(CmsStringUtil.substitute(getReportUpdate(), "\\n", "").getBytes());
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    /**
     * Returns a publish report instance that writes to the given report as well as
     * to the given temporary file.<p>
     *
     * @param report the report to decorate
     *
     * @return the publish report
     */
    protected static CmsPrintStreamReport decorate(final I_CmsReport report) {

        return new CmsPublishReport(report);
    }

    /**
     * @see org.opencms.report.A_CmsReport#addError(java.lang.Object)
     */
    @Override
    public void addError(Object obj) {

        if (!m_busy && (m_report != null)) {
            m_report.addError(obj);
        }
        m_busy = true;
        super.addError(obj);
        m_busy = false;
    }

    /**
     * @see org.opencms.report.A_CmsReport#addWarning(java.lang.Object)
     */
    @Override
    public void addWarning(Object obj) {

        if (!m_busy && (m_report != null)) {
            m_report.addWarning(obj);
        }
        m_busy = true;
        super.addWarning(obj);
        m_busy = false;
    }

    /**
     * Returns the contents of the publish report as byte array.<p>
     *
     * @return the contents of the publish report
     */
    public byte[] getContents() {

        return m_outputStream.toByteArray();
    }

    /**
     * @see org.opencms.report.A_CmsReport#getErrors()
     */
    @Override
    public List<Object> getErrors() {

        if (m_report != null) {
            return m_report.getErrors();
        }
        return super.getErrors();
    }

    /**
     * @see org.opencms.report.CmsPrintStreamReport#getReportUpdate()
     */
    @Override
    public synchronized String getReportUpdate() {

        if (m_report != null) {
            return m_report.getReportUpdate();
        }
        return super.getReportUpdate();
    }

    /**
     * @see org.opencms.report.A_CmsReport#getWarnings()
     */
    @Override
    public List<Object> getWarnings() {

        if (m_report != null) {
            return m_report.getWarnings();
        }
        return super.getWarnings();
    }

    /**
     * @see org.opencms.report.A_CmsReport#print(org.opencms.i18n.CmsMessageContainer)
     */
    @Override
    public void print(CmsMessageContainer container) {

        if (!m_busy && (m_report != null)) {
            m_report.print(container);
        }
        m_busy = true;
        super.print(container);
        m_busy = false;
    }

    /**
     * @see org.opencms.report.A_CmsReport#print(org.opencms.i18n.CmsMessageContainer, int)
     */
    @Override
    public void print(CmsMessageContainer container, int format) {

        if (!m_busy && (m_report != null)) {
            m_report.print(container, format);
        }
        m_busy = true;
        super.print(container, format);
        m_busy = false;
    }

    /**
     * @see org.opencms.report.CmsPrintStreamReport#println()
     */
    @Override
    public synchronized void println() {

        if (!m_busy && (m_report != null)) {
            m_report.println();
        }
        m_busy = true;
        super.println();
        m_busy = false;
    }

    /**
     * @see org.opencms.report.A_CmsReport#println(org.opencms.i18n.CmsMessageContainer)
     */
    @Override
    public void println(CmsMessageContainer container) {

        if (!m_busy && (m_report != null)) {
            m_report.println(container);
        }
        m_busy = true;
        super.println(container);
        m_busy = false;
    }

    /**
     * @see org.opencms.report.A_CmsReport#println(org.opencms.i18n.CmsMessageContainer, int)
     */
    @Override
    public void println(CmsMessageContainer container, int format) {

        if (!m_busy && (m_report != null)) {
            m_report.println(container, format);
        }
        m_busy = true;
        super.println(container, format);
        m_busy = false;
    }

    /**
     * @see org.opencms.report.CmsPrintStreamReport#println(java.lang.Throwable)
     */
    @Override
    public synchronized void println(Throwable t) {

        if (!m_busy && (m_report != null)) {
            m_report.println(t);
        }
        m_busy = true;
        super.println(t);
        m_busy = false;
    }

    /**
     * @see org.opencms.report.A_CmsReport#printMessageWithParam(org.opencms.i18n.CmsMessageContainer, java.lang.Object)
     */
    @Override
    public void printMessageWithParam(CmsMessageContainer container, Object param) {

        if (!m_busy && (m_report != null)) {
            m_report.printMessageWithParam(container, param);
        }
        m_busy = true;
        super.printMessageWithParam(container, param);
        m_busy = false;
    }

    /**
     * @see org.opencms.report.A_CmsReport#printMessageWithParam(int, int, org.opencms.i18n.CmsMessageContainer, java.lang.Object)
     */
    @Override
    public void printMessageWithParam(int m, int n, CmsMessageContainer container, Object param) {

        if (!m_busy && (m_report != null)) {
            m_report.printMessageWithParam(m, n, container, param);
        }
        m_busy = true;
        super.printMessageWithParam(m, n, container, param);
        m_busy = false;
    }

    /**
     * @see org.opencms.report.A_CmsReport#resetRuntime()
     */
    @Override
    public void resetRuntime() {

        if (!m_busy && (m_report != null)) {
            m_report.resetRuntime();
        }
        m_busy = true;
        super.resetRuntime();
        m_busy = false;
    }
}