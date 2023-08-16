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

package org.opencms.xml.containerpage;

import org.opencms.ade.configuration.CmsADEConfigData;
import org.opencms.ade.configuration.CmsFormatterUtils;
import org.opencms.ade.containerpage.CmsContainerpageService;
import org.opencms.ade.containerpage.CmsModelGroupHelper;
import org.opencms.ade.containerpage.CmsSettingTranslator;
import org.opencms.ade.containerpage.shared.CmsContainerElement;
import org.opencms.ade.containerpage.shared.CmsFormatterConfig;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
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
import org.opencms.util.CmsUUID;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.CmsXmlGenericWrapper;
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentMacroVisitor;
import org.opencms.xml.content.CmsXmlContentProperty;
import org.opencms.xml.content.CmsXmlContentPropertyHelper;
import org.opencms.xml.page.CmsXmlPage;
import org.opencms.xml.types.CmsXmlNestedContentDefinition;
import org.opencms.xml.types.CmsXmlVfsFileValue;
import org.opencms.xml.types.I_CmsXmlContentValue;
import org.opencms.xml.types.I_CmsXmlSchemaType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.apache.commons.logging.Log;

import org.dom4j.Document;
import org.dom4j.Element;
import org.xml.sax.EntityResolver;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;

/**
 * Implementation of a object used to access and manage the xml data of a container page.<p>
 *
 * In addition to the XML content interface. It also provides access to more comfortable beans.
 *
 * @since 7.5.2
 *
 * @see #getContainerPage(CmsObject)
 */
public class CmsXmlContainerPage extends CmsXmlContent {

    /** XML node name constants. */
    public enum XmlNode {

        /** Container attribute node name. */
        Attribute,
        /** Main node name. */
        Containers,
        /** The create new element node name. */
        CreateNew,

        /** Element instance id node name. */
        ElementInstanceId,
        /** Container elements node name. */
        Elements,
        /** Element formatter node name. */
        Formatter,

        /** Formatter key node name.*/
        FormatterKey,
        /** The is root container node name. */
        IsRootContainer,
        /** Container attribute key node name. */
        Key,
        /** Container name node name. */
        Name,
        /** Parent element instance id node name. */
        ParentInstanceId,
        /** Container type node name. */
        Type,
        /** Element URI node name. */
        Uri,
        /** Container attribute value node name. */
        Value;
    }

    /** Name for old internal setting names that are not used with the SYSTEM:: prefix in code. */
    public static final Set<String> LEGACY_SYSTEM_SETTING_NAMES = Collections.unmodifiableSet(
        new HashSet<>(
            Arrays.asList(
                CmsContainerElement.USE_AS_COPY_MODEL,
                CmsContainerElement.MODEL_GROUP_ID,
                CmsContainerElement.MODEL_GROUP_STATE,
                CmsContainerElement.USE_AS_COPY_MODEL,
                CmsContainerpageService.SOURCE_CONTAINERPAGE_ID_SETTING)));

    /** Prefix for system element settings. */
    public static final String SYSTEM_SETTING_PREFIX = "SYSTEM::";

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
                LOG.error(e.getMessage(), e);
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
        Document document = m_contentDefinition.createDocument(cms, this, CmsLocaleManager.MASTER_LOCALE);
        // initialize the XML content structure
        initDocument(cms, document, encoding, m_contentDefinition);
    }

    /**
     * Saves a container page bean to the in-memory XML structure and returns the changed content.<p>
     *
     * @param cms the current CMS context
     * @param cntPage the container page bean
     * @return the new content for the container page
     * @throws CmsException if something goes wrong
     */
    public byte[] createContainerPageXml(CmsObject cms, CmsContainerPageBean cntPage) throws CmsException {

        // make sure all links are validated
        writeContainerPage(cms, cntPage);
        checkLinkConcistency(cms);
        return marshal();

    }

    /**
     * Gets the container page content as a bean.<p>
     *
     * @param cms the current CMS context
     * @return the bean containing the container page data
     */
    public CmsContainerPageBean getContainerPage(CmsObject cms) {

        Locale masterLocale = CmsLocaleManager.MASTER_LOCALE;
        Locale localeToLoad = null;
        // always use master locale if possible, otherwise use the first locale.
        // this is important for 'legacy' container pages which were created before container pages became locale independent
        if (m_cntPages.containsKey(masterLocale)) {
            localeToLoad = masterLocale;
        } else if (!m_cntPages.isEmpty()) {
            localeToLoad = m_cntPages.keySet().iterator().next();
        }
        if (localeToLoad == null) {
            return null;
        } else {
            CmsContainerPageBean result = m_cntPages.get(localeToLoad);
            return result;
        }
    }

    /**
     * Calls initDocument, but with a different CmsObject
     *
     * @param cms the CmsObject to use
     */
    public void initDocument(CmsObject cms) {

        initDocument(cms, m_document, m_encoding, getContentDefinition());
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

        save(cms, cntPage, false);
    }

    /**
     * Saves given container page in the current locale, and not only in memory but also to VFS.<p>
     *
     * @param cms the current cms context
     * @param cntPage the container page to save
     * @param ifChangedOnly <code>true</code> to only write the file if the content has changed
     *
     * @throws CmsException if something goes wrong
     */
    public void save(CmsObject cms, CmsContainerPageBean cntPage, boolean ifChangedOnly) throws CmsException {

        CmsFile file = getFile();
        byte[] data = createContainerPageXml(cms, cntPage);
        if (ifChangedOnly && Arrays.equals(file.getContents(), data)) {
            return;
        }
        // lock the file
        cms.lockResourceTemporary(file);
        file.setContents(data);
        cms.writeFile(file);
    }

    /**
     * Saves a container page in in-memory XML structure.<p>
     *
     * @param cms the current CMS context
     * @param cntPage the container page bean to save
     *
     * @throws CmsException if something goes wrong
     */
    public void writeContainerPage(CmsObject cms, CmsContainerPageBean cntPage) throws CmsException {

        // keep unused containers
        CmsContainerPageBean savePage = cleanupContainersContainers(cms, cntPage);
        savePage = removeEmptyContainers(cntPage);
        // Replace existing locales with master locale
        for (Locale locale : getLocales()) {
            removeLocale(locale);
        }
        Locale masterLocale = CmsLocaleManager.MASTER_LOCALE;
        addLocale(cms, masterLocale);

        // add the nodes to the raw XML structure
        Element parent = getLocaleNode(masterLocale);
        saveContainerPage(cms, parent, savePage);
        initDocument(m_document, m_encoding, m_contentDefinition);
    }

    /**
     * Checks the link consistency for a given locale and reinitializes the document afterwards.<p>
     *
     * @param cms the cms context
     */
    protected void checkLinkConcistency(CmsObject cms) {

        Locale masterLocale = CmsLocaleManager.MASTER_LOCALE;

        for (I_CmsXmlContentValue contentValue : getValues(masterLocale)) {
            if (contentValue instanceof CmsXmlVfsFileValue) {
                CmsLink link = ((CmsXmlVfsFileValue)contentValue).getLink(cms);
                link.checkConsistency(cms);
            }
        }
        initDocument();
    }

    /**
     * Removes all empty containers and merges the containers of the current document that are not used in the given container page with it.<p>
     *
     * @param cms the current CMS context
     * @param cntPage the container page to merge
     *
     * @return a new container page with the additional unused containers
     */
    protected CmsContainerPageBean cleanupContainersContainers(CmsObject cms, CmsContainerPageBean cntPage) {

        CmsADEConfigData config = OpenCms.getADEManager().lookupConfiguration(cms, getFile().getRootPath());
        // get the used containers first
        Map<String, CmsContainerBean> currentContainers = cntPage.getContainers();
        List<CmsContainerBean> containers = new ArrayList<CmsContainerBean>();
        for (String cntName : cntPage.getNames()) {
            CmsContainerBean container = currentContainers.get(cntName);
            if (!container.getElements().isEmpty()) {
                containers.add(container);
            }
        }

        // now get the unused containers
        CmsContainerPageBean currentContainerPage = getContainerPage(cms);
        if (currentContainerPage != null) {
            for (String cntName : currentContainerPage.getNames()) {
                if (!currentContainers.containsKey(cntName)) {
                    CmsContainerBean container = currentContainerPage.getContainers().get(cntName);
                    if (!container.getElements().isEmpty()) {
                        containers.add(container);
                    }
                }
            }
        }

        // check if any nested containers have lost their parent element

        // first collect all present elements
        Map<String, CmsContainerElementBean> pageElements = new HashMap<String, CmsContainerElementBean>();
        Map<String, String> parentContainers = new HashMap<String, String>();
        for (CmsContainerBean container : containers) {
            for (CmsContainerElementBean element : container.getElements()) {
                try {
                    element.initResource(cms);

                    if (!CmsModelGroupHelper.isModelGroupResource(element.getResource())) {
                        pageElements.put(element.getInstanceId(), element);
                        parentContainers.put(element.getInstanceId(), container.getName());
                    }
                } catch (CmsException e) {
                    LOG.warn(e.getLocalizedMessage(), e);
                }
            }
        }
        Iterator<CmsContainerBean> cntIt = containers.iterator();
        while (cntIt.hasNext()) {
            CmsContainerBean container = cntIt.next();
            // check all unused nested containers if their parent element is still part of the page
            if (!currentContainers.containsKey(container.getName())
                && (container.isNestedContainer() && !container.isRootContainer())) {
                boolean remove = !pageElements.containsKey(container.getParentInstanceId())
                    || container.getElements().isEmpty();
                if (!remove) {
                    // check if the parent element formatter is set to strictly render all nested containers
                    CmsContainerElementBean element = pageElements.get(container.getParentInstanceId());
                    String settingsKey = CmsFormatterConfig.getSettingsKeyForContainer(
                        parentContainers.get(element.getInstanceId()));
                    String formatterId = element.getIndividualSettings().get(settingsKey);
                    I_CmsFormatterBean bean = config.findFormatter(formatterId);
                    if (bean != null) {
                        remove = (bean instanceof CmsFormatterBean) && ((CmsFormatterBean)bean).isStrictContainers();
                    }
                }
                if (remove) {
                    // remove the sub elements from the page list
                    for (CmsContainerElementBean element : container.getElements()) {
                        pageElements.remove(element.getInstanceId());
                    }
                    // remove the container
                    cntIt.remove();
                }
            }
        }

        return new CmsContainerPageBean(containers);
    }

    /**
     * Fills a {@link CmsXmlVfsFileValue} with the resource identified by the given id.<p>
     *
     * @param cms the current CMS context
     * @param element the XML element to fill
     * @param resourceId the ID identifying the resource to use
     *
     * @return the resource
     *
     * @throws CmsException if the resource can not be read
     */
    protected CmsResource fillResource(CmsObject cms, Element element, CmsUUID resourceId) throws CmsException {

        String xpath = element.getPath();
        int pos = xpath.lastIndexOf("/" + XmlNode.Containers.name() + "/");
        if (pos > 0) {
            xpath = xpath.substring(pos + 1);
        }
        CmsRelationType type = getHandler().getRelationType(xpath);
        CmsResource res = cms.readResource(resourceId, CmsResourceFilter.IGNORE_EXPIRATION);
        CmsXmlVfsFileValue.fillEntry(element, res.getStructureId(), res.getRootPath(), type);
        return res;
    }

    /**
     * @see org.opencms.xml.content.CmsXmlContent#initDocument(org.opencms.file.CmsObject, org.dom4j.Document, java.lang.String, org.opencms.xml.CmsXmlContentDefinition)
     */
    @Override
    protected void initDocument(CmsObject cms, Document document, String encoding, CmsXmlContentDefinition definition) {

        m_document = document;
        m_contentDefinition = definition;
        m_encoding = CmsEncoder.lookupEncoding(encoding, encoding);
        m_elementLocales = new HashMap<String, Set<Locale>>();
        m_elementNames = new HashMap<Locale, Set<String>>();
        m_locales = new HashSet<Locale>();
        m_cntPages = new LinkedHashMap<Locale, CmsContainerPageBean>();
        clearBookmarks();
        CmsADEConfigData config = null;
        CmsSettingTranslator settingTranslator = null;
        if ((getFile() != null) && (cms != null)) {
            config = OpenCms.getADEManager().lookupConfiguration(cms, getFile().getRootPath());
            settingTranslator = new CmsSettingTranslator(config);
        }

        // initialize the bookmarks
        for (Iterator<Element> itCntPages = CmsXmlGenericWrapper.elementIterator(
            m_document.getRootElement()); itCntPages.hasNext();) {
            Element cntPage = itCntPages.next();

            try {
                Locale locale = CmsLocaleManager.getLocale(
                    cntPage.attribute(CmsXmlContentDefinition.XSD_ATTRIBUTE_VALUE_LANGUAGE).getValue());

                addLocale(locale);

                List<CmsContainerBean> containers = new ArrayList<CmsContainerBean>();
                for (Iterator<Element> itCnts = CmsXmlGenericWrapper.elementIterator(
                    cntPage,
                    XmlNode.Containers.name()); itCnts.hasNext();) {
                    Element container = itCnts.next();

                    // container itself
                    int cntIndex = CmsXmlUtils.getXpathIndexInt(container.getUniquePath(cntPage));
                    String cntPath = CmsXmlUtils.createXpathElement(container.getName(), cntIndex);
                    I_CmsXmlSchemaType cntSchemaType = definition.getSchemaType(container.getName());
                    I_CmsXmlContentValue cntValue = cntSchemaType.createValue(this, container, locale);
                    addBookmark(cntPath, locale, true, cntValue);
                    CmsXmlContentDefinition cntDef = ((CmsXmlNestedContentDefinition)cntSchemaType).getNestedContentDefinition();

                    // name
                    Element name = container.element(XmlNode.Name.name());
                    String containerName = name.getText();
                    addBookmarkForElement(name, locale, container, cntPath, cntDef);

                    // type
                    Element type = container.element(XmlNode.Type.name());
                    addBookmarkForElement(type, locale, container, cntPath, cntDef);

                    // parent instance id
                    Element parentInstance = container.element(XmlNode.ParentInstanceId.name());
                    if (parentInstance != null) {
                        addBookmarkForElement(parentInstance, locale, container, cntPath, cntDef);
                    }

                    Element isRootContainer = container.element(XmlNode.IsRootContainer.name());
                    if (isRootContainer != null) {
                        addBookmarkForElement(isRootContainer, locale, container, cntPath, cntDef);
                    }

                    List<CmsContainerElementBean> elements = new ArrayList<CmsContainerElementBean>();
                    // Elements
                    for (Iterator<Element> itElems = CmsXmlGenericWrapper.elementIterator(
                        container,
                        XmlNode.Elements.name()); itElems.hasNext();) {
                        Element element = itElems.next();

                        // element itself
                        int elemIndex = CmsXmlUtils.getXpathIndexInt(element.getUniquePath(container));
                        String elemPath = CmsXmlUtils.concatXpath(
                            cntPath,
                            CmsXmlUtils.createXpathElement(element.getName(), elemIndex));
                        I_CmsXmlSchemaType elemSchemaType = cntDef.getSchemaType(element.getName());
                        I_CmsXmlContentValue elemValue = elemSchemaType.createValue(this, element, locale);
                        addBookmark(elemPath, locale, true, elemValue);
                        CmsXmlContentDefinition elemDef = ((CmsXmlNestedContentDefinition)elemSchemaType).getNestedContentDefinition();

                        Element instanceIdElem = element.element(XmlNode.ElementInstanceId.name());
                        String elementInstanceId = null;
                        if (instanceIdElem != null) {
                            elementInstanceId = instanceIdElem.getTextTrim();
                        }

                        Element formatterKeyElem = element.element(XmlNode.FormatterKey.name());
                        String formatterKey = null;
                        if (formatterKeyElem != null) {
                            formatterKey = formatterKeyElem.getTextTrim();
                        }

                        // uri
                        Element uri = element.element(XmlNode.Uri.name());
                        CmsUUID elementId = null;
                        if (uri != null) {
                            addBookmarkForElement(uri, locale, element, elemPath, elemDef);
                            Element uriLink = uri.element(CmsXmlPage.NODE_LINK);
                            if (uriLink == null) {
                                // this can happen when adding the elements node to the xml content
                                // it is not dangerous since the link has to be set before saving
                            } else {
                                CmsLink link = new CmsLink(uriLink);
                                if (cms != null) {
                                    link.checkConsistency(cms);
                                }
                                elementId = link.getStructureId();
                            }
                        }
                        // uri may be null for dynamic functions, try find the element id from the settings later

                        Element createNewElement = element.element(XmlNode.CreateNew.name());
                        boolean createNew = (createNewElement != null)
                            && Boolean.parseBoolean(createNewElement.getStringValue());

                        // formatter
                        Element formatter = element.element(XmlNode.Formatter.name());
                        CmsUUID formatterId = null;
                        if (formatter != null) {
                            addBookmarkForElement(formatter, locale, element, elemPath, elemDef);
                            Element formatterLink = formatter.element(CmsXmlPage.NODE_LINK);

                            if (formatterLink == null) {
                                // this can happen when adding the elements node to the xml content
                                // it is not dangerous since the link has to be set before saving
                            } else {
                                CmsLink link = new CmsLink(formatterLink);
                                if (cms != null) {
                                    link.checkConsistency(cms);
                                }
                                formatterId = link.getStructureId();
                            }
                        }

                        // the properties
                        Map<String, String> propertiesMap = CmsXmlContentPropertyHelper.readProperties(
                            this,
                            locale,
                            element,
                            elemPath,
                            elemDef);
                        propertiesMap = translateMapKeys(propertiesMap, this::translateSettingNameForLoad);
                        if ((config != null) && (getFile() != null)) {
                            propertiesMap = fixNestedFormatterSettings(cms, config, propertiesMap);
                        }
                        if (formatterKey != null) {
                            propertiesMap.put(CmsFormatterConfig.FORMATTER_SETTINGS_KEY + containerName, formatterKey);
                        }

                        I_CmsFormatterBean dynamicFormatter = null;
                        if (config != null) {
                            // make sure alias keys are replaced with main keys in the settings
                            String key1 = CmsFormatterConfig.FORMATTER_SETTINGS_KEY + containerName;
                            String key2 = CmsFormatterConfig.FORMATTER_SETTINGS_KEY;
                            for (String key : new String[] {key1, key2}) {
                                String value = propertiesMap.get(key);
                                if (value != null) {
                                    I_CmsFormatterBean temp = config.findFormatter(value);
                                    if (temp != null) {
                                        dynamicFormatter = temp;
                                        propertiesMap.put(key, dynamicFormatter.getKeyOrId());
                                        break;
                                    }
                                }
                            }
                        }
                        if ((config != null) && (dynamicFormatter != null) && (settingTranslator != null)) {
                            propertiesMap = settingTranslator.translateSettings(dynamicFormatter, propertiesMap);
                        }

                        if (elementInstanceId != null) {
                            propertiesMap.put(CmsContainerElement.ELEMENT_INSTANCE_ID, elementInstanceId);
                        }

                        CmsUUID pageId;
                        if (getFile() != null) {
                            pageId = getFile().getStructureId();
                        } else {
                            pageId = CmsUUID.getNullUUID();
                        }
                        propertiesMap.put(CmsContainerElement.SETTING_PAGE_ID, "" + pageId);

                        boolean createNewFromSetting = Boolean.parseBoolean(
                            propertiesMap.remove(CmsContainerElement.SETTING_CREATE_NEW));
                        createNew |= createNewFromSetting;

                        if (config != null) {
                            // in the new container page format, new dynamic functions are not stored with their URIs in the page
                            String key = CmsFormatterUtils.getFormatterKey(containerName, propertiesMap);
                            I_CmsFormatterBean maybeFunction = config.findFormatter(key);
                            if (maybeFunction instanceof CmsFunctionFormatterBean) {
                                elementId = new CmsUUID(maybeFunction.getId());
                            }
                        }

                        if (elementId != null) {
                            elements.add(new CmsContainerElementBean(elementId, formatterId, propertiesMap, createNew));
                        }
                    }
                    CmsContainerBean newContainerBean = new CmsContainerBean(
                        name.getText(),
                        type.getText(),
                        parentInstance != null ? parentInstance.getText() : null,
                        (isRootContainer != null) && Boolean.valueOf(isRootContainer.getText()).booleanValue(),
                        elements);
                    containers.add(newContainerBean);
                }

                m_cntPages.put(locale, new CmsContainerPageBean(containers));
            } catch (NullPointerException e) {
                LOG.error(
                    org.opencms.xml.content.Messages.get().getBundle().key(
                        org.opencms.xml.content.Messages.LOG_XMLCONTENT_INIT_BOOKMARKS_0),
                    e);
            }
        }

        if (cms != null) {
            // this will remove all invalid links
            getHandler().invalidateBrokenLinks(cms, this);
        }
    }

    /**
     * @see org.opencms.xml.A_CmsXmlDocument#initDocument(org.dom4j.Document, java.lang.String, org.opencms.xml.CmsXmlContentDefinition)
     */
    @Override
    protected void initDocument(Document document, String encoding, CmsXmlContentDefinition definition) {

        initDocument(null, document, encoding, definition);
    }

    /**
     * Removes all empty containers to clean up container page XML.<p>
     *
     * @param cntPage the container page bean
     *
     * @return the newly generated result
     */
    protected CmsContainerPageBean removeEmptyContainers(CmsContainerPageBean cntPage) {

        List<CmsContainerBean> containers = new ArrayList<CmsContainerBean>();
        for (CmsContainerBean container : cntPage.getContainers().values()) {
            if (container.getElements().size() > 0) {
                containers.add(container);
            }
        }
        return new CmsContainerPageBean(containers);
    }

    /**
     * Adds the given container page to the given element.<p>
     *
     * @param cms the current CMS object
     * @param parent the element to add it
     * @param cntPage the container page to add
     *
     * @throws CmsException if something goes wrong
     */
    protected void saveContainerPage(CmsObject cms, Element parent, CmsContainerPageBean cntPage) throws CmsException {

        parent.clearContent();

        CmsADEConfigData adeConfig = OpenCms.getADEManager().lookupConfiguration(cms, getFile().getRootPath());
        if (adeConfig.isUseFormatterKeys()) {
            saveContainerPageV2(cms, parent, cntPage, adeConfig);
        } else {
            saveContainerPageV1(cms, parent, cntPage, adeConfig);
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

    /**
     * Replaces formatter id prefixes for nested settings with corresponding formatter keys, if possible.<p>
     *
     * Also handles replacement of alias keys with main keys in nested settings.
     *
     * @param cms the CMS Context
     * @param config the sitemap configuration
      *@param propertiesMap the map of setting s
     * @return the modified settings
     */
    private Map<String, String> fixNestedFormatterSettings(
        CmsObject cms,
        CmsADEConfigData config,
        Map<String, String> propertiesMap) {

        Map<String, String> result = new HashMap<>();
        for (Map.Entry<String, String> entry : propertiesMap.entrySet()) {
            String key = entry.getKey();

            // replace structure ids, fallback keys or alias keys with the main key if possible

            int underscorePos = key.indexOf("_");
            if (underscorePos >= 0) {
                String prefix = key.substring(0, underscorePos);
                I_CmsFormatterBean formatter = config.findFormatter(prefix, /* noWarn = */true);
                if (formatter != null) {
                    key = formatter.getKeyOrId() + key.substring(underscorePos);
                }
            }

            result.put(key, entry.getValue());
        }
        return result;
    }

    /**
     * Do some processing for the element settings before saving them.
     *
     * @param config the ADE configuration
     * @param settings the element settings
     * @return the modified element settings
     */
    private Map<String, String> processSettingsForSaveV1(CmsADEConfigData config, Map<String, String> settings) {

        Map<String, String> result = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : settings.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (key.startsWith(CmsFormatterConfig.FORMATTER_SETTINGS_KEY)) {
                if (!CmsUUID.isValidUUID(value)) {
                    I_CmsFormatterBean dynamicFmt = config.findFormatter(value);
                    if ((dynamicFmt != null) && (dynamicFmt.getId() != null)) {
                        value = dynamicFmt.getId();
                    }
                }
            } else {
                // nested formatters
                int underscorePos = key.indexOf("_");
                if (underscorePos != -1) {
                    String partBeforeUnderscore = key.substring(0, underscorePos);
                    String partAfterUnderscore = key.substring(underscorePos + 1);
                    I_CmsFormatterBean dynamicFmt = config.findFormatter(partBeforeUnderscore);
                    if ((dynamicFmt != null) && dynamicFmt.getSettings(config).containsKey(partAfterUnderscore)) {
                        String id = dynamicFmt.getId();
                        if (id != null) {
                            key = id + "_" + partAfterUnderscore;
                        }
                    }
                }
            }
            result.put(key, value);
        }
        result.remove(CmsContainerElement.SETTING_PAGE_ID);
        return result;
    }

    /**
     * Do some processing for the element settings before saving them.
     *
     * @param config the ADE configuration
     * @param settings the element settings
     * @return the modified element settings
     */
    private Map<String, String> processSettingsForSaveV2(CmsADEConfigData config, Map<String, String> settings) {

        Map<String, String> result = new LinkedHashMap<>();

        for (Map.Entry<String, String> entry : settings.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (key.startsWith(CmsFormatterConfig.FORMATTER_SETTINGS_KEY)) {
                if (CmsUUID.isValidUUID(value)) {
                    I_CmsFormatterBean dynamicFmt = config.findFormatter(value);
                    if ((dynamicFmt != null) && (dynamicFmt.getKey() != null)) {
                        value = dynamicFmt.getKey();
                    }
                }
            }
            result.put(key, value);
        }
        result.remove(CmsContainerElement.SETTING_PAGE_ID);
        result = sortSettingsForSave(translateMapKeys(result, this::translateSettingNameForSave));
        return result;
    }

    /**
     * Adds the given container page to the given element.<p>
     *
     * @param cms the current CMS object
     * @param parent the element to add it
     * @param cntPage the container page to add
     * @param adeConfig the current sitemap configuration
     *
     * @throws CmsException if something goes wrong
     */
    private void saveContainerPageV1(
        CmsObject cms,
        Element parent,
        CmsContainerPageBean cntPage,
        CmsADEConfigData adeConfig)
    throws CmsException {

        // save containers in a defined order
        List<String> containerNames = new ArrayList<String>(cntPage.getNames());
        Collections.sort(containerNames);

        for (String containerName : containerNames) {
            CmsContainerBean container = cntPage.getContainers().get(containerName);

            // the container
            Element cntElement = parent.addElement(XmlNode.Containers.name());
            cntElement.addElement(XmlNode.Name.name()).addCDATA(container.getName());
            cntElement.addElement(XmlNode.Type.name()).addCDATA(container.getType());
            if (container.isNestedContainer()) {
                cntElement.addElement(XmlNode.ParentInstanceId.name()).addCDATA(container.getParentInstanceId());
            }
            if (container.isRootContainer()) {
                cntElement.addElement(XmlNode.IsRootContainer.name()).addText(Boolean.TRUE.toString());
            }

            // the elements
            for (CmsContainerElementBean element : container.getElements()) {
                Element elemElement = cntElement.addElement(XmlNode.Elements.name());

                // the element
                Element uriElem = elemElement.addElement(XmlNode.Uri.name());
                CmsResource uriRes = fillResource(cms, uriElem, element.getId());
                if (element.getFormatterId() != null) {
                    Element formatterElem = elemElement.addElement(XmlNode.Formatter.name());
                    fillResource(cms, formatterElem, element.getFormatterId());
                }
                if (element.isCreateNew()) {
                    Element createNewElem = elemElement.addElement(XmlNode.CreateNew.name());
                    createNewElem.addText(Boolean.TRUE.toString());
                }
                // the properties
                Map<String, String> properties = element.getIndividualSettings();
                Map<String, String> processedSettings = processSettingsForSaveV1(adeConfig, properties);
                Map<String, CmsXmlContentProperty> propertiesConf = OpenCms.getADEManager().getElementSettings(
                    cms,
                    uriRes);

                CmsXmlContentPropertyHelper.saveProperties(cms, elemElement, processedSettings, propertiesConf, true);
            }
        }
    }

    /**
     * Adds the given container page to the given element.<p>
     *
     * @param cms the current CMS object
     * @param parent the element to add it
     * @param cntPage the container page to add
     * @param adeConfig the current sitemap configuration
     *
     * @throws CmsException if something goes wrong
     */
    private void saveContainerPageV2(
        CmsObject cms,
        Element parent,
        CmsContainerPageBean cntPage,
        CmsADEConfigData adeConfig)
    throws CmsException {

        // save containers in a defined order
        List<String> containerNames = sortContainerNames(cntPage);

        for (String containerName : containerNames) {
            CmsContainerBean container = cntPage.getContainers().get(containerName);

            // the container
            Element cntElement = parent.addElement(XmlNode.Containers.name());
            cntElement.addElement(XmlNode.Name.name()).addCDATA(container.getName());
            cntElement.addElement(XmlNode.Type.name()).addCDATA(container.getType());
            if (container.isNestedContainer()) {
                cntElement.addElement(XmlNode.ParentInstanceId.name()).addCDATA(container.getParentInstanceId());
            }
            if (container.isRootContainer()) {
                cntElement.addElement(XmlNode.IsRootContainer.name()).addText(Boolean.TRUE.toString());
            }

            // the elements
            for (CmsContainerElementBean element : container.getElements()) {
                Element elemElement = cntElement.addElement(XmlNode.Elements.name());

                Map<String, String> properties = new HashMap<>(element.getIndividualSettings());

                String instanceId = properties.remove(CmsContainerElement.ELEMENT_INSTANCE_ID);
                if (instanceId != null) {
                    Element instanceIdElem = elemElement.addElement(XmlNode.ElementInstanceId.name());
                    instanceIdElem.addText(instanceId);
                }

                String formatterKey = CmsFormatterUtils.removeFormatterKey(containerName, properties);
                I_CmsFormatterBean formatter = null;
                if (formatterKey != null) {
                    Element formatterKeyElem = elemElement.addElement(XmlNode.FormatterKey.name());

                    formatter = adeConfig.findFormatter(formatterKey);
                    if ((formatter != null) && (formatter.getKeyOrId() != null)) {
                        formatterKey = formatter.getKeyOrId();
                    }
                    formatterKeyElem.addText(formatterKey);
                }

                CmsResource elementRes;
                if (!(formatter instanceof CmsFunctionFormatterBean)) {
                    // the element
                    Element uriElem = elemElement.addElement(XmlNode.Uri.name());
                    elementRes = fillResource(cms, uriElem, element.getId());
                    if ((element.getFormatterId() != null) && (formatterKey == null)) {
                        Element formatterElem = elemElement.addElement(XmlNode.Formatter.name());
                        fillResource(cms, formatterElem, element.getFormatterId());
                    }
                } else {
                    elementRes = cms.readResource(element.getId(), CmsResourceFilter.IGNORE_EXPIRATION);
                }
                if (element.isCreateNew()) {
                    properties.put(CmsContainerElement.SETTING_CREATE_NEW, "true");
                }
                // the properties

                Map<String, String> processedSettings = processSettingsForSaveV2(adeConfig, properties);
                Map<String, CmsXmlContentProperty> propertiesConf = OpenCms.getADEManager().getElementSettings(
                    cms,
                    elementRes);
                CmsXmlContentPropertyHelper.saveProperties(cms, elemElement, processedSettings, propertiesConf, false);
            }
        }
    }

    /**
     * Computes a container sort ordering for saving the containers of a container page bean.<p>
     *
     * @param page the container page bean
     * @return the sorted list of container names
     */
    private List<String> sortContainerNames(CmsContainerPageBean page) {

        Multimap<String, CmsContainerBean> containersByParentId = ArrayListMultimap.create();
        Map<String, CmsContainerElementBean> elementsById = new HashMap<>();
        List<CmsContainerBean> rootContainers = new ArrayList<>();

        //  make table of container elements by instance id

        for (CmsContainerBean container : page.getContainers().values()) {
            for (CmsContainerElementBean element : container.getElements()) {
                if (element.getInstanceId() != null) {
                    elementsById.put(element.getInstanceId(), element);
                }
            }
        }

        // make table of containers by their parent instance id

        for (CmsContainerBean container : page.getContainers().values()) {
            String parentInstanceId = container.getParentInstanceId();
            if (parentInstanceId != null) {
                containersByParentId.put(parentInstanceId, container);
            }
            if ((parentInstanceId == null) || !elementsById.containsKey(parentInstanceId)) {
                rootContainers.add(container);
            }
        }

        // Visit all containers via depth-first traversal, using the previously constructed tables and a stack.
        // Record their names in the order they were encountered.
        // For children of the same container, they are ordered by name.

        rootContainers.sort((a, b) -> b.getName().compareTo(a.getName())); // we put them on a stack, so the last element should be the smallest one
        ArrayList<CmsContainerBean> stack = new ArrayList<>();
        stack.addAll(rootContainers);
        Map<String, Integer> order = new HashMap<>();
        int counter = 0;
        while (stack.size() > 0) {
            CmsContainerBean container = stack.remove(stack.size() - 1);

            // avoid already visited containers, in case there are cycles (possible in principle, if you change the container page manually)
            if (order.containsKey(container.getName())) {
                continue;
            }
            order.put(container.getName(), Integer.valueOf(counter));
            counter += 1;

            for (CmsContainerElementBean element : container.getElements()) {
                String instanceId = element.getInstanceId();
                if (instanceId != null) {
                    List<CmsContainerBean> childContainers = new ArrayList<>(containersByParentId.get(instanceId));
                    childContainers.sort((a, b) -> b.getName().compareTo(a.getName()));
                    stack.addAll(childContainers);
                }
            }
        }
        List<String> result = new ArrayList<>(page.getContainers().keySet());

        result.sort(
            (
                a,
                b) -> ComparisonChain.start().compare(
                    order.get(a),
                    order.get(b),
                    Ordering.natural().nullsLast()).compare(a, b).result());
        return result;
    }

    /**
     * Sort element settings such that system settings come first and normal element settings after that, with each group alphabetically sorted.
     *
     * @param settings the map of settings
     * @return the sorted settings map
     */
    private LinkedHashMap<String, String> sortSettingsForSave(Map<String, String> settings) {

        LinkedHashMap<String, String> result = new LinkedHashMap<>();
        List<String> keys = new ArrayList<>(settings.keySet());
        keys.sort(
            (
                a,
                b) -> ComparisonChain.start().compareTrueFirst(
                    a.startsWith(SYSTEM_SETTING_PREFIX),
                    b.startsWith(SYSTEM_SETTING_PREFIX)).compare(a, b).result());
        for (String key : keys) {
            result.put(key, settings.get(key));
        }
        return result;
    }

    /**
     * Converts a string map to a new map by applying a translation function to the map keys.
     *
     * @param settings the original map
     * @param translation the translation function
     * @return the new map with the translated keys
     */
    private Map<String, String> translateMapKeys(Map<String, String> settings, Function<String, String> translation) {

        LinkedHashMap<String, String> result = new LinkedHashMap<>();
        settings.entrySet().forEach(e -> result.put(translation.apply(e.getKey()), e.getValue()));
        return result;

    }

    /**
     * Translates new SYSTEM:: prefixed names for legacy system element settings to their non-prefixed form.
     *
     * @param name  the setting name
     * @return the translated setting name
     */
    private String translateSettingNameForLoad(String name) {

        if (name.startsWith(SYSTEM_SETTING_PREFIX)) {
            String remainder = name.substring(SYSTEM_SETTING_PREFIX.length());
            if (LEGACY_SYSTEM_SETTING_NAMES.contains(remainder)) {
                return remainder;
            }
        }
        return name;
    }

    /**
     * Translates legacy non-prefixed system settings to the form prefixed with SYSTEM:: .
     *
     * @param name a setting name
     * @return the translated setting name
     */
    private String translateSettingNameForSave(String name) {

        if (LEGACY_SYSTEM_SETTING_NAMES.contains(name)) {
            return SYSTEM_SETTING_PREFIX + name;
        }
        return name;
    }

}
