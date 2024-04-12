/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.ade.configuration;

import org.opencms.ade.configuration.CmsADEConfigDataInternal.ConfigReference;
import org.opencms.ade.configuration.CmsADEConfigDataInternal.ConfigReferenceMeta;
import org.opencms.ade.configuration.formatters.CmsFormatterChangeSet;
import org.opencms.ade.configuration.formatters.CmsFormatterConfigurationCache;
import org.opencms.ade.containerpage.shared.CmsCntPageData.ElementDeleteMode;
import org.opencms.ade.detailpage.CmsDetailPageInfo;
import org.opencms.ade.galleries.CmsAddContentRestriction;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.gwt.CmsIconUtil;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.module.CmsModule;
import org.opencms.relations.CmsLink;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.xml.containerpage.CmsFormatterBean;
import org.opencms.xml.containerpage.CmsXmlDynamicFunctionHandler;
import org.opencms.xml.containerpage.I_CmsFormatterBean;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.content.CmsXmlContentProperty;
import org.opencms.xml.content.CmsXmlContentProperty.Visibility;
import org.opencms.xml.content.CmsXmlContentRootLocation;
import org.opencms.xml.content.I_CmsXmlContentLocation;
import org.opencms.xml.content.I_CmsXmlContentValueLocation;
import org.opencms.xml.types.CmsXmlVarLinkValue;
import org.opencms.xml.types.CmsXmlVfsFileValue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.google.common.collect.Lists;

/**
 * A class to parse ADE sitemap or module configuration files and create configuration objects from them.<p>
 */
public class CmsConfigurationReader {

    /**
     * Enum describing how to deal with inherited properties.
     */
    enum DiscardPropertiesMode {

        /** Remove properties from parent sitemaps. */
        discard("true"),

        /** Inherit properties from parent sitemaps. */
        keep("false"),

        /** Remove properties from parent sitemaps, and mark properties defined in this sitemap as 'top', which moves them over the properties which are only defined in opencms-workplace.xml/opencms-modules.xml. */
        top("top");

        /** The value representing the mode in an actual sitemap configuration content. */
        private String m_stringValue;

        /**
         * Creates the enum value.
         *
         * @param stringValue the string value from the configuration
         */
        private DiscardPropertiesMode(String stringValue) {

            m_stringValue = stringValue;

        }

        /**
         * Gets the string value occurring in the  sitemap configuration.
         *
         * @return the string value
         */
        public String getStringValue() {

            return m_stringValue;
        }
    }

    /** The default locale for configuration objects. */
    public static final Locale DEFAULT_LOCALE = CmsLocaleManager.getLocale("en");

    /** The AddContentReplacements node name. */
    public static final String N_ADD_CONTENT_RESTRICTION = "AddContentRestriction";

    /** Node name for added formatters. */
    public static final String N_ADD_FORMATTER = "AddFormatter";

    /** Node name for the nested content with the added formatters. */
    public static final String N_ADD_FORMATTERS = "AddFormatters";

    /** The AddPlugin node name. */
    public static final String N_ADD_PLUGIN = "AddPlugin";

    /** The AddPlugins node name. */
    public static final String N_ADD_PLUGINS = "AddPlugins";

    /** The Attribute node name. */
    public static final String N_ATTRIBUTE = "Attribute";

    /** Node name for the attribute editor configuration reference. */
    public static final String N_ATTRIBUTE_EDITOR_CONFIG = "AttributeEditorConfig";

    /** Node name for the 'Check reuse' option. */
    public static final String N_CHECK_REUSE = "CheckReuse";

    /** The CopyInModels node name. */
    public static final String N_COPY_IN_MODELS = "CopyInModels";

    /** The create content locally node name. */
    public static final String N_CREATE_CONTENTS_LOCALLY = "CreateContentsLocally";

    /** The default node name. */
    public static final String N_DEFAULT = "Default";

    /** The description node name. */
    public static final String N_DESCRIPTION = "Description";

    /** The detail page node name. */
    public static final String N_DETAIL_PAGE = "DetailPage";

    /** The detail pages disabled node name. */
    public static final String N_DETAIL_PAGES_DISABLED = "DetailPagesDisabled";

    /** The disabled node name. */
    public static final String N_DISABLED = "Disabled";

    /** The DisabledFunctionsMode node name. */
    public static final String N_DISABLED_FUNCTIONS_MODE = "DisabledFunctionsMode";

    /** The DisabledTypesMode node name. */
    public static final String N_DISABLED_TYPES_MODE = "DisabledTypesMode";

    /** The discard model pages node name. */
    public static final String N_DISCARD_MODEL_PAGES = "DiscardModelPages";

    /** The discard properties node name. */
    public static final String N_DISCARD_PROPERTIES = "DiscardProperties";

    /** The discard types node name. */
    public static final String N_DISCARD_TYPES = "DiscardTypes";

    /** The display name node name. */
    public static final String N_DISPLAY_NAME = "DisplayName";

    /** The element view node name. */
    public static final String N_ELEMENT_VIEW = "ElementView";

    /** The error node name. */
    public static final String N_ERROR = "Error";

    /** The 'exclude external detail contents' node name. */
    public static final String N_EXCLUDE_EXTERNAL_DETAIL_CONTENTS = "ExcludeExternalDetailContents";

    /** The folder node name. */
    public static final String N_FOLDER = "Folder";

    /** The formatter node name. */
    public static final String N_FORMATTER = "Formatter";

    /** The function node name. */
    public static final String N_FUNCTION = "Function";

    /** The function node name. */
    public static final String N_FUNCTION_DEFAULT_PAGE = "FunctionDefaultPage";

    /** The function reference node name. */
    public static final String N_FUNCTION_REF = "FunctionRef";

    /** The 'include in site selector' node name. */
    public static final String N_INCLUDE_IN_SITE_SELECTOR = "IncludeInSiteSelector";

    /** The IncludeName node name. */
    public static final String N_INCLUDE_NAME = "IncludeName";

    /** The is default node name. */
    public static final String N_IS_DEFAULT = "IsDefault";

    /** The is preview node name. */
    public static final String N_IS_PREVIEW = "IsPreview";

    /** The JSP node name. */
    public static final String N_JSP = "Jsp";

    /** The Key node name. */
    public static final String N_KEY = "Key";

    /** The localization node name. */
    public static final String N_LOCALIZATION = "Localization";

    /** The master configuration node name. */
    public static final String N_MASTER_CONFIG = "MasterConfig";

    /** The max width node name. */
    public static final String N_MAX_WIDTH = "MaxWidth";

    /** The min width node name. */
    public static final String N_MIN_WIDTH = "MinWidth";

    /** The model page node name. */
    public static final String N_MODEL_PAGE = "ModelPage";

    /** The folder name node name. */
    public static final String N_NAME = "Name";

    /** The name pattern node name. */
    public static final String N_NAME_PATTERN = "NamePattern";

    /** The order node name. */
    public static final String N_ORDER = "Order";

    /** The page node name. */
    public static final String N_PAGE = "Page";

    /** The PageRelative node name. */
    public static final String N_PAGE_RELATIVE = "PageRelative";

    /** The folder path node name. */
    public static final String N_PATH = "Path";

    /** The Plugin node name. */
    public static final String N_PLUGIN = "Plugin";

    /** The  PreferDetailPagesForLocalContents node name. */
    public static final String N_PREFER_DETAIL_PAGES_FOR_LOCAL_CONTENTS = "PreferDetailPagesForLocalContents";

    /** The prefer folder node name. */
    public static final String N_PREFER_FOLDER = "PreferFolder";

    /** The property node name. */
    public static final String N_PROPERTY = "Property";

    /** The property name node name. */
    public static final String N_PROPERTY_NAME = "PropertyName";

    /** XML node name. */
    public static final String N_PROPERTY_NAME_ALIAS = "PropertyNameAlias";

    /** Node name for the "Remove all formatters"-option. */
    public static final String N_REMOVE_ALL_FORMATTERS = "RemoveAllFormatters";

    /** Field name for the 'Remove all functions' setting. */
    public static final String N_REMOVE_ALL_FUNCTIONS = "RemoveAllFunctions";

    /** The RemoveAllPlugins node name. */
    public static final String N_REMOVE_ALL_PLUGINS = "RemoveAllPlugins";

    /** The RemoveAllSharedSettingOverrides node name. */
    public static final String N_REMOVE_ALL_SHARED_SETTING_OVERRIDES = "RemoveAllSharedSettingOverrides";

    /** Node name for removed formatters. */
    public static final String N_REMOVE_FORMATTER = "RemoveFormatter";

    /** Node name for the nested content with the removed formatters. */
    public static final String N_REMOVE_FORMATTERS = "RemoveFormatters";

    /** The remove function node name. */
    public static final String N_REMOVE_FUNCTIONS = "RemoveFunctions";

    /** The RemovePlugin node name. */
    public static final String N_REMOVE_PLUGIN = "RemovePlugin";

    /** The RemovePlugins node name. */
    public static final String N_REMOVE_PLUGINS = "RemovePlugins";

    /** The resource type node name. */
    public static final String N_RESOURCE_TYPE = "ResourceType";

    /** The regex rule node name. */
    public static final String N_RULE_REGEX = "RuleRegex";

    /** The rule type node name. */
    public static final String N_RULE_TYPE = "RuleType";

    /** The SharedSettingOverride node name. */
    public static final String N_SHARED_SETTING_OVERRIDE = "SharedSettingOverride";

    /** The ShowInDefaultView node name. */
    public static final String N_SHOW_IN_DEFAULT_VIEW = "ShowInDefaultView";

    /** The type node name. */
    public static final String N_TYPE = "Type";

    /** The type name node name. */
    public static final String N_TYPE_NAME = "TypeName";

    /** The node name for the type ordering mode. */
    public static final String N_TYPE_ORDERING_MODE = "TypeOrderingMode";

    /** Node name. */
    public static final String N_USE_FORMATTER_KEYS = "UseFormatterKeys";

    /** The Value node name. */
    public static final String N_VALUE = "Value";

    /** XML node name. */
    public static final String N_VALUE_TRANSLATION = "ValueTranslation";

    /** The widget node name. */
    public static final String N_VISIBILITY = "Visibility";

    /** The widget node name. */
    public static final String N_WIDGET = "Widget";

    /** The widget configuration node name. */
    public static final String N_WIDGET_CONFIG = "WidgetConfig";

    /** Scheme for explorer type view links. */
    public static final String VIEW_SCHEME = "view://";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsConfigurationReader.class);

    /** The ElementDeleteMode node name. */
    private static final String N_ELEMENT_DELETE_MODE = "ElementDeleteMode";

    /** The CMS context used for reading the configuration data. */
    private CmsObject m_cms;

    /** The parsed detail page configuration elements. */
    private List<CmsDetailPageInfo> m_detailPageConfigs = new ArrayList<CmsDetailPageInfo>();

    /** The list of configured function references. */
    private List<CmsFunctionReference> m_functionReferences = new ArrayList<CmsFunctionReference>();

    /** The parsed model page configuration elements. */
    private List<CmsModelPageConfigWithoutResource> m_modelPageConfigs = new ArrayList<CmsModelPageConfigWithoutResource>();

    /** The parsed property configuration elements. */
    private List<CmsPropertyConfig> m_propertyConfigs = new ArrayList<CmsPropertyConfig>();

    /** The resource type configuration objects. */
    private List<CmsResourceTypeConfig> m_resourceTypeConfigs = new ArrayList<CmsResourceTypeConfig>();

    /**
     * Creates a new configuration reader.<p>
     *
     * @param cms the CMS context which should be used to read the configuration data.<p>
     */
    public CmsConfigurationReader(CmsObject cms) {

        m_cms = cms;
    }

    /**
     * Gets the string value of an XML content location.<p>
     *
     * @param cms the CMS context to use
     * @param location an XML content location
     *
     * @return the string value of that XML content location
     */
    public static String getString(CmsObject cms, I_CmsXmlContentValueLocation location) {

        if (location == null) {
            return null;
        }
        return location.asString(cms);
    }

    /**
     * Helper method to parse a property.<p>
     *
     * @param cms the CMS context to use
     * @param field the location of the parent value
     *
     * @return the parsed property configuration
     */
    public static CmsPropertyConfig parseProperty(CmsObject cms, I_CmsXmlContentLocation field) {

        String name = getString(cms, field.getSubValue(N_PROPERTY_NAME));
        String includeName = getString(cms, field.getSubValue(N_INCLUDE_NAME));
        String widget = getString(cms, field.getSubValue(N_WIDGET));
        String widgetConfig = getString(cms, field.getSubValue(N_WIDGET_CONFIG));
        String ruleRegex = getString(cms, field.getSubValue(N_RULE_REGEX));
        String ruleType = getString(cms, field.getSubValue(N_RULE_TYPE));
        String default1 = getString(cms, field.getSubValue(N_DEFAULT));
        String error = getString(cms, field.getSubValue(N_ERROR));
        String niceName = getString(cms, field.getSubValue(N_DISPLAY_NAME));
        String description = getString(cms, field.getSubValue(N_DESCRIPTION));
        String preferFolder = getString(cms, field.getSubValue(N_PREFER_FOLDER));
        String aliasName = getString(cms, field.getSubValue(N_PROPERTY_NAME_ALIAS));
        String valueTranslations = getString(cms, field.getSubValue(N_VALUE_TRANSLATION));

        String disabledStr = getString(cms, field.getSubValue(N_DISABLED));
        boolean disabled = ((disabledStr != null) && Boolean.parseBoolean(disabledStr));

        String orderStr = getString(cms, field.getSubValue(N_ORDER));
        int order = I_CmsConfigurationObject.DEFAULT_ORDER;

        try {
            order = Integer.parseInt(orderStr);
        } catch (NumberFormatException e) {
            // noop
        }

        Visibility visibility;
        String visibilityStr = getString(cms, field.getSubValue(N_VISIBILITY));
        try {
            // to stay compatible with former visibility option values
            if ("both".equals(visibilityStr)) {
                visibilityStr = Visibility.elementAndParentIndividual.name();
            } else if ("parent".equals(visibilityStr)) {
                visibilityStr = Visibility.parentShared.name();
            }
            visibility = Visibility.valueOf(visibilityStr);
        } catch (Exception e) {
            visibility = null;
        }
        CmsXmlContentProperty prop = new CmsXmlContentProperty(
            name,
            aliasName,
            "string",
            visibility,
            widget,
            widgetConfig,
            ruleRegex,
            ruleType,
            default1,
            niceName,
            description,
            error,
            preferFolder,
            valueTranslations).withIncludeName(includeName);
        // since these are real properties, using type vfslist makes no sense, so we always use the "string" type
        CmsPropertyConfig propConfig = new CmsPropertyConfig(prop, disabled, order);
        return propConfig;

    }

    /**
     * Returns the list of function references.<p>
     *
     * @return the list of function references
     */
    public List<CmsFunctionReference> getFunctionReferences() {

        return new ArrayList<CmsFunctionReference>(m_functionReferences);
    }

    /**
     * Returns the modelPageConfigs.<p>
     *
     * @return the modelPageConfigs
     */
    public List<CmsModelPageConfigWithoutResource> getModelPageConfigs() {

        return m_modelPageConfigs;
    }

    /**
     * Parses the formatters to add.<p>
     *
     * @param node the parent node
     * @return the set of keys of the formatters to add
     */
    public Set<String> parseAddFormatters(I_CmsXmlContentLocation node) {

        Set<String> addFormatters = new HashSet<String>();
        for (I_CmsXmlContentValueLocation addLoc : node.getSubValues(N_ADD_FORMATTERS + "/" + N_ADD_FORMATTER)) {
            CmsXmlVfsFileValue value = (CmsXmlVfsFileValue)addLoc.getValue();
            CmsLink link = value.getLink(m_cms);
            if (link != null) {
                CmsUUID structureId = link.getStructureId();
                if (structureId != null) {
                    addFormatters.add(structureId.toString());
                }
            }
        }
        return addFormatters;
    }

    /**
     * Parses a configuration XML content and creates a configuration object from it.<p>
     *
     * @param basePath the base path
     * @param content the XML content
     *
     * @return the created configuration object with the data from the XML content
     * @throws CmsException if something goes wrong
     */
    public CmsADEConfigDataInternal parseConfiguration(String basePath, CmsXmlContent content) throws CmsException {

        m_detailPageConfigs = Lists.newArrayList();
        m_functionReferences = Lists.newArrayList();
        m_modelPageConfigs = Lists.newArrayList();
        m_propertyConfigs = Lists.newArrayList();
        m_resourceTypeConfigs = Lists.newArrayList();

        if (!content.hasLocale(DEFAULT_LOCALE)) {
            return CmsADEConfigDataInternal.emptyConfiguration(basePath);
        }
        CmsXmlContentRootLocation root = new CmsXmlContentRootLocation(content, DEFAULT_LOCALE);
        for (I_CmsXmlContentValueLocation node : root.getSubValues(N_RESOURCE_TYPE)) {
            try {
                parseResourceTypeConfig(basePath, node);
            } catch (CmsException e) {
                LOG.warn(e.getLocalizedMessage(), e);
            }
        }
        for (I_CmsXmlContentValueLocation node : root.getSubValues(N_MODEL_PAGE)) {
            try {
                parseModelPage(node);
            } catch (Exception e) {
                LOG.warn(e.getLocalizedMessage(), e);
            }
        }
        for (I_CmsXmlContentLocation node : root.getSubValues(N_DETAIL_PAGE)) {
            try {
                parseDetailPage(node);
            } catch (Exception e) {
                LOG.warn(e.getLocalizedMessage(), e);
            }
        }

        for (I_CmsXmlContentLocation node : root.getSubValues(N_FUNCTION_REF)) {
            parseFunctionReference(node);
        }

        CmsUUID sharedSettingOverride = null;
        for (I_CmsXmlContentValueLocation node : root.getSubValues(N_SHARED_SETTING_OVERRIDE)) {
            sharedSettingOverride = ((CmsXmlVfsFileValue)node.getValue()).getLink(m_cms).getStructureId();
        }

        boolean removeSharedSettingOverrides = getBoolean(root, N_REMOVE_ALL_SHARED_SETTING_OVERRIDES);

        boolean removeFunctions = false;
        removeFunctions = getBoolean(root, N_REMOVE_ALL_FUNCTIONS);

        Set<CmsUUID> functions = new LinkedHashSet<>();
        for (I_CmsXmlContentValueLocation node : root.getSubValues(N_FUNCTION)) {
            CmsXmlVfsFileValue value = (CmsXmlVfsFileValue)node.getValue();
            CmsLink link = value.getLink(m_cms);
            if (link != null) {
                CmsUUID structureId = link.getStructureId();
                if (structureId != null) {
                    functions.add(link.getStructureId());
                }
            }
        }

        Set<CmsUUID> functionsToRemove = new LinkedHashSet<>();
        for (I_CmsXmlContentValueLocation parent : root.getSubValues(N_REMOVE_FUNCTIONS)) {
            for (I_CmsXmlContentValueLocation node : parent.getSubValues(N_FUNCTION)) {
                CmsXmlVfsFileValue value = (CmsXmlVfsFileValue)node.getValue();
                CmsLink link = value.getLink(m_cms);
                if (link != null) {
                    CmsUUID structureId = link.getStructureId();
                    if (structureId != null) {
                        functionsToRemove.add(link.getStructureId());
                    }
                }
            }
        }

        boolean removeAllPlugins = getBoolean(root, N_REMOVE_ALL_PLUGINS);
        Set<CmsUUID> pluginsToRemove = readInternalLinkListTargetIds(root, N_REMOVE_PLUGINS, N_PLUGIN);
        Set<CmsUUID> pluginsToAdd = readInternalLinkListTargetIds(root, N_ADD_PLUGINS, N_PLUGIN);

        boolean removeAllFormatters = getBoolean(root, N_REMOVE_ALL_FORMATTERS);
        CmsFormatterChangeSet formatterChangeSet = parseFormatterChangeSet(
            basePath,
            root,
            removeAllFormatters,
            removeFunctions,
            functions,
            functionsToRemove);
        boolean discardInheritedTypes = getBoolean(root, N_DISCARD_TYPES);
        // boolean discardInheritedProperties = getBoolean(root, N_DISCARD_PROPERTIES);
        I_CmsXmlContentValueLocation discardPropertiesLoc = root.getSubValue(N_DISCARD_PROPERTIES);
        DiscardPropertiesMode discardPropertiesMode = DiscardPropertiesMode.keep;
        if (discardPropertiesLoc != null) {
            String discardPropertiesStr = discardPropertiesLoc.getValue().getStringValue(m_cms);
            for (DiscardPropertiesMode discardMode : DiscardPropertiesMode.values()) {
                if (discardMode.getStringValue().contentEquals(discardPropertiesStr)) {
                    discardPropertiesMode = discardMode;
                }
            }
        }
        for (I_CmsXmlContentLocation node : root.getSubValues(N_PROPERTY)) {
            parseProperty(node, discardPropertiesMode);
        }

        boolean discardInheritedModelPages = getBoolean(root, N_DISCARD_MODEL_PAGES);

        boolean createContentsLocally = getBoolean(root, N_CREATE_CONTENTS_LOCALLY);
        boolean preferDetailPagesForLocalContents = getBoolean(root, N_PREFER_DETAIL_PAGES_FOR_LOCAL_CONTENTS);
        boolean exludeExternalDetailContents = getBoolean(root, N_EXCLUDE_EXTERNAL_DETAIL_CONTENTS);
        boolean includeInSiteSelector = getBoolean(root, N_INCLUDE_IN_SITE_SELECTOR);

        String galleryDisabledTypesStr = getString(root.getSubValue(N_DISABLED_TYPES_MODE));
        CmsGalleryDisabledTypesMode galleryDisabledTypesMode = null;
        if (galleryDisabledTypesStr != null) {
            galleryDisabledTypesMode = CmsGalleryDisabledTypesMode.valueOf(galleryDisabledTypesStr);
        }

        String galleryDisabledFunctionsStr = getString(root.getSubValue(N_DISABLED_FUNCTIONS_MODE));
        CmsGalleryDisabledTypesMode galleryDisabledFunctionsMode = null;
        if (galleryDisabledFunctionsStr != null) {
            galleryDisabledFunctionsMode = CmsGalleryDisabledTypesMode.valueOf(galleryDisabledFunctionsStr);
        }

        I_CmsXmlContentValueLocation typeOrderingLoc = root.getSubValue(N_TYPE_ORDERING_MODE);
        CmsTypeOrderingMode typeOrderingMode = null;
        if (typeOrderingLoc != null) {
            boolean byDisplayOrder = Boolean.parseBoolean(typeOrderingLoc.getValue().getStringValue(m_cms));
            typeOrderingMode = byDisplayOrder ? CmsTypeOrderingMode.byDisplayOrder : CmsTypeOrderingMode.latestOnTop;
        }

        I_CmsXmlContentValueLocation useFormatterKeysLoc = root.getSubValue(N_USE_FORMATTER_KEYS);
        Boolean useFormatterKeys = null;
        if (useFormatterKeysLoc != null) {
            useFormatterKeys = Boolean.valueOf(useFormatterKeysLoc.getValue().getStringValue(m_cms));
        }

        boolean isModuleConfig = OpenCms.getResourceManager().getResourceType(
            content.getFile().getTypeId()).getTypeName().equals(CmsADEManager.MODULE_CONFIG_TYPE);

        List<ConfigReference> masterConfigIds = new ArrayList<>();
        for (I_CmsXmlContentValueLocation masterConfigLoc : root.getSubValues(N_MASTER_CONFIG)) {
            CmsXmlVfsFileValue value = (CmsXmlVfsFileValue)masterConfigLoc.getValue();
            CmsLink link = value.getUncheckedLink();
            ConfigReferenceMeta meta = null;
            if (link != null) {
                meta = new ConfigReferenceMeta(link.getParameterMap());
            }
            CmsUUID id = masterConfigLoc.asId(m_cms);
            if (id != null) {
                masterConfigIds.add(new ConfigReference(id, meta));
            }
        }

        Map<String, String> attributes = new LinkedHashMap<>();
        for (I_CmsXmlContentValueLocation mappingLoc : root.getSubValues(N_ATTRIBUTE)) {
            String key = getString(mappingLoc.getSubValue(N_KEY)).trim();
            String value = getString(mappingLoc.getSubValue(N_VALUE)).trim();
            attributes.put(key, value);
        }

        I_CmsXmlContentValueLocation attributeEditorConfigLoc = root.getSubValue(N_ATTRIBUTE_EDITOR_CONFIG);
        CmsUUID attributeEditorConfigId = null;
        if (attributeEditorConfigLoc != null) {
            attributeEditorConfigId = attributeEditorConfigLoc.asId(m_cms);
        }

        CmsAddContentRestriction addContentRestriction = CmsAddContentRestriction.read(
            m_cms,
            root,
            N_ADD_CONTENT_RESTRICTION);

        CmsADEConfigDataInternal result = new CmsADEConfigDataInternal(
            m_cms,
            content.getFile(),
            isModuleConfig,
            basePath,
            masterConfigIds,
            m_resourceTypeConfigs,
            galleryDisabledTypesMode,
            galleryDisabledFunctionsMode,
            discardInheritedTypes,
            m_propertyConfigs,
            discardPropertiesMode,
            m_detailPageConfigs,
            m_modelPageConfigs,
            m_functionReferences,
            discardInheritedModelPages,
            createContentsLocally,
            preferDetailPagesForLocalContents,
            exludeExternalDetailContents,
            includeInSiteSelector,
            formatterChangeSet,
            removeFunctions,
            functions,
            functionsToRemove,
            removeAllPlugins,
            pluginsToAdd,
            pluginsToRemove,
            useFormatterKeys,
            typeOrderingMode,
            addContentRestriction,
            sharedSettingOverride,
            removeSharedSettingOverrides,
            attributeEditorConfigId,
            attributes);
        return result;
    }

    /**
     * Parses a folder which may either be given as a path or as a folder name.<p>
     *
     * @param basePath the  base path for the configuration
     * @param location the XML content node from which to parse the folder
     * @return the folder bean
     *
     * @throws CmsException if something goes wrong
     */
    public CmsContentFolderDescriptor parseFolderOrName(String basePath, I_CmsXmlContentLocation location)
    throws CmsException {

        if (location == null) {
            return null;
        }
        I_CmsXmlContentValueLocation nameLoc = location.getSubValue(N_NAME);
        I_CmsXmlContentValueLocation pathLoc = location.getSubValue(N_PATH);
        I_CmsXmlContentValueLocation pageRelativeLoc = location.getSubValue(N_PAGE_RELATIVE);
        if (nameLoc != null) {
            String name = nameLoc.asString(m_cms);
            return new CmsContentFolderDescriptor(
                basePath == null ? null : CmsStringUtil.joinPaths(basePath, CmsADEManager.CONTENT_FOLDER_NAME),
                name);
        } else if (pathLoc != null) {
            String path = pathLoc.asString(m_cms);
            CmsResource folder = m_cms.readResource(path);
            return new CmsContentFolderDescriptor(folder);
        } else if (pageRelativeLoc != null) {
            return CmsContentFolderDescriptor.createPageRelativeFolderDescriptor();
        } else {
            return null;
        }
    }

    /**
     * Parses a formatter bean.<p>
     *
     * @param typeName the type name for which the formatter is being parsed
     * @param node the node from which to parse the formatter data
     *
     * @return the formatter bean from the XML
     */
    public CmsFormatterBean parseFormatter(String typeName, I_CmsXmlContentLocation node) {

        String type = getString(node.getSubValue(N_TYPE));
        String minWidth = getString(node.getSubValue(N_MIN_WIDTH));
        String maxWidth = getString(node.getSubValue(N_MAX_WIDTH));
        boolean preview = false;
        I_CmsXmlContentValueLocation previewLoc = node.getSubValue(N_IS_PREVIEW);
        preview = (previewLoc != null) && Boolean.parseBoolean(previewLoc.asString(m_cms));
        String jsp = m_cms.getRequestContext().addSiteRoot(getString(node.getSubValue(N_JSP)));
        boolean searchContent = true;
        CmsFormatterBean formatterBean = new CmsFormatterBean(
            type,
            jsp,
            minWidth,
            maxWidth,
            "" + preview,
            "" + searchContent,
            null);
        return formatterBean;

    }

    /**
     * Parses model page data from the XML content.<p>
     *
     * @param node the XML content node
     */
    public void parseModelPage(I_CmsXmlContentLocation node) {

        CmsXmlVfsFileValue pageValue = (CmsXmlVfsFileValue)node.getSubValue(N_PAGE).getValue();
        CmsLink link = pageValue.getUncheckedLink();
        if ((link == null) || (link.getStructureId() == null)) {
            return;
        }
        I_CmsXmlContentValueLocation disabledLoc = node.getSubValue(N_DISABLED);
        boolean disabled = (disabledLoc != null) && Boolean.parseBoolean(disabledLoc.asString(m_cms));
        I_CmsXmlContentValueLocation defaultLoc = node.getSubValue(N_IS_DEFAULT);
        boolean isDefault = (defaultLoc != null) && Boolean.parseBoolean(defaultLoc.asString(m_cms));
        CmsModelPageConfigWithoutResource modelPage = new CmsModelPageConfigWithoutResource(
            link.getStructureId(),
            isDefault,
            disabled);
        m_modelPageConfigs.add(modelPage);

    }

    /**
     * Parses the set of formatters to remove.<p>
     *
     * @param node the parent node
     * @return the set of formatters to remove
     */
    public Set<String> parseRemoveFormatters(I_CmsXmlContentLocation node) {

        Set<String> removeFormatters = new HashSet<String>();
        for (I_CmsXmlContentValueLocation removeLoc : node.getSubValues(
            N_REMOVE_FORMATTERS + "/" + N_REMOVE_FORMATTER)) {
            CmsXmlVfsFileValue value = (CmsXmlVfsFileValue)removeLoc.getValue();
            CmsLink link = value.getLink(m_cms);
            if (link != null) {
                CmsUUID structureId = link.getStructureId();
                if (structureId != null) {
                    removeFormatters.add(structureId.toString());
                }
            }
        }
        return removeFormatters;
    }

    /**
     * Parses a resource type configuration element from the XML content.<p>
     *
     * @param basePath the base path of the configuration
     * @param node the XML configuration node
     * @throws CmsException if something goes wrong
     */
    public void parseResourceTypeConfig(String basePath, I_CmsXmlContentLocation node) throws CmsException {

        I_CmsXmlContentValueLocation typeNameLoc = node.getSubValue(N_TYPE_NAME);
        String typeName = typeNameLoc.asString(m_cms);
        CmsContentFolderDescriptor folderOrName = parseFolderOrName(basePath, node.getSubValue(N_FOLDER));
        I_CmsXmlContentValueLocation disabledLoc = node.getSubValue(N_DISABLED);
        boolean disabled = false;
        boolean addDisabled = false;
        boolean createDisabled = false;
        boolean editDisabled = false;
        boolean listsOnly = false;
        String disabledStr = disabledLoc == null ? null : disabledLoc.asString(m_cms);
        boolean availabilityNotSet = false;
        if (disabledStr != null) {
            if ("add".equalsIgnoreCase(disabledStr.trim())) {
                addDisabled = true;
            } else if ("create".equalsIgnoreCase(disabledStr.trim())) {
                createDisabled = true;
            } else if ("createOrEdit".equalsIgnoreCase(disabledStr.trim())) {
                createDisabled = true;
                editDisabled = true;
            } else if ("listsOnly".equalsIgnoreCase(disabledStr.trim())) {
                listsOnly = true;
                addDisabled = true;
            } else {
                disabled = Boolean.parseBoolean(disabledStr);
            }
        } else {
            availabilityNotSet = true;
        }

        I_CmsXmlContentValueLocation namePatternLoc = node.getSubValue(N_NAME_PATTERN);
        String namePattern = null;
        if (namePatternLoc != null) {
            namePattern = namePatternLoc.asString(m_cms);
        }

        boolean detailPagesDisabled = false;
        I_CmsXmlContentValueLocation detailDisabledLoc = node.getSubValue(N_DETAIL_PAGES_DISABLED);
        if (detailDisabledLoc != null) {
            String detailPagesDisabledStr = detailDisabledLoc.asString(m_cms);
            detailPagesDisabled = Boolean.parseBoolean(detailPagesDisabledStr);
        }

        Integer order = null;
        I_CmsXmlContentValueLocation orderLoc = node.getSubValue(N_ORDER);
        if (orderLoc != null) {
            try {
                String orderStr = orderLoc.asString(m_cms);
                order = Integer.valueOf(orderStr);
            } catch (NumberFormatException e) {
                // noop
            }
        }

        I_CmsXmlContentValueLocation elementViewLoc = node.getSubValue(N_ELEMENT_VIEW);
        CmsUUID elementView = null;
        if (elementViewLoc != null) {
            try {
                CmsXmlVarLinkValue elementViewValue = (CmsXmlVarLinkValue)elementViewLoc.getValue();
                String stringValue = elementViewValue.getStringValue(m_cms);
                if ("".equals(stringValue)) {
                    elementView = CmsUUID.getNullUUID();
                } else if (stringValue.startsWith(VIEW_SCHEME)) {
                    elementView = new CmsUUID(stringValue.substring(VIEW_SCHEME.length()));
                } else {
                    elementView = elementViewValue.getLink(m_cms).getStructureId();
                }
            } catch (Exception e) {
                // in case parsing the link fails, the default element view will be used
            }
        }

        I_CmsXmlContentValueLocation locationLoc = node.getSubValue(N_LOCALIZATION);
        String localization = null;
        if (locationLoc != null) {
            CmsXmlVfsFileValue locationValue = (CmsXmlVfsFileValue)locationLoc.getValue();
            CmsLink link = locationValue.getLink(m_cms);
            if (null != link) {
                String stringValue = link.getSitePath(m_cms);
                // extract bundle base name from the path to the bundle file
                int lastSlashIndex = stringValue.lastIndexOf("/");
                String fileName = stringValue.substring(lastSlashIndex + 1);
                if (CmsFileUtil.getExtension(fileName).equals(".properties")) {
                    fileName = fileName.substring(0, fileName.length() - ".properties".length());
                }
                String localeSuffix = CmsStringUtil.getLocaleSuffixForName(fileName);
                if ((localeSuffix != null) && fileName.endsWith(localeSuffix)) {
                    fileName = fileName.substring(0, fileName.length() - localeSuffix.length() - 1);
                }
                localization = fileName;
            }
        }

        I_CmsXmlContentValueLocation showDefaultViewLoc = node.getSubValue(N_SHOW_IN_DEFAULT_VIEW);
        Boolean showInDefaultView = null;
        if (showDefaultViewLoc != null) {
            showInDefaultView = Boolean.valueOf(
                Boolean.parseBoolean(showDefaultViewLoc.getValue().getStringValue(m_cms)));
        }

        I_CmsXmlContentValueLocation copyInModelsLoc = node.getSubValue(N_COPY_IN_MODELS);
        Boolean copyInModels = null;
        if (copyInModelsLoc != null) {
            copyInModels = Boolean.valueOf(Boolean.parseBoolean(copyInModelsLoc.getValue().getStringValue(m_cms)));
        }

        I_CmsXmlContentValueLocation elementDeleteModeLoc = node.getSubValue(N_ELEMENT_DELETE_MODE);
        ElementDeleteMode elementDeleteMode = null;
        if (elementDeleteModeLoc != null) {
            try {
                elementDeleteMode = ElementDeleteMode.valueOf(elementDeleteModeLoc.getValue().getStringValue(m_cms));
            } catch (Exception e) {
                LOG.warn(e.getLocalizedMessage(), e);
            }
        }

        I_CmsXmlContentValueLocation checkReuseLoc = node.getSubValue(N_CHECK_REUSE);
        Boolean checkReuse = null;
        if (checkReuseLoc != null) {
            try {
                checkReuse = Boolean.valueOf(checkReuseLoc.getValue().getStringValue(m_cms));
            } catch (Exception e) {
                LOG.warn(e.getLocalizedMessage(), e);
            }
        }

        List<I_CmsFormatterBean> formatters = new ArrayList<I_CmsFormatterBean>();
        for (I_CmsXmlContentValueLocation formatterLoc : node.getSubValues(N_FORMATTER)) {
            CmsFormatterBean formatter = parseFormatter(typeName, formatterLoc);
            formatters.add(formatter);
        }

        CmsResourceTypeConfig typeConfig = new CmsResourceTypeConfig(
            typeName,
            disabled,
            folderOrName,
            namePattern,
            detailPagesDisabled,
            addDisabled,
            createDisabled,
            editDisabled,
            listsOnly,
            availabilityNotSet,
            elementView,
            localization,
            showInDefaultView,
            copyInModels,
            order,
            elementDeleteMode,
            checkReuse);
        m_resourceTypeConfigs.add(typeConfig);
    }

    /**
     * Parses the sitemap configuration given the configuration file and base path.<p>
     *
     * @param basePath the base path
     * @param configRes the configuration file resource
     * @return the parsed configuration data
     * @throws CmsException if something goes wrong
     */
    public CmsADEConfigDataInternal parseSitemapConfiguration(String basePath, CmsResource configRes)
    throws CmsException {

        LOG.info("Parsing configuration " + configRes.getRootPath());
        CmsFile configFile = m_cms.readFile(configRes);
        CmsXmlContent content = CmsXmlContentFactory.unmarshal(m_cms, configFile);
        return parseConfiguration(basePath, content);
    }

    /**
     * Reads the configurations of all modules and combines them into a single configuration object.<p>
     *
     * @return the combined configuration object
     */
    public List<CmsADEConfigDataInternal> readModuleConfigurations() {

        List<CmsADEConfigDataInternal> configurations = new ArrayList<CmsADEConfigDataInternal>();
        List<CmsModule> modules = OpenCms.getModuleManager().getAllInstalledModules();
        long beginTime = System.currentTimeMillis();
        for (CmsModule module : modules) {
            String configPath = module.getConfigurationPath();
            if (m_cms.existsResource(configPath)) {
                try {
                    CmsResource configFile = m_cms.readResource(configPath);
                    LOG.info("Found module configuration " + configPath + " for module " + module.getName());
                    CmsADEConfigDataInternal config = parseSitemapConfiguration(null, configFile);
                    configurations.add(config);
                } catch (CmsException e) {
                    // errors while parsing configuration
                    LOG.error(e.getLocalizedMessage(), e);
                } catch (CmsRuntimeException e) {
                    // may happen during import of org.opencms.ade.configuration module
                    LOG.warn(e.getLocalizedMessage(), e);
                } catch (Throwable e) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
        }
        long endTime = System.currentTimeMillis();
        LOG.debug("readModuleConfiguations took " + (endTime - beginTime) + "ms");
        return configurations;
    }

    /**
     * Helper method to read a boolean value from the XML.<p>
     *
     * If the element is not found in the XML, false is returned.<p>
     *
     * @param parent the parent node
     * @param name the name of the XML content value
     * @return the boolean value
     */
    protected boolean getBoolean(I_CmsXmlContentLocation parent, String name) {

        I_CmsXmlContentValueLocation location = parent.getSubValue(name);
        if (location == null) {
            return false;
        }
        String value = location.getValue().getStringValue(m_cms);
        return Boolean.parseBoolean(value);
    }

    /**
     * Gets the string value of an XML content location.<p>
     *
     * @param location an XML content location
     *
     * @return the string value of that XML content location
     */
    protected String getString(I_CmsXmlContentValueLocation location) {

        return getString(m_cms, location);
    }

    /**
     * Parses the detail pages from an XML content node.<p>
     *
     * @param node the XML content node
     */
    protected void parseDetailPage(I_CmsXmlContentLocation node) {

        I_CmsXmlContentValueLocation pageLoc = node.getSubValue(N_PAGE);
        String typeName = getString(node.getSubValue(N_TYPE));
        int qualifierPos = typeName.indexOf(CmsDetailPageInfo.QUALIFIER_SEPARATOR);
        String qualifier = null;
        if (qualifierPos != -1) {
            qualifier = typeName.substring(qualifierPos + 1);
            typeName = typeName.substring(0, qualifierPos);
        }
        CmsXmlVfsFileValue detailPageValue = (CmsXmlVfsFileValue)pageLoc.getValue();
        CmsLink uncheckedLink = detailPageValue.getUncheckedLink();
        if (uncheckedLink == null) {
            LOG.warn(
                "Missing detail page link in " + CmsLog.eval(LOG, () -> node.getDocument().getFile().getRootPath()));
            return;
        }
        String page = uncheckedLink.getTarget();
        CmsUUID structureId = uncheckedLink.getStructureId();
        if (structureId == null) {
            return;
        }

        String iconClasses;
        if (typeName.startsWith(CmsDetailPageInfo.FUNCTION_PREFIX)) {
            iconClasses = CmsIconUtil.getIconClasses(CmsXmlDynamicFunctionHandler.TYPE_FUNCTION, null, false);
        } else {
            iconClasses = CmsIconUtil.getIconClasses(typeName, null, false);
        }

        CmsDetailPageInfo detailPage = new CmsDetailPageInfo(structureId, page, typeName, qualifier, iconClasses);
        m_detailPageConfigs.add(detailPage);

    }

    /**
     * Parses the formatter change set.<p>
     *
     * @param basePath the configuration base path
     * @param node the parent node
     * @param removeAllFormatters flag, indicating if all formatters that are not explicitly added should be removed
     * @param removeFunctions if true, remove functions
     * @param functions the functions to add
     * @param functionsToRemove the functions to remove
     *
     * @return the formatter change set
     */
    protected CmsFormatterChangeSet parseFormatterChangeSet(
        String basePath,
        I_CmsXmlContentLocation node,
        boolean removeAllFormatters,
        boolean removeFunctions,
        Set<CmsUUID> functions,
        Set<CmsUUID> functionsToRemove) {

        Set<String> addFormatters = parseAddFormatters(node);
        addFormatters.addAll(readLocalFormatters(node));
        Set<String> removeFormatters = removeAllFormatters ? new HashSet<String>() : parseRemoveFormatters(node);
        String siteRoot = null;
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(basePath)) {
            siteRoot = OpenCms.getSiteManager().getSiteRoot(basePath);
        }
        CmsFormatterChangeSet result = new CmsFormatterChangeSet(
            removeFormatters,
            addFormatters,
            siteRoot,
            removeAllFormatters,
            removeFunctions,
            functions,
            functionsToRemove);
        return result;
    }

    /**
     * Parses a function reference node.<p>
     *
     * @param node the function reference node
     */
    protected void parseFunctionReference(I_CmsXmlContentLocation node) {

        String name = node.getSubValue(N_NAME).asString(m_cms);
        CmsUUID functionId = node.getSubValue(N_FUNCTION).asId(m_cms);
        CmsUUID functionDefaultPageId = null;
        I_CmsXmlContentValueLocation defaultPageValue = node.getSubValue(N_FUNCTION_DEFAULT_PAGE);
        if (defaultPageValue != null) {
            functionDefaultPageId = defaultPageValue.asId(m_cms);
        }
        I_CmsXmlContentValueLocation orderNode = node.getSubValue(N_ORDER);
        int order = I_CmsConfigurationObject.DEFAULT_ORDER;
        if (orderNode != null) {
            String orderStr = orderNode.asString(m_cms);
            try {
                order = Integer.parseInt(orderStr);
            } catch (NumberFormatException e) {
                // noop
            }
        }
        m_functionReferences.add(new CmsFunctionReference(name, functionId, functionDefaultPageId, order));
    }

    /**
     * Parses a single field definition from a content value.<p>
     *
     * @param field the content value to parse the field from
     * @param mode the property discard mode
     */
    private void parseProperty(I_CmsXmlContentLocation field, DiscardPropertiesMode mode) {

        CmsPropertyConfig propConfig = parseProperty(m_cms, field);
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(propConfig.getName())) {
            return;
        }
        if (mode == DiscardPropertiesMode.top) {
            propConfig = propConfig.cloneWithTop(true);
        }
        m_propertyConfigs.add(propConfig);
    }

    /**
     * Helper method for reading the target ids from a list of internal links two levels nested.
     *
     * @param root the parent location
     * @param childName the node name for the children
     * @param grandchildName the node name for the grandchildren
     *
     * @return the set of target ids collected from the grandchildren
     */
    private Set<CmsUUID> readInternalLinkListTargetIds(
        I_CmsXmlContentLocation root,
        String childName,
        String grandchildName) {

        Set<CmsUUID> result = new LinkedHashSet<>();
        for (I_CmsXmlContentValueLocation parent : root.getSubValues(childName)) {
            for (I_CmsXmlContentValueLocation node : parent.getSubValues(grandchildName)) {
                CmsXmlVfsFileValue value = (CmsXmlVfsFileValue)node.getValue();
                CmsLink link = value.getLink(m_cms);
                if (link != null) {
                    CmsUUID structureId = link.getStructureId();
                    if (structureId != null) {
                        result.add(link.getStructureId());
                    }
                }
            }
        }
        return result;
    }

    /**
     * Reads the local macro or flex formatters from the .formatters folder if present.<p>
     *
     * @param node the xml content node
     *
     * @return the local formatters
     */
    private Set<String> readLocalFormatters(I_CmsXmlContentLocation node) {

        Set<String> addFormatters = new HashSet<String>();
        String path = m_cms.getSitePath(node.getDocument().getFile());
        path = CmsStringUtil.joinPaths(CmsResource.getParentFolder(path), ".formatters");
        try {
            if (m_cms.existsResource(path, CmsResourceFilter.IGNORE_EXPIRATION)) {
                I_CmsResourceType macroType = OpenCms.getResourceManager().getResourceType(
                    CmsFormatterConfigurationCache.TYPE_MACRO_FORMATTER);
                CmsResourceFilter filter = CmsResourceFilter.IGNORE_EXPIRATION.addRequireType(macroType);
                List<CmsResource> macroFormatters = m_cms.readResources(path, filter);
                for (CmsResource formatter : macroFormatters) {
                    addFormatters.add(formatter.getStructureId().toString());
                }
                I_CmsResourceType flexType = OpenCms.getResourceManager().getResourceType(
                    CmsFormatterConfigurationCache.TYPE_FLEX_FORMATTER);
                CmsResourceFilter filterFlex = CmsResourceFilter.IGNORE_EXPIRATION.addRequireType(flexType);
                List<CmsResource> flexFormatters = m_cms.readResources(path, filterFlex);
                for (CmsResource formatter : flexFormatters) {
                    addFormatters.add(formatter.getStructureId().toString());
                }
            }
        } catch (CmsException e) {
            LOG.warn(e.getMessage(), e);
        }
        return addFormatters;
    }

}
