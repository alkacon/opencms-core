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

package org.opencms.workplace.tools.searchindex.sourcesearch;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.loader.CmsLoaderException;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.report.A_CmsReportThread;
import org.opencms.report.I_CmsReport;
import org.opencms.search.CmsSearchException;
import org.opencms.search.solr.CmsSolrIndex;
import org.opencms.search.solr.CmsSolrQuery;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
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
public class CmsSearchReplaceThread extends A_CmsReportThread {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSearchReplaceThread.class);

    /** The number of Solr search results to be processed at maximum. */
    private static final int MAX_PROCESSED_SOLR_RESULTS = 10000;

    /** Number of errors while searching. */
    private int m_errorSearch;

    /** Number of errors while updating. */
    private int m_errorUpdate;

    /** Number of locked files during updating. */
    private int m_lockedFiles;

    /** The found resources. */
    private Set<CmsResource> m_matchedResources = new HashSet<CmsResource>();

    /** The current session. */
    private HttpSession m_session;

    /** Settings. */
    private CmsSearchReplaceSettings m_settings;

    /**
     * Creates a replace html tag Thread.<p>
     *
     * @param session the current session
     * @param cms the current cms object
     * @param settings the settings needed to perform the operation.
     */
    public CmsSearchReplaceThread(HttpSession session, CmsObject cms, CmsSearchReplaceSettings settings) {

        super(cms, "searchAndReplace");
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
        if (CmsStringUtil.isEmpty(m_settings.getReplacepattern()) && !m_settings.isForceReplace()) {
            // empty replace pattern, search only
            report.println(
                Messages.get().container(Messages.RPT_SOURCESEARCH_PARAMETERS_EMPTY_REPLACEPATTERN_0),
                I_CmsReport.FORMAT_NOTE);
        } else {
            // not empty replace pattern, search and replace
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
                    Messages.get().container(Messages.RPT_SOURCESEARCH_INIT_CMS_OBJECT_FAILED_0),
                    I_CmsReport.FORMAT_NOTE);
                replace = false;
            }
        }

        // search the resources and replace the patterns
        if (!isError) {
            List<CmsResource> resources = searchResources(report, cmsObject);

            if (resources.isEmpty()) {
                // no resources found, so search is not possible
                report.println(
                    Messages.get().container(Messages.RPT_SOURCESEARCH_NO_FILES_TO_SEARCH_IN_0),
                    I_CmsReport.FORMAT_NOTE);
            } else {

                report.println(
                    Messages.get().container(
                        Messages.RPT_SOURCESEARCH_NR_OF_FILES_TO_SEARCH_IN_1,
                        Integer.valueOf(resources.size())),
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

                searchAndReplace(cmsObject, resources, replace, report);
            }

            // show the resources
            // save the matched file list in the session
            m_session.setAttribute(
                CmsSearchReplaceSettings.ATTRIBUTE_NAME_SOURCESEARCH_RESULT_LIST,
                m_matchedResources);
        } else {
            // do not show the resources, because there were errors while searching
        }

        report.println(
            Messages.get().container(Messages.RPT_SOURCESEARCH_END_SEARCH_THREAD_0),
            I_CmsReport.FORMAT_HEADLINE);
    }

    /**
     * Search the resources.<p>
     *
     * @param cmsObject the CmsObject using to write files
     * @param resources the relevant resources
     * @param replace true, if search and replace. False is search only.
     * @param report the report.
     */
    protected void searchAndReplace(
        CmsObject cmsObject,
        List<CmsResource> resources,
        boolean replace,
        I_CmsReport report) {

        // the file counter
        int counter = 0;
        int resCount = resources.size();

        // iterate over the files in the selected path
        for (CmsResource resource : resources) {

            try {

                // get the content
                CmsFile file = getCms().readFile(resource);
                byte[] contents = file.getContents();

                // report the current resource
                ++counter;
                report(report, counter, resCount, resource);

                // search and replace
                byte[] result = null;
                boolean xpath = false;
                if ((CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_settings.getXpath())
                    || m_settings.isOnlyContentValues()) && CmsResourceTypeXmlContent.isXmlContent(resource)) {
                    xpath = true;
                }
                if (!xpath) {
                    result = replaceInContent(cmsObject, report, file, contents, replace);
                } else {
                    result = replaceInXml(cmsObject, file, replace, report);
                }

                if ((result != null) && (contents != null) && !contents.equals(result)) {
                    // rewrite the content
                    writeContent(cmsObject, report, file, result);
                }

            } catch (Exception e) {
                report.print(
                    org.opencms.report.Messages.get().container(Messages.RPT_SOURCESEARCH_COULD_NOT_READ_FILE_0),
                    I_CmsReport.FORMAT_ERROR);
                report.addError(e);
                report.println(
                    org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_FAILED_0),
                    I_CmsReport.FORMAT_ERROR);
                m_errorSearch += 1;
                LOG.error(
                    org.opencms.report.Messages.get().container(Messages.RPT_SOURCESEARCH_COULD_NOT_READ_FILE_0),
                    e);
                continue;
            }
        }

        // report results
        reportResults(replace, report, resources.size());
    }

    /**
     * Locks the current resource.<p>
     *
     * @param cms the current CmsObject
     * @param cmsResource the resource to lock
     * @param report the report
     *
     * @return <code>true</code> if the given resource was locked was successfully
     *
     * @throws CmsException if some goes wrong
     */
    private boolean lockResource(CmsObject cms, CmsResource cmsResource, I_CmsReport report) throws CmsException {

        CmsLock lock = cms.getLock(cms.getSitePath(cmsResource));
        // check the lock
        if ((lock != null)
            && lock.isOwnedBy(cms.getRequestContext().getCurrentUser())
            && lock.isOwnedInProjectBy(
                cms.getRequestContext().getCurrentUser(),
                cms.getRequestContext().getCurrentProject())) {
            // prove is current lock from current user in current project
            return true;
        } else if ((lock != null) && !lock.isUnlocked() && !lock.isOwnedBy(cms.getRequestContext().getCurrentUser())) {
            // the resource is not locked by the current user, so can not lock it
            m_lockedFiles += 1;
            return false;
        } else if ((lock != null)
            && !lock.isUnlocked()
            && lock.isOwnedBy(cms.getRequestContext().getCurrentUser())
            && !lock.isOwnedInProjectBy(
                cms.getRequestContext().getCurrentUser(),
                cms.getRequestContext().getCurrentProject())) {
            // prove is current lock from current user but not in current project
            // file is locked by current user but not in current project
            // change the lock
            cms.changeLock(cms.getSitePath(cmsResource));
        } else if ((lock != null) && lock.isUnlocked()) {
            // lock resource from current user in current project
            cms.lockResource(cms.getSitePath(cmsResource));
        }
        lock = cms.getLock(cms.getSitePath(cmsResource));
        if ((lock != null)
            && lock.isOwnedBy(cms.getRequestContext().getCurrentUser())
            && !lock.isOwnedInProjectBy(
                cms.getRequestContext().getCurrentUser(),
                cms.getRequestContext().getCurrentProject())) {
            // resource could not be locked
            m_lockedFiles += 1;

            return false;
        }
        // resource is locked successfully
        return true;
    }

    /**
     * Performs the replacement in content.<p>
     *
     * @param cmsObject the cms context
     * @param report the report to print messages to
     * @param file the file object
     * @param contents the byte content
     * @param replace signals whether to execute a replacement or not
     *
     * @return the new content if a replacement has been performed
     *
     * @throws Exception if something goes wrong
     */
    private byte[] replaceInContent(
        CmsObject cmsObject,
        I_CmsReport report,
        CmsFile file,
        byte[] contents,
        boolean replace)
    throws Exception {

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_settings.getLocale())) {
            Locale contentLocale = CmsLocaleManager.getMainLocale(cmsObject, file);
            if (!contentLocale.toString().equalsIgnoreCase(m_settings.getLocale())) {
                // content does not match the requested locale, skip it
                report.println(
                    Messages.get().container(Messages.RPT_SOURCESEARCH_NOT_MATCHED_0),
                    I_CmsReport.FORMAT_NOTE);
                return null;
            }
        }

        String encoding = CmsLocaleManager.getResourceEncoding(cmsObject, file);
        String content = new String(contents, encoding);

        Matcher matcher;
        matcher = Pattern.compile(m_settings.getSearchpattern()).matcher(content);
        if (matcher.find()) {
            // search pattern did match here, so take this file in the list with matches resources
            m_matchedResources.add(file);
            report.println(Messages.get().container(Messages.RPT_SOURCESEARCH_MATCHED_0), I_CmsReport.FORMAT_OK);
            if (replace) {
                return matcher.replaceAll(m_settings.getReplacepattern()).getBytes(encoding);
            }
        } else {
            // search pattern did not match
            report.println(Messages.get().container(Messages.RPT_SOURCESEARCH_NOT_MATCHED_0), I_CmsReport.FORMAT_NOTE);
        }
        return null;
    }

    /**
     * Performs a replacement for XML contents.<p>
     *
     * @param cmsObject the cms object to perform the actio with
     * @param cmsFile the file to operate on
     * @param replace <code>true</code> if a replacement should be performed
     * @param report the report to print messages to
     *
     * @return the marshaled content
     * @throws Exception if something goes wrong
     */
    private byte[] replaceInXml(CmsObject cmsObject, CmsFile cmsFile, boolean replace, I_CmsReport report)
    throws Exception {

        Exception e = null;
        CmsXmlContent xmlContent = CmsXmlContentFactory.unmarshal(cmsObject, cmsFile);
        Pattern pattern = Pattern.compile(m_settings.getSearchpattern());
        // loop over the locales of the content
        boolean modified = false;
        boolean matched = false;
        String requestedLocale = m_settings.getLocale();
        for (Locale locale : xmlContent.getLocales()) {
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(requestedLocale)
                && !locale.toString().equalsIgnoreCase(requestedLocale)) {
                // does not match the requested locale, skip it
                continue;
            }
            // loop over the available element paths of the current content locale
            List<String> paths = xmlContent.getNames(locale);
            for (String xpath : paths) {
                // try to get the value extraction for the current element path
                I_CmsXmlContentValue value = xmlContent.getValue(xpath, locale);
                if (value.isSimpleType()) {
                    try {
                        String currPath = value.getPath();
                        if (CmsStringUtil.isEmptyOrWhitespaceOnly(m_settings.getXpath())
                            || currPath.equals(m_settings.getXpath())
                            || (CmsXmlUtils.removeXpath(currPath).equals(m_settings.getXpath()))) {
                            // xpath match
                            String oldVal = value.getStringValue(cmsObject);
                            Matcher matcher = pattern.matcher(oldVal);
                            matcher = Pattern.compile(m_settings.getSearchpattern()).matcher(oldVal);
                            if (matcher.find()) {
                                matched = true;
                                m_matchedResources.add(cmsFile);
                                if (replace) {
                                    String newVal = matcher.replaceAll(m_settings.getReplacepattern());
                                    if (!oldVal.equals(newVal)) {
                                        value.setStringValue(cmsObject, newVal);
                                        modified = true;
                                    }
                                }
                            }
                        }
                    } catch (Exception ex) {
                        // log and go on
                        LOG.error(ex.getMessage(), ex);
                        e = ex;
                    }
                }
            }
        }
        if (e != null) {
            throw e;
        }
        if (matched) {
            report.println(Messages.get().container(Messages.RPT_SOURCESEARCH_MATCHED_0), I_CmsReport.FORMAT_OK);
        } else {
            report.println(Messages.get().container(Messages.RPT_SOURCESEARCH_NOT_MATCHED_0), I_CmsReport.FORMAT_NOTE);
        }
        if (modified) {
            return xmlContent.marshal();
        }
        return null;
    }

    /**
     * Reads the content as byte array of the given resource and prints a message to the report.<p>
     *
     * @param report the report
     * @param counter the counter
     * @param resCount the total resource count
     * @param resource the file to get the content for
     */
    private void report(I_CmsReport report, int counter, int resCount, CmsResource resource) {

        // report entries
        report.print(
            org.opencms.report.Messages.get().container(
                org.opencms.report.Messages.RPT_SUCCESSION_2,
                String.valueOf(counter),
                String.valueOf(resCount)),
            I_CmsReport.FORMAT_NOTE);
        report.print(
            org.opencms.report.Messages.get().container(
                org.opencms.report.Messages.RPT_ARGUMENT_1,
                report.removeSiteRoot(resource.getRootPath())));
        report.print(
            org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0),
            I_CmsReport.FORMAT_DEFAULT);
    }

    /**
     * Prints the result messages into the report.<p>
     *
     * @param replace if replacement has to be executed
     * @param report the report to use
     * @param nrOfFiles the total number of files
     */
    private void reportResults(boolean replace, I_CmsReport report, int nrOfFiles) {

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
                Integer.valueOf(nrOfFiles).toString()),
            I_CmsReport.FORMAT_NOTE);
        report.println(
            Messages.get().container(
                Messages.RPT_SOURCESEARCH_NR_OF_FILES_MATCHED_1,
                Integer.valueOf(m_matchedResources.size()).toString()),
            I_CmsReport.FORMAT_NOTE);
        report.println(
            Messages.get().container(
                Messages.RPT_SOURCESEARCH_SEARCH_ERROR_COUNT_1,
                Integer.valueOf(m_errorSearch).toString()),
            I_CmsReport.FORMAT_NOTE);
        if (replace) {
            // replace report entries
            report.println(
                Messages.get().container(
                    Messages.RPT_SOURCESEARCH_REPLACE_ERROR_COUNT_1,
                    Integer.valueOf(m_errorUpdate).toString()),
                I_CmsReport.FORMAT_NOTE);
            report.println(
                Messages.get().container(
                    Messages.RPT_SOURCESEARCH_LOCKED_FILES_1,
                    Integer.valueOf(m_lockedFiles).toString()),
                I_CmsReport.FORMAT_NOTE);
            if (m_matchedResources.size() == 0) {
                report.println(
                    Messages.get().container(Messages.RPT_SOURCESEARCH_NO_FILES_FOUND_0),
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
            if (m_matchedResources.size() == 0) {
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
    }

    /**
     * Searches/reads all resources that are relevant.<p>
     *
     * @param report the report
     * @param cmsObject the cms Object to use
     *
     * @return the relevant resources
     */
    private List<CmsResource> searchResources(I_CmsReport report, CmsObject cmsObject) {

        report.println(
            Messages.get().container(Messages.RPT_SOURCESEARCH_START_COLLECTING_FILES_TO_SEARCH_IN_0),
            I_CmsReport.FORMAT_HEADLINE);

        List<CmsResource> resources = new ArrayList<CmsResource>();
        if (m_settings.isSolrSearch()) {
            CmsSolrIndex index = OpenCms.getSearchManager().getIndexSolr(m_settings.getSource());
            if (index != null) {
                CmsSolrQuery query = new CmsSolrQuery(
                    null,
                    CmsRequestUtil.createParameterMap(m_settings.getQuery() + "&fl=path,type"));
                query.setSearchRoots(m_settings.getPaths());
                if ((m_settings.getTypesArray() != null) && (m_settings.getTypesArray().length > 0)) {
                    query.setResourceTypes(m_settings.getTypesArray());
                }
                query.setRows(Integer.valueOf(MAX_PROCESSED_SOLR_RESULTS));
                query.ensureParameters();
                try {
                    resources.addAll(
                        index.search(cmsObject, query, true, null, false, null, MAX_PROCESSED_SOLR_RESULTS));
                } catch (CmsSearchException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        } else {
            CmsResourceFilter filter = CmsResourceFilter.ALL.addRequireFile().addExcludeState(
                CmsResource.STATE_DELETED).addRequireTimerange().addRequireVisible();
            if ((m_settings.getTypesArray() != null) && (m_settings.getTypesArray().length > 0)) {
                for (String resTypeName : m_settings.getTypesArray()) {
                    try {
                        I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(resTypeName);
                        filter = filter.addRequireType(type);
                    } catch (CmsLoaderException e) {
                        // noop
                    } catch (NullPointerException e) {
                        // noop
                    }
                }
            }

            // iterate over all selected paths
            Iterator<String> iterPaths = m_settings.getPaths().iterator();
            while (iterPaths.hasNext()) {
                String path = iterPaths.next();
                try {
                    // only read resources which are files and not deleted, which are in the current time range window and where the current
                    // user has the sufficient permissions to read them
                    List<CmsResource> tmpResources = getCms().readResources(path, filter);
                    if ((tmpResources != null) && !tmpResources.isEmpty()) {
                        resources.addAll(tmpResources);
                    }
                } catch (CmsException e) {
                    // an error occured
                    LOG.error(Messages.get().container(Messages.RPT_SOURCESEARCH_ERROR_READING_RESOURCES_1, path), e);
                    report.println(
                        Messages.get().container(Messages.RPT_SOURCESEARCH_ERROR_READING_RESOURCES_1, path),
                        I_CmsReport.FORMAT_ERROR);
                }
            }
        }
        return resources;
    }

    /**
     * Writes the file contents.<p>
     *
     * @param cmsObject the cms context
     * @param report the report
     * @param file the file to write
     * @param content the file content
     *
     * @return success flag
     */
    private boolean writeContent(CmsObject cmsObject, I_CmsReport report, CmsFile file, byte[] content) {

        boolean success = true;

        // get current lock from file
        try {
            // try to lock the resource
            if (!lockResource(cmsObject, file, report)) {
                report.println(
                    Messages.get().container(Messages.RPT_SOURCESEARCH_LOCKED_FILE_0, cmsObject.getSitePath(file)),
                    I_CmsReport.FORMAT_ERROR);
                success = false;
            }
        } catch (CmsException e) {
            report.println(
                Messages.get().container(Messages.RPT_SOURCESEARCH_LOCKED_FILE_0, cmsObject.getSitePath(file)),
                I_CmsReport.FORMAT_ERROR);
            if (LOG.isErrorEnabled()) {
                LOG.error(e.getMessageContainer(), e);
            }
            success = false;
        }

        // write the file content
        try {
            file.setContents(content);
            cmsObject.writeFile(file);
        } catch (Exception e) {
            m_errorUpdate += 1;
            report.println(
                org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_FAILED_0),
                I_CmsReport.FORMAT_ERROR);
            if (LOG.isErrorEnabled()) {
                LOG.error(e.toString());
            }
            success = false;
        }

        // unlock the resource
        try {
            cmsObject.unlockResource(cmsObject.getSitePath(file));
        } catch (CmsException e) {
            m_errorUpdate += 1;
            report.println(
                Messages.get().container(Messages.RPT_SOURCESEARCH_UNLOCK_FILE_0),
                I_CmsReport.FORMAT_WARNING);
            if (LOG.isErrorEnabled()) {
                LOG.error(e.getMessageContainer(), e);
            }
            success = false;
        }

        if (success) {
            // successfully updated
            report.println(
                org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0),
                I_CmsReport.FORMAT_OK);
        }

        return success;
    }
}
