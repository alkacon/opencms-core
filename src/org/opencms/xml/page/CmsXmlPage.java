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

package org.opencms.xml.page;

import org.opencms.configuration.CmsConfigurationManager;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsRuntimeException;
import org.opencms.staticexport.CmsLinkProcessor;
import org.opencms.staticexport.CmsLinkTable;
import org.opencms.xml.A_CmsXmlDocument;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.CmsXmlEntityResolver;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.CmsXmlGenericWrapper;
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.content.CmsXmlContentErrorHandler;
import org.opencms.xml.content.I_CmsXmlContentHandler;
import org.opencms.xml.types.CmsXmlHtmlValue;
import org.opencms.xml.types.I_CmsXmlContentValue;
import org.opencms.xml.types.I_CmsXmlSchemaType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.xml.sax.InputSource;

/**
 * Implementation of a page object used to access and manage xml data.<p>
 *
 * This implementation consists of several named elements optionally available for
 * various languages. The data of each element is accessible via its name and language.
 *
 * The content of each element is stored as CDATA, links within the
 * content are processed and are separately accessible as entries of a CmsLinkTable.
 *
 * @since 6.0.0
 */
public class CmsXmlPage extends A_CmsXmlDocument {

    /** Name of the name attribute of the elements node. */
    public static final String ATTRIBUTE_ENABLED = "enabled";

    /** Name of the language attribute of the elements node. */
    public static final String ATTRIBUTE_LANGUAGE = "language";

    /** Name of the name attribute of the elements node. */
    public static final String ATTRIBUTE_NAME = "name";

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

    /** Property to check if relative links are allowed. */
    public static final String PROPERTY_ALLOW_RELATIVE = "allowRelativeLinks";

    /** The DTD address of the OpenCms xmlpage. */
    public static final String XMLPAGE_XSD_SYSTEM_ID = CmsConfigurationManager.DEFAULT_DTD_PREFIX + "xmlpage.xsd";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsXmlPage.class);

    /** The XML page content definition is static. */
    private static CmsXmlContentDefinition m_xmlPageContentDefinition;

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

        initDocument(document, encoding, getContentDefinition());
    }

    /**
     * Creates an empty XML page in the provided locale using
     * the provided encoding.<p>
     *
     * The page is initialized according to the minimal necessary xml structure.
     * The encoding is used for marshalling the XML document later.<p>
     *
     * @param locale the initial locale of the XML page
     * @param encoding the encoding of the XML page
     */
    public CmsXmlPage(Locale locale, String encoding) {

        initDocument(CmsXmlPageFactory.createDocument(locale), encoding, getContentDefinition());
    }

    /**
     * @see org.opencms.xml.I_CmsXmlDocument#addLocale(org.opencms.file.CmsObject, java.util.Locale)
     */
    public void addLocale(CmsObject cms, Locale locale) throws CmsXmlException {

        if (hasLocale(locale)) {
            throw new CmsXmlException(Messages.get().container(Messages.ERR_XML_PAGE_LOCALE_EXISTS_1, locale));
        }
        // add element node for Locale
        getContentDefinition().createLocale(cms, this, m_document.getRootElement(), locale);
        // re-initialize the bookmarks
        initDocument(m_document, m_encoding, getContentDefinition());
    }

    /**
     * Adds a new, empty value with the given name and locale
     * to this XML document.<p>
     *
     * @param name the name of the value
     * @param locale the locale of the value
     *
     * @throws CmsIllegalArgumentException if the name contains an index ("[&lt;number&gt;]") or the value for the
     *         given locale already exists in the xmlpage.
     *
     */
    public void addValue(String name, Locale locale) throws CmsIllegalArgumentException {

        if (name.indexOf('[') >= 0) {
            throw new CmsIllegalArgumentException(
                Messages.get().container(Messages.ERR_XML_PAGE_CONTAINS_INDEX_1, name));
        }

        if (hasValue(name, locale)) {
            throw new CmsIllegalArgumentException(
                Messages.get().container(Messages.ERR_XML_PAGE_LANG_ELEM_EXISTS_2, name, locale));
        }

        Element pages = m_document.getRootElement();
        String localeStr = locale.toString();
        Element page = null;

        // search if a page for the selected language is already available
        for (Iterator<Element> i = CmsXmlGenericWrapper.elementIterator(pages, NODE_PAGE); i.hasNext();) {
            Element nextPage = i.next();
            String language = nextPage.attributeValue(ATTRIBUTE_LANGUAGE);
            if (localeStr.equals(language)) {
                // a page for the selected language was found
                page = nextPage;
                break;
            }
        }

        // create the new element
        Element element;
        if (page != null) {
            // page for selected language already available
            element = page.addElement(NODE_ELEMENT).addAttribute(ATTRIBUTE_NAME, name);
        } else {
            // no page for the selected language was found
            element = pages.addElement(NODE_PAGE).addAttribute(ATTRIBUTE_LANGUAGE, localeStr);
            element = element.addElement(NODE_ELEMENT).addAttribute(ATTRIBUTE_NAME, name);
        }

        // add empty nodes for link table and content to the element
        element.addElement(NODE_LINKS);
        element.addElement(NODE_CONTENT);

        CmsXmlHtmlValue value = new CmsXmlHtmlValue(this, element, locale);

        // bookmark the element
        addBookmark(CmsXmlUtils.createXpathElement(name, 1), locale, true, value);
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
     * @see org.opencms.xml.I_CmsXmlDocument#getContentDefinition()
     */
    public CmsXmlContentDefinition getContentDefinition() throws CmsRuntimeException {

        if (m_xmlPageContentDefinition == null) {
            // since XML page schema is cached anyway we don't need an CmsObject instance
            CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(null);
            InputSource source;
            try {
                source = resolver.resolveEntity(null, XMLPAGE_XSD_SYSTEM_ID);
                // store content definition in static variable
                m_xmlPageContentDefinition = CmsXmlContentDefinition.unmarshal(source, XMLPAGE_XSD_SYSTEM_ID, resolver);
            } catch (CmsXmlException e) {
                throw new CmsRuntimeException(
                    Messages.get().container(Messages.ERR_XML_PAGE_UNMARSHAL_CONTENDDEF_0),
                    e);
            }
        }
        return m_xmlPageContentDefinition;
    }

    /**
     * @see org.opencms.xml.I_CmsXmlDocument#getHandler()
     */
    public I_CmsXmlContentHandler getHandler() {

        return getContentDefinition().getContentHandler();
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
     * @see org.opencms.xml.A_CmsXmlDocument#getNames(java.util.Locale)
     */
    @Override
    public List<String> getNames(Locale locale) {

        Set<String> sn = m_elementNames.get(locale);
        if (sn != null) {
            List<String> result = new ArrayList<String>();
            Iterator<String> i = sn.iterator();
            while (i.hasNext()) {
                String path = i.next();
                result.add(CmsXmlUtils.removeXpathIndex(path));
            }
            return result;
        }
        return Collections.emptyList();
    }

    /**
     * Checks if the element of a page object is enabled.<p>
     *
     * @param name the name of the element
     * @param locale the locale of the element
     * @return true if the element exists and is not disabled
     */
    @Override
    public boolean isEnabled(String name, Locale locale) {

        CmsXmlHtmlValue value = (CmsXmlHtmlValue)getValue(name, locale);

        if (value != null) {
            Element element = value.getElement();
            Attribute enabled = element.attribute(ATTRIBUTE_ENABLED);
            return ((enabled == null) || Boolean.valueOf(enabled.getValue()).booleanValue());
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

        I_CmsXmlContentValue value = removeBookmark(CmsXmlUtils.createXpath(name, 1), locale);
        if (value != null) {
            Element element = value.getElement();
            element.detach();
        }
    }

    /**
     * Renames the page-element value from the old to the new one.<p>
     *
     * @param oldValue the old value
     * @param newValue the new value
     * @param locale the locale
     *
     * @throws CmsIllegalArgumentException if the name contains an index ("[&lt;number&gt;]"), the new value for the
     *         given locale already exists in the xmlpage or the the old value does not exist for the locale in the xmlpage.
     *
     */
    public void renameValue(String oldValue, String newValue, Locale locale) throws CmsIllegalArgumentException {

        CmsXmlHtmlValue oldXmlHtmlValue = (CmsXmlHtmlValue)getValue(oldValue, locale);
        if (oldXmlHtmlValue == null) {
            throw new CmsIllegalArgumentException(
                Messages.get().container(Messages.ERR_XML_PAGE_NO_ELEM_FOR_LANG_2, oldValue, locale));
        }

        if (hasValue(newValue, locale)) {
            throw new CmsIllegalArgumentException(
                Messages.get().container(Messages.ERR_XML_PAGE_LANG_ELEM_EXISTS_2, newValue, locale));
        }
        if (newValue.indexOf('[') >= 0) {
            throw new CmsIllegalArgumentException(
                Messages.get().container(Messages.ERR_XML_PAGE_CONTAINS_INDEX_1, newValue));
        }

        // get the element
        Element element = oldXmlHtmlValue.getElement();

        // update value of the element attribute 'NAME'
        element.addAttribute(ATTRIBUTE_NAME, newValue);

        // re-initialize the document to update the bookmarks
        initDocument(m_document, m_encoding, getContentDefinition());
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
            value.setStringValue(cms, content);
        } else {
            throw new CmsXmlException(
                Messages.get().container(Messages.ERR_XML_PAGE_INVALID_ELEM_SELECT_2, locale, name));
        }
    }

    /**
     * @see org.opencms.xml.I_CmsXmlDocument#validate(org.opencms.file.CmsObject)
     */
    public CmsXmlContentErrorHandler validate(CmsObject cms) {

        // XML pages currently do not support validation
        return new CmsXmlContentErrorHandler();
    }

    /**
     * @see org.opencms.xml.A_CmsXmlDocument#initDocument(org.dom4j.Document, java.lang.String, org.opencms.xml.CmsXmlContentDefinition)
     */
    @Override
    protected void initDocument(Document document, String encoding, CmsXmlContentDefinition definition) {

        m_encoding = CmsEncoder.lookupEncoding(encoding, encoding);
        m_document = document;
        m_elementLocales = new HashMap<String, Set<Locale>>();
        m_elementNames = new HashMap<Locale, Set<String>>();
        m_locales = new HashSet<Locale>();

        // convert pre 5.3.6 XML page documents
        if (!NODE_PAGES.equals(m_document.getRootElement().getName())) {
            convertOldDocument();
        }

        // initialize the bookmarks
        clearBookmarks();
        Element pages = m_document.getRootElement();
        try {
            for (Iterator<Element> i = CmsXmlGenericWrapper.elementIterator(pages, NODE_PAGE); i.hasNext();) {

                Element page = i.next();
                Locale locale = CmsLocaleManager.getLocale(page.attributeValue(ATTRIBUTE_LANGUAGE));
                for (Iterator<Element> j = CmsXmlGenericWrapper.elementIterator(page, NODE_ELEMENT); j.hasNext();) {

                    Element element = j.next();
                    String name = element.attributeValue(ATTRIBUTE_NAME);

                    String elementEnabled = element.attributeValue(ATTRIBUTE_ENABLED);
                    boolean enabled = (elementEnabled == null) ? true : Boolean.valueOf(elementEnabled).booleanValue();

                    // create an element type from the XML node
                    CmsXmlHtmlValue value = new CmsXmlHtmlValue(this, element, locale);
                    value.setContentDefinition(definition);

                    // add the element type bookmark
                    addBookmark(CmsXmlUtils.createXpathElement(name, 1), locale, enabled, value);
                }
                addLocale(locale);
            }
        } catch (NullPointerException e) {
            LOG.error(Messages.get().getBundle().key(Messages.ERR_XML_PAGE_INIT_BOOKMARKS_0), e);
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
        root.addAttribute(I_CmsXmlSchemaType.XSI_NAMESPACE_ATTRIBUTE_NO_SCHEMA_LOCATION, XMLPAGE_XSD_SYSTEM_ID);

        Map<String, Element> pages = new HashMap<String, Element>();

        if ((m_document.getRootElement() != null) && (m_document.getRootElement().element(NODE_ELEMENTS) != null)) {
            for (Iterator<Element> i = CmsXmlGenericWrapper.elementIterator(
                m_document.getRootElement().element(NODE_ELEMENTS),
                NODE_ELEMENT); i.hasNext();) {

                Element elem = i.next();
                try {
                    String elementName = elem.attributeValue(ATTRIBUTE_NAME);
                    String elementLang = elem.attributeValue(ATTRIBUTE_LANGUAGE);
                    String elementEnabled = elem.attributeValue(ATTRIBUTE_ENABLED);
                    boolean enabled = (elementEnabled == null) ? true : Boolean.valueOf(elementEnabled).booleanValue();

                    Element page = pages.get(elementLang);
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
                    LOG.error(Messages.get().getBundle().key(Messages.ERR_XML_PAGE_CONVERT_CONTENT_0), e);
                }
            }
        }

        // now replace the old with the new document
        m_document = newDocument;
    }
}