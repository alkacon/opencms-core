/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.workplace.tools.searchindex.sourcesearch;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.report.A_CmsReportThread;
import org.opencms.report.I_CmsReport;
import org.opencms.util.CmsStringUtil;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;

/**
 * Searches in sources.
 * <p>
 * 
 * @since 7.5.3
 */
public class CmsSourceSearchThread extends A_CmsReportThread {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSourceSearchThread.class);

    /** Number of errors while searching. */
    private int m_errorSearch;

    /** Number of errors while updating. */
    private int m_errorUpdate;

    /** Number of locked files during updating. */
    private int m_lockedFiles;

    /** The found resources. */
    private List<CmsResource> m_matchedResources = new ArrayList<CmsResource>();

    /** The current session. */
    private HttpSession m_session;

    /** Settings. */
    private CmsSourceSearchSettings m_settings;

    /**
     * Creates a replace html tag Thread.<p>
     * 
     * @param session the current session
     * @param cms the current cms object
     * @param settings the settings needed to perform the operation.
     */
    public CmsSourceSearchThread(HttpSession session, CmsObject cms, CmsSourceSearchSettings settings) {

        super(cms, Messages.get().getBundle().key(Messages.GUI_SOURCESEARCH_THREAD_NAME_0));
        initHtmlReport(cms.getRequestContext().getLocale());
        m_session = session;
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

        // get the report
        I_CmsReport report = getReport();
        boolean isError = false;
        report.println(
            Messages.get().container(Messages.RPT_SOURCESEARCH_BEGIN_SEARCH_THREAD_0),
            I_CmsReport.FORMAT_HEADLINE);
        // write parameters to report
        report.println(Messages.get().container(Messages.RPT_SOURCESEARCH_PARAMETERS_0), I_CmsReport.FORMAT_HEADLINE);
        // the paths
        if (!m_settings.getPaths().isEmpty()) {
            // iterate over the paths
            Iterator<String> iter = m_settings.getPaths().iterator();
            while (iter.hasNext()) {
                String path = iter.next();
                report.println(
                    Messages.get().container(Messages.RPT_SOURCESEARCH_PARAMETERS_RESOURCE_PATH_1, path),
                    I_CmsReport.FORMAT_NOTE);
            }
        } else {
            // no paths selected
            isError = true;
            report.println(
                Messages.get().container(Messages.RPT_SOURCESEARCH_PARAMETERS_EMPTY_RESOURCE_PATHS_0),
                I_CmsReport.FORMAT_ERROR);
        }
        // the search pattern
        if (!CmsStringUtil.isEmptyOrWhitespaceOnly(m_settings.getSearchpattern())) {
            // there is a search pattern
            report.println(
                Messages.get().container(
                    Messages.RPT_SOURCESEARCH_PARAMETERS_SEARCHPATTERN_1,
                    CmsStringUtil.escapeHtml(m_settings.getSearchpattern())),
                I_CmsReport.FORMAT_NOTE);
        } else {
            // empty search pattern
            isError = true;
            report.println(
                Messages.get().container(Messages.RPT_SOURCESEARCH_PARAMETERS_EMPTY_SEARCHPATTERN_0),
                I_CmsReport.FORMAT_ERROR);
        }
        // the replace pattern
        report.println(
            Messages.get().container(
                Messages.RPT_SOURCESEARCH_PARAMETERS_REPLACEPATTERN_1,
                CmsStringUtil.escapeHtml(m_settings.getReplacepattern())),
            I_CmsReport.FORMAT_NOTE);
        // the project
        report.println(
            Messages.get().container(Messages.RPT_SOURCESEARCH_PARAMETERS_PROJECT_1, m_settings.getProject()),
            I_CmsReport.FORMAT_NOTE);
        // remarks for search/replace dependent od the replace pattern and the selected project
        // in the online project search is possible only
        // in other projects there is replaced, if the replace pattern is not empty
        boolean replace = false;
        if (CmsStringUtil.isEmpty(m_settings.getReplacepattern())) {
            // empty search pattern, search only   
            report.println(
                Messages.get().container(Messages.RPT_SOURCESEARCH_PARAMETERS_EMPTY_REPLACEPATTERN_0),
                I_CmsReport.FORMAT_NOTE);
        } else {
            // not empty search pattern, search and replace   
            replace = true;
            report.println(
                Messages.get().container(Messages.RPT_SOURCESEARCH_PARAMETERS_NOTEMPTY_REPLACEPATTERN_0),
                I_CmsReport.FORMAT_NOTE);
        }

        // make an OpenCms object copy if replace is active
        CmsObject cmsObject = getCms();
        if (replace && !m_settings.getProject().equals(cmsObject.getRequestContext().getCurrentProject().getName())) {
            try {
                cmsObject = OpenCms.initCmsObject(getCms());
                CmsProject cmsProject = getCms().readProject(m_settings.getProject());
                cmsObject.getRequestContext().setCurrentProject(cmsProject);
            } catch (CmsException e) {
                report.println(
                    Messages.get().container(Messages.RPT_SOURCESEARCH_WRONG_ENVIRONMENT_REPLACING_0),
                    I_CmsReport.FORMAT_NOTE);
                replace = false;
            }
        }

        // search the resources and replace the patterns
        if (!isError && searchResources(report, replace, cmsObject)) {
            // show the resources
            // save the matched file list in the session
            m_session.setAttribute(CmsSourceSearchSettings.ATTRIBUTE_NAME_SOURCESEARCH_RESULT_LIST, m_matchedResources);
        } else {
            // do not show the resources, because there were errors while searching
        }

        report.println(
            Messages.get().container(Messages.RPT_SOURCESEARCH_END_SEARCH_THREAD_0),
            I_CmsReport.FORMAT_HEADLINE);
    }

    /**
     * Locks the current resource.<p>
     * 
     * @param cmsObject the current CmsObject
     * @param cmsResource the resource to lock
     * @param report the report
     * 
     * @return <code>true</code> if the resource could be locked
     * 
     * @throws CmsException if some goes wrong
     */
    private boolean lockResource(CmsObject cmsObject, CmsResource cmsResource, I_CmsReport report) throws CmsException {

        CmsLock lock = cmsObject.getLock(cmsObject.getSitePath(cmsResource));
        // check the lock
        if ((lock != null)
            && lock.isOwnedBy(cmsObject.getRequestContext().getCurrentUser())
            && lock.isOwnedInProjectBy(
                cmsObject.getRequestContext().getCurrentUser(),
                cmsObject.getRequestContext().getCurrentProject())) {
            // prove is current lock from current user in current project
            return true;
        } else if ((lock != null)
            && !lock.isUnlocked()
            && !lock.isOwnedBy(cmsObject.getRequestContext().getCurrentUser())) {
            // the resource is not locked by the current user, so can not lock it
            m_lockedFiles += 1;
            return false;
        } else if ((lock != null)
            && !lock.isUnlocked()
            && lock.isOwnedBy(cmsObject.getRequestContext().getCurrentUser())
            && !lock.isOwnedInProjectBy(
                cmsObject.getRequestContext().getCurrentUser(),
                cmsObject.getRequestContext().getCurrentProject())) {
            // prove is current lock from current user but not in current project
            // file is locked by current user but not in current project
            // change the lock 
            cmsObject.changeLock(cmsObject.getSitePath(cmsResource));
        } else if ((lock != null) && lock.isUnlocked()) {
            // lock resource from current user in current project
            cmsObject.lockResource(cmsObject.getSitePath(cmsResource));
        }
        lock = cmsObject.getLock(cmsObject.getSitePath(cmsResource));
        if ((lock != null)
            && lock.isOwnedBy(cmsObject.getRequestContext().getCurrentUser())
            && !lock.isOwnedInProjectBy(
                cmsObject.getRequestContext().getCurrentUser(),
                cmsObject.getRequestContext().getCurrentProject())) {
            // resource could not be locked
            m_lockedFiles += 1;

            return false;
        }
        // resource is locked successfully
        return true;
    }

    /**
     * Search the resources.<p>
     * 
     * @param report the report.
     * @param replace true, if search and replace. False is search only.
     * @param cmsObject the CmsObject using to write files
     * 
     * @return true, if searching was successful, otherwise false. 
     */
    private boolean searchResources(I_CmsReport report, boolean replace, CmsObject cmsObject) {

        // collect all file contents in the selected folder
        report.println(
            Messages.get().container(Messages.RPT_SOURCESEARCH_START_COLLECTING_FILES_TO_SEARCH_IN_0),
            I_CmsReport.FORMAT_HEADLINE);
        List<CmsResource> resources = new ArrayList<CmsResource>();
        // iterate over all selected paths
        Iterator<String> iterPaths = m_settings.getPaths().iterator();
        while (iterPaths.hasNext()) {
            String path = iterPaths.next();
            try {
                // only read resources which are files and not deleted, which are in the current time range window and where the current 
                // user has the sufficient permissions to read them
                List<CmsResource> tmpResources = getCms().readResources(
                    path,
                    CmsResourceFilter.ALL.addRequireFile().addExcludeState(CmsResource.STATE_DELETED).addRequireTimerange().addRequireVisible());
                if ((tmpResources != null) && !tmpResources.isEmpty()) {
                    resources.addAll(tmpResources);
                }
            } catch (CmsException e) {
                // an error occured
                LOG.error(Messages.get().container(Messages.RPT_SOURCESEARCH_ERROR_READING_RESOURCES_1, path), e);
                report.println(
                    Messages.get().container(Messages.RPT_SOURCESEARCH_ERROR_READING_RESOURCES_1, path),
                    I_CmsReport.FORMAT_ERROR);
                return false;
            }
        }
        if (resources.isEmpty()) {
            // no resources found, so search is not possible
            report.println(
                Messages.get().container(Messages.RPT_SOURCESEARCH_NO_FILES_TO_SEARCH_IN_0),
                I_CmsReport.FORMAT_NOTE);
            return true;
        }
        // at least one file in the select path could be read
        // number of files to update
        int nrOfFiles = resources.size();
        report.println(
            Messages.get().container(Messages.RPT_SOURCESEARCH_NR_OF_FILES_TO_SEARCH_IN_1, new Integer(nrOfFiles)),
            I_CmsReport.FORMAT_NOTE);
        if (replace) {
            // start searching and replacing
            report.println(
                Messages.get().container(Messages.RPT_SOURCESEARCH_START_SEARCHING_REPLACING_0),
                I_CmsReport.FORMAT_HEADLINE);
        } else {
            // start searching
            report.println(
                Messages.get().container(Messages.RPT_SOURCESEARCH_START_SEARCHING_0),
                I_CmsReport.FORMAT_HEADLINE);
        }
        // iterate over the files in the selected path
        Iterator<CmsResource> iterResources = resources.iterator();
        // the file counter
        int fileCounter = 0;
        int matchedFiles = 0;
        while (iterResources.hasNext()) {
            CmsResource cmsResource = iterResources.next();
            fileCounter += 1;
            CmsFile cmsFile = null;
            try {
                cmsFile = getCms().readFile(cmsResource);
            } catch (CmsException e) {
                report.print(
                    org.opencms.report.Messages.get().container(Messages.RPT_SOURCESEARCH_COULD_NOT_READ_FILE_0),
                    I_CmsReport.FORMAT_ERROR);
                report.println(
                    org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_FAILED_0),
                    I_CmsReport.FORMAT_ERROR);
                m_errorSearch += 1;
                continue;
            }
            // report entries
            report.print(
                org.opencms.report.Messages.get().container(
                    org.opencms.report.Messages.RPT_SUCCESSION_2,
                    String.valueOf(fileCounter),
                    String.valueOf(nrOfFiles)),
                I_CmsReport.FORMAT_NOTE);
            report.print(org.opencms.report.Messages.get().container(
                org.opencms.report.Messages.RPT_ARGUMENT_1,
                report.removeSiteRoot(cmsResource.getRootPath())));
            report.print(
                org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0),
                I_CmsReport.FORMAT_DEFAULT);
            byte[] contents = cmsFile.getContents();
            String encoding = CmsLocaleManager.getResourceEncoding(cmsObject, cmsFile);
            String content = null;
            try {
                content = new String(contents, encoding);
            } catch (UnsupportedEncodingException e1) {
                report.print(
                    org.opencms.report.Messages.get().container(Messages.RPT_SOURCESEARCH_COULD_NOT_READ_FILE_0),
                    I_CmsReport.FORMAT_ERROR);
                report.println(
                    org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_FAILED_0),
                    I_CmsReport.FORMAT_ERROR);
                m_errorSearch += 1;
                continue;
            }
            boolean matched = false;
            Matcher matcher = null;
            try {
                matcher = Pattern.compile(m_settings.getSearchpattern()).matcher(content);
                if (matcher.find()) {
                    // search pattern did match here, so take this file in the list with matches resources
                    m_matchedResources.add(cmsResource);
                    matched = true;
                    matchedFiles += 1;
                    if (replace) {
                        report.print(
                            Messages.get().container(Messages.RPT_SOURCESEARCH_MATCHED_0),
                            I_CmsReport.FORMAT_OK);
                    } else {
                        report.println(
                            Messages.get().container(Messages.RPT_SOURCESEARCH_MATCHED_0),
                            I_CmsReport.FORMAT_OK);
                    }
                } else {
                    // search pattern did not match
                    report.println(
                        Messages.get().container(Messages.RPT_SOURCESEARCH_NOT_MATCHED_0),
                        I_CmsReport.FORMAT_NOTE);
                }
            } catch (Exception e) {
                report.println(
                    Messages.get().container(Messages.RPT_SOURCESEARCH_APPLY_PATTERN_ERROR_1, e),
                    I_CmsReport.FORMAT_ERROR);
                m_errorSearch += 1;
                continue;
            }

            // replace if matched and configured
            if (replace && matched) {
                // get current lock from file
                try {
                    // try to lock the resource
                    if (!lockResource(cmsObject, cmsResource, report)) {
                        report.println(
                            Messages.get().container(
                                Messages.RPT_SOURCESEARCH_LOCKED_FILE_0,
                                cmsObject.getSitePath(cmsResource)),
                            I_CmsReport.FORMAT_ERROR);
                        continue;
                    }
                } catch (CmsException e) {
                    report.println(
                        Messages.get().container(
                            Messages.RPT_SOURCESEARCH_LOCKED_FILE_0,
                            cmsObject.getSitePath(cmsResource)),
                        I_CmsReport.FORMAT_ERROR);
                    if (LOG.isErrorEnabled()) {
                        LOG.error(e.getMessageContainer(), e);
                    }
                    continue;
                }

                // replace the content
                content = matcher.replaceAll(m_settings.getReplacepattern());
                // write the resource
                try {
                    cmsFile.setContents(content.getBytes(encoding));
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
                        Messages.get().container(Messages.RPT_SOURCESEARCH_UNLOCK_FILE_0),
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
        }
        // report entries
        if (replace) {
            // finish searching and replacing
            report.println(
                Messages.get().container(Messages.RPT_SOURCESEARCH_END_SEARCHING_REPLACING_0),
                I_CmsReport.FORMAT_HEADLINE);
        } else {
            // finish searching
            report.println(
                Messages.get().container(Messages.RPT_SOURCESEARCH_END_SEARCHING_0),
                I_CmsReport.FORMAT_HEADLINE);
        }
        // the results are written in the report
        report.println(Messages.get().container(Messages.RPT_SOURCESEARCH_RESULT_0), I_CmsReport.FORMAT_HEADLINE);
        report.println(
            Messages.get().container(
                Messages.RPT_SOURCESEARCH_NR_OF_FILES_TO_SEARCH_IN_1,
                new Integer(nrOfFiles).toString()),
            I_CmsReport.FORMAT_NOTE);
        report.println(
            Messages.get().container(
                Messages.RPT_SOURCESEARCH_NR_OF_FILES_MATCHED_1,
                new Integer(matchedFiles).toString()),
            I_CmsReport.FORMAT_NOTE);
        report.println(
            Messages.get().container(
                Messages.RPT_SOURCESEARCH_NUMBER_OF_SEARCH_ERRORS_1,
                new Integer(m_errorSearch).toString()),
            I_CmsReport.FORMAT_NOTE);
        if (replace) {
            // replace report entries
            report.println(
                Messages.get().container(
                    Messages.RPT_SOURCESEARCH_NUMBER_OF_REPLACE_ERRORS_1,
                    new Integer(m_errorUpdate).toString()),
                I_CmsReport.FORMAT_NOTE);
            report.println(
                Messages.get().container(
                    Messages.RPT_SOURCESEARCH_LOCKED_FILES_1,
                    new Integer(m_lockedFiles).toString()),
                I_CmsReport.FORMAT_NOTE);
            if (matchedFiles == 0) {
                report.println(
                    Messages.get().container(Messages.RPT_SOURCESEARCH_NO_FILES_TO_REPLACE_FOUND_0),
                    I_CmsReport.FORMAT_OK);
            } else {
                report.println(
                    Messages.get().container(Messages.RPT_SOURCESEARCH_CLICK_OK_TO_GET_LIST_0),
                    I_CmsReport.FORMAT_OK);
            }
            if (m_lockedFiles > 0) {
                report.println(
                    Messages.get().container(Messages.RPT_SOURCESEARCH_REPLACE_FAILED_0),
                    I_CmsReport.FORMAT_ERROR);
            } else {
                report.println(
                    Messages.get().container(Messages.RPT_SOURCESEARCH_REPLACE_SUCCESS_0),
                    I_CmsReport.FORMAT_OK);
            }
        } else {
            // search report entries
            if (matchedFiles == 0) {
                report.println(
                    Messages.get().container(Messages.RPT_SOURCESEARCH_NO_FILES_FOUND_0),
                    I_CmsReport.FORMAT_OK);
            } else {
                report.println(
                    Messages.get().container(Messages.RPT_SOURCESEARCH_CLICK_OK_TO_GET_LIST_0),
                    I_CmsReport.FORMAT_OK);
            }
            if (m_errorSearch > 0) {
                // only searching failed
                report.println(
                    Messages.get().container(Messages.RPT_SOURCESEARCH_SEARCH_FAILED_0),
                    I_CmsReport.FORMAT_ERROR);
            } else {
                // only searching was successful   
                report.println(
                    Messages.get().container(Messages.RPT_SOURCESEARCH_SEARCH_SUCCESS_0),
                    I_CmsReport.FORMAT_OK);
            }
        }
        // searching was successful
        return true;

    }
}
