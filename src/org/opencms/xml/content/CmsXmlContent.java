/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/content/CmsXmlContent.java,v $
 * Date   : $Date: 2004/11/02 08:30:56 $
 * Version: $Revision: 1.5 $
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
import org.opencms.xml.types.I_CmsXmlContentValue;
import org.opencms.xml.types.I_CmsXmlSchemaType;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Implementation of a XML content object,
 * used to access and manage structured content.<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.5 $
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
                for (Iterator j = node.elementIterator(); j.hasNext();) {
                    Element element = (Element)j.next();
                    String name = element.getName();

                    int pos = 0;
                    List list = getBookmark(name, locale);
                    if (list != null) {
                        pos = list.size();
                    }

                    // create a XML content value element
                    I_CmsXmlSchemaType schemaType = m_contentDefinition.getSchemaType(name);
                    I_CmsXmlContentValue value = schemaType.createValue(element, name, pos);

                    addBookmark(name, locale, true, value);
                }
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
}