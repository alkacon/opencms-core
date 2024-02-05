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

package org.opencms.ui.apps.search;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.jsp.CmsJspTagContainer;
import org.opencms.loader.CmsLoaderException;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.report.A_CmsReportThread;
import org.opencms.report.I_CmsReport;
import org.opencms.report.I_CmsReportUpdateFormatter;
import org.opencms.search.CmsSearchException;
import org.opencms.search.solr.CmsSolrIndex;
import org.opencms.search.solr.CmsSolrQuery;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.apps.search.CmsSourceSearchForm.SearchType;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.containerpage.CmsContainerElementBean;
import org.opencms.xml.containerpage.CmsXmlContainerPage;
import org.opencms.xml.containerpage.CmsXmlContainerPageFactory;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;
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

    /** Time after which the search operation is cancelled if no report update is requested. */
    public static final long ABANDON_TIMEOUT = TimeUnit.MINUTES.toMillis(5);

    /** Number of errors while searching. */
    private int m_errorSearch;

    /** Number of errors while updating. */
    private int m_errorUpdate;

    /** Number of locked files during updating. */
    private int m_lockedFiles;

    /** The found resources. */
    private Set<CmsResource> m_matchedResources = new LinkedHashSet<CmsResource>();

    /** The replace flag. */
    private boolean m_replace;

    /** Settings. */
    private CmsSearchReplaceSettings m_settings;

    /** True if this thread was started with a non-null session argument. */
    private boolean m_hasSession;

    /** Timestamp of last report update. */
    private volatile long m_lastTimestamp = -1;

    /**
     * Creates a replace html tag Thread.<p>
     *
     * @param session the current session
     * @param cms the current cms object
     * @param settings the settings needed to perform the operation.
     */
    public CmsSearchReplaceThread(HttpSession session, CmsObject cms, CmsSearchReplaceSettings settings) {

        super(cms, "searchAndReplace");
        m_hasSession = session != null;
        initHtmlReport(cms.getRequestContext().getLocale());
        m_settings = settings;
    }

    /**
     * Creates a replace html tag Thread.<p>
     *
     * @param session the current session
     * @param cms the current cms object
     * @param settings the settings needed to perform the operation.
     */
    public CmsSearchReplaceThread(
        HttpSession session,
        CmsObject cms,
        CmsSearchReplaceSettings settings,
        I_CmsReport report) {

        super(cms, "searchAndReplace");
        m_report = report;
        m_settings = settings;
    }

    /**
     * Returns the matched resources.<p>
     *
     * @return the matched resources
     */
    public List<CmsResource> getMatchedResources() {

        if (m_replace) {
            // re-read the resources to include changes
            List<CmsResource> result = new ArrayList<CmsResource>();
            for (CmsResource resource : m_matchedResources) {
                try {
                    result.add(getCms().readResource(resource.getStructureId()));
                } catch (CmsException e) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
            return result;
        } else {
            return new ArrayList<CmsResource>(m_matchedResources);
        }
    }

    /**
     * @see org.opencms.report.A_CmsReportThread#getReportUpdate()
     */
    @Override
    public String getReportUpdate() {

        m_lastTimestamp = System.currentTimeMillis();
        return getReport().getReportUpdate();
    }

    /**
     * @see org.opencms.report.A_CmsReportThread#getReportUpdate(org.opencms.report.I_CmsReportUpdateFormatter)
     */
    @Override
    public String getReportUpdate(I_CmsReportUpdateFormatter formatter) {

        m_lastTimestamp = System.currentTimeMillis();
        return super.getReportUpdate(formatter);
    }

    /**
     * Returns true if the last report update is too far back in time, so the user has probably closed the window/tab.
     *
     * @return true if the last report update is too far back
     */
    public boolean isAbandoned() {

        boolean result = m_hasSession
            && (m_lastTimestamp != -1)
            && ((System.currentTimeMillis() - m_lastTimestamp) > ABANDON_TIMEOUT);
        return result;
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
        if (CmsStringUtil.isEmpty(m_settings.getReplacepattern()) && !m_settings.isForceReplace()) {
            report.println(
                Messages.get().container(Messages.RPT_SOURCESEARCH_PARAMETERS_EMPTY_REPLACEPATTERN_0),
                I_CmsReport.FORMAT_NOTE);
        } else {
            // not empty replace pattern, search and replace
            m_replace = true;
            report.println(
                Messages.get().container(Messages.RPT_SOURCESEARCH_PARAMETERS_NOTEMPTY_REPLACEPATTERN_0),
                I_CmsReport.FORMAT_NOTE);
        }

        // make an OpenCms object copy if replace is active
        CmsObject cmsObject = getCms();
        if (m_replace && !m_settings.getProject().equals(cmsObject.getRequestContext().getCurrentProject().getName())) {
            try {
                cmsObject = OpenCms.initCmsObject(getCms());
                CmsProject cmsProject = getCms().readProject(m_settings.getProject());
                cmsObject.getRequestContext().setCurrentProject(cmsProject);
            } catch (CmsException e) {
                report.println(
                    Messages.get().container(Messages.RPT_SOURCESEARCH_INIT_CMS_OBJECT_FAILED_0),
                    I_CmsReport.FORMAT_NOTE);
                m_replace = false;
            }
        }

        // search the resources and replace the patterns
        if (!isError) {
            List<CmsResource> resources = searchResources();

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
                if (m_replace) {
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

                if (m_settings.getType().isPropertySearch()) {
                    searchProperties(resources);
                } else {
                    searchAndReplace(resources);
                }
            }

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
     * @param resources the relevant resources
     */
    protected void searchAndReplace(List<CmsResource> resources) {

        // the file counter
        int counter = 0;
        int resCount = resources.size();
        I_CmsReport report = getReport();
        // iterate over the files in the selected path
        for (CmsResource resource : resources) {
            if (isAbandoned() && !m_replace) { // if it's not a replace operation, it's safe to cancel
                return;
            }

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
                    result = replaceInContent(file, contents);
                } else {
                    result = replaceInXml(file);
                }

                if ((result != null) && (contents != null) && !contents.equals(result)) {
                    // rewrite the content
                    writeContent(file, result);
                } else {
                    getReport().println();
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
        reportResults(resources.size());
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
                } else
            if ((lock != null) && lock.isUnlocked()) {
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
     * Renames a nested container within a container page XML.<p>
     *
     * @param targetContainerPage the target container page
     * @param layoutResource the container element resource generating the nested container
     * @param oldName the old container name
     * @param newName the new container name
     *
     * @return the changed content bytes
     *
     * @throws Exception in case unmarshalling of the container page fails
     */
    private byte[] renameNestedContainers(
        CmsFile targetContainerPage,
        CmsResource layoutResource,
        String oldName,
        String newName)
    throws Exception {

        byte[] contents = targetContainerPage.getContents();
        Set<String> replaceElementIds = new HashSet<String>();
        try {
            CmsXmlContainerPage page = CmsXmlContainerPageFactory.unmarshal(getCms(), targetContainerPage);
            for (CmsContainerElementBean element : page.getContainerPage(getCms()).getElements()) {
                if (element.getId().equals(layoutResource.getStructureId()) && (element.getInstanceId() != null)) {
                    replaceElementIds.add(element.getInstanceId());
                }
            }
            if (replaceElementIds.size() > 0) {
                String encoding = CmsLocaleManager.getResourceEncoding(getCms(), targetContainerPage);
                String content = new String(contents, encoding);
                for (String instanceId : replaceElementIds) {
                    Pattern patt = Pattern.compile(
                        CmsJspTagContainer.getNestedContainerName(oldName, instanceId, null));
                    Matcher m = patt.matcher(content);
                    StringBuffer sb = new StringBuffer(content.length());
                    while (m.find()) {
                        m.appendReplacement(
                            sb,
                            Matcher.quoteReplacement(
                                CmsJspTagContainer.getNestedContainerName(newName, instanceId, null)));
                    }
                    m.appendTail(sb);
                    content = sb.toString();
                }
                contents = content.getBytes(encoding);
            }
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw e;
        }
        return contents;
    }

    /**
     * Performs the replacement in content.<p>
     *
     * @param file the file object
     * @param contents the byte content
     *
     * @return the new content if a replacement has been performed
     *
     * @throws Exception if something goes wrong
     */
    private byte[] replaceInContent(CmsFile file, byte[] contents) throws Exception {

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_settings.getLocale())) {
            Locale contentLocale = CmsLocaleManager.getMainLocale(getCms(), file);
            if (!contentLocale.toString().equalsIgnoreCase(m_settings.getLocale())) {
                // content does not match the requested locale, skip it
                getReport().println(
                    Messages.get().container(Messages.RPT_SOURCESEARCH_NOT_MATCHED_0),
                    I_CmsReport.FORMAT_NOTE);
                return null;
            }
        }

        String encoding = CmsLocaleManager.getResourceEncoding(getCms(), file);
        String content = new String(contents, encoding);

        if (CmsSourceSearchForm.REGEX_ALL.equals(m_settings.getSearchpattern()) & !m_replace) {
            m_matchedResources.add(file);
            getReport().print(Messages.get().container(Messages.RPT_SOURCESEARCH_MATCHED_0), I_CmsReport.FORMAT_OK);
            return null;
        }

        Matcher matcher = Pattern.compile(m_settings.getSearchpattern()).matcher(content);

        if (matcher.find()) {
            // search pattern did match here, so take this file in the list with matches resources
            m_matchedResources.add(file);
            getReport().print(Messages.get().container(Messages.RPT_SOURCESEARCH_MATCHED_0), I_CmsReport.FORMAT_OK);
            if (m_replace) {
                if (m_settings.getType().equals(SearchType.renameContainer)) {

                    return renameNestedContainers(
                        file,
                        m_settings.getElementResource(),
                        m_settings.getReplacepattern().split(";")[0],
                        m_settings.getReplacepattern().split(";")[1]);
                }
                return matcher.replaceAll(m_settings.getReplacepattern()).getBytes(encoding);
            }
        } else {
            // search pattern did not match
            getReport().print(
                Messages.get().container(Messages.RPT_SOURCESEARCH_NOT_MATCHED_0),
                I_CmsReport.FORMAT_NOTE);
        }
        return null;
    }

    /**
     * Performs a replacement for XML contents.<p>
     *
     * @param cmsFile the file to operate on
     *
     * @return the marshaled content
     * @throws Exception if something goes wrong
     */
    private byte[] replaceInXml(CmsFile cmsFile) throws Exception {

        Exception e = null;
        CmsXmlContent xmlContent = CmsXmlContentFactory.unmarshal(getCms(), cmsFile);
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
                            String oldVal = value.getStringValue(getCms());
                            Matcher matcher = pattern.matcher(oldVal);
                            matcher = Pattern.compile(m_settings.getSearchpattern()).matcher(oldVal);
                            if (matcher.find()) {
                                matched = true;
                                m_matchedResources.add(cmsFile);
                                if (m_replace) {
                                    String newVal = matcher.replaceAll(m_settings.getReplacepattern());
                                    if (!oldVal.equals(newVal)) {
                                        value.setStringValue(getCms(), newVal);
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
            getReport().println(Messages.get().container(Messages.RPT_SOURCESEARCH_MATCHED_0), I_CmsReport.FORMAT_OK);
        } else {
            getReport().println(
                Messages.get().container(Messages.RPT_SOURCESEARCH_NOT_MATCHED_0),
                I_CmsReport.FORMAT_NOTE);
        }
        if (modified) {
            return xmlContent.marshal();
        }
        return null;
    }

    /**
     * Replace properties of given resources.<p>
     *
     * @param matchedResources to replace properties
     */
    private void replaceProperties(Set<CmsResource> matchedResources) {

        for (CmsResource resource : matchedResources) {
            try {
                CmsProperty prop = getCms().readPropertyObject(resource, m_settings.getProperty().getName(), false);
                Matcher matcher = Pattern.compile(m_settings.getSearchpattern()).matcher(prop.getValue());
                if (m_settings.getReplacepattern().isEmpty()) {
                    prop.setValue("", "");
                } else {
                    prop.setValue(matcher.replaceAll(m_settings.getReplacepattern()), CmsProperty.TYPE_INDIVIDUAL);
                }
                getCms().lockResource(resource);
                getCms().writePropertyObjects(resource, Collections.singletonList(prop));
                getCms().unlockResource(resource);
            } catch (CmsException e) {
                LOG.error("Ubable to change property", e);
            }
        }
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
     * @param nrOfFiles the total number of files
     */
    private void reportResults(int nrOfFiles) {

        I_CmsReport report = getReport();
        // report entries
        if (m_replace) {
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
        if (m_replace) {
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
     * Search and replace function for properties.<p>
     *
     * @param resources to be considered
     */
    private void searchProperties(List<CmsResource> resources) {

        if (CmsSourceSearchForm.REGEX_ALL.equals(m_settings.getSearchpattern())) {
            for (CmsResource resource : resources) {
                m_matchedResources.add(resource);
                getReport().println(
                    Messages.get().container(Messages.RPT_SOURCESEARCH_MATCHED_0),
                    I_CmsReport.FORMAT_OK);
            }

        } else {
            for (CmsResource resource : resources) {
                if (isAbandoned() && !m_replace) { // if it's not a replace operation, it's safe to cancel
                    return;
                }
                Matcher matcher;
                try {
                    CmsProperty prop = getCms().readPropertyObject(resource, m_settings.getProperty().getName(), false);
                    matcher = Pattern.compile(m_settings.getSearchpattern()).matcher(prop.getValue());
                    if (matcher.find()) {
                        m_matchedResources.add(resource);
                        getReport().println(
                            Messages.get().container(Messages.RPT_SOURCESEARCH_MATCHED_0),
                            I_CmsReport.FORMAT_OK);
                    } else {
                        getReport().println(
                            Messages.get().container(Messages.RPT_SOURCESEARCH_NOT_MATCHED_0),
                            I_CmsReport.FORMAT_NOTE);
                    }

                } catch (CmsException e) {
                    LOG.error("Ubable to read property", e);
                }
            }
        }
        if (m_replace) {
            replaceProperties(m_matchedResources);
        }
        // report results
        reportResults(resources.size());
    }

    /**
     * Searches/reads all resources that are relevant.<p>
     *
     * @return the relevant resources
     */
    @SuppressWarnings("deprecation")
    private List<CmsResource> searchResources() {

        getReport().println(
            Messages.get().container(Messages.RPT_SOURCESEARCH_START_COLLECTING_FILES_TO_SEARCH_IN_0),
            I_CmsReport.FORMAT_HEADLINE);

        List<CmsResource> resources = new ArrayList<CmsResource>();
        if (m_settings.isSolrSearch()) {
            CmsSolrIndex index = OpenCms.getSearchManager().getIndexSolr(m_settings.getSource());
            if (index != null) {
                CmsSolrQuery query = new CmsSolrQuery(
                    null,
                    CmsRequestUtil.createParameterMap(m_settings.getQuery() + "&fl=path,type"));
                List<String> rootPaths = new ArrayList<>(m_settings.getPaths().size());
                String siteRoot = getCms().getRequestContext().getSiteRoot();
                for (String path : m_settings.getPaths()) {
                    rootPaths.add(path.startsWith(siteRoot) ? path : getCms().addSiteRoot(path));
                }
                query.setSearchRoots(rootPaths);
                if (CmsSourceSearchForm.RESOURCE_TYPES_ALL_NON_BINARY.equals(m_settings.getTypes())) {
                    query.addFilterQuery("type:-(\"image\" OR \"binary\")");
                } else if ((m_settings.getTypesArray() != null) && (m_settings.getTypesArray().length > 0)) {
                    query.setResourceTypes(m_settings.getTypesArray());
                }
                query.setRows(Integer.valueOf(MAX_PROCESSED_SOLR_RESULTS));
                query.ensureParameters();
                try {
                    resources.addAll(
                        index.search(getCms(), query, true, null, false, null, MAX_PROCESSED_SOLR_RESULTS));
                } catch (CmsSearchException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        } else {
            CmsResourceFilter filter = CmsResourceFilter.ALL.addExcludeState(
                CmsResource.STATE_DELETED).addRequireVisible();
            List<CmsResourceFilter> filterList = new ArrayList<CmsResourceFilter>();
            List<Integer> filterByExcludeType = null;
            if (CmsSourceSearchForm.RESOURCE_TYPES_ALL_NON_BINARY.equals(m_settings.getTypes())) {
                try {
                    int typeBinary = OpenCms.getResourceManager().getResourceType("binary").getTypeId();
                    int typeImage = OpenCms.getResourceManager().getResourceType("image").getTypeId();
                    filterByExcludeType = Arrays.asList(Integer.valueOf(typeBinary), Integer.valueOf(typeImage));
                } catch (CmsLoaderException e) {
                    // noop
                } catch (NullPointerException e) {
                    // noop
                }
            } else if ((m_settings.getTypesArray() != null) && (m_settings.getTypesArray().length > 0)) {
                for (String resTypeName : m_settings.getTypesArray()) {
                    try {
                        int typeId = OpenCms.getResourceManager().getResourceType(resTypeName).getTypeId();
                        filterList.add(((CmsResourceFilter)filter.clone()).addRequireType(typeId));
                    } catch (CmsLoaderException e) {
                        // noop
                    } catch (NullPointerException e) {
                        // noop
                    }
                }
            }
            if (filterList.size() == 1) {
                filter = filterList.get(0);
            }

            // iterate over all selected paths
            Iterator<String> iterPaths = m_settings.getPaths().iterator();

            if (!m_settings.getType().isPropertySearch()) {
                filter = filter.addRequireFile();
            }
            while (iterPaths.hasNext()) {
                String path = iterPaths.next();
                try {
                    if (m_settings.getType().isPropertySearch()) {
                        resources.addAll(
                            getCms().readResourcesWithProperty(path, m_settings.getProperty().getName(), null, filter));
                    } else {
                        // only read resources which are files and not deleted, which are in the current time range window and where the current
                        // user has the sufficient permissions to read them

                        List<CmsResource> tmpResources = getCms().readResources(path, filter);
                        List<String> subsites = null;
                        if (m_settings.ignoreSubSites()) {
                            subsites = OpenCms.getADEManager().getSubSitePaths(getCms(), path);
                            subsites.remove(
                                OpenCms.getADEManager().getSubSiteRoot(
                                    getCms(),
                                    getCms().readResource(path).getRootPath()));
                        }
                        Iterator<CmsResource> iterator = tmpResources.iterator();
                        while (iterator.hasNext()) {
                            CmsResource r = iterator.next();
                            boolean remove = true;
                            if (null != filterByExcludeType) {
                                remove = filterByExcludeType.contains(Integer.valueOf(r.getTypeId()));
                            } else if (filterList.size() > 1) {
                                for (CmsResourceFilter f : filterList) {
                                    if (f.isValid(getCms().getRequestContext(), r)) {
                                        remove = false;
                                    }
                                }
                            } else {
                                remove = false;
                            }
                            if ((subsites != null) & !remove) {
                                if (subsites.contains(
                                    OpenCms.getADEManager().getSubSiteRoot(getCms(), r.getRootPath()))) {
                                    remove = true;
                                }
                            }
                            if (!remove && m_settings.getType().isContentValuesOnly()) {
                                remove = !(OpenCms.getResourceManager().getResourceType(
                                    r) instanceof CmsResourceTypeXmlContent);
                            }
                            if (remove) {
                                iterator.remove();
                            }
                        }

                        if ((tmpResources != null) && !tmpResources.isEmpty()) {
                            resources.addAll(tmpResources);
                        }
                    }
                } catch (CmsException e) {
                    // an error occured
                    LOG.error(Messages.get().container(Messages.RPT_SOURCESEARCH_ERROR_READING_RESOURCES_1, path), e);
                    getReport().println(
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
     * @param file the file to write
     * @param content the file content
     *
     * @return success flag
     */
    private boolean writeContent(CmsFile file, byte[] content) {

        boolean success = true;
        I_CmsReport report = getReport();
        CmsObject cmsObject = getCms();
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
