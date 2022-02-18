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

package org.opencms.ade.configuration.formatters;

import org.opencms.ade.configuration.CmsConfigurationReader;
import org.opencms.ade.configuration.CmsPropertyConfig;
import org.opencms.ade.configuration.plugins.CmsTemplatePlugin;
import org.opencms.configuration.CmsConfigurationException;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypeFunctionConfig;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.jsp.util.CmsFunctionRenderer;
import org.opencms.jsp.util.CmsMacroFormatterResolver;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsLink;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.xml.containerpage.CmsFlexFormatterBean;
import org.opencms.xml.containerpage.CmsFormatterBean;
import org.opencms.xml.containerpage.CmsFunctionFormatterBean;
import org.opencms.xml.containerpage.CmsMacroFormatterBean;
import org.opencms.xml.containerpage.CmsMetaMapping;
import org.opencms.xml.containerpage.I_CmsFormatterBean;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentProperty;
import org.opencms.xml.content.CmsXmlContentRootLocation;
import org.opencms.xml.content.I_CmsXmlContentLocation;
import org.opencms.xml.content.I_CmsXmlContentValueLocation;
import org.opencms.xml.types.CmsXmlVarLinkValue;
import org.opencms.xml.types.CmsXmlVfsFileValue;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;

/**
 * Parses formatter beans from formatter configuration XML contents.<p>
 */
public class CmsFormatterBeanParser {

    /**
     * Exception for the errors in the configuration file not covered by other exception types.<p>
     */
    public static class ParseException extends Exception {

        /** Serial version id. */
        private static final long serialVersionUID = 1L;

        /**
         * Creates a new exception.<p>
         *
         * @param message the error message
         */
        public ParseException(String message) {

            super(message);
        }

        /**
         * Creates a new exception.<p>
         *
         * @param message the error message
         * @param cause the cause
         */
        public ParseException(String message, Throwable cause) {

            super(message, cause);
        }
    }

    /** Content value node name. */
    public static final String N_ALLOWS_SETTINGS_IN_EDITOR = "AllowsSettingsInEditor";

    /** Content value node name. */
    public static final String N_ATTRIBUTE = "Attribute";

    /** Content value node name. */
    public static final String N_AUTO_ENABLED = "AutoEnabled";

    /** Content value node name. */
    public static final String N_CHOICE_NEW_LINK = "ChoiceNewLink";

    /** Content value node name. */
    public static final String N_CONTAINER_TYPE = "ContainerType";

    /** Content value node name. */
    public static final String N_CSS_INLINE = "CssInline";

    /** Content value node name. */
    public static final String N_CSS_LINK = "CssLink";

    /** Content value node name. */
    public static final String N_DEFAULT = "Default";

    /** Content value node name. */
    public static final String N_DEFAULT_CONTENT = "DefaultContent";

    /** Content value node name. */
    public static final String N_DESCRIPTION = "Description";

    /** Content value node name. */
    public static final String N_DETAIL = "Detail";

    /** Content value node name. */
    public static final String N_DISPLAY = "Display";

    /** Content value node name. */
    public static final String N_ELEMENT = "Element";

    /** Node name. */
    public static final String N_FORMATTER = "Formatter";

    /** Node name. */
    public static final String N_FORMATTERS = "Formatters";

    /** Content value node name. */
    public static final String N_GROUP = "Group";

    /** Content value node name. */
    public static final String N_HEAD_INCLUDE_CSS = "HeadIncludeCss";

    /** Content value node name. */
    public static final String N_HEAD_INCLUDE_JS = "HeadIncludeJs";

    /** Content value node name. */
    public static final String N_INCLUDE_SETTINGS = "IncludeSettings";

    /** Content value node name. */
    public static final String N_JAVASCRIPT_INLINE = "JavascriptInline";

    /** Content value node name. */
    public static final String N_JAVASCRIPT_LINK = "JavascriptLink";

    /** Content value node name. */
    public static final String N_JSP = "Jsp";

    /** Content value node name. */
    public static final String N_KEY = "Key";

    /** Content value node name. */
    public static final String N_KEY_ALIAS = "KeyAlias";

    /** Node name. */
    public static final String N_MACRO = "Macro";

    /** Node name. */
    public static final String N_MACRO_NAME = "MacroName";

    /** Content value node name. */
    public static final String N_MATCH = "Match";

    /** Content value node name. */
    public static final String N_MAX_WIDTH = "MaxWidth";

    /** Content value node name. */
    public static final String N_META_MAPPING = "MetaMapping";

    /** Content value node name. */
    public static final String N_NESTED_FORMATTER_SETTINGS = "NestedFormatterSettings";

    /** Content value node name. */
    public static final String N_NICE_NAME = "NiceName";

    /** Content value node name. */
    public static final String N_ORDER = "Order";

    /** Content value node name. */
    public static final String N_PARAMETER = "Parameter";

    /** Content value node name. */
    public static final String N_PLACEHOLDER_MACRO = "PlaceholderMacro";

    /** Node name. */
    public static final String N_PLACEHOLDER_STRING_TEMPLATE = "PlaceholderStringTemplate";

    /** Content value node name. */
    public static final String N_PLUGIN = "Plugin";

    /** Content value node name. */
    public static final String N_PREVIEW = "Preview";

    /** Content value node name. */
    public static final String N_RANK = "Rank";

    /** Content value node name. */
    public static final String N_SEARCH_CONTENT = "SearchContent";

    /** Content value node name. */
    public static final String N_SETTING = "Setting";

    /** Content value node name. */
    public static final String N_STRICT_CONTAINERS = "StrictContainers";

    /** Node name. */
    public static final String N_STRING_TEMPLATE = "StringTemplate";

    /** XML node name. */
    public static final String N_TARGET = "Target";

    /** Content value node name. */
    public static final String N_TYPE = "Type";

    /** Content value node name. */
    public static final String N_TYPES = "Types";

    /** Node name for the 'use meta mappings for normal elements' check box. */
    public static final String N_USE_META_MAPPINGS_FOR_NORMAL_ELEMENTS = "AlwaysApplyMetaMappings";

    /** Content value node name. */
    public static final String N_VALUE = "Value";

    /** Content value node name. */
    public static final String N_WIDTH = "Width";

    /** The key for the setting display type. */
    public static final String SETTING_DISPLAY_TYPE = "displayType";

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsFormatterBeanParser.class);

    /** Parsed field. */
    int m_width;

    /** Additional setting configurations for includes. Entries consist of structure ids of setting definition files as keys and the corresponding setting definition maps as entries. */
    private Map<CmsUUID, Map<CmsSharedSettingKey, CmsXmlContentProperty>> m_additionalSettingConfigs = new HashMap<>();

    /** Parsed field. */
    private boolean m_autoEnabled;

    /** The CMS object used for parsing. */
    private CmsObject m_cms;

    /** Parsed field. */
    private Set<String> m_containerTypes;

    /** Parsed field. */
    private List<String> m_cssPaths = new ArrayList<String>();

    /** Parsed field. */
    private boolean m_extractContent;

    /** Parsed field. */
    private CmsResource m_formatterResource;

    /** Parsed field. */
    private StringBuffer m_inlineCss = new StringBuffer();

    /** Parsed field. */
    private StringBuffer m_inlineJs = new StringBuffer();

    /** Parsed field. */
    private List<String> m_jsPaths = new ArrayList<String>();

    /** The formatter key. */
    private String m_key;

    /** Parsed field. */
    private int m_maxWidth;

    /** Parsed field. */
    private String m_niceName;

    /** Parsed field. */
    private boolean m_preview;

    /** Parsed field. */
    private int m_rank;

    /** Parsed field. */
    private Set<String> m_resourceType;

    /** Setting configurations read from content. **/
    private List<CmsXmlContentProperty> m_settingList = new ArrayList<>();

    /**
     * Creates a new parser instance.<p>
     *
     * A  new parser instance should be created for every formatter configuration you want to parse.<p>
     *
     * @param cms the CMS context to use for parsing
     * @param settingConfigs the additional setting configurations used for includes
     */
    public CmsFormatterBeanParser(
        CmsObject cms,
        Map<CmsUUID, Map<CmsSharedSettingKey, CmsXmlContentProperty>> settingConfigs) {

        m_cms = cms;
        m_additionalSettingConfigs = settingConfigs;
    }

    /**
     * Creates an xpath from the given components.<p>
     *
     * @param components the xpath componentns
     *
     * @return the composed xpath
     */
    public static String path(String... components) {

        return CmsStringUtil.joinPaths(components);
    }

    /**
     * Reads the formatter bean from the given XML content.<p>
     *
     * @param content the formatter configuration XML content
     * @param location a string indicating the location of the configuration
     * @param id the id to use as the formatter id
     *
     * @return the parsed formatter bean
     *
     * @throws ParseException if parsing goes wrong
     * @throws CmsException if something else goes wrong
     */
    public I_CmsFormatterBean parse(CmsXmlContent content, String location, String id)
    throws CmsException, ParseException {

        I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(content.getFile());
        boolean isMacroFromatter = CmsFormatterConfigurationCache.TYPE_MACRO_FORMATTER.equals(type.getTypeName());
        boolean isFlexFormatter = CmsFormatterConfigurationCache.TYPE_FLEX_FORMATTER.equals(type.getTypeName());
        boolean isFunction = OpenCms.getResourceManager().matchResourceType(
            CmsResourceTypeFunctionConfig.TYPE_NAME,
            content.getFile().getTypeId());

        Locale en = Locale.ENGLISH;
        I_CmsXmlContentValue niceName = content.getValue(N_NICE_NAME, en);
        m_niceName = niceName != null ? niceName.getStringValue(m_cms) : null;
        CmsXmlContentRootLocation root = new CmsXmlContentRootLocation(content, en);
        I_CmsXmlContentValueLocation rankLoc = root.getSubValue(N_RANK);
        if (rankLoc != null) {
            String rankStr = rankLoc.getValue().getStringValue(m_cms);
            if (rankStr != null) {
                rankStr = rankStr.trim();
            }
            int rank;
            try {
                rank = Integer.parseInt(rankStr);
            } catch (NumberFormatException e) {
                rank = CmsFormatterBean.DEFAULT_CONFIGURATION_RANK;
                LOG.debug("Error parsing formatter rank.", e);
            }
            m_rank = rank;
        }

        m_resourceType = getStringSet(root, N_TYPE);
        parseSettings(root);
        List<I_CmsXmlContentValue> settingIncludes = content.getValues(N_INCLUDE_SETTINGS, en);
        settingIncludes = Lists.reverse(settingIncludes); // make defaults from earlier include files 'win' when merging them into a map
        List<CmsUUID> includeIds = new ArrayList<>();
        for (I_CmsXmlContentValue settingInclude : settingIncludes) {
            try {
                CmsXmlVfsFileValue includeFileVal = (CmsXmlVfsFileValue)settingInclude;
                CmsUUID includeSettingsId = includeFileVal.getLink(m_cms).getStructureId();
                includeIds.add(includeSettingsId);
            } catch (Exception e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }

        String isDetailStr = getString(root, N_DETAIL, "false");
        boolean isDetail = Boolean.parseBoolean(isDetailStr);

        String displayType = getString(root, N_DISPLAY, null);
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(displayType) || "false".equals(displayType)) {
            displayType = null;
        }

        String key = getString(root, N_KEY, "").trim();
        if (key.equals("")) {
            key = null;
        }
        Set<String> aliasKeys = new HashSet<>();
        for (I_CmsXmlContentValueLocation aliasKeyLoc : root.getSubValues(N_KEY_ALIAS)) {
            String aliasKey = aliasKeyLoc.getValue().getStringValue(m_cms);
            aliasKey = aliasKey.trim();
            if (!aliasKey.equals("")) {
                aliasKeys.add(aliasKey);
            }
        }

        CmsSettingConfiguration settingConfig = new CmsSettingConfiguration(
            m_settingList,
            m_additionalSettingConfigs,
            includeIds,
            key,
            displayType);

        String isAllowSettingsStr = getString(root, N_ALLOWS_SETTINGS_IN_EDITOR, "false");
        boolean isAllowSettings = Boolean.parseBoolean(isAllowSettingsStr);

        String isStrictContainersStr = getString(root, N_STRICT_CONTAINERS, "false");
        boolean isStrictContainers = Boolean.parseBoolean(isStrictContainersStr);

        String description = getString(root, N_DESCRIPTION, null);

        String autoEnabled = getString(root, N_AUTO_ENABLED, "false");
        m_autoEnabled = Boolean.parseBoolean(autoEnabled);

        String nestedFormatterSettings = getString(root, N_NESTED_FORMATTER_SETTINGS, "false");
        boolean nestedFormatters = Boolean.parseBoolean(nestedFormatterSettings);

        String useMetaMappinsForNormalElementsStr = getString(root, N_USE_META_MAPPINGS_FOR_NORMAL_ELEMENTS, "false");
        boolean useMetaMappingsForNormalElements = Boolean.parseBoolean(useMetaMappinsForNormalElementsStr);

        List<CmsTemplatePlugin> plugins = CmsTemplatePlugin.parsePlugins(m_cms, root, N_PLUGIN);

        // Functions which just have been created don't have any matching rules, but should fit anywhere
        boolean strictMode = !isFunction;
        parseMatch(root, strictMode);

        m_key = key;

        List<CmsMetaMapping> mappings = parseMetaMappings(root);
        Map<String, String> attributes = parseAttributes(root);

        I_CmsFormatterBean formatterBean;
        if (isMacroFromatter || isFlexFormatter) {
            // setting macro formatter defaults
            m_formatterResource = content.getFile();
            m_preview = false;
            m_extractContent = true;
            CmsResource defContentRes = null;
            I_CmsXmlContentValueLocation defContentLoc = root.getSubValue(N_DEFAULT_CONTENT);
            if (defContentLoc != null) {
                CmsXmlVfsFileValue defContentValue = (CmsXmlVfsFileValue)(defContentLoc.getValue());
                CmsLink defContentLink = defContentValue.getLink(m_cms);
                if (defContentLink != null) {
                    CmsUUID defContentID = defContentLink.getStructureId();
                    defContentRes = m_cms.readResource(defContentID);
                }
            }
            if (isMacroFromatter) {
                String macroInput = getString(root, N_MACRO, "");
                String placeholderMacroInput = getString(root, N_PLACEHOLDER_MACRO, "");
                Map<String, CmsUUID> referencedFormatters = readReferencedFormatters(content);
                formatterBean = new CmsMacroFormatterBean(
                    m_containerTypes,
                    m_formatterResource.getRootPath(),
                    m_formatterResource.getStructureId(),
                    m_width,
                    m_maxWidth,
                    m_extractContent,
                    location,
                    m_niceName,
                    description,
                    m_resourceType,
                    m_rank,
                    id,
                    defContentRes != null ? defContentRes.getRootPath() : null,
                    defContentRes != null ? defContentRes.getStructureId() : null,
                    settingConfig,
                    m_autoEnabled,
                    isDetail,
                    displayType,
                    isAllowSettings,
                    macroInput,
                    placeholderMacroInput,
                    referencedFormatters,
                    m_cms.getRequestContext().getCurrentProject().isOnlineProject(),
                    mappings,
                    useMetaMappingsForNormalElements);
            } else {
                String stringTemplate = getString(root, N_STRING_TEMPLATE, "");
                String placeholder = getString(root, N_PLACEHOLDER_STRING_TEMPLATE, "");
                formatterBean = new CmsFlexFormatterBean(
                    m_containerTypes,
                    m_formatterResource.getRootPath(),
                    m_formatterResource.getStructureId(),
                    m_key,
                    m_width,
                    m_maxWidth,
                    m_extractContent,
                    location,
                    m_niceName,
                    description,
                    m_resourceType,
                    m_rank,
                    id,
                    defContentRes != null ? defContentRes.getRootPath() : null,
                    defContentRes != null ? defContentRes.getStructureId() : null,
                    settingConfig,
                    m_autoEnabled,
                    isDetail,
                    displayType,
                    isAllowSettings,
                    stringTemplate,
                    placeholder,
                    mappings,
                    useMetaMappingsForNormalElements);
            }
        } else {
            I_CmsXmlContentValueLocation jspLoc = root.getSubValue(N_JSP);
            CmsXmlVfsFileValue jspValue = (CmsXmlVfsFileValue)(jspLoc.getValue());
            CmsLink link = jspValue.getLink(m_cms);

            CmsUUID jspID = null;
            if (link == null) {
                if (isFunction) {
                    CmsResource defaultFormatter = CmsFunctionRenderer.getDefaultFunctionJsp(m_cms);
                    jspID = defaultFormatter.getStructureId();
                } else {
                    // JSP link is not set (for example because the formatter configuration has just been created)
                    LOG.info("JSP link is null in formatter configuration: " + content.getFile().getRootPath());
                    return null;
                }
            } else {
                jspID = link.getStructureId();
            }

            if (jspID == null) {
                throw new CmsConfigurationException(
                    org.opencms.main.Messages.get().container(
                        org.opencms.main.Messages.ERR_READ_FORMATTER_CONFIG_4,
                        new Object[] {
                            link != null ? link.getUri() : " ??? ",
                            m_niceName,
                            location,
                            "" + m_resourceType}));
            }

            CmsResource formatterRes = m_cms.readResource(jspID);
            m_formatterResource = formatterRes;
            String previewStr = getString(root, N_PREVIEW, "false");
            m_preview = Boolean.parseBoolean(previewStr);

            String searchableStr = getString(root, N_SEARCH_CONTENT, "true");
            m_extractContent = Boolean.parseBoolean(searchableStr);
            parseHeadIncludes(root);
            if (isFunction) {
                CmsResource functionFormatter = m_cms.readResource(CmsResourceTypeFunctionConfig.FORMATTER_PATH);
                Map<String, String[]> rparams = parseParams(root);
                formatterBean = new CmsFunctionFormatterBean(
                    m_containerTypes,
                    m_formatterResource.getRootPath(),
                    m_formatterResource.getStructureId(),
                    m_key,
                    aliasKeys,
                    functionFormatter.getStructureId(),
                    m_width,
                    m_maxWidth,
                    location,
                    m_cssPaths,
                    m_inlineCss.toString(),
                    m_jsPaths,
                    m_inlineJs.toString(),
                    plugins,
                    m_niceName,
                    description,
                    id,
                    settingConfig,
                    isAllowSettings,
                    isStrictContainers,
                    rparams);
            } else {
                formatterBean = new CmsFormatterBean(
                    m_containerTypes,
                    m_formatterResource.getRootPath(),
                    m_formatterResource.getStructureId(),
                    m_key,
                    aliasKeys,
                    m_width,
                    m_maxWidth,
                    m_preview,
                    m_extractContent,
                    location,
                    m_cssPaths,
                    m_inlineCss.toString(),
                    m_jsPaths,
                    m_inlineJs.toString(),
                    plugins,
                    m_niceName,
                    description,
                    m_resourceType,
                    m_rank,
                    id,
                    settingConfig,
                    true,
                    m_autoEnabled,
                    isDetail,
                    displayType,
                    isAllowSettings,
                    isStrictContainers,
                    nestedFormatters,
                    mappings,
                    attributes,
                    useMetaMappingsForNormalElements);
            }
        }

        return formatterBean;
    }

    /**
     * Gets an XML string value.<p>
     *
     * @param val the location of the parent value
     * @param path the path of the sub-value
     * @param defaultValue the default value to use if no value was found
     *
     * @return the found value
     */
    private String getString(I_CmsXmlContentLocation val, String path, String defaultValue) {

        if ((val != null)) {
            I_CmsXmlContentValueLocation subVal = val.getSubValue(path);
            if ((subVal != null) && (subVal.getValue() != null)) {
                return subVal.getValue().getStringValue(m_cms);
            }
        }
        return defaultValue;
    }

    /**
     * Returns a set of string values.<p>
     *
     * @param val the location of the parent value
     * @param path the path of the sub-values
     *
     * @return a set of string values
     */
    private Set<String> getStringSet(I_CmsXmlContentLocation val, String path) {

        Set<String> valueSet = new HashSet<String>();
        if ((val != null)) {
            List<I_CmsXmlContentValueLocation> singleValueLocs = val.getSubValues(path);
            for (I_CmsXmlContentValueLocation singleValueLoc : singleValueLocs) {
                String value = singleValueLoc.getValue().getStringValue(m_cms).trim();
                valueSet.add(value);
            }
        }
        return valueSet;
    }

    /**
     * Parses formatter attributes.
     *
     * @param formatterLoc the node location
     * @return the map of formatter attributes (unmodifiable)
     */
    private Map<String, String> parseAttributes(I_CmsXmlContentLocation formatterLoc) {

        Map<String, String> result = new LinkedHashMap<>();
        for (I_CmsXmlContentValueLocation mappingLoc : formatterLoc.getSubValues(N_ATTRIBUTE)) {
            String key = CmsConfigurationReader.getString(m_cms, mappingLoc.getSubValue(N_KEY));
            String value = CmsConfigurationReader.getString(m_cms, mappingLoc.getSubValue(N_VALUE));
            result.put(key, value);
        }
        return Collections.unmodifiableMap(result);
    }

    /**
     * Parses the head includes.<p>
     *
     * @param formatterLoc the parent value location
     */
    private void parseHeadIncludes(I_CmsXmlContentLocation formatterLoc) {

        I_CmsXmlContentValueLocation headIncludeCss = formatterLoc.getSubValue(N_HEAD_INCLUDE_CSS);
        if (headIncludeCss != null) {
            for (I_CmsXmlContentValueLocation inlineCssLoc : headIncludeCss.getSubValues(N_CSS_INLINE)) {
                String inlineCss = inlineCssLoc.getValue().getStringValue(m_cms);
                m_inlineCss.append(inlineCss);
            }

            for (I_CmsXmlContentValueLocation cssLinkLoc : headIncludeCss.getSubValues(N_CSS_LINK)) {
                CmsXmlVarLinkValue fileValue = (CmsXmlVarLinkValue)cssLinkLoc.getValue();
                CmsLink link = fileValue.getLink(m_cms);
                if (link != null) {
                    String cssPath = link.getTargetWithQuery();
                    m_cssPaths.add(cssPath);
                }
            }
        }
        I_CmsXmlContentValueLocation headIncludeJs = formatterLoc.getSubValue(N_HEAD_INCLUDE_JS);
        if (headIncludeJs != null) {
            for (I_CmsXmlContentValueLocation inlineJsLoc : headIncludeJs.getSubValues(N_JAVASCRIPT_INLINE)) {
                String inlineJs = inlineJsLoc.getValue().getStringValue(m_cms);
                m_inlineJs.append(inlineJs);
            }
            for (I_CmsXmlContentValueLocation jsLinkLoc : headIncludeJs.getSubValues(N_JAVASCRIPT_LINK)) {
                CmsXmlVarLinkValue fileValue = (CmsXmlVarLinkValue)jsLinkLoc.getValue();
                CmsLink link = fileValue.getLink(m_cms);
                if (link != null) {
                    String jsPath = link.getTargetWithQuery();
                    m_jsPaths.add(jsPath);
                }
            }
        }
    }

    /**
     * Parses the matching criteria (container types or widths) for the formatter.<p>
     *
     * @param linkFormatterLoc the formatter value location
     * @param strict if we should throw an error for incomplete match
     *
     * @throws ParseException if parsing goes wrong
     */
    private void parseMatch(I_CmsXmlContentLocation linkFormatterLoc, boolean strict) throws ParseException {

        Set<String> containerTypes = new HashSet<String>();
        I_CmsXmlContentValueLocation typesLoc = linkFormatterLoc.getSubValue(path(N_MATCH, N_TYPES));
        I_CmsXmlContentValueLocation widthLoc = linkFormatterLoc.getSubValue(path(N_MATCH, N_WIDTH));
        if (typesLoc != null) {
            List<I_CmsXmlContentValueLocation> singleTypeLocs = typesLoc.getSubValues(N_CONTAINER_TYPE);
            for (I_CmsXmlContentValueLocation singleTypeLoc : singleTypeLocs) {
                String containerType = singleTypeLoc.getValue().getStringValue(m_cms).trim();
                containerTypes.add(containerType);
            }
            m_containerTypes = containerTypes;
        } else if (widthLoc != null) {
            String widthStr = getString(widthLoc, N_WIDTH, null);
            String maxWidthStr = getString(widthLoc, N_MAX_WIDTH, null);
            try {
                m_width = Integer.parseInt(widthStr);
            } catch (Exception e) {
                throw new ParseException("Invalid container width: [" + widthStr + "]", e);
            }
            try {
                m_maxWidth = Integer.parseInt(maxWidthStr);
            } catch (Exception e) {
                m_maxWidth = Integer.MAX_VALUE;
                LOG.debug(maxWidthStr, e);
            }
        } else {
            if (strict) {
                throw new ParseException("Neither container types nor container widths defined!");
            } else {
                m_width = -1;
                m_maxWidth = Integer.MAX_VALUE;
            }
        }
    }

    /**
     * Parses the mappings.<p>
     *
     * @param formatterLoc the formatter value location
     *
     * @return the mappings
     */
    private List<CmsMetaMapping> parseMetaMappings(I_CmsXmlContentLocation formatterLoc) {

        List<CmsMetaMapping> mappings = new ArrayList<CmsMetaMapping>();
        for (I_CmsXmlContentValueLocation mappingLoc : formatterLoc.getSubValues(N_META_MAPPING)) {
            String key = CmsConfigurationReader.getString(m_cms, mappingLoc.getSubValue(N_KEY));
            String element = CmsConfigurationReader.getString(m_cms, mappingLoc.getSubValue(N_ELEMENT));
            String defaultValue = CmsConfigurationReader.getString(m_cms, mappingLoc.getSubValue(N_DEFAULT));
            String orderStr = CmsConfigurationReader.getString(m_cms, mappingLoc.getSubValue(N_ORDER));
            int order = 1000;
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(orderStr)) {
                try {
                    order = Integer.parseInt(orderStr);
                } catch (NumberFormatException e) {
                    // nothing to do
                }
            }
            CmsMetaMapping mapping = new CmsMetaMapping(key, element, order, defaultValue);
            mappings.add(mapping);
        }
        return mappings;
    }

    /**
     * Parse parameters and put them in a map.<p>
     *
     * @param root the location from which to start parsing
     *
     * @return the parameter map
     */
    private Map<String, String[]> parseParams(I_CmsXmlContentLocation root) {

        // first use multimap for convenience, to group values for the same key,
        // and then convert to result format
        ArrayListMultimap<String, String> mmap = ArrayListMultimap.create();
        for (I_CmsXmlContentLocation location : root.getSubValues(N_PARAMETER)) {
            String key = location.getSubValue(N_KEY).getValue().getStringValue(m_cms);
            String value = location.getSubValue(N_VALUE).getValue().getStringValue(m_cms);
            mmap.put(key, value);
        }
        Map<String, String[]> result = new HashMap<>();
        String[] emptyArray = new String[] {}; // need this for toArray
        for (String key : mmap.keySet()) {
            List<String> values = mmap.get(key);
            String[] valuesArray = values.toArray(emptyArray);
            result.put(key, valuesArray);
        }
        return result;

    }

    /**
     * Parses the settings.<p>
     *
     * @param formatterLoc the formatter value location
     */
    private void parseSettings(I_CmsXmlContentLocation formatterLoc) {

        for (I_CmsXmlContentValueLocation settingLoc : formatterLoc.getSubValues(N_SETTING)) {
            CmsPropertyConfig propConfig = CmsConfigurationReader.parseProperty(m_cms, settingLoc);
            CmsXmlContentProperty property = propConfig.getPropertyData();
            m_settingList.add(property);
        }
    }

    /**
     * Reads the referenced formatters.<p>
     *
     * @param xmlContent the XML content
     *
     * @return the referenced formatters
     */
    private Map<String, CmsUUID> readReferencedFormatters(CmsXmlContent xmlContent) {

        Map<String, CmsUUID> result = new LinkedHashMap<String, CmsUUID>();
        List<I_CmsXmlContentValue> formatters = xmlContent.getValues(
            CmsMacroFormatterResolver.N_FORMATTERS,
            CmsLocaleManager.MASTER_LOCALE);
        for (I_CmsXmlContentValue formatterValue : formatters) {
            CmsXmlVfsFileValue file = (CmsXmlVfsFileValue)xmlContent.getValue(
                formatterValue.getPath() + "/" + CmsMacroFormatterResolver.N_FORMATTER,
                CmsLocaleManager.MASTER_LOCALE);
            CmsUUID formatterId = file.getLink(m_cms).getStructureId();
            String macroName = xmlContent.getStringValue(
                m_cms,
                formatterValue.getPath() + "/" + CmsMacroFormatterResolver.N_MACRO_NAME,
                CmsLocaleManager.MASTER_LOCALE);
            result.put(macroName, formatterId);
        }
        return result;
    }

}
