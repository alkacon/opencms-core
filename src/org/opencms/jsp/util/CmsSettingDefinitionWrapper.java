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

import org.opencms.main.CmsLog;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.widgets.CmsSelectWidgetOption;
import org.opencms.xml.content.CmsXmlContentProperty;
import org.opencms.xml.content.CmsXmlContentProperty.Visibility;
import org.opencms.xml.content.CmsXmlContentPropertyHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.logging.Log;

/**
 * Wrapper used to access element setting definition information in JSP code.
 */
public class CmsSettingDefinitionWrapper {

    /**
     * Macro resolver used to temporarily replace localization message macros with random UUIDs and then replace the UUIDs
     * with the original key after all other macro processing has happened.
     *
     * <p>We need this because we want to preserve the message key, but evaluate macros they may be nested in.
     */
    private class CmsKeyDummyMacroResolver extends CmsMacroResolver {

        /** The macro resolver to delegate to. */
        private CmsMacroResolver m_delegate;

        /** The map containing the original string for each ID it was replaced with. */
        private Map<CmsUUID, String> m_keys = new HashMap<>();

        /**
         * Creates a new instance
         *
         * @param delegate the macro resolver to delegate to
         */
        public CmsKeyDummyMacroResolver(CmsMacroResolver delegate) {

            m_delegate = delegate;
        }

        /**
         * @see org.opencms.util.CmsMacroResolver#getMacroValue(java.lang.String)
         */
        @Override
        public String getMacroValue(String macro) {

            if (macro.startsWith(CmsMacroResolver.KEY_LOCALIZED_PREFIX)) {
                String key = macro.substring(CmsMacroResolver.KEY_LOCALIZED_PREFIX.length());
                CmsUUID id = new CmsUUID();
                m_keys.put(id, key);
                return id.toString();
            } else {
                String result = m_delegate.getMacroValue(macro);
                return result;
            }
        }

        /**
         * @see org.opencms.util.CmsMacroResolver#resolveMacros(java.lang.String)
         */
        @Override
        public String resolveMacros(String input) {

            String processedInput = super.resolveMacros(input);
            @SuppressWarnings("synthetic-access")
            String result = CmsStringUtil.substitute(UUID_PATTERN, processedInput, (s, matcher) -> {
                CmsUUID id = new CmsUUID(matcher.group());
                if (m_keys.containsKey(id)) {
                    return "%(key." + m_keys.get(id) + ")";
                } else {
                    return matcher.group();
                }
            });
            return result;
        }

    }

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSettingDefinitionWrapper.class);

    /** Pattern to match message key macros. */
    private static final Pattern PATTERN_MESSAGE = Pattern.compile("^%\\(key\\.([^\\)]++)\\)$");

    /** Pattern to match UUIDs. */
    private static final Pattern UUID_PATTERN = Pattern.compile(CmsUUID.UUID_REGEX);

    /** The definition. containing the original message keys.*/
    private CmsXmlContentProperty m_definitionWithKeys;

    /** The raw setting definition (unresolved macros). */
    private CmsXmlContentProperty m_rawDefinition;

    /** The resolved definition. */
    private CmsXmlContentProperty m_resolvedDefinition;

    /**
     * Creates a new instance.
     *
     * @param settingDef the raw setting definition
     * @param resolver the macro resolver to use
     */
    public CmsSettingDefinitionWrapper(CmsXmlContentProperty settingDef, CmsMacroResolver resolver) {

        m_rawDefinition = settingDef;
        m_resolvedDefinition = CmsXmlContentPropertyHelper.resolveMacrosInProperty(settingDef, resolver);
        CmsKeyDummyMacroResolver keyResolver = new CmsKeyDummyMacroResolver(resolver);
        m_definitionWithKeys = CmsXmlContentPropertyHelper.resolveMacrosInProperty(settingDef, keyResolver);
    }

    /**
     * Extracts the message from a string of the form %(key.{message}), or returns null if the input string
     * does not have this form.
     *
     * @param s the input string
     * @return the key the extracted message key
     */
    private static String getKey(String s) {

        if (s == null) {
            return null;
        }
        Matcher matcher = PATTERN_MESSAGE.matcher(s);
        if (matcher.matches()) {
            return matcher.group(1);
        } else {
            return null;
        }
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

        return getKey(m_definitionWithKeys.getDescription());
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

        return getKey(m_definitionWithKeys.getNiceName());
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
            return org.opencms.widgets.CmsSelectWidgetOption.parseOptions(widgetConfig);
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
