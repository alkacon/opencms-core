/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/containerpage/Attic/CmsXmlContainerPage.java,v $
 * Date   : $Date: 2009/10/13 11:59:42 $
 * Version: $Revision: 1.1.2.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2009 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.xml.containerpage;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResourceFilter;
import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsMacroResolver;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.CmsXmlGenericWrapper;
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentMacroVisitor;
import org.opencms.xml.content.Messages;
import org.opencms.xml.types.CmsXmlNestedContentDefinition;
import org.opencms.xml.types.I_CmsXmlContentValue;
import org.opencms.xml.types.I_CmsXmlSchemaType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.logging.Log;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.xml.sax.EntityResolver;

/**
 * Implementation of a object used to access and manage the xml data of a container page.<p>
 * 
 * This implementation consists of several named elements optionally available for 
 * various languages. The data of each element is accessible via its name and language. 
 * 
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.1.2.1 $ 
 * 
 * @since 7.5.2
 */
public class CmsXmlContainerPage extends CmsXmlContent {

    /** Xml content node constant name. */
    public static final String N_CONTAINER = "Containers";

    /** Xml content node constant element. */
    public static final String N_ELEMENT = "Elements";

    /** Xml content node constant formatter. */
    public static final String N_FORMATTER = "Formatter";

    /** Xml content node constant name. */
    public static final String N_NAME = "Name";

    /** Xml content node constant properties. */
    public static final String N_PROPERTIES = "Properties";

    /** Xml content node constant property. */
    public static final String N_PROPERTY = "Property";

    /** Xml content node constant string. */
    public static final String N_STRING = "String";

    /** Xml content node constant type. */
    public static final String N_TYPE = "Type";

    /** Xml content node constant uri. */
    public static final String N_URI = "Uri";

    /** Xml content node constant value. */
    public static final String N_VALUE = "Value";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsXmlContainerPage.class);

    /**
     * Hides the public constructor.<p>
     */
    protected CmsXmlContainerPage() {

        // noop
    }

    /**
     * Creates a new container page based on the provided XML document.<p>
     * 
     * The given encoding is used when marshalling the XML again later.<p>
     * 
     * @param cms the cms context, if <code>null</code> no link validation is performed 
     * @param document the document to create the container page from
     * @param encoding the encoding of the container page
     * @param resolver the XML entity resolver to use
     */
    protected CmsXmlContainerPage(CmsObject cms, Document document, String encoding, EntityResolver resolver) {

        // must set document first to be able to get the content definition
        m_document = document;
        // for the next line to work the document must already be available
        m_contentDefinition = getContentDefinition(resolver);
        // initialize the XML content structure
        initDocument(cms, m_document, encoding, m_contentDefinition);
    }

    /**
     * Create a new container page based on the given default content,
     * that will have all language nodes of the default content and ensures the presence of the given locale.<p> 
     * 
     * The given encoding is used when marshalling the XML again later.<p>
     * 
     * @param cms the current users OpenCms content
     * @param locale the locale to generate the default content for
     * @param modelUri the absolute path to the container page file acting as model
     * 
     * @throws CmsException in case the model file is not found or not valid
     */
    protected CmsXmlContainerPage(CmsObject cms, Locale locale, String modelUri)
    throws CmsException {

        // init model from given modelUri
        CmsFile modelFile = cms.readFile(modelUri, CmsResourceFilter.ONLY_VISIBLE_NO_DELETED);
        CmsXmlContainerPage model = CmsXmlContainerPageFactory.unmarshal(cms, modelFile);

        // initialize macro resolver to use on model file values
        CmsMacroResolver macroResolver = CmsMacroResolver.newInstance().setCmsObject(cms);

        // content defition must be set here since it's used during document creation
        m_contentDefinition = model.getContentDefinition();
        // get the document from the default content
        Document document = (Document)model.m_document.clone();
        // initialize the XML content structure
        initDocument(cms, document, model.getEncoding(), m_contentDefinition);
        // resolve eventual macros in the nodes
        visitAllValuesWith(new CmsXmlContentMacroVisitor(cms, macroResolver));
        if (!hasLocale(locale)) {
            // required locale not present, add it
            try {
                addLocale(cms, locale);
            } catch (CmsXmlException e) {
                // this can not happen since the locale does not exist
            }
        }
    }

    /**
     * Create a new container page based on the given content definition,
     * that will have one language node for the given locale all initialized with default values.<p> 
     * 
     * The given encoding is used when marshalling the XML again later.<p>
     * 
     * @param cms the current users OpenCms content
     * @param locale the locale to generate the default content for
     * @param encoding the encoding to use when marshalling the container page later
     * @param contentDefinition the content definition to create the content for
     */
    protected CmsXmlContainerPage(
        CmsObject cms,
        Locale locale,
        String encoding,
        CmsXmlContentDefinition contentDefinition) {

        // content defition must be set here since it's used during document creation
        m_contentDefinition = contentDefinition;
        // create the XML document according to the content definition
        Document document = m_contentDefinition.createDocument(cms, this, locale);
        // initialize the XML content structure
        initDocument(cms, document, encoding, m_contentDefinition);
    }

    /**
     * @see org.opencms.xml.A_CmsXmlDocument#initDocument(org.dom4j.Document, java.lang.String, org.opencms.xml.CmsXmlContentDefinition)
     */
    @Override
    protected void initDocument(Document document, String encoding, CmsXmlContentDefinition definition) {

        m_document = document;
        m_contentDefinition = definition;
        m_encoding = CmsEncoder.lookupEncoding(encoding, encoding);
        m_elementLocales = new HashMap<String, Set<Locale>>();
        m_elementNames = new HashMap<Locale, Set<String>>();
        m_locales = new HashSet<Locale>();
        clearBookmarks();

        // initialize the bookmarks
        for (Iterator<Element> i = CmsXmlGenericWrapper.elementIterator(m_document.getRootElement()); i.hasNext();) {
            Element node = i.next();
            try {
                Locale locale = CmsLocaleManager.getLocale(node.attribute(
                    CmsXmlContentDefinition.XSD_ATTRIBUTE_VALUE_LANGUAGE).getValue());

                addLocale(locale);
                processSchemaNode(node, null, locale, definition);
            } catch (NullPointerException e) {
                LOG.error(Messages.get().getBundle().key(Messages.LOG_XMLCONTENT_INIT_BOOKMARKS_0), e);
            }
        }

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
    @Override
    protected void processSchemaNode(Element root, String rootPath, Locale locale, CmsXmlContentDefinition definition) {

        // iterate all XML nodes 
        for (Iterator<Node> i = CmsXmlGenericWrapper.content(root).iterator(); i.hasNext();) {
            Node node = i.next();
            if (!(node instanceof Element)) {
                // this node is not an element, so it must be a white space text node, remove it
                node.detach();
            } else {
                // node must be an element 
                Element element = (Element)node;
                String name = element.getName();
                int xpathIndex = CmsXmlUtils.getXpathIndexInt(element.getUniquePath(root));

                // build the Xpath expression for the current node
                String path;
                if (rootPath != null) {
                    StringBuffer b = new StringBuffer(rootPath.length() + name.length() + 6);
                    b.append(rootPath);
                    b.append('/');
                    b.append(CmsXmlUtils.createXpathElement(name, xpathIndex));
                    path = b.toString();
                } else {
                    path = CmsXmlUtils.createXpathElement(name, xpathIndex);
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
                    if (LOG.isWarnEnabled()) {
                        LOG.warn(Messages.get().getBundle().key(
                            Messages.LOG_XMLCONTENT_INVALID_ELEM_2,
                            name,
                            definition.getSchemaLocation()));
                    }
                }
            }
        }
    }

    /**
     * @see org.opencms.xml.content.CmsXmlContent#setFile(org.opencms.file.CmsFile)
     */
    @Override
    protected void setFile(CmsFile file) {

        // TODO: Auto-generated method stub
        super.setFile(file);
    }
}