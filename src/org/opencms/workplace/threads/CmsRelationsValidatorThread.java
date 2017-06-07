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

package org.opencms.workplace.threads;

import org.opencms.db.CmsPublishList;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.report.A_CmsReportThread;
import org.opencms.workplace.CmsWorkplaceSettings;

import org.apache.commons.logging.Log;

/**
 * A report thread for the relations validator.<p>
 *
 * @since 6.0.0
 */
public class CmsRelationsValidatorThread extends A_CmsReportThread {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsRelationsValidatorThread.class);

    /** A list of cms resources to be published directly.<p> */
    private CmsPublishList m_publishList;

    /** Flag that indicates whether the publish list should be svaed in the workplace settings. */
    private boolean m_savePublishList;

    /** The current user's workplace settings.<p> */
    private CmsWorkplaceSettings m_settings;

    /**
     * Creates a thread that validates the relations for all files of the current project.<p>
     *
     * @param cms the current OpenCms context object
     */
    public CmsRelationsValidatorThread(CmsObject cms) {

        super(
            cms,
            Messages.get().getBundle().key(
                Messages.GUI_HTML_LINK_VALIDATOR_THREAD_NAME_1,
                new Object[] {cms.getRequestContext().getCurrentProject().getName()}));

        m_publishList = null;
        m_savePublishList = false;
        m_settings = null;

        initHtmlReport(cms.getRequestContext().getLocale());
    }

    /**
     * Creates a thread that validates the relations in the list of unpublished file(s) of the
     * current (offline) project.<p>
     *
     * The publish list *IS* saved in the current user's workplace settings for
     * further processing by other threads. The last thread processing this publish list *MUST*
     * ensure that the publish list gets removed from the current user's workplace settings!<p>
     *
     * @param cms the current OpenCms context object
     * @param publishList the list of resources which will be directly published
     * @param settings the current user's workplace settings
     */
    public CmsRelationsValidatorThread(CmsObject cms, CmsPublishList publishList, CmsWorkplaceSettings settings) {

        super(
            cms,
            Messages.get().getBundle().key(
                Messages.GUI_HTML_LINK_VALIDATOR_THREAD_NAME_1,
                new Object[] {cms.getRequestContext().getCurrentProject().getName()}));

        m_publishList = publishList;
        if (m_publishList == null) {
            try {
                m_publishList = OpenCms.getPublishManager().getPublishList(cms);
            } catch (CmsException e) {
                // ignore
            }
        }
        m_savePublishList = true;
        m_settings = settings;

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
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {

        try {
            // validate the HTML links in the resources that actually get published
            OpenCms.getPublishManager().validateRelations(getCms(), m_publishList, getReport());

            if (m_savePublishList && (m_settings != null)) {
                // save the publish list optionally to be processed by further workplace threads
                m_settings.setPublishList(m_publishList);
            }
        } catch (Throwable e) {
            if (m_savePublishList && (m_settings != null)) {
                // overwrite the publish list in any case with null
                m_settings.setPublishList(null);
            }
            getReport().println(e);
            LOG.error(Messages.get().getBundle().key(Messages.ERR_LINK_VALIDATION_0), e);
        }
    }
}