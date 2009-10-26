/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/containerpage/Attic/CmsXmlContainerPage.java,v $
 * Date   : $Date: 2009/10/26 10:45:13 $
 * Version: $Revision: 1.1.2.6 $
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
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsLink;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsUUID;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.CmsXmlGenericWrapper;
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentMacroVisitor;
import org.opencms.xml.page.CmsXmlPage;
import org.opencms.xml.types.CmsXmlNestedContentDefinition;
import org.opencms.xml.types.I_CmsXmlContentValue;
import org.opencms.xml.types.I_CmsXmlSchemaType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.xml.sax.EntityResolver;

/**
 * Implementation of a object used to access and manage the xml data of a container page.<p>
 * 
 * In addition to the XML content interface. It also provides access to more comfortable beans. 
 * 
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.1.2.6 $ 
 * 
 * @since 7.5.2
 * 
 * @see #getCntPage(CmsObject, Locale)
 */
public class CmsXmlContainerPage extends CmsXmlContent {

    /** XML node name constants. */
    public enum XmlNode {

        /** Main node name. */
        CONTAINER("Containers"),
        /** Container elements node name. */
        ELEMENT("Elements"),
        /** Element formatter node name. */
        FORMATTER("Formatter"),
        /** Container or property name node name. */
        NAME("Name"),
        /** Element properties node name. */
        PROPERTIES("Properties"),
        /** Value string node name. */
        STRING("String"),
        /** Container type node name. */
        TYPE("Type"),
        /** Value URI node name. */
        URI("Uri"),
        /** Property value node name. */
        VALUE("Value");

        /** Property name. */
        private String m_name;

        /** Constructor.<p> */
        private XmlNode(String name) {

            m_name = name;
        }

        /** 
         * Returns the name.<p>
         * 
         * @return the name
         */
        public String getName() {

            return m_name;
        }
    }

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsXmlContainerPage.class);

    /** The container page objects. */
    private Map<Locale, CmsContainerPageBean> m_cntPages;

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

        // content definition must be set here since it's used during document creation
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

        // content definition must be set here since it's used during document creation
        m_contentDefinition = contentDefinition;
        // create the XML document according to the content definition
        Document document = m_contentDefinition.createDocument(cms, this, locale);
        // initialize the XML content structure
        initDocument(cms, document, encoding, m_contentDefinition);
    }

    /**
     * Returns the container page bean for the given locale.<p>
     *
     * @param cms the cms context
     * @param locale the locale to use
     *
     * @return the container page bean
     */
    public CmsContainerPageBean getCntPage(CmsObject cms, Locale locale) {

        Locale theLocale = locale;
        if (!m_cntPages.containsKey(theLocale)) {
            LOG.warn(Messages.get().container(
                Messages.LOG_CONTAINER_PAGE_LOCALE_NOT_FOUND_2,
                cms.getSitePath(getFile()),
                theLocale.toString()).key());
            theLocale = OpenCms.getLocaleManager().getDefaultLocales(cms, getFile()).get(0);
            if (!m_cntPages.containsKey(theLocale)) {
                // locale not found!!
                LOG.error(Messages.get().container(
                    Messages.LOG_CONTAINER_PAGE_LOCALE_NOT_FOUND_2,
                    cms.getSitePath(getFile()),
                    theLocale).key());
                return null;
            }
        }
        return m_cntPages.get(theLocale);
    }

    /**
     * Creates a new bookmark for the given element.<p>
     * 
     * @param element the element to create the bookmark for 
     * @param locale the locale
     * @param parent the parent node of the element
     * @param parentPath the parent's path
     * @param parentDef the parent's content definition
     */
    protected void createBookmark(
        Element element,
        Locale locale,
        Element parent,
        String parentPath,
        CmsXmlContentDefinition parentDef) {

        int elemIndex = CmsXmlUtils.getXpathIndexInt(element.getUniquePath(parent));
        String elemPath = CmsXmlUtils.concatXpath(parentPath, CmsXmlUtils.createXpathElement(
            element.getName(),
            elemIndex));
        I_CmsXmlSchemaType elemSchemaType = parentDef.getSchemaType(element.getName());
        I_CmsXmlContentValue elemValue = elemSchemaType.createValue(this, element, locale);
        addBookmark(elemPath, locale, true, elemValue);
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
        m_cntPages = new HashMap<Locale, CmsContainerPageBean>();
        clearBookmarks();

        // initialize the bookmarks
        for (Iterator<Element> itCntPages = CmsXmlGenericWrapper.elementIterator(m_document.getRootElement()); itCntPages.hasNext();) {
            Element cntPage = itCntPages.next();

            try {
                Locale locale = CmsLocaleManager.getLocale(cntPage.attribute(
                    CmsXmlContentDefinition.XSD_ATTRIBUTE_VALUE_LANGUAGE).getValue());

                addLocale(locale);

                List<CmsContainerBean> containers = new ArrayList<CmsContainerBean>();
                for (Iterator<Element> itCnts = CmsXmlGenericWrapper.elementIterator(
                    cntPage,
                    XmlNode.CONTAINER.getName()); itCnts.hasNext();) {
                    Element container = itCnts.next();

                    // container itself
                    int cntIndex = CmsXmlUtils.getXpathIndexInt(container.getUniquePath(cntPage));
                    String cntPath = CmsXmlUtils.createXpathElement(container.getName(), cntIndex);
                    I_CmsXmlSchemaType cntSchemaType = definition.getSchemaType(container.getName());
                    I_CmsXmlContentValue cntValue = cntSchemaType.createValue(this, container, locale);
                    addBookmark(cntPath, locale, true, cntValue);
                    CmsXmlContentDefinition cntDef = ((CmsXmlNestedContentDefinition)cntSchemaType).getNestedContentDefinition();

                    // name
                    Element name = container.element(XmlNode.NAME.getName());
                    createBookmark(name, locale, container, cntPath, cntDef);

                    // type
                    Element type = container.element(XmlNode.TYPE.getName());
                    createBookmark(type, locale, container, cntPath, cntDef);

                    List<CmsContainerElementBean> elements = new ArrayList<CmsContainerElementBean>();
                    // Elements
                    for (Iterator<Element> itElems = CmsXmlGenericWrapper.elementIterator(
                        container,
                        XmlNode.ELEMENT.getName()); itElems.hasNext();) {
                        Element element = itElems.next();

                        // element itself
                        int elemIndex = CmsXmlUtils.getXpathIndexInt(element.getUniquePath(container));
                        String elemPath = CmsXmlUtils.createXpathElement(element.getName(), elemIndex);
                        I_CmsXmlSchemaType elemSchemaType = cntDef.getSchemaType(element.getName());
                        I_CmsXmlContentValue elemValue = elemSchemaType.createValue(this, element, locale);
                        addBookmark(elemPath, locale, true, elemValue);
                        CmsXmlContentDefinition elemDef = ((CmsXmlNestedContentDefinition)elemSchemaType).getNestedContentDefinition();

                        // uri
                        Element uri = element.element(XmlNode.URI.getName());
                        createBookmark(uri, locale, element, elemPath, elemDef);
                        CmsUUID elementId = new CmsLink(uri.element(CmsXmlPage.NODE_LINK)).getStructureId();

                        // formatter
                        Element formatter = element.element(XmlNode.FORMATTER.getName());
                        createBookmark(formatter, locale, element, elemPath, elemDef);
                        CmsUUID formatterId = new CmsLink(formatter.element(CmsXmlPage.NODE_LINK)).getStructureId();

                        Map<String, String> propertiesMap = new HashMap<String, String>();

                        // Properties
                        for (Iterator<Element> itProps = CmsXmlGenericWrapper.elementIterator(
                            element,
                            XmlNode.PROPERTIES.getName()); itProps.hasNext();) {
                            Element property = itProps.next();

                            // property itself
                            int propIndex = CmsXmlUtils.getXpathIndexInt(property.getUniquePath(element));
                            String propPath = CmsXmlUtils.createXpathElement(property.getName(), propIndex);
                            I_CmsXmlSchemaType propSchemaType = elemDef.getSchemaType(property.getName());
                            I_CmsXmlContentValue propValue = propSchemaType.createValue(this, property, locale);
                            addBookmark(propPath, locale, true, propValue);
                            CmsXmlContentDefinition propDef = ((CmsXmlNestedContentDefinition)propSchemaType).getNestedContentDefinition();

                            // name
                            Element propName = property.element(XmlNode.NAME.getName());
                            createBookmark(propName, locale, property, propPath, propDef);

                            // choice value 
                            Element value = property.element(XmlNode.VALUE.getName());
                            int valueIndex = CmsXmlUtils.getXpathIndexInt(value.getUniquePath(property));
                            String valuePath = CmsXmlUtils.createXpathElement(value.getName(), valueIndex);
                            I_CmsXmlSchemaType valueSchemaType = propDef.getSchemaType(value.getName());
                            I_CmsXmlContentValue valueValue = propSchemaType.createValue(this, value, locale);
                            addBookmark(valuePath, locale, true, valueValue);
                            CmsXmlContentDefinition valueDef = ((CmsXmlNestedContentDefinition)valueSchemaType).getNestedContentDefinition();

                            String val = null;
                            Element string = value.element(XmlNode.STRING.getName());
                            if (string != null) {
                                // string value
                                createBookmark(string, locale, value, valuePath, valueDef);
                                val = string.getTextTrim();
                            } else {
                                // uri value
                                Element valueUri = value.element(XmlNode.URI.getName());
                                createBookmark(valueUri, locale, value, valuePath, valueDef);
                                val = new CmsLink(valueUri.element(CmsXmlPage.NODE_LINK)).getStructureId().toString(); // uuid
                            }

                            propertiesMap.put(propName.getTextTrim(), val);
                        }

                        elements.add(new CmsContainerElementBean(elementId, formatterId, propertiesMap));
                    }

                    containers.add(new CmsContainerBean(name.getText(), type.getText(), -1, elements));
                }

                m_cntPages.put(locale, new CmsContainerPageBean(locale, containers));
            } catch (NullPointerException e) {
                LOG.error(org.opencms.xml.content.Messages.get().getBundle().key(
                    org.opencms.xml.content.Messages.LOG_XMLCONTENT_INIT_BOOKMARKS_0), e);
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
                        LOG.warn(org.opencms.xml.content.Messages.get().getBundle().key(
                            org.opencms.xml.content.Messages.LOG_XMLCONTENT_INVALID_ELEM_2,
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

        // just for visibility from the factory
        super.setFile(file);
    }
}