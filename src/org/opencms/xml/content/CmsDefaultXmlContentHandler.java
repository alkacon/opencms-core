/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/content/CmsDefaultXmlContentHandler.java,v $
 * Date   : $Date: 2007/07/04 16:57:17 $
 * Version: $Revision: 1.47 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.xml.content;

import org.opencms.configuration.CmsConfigurationManager;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsMessages;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsLink;
import org.opencms.relations.CmsRelationType;
import org.opencms.site.CmsSiteManager;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsHtmlConverter;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.I_CmsWidget;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.CmsXmlEntityResolver;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.types.CmsXmlNestedContentDefinition;
import org.opencms.xml.types.CmsXmlVarLinkValue;
import org.opencms.xml.types.CmsXmlVfsFileValue;
import org.opencms.xml.types.I_CmsXmlContentValue;
import org.opencms.xml.types.I_CmsXmlSchemaType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

/**
 * Default implementation for the XML content handler, will be used by all XML contents that do not
 * provide their own handler.<p>
 * 
 * @author Alexander Kandzior 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.47 $ 
 * 
 * @since 6.0.0 
 */
public class CmsDefaultXmlContentHandler implements I_CmsXmlContentHandler {

    /** Constant for the "appinfo" element name itself. */
    public static final String APPINFO_APPINFO = "appinfo";

    /** Constant for the "configuration" appinfo attribute name. */
    public static final String APPINFO_ATTR_CONFIGURATION = "configuration";

    /** Constant for the "element" appinfo attribute name. */
    public static final String APPINFO_ATTR_ELEMENT = "element";

    /** Constant for the "invalidate" appinfo attribute name. */
    public static final String APPINFO_ATTR_INVALIDATE = "invalidate";

    /** Constant for the "mapto" appinfo attribute name. */
    public static final String APPINFO_ATTR_MAPTO = "mapto";

    /** Constant for the "message" appinfo attribute name. */
    public static final String APPINFO_ATTR_MESSAGE = "message";

    /** Constant for the "name" appinfo attribute name. */
    public static final String APPINFO_ATTR_NAME = "name";

    /** Constant for the "regex" appinfo attribute name. */
    public static final String APPINFO_ATTR_REGEX = "regex";

    /** Constant for the "type" appinfo attribute name. */
    public static final String APPINFO_ATTR_TYPE = "type";

    /** Constant for the "node" appinfo attribute value. */
    public static final String APPINFO_ATTR_TYPE_NODE = "node";

    /** Constant for the "parent" appinfo attribute value. */
    public static final String APPINFO_ATTR_TYPE_PARENT = "parent";

    /** Constant for the "warning" appinfo attribute value. */
    public static final String APPINFO_ATTR_TYPE_WARNING = "warning";

    /** Constant for the "uri" appinfo attribute name. */
    public static final String APPINFO_ATTR_URI = "uri";

    /** Constant for the "value" appinfo attribute name. */
    public static final String APPINFO_ATTR_VALUE = "value";

    /** Constant for the "widget" appinfo attribute name. */
    public static final String APPINFO_ATTR_WIDGET = "widget";

    /** Constant for the "default" appinfo element name. */
    public static final String APPINFO_DEFAULT = "default";

    /** Constant for the "defaults" appinfo element name. */
    public static final String APPINFO_DEFAULTS = "defaults";

    /** Constant for the "layout" appinfo element name. */
    public static final String APPINFO_LAYOUT = "layout";

    /** Constant for the "layouts" appinfo element name. */
    public static final String APPINFO_LAYOUTS = "layouts";

    /** Constant for the "mapping" appinfo element name. */
    public static final String APPINFO_MAPPING = "mapping";

    /** Constant for the "mappings" appinfo element name. */
    public static final String APPINFO_MAPPINGS = "mappings";

    /** Constant for the "modelfolder" appinfo element name. */
    public static final String APPINFO_MODELFOLDER = "modelfolder";

    /** Constant for the "preview" appinfo element name. */
    public static final String APPINFO_PREVIEW = "preview";

    /** Constant for the "relation" appinfo element name. */
    public static final String APPINFO_RELATION = "relation";

    /** Constant for the "relations" appinfo element name. */
    public static final String APPINFO_RELATIONS = "relations";

    /** Constant for the "resourcebundle" appinfo element name. */
    public static final String APPINFO_RESOURCEBUNDLE = "resourcebundle";

    /** Constant for the "rule" appinfo element name. */
    public static final String APPINFO_RULE = "rule";

    /** The file where the appinfo schema is located. */
    public static final String APPINFO_SCHEMA_FILE = "org/opencms/xml/content/DefaultAppinfo.xsd";

    /** The XML system is for the appinfo schema. */
    public static final String APPINFO_SCHEMA_SYSTEM_ID = CmsConfigurationManager.DEFAULT_DTD_PREFIX
        + APPINFO_SCHEMA_FILE;

    /** Constant for the "validationrule" appinfo element name. */
    public static final String APPINFO_VALIDATIONRULE = "validationrule";

    /** Constant for the "validationrules" appinfo element name. */
    public static final String APPINFO_VALIDATIONRULES = "validationrules";

    /** Macro for resolving the preview URI. */
    public static final String MACRO_PREVIEW_TEMPFILE = "previewtempfile";

    /** Default message for validation errors. */
    protected static final String MESSAGE_VALIDATION_DEFAULT_ERROR = "${validation.path}: "
        + "${key."
        + Messages.GUI_EDITOR_XMLCONTENT_VALIDATION_ERROR_2
        + "|${validation.value}|[${validation.regex}]}";

    /** Default message for validation warnings. */
    protected static final String MESSAGE_VALIDATION_DEFAULT_WARNING = "${validation.path}: "
        + "${key."
        + Messages.GUI_EDITOR_XMLCONTENT_VALIDATION_WARNING_2
        + "|${validation.value}|[${validation.regex}]}";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsDefaultXmlContentHandler.class);

    /** Prefix to cache the checkrule relation types. */
    private static final String RELATION_TYPE_PREFIX = "rt_";

    /** The configuration values for the element widgets (as defined in the annotations). */
    protected Map m_configurationValues;

    /** The default values for the elements (as defined in the annotations). */
    protected Map m_defaultValues;

    /** The element mappings (as defined in the annotations). */
    protected Map m_elementMappings;

    /** The widgets used for the elements (as defined in the annotations). */
    protected Map m_elementWidgets;

    /** The resource bundle name to be used for localization of this content handler. */
    protected String m_messageBundleName;

    /** The folder containing the model file(s) for the content. */
    protected String m_modelFolder;

    /** The preview location (as defined in the annotations). */
    protected String m_previewLocation;

    /** The check rules. */
    protected Map m_relations;

    /** The messages for the error validation rules. */
    protected Map m_validationErrorMessages;

    /** The validation rules that cause an error (as defined in the annotations). */
    protected Map m_validationErrorRules;

    /** The messages for the warning validation rules. */
    protected Map m_validationWarningMessages;

    /** The validation rules that cause a warning (as defined in the annotations). */
    protected Map m_validationWarningRules;

    /**
     * Creates a new instance of the default XML content handler.<p>  
     */
    public CmsDefaultXmlContentHandler() {

        init();
    }

    /**
     * Static initializer for caching the default appinfo validation schema.<p>
     */
    static {

        // the schema definition is located in a separate file for easier editing
        byte[] appinfoSchema;
        try {
            appinfoSchema = CmsFileUtil.readFile(APPINFO_SCHEMA_FILE);
        } catch (Exception e) {
            throw new CmsRuntimeException(Messages.get().container(
                org.opencms.xml.types.Messages.ERR_XMLCONTENT_LOAD_SCHEMA_1,
                APPINFO_SCHEMA_FILE), e);
        }
        CmsXmlEntityResolver.cacheSystemId(APPINFO_SCHEMA_SYSTEM_ID, appinfoSchema);
    }

    /**
     * @see org.opencms.xml.content.I_CmsXmlContentHandler#getConfiguration(org.opencms.xml.types.I_CmsXmlSchemaType)
     */
    public String getConfiguration(I_CmsXmlSchemaType type) {

        String elementName = type.getName();
        return (String)m_configurationValues.get(elementName);
    }

    /**
     * @see org.opencms.xml.content.I_CmsXmlContentHandler#getDefault(org.opencms.file.CmsObject, I_CmsXmlContentValue, java.util.Locale)
     */
    public String getDefault(CmsObject cms, I_CmsXmlContentValue value, Locale locale) {

        String defaultValue;
        if (value.getElement() == null) {
            // use the "getDefault" method of the given value, will use value from standard XML schema
            defaultValue = value.getDefault(locale);
        } else {
            String xpath = value.getPath();
            // look up the default from the configured mappings
            defaultValue = (String)m_defaultValues.get(xpath);
            if (defaultValue == null) {
                // no value found, try default xpath
                xpath = CmsXmlUtils.removeXpath(xpath);
                xpath = CmsXmlUtils.createXpath(xpath, 1);
                // look up the default value again with default index of 1 in all path elements
                defaultValue = (String)m_defaultValues.get(xpath);
            }
        }
        if (defaultValue != null) {
            // return the default value with processed macros
            CmsMacroResolver resolver = CmsMacroResolver.newInstance().setCmsObject(cms).setMessages(
                getMessages(locale));
            return resolver.resolveMacros(defaultValue);
        }
        // no default value is available
        return null;
    }

    /**
     * Returns the mapping defined for the given element xpath.<p>
     * 
     * @param elementName the element xpath to look up the mapping for
     * @return the mapping defined for the given element xpath
     */
    public String getMapping(String elementName) {

        return (String)m_elementMappings.get(elementName);
    }

    /**
     * @see org.opencms.xml.content.I_CmsXmlContentHandler#getMessages(java.util.Locale)
     */
    public CmsMessages getMessages(Locale locale) {

        if (m_messageBundleName == null) {
            // no message bundle was initialized
            return null;
        }

        return new CmsMessages(m_messageBundleName, locale);
    }

    /**
     * @see org.opencms.xml.content.I_CmsXmlContentHandler#getModelFolder(org.opencms.file.CmsObject, java.lang.String)
     */
    public String getModelFolder(CmsObject cms, String currentFolder) {

        String result = m_modelFolder;
        // store the original uri
        String uri = cms.getRequestContext().getUri();
        try {
            // set uri to current folder
            cms.getRequestContext().setUri(currentFolder);
            CmsMacroResolver resolver = CmsMacroResolver.newInstance().setCmsObject(cms);
            // resolve eventual macros
            result = resolver.resolveMacros(m_modelFolder);
        } finally {
            // switch back to stored uri
            cms.getRequestContext().setUri(uri);
        }
        return result;
    }

    /**
     * @see org.opencms.xml.content.I_CmsXmlContentHandler#getPreview(org.opencms.file.CmsObject, org.opencms.xml.content.CmsXmlContent, java.lang.String)
     */
    public String getPreview(CmsObject cms, CmsXmlContent content, String resourcename) {

        CmsMacroResolver resolver = CmsMacroResolver.newInstance().setCmsObject(cms);
        resolver.addMacro(MACRO_PREVIEW_TEMPFILE, resourcename);

        return resolver.resolveMacros(m_previewLocation);
    }

    /**
     * @see I_CmsXmlContentHandler#getRelationType(I_CmsXmlContentValue)
     */
    public CmsRelationType getRelationType(I_CmsXmlContentValue value) {

        if (value == null) {
            return null;
        }
        String xpath = value.getPath();
        CmsRelationType relationType = null;
        // look up the default from the configured mappings
        relationType = (CmsRelationType)m_relations.get(RELATION_TYPE_PREFIX + xpath);
        if (relationType == null) {
            // no value found, try default xpath
            String path = CmsXmlUtils.removeXpathIndex(xpath);
            // look up the default value again without indexes
            relationType = (CmsRelationType)m_relations.get(RELATION_TYPE_PREFIX + path);
        }
        if (relationType == null) {
            // no value found, try the last simple type path
            String path = CmsXmlUtils.getLastXpathElement(xpath);
            // look up the default value again for the last simple type
            relationType = (CmsRelationType)m_relations.get(RELATION_TYPE_PREFIX + path);
        }
        if (relationType == null) {
            return CmsRelationType.XML_WEAK;
        }
        return relationType;
    }

    /**
     * @see org.opencms.xml.content.I_CmsXmlContentHandler#getWidget(org.opencms.xml.types.I_CmsXmlContentValue)
     */
    public I_CmsWidget getWidget(I_CmsXmlContentValue value) {

        // try the specific widget settings first
        I_CmsWidget result = (I_CmsWidget)m_elementWidgets.get(value.getName());
        if (result == null) {
            // use default widget mappings
            result = OpenCms.getXmlContentTypeManager().getWidgetDefault(value.getTypeName());
        } else {
            result = result.newInstance();
        }
        // set the configuration value for this widget
        result.setConfiguration(getConfiguration(value));

        return result;
    }

    /**
     * @see org.opencms.xml.content.I_CmsXmlContentHandler#initialize(org.dom4j.Element, org.opencms.xml.CmsXmlContentDefinition)
     */
    public synchronized void initialize(Element appInfoElement, CmsXmlContentDefinition contentDefinition)
    throws CmsXmlException {

        if (appInfoElement != null) {
            // validate the appinfo element XML content with the default appinfo handler schema
            validateAppinfoElement(appInfoElement);

            // re-initialize the local variables
            init();

            Iterator i = appInfoElement.elements().iterator();
            while (i.hasNext()) {
                // iterate all elements in the appinfo node
                Element element = (Element)i.next();
                String nodeName = element.getName();
                if (nodeName.equals(APPINFO_MAPPINGS)) {
                    initMappings(element, contentDefinition);
                } else if (nodeName.equals(APPINFO_LAYOUTS)) {
                    initLayouts(element, contentDefinition);
                } else if (nodeName.equals(APPINFO_VALIDATIONRULES)) {
                    initValidationRules(element, contentDefinition);
                } else if (nodeName.equals(APPINFO_RELATIONS)) {
                    initRelations(element, contentDefinition);
                } else if (nodeName.equals(APPINFO_DEFAULTS)) {
                    initDefaultValues(element, contentDefinition);
                } else if (nodeName.equals(APPINFO_MODELFOLDER)) {
                    initModelFolder(element, contentDefinition);
                } else if (nodeName.equals(APPINFO_PREVIEW)) {
                    initPreview(element, contentDefinition);
                } else if (nodeName.equals(APPINFO_RESOURCEBUNDLE)) {
                    initResourceBundle(element, contentDefinition);
                }
            }
        }

        // at the end, add default check rules for optional file references
        addDefaultCheckRules(contentDefinition, null, null);
    }

    /**
     * @see org.opencms.xml.content.I_CmsXmlContentHandler#invalidateBrokenLinks(CmsObject, CmsXmlContent)
     */
    public void invalidateBrokenLinks(CmsObject cms, CmsXmlContent document) {

        if ((cms == null) || (cms.getRequestContext().getRequestTime() == CmsResource.DATE_RELEASED_EXPIRED_IGNORE)) {
            // do not check if the request comes the editor
            return;
        }
        boolean needReinitialization = false;
        // iterate the locales
        Iterator itLocales = document.getLocales().iterator();
        while (itLocales.hasNext()) {
            Locale locale = (Locale)itLocales.next();
            List removedNodes = new ArrayList();
            // iterate the values
            Iterator itValues = document.getValues(locale).iterator();
            while (itValues.hasNext()) {
                I_CmsXmlContentValue value = (I_CmsXmlContentValue)itValues.next();
                String path = value.getPath();
                // check if this value has already been deleted by parent rules 
                boolean alreadyRemoved = false;
                Iterator itRemNodes = removedNodes.iterator();
                while (itRemNodes.hasNext()) {
                    String remNode = (String)itRemNodes.next();
                    if (path.startsWith(remNode)) {
                        alreadyRemoved = true;
                        break;
                    }
                }
                // only continue if not already removed and if a rule match
                if (alreadyRemoved
                    || ((m_relations.get(path) == null) && (m_relations.get(CmsXmlUtils.removeXpath(path)) == null))) {
                    continue;
                }

                // check rule matched
                if (LOG.isDebugEnabled()) {
                    LOG.debug(Messages.get().getBundle().key(Messages.LOG_XMLCONTENT_CHECK_RULE_MATCH_1, path));
                }
                if (validateLink(cms, value, null)) {
                    // invalid link
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(Messages.get().getBundle().key(
                            Messages.LOG_XMLCONTENT_CHECK_WARNING_2,
                            path,
                            value.getStringValue(cms)));
                    }
                    // find the node to remove
                    String parentPath = path;
                    while (isInvalidateParent(parentPath)) {
                        // check parent
                        parentPath = CmsXmlUtils.removeLastXpathElement(parentPath);
                        // log info
                        if (LOG.isDebugEnabled()) {
                            LOG.debug(Messages.get().getBundle().key(
                                Messages.LOG_XMLCONTENT_CHECK_PARENT_2,
                                path,
                                parentPath));
                        }
                    }
                    value = document.getValue(parentPath, locale);
                    // detach the value node from the XML document
                    value.getElement().detach();
                    // mark node as deleted
                    removedNodes.add(parentPath);
                }
            }
            if (!removedNodes.isEmpty()) {
                needReinitialization = true;
            }
        }
        if (needReinitialization) {
            // re-initialize the XML content 
            document.initDocument();
        }
    }

    /**
     * @see org.opencms.xml.content.I_CmsXmlContentHandler#prepareForUse(org.opencms.file.CmsObject, org.opencms.xml.content.CmsXmlContent)
     */
    public CmsXmlContent prepareForUse(CmsObject cms, CmsXmlContent content) {

        // noop, just return the unmodified content
        return content;
    }

    /**
     * @see org.opencms.xml.content.I_CmsXmlContentHandler#prepareForWrite(org.opencms.file.CmsObject, org.opencms.xml.content.CmsXmlContent, org.opencms.file.CmsFile)
     */
    public CmsFile prepareForWrite(CmsObject cms, CmsXmlContent content, CmsFile file) throws CmsException {

        if (!content.isAutoCorrectionEnabled()) {
            // check if the XML should be corrected automatically (if not already set)
            Object attribute = cms.getRequestContext().getAttribute(CmsXmlContent.AUTO_CORRECTION_ATTRIBUTE);
            // set the auto correction mode as required
            boolean autoCorrectionEnabled = (attribute != null) && ((Boolean)attribute).booleanValue();
            content.setAutoCorrectionEnabled(autoCorrectionEnabled);
        }
        // validate the xml structure before writing the file if required                 
        if (!content.isAutoCorrectionEnabled()) {
            // an exception will be thrown if the structure is invalid
            content.validateXmlStructure(new CmsXmlEntityResolver(cms));
        }
        // read the content-conversion property
        String contentConversion = CmsHtmlConverter.getConversionSettings(cms, file);
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(contentConversion)) {
            // enable pretty printing and XHTML conversion of XML content html fields by default
            contentConversion = CmsHtmlConverter.PARAM_XHTML;
        }
        content.setConversion(contentConversion);
        // correct the HTML structure
        file = content.correctXmlStructure(cms);
        content.setFile(file);
        // resolve the file mappings
        content.resolveMappings(cms);
        // ensure all property mappings of deleted optional values are removed
        removeEmptyMappings(cms, content);
        // return the result
        return file;
    }

    /**
     * @see org.opencms.xml.content.I_CmsXmlContentHandler#resolveMapping(org.opencms.file.CmsObject, org.opencms.xml.content.CmsXmlContent, org.opencms.xml.types.I_CmsXmlContentValue)
     */
    public void resolveMapping(CmsObject cms, CmsXmlContent content, I_CmsXmlContentValue value) throws CmsException {

        if (!value.isSimpleType()) {
            // no mappings for a nested schema are possible
            // note that the sub-elemenets of the nested schema ARE mapped by the node visitor,
            // it's just the nested schema value itself that does not support mapping
            return;
        }

        // get the original VFS file from the content
        CmsFile file = content.getFile();
        if (file == null) {
            throw new CmsXmlException(Messages.get().container(Messages.ERR_XMLCONTENT_RESOLVE_FILE_NOT_FOUND_0));
        }

        // get the mapping for the element name        
        String mapping = getMapping(value.getPath());

        if (CmsStringUtil.isNotEmpty(mapping)) {

            // get root path of the file 
            String rootPath = content.getFile().getRootPath();
            String storedSiteRoot = cms.getRequestContext().getSiteRoot();

            try {
                // try / catch to ensure site root is always restored
                cms.getRequestContext().setSiteRoot("/");

                // read all siblings of the file
                List siblings = cms.readSiblings(rootPath, CmsResourceFilter.IGNORE_EXPIRATION);

                // for multilanguage mappings, we need to ensure 
                // a) all siblings are handled
                // b) only the "right" locale is mapped to a sibling

                for (int i = (siblings.size() - 1); i >= 0; i--) {
                    // get filename
                    String filename = ((CmsResource)siblings.get(i)).getRootPath();
                    Locale locale = OpenCms.getLocaleManager().getDefaultLocale(cms, filename);

                    if (!locale.equals(value.getLocale())) {
                        // only map property if the locale fits
                        continue;
                    }

                    // get the string value of the current node
                    String stringValue = value.getStringValue(cms);
                    if (mapping.startsWith(MAPTO_PROPERTY_LIST) && (value.getIndex() == 0)) {

                        boolean mapToShared;
                        int prefixLength;
                        // check which mapping is used (shared or individual)
                        if (mapping.startsWith(MAPTO_PROPERTY_LIST_SHARED)) {
                            mapToShared = true;
                            prefixLength = MAPTO_PROPERTY_LIST_SHARED.length();
                        } else if (mapping.startsWith(MAPTO_PROPERTY_LIST_INDIVIDUAL)) {
                            mapToShared = false;
                            prefixLength = MAPTO_PROPERTY_LIST_INDIVIDUAL.length();
                        } else {
                            mapToShared = false;
                            prefixLength = MAPTO_PROPERTY_LIST.length();
                        }

                        // this is a property list mapping
                        String property = mapping.substring(prefixLength);

                        String path = CmsXmlUtils.removeXpathIndex(value.getPath());
                        List values = content.getValues(path, locale);
                        Iterator j = values.iterator();
                        StringBuffer result = new StringBuffer(values.size() * 64);
                        while (j.hasNext()) {
                            I_CmsXmlContentValue val = (I_CmsXmlContentValue)j.next();
                            result.append(val.getStringValue(cms));
                            if (j.hasNext()) {
                                result.append(CmsProperty.VALUE_LIST_DELIMITER);
                            }
                        }

                        CmsProperty p;
                        if (mapToShared) {
                            // map to shared value
                            p = new CmsProperty(property, null, result.toString());
                        } else {
                            // map to individual value
                            p = new CmsProperty(property, result.toString(), null);
                        }
                        // write the created list string value in the selected property
                        cms.writePropertyObject(filename, p);
                        if (mapToShared) {
                            // special case: shared mappings must be written only to one sibling, end loop
                            i = 0;
                        }

                    } else if (mapping.startsWith(MAPTO_PROPERTY)) {

                        boolean mapToShared;
                        int prefixLength;
                        // check which mapping is used (shared or individual)                        
                        if (mapping.startsWith(MAPTO_PROPERTY_SHARED)) {
                            mapToShared = true;
                            prefixLength = MAPTO_PROPERTY_SHARED.length();
                        } else if (mapping.startsWith(MAPTO_PROPERTY_INDIVIDUAL)) {
                            mapToShared = false;
                            prefixLength = MAPTO_PROPERTY_INDIVIDUAL.length();
                        } else {
                            mapToShared = false;
                            prefixLength = MAPTO_PROPERTY.length();
                        }

                        // this is a property mapping
                        String property = mapping.substring(prefixLength);

                        CmsProperty p;
                        if (mapToShared) {
                            // map to shared value
                            p = new CmsProperty(property, null, stringValue);
                        } else {
                            // map to individual value
                            p = new CmsProperty(property, stringValue, null);
                        }
                        // just store the string value in the selected property
                        cms.writePropertyObject(filename, p);
                        if (mapToShared) {
                            // special case: shared mappings must be written only to one sibling, end loop
                            i = 0;
                        }

                    } else if (mapping.startsWith(MAPTO_ATTRIBUTE)) {

                        // this is an attribute mapping                        
                        String attribute = mapping.substring(MAPTO_ATTRIBUTE.length());
                        switch (ATTRIBUTES.indexOf(attribute)) {
                            case 0: // datereleased
                                long date = 0;
                                try {
                                    date = Long.valueOf(stringValue).longValue();
                                } catch (NumberFormatException e) {
                                    // ignore, value can be a macro
                                }
                                if (date == 0) {
                                    date = CmsResource.DATE_RELEASED_DEFAULT;
                                }
                                file.setDateReleased(date);
                                break;
                            case 1: // dateexpired
                                date = 0;
                                try {
                                    date = Long.valueOf(stringValue).longValue();
                                } catch (NumberFormatException e) {
                                    // ignore, value can be a macro
                                }
                                if (date == 0) {
                                    date = CmsResource.DATE_EXPIRED_DEFAULT;
                                }
                                file.setDateExpired(date);
                                break;
                            default:
                                // ignore invalid / other mappings                                
                        }
                    }
                }

            } finally {
                // restore the saved site root
                cms.getRequestContext().setSiteRoot(storedSiteRoot);
            }
        }
    }

    /**
     * @see org.opencms.xml.content.I_CmsXmlContentHandler#resolveValidation(org.opencms.file.CmsObject, org.opencms.xml.types.I_CmsXmlContentValue, org.opencms.xml.content.CmsXmlContentErrorHandler)
     */
    public CmsXmlContentErrorHandler resolveValidation(
        CmsObject cms,
        I_CmsXmlContentValue value,
        CmsXmlContentErrorHandler errorHandler) {

        if (errorHandler == null) {
            // init a new error handler if required
            errorHandler = new CmsXmlContentErrorHandler();
        }

        if (!value.isSimpleType()) {
            // no validation for a nested schema is possible
            // note that the sub-elemenets of the nested schema ARE validated by the node visitor,
            // it's just the nested schema value itself that does not support validation
            return errorHandler;
        }

        // validate the error rules
        errorHandler = validateValue(cms, value, errorHandler, m_validationErrorRules, false);
        // validate the warning rules
        errorHandler = validateValue(cms, value, errorHandler, m_validationWarningRules, true);

        // return the result
        return errorHandler;
    }

    /**
     * Adds a check rule for a specified element.<p> 
     * 
     * @param contentDefinition the XML content definition this XML content handler belongs to
     * @param elementName the element name to add the rule to 
     * @param invalidate 
     *              <code>false</code>, to disable link check
     *              <code>true</code> or <code>node</code>, to invalidate just the single node if the link is broken 
     *              <code>parent</code>, if this rule will invalidate the whole parent node in nested content
     * @param type the relation type
     * 
     * @throws CmsXmlException in case an unknown element name is used
     */
    protected void addCheckRule(
        CmsXmlContentDefinition contentDefinition,
        String elementName,
        String invalidate,
        String type) throws CmsXmlException {

        I_CmsXmlSchemaType schemaType = contentDefinition.getSchemaType(elementName);
        if (schemaType == null) {
            // no element with the given name
            throw new CmsXmlException(Messages.get().container(
                Messages.ERR_XMLCONTENT_CHECK_INVALID_ELEM_1,
                elementName));
        }
        if (!CmsXmlVfsFileValue.TYPE_NAME.equals(schemaType.getTypeName())
            && !CmsXmlVarLinkValue.TYPE_NAME.equals(schemaType.getTypeName())) {
            // element is not a OpenCmsVfsFile
            throw new CmsXmlException(Messages.get().container(
                Messages.ERR_XMLCONTENT_CHECK_INVALID_TYPE_1,
                elementName));
        }

        // cache the check rule data
        Boolean invalidateParent = null;
        if ((invalidate == null)
            || invalidate.equalsIgnoreCase(Boolean.TRUE.toString())
            || invalidate.equalsIgnoreCase(APPINFO_ATTR_TYPE_NODE)) {
            invalidateParent = Boolean.FALSE;
        } else if (invalidate.equalsIgnoreCase(APPINFO_ATTR_TYPE_PARENT)) {
            invalidateParent = Boolean.TRUE;
        }
        if (invalidateParent != null) {
            m_relations.put(elementName, invalidateParent);
        }
        CmsRelationType relationType = (type == null ? CmsRelationType.XML_WEAK : CmsRelationType.valueOfXml(type));
        m_relations.put(RELATION_TYPE_PREFIX + elementName, relationType);

        if (invalidateParent != null) {
            // check the whole xpath hierarchy
            String path = elementName;
            while (CmsStringUtil.isNotEmptyOrWhitespaceOnly(path)) {
                if (!isInvalidateParent(path)) {
                    // if invalidate type = node, then the node needs to be optional
                    if (contentDefinition.getSchemaType(path).getMinOccurs() > 0) {
                        // element is not optional
                        throw new CmsXmlException(Messages.get().container(
                            Messages.ERR_XMLCONTENT_CHECK_NOT_OPTIONAL_1,
                            path));
                    }
                    // no need to further check
                    break;
                } else if (!CmsXmlUtils.isDeepXpath(path)) {
                    // if invalidate type = parent, then the node needs to be nested
                    // document root can not be invalidated
                    throw new CmsXmlException(Messages.get().container(Messages.ERR_XMLCONTENT_CHECK_NOT_EMPTY_DOC_0));
                }
                path = CmsXmlUtils.removeLastXpathElement(path);
            }
        }
    }

    /**
     * Adds a configuration value for an element widget.<p>
     * 
     * @param contentDefinition the XML content definition this XML content handler belongs to
     * @param elementName the element name to map
     * @param configurationValue the configuration value to use
     * 
     * @throws CmsXmlException in case an unknown element name is used
     */
    protected void addConfiguration(
        CmsXmlContentDefinition contentDefinition,
        String elementName,
        String configurationValue) throws CmsXmlException {

        if (contentDefinition.getSchemaType(elementName) == null) {
            throw new CmsXmlException(Messages.get().container(
                Messages.ERR_XMLCONTENT_CONFIG_ELEM_UNKNOWN_1,
                elementName));
        }

        m_configurationValues.put(elementName, configurationValue);
    }

    /**
     * Adds a default value for an element.<p>
     * 
     * @param contentDefinition the XML content definition this XML content handler belongs to
     * @param elementName the element name to map
     * @param defaultValue the default value to use
     * 
     * @throws CmsXmlException in case an unknown element name is used
     */
    protected void addDefault(CmsXmlContentDefinition contentDefinition, String elementName, String defaultValue)
    throws CmsXmlException {

        if (contentDefinition.getSchemaType(elementName) == null) {
            throw new CmsXmlException(org.opencms.xml.types.Messages.get().container(
                org.opencms.xml.types.Messages.ERR_XMLCONTENT_INVALID_ELEM_DEFAULT_1,
                elementName));
        }
        // store mappings as Xpath to allow better control about what is mapped
        String xpath = CmsXmlUtils.createXpath(elementName, 1);
        m_defaultValues.put(xpath, defaultValue);
    }

    /**
     * Adds all needed default check rules recursively for the given schema type.<p> 
     * 
     * @param rootContentDefinition the root content definition
     * @param schemaType the schema type to check
     * @param elementPath the current element path
     * 
     * @throws CmsXmlException if something goes wrong
     */
    protected void addDefaultCheckRules(
        CmsXmlContentDefinition rootContentDefinition,
        I_CmsXmlSchemaType schemaType,
        String elementPath) throws CmsXmlException {

        if ((schemaType != null) && schemaType.isSimpleType()) {
            if ((schemaType.getMinOccurs() == 0)
                && (CmsXmlVfsFileValue.TYPE_NAME.equals(schemaType.getTypeName()) || CmsXmlVarLinkValue.TYPE_NAME.equals(schemaType.getTypeName()))
                && !m_relations.containsKey(elementPath)
                && !m_relations.containsKey(RELATION_TYPE_PREFIX + elementPath)) {
                // add default check rule for the element
                addCheckRule(rootContentDefinition, elementPath, null, null);
            }
        } else {
            // recursion required
            CmsXmlContentDefinition nestedContentDefinition = rootContentDefinition;
            if (schemaType != null) {
                CmsXmlNestedContentDefinition nestedDefinition = (CmsXmlNestedContentDefinition)schemaType;
                nestedContentDefinition = nestedDefinition.getNestedContentDefinition();
            }
            Iterator itElems = nestedContentDefinition.getSchemaTypes().iterator();
            while (itElems.hasNext()) {
                String element = (String)itElems.next();
                String path = (schemaType != null ? CmsXmlUtils.concatXpath(elementPath, element) : element);
                addDefaultCheckRules(rootContentDefinition, nestedContentDefinition.getSchemaType(element), path);
            }
        }
    }

    /**
     * Adds an element mapping.<p>
     * 
     * @param contentDefinition the XML content definition this XML content handler belongs to
     * @param elementName the element name to map
     * @param mapping the mapping to use
     * 
     * @throws CmsXmlException in case an unknown element name is used
     */
    protected void addMapping(CmsXmlContentDefinition contentDefinition, String elementName, String mapping)
    throws CmsXmlException {

        if (contentDefinition.getSchemaType(elementName) == null) {
            throw new CmsXmlException(Messages.get().container(
                Messages.ERR_XMLCONTENT_INVALID_ELEM_MAPPING_1,
                elementName));
        }

        // store mappings as Xpath to allow better control about what is mapped
        String xpath = CmsXmlUtils.createXpath(elementName, 1);
        m_elementMappings.put(xpath, mapping);
    }

    /**
     * Adds a validation rule for a specified element.<p> 
     * 
     * @param contentDefinition the XML content definition this XML content handler belongs to
     * @param elementName the element name to add the rule to 
     * @param regex the validation rule regular expression
     * @param message the message in case validation fails (may be null)
     * @param isWarning if true, this rule is used for warnings, otherwise it's an error
     * 
     * @throws CmsXmlException in case an unknown element name is used
     */
    protected void addValidationRule(
        CmsXmlContentDefinition contentDefinition,
        String elementName,
        String regex,
        String message,
        boolean isWarning) throws CmsXmlException {

        if (contentDefinition.getSchemaType(elementName) == null) {
            throw new CmsXmlException(Messages.get().container(
                Messages.ERR_XMLCONTENT_INVALID_ELEM_VALIDATION_1,
                elementName));
        }

        if (isWarning) {
            m_validationWarningRules.put(elementName, regex);
            if (message != null) {
                m_validationWarningMessages.put(elementName, message);
            }
        } else {
            m_validationErrorRules.put(elementName, regex);
            if (message != null) {
                m_validationErrorMessages.put(elementName, message);
            }
        }
    }

    /**
     * Adds a GUI widget for a specified element.<p> 
     * 
     * @param contentDefinition the XML content definition this XML content handler belongs to
     * @param elementName the element name to map
     * @param widgetClassOrAlias the widget to use as GUI for the element (registered alias or class name)
     * 
     * @throws CmsXmlException in case an unknown element name is used
     */
    protected void addWidget(CmsXmlContentDefinition contentDefinition, String elementName, String widgetClassOrAlias)
    throws CmsXmlException {

        if (contentDefinition.getSchemaType(elementName) == null) {
            throw new CmsXmlException(Messages.get().container(
                Messages.ERR_XMLCONTENT_INVALID_ELEM_LAYOUTWIDGET_1,
                elementName));
        }

        // get the base widget from the XML content type manager
        I_CmsWidget widget = OpenCms.getXmlContentTypeManager().getWidget(widgetClassOrAlias);

        if (widget == null) {
            // no registered widget class found
            if (CmsStringUtil.isValidJavaClassName(widgetClassOrAlias)) {
                // java class name given, try to create new instance of the class and cast to widget
                try {
                    Class specialWidgetClass = Class.forName(widgetClassOrAlias);
                    widget = (I_CmsWidget)specialWidgetClass.newInstance();
                } catch (Exception e) {
                    throw new CmsXmlException(Messages.get().container(
                        Messages.ERR_XMLCONTENT_INVALID_CUSTOM_CLASS_3,
                        widgetClassOrAlias,
                        elementName,
                        contentDefinition.getSchemaLocation()), e);
                }
            }
            if (widget == null) {
                // no valid widget found
                throw new CmsXmlException(Messages.get().container(
                    Messages.ERR_XMLCONTENT_INVALID_WIDGET_3,
                    widgetClassOrAlias,
                    elementName,
                    contentDefinition.getSchemaLocation()));
            }
        }
        m_elementWidgets.put(elementName, widget);
    }

    /**
     * Returns the validation message to be displayed if a certain rule was violated.<p> 
     * 
     * @param cms the current users OpenCms context
     * @param value the value to validate
     * @param regex the rule that was vialoted
     * @param valueStr the string value of the given value
     * @param matchResult if false, the rule was negated
     * @param isWarning if true, this validation indicate a warning, otherwise an error
     * 
     * @return the validation message to be displayed 
     */
    protected String getValidationMessage(
        CmsObject cms,
        I_CmsXmlContentValue value,
        String regex,
        String valueStr,
        boolean matchResult,
        boolean isWarning) {

        String message = null;
        if (isWarning) {
            message = (String)m_validationWarningMessages.get(value.getName());
        } else {
            message = (String)m_validationErrorMessages.get(value.getName());
        }

        if (message == null) {
            if (isWarning) {
                message = MESSAGE_VALIDATION_DEFAULT_WARNING;
            } else {
                message = MESSAGE_VALIDATION_DEFAULT_ERROR;
            }
        }

        // create additional macro values
        Map additionalValues = new HashMap();
        additionalValues.put(CmsMacroResolver.KEY_VALIDATION_VALUE, valueStr);
        additionalValues.put(CmsMacroResolver.KEY_VALIDATION_REGEX, ((!matchResult) ? "!" : "") + regex);
        additionalValues.put(CmsMacroResolver.KEY_VALIDATION_PATH, value.getPath());

        CmsMacroResolver resolver = CmsMacroResolver.newInstance().setCmsObject(cms).setMessages(
            getMessages(cms.getRequestContext().getLocale())).setAdditionalMacros(additionalValues);

        return resolver.resolveMacros(message);
    }

    /**
     * Called when this content handler is initialized.<p> 
     */
    protected void init() {

        m_elementMappings = new HashMap();
        m_elementWidgets = new HashMap();
        m_validationErrorRules = new HashMap();
        m_validationErrorMessages = new HashMap();
        m_validationWarningRules = new HashMap();
        m_validationWarningMessages = new HashMap();
        m_defaultValues = new HashMap();
        m_configurationValues = new HashMap();
        m_relations = new HashMap();
        m_previewLocation = null;
        m_modelFolder = null;
    }

    /**
     * Initializes the default values for this content handler.<p>
     * 
     * Using the default values from the appinfo node, it's possible to have more 
     * sophisticated logic for generating the defaults then just using the XML schema "default"
     * attribute.<p> 
     * 
     * @param root the "defaults" element from the appinfo node of the XML content definition
     * @param contentDefinition the content definition the default values belong to
     * @throws CmsXmlException if something goes wrong
     */
    protected void initDefaultValues(Element root, CmsXmlContentDefinition contentDefinition) throws CmsXmlException {

        Iterator i = root.elementIterator(APPINFO_DEFAULT);
        while (i.hasNext()) {
            // iterate all "default" elements in the "defaults" node
            Element element = (Element)i.next();
            String elementName = element.attributeValue(APPINFO_ATTR_ELEMENT);
            String defaultValue = element.attributeValue(APPINFO_ATTR_VALUE);
            if ((elementName != null) && (defaultValue != null)) {
                // add a default value mapping for the element
                addDefault(contentDefinition, elementName, defaultValue);
            }
        }
    }

    /**
     * Initializes the layout for this content handler.<p>
     * 
     * Unless otherwise instructed, the editor uses one specific GUI widget for each 
     * XML value schema type. For example, for a {@link org.opencms.xml.types.CmsXmlStringValue} 
     * the default widget is the {@link org.opencms.widgets.CmsInputWidget}.
     * However, certain values can also use more then one widget, for example you may 
     * also use a {@link org.opencms.widgets.CmsCheckboxWidget} for a String value,
     * and as a result the Strings possible values would be eithe <code>"false"</code> or <code>"true"</code>,
     * but nevertheless be a String.<p>
     *
     * The widget to use can further be controlled using the <code>widget</code> attribute.
     * You can specifiy either a valid widget alias such as <code>StringWidget</code>, 
     * or the name of a Java class that implements <code>{@link I_CmsWidget}</code>.<p>
     * 
     * Configuration options to the widget can be passed using the <code>configuration</code>
     * attribute. You can specify any String as configuration. This String is then passed
     * to the widget during initialization. It's up to the individual widget implementation 
     * to interpret this configuration String.<p>
     * 
     * @param root the "layouts" element from the appinfo node of the XML content definition
     * @param contentDefinition the content definition the layout belongs to
     * 
     * @throws CmsXmlException if something goes wrong
     */
    protected void initLayouts(Element root, CmsXmlContentDefinition contentDefinition) throws CmsXmlException {

        Iterator i = root.elementIterator(APPINFO_LAYOUT);
        while (i.hasNext()) {
            // iterate all "layout" elements in the "layouts" node
            Element element = (Element)i.next();
            String elementName = element.attributeValue(APPINFO_ATTR_ELEMENT);
            String widgetClassOrAlias = element.attributeValue(APPINFO_ATTR_WIDGET);
            String configuration = element.attributeValue(APPINFO_ATTR_CONFIGURATION);
            if ((elementName != null) && (widgetClassOrAlias != null)) {
                // add a widget mapping for the element
                addWidget(contentDefinition, elementName, widgetClassOrAlias);
                if (configuration != null) {
                    addConfiguration(contentDefinition, elementName, configuration);
                }
            }
        }
    }

    /**
     * Initializes the element mappings for this content handler.<p>
     * 
     * Element mappings allow storing values from the XML content in other locations.
     * For example, if you have an elemenet called "Title", it's likley a good idea to 
     * store the value of this element also in the "Title" property of a XML content resource.<p>
     * 
     * @param root the "mappings" element from the appinfo node of the XML content definition
     * @param contentDefinition the content definition the mappings belong to
     * @throws CmsXmlException if something goes wrong
     */
    protected void initMappings(Element root, CmsXmlContentDefinition contentDefinition) throws CmsXmlException {

        Iterator i = root.elementIterator(APPINFO_MAPPING);
        while (i.hasNext()) {
            // iterate all "mapping" elements in the "mappings" node
            Element element = (Element)i.next();
            // this is a mapping node
            String elementName = element.attributeValue(APPINFO_ATTR_ELEMENT);
            String maptoName = element.attributeValue(APPINFO_ATTR_MAPTO);
            if ((elementName != null) && (maptoName != null)) {
                // add the element mapping 
                addMapping(contentDefinition, elementName, maptoName);
            }
        }
    }

    /**
     * Initializes the folder containing the model file(s) for this content handler.<p>
     * 
     * @param root the "modelfolder" element from the appinfo node of the XML content definition
     * @param contentDefinition the content definition the model folder belongs to
     * @throws CmsXmlException if something goes wrong
     */
    protected void initModelFolder(Element root, CmsXmlContentDefinition contentDefinition) throws CmsXmlException {

        String master = root.attributeValue(APPINFO_ATTR_URI);
        if (master == null) {
            throw new CmsXmlException(Messages.get().container(
                Messages.ERR_XMLCONTENT_MISSING_MODELFOLDER_URI_2,
                root.getName(),
                contentDefinition.getSchemaLocation()));
        }
        m_modelFolder = master;
    }

    /**
     * Initializes the preview location for this content handler.<p>
     * 
     * @param root the "preview" element from the appinfo node of the XML content definition
     * @param contentDefinition the content definition the validation rules belong to
     * @throws CmsXmlException if something goes wrong
     */
    protected void initPreview(Element root, CmsXmlContentDefinition contentDefinition) throws CmsXmlException {

        String preview = root.attributeValue(APPINFO_ATTR_URI);
        if (preview == null) {
            throw new CmsXmlException(Messages.get().container(
                Messages.ERR_XMLCONTENT_MISSING_PREVIEW_URI_2,
                root.getName(),
                contentDefinition.getSchemaLocation()));
        }
        m_previewLocation = preview;
    }

    /**
     * Initializes the relation configuration for this content handler.<p>
     * 
     * OpenCms performs link checks for all OPTIONAL links defined in XML content values of type 
     * OpenCmsVfsFile. However, for most projects in the real world a more fine-grained control 
     * over the link check process is required. For these cases, individual relation behaviour can 
     * be defined for the appinfo node.<p>
     * 
     * Additional here can be defined an optional type for the relations, for instance.<p>
     * 
     * @param root the "relations" element from the appinfo node of the XML content definition
     * @param contentDefinition the content definition the check rules belong to
     * 
     * @throws CmsXmlException if something goes wrong
     */
    protected void initRelations(Element root, CmsXmlContentDefinition contentDefinition) throws CmsXmlException {

        Iterator i = root.elementIterator(APPINFO_RELATION);
        while (i.hasNext()) {
            // iterate all "checkrule" elements in the "checkrule" node
            Element element = (Element)i.next();
            String elementName = element.attributeValue(APPINFO_ATTR_ELEMENT);
            String invalidate = element.attributeValue(APPINFO_ATTR_INVALIDATE);
            if (invalidate != null) {
                invalidate = invalidate.toUpperCase();
            }
            String type = element.attributeValue(APPINFO_ATTR_TYPE);
            if (type != null) {
                type = type.toLowerCase();
            }
            if (elementName != null) {
                // add a check rule for the element
                addCheckRule(contentDefinition, elementName, invalidate, type);
            }
        }
    }

    /**
     * Initializes the resource bundle to use for localized messages in this content handler.<p>
     * 
     * @param root the "resourcebundle" element from the appinfo node of the XML content definition
     * @param contentDefinition the content definition the validation rules belong to
     * 
     * @throws CmsXmlException if something goes wrong
     */
    protected void initResourceBundle(Element root, CmsXmlContentDefinition contentDefinition) throws CmsXmlException {

        String name = root.attributeValue(APPINFO_ATTR_NAME);
        if (name == null) {
            throw new CmsXmlException(Messages.get().container(
                Messages.ERR_XMLCONTENT_MISSING_RESOURCE_BUNDLE_NAME_2,
                root.getName(),
                contentDefinition.getSchemaLocation()));
        }
        m_messageBundleName = name;
    }

    /**
     * Initializes the validation rules this content handler.<p>
     * 
     * OpenCms always performs XML schema validation for all XML contents. However,
     * for most projects in the real world a more fine-grained control over the validation process is
     * required. For these cases, individual validation rules can be defined for the appinfo node.<p>
     * 
     * @param root the "validationrules" element from the appinfo node of the XML content definition
     * @param contentDefinition the content definition the validation rules belong to
     * 
     * @throws CmsXmlException if something goes wrong
     */
    protected void initValidationRules(Element root, CmsXmlContentDefinition contentDefinition) throws CmsXmlException {

        List elements = new ArrayList(root.elements(APPINFO_RULE));
        elements.addAll(root.elements(APPINFO_VALIDATIONRULE));
        Iterator i = elements.iterator();
        while (i.hasNext()) {
            // iterate all "rule" or "validationrule" elements in the "validationrules" node
            Element element = (Element)i.next();
            String elementName = element.attributeValue(APPINFO_ATTR_ELEMENT);
            String regex = element.attributeValue(APPINFO_ATTR_REGEX);
            String type = element.attributeValue(APPINFO_ATTR_TYPE);
            if (type != null) {
                type = type.toLowerCase();
            }
            String message = element.attributeValue(APPINFO_ATTR_MESSAGE);
            if ((elementName != null) && (regex != null)) {
                // add a validation rule for the element
                addValidationRule(
                    contentDefinition,
                    elementName,
                    regex,
                    message,
                    APPINFO_ATTR_TYPE_WARNING.equals(type));
            }
        }
    }

    /**
     * Returns the is-invalidate-parent flag for the given xpath.<p>
     * 
     * @param xpath the path to get the check rule for
     * 
     * @return the configured is-invalidate-parent flag for the given xpath
     */
    protected boolean isInvalidateParent(String xpath) {

        if (!CmsXmlUtils.isDeepXpath(xpath)) {
            return false;
        }
        Boolean isInvalidateParent = null;
        // look up the default from the configured mappings
        isInvalidateParent = (Boolean)m_relations.get(xpath);
        if (isInvalidateParent == null) {
            // no value found, try default xpath
            String path = CmsXmlUtils.removeXpath(xpath);
            // look up the default value again without indexes
            isInvalidateParent = (Boolean)m_relations.get(path);
        }
        if (isInvalidateParent == null) {
            return false;
        }
        return isInvalidateParent.booleanValue();
    }

    /**
     * Returns the localized resource string for a given message key according to the configured resource bundle
     * of this content handler.<p>
     * 
     * If the key was not found in the configuredd bundle, or no bundle is configured for this 
     * content handler, the return value is
     * <code>"??? " + keyName + " ???"</code>.<p>
     * 
     * @param keyName the key for the desired string 
     * @param locale the locale to get the key from
     * 
     * @return the resource string for the given key 
     * 
     * @see CmsMessages#formatUnknownKey(String)
     * @see CmsMessages#isUnknownKey(String)
     */
    protected String key(String keyName, Locale locale) {

        CmsMessages messages = getMessages(locale);
        if (messages != null) {
            return messages.key(keyName);
        }
        return CmsMessages.formatUnknownKey(keyName);
    }

    /**
     * Removes property values on resources for non-existing, optional elements.<p>
     * 
     * @param cms the current users OpenCms context
     * @param content the XML content to remove the property values for
     * 
     * @throws CmsException in case of read/write errors accessing the OpenCms VFS
     */
    protected void removeEmptyMappings(CmsObject cms, CmsXmlContent content) throws CmsException {

        String rootPath = null;
        List siblings = null;

        Iterator mappings = m_elementMappings.entrySet().iterator();
        while (mappings.hasNext()) {
            Map.Entry e = (Map.Entry)mappings.next();
            String path = e.getKey().toString();
            String mapping = e.getValue().toString();
            if (mapping.startsWith(MAPTO_PROPERTY_LIST) || mapping.startsWith(MAPTO_PROPERTY)) {

                // get root path of the file
                if (rootPath == null) {
                    rootPath = content.getFile().getRootPath();
                }

                String storedSiteRoot = cms.getRequestContext().getSiteRoot();
                try {
                    // try / catch to ensure site root is always restored
                    cms.getRequestContext().setSiteRoot("/");

                    // read all siblings of the file
                    if (siblings == null) {
                        siblings = cms.readSiblings(rootPath, CmsResourceFilter.IGNORE_EXPIRATION);
                    }

                    for (int i = 0; i < siblings.size(); i++) {

                        // get sibline filename and locale
                        String filename = ((CmsResource)siblings.get(i)).getRootPath();
                        Locale locale = OpenCms.getLocaleManager().getDefaultLocale(cms, filename);

                        if (!content.hasLocale(locale)) {
                            // only remove property if the locale fits
                            continue;
                        }
                        if (content.hasValue(path, locale)) {
                            // value is available, property must be kept
                            continue;
                        }

                        String property;
                        if (mapping.startsWith(MAPTO_PROPERTY_LIST)) {
                            // this is a property list mapping
                            property = mapping.substring(MAPTO_PROPERTY_LIST.length());
                        } else {
                            // this is a property mapping
                            property = mapping.substring(MAPTO_PROPERTY.length());
                        }
                        // delete the property value for the not existing node
                        cms.writePropertyObject(filename, new CmsProperty(property, CmsProperty.DELETE_VALUE, null));
                    }

                } finally {
                    // restore the saved site root
                    cms.getRequestContext().setSiteRoot(storedSiteRoot);
                }
            }
        }
    }

    /**
     * Validates if the given <code>appinfo</code> element node from the XML content definition schema
     * is valid according the the capabilities of this content handler.<p> 
     * 
     * @param appinfoElement the <code>appinfo</code> element node to validate
     *  
     * @throws CmsXmlException in case the element validation fails
     */
    protected void validateAppinfoElement(Element appinfoElement) throws CmsXmlException {

        // create a document to validate
        Document doc = DocumentHelper.createDocument();
        Element root = doc.addElement(APPINFO_APPINFO);
        // attach the default appinfo schema
        root.add(I_CmsXmlSchemaType.XSI_NAMESPACE);
        root.addAttribute(I_CmsXmlSchemaType.XSI_NAMESPACE_ATTRIBUTE_NO_SCHEMA_LOCATION, APPINFO_SCHEMA_SYSTEM_ID);
        // append the content from the appinfo node in the content definition 
        root.appendContent(appinfoElement);
        // now validate the document with the default appinfo schema
        CmsXmlUtils.validateXmlStructure(doc, CmsEncoder.ENCODING_UTF_8, new CmsXmlEntityResolver(null));
    }

    /**
     * Validates the given rules against the given value.<p> 
     * 
     * @param cms the current users OpenCms context
     * @param value the value to validate
     * @param errorHandler the error handler to use in case errors or warnings are detected
     * 
     * @return if a broken link has been found
     */
    protected boolean validateLink(CmsObject cms, I_CmsXmlContentValue value, CmsXmlContentErrorHandler errorHandler) {

        // if there is a value of type file ref
        if ((value == null) || (!(value instanceof CmsXmlVfsFileValue) && !(value instanceof CmsXmlVarLinkValue))) {
            return false;
        }
        // if the value has a link (this will automatically fix, for instance, the path of moved resources)
        CmsLink link = null;
        if (value instanceof CmsXmlVfsFileValue) {
            link = ((CmsXmlVfsFileValue)value).getLink(cms);
        } else if (value instanceof CmsXmlVarLinkValue) {
            link = ((CmsXmlVarLinkValue)value).getLink(cms);
        }
        if ((link == null) || !link.isInternal()) {
            return false;
        }
        String siteRoot = CmsSiteManager.getSiteRoot(link.getTarget());
        String oldSite = cms.getRequestContext().getSiteRoot();
        try {
            if (siteRoot != null) {
                cms.getRequestContext().setSiteRoot(siteRoot);
            }
            String resName = cms.getRequestContext().removeSiteRoot(link.getTarget());
            // validate the link for error
            CmsResource res = cms.readResource(resName, CmsResourceFilter.IGNORE_EXPIRATION);

            // check the time range 
            if (res != null) {
                long time = System.currentTimeMillis();
                if (!res.isReleased(time)) {
                    if (errorHandler != null) {
                        // generate warning message
                        errorHandler.addWarning(value, Messages.get().getBundle(value.getLocale()).key(
                            Messages.GUI_XMLCONTENT_CHECK_WARNING_NOT_RELEASED_0));
                    }
                    return true;
                } else if (res.isExpired(time)) {
                    if (errorHandler != null) {
                        // generate warning message
                        errorHandler.addWarning(value, Messages.get().getBundle(value.getLocale()).key(
                            Messages.GUI_XMLCONTENT_CHECK_WARNING_EXPIRED_0));
                    }
                    return true;
                }
            }
        } catch (CmsException e) {
            if (errorHandler != null) {
                // generate error message
                errorHandler.addError(value, Messages.get().getBundle(value.getLocale()).key(
                    Messages.GUI_XMLCONTENT_CHECK_ERROR_0));
            }
            return true;
        } finally {
            cms.getRequestContext().setSiteRoot(oldSite);
        }
        return false;
    }

    /**
     * Validates the given rules against the given value.<p> 
     * 
     * @param cms the current users OpenCms context
     * @param value the value to validate
     * @param errorHandler the error handler to use in case errors or warnings are detected
     * @param rules the rules to validate the value against
     * @param isWarning if true, this validation should be stored as a warning, otherwise as an error
     * 
     * @return the updated error handler
     */
    protected CmsXmlContentErrorHandler validateValue(
        CmsObject cms,
        I_CmsXmlContentValue value,
        CmsXmlContentErrorHandler errorHandler,
        Map rules,
        boolean isWarning) {

        if (validateLink(cms, value, errorHandler)) {
            return errorHandler;
        }

        String valueStr;
        try {
            valueStr = value.getStringValue(cms);
        } catch (Exception e) {
            // if the value can not be accessed it's useless to continue
            errorHandler.addError(value, e.getMessage());
            return errorHandler;
        }

        String regex = (String)rules.get(value.getName());
        if (regex == null) {
            // no customized rule, check default XML schema validation rules
            return validateValue(cms, value, valueStr, errorHandler, isWarning);
        }

        boolean matchResult = true;
        if (regex.charAt(0) == '!') {
            // negate the pattern
            matchResult = false;
            regex = regex.substring(1);
        }

        String matchValue = valueStr;
        if (matchValue == null) {
            // set match value to empty String to avoid exceptions in pattern matcher
            matchValue = "";
        }

        // use the custom validation pattern
        if (matchResult != Pattern.matches(regex, matchValue)) {
            // generate the message
            String message = getValidationMessage(cms, value, regex, valueStr, matchResult, isWarning);
            if (isWarning) {
                errorHandler.addWarning(value, message);
            } else {
                errorHandler.addError(value, message);
                // if an error was found, the default XML schema validation is not applied
                return errorHandler;
            }
        }

        // no error found, check default XML schema validation rules
        return validateValue(cms, value, valueStr, errorHandler, isWarning);
    }

    /**
     * Checks the default XML schema vaildation rules.<p>
     * 
     * These rules should only be tested if this is not a test for warnings.<p>
     * 
     * @param cms the current users OpenCms context
     * @param value the value to validate
     * @param valueStr the string value of the given value
     * @param errorHandler the error handler to use in case errors or warnings are detected
     * @param isWarning if true, this validation should be stored as a warning, otherwise as an error
     * 
     * @return the updated error handler
     */
    protected CmsXmlContentErrorHandler validateValue(
        CmsObject cms,
        I_CmsXmlContentValue value,
        String valueStr,
        CmsXmlContentErrorHandler errorHandler,
        boolean isWarning) {

        if (isWarning) {
            // default schema validation only applies to errors
            return errorHandler;
        }

        if (!value.validateValue(valueStr)) {
            // value is not valid, add an error to the handler
            String message = getValidationMessage(cms, value, value.getTypeName(), valueStr, true, false);
            errorHandler.addError(value, message);
        }

        return errorHandler;
    }
}