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

package org.opencms.xml.xml2json;

import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.json.JSONArray;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.content.CmsXmlContent;

import java.util.Collections;
import java.util.Locale;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

/**
 * Sub-handler for converting XML contents to JSON, either as a whole or just specific locales or paths.
 */
public class CmsXmlContentJsonHandler implements I_CmsJsonHandler {

    /** Request parameter name. */
    public static final String PARAM_LOCALE = "locale";

    /** Request parameter name. */
    public static final String PARAM_PATH = "path";

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsXmlContentJsonHandler.class);

    /**
     * Creates an empty JSON object.
     *
     * @return the empty JSON object
     */
    public static JSONObject empty() {

        try {
            return new JSONObject("{}");
        } catch (JSONException e) {
            return null;
        }
    }

    /**
     * Looks up sub-object in the given JSON object.
     *
     * @param current the initial object
     * @param path the path to look up
     * @return the sub-object
     *
     * @throws JSONException if something goes wrong
     */
    public static Object lookupPath(Object current, String path) throws JSONException {

        String[] tokens = path.split("[/\\[\\]]");
        for (String token : tokens) {
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(token)) {
                continue;
            }
            if (StringUtils.isNumeric(token) && (current instanceof JSONArray)) {
                current = ((JSONArray)current).get(Integer.parseInt(token) - 1);
            } else {
                current = ((JSONObject)current).get(token);
            }
        }
        return current;
    }

    /**
     * @see org.opencms.xml.xml2json.I_CmsJsonHandler#getOrder()
     */
    public double getOrder() {

        return 100.0;

    }

    /**
     * @see org.opencms.xml.xml2json.I_CmsJsonHandler#matches(org.opencms.xml.xml2json.CmsJsonHandlerContext)
     */
    public boolean matches(CmsJsonHandlerContext context) {

        return CmsResourceTypeXmlContent.isXmlContent(context.getResource());
    }

    /**
     * @see org.opencms.xml.xml2json.I_CmsJsonHandler#renderJson(org.opencms.xml.xml2json.CmsJsonHandlerContext)
     */
    public CmsJsonResult renderJson(CmsJsonHandlerContext context) throws CmsException {

        try {
            CmsXmlContent content = context.getContent();
            I_CmsXmlContentJsonRenderer renderer = createContentRenderer(context);

            Object json = null;
            String localeParam = context.getParameters().get(PARAM_LOCALE);
            String pathParam = context.getParameters().get(PARAM_PATH);
            if ((localeParam == null) && (pathParam == null)) {
                json = CmsDefaultXmlContentJsonRenderer.renderAllLocales(content, renderer);
            } else if (localeParam != null) {
                Locale locale = CmsLocaleManager.getLocale(localeParam);
                Locale selectedLocale = OpenCms.getLocaleManager().getBestMatchingLocale(
                    locale,
                    Collections.emptyList(),
                    context.getContent().getLocales());
                json = renderer.render(context.getContent(), selectedLocale);
                if (pathParam != null) {
                    Object result = lookupPath(json, pathParam);
                    json = result;
                }
            } else {
                throw new IllegalArgumentException("Can not use path parameter without locale parameter.");
            }
            CmsJsonResult res = new CmsJsonResult(json, HttpServletResponse.SC_OK);
            return res;

        } catch (JSONException e) {
            LOG.error(e.getLocalizedMessage(), e);
            return new CmsJsonResult(empty(), HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * Creates the content renderer instance.
     *
     * @param context the JSON handler context
     *
     * @return the content renderer instance
     *
     * @throws CmsException if something goes wrong
     */
    protected I_CmsXmlContentJsonRenderer createContentRenderer(CmsJsonHandlerContext context) throws CmsException {

        CmsDefaultXmlContentJsonRenderer renderer = new CmsDefaultXmlContentJsonRenderer();
        renderer.initialize(context);
        return renderer;
    }

}
