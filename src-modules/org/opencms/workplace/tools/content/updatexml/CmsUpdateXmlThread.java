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

package org.opencms.workplace.tools.content.updatexml;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.file.types.CmsResourceTypeXmlPage;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.report.A_CmsReportThread;
import org.opencms.report.I_CmsReport;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;

/**
 * Converting xml contents according to new schema.
 * <p>
 *
 * @since 7.0.5
 */
public class CmsUpdateXmlThread extends A_CmsReportThread {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsUpdateXmlThread.class);

    /** Current CmsObject. */
    private CmsObject m_cmsObject;

    /** Number of errors while updating. */
    private int m_errorUpdate;

    /** Number of locked files during updating. */
    private int m_lockedFiles;

    /** Settings. */
    private CmsUpdateXmlSettings m_settings;

    /**
     * Creates a replace html tag Thread.<p>
     *
     * @param cms the current cms context.
     *
     * @param settings the settings needed to perform the operation.
     */
    public CmsUpdateXmlThread(CmsObject cms, CmsUpdateXmlSettings settings) {

        super(cms, Messages.get().getBundle().key(Messages.GUI_UPDATEXML_THREAD_NAME_0));
        initHtmlReport(cms.getRequestContext().getLocale());
        m_cmsObject = cms;
        m_settings = settings;
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

        I_CmsReport report = getReport();
        report.println(
            Messages.get().container(Messages.RPT_UPDATEXML_BEGIN_UPDATE_THREAD_0),
            I_CmsReport.FORMAT_HEADLINE);
        try {
            // update xml contents
            updateXmlContents(report, m_settings.getVfsFolder(), m_settings.getIncludeSubFolders(), m_cmsObject);
        } catch (Throwable f) {
            m_errorUpdate += 1;
            report.println(Messages.get().container(Messages.RPT_UPDATETXML_UPDATE_ERROR_0));
            if (LOG.isErrorEnabled()) {
                LOG.error(f.toString());
            }
        }

        // append runtime statistics to report
        getReport().print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_STAT_0));
        getReport().println(
            org.opencms.report.Messages.get().container(
                org.opencms.report.Messages.RPT_STAT_DURATION_1,
                getReport().formatRuntime()));
        getReport().println(
            Messages.get().container(Messages.RPT_UPDATEXML_END_UPDATE_THREAD_0),
            I_CmsReport.FORMAT_HEADLINE);
    }

    /**
     * Locks the current resource.<p>
     *
     * @param cms the current CmsObject
     * @param cmsResource the resource to lock
     * @param report the report
     *
     * @throws CmsException if some goes wrong
     */
    private boolean lockResource(CmsObject cms, CmsResource cmsResource, I_CmsReport report) throws CmsException {

        CmsLock lock = cms.getLock(getCms().getSitePath(cmsResource));
        // check the lock
        if ((lock != null)
            && lock.isOwnedBy(getCms().getRequestContext().getCurrentUser())
            && lock.isOwnedInProjectBy(
                getCms().getRequestContext().getCurrentUser(),
                getCms().getRequestContext().getCurrentProject())) {
            // prove is current lock from current user in current project
            return true;
        } else if ((lock != null)
            && !lock.isUnlocked()
            && !lock.isOwnedBy(getCms().getRequestContext().getCurrentUser())) {
            // the resource is not locked by the current user, so can not lock it
            m_lockedFiles += 1;
            return false;
        } else if ((lock != null)
            && !lock.isUnlocked()
            && lock.isOwnedBy(getCms().getRequestContext().getCurrentUser())
            && !lock.isOwnedInProjectBy(
                getCms().getRequestContext().getCurrentUser(),
                getCms().getRequestContext().getCurrentProject())) {
            // prove is current lock from current user but not in current project
            // file is locked by current user but not in current project
            // change the lock
            cms.changeLock(getCms().getSitePath(cmsResource));
        } else if ((lock != null) && lock.isUnlocked()) {
            // lock resource from current user in current project
            cms.lockResource(getCms().getSitePath(cmsResource));
        }
        lock = cms.getLock(getCms().getSitePath(cmsResource));
        if ((lock != null)
            && lock.isOwnedBy(getCms().getRequestContext().getCurrentUser())
            && !lock.isOwnedInProjectBy(
                getCms().getRequestContext().getCurrentUser(),
                getCms().getRequestContext().getCurrentProject())) {
            // resource could not be locked
            m_lockedFiles += 1;

            return false;
        }
        // resource is locked successfully
        return true;
    }

    /**
     * The method to update xml contents.<p>
     *
     * @param report I_CmsReport
     * @param resourcePath Path to update xml contents in
     * @param inclSubFolder true, if also resources in subfolders in the vfs folder shall be updated, otherwise false
     * @param cmsObject Current CmsObject
     */
    @SuppressWarnings("unchecked")
    private void updateXmlContents(
        I_CmsReport report,
        String resourcePath,
        boolean inclSubFolder,
        CmsObject cmsObject) {

        // write parameters to report
        report.println(Messages.get().container(Messages.RPT_UPDATEXML_BEGIN_UPDATE_0), I_CmsReport.FORMAT_NOTE);
        report.println(Messages.get().container(Messages.RPT_UPDATEXML_PARAMETERS_0), I_CmsReport.FORMAT_HEADLINE);
        report.println(
            Messages.get().container(Messages.RPT_UPDATEXML_PARAMETERS_RESOURCE_PATH_1, resourcePath),
            I_CmsReport.FORMAT_NOTE);
        report.println(
            Messages.get().container(
                Messages.RPT_UPDATEXML_PARAMETERS_INC_SUBFOLDERS_1,
                Boolean.valueOf(inclSubFolder).toString()),
            I_CmsReport.FORMAT_NOTE);

        // check for valid parameters (vfs folder)
        if (CmsStringUtil.isEmpty(resourcePath)) {
            report.println(Messages.get().container(Messages.RPT_UPDATEXML_NO_VFS_FOLDER_0), I_CmsReport.FORMAT_ERROR);
            return;
        }

        // read all files in the vfs folder
        report.println(Messages.get().container(Messages.RPT_UPDATEXML_START_SEARCHING_0), I_CmsReport.FORMAT_HEADLINE);
        List<CmsResource> allFiles = null;
        try {
            allFiles = cmsObject.readResources(resourcePath, CmsResourceFilter.DEFAULT, inclSubFolder);
        } catch (CmsException e) {
            m_errorUpdate += 1;
            report.println(Messages.get().container(Messages.RPT_UPDATEXML_SEARCH_ERROR_0), I_CmsReport.FORMAT_ERROR);
            if (LOG.isErrorEnabled()) {
                LOG.error(e.getMessageContainer(), e);
            }
            report.println(Messages.get().container(Messages.RPT_UPDATEXML_END_UPDATE_0), I_CmsReport.FORMAT_NOTE);
            return;
        }

        // get the files to update
        List<CmsResource> files2Update = new ArrayList<CmsResource>();
        Iterator<CmsResource> iter = allFiles.iterator();
        while (iter.hasNext()) {
            CmsResource cmsResource = iter.next();
            // only update Xml contents
            if (cmsResource.isFile()
                && (CmsResourceTypeXmlContent.isXmlContent(cmsResource)
                    || CmsResourceTypeXmlPage.isXmlPage(cmsResource))) {
                files2Update.add(cmsResource);
            }
        }

        // number of files to update
        int nrOfFiles = files2Update.size();
        report.println(
            Messages.get().container(Messages.RPT_UPDATEXML_FILES_TO_UPDATE_1, Integer.valueOf(nrOfFiles).toString()),
            I_CmsReport.FORMAT_NOTE);
        // the file counter
        int fileCounter = 0;
        // update the files
        if (nrOfFiles > 0) {
            // report entry
            report.println(
                Messages.get().container(Messages.RPT_UPDATEXML_START_UPDATING_0),
                I_CmsReport.FORMAT_HEADLINE);
            // loop over all files
            iter = files2Update.iterator();
            while (iter.hasNext()) {
                CmsResource cmsResource = iter.next();
                fileCounter += 1;
                // report entries
                report.print(
                    org.opencms.report.Messages.get().container(
                        org.opencms.report.Messages.RPT_SUCCESSION_2,
                        String.valueOf(fileCounter),
                        String.valueOf(nrOfFiles)),
                    I_CmsReport.FORMAT_NOTE);
                report.print(Messages.get().container(Messages.RPT_UPDATEXML_CURRENT_FILE_0), I_CmsReport.FORMAT_NOTE);
                report.print(
                    org.opencms.report.Messages.get().container(
                        org.opencms.report.Messages.RPT_ARGUMENT_1,
                        report.removeSiteRoot(cmsResource.getRootPath())));
                report.print(
                    org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0),
                    I_CmsReport.FORMAT_DEFAULT);

                // get current lock from file
                try {
                    // try to lock the resource
                    if (!lockResource(cmsObject, cmsResource, report)) {
                        report.println(
                            Messages.get().container(
                                Messages.RPT_UPDATEXML_LOCKED_FILE_0,
                                cmsObject.getSitePath(cmsResource)),
                            I_CmsReport.FORMAT_ERROR);
                        continue;
                    }
                } catch (CmsException e) {
                    report.println(
                        Messages.get().container(
                            Messages.RPT_UPDATEXML_LOCKED_FILE_0,
                            cmsObject.getSitePath(cmsResource)),
                        I_CmsReport.FORMAT_ERROR);
                    if (LOG.isErrorEnabled()) {
                        LOG.error(e.getMessageContainer(), e);
                    }
                    continue;
                }

                // write the resource
                try {
                    // do not change the date last modified
                    long lastModified = cmsResource.getDateLastModified();
                    CmsFile cmsFile = cmsObject.readFile(cmsResource);
                    cmsFile.setDateLastModified(lastModified);
                    cmsObject.writeFile(cmsFile);
                } catch (Exception e) {
                    m_errorUpdate += 1;
                    report.println(
                        org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_FAILED_0),
                        I_CmsReport.FORMAT_ERROR);
                    if (LOG.isErrorEnabled()) {
                        LOG.error(e.toString());
                    }
                    continue;
                }

                // unlock the resource
                try {
                    cmsObject.unlockResource(cmsObject.getSitePath(cmsResource));
                } catch (CmsException e) {
                    m_errorUpdate += 1;
                    report.println(
                        Messages.get().container(Messages.RPT_UPDATEXML_UNLOCK_FILE_0),
                        I_CmsReport.FORMAT_WARNING);
                    if (LOG.isErrorEnabled()) {
                        LOG.error(e.getMessageContainer(), e);
                    }
                    continue;
                }
                // successfully updated
                report.println(
                    org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0),
                    I_CmsReport.FORMAT_OK);
            }
        } else {
            // no files to update
            report.println(Messages.get().container(Messages.RPT_UPDATEXML_NO_FILES_FOUND_0), I_CmsReport.FORMAT_NOTE);
        }

        // the results are written in the report
        report.println(Messages.get().container(Messages.RPT_UPDATEXML_RESULT_0), I_CmsReport.FORMAT_HEADLINE);
        report.println(
            Messages.get().container(Messages.RPT_UPDATEXML_FILES_TO_UPDATE_1, Integer.valueOf(nrOfFiles).toString()),
            I_CmsReport.FORMAT_NOTE);
        report.println(
            Messages.get().container(
                Messages.RPT_UPDATEXML_UPDATE_NUMBER_ERRORS_1,
                Integer.valueOf(m_errorUpdate).toString()),
            I_CmsReport.FORMAT_NOTE);
        report.println(
            Messages.get().container(Messages.RPT_UPDATEXML_LOCKED_FILES_1, Integer.valueOf(m_lockedFiles).toString()),
            I_CmsReport.FORMAT_NOTE);
        if (m_lockedFiles > 0) {
            report.println(Messages.get().container(Messages.RPT_UPDATEXML_UPDATE_FAILED_0), I_CmsReport.FORMAT_ERROR);
        } else {
            report.println(Messages.get().container(Messages.RPT_UPDATEXML_UPDATE_SUCCESS_0), I_CmsReport.FORMAT_OK);
        }
        report.println(Messages.get().container(Messages.RPT_UPDATEXML_END_UPDATE_0), I_CmsReport.FORMAT_NOTE);
    }
}
