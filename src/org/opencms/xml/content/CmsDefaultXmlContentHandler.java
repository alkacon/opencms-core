/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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
import org.opencms.db.log.CmsLogEntry;
import org.opencms.file.CmsDataAccessException;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsListResourceBundle;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.i18n.CmsMessages;
import org.opencms.i18n.CmsMultiMessages;
import org.opencms.i18n.CmsResourceBundleLoader;
import org.opencms.loader.I_CmsFileNameGenerator;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsCategory;
import org.opencms.relations.CmsCategoryService;
import org.opencms.relations.CmsLink;
import org.opencms.relations.CmsRelationType;
import org.opencms.security.CmsAccessControlEntry;
import org.opencms.security.CmsPrincipal;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.site.CmsSite;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsHtmlConverter;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.CmsCategoryWidget;
import org.opencms.widgets.CmsDisplayWidget;
import org.opencms.widgets.I_CmsWidget;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.editors.CmsXmlContentWidgetVisitor;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.CmsXmlEntityResolver;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.CmsXmlGenericWrapper;
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.containerpage.CmsFormatterBean;
import org.opencms.xml.containerpage.CmsFormatterConfiguration;
import org.opencms.xml.types.CmsXmlNestedContentDefinition;
import org.opencms.xml.types.CmsXmlVarLinkValue;
import org.opencms.xml.types.CmsXmlVfsFileValue;
import org.opencms.xml.types.I_CmsXmlContentValue;
import org.opencms.xml.types.I_CmsXmlSchemaType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

/**
 * Default implementation for the XML content handler, will be used by all XML contents that do not
 * provide their own handler.<p>
 * 
 * @since 6.0.0 
 */
public class CmsDefaultXmlContentHandler implements I_CmsXmlContentHandler {

    /** Constant for the "appinfo" element name itself. */
    public static final String APPINFO_APPINFO = "appinfo";

    /** Constant for the "collapse" appinfo attribute name. */
    public static final String APPINFO_ATTR_COLLAPSE = "collapse";

    /** Constant for the "configuration" appinfo attribute name. */
    public static final String APPINFO_ATTR_CONFIGURATION = "configuration";

    /** Constant for the "default" appinfo attribute name. */
    public static final String APPINFO_ATTR_DEFAULT = "default";

    /** Constant for the "description" appinfo attribute name. */
    public static final String APPINFO_ATTR_DESCRIPTION = "description";

    /** Constant for the "element" appinfo attribute name. */
    public static final String APPINFO_ATTR_ELEMENT = "element";

    /** Constant for the "error" appinfo attribute name. */
    public static final String APPINFO_ATTR_ERROR = "error";

    /** Constant for the "invalidate" appinfo attribute name. */
    public static final String APPINFO_ATTR_INVALIDATE = "invalidate";

    /** Constant for the "key" appinfo attribute name. */
    public static final String APPINFO_ATTR_KEY = "key";

    /** Constant for the "locale" appinfo attribute name. */
    public static final String APPINFO_ATTR_LOCALE = "locale";

    /** Constant for the "mapto" appinfo attribute name. */
    public static final String APPINFO_ATTR_MAPTO = "mapto";

    /** Constant for the "maxwidth" appinfo attribute name. */
    public static final String APPINFO_ATTR_MAXWIDTH = "maxwidth";

    /** Constant for the "message" appinfo attribute name. */
    public static final String APPINFO_ATTR_MESSAGE = "message";

    /** Constant for the "minwidth" appinfo attribute name. */
    public static final String APPINFO_ATTR_MINWIDTH = "minwidth";

    /** Constant for the "name" appinfo attribute name. */
    public static final String APPINFO_ATTR_NAME = "name";

    /** Constant for the "nice-name" appinfo attribute name. */
    public static final String APPINFO_ATTR_NICE_NAME = "nice-name";

    /** Constant for the "preview" appinfo attribute name. */
    public static final String APPINFO_ATTR_PREVIEW = "preview";

    /** Constant for the "regex" appinfo attribute name. */
    public static final String APPINFO_ATTR_REGEX = "regex";

    /** Constant for the "rule-regex" appinfo attribute name. */
    public static final String APPINFO_ATTR_RULE_REGEX = "rule-regex";

    /** Constant for the "rule-type" appinfo attribute name. */
    public static final String APPINFO_ATTR_RULE_TYPE = "rule-type";

    /** Constant for the "searchcontent" appinfo attribute name. */
    public static final String APPINFO_ATTR_SEARCHCONTENT = "searchcontent";

    /** Constant for the "select-inherit" appinfo attribute name. */
    public static final String APPINFO_ATTR_SELECT_INHERIT = "select-inherit";

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

    /** Constant for the "useall" appinfo attribute name. */
    public static final String APPINFO_ATTR_USEALL = "useall";

    /** Constant for the "value" appinfo attribute name. */
    public static final String APPINFO_ATTR_VALUE = "value";

    /** Constant for the "widget" appinfo attribute name. */
    public static final String APPINFO_ATTR_WIDGET = "widget";

    /** Constant for the "widget-config" appinfo attribute name. */
    public static final String APPINFO_ATTR_WIDGET_CONFIG = "widget-config";

    /** Constant for formatter include resource type 'CSS'. */
    public static final String APPINFO_ATTRIBUTE_TYPE_CSS = "css";

    /** Constant for formatter include resource type 'JAVASCRIPT'. */
    public static final String APPINFO_ATTRIBUTE_TYPE_JAVASCRIPT = "javascript";

    /** Constant for the "bundle" appinfo element name. */
    public static final String APPINFO_BUNDLE = "bundle";

    /** Constant for the "default" appinfo element name. */
    public static final String APPINFO_DEFAULT = "default";

    /** Constant for the "defaults" appinfo element name. */
    public static final String APPINFO_DEFAULTS = "defaults";

    /** Constant for the "formatter" appinfo element name. */
    public static final String APPINFO_FORMATTER = "formatter";

    /** Constant for the "formatters" appinfo element name. */
    public static final String APPINFO_FORMATTERS = "formatters";

    /** Constant for the "headinclude" appinfo element name. */
    public static final String APPINFO_HEAD_INCLUDE = "headinclude";

    /** Constant for the "headincludes" appinfo element name. */
    public static final String APPINFO_HEAD_INCLUDES = "headincludes";

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

    /** Constant for the "propertybundle" appinfo element name. */
    public static final String APPINFO_PROPERTYBUNDLE = "propertybundle";

    /** Constant for the "relation" appinfo element name. */
    public static final String APPINFO_RELATION = "relation";

    /** Constant for the "relations" appinfo element name. */
    public static final String APPINFO_RELATIONS = "relations";

    /** Constant for the "resource" appinfo element name. */
    public static final String APPINFO_RESOURCE = "resource";

    /** Constant for the "resourcebundle" appinfo element name. */
    public static final String APPINFO_RESOURCEBUNDLE = "resourcebundle";

    /** Constant for the "resourcebundles" appinfo element name. */
    public static final String APPINFO_RESOURCEBUNDLES = "resourcebundles";

    /** Constant for the "rule" appinfo element name. */
    public static final String APPINFO_RULE = "rule";

    /** The file where the default appinfo schema is located. */
    public static final String APPINFO_SCHEMA_FILE = "org/opencms/xml/content/DefaultAppinfo.xsd";

    /** The file where the default appinfo schema types are located. */
    public static final String APPINFO_SCHEMA_FILE_TYPES = "org/opencms/xml/content/DefaultAppinfoTypes.xsd";

    /** The XML system id for the default appinfo schema types. */
    public static final String APPINFO_SCHEMA_SYSTEM_ID = CmsConfigurationManager.DEFAULT_DTD_PREFIX
        + APPINFO_SCHEMA_FILE;

    /** The XML system id for the default appinfo schema types. */
    public static final String APPINFO_SCHEMA_TYPES_SYSTEM_ID = CmsConfigurationManager.DEFAULT_DTD_PREFIX
        + APPINFO_SCHEMA_FILE_TYPES;

    /** Constant for the "searchsetting" appinfo element name. */
    public static final String APPINFO_SEARCHSETTING = "searchsetting";

    /** Constant for the "searchsettings" appinfo element name. */
    public static final String APPINFO_SEARCHSETTINGS = "searchsettings";

    /** Constant for the "setting" appinfo element name. */
    public static final String APPINFO_SETTING = "setting";

    /** Constant for the "settings" appinfo element name. */
    public static final String APPINFO_SETTINGS = "settings";

    /** Constant for the "tab" appinfo element name. */
    public static final String APPINFO_TAB = "tab";

    /** Constant for the "tabs" appinfo element name. */
    public static final String APPINFO_TABS = "tabs";

    /** Constant for the "validationrule" appinfo element name. */
    public static final String APPINFO_VALIDATIONRULE = "validationrule";

    /** Constant for the "validationrules" appinfo element name. */
    public static final String APPINFO_VALIDATIONRULES = "validationrules";

    /** Constant for the "xmlbundle" appinfo element name. */
    public static final String APPINFO_XMLBUNDLE = "xmlbundle";

    /** Constant for head include type attribute: CSS. */
    public static final String ATTRIBUTE_INCLUDE_TYPE_CSS = "css";

    /** Constant for head include type attribute: java-script. */
    public static final String ATTRIBUTE_INCLUDE_TYPE_JAVASCRIPT = "javascript";

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

    /** The attribute name for the "prefer folder" option for properties. */
    private static final String APPINFO_ATTR_PREFERFOLDER = "PreferFolder";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsDefaultXmlContentHandler.class);

    /** The principal list separator. */
    private static final String PRINCIPAL_LIST_SEPARATOR = ",";

    /** The configuration values for the element widgets (as defined in the annotations). */
    protected Map<String, String> m_configurationValues;

    /** The CSS resources to include into the html-page head. */
    protected Set<String> m_cssHeadIncludes;

    /** The default values for the elements (as defined in the annotations). */
    protected Map<String, String> m_defaultValues;

    /** The element mappings (as defined in the annotations). */
    protected Map<String, List<String>> m_elementMappings;

    /** The widgets used for the elements (as defined in the annotations). */
    protected Map<String, I_CmsWidget> m_elementWidgets;

    /** The formatter configuration. */
    protected CmsFormatterConfiguration m_formatterConfiguration;

    /** The list of formatters from the XSD. */
    protected List<CmsFormatterBean> m_formatters;

    /** The java-script resources to include into the html-page head. */
    protected Set<String> m_jsHeadIncludes;

    /** The resource bundle name to be used for localization of this content handler. */
    protected List<String> m_messageBundleNames;

    /** The folder containing the model file(s) for the content. */
    protected String m_modelFolder;

    /** The preview location (as defined in the annotations). */
    protected String m_previewLocation;

    /** The relation check rules. */
    protected Map<String, Boolean> m_relationChecks;

    /** The relation check rules. */
    protected Map<String, CmsRelationType> m_relations;

    /** The search settings. */
    protected Map<String, Boolean> m_searchSettings;

    /** The configured settings for the formatters (as defined in the annotations). */
    protected Map<String, CmsXmlContentProperty> m_settings;

    /** The configured tabs. */
    protected List<CmsXmlContentTab> m_tabs;

    /** The list of mappings to the "Title" property. */
    protected List<String> m_titleMappings;

    /** The messages for the error validation rules. */
    protected Map<String, String> m_validationErrorMessages;

    /** The validation rules that cause an error (as defined in the annotations). */
    protected Map<String, String> m_validationErrorRules;

    /** The messages for the warning validation rules. */
    protected Map<String, String> m_validationWarningMessages;

    /** The validation rules that cause a warning (as defined in the annotations). */
    protected Map<String, String> m_validationWarningRules;

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

        // the schema definition is located in 2 separates file for easier editing
        // 2 files are required in case an extended schema want to use the default definitions,
        // but with an extended "appinfo" node 
        byte[] appinfoSchemaTypes;
        try {
            // first read the default types
            appinfoSchemaTypes = CmsFileUtil.readFile(APPINFO_SCHEMA_FILE_TYPES);
        } catch (Exception e) {
            throw new CmsRuntimeException(Messages.get().container(
                org.opencms.xml.types.Messages.ERR_XMLCONTENT_LOAD_SCHEMA_1,
                APPINFO_SCHEMA_FILE_TYPES), e);
        }
        CmsXmlEntityResolver.cacheSystemId(APPINFO_SCHEMA_TYPES_SYSTEM_ID, appinfoSchemaTypes);
        byte[] appinfoSchema;
        try {
            // now read the default base schema
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
        return m_configurationValues.get(elementName);

    }

    /**
     * @see org.opencms.xml.content.I_CmsXmlContentHandler#getCSSHeadIncludes()
     */
    public Set<String> getCSSHeadIncludes() {

        return Collections.unmodifiableSet(m_cssHeadIncludes);
    }

    /**
     * @see org.opencms.xml.content.I_CmsXmlContentHandler#getCSSHeadIncludes(org.opencms.file.CmsObject, org.opencms.file.CmsResource)
     */
    @SuppressWarnings("unused")
    public Set<String> getCSSHeadIncludes(CmsObject cms, CmsResource resource) throws CmsException {

        return getCSSHeadIncludes();
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
            defaultValue = m_defaultValues.get(xpath);
            if (defaultValue == null) {
                // no value found, try default xpath
                xpath = CmsXmlUtils.removeXpath(xpath);
                xpath = CmsXmlUtils.createXpath(xpath, 1);
                // look up the default value again with default index of 1 in all path elements
                defaultValue = m_defaultValues.get(xpath);
            }
        }
        if (defaultValue != null) {
            CmsObject newCms = cms;
            try {
                // switch the current URI to the XML document resource so that properties can be read
                CmsResource file = value.getDocument().getFile();
                CmsSite site = OpenCms.getSiteManager().getSiteForRootPath(file.getRootPath());
                if (site != null) {
                    newCms = OpenCms.initCmsObject(cms);
                    newCms.getRequestContext().setSiteRoot(site.getSiteRoot());
                    newCms.getRequestContext().setUri(newCms.getSitePath(file));
                }
            } catch (Exception e) {
                // on any error just use the default input OpenCms context
            }
            // return the default value with processed macros
            CmsMacroResolver resolver = CmsMacroResolver.newInstance().setCmsObject(newCms).setMessages(
                getMessages(locale));
            return resolver.resolveMacros(defaultValue);
        }
        // no default value is available
        return null;
    }

    /**
     * @see org.opencms.xml.content.I_CmsXmlContentHandler#getFormatterConfiguration(org.opencms.file.CmsObject, org.opencms.file.CmsResource)
     */
    public CmsFormatterConfiguration getFormatterConfiguration(CmsObject cms, CmsResource resource) {

        if (m_formatterConfiguration == null) {
            m_formatterConfiguration = CmsFormatterConfiguration.create(cms, m_formatters);
        }
        return m_formatterConfiguration;
    }

    /**
     * @see org.opencms.xml.content.I_CmsXmlContentHandler#getJSHeadIncludes()
     */
    public Set<String> getJSHeadIncludes() {

        return Collections.<String> unmodifiableSet(m_jsHeadIncludes);
    }

    /**
     * @see org.opencms.xml.content.I_CmsXmlContentHandler#getJSHeadIncludes(org.opencms.file.CmsObject, org.opencms.file.CmsResource)
     */
    @SuppressWarnings("unused")
    public Set<String> getJSHeadIncludes(CmsObject cms, CmsResource resource) throws CmsException {

        return getJSHeadIncludes();
    }

    /**
     * Returns the all mappings defined for the given element xpath.<p>
     * 
     * @since 7.0.2
     * 
     * @param elementName the element xpath to look up the mapping for
     * 
     * @return the mapping defined for the given element xpath
     */
    public List<String> getMappings(String elementName) {

        return m_elementMappings.get(elementName);
    }

    /**
     * @see org.opencms.xml.content.I_CmsXmlContentHandler#getMessages(java.util.Locale)
     */
    public CmsMessages getMessages(Locale locale) {

        CmsMessages result = null;
        if ((m_messageBundleNames != null) && !m_messageBundleNames.isEmpty()) {
            // a message bundle was initialized
            if (m_messageBundleNames.size() == 1) {
                // single message bundle
                result = new CmsMessages(m_messageBundleNames.get(0), locale);
            } else {
                // multiple message bundle
                CmsMultiMessages multiMessages = new CmsMultiMessages(locale);
                for (String messageBundleName : m_messageBundleNames) {
                    multiMessages.addMessages(new CmsMessages(messageBundleName, locale));
                }
                result = multiMessages;
            }
        }
        return result;
    }

    /**
     * @see org.opencms.xml.content.I_CmsXmlContentHandler#getModelFolder()
     */
    public String getModelFolder() {

        return m_modelFolder;
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
    @Deprecated
    public CmsRelationType getRelationType(I_CmsXmlContentValue value) {

        if (value == null) {
            return CmsRelationType.XML_WEAK;
        }
        return getRelationType(value.getPath());
    }

    /**
     * @see I_CmsXmlContentHandler#getRelationType(String)
     */
    public CmsRelationType getRelationType(String xpath) {

        if (xpath == null) {
            return CmsRelationType.XML_WEAK;
        }
        CmsRelationType relationType = null;
        // look up the default from the configured mappings
        relationType = m_relations.get(xpath);
        if (relationType == null) {
            // no value found, try default xpath
            String path = CmsXmlUtils.removeXpathIndex(xpath);
            // look up the default value again without indexes
            relationType = m_relations.get(path);
        }
        if (relationType == null) {
            // no value found, try the last simple type path
            String path = CmsXmlUtils.getLastXpathElement(xpath);
            // look up the default value again for the last simple type
            relationType = m_relations.get(path);
        }
        if (relationType == null) {
            return CmsRelationType.XML_WEAK;
        }
        return relationType;
    }

    /**
     * @see org.opencms.xml.content.I_CmsXmlContentHandler#getSettings(org.opencms.file.CmsObject, org.opencms.file.CmsResource)
     */
    public Map<String, CmsXmlContentProperty> getSettings(CmsObject cms, CmsResource resource) {

        return Collections.unmodifiableMap(m_settings);
    }

    /**
     * @see org.opencms.xml.content.I_CmsXmlContentHandler#getTabs()
     */
    public List<CmsXmlContentTab> getTabs() {

        return Collections.unmodifiableList(m_tabs);
    }

    /**
     * @see org.opencms.xml.content.I_CmsXmlContentHandler#getTitleMapping(org.opencms.file.CmsObject, org.opencms.xml.content.CmsXmlContent, java.util.Locale)
     */
    public String getTitleMapping(CmsObject cms, CmsXmlContent document, Locale locale) {

        String result = null;
        if (m_titleMappings.size() > 0) {
            // a title mapping is available
            String xpath = m_titleMappings.get(0);
            // currently just use the first mapping found, unsure if multiple "Title" mappings would make sense anyway
            result = document.getStringValue(cms, xpath, locale);
        }
        return result;
    }

    /**
     * @see org.opencms.xml.content.I_CmsXmlContentHandler#getWidget(org.opencms.xml.types.I_CmsXmlContentValue)
     */
    public I_CmsWidget getWidget(I_CmsXmlContentValue value) {

        // try the specific widget settings first
        I_CmsWidget result = m_elementWidgets.get(value.getName());
        if (result == null) {
            // use default widget mappings
            result = OpenCms.getXmlContentTypeManager().getWidgetDefault(value.getTypeName());
        } else {
            result = result.newInstance();
        }
        // set the configuration value for this widget
        String configuration = getConfiguration(value);
        if (configuration == null) {
            // no individual configuration defined, try to get global default configuration
            configuration = OpenCms.getXmlContentTypeManager().getWidgetDefaultConfiguration(result);
        }
        result.setConfiguration(configuration);

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

            Iterator<Element> i = CmsXmlGenericWrapper.elementIterator(appInfoElement);
            while (i.hasNext()) {
                // iterate all elements in the appinfo node
                Element element = i.next();
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
                    initResourceBundle(element, contentDefinition, true);
                } else if (nodeName.equals(APPINFO_RESOURCEBUNDLES)) {
                    initResourceBundle(element, contentDefinition, false);
                } else if (nodeName.equals(APPINFO_SEARCHSETTINGS)) {
                    initSearchSettings(element, contentDefinition);
                } else if (nodeName.equals(APPINFO_TABS)) {
                    initTabs(element, contentDefinition);
                } else if (nodeName.equals(APPINFO_FORMATTERS)) {
                    initFormatters(element, contentDefinition);
                } else if (nodeName.equals(APPINFO_HEAD_INCLUDES)) {
                    initHeadIncludes(element, contentDefinition);
                } else if (nodeName.equals(APPINFO_SETTINGS)) {
                    initSettings(element, contentDefinition);
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
        Iterator<Locale> itLocales = document.getLocales().iterator();
        while (itLocales.hasNext()) {
            Locale locale = itLocales.next();
            List<String> removedNodes = new ArrayList<String>();
            // iterate the values
            Iterator<I_CmsXmlContentValue> itValues = document.getValues(locale).iterator();
            while (itValues.hasNext()) {
                I_CmsXmlContentValue value = itValues.next();
                String path = value.getPath();
                // check if this value has already been deleted by parent rules 
                boolean alreadyRemoved = false;
                Iterator<String> itRemNodes = removedNodes.iterator();
                while (itRemNodes.hasNext()) {
                    String remNode = itRemNodes.next();
                    if (path.startsWith(remNode)) {
                        alreadyRemoved = true;
                        break;
                    }
                }
                // only continue if not already removed and if a rule match
                if (alreadyRemoved
                    || ((m_relationChecks.get(path) == null) && (m_relationChecks.get(CmsXmlUtils.removeXpath(path)) == null))) {
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
     * @see org.opencms.xml.content.I_CmsXmlContentHandler#isSearchable(org.opencms.xml.types.I_CmsXmlContentValue)
     */
    public boolean isSearchable(I_CmsXmlContentValue value) {

        // check for name configured in the annotations
        Boolean anno = m_searchSettings.get(value.getName());
        // if no annotation has been found, use default for value
        return (anno == null) ? value.isSearchable() : anno.booleanValue();
    }

    /**
     * @see org.opencms.xml.content.I_CmsXmlContentHandler#prepareForUse(org.opencms.file.CmsObject, org.opencms.xml.content.CmsXmlContent)
     */
    public CmsXmlContent prepareForUse(CmsObject cms, CmsXmlContent content) {

        // NOOP, just return the unmodified content
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
        // validate the XML structure before writing the file if required                 
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
        // ensure all property or permission mappings of deleted optional values are removed
        removeEmptyMappings(cms, content);
        // write categories (if there is a category widget present)
        file = writeCategories(cms, file, content);
        // return the result
        return file;
    }

    /**
     * @see org.opencms.xml.content.I_CmsXmlContentHandler#resolveMapping(org.opencms.file.CmsObject, org.opencms.xml.content.CmsXmlContent, org.opencms.xml.types.I_CmsXmlContentValue)
     */
    public void resolveMapping(CmsObject cms, CmsXmlContent content, I_CmsXmlContentValue value) throws CmsException {

        if (!value.isSimpleType()) {
            // no mappings for a nested schema are possible
            // note that the sub-elements of the nested schema ARE mapped by the node visitor,
            // it's just the nested schema value itself that does not support mapping
            return;
        }

        // get the original VFS file from the content
        CmsFile file = content.getFile();
        if (file == null) {
            throw new CmsXmlException(Messages.get().container(Messages.ERR_XMLCONTENT_RESOLVE_FILE_NOT_FOUND_0));
        }

        // get the mappings for the element name        
        List<String> mappings = getMappings(value.getPath());
        if (mappings == null) {
            // nothing to do if we have no mappings at all
            return;
        }
        // create OpenCms user context initialized with "/" as site root to read all siblings
        CmsObject rootCms = OpenCms.initCmsObject(cms);
        Object logEntry = cms.getRequestContext().getAttribute(CmsLogEntry.ATTR_LOG_ENTRY);
        if (logEntry != null) {
            rootCms.getRequestContext().setAttribute(CmsLogEntry.ATTR_LOG_ENTRY, logEntry);
        }
        rootCms.getRequestContext().setSiteRoot("/");
        // read all siblings of the file
        List<CmsResource> siblings = rootCms.readSiblings(
            content.getFile().getRootPath(),
            CmsResourceFilter.IGNORE_EXPIRATION);

        Set<CmsResource> urlNameMappingResources = new HashSet<CmsResource>();
        boolean mapToUrlName = false;
        urlNameMappingResources.add(content.getFile());
        // since 7.0.2 multiple mappings are possible
        for (String mapping : mappings) {

            // for multiple language mappings, we need to ensure 
            // a) all siblings are handled
            // b) only the "right" locale is mapped to a sibling
            if (CmsStringUtil.isNotEmpty(mapping)) {
                for (int i = (siblings.size() - 1); i >= 0; i--) {
                    // get filename
                    String filename = (siblings.get(i)).getRootPath();
                    Locale locale = OpenCms.getLocaleManager().getDefaultLocale(rootCms, filename);
                    if (mapping.startsWith(MAPTO_URLNAME)) {
                        // should be written regardless of whether there is a sibling with the correct locale 
                        mapToUrlName = true;
                    }
                    if (!locale.equals(value.getLocale())) {
                        // only map property if the locale fits
                        continue;
                    }

                    // make sure the file is locked
                    CmsLock lock = rootCms.getLock(filename);
                    if (lock.isUnlocked()) {
                        rootCms.lockResource(filename);
                    } else if (!lock.isDirectlyOwnedInProjectBy(rootCms)) {
                        rootCms.changeLock(filename);
                    }

                    // get the string value of the current node
                    String stringValue = value.getStringValue(rootCms);
                    if (mapping.startsWith(MAPTO_PERMISSION) && (value.getIndex() == 0)) {

                        // map value to a permission
                        // example of a mapping: mapto="permission:GROUP:+r+v|GROUP.ALL_OTHERS:|GROUP.Projectmanagers:+r+v+w+c"

                        // get permission(s) to set
                        String permissionMappings = mapping.substring(MAPTO_PERMISSION.length());
                        String mainMapping = permissionMappings;
                        Map<String, String> permissionsToSet = new HashMap<String, String>();

                        // separate permission to set for element value from other permissions to set
                        int sepIndex = permissionMappings.indexOf('|');
                        if (sepIndex != -1) {
                            mainMapping = permissionMappings.substring(0, sepIndex);
                            permissionMappings = permissionMappings.substring(sepIndex + 1);
                            permissionsToSet = CmsStringUtil.splitAsMap(permissionMappings, "|", ":");
                        }

                        // determine principal type and permission string to set
                        String principalType = I_CmsPrincipal.PRINCIPAL_GROUP;
                        String permissionString = mainMapping;
                        sepIndex = mainMapping.indexOf(':');
                        if (sepIndex != -1) {
                            principalType = mainMapping.substring(0, sepIndex);
                            permissionString = mainMapping.substring(sepIndex + 1);
                        }
                        if (permissionString.toLowerCase().indexOf('o') == -1) {
                            permissionString += "+o";
                        }

                        // remove all existing permissions from the file
                        List<CmsAccessControlEntry> aces = rootCms.getAccessControlEntries(filename, false);
                        for (Iterator<CmsAccessControlEntry> j = aces.iterator(); j.hasNext();) {
                            CmsAccessControlEntry ace = j.next();
                            if (ace.getPrincipal().equals(CmsAccessControlEntry.PRINCIPAL_ALL_OTHERS_ID)) {
                                // remove the entry "All others", which has to be treated in a special way
                                rootCms.rmacc(
                                    filename,
                                    CmsAccessControlEntry.PRINCIPAL_ALL_OTHERS_NAME,
                                    CmsAccessControlEntry.PRINCIPAL_ALL_OTHERS_ID.toString());
                            } else {
                                // this is a group or user principal
                                I_CmsPrincipal principal = CmsPrincipal.readPrincipal(rootCms, ace.getPrincipal());
                                if (principal.isGroup()) {
                                    rootCms.rmacc(filename, I_CmsPrincipal.PRINCIPAL_GROUP, principal.getName());
                                } else if (principal.isUser()) {
                                    rootCms.rmacc(filename, I_CmsPrincipal.PRINCIPAL_USER, principal.getName());
                                }
                            }
                        }

                        // set additional permissions that are defined in mapping
                        for (Iterator<Map.Entry<String, String>> j = permissionsToSet.entrySet().iterator(); j.hasNext();) {
                            Map.Entry<String, String> entry = j.next();
                            sepIndex = entry.getKey().indexOf('.');
                            if (sepIndex != -1) {
                                String type = entry.getKey().substring(0, sepIndex);
                                String name = entry.getKey().substring(sepIndex + 1);
                                String permissions = entry.getValue();
                                if (permissions.toLowerCase().indexOf('o') == -1) {
                                    permissions += "+o";
                                }
                                try {
                                    rootCms.chacc(filename, type, name, permissions);
                                } catch (CmsException e) {
                                    // setting permission did not work
                                    LOG.error(e);
                                }
                            }
                        }

                        // set permission(s) using the element value(s)
                        // the set with all selected principals
                        TreeSet<String> allPrincipals = new TreeSet<String>();
                        String path = CmsXmlUtils.removeXpathIndex(value.getPath());
                        List<I_CmsXmlContentValue> values = content.getValues(path, locale);
                        Iterator<I_CmsXmlContentValue> j = values.iterator();
                        while (j.hasNext()) {
                            I_CmsXmlContentValue val = j.next();
                            String principalName = val.getStringValue(rootCms);
                            // the prinicipal name can be a principal list
                            List<String> principalNames = CmsStringUtil.splitAsList(
                                principalName,
                                PRINCIPAL_LIST_SEPARATOR);
                            // iterate over the principals
                            Iterator<String> iterPrincipals = principalNames.iterator();
                            while (iterPrincipals.hasNext()) {
                                // get the next principal
                                String principal = iterPrincipals.next();
                                allPrincipals.add(principal);
                            }
                        }
                        // iterate over the set with all principals and set the permissions
                        Iterator<String> iterAllPricinipals = allPrincipals.iterator();
                        while (iterAllPricinipals.hasNext()) {
                            // get the next principal
                            String principal = iterAllPricinipals.next();
                            rootCms.chacc(filename, principalType, principal, permissionString);
                        }
                        // special case: permissions are written only to one sibling, end loop
                        i = 0;
                    } else if (mapping.startsWith(MAPTO_PROPERTY_LIST) && (value.getIndex() == 0)) {

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
                        List<I_CmsXmlContentValue> values = content.getValues(path, locale);
                        Iterator<I_CmsXmlContentValue> j = values.iterator();
                        StringBuffer result = new StringBuffer(values.size() * 64);
                        while (j.hasNext()) {
                            I_CmsXmlContentValue val = j.next();
                            result.append(val.getStringValue(rootCms));
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
                        rootCms.writePropertyObject(filename, p);
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
                        rootCms.writePropertyObject(filename, p);
                        if (mapToShared) {
                            // special case: shared mappings must be written only to one sibling, end loop
                            i = 0;
                        }
                    } else if (mapping.startsWith(MAPTO_URLNAME)) {
                        // we write the actual mappings later 
                        urlNameMappingResources.add(siblings.get(i));
                    } else if (mapping.startsWith(MAPTO_ATTRIBUTE)) {

                        // this is an attribute mapping                        
                        String attribute = mapping.substring(MAPTO_ATTRIBUTE.length());
                        switch (ATTRIBUTES.indexOf(attribute)) {
                            case 0: // date released
                                long date = 0;
                                try {
                                    date = Long.valueOf(stringValue).longValue();
                                } catch (NumberFormatException e) {
                                    // ignore, value can be a macro
                                }
                                if (date == 0) {
                                    date = CmsResource.DATE_RELEASED_DEFAULT;
                                }
                                // set the sibling release date
                                rootCms.setDateReleased(filename, date, false);
                                // set current file release date
                                if (filename.equals(rootCms.getSitePath(file))) {
                                    file.setDateReleased(date);
                                }
                                break;
                            case 1: // date expired
                                date = 0;
                                try {
                                    date = Long.valueOf(stringValue).longValue();
                                } catch (NumberFormatException e) {
                                    // ignore, value can be a macro
                                }
                                if (date == 0) {
                                    date = CmsResource.DATE_EXPIRED_DEFAULT;
                                }
                                // set the sibling expired date
                                rootCms.setDateExpired(filename, date, false);
                                // set current file expired date
                                if (filename.equals(rootCms.getSitePath(file))) {
                                    file.setDateExpired(date);
                                }
                                break;
                            default:
                                // ignore invalid / other mappings                                
                        }
                    }
                }
            }
        }
        if (mapToUrlName) {
            // now actually write the URL name mappings 
            for (CmsResource resourceForUrlNameMapping : urlNameMappingResources) {
                if (!CmsResource.isTemporaryFileName(resourceForUrlNameMapping.getRootPath())) {
                    I_CmsFileNameGenerator nameGen = OpenCms.getResourceManager().getNameGenerator();
                    Iterator<String> nameSeq = nameGen.getUrlNameSequence(value.getStringValue(cms));
                    cms.writeUrlNameMapping(
                        nameSeq,
                        resourceForUrlNameMapping.getStructureId(),
                        value.getLocale().toString());
                }
            }
        }

        // make sure the original is locked
        CmsLock lock = rootCms.getLock(file);
        if (lock.isUnlocked()) {
            rootCms.lockResource(file.getRootPath());
        } else if (!lock.isExclusiveOwnedBy(rootCms.getRequestContext().getCurrentUser())) {
            rootCms.changeLock(file.getRootPath());
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
            // note that the sub-elements of the nested schema ARE validated by the node visitor,
            // it's just the nested schema value itself that does not support validation
            return errorHandler;
        }

        // validate the error rules
        errorHandler = validateValue(cms, value, errorHandler, m_validationErrorRules, false);
        // validate the warning rules
        errorHandler = validateValue(cms, value, errorHandler, m_validationWarningRules, true);
        // validate categories
        errorHandler = validateCategories(cms, value, errorHandler);
        // return the result
        return errorHandler;
    }

    /**
     * Adds a check rule for a specified element.<p> 
     * 
     * @param contentDefinition the XML content definition this XML content handler belongs to
     * @param elementName the element name to add the rule to 
     * @param invalidate <code>false</code>, to disable link check /
     *                   <code>true</code> or <code>node</code>, to invalidate just the single node if the link is broken /
     *                   <code>parent</code>, if this rule will invalidate the whole parent node in nested content
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
            m_relationChecks.put(elementName, invalidateParent);
        }
        CmsRelationType relationType = (type == null ? CmsRelationType.XML_WEAK : CmsRelationType.valueOfXml(type));
        m_relations.put(elementName, relationType);

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
                Messages.ERR_XMLCONTENT_INVALID_ELEM_DEFAULT_1,
                elementName));
        }
        // store mappings as xpath to allow better control about what is mapped
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
                && !m_relationChecks.containsKey(elementPath)
                && !m_relations.containsKey(elementPath)) {
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
            Iterator<String> itElems = nestedContentDefinition.getSchemaTypes().iterator();
            while (itElems.hasNext()) {
                String element = itElems.next();
                String path = (schemaType != null) ? CmsXmlUtils.concatXpath(elementPath, element) : element;
                I_CmsXmlSchemaType nestedSchema = nestedContentDefinition.getSchemaType(element);
                if ((schemaType == null) || !nestedSchema.equals(schemaType)) {
                    addDefaultCheckRules(rootContentDefinition, nestedSchema, path);
                }
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

        // store mappings as xpath to allow better control about what is mapped
        String xpath = CmsXmlUtils.createXpath(elementName, 1);
        // since 7.0.2 multiple mappings are possible, so the mappings are stored in an array
        List<String> values = m_elementMappings.get(xpath);
        if (values == null) {
            // there should not really be THAT much multiple mappings per value...
            values = new ArrayList<String>(4);
            m_elementMappings.put(xpath, values);
        }
        values.add(mapping);
        if (mapping.startsWith(MAPTO_PROPERTY) && mapping.endsWith(":" + CmsPropertyDefinition.PROPERTY_TITLE)) {
            // this is a title mapping
            m_titleMappings.add(xpath);
        }
    }

    /**
     * Adds a search setting for an element.<p>
     * 
     * @param contentDefinition the XML content definition this XML content handler belongs to
     * @param elementName the element name to map
     * @param value the search setting value to store
     * 
     * @throws CmsXmlException in case an unknown element name is used
     */
    protected void addSearchSetting(CmsXmlContentDefinition contentDefinition, String elementName, Boolean value)
    throws CmsXmlException {

        if (contentDefinition.getSchemaType(elementName) == null) {
            throw new CmsXmlException(org.opencms.xml.types.Messages.get().container(
                Messages.ERR_XMLCONTENT_INVALID_ELEM_SEARCHSETTINGS_1,
                elementName));
        }
        // store the search exclusion as defined
        m_searchSettings.put(elementName, value);
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
                    Class<?> specialWidgetClass = Class.forName(widgetClassOrAlias);
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
     * Returns the default locale in the content of the given resource.<p>
     * 
     * @param cms the cms context
     * @param resource the resource path to get the default locale for
     * 
     * @return the default locale of the resource
     */
    protected Locale getLocaleForResource(CmsObject cms, String resource) {

        Locale locale = OpenCms.getLocaleManager().getDefaultLocale(cms, resource);
        if (locale == null) {
            List<Locale> locales = OpenCms.getLocaleManager().getAvailableLocales();
            if (locales.size() > 0) {
                locale = locales.get(0);
            } else {
                locale = Locale.ENGLISH;
            }
        }
        return locale;
    }

    /**
     * Returns the category reference path for the given value.<p>
     * 
     * @param cms the cms context
     * @param value the xml content value
     * 
     * @return the category reference path for the given value
     */
    protected String getReferencePath(CmsObject cms, I_CmsXmlContentValue value) {

        // get the original file instead of the temp file
        CmsFile file = value.getDocument().getFile();
        String resourceName = cms.getSitePath(file);
        if (CmsWorkplace.isTemporaryFile(file)) {
            StringBuffer result = new StringBuffer(resourceName.length() + 2);
            result.append(CmsResource.getFolderPath(resourceName));
            result.append(CmsResource.getName(resourceName).substring(1));
            resourceName = result.toString();
        }
        try {
            List<CmsResource> listsib = cms.readSiblings(resourceName, CmsResourceFilter.ALL);
            for (int i = 0; i < listsib.size(); i++) {
                CmsResource resource = listsib.get(i);
                // get the default locale of the resource and set the categories
                Locale locale = getLocaleForResource(cms, cms.getSitePath(resource));
                if (value.getLocale().equals(locale)) {
                    return cms.getSitePath(resource);
                }
            }
        } catch (CmsVfsResourceNotFoundException e) {
            // may hapen if editing a new resource
            if (LOG.isDebugEnabled()) {
                LOG.debug(e.getLocalizedMessage(), e);
            }
        } catch (CmsException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        // if the locale can not be found, just take the current file
        return cms.getSitePath(file);
    }

    /**
     * Returns the validation message to be displayed if a certain rule was violated.<p> 
     * 
     * @param cms the current users OpenCms context
     * @param value the value to validate
     * @param regex the rule that was violated
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
            message = m_validationWarningMessages.get(value.getName());
        } else {
            message = m_validationErrorMessages.get(value.getName());
        }

        if (message == null) {
            if (isWarning) {
                message = MESSAGE_VALIDATION_DEFAULT_WARNING;
            } else {
                message = MESSAGE_VALIDATION_DEFAULT_ERROR;
            }
        }

        // create additional macro values
        Map<String, String> additionalValues = new HashMap<String, String>();
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

        m_elementMappings = new HashMap<String, List<String>>();
        m_elementWidgets = new HashMap<String, I_CmsWidget>();
        m_validationErrorRules = new HashMap<String, String>();
        m_validationErrorMessages = new HashMap<String, String>();
        m_validationWarningRules = new HashMap<String, String>();
        m_validationWarningMessages = new HashMap<String, String>();
        m_defaultValues = new HashMap<String, String>();
        m_configurationValues = new HashMap<String, String>();
        m_searchSettings = new HashMap<String, Boolean>();
        m_relations = new HashMap<String, CmsRelationType>();
        m_relationChecks = new HashMap<String, Boolean>();
        m_previewLocation = null;
        m_modelFolder = null;
        m_tabs = new ArrayList<CmsXmlContentTab>();
        m_cssHeadIncludes = new LinkedHashSet<String>();
        m_jsHeadIncludes = new LinkedHashSet<String>();
        m_settings = new LinkedHashMap<String, CmsXmlContentProperty>();
        m_titleMappings = new ArrayList<String>(2);
        m_formatters = new ArrayList<CmsFormatterBean>();
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

        Iterator<Element> i = CmsXmlGenericWrapper.elementIterator(root, APPINFO_DEFAULT);
        while (i.hasNext()) {
            // iterate all "default" elements in the "defaults" node
            Element element = i.next();
            String elementName = element.attributeValue(APPINFO_ATTR_ELEMENT);
            String defaultValue = element.attributeValue(APPINFO_ATTR_VALUE);
            if ((elementName != null) && (defaultValue != null)) {
                // add a default value mapping for the element
                addDefault(contentDefinition, elementName, defaultValue);
            }
        }
    }

    /**
     * Initializes the formatters for this content handler.<p>
     * 
     * @param root the "formatters" element from the appinfo node of the XML content definition
     * @param contentDefinition the content definition the formatters belong to
     */
    protected void initFormatters(Element root, CmsXmlContentDefinition contentDefinition) {

        // reading the include resources common for all formatters 
        Iterator<Element> itFormatter = CmsXmlGenericWrapper.elementIterator(root, APPINFO_FORMATTER);
        while (itFormatter.hasNext()) {
            // iterate all "formatter" elements in the "formatters" node
            Element element = itFormatter.next();
            String type = element.attributeValue(APPINFO_ATTR_TYPE);
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(type)) {
                // if not set use "*" as default for type
                type = CmsFormatterBean.WILDCARD_TYPE;
            }
            String jspRootPath = element.attributeValue(APPINFO_ATTR_URI);
            String minWidthStr = element.attributeValue(APPINFO_ATTR_MINWIDTH);
            String maxWidthStr = element.attributeValue(APPINFO_ATTR_MAXWIDTH);
            String preview = element.attributeValue(APPINFO_ATTR_PREVIEW);
            String searchContent = element.attributeValue(APPINFO_ATTR_SEARCHCONTENT);
            m_formatters.add(new CmsFormatterBean(
                type,
                jspRootPath,
                minWidthStr,
                maxWidthStr,
                preview,
                searchContent,
                contentDefinition.getSchemaLocation()));
        }
    }

    /**
     * Initializes the head includes for this content handler.<p>
     * 
     * @param root the "headincludes" element from the appinfo node of the XML content definition
     * @param contentDefinition the content definition the head-includes belong to
     */
    protected void initHeadIncludes(Element root, CmsXmlContentDefinition contentDefinition) {

        Iterator<Element> itInclude = CmsXmlGenericWrapper.elementIterator(root, APPINFO_HEAD_INCLUDE);
        while (itInclude.hasNext()) {
            Element element = itInclude.next();
            String type = element.attributeValue(APPINFO_ATTR_TYPE);
            String uri = element.attributeValue(APPINFO_ATTR_URI);
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(uri)) {
                if (ATTRIBUTE_INCLUDE_TYPE_CSS.equals(type)) {
                    m_cssHeadIncludes.add(uri);
                } else if (ATTRIBUTE_INCLUDE_TYPE_JAVASCRIPT.equals(type)) {
                    m_jsHeadIncludes.add(uri);
                }
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
    * You can specify either a valid widget alias such as <code>StringWidget</code>, 
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

        Iterator<Element> i = CmsXmlGenericWrapper.elementIterator(root, APPINFO_LAYOUT);
        while (i.hasNext()) {
            // iterate all "layout" elements in the "layouts" node
            Element element = i.next();
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
     * For example, if you have an element called "Title", it's likely a good idea to 
     * store the value of this element also in the "Title" property of a XML content resource.<p>
     * 
     * @param root the "mappings" element from the appinfo node of the XML content definition
     * @param contentDefinition the content definition the mappings belong to
     * @throws CmsXmlException if something goes wrong
     */
    protected void initMappings(Element root, CmsXmlContentDefinition contentDefinition) throws CmsXmlException {

        Iterator<Element> i = CmsXmlGenericWrapper.elementIterator(root, APPINFO_MAPPING);
        while (i.hasNext()) {
            // iterate all "mapping" elements in the "mappings" node
            Element element = i.next();
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
     * over the link check process is required. For these cases, individual relation behavior can 
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

        Iterator<Element> i = CmsXmlGenericWrapper.elementIterator(root, APPINFO_RELATION);
        while (i.hasNext()) {
            // iterate all "checkrule" elements in the "checkrule" node
            Element element = i.next();
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
     * @param single if <code>true</code> we process the classic sinle line entry, otherwise it's the multiple line setting
     * 
     * @throws CmsXmlException if something goes wrong
     */
    protected void initResourceBundle(Element root, CmsXmlContentDefinition contentDefinition, boolean single)
    throws CmsXmlException {

        if (m_messageBundleNames == null) {
            // it's uncommon to have more then one bundle so just initialize an array length of 2
            m_messageBundleNames = new ArrayList<String>(2);
        }

        if (single) {
            // single "resourcebundle" node

            String messageBundleName = root.attributeValue(APPINFO_ATTR_NAME);
            if (messageBundleName == null) {
                throw new CmsXmlException(Messages.get().container(
                    Messages.ERR_XMLCONTENT_MISSING_RESOURCE_BUNDLE_NAME_2,
                    root.getName(),
                    contentDefinition.getSchemaLocation()));
            }
            if (!m_messageBundleNames.contains(messageBundleName)) {
                // avoid duplicates
                m_messageBundleNames.add(messageBundleName);
            }
            // clear the cached resource bundles for this bundle
            CmsResourceBundleLoader.flushBundleCache(messageBundleName);

        } else {
            // multiple "resourcebundles" node

            // get an iterator for all "propertybundle" subnodes
            Iterator<Element> propertybundles = CmsXmlGenericWrapper.elementIterator(root, APPINFO_PROPERTYBUNDLE);
            while (propertybundles.hasNext()) {
                // iterate all "propertybundle" elements in the "resourcebundle" node
                Element propBundle = propertybundles.next();
                String propertyBundleName = propBundle.attributeValue(APPINFO_ATTR_NAME);
                if (!m_messageBundleNames.contains(propertyBundleName)) {
                    // avoid duplicates
                    m_messageBundleNames.add(propertyBundleName);
                }
                // clear the cached resource bundles for this bundle
                CmsResourceBundleLoader.flushBundleCache(propertyBundleName);
            }

            // get an iterator for all "xmlbundle" subnodes
            Iterator<Element> xmlbundles = CmsXmlGenericWrapper.elementIterator(root, APPINFO_XMLBUNDLE);
            while (xmlbundles.hasNext()) {
                Element xmlbundle = xmlbundles.next();
                String xmlBundleName = xmlbundle.attributeValue(APPINFO_ATTR_NAME);
                // cache the bundle from the XML
                if (!m_messageBundleNames.contains(xmlBundleName)) {
                    // avoid duplicates
                    m_messageBundleNames.add(xmlBundleName);
                }
                // clear the cached resource bundles for this bundle
                CmsResourceBundleLoader.flushBundleCache(xmlBundleName);
                Iterator<Element> bundles = CmsXmlGenericWrapper.elementIterator(xmlbundle, APPINFO_BUNDLE);
                while (bundles.hasNext()) {
                    // iterate all "bundle" elements in the "xmlbundle" node
                    Element bundle = bundles.next();
                    String localeStr = bundle.attributeValue(APPINFO_ATTR_LOCALE);
                    Locale locale;
                    if (CmsStringUtil.isEmptyOrWhitespaceOnly(localeStr)) {
                        // no locale set, so use no locale
                        locale = null;
                    } else {
                        // use provided locale
                        locale = CmsLocaleManager.getLocale(localeStr);
                    }
                    if (CmsLocaleManager.getDefaultLocale().equals(locale)) {
                        // in case the default locale is given, we store this as root
                        locale = null;
                    }

                    CmsListResourceBundle xmlBundle = null;

                    Iterator<Element> resources = CmsXmlGenericWrapper.elementIterator(bundle, APPINFO_RESOURCE);
                    while (resources.hasNext()) {
                        // now collect all resource bundle keys
                        Element resource = resources.next();
                        String key = resource.attributeValue(APPINFO_ATTR_KEY);
                        String value = resource.attributeValue(APPINFO_ATTR_VALUE);
                        if (CmsStringUtil.isEmptyOrWhitespaceOnly(value)) {
                            // read from inside XML tag if value attribute is not set
                            value = resource.getTextTrim();
                        }
                        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(key)
                            && CmsStringUtil.isNotEmptyOrWhitespaceOnly(value)) {
                            if (xmlBundle == null) {
                                // use lazy initilaizing of the bundle
                                xmlBundle = new CmsListResourceBundle();
                            }
                            xmlBundle.addMessage(key.trim(), value.trim());
                        }
                    }
                    if (xmlBundle != null) {
                        CmsResourceBundleLoader.addBundleToCache(xmlBundleName, locale, xmlBundle);
                    }
                }
            }
        }
    }

    /**
     * Initializes the search exclusions values for this content handler.<p>
     * 
     * For the full text search, the value of all elements in one locale of the XML content are combined
     * to one big text, which is referred to as the "content" in the context of the full text search.
     * With this option, it is possible to hide certain elements from this "content" that does not make sense 
     * to include in the full text search.<p>   
     * 
     * @param root the "searchsettings" element from the appinfo node of the XML content definition
     * @param contentDefinition the content definition the default values belong to
     * 
     * @throws CmsXmlException if something goes wrong
     */
    protected void initSearchSettings(Element root, CmsXmlContentDefinition contentDefinition) throws CmsXmlException {

        Iterator<Element> i = CmsXmlGenericWrapper.elementIterator(root, APPINFO_SEARCHSETTING);
        while (i.hasNext()) {
            // iterate all "searchsetting" elements in the "searchsettings" node
            Element element = i.next();
            String elementName = element.attributeValue(APPINFO_ATTR_ELEMENT);
            String searchContent = element.attributeValue(APPINFO_ATTR_SEARCHCONTENT);
            boolean include = CmsStringUtil.isEmpty(searchContent) || Boolean.valueOf(searchContent).booleanValue();
            if (elementName != null) {
                // add search exclusion for the element
                // this may also be "false" in case a default of "true" is to be overwritten
                addSearchSetting(contentDefinition, elementName, Boolean.valueOf(include));
            }
        }
    }

    /**
     * Initializes the element settings for this content handler.<p>
     * 
     * @param root the "settings" element from the appinfo node of the XML content definition
     * @param contentDefinition the content definition the element settings belong to
     */
    protected void initSettings(Element root, CmsXmlContentDefinition contentDefinition) {

        Iterator<Element> itProperties = CmsXmlGenericWrapper.elementIterator(root, APPINFO_SETTING);
        while (itProperties.hasNext()) {
            Element element = itProperties.next();
            CmsXmlContentProperty setting = new CmsXmlContentProperty(
                element.attributeValue(APPINFO_ATTR_NAME),
                element.attributeValue(APPINFO_ATTR_TYPE),
                element.attributeValue(APPINFO_ATTR_WIDGET),
                element.attributeValue(APPINFO_ATTR_WIDGET_CONFIG),
                element.attributeValue(APPINFO_ATTR_RULE_REGEX),
                element.attributeValue(APPINFO_ATTR_RULE_TYPE),
                element.attributeValue(APPINFO_ATTR_DEFAULT),
                element.attributeValue(APPINFO_ATTR_NICE_NAME),
                element.attributeValue(APPINFO_ATTR_DESCRIPTION),
                element.attributeValue(APPINFO_ATTR_ERROR),
                element.attributeValue(APPINFO_ATTR_PREFERFOLDER));
            String name = setting.getName();
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(name)) {
                m_settings.put(name, setting);
            }
        }
    }

    /**
     * Initializes the tabs for this content handler.<p>
     * 
     * @param root the "tabs" element from the appinfo node of the XML content definition
     * @param contentDefinition the content definition the tabs belong to
     */
    protected void initTabs(Element root, CmsXmlContentDefinition contentDefinition) {

        if (Boolean.valueOf(root.attributeValue(APPINFO_ATTR_USEALL, CmsStringUtil.FALSE)).booleanValue()) {
            // all first level elements should be treated as tabs
            Iterator<I_CmsXmlSchemaType> i = contentDefinition.getTypeSequence().iterator();
            while (i.hasNext()) {
                // get the type
                I_CmsXmlSchemaType type = i.next();
                m_tabs.add(new CmsXmlContentTab(type.getName()));
            }
        } else {
            // manual definition of tabs
            Iterator<Element> i = CmsXmlGenericWrapper.elementIterator(root, APPINFO_TAB);
            while (i.hasNext()) {
                // iterate all "tab" elements in the "tabs" node
                Element element = i.next();
                // this is a tab node
                String elementName = element.attributeValue(APPINFO_ATTR_ELEMENT);
                String collapseValue = element.attributeValue(APPINFO_ATTR_COLLAPSE, CmsStringUtil.TRUE);
                String tabName = element.attributeValue(APPINFO_ATTR_NAME, elementName);
                if (elementName != null) {
                    // add the element tab 
                    m_tabs.add(new CmsXmlContentTab(elementName, Boolean.valueOf(collapseValue).booleanValue(), tabName));
                }
            }
            // check if first element has been defined as tab
            I_CmsXmlSchemaType type = contentDefinition.getTypeSequence().get(0);
            CmsXmlContentTab tab = new CmsXmlContentTab(type.getName());
            if (!m_tabs.contains(tab)) {
                m_tabs.add(0, tab);
            }
        }
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

        List<Element> elements = new ArrayList<Element>(CmsXmlGenericWrapper.elements(root, APPINFO_RULE));
        elements.addAll(CmsXmlGenericWrapper.elements(root, APPINFO_VALIDATIONRULE));
        Iterator<Element> i = elements.iterator();
        while (i.hasNext()) {
            // iterate all "rule" or "validationrule" elements in the "validationrules" node
            Element element = i.next();
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
        isInvalidateParent = m_relationChecks.get(xpath);
        if (isInvalidateParent == null) {
            // no value found, try default xpath
            String path = CmsXmlUtils.removeXpath(xpath);
            // look up the default value again without indexes
            isInvalidateParent = m_relationChecks.get(path);
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
     * If the key was not found in the configured bundle, or no bundle is configured for this 
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

        List<CmsResource> siblings = null;
        CmsObject rootCms = null;

        Iterator<Map.Entry<String, List<String>>> allMappings = m_elementMappings.entrySet().iterator();
        while (allMappings.hasNext()) {
            Map.Entry<String, List<String>> e = allMappings.next();
            String path = e.getKey();
            List<String> mappings = e.getValue();
            if (mappings == null) {
                // nothing to do if we have no mappings at all
                continue;
            }
            if ((siblings == null) || (rootCms == null)) {
                // create OpenCms user context initialized with "/" as site root to read all siblings
                rootCms = OpenCms.initCmsObject(cms);
                rootCms.getRequestContext().setSiteRoot("/");
                siblings = rootCms.readSiblings(content.getFile().getRootPath(), CmsResourceFilter.IGNORE_EXPIRATION);
            }
            for (int v = mappings.size() - 1; v >= 0; v--) {
                String mapping = mappings.get(v);
                if (mapping.startsWith(MAPTO_PROPERTY_LIST) || mapping.startsWith(MAPTO_PROPERTY)) {

                    for (int i = 0; i < siblings.size(); i++) {

                        // get siblings filename and locale
                        String filename = siblings.get(i).getRootPath();
                        Locale locale = OpenCms.getLocaleManager().getDefaultLocale(rootCms, filename);

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
                        rootCms.writePropertyObject(filename, new CmsProperty(property, CmsProperty.DELETE_VALUE, null));
                    }
                } else if (mapping.startsWith(MAPTO_PERMISSION)) {
                    for (int i = 0; i < siblings.size(); i++) {

                        // get siblings filename and locale
                        String filename = siblings.get(i).getRootPath();
                        Locale locale = OpenCms.getLocaleManager().getDefaultLocale(rootCms, filename);

                        if (!content.hasLocale(locale)) {
                            // only remove property if the locale fits
                            continue;
                        }
                        if (content.hasValue(path, locale)) {
                            // value is available, property must be kept
                            continue;
                        }
                        // remove all existing permissions from the file
                        List<CmsAccessControlEntry> aces = rootCms.getAccessControlEntries(filename, false);
                        for (Iterator<CmsAccessControlEntry> j = aces.iterator(); j.hasNext();) {
                            CmsAccessControlEntry ace = j.next();
                            if (ace.getPrincipal().equals(CmsAccessControlEntry.PRINCIPAL_ALL_OTHERS_ID)) {
                                // remove the entry "All others", which has to be treated in a special way
                                rootCms.rmacc(
                                    filename,
                                    CmsAccessControlEntry.PRINCIPAL_ALL_OTHERS_NAME,
                                    CmsAccessControlEntry.PRINCIPAL_ALL_OTHERS_ID.toString());
                            } else {
                                // this is a group or user principal
                                I_CmsPrincipal principal = CmsPrincipal.readPrincipal(rootCms, ace.getPrincipal());
                                if (principal.isGroup()) {
                                    rootCms.rmacc(filename, I_CmsPrincipal.PRINCIPAL_GROUP, principal.getName());
                                } else if (principal.isUser()) {
                                    rootCms.rmacc(filename, I_CmsPrincipal.PRINCIPAL_USER, principal.getName());
                                }
                            }
                        }
                    }
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
     * The errorHandler parameter is optional, if <code>null</code> is given a new error handler 
     * instance must be created.<p>
     * 
     * @param cms the current OpenCms user context
     * @param value the value to resolve the validation rules for
     * @param errorHandler (optional) an error handler instance that contains previous error or warnings
     * 
     * @return an error handler that contains all errors and warnings currently found
     */
    protected CmsXmlContentErrorHandler validateCategories(
        CmsObject cms,
        I_CmsXmlContentValue value,
        CmsXmlContentErrorHandler errorHandler) {

        if (!value.isSimpleType()) {
            // do not validate complex types
            return errorHandler;
        }
        I_CmsWidget widget = null;
        try {
            widget = value.getContentDefinition().getContentHandler().getWidget(value);
        } catch (CmsXmlException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        if (!(widget instanceof CmsCategoryWidget)) {
            // do not validate widget that are not category widgets
            return errorHandler;
        }
        String stringValue = value.getStringValue(cms);
        try {
            String catPath = CmsCategoryService.getInstance().getCategory(cms, stringValue).getPath();
            String refPath = getReferencePath(cms, value);
            CmsCategoryService.getInstance().readCategory(cms, catPath, refPath);
            if (((CmsCategoryWidget)widget).isOnlyLeafs()) {
                if (!CmsCategoryService.getInstance().readCategories(cms, catPath, false, refPath).isEmpty()) {
                    errorHandler.addError(
                        value,
                        Messages.get().getBundle(value.getLocale()).key(Messages.GUI_CATEGORY_CHECK_NOLEAF_ERROR_0));
                }
            }
        } catch (CmsDataAccessException e) {
            // expected error in case of empty/invalid value
            // see CmsCategory#getCategoryPath(String, String)
            if (LOG.isDebugEnabled()) {
                LOG.debug(e.getLocalizedMessage(), e);
            }
            errorHandler.addError(
                value,
                Messages.get().getBundle(value.getLocale()).key(Messages.GUI_CATEGORY_CHECK_EMPTY_ERROR_0));
        } catch (CmsException e) {
            // unexpected error
            if (LOG.isErrorEnabled()) {
                LOG.error(e.getLocalizedMessage(), e);
            }
            errorHandler.addError(value, e.getLocalizedMessage());
        }
        return errorHandler;
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

        // if there is a value of type file reference
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
        try {
            String sitePath = cms.getRequestContext().removeSiteRoot(link.getTarget());
            // validate the link for error
            CmsResource res = null;
            CmsSite site = OpenCms.getSiteManager().getSiteForRootPath(link.getTarget());
            // the link target may be a root path for a resource in another site 
            if (site != null) {
                CmsObject rootCms = OpenCms.initCmsObject(cms);
                rootCms.getRequestContext().setSiteRoot("");
                res = rootCms.readResource(link.getTarget(), CmsResourceFilter.IGNORE_EXPIRATION);
            } else {
                res = cms.readResource(sitePath, CmsResourceFilter.IGNORE_EXPIRATION);
            }
            // check the time range 
            if (res != null) {
                long time = System.currentTimeMillis();
                if (!res.isReleased(time)) {
                    if (errorHandler != null) {
                        // generate warning message
                        errorHandler.addWarning(
                            value,
                            Messages.get().getBundle(value.getLocale()).key(
                                Messages.GUI_XMLCONTENT_CHECK_WARNING_NOT_RELEASED_0));
                    }
                    return true;
                } else if (res.isExpired(time)) {
                    if (errorHandler != null) {
                        // generate warning message
                        errorHandler.addWarning(
                            value,
                            Messages.get().getBundle(value.getLocale()).key(
                                Messages.GUI_XMLCONTENT_CHECK_WARNING_EXPIRED_0));
                    }
                    return true;
                }
            }
        } catch (CmsException e) {
            if (errorHandler != null) {
                // generate error message
                errorHandler.addError(
                    value,
                    Messages.get().getBundle(value.getLocale()).key(Messages.GUI_XMLCONTENT_CHECK_ERROR_0));
            }
            return true;
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
        Map<String, String> rules,
        boolean isWarning) {

        if (validateLink(cms, value, errorHandler)) {
            return errorHandler;
        }
        try {
            if (value.getContentDefinition().getContentHandler().getWidget(value) instanceof CmsDisplayWidget) {
                // display widgets should not be validated
                return errorHandler;
            }
        } catch (CmsXmlException e) {
            errorHandler.addError(value, e.getMessage());
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

        String regex = rules.get(value.getName());
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
     * Checks the default XML schema validation rules.<p>
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

    /**
     * Writes the categories if a category widget is present.<p>
     * 
     * @param cms the cms context
     * @param file the file
     * @param content the xml content to set the categories for
     * 
     * @return the perhaps modified file
     * 
     * @throws CmsException if something goes wrong
     */
    protected CmsFile writeCategories(CmsObject cms, CmsFile file, CmsXmlContent content) throws CmsException {

        if (CmsWorkplace.isTemporaryFile(file)) {
            // ignore temporary file if the original file exists (not the case for direct edit: "new")
            if (CmsResource.isTemporaryFileName(file.getRootPath())) {
                String originalFileName = CmsResource.getFolderPath(file.getRootPath())
                    + CmsResource.getName(file.getRootPath()).substring(CmsResource.TEMP_FILE_PREFIX.length());
                if (cms.existsResource(cms.getRequestContext().removeSiteRoot(originalFileName))) {
                    // original file exists, ignore it
                    return file;
                }
            } else {
                // file name does not start with temporary prefix, ignore the file
                return file;
            }
        }
        // check the presence of a category widget
        boolean hasCategoryWidget = false;
        Iterator<I_CmsWidget> it = m_elementWidgets.values().iterator();
        while (it.hasNext()) {
            Object widget = it.next();
            if (widget instanceof CmsCategoryWidget) {
                hasCategoryWidget = true;
                break;
            }
        }
        if (!hasCategoryWidget) {
            // nothing to do if no category widget is present
            return file;
        }
        boolean modified = false;
        // clone the cms object, and use the root site
        CmsObject tmpCms = OpenCms.initCmsObject(cms);
        tmpCms.getRequestContext().setSiteRoot("");
        // read all siblings
        try {
            List<CmsResource> listsib = tmpCms.readSiblings(file.getRootPath(), CmsResourceFilter.ALL);
            for (int i = 0; i < listsib.size(); i++) {
                CmsResource resource = listsib.get(i);
                // get the default locale of the sibling
                Locale locale = getLocaleForResource(tmpCms, resource.getRootPath());
                // remove all previously set categories
                CmsCategoryService.getInstance().clearCategoriesForResource(tmpCms, resource.getRootPath());
                // iterate over all values checking for the category widget
                CmsXmlContentWidgetVisitor widgetCollector = new CmsXmlContentWidgetVisitor(locale);
                content.visitAllValuesWith(widgetCollector);
                Iterator<Map.Entry<String, I_CmsXmlContentValue>> itWidgets = widgetCollector.getValues().entrySet().iterator();
                while (itWidgets.hasNext()) {
                    Map.Entry<String, I_CmsXmlContentValue> entry = itWidgets.next();
                    String xpath = entry.getKey();
                    I_CmsWidget widget = widgetCollector.getWidgets().get(xpath);
                    if (!(widget instanceof CmsCategoryWidget)) {
                        // ignore other values than categories
                        continue;
                    }
                    I_CmsXmlContentValue value = entry.getValue();
                    String catRootPath = value.getStringValue(tmpCms);
                    if (CmsStringUtil.isEmptyOrWhitespaceOnly(catRootPath)) {
                        // skip empty values
                        continue;
                    }
                    try {
                        // add the file to the selected category
                        CmsCategory cat = CmsCategoryService.getInstance().getCategory(tmpCms, catRootPath);
                        CmsCategoryService.getInstance().addResourceToCategory(
                            tmpCms,
                            resource.getRootPath(),
                            cat.getPath());
                    } catch (CmsVfsResourceNotFoundException e) {
                        // invalid category
                        try {
                            // try to remove invalid value
                            content.removeValue(value.getName(), value.getLocale(), value.getIndex());
                            modified = true;
                        } catch (CmsRuntimeException ex) {
                            // in case minoccurs prevents removing the invalid value
                            if (LOG.isDebugEnabled()) {
                                LOG.debug(ex.getLocalizedMessage(), ex);
                            }
                        }
                    }
                }
            }
        } catch (CmsException ex) {
            if (LOG.isErrorEnabled()) {
                LOG.error(ex.getLocalizedMessage(), ex);
            }
        }
        if (modified) {
            // when an invalid category has been removed
            file = content.correctXmlStructure(cms);
            content.setFile(file);
        }
        return file;
    }

}
