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
import org.opencms.util.CmsUUID;
import org.opencms.xml.I_CmsXmlDocument;
import org.opencms.xml.page.CmsXmlPage;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.dom4j.Attribute;
import org.dom4j.Element;

/**
 * Describes the XML content type "OpenCmsVfsFile".<p>
 *
 * This type allows links to internal VFS resources only.<p>
 *
 * @since 7.0.0
 */
public class CmsXmlCategoryValue extends A_CmsXmlContentValue {

    /** Value to mark that no link is defined, "none". */
    public static final String NO_LINK = "none";

    /** The name of this type as used in the XML schema. */
    public static final String TYPE_NAME = "OpenCmsCategory";

    /** The vfs link type constant. */
    public static final String TYPE_VFS_LINK = "vfsLink";

    /** The schema definition String is located in a text for easier editing. */
    private static String m_schemaDefinition;

    /** The String value of the element node. */
    private String m_stringValue;

    /**
     * Creates a new, empty schema type descriptor of type "OpenCmsCategoryValue".<p>
     */
    public CmsXmlCategoryValue() {

        // empty constructor is required for class registration
    }

    /**
     * Creates a new XML content value of type "OpenCmsCategoryValue".<p>
     *
     * @param document the XML content instance this value belongs to
     * @param element the XML element that contains this value
     * @param locale the locale this value is created for
     * @param type the type instance to create the value for
     */
    public CmsXmlCategoryValue(I_CmsXmlDocument document, Element element, Locale locale, I_CmsXmlSchemaType type) {

        super(document, element, locale, type);
    }

    /**
     * Creates a new schema type descriptor for the type "OpenCmsCategoryValue".<p>
     *
     * @param name the name of the XML node containing the value according to the XML schema
     * @param minOccurs minimum number of occurrences of this type according to the XML schema
     * @param maxOccurs maximum number of occurrences of this type according to the XML schema
     */
    public CmsXmlCategoryValue(String name, String minOccurs, String maxOccurs) {

        super(name, minOccurs, maxOccurs);
    }

    /**
     * Fills the given element with a {@link CmsXmlCategoryValue} for the given data.<p>
     *
     * @param element the element to fill
     * @param id the id to use
     * @param rootPath the path to use
     * @param type the relation type to use
     */
    public static void fillEntry(Element element, CmsUUID id, String rootPath, CmsRelationType type) {

        CmsLink link = new CmsLink(CmsXmlCategoryValue.TYPE_VFS_LINK, type, id, rootPath, true);
        // get xml node
        Element linkElement = element.element(CmsXmlPage.NODE_LINK);
        if (linkElement == null) {
            // create xml node if needed
            linkElement = element.addElement(CmsXmlPage.NODE_LINK);
        }
        // update xml node
        CmsLinkUpdateUtil.updateXmlForVfsFile(link, linkElement);
    }

    /**
     * @see org.opencms.xml.types.A_CmsXmlContentValue#createValue(I_CmsXmlDocument, org.dom4j.Element, Locale)
     */
    public I_CmsXmlContentValue createValue(I_CmsXmlDocument document, Element element, Locale locale) {

        return new CmsXmlCategoryValue(document, element, locale, this);
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
     * Returns the link objects represented by this XML content value.<p>
     *
     * @param cms the cms context, can be <code>null</code> but in this case no link check is performed
     *
     * @return  a list of link objects represented by this XML content value
     */
    public List<CmsLink> getLinks(CmsObject cms) {

        List<CmsLink> result = new ArrayList<CmsLink>();

        @SuppressWarnings("unchecked")
        List<Element> linkElements = m_element.elements(CmsXmlPage.NODE_LINK);
        for (Element linkElement : linkElements) {
            if (linkElement == null) {
                String uri = m_element.getText();
                if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(uri)) {
                    setStringValue(cms, uri);
                }
            }
            CmsLinkUpdateUtil.updateType(linkElement, getRelationType(getPath()));
            CmsLink link = new CmsLink(linkElement);
            link.checkConsistency(cms);
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(link.getTarget())) {
                continue;
            }
            result.add(link);
        }
        return result;
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
            m_schemaDefinition = readSchemaDefinition("org/opencms/xml/types/XmlCategoryValue.xsd");
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

        return new CmsXmlCategoryValue(name, minOccurs, maxOccurs);
    }

    /**
     * Sets the value as a structure id.<p>
     *
     * @param cms the current CMS context
     * @param id the structure id which should be stored in the category value
     */
    public void setIdValue(CmsObject cms, CmsUUID id) {

        CmsRelationType type = CmsRelationType.CATEGORY;
        CmsLink link = new CmsLink(TYPE_VFS_LINK, type, id, "@", true);
        // link management check
        link.checkConsistency(cms);
        // update xml node
        CmsLinkUpdateUtil.updateXmlForVfsFile(link, m_element.addElement(CmsXmlPage.NODE_LINK));

    }

    /**
     * @see org.opencms.xml.types.A_CmsXmlContentValue#setStringValue(org.opencms.file.CmsObject, java.lang.String)
     */
    public void setStringValue(CmsObject cms, String value) throws CmsIllegalArgumentException {

        m_element.clearContent();
        // ensure the String value is re-calculated next time it's needed
        m_stringValue = null;
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(value)) {
            // no valid value given
            return;
        }
        String[] pathes = value.split(",");
        for (String path : pathes) {
            if (cms != null) {
                String siteRoot = OpenCms.getSiteManager().getSiteRoot(path);
                String oldSite = cms.getRequestContext().getSiteRoot();
                try {
                    if (siteRoot != null) {
                        // only switch the site if needed
                        cms.getRequestContext().setSiteRoot(siteRoot);
                        // remove the site root, because the link manager call will append it anyway
                        path = cms.getRequestContext().removeSiteRoot(path);
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
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(path)) {
                continue;
            }
            CmsRelationType type = getRelationType(getPath());
            CmsLink link = new CmsLink(TYPE_VFS_LINK, type, path, true);
            // link management check
            link.checkConsistency(cms);
            // update xml node
            CmsLinkUpdateUtil.updateXmlForVfsFile(link, m_element.addElement(CmsXmlPage.NODE_LINK));
        }
    }

    /**
     * Creates the String value for this category value element.<p>
     *
     * @param cms the cms context
     *
     * @return the String value for this category value element
     */
    private String createStringValue(CmsObject cms) {

        Attribute enabled = m_element.attribute(CmsXmlPage.ATTRIBUTE_ENABLED);

        String content = "";
        if ((enabled == null) || Boolean.valueOf(enabled.getText()).booleanValue()) {
            List<CmsLink> links = getLinks(cms);
            int i = 0;
            for (CmsLink link : links) {
                if (link != null) {
                    String uri = "";
                    uri += link.getUri();
                    if (cms != null) {
                        uri = cms.getRequestContext().removeSiteRoot(link.getUri());
                    }
                    if (i > 0) {
                        content += ",";
                    }
                    content += uri;
                    i++;
                }
            }
        }
        return content;
    }
}