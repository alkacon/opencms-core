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

package org.opencms.workplace.tools.content;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsLog;
import org.opencms.report.A_CmsReportThread;

import org.apache.commons.logging.Log;

/**
 * Thread for element rename.<p>
 *
 * @since 6.0.0
 */
public class CmsElementRenameThread extends A_CmsReportThread {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsElementRenameThread.class);

    private CmsElementRename m_elementRename;

    /**
     * Constructor, creates a new CmsElementRenameThread.<p>
     *
     * @param cms the current CmsObject
     * @param elementRename the initialized CmsElementRename Object
     */
    public CmsElementRenameThread(CmsObject cms, CmsElementRename elementRename) {

        super(cms, "Renaming Elements");
        cms.getRequestContext().setUpdateSessionEnabled(false);
        initHtmlReport(cms.getRequestContext().getLocale());
        m_elementRename = elementRename;
    }

    /**
     * @see org.opencms.report.A_CmsReportThread#getReportUpdate()
     */
    @Override
    public String getReportUpdate() {

        return getReport().getReportUpdate();
    }

    /**
     * The run method which starts the rename process.<p>
     */
    @Override
    public synchronized void run() {

        try {
            // do the rename operation
            m_elementRename.actionRename(getReport());
        } catch (Exception e) {
            getReport().println(e);
            if (LOG.isErrorEnabled()) {
                LOG.error(e.getMessage());
            }
        }
    }
}