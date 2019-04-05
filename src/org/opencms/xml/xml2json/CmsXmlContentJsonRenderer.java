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

import org.opencms.file.CmsObject;
import org.opencms.json.JSONArray;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.types.CmsXmlVarLinkValue;
import org.opencms.xml.types.CmsXmlVfsFileValue;
import org.opencms.xml.types.I_CmsXmlContentValue;
import org.opencms.xml.xml2json.CmsXmlContentTree.Field;
import org.opencms.xml.xml2json.CmsXmlContentTree.Node;

import java.util.List;
import java.util.Locale;

/**
 * Converts an XML content to JSON.
 */
public class CmsXmlContentJsonRenderer {

    /** The CMS context. */
    private CmsObject m_cms;

    /** The root Cms context. */
    private CmsObject m_rootCms;

    /**
     * Creates a new instance.
     *
     * @param cms the CMS context to use
     * @throws CmsException if something goes wrong
     */
    public CmsXmlContentJsonRenderer(CmsObject cms)
    throws CmsException {

        m_cms = cms;
        m_rootCms = OpenCms.initCmsObject(cms);
        m_rootCms.getRequestContext().setSiteRoot("");
    }

    /**
     * Converts an XML content tree to a JSON object
     *
     * @param tree the tree
     * @return the JSON object
     *
     * @throws JSONException if something goes wrong
     */
    public JSONObject render(CmsXmlContentTree tree) throws JSONException {

        Node root = tree.getRoot();
        return (JSONObject)renderNode(root);

    }

    /**
     * Renders the JSON representations for all locales in the given content, and adds them as fields
     * to the result JSON, with the locales as field names.
     *
     * @param content the content
     * @return the result JSON for all locales
     * @throws JSONException if something goes wrong
     */
    public JSONObject renderAllLocales(CmsXmlContent content) throws JSONException {

        List<Locale> locales = content.getLocales();
        JSONObject result = new JSONObject(true);
        for (Locale locale : locales) {
            CmsXmlContentTree tree = new CmsXmlContentTree(content, locale);
            JSONObject jsonForLocale = render(tree);
            result.put(locale.toString(), jsonForLocale);
        }
        return result;
    }

    /**
     * Renders a tree field as a field in the given JSON object.
     *
     * @param field the field to render
     * @param result the result in which to put the field
     *
     * @throws JSONException if something goes wrong
     */
    protected void renderField(Field field, JSONObject result) throws JSONException {

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
            result.put(name, array);
        } else if (field.getNodes().size() == 1) {
            if (field.getFieldDefinition().isChoiceType() && !field.isMultiChoice()) {
                // field *and* choice single-valued, so we can unwrap the single value
                JSONArray array = (JSONArray)renderNode(field.getNode());
                if (array.length() == 1) {
                    result.put(name, array.get(0));
                }
            } else {
                result.put(name, renderNode(field.getNodes().get(0)));
            }
        }
    }

    /**
     * Renders a tree node as JSON.
     *
     * @param node the tree node
     * @return the JSON (may be JSONObject, JSONArray, or String)
     *
     * @throws JSONException if something goes wrong
     */
    protected Object renderNode(Node node) throws JSONException {

        switch (node.getType()) {
            case sequence:
                List<Field> fields = node.getFields();
                JSONObject result = new JSONObject(true);
                for (Field field : fields) {
                    renderField(field, result);
                }
                return result;
            case choice:
                JSONArray array = new JSONArray();
                for (Field field : node.getFields()) {
                    JSONObject choiceObj = new JSONObject(true);
                    renderField(field, choiceObj);
                    array.put(choiceObj);
                }
                return array;
            case simple:
                return renderSimpleValue(node);
            default:
                throw new IllegalArgumentException("Unsupported node: " + node.getType());

        }

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
        if (value instanceof CmsXmlVfsFileValue) {
            CmsXmlVfsFileValue fileValue = (CmsXmlVfsFileValue)value;
            String link = fileValue.getLink(m_cms).getLink(m_cms);
            String path = fileValue.getStringValue(m_rootCms);
            return linkAndPath(link, path);
        } else if (value instanceof CmsXmlVarLinkValue) {
            CmsXmlVarLinkValue linkValue = (CmsXmlVarLinkValue)value;
            String link = linkValue.getLink(m_cms).getLink(m_cms);
            String path = linkValue.getStringValue(m_rootCms);
            return linkAndPath(link, path);
        } else {
            return node.getValue().getStringValue(m_cms);
        }
    }

    /**
     * Builds a simple JSON object with link and path fields whose values are taken from the corresponding parameters.
     *
     * @param link the value for the link field
     * @param path the value for the path field
     * @return the link-and-path object
     * @throws JSONException if something goes wrong
     */
    JSONObject linkAndPath(String link, String path) throws JSONException {

        JSONObject result = new JSONObject();
        result.put("link", link);
        result.put("path", path);
        return result;
    }

}
