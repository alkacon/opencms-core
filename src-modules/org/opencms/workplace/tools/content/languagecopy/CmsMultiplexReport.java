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

package org.opencms.workplace.tools.content.languagecopy;

import org.opencms.i18n.CmsMessageContainer;
import org.opencms.report.A_CmsReport;
import org.opencms.report.I_CmsReport;

import java.util.LinkedList;
import java.util.List;

/**
 * Report proxy that multiplexes to all contained <code>{@link I_CmsReport}</code> instances.<p>
 *
 * @since 7.5.1
 */
public class CmsMultiplexReport extends A_CmsReport {

    /** The reports to multiplex to. */
    private List<A_CmsReport> m_delegates = new LinkedList<A_CmsReport>();

    /**
     * Default constructor.<p>
     */
    public CmsMultiplexReport() {

        // nop
    }

    /**
     * Adds the given report to become a proxy delegate of this multiplexer.<p>
     *
     * @param report the report to be on the recipient list.
     */
    public void addReport(final A_CmsReport report) {

        m_delegates.add(report);
    }

    /**
     * @see org.opencms.report.A_CmsReport#getLastEntryTime()
     */
    @Override
    public long getLastEntryTime() {

        if (m_delegates.isEmpty()) {
            return 0;
        }
        return m_delegates.get(0).getLastEntryTime();
    }

    /**
     * This searches for the first instance of a link in the internal delegate list and
     * returns the value of it's invocation.
     * <p>
     *
     * If no such report is found an empty String will be returned.
     * <p>
     *
     * @see org.opencms.report.I_CmsReport#getReportUpdate()
     */
    public String getReportUpdate() {

        for (I_CmsReport report : m_delegates) {
            if (report.getClass().getName().toLowerCase().contains("html")) {
                return report.getReportUpdate();
            }
        }
        return "";
    }

    /**
     * @see I_CmsReport#print(CmsMessageContainer)
     */
    @Override
    public void print(final CmsMessageContainer container) {

        for (I_CmsReport report : m_delegates) {
            report.print(container);
        }
    }

    /**
     * @see I_CmsReport#print(CmsMessageContainer, int)
     */
    @Override
    public void print(final CmsMessageContainer container, final int format) {

        for (I_CmsReport report : m_delegates) {
            report.print(container, format);
        }

    }

    /**
     * @see I_CmsReport#println()
     */
    public void println() {

        for (I_CmsReport report : m_delegates) {
            report.println();
        }
    }

    /**
     * @see I_CmsReport#println(CmsMessageContainer)
     */
    @Override
    public void println(final CmsMessageContainer container) {

        for (I_CmsReport report : m_delegates) {
            report.println(container);
        }
    }

    /**
     * @see I_CmsReport#println(CmsMessageContainer, int)
     */
    @Override
    public void println(final CmsMessageContainer container, final int format) {

        for (I_CmsReport report : m_delegates) {
            report.println(container, format);
        }
    }

    /**
     * @see I_CmsReport#println(Throwable)
     */
    public void println(final Throwable t) {

        // do nothing
    }

    /**
     * @see I_CmsReport#printMessageWithParam(CmsMessageContainer, Object)
     */
    @Override
    public void printMessageWithParam(final CmsMessageContainer container, final Object param) {

        for (I_CmsReport report : m_delegates) {
            report.printMessageWithParam(container, param);
        }
    }

    /**
     * @see I_CmsReport#printMessageWithParam(int, int, CmsMessageContainer, Object)
     */
    @Override
    public void printMessageWithParam(
        final int m,
        final int n,
        final CmsMessageContainer container,
        final Object param) {

        for (I_CmsReport report : m_delegates) {
            report.printMessageWithParam(m, n, container, param);
        }
    }

    /**
     * @see I_CmsReport#resetRuntime()
     */
    @Override
    public void resetRuntime() {

        for (I_CmsReport report : m_delegates) {
            report.resetRuntime();
        }
    }

    /**
     * @see org.opencms.report.A_CmsReport#print(java.lang.String, int)
     */
    @Override
    protected void print(String value, int format) {

        // nop, this is a helper method in A_CmsReport just called from the other routines but not directly.
    }
}
