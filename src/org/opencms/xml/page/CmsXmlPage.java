/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/page/CmsXmlPage.java,v $
 * Date   : $Date: 2004/10/14 15:05:54 $
 * Version: $Revision: 1.12 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.xml.page;

import org.opencms.configuration.CmsConfigurationManager;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.main.OpenCms;
import org.opencms.staticexport.CmsLinkProcessor;
import org.opencms.staticexport.CmsLinkTable;
import org.opencms.xml.A_CmsXmlDocument;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.types.CmsXmlHtmlValue;
import org.opencms.xml.types.I_CmsXmlContentValue;
import org.opencms.xml.types.I_CmsXmlSchemaType;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Implementation of a page object used to access and manage xml data.<p>
 * 
 * This implementation consists of several named elements optionally available for 
 * various languages. The data of each element is accessible via its name and language. 
 * 
 * The content of each element is stored as CDATA, links within the 
 * content are processed and are seperately accessible as entries of a CmsLinkTable.
 * 
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.12 $
 */
public class CmsXmlPage extends A_CmsXmlDocument {

    /** Name of the name attribute of the elements node. */
    public static final String ATTRIBUTE_ENABLED = "enabled";

    /** Name of the internal attribute of the link node. */
    public static final String ATTRIBUTE_INTERNAL = "internal";

    /** Name of the language attribute of the elements node. */
    public static final String ATTRIBUTE_LANGUAGE = "language";

    /** Name of the name attribute of the elements node. */
    public static final String ATTRIBUTE_NAME = "name";

    /** Name of the type attribute of the elements node. */
    public static final String ATTRIBUTE_TYPE = "type";

    /** Property to check if relative links are allowed. */
    public static final String C_PROPERTY_ALLOW_RELATIVE = "allowRelativeLinks";

    /** The DTD address of the OpenCms xmlpage. */
    public static final String C_XMLPAGE_XSD_SYSTEM_ID = CmsConfigurationManager.C_DEFAULT_DTD_PREFIX + "xmlpage.xsd";

    /** Name of the anchor node. */
    public static final String NODE_ANCHOR = "anchor";

    /** Name of the element node. */
    public static final String NODE_CONTENT = "content";

    /** Name of the elements node. */
    public static final String NODE_ELEMENTS = "elements";

    /** Name of the link node. */
    public static final String NODE_LINK = "link";

    /** Name of the links node. */
    public static final String NODE_LINKS = "links";

    /** Name of the page node. */
    public static final String NODE_PAGE = "page";

    /** Name of the page node. */
    public static final String NODE_PAGES = "pages";

    /** Name of the query node. */
    public static final String NODE_QUERY = "query";

    /** Name of the target node. */
    public static final String NODE_TARGET = "target";

    /** Name of the element node. */
    private static final String NODE_ELEMENT = "element";

    /** Indicates if relative Links are allowed. */
    private boolean m_allowRelativeLinks;

    /**
     * Creates a new CmsXmlPage based on the provided document and encoding.<p>
     * 
     * The encoding is used for marshalling the XML document later.<p>
     * 
     * @param document the document to create the CmsXmlPage from
     * @param encoding the encoding of the xml page
     */
    public CmsXmlPage(Document document, String encoding) {

        initDocument(document, encoding, null);
    }

    /**
     * Creates an empty XML page in the provided locale using 
     * the provided encoding.<p>
     * 
     * The page is initialized according to the minimal neccessary xml structure.
     * The encoding is used for marshalling the XML document later.<p>
     * 
     * @param locale the initial locale of the XML page
     * @param encoding the encoding of the XML page
     */
    public CmsXmlPage(Locale locale, String encoding) {

        initDocument(CmsXmlPageFactory.createDocument(locale), encoding, null);
    }

    /**
     * Adds a new, empty value with the given name and locale
     * to this XML document.<p>
     *  
     * @param name the name of the value
     * @param locale the locale of the value
     */
    public void addValue(String name, Locale locale) {

        Element pages = m_document.getRootElement();
        String localeStr = locale.toString();
        Element page = null;

        // search if a page for the selected language is already available
        for (Iterator i = pages.elementIterator(NODE_PAGE); i.hasNext();) {
            Element nextPage = (Element)i.next();
            String language = nextPage.attributeValue(ATTRIBUTE_LANGUAGE);
            if (localeStr.equals(language)) {
                // a page for the selected language was found
                page = nextPage;
                break;
            }
        }

        int pos = 0;
        // create the new element
        Element element;
        if (page != null) {
            // page for selected language already available
            pos = page.elements(NODE_ELEMENT).size();
            element = page.addElement(NODE_ELEMENT).addAttribute(ATTRIBUTE_NAME, name);
        } else {
            // no page for the selected language was found
            element = pages.addElement(NODE_PAGE).addAttribute(ATTRIBUTE_LANGUAGE, localeStr);
            element = element.addElement(NODE_ELEMENT).addAttribute(ATTRIBUTE_NAME, name);
        }

        // add empty nodes for link table and content to the element
        element.addElement(NODE_LINKS);
        element.addElement(NODE_CONTENT);

        CmsXmlHtmlValue value = new CmsXmlHtmlValue(element, NODE_ELEMENT, pos);

        // bookmark the element
        addBookmark(name, locale, true, value);
    }

    /**
     * Returns if relative links are accepted (and left unprocessed).<p>
     * 
     * @return true if relative links are allowed
     */
    public boolean getAllowRelativeLinks() {

        return m_allowRelativeLinks;
    }

    /**
     * @see org.opencms.xml.I_CmsXmlDocument#getContentDefinition(org.xml.sax.EntityResolver)
     */
    public CmsXmlContentDefinition getContentDefinition(EntityResolver resolver) {

        // Note regarding exception handling:
        // Since this object already is a valid XML content object,
        // it must have a valid schema, otherwise it would not exist.
        // Therefore the exceptions should never be really thrown.
        InputSource source;
        try {
            source = resolver.resolveEntity(null, C_XMLPAGE_XSD_SYSTEM_ID);
            return CmsXmlContentDefinition.unmarshal(source, C_XMLPAGE_XSD_SYSTEM_ID, resolver);
        } catch (SAXException e) {
            throw new RuntimeException("Could not parse XML page content definition schema", e);
        } catch (IOException e) {
            throw new RuntimeException("IO error resolving XML page content definition schema", e);
        } catch (CmsXmlException e) {
            throw new RuntimeException("Unable to unmarshal XML page content definition schema", e);
        }
    }

    /**
     * @see org.opencms.xml.A_CmsXmlDocument#getLinkProcessor(org.opencms.file.CmsObject, org.opencms.staticexport.CmsLinkTable)
     */
    public CmsLinkProcessor getLinkProcessor(CmsObject cms, CmsLinkTable linkTable) {

        // initialize link processor
        String relativeRoot = null;
        if ((!m_allowRelativeLinks) && (m_file != null)) {
            relativeRoot = CmsResource.getParentFolder(cms.getSitePath(m_file));
        }
        return new CmsLinkProcessor(cms, linkTable, getEncoding(), relativeRoot);
    }

    /**
     * Returns the link table of an element.<p>
     * 
     * @param name name of the element
     * @param locale locale of the element
     * @return the link table
     */
    public CmsLinkTable getLinkTable(String name, Locale locale) {

        CmsXmlHtmlValue value = (CmsXmlHtmlValue)getValue(name, locale);
        if (value != null) {
            return value.getLinkTable();
        }
        return new CmsLinkTable();
    }

    /**
     * Checks if the element of a page object is enabled.<p>
     * 
     * @param name the name of the element
     * @param locale the locale of the element
     * @return true if the element exists and is not disabled
     */
    public boolean isEnabled(String name, Locale locale) {

        CmsXmlHtmlValue value = (CmsXmlHtmlValue)getValue(name, locale);

        if (value != null) {
            Element element = value.getElement();
            Attribute enabled = element.attribute(ATTRIBUTE_ENABLED);
            return (enabled == null || Boolean.valueOf(enabled.getValue()).booleanValue());
        }

        return false;
    }

    /**
     * Removes an existing value with the given name and locale
     * from this XML document.<p>
     * 
     * @param name the name of the value
     * @param locale the locale of the value
     */
    public void removeValue(String name, Locale locale) {

        List list = removeBookmark(name, locale);
        Iterator i = list.iterator();
        while (i.hasNext()) {
            I_CmsXmlContentValue value = (I_CmsXmlContentValue)i.next();
            Element element = value.getElement();
            element.detach();
        }
    }     
    
    /**
     * Sets the enabled flag of an already existing element.<p>
     * 
     * Note: if isEnabled is set to true, the attribute is removed
     * since true is the default
     * 
     * @param name name name of the element
     * @param locale locale of the element
     * @param isEnabled enabled flag for the element
     */
    public void setEnabled(String name, Locale locale, boolean isEnabled) {

        CmsXmlHtmlValue value = (CmsXmlHtmlValue)getValue(name, locale);
        Element element = value.getElement();
        Attribute enabled = element.attribute(ATTRIBUTE_ENABLED);

        if (enabled == null) {
            if (!isEnabled) {
                element.addAttribute(ATTRIBUTE_ENABLED, Boolean.toString(isEnabled));
            }
        } else if (isEnabled) {
            element.remove(enabled);
        } else {
            enabled.setValue(Boolean.toString(isEnabled));
        }
    }

    /**
     * Sets the data of an already existing value.<p>
     * 
     * The data will be enclosed as CDATA within the xml page structure.
     * When setting the element data, the content of this element will be
     * processed automatically.<p>
     * 
     * @param cms the cms object
     * @param name name of the element
     * @param locale locale of the element
     * @param content character data (CDATA) of the element
     * 
     * @throws CmsXmlException if something goes wrong
     */
    public void setStringValue(CmsObject cms, String name, Locale locale, String content) throws CmsXmlException {

        CmsXmlHtmlValue value = (CmsXmlHtmlValue)getValue(name, locale);

        if (value != null) {
            // set the values
            value.setStringValue(cms, this, content);
        } else {
            throw new CmsXmlException("Invalid XML page element '" + locale + "/" + name + "' selected");
        }
    }

    /**
     * @see org.opencms.xml.A_CmsXmlDocument#initDocument(org.dom4j.Document, java.lang.String, org.xml.sax.EntityResolver)
     */
    protected void initDocument(Document document, String encoding, EntityResolver resolver) {

        m_encoding = CmsEncoder.lookupEncoding(encoding, encoding);
        m_document = document;
        m_elementLocales = new HashMap();
        m_elementNames = new HashMap();

        // convert pre 5.5.6 XML page documents
        if (!NODE_PAGES.equals(m_document.getRootElement().getName())) {
            convertOldDocument();
        }

        // initialize the bookmarks
        Element pages = m_document.getRootElement();
        try {
            for (Iterator i = pages.elementIterator(NODE_PAGE); i.hasNext();) {

                Element page = (Element)i.next();
                Locale locale = CmsLocaleManager.getLocale(page.attributeValue(ATTRIBUTE_LANGUAGE));
                int pos = 0;
                for (Iterator j = page.elementIterator(NODE_ELEMENT); j.hasNext();) {

                    Element element = (Element)j.next();
                    String name = element.attributeValue(ATTRIBUTE_NAME);

                    String elementEnabled = element.attributeValue(ATTRIBUTE_ENABLED);
                    boolean enabled = (elementEnabled == null) ? true : Boolean.valueOf(elementEnabled).booleanValue();

                    // create an element type from the XML node                    
                    CmsXmlHtmlValue value = new CmsXmlHtmlValue(element, NODE_ELEMENT, pos);

                    // add the element type bookmark
                    addBookmark(name, locale, enabled, value);
                    pos++;
                }
            }
        } catch (NullPointerException e) {
            OpenCms.getLog(this).error("Error while initalizing XML page bookmarks", e);
        }
    }

    /**
     * Sets the parameter that controls the relative link generation.<p>
     * 
     * @param value the parameter that controls the relative link generation
     */
    protected void setAllowRelativeLinks(boolean value) {

        m_allowRelativeLinks = value;
    }

    /**
     * Sets the file this XML page content is written to.<p> 
     * 
     * @param file the file this XML page content is written to
     */
    protected void setFile(CmsFile file) {

        m_file = file;
    }

    /**
     * Converts the XML structure of the pre 5.5.0 development version of 
     * the XML page to the final 6.0 version.<p>
     */
    private void convertOldDocument() {

        Document newDocument = DocumentHelper.createDocument();
        Element root = newDocument.addElement(NODE_PAGES);
        root.add(I_CmsXmlSchemaType.XSI_NAMESPACE);
        root.addAttribute(I_CmsXmlSchemaType.XSI_NAMESPACE_ATTRIBUTE_NO_SCHEMA_LOCATION, C_XMLPAGE_XSD_SYSTEM_ID);

        Map pages = new HashMap();

        for (Iterator i = m_document.getRootElement().element(NODE_ELEMENTS).elementIterator(NODE_ELEMENT); i.hasNext();) {

            Element elem = (Element)i.next();
            try {
                String elementName = elem.attributeValue(ATTRIBUTE_NAME);
                String elementLang = elem.attributeValue(ATTRIBUTE_LANGUAGE);
                String elementEnabled = elem.attributeValue(ATTRIBUTE_ENABLED);
                boolean enabled = (elementEnabled == null) ? true : Boolean.valueOf(elementEnabled).booleanValue();

                Element page = (Element)pages.get(elementLang);
                if (page == null) {
                    // no page available for the language, add one
                    page = root.addElement(NODE_PAGE).addAttribute(ATTRIBUTE_LANGUAGE, elementLang);
                    pages.put(elementLang, page);
                }

                Element newElement = page.addElement(NODE_ELEMENT).addAttribute(ATTRIBUTE_NAME, elementName);
                if (!enabled) {
                    newElement.addAttribute(ATTRIBUTE_ENABLED, String.valueOf(enabled));
                }
                Element links = elem.element(NODE_LINKS);
                if (links != null) {
                    newElement.add(links.createCopy());
                }
                Element content = elem.element(NODE_CONTENT);
                if (content != null) {
                    newElement.add(content.createCopy());
                }

            } catch (NullPointerException e) {
                OpenCms.getLog(this).error("Error while converting old xmlPage content", e);
            }
        }

        // now replace the old with the new document
        m_document = newDocument;
    }
}