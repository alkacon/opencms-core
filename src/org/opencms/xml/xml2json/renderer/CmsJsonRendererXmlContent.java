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

package org.opencms.xml.xml2json.renderer;

import org.opencms.configuration.CmsParameterConfiguration;
import org.opencms.file.CmsObject;
import org.opencms.json.JSONArray;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsLink;
import org.opencms.relations.I_CmsCustomLinkRenderer;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.types.I_CmsXmlContentValue;
import org.opencms.xml.xml2json.CmsJsonResourceHandler;
import org.opencms.xml.xml2json.CmsXmlContentTree;
import org.opencms.xml.xml2json.CmsXmlContentTree.Field;
import org.opencms.xml.xml2json.CmsXmlContentTree.Node;
import org.opencms.xml.xml2json.I_CmsJsonFormattableValue;
import org.opencms.xml.xml2json.handler.CmsJsonHandlerContext;

import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Locale;

/**
 * Converts an XML content to JSON by creating a CmsXmlContentTree and then recursively processing its nodes.
 *
 * <p>This specific renderer class does not need to be initialized with a CmsJsonHandlerContext, you can
 * just initialize it with a CmsObject.
 */
public class CmsJsonRendererXmlContent implements I_CmsJsonRendererXmlContent {

    /** The CMS context. */
    private CmsObject m_cms;

    /** The root Cms context. */
    private CmsObject m_rootCms;

    /**
     * Creates a new instance.
     *
     * If this constructor is used, you still have to call one of the initialize() methods before rendering XML content to JSON.
     */
    public CmsJsonRendererXmlContent() {

        // do nothing
    }

    /**
     * Creates a new instance.
     *
     * @param cms the CMS context to use
     * @throws CmsException if something goes wrong
     */
    public CmsJsonRendererXmlContent(CmsObject cms)
    throws CmsException {

        initialize(cms);
    }

    /**
     * Builds a simple JSON object with link and path fields whose values are taken from the corresponding parameters.
     *
     * <p>If path is null, it will not be added to the result JSON.
     *
     * @param link the value for the link field
     * @param path the value for the path field
     * @return the link-and-path object
     * @throws JSONException if something goes wrong
     */
    public static JSONObject linkAndPath(String link, String path, CmsObject cms) throws JSONException {

        JSONObject result = new JSONObject();
        result.put("link", link);
        if (path != null) {
            int paramPos = path.indexOf("?");
            if (paramPos != -1) {
                path = path.substring(0, paramPos);
            }
            path = OpenCms.getLinkManager().getRootPath(cms, path);
            result.put("path", path);
        }
        return result;
    }

    /**
     * Helper method to apply renderer to all locales of an XML content, and put the resulting objects into a JSON object with the locales as keys.
     *
     * @param content the content
     * @param renderer the renderer to use
     * @return the result JSON
     * @throws JSONException if something goes wrong
     */
    public static JSONObject renderAllLocales(CmsXmlContent content, I_CmsJsonRendererXmlContent renderer)
    throws JSONException {

        List<Locale> locales = content.getLocales();
        JSONObject result = new JSONObject(true);
        for (Locale locale : locales) {
            Object jsonForLocale = renderer.render(content, locale);
            result.put(locale.toString(), jsonForLocale);
        }
        return result;
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#addConfigurationParameter(java.lang.String, java.lang.String)
     */
    public void addConfigurationParameter(String paramName, String paramValue) {

        // do nothing
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#getConfiguration()
     */
    public CmsParameterConfiguration getConfiguration() {

        return null;
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#initConfiguration()
     */
    public void initConfiguration() {

        // do nothing
    }

    /**
     * @see org.opencms.xml.xml2json.renderer.I_CmsJsonRendererXmlContent#initialize(org.opencms.xml.xml2json.handler.CmsJsonHandlerContext)
     */
    public void initialize(CmsJsonHandlerContext context) throws CmsException {

        initialize(context.getCms());
    }

    /**
     * Initializes the renderer.
     *
     * @param cms the CMS context to use
     *
     * @throws CmsException if something goes wrong
     */
    public void initialize(CmsObject cms) throws CmsException {

        m_cms = OpenCms.initCmsObject(cms);
        Object context = cms.getRequestContext().getAttribute(CmsJsonResourceHandler.ATTR_CONTEXT);
        m_rootCms = OpenCms.initCmsObject(m_cms);
        m_rootCms.getRequestContext().setSiteRoot("");
        if (context != null) {
            for (CmsObject currentCms : new CmsObject[] {m_cms, m_rootCms}) {
                currentCms.getRequestContext().setAttribute(CmsJsonResourceHandler.ATTR_CONTEXT, context);
            }
        }
        I_CmsCustomLinkRenderer linkRenderer = CmsJsonResourceHandler.getLinkRenderer(cms);
        if (linkRenderer != null) {
            m_cms.getRequestContext().setAttribute(CmsLink.CUSTOM_LINK_HANDLER, linkRenderer);
        }
    }

    /**
     * @see org.opencms.xml.xml2json.renderer.I_CmsJsonRendererXmlContent#render(org.opencms.xml.content.CmsXmlContent, java.util.Locale)
     */
    @Override
    public Object render(CmsXmlContent content, Locale locale) throws JSONException {

        CmsXmlContentTree tree = new CmsXmlContentTree(content, locale);
        m_cms.getRequestContext().setLocale(locale);
        m_rootCms.getRequestContext().setLocale(locale);
        Node root = tree.getRoot();
        return renderNode(root);

    }

    /**
     * Renders a tree node as JSON.
     *
     * @param node the tree node
     * @return the JSON (may be JSONObject, JSONArray, or String)
     *
     * @throws JSONException if something goes wrong
     */
    public Object renderNode(Node node) throws JSONException {

        switch (node.getType()) {
            case sequence:
                List<Field> fields = node.getFields();
                JSONObject result = new JSONObject(true);
                for (Field field : fields) {
                    SimpleEntry<String, Object> keyAndValue = renderField(field);
                    if (keyAndValue != null) {
                        result.put(keyAndValue.getKey(), keyAndValue.getValue());
                    }
                }
                return result;
            case choice:
                JSONArray array = new JSONArray();
                for (Field field : node.getFields()) {

                    SimpleEntry<String, Object> keyAndValue = renderField(field);
                    if (keyAndValue != null) {
                        JSONObject choiceObj = new JSONObject(true);
                        choiceObj.put(keyAndValue.getKey(), keyAndValue.getValue());
                        array.put(choiceObj);
                    }

                }
                return array;
            case simple:
                Object valueJson = renderSimpleValue(node);
                return valueJson;
            default:
                throw new IllegalArgumentException("Unsupported node: " + node.getType());

        }

    }

    /**
     * Renders a tree field as a field in the given JSON object.
     *
     * @param field the field to render
     * @return the key/value pair for the field
     *
     * @throws JSONException if something goes wrong
     */
    protected SimpleEntry<String, Object> renderField(Field field) throws JSONException {

        String name = field.getName();
        if (field.isMultivalue()) {
            // If field is *potentially* multivalue,
            // we always generate a JSON array for the sake of consistency,
            // no matter how many actual values we currently have
            JSONArray array = new JSONArray();
            if (field.getFieldDefinition().isChoiceType()) {

                // Multiple choice values can be represented by either a multivalued field of single-valued choices,
                // or a single-valued field of multivalue choices. Using both in combination doesn't seem to make sense.
                // So we collapse consecutive choice values in a multivalue field to give a uniform JSON syntax for the two
                // cases to make the JSON easier to work with, but we lose some information about the structure of the XML.
                for (Node subNode : field.getNodes()) {
                    JSONArray choiceJson = (JSONArray)renderNode(subNode);
                    array.append(choiceJson);
                }
            } else {
                for (Node subNode : field.getNodes()) {
                    array.put(renderNode(subNode));
                }

            }
            return new SimpleEntry<>(name, array);
        } else if (field.getNodes().size() == 1) {
            if (field.getFieldDefinition().isChoiceType() && !field.isMultiChoice()) {
                // field *and* choice single-valued, so we can unwrap the single value
                JSONArray array = (JSONArray)renderNode(field.getNode());
                if (array.length() == 1) {
                    return new SimpleEntry<>(name, array.get(0));

                }
            } else {
                return new SimpleEntry<>(name, renderNode(field.getNodes().get(0)));
            }
        }
        return null;
    }

    /**
     * Renders a simple value (i.e. not a nested content).
     *
     * @param node the node
     * @return the JSON representation for the value
     * @throws JSONException if something goes wrong
     */
    protected Object renderSimpleValue(Node node) throws JSONException {

        I_CmsXmlContentValue value = node.getValue();
        if (value instanceof I_CmsJsonFormattableValue) {
            return ((I_CmsJsonFormattableValue)value).toJson(m_cms);
        } else {
            return node.getValue().getStringValue(m_cms);
        }
    }

}
