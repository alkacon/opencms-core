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

package org.opencms.xml.xml2json.document;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsResourceFilter;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.jsp.search.config.CmsSearchConfiguration;
import org.opencms.jsp.search.config.I_CmsSearchConfigurationCommon;
import org.opencms.jsp.search.config.parser.CmsSimpleSearchConfigurationParser;
import org.opencms.jsp.search.config.parser.simplesearch.CmsConfigParserUtils;
import org.opencms.jsp.search.config.parser.simplesearch.CmsConfigurationBean;
import org.opencms.jsp.search.controller.CmsSearchController;
import org.opencms.jsp.search.result.CmsSearchResultWrapper;
import org.opencms.jsp.search.result.I_CmsSearchResourceBean;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.search.CmsSearchException;
import org.opencms.search.solr.CmsSolrIndex;
import org.opencms.search.solr.CmsSolrQuery;
import org.opencms.search.solr.CmsSolrResultList;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.xml2json.CmsJsonRequest;
import org.opencms.xml.xml2json.handler.CmsJsonHandlerContext;
import org.opencms.xml.xml2json.handler.CmsJsonHandlerException;
import org.opencms.xml.xml2json.handler.CmsJsonHandlerXmlContent.PathNotFoundException;

import java.util.Collections;
import java.util.Locale;

/**
 * Class representing a JSON document for a CMS list.
 */
public class CmsJsonDocumentList extends CmsJsonDocumentXmlContent {

    /** The maximum number of list items to be returned. */
    private final static Integer MAX_ROWS = Integer.valueOf(600);

    /**
     * Creates a new JSON document.<p>
     *
     * @param jsonRequest the JSON request
     * @param xmlContent the listconfig XML content
     * @throws Exception if something goes wrong
     */
    public CmsJsonDocumentList(CmsJsonRequest jsonRequest, CmsXmlContent xmlContent)
    throws Exception {

        super(jsonRequest, xmlContent);
        m_throwException = false;
    }

    /**
     * @see org.opencms.xml.xml2json.document.I_CmsJsonDocument#getJson()
     */
    @Override
    public Object getJson()
    throws JSONException, CmsException, CmsJsonHandlerException, PathNotFoundException, Exception {

        super.getJson();
        insertJsonList();
        return m_json;
    }

    /**
     * @see org.opencms.xml.xml2json.document.CmsJsonDocumentXmlContent#isLocaleAllRequest()
     */
    @Override
    protected boolean isLocaleAllRequest() {

        return false;
    }

    /**
     * @see org.opencms.xml.xml2json.document.CmsJsonDocumentXmlContent#isLocalePathRequest()
     */
    @Override
    protected boolean isLocalePathRequest() {

        return false;
    }

    /**
     * @see org.opencms.xml.xml2json.document.CmsJsonDocumentXmlContent#isLocaleRequest()
     */
    @Override
    protected boolean isLocaleRequest() {

        return true;
    }

    /**
     * @see org.opencms.xml.xml2json.document.CmsJsonDocumentXmlContent#isShowFallbackLocaleRequest()
     */
    @Override
    protected boolean isShowFallbackLocaleRequest() {

        return true;
    }

    /**
     * @see org.opencms.xml.xml2json.document.CmsJsonDocumentXmlContent#isShowWrapperRequest()
     */
    @Override
    protected boolean isShowWrapperRequest() {

        return false;
    }

    /**
     * Evaluates the list configuration and returns the search result.<p>
     *
     * @param context the handler context
     * @return the search result
     * @throws JSONException if something goes wrong
     * @throws CmsSearchException if something goes wrong
     */
    private CmsSearchResultWrapper getSearchResult(CmsJsonHandlerContext context)
    throws JSONException, CmsSearchException {

        CmsConfigurationBean listConfigurationBean = CmsConfigParserUtils.parseListConfiguration(
            context.getCms(),
            context.getResource());
        CmsSimpleSearchConfigurationParser searchConfigurationParser = new CmsSimpleSearchConfigurationParser(
            context.getCms(),
            listConfigurationBean,
            "{}");
        Locale locale = CmsLocaleManager.getLocale(m_jsonRequest.getParamLocale());
        Locale selectedLocale = OpenCms.getLocaleManager().getBestMatchingLocale(
            locale,
            Collections.emptyList(),
            m_xmlContent.getLocales());
        searchConfigurationParser.setSearchLocale(selectedLocale);
        String paramSort = m_jsonRequest.getParamSort();
        if (paramSort != null) {
            searchConfigurationParser.setSortOption(paramSort);
        }

        CmsSolrQuery query = searchConfigurationParser.getInitialQuery();
        Integer paramStart = m_jsonRequest.getParamStart();
        Integer paramRows = m_jsonRequest.getParamRows();
        if (paramStart != null) {
            query.setStart(paramStart);
        }
        if (paramRows != null) {
            query.setRows(paramRows);
        } else {
            query.setRows(MAX_ROWS);
        }
        CmsSearchController searchController = new CmsSearchController(
            new CmsSearchConfiguration(searchConfigurationParser, context.getCms()));
        searchController.addQueryParts(query, context.getCms());
        I_CmsSearchConfigurationCommon searchConfigurationCommon = searchController.getCommon().getConfig();
        CmsSolrIndex index = OpenCms.getSearchManager().getIndexSolr(searchConfigurationCommon.getSolrIndex());
        CmsSolrResultList solrResultList = index.search(
            context.getCms(),
            query,
            true,
            null,
            false,
            CmsResourceFilter.DEFAULT,
            searchConfigurationCommon.getMaxReturnedResults());
        return new CmsSearchResultWrapper(searchController, solrResultList, query, context.getCms(), null);
    }

    /**
     * If the request parameter "list" is set, evaluates the list configuration and inserts
     * the result into this JSON document.<p>
     *
     * @throws Exception if something goes wrong
     */
    private void insertJsonList() throws Exception {

        Boolean paramContent = m_jsonRequest.getParamContent();
        if (paramContent.booleanValue()) {
            CmsSearchResultWrapper searchResult = getSearchResult(m_context);
            insertJsonListInfo(searchResult);
            insertJsonListItems(searchResult);
        }
    }

    /**
     * Inserts information about the list result into this JSON document.<p>
     *
     * @param searchResult the search result
     * @throws JSONException if something goes wrong
     */
    private void insertJsonListInfo(CmsSearchResultWrapper searchResult) throws JSONException {

        JSONObject jsonObject = new JSONObject(true);
        jsonObject.put("numFound", searchResult.getNumFound());
        m_json.put("listInfo", jsonObject);
    }

    /**
     * Inserts the list items into this JSON document.<p>
     *
     * @param searchResult the search result
     * @throws Exception if something goes wrong
     */
    private void insertJsonListItems(CmsSearchResultWrapper searchResult) throws Exception {

        for (I_CmsSearchResourceBean searchResourceBean : searchResult.getSearchResults()) {
            CmsFile file = searchResourceBean.getXmlContent().getFile();
            CmsXmlContent xmlContent = CmsXmlContentFactory.unmarshal(m_context.getCms(), file);
            CmsJsonDocumentEmbeddedXmlContent jsonDocument = new CmsJsonDocumentEmbeddedXmlContent(
                m_jsonRequest,
                xmlContent);
            m_json.append("list", jsonDocument.getJson());
        }
    }
}
