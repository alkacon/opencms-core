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
 * For further information about Alkacon Software, please see the
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
import org.opencms.main.CmsLog;

import java.util.Locale;

import org.apache.commons.logging.Log;

/**
 * Report for shell which writes to LOG.<p>
 */
public class CmsShellLogReport extends CmsShellReport {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsShellLogReport.class);

    /**Line to write in LOG. */
    private String m_line = "";

    /**
     * public constructor.<p>
     *
     * @param locale locale
     */
    public CmsShellLogReport(Locale locale) {

        super(locale);

    }

    /**
     * @see org.opencms.report.A_CmsReport#print(org.opencms.i18n.CmsMessageContainer)
     */
    @Override
    public void print(CmsMessageContainer container) {

        super.print(container);
        m_line += (container.key());
    }

    /**
     * @see org.opencms.report.A_CmsReport#print(org.opencms.i18n.CmsMessageContainer, int)
     */
    @Override
    public void print(CmsMessageContainer container, int format) {

        super.print(container, format);
        m_line += (container.key());
    }

    /**
     * @see org.opencms.report.CmsPrintStreamReport#println()
     */
    @Override
    public void println() {

        super.println();
        LOG.info(m_line);
        m_line = "";
    }

    /**
     * @see org.opencms.report.A_CmsReport#println(org.opencms.i18n.CmsMessageContainer)
     */
    @Override
    public void println(CmsMessageContainer container) {

        super.println(container);
        m_line += container.key();
        LOG.info(m_line);
        m_line = "";
    }

    /**
     * @see org.opencms.report.A_CmsReport#println(org.opencms.i18n.CmsMessageContainer, int)
     */
    @Override
    public void println(CmsMessageContainer container, int format) {

        super.println(container, format);

        m_line += container.key();
        LOG.info(m_line);
        m_line = "";
    }

    /**
     * @see org.opencms.report.A_CmsReport#printMessageWithParam(org.opencms.i18n.CmsMessageContainer, java.lang.Object)
     */
    @Override
    public void printMessageWithParam(CmsMessageContainer container, Object param) {

        super.printMessageWithParam(container, param);

        m_line += container.key();
    }

    /**
     * @see org.opencms.report.A_CmsReport#printMessageWithParam(int, int, org.opencms.i18n.CmsMessageContainer, java.lang.Object)
     */
    @Override
    public void printMessageWithParam(int m, int n, CmsMessageContainer container, Object param) {

        super.printMessageWithParam(m, n, container, param);
        m_line += container.key();
    }

}
