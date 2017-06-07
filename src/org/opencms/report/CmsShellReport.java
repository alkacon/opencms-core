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

import org.opencms.i18n.CmsMessageContainer;

import java.util.Locale;

/**
 * Report class used for the shell.<p>
 *
 * It stores nothing. It just prints everything to <code>{@link System#out}</code><p>.
 *
 * @since 6.0.0
 */
public class CmsShellReport extends CmsPrintStreamReport {

    /** Flag indicating if the job is still running. */
    private boolean m_stillRunning;

    /**
     * Constructs a new report using the provided locale for the output language.<p>
     *
     * @param locale the locale to use for the output language
     */
    public CmsShellReport(Locale locale) {

        super(System.out, locale, false);
    }

    /**
     * @see org.opencms.report.I_CmsReport#getReportUpdate()
     */
    @Override
    public synchronized String getReportUpdate() {

        // to avoid premature interruption of the reporting thread (@see org.opencms.main.CmsThreadStore),
        // a not empty string is returned, if there have been any print outs since the last check
        if (m_stillRunning) {
            m_stillRunning = false;
            return "*";
        }
        return "";
    }

    /**
     * @see org.opencms.report.A_CmsReport#print(org.opencms.i18n.CmsMessageContainer)
     */
    @Override
    public void print(CmsMessageContainer container) {

        super.print(container);
        m_stillRunning = true;
    }

    /**
     * @see org.opencms.report.A_CmsReport#print(org.opencms.i18n.CmsMessageContainer, int)
     */
    @Override
    public void print(CmsMessageContainer container, int format) {

        super.print(container, format);
        m_stillRunning = true;
    }

    /**
     * @see org.opencms.report.CmsPrintStreamReport#println()
     */
    @Override
    public void println() {

        super.println();
        m_stillRunning = true;
    }

    /**
     * @see org.opencms.report.A_CmsReport#println(org.opencms.i18n.CmsMessageContainer)
     */
    @Override
    public void println(CmsMessageContainer container) {

        super.println(container);
        m_stillRunning = true;
    }

    /**
     * @see org.opencms.report.A_CmsReport#println(org.opencms.i18n.CmsMessageContainer, int)
     */
    @Override
    public void println(CmsMessageContainer container, int format) {

        super.println(container, format);
        m_stillRunning = true;
    }

    /**
     * @see org.opencms.report.CmsPrintStreamReport#println(java.lang.Throwable)
     */
    @Override
    public void println(Throwable t) {

        super.println(t);
        m_stillRunning = true;
    }

    /**
     * @see org.opencms.report.A_CmsReport#printMessageWithParam(org.opencms.i18n.CmsMessageContainer, java.lang.Object)
     */
    @Override
    public void printMessageWithParam(CmsMessageContainer container, Object param) {

        super.printMessageWithParam(container, param);
        m_stillRunning = true;
    }

    /**
     * @see org.opencms.report.A_CmsReport#printMessageWithParam(int, int, org.opencms.i18n.CmsMessageContainer, java.lang.Object)
     */
    @Override
    public void printMessageWithParam(int m, int n, CmsMessageContainer container, Object param) {

        super.printMessageWithParam(m, n, container, param);
        m_stillRunning = true;
    }

    /**
     * @see org.opencms.report.CmsPrintStreamReport#start()
     */
    @Override
    public void start() {

        super.start();
        m_stillRunning = true;
    }

}