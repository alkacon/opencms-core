/*
 * This program is part of the Alkacon OpenCms Software library.
 *
 * This license applies to all programs, pages, Java classes, parts and
 * modules of the Alkacon OpenCms Software library published by
 * Alkacon Software GmbH & Co. KG, unless otherwise noted.
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * companys website: http://www.alkacon.com.
 *
 * For further information about OpenCms, please see the OpenCms project
 * website: http://www.opencms.org.
 *
 * The names "Alkacon", "Alkacon Software GmbH & Co. KG" and "OpenCms" must not be used
 * to endorse or promote products derived from this software without prior
 * written permission. For written permission, please contact info@alkacon.com.
 *
 * Products derived from this software may not be called "Alkacon",
 * "Alkacon Software GmbH & Co. KG" or "OpenCms", nor may "Alkacon", "Alkacon Software GmbH & Co. KG"
 * or "OpenCms" appear in their name, without prior written permission of
 * Alkacon Software GmbH & Co. KG.
 *
 * This program is also available under a commercial non-GPL license. For
 * pricing and ordering information, please inquire at sales@alkacon.com.
 */

package org.opencms.workplace.tools.database;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.lock.CmsLockFilter;
import org.opencms.lock.CmsLockType;
import org.opencms.main.OpenCms;
import org.opencms.report.A_CmsReportThread;
import org.opencms.report.I_CmsReport;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Remove the publish locks.<p>
 *
 * @since 7.0.2
 */
public class CmsRemovePubLocksThread extends A_CmsReportThread {

    /** The last error occurred. */
    private Throwable m_error;

    /** The list of resource names. */
    private List m_resources;

    /**
     * Creates an Thread to remove the publish locks.<p>
     *
     * @param cms the current OpenCms context object
     * @param resources a list of resource names
     */
    public CmsRemovePubLocksThread(CmsObject cms, List resources) {

        super(cms, Messages.get().getBundle().key(Messages.GUI_DB_PUBLOCKS_THREAD_NAME_0));
        m_resources = new ArrayList(resources);
        initHtmlReport(cms.getRequestContext().getLocale());
    }

    /**
     * Returns the last error.<p>
     *
     * @see org.opencms.report.A_CmsReportThread#getError()
     */
    @Override
    public Throwable getError() {

        return m_error;
    }

    /**
     * Updates the report.<p>
     *
     * @see org.opencms.report.A_CmsReportThread#getReportUpdate()
     */
    @Override
    public String getReportUpdate() {

        return getReport().getReportUpdate();
    }

    /**
     * Starts the report thread.<p>
     *
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {

        try {
            getReport().println(
                Messages.get().container(Messages.RPT_DB_PUBLOCKS_BEGIN_0),
                I_CmsReport.FORMAT_HEADLINE);
            CmsObject cms = getCms();
            CmsLockFilter filter = CmsLockFilter.FILTER_ALL;
            filter = filter.filterType(CmsLockType.PUBLISH);

            Iterator it = m_resources.iterator();
            while (it.hasNext()) {
                String paramResName = (String)it.next();
                getReport().println(
                    Messages.get().container(Messages.RPT_DB_PUBLOCKS_READLOCKS_1, paramResName),
                    I_CmsReport.FORMAT_NOTE);
                Iterator itResources = cms.getLockedResources(paramResName, filter).iterator();
                while (itResources.hasNext()) {
                    String resName = (String)itResources.next();
                    if (!cms.existsResource(resName, CmsResourceFilter.ALL)) {
                        getReport().println(
                            Messages.get().container(Messages.RPT_DB_PUBLOCKS_UNLOCKING_1, resName),
                            I_CmsReport.FORMAT_DEFAULT);
                        OpenCms.getMemoryMonitor().uncacheLock(cms.getRequestContext().addSiteRoot(resName));
                        continue;
                    }
                    Iterator itSiblings = cms.readSiblings(resName, CmsResourceFilter.ALL).iterator();
                    while (itSiblings.hasNext()) {
                        CmsResource res = (CmsResource)itSiblings.next();
                        getReport().println(
                            Messages.get().container(Messages.RPT_DB_PUBLOCKS_UNLOCKING_1, cms.getSitePath(res)),
                            I_CmsReport.FORMAT_DEFAULT);
                        OpenCms.getMemoryMonitor().uncacheLock(res.getRootPath());
                    }
                }
            }
            getReport().println(Messages.get().container(Messages.RPT_DB_PUBLOCKS_END_0), I_CmsReport.FORMAT_HEADLINE);
        } catch (Throwable exc) {
            getReport().println(
                Messages.get().container(Messages.RPT_DB_PUBLOCKS_FAILED_0),
                I_CmsReport.FORMAT_WARNING);
            getReport().println(exc);
            m_error = exc;
        }
    }
}
