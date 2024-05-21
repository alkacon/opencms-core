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

package org.opencms.jsp.util;

import org.opencms.ade.configuration.CmsADEConfigData;
import org.opencms.file.CmsObject;
import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.i18n.CmsMessages;
import org.opencms.i18n.CmsMultiMessages;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsMacroResolver;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.containerpage.CmsFormatterBean;
import org.opencms.xml.containerpage.CmsFunctionFormatterBean;
import org.opencms.xml.containerpage.I_CmsFormatterBean;
import org.opencms.xml.content.CmsXmlContentProperty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Wrapper class for accessing formatter information from JSPs.
 */
public class CmsFormatterInfoWrapper implements I_CmsFormatterInfo {

    /** The CMS context to use. */
    private CmsObject m_cms;

    /** The sitemap configuration for the current context. */
    private CmsADEConfigData m_config;

    /** The wrapped formatter. */
    private I_CmsFormatterBean m_formatter;

    /** The macro resolver to use. */
    private CmsMacroResolver m_macroResolver;

    /**
     * Creates a new instance.
     *
     * @param cms the CMS context
     * @param config the sitemap configuration
     * @param formatter the formatter bean to wrap
     */
    public CmsFormatterInfoWrapper(CmsObject cms, CmsADEConfigData config, I_CmsFormatterBean formatter) {

        m_cms = cms;
        m_formatter = formatter;
        m_macroResolver = getMacroResolverForFormatter(cms, formatter);
        m_config = config;
    }

    /**
     * Prepares the macro resolver to use for formatter info / setting info beans.
     *
     * @param cms the CMS context to use
     * @param formatter the formatter bean
     * @return the macro resolver to sue
     */
    public static CmsMacroResolver getMacroResolverForFormatter(CmsObject cms, I_CmsFormatterBean formatter) {

        final CmsMacroResolver resolver = new CmsMacroResolver();
        resolver.setCmsObject(cms);
        Locale locale = cms.getRequestContext().getLocale();
        CmsMultiMessages messages = new CmsMultiMessages(locale);
        messages.addMessages(OpenCms.getWorkplaceManager().getMessages(locale));
        for (String type : formatter.getResourceTypeNames()) {
            try {
                I_CmsResourceType typeObj = OpenCms.getResourceManager().getResourceType(type);
                String schema = typeObj.getConfiguration().getString(
                    CmsResourceTypeXmlContent.CONFIGURATION_SCHEMA,
                    null);
                if (schema != null) {
                    CmsXmlContentDefinition contentDef = CmsXmlContentDefinition.unmarshal(cms, schema);
                    CmsMessages schemaMessages = contentDef.getContentHandler().getMessages(locale);
                    messages.addMessages(schemaMessages);

                }
            } catch (Exception e) {
                CmsJspStandardContextBean.LOG.warn(e.getLocalizedMessage(), e);
            }
        }
        resolver.setCmsObject(cms);
        resolver.setKeepEmptyMacros(true);
        resolver.setMessages(messages);
        return resolver;
    }

    /**
     * Gets the description of the formatter in the given locale.
     *
     * @param locale the locale to use
     * @return the description
     */
    public String description(Locale locale) {

        String result = m_formatter.getDescription(locale);
        result = m_macroResolver.resolveMacros(result);
        return result;
    }

    /**
     * Gets the matching container types.
     *
     * @return the container types the formatter fits into
     */
    public List<String> getContainerTypes() {

        return new ArrayList<>(m_formatter.getContainerTypes());
    }

    /**
     * Gets the path of the formatter definition XML file.
     *
     * @return the path of the formatter definition XML file
     */
    public String getDefinition() {

        return m_formatter.getLocation();
    }

    /**
     * Gets the localized description.
     *
     * @return the description
     */
    public String getDescription() {

        Locale locale = m_cms.getRequestContext().getLocale();
        String result = m_formatter.getDescription(locale);
        result = m_macroResolver.resolveMacros(result);
        return result;
    }

    /**
     * @see org.opencms.jsp.util.I_CmsFormatterInfo#getDescriptionKey()
     */
    public String getDescriptionKey() {

        return CmsKeyDummyMacroResolver.getKey(m_formatter.getDescription(null), m_macroResolver);
    }

    /**
     * Gets the raw description, without resolving any macros.
     *
     * @return the raw description
     */
    public String getDescriptionRaw() {

        return m_formatter.getDescription(null);

    }

    /**
     * Gets the display type of the formatter.
     *
     * @return the display type of the formatter
     */
    public String getDisplay() {

        return m_formatter.getDisplayType();
    }

    /**
     * @see org.opencms.jsp.util.I_CmsFormatterInfo#getIsActive()
     */
    public boolean getIsActive() {

        return true;
    }

    /**
     * Checks if the formatter is a detail formatter.
     *
     * @return true if the formatter is a detail formatter
     */
    public boolean getIsDetailFormatter() {

        return m_formatter.isDetailFormatter();
    }

    /**
     * Checks if the formatter is a display formatter.
     *
     * @return true if the formatter is a display formatter
     */
    public boolean getIsDisplayFormatter() {

        return m_formatter.isDisplayFormatter();
    }

    /**
     * @see org.opencms.jsp.util.I_CmsFormatterInfo#getIsFormatter()
     */
    public boolean getIsFormatter() {

        return m_formatter instanceof CmsFormatterBean;
    }

    /**
     * @see org.opencms.jsp.util.I_CmsFormatterInfo#getIsFunction()
     */
    public boolean getIsFunction() {

        return m_formatter instanceof CmsFunctionFormatterBean;
    }

    /**
     * @see org.opencms.jsp.util.I_CmsFormatterInfo#getIsResourceType()
     */
    public boolean getIsResourceType() {

        return false;
    }

    /**
     * Gets the JSP path.
     *
     * @return the JSP path
     */
    public String getJsp() {

        if (m_formatter instanceof CmsFunctionFormatterBean) {
            return ((CmsFunctionFormatterBean)m_formatter).getRealJspRootPath();
        } else {
            return m_formatter.getJspRootPath();
        }
    }

    /**
     * Gets the formatter key.
     *
     * @return the formatter key
     */
    public String getKey() {

        return m_formatter.getKey();
    }

    /**
     * Gets the maximum container width.
     *
     * @return the maximum container width
     */
    public int getMaxWidth() {

        return m_formatter.getMaxWidth();
    }

    /**
     * Gets the minimum container width.
     *
     * @return the minimum container width
     */
    public int getMinWidth() {

        return m_formatter.getMinWidth();
    }

    /**
     * @see org.opencms.jsp.util.I_CmsFormatterInfo#getName()
     */
    public String getName() {

        return getKey();
    }

    /**
     * Gets the user-readable formatter name.
     *
     * @return the user-readable name
     */
    public String getNiceName() {

        return m_formatter.getNiceName(m_cms.getRequestContext().getLocale());
    }

    /**
     * Gets the localization key for the nice name, if one was used, or null otherwise.
     *
     * @return the localization key
     */
    public String getNiceNameKey() {

        return CmsKeyDummyMacroResolver.getKey(m_formatter.getNiceName(null), m_macroResolver);
    }

    /**
     * Gets the raw nice name, without resolving any macros.
     *
     * @return the raw nice name
     */
    public String getNiceNameRaw() {

        return m_formatter.getNiceName(null);
    }

    /**
     * Returns the rank of the formatter.
     *
     * @return the rank
     */
    public int getRank() {

        return m_formatter.getRank();
    }

    /**
     * Gets a list of wrapper beans for the element setting definitions.
     *
     * @return the element setting definition wrappers
     */
    public List<CmsSettingDefinitionWrapper> getSettings() {

        Map<String, CmsXmlContentProperty> settingDefs = m_formatter.getSettings(m_config);
        List<CmsSettingDefinitionWrapper> result = new ArrayList<>();
        for (Map.Entry<String, CmsXmlContentProperty> entry : settingDefs.entrySet()) {
            CmsSettingDefinitionWrapper setting = new CmsSettingDefinitionWrapper(
                m_cms,
                entry.getValue(),
                m_macroResolver);
            result.add(setting);
        }
        return result;

    }

    /**
     * Gets the resource types.
     *
     * @return the resource types
     */
    public List<String> getTypes() {

        List<String> result = new ArrayList<>(m_formatter.getResourceTypeNames());
        Collections.sort(result);
        return result;
    }

    /**
     * Gets the nice name of the formatter in the given locale.
     *
     * @param locale the locale to use
     * @return the nice name of the formatter
     */
    public String niceName(Locale locale) {

        return m_formatter.getNiceName(locale);
    }

}
