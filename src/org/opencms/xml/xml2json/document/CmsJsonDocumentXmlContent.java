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

import org.opencms.i18n.CmsLocaleManager;
import org.opencms.json.JSONArray;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.I_CmsXmlContentHandler;
import org.opencms.xml.content.I_CmsXmlContentHandler.JsonRendererSettings;
import org.opencms.xml.xml2json.CmsDefaultXmlContentJsonRenderer;
import org.opencms.xml.xml2json.CmsJsonHandlerException;
import org.opencms.xml.xml2json.CmsJsonRequest;
import org.opencms.xml.xml2json.CmsXmlContentJsonHandler.PathNotFoundException;
import org.opencms.xml.xml2json.I_CmsXmlContentJsonRenderer;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

/**
 * Class representing a JSON document for an XML content.
 */
public class CmsJsonDocumentXmlContent extends CmsJsonDocumentResource {

    /** The JSON part in the case of a path request. */
    private Object m_jsonPart;

    /** The XML content. */
    private CmsXmlContent m_xmlContent;

    /** The XML content renderer. */
    private I_CmsXmlContentJsonRenderer m_renderer;

    /**
     * Creates a new JSON document.<p>
     *
     * @param jsonRequest the JSON request
     * @param xmlContent the XML content
     * @throws Exception if something goes wrong
     */
    public CmsJsonDocumentXmlContent(CmsJsonRequest jsonRequest, CmsXmlContent xmlContent)
    throws Exception {

        super(jsonRequest, xmlContent.getFile());
        m_xmlContent = xmlContent;
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
        return m_jsonPart != null ? m_jsonPart : m_json;
    }

    /**
     * Initializes the content renderer<p>
     *
     * @throws Exception if something goes wrong
     */
    private void initRenderer() throws Exception {

        I_CmsXmlContentHandler handler = m_xmlContent.getContentDefinition().getContentHandler();
        JsonRendererSettings settings = handler.getJsonRendererSettings();
        if (settings == null) {
            m_renderer = new CmsDefaultXmlContentJsonRenderer();
        } else {
            m_renderer = (I_CmsXmlContentJsonRenderer)Class.forName(settings.getClassName()).newInstance();
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

        String paramLocale = m_context.getParameters().get("locale");
        String paramPath = m_context.getParameters().get("path");
        if ((paramLocale == null) && (paramPath == null)) {
            insertJsonContentAllLocales();
        } else if ((paramLocale != null) && (paramPath == null)) {
            insertJsonContentLocale();
        } else if ((paramLocale != null) && (paramPath != null)) {
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

        m_json = CmsDefaultXmlContentJsonRenderer.renderAllLocales(m_xmlContent, m_renderer);
    }

    /**
     * Inserts one locale content into this JSON document.<p>
     *
     * @throws JSONException if JSON rendering fails
     */
    private void insertJsonContentLocale() throws JSONException {

        String paramLocale = m_context.getParameters().get("locale");
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
        String paramPath = m_context.getParameters().get("path");
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

    /**
     * Inserts a wrapper with resource information into this JSON document.<p>
     *
     * @throws JSONException if JSON rendering fails
     * @throws CmsException if reading resource properties fails
     */
    private void insertJsonWrapper() throws JSONException, CmsException {

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
    private boolean isLocaleAllRequest() {

        String paramLocale = m_context.getParameters().get("locale");
        String paramPath = m_context.getParameters().get("path");
        return (paramLocale == null) && (paramPath == null);
    }

    /**
     * Whether one locale of this XML content is requested.<p>
     *
     * @return whether one locale or not
     */
    private boolean isLocalePathRequest() {

        String paramLocale = m_context.getParameters().get("locale");
        String paramPath = m_context.getParameters().get("path");
        return (paramLocale != null) && (paramPath != null);

    }

    /**
     * Whether a part of a locale of this XML content is requested.<p>
     *
     * @return whether a part of a locale or not
     */
    private boolean isLocaleRequest() {

        String paramLocale = m_context.getParameters().get("locale");
        String paramPath = m_context.getParameters().get("path");
        return (paramLocale != null) && (paramPath == null);
    }

    /**
     * Whether the default locale content shall be shown in the case
     * the requested locale is not available.<p>
     *
     * @return whether to show the default locale or not
     */
    private boolean isShowFallbackLocaleRequest() {

        String paramFallbackLocale = m_context.getParameters().get("fallbackLocale");
        boolean showDefaultLocale = false;
        if ((paramFallbackLocale != null) && !paramFallbackLocale.equals("false")) {
            showDefaultLocale = true;
        }
        return showDefaultLocale;
    }

    /**
     * Whether to show the wrapper with resource information. For backward
     * compatibility the wrapper is shown for the all locales request but not for
     * the locale and locale path request as a default. This default behavior
     * can be changed by means of the "wrapper" request parameter. For locale
     * path requests, wrapper information is not available.
     *
     * @return whether to show the wrapper or not
     */
    private boolean isShowWrapperRequest() {

        boolean showWrapper = true;
        Boolean paramWrapper = m_jsonRequest.getParamWrapper();
        if (isLocaleAllRequest()) {
            showWrapper = true;
            if ((paramWrapper != null) && (paramWrapper.booleanValue() == false)) {
                showWrapper = false;
            }
        } else if (isLocaleRequest()) {
            showWrapper = false;
            if ((paramWrapper != null) && (paramWrapper.booleanValue() == true)) {
                showWrapper = true;
            }
        } else if (isLocalePathRequest()) {
            showWrapper = false;
        }
        return showWrapper;
    }
}
