/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/content/CmsXmlContent.java,v $
 * Date   : $Date: 2005/05/07 16:08:28 $
 * Version: $Revision: 1.23 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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
import org.opencms.main.OpenCms;
import org.opencms.staticexport.CmsLinkProcessor;
import org.opencms.staticexport.CmsLinkTable;
import org.opencms.xml.A_CmsXmlDocument;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.CmsXmlEntityResolver;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.I_CmsXmlDocument;
import org.opencms.xml.types.CmsXmlNestedContentDefinition;
import org.opencms.xml.types.I_CmsXmlContentValue;
import org.opencms.xml.types.I_CmsXmlSchemaType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Implementation of a XML content object,
 * used to access and manage structured content.<p>
 *
 * Use the {@link org.opencms.xml.content.CmsXmlContentFactory} to generate an
 * instance of this class.<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.23 $
 * @since 5.5.0
 */
public class CmsXmlContent extends A_CmsXmlDocument implements I_CmsXmlDocument {

    /** The property to set to enable xerces schema validation. */
    public static final String C_XERCES_SCHEMA_PROPERTY = "http://apache.org/xml/properties/schema/external-noNamespaceSchemaLocation";

    /**
     * Hides the public constructor.<p>
     */
    protected CmsXmlContent() {

        // noop
    }

    /**
     * Create a new XML content based on the given content definiton,
     * that will have one language node for the given locale all initialized with default values.<p> 
     * 
     * The given encoding is used when marshalling the XML again later.<p>
     * 
     * @param cms the current users OpenCms content
     * @param locale the locale to generate the default content for
     * @param encoding the encoding to use when marshalling the XML content later
     * @param contentDefinition the content definiton to create the content for
     */
    protected CmsXmlContent(CmsObject cms, Locale locale, String encoding, CmsXmlContentDefinition contentDefinition) {

        // content defition must be set here since it's used during document creation
        m_contentDefinition = contentDefinition;
        // create the XML document according to the content definition
        Document document = contentDefinition.createDocument(cms, this, locale);
        // initialize the XML content structure
        initDocument(document, encoding, m_contentDefinition);
    }

    /**
     * Creates a new XML content based on the provided XML document.<p>
     * 
     * The given encoding is used when marshalling the XML again later.<p>
     * 
     * @param document the document to create the xml content from
     * @param encoding the encoding of the xml content
     * @param resolver the XML entitiy resolver to use
     */
    protected CmsXmlContent(Document document, String encoding, EntityResolver resolver) {

        // must set document first to be able to get the content definition
        m_document = document;
        // for the next line to work the document must already be available
        m_contentDefinition = getContentDefinition(resolver);
        // initialize the XML content structure
        initDocument(m_document, encoding, m_contentDefinition);
    }

    /**
     * @see org.opencms.xml.I_CmsXmlDocument#addLocale(org.opencms.file.CmsObject, java.util.Locale)
     */
    public void addLocale(CmsObject cms, Locale locale) throws CmsXmlException {

        if (hasLocale(locale)) {
            throw new CmsXmlException("Locale '" + locale + "' already exists in XML document");
        }
        // add element node for Locale
        m_contentDefinition.createLocale(cms, this, m_document.getRootElement(), locale);
        // re-initialize the bookmarks
        initDocument(m_document, m_encoding, m_contentDefinition);
    }

    /**
     * Adds a new XML content value for the given element name and locale at the given index position
     * to this XML content document.<p> 
     * 
     * @param cms the current users OpenCms context
     * @param path the path to the XML content value element
     * @param locale the locale where to add the new value 
     * @param index the index where to add the value (relative to all other values of this type)
     * 
     * @return the created XML content value
     */
    public I_CmsXmlContentValue addValue(CmsObject cms, String path, Locale locale, int index) {

        // get the schema type of the requested path           
        I_CmsXmlSchemaType type = m_contentDefinition.getSchemaType(path);
        if (type == null) {
            throw new IllegalArgumentException("Unknown XML content element path according to schema: " + path);
        }
        
        Element parentElement;
        String elementName;
        CmsXmlContentDefinition contentDefinition;
        if (CmsXmlUtils.isDeepXpath(path)) {
            // this is a nested content definition, so the parent element must be in the bookmarks
            String parentPath = CmsXmlUtils.removeLastXpathElement(path);
            Object o = getBookmark(parentPath, locale);
            if (o == null) {
                throw new IllegalArgumentException("Unknown XML content element path: " + path);
            }
            CmsXmlNestedContentDefinition parentValue = (CmsXmlNestedContentDefinition)o;
            parentElement = parentValue.getElement();
            elementName = CmsXmlUtils.getLastXpathElement(path);
            contentDefinition = parentValue.getNestedContentDefinition();
        } else {
            // the parent element is the locale element
            parentElement = getLocaleNode(locale);
            elementName = CmsXmlUtils.removeXpathIndex(path);
            contentDefinition = m_contentDefinition;
        }

        // read the XML siblings from the parent node
        List siblings = parentElement.elements(elementName);

        int insertIndex;
        if (siblings.size() > 0) {

            if (siblings.size() >= type.getMaxOccurs()) {
                // must not allow adding an element if max occurs would be violated
                throw new RuntimeException("Element '"
                    + elementName
                    + "' can occur at maximum "
                    + type.getMaxOccurs()
                    + " times");
            }

            if (index > siblings.size()) {
                // index position behind last element of the list
                throw new RuntimeException("You can't insert at position "
                    + index
                    + " because element '"
                    + elementName
                    + "' only occurs "
                    + siblings.size()
                    + " times");
            }

            // check for offset required to append beyond last position
            int offset = (index == siblings.size()) ? 1 : 0;
            // get the element from the parent at the selected position
            Element sibling = (Element)siblings.get(index - offset);
            // check position of the node in the parent node content
            insertIndex = sibling.getParent().content().indexOf(sibling) + offset;
        } else {

            if (index > 0) {
                // since the element does not occur, index must be 0
                throw new RuntimeException("You must insert at 0 not at position "
                    + index
                    + " because element '"
                    + elementName
                    + "' does not yet occur in the parent node");
            }

            // check where in the type sequence the type should appear
            int typeIndex = contentDefinition.getTypeSequence().indexOf(type);
            if (typeIndex == 0) {
                // this is the first type, so we just add at the very first position
                insertIndex = 0;
            } else {

                // create a list of all element names that should occur before the selected type
                List previousTypeNames = new ArrayList();
                for (int i = 0; i < typeIndex; i++) {
                    I_CmsXmlSchemaType t = (I_CmsXmlSchemaType)contentDefinition.getTypeSequence().get(i);
                    previousTypeNames.add(t.getName());
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
        I_CmsXmlContentValue newValue = addValue(cms, parentElement, type, locale, insertIndex);

        // re-initialize this XML content 
        initDocument(m_document, m_encoding, m_contentDefinition);

        // return the value instance that was stored in the bookmarks 
        // just returning "newValue" isn't enough since this instance is NOT stored in the bookmarks
        return (I_CmsXmlContentValue)getBookmark(getBookmarkName(newValue.getPath(), locale));
    }

    /**
     * @see org.opencms.xml.I_CmsXmlDocument#getContentDefinition()
     */
    public CmsXmlContentDefinition getContentDefinition() {

        return m_contentDefinition;
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
     * If the given element name is not valid according to the schema of this XML content,
     * <code>null</code> is returned.<p>
     * 
     * @param name the element name (XML node name) to the the value sequence for
     * @param locale the locale to get the value sequence for
     * 
     * @return the value sequence for the selected element name in this XML content
     */
    public CmsXmlContentValueSequence getValueSequence(String name, Locale locale) {

        I_CmsXmlSchemaType type = m_contentDefinition.getSchemaType(name);
        if (type == null) {
            return null;
        }
        return new CmsXmlContentValueSequence(name, type, locale, this);
    }

    /**
     * Removes an existing XML content value of the given element name and locale at the given index position
     * from this XML content document.<p> 
     * 
     * @param name the name of the XML content value element
     * @param locale the locale where to remove the value 
     * @param index the index where to remove the value (relative to all other values of this type)
     */
    public void removeValue(String name, Locale locale, int index) {

        // first get the value from the selected locale and index
        I_CmsXmlContentValue value = getValue(name, locale, index);

        // chech for the min / max occurs constrains
        List values = getValues(name, locale);
        if (values.size() <= value.getMinOccurs()) {
            // must not allow removing an element if min occurs would be violated
            throw new RuntimeException("Element '" + name + "' must occur at last " + value.getMinOccurs() + " times");
        }

        // detach the value node from the XML document
        value.getElement().detach();

        // re-initialize this XML content 
        initDocument(m_document, m_encoding, m_contentDefinition);

    }

    /**
     * Resolves the mappings for all values of this XML content.<p>
     * 
     * @param cms the current users OpenCms context
     */
    public void resolveMappings(CmsObject cms) {
        
        // iterate through all initialized value nodes in this XML content
        CmsXmlContentMappingVisitor visitor = new CmsXmlContentMappingVisitor(cms, this);
        visitAllValuesWith(visitor);
    }

    /**
     * @see org.opencms.xml.I_CmsXmlDocument#validate(org.opencms.file.CmsObject)
     */
    public CmsXmlContentErrorHandler validate(CmsObject cms) {

        // iterate through all initialized value nodes in this XML content
        CmsXmlContentValidationVisitor visitor = new CmsXmlContentValidationVisitor(cms);
        visitAllValuesWith(visitor);

        return visitor.getErrorHandler();
    }
    
    /**
     * Visists all values of this XML content with the given value visitor.<p>
     * 
     * Please note that the order in which the values are visited may NOT be the
     * order they apper in the XML document. It is ensured that the the parent 
     * of a nested value is visited before the element it contains.<p>
     * 
     * @param visitor the value visitor implementation to visit the values with
     */
    public void visitAllValuesWith(I_CmsXmlContentValueVisitor visitor) {

        List bookmarks = new ArrayList(getBookmarks());
        Collections.sort(bookmarks);

        for (int i = 0; i < bookmarks.size(); i++) {

            String key = (String)bookmarks.get(i);
            I_CmsXmlContentValue value = (I_CmsXmlContentValue)getBookmark(key);
            visitor.visit(value);
        }
    }
    
    /**
     * @see org.opencms.xml.A_CmsXmlDocument#getBookmark(java.lang.String)
     */
    protected Object getBookmark(String bookmark) {

        // allows package classes to directly access the bookmark information of the XML content 
        return super.getBookmark(bookmark);
    }

    /**
     * @see org.opencms.xml.A_CmsXmlDocument#getBookmarks()
     */
    protected Set getBookmarks() {
        
        // allows package classes to directly access the bookmark information of the XML content 
        return super.getBookmarks();
    }    

    /**
     * Returns the XML root element node for the given locale.<p>
     * 
     * @param locale the locale to get the root element for
     * 
     * @return the XML root element node for the given locale
     */
    protected Element getLocaleNode(Locale locale) {

        String localeStr = locale.toString();
        Iterator i = m_document.getRootElement().elements().iterator();
        while (i.hasNext()) {
            Element element = (Element)i.next();
            if (localeStr.equals(element.attributeValue(CmsXmlContentDefinition.XSD_ATTRIBUTE_VALUE_LANGUAGE))) {
                // language element found, return it
                return element;
            }
        }

        // language element was not found
        throw new RuntimeException("No initialized locale " + locale + " available in XML document");
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
        m_locales = new HashSet();
        clearBookmarks();

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
     * @param locale the locale to add the new value for
     * @param insertIndex the index in the XML document where to add the XML node
     * 
     * @return the created XML content value
     */
    private I_CmsXmlContentValue addValue(
        CmsObject cms,
        Element parent,
        I_CmsXmlSchemaType type,
        Locale locale,
        int insertIndex) {

        // first generate the XML element for the new value
        Element element = type.generateXml(cms, this, parent, locale);
        // detatch the XML element from the appended position in order to insert it at the required position
        element.detach();
        // add the XML element at the required position in the parent XML node 
        parent.content().add(insertIndex, element);
        // create the type and return it
        return type.createValue(this, element, locale);
    }

    /**
     * Returns the content definition object for this xml content object.<p>
     * 
     * @param resolver the XML entity resolver to use, required for VFS access
     * 
     * @return the content definition object for this xml content object
     */
    private CmsXmlContentDefinition getContentDefinition(EntityResolver resolver) {

        String schema = m_document.getRootElement().attributeValue(
            I_CmsXmlSchemaType.XSI_NAMESPACE_ATTRIBUTE_NO_SCHEMA_LOCATION);

        // Note regarding exception handling:
        // Since this object already is a valid XML content object,
        // it must have a valid schema, otherwise it would not exist.
        // Therefore the exceptions should never be really thrown.
        if (schema == null) {
            throw new RuntimeException("No XML schema set for content definition");
        }

        CmsXmlContentDefinition result = null;
        CmsXmlEntityResolver cmsResolver = null;
        if (resolver instanceof CmsXmlEntityResolver) {
            // check for a cached version of this content definition
            cmsResolver = (CmsXmlEntityResolver)resolver;
            result = cmsResolver.getCachedContentDefinition(schema);
        }

        if (result == null) {
            // result was not already cached
            InputSource source;
            try {
                source = resolver.resolveEntity(null, schema);
                result = CmsXmlContentDefinition.unmarshal(source, schema, resolver);
                if (cmsResolver != null) {
                    // cache the result content definition
                    cmsResolver.cacheContentDefinition(schema, result);
                }
            } catch (SAXException e) {
                throw new RuntimeException("Could not parse XML content definition schema", e);
            } catch (IOException e) {
                throw new RuntimeException("IO error resolving XML content definition schema", e);
            } catch (CmsXmlException e) {
                throw new RuntimeException("Unable to unmarshal XML content definition schema", e);
            }
        }

        return result;
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

        int count = 1;
        String previousName = null;

        // first remove all non-element node (i.e. white space text nodes)
        List content = root.content();
        for (int i = content.size() - 1; i >= 0; i--) {
            Node node = (Node)content.get(i);
            if (!(node instanceof Element)) {
                // this node is not an element, so it must be a white space text node, remove it
                content.remove(i);
            }
        }

        // iterate all elements again
        for (Iterator i = root.content().iterator(); i.hasNext();) {

            // node must be an element since all non-elements where removed
            Element element = (Element)i.next();

            // check if this is a new node, if so reset the node counter
            String name = element.getName();
            if ((previousName == null) || !previousName.equals(name)) {
                previousName = name;
                count = 1;
            }

            // build the Xpath expression for the current node
            String path;
            if (rootPath != null) {
                StringBuffer b = new StringBuffer(rootPath.length() + name.length() + 6);
                b.append(rootPath);
                b.append('/');
                b.append(CmsXmlUtils.createXpathElement(name, count));
                path = b.toString();
            } else {
                path = CmsXmlUtils.createXpathElement(name, count);
            }

            // create a XML content value element
            I_CmsXmlSchemaType schemaType = definition.getSchemaType(name);

            if (schemaType != null) {
                // directly add simple type to schema
                I_CmsXmlContentValue value = schemaType.createValue(this, element, locale);
                addBookmark(path, locale, true, value);

                if (!schemaType.isSimpleType()) {
                    // recurse for nested schema
                    CmsXmlNestedContentDefinition nestedSchema = (CmsXmlNestedContentDefinition)schemaType;
                    processSchemaNode(element, path, locale, nestedSchema.getNestedContentDefinition());
                }
            } else {
                // unknown XML node name according to schema
                if (OpenCms.getLog(this).isErrorEnabled()) {
                    OpenCms.getLog(this).error("XML node name '" + name + "' invalid according to schema " + definition.getSchemaLocation());
                }
            }

            // increase the node counter
            count++;
        }
    }
}