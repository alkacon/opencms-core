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

package org.opencms.xml.types;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsLink;
import org.opencms.relations.CmsLinkUpdateUtil;
import org.opencms.relations.CmsRelationType;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.I_CmsXmlDocument;
import org.opencms.xml.page.CmsXmlPage;

import java.util.Locale;

import org.dom4j.Attribute;
import org.dom4j.Element;

/**
 * Describes the XML content type "OpenCmsVarLink".<p>
 *
 * This type allows a link to either an internal VFS resource, or to an external website.<p>
 *
 * @since 7.0.0
 */
public class CmsXmlVarLinkValue extends A_CmsXmlContentValue {

    /** Value to mark that no link is defined, "none". */
    public static final String NO_LINK = "none";

    /** The name of this type as used in the XML schema. */
    public static final String TYPE_NAME = "OpenCmsVarLink";

    /** The var link type constant. */
    public static final String TYPE_VAR_LINK = "varLink";

    /** The schema definition String is located in a text for easier editing. */
    private static String m_schemaDefinition;

    /** The link object represented by this object. */
    private CmsLink m_linkValue;

    /** The String value of the element node. */
    private String m_stringValue;

    /**
     * Creates a new, empty schema type descriptor of type "OpenCmsVfsFile".<p>
     */
    public CmsXmlVarLinkValue() {

        // empty constructor is required for class registration
    }

    /**
     * Creates a new XML content value of type "OpenCmsVfsFile".<p>
     *
     * @param document the XML content instance this value belongs to
     * @param element the XML element that contains this value
     * @param locale the locale this value is created for
     * @param type the type instance to create the value for
     */
    public CmsXmlVarLinkValue(I_CmsXmlDocument document, Element element, Locale locale, I_CmsXmlSchemaType type) {

        super(document, element, locale, type);
    }

    /**
     * Creates a new schema type descriptor for the type "OpenCmsVfsFile".<p>
     *
     * @param name the name of the XML node containing the value according to the XML schema
     * @param minOccurs minimum number of occurrences of this type according to the XML schema
     * @param maxOccurs maximum number of occurrences of this type according to the XML schema
     */
    public CmsXmlVarLinkValue(String name, String minOccurs, String maxOccurs) {

        super(name, minOccurs, maxOccurs);
    }

    /**
     * @see org.opencms.xml.types.A_CmsXmlContentValue#createValue(I_CmsXmlDocument, org.dom4j.Element, Locale)
     */
    public I_CmsXmlContentValue createValue(I_CmsXmlDocument document, Element element, Locale locale) {

        return new CmsXmlVarLinkValue(document, element, locale, this);
    }

    /**
     * @see org.opencms.xml.types.I_CmsXmlSchemaType#generateXml(org.opencms.file.CmsObject, org.opencms.xml.I_CmsXmlDocument, org.dom4j.Element, java.util.Locale)
     */
    @Override
    public Element generateXml(CmsObject cms, I_CmsXmlDocument document, Element root, Locale locale) {

        Element element = root.addElement(getName());

        // get the default value from the content handler
        String defaultValue = document.getHandler().getDefault(cms, this, locale);
        if (defaultValue != null) {
            I_CmsXmlContentValue value = createValue(document, element, locale);
            value.setStringValue(cms, defaultValue);
        }
        return element;
    }

    /**
     * Returns the link object represented by this XML content value.<p>
     *
     * @param cms the cms context, can be <code>null</code> but in this case no link check is performed,
     *      and the target is marked as "external"
     *
     * @return the link object represented by this XML content value
     */
    public CmsLink getLink(CmsObject cms) {

        if (m_linkValue == null) {
            // need to to calculate link value twice
            Element linkElement = m_element.element(CmsXmlPage.NODE_LINK);
            if (linkElement == null) {
                setStringValue(cms, m_element.getText());
            } else {
                CmsLinkUpdateUtil.updateType(linkElement, getRelationType(getPath()));
                CmsLink link = new CmsLink(linkElement);
                if (link.isInternal()) {
                    // link management check
                    link.checkConsistency(cms);
                }
                if (CmsStringUtil.isEmptyOrWhitespaceOnly(link.getTarget())) {
                    // this may just be an anchor link
                    m_linkValue = CmsLink.NULL_LINK;
                } else {
                    m_linkValue = link;
                }
            }
        }
        if (m_linkValue == CmsLink.NULL_LINK) {
            return null;
        }
        return m_linkValue;
    }

    /**
     * @see org.opencms.xml.types.I_CmsXmlContentValue#getPlainText(org.opencms.file.CmsObject)
     */
    @Override
    public String getPlainText(CmsObject cms) {

        return getStringValue(cms);
    }

    /**
     * @see org.opencms.xml.types.I_CmsXmlSchemaType#getSchemaDefinition()
     */
    public String getSchemaDefinition() {

        // the schema definition is located in a separate file for easier editing
        if (m_schemaDefinition == null) {
            m_schemaDefinition = readSchemaDefinition("org/opencms/xml/types/XmlVarLinkValue.xsd");
        }
        return m_schemaDefinition;
    }

    /**
     * @see org.opencms.xml.types.I_CmsXmlContentValue#getStringValue(CmsObject)
     */
    public String getStringValue(CmsObject cms) throws CmsRuntimeException {

        if (m_stringValue == null) {
            m_stringValue = createStringValue(cms);
        }
        return m_stringValue;
    }

    /**
     * @see org.opencms.xml.types.A_CmsXmlContentValue#getTypeName()
     */
    public String getTypeName() {

        return TYPE_NAME;
    }

    /**
     * @see org.opencms.xml.types.A_CmsXmlContentValue#isSearchable()
     */
    @Override
    public boolean isSearchable() {

        // there is no point in searching link values
        return false;
    }

    /**
     * @see org.opencms.xml.types.A_CmsXmlContentValue#newInstance(java.lang.String, java.lang.String, java.lang.String)
     */
    public I_CmsXmlSchemaType newInstance(String name, String minOccurs, String maxOccurs) {

        return new CmsXmlVarLinkValue(name, minOccurs, maxOccurs);
    }

    /**
     * @see org.opencms.xml.types.A_CmsXmlContentValue#setStringValue(org.opencms.file.CmsObject, java.lang.String)
     */
    public void setStringValue(CmsObject cms, String value) throws CmsIllegalArgumentException {

        // element is rebuild from given String value below
        m_element.clearContent();
        // link value is re-calculated below
        m_linkValue = null;
        // ensure the String value is re-calculated next time it's needed
        m_stringValue = null;
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(value)) {
            // no valid value given
            return;
        }

        String path = value;
        if (cms != null) {
            String siteRoot = OpenCms.getSiteManager().getSiteRoot(value);
            String oldSite = cms.getRequestContext().getSiteRoot();
            try {
                if (siteRoot != null) {
                    // only switch the site if needed
                    cms.getRequestContext().setSiteRoot(siteRoot);
                    // remove the site root, because the link manager call will append it anyway
                    path = cms.getRequestContext().removeSiteRoot(value);
                }
                // remove parameters, if not the link manager call might fail
                String query = "";
                int pos = path.indexOf(CmsRequestUtil.URL_DELIMITER);
                int anchorPos = path.indexOf('#');
                if ((pos == -1) || ((anchorPos > -1) && (pos > anchorPos))) {
                    pos = anchorPos;
                }
                if (pos > -1) {
                    query = path.substring(pos);
                    path = path.substring(0, pos);
                }
                // get the root path
                path = OpenCms.getLinkManager().getRootPath(cms, path);
                if (path != null) {
                    // append parameters again
                    path += query;
                }
            } finally {
                if (siteRoot != null) {
                    cms.getRequestContext().setSiteRoot(oldSite);
                }
            }
        }
        boolean internal = (path != null);
        CmsRelationType type;
        if (internal) {
            type = getRelationType(getPath());
        } else {
            // use original value for external links
            path = value;
            // external links are always "weak"
            type = CmsRelationType.XML_WEAK;
        }
        CmsLink link = new CmsLink(TYPE_VAR_LINK, type, path, internal);
        if (internal) {
            // link management check for internal links
            link.checkConsistency(cms);
        }
        // update xml node
        CmsLinkUpdateUtil.updateXmlForHtmlValue(link, null, m_element.addElement(CmsXmlPage.NODE_LINK));
        // store the calculated link
        m_linkValue = link;
    }

    /**
     * Creates the String value for this VarLink value element.<p>
     *
     * @param cms the current users OpenCms context
     *
     * @return the String value for this VarLink value element
     */
    private String createStringValue(CmsObject cms) {

        Attribute enabled = m_element.attribute(CmsXmlPage.ATTRIBUTE_ENABLED);

        String content = "";
        if ((enabled == null) || Boolean.valueOf(enabled.getText()).booleanValue()) {
            CmsLink link = getLink(cms);
            if (link != null) {
                content = link.getUri();
                if (link.isInternal() && (cms != null)) {
                    // remove site root for internal links
                    content = cms.getRequestContext().removeSiteRoot(link.getUri());
                }
            }
        }
        return content;
    }
}