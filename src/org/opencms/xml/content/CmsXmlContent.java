/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/content/CmsXmlContent.java,v $
 * Date   : $Date: 2004/11/28 21:57:59 $
 * Version: $Revision: 1.7 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.xml.content;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.staticexport.CmsLinkProcessor;
import org.opencms.staticexport.CmsLinkTable;
import org.opencms.xml.A_CmsXmlDocument;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.I_CmsXmlDocument;
import org.opencms.xml.types.CmsXmlCascadedContentDefinition;
import org.opencms.xml.types.I_CmsXmlContentValue;
import org.opencms.xml.types.I_CmsXmlSchemaType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Implementation of a XML content object,
 * used to access and manage structured content.<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.7 $
 * @since 5.5.0
 */
public class CmsXmlContent extends A_CmsXmlDocument implements I_CmsXmlDocument {

    /** The property to set to enable xerces schema validation. */
    public static final String C_XERCES_SCHEMA_PROPERTY = "http://apache.org/xml/properties/schema/external-noNamespaceSchemaLocation";

    /**
     * Creates a new xml content based on the provided content definition and encoding.<p>
     * 
     * The encoding is used when saving/serializing the XML document.<p>
     * 
     * @param contentDefinition the content definition to create the xml content from
     * @param locale the locale for the xml content to create
     * @param encoding the encoding of the xml content
     */
    public CmsXmlContent(CmsXmlContentDefinition contentDefinition, Locale locale, String encoding) {

        initDocument(contentDefinition.createDocument(locale), encoding, contentDefinition);
    }

    /**
     * Creates a new xml content based on the provided document and encoding.<p>
     * 
     * The encoding is used when saving/serializing the XML document.<p>
     * 
     * @param document the document to create the xml content from
     * @param encoding the encoding of the xml content
     * @param resolver the XML entitiy resolver to use
     */
    public CmsXmlContent(Document document, String encoding, EntityResolver resolver) {

        m_document = document;
        initDocument(m_document, encoding, getContentDefinition(resolver));
    }

    /**
     * Creates a new empty xml content with the provided encoding.<p>
     * 
     * The content is initialized according to the minimal neccessary xml structure.
     * The encoding is used when saving/serializing the XML document.<p>
     * 
     * @param encoding the encoding of the xml content
     * @param resolver the XML entitiy resolver to use
     */
    public CmsXmlContent(String encoding, EntityResolver resolver) {

        m_document = DocumentHelper.createDocument();
        initDocument(m_document, encoding, getContentDefinition(resolver));
    }

    /**
     * @see org.opencms.xml.I_CmsXmlDocument#addLocale(java.util.Locale)
     */
    public void addLocale(Locale locale) throws CmsXmlException {

        if (hasLocale(locale)) {
            throw new CmsXmlException("Locale '" + locale + "' already exists in XML document");
        }

        // create empty document with new Locale
        Document newDocument = m_contentDefinition.createDocument(locale);
        Element newElement = (Element)newDocument.getRootElement().elements().get(0);

        // detach new element from parent folder before adding it 
        m_document.getRootElement().add(newElement.detach());

        // re-initialize the bookmarks
        initDocument(m_document, m_encoding, m_contentDefinition);
    }

    /**
     * Adds a new XML content value for the given element name and locale at the given index position
     * to this XML content document.<p> 
     * 
     * @param name the name of the XML content value element
     * @param locale the locale where to add the new value 
     * @param index the index where to add the value (relatice to all other values of this type)
     * 
     * @return the created XML content value
     */
    public I_CmsXmlContentValue addValue(String name, Locale locale, int index) {

        int todo = 0;
        // TODO: handle cascaded value types, also handle index based names like "Title[0]"
        Element parentElement = getLocaleNode(locale);

        int insertIndex;
        I_CmsXmlSchemaType type;

        List values = getValues(name, locale);

        if (values.size() > 0) {

            // there is at last one value already available of this type 
            type = (I_CmsXmlContentValue)values.get(index == values.size() ? index - 1 : index);

            // iterate all elements of the parent node            
            Iterator i = parentElement.content().iterator();
            int pos = 0;
            int foundCount = 0;
            while (i.hasNext()) {
                pos++;
                Node node = (Node)i.next();
                if (node instanceof Element) {
                    if (node.getName().equals(name)) {
                        // found an element of this type
                        foundCount++;
                        if (foundCount >= index) {
                            // found the index position required
                            if (index == 0) {
                                // insert before the last position found as first element of this type
                                pos--;
                            }
                            break;
                        }
                    }
                }
            }
            insertIndex = pos;

        } else {

            // no value of this type is currently available            
            type = m_contentDefinition.getSchemaType(name);

            // check where in the type sequence the type should appear
            int typeIndex = m_contentDefinition.getTypeSequence().indexOf(type);
            if (typeIndex == 0) {
                // this is the first type, so we just add at the very first position
                insertIndex = 0;
            } else {

                // create a list of all element names that should occur before the selected type
                List previousTypeNames = new ArrayList();
                for (int i = 0; i < typeIndex; i++) {
                    I_CmsXmlSchemaType t = (I_CmsXmlSchemaType)m_contentDefinition.getTypeSequence().get(i);
                    previousTypeNames.add(t.getElementName());
                }

                // iterate all elements of the parent node
                Iterator i = parentElement.content().iterator();
                int pos = 0;
                while (i.hasNext()) {
                    Node node = (Node)i.next();
                    if (node instanceof Element) {
                        if (!previousTypeNames.contains(node.getName())) {
                            // the element name is NOT in the list of names that occure before the selected type, 
                            // so it must be an element that occurs AFTER the type
                            break;
                        }
                    }
                    pos++;
                }
                insertIndex = pos;
            }
        }

        // append the new element at the calculated position
        I_CmsXmlContentValue newValue = addValue(parentElement, type, insertIndex, index);

        // re-initialize this XML content 
        initDocument(m_document, m_encoding, m_contentDefinition);

        return newValue;
    }

    /**
     * @see org.opencms.xml.I_CmsXmlDocument#getContentDefinition(org.xml.sax.EntityResolver)
     */
    public CmsXmlContentDefinition getContentDefinition(EntityResolver resolver) {

        String schema = m_document.getRootElement().attributeValue(
            I_CmsXmlSchemaType.XSI_NAMESPACE_ATTRIBUTE_NO_SCHEMA_LOCATION);

        // Note regarding exception handling:
        // Since this object already is a valid XML content object,
        // it must have a valid schema, otherwise it would not exist.
        // Therefore the exceptions should never be really thrown.
        if (schema == null) {
            throw new RuntimeException("No XML schema set for content definition");
        }
        InputSource source;
        try {
            source = resolver.resolveEntity(null, schema);
            return CmsXmlContentDefinition.unmarshal(source, schema, resolver);
        } catch (SAXException e) {
            throw new RuntimeException("Could not parse XML content definition schema", e);
        } catch (IOException e) {
            throw new RuntimeException("IO error resolving XML content definition schema", e);
        } catch (CmsXmlException e) {
            throw new RuntimeException("Unable to unmarshal XML content definition schema", e);
        }
    }

    /**
     * @see org.opencms.xml.A_CmsXmlDocument#getLinkProcessor(org.opencms.file.CmsObject, org.opencms.staticexport.CmsLinkTable)
     */
    public CmsLinkProcessor getLinkProcessor(CmsObject cms, CmsLinkTable linkTable) {

        // initialize link processor
        return new CmsLinkProcessor(cms, linkTable, getEncoding(), null);
    }
    
    /**
     * Returns the value sequence for the selected element name in this XML content.<p>
     * 
     * @param name the element name (XML node name) to the the value sequence for
     * @param locale the locale to get the value sequence for
     * 
     * @return the value sequence for the selected element name in this XML content
     */
    public CmsXmlContentValueSequence getValueSequence(String name, Locale locale) {

        I_CmsXmlSchemaType type = m_contentDefinition.getSchemaType(name);
        return new CmsXmlContentValueSequence(type, locale, this);
    }

    /**
     * Resolves the information in the optional "appinfo" schema node according to the rules of the XML content handler that 
     * has been configured for the XML content definition of this XML content.<p>
     * 
     * @param cms an initialized CmsObject
     * @throws CmsException if something goes wrong
     */
    public void resolveAppInfo(CmsObject cms) throws CmsException {

        // call the appinfo resolver of the configured XML content handler
        m_contentDefinition.getContentHandler().resolveAppInfo(cms, this, m_contentDefinition);
    }

    /**
     * Returns the XML root element node for the given locale.<p>
     * 
     * @param locale the locale to get the root element for
     * 
     * @return the XML root element node for the given locale
     */
    protected Element getLocaleNode(Locale locale) {

        if (!m_locales.contains(locale)) {
            throw new RuntimeException("No initialized locale " + locale + " available in XML document");
        }

        String localeStr = locale.toString();
        Iterator i = m_document.getRootElement().elements().iterator();
        while (i.hasNext()) {
            Element element = (Element)i.next();
            if (localeStr.equals(element.attributeValue(CmsXmlContentDefinition.XSD_ATTRIBUTE_VALUE_LANGUAGE))) {
                // language element found, return it
                return element;
            }
        }

        // language element was not found (should not happen since we throw a RuntimeException in this case
        return null;
    }

    /**
     * @see org.opencms.xml.A_CmsXmlDocument#initDocument(org.dom4j.Document, java.lang.String, org.opencms.xml.CmsXmlContentDefinition)
     */
    protected void initDocument(Document document, String encoding, CmsXmlContentDefinition definition) {

        m_document = document;
        m_contentDefinition = definition;
        m_encoding = CmsEncoder.lookupEncoding(encoding, encoding);
        m_elementLocales = new HashMap();
        m_elementNames = new HashMap();

        // initialize the bookmarks
        for (Iterator i = m_document.getRootElement().elementIterator(); i.hasNext();) {
            Element node = (Element)i.next();
            try {
                Locale locale = CmsLocaleManager.getLocale(node.attribute(
                    CmsXmlContentDefinition.XSD_ATTRIBUTE_VALUE_LANGUAGE).getValue());

                processSchemaNode(node, null, locale, definition);
            } catch (NullPointerException e) {
                OpenCms.getLog(this).error("Error while initalizing XML content bookmarks", e);
            }
        }

    }

    /**
     * Sets the file this XML content is written to.<p> 
     * 
     * @param file the file this XML content content is written to
     */
    protected void setFile(CmsFile file) {

        m_file = file;
    }

    /**
     * Adds a new XML schema type with the default value to the given parent node.<p>
     * 
     * @param parent the XML parent element to add the new value to
     * @param type the type of the value to add
     * @param insertIndex the index in the XML document where to add the XML node
     * @param index the index of the schema type relative to all other values of this type
     * 
     * @return the created XML content value
     */
    private I_CmsXmlContentValue addValue(Element parent, I_CmsXmlSchemaType type, int insertIndex, int index) {

        Element element = (Element)parent.addElement(type.getElementName()).detach();
        List parentContent = parent.content();
        parentContent.add(insertIndex, element);

        I_CmsXmlContentValue value = type.createValue(element, type.getElementName(), index);
        if (type.getDefault() != null) {
            try {
                value.setStringValue(type.getDefault());
            } catch (CmsXmlException e) {
                // should not happen if default value is correct
                OpenCms.getLog(this).error("Invalid default value '" + type.getDefault() + "' for XML content", e);
                element.clearContent();
            }
        }
        return value;
    }

    /**
     * Processes a document node and extracts the values of the node according to the provided XML
     * content definition.<p> 
     * 
     * @param root the root node element to process
     * @param rootPath the Xpath of the root node in the document
     * @param locale the locale 
     * @param definition the XML content definition to use for processing the values
     */
    private void processSchemaNode(Element root, String rootPath, Locale locale, CmsXmlContentDefinition definition) {

        int count = 0;
        String previousName = null;

        for (Iterator j = root.elementIterator(); j.hasNext();) {
            Element element = (Element)j.next();

            // check if this is a new node, if so reset the node counter
            String name = element.getName();
            if ((previousName == null) || !previousName.equals(name)) {
                previousName = name;
                count = 0;
            }

            // build the Xpath expression for the current node
            String path;
            if (rootPath != null) {
                StringBuffer b = new StringBuffer(rootPath.length() + name.length() + 6);
                b.append(rootPath);
                b.append('/');
                b.append(createXpathElement(name, count));
                path = b.toString();
            } else {
                path = createXpathElement(name, count);
            }

            // create a XML content value element
            I_CmsXmlSchemaType schemaType = definition.getSchemaType(name);

            if (schemaType.isSimpleType()) {
                // directly add simple type to schema
                I_CmsXmlContentValue value = schemaType.createValue(element, name, count);
                addBookmark(path, locale, true, value);
            } else {
                // recurse for cascaded schema
                CmsXmlCascadedContentDefinition cascadedSchema = (CmsXmlCascadedContentDefinition)schemaType;
                processSchemaNode(element, path, locale, cascadedSchema.getContentDefinition());
            }
            // increase the node counter
            count++;
        }
    }
}