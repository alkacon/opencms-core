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
 * For further information about Alkacon Software GmbH, please see the
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
import org.opencms.i18n.CmsEncoder;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsRuntimeException;
import org.opencms.relations.CmsLink;
import org.opencms.relations.CmsLinkUpdateUtil;
import org.opencms.staticexport.CmsLinkProcessor;
import org.opencms.staticexport.CmsLinkTable;
import org.opencms.util.CmsHtmlConverter;
import org.opencms.util.CmsHtmlExtractor;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.CmsXmlGenericWrapper;
import org.opencms.xml.I_CmsXmlDocument;
import org.opencms.xml.page.CmsXmlPage;

import java.util.Iterator;
import java.util.Locale;

import org.apache.commons.logging.Log;

import org.dom4j.Attribute;
import org.dom4j.Element;
import org.htmlparser.util.ParserException;

/**
 * Describes the XML content type "OpenCmsHtml".<p>
 * 
 * @since 6.0.0 
 */
public class CmsXmlHtmlValue extends A_CmsXmlContentValue {

    /** The name of this type as used in the XML schema. */
    public static final String TYPE_NAME = "OpenCmsHtml";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsXmlHtmlValue.class);

    /** The schema definition String is located in a text for easier editing. */
    private static String m_schemaDefinition;

    /** Null value for plain text extraction errors. */
    private static final String NULL_VALUE = "null";

    /** Base type for single type instances, required for XML pages. */
    private static final I_CmsXmlSchemaType TYPE_BASE = new CmsXmlHtmlValue("base", "1", "1");

    /** The plain text value of the element node. */
    private String m_plainTextValue;

    /** The String value of the element node. */
    private String m_stringValue;

    /**
     * Creates a new, empty schema type descriptor of type "OpenCmsHtml".<p>
     */
    public CmsXmlHtmlValue() {

        // empty constructor is required for class registration
    }

    /**
     * Creates a new XML content value of type "OpenCmsHtml".<p>
     * 
     * @param document the XML content instance this value belongs to
     * @param element the XML element that contains this value
     * @param locale the locale this value is created for
     */
    public CmsXmlHtmlValue(I_CmsXmlDocument document, Element element, Locale locale) {

        super(document, element, locale, TYPE_BASE);
    }

    /**
     * Creates a new XML content value of type "OpenCmsHtml".<p>
     * 
     * @param document the XML content instance this value belongs to
     * @param element the XML element that contains this value
     * @param locale the locale this value is created for
     * @param type the type instance to create the value for
     */
    public CmsXmlHtmlValue(I_CmsXmlDocument document, Element element, Locale locale, I_CmsXmlSchemaType type) {

        super(document, element, locale, type);
    }

    /**
     * Creates a new schema type descriptor for the type "OpenCmsHtml".<p>
     * 
     * @param name the name of the XML node containing the value according to the XML schema
     * @param minOccurs minimum number of occurrences of this type according to the XML schema
     * @param maxOccurs maximum number of occurrences of this type according to the XML schema
     */
    public CmsXmlHtmlValue(String name, String minOccurs, String maxOccurs) {

        super(name, minOccurs, maxOccurs);
    }

    /**
     * @see org.opencms.xml.types.A_CmsXmlContentValue#createValue(I_CmsXmlDocument, org.dom4j.Element, Locale)
     */
    public I_CmsXmlContentValue createValue(I_CmsXmlDocument document, Element element, Locale locale) {

        return new CmsXmlHtmlValue(document, element, locale, this);
    }

    /**
     * @see org.opencms.xml.types.I_CmsXmlSchemaType#generateXml(org.opencms.file.CmsObject, org.opencms.xml.I_CmsXmlDocument, org.dom4j.Element, java.util.Locale)
     */
    @Override
    public Element generateXml(CmsObject cms, I_CmsXmlDocument document, Element root, Locale locale) {

        Element element = root.addElement(getName());
        int index = element.getParent().elements(element.getQName()).indexOf(element);
        element.addAttribute(CmsXmlPage.ATTRIBUTE_NAME, getName() + index);
        element.addElement(CmsXmlPage.NODE_LINKS);
        element.addElement(CmsXmlPage.NODE_CONTENT);

        // get the default value from the content handler
        String defaultValue = document.getHandler().getDefault(cms, this, locale);
        if (defaultValue != null) {
            try {
                I_CmsXmlContentValue value = createValue(document, element, locale);
                value.setStringValue(cms, defaultValue);
            } catch (CmsRuntimeException e) {
                // should not happen if default value is correct
                LOG.error(
                    Messages.get().getBundle().key(Messages.ERR_XMLCONTENT_INVALID_ELEM_DEFAULT_1, defaultValue),
                    e);
                element.clearContent();
            }
        }
        return element;
    }

    /**
     * Returns the link table of this XML page element.<p>
     * 
     * @return the link table of this XML page element
     */
    public CmsLinkTable getLinkTable() {

        CmsLinkTable linkTable = new CmsLinkTable();
        Element links = m_element.element(CmsXmlPage.NODE_LINKS);
        if (links != null) {
            Iterator<Element> itLinks = CmsXmlGenericWrapper.elementIterator(links, CmsXmlPage.NODE_LINK);
            while (itLinks.hasNext()) {
                Element lelem = itLinks.next();
                linkTable.addLink(new CmsLink(lelem));
            }
        }
        return linkTable;
    }

    /**
     * @see org.opencms.xml.types.I_CmsXmlContentValue#getPlainText(org.opencms.file.CmsObject)
     */
    @Override
    public String getPlainText(CmsObject cms) {

        if (m_plainTextValue == null) {
            try {
                m_plainTextValue = CmsHtmlExtractor.extractText(this.getStringValue(cms), m_document.getEncoding());
            } catch (Exception exc) {
                m_plainTextValue = NULL_VALUE;
            }
        }
        if (m_plainTextValue == NULL_VALUE) {
            return null;
        }
        return m_plainTextValue;
    }

    /**
     * @see org.opencms.xml.types.I_CmsXmlSchemaType#getSchemaDefinition()
     */
    public String getSchemaDefinition() {

        // the schema definition is located in a separate file for easier editing
        if (m_schemaDefinition == null) {
            m_schemaDefinition = readSchemaDefinition("org/opencms/xml/types/XmlHtmlValue.xsd");
        }
        return m_schemaDefinition;
    }

    /**
     * @see org.opencms.xml.types.I_CmsXmlContentValue#getStringValue(org.opencms.file.CmsObject)
     */
    public String getStringValue(CmsObject cms) {

        if (m_stringValue == null) {
            m_stringValue = createStringValue(cms, m_document);
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
     * @see org.opencms.xml.types.A_CmsXmlContentValue#newInstance(java.lang.String, java.lang.String, java.lang.String)
     */
    public I_CmsXmlSchemaType newInstance(String name, String minOccurs, String maxOccurs) {

        return new CmsXmlHtmlValue(name, minOccurs, maxOccurs);
    }

    /**
     * @see org.opencms.xml.types.I_CmsXmlContentValue#setStringValue(org.opencms.file.CmsObject, java.lang.String)
     */
    public void setStringValue(CmsObject cms, String value) {

        Element content = m_element.element(CmsXmlPage.NODE_CONTENT);
        Element links = m_element.element(CmsXmlPage.NODE_LINKS);
        CmsLinkProcessor linkProcessor = null;

        String encoding = m_document.getEncoding();
        linkProcessor = m_document.getLinkProcessor(cms, new CmsLinkTable());

        String finalValue = value;
        if (finalValue != null) {
            // nested CDATA tags are not allowed, so replace CDATA tags with their contents
            finalValue = finalValue.replaceAll("(?s)// <!\\[CDATA\\[(.*?)// \\]\\]>", "$1"); // special case for embedded Javascript 
            finalValue = finalValue.replaceAll("(?s)<!\\[CDATA\\[(.*?)\\]\\]>", "$1");
        }
        if (encoding != null) {
            // ensure all chars in the given content are valid chars for the selected charset
            finalValue = CmsEncoder.adjustHtmlEncoding(finalValue, encoding);
        }

        // remove unnecessary tags if required
        String contentConversion = m_document.getConversion();
        if (CmsHtmlConverter.isConversionEnabled(contentConversion)) {
            CmsHtmlConverter converter = new CmsHtmlConverter(encoding, contentConversion);
            finalValue = converter.convertToStringSilent(finalValue);
            finalValue = fixNullCharacters(finalValue);
        }
        if (linkProcessor != null) {
            try {
                // replace links in HTML by macros and fill link table      
                finalValue = linkProcessor.replaceLinks(finalValue);
            } catch (Exception exc) {
                throw new CmsRuntimeException(Messages.get().container(Messages.ERR_HTML_DATA_PROCESSING_0), exc);
            }
        }

        content.clearContent();
        links.clearContent();

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(finalValue)) {
            content.addCDATA(finalValue);
            if (linkProcessor != null) {
                // may be null in case of default value generation (i.e. setStringValue(String) was called)

                CmsLinkTable linkTable = linkProcessor.getLinkTable();
                for (Iterator<CmsLink> i = linkTable.iterator(); i.hasNext();) {
                    CmsLink link = i.next();
                    CmsLinkUpdateUtil.updateXmlForHtmlValue(
                        link,
                        link.getName(),
                        links.addElement(CmsXmlPage.NODE_LINK));
                }
            }
        }

        // ensure the String value is re-calculated next time
        m_stringValue = null;
    }

    /**
     * JTidy sometimes erroneouslsy produces HTML containing 'null' characters (Unicode code point 0), which are 
     * invalid in an XML document. Until we find a way to prevent JTidy doing that, we remove the null characters 
     * from the HTML, and log a warning.<p>
     *
     * @param jtidyOutput the JTidy output 
     * @return the output with null characters removed 
     */
    protected String fixNullCharacters(String jtidyOutput) {

        String outputWithoutNullChars = jtidyOutput.replaceAll("\u0000", "");
        if (jtidyOutput.length() != outputWithoutNullChars.length()) {
            String context = "";
            if (m_document.getFile() != null) {
                context = "(file=" + m_document.getFile().getRootPath() + ")";
            }
            LOG.warn("HTML cleanup produced invalid null characters in output. " + context);
            LOG.debug("HTML cleanup output = " + jtidyOutput);
        }
        return outputWithoutNullChars;
    }

    /**
     * Creates the String value for this HTML value element.<p>
     * 
     * @param cms an initialized instance of a CmsObject
     * @param document the XML document this value belongs to
     * 
     * @return the String value for this HTML value element
     */
    private String createStringValue(CmsObject cms, I_CmsXmlDocument document) {

        Element data = m_element.element(CmsXmlPage.NODE_CONTENT);
        if (data == null) {
            String content = m_element.getText();
            m_element.clearContent();
            int index = m_element.getParent().elements(m_element.getQName()).indexOf(m_element);
            m_element.addAttribute(CmsXmlPage.ATTRIBUTE_NAME, getName() + index);
            m_element.addElement(CmsXmlPage.NODE_LINKS);
            m_element.addElement(CmsXmlPage.NODE_CONTENT).addCDATA(content);
            data = m_element.element(CmsXmlPage.NODE_CONTENT);
        }
        Attribute enabled = m_element.attribute(CmsXmlPage.ATTRIBUTE_ENABLED);

        String content = "";
        if ((enabled == null) || Boolean.valueOf(enabled.getText()).booleanValue()) {

            content = data.getText();

            CmsLinkTable linkTable = getLinkTable();
            if (!linkTable.isEmpty()) {

                // link processing: replace macros with links
                CmsLinkProcessor linkProcessor = document.getLinkProcessor(cms, linkTable);
                try {
                    content = linkProcessor.processLinks(content);
                } catch (ParserException e) {
                    // should better not happen
                    LOG.error(Messages.get().getBundle().key(Messages.ERR_XMLCONTENT_LINK_PROCESS_FAILED_0), e);
                }
            }
        }
        return content;
    }
}