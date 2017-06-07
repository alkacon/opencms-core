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
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.jsp.util.CmsMacroFormatterResolver;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsLink;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.xml.containerpage.CmsFormatterBean;
import org.opencms.xml.containerpage.CmsMacroFormatterBean;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentProperty;
import org.opencms.xml.content.CmsXmlContentRootLocation;
import org.opencms.xml.content.I_CmsXmlContentLocation;
import org.opencms.xml.content.I_CmsXmlContentValueLocation;
import org.opencms.xml.types.CmsXmlVfsFileValue;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

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
    public static final String N_DETAIL = "Detail";

    /** Content value node name. */
    public static final String N_DISPLAY = "Display";

    /** Node name. */
    public static final String N_FORMATTER = "Formatter";

    /** Node name. */
    public static final String N_FORMATTERS = "Formatters";

    /** Content value node name. */
    public static final String N_HEAD_INCLUDE_CSS = "HeadIncludeCss";

    /** Content value node name. */
    public static final String N_HEAD_INCLUDE_JS = "HeadIncludeJs";

    /** Content value node name. */
    public static final String N_DEFAULT_CONTENT = "DefaultContent";

    /** Content value node name. */
    public static final String N_JAVASCRIPT_INLINE = "JavascriptInline";

    /** Content value node name. */
    public static final String N_JAVASCRIPT_LINK = "JavascriptLink";

    /** Content value node name. */
    public static final String N_JSP = "Jsp";

    /** Node name. */
    public static final String N_MACRO = "Macro";

    /** Node name. */
    public static final String N_MACRO_NAME = "MacroName";

    /** Content value node name. */
    public static final String N_MATCH = "Match";

    /** Content value node name. */
    public static final String N_MAX_WIDTH = "MaxWidth";

    /** Content value node name. */
    public static final String N_NESTED_CONTAINERS = "NestedContainers";

    /** Content value node name. */
    public static final String N_NICE_NAME = "NiceName";

    /** Content value node name. */
    public static final String N_PLACEHOLDER_MACRO = "PlaceholderMacro";

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

    /** Content value node name. */
    public static final String N_TYPE = "Type";

    /** Content value node name. */
    public static final String N_TYPES = "Types";

    /** Content value node name. */
    public static final String N_WIDTH = "Width";

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsFormatterBeanParser.class);

    /** Parsed field. */
    int m_width;

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

    /** Parsed field. */
    private Map<String, CmsXmlContentProperty> m_settings = new LinkedHashMap<String, CmsXmlContentProperty>();

    /**
     * Creates a new parser instance.<p>
     *
     * A  new parser instance should be created for every formatter configuration you want to parse.<p>
     *
     * @param cms the CMS context to use for parsing
     */
    public CmsFormatterBeanParser(CmsObject cms) {

        m_cms = cms;
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
    public CmsFormatterBean parse(CmsXmlContent content, String location, String id)
    throws CmsException, ParseException {

        I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(content.getFile());
        boolean isMacroFromatter = CmsFormatterConfigurationCache.TYPE_MACRO_FORMATTER.equals(type.getTypeName());

        Locale en = Locale.ENGLISH;
        I_CmsXmlContentValue niceName = content.getValue(N_NICE_NAME, en);
        m_niceName = niceName != null ? niceName.getStringValue(m_cms) : null;
        CmsXmlContentRootLocation root = new CmsXmlContentRootLocation(content, en);

        I_CmsXmlContentValueLocation rankLoc = root.getSubValue(N_RANK);
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

        m_resourceType = getStringSet(root, N_TYPE);
        parseSettings(root);
        String isDetailStr = getString(root, N_DETAIL, "true");
        boolean isDetail = Boolean.parseBoolean(isDetailStr);

        String isDisplayStr = getString(root, N_DISPLAY, "false");
        boolean isDisplay = Boolean.parseBoolean(isDisplayStr);

        String isStrictContainersStr = getString(root, N_STRICT_CONTAINERS, "false");
        boolean isStrictContainers = Boolean.parseBoolean(isStrictContainersStr);

        String autoEnabled = getString(root, N_AUTO_ENABLED, "false");
        m_autoEnabled = Boolean.parseBoolean(autoEnabled);

        parseMatch(root);

        boolean hasNestedContainers;
        CmsFormatterBean formatterBean;
        if (isMacroFromatter) {
            // setting macro formatter defaults
            m_formatterResource = content.getFile();
            m_preview = false;
            m_extractContent = true;
            hasNestedContainers = false;
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
                m_resourceType,
                rank,
                id,
                defContentRes != null ? defContentRes.getRootPath() : null,
                defContentRes != null ? defContentRes.getStructureId() : null,
                m_settings,
                m_autoEnabled,
                isDetail,
                isDisplay,
                macroInput,
                placeholderMacroInput,
                referencedFormatters,
                m_cms.getRequestContext().getCurrentProject().isOnlineProject());
        } else {
            I_CmsXmlContentValueLocation jspLoc = root.getSubValue(N_JSP);
            CmsXmlVfsFileValue jspValue = (CmsXmlVfsFileValue)(jspLoc.getValue());
            CmsLink link = jspValue.getLink(m_cms);
            if (link == null) {
                // JSP link is not set (for example because the formatter configuration has just been created)
                LOG.info("JSP link is null in formatter configuration: " + content.getFile().getRootPath());
                return null;
            }
            CmsUUID jspID = link.getStructureId();
            CmsResource formatterRes = m_cms.readResource(jspID);
            m_formatterResource = formatterRes;
            String previewStr = getString(root, N_PREVIEW, "false");
            m_preview = Boolean.parseBoolean(previewStr);

            String searchableStr = getString(root, N_SEARCH_CONTENT, "true");
            m_extractContent = Boolean.parseBoolean(searchableStr);
            String hasNestedContainersString = getString(root, N_NESTED_CONTAINERS, "false");
            hasNestedContainers = Boolean.parseBoolean(hasNestedContainersString);
            parseHeadIncludes(root);
            formatterBean = new CmsFormatterBean(
                m_containerTypes,
                m_formatterResource.getRootPath(),
                m_formatterResource.getStructureId(),
                m_width,
                m_maxWidth,
                m_preview,
                m_extractContent,
                location,
                m_cssPaths,
                m_inlineCss.toString(),
                m_jsPaths,
                m_inlineJs.toString(),
                m_niceName,
                m_resourceType,
                m_rank,
                id,
                m_settings,
                true,
                m_autoEnabled,
                isDetail,
                isDisplay,
                hasNestedContainers,
                isStrictContainers);
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
                CmsXmlVfsFileValue fileValue = (CmsXmlVfsFileValue)cssLinkLoc.getValue();
                CmsLink link = fileValue.getLink(m_cms);
                if (link != null) {
                    String cssPath = link.getTarget();
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
                CmsXmlVfsFileValue fileValue = (CmsXmlVfsFileValue)jsLinkLoc.getValue();
                CmsLink link = fileValue.getLink(m_cms);
                if (link != null) {
                    String jsPath = link.getTarget();
                    m_jsPaths.add(jsPath);
                }
            }
        }
    }

    /**
     * Parses the matching criteria (container types or widths) for the formatter.<p>
     *
     * @param linkFormatterLoc the formatter value location
     *
     * @throws ParseException if parsing goes wrong
     */
    private void parseMatch(I_CmsXmlContentLocation linkFormatterLoc) throws ParseException {

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
            throw new ParseException("Neither container types nor container widths defined!");
        }
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
            m_settings.put(property.getName(), property);
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
