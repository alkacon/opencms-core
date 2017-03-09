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

package org.opencms.jsp;

import org.opencms.ade.publish.CmsPublishListHelper;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.collectors.I_CmsCollectorPublishListProvider;
import org.opencms.flex.CmsFlexController;
import org.opencms.gwt.shared.I_CmsContentLoadCollectorInfo;
import org.opencms.jsp.search.config.I_CmsSearchConfiguration;
import org.opencms.jsp.search.controller.CmsSearchController;
import org.opencms.jsp.search.controller.I_CmsSearchControllerCommon;
import org.opencms.jsp.search.controller.I_CmsSearchControllerMain;
import org.opencms.jsp.search.result.CmsSearchResultWrapper;
import org.opencms.jsp.search.result.I_CmsSearchResultWrapper;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.search.CmsSearchException;
import org.opencms.search.CmsSearchResource;
import org.opencms.search.fields.CmsSearchField;
import org.opencms.search.solr.CmsSolrIndex;
import org.opencms.search.solr.CmsSolrQuery;
import org.opencms.search.solr.CmsSolrResultList;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsUUID;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.jsp.JspException;

import org.apache.commons.logging.Log;

/**
 * This tag is used to easily create a search form for a Solr search within a JSP.<p>
 */
public class CmsJspTagSimpleSearch extends CmsJspScopedVarBodyTagSuport implements I_CmsCollectorPublishListProvider {

    /** Default number of items which are checked for change for the "This page" publish dialog. */
    public static final int DEFAULT_CONTENTINFO_ROWS = 200;

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsJspTagSimpleSearch.class);

    /** The serial version id. */
    private static final long serialVersionUID = -12197069109672022L;

    /** Number of entries for which content info should be added to allow correct relations in "This page" publish dialog. */
    private Integer m_addContentInfoForEntries;

    /** The "configFile" tag attribute. */
    private String m_configFile;

    /** The "configString" tag attribute. */
    private String m_configString;

    /** The search index that should be used .
     *  It will either be the configured index, or "Solr Offline" / "Solr Online" depending on the project.
     * */
    private CmsSolrIndex m_index;

    /** Search controller keeping all the config and state from the search. */
    private I_CmsSearchControllerMain m_searchController;

    /**
     * @see org.opencms.file.collectors.I_CmsCollectorPublishListProvider#getPublishResources(org.opencms.file.CmsObject, org.opencms.gwt.shared.I_CmsContentLoadCollectorInfo)
     */
    @SuppressWarnings("javadoc")
    public static Set<CmsResource> getPublishResourcesInternal(CmsObject cms, I_CmsContentLoadCollectorInfo info)
    throws CmsException {

        CmsSolrIndex solrOnline = OpenCms.getSearchManager().getIndexSolr(CmsSolrIndex.DEFAULT_INDEX_NAME_ONLINE);
        CmsSolrIndex solrOffline = OpenCms.getSearchManager().getIndexSolr(CmsSolrIndex.DEFAULT_INDEX_NAME_OFFLINE);
        Set<CmsResource> result = new HashSet<CmsResource>();
        try {
            // use "complicated" constructor to allow more than 50 results -> set ignoreMaxResults to true
            // adjust the CmsObject to prevent unintended filtering of resources
            CmsSolrResultList offlineResults = solrOffline.search(
                CmsPublishListHelper.adjustCmsObject(cms, false),
                new CmsSolrQuery(null, CmsRequestUtil.createParameterMap(info.getCollectorParams())),
                true);
            Set<String> offlineIds = new HashSet<String>(offlineResults.size());
            for (CmsSearchResource offlineResult : offlineResults) {
                offlineIds.add(offlineResult.getField(CmsSearchField.FIELD_ID));
            }
            for (String id : offlineIds) {
                CmsResource resource = cms.readResource(new CmsUUID(id));
                if (!(resource.getState().isUnchanged())) {
                    result.add(resource);
                }
            }
            CmsSolrResultList onlineResults = solrOnline.search(
                CmsPublishListHelper.adjustCmsObject(cms, true),
                new CmsSolrQuery(null, CmsRequestUtil.createParameterMap(info.getCollectorParams())),
                true);
            Set<String> deletedIds = new HashSet<String>(onlineResults.size());
            for (CmsSearchResource onlineResult : onlineResults) {
                String uuid = onlineResult.getField(CmsSearchField.FIELD_ID);
                if (!offlineIds.contains(uuid)) {
                    deletedIds.add(uuid);
                }
            }
            for (String uuid : deletedIds) {
                CmsResource resource = cms.readResource(new CmsUUID(uuid));
                if (!(resource.getState().isUnchanged())) {
                    result.add(resource);
                }
            }
        } catch (CmsSearchException e) {
            LOG.warn(Messages.get().getBundle().key(Messages.LOG_TAG_SEARCH_SEARCH_FAILED_0), e);
        }
        return result;
    }

    /**
     * @see javax.servlet.jsp.tagext.BodyTagSupport#doEndTag()
     */
    @Override
    public int doEndTag() throws JspException {

        release();
        return super.doEndTag();
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#doStartTag()
     */
    @Override
    public int doStartTag() throws JspException, CmsIllegalArgumentException {

        CmsFlexController controller = CmsFlexController.getController(pageContext.getRequest());
        CmsObject cms = controller.getCmsObject();

        try {
            I_CmsSearchConfiguration config = null;
            //             if (m_configString != null) {
            //                 config = new CmsSearchConfiguration(new CmsJSONSearchConfigurationParser(m_configString));
            //             } else if (m_fileFormat == FileFormat.JSON) {
            //                 // read the JSON config file
            //                 CmsFile configFile = cms.readFile(m_configFile);
            //                 OpenCms.getLocaleManager();
            //                 String configString = new String(
            //                     configFile.getContents(),
            //                     CmsLocaleManager.getResourceEncoding(cms, configFile));
            //                 config = new CmsSearchConfiguration(new CmsJSONSearchConfigurationParser(configString));
            //             } else { // assume XML
            //                 CmsFile file = cms.readFile(m_configFile);
            //                 CmsXmlContent xmlContent = CmsXmlContentFactory.unmarshal(cms, file);
            //                 config = new CmsSearchConfiguration(
            //                     new CmsXMLSearchConfigurationParser(xmlContent, cms.getRequestContext().getLocale()));
            //             }
            m_searchController = new CmsSearchController(config);

            String indexName = m_searchController.getCommon().getConfig().getSolrIndex();
            // try to use configured index
            if ((indexName != null) && !indexName.trim().isEmpty()) {
                m_index = OpenCms.getSearchManager().getIndexSolr(indexName);
            }
            // if not successful, use the following default
            if (m_index == null) {
                m_index = OpenCms.getSearchManager().getIndexSolr(
                    cms.getRequestContext().getCurrentProject().isOnlineProject()
                    ? CmsSolrIndex.DEFAULT_INDEX_NAME_ONLINE
                    : CmsSolrIndex.DEFAULT_INDEX_NAME_OFFLINE);
            }

            storeAttribute(getVar(), getSearchResults(cms));

        } catch (Exception e) { // CmsException | UnsupportedEncodingException | JSONException
            LOG.error(e.getLocalizedMessage(), e);
            controller.setThrowable(e, cms.getRequestContext().getUri());
            throw new JspException(e);
        }
        if (!cms.getRequestContext().getCurrentProject().isOnlineProject()
            && (null == m_searchController.getCommon().getConfig().getSolrIndex())
            && (null != m_addContentInfoForEntries)) {
            CmsSolrQuery query = new CmsSolrQuery();
            m_searchController.addQueryParts(query);
            query.setStart(Integer.valueOf(0));
            query.setRows(m_addContentInfoForEntries);
            CmsContentLoadCollectorInfo info = new CmsContentLoadCollectorInfo();
            info.setCollectorClass(this.getClass().getName());
            info.setCollectorParams(query.getQuery());
            info.setId((new CmsUUID()).getStringValue());
            if (CmsJspTagEditable.getDirectEditProvider(pageContext) != null) {
                try {
                    CmsJspTagEditable.getDirectEditProvider(pageContext).insertDirectEditListMetadata(
                        pageContext,
                        info);
                } catch (JspException e) {
                    LOG.error("Could not write content info.", e);
                }
            }
        }
        return EVAL_BODY_INCLUDE;
    }

    /**
     * Returns the value of the specified configuration file (given via the tag's "configFile" attribute).<p>
     *
     * @return the config file
     */
    public String getConfigFile() {

        return m_configFile;
    }

    /**
     * Returns the "configString".<p>
     *
     * @return the "configString"
     */
    public String getConfigString() {

        return m_configString;
    }

    /**
     * @see org.opencms.file.collectors.I_CmsCollectorPublishListProvider#getPublishResources(org.opencms.file.CmsObject, org.opencms.gwt.shared.I_CmsContentLoadCollectorInfo)
     */
    public Set<CmsResource> getPublishResources(CmsObject cms, I_CmsContentLoadCollectorInfo info) throws CmsException {

        return getPublishResourcesInternal(cms, info);
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#release()
     */
    @Override
    public void release() {

        m_configFile = null;
        m_configString = null;
        m_searchController = null;
        m_index = null;
        super.release();
    }

    /** Setter for "addContentInfo", indicating if content information should be added.
     * @param doAddInfo The value of the "addContentInfo" attribute of the tag
     */
    public void setAddContentInfo(final Boolean doAddInfo) {

        if ((null != doAddInfo) && doAddInfo.booleanValue() && (null != m_addContentInfoForEntries)) {
            m_addContentInfoForEntries = Integer.valueOf(DEFAULT_CONTENTINFO_ROWS);
        }
    }

    /** Setter for the configuration file.
     * @param fileName Name of the configuration file to use for the search.
     */
    public void setConfigFile(String fileName) {

        m_configFile = fileName;
    }

    /** Setter for the "configString".
     * @param configString The "configString".
     */
    public void setConfigString(final String configString) {

        m_configString = configString;
    }

    /** Setter for "contentInfoMaxItems".
     * @param maxItems number of items to maximally check for alterations.
     */
    public void setContentInfoMaxItems(Integer maxItems) {

        if (null != maxItems) {
            m_addContentInfoForEntries = maxItems;
        }
    }

    /**
     * Here the search query is composed and executed.
     * The result is wrapped in an easily usable form.
     * It is exposed to the JSP via the tag's "var" attribute.<p>
     *
     * @param cms the cms context
     *
     * @return The result object exposed via the tag's attribute "var".
     */
    private I_CmsSearchResultWrapper getSearchResults(CmsObject cms) {

        // The second parameter is just ignored - so it does not matter
        m_searchController.updateFromRequestParameters(pageContext.getRequest().getParameterMap(), false);
        I_CmsSearchControllerCommon common = m_searchController.getCommon();
        // Do not search for empty query, if configured
        if (common.getState().getQuery().isEmpty()
            && (!common.getConfig().getIgnoreQueryParam() && !common.getConfig().getSearchForEmptyQueryParam())) {
            return new CmsSearchResultWrapper(m_searchController, null, null, cms, null);
        }
        Map<String, String[]> queryParams = null;
        boolean isEditMode = CmsJspTagEditable.isEditableRequest(pageContext.getRequest());
        if (isEditMode) {
            String params = "";
            if (common.getConfig().getIgnoreReleaseDate()) {
                params += "&fq=released:[* TO *]";
            }
            if (common.getConfig().getIgnoreExpirationDate()) {
                params += "&fq=expired:[* TO *]";
            }
            if (!params.isEmpty()) {
                queryParams = CmsRequestUtil.createParameterMap(params.substring(1));
            }
        }
        CmsSolrQuery query = new CmsSolrQuery(null, queryParams);
        m_searchController.addQueryParts(query);
        try {
            // use "complicated" constructor to allow more than 50 results -> set ignoreMaxResults to true
            // also set resource filter to allow for returning unreleased/expired resources if necessary.
            CmsSolrResultList solrResultList = m_index.search(
                cms,
                query,
                true,
                isEditMode ? CmsResourceFilter.IGNORE_EXPIRATION : null);
            return new CmsSearchResultWrapper(m_searchController, solrResultList, query, cms, null);
        } catch (CmsSearchException e) {
            LOG.warn(Messages.get().getBundle().key(Messages.LOG_TAG_SEARCH_SEARCH_FAILED_0), e);
            return new CmsSearchResultWrapper(m_searchController, null, query, cms, e);
        }
    }
}
