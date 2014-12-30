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

package org.opencms.xml.containerpage;

import org.opencms.cache.CmsVfsMemoryObjectCache;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.main.CmsException;
import org.opencms.util.CmsPair;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.xml.containerpage.CmsDynamicFunctionBean.Format;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.content.CmsXmlContentProperty;
import org.opencms.xml.content.CmsXmlContentRootLocation;
import org.opencms.xml.content.I_CmsXmlContentLocation;
import org.opencms.xml.content.I_CmsXmlContentValueLocation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * The parser class for creating dynamic function beans from XML contents.<p>
 */
public class CmsDynamicFunctionParser {

    /** The path of the formatter which calls the JSP. */
    public static final String FORMATTER_PATH = "/system/modules/org.opencms.ade.containerpage/formatters/function.jsp";

    /** The node name for the formatter settings. */
    public static final String N_CONTAINER_SETTINGS = "ContainerSettings";

    /**
     * Parses a dynamic function bean given a resource.<p>
     * 
     * @param cms the current CMS context 
     * @param res the resource from which to read the dynamic function 
     * 
     * @return the dynamic function bean created from the resource 
     * 
     * @throws CmsException if something goes wrong 
     */
    public CmsDynamicFunctionBean parseFunctionBean(CmsObject cms, CmsResource res) throws CmsException {

        CmsFile file = cms.readFile(res);
        CmsXmlContent xmlContent = CmsXmlContentFactory.unmarshal(cms, file);
        return parseFunctionBean(cms, xmlContent);
    }

    /**
     * Parses a dynamic function bean from an in-memory XML content object.<p>
     * 
     * @param cms the current CMS context 
     * @param content the XML content from which to read the dynamic function bean 
     * 
     * @return the dynamic function bean read from the XML content
     * 
     * @throws CmsException if something goes wrong 
     */
    public CmsDynamicFunctionBean parseFunctionBean(CmsObject cms, CmsXmlContent content) throws CmsException {

        Locale locale = getLocaleToUse(cms, content);
        String oldSiteRoot = cms.getRequestContext().getSiteRoot();
        try {
            cms.getRequestContext().setSiteRoot("");
            CmsResource functionFormatter = getFunctionFormatter(cms);
            CmsXmlContentRootLocation root = new CmsXmlContentRootLocation(content, locale);
            CmsDynamicFunctionBean functionBean = parseFunctionBean(cms, root, content.getFile(), functionFormatter);
            return functionBean;
        } finally {
            cms.getRequestContext().setSiteRoot(oldSiteRoot);
        }
    }

    /**
     * Parses all the additional formats from the XML content.<p> 
     * 
     * @param cms the current CMS context 
     * @param location the location from which to parse the additional formats 
     * @param functionRes the dynamic function resource 
     * 
     * @return the list of parsed formats 
     */
    protected List<Format> getAdditionalFormats(CmsObject cms, I_CmsXmlContentLocation location, CmsResource functionRes) {

        List<I_CmsXmlContentValueLocation> locations = location.getSubValues("AdditionalFormat");
        List<Format> formats = new ArrayList<Format>();
        for (I_CmsXmlContentValueLocation formatLocation : locations) {
            Format format = parseAdditionalFormat(cms, formatLocation, functionRes);
            formats.add(format);
        }
        return formats;

    }

    /**
     * Gets the function formatter resource, possibly from the cache.<p>
     * 
     * @param cms the current CMS context 
     * @return the function formatter resource 
     * 
     * @throws CmsException if something goes wrong 
     */
    protected CmsResource getFunctionFormatter(CmsObject cms) throws CmsException {

        CmsVfsMemoryObjectCache cache = CmsVfsMemoryObjectCache.getVfsMemoryObjectCache();
        String path = CmsDynamicFunctionBean.FORMATTER_PATH;
        Object cacheValue = cache.getCachedObject(cms, path);
        if (cacheValue == null) {
            CmsResource functionRes = cms.readResource(path);
            cache.putCachedObject(cms, path, functionRes);
            return functionRes;
        } else {
            return (CmsResource)cacheValue;
        }
    }

    /**
     * Gets the locale to use for parsing the dynamic function.<p>
     * 
     * @param cms the current CMS context 
     * @param xmlContent the xml content from which the dynamic function should be read 
     * 
     * @return the locale from which the dynamic function should be read 
     */
    protected Locale getLocaleToUse(CmsObject cms, CmsXmlContent xmlContent) {

        Locale contextLocale = cms.getRequestContext().getLocale();
        if (xmlContent.hasLocale(contextLocale)) {
            return contextLocale;
        }
        Locale defaultLocale = CmsLocaleManager.getDefaultLocale();
        if (xmlContent.hasLocale(defaultLocale)) {
            return defaultLocale;
        }
        if (!xmlContent.getLocales().isEmpty()) {
            return xmlContent.getLocales().get(0);
        } else {
            return defaultLocale;
        }
    }

    /**
     * Parses the main format from the XML content.<p>
     * @param cms the current CMS context 
     * @param location the location from which to parse main format 
     * @param functionRes the dynamic function resource 
     * 
     * @return the parsed main format 
     */
    protected Format getMainFormat(CmsObject cms, I_CmsXmlContentLocation location, CmsResource functionRes) {

        I_CmsXmlContentValueLocation jspLoc = location.getSubValue("FunctionProvider");
        CmsUUID structureId = jspLoc.asId(cms);
        I_CmsXmlContentValueLocation containerSettings = location.getSubValue("ContainerSettings");
        Map<String, String> parameters = parseParameters(cms, location, "Parameter");
        if (containerSettings != null) {
            String type = getStringValue(cms, containerSettings.getSubValue("Type"), "");
            String minWidth = getStringValue(cms, containerSettings.getSubValue("MinWidth"), "");
            String maxWidth = getStringValue(cms, containerSettings.getSubValue("MaxWidth"), "");
            Format result = new Format(structureId, type, minWidth, maxWidth, parameters);
            return result;
        } else {
            Format result = new Format(structureId, "", "", "", parameters);
            result.setNoContainerSettings(true);
            return result;
        }
    }

    /**
     * Gets the string value of an XML content location.<p>
     *
     * @param cms the current CMS context 
     * @param location an XML content location 
     * 
     * @return the string value of that XML content location 
     */
    protected String getString(CmsObject cms, I_CmsXmlContentValueLocation location) {

        if (location == null) {
            return null;
        }
        return location.asString(cms);
    }

    /**
     * Converts a (possibly null) content value location to a string.<p>
     * 
     * @param cms the current CMS context 
     * @param location the content value location 
     * @param defaultValue the value to return if the location is null
     *  
     * @return the string value of the content value location 
     */
    protected String getStringValue(CmsObject cms, I_CmsXmlContentValueLocation location, String defaultValue) {

        if (location == null) {
            return defaultValue;
        }
        return location.asString(cms);
    }

    /**
     * Parses an additional format from the XML content.<p>
     * 
     * @param cms the current CMS context 
     * 
     * @param location the location from which to parse the additional format 
     * @param functionRes the dynamic function resource 
     * 
     * @return the additional format 
     */
    protected Format parseAdditionalFormat(CmsObject cms, I_CmsXmlContentValueLocation location, CmsResource functionRes) {

        I_CmsXmlContentValueLocation jspLoc = location.getSubValue("FunctionProvider");
        CmsUUID structureId = jspLoc.asId(cms);
        I_CmsXmlContentValueLocation minWidthLoc = location.getSubValue("MinWidth");
        String minWidth = getStringValue(cms, minWidthLoc, "");
        I_CmsXmlContentValueLocation maxWidthLoc = location.getSubValue("MaxWidth");
        String maxWidth = getStringValue(cms, maxWidthLoc, "");
        I_CmsXmlContentValueLocation typeLoc = location.getSubValue("Type");
        String type = getStringValue(cms, typeLoc, "");
        Map<String, String> parameters = parseParameters(cms, location, "Parameter");
        return new Format(structureId, type, minWidth, maxWidth, parameters);
    }

    /**
     * Parses a dynamic function bean.<p>
     * 
     * @param cms the current CMS context 
     * @param location the location from which to parse the dynamic function bean 
     * @param functionRes the dynamic function resource 
     * @param functionFormatter the function formatter resource 
     * 
     * @return the parsed dynamic function bean 
     */
    protected CmsDynamicFunctionBean parseFunctionBean(
        CmsObject cms,
        I_CmsXmlContentLocation location,
        CmsResource functionRes,
        CmsResource functionFormatter) {

        Format mainFormat = getMainFormat(cms, location, functionRes);
        List<Format> otherFormats = getAdditionalFormats(cms, location, functionRes);
        Map<String, CmsXmlContentProperty> definedSettings = parseSettings(cms, location, functionRes);
        CmsDynamicFunctionBean result = new CmsDynamicFunctionBean(
            mainFormat,
            otherFormats,
            definedSettings,
            functionRes,
            functionFormatter);
        return result;
    }

    /**
     * Parses a request parameter for the JSP from the XML content.<p>
     * 
     * @param cms the current CMS context 
     * @param valueLocation the location from which to parse the parameter
     * 
     * @return the parsed parameter key/value pair  
     */
    protected CmsPair<String, String> parseParameter(CmsObject cms, I_CmsXmlContentValueLocation valueLocation) {

        String key = valueLocation.getSubValue("Key").asString(cms);
        String value = valueLocation.getSubValue("Value").asString(cms);
        return CmsPair.create(key, value);
    }

    /**
     * Parses all parameters for the JSP from the XML content.<p>
     * 
     * @param cms the current CMS context
     * @param location the location from which to read the parameters 
     * @param name the name of the tag from which to read the parameters
     *  
     * @return the parsed map of parameters 
     */
    protected Map<String, String> parseParameters(CmsObject cms, I_CmsXmlContentLocation location, String name) {

        List<I_CmsXmlContentValueLocation> locations = location.getSubValues(name);
        Map<String, String> result = new LinkedHashMap<String, String>();
        for (I_CmsXmlContentValueLocation paramLocation : locations) {
            CmsPair<String, String> param = parseParameter(cms, paramLocation);
            result.put(param.getFirst(), param.getSecond());
        }
        return result;
    }

    /**
     * Helper method for parsing a settings definition.<p>
     * 
     * @param cms the current CMS context
     * @param field the node from which to read the settings definition 
     * 
     * @return the parsed setting definition 
     */
    protected CmsXmlContentProperty parseProperty(CmsObject cms, I_CmsXmlContentLocation field) {

        String name = getString(cms, field.getSubValue("PropertyName"));
        String widget = getString(cms, field.getSubValue("Widget"));
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(widget)) {
            widget = "string";
        }
        String type = getString(cms, field.getSubValue("Type"));
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(type)) {
            type = "string";
        }
        String widgetConfig = getString(cms, field.getSubValue("WidgetConfig"));
        String ruleRegex = getString(cms, field.getSubValue("RuleRegex"));
        String ruleType = getString(cms, field.getSubValue("RuleType"));
        String default1 = getString(cms, field.getSubValue("Default"));
        String error = getString(cms, field.getSubValue("Error"));
        String niceName = getString(cms, field.getSubValue("DisplayName"));
        String description = getString(cms, field.getSubValue("Description"));

        CmsXmlContentProperty prop = new CmsXmlContentProperty(
            name,
            type,
            widget,
            widgetConfig,
            ruleRegex,
            ruleType,
            default1,
            niceName,
            description,
            error,
            "true");
        return prop;
    }

    /**
     * Parses the settings for the dynamic function from the XML content.<p>
     * 
     * @param cms the current CMS context 
     * @param location the location from which to read the dynamic function settings 
     * @param functionResource the dynamic function resource 
     * 
     * @return the parsed map of settings for the dynamic function
     */
    protected Map<String, CmsXmlContentProperty> parseSettings(
        CmsObject cms,
        I_CmsXmlContentLocation location,
        CmsResource functionResource) {

        LinkedHashMap<String, CmsXmlContentProperty> settingConfigs = new LinkedHashMap<String, CmsXmlContentProperty>();
        List<I_CmsXmlContentValueLocation> locations = location.getSubValues("SettingConfig");
        for (I_CmsXmlContentValueLocation settingLoc : locations) {
            CmsXmlContentProperty settingConfigBean = parseProperty(cms, settingLoc);
            settingConfigs.put(settingConfigBean.getName(), settingConfigBean);
        }
        return settingConfigs;
    }

}
