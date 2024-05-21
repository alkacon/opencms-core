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

import org.opencms.file.CmsObject;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.CmsSelectWidgetOption;
import org.opencms.xml.content.CmsXmlContentProperty;
import org.opencms.xml.content.CmsXmlContentProperty.Visibility;
import org.opencms.xml.content.CmsXmlContentPropertyHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.logging.Log;

/**
 * Wrapper used to access element setting definition information in JSP code.
 */
public class CmsSettingDefinitionWrapper implements I_CmsInfoWrapper {

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSettingDefinitionWrapper.class);

    /** The definition. containing the original message keys.*/
    private CmsXmlContentProperty m_definitionWithKeys;

    /** The raw setting definition (unresolved macros). */
    private CmsXmlContentProperty m_rawDefinition;

    /** The resolved definition. */
    private CmsXmlContentProperty m_resolvedDefinition;

    /** The CmsObject used. */
    private CmsObject m_cms;

    /**
     * Creates a new instance.
     *
     * @param cms the current CMS context
     * @param settingDef the raw setting definition
     * @param resolver the macro resolver to use
     */
    public CmsSettingDefinitionWrapper(CmsObject cms, CmsXmlContentProperty settingDef, CmsMacroResolver resolver) {

        m_rawDefinition = settingDef;
        m_cms = cms;
        m_resolvedDefinition = CmsXmlContentPropertyHelper.resolveMacrosInProperty(settingDef, resolver);
        CmsKeyDummyMacroResolver keyResolver = new CmsKeyDummyMacroResolver(resolver);
        m_definitionWithKeys = CmsXmlContentPropertyHelper.resolveMacrosInProperty(settingDef, keyResolver);
    }

    /**
     * Gets the default value.
     *
     * @return the default value
     */
    public String getDefaultValue() {

        return m_resolvedDefinition.getDefault();
    }

    /**
     * Gets the description.
     *
     * @return the description
     */
    public String getDescription() {

        return m_resolvedDefinition.getDescription();
    }

    /**
     * Gets the description key.
     *
     * @return the description key
     */
    public String getDescriptionKey() {

        return CmsKeyDummyMacroResolver.getKey(m_definitionWithKeys.getDescription());
    }

    /**
     * Gets the raw description configured for the setting.
     *
     * @return the raw description
     */
    public String getDescriptionRaw() {

        return m_rawDefinition.getDescription();
    }

    /**
     * Gets the display name.
     *
     * @return the display name
     */
    public String getDisplayName() {

        return m_resolvedDefinition.getNiceName();
    }

    /**
     * Gets the display name key.
     *
     * @return the display name key
     */
    public String getDisplayNameKey() {

        return CmsKeyDummyMacroResolver.getKey(m_definitionWithKeys.getNiceName());
    }

    /**
     * Gets the raw configured display name.
     *
     * @return the raw display name
     */
    public String getDisplayNameRaw() {
        return m_rawDefinition.getNiceName();
    }

    /**
     * Tries to interpret the widget configuration as a select option configuration and returns the list of select options if this succeeds, and null otherwise.
     *
     * @return the list of parsed select options, or null if the widget configuration couldn't be interpreted that way
     */
    public List<CmsSelectWidgetOption> getParsedSelectOptions() {

        String widgetConfig = getWidgetConfig();
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(widgetConfig)) {
            // passing an empty/null configuration to parseOptions would result in an empty list, not null, and we want null here
            return null;
        }
        try {
            List<CmsSelectWidgetOption> options = org.opencms.widgets.CmsSelectWidgetOption.parseOptions(widgetConfig);
            List<CmsSelectWidgetOption> result = new ArrayList<>();
            Set<String> values = options.stream().map(option -> option.getValue()).collect(Collectors.toSet());
            String defaultValue = getDefaultValue();
            Locale locale = m_cms.getRequestContext().getLocale();
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(defaultValue) || !values.contains(defaultValue)) {
                CmsSelectWidgetOption noValue = new CmsSelectWidgetOption(
                    "",
                    true,
                    org.opencms.gwt.Messages.get().getBundle(locale).key(
                        org.opencms.gwt.Messages.GUI_SELECTBOX_EMPTY_SELECTION_0));
                result.add(noValue);
            }

            result.addAll(options);
            return result;
        } catch (Exception e) {
            LOG.info(e.getLocalizedMessage(), e);
            return null;
        }
    }

    /**
     * Gets the property name.
     *
     * @return the property name
     */
    public String getPropertyName() {

        return m_resolvedDefinition.getName();
    }

    /**
     * Gets the visibility.
     *
     * @return the visibility
     */
    public String getVisibility() {

        return "" + m_resolvedDefinition.getVisibility(Visibility.elementAndParentIndividual);
    }

    /**
     * Gets the widget.
     *
     * @return the widget
     */
    public String getWidget() {

        return m_resolvedDefinition.getWidget();
    }

    /**
     * Gets the widget config.
     *
     * @return the widget config
     */
    public String getWidgetConfig() {

        return m_resolvedDefinition.getWidgetConfiguration();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        // for debugging
        try {
            return BeanUtils.describe(this).toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "???";
        }

    }

}
