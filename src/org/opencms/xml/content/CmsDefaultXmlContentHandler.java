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

package org.opencms.xml.content;

import org.opencms.configuration.CmsConfigurationManager;
import org.opencms.db.log.CmsLogEntry;
import org.opencms.file.CmsDataAccessException;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsListResourceBundle;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.i18n.CmsMessages;
import org.opencms.i18n.CmsMultiMessages;
import org.opencms.i18n.CmsMultiMessages.I_KeyFallbackHandler;
import org.opencms.i18n.CmsResourceBundleLoader;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.CmsStaticResourceHandler;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsCategory;
import org.opencms.relations.CmsCategoryService;
import org.opencms.relations.CmsLink;
import org.opencms.relations.CmsRelationType;
import org.opencms.search.fields.CmsSearchField;
import org.opencms.search.fields.CmsSearchFieldMapping;
import org.opencms.search.fields.CmsSearchFieldMappingType;
import org.opencms.search.fields.I_CmsSearchFieldMapping;
import org.opencms.search.galleries.CmsGalleryNameMacroResolver;
import org.opencms.search.solr.CmsSolrField;
import org.opencms.security.CmsAccessControlEntry;
import org.opencms.security.CmsPrincipal;
import org.opencms.security.CmsRole;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.site.CmsSite;
import org.opencms.util.CmsDefaultSet;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsHtmlConverter;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.CmsCategoryWidget;
import org.opencms.widgets.CmsDisplayWidget;
import org.opencms.widgets.I_CmsComplexWidget;
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
import org.opencms.xml.containerpage.CmsSchemaFormatterBeanWrapper;
import org.opencms.xml.containerpage.I_CmsFormatterBean;
import org.opencms.xml.types.CmsXmlDynamicCategoryValue;
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
import org.dom4j.Node;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Default implementation for the XML content handler, will be used by all XML contents that do not
 * provide their own handler.<p>
 *
 * @since 6.0.0
 */
public class CmsDefaultXmlContentHandler implements I_CmsXmlContentHandler, I_CmsXmlContentVisibilityHandler {

    /**
     * Contains the visibility handler configuration for a content field path.<p>
     */
    protected static class VisibilityConfiguration {

        /** The handler instance. */
        private I_CmsXmlContentVisibilityHandler m_handler;

        /** The handler configuration parameters. */
        private String m_params;

        /**
         * Constructor.<p>
         *
         * @param handler the handler instance
         * @param params the handler configuration parameteres
         */
        protected VisibilityConfiguration(I_CmsXmlContentVisibilityHandler handler, String params) {

            m_handler = handler;
            m_params = params;
        }

        /**
         * Returns the visibility handler instance.<p>
         *
         * @return the handler instance
         */
        public I_CmsXmlContentVisibilityHandler getHandler() {

            return m_handler;
        }

        /**
         * Returns the visibility handler configuration parameters.<p>
         *
         * @return the configuration parameters
         */
        public String getParams() {

            return m_params;
        }
    }

    /** Constant for the "appinfo" element name itself. */
    public static final String APPINFO_APPINFO = "appinfo";

    /** Constant for the "addto" appinfo attribute name. */
    public static final String APPINFO_ATTR_ADD_TO = "addto";

    /** Constant for the "boost" appinfo attribute name. */
    public static final String APPINFO_ATTR_BOOST = "boost";

    /** Constant for the "class" appinfo attribute name. */
    public static final String APPINFO_ATTR_CLASS = "class";

    /** Constant for the "collapse" appinfo attribute name. */
    public static final String APPINFO_ATTR_COLLAPSE = "collapse";

    /** Constant for the "configuration" appinfo attribute name. */
    public static final String APPINFO_ATTR_CONFIGURATION = "configuration";

    /** The exclude from index attribute. */
    public static final String APPINFO_ATTR_CONTAINER_PAGE_ONLY = "containerPageOnly";

    /** Constant for the "copyfields" appinfo attribute name. */
    public static final String APPINFO_ATTR_COPY_FIELDS = "copyfields";

    /** Constant for the "default" appinfo attribute name. */
    public static final String APPINFO_ATTR_DEFAULT = "default";

    /** Constant for the "description" appinfo attribute name. */
    public static final String APPINFO_ATTR_DESCRIPTION = "description";

    /** Constant for the "displaycompact" appinfo attribute name. */
    public static final String APPINFO_ATTR_DISPLAY = "display";

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

    /** Constant for the "mapping" appinfo attribute name. */
    public static final String APPINFO_ATTR_MAPPING = "mapping";

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

    /** Constant for the "params" appinfo attribute name. */
    public static final String APPINFO_ATTR_PARAMS = "params";

    /** Constant for the "preview" appinfo attribute name. */
    public static final String APPINFO_ATTR_PREVIEW = "preview";

    /** Constant for the "regex" appinfo attribute name. */
    public static final String APPINFO_ATTR_REGEX = "regex";

    /** Constant for the "resolveMacros" attribute name. */
    public static final String APPINFO_ATTR_RESOLVE_MACROS = "resolveMacros";

    /** Constant for the "rule-regex" appinfo attribute name. */
    public static final String APPINFO_ATTR_RULE_REGEX = "rule-regex";

    /** Constant for the "rule-type" appinfo attribute name. */
    public static final String APPINFO_ATTR_RULE_TYPE = "rule-type";

    /** Constant for the "scope" appinfo attribute name. */
    public static final String APPINFO_ATTR_SCOPE = "scope";

    /** Constant for the "searchcontent" appinfo attribute name. */
    public static final String APPINFO_ATTR_SEARCHCONTENT = "searchcontent";

    /** Constant for the "select-inherit" appinfo attribute name. */
    public static final String APPINFO_ATTR_SELECT_INHERIT = "select-inherit";

    /** Constant for the "sourcefield" appinfo attribute name. */
    public static final String APPINFO_ATTR_SOURCE_FIELD = "sourcefield";

    /** Constant for the "targetfield" appinfo attribute name. */
    public static final String APPINFO_ATTR_TARGET_FIELD = "targetfield";

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

    /** Constant for the "editorchangehandler" appinfo element name. */
    public static final String APPINFO_EDITOR_CHANGE_HANDLER = "editorchangehandler";

    /** Constant for the "editorchangehandlers" appinfo element name. */
    public static final String APPINFO_EDITOR_CHANGE_HANDLERS = "editorchangehandlers";

    /** Constant for the "forbidden-contexts" appinfo attribute name. */
    public static final String APPINFO_FORBIDDEN_CONTEXTS = "forbidden-contexts";

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

    /** Constant for the 'messagekeyhandler' node. */
    public static final String APPINFO_MESSAGEKEYHANDLER = "messagekeyhandler";

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

    /** Constant for the "solrfield" appinfo element name. */
    public static final String APPINFO_SOLR_FIELD = "solrfield";

    /** Constant for the "synchronization" appinfo element name. */
    public static final String APPINFO_SYNCHRONIZATION = "synchronization";

    /** Constant for the "synchronizations" appinfo element name. */
    public static final String APPINFO_SYNCHRONIZATIONS = "synchronizations";

    /** Constant for the "tab" appinfo element name. */
    public static final String APPINFO_TAB = "tab";

    /** Constant for the "tabs" appinfo element name. */
    public static final String APPINFO_TABS = "tabs";

    /** Node name. */
    public static final String APPINFO_TEMPLATE = "template";

    /** Node name. */
    public static final String APPINFO_TEMPLATES = "templates";

    /** Constant for the "validationrule" appinfo element name. */
    public static final String APPINFO_VALIDATIONRULE = "validationrule";

    /** Constant for the "validationrules" appinfo element name. */
    public static final String APPINFO_VALIDATIONRULES = "validationrules";

    /** Constant for the "element" value of the appinfo attribute "addto". */
    public static final String APPINFO_VALUE_ADD_TO_CONTENT = "element";

    /** Constant for the "page" value of the appinfo attribute "addto". */
    public static final String APPINFO_VALUE_ADD_TO_PAGE = "page";

    /** Constant for the "visibilities" appinfo element name. */
    public static final String APPINFO_VISIBILITIES = "visibilities";

    /** Constant for the "visibility" appinfo element name. */
    public static final String APPINFO_VISIBILITY = "visibility";

    /** Constant for the "xmlbundle" appinfo element name. */
    public static final String APPINFO_XMLBUNDLE = "xmlbundle";

    /** Attribute name. */
    public static final String ATTR_ENABLED = "enabled";

    /** Attribute name. */
    public static final String ATTR_ENABLED_BY_DEFAULT = "enabledByDefault";

    /** Attribute name. */
    public static final String ATTR_USE_ACACIA = "useAcacia";

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

    /** The 'useDefault' attribute name. */
    private static final String APPINFO_ATTR_USE_DEFAULT = "useDefault";

    /** The node name for the default complex widget configuration. */
    private static final Object APPINFO_DEFAULTWIDGET = "defaultwidget";

    /** Attribute name for the context used for resolving content mappings. */
    private static final String ATTR_MAPPING_RESOLUTION_CONTEXT = "MAPPING_RESOLUTION_CONTEXT";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsDefaultXmlContentHandler.class);

    /** The principal list separator. */
    private static final String PRINCIPAL_LIST_SEPARATOR = ",";

    /** The title property individual mapping key. */
    private static final String TITLE_PROPERTY_INDIVIDUAL_MAPPING = MAPTO_PROPERTY_INDIVIDUAL
        + CmsPropertyDefinition.PROPERTY_TITLE;

    /** The title property mapping key. */
    private static final String TITLE_PROPERTY_MAPPING = MAPTO_PROPERTY + CmsPropertyDefinition.PROPERTY_TITLE;

    /** The title property shared mapping key. */
    private static final String TITLE_PROPERTY_SHARED_MAPPING = MAPTO_PROPERTY_SHARED
        + CmsPropertyDefinition.PROPERTY_TITLE;

    /** The set of allowed templates. */
    protected CmsDefaultSet<String> m_allowedTemplates = new CmsDefaultSet<String>();

    /** A map from attribute name to complex widgets. */
    protected Map<String, I_CmsComplexWidget> m_complexWidgets = new HashMap<String, I_CmsComplexWidget>();

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

    /** The Solr field configurations. */
    protected Map<String, CmsSearchField> m_searchFields;

    /** The Solr field configurations added to the container pages contents are on. */
    protected Map<String, CmsSearchField> m_searchFieldsPage;

    /** The search settings. */
    protected Map<String, Boolean> m_searchSettings;

    /** The configured settings for the formatters (as defined in the annotations). */
    protected Map<String, CmsXmlContentProperty> m_settings;

    /** The configured locale synchronization elements. */
    protected List<String> m_synchronizations;

    /** The configured tabs. */
    protected List<CmsXmlContentTab> m_tabs;

    /** The list of mappings to the "Title" property. */
    protected List<String> m_titleMappings;

    /** Flag which controls whether the Acacia editor should be disabled for this type. */
    protected boolean m_useAcacia = true;

    /** The messages for the error validation rules. */
    protected Map<String, String> m_validationErrorMessages;

    /** The validation rules that cause an error (as defined in the annotations). */
    protected Map<String, String> m_validationErrorRules;

    /** The messages for the warning validation rules. */
    protected Map<String, String> m_validationWarningMessages;

    /** The validation rules that cause a warning (as defined in the annotations). */
    protected Map<String, String> m_validationWarningRules;

    /** The container page only flag, indicating if this XML content should be indexed on container pages only. */
    private boolean m_containerPageOnly;

    /** The default complex widget class name. */
    private String m_defaultWidget;

    /** The default complex widget configuration. */
    private String m_defaultWidgetConfig;

    /** The default complex widget for this type. */
    private I_CmsComplexWidget m_defaultWidgetInstance;

    /** The elements to display in ncompact view. */
    private HashMap<String, DisplayType> m_displayTypes;

    /** The editor change handlers. */
    private List<I_CmsXmlContentEditorChangeHandler> m_editorChangeHandlers;

    /** A set of keys identifying the mappings which should use default values if the corresponding values are not set in the XML content. */
    private Set<String> m_mappingsUsingDefault = new HashSet<String>();

    /** Message key fallback handler for the editor. */
    private CmsMultiMessages.I_KeyFallbackHandler m_messageKeyHandler = new CmsMultiMessages.I_KeyFallbackHandler() {

        public Optional<String> getFallbackKey(String key) {

            return Optional.absent();
        }
    };

    /** The paths of values for which no macros should be resolved when getting the default value. */
    private Set<String> m_nonMacroResolvableDefaults = new HashSet<String>();

    /** The visibility configurations by element path. */
    private Map<String, VisibilityConfiguration> m_visibilityConfigurations;

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
            throw new CmsRuntimeException(
                Messages.get().container(
                    org.opencms.xml.types.Messages.ERR_XMLCONTENT_LOAD_SCHEMA_1,
                    APPINFO_SCHEMA_FILE_TYPES),
                e);
        }
        CmsXmlEntityResolver.cacheSystemId(APPINFO_SCHEMA_TYPES_SYSTEM_ID, appinfoSchemaTypes);
        byte[] appinfoSchema;
        try {
            // now read the default base schema
            appinfoSchema = CmsFileUtil.readFile(APPINFO_SCHEMA_FILE);
        } catch (Exception e) {
            throw new CmsRuntimeException(
                Messages.get().container(
                    org.opencms.xml.types.Messages.ERR_XMLCONTENT_LOAD_SCHEMA_1,
                    APPINFO_SCHEMA_FILE),
                e);
        }
        CmsXmlEntityResolver.cacheSystemId(APPINFO_SCHEMA_SYSTEM_ID, appinfoSchema);
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
            throw new CmsRuntimeException(
                Messages.get().container(
                    org.opencms.xml.types.Messages.ERR_XMLCONTENT_LOAD_SCHEMA_1,
                    APPINFO_SCHEMA_FILE_TYPES),
                e);
        }
        CmsXmlEntityResolver.cacheSystemId(APPINFO_SCHEMA_TYPES_SYSTEM_ID, appinfoSchemaTypes);
        byte[] appinfoSchema;
        try {
            // now read the default base schema
            appinfoSchema = CmsFileUtil.readFile(APPINFO_SCHEMA_FILE);
        } catch (Exception e) {
            throw new CmsRuntimeException(
                Messages.get().container(
                    org.opencms.xml.types.Messages.ERR_XMLCONTENT_LOAD_SCHEMA_1,
                    APPINFO_SCHEMA_FILE),
                e);
        }
        CmsXmlEntityResolver.cacheSystemId(APPINFO_SCHEMA_SYSTEM_ID, appinfoSchema);
    }

    /**
     * Copies a given CMS context and set the copy's site root to '/'.<p>
     *
     * @param cms the CMS context to copy
     * @return the copy
     *
     * @throws CmsException if something goes wrong
     */
    public CmsObject createRootCms(CmsObject cms) throws CmsException {

        CmsObject rootCms = OpenCms.initCmsObject(cms);
        Object logEntry = cms.getRequestContext().getAttribute(CmsLogEntry.ATTR_LOG_ENTRY);
        if (logEntry != null) {
            rootCms.getRequestContext().setAttribute(CmsLogEntry.ATTR_LOG_ENTRY, logEntry);
        }
        rootCms.getRequestContext().setSiteRoot("/");
        return rootCms;
    }

    /**
     * @see org.opencms.xml.content.I_CmsXmlContentHandler#getAllowedTemplates()
     */
    public CmsDefaultSet<String> getAllowedTemplates() {

        return m_allowedTemplates;

    }

    /**
     * @see org.opencms.xml.content.I_CmsXmlContentHandler#getComplexWidget(org.opencms.xml.types.I_CmsXmlSchemaType)
     */
    public I_CmsComplexWidget getComplexWidget(I_CmsXmlSchemaType value) {

        I_CmsComplexWidget result = m_complexWidgets.get(value.getName());
        if (result == null) {
            if (value instanceof CmsXmlNestedContentDefinition) {
                I_CmsXmlContentHandler contentHandler = ((CmsXmlNestedContentDefinition)value).getNestedContentDefinition().getContentHandler();
                if (contentHandler.getDefaultComplexWidget() != null) {
                    return contentHandler.getDefaultComplexWidget().configure(
                        contentHandler.getDefaultComplexWidgetConfiguration());
                }
            }
            return null;
        } else {
            String configuration = getConfiguration(value);
            result = result.configure(configuration);
            return result;
        }
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

    /***
     * @see org.opencms.xml.content.I_CmsXmlContentHandler#getCSSHeadIncludes(org.opencms.file.CmsObject, org.opencms.file.CmsResource)
     */
    @SuppressWarnings("unused")
    public Set<String> getCSSHeadIncludes(CmsObject cms, CmsResource resource) throws CmsException {

        return getCSSHeadIncludes();
    }

    /**
     * @see org.opencms.xml.content.I_CmsXmlContentHandler#getDefault(org.opencms.file.CmsObject, org.opencms.file.CmsResource, org.opencms.xml.types.I_CmsXmlSchemaType, java.lang.String, java.util.Locale)
     */
    public String getDefault(CmsObject cms, CmsResource resource, I_CmsXmlSchemaType type, String path, Locale locale) {

        String defaultValue;
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(path)) {
            // use the "getDefault" method of the given value, will use value from standard XML schema
            defaultValue = type.getDefault(locale);
        } else {
            // look up the default from the configured mappings
            defaultValue = m_defaultValues.get(path);
            if (defaultValue == null) {
                // no value found, try default xpath
                path = CmsXmlUtils.removeXpath(path);
                path = CmsXmlUtils.createXpath(path, 1);
                // look up the default value again with default index of 1 in all path elements
                defaultValue = m_defaultValues.get(path);
            }
        }
        if (defaultValue != null) {
            CmsObject newCms = cms;
            if (resource != null) {
                try {
                    // switch the current URI to the XML document resource so that properties can be read
                    CmsSite site = OpenCms.getSiteManager().getSiteForRootPath(resource.getRootPath());
                    if (site != null) {
                        newCms = OpenCms.initCmsObject(cms);
                        newCms.getRequestContext().setSiteRoot(site.getSiteRoot());
                        newCms.getRequestContext().setUri(newCms.getSitePath(resource));
                    }
                } catch (Exception e) {
                    // on any error just use the default input OpenCms context
                }
            }
            // return the default value with processed macros
            String result = defaultValue;
            if (!m_nonMacroResolvableDefaults.contains(path)) {
                CmsMacroResolver resolver = CmsMacroResolver.newInstance().setCmsObject(newCms).setMessages(
                    getMessages(locale));
                result = resolver.resolveMacros(defaultValue);
            }
            return result;
        }
        // no default value is available
        return null;
    }

    /**
     * @see org.opencms.xml.content.I_CmsXmlContentHandler#getDefault(org.opencms.file.CmsObject, I_CmsXmlContentValue, java.util.Locale)
     */
    public String getDefault(CmsObject cms, I_CmsXmlContentValue value, Locale locale) {

        String path = null;
        if (value.getElement() != null) {
            path = value.getPath();
        }

        return getDefault(cms, value.getDocument() != null ? value.getDocument().getFile() : null, value, path, locale);
    }

    /**
     * @see org.opencms.xml.content.I_CmsXmlContentHandler#getDefaultComplexWidget()
     */
    public I_CmsComplexWidget getDefaultComplexWidget() {

        return m_defaultWidgetInstance;
    }

    /**
     * @see org.opencms.xml.content.I_CmsXmlContentHandler#getDefaultComplexWidgetClass()
     */
    public String getDefaultComplexWidgetClass() {

        return m_defaultWidget;
    }

    /**
     * @see org.opencms.xml.content.I_CmsXmlContentHandler#getDefaultComplexWidgetConfiguration()
     */
    public String getDefaultComplexWidgetConfiguration() {

        return m_defaultWidgetConfig;
    }

    /**
     * @see org.opencms.xml.content.I_CmsXmlContentHandler#getDisplayType(org.opencms.xml.types.I_CmsXmlSchemaType)
     */
    public DisplayType getDisplayType(I_CmsXmlSchemaType type) {

        if (m_displayTypes.containsKey(type.getName())) {
            return m_displayTypes.get(type.getName());
        } else {
            return DisplayType.none;
        }
    }

    /**
     * @see org.opencms.xml.content.I_CmsXmlContentHandler#getEditorChangeHandlers()
     */
    public List<I_CmsXmlContentEditorChangeHandler> getEditorChangeHandlers() {

        return Collections.unmodifiableList(m_editorChangeHandlers);
    }

    /**
     * @see org.opencms.xml.content.I_CmsXmlContentHandler#getFormatterConfiguration(org.opencms.file.CmsObject, org.opencms.file.CmsResource)
     */
    public CmsFormatterConfiguration getFormatterConfiguration(CmsObject cms, CmsResource resource) {

        List<I_CmsFormatterBean> wrappers = Lists.newArrayList();
        for (CmsFormatterBean formatter : m_formatters) {
            CmsSchemaFormatterBeanWrapper wrapper = new CmsSchemaFormatterBeanWrapper(cms, formatter, this, resource);
            wrappers.add(wrapper);
        }
        return CmsFormatterConfiguration.create(cms, wrappers);
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

        List<String> result = m_elementMappings.get(elementName);
        if (result == null) {
            result = Collections.emptyList();
        }
        return result;
    }

    /**
     * @see org.opencms.xml.content.I_CmsXmlContentHandler#getMessageKeyHandler()
     */
    public I_KeyFallbackHandler getMessageKeyHandler() {

        return m_messageKeyHandler;
    }

    /**
     * @see org.opencms.xml.content.I_CmsXmlContentHandler#getMessages(java.util.Locale)
     */
    public CmsMessages getMessages(Locale locale) {

        CmsMessages result = null;
        if ((m_messageBundleNames == null) || m_messageBundleNames.isEmpty()) {
            return new CmsMessages(Messages.get().getBundleName(), locale);
        } else {
            // a message bundle was initialized
            CmsMultiMessages multiMessages = new CmsMultiMessages(locale);
            for (String messageBundleName : m_messageBundleNames) {
                multiMessages.addMessages(new CmsMessages(messageBundleName, locale));
            }
            if (!m_messageBundleNames.contains(Messages.get().getBundleName())) {
                multiMessages.addMessages(new CmsMessages(Messages.get().getBundleName(), locale));
            }
            result = multiMessages;

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

        return getRelationType(xpath, CmsRelationType.XML_WEAK);
    }

    /**
     * @see org.opencms.xml.content.I_CmsXmlContentHandler#getRelationType(java.lang.String, org.opencms.relations.CmsRelationType)
     */
    public CmsRelationType getRelationType(String xpath, CmsRelationType defaultType) {

        CmsRelationType relationType = null;
        if (xpath != null) {

            // look up the default from the configured mappings
            relationType = m_relations.get(xpath);
            if (relationType == null) {
                // no value found, try default xpath
                String path = CmsXmlUtils.removeAllXpathIndices(xpath);
                // look up the default value again without indexes
                relationType = m_relations.get(path);
            }
            if (relationType == null) {
                // no value found, try the last simple type path
                String path = CmsXmlUtils.getLastXpathElement(xpath);
                // look up the default value again for the last simple type
                relationType = m_relations.get(path);
            }
        }
        if (relationType == null) {
            relationType = defaultType;
        }
        return relationType;
    }

    /**
     * @see org.opencms.xml.content.I_CmsXmlContentHandler#getSearchFields()
     */
    public Set<CmsSearchField> getSearchFields() {

        return Collections.unmodifiableSet(new HashSet<CmsSearchField>(m_searchFields.values()));
    }

    /**
     * @see org.opencms.xml.content.I_CmsXmlContentHandler#getSearchFieldsForPage()
     */
    public Set<CmsSearchField> getSearchFieldsForPage() {

        return Collections.unmodifiableSet(new HashSet<CmsSearchField>(m_searchFieldsPage.values()));
    }

    /**
     * @see org.opencms.xml.content.I_CmsXmlContentHandler#getSearchSettings()
     */
    public Map<String, Boolean> getSearchSettings() {

        return m_searchSettings;
    }

    /**
     * @see org.opencms.xml.content.I_CmsXmlContentHandler#getSettings(org.opencms.file.CmsObject, org.opencms.file.CmsResource)
     */
    public Map<String, CmsXmlContentProperty> getSettings(CmsObject cms, CmsResource resource) {

        return Collections.unmodifiableMap(m_settings);
    }

    /**
     * @see org.opencms.xml.content.I_CmsXmlContentHandler#getSynchronizations()
     */
    public List<String> getSynchronizations() {

        return Collections.unmodifiableList(m_synchronizations);
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
            if ((result == null)
                && (isMappingUsingDefault(xpath, TITLE_PROPERTY_MAPPING)
                    || isMappingUsingDefault(xpath, TITLE_PROPERTY_SHARED_MAPPING)
                    || isMappingUsingDefault(xpath, TITLE_PROPERTY_INDIVIDUAL_MAPPING))) {
                result = getDefault(cms, document.getFile(), null, xpath, locale);
            }
            if (result != null) {
                try {
                    CmsGalleryNameMacroResolver resolver = new CmsGalleryNameMacroResolver(
                        createRootCms(cms),
                        document,
                        locale);
                    resolver.setKeepEmptyMacros(true);
                    result = resolver.resolveMacros(result);
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        }
        return result;
    }

    /**
     * @see org.opencms.xml.content.I_CmsXmlContentHandler#getWidget(org.opencms.xml.types.I_CmsXmlSchemaType)
     */
    public I_CmsWidget getWidget(I_CmsXmlSchemaType value) {

        // try the specific widget settings first
        I_CmsWidget result = m_elementWidgets.get(value.getName());
        if (result == null) {
            // use default widget mappings
            result = OpenCms.getXmlContentTypeManager().getWidgetDefault(value.getTypeName());
        } else {
            result = result.newInstance();
        }
        if (result != null) {
            // set the configuration value for this widget
            String configuration = getConfiguration(value);
            if (configuration == null) {
                // no individual configuration defined, try to get global default configuration
                configuration = OpenCms.getXmlContentTypeManager().getWidgetDefaultConfiguration(result);
            }
            result.setConfiguration(configuration);
        }
        return result;
    }

    /**
     * @see org.opencms.xml.content.I_CmsXmlContentHandler#hasModifiableFormatters()
     */
    public boolean hasModifiableFormatters() {

        return (m_formatters != null) && (m_formatters.size() > 0);
    }

    /**
     * @see org.opencms.xml.content.I_CmsXmlContentHandler#hasSynchronizedElements()
     */
    public boolean hasSynchronizedElements() {

        return !m_synchronizations.isEmpty();
    }

    /**
     * @see org.opencms.xml.content.I_CmsXmlContentHandler#hasVisibilityHandlers()
     */
    public boolean hasVisibilityHandlers() {

        return (m_visibilityConfigurations != null) && !m_visibilityConfigurations.isEmpty();
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
                } else if (nodeName.equals(APPINFO_TEMPLATES)) {
                    initTemplates(element, contentDefinition);
                } else if (nodeName.equals(APPINFO_DEFAULTWIDGET)) {
                    initDefaultWidget(element);
                } else if (nodeName.equals(APPINFO_VISIBILITIES)) {
                    initVisibilities(element, contentDefinition);
                } else if (nodeName.equals(APPINFO_SYNCHRONIZATIONS)) {
                    initSynchronizations(element, contentDefinition);
                } else if (nodeName.equals(APPINFO_EDITOR_CHANGE_HANDLERS)) {
                    initEditorChangeHandlers(element);
                } else if (nodeName.equals(APPINFO_MESSAGEKEYHANDLER)) {
                    initMessageKeyHandler(element);
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
            Map<String, I_CmsXmlContentValue> valuesToRemove = Maps.newHashMap();
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
                    || ((m_relationChecks.get(path) == null)
                        && (m_relationChecks.get(CmsXmlUtils.removeXpath(path)) == null))) {
                    continue;
                }

                // check rule matched
                if (LOG.isDebugEnabled()) {
                    LOG.debug(Messages.get().getBundle().key(Messages.LOG_XMLCONTENT_CHECK_RULE_MATCH_1, path));
                }
                if (validateLink(cms, value, null)) {
                    // invalid link
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(
                            Messages.get().getBundle().key(
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
                            LOG.debug(
                                Messages.get().getBundle().key(
                                    Messages.LOG_XMLCONTENT_CHECK_PARENT_2,
                                    path,
                                    parentPath));
                        }
                    }
                    value = document.getValue(parentPath, locale);
                    // Doing the actual DOM modifications here would make the bookmarks for this locale invalid,
                    // so we delay it until later because we need the bookmarks for document.getValue() in the next loop iterations
                    valuesToRemove.put(parentPath, value);
                    // mark node as deleted
                    removedNodes.add(parentPath);
                }
            }
            if (!removedNodes.isEmpty()) {
                needReinitialization = true;
            }
            for (I_CmsXmlContentValue valueToRemove : valuesToRemove.values()) {
                // detach the value node from the XML document
                valueToRemove.getElement().detach();
            }
        }
        if (needReinitialization) {
            // re-initialize the XML content
            document.initDocument();
        }
    }

    /**
     * Returns true if the Acacia editor is disabled for this type.<p>
     *
     * @return true if the acacia editor is disabled
     */
    public boolean isAcaciaEditorDisabled() {

        return !m_useAcacia;
    }

    /**
     * @see org.opencms.xml.content.I_CmsXmlContentHandler#isContainerPageOnly()
     */
    public boolean isContainerPageOnly() {

        return m_containerPageOnly;
    }

    /**
     * @see org.opencms.xml.content.I_CmsXmlContentHandler#isSearchable(org.opencms.xml.types.I_CmsXmlContentValue)
     */
    public boolean isSearchable(I_CmsXmlContentValue value) {

        String path = CmsXmlUtils.removeXpath(value.getPath());
        // check for name configured in the annotations
        Boolean searchSetting = m_searchSettings.get(path);
        // if no search setting is found within the root handler, move the path upwards to look for other configurations
        if (searchSetting == null) {
            String[] pathElements = path.split("/");
            I_CmsXmlSchemaType type = value.getDocument().getContentDefinition().getSchemaType(pathElements[0]);
            for (int i = 1; i < pathElements.length; i++) {
                type = ((CmsXmlNestedContentDefinition)type).getNestedContentDefinition().getSchemaType(
                    pathElements[i]);
                String subPath = getSubPath(pathElements, i);
                searchSetting = type.getContentDefinition().getContentHandler().getSearchSettings().get(subPath);
                if (searchSetting != null) {
                    break;
                }
            }
        }
        // if no annotation has been found, use default for value
        return (searchSetting == null) ? value.isSearchable() : searchSetting.booleanValue();
    }

    /**
     * Returns the content field visibilty.<p>
     *
     * This implementation will be used as default if no other <link>org.opencms.xml.content.I_CmsXmlContentVisibilityHandler</link> is configured.<p>
     *
     * Only users that are member in one of the specified groups will be allowed to view and edit the given content field.<p>
     * The parameter should contain a '|' separated list of group names.<p>
     *
     * @see org.opencms.xml.content.I_CmsXmlContentVisibilityHandler#isValueVisible(org.opencms.file.CmsObject, org.opencms.xml.types.I_CmsXmlSchemaType, java.lang.String, java.lang.String, org.opencms.file.CmsResource, java.util.Locale)
     */
    public boolean isValueVisible(
        CmsObject cms,
        I_CmsXmlSchemaType value,
        String elementName,
        String params,
        CmsResource resource,
        Locale contentLocale) {

        CmsUser user = cms.getRequestContext().getCurrentUser();
        boolean result = false;

        try {
            List<CmsRole> roles = OpenCms.getRoleManager().getRolesOfUser(cms, user.getName(), "", true, false, true);
            List<CmsGroup> groups = cms.getGroupsOfUser(user.getName(), false);

            String[] allowedPrincipals = params.split("\\|");
            List<String> groupNames = new ArrayList<String>();
            List<String> roleNames = new ArrayList<String>();

            for (CmsGroup group : groups) {
                groupNames.add(group.getName());
            }
            for (CmsRole role : roles) {
                roleNames.add(role.getRoleName());
            }
            for (String principal : allowedPrincipals) {
                if (CmsRole.hasPrefix(principal)) {
                    // prefixed as a role
                    principal = CmsRole.removePrefix(principal);
                    if (roleNames.contains(principal)) {
                        result = true;
                        break;
                    }
                } else {
                    // otherwise we always assume this is a group, will work if prefixed or not
                    principal = CmsGroup.removePrefix(principal);
                    if (groupNames.contains(principal)) {
                        result = true;
                        break;
                    }
                }
            }
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }

        return result;
    }

    /**
     * @see org.opencms.xml.content.I_CmsXmlContentHandler#isVisible(org.opencms.file.CmsObject, org.opencms.xml.types.I_CmsXmlSchemaType, java.lang.String, org.opencms.file.CmsResource, java.util.Locale)
     */
    public boolean isVisible(
        CmsObject cms,
        I_CmsXmlSchemaType contentValue,
        String valuePath,
        CmsResource resource,
        Locale contentLocale) {

        if (hasVisibilityHandlers() && m_visibilityConfigurations.containsKey(valuePath)) {
            VisibilityConfiguration config = m_visibilityConfigurations.get(valuePath);
            return config.getHandler().isValueVisible(
                cms,
                contentValue,
                valuePath,
                config.getParams(),
                resource,
                contentLocale);
        }
        return true;

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
        CmsMappingResolutionContext mappingContext = new CmsMappingResolutionContext();
        mappingContext.setCmsObject(cms);
        // pass the mapping context as a request context attribute to preserve interface compatibility
        cms.getRequestContext().setAttribute(ATTR_MAPPING_RESOLUTION_CONTEXT, mappingContext);
        content.resolveMappings(cms);
        // ensure all property or permission mappings of deleted optional values are removed
        removeEmptyMappings(cms, file, content);
        resolveDefaultMappings(cms, file, content);
        cms.getRequestContext().removeAttribute(ATTR_MAPPING_RESOLUTION_CONTEXT);
        mappingContext.finalizeMappings();
        // write categories (if there is a category widget present)
        file = writeCategories(cms, file, content);
        // return the result
        return file;
    }

    /**
     * @see org.opencms.xml.content.I_CmsXmlContentHandler#resolveMapping(org.opencms.file.CmsObject, org.opencms.xml.content.CmsXmlContent, org.opencms.xml.types.I_CmsXmlContentValue)
     */
    public void resolveMapping(CmsObject cms, CmsXmlContent content, I_CmsXmlContentValue value) throws CmsException {

        if (content.getFile() == null) {
            throw new CmsXmlException(Messages.get().container(Messages.ERR_XMLCONTENT_RESOLVE_FILE_NOT_FOUND_0));
        }

        // get the mappings for the element name
        boolean valueIsSimple = value.isSimpleType();
        String valuePath = value.getPath();
        int valueIndex = value.getIndex();
        Locale valueLocale = value.getLocale();
        CmsObject rootCms1 = createRootCms(cms);
        String originalStringValue = null;
        if (valueIsSimple) {
            originalStringValue = value.getStringValue(rootCms1);
        }
        resolveMapping(cms, content, valuePath, valueIsSimple, valueIndex, valueLocale, originalStringValue);
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
        String type)
    throws CmsXmlException {

        I_CmsXmlSchemaType schemaType = contentDefinition.getSchemaType(elementName);
        if (schemaType == null) {
            // no element with the given name
            throw new CmsXmlException(
                Messages.get().container(Messages.ERR_XMLCONTENT_CHECK_INVALID_ELEM_1, elementName));
        }
        if (!CmsXmlVfsFileValue.TYPE_NAME.equals(schemaType.getTypeName())
            && !CmsXmlVarLinkValue.TYPE_NAME.equals(schemaType.getTypeName())) {
            // element is not a OpenCmsVfsFile
            throw new CmsXmlException(
                Messages.get().container(Messages.ERR_XMLCONTENT_CHECK_INVALID_TYPE_1, elementName));
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
                        throw new CmsXmlException(
                            Messages.get().container(Messages.ERR_XMLCONTENT_CHECK_NOT_OPTIONAL_1, path));
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
        String configurationValue)
    throws CmsXmlException {

        if (contentDefinition.getSchemaType(elementName) == null) {
            throw new CmsXmlException(
                Messages.get().container(Messages.ERR_XMLCONTENT_CONFIG_ELEM_UNKNOWN_1, elementName));
        }

        m_configurationValues.put(elementName, configurationValue);
    }

    /**
     * Adds a default value for an element.<p>
     *
     * @param contentDefinition the XML content definition this XML content handler belongs to
     * @param elementName the element name to map
     * @param defaultValue the default value to use
     * @param resolveMacrosValue the value of the 'resolveMacros' attribute
     *
     * @throws CmsXmlException in case an unknown element name is used
     */
    protected void addDefault(
        CmsXmlContentDefinition contentDefinition,
        String elementName,
        String defaultValue,
        String resolveMacrosValue)
    throws CmsXmlException {

        if (contentDefinition.getSchemaType(elementName) == null) {
            throw new CmsXmlException(
                org.opencms.xml.types.Messages.get().container(
                    Messages.ERR_XMLCONTENT_INVALID_ELEM_DEFAULT_1,
                    elementName));
        }
        // store mappings as xpath to allow better control about what is mapped
        String xpath = CmsXmlUtils.createXpath(elementName, 1);
        m_defaultValues.put(xpath, defaultValue);

        // macros are resolved by default
        if ((resolveMacrosValue != null) && !Boolean.parseBoolean(resolveMacrosValue)) {
            m_nonMacroResolvableDefaults.add(xpath);
        }
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
        String elementPath)
    throws CmsXmlException {

        if ((schemaType != null) && schemaType.isSimpleType()) {
            if ((schemaType.getMinOccurs() == 0)
                && (CmsXmlVfsFileValue.TYPE_NAME.equals(schemaType.getTypeName())
                    || CmsXmlVarLinkValue.TYPE_NAME.equals(schemaType.getTypeName()))
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
     * Adds the given element to the compact view set.<p>
     *
     * @param contentDefinition the XML content definition this XML content handler belongs to
     * @param elementName the element name
     * @param displayType the display type to use for the element widget
     *
     * @throws CmsXmlException in case an unknown element name is used
     */
    protected void addDisplayType(
        CmsXmlContentDefinition contentDefinition,
        String elementName,
        DisplayType displayType)
    throws CmsXmlException {

        if (contentDefinition.getSchemaType(elementName) == null) {
            throw new CmsXmlException(
                Messages.get().container(Messages.ERR_XMLCONTENT_CONFIG_ELEM_UNKNOWN_1, elementName));
        }
        m_displayTypes.put(elementName, displayType);
    }

    /**
     * Adds an element mapping.<p>
     *
     * @param contentDefinition the XML content definition this XML content handler belongs to
     * @param elementName the element name to map
     * @param mapping the mapping to use
     * @param useDefault the 'useDefault' attribute
     *
     * @throws CmsXmlException in case an unknown element name is used
     */
    protected void addMapping(
        CmsXmlContentDefinition contentDefinition,
        String elementName,
        String mapping,
        String useDefault)
    throws CmsXmlException {

        if (contentDefinition.getSchemaType(elementName) == null) {
            throw new CmsXmlException(
                Messages.get().container(Messages.ERR_XMLCONTENT_INVALID_ELEM_MAPPING_1, elementName));
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
        if (Boolean.parseBoolean(useDefault)) {
            m_mappingsUsingDefault.add(xpath + ":" + mapping);
        }
        values.add(mapping);
        if (mapping.startsWith(MAPTO_PROPERTY) && mapping.endsWith(":" + CmsPropertyDefinition.PROPERTY_TITLE)) {
            // this is a title mapping
            m_titleMappings.add(xpath);
        }
    }

    /**
     * Adds a Solr field for an element.<p>
     *
     * @param contentDefinition the XML content definition this XML content handler belongs to
     * @param field the Solr field
     */
    @Deprecated
    protected void addSearchField(CmsXmlContentDefinition contentDefinition, CmsSearchField field) {

        addSearchField(contentDefinition, field, I_CmsXmlContentHandler.MappingType.ELEMENT);
    }

    /**
     * Adds a Solr field for an element.<p>
     *
     * @param contentDefinition the XML content definition this XML content handler belongs to
     * @param field the Solr field
     * @param type the type, specifying if the field should be attached to the document of the XML content or to all container pages the content is placed on
     */
    protected void addSearchField(
        CmsXmlContentDefinition contentDefinition,
        CmsSearchField field,
        I_CmsXmlContentHandler.MappingType type) {

        Locale locale = null;
        if (field instanceof CmsSolrField) {
            locale = ((CmsSolrField)field).getLocale();
        }
        String key = CmsXmlUtils.concatXpath(locale != null ? locale.toString() : null, field.getName());
        switch (type) {
            case PAGE:
                m_searchFieldsPage.put(key, field);
                break;
            case ELEMENT:
            default:
                m_searchFields.put(key, field);
                break;
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
            throw new CmsXmlException(
                Messages.get().container(Messages.ERR_XMLCONTENT_INVALID_ELEM_SEARCHSETTINGS_1, elementName));
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
        boolean isWarning)
    throws CmsXmlException {

        if (contentDefinition.getSchemaType(elementName) == null) {
            throw new CmsXmlException(
                Messages.get().container(Messages.ERR_XMLCONTENT_INVALID_ELEM_VALIDATION_1, elementName));
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
            throw new CmsXmlException(
                Messages.get().container(Messages.ERR_XMLCONTENT_INVALID_ELEM_LAYOUTWIDGET_1, elementName));
        }

        // get the base widget from the XML content type manager
        I_CmsWidget widget = OpenCms.getXmlContentTypeManager().getWidget(widgetClassOrAlias);

        if (widget == null) {
            // no registered widget class found
            if (CmsStringUtil.isValidJavaClassName(widgetClassOrAlias)) {
                // java class name given, try to create new instance of the class and cast to widget
                try {
                    Class<?> specialWidgetClass = Class.forName(widgetClassOrAlias);
                    if (I_CmsComplexWidget.class.isAssignableFrom(specialWidgetClass)) {
                        m_complexWidgets.put(elementName, (I_CmsComplexWidget)(specialWidgetClass.newInstance()));
                        return;
                    } else {
                        widget = (I_CmsWidget)specialWidgetClass.newInstance();
                    }
                } catch (Exception e) {
                    throw new CmsXmlException(
                        Messages.get().container(
                            Messages.ERR_XMLCONTENT_INVALID_CUSTOM_CLASS_3,
                            widgetClassOrAlias,
                            elementName,
                            contentDefinition.getSchemaLocation()),
                        e);
                }
            }
            if (widget == null) {
                // no valid widget found
                throw new CmsXmlException(
                    Messages.get().container(
                        Messages.ERR_XMLCONTENT_INVALID_WIDGET_3,
                        widgetClassOrAlias,
                        elementName,
                        contentDefinition.getSchemaLocation()));
            }
        }
        m_elementWidgets.put(elementName, widget);
    }

    /**
     * Returns the configured default locales for the content of the given resource.<p>
     *
     * @param cms the cms context
     * @param resource the resource path to get the default locales for
     *
     * @return the default locales of the resource
     */
    protected List<Locale> getLocalesForResource(CmsObject cms, String resource) {

        List<Locale> locales = OpenCms.getLocaleManager().getDefaultLocales(cms, resource);
        if ((locales == null) || locales.isEmpty()) {
            locales = OpenCms.getLocaleManager().getAvailableLocales();
        }
        return locales;
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
                List<Locale> locales = getLocalesForResource(cms, cms.getSitePath(resource));
                for (Locale l : locales) {
                    if (value.getLocale().equals(l)) {
                        return cms.getSitePath(resource);
                    }
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
            getMessages(OpenCms.getWorkplaceManager().getWorkplaceLocale(cms))).setAdditionalMacros(additionalValues);

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
        m_searchFields = new HashMap<String, CmsSearchField>();
        m_searchFieldsPage = new HashMap<String, CmsSearchField>();
        m_allowedTemplates = new CmsDefaultSet<String>();
        m_allowedTemplates.setDefaultMembership(true);
        m_displayTypes = new HashMap<String, DisplayType>();
        m_synchronizations = new ArrayList<String>();
        m_editorChangeHandlers = new ArrayList<I_CmsXmlContentEditorChangeHandler>();
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
            String resolveMacrosValue = element.attributeValue(APPINFO_ATTR_RESOLVE_MACROS);
            if ((elementName != null) && (defaultValue != null)) {
                // add a default value mapping for the element
                addDefault(contentDefinition, elementName, defaultValue, resolveMacrosValue);
            }
        }
    }

    /**
     * Initializes the default complex widget.<p>
     *
     * @param element the element in which the default complex widget is configured
     */
    protected void initDefaultWidget(Element element) {

        m_defaultWidget = element.attributeValue(APPINFO_ATTR_WIDGET);
        m_defaultWidgetConfig = element.attributeValue(APPINFO_ATTR_CONFIGURATION);
        try {
            m_defaultWidgetInstance = (I_CmsComplexWidget)(Class.forName(m_defaultWidget).newInstance());
        } catch (Exception e) {
            LOG.error(e);
        }
    }

    /**
     * Initializes the editor change handlers.<p>
     *
     * @param element the editorchangehandlers node of the app info
     */
    protected void initEditorChangeHandlers(Element element) {

        Iterator<Element> i = CmsXmlGenericWrapper.elementIterator(element, APPINFO_EDITOR_CHANGE_HANDLER);
        while (i.hasNext()) {
            // iterate all "default" elements in the "defaults" node
            Element handlerElement = i.next();
            String handlerClass = handlerElement.attributeValue(APPINFO_ATTR_CLASS);
            String configuration = handlerElement.attributeValue(APPINFO_ATTR_CONFIGURATION);
            String scope = handlerElement.attributeValue(APPINFO_ATTR_SCOPE);
            try {
                I_CmsXmlContentEditorChangeHandler handler = (I_CmsXmlContentEditorChangeHandler)Class.forName(
                    handlerClass).newInstance();
                handler.setConfiguration(configuration);
                handler.setScope(scope);
                m_editorChangeHandlers.add(handler);
            } catch (Exception e) {
                LOG.error(e.getLocalizedMessage(), e);
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
            m_formatters.add(
                new CmsFormatterBean(
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

        m_useAcacia = safeParseBoolean(root.attributeValue(ATTR_USE_ACACIA), true);
        Iterator<Element> i = CmsXmlGenericWrapper.elementIterator(root, APPINFO_LAYOUT);
        while (i.hasNext()) {
            // iterate all "layout" elements in the "layouts" node
            Element element = i.next();
            String elementName = element.attributeValue(APPINFO_ATTR_ELEMENT);
            String widgetClassOrAlias = element.attributeValue(APPINFO_ATTR_WIDGET);
            String configuration = element.attributeValue(APPINFO_ATTR_CONFIGURATION);
            String displayCompact = element.attributeValue(APPINFO_ATTR_DISPLAY);
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(displayCompact) && (elementName != null)) {
                addDisplayType(contentDefinition, elementName, DisplayType.valueOf(displayCompact));
            }
            if ((elementName != null) && CmsStringUtil.isNotEmptyOrWhitespaceOnly(widgetClassOrAlias)) {
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
            String useDefault = element.attributeValue(APPINFO_ATTR_USE_DEFAULT);
            if ((elementName != null) && (maptoName != null)) {
                // add the element mapping
                addMapping(contentDefinition, elementName, maptoName, useDefault);
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
            throw new CmsXmlException(
                Messages.get().container(
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
            throw new CmsXmlException(
                Messages.get().container(
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
                throw new CmsXmlException(
                    Messages.get().container(
                        Messages.ERR_XMLCONTENT_MISSING_RESOURCE_BUNDLE_NAME_2,
                        root.getName(),
                        contentDefinition.getSchemaLocation()));
            }
            if (!m_messageBundleNames.contains(messageBundleName)) {
                // avoid duplicates
                m_messageBundleNames.add(messageBundleName);
            }
            // clear the cached resource bundles for this bundle
            CmsResourceBundleLoader.flushBundleCache(messageBundleName, false);

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
                CmsResourceBundleLoader.flushBundleCache(propertyBundleName, false);
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
                CmsResourceBundleLoader.flushBundleCache(xmlBundleName, true);
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
                    boolean isDefaultLocaleAndNotNull = (locale != null)
                        && locale.equals(CmsLocaleManager.getDefaultLocale());

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
                        if (isDefaultLocaleAndNotNull) {
                            CmsResourceBundleLoader.addBundleToCache(xmlBundleName, null, xmlBundle);
                        }
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

        String containerPageOnly = root.attributeValue(APPINFO_ATTR_CONTAINER_PAGE_ONLY);
        m_containerPageOnly = (!CmsStringUtil.isEmpty(containerPageOnly))
            && (Boolean.valueOf(containerPageOnly).booleanValue());
        Iterator<Element> i = CmsXmlGenericWrapper.elementIterator(root, APPINFO_SEARCHSETTING);
        while (i.hasNext()) {
            Element element = i.next();
            String elementName = element.attributeValue(APPINFO_ATTR_ELEMENT);
            String searchContent = element.attributeValue(APPINFO_ATTR_SEARCHCONTENT);
            boolean include = (CmsStringUtil.isEmpty(searchContent)) || (Boolean.valueOf(searchContent).booleanValue());
            if (elementName != null) {
                addSearchSetting(contentDefinition, elementName, Boolean.valueOf(include));
            }
            Iterator<Element> it = CmsXmlGenericWrapper.elementIterator(element, APPINFO_SOLR_FIELD);
            Element solrElement;
            while (it.hasNext()) {
                solrElement = it.next();

                String localeNames = solrElement.attributeValue(APPINFO_ATTR_LOCALE);
                boolean localized = true;
                if ((localeNames != null)
                    && (localeNames.equals("none") || localeNames.equals("null") || localeNames.trim().equals(""))) {
                    localized = false;
                }
                List<Locale> locales = OpenCms.getLocaleManager().getAvailableLocales(localeNames);
                if (localized && ((locales == null) || locales.isEmpty())) {
                    locales = OpenCms.getLocaleManager().getAvailableLocales();
                } else if (locales.isEmpty()) {
                    locales.add(CmsLocaleManager.getDefaultLocale());
                }
                for (Locale locale : locales) {
                    String targetField = solrElement.attributeValue(APPINFO_ATTR_TARGET_FIELD);
                    if (localized) {
                        targetField = targetField + "_" + locale.toString();
                    }
                    String sourceField = solrElement.attributeValue(APPINFO_ATTR_SOURCE_FIELD);
                    if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(sourceField)) {
                        int lastUnderScore = sourceField.lastIndexOf("_");
                        if (lastUnderScore > 0) {
                            sourceField = sourceField.substring(lastUnderScore);
                        }
                        targetField += sourceField;
                    }

                    String copyFieldNames = solrElement.attributeValue(APPINFO_ATTR_COPY_FIELDS, "");
                    List<String> copyFields = CmsStringUtil.splitAsList(copyFieldNames, ',');
                    String defaultValue = solrElement.attributeValue(APPINFO_ATTR_DEFAULT);
                    String defaultBoost = String.valueOf(CmsSearchField.BOOST_DEFAULT);
                    float boost = Float.valueOf(
                        solrElement.attributeValue(APPINFO_ATTR_BOOST, defaultBoost)).floatValue();
                    CmsSolrField field = new CmsSolrField(targetField, copyFields, locale, defaultValue, boost);

                    // create the field mappings for this element
                    Iterator<Element> ite = CmsXmlGenericWrapper.elementIterator(solrElement, APPINFO_ATTR_MAPPING);
                    while (ite.hasNext()) {
                        Element mappingElement = ite.next();
                        field.addMapping(createSearchFieldMapping(contentDefinition, mappingElement, locale));
                    }

                    // if no mapping was defined yet, create a mapping for the element itself
                    if ((field.getMappings() == null) || field.getMappings().isEmpty()) {
                        String param = locale.toString() + "|" + elementName;
                        CmsSearchFieldMapping map = new CmsSearchFieldMapping(CmsSearchFieldMappingType.ITEM, param);
                        field.addMapping(map);
                    }
                    Set<I_CmsXmlContentHandler.MappingType> mappingTypes = parseSearchMappingTypes(solrElement);
                    for (I_CmsXmlContentHandler.MappingType type : mappingTypes) {
                        addSearchField(contentDefinition, field, type);
                    }
                }
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
     * Initializes the locale synchronizations elements.<p>
     *
     * @param root the synchronizations element of the content schema appinfo.
     * @param contentDefinition the content definition
     */
    protected void initSynchronizations(Element root, CmsXmlContentDefinition contentDefinition) {

        List<Element> elements = new ArrayList<Element>(CmsXmlGenericWrapper.elements(root, APPINFO_SYNCHRONIZATION));
        for (Element element : elements) {
            String elementName = element.attributeValue(APPINFO_ATTR_ELEMENT);
            m_synchronizations.add(elementName);
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
                Node descriptionNode = element.selectSingleNode(APPINFO_ATTR_DESCRIPTION + "/text()");
                String description = null;
                if (descriptionNode != null) {
                    description = descriptionNode.getText();
                }
                String tabName = element.attributeValue(APPINFO_ATTR_NAME, elementName);
                if (elementName != null) {
                    // add the element tab
                    m_tabs.add(
                        new CmsXmlContentTab(
                            elementName,
                            Boolean.valueOf(collapseValue).booleanValue(),
                            tabName,
                            description));
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
     * Initializes the forbidden template contexts.<p>
     *
     * @param root the root XML element
     * @param contentDefinition the content definition
     */
    protected void initTemplates(Element root, CmsXmlContentDefinition contentDefinition) {

        String strEnabledByDefault = root.attributeValue(ATTR_ENABLED_BY_DEFAULT);
        m_allowedTemplates.setDefaultMembership(safeParseBoolean(strEnabledByDefault, true));
        List<Element> elements = root.selectNodes(APPINFO_TEMPLATE);
        for (Element elem : elements) {
            boolean enabled = safeParseBoolean(elem.attributeValue(ATTR_ENABLED), true);
            String templateName = elem.getText().trim();
            m_allowedTemplates.setContains(templateName, enabled);
        }
        m_allowedTemplates.freeze();
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
     * Initializes the content visibility settings.<p>
     *
     * @param root the visibilities appinfo element
     * @param contentDefinition the content definition
     */
    protected void initVisibilities(Element root, CmsXmlContentDefinition contentDefinition) {

        m_visibilityConfigurations = new HashMap<String, VisibilityConfiguration>();
        String mainHandlerClassName = root.attributeValue(APPINFO_ATTR_CLASS);
        // using self as the default visibility handler implementation
        I_CmsXmlContentVisibilityHandler mainHandler = this;
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(mainHandlerClassName)) {
            try {
                // in case there is a main handler configured, try to instanciate it
                Class<?> handlerClass = Class.forName(mainHandlerClassName);
                mainHandler = (I_CmsXmlContentVisibilityHandler)handlerClass.newInstance();
            } catch (Exception e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        List<Element> elements = new ArrayList<Element>(CmsXmlGenericWrapper.elements(root, APPINFO_VISIBILITY));
        for (Element element : elements) {
            try {
                String elementName = element.attributeValue(APPINFO_ATTR_ELEMENT);
                String handlerClassName = element.attributeValue(APPINFO_ATTR_CLASS);
                String params = element.attributeValue(APPINFO_ATTR_PARAMS);
                I_CmsXmlContentVisibilityHandler handler = null;
                if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(handlerClassName)) {

                    Class<?> handlerClass = Class.forName(handlerClassName);
                    handler = (I_CmsXmlContentVisibilityHandler)handlerClass.newInstance();
                } else {
                    handler = mainHandler;
                }
                m_visibilityConfigurations.put(elementName, new VisibilityConfiguration(handler, params));

            } catch (Exception e) {
                LOG.error(e.getLocalizedMessage(), e);
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
     * @param solrElement the XML node of the &lt;solrfield&gt; node
     * @return parsed values of the attribute "addto"
     */
    protected Set<MappingType> parseSearchMappingTypes(Element solrElement) {

        Set<MappingType> result = new HashSet<MappingType>();
        String mappingTypes = solrElement.attributeValue(APPINFO_ATTR_ADD_TO);
        if (mappingTypes != null) {
            String[] types = mappingTypes.split(",");
            for (int i = 0; i < types.length; i++) {
                String type = types[i].trim();
                if (APPINFO_VALUE_ADD_TO_PAGE.equals(type)) {
                    result.add(MappingType.PAGE);
                } else if (APPINFO_VALUE_ADD_TO_CONTENT.equals(type)) {
                    result.add(MappingType.ELEMENT);
                }
            }
        } else {
            // for backwards compatibility
            result.add(MappingType.ELEMENT);
        }

        return result;
    }

    /**
     * Removes property values on resources for non-existing, optional elements.<p>
     *
     * @param cms the current users OpenCms context
     * @param file the file which is currently being prepared for writing
     * @param content the XML content to remove the property values for
     * @throws CmsException in case of read/write errors accessing the OpenCms VFS
     */
    protected void removeEmptyMappings(CmsObject cms, CmsFile file, CmsXmlContent content) throws CmsException {

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

                if (mapping.startsWith(MAPTO_ATTRIBUTE)
                    || mapping.startsWith(MAPTO_PROPERTY_LIST)
                    || mapping.startsWith(MAPTO_PROPERTY)) {
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

                        if (mapping.startsWith(MAPTO_PROPERTY_LIST) || mapping.startsWith(MAPTO_PROPERTY)) {

                            String property;
                            boolean shared = false;
                            if (mapping.startsWith(MAPTO_PROPERTY_LIST_INDIVIDUAL)) {
                                property = mapping.substring(MAPTO_PROPERTY_LIST_INDIVIDUAL.length());
                            } else if (mapping.startsWith(MAPTO_PROPERTY_LIST_SHARED)) {
                                property = mapping.substring(MAPTO_PROPERTY_LIST_SHARED.length());
                                shared = true;
                            } else if (mapping.startsWith(MAPTO_PROPERTY_LIST)) {
                                property = mapping.substring(MAPTO_PROPERTY_LIST.length());
                            } else if (mapping.startsWith(MAPTO_PROPERTY_SHARED)) {
                                property = mapping.substring(MAPTO_PROPERTY_SHARED.length());
                                shared = true;
                            } else if (mapping.startsWith(MAPTO_PROPERTY_INDIVIDUAL)) {
                                property = mapping.substring(MAPTO_PROPERTY_INDIVIDUAL.length());
                            } else {
                                property = mapping.substring(MAPTO_PROPERTY.length());
                            }
                            rootCms.writePropertyObject(
                                filename,
                                new CmsProperty(
                                    property,
                                    CmsProperty.DELETE_VALUE,
                                    shared ? CmsProperty.DELETE_VALUE : null));
                        } else if (mapping.startsWith(MAPTO_ATTRIBUTE)) {
                            if (mapping.equals(MAPTO_ATTRIBUTE + ATTRIBUTE_DATERELEASED)) {
                                rootCms.setDateReleased(filename, CmsResource.DATE_RELEASED_DEFAULT, false);
                                if (filename.equals(rootCms.getSitePath(file))) {
                                    file.setDateReleased(CmsResource.DATE_RELEASED_DEFAULT);
                                }
                            } else if (mapping.equals(MAPTO_ATTRIBUTE + ATTRIBUTE_DATEEXPIRED)) {
                                rootCms.setDateExpired(filename, CmsResource.DATE_EXPIRED_DEFAULT, false);
                                if (filename.equals(rootCms.getSitePath(file))) {
                                    file.setDateExpired(CmsResource.DATE_EXPIRED_DEFAULT);
                                }
                            }
                        }
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
     * Resolves those mappings for which no content value exists and useDefault is set to true.<p>
     *
     * @param cms the CMS context to use
     * @param file the content file
     * @param content the content object
     *
     * @throws CmsException if something goes wrong
     */
    protected void resolveDefaultMappings(CmsObject cms, CmsFile file, CmsXmlContent content) throws CmsException {

        for (Map.Entry<String, List<String>> e : m_elementMappings.entrySet()) {
            String path = e.getKey();
            List<String> mappings = e.getValue();
            if (mappings == null) {
                // nothing to do if we have no mappings at all
                continue;
            }
            for (int v = mappings.size() - 1; v >= 0; v--) {
                String mapping = mappings.get(v);
                if (!isMappingUsingDefault(path, mapping)) {
                    continue;
                }
                for (Locale locale : content.getLocales()) {
                    if (content.hasValue(path, locale)) {
                        continue;
                    } else {
                        String defaultValue = getDefault(cms, file, null, path, locale);
                        if (defaultValue != null) {
                            resolveMapping(cms, content, path, true, 0, locale, defaultValue);
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
        if (stringValue.isEmpty()) {
            return errorHandler;
        }
        try {
            String[] values = stringValue.split(",");
            for (int i = 0; i < values.length; i++) {
                String val = values[i];
                String catPath = CmsCategoryService.getInstance().getCategory(cms, val).getPath();
                String refPath = getReferencePath(cms, value);
                CmsCategoryService.getInstance().readCategory(cms, catPath, refPath);
                if (((CmsCategoryWidget)widget).isOnlyLeafs()) {
                    if (!CmsCategoryService.getInstance().readCategories(cms, catPath, false, refPath).isEmpty()) {
                        errorHandler.addError(
                            value,
                            Messages.get().getBundle(value.getLocale()).key(
                                Messages.GUI_CATEGORY_CHECK_NOLEAF_ERROR_0));
                    }
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

            // check for links to static resources
            if (CmsStaticResourceHandler.isStaticResourceUri(sitePath)) {
                return false;
            }
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
                List<Locale> locales = getLocalesForResource(tmpCms, resource.getRootPath());
                Locale locale = locales.get(0);
                for (Locale l : locales) {
                    if (content.hasLocale(l)) {
                        locale = l;
                        break;
                    }
                }
                // remove all previously set categories
                boolean clearedCategories = false;
                // iterate over all values checking for the category widget
                CmsXmlContentWidgetVisitor widgetCollector = new CmsXmlContentWidgetVisitor(locale);
                content.visitAllValuesWith(widgetCollector);
                Iterator<Map.Entry<String, I_CmsXmlContentValue>> itWidgets = widgetCollector.getValues().entrySet().iterator();
                while (itWidgets.hasNext()) {
                    Map.Entry<String, I_CmsXmlContentValue> entry = itWidgets.next();
                    String xpath = entry.getKey();
                    I_CmsWidget widget = widgetCollector.getWidgets().get(xpath);
                    I_CmsXmlContentValue value = entry.getValue();
                    if (!(widget instanceof CmsCategoryWidget)
                        || value.getTypeName().equals(CmsXmlDynamicCategoryValue.TYPE_NAME)) {
                        // ignore other values than categories
                        continue;
                    }
                    if (!clearedCategories) {
                        CmsCategoryService.getInstance().clearCategoriesForResource(tmpCms, resource.getRootPath());
                        clearedCategories = true;
                    }
                    String stringValue = value.getStringValue(tmpCms);
                    if (CmsStringUtil.isEmptyOrWhitespaceOnly(stringValue)) {
                        // skip empty values
                        continue;
                    }
                    try {
                        // add the file to the selected category
                        String[] catRootPathes = stringValue.split(",");
                        for (String catRootPath : catRootPathes) {
                            CmsCategory cat = CmsCategoryService.getInstance().getCategory(tmpCms, catRootPath);
                            CmsCategoryService.getInstance().addResourceToCategory(
                                tmpCms,
                                resource.getRootPath(),
                                cat.getPath());
                        }
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

    /**
     * Creates a search field mapping for the given mapping element and the locale.<p>
     *
     * @param contentDefinition the content definition
     * @param element the mapping element configured in the schema
     * @param locale the locale
     *
     * @return the created search field mapping
     *
     * @throws CmsXmlException if the dynamic field class could not be found
     */
    private I_CmsSearchFieldMapping createSearchFieldMapping(
        CmsXmlContentDefinition contentDefinition,
        Element element,
        Locale locale)
    throws CmsXmlException {

        I_CmsSearchFieldMapping fieldMapping = null;
        String typeAsString = element.attributeValue(APPINFO_ATTR_TYPE);
        CmsSearchFieldMappingType type = CmsSearchFieldMappingType.valueOf(typeAsString);
        switch (type.getMode()) {
            case 0: // content
            case 3: // item
                // localized
                String param = locale.toString() + "|" + element.getStringValue();
                fieldMapping = new CmsSearchFieldMapping(type, param);
                break;
            case 1: // property
            case 2: // property-search
            case 5: // attribute
                // not localized
                fieldMapping = new CmsSearchFieldMapping(type, element.getStringValue());
                break;
            case 4: // dynamic
                String mappingClass = element.attributeValue(APPINFO_ATTR_CLASS);
                if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(mappingClass)) {
                    try {
                        fieldMapping = (I_CmsSearchFieldMapping)Class.forName(mappingClass).newInstance();
                        fieldMapping.setType(CmsSearchFieldMappingType.DYNAMIC);
                        fieldMapping.setParam(element.getStringValue());
                    } catch (Exception e) {
                        throw new CmsXmlException(
                            Messages.get().container(
                                Messages.ERR_XML_SCHEMA_MAPPING_CLASS_NOT_EXIST_3,
                                mappingClass,
                                contentDefinition.getTypeName(),
                                contentDefinition.getSchemaLocation()));
                    }

                }
                break;
            default:
                // NOOP
        }
        if (fieldMapping != null) {
            fieldMapping.setDefaultValue(element.attributeValue(APPINFO_ATTR_DEFAULT));
        }
        return fieldMapping;
    }

    /**
     * Utility method to return a path fragment.<p>
     *
     * @param pathElements the path elements
     * @param begin the begin index
     *
     * @return the path
     */
    private String getSubPath(String[] pathElements, int begin) {

        String result = "";
        for (int i = begin; i < pathElements.length; i++) {
            result += pathElements[i] + "/";
        }
        if (result.length() > 0) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    /**
     * Initializes the message key fall back handler.<p>
     *
     * @param element the XML element node
     */
    private void initMessageKeyHandler(Element element) {

        String className = element.attributeValue(APPINFO_ATTR_CLASS);
        String configuration = element.attributeValue(APPINFO_ATTR_CONFIGURATION);
        try {
            Object messageKeyHandler = Class.forName(className).getConstructor(String.class).newInstance(configuration);
            m_messageKeyHandler = (CmsMultiMessages.I_KeyFallbackHandler)messageKeyHandler;
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Checks if the given mapping has the 'useDefault' flag set to true.<p>
     *
     * @param path the mapping path
     * @param mapping the mapping type
     *
     * @return true if 'useDefault' is enabled for this mapping
     */
    private boolean isMappingUsingDefault(String path, String mapping) {

        String key = path + ":" + mapping;
        return m_mappingsUsingDefault.contains(key);
    }

    /**
     * Helper method which does most of the mapping resolution work.<p>
     *
     * @param cms the CMS context to use
     * @param content the content object
     * @param valuePath the xpath of the value
     * @param valueIsSimple true if this is a simple value
     * @param valueIndex the index of the value
     * @param valueLocale the locale of the value
     * @param originalStringValue the value as a string
     *
     * @throws CmsException if something goes wrong
     */
    private void resolveMapping(
        CmsObject cms,
        CmsXmlContent content,
        String valuePath,
        boolean valueIsSimple,
        int valueIndex,
        Locale valueLocale,
        String originalStringValue)
    throws CmsException {

        CmsObject rootCms = createRootCms(cms);
        // get the original VFS file from the content
        CmsFile file = content.getFile();
        if (!valueIsSimple) {
            // no mappings for a nested schema are possible
            // note that the sub-elements of the nested schema ARE mapped by the node visitor,
            // it's just the nested schema value itself that does not support mapping
            return;
        }

        List<String> mappings = getMappings(valuePath);
        if (mappings.size() == 0) {
            // nothing to do if we have no mappings at all
            return;
        }
        // create OpenCms user context initialized with "/" as site root to read all siblings
        // read all siblings of the file
        List<CmsResource> siblings = rootCms.readSiblings(
            content.getFile().getRootPath(),
            CmsResourceFilter.IGNORE_EXPIRATION);

        Set<CmsResource> urlNameMappingResources = new HashSet<CmsResource>();
        boolean mapToUrlName = false;
        urlNameMappingResources.add(content.getFile());
        // since 7.0.2 multiple mappings are possible

        // get the string value of the current node

        CmsGalleryNameMacroResolver resolver = new CmsGalleryNameMacroResolver(rootCms, content, valueLocale);
        resolver.setKeepEmptyMacros(true);
        String stringValue = resolver.resolveMacros(originalStringValue);

        for (String mapping : mappings) {

            // for multiple language mappings, we need to ensure
            // a) all siblings are handled
            // b) only the "right" locale is mapped to a sibling
            if (CmsStringUtil.isNotEmpty(mapping)) {
                for (int i = (siblings.size() - 1); i >= 0; i--) {
                    // get filename
                    String filename = (siblings.get(i)).getRootPath();
                    if (mapping.startsWith(MAPTO_URLNAME)) {
                        // should be written regardless of whether there is a sibling with the correct locale
                        mapToUrlName = true;
                    }
                    Locale locale = OpenCms.getLocaleManager().getDefaultLocale(rootCms, filename);
                    if (!locale.equals(valueLocale)) {
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

                    if (mapping.startsWith(MAPTO_PERMISSION) && (valueIndex == 0)) {

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
                        String path = CmsXmlUtils.removeXpathIndex(valuePath);
                        List<I_CmsXmlContentValue> values = content.getValues(path, valueLocale);
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
                    } else if (mapping.startsWith(MAPTO_PROPERTY_LIST) && (valueIndex == 0)) {

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

                        String path = CmsXmlUtils.removeXpathIndex(valuePath);
                        List<I_CmsXmlContentValue> values = content.getValues(path, valueLocale);
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
            CmsMappingResolutionContext context = (CmsMappingResolutionContext)(cms.getRequestContext().getAttribute(
                ATTR_MAPPING_RESOLUTION_CONTEXT));
            for (CmsResource resourceForUrlNameMapping : urlNameMappingResources) {
                if (!CmsResource.isTemporaryFileName(resourceForUrlNameMapping.getRootPath())) {
                    String mappedName = stringValue;
                    if (!CmsStringUtil.isEmptyOrWhitespaceOnly(mappedName)) {
                        mappedName = mappedName.trim();
                        context.addUrlNameMapping(mappedName, valueLocale, resourceForUrlNameMapping.getStructureId());
                    }
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
     * Parses a boolean from a string and returns a default value if the string couldn't be parsed.<p>
     *
     * @param text the text from which to get the boolean value
     * @param defaultValue the value to return if parsing fails
     *
     * @return the parsed boolean
     */
    private boolean safeParseBoolean(String text, boolean defaultValue) {

        if (text == null) {
            return defaultValue;
        }
        try {
            return Boolean.parseBoolean(text);
        } catch (Throwable t) {
            return defaultValue;
        }
    }

}
