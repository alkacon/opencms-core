/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/containerpage/CmsXmlContainerPage.java,v $
 * Date   : $Date: 2010/01/20 14:27:21 $
 * Version: $Revision: 1.8 $
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
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsLink;
import org.opencms.relations.CmsRelationType;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.CmsXmlGenericWrapper;
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentMacroVisitor;
import org.opencms.xml.content.CmsXmlContentProperty;
import org.opencms.xml.page.CmsXmlPage;
import org.opencms.xml.types.CmsXmlNestedContentDefinition;
import org.opencms.xml.types.CmsXmlVfsFileValue;
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
import org.xml.sax.EntityResolver;

/**
 * Implementation of a object used to access and manage the xml data of a container page.<p>
 * 
 * In addition to the XML content interface. It also provides access to more comfortable beans. 
 * 
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.8 $ 
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
        /** Value file list node name. */
        FILELIST("FileList"),
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
        /** File list URI node name. */
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
     * @see org.opencms.xml.content.CmsXmlContent#isAutoCorrectionEnabled()
     */
    @Override
    public boolean isAutoCorrectionEnabled() {

        return true;
    }

    /**
     * Saves given container page in the current locale, and not only in memory but also to VFS.<p>
     * 
     * @param cms the current cms context
     * @param cntPage the container page to save
     * 
     * @throws CmsException if something goes wrong
     */
    public void save(CmsObject cms, CmsContainerPageBean cntPage) throws CmsException {

        CmsFile file = getFile();

        // lock the file
        cms.lockResourceTemporary(cms.getSitePath(file));

        // wipe the locale
        Locale locale = cms.getRequestContext().getLocale();
        if (hasLocale(locale)) {
            removeLocale(locale);
        }
        addLocale(cms, locale);

        // get the properties
        Map<String, CmsXmlContentProperty> propertiesConf = OpenCms.getADEManager().getElementPropertyConfiguration(
            cms,
            getFile());

        // add the nodes to the raw XML structure
        Element parent = getLocaleNode(locale);
        saveCntPage(cms, parent, cntPage, propertiesConf);

        // generate bookmarks
        initDocument(m_document, m_encoding, m_contentDefinition);

        // write to VFS
        file.setContents(marshal());
        cms.writeFile(file);
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
     * Fills a {@link CmsXmlVfsFileValue} with the resource identified by the given id.<p>
     * 
     * @param cms the current CMS context
     * @param element the XML element to fill
     * @param resourceId the ID identifying the resource to use
     * 
     * @throws CmsException if the resource can not be read
     */
    protected void fillResource(CmsObject cms, Element element, CmsUUID resourceId) throws CmsException {

        String xpath = element.getPath();
        int pos = xpath.lastIndexOf("/" + XmlNode.CONTAINER.getName() + "/");
        if (pos > 0) {
            xpath = xpath.substring(pos + 1);
        }
        CmsRelationType type = getContentDefinition().getContentHandler().getRelationType(xpath);
        CmsResource res = cms.readResource(resourceId);
        CmsXmlVfsFileValue.fillResource(element, res, type);
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
                        String elemPath = CmsXmlUtils.concatXpath(cntPath, CmsXmlUtils.createXpathElement(
                            element.getName(),
                            elemIndex));
                        I_CmsXmlSchemaType elemSchemaType = cntDef.getSchemaType(element.getName());
                        I_CmsXmlContentValue elemValue = elemSchemaType.createValue(this, element, locale);
                        addBookmark(elemPath, locale, true, elemValue);
                        CmsXmlContentDefinition elemDef = ((CmsXmlNestedContentDefinition)elemSchemaType).getNestedContentDefinition();

                        // uri
                        Element uri = element.element(XmlNode.URI.getName());
                        createBookmark(uri, locale, element, elemPath, elemDef);
                        Element uriLink = uri.element(CmsXmlPage.NODE_LINK);
                        CmsUUID elementId = null;
                        if (uriLink == null) {
                            // this can happen when adding the elements node to the xml content
                            // it is not dangerous since the link has to be set before saving 
                        } else {
                            elementId = new CmsLink(uriLink).getStructureId();
                        }

                        // formatter
                        Element formatter = element.element(XmlNode.FORMATTER.getName());
                        createBookmark(formatter, locale, element, elemPath, elemDef);
                        Element formatterLink = formatter.element(CmsXmlPage.NODE_LINK);
                        CmsUUID formatterId = null;
                        if (formatterLink == null) {
                            // this can happen when adding the elements node to the xml content
                            // it is not dangerous since the link has to be set before saving 
                        } else {
                            formatterId = new CmsLink(formatterLink).getStructureId();
                        }

                        Map<String, String> propertiesMap = new HashMap<String, String>();

                        // Properties
                        for (Iterator<Element> itProps = CmsXmlGenericWrapper.elementIterator(
                            element,
                            XmlNode.PROPERTIES.getName()); itProps.hasNext();) {
                            Element property = itProps.next();

                            // property itself
                            int propIndex = CmsXmlUtils.getXpathIndexInt(property.getUniquePath(element));
                            String propPath = CmsXmlUtils.concatXpath(elemPath, CmsXmlUtils.createXpathElement(
                                property.getName(),
                                propIndex));
                            I_CmsXmlSchemaType propSchemaType = elemDef.getSchemaType(property.getName());
                            I_CmsXmlContentValue propValue = propSchemaType.createValue(this, property, locale);
                            addBookmark(propPath, locale, true, propValue);
                            CmsXmlContentDefinition propDef = ((CmsXmlNestedContentDefinition)propSchemaType).getNestedContentDefinition();

                            // name
                            Element propName = property.element(XmlNode.NAME.getName());
                            createBookmark(propName, locale, property, propPath, propDef);

                            // choice value 
                            Element value = property.element(XmlNode.VALUE.getName());
                            if (value == null) {
                                // this can happen when adding the elements node to the xml content
                                continue;
                            }
                            int valueIndex = CmsXmlUtils.getXpathIndexInt(value.getUniquePath(property));
                            String valuePath = CmsXmlUtils.concatXpath(propPath, CmsXmlUtils.createXpathElement(
                                value.getName(),
                                valueIndex));
                            I_CmsXmlSchemaType valueSchemaType = propDef.getSchemaType(value.getName());
                            I_CmsXmlContentValue valueValue = valueSchemaType.createValue(this, value, locale);
                            addBookmark(valuePath, locale, true, valueValue);
                            CmsXmlContentDefinition valueDef = ((CmsXmlNestedContentDefinition)valueSchemaType).getNestedContentDefinition();

                            String val = null;
                            Element string = value.element(XmlNode.STRING.getName());
                            if (string != null) {
                                // string value
                                createBookmark(string, locale, value, valuePath, valueDef);
                                val = string.getTextTrim();
                            } else {
                                // file list value
                                Element valueFileList = value.element(XmlNode.FILELIST.getName());
                                if (valueFileList == null) {
                                    // this can happen when adding the elements node to the xml content
                                    continue;
                                }
                                int valueFileListIndex = CmsXmlUtils.getXpathIndexInt(valueFileList.getUniquePath(value));
                                String valueFileListPath = CmsXmlUtils.concatXpath(
                                    valuePath,
                                    CmsXmlUtils.createXpathElement(valueFileList.getName(), valueFileListIndex));
                                I_CmsXmlSchemaType valueFileListSchemaType = valueDef.getSchemaType(valueFileList.getName());
                                I_CmsXmlContentValue valueFileListValue = valueFileListSchemaType.createValue(
                                    this,
                                    valueFileList,
                                    locale);
                                addBookmark(valueFileListPath, locale, true, valueFileListValue);
                                CmsXmlContentDefinition valueFileListDef = ((CmsXmlNestedContentDefinition)valueFileListSchemaType).getNestedContentDefinition();

                                List<CmsUUID> idList = new ArrayList<CmsUUID>();
                                // files
                                for (Iterator<Element> itFiles = CmsXmlGenericWrapper.elementIterator(
                                    valueFileList,
                                    XmlNode.URI.getName()); itFiles.hasNext();) {

                                    Element valueUri = itFiles.next();
                                    createBookmark(valueUri, locale, valueFileList, valueFileListPath, valueFileListDef);
                                    Element valueUriLink = valueUri.element(CmsXmlPage.NODE_LINK);
                                    CmsUUID fileId = null;
                                    if (valueUriLink == null) {
                                        // this can happen when adding the elements node to the xml content
                                        // it is not dangerous since the link has to be set before saving 
                                    } else {
                                        fileId = new CmsLink(valueUriLink).getStructureId();
                                    }
                                    idList.add(fileId);
                                }
                                // comma separated list of UUIDs
                                val = CmsStringUtil.listAsString(idList, CmsXmlContentProperty.PROP_SEPARATOR);
                            }

                            propertiesMap.put(propName.getTextTrim(), val);
                        }
                        if (elementId != null) {
                            elements.add(new CmsContainerElementBean(elementId, formatterId, propertiesMap));
                        }
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
     * Adds the given container page to the given element.<p>
     * 
     * @param cms the current CMS object
     * @param parent the element to add it
     * @param cntPage the container page to add
     * @param propertiesConf the properties configuration
     * 
     * @throws CmsException if something goes wrong
     */
    protected void saveCntPage(
        CmsObject cms,
        Element parent,
        CmsContainerPageBean cntPage,
        Map<String, CmsXmlContentProperty> propertiesConf) throws CmsException {

        parent.clearContent();
        CmsADEManager manager = OpenCms.getADEManager();
        for (String containerName : cntPage.getNames()) {
            CmsContainerBean container = cntPage.getContainers().get(containerName);

            // the container
            Element cntElement = parent.addElement(XmlNode.CONTAINER.getName());
            cntElement.addElement(XmlNode.NAME.getName()).addCDATA(container.getName());
            cntElement.addElement(XmlNode.TYPE.getName()).addCDATA(container.getType());

            // the elements
            for (CmsContainerElementBean element : container.getElements()) {
                Element elemElement = cntElement.addElement(XmlNode.ELEMENT.getName());

                // the element
                Element uriElem = elemElement.addElement(XmlNode.URI.getName());
                fillResource(cms, uriElem, element.getElementId());
                Element formatterElem = elemElement.addElement(XmlNode.FORMATTER.getName());
                fillResource(cms, formatterElem, element.getFormatterId());

                // the properties
                Element propElement = null;

                Map<String, CmsProperty> properties = manager.getElementProperties(cms, element);
                for (Map.Entry<String, CmsProperty> property : properties.entrySet()) {
                    String propName = property.getKey();
                    String propValue = property.getValue().getStructureValue();
                    if (!propertiesConf.containsKey(propName) || (propValue == null)) {
                        continue;
                    }
                    // only if the property is configured in the schema we will save it
                    if (propElement == null) {
                        propElement = elemElement.addElement(XmlNode.PROPERTIES.getName());
                    }

                    // the property name
                    propElement.addElement(XmlNode.NAME.getName()).addCDATA(propName);
                    Element valueElement = propElement.addElement(XmlNode.VALUE.getName());

                    // the property value
                    if (propertiesConf.get(propName).getPropertyType().equals(CmsXmlContentProperty.T_VFSLIST)) {
                        // resource list value
                        Element filelistElem = valueElement.addElement(XmlNode.FILELIST.getName());
                        for (String strId : CmsStringUtil.splitAsList(propValue, CmsXmlContentProperty.PROP_SEPARATOR)) {
                            try {
                                Element fileValueElem = filelistElem.addElement(XmlNode.URI.getName());
                                fillResource(cms, fileValueElem, new CmsUUID(strId));
                            } catch (CmsException e) {
                                // should never happen
                                LOG.error(e.getLocalizedMessage(), e);
                            }
                        }
                    } else {
                        // string value
                        valueElement.addElement(XmlNode.STRING.getName()).addCDATA(propValue);
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