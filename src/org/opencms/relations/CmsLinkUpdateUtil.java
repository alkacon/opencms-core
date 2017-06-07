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

package org.opencms.relations;

import org.dom4j.Attribute;
import org.dom4j.Element;
import org.htmlparser.util.Translate;

/**
 * An utility class for updating the link xml node.<p>
 *
 * @since 6.0.0
 */
public final class CmsLinkUpdateUtil {

    /**
     * Prevents the instanciation of this utility class.<p>
     */
    private CmsLinkUpdateUtil() {

        // no-op
    }

    /**
     * Updates the type for a link xml element node.<p>
     *
     * @param element the link element node to update
     * @param type the relation type to set
     */
    public static void updateType(Element element, CmsRelationType type) {

        updateAttribute(element, CmsLink.ATTRIBUTE_TYPE, type.getNameForXml());
    }

    /**
     * Updates the link node in the underlying XML page document.<p>
     *
     * @param link the link to update
     * @param element the &lt;link&gt; element to update
     * @param updateOnly if set and the element has no {@link CmsLink#NODE_TARGET} subelement, so no action if executed at all
     */
    public static void updateXml(CmsLink link, Element element, boolean updateOnly) {

        // if element is not null
        if (element != null) {
            if (!updateOnly || (element.element(CmsLink.NODE_TARGET) != null)) {
                String strId = (link.getStructureId() == null ? null : link.getStructureId().toString());
                // there may still be entities in the target, so we decode it
                updateNode(element, CmsLink.NODE_TARGET, link.getTarget(), true);
                updateNode(element, CmsLink.NODE_UUID, strId, false);
                updateNode(element, CmsLink.NODE_ANCHOR, link.getAnchor(), true);
                updateNode(element, CmsLink.NODE_QUERY, link.getQuery(), true);
            }
        }
    }

    /**
     * Updates the given xml element with this link information.<p>
     *
     * @param link the link to get the information from
     * @param name the (optional) name of the link
     * @param element the &lt;link&gt; element to update
     */
    public static void updateXmlForHtmlValue(CmsLink link, String name, Element element) {

        // if element is not null
        if (element != null) {
            // update the additional attributes
            if (name != null) {
                updateAttribute(element, CmsLink.ATTRIBUTE_NAME, link.getName());
            }
            updateAttribute(element, CmsLink.ATTRIBUTE_INTERNAL, Boolean.toString(link.isInternal()));
            // update the common sub-elements and attributes
            updateXmlForVfsFile(link, element);
        }
    }

    /**
     * Updates the given xml element with this link information.<p>
     *
     * @param link the link to get the information from
     * @param element the &lt;link&gt; element to update
     */
    public static void updateXmlForVfsFile(CmsLink link, Element element) {

        // if element is not null
        if (element != null) {
            // update the type attribute
            updateAttribute(element, CmsLink.ATTRIBUTE_TYPE, link.getType().getNameForXml());
            // update the sub-elements
            updateXml(link, element, false);
        }
    }

    /**
     * Decodes entities in a string if it isn't null.<p>
     *
     * @param value the string for which to decode entities
     *
     * @return the string with the decoded entities
     */
    protected static String decodeEntities(String value) {

        if (value != null) {
            value = Translate.decode(value);
        }
        return value;
    }

    /**
     * Updates the given xml element attribute with the given value.<p>
     *
     * @param parent the element to set the attribute for
     * @param attrName the attribute name
     * @param value the value to set, or <code>null</code> to remove
     */
    private static void updateAttribute(Element parent, String attrName, String value) {

        if (parent != null) {
            Attribute attribute = parent.attribute(attrName);
            if (value != null) {
                if (attribute == null) {
                    parent.addAttribute(attrName, value);
                } else {
                    attribute.setValue(value);
                }
            } else {
                // remove only if exists
                if (attribute != null) {
                    parent.remove(attribute);
                }
            }
        }
    }

    /**
     * Updates the given xml node with the given value.<p>
     *
     * @param parent the parent node
     * @param nodeName the node to update
     * @param value the value to use to update the given node, can be <code>null</code>
     * @param cdata if the value should be in a CDATA section or not
     */
    private static void updateNode(Element parent, String nodeName, String value, boolean cdata) {

        // get current node element
        Element nodeElement = parent.element(nodeName);
        if (value != null) {
            if (nodeElement == null) {
                // element wasn't there before, add element and set value
                nodeElement = parent.addElement(nodeName);
            }
            // element is there, update element value
            nodeElement.clearContent();
            if (cdata) {
                nodeElement.addCDATA(value);
            } else {
                nodeElement.addText(value);
            }
        } else {
            // remove only if element exists
            if (nodeElement != null) {
                // remove element
                parent.remove(nodeElement);
            }
        }
    }
}