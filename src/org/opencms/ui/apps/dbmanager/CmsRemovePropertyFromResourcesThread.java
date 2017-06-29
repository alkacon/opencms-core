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

package org.opencms.ui.apps.dbmanager;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.report.A_CmsReportThread;
import org.opencms.report.I_CmsReport;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.apps.sitemanager.CmsSitesWebserverThread;

import org.apache.commons.logging.Log;

/**
 * Thread which removes property from ressources and deletes property definition.<p>
 */
public class CmsRemovePropertyFromResourcesThread extends A_CmsReportThread {

    /** The logger for this class. */
    static Log LOG = CmsLog.getLog(CmsSitesWebserverThread.class.getName());

    /** The file path. */
    private String m_propName;

    /**
     * Public constructor.<p>
     *
     * @param cms CmsObejct
     * @param propName propertydefinition name
     */
    public CmsRemovePropertyFromResourcesThread(CmsObject cms, String propName) {

        super(cms, "write-to-webserver");
        m_propName = propName;

        initHtmlReport(cms.getRequestContext().getLocale());
    }

    /**
     * @see org.opencms.report.A_CmsReportThread#getReportUpdate()
     */
    @Override
    public String getReportUpdate() {

        return getReport().getReportUpdate();
    }

    /**
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {

        try {
            getReport().println(
                Messages.get().container(Messages.RPT_DATABASEAPP_DEL_PROPERTY_START_0),
                I_CmsReport.FORMAT_DEFAULT);
            getReport().println();
            getReport().println(
                Messages.get().container(Messages.RPT_DATABASEAPP_DEL_PROPERTY_REMOVE_FROM_RESOURCE_START_0),
                I_CmsReport.FORMAT_DEFAULT);
            for (CmsResource res : getCms().readResourcesWithProperty(m_propName)) {
                getReport().print(
                    Messages.get().container(
                        Messages.RPT_DATABASEAPP_DEL_PROPERTY_REMOVE_FROM_RESOURCE_1,
                        res.getRootPath()),
                    I_CmsReport.FORMAT_DEFAULT);
                CmsProperty prop = new CmsProperty(m_propName, "", "");
                try {
                    getCms().lockResource(res);
                    getCms().writePropertyObject(res.getRootPath(), prop);
                    getCms().unlockResource(res);
                    getReport().println(Messages.get().container(Messages.RPT_DATABASEAPP_OK_0), I_CmsReport.FORMAT_OK);
                } catch (CmsException e) {
                    LOG.error("unable to remove property from resource", e);
                    getReport().println(
                        Messages.get().container(Messages.RPT_DATABASEAPP_FAILED_0),
                        I_CmsReport.FORMAT_ERROR);
                }
            }
            try {
                getCms().deletePropertyDefinition(m_propName);
                getReport().println();
                getReport().println(
                    Messages.get().container(Messages.RPT_DATABASEAPP_DEL_PROPERTY_END_OK_0),
                    I_CmsReport.FORMAT_OK);
            } catch (CmsException e) {
                LOG.error("Unable to delete property definition", e);
                getReport().println(
                    Messages.get().container(Messages.RPT_DATABASEAPP_DEL_PROPERTY_END_FAILED_0),
                    I_CmsReport.FORMAT_ERROR);
            }

        } catch (Exception e) {
            LOG.error("Unable to read resources for property", e);
            getReport().println(e);
        }
    }
}
