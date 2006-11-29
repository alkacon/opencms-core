/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/publish/CmsPublishReport.java,v $
 * Date   : $Date: 2006/11/29 15:04:09 $
 * Version: $Revision: 1.1.2.1 $
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

package org.opencms.publish;

import org.opencms.i18n.CmsMessageContainer;
import org.opencms.report.CmsPrintStreamReport;
import org.opencms.report.I_CmsReport;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Locale;

/**
 * Report class used for the publish operations.<p>
 * 
 * It stores nothing. It just prints everthing to a temporary file.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.1.2.1 $ 
 * 
 * @since 6.5.5 
 */
class CmsPublishReport extends CmsPrintStreamReport {

    /** The busy flag to prevent duplicated output. */
    private boolean m_busy;

    /** The original report. */
    private I_CmsReport m_report;

    /**
     * Constructs a new publish report using the provided locale for the output language.<p>
     *  
     * @param locale the locale to use for the output language
     * @param path the file to write this report to
     * 
     * @throws IOException if something goes wrong
     */
    protected CmsPublishReport(Locale locale, String path)
    throws IOException {

        super(new PrintStream(new FileOutputStream(new File(path))), locale, true);
        init(locale, null);
    }

    /**
     * Constructs a new publish report decorating the provided report.<p>
     *  
     * @param report the report to decorate
     * @param path the file to write this report to
     * 
     * @throws IOException if something goes wrong
     */
    private CmsPublishReport(I_CmsReport report, String path)
    throws IOException {

        this(report.getLocale(), path);
        m_report = report;
    }

    /**
     * Returns a publish report instance that writes to the given report as well as 
     * to the given temporary file.<p> 
     * 
     * @param report the report to decorate
     * @param path the file to write this report to
     * 
     * @return the publish report
     * 
     * @throws IOException if something ggoes wrong
     */
    protected static CmsPrintStreamReport decorate(final I_CmsReport report, String path) throws IOException {

        return new CmsPublishReport(report, path);
    }

    /**
     * @see org.opencms.report.A_CmsReport#addError(java.lang.Object)
     */
    public void addError(Object obj) {

        if (!m_busy && m_report != null) {
            m_report.addError(obj);
        }
        m_busy = true;
        super.addError(obj);
        m_busy = false;
    }

    /**
     * @see org.opencms.report.A_CmsReport#addWarning(java.lang.Object)
     */
    public void addWarning(Object obj) {

        if (!m_busy && m_report != null) {
            m_report.addWarning(obj);
        }
        m_busy = true;
        super.addWarning(obj);
        m_busy = false;
    }

    /**
     * @see org.opencms.report.CmsPrintStreamReport#getReportUpdate()
     */
    public synchronized String getReportUpdate() {

        if (m_report != null) {
            return m_report.getReportUpdate();
        }
        return super.getReportUpdate();
    }

    /**
     * @see org.opencms.report.A_CmsReport#print(org.opencms.i18n.CmsMessageContainer)
     */
    public void print(CmsMessageContainer container) {

        if (!m_busy && m_report != null) {
            m_report.print(container);
        }
        m_busy = true;
        super.print(container);
        m_busy = false;
    }

    /**
     * @see org.opencms.report.A_CmsReport#print(org.opencms.i18n.CmsMessageContainer, int)
     */
    public void print(CmsMessageContainer container, int format) {

        if (!m_busy && m_report != null) {
            m_report.print(container, format);
        }
        m_busy = true;
        super.print(container, format);
        m_busy = false;
    }

    /**
     * @see org.opencms.report.CmsPrintStreamReport#println()
     */
    public synchronized void println() {

        if (!m_busy && m_report != null) {
            m_report.println();
        }
        m_busy = true;
        super.println();
        m_busy = false;
    }

    /**
     * @see org.opencms.report.A_CmsReport#println(org.opencms.i18n.CmsMessageContainer)
     */
    public void println(CmsMessageContainer container) {

        if (!m_busy && m_report != null) {
            m_report.println(container);
        }
        m_busy = true;
        super.println(container);
        m_busy = false;
    }

    /**
     * @see org.opencms.report.A_CmsReport#println(org.opencms.i18n.CmsMessageContainer, int)
     */
    public void println(CmsMessageContainer container, int format) {

        if (!m_busy && m_report != null) {
            m_report.println(container, format);
        }
        m_busy = true;
        super.println(container, format);
        m_busy = false;
    }

    /**
     * @see org.opencms.report.CmsPrintStreamReport#println(java.lang.Throwable)
     */
    public synchronized void println(Throwable t) {

        if (!m_busy && m_report != null) {
            m_report.println(t);
        }
        m_busy = true;
        super.println(t);
        m_busy = false;
    }

    /**
     * @see org.opencms.report.A_CmsReport#printMessageWithParam(org.opencms.i18n.CmsMessageContainer, java.lang.Object)
     */
    public void printMessageWithParam(CmsMessageContainer container, Object param) {

        if (!m_busy && m_report != null) {
            m_report.printMessageWithParam(container, param);
        }
        m_busy = true;
        super.printMessageWithParam(container, param);
        m_busy = false;
    }

    /**
     * @see org.opencms.report.A_CmsReport#printMessageWithParam(int, int, org.opencms.i18n.CmsMessageContainer, java.lang.Object)
     */
    public void printMessageWithParam(int m, int n, CmsMessageContainer container, Object param) {

        if (!m_busy && m_report != null) {
            m_report.printMessageWithParam(m, n, container, param);
        }
        m_busy = true;
        super.printMessageWithParam(m, n, container, param);
        m_busy = false;
    }

    /**
     * @see org.opencms.report.A_CmsReport#resetRuntime()
     */
    public void resetRuntime() {

        if (!m_busy && m_report != null) {
            m_report.resetRuntime();
        }
        m_busy = true;
        super.resetRuntime();
        m_busy = false;
    }
}