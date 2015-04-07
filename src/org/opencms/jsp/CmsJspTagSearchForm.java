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

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.flex.CmsFlexController;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.jsp.search.config.CmsSearchConfiguration;
import org.opencms.jsp.search.config.I_CmsSearchConfiguration;
import org.opencms.jsp.search.config.parser.CmsJSONSearchConfigurationParser;
import org.opencms.jsp.search.config.parser.CmsXMLSearchConfigurationParser;
import org.opencms.jsp.search.controller.CmsSearchController;
import org.opencms.jsp.search.controller.I_CmsSearchControllerMain;
import org.opencms.jsp.search.result.CmsSearchResultWrapper;
import org.opencms.jsp.search.result.I_SearchResultWrapper;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.search.CmsSearchException;
import org.opencms.search.solr.CmsSolrIndex;
import org.opencms.search.solr.CmsSolrQuery;
import org.opencms.search.solr.CmsSolrResultList;
import org.opencms.util.CmsRequestUtil;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;

import javax.servlet.jsp.JspException;

import org.apache.commons.logging.Log;

/**
 * This tag is used to easily create a search form for a Solr search within a JSP.
 */
public class CmsJspTagSearchForm extends CmsJspScopedVarBodyTagSuport {

    /**
     * Type for the file formats that can be parsed.
     * The format is given via the tag's attribute "fileFormat".
     */
    private static enum FileFormat {
        /**
         * XML file (of type jsp-search-form).
         */
        XML,

        /**
         * json file in respective format.
         */
        JSON
    }

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsJspTagSearchForm.class);

    /** Serial version UID required for safe serialization. */
    private static final long serialVersionUID = 6048771777971251L;

    /** The CmsObject for the current user. */
    protected transient CmsObject m_cms;

    /** The FlexController for the current request. */
    protected CmsFlexController m_controller;

    /** The "configFile" tag attribute. */
    private String m_configFile;

    /** The "configString" tag attribute. */
    private String m_configString;

    /** The "fileFormat" tag attribute converted to type FileFormat. */
    private FileFormat m_fileFormat;

    /** Search controller keeping all the config and state from the search. */
    private I_CmsSearchControllerMain m_searchController;

    /** The search index that should be used .
     *  It will either be the configured index, or "Solr Offline" / "Solr Online" depending on the project.
     * */
    private CmsSolrIndex m_index;

    /**
     * Empty constructor, required for JSP tags.
     *
     */
    public CmsJspTagSearchForm() {

        super();
        m_fileFormat = FileFormat.XML;
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

        // initialize the content load tag
        init();
        return EVAL_BODY_INCLUDE;
    }

    /** Get the value of the specified configuration file (given via the tag's "configFile" attribute).
     * @return The config file.
     */
    public String getConfigFile() {

        return m_configFile;
    }

    /** Getter for the "configString".
     * @return The "configString".
     */
    public String getConfigString() {

        return m_configString;
    }

    /** Get the value of the specified format of the configuration file (given via the tag's "fileFormat" attribute).
     * @return The file format.
     */
    public String getFileFormat() {

        return m_fileFormat.toString();
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#release()
     */
    @Override
    public void release() {

        m_cms = null;
        m_configFile = null;
        setConfigString(null);
        m_searchController = null;
        m_index = null;
        m_controller = null;
        super.release();
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

    /** Setter for the file format.
     * @param fileFormat File format the configuration file is in.
     */
    public void setFileFormat(String fileFormat) {

        if (fileFormat.toUpperCase().equals(FileFormat.JSON.toString())) {
            m_fileFormat = FileFormat.JSON;
        }
    }

    /**
     * Initializes this formatter tag.
     * <p>
     *
     * @throws JspException
     *             in case something goes wrong
     */
    protected void init() throws JspException {

        // initialize OpenCms access objects
        m_controller = CmsFlexController.getController(pageContext.getRequest());
        m_cms = m_controller.getCmsObject();

        try {
            I_CmsSearchConfiguration config;
            if (m_configString != null) {
                config = new CmsSearchConfiguration(new CmsJSONSearchConfigurationParser(m_configString));
            } else if (m_fileFormat == FileFormat.JSON) {
                // read the JSON config file
                CmsFile configFile = m_cms.readFile(m_configFile);
                OpenCms.getLocaleManager();
                String configString = new String(configFile.getContents(), CmsLocaleManager.getResourceEncoding(
                    m_cms,
                    configFile));
                config = new CmsSearchConfiguration(new CmsJSONSearchConfigurationParser(configString));
            } else { // assume XML
                CmsFile file = m_cms.readFile(m_configFile);
                CmsXmlContent xmlContent = CmsXmlContentFactory.unmarshal(m_cms, file);
                config = new CmsSearchConfiguration(new CmsXMLSearchConfigurationParser(
                    xmlContent,
                    m_cms.getRequestContext().getLocale()));
            }
            m_searchController = new CmsSearchController(config);

            String indexName = m_searchController.getCommon().getConfig().getSolrIndex();
            // try to use configured index
            if ((indexName != null) && !indexName.trim().isEmpty()) {
                m_index = OpenCms.getSearchManager().getIndexSolr(indexName);
            }
            // if not successful, use the following default
            if (m_index == null) {
                m_index = OpenCms.getSearchManager().getIndexSolr(
                    m_cms.getRequestContext().getCurrentProject().isOnlineProject() ? "Solr Online" : "Solr Offline");
            }

            storeAttribute(getVar(), getSearchResults());

        } catch (Exception e) { // CmsException | UnsupportedEncodingException | JSONException
            LOG.error(e.getLocalizedMessage(), e);
            m_controller.setThrowable(e, m_cms.getRequestContext().getUri());
            throw new JspException(e);
        }
    }

    /** Here the search query is composed and executed.
     *  The result is wrapped in an easily usable form.
     *  It is exposed to the JSP via the tag's "var" attribute.
     * @return The result object exposed via the tag's attribute "var".
     */
    private I_SearchResultWrapper getSearchResults() {

        m_searchController.updateFromRequestParameters(pageContext.getRequest().getParameterMap());
        String query = m_searchController.generateQuery();
        try {
            // use "complicated" constructor to allow more than 50 results -> set ignoreMaxResults to true
            CmsSolrResultList solrResultList = m_index.search(
                m_cms,
                new CmsSolrQuery(null, CmsRequestUtil.createParameterMap(query)),
                true);
            return new CmsSearchResultWrapper(m_searchController, solrResultList, m_cms);
        } catch (CmsSearchException e) {
            LOG.warn(Messages.get().getBundle().key(Messages.LOG_TAG_SEARCHFORM_SEARCH_FAILED_0), e);
            return null;
        }
    }
}
