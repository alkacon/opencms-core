/*
 * File   : $Source$
 * Date   : $Date$
 * Version: $Revision$
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2011 Alkacon Software (http://www.alkacon.com)
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

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.content.CmsDefaultXmlContentHandler;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.content.CmsXmlContentProperty;
import org.opencms.xml.content.CmsXmlContentValueLocation;
import org.opencms.xml.content.I_CmsXmlContentLocation;
import org.opencms.xml.content.I_CmsXmlContentValueLocation;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * This is the XML content handler class for the "dynamic functionality" resource type.<p>
 * 
 * This resource type needs special handling of formatters and element settings: They are 
 * read from each content of this type rather than from the XSD.<p>
 */
public class CmsXmlDynamicFunctionHandler extends CmsDefaultXmlContentHandler {

    /** The path of the formatter which calls the JSP. */
    public static final String FORMATTER_PATH = "/system/modules/org.opencms.ade.containerpage/elements/function.jsp";

    /** The resource type for dynamic functions. */
    public static final String TYPE_FUNCTION = "function";

    /** The node name for the formatter settings. */
    public static final String N_CONTAINER_SETTINGS = "ContainerSettings";

    /**
     * Default constructor.<p>
     */
    public CmsXmlDynamicFunctionHandler() {

        super();
    }

    /**
     * @see org.opencms.xml.content.CmsDefaultXmlContentHandler#getFormatterConfiguration(org.opencms.file.CmsObject, org.opencms.file.CmsResource)
     */
    @Override
    public CmsFormatterConfiguration getFormatterConfiguration(CmsObject cms, CmsResource resource) {

        try {
            CmsFile file = cms.readFile(resource);
            CmsXmlContent content = CmsXmlContentFactory.unmarshal(cms, file);
            Locale locale = new Locale("en");
            I_CmsXmlContentValue value = content.getValue("ContainerSettings", new Locale("en"));
            CmsFormatterBean formatterBean;
            CmsResource jspResource = cms.readResource(FORMATTER_PATH);
            if (value == null) {
                formatterBean = new CmsFormatterBean(
                    "*",
                    jspResource.getRootPath(),
                    jspResource.getStructureId(),
                    -1,
                    Integer.MAX_VALUE,
                    false,
                    false,
                    resource.getRootPath());
            } else {
                String type = "";
                String minWidth = "";
                String maxWidth = "";
                I_CmsXmlContentValue typeVal = content.getValue(N_CONTAINER_SETTINGS + "/Type", locale);
                if (typeVal != null) {
                    type = typeVal.getStringValue(cms);
                }
                I_CmsXmlContentValue minWidthVal = content.getValue(N_CONTAINER_SETTINGS + "/MinWidth", locale);
                if (minWidthVal != null) {
                    minWidth = minWidthVal.getStringValue(cms);
                }
                I_CmsXmlContentValue maxWidthVal = content.getValue(N_CONTAINER_SETTINGS + "/MaxWidth", locale);
                if (maxWidthVal != null) {
                    maxWidth = maxWidthVal.getStringValue(cms);
                }

                formatterBean = new CmsFormatterBean(
                    type,
                    FORMATTER_PATH,
                    minWidth,
                    maxWidth,
                    "false",
                    "false",
                    resource.getRootPath());
                formatterBean.setJspStructureId(jspResource.getStructureId());
            }
            return CmsFormatterConfiguration.create(cms, Collections.singletonList(formatterBean));
        } catch (CmsException e) {
            return CmsFormatterConfiguration.EMPTY_CONFIGURATION;
        }
    }

    /**
     * @see org.opencms.xml.content.CmsDefaultXmlContentHandler#getSettings(org.opencms.file.CmsObject, org.opencms.file.CmsResource)
     */
    @Override
    public Map<String, CmsXmlContentProperty> getSettings(CmsObject cms, CmsResource res) {

        try {
            CmsFile file = cms.readFile(res);
            CmsXmlContent xmlContent = CmsXmlContentFactory.unmarshal(cms, file);
            Locale locale = cms.getRequestContext().getLocale();
            if (!xmlContent.hasLocale(locale)) {
                locale = new Locale("en");
            }
            LinkedHashMap<String, CmsXmlContentProperty> settingConfigs = new LinkedHashMap<String, CmsXmlContentProperty>();
            List<I_CmsXmlContentValue> values = xmlContent.getValues("SettingConfig", locale);
            for (I_CmsXmlContentValue settingConfig : values) {
                CmsXmlContentValueLocation location = new CmsXmlContentValueLocation(settingConfig);
                CmsXmlContentProperty settingConfigBean = parseProperty(cms, location);
                settingConfigs.put(settingConfigBean.getName(), settingConfigBean);
            }
            return settingConfigs;
        } catch (CmsException e) {
            return Collections.<String, CmsXmlContentProperty> emptyMap();
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

}
