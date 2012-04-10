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

package org.opencms.widgets;

import org.opencms.file.CmsObject;
import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.i18n.CmsMessages;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.I_CmsMacroResolver;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provides a standard HTML form input widget for overwriting localized values of a resource bundle, for use on a widget dialog.<p>
 *
 * The resource bundle is configured with the widget configuration attribute. An optional key name to look up in the bundle
 * can be given, too, in case it is different from the element name: <code>key=mykey</code>.<p>
 * 
 * The locale to get the value for can be configured, too, by adding a configuration directive: <code>locale=en</code>.<p>
 * 
 * Example: <code><layout element="elemname" widget="LocalizationWidget" configuration="org.opencms.workplace.messages|key=mykey|locale=en" /></code>.<p>
 *
 * To use the stored localization values and have the values of the resource bundles as fallback,
 * use the {@link org.opencms.xml.CmsXmlMessages} object.<p>
 * 
 * @since 6.5.4
 */
public class CmsLocalizationWidget extends A_CmsWidget {

    /** The option for the localized key name. */
    public static final String OPTION_KEY = "key=";

    /** The option for the locale to use.  */
    public static final String OPTION_LOCALE = "locale=";

    /** Pattern to get OpenCms like macros, e.g. "%(0)". */
    private static Pattern PATTERN_MACRO = Pattern.compile(".*("
        + I_CmsMacroResolver.MACRO_DELIMITER
        + "\\"
        + I_CmsMacroResolver.MACRO_START
        + ")(\\d*)(\\"
        + I_CmsMacroResolver.MACRO_END
        + ").*");

    /** Pattern to get message bundle arguments, e.g. "{0}". */
    private static Pattern PATTERN_MESSAGEARGUMENT = Pattern.compile(".*(\\{)(\\d*)(\\}).*");

    /** The bundle key (optional, if not equal to the element name). */
    private String m_bundleKey;

    /** The locale to get the value for. */
    private Locale m_locale;

    /** The localized bundle to get the value from. */
    private CmsMessages m_messages;

    /**
     * Creates a new input localization widget.<p>
     */
    public CmsLocalizationWidget() {

        // empty constructor is required for class registration
        this("");
    }

    /**
     * Creates a new input localization widget with the given configuration.<p>
     * 
     * @param configuration the configuration to use
     */
    public CmsLocalizationWidget(String configuration) {

        super(configuration);
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#getDialogWidget(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog, org.opencms.widgets.I_CmsWidgetParameter)
     */
    public String getDialogWidget(CmsObject cms, I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter param) {

        String id = param.getId();
        // initialize bundle
        initConfiguration(cms, param);

        StringBuffer result = new StringBuffer(256);

        result.append("<td class=\"xmlTd\">");
        result.append("<input class=\"xmlInput textInput");
        if (param.hasError()) {
            result.append(" xmlInputError");
        }
        result.append("\"");
        result.append(" name=\"");
        result.append(id);
        result.append("\" id=\"");
        result.append(id);
        result.append("\" value=\"");

        // determine value to show in editor
        String value = m_messages.key(m_bundleKey);
        if ((CmsStringUtil.isNotEmptyOrWhitespaceOnly(param.getStringValue(cms)) && !value.equals(param.getStringValue(cms)))
            || value.startsWith(CmsMessages.UNKNOWN_KEY_EXTENSION)) {
            // saved value is provided and different from localized value in bundle or no value found in bundle, use it
            value = param.getStringValue(cms);
            // replace OpenCms macro syntax with message bundle arguments
            Matcher matcher = PATTERN_MACRO.matcher(value);
            while (matcher.matches()) {
                int startIndex = matcher.start(1);
                int endIndex = matcher.end(3);
                String number = matcher.group(2);
                value = value.substring(0, startIndex) + "{" + number + "}" + value.substring(endIndex);
                matcher = PATTERN_MACRO.matcher(value);
            }

        }

        result.append(CmsEncoder.escapeXml(value));
        result.append("\">");
        result.append("</td>");

        return result.toString();
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#newInstance()
     */
    public I_CmsWidget newInstance() {

        return new CmsLocalizationWidget(getConfiguration());
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#setEditorValue(org.opencms.file.CmsObject, java.util.Map, org.opencms.widgets.I_CmsWidgetDialog, org.opencms.widgets.I_CmsWidgetParameter)
     */
    @Override
    public void setEditorValue(
        CmsObject cms,
        Map<String, String[]> formParameters,
        I_CmsWidgetDialog widgetDialog,
        I_CmsWidgetParameter param) {

        String[] values = formParameters.get(param.getId());
        if ((values != null) && (values.length > 0)) {
            // initialize bundle
            initConfiguration(cms, param);
            String value = m_messages.key(m_bundleKey);
            if (value.equals(values[0].trim())) {
                // value is equal to localized value, do not save
                value = "";
            } else {
                // value is different, save it
                value = values[0];
                // now replace message bundle like argument placeholders like "{0}" with OpenCms macros
                Matcher matcher = PATTERN_MESSAGEARGUMENT.matcher(value);
                while (matcher.matches()) {
                    int startIndex = matcher.start(1);
                    int endIndex = matcher.end(3);
                    String number = CmsMacroResolver.formatMacro(matcher.group(2));
                    // replace arguments with macros
                    value = value.substring(0, startIndex) + number + value.substring(endIndex);
                    matcher = PATTERN_MESSAGEARGUMENT.matcher(value);
                }
            }
            param.setStringValue(cms, value);
        }
    }

    /**
     * Initializes the localized bundle to get the value from, the optional key name and the optional locale.<p>
     * 
     * @param cms an initialized instance of a CmsObject
     * @param param the widget parameter to generate the widget for
     */
    protected void initConfiguration(CmsObject cms, I_CmsWidgetParameter param) {

        // set the default bundle key
        m_bundleKey = param.getName();
        // set the default locale for XML contents
        m_locale = cms.getRequestContext().getLocale();
        try {
            I_CmsXmlContentValue value = (I_CmsXmlContentValue)param;
            m_locale = value.getLocale();
        } catch (Exception e) {
            // ignore, this is no XML content
        }

        // check the message bundle
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(getConfiguration())) {
            //initialize messages, the optional bundle key name and the optional locale from configuration String
            String bundleName = "";
            List<String> configs = CmsStringUtil.splitAsList(getConfiguration(), '|');
            Iterator<String> i = configs.iterator();
            while (i.hasNext()) {
                String config = i.next();
                if (config.startsWith(OPTION_KEY)) {
                    m_bundleKey = config.substring(OPTION_KEY.length());
                } else if (config.startsWith(OPTION_LOCALE)) {
                    m_locale = CmsLocaleManager.getLocale(config.substring(OPTION_LOCALE.length()));
                } else {
                    bundleName = config;
                }
            }
            // create messages object
            m_messages = new CmsMessages(bundleName, m_locale);
        } else {
            // initialize empty messages object to avoid NPE
            m_messages = new CmsMessages("", m_locale);
        }
    }
}