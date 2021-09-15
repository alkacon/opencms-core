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
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.json.JSONArray;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.content.I_CmsXmlContentHandler;
import org.opencms.xml.content.I_CmsXmlContentHandler.JsonRendererSettings;
import org.opencms.xml.xml2json.CmsJsonRequest;
import org.opencms.xml.xml2json.handler.CmsJsonHandlerException;
import org.opencms.xml.xml2json.handler.CmsJsonHandlerXmlContent.PathNotFoundException;
import org.opencms.xml.xml2json.renderer.CmsJsonRendererXmlContent;
import org.opencms.xml.xml2json.renderer.I_CmsJsonRendererXmlContent;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

/**
 * Class representing a JSON document for an XML content.
 */
public class CmsJsonDocumentXmlContent extends CmsJsonDocumentResource {

    /** The XML content. */
    protected CmsXmlContent m_xmlContent;

    /** Whether to throw exceptions. */
    protected boolean m_throwException = true;

    /** Whether to embed linked contents. */
    protected boolean m_embedLinkedModelgroup = true;

    /** The JSON part in the case of a path request. */
    private Object m_jsonPart;

    /** The XML content renderer. */
    private I_CmsJsonRendererXmlContent m_renderer;

    /**
     * Creates a new JSON document.<p>
     *
     * @param jsonRequest the JSON request
     * @param xmlContent the XML content
     * @throws Exception if something goes wrong
     */
    public CmsJsonDocumentXmlContent(CmsJsonRequest jsonRequest, CmsXmlContent xmlContent)
    throws Exception {

        this(jsonRequest, xmlContent, true);
    }

    /**
     * Creates a new JSON document.<p>
     *
     * @param jsonRequest the JSON request
     * @param xmlContent the XML content
     * @param embedLinkedModelgroup whether to embed linked model groups
     * @throws Exception if something goes wrong
     */
    public CmsJsonDocumentXmlContent(
        CmsJsonRequest jsonRequest,
        CmsXmlContent xmlContent,
        boolean embedLinkedModelgroup)
    throws Exception {

        super(jsonRequest, xmlContent.getFile());
        m_xmlContent = xmlContent;
        m_embedLinkedModelgroup = embedLinkedModelgroup;
        initRenderer();
    }

    /**
     * @see org.opencms.xml.xml2json.document.CmsJsonDocumentResource#getJson()
     */
    @Override
    public Object getJson()
    throws JSONException, CmsException, CmsJsonHandlerException, PathNotFoundException, Exception {

        insertJsonContent();
        insertJsonWrapper();
        if (isShowLinkedContentsRequest()) {
            insertJsonLinkedContents();
        }
        return m_jsonPart != null ? m_jsonPart : m_json;
    }

    /**
     * Inserts a JSON representation of a linked content into this JSON document.<p>
     *
     * @param resource the resource
     * @throws Exception if something goes wrong
     */
    protected void insertJsonLinkedContent(CmsResource resource) throws Exception {

        JSONObject jsonObject = (JSONObject)m_json.get(FIELD_LINKED_CONTENTS);
        String key = resource.getRootPath();
        try {
            CmsFile file = m_context.getCms().readFile(resource);
            CmsXmlContent xmlContent = CmsXmlContentFactory.unmarshal(m_context.getCms(), file);
            Object value = null;
            if (CmsResourceTypeXmlContainerPage.isContainerPage(resource) && m_embedLinkedModelgroup) {
                CmsJsonDocumentContainerPage document = new CmsJsonDocumentContainerPage(
                    m_jsonRequest,
                    xmlContent,
                    false);
                value = document.getJson();
            } else if (CmsResourceTypeXmlContent.isXmlContent(resource)) {
                CmsJsonDocumentXmlContent document = new CmsJsonDocumentXmlContent(m_jsonRequest, xmlContent, false);
                value = document.getJson();
            }
            jsonObject.put(key, value);
        } catch (Exception e) {
            JSONObject exception = new JSONObject();
            exception.put("exception", e.getLocalizedMessage());
            jsonObject.put(key, exception);
        }
    }

    /**
     * For each linked content, inserts a JSON representation of the linked content into this JSON document.<p>
     *
     * @throws Exception if something goes wrong
     */
    protected void insertJsonLinkedContents() throws Exception {

        List<CmsRelation> relationList = m_context.getCms().getRelationsForResource(
            m_xmlContent.getFile(),
            CmsRelationFilter.TARGETS);
        m_json.put(FIELD_LINKED_CONTENTS, new JSONObject());
        for (CmsRelation relation : relationList) {
            CmsResource resource = relation.getTarget(m_context.getCms(), CmsResourceFilter.DEFAULT);
            if (CmsResourceTypeXmlContent.isXmlContent(resource)
                || CmsResourceTypeXmlContainerPage.isContainerPage(resource)) {
                insertJsonLinkedContent(resource);
            }
        }
    }

    /**
     * Inserts a wrapper with resource information into this JSON document.<p>
     *
     * @throws JSONException if JSON rendering fails
     * @throws CmsException if reading resource properties fails
     */
    protected void insertJsonWrapper() throws JSONException, CmsException {

        if (isShowWrapperRequest()) {
            insertJsonLocales();
            insertJsonResource();
        }
    }

    /**
     * Whether all locales of this XML content are requested.<p>
     *
     * @return whether all or not
     */
    protected boolean isLocaleAllRequest() {

        String paramLocale = m_jsonRequest.getParamLocale();
        String paramPath = m_jsonRequest.getParamPath();
        return (paramLocale == null) && (paramPath == null);
    }

    /**
     * Whether one locale of this XML content is requested.<p>
     *
     * @return whether one locale or not
     */
    protected boolean isLocalePathRequest() {

        String paramLocale = m_jsonRequest.getParamLocale();
        String paramPath = m_jsonRequest.getParamPath();
        return (paramLocale != null) && (paramPath != null);

    }

    /**
     * Whether a part of a locale of this XML content is requested.<p>
     *
     * @return whether a part of a locale or not
     */
    protected boolean isLocaleRequest() {

        String paramLocale = m_jsonRequest.getParamLocale();
        String paramPath = m_jsonRequest.getParamPath();
        return (paramLocale != null) && (paramPath == null);
    }

    /**
     * Whether the default locale content shall be shown in the case
     * the requested locale is not available.<p>
     *
     * @return whether to show the default locale or not
     */
    protected boolean isShowFallbackLocaleRequest() {

        return m_jsonRequest.getParamFallbackLocale().booleanValue();
    }

    /**
     * Whether all linked contents shall be embedded into this document.
     *
     * @return whether to embed the linked contents or not
     */
    protected boolean isShowLinkedContentsRequest() {

        return m_jsonRequest.getParamContent().booleanValue();
    }

    /**
     * Whether to show the wrapper with resource information. For backward
     * compatibility the wrapper is shown for the all locale request but not for
     * the locale and locale path request as a default. This default behavior
     * can be changed by means of the "wrapper" request parameter. For locale
     * path requests, wrapper information is not available.
     *
     * @return whether to show the wrapper or not
     */
    protected boolean isShowWrapperRequest() {

        boolean showWrapper = true;
        String paramWrapperRaw = m_context.getParameters().get(CmsJsonRequest.PARAM_WRAPPER);
        Boolean paramWrapper = m_jsonRequest.getParamWrapper();
        if (isLocaleAllRequest()) {
            showWrapper = true;
            if ((paramWrapperRaw != null) && (paramWrapperRaw.equals("false"))) {
                showWrapper = false;
            }
        } else if (isLocaleRequest()) {
            showWrapper = false;
            if (paramWrapper.booleanValue()) {
                showWrapper = true;
            }
        } else if (isLocalePathRequest()) {
            showWrapper = false;
        }
        return showWrapper;
    }

    /**
     * Initializes the content renderer.<p>
     *
     * @throws Exception if something goes wrong
     */
    private void initRenderer() throws Exception {

        I_CmsXmlContentHandler handler = m_xmlContent.getContentDefinition().getContentHandler();
        JsonRendererSettings settings = handler.getJsonRendererSettings();
        if (settings == null) {
            m_renderer = new CmsJsonRendererXmlContent();
        } else {
            m_renderer = (I_CmsJsonRendererXmlContent)Class.forName(settings.getClassName()).newInstance();
            for (Map.Entry<String, String> entry : settings.getParameters().entrySet()) {
                m_renderer.addConfigurationParameter(entry.getKey(), entry.getValue());
            }
            m_renderer.initConfiguration();
        }
        m_renderer.initialize(m_context.getCms());
    }

    /**
     * Inserts a JSON representation of this XML content into this JSON document.<p>
     *
     * @throws CmsJsonHandlerException if something goes wrong
     * @throws JSONException if JSON rendering fails
     * @throws PathNotFoundException if path selection fails
     */
    private void insertJsonContent() throws CmsJsonHandlerException, JSONException, PathNotFoundException {

        if (isLocaleAllRequest()) {
            insertJsonContentAllLocales();
        } else if (isLocaleRequest()) {
            insertJsonContentLocale();
        } else if (isLocalePathRequest()) {
            insertJsonContentLocalePath();
        } else {
            throw new CmsJsonHandlerException("Can not use path parameter without locale parameter.");
        }
    }

    /**
     * Inserts all locale contents into this JSON document.<p>
     *
     * @throws JSONException if JSON rendering fails
     */
    private void insertJsonContentAllLocales() throws JSONException {

        m_json = CmsJsonRendererXmlContent.renderAllLocales(m_xmlContent, m_renderer);
    }

    /**
     * Inserts one locale content into this JSON document.<p>
     *
     * @throws JSONException if JSON rendering fails
     * @throws PathNotFoundException if the selected locale does not exist and
     * no fallbackLocale parameter is given
     */
    private void insertJsonContentLocale() throws JSONException, PathNotFoundException {

        String paramLocale = m_jsonRequest.getParamLocale();
        Locale locale = CmsLocaleManager.getLocale(paramLocale);
        Locale selectedLocale = OpenCms.getLocaleManager().getBestMatchingLocale(
            locale,
            Collections.emptyList(),
            m_xmlContent.getLocales());
        boolean localeExists = true;
        if ((selectedLocale == null) || !m_xmlContent.hasLocale(selectedLocale)) {
            localeExists = false;
        }
        JSONObject jsonObject = new JSONObject(true);
        if (localeExists) {
            jsonObject = (JSONObject)m_renderer.render(m_xmlContent, selectedLocale);
        } else if (isShowFallbackLocaleRequest()) {
            List<Locale> localeList = m_xmlContent.getLocales();
            if (!localeList.isEmpty()) {
                jsonObject = (JSONObject)m_renderer.render(m_xmlContent, localeList.get(0));
                m_json.put("localeFallback", localeList.get(0).toString());
            }
        } else if (m_throwException) {
            throw new PathNotFoundException("Locale <" + this.m_jsonRequest.getParamLocale() + "> not found.");
        }
        if (isShowWrapperRequest()) {
            m_json.put("localeContent", jsonObject);
        } else {
            m_json = jsonObject;
        }
    }

    /**
     * Inserts a fragment of one locale content into this JSON document.<p>
     *
     * @throws PathNotFoundException if the path selection fails
     * @throws JSONException if JSON rendering fails
     */
    private void insertJsonContentLocalePath() throws PathNotFoundException, JSONException {

        insertJsonContentLocale();
        String paramPath = m_jsonRequest.getParamPath();
        String[] tokens = paramPath.split("[/\\[\\]]");
        Object current = m_json;
        for (String token : tokens) {
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(token)) {
                continue;
            }
            if (StringUtils.isNumeric(token) && (current instanceof JSONArray)) {
                current = ((JSONArray)current).get(Integer.parseInt(token));
            } else if (current instanceof JSONObject) {
                current = ((JSONObject)current).get(token);
            } else {
                throw new PathNotFoundException("Path not found");
            }
        }
        m_jsonPart = current;
    }

    /**
     * Insert a list of all locale names available for this content.<p>
     *
     * @throws JSONException if JSON rendering fails
     */
    private void insertJsonLocales() throws JSONException {

        JSONArray locales = new JSONArray();
        for (Locale locale : m_xmlContent.getLocales()) {
            locales.put(locale.toString());
        }
        m_json.put("locales", locales);
    }
}
