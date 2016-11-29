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

package org.opencms.widgets;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsMessages;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.content.I_CmsXmlContentHandler.DisplayType;
import org.opencms.xml.types.A_CmsXmlContentValue;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.logging.Log;

/**
 * Provides a display only widget, for use on a widget dialog.<p>
 *
 * @since 6.0.0
 */
public class CmsLocationPickerWidget extends A_CmsWidget implements I_CmsADEWidget {

    /** The default widget configuration. */
    private static final String DEFAULT_CONFIG = "{\"edit\":[\"map\", \"address\", \"coords\", \"size\", \"zoom\", \"type\", \"mode\"]}";

    /** Key post fix, so you can display different help text if used a "normal" widget, and a display widget. */
    private static final String DISABLED_POSTFIX = ".disabled";

    /** The configuration key for the Google maps API key. */
    public static final String CONFIG_API_KEY = "apiKey";

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsLocationPickerWidget.class);

    /**
     * Creates a new input widget.<p>
     */
    public CmsLocationPickerWidget() {

        // empty constructor is required for class registration
        this("");
    }

    /**
     * Creates a new input widget with the given configuration.<p>
     *
     * @param configuration the configuration to use
     */
    public CmsLocationPickerWidget(String configuration) {

        super(configuration);
    }

    /**
     * @see org.opencms.widgets.I_CmsADEWidget#getConfiguration(org.opencms.file.CmsObject, org.opencms.xml.types.A_CmsXmlContentValue, org.opencms.i18n.CmsMessages, org.opencms.file.CmsResource, java.util.Locale)
     */
    public String getConfiguration(
        CmsObject cms,
        A_CmsXmlContentValue schemaType,
        CmsMessages messages,
        CmsResource resource,
        Locale contentLocale) {

        String config = getConfiguration();
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(config)) {
            config = DEFAULT_CONFIG;
        } else {
            if (!config.startsWith("{")) {
                config = "{" + config + "}";
            }
            try {
                // make sure the configuration is a parsable JSON string
                JSONObject conf = new JSONObject(config);
                if (!conf.has(CONFIG_API_KEY)) {
                    String sitePath;
                    if (resource.getStructureId().isNullUUID()) {
                        sitePath = "/";
                    } else {
                        sitePath = cms.getSitePath(resource);
                    }
                    try {
                        String apiKey = cms.readPropertyObject(
                            sitePath,
                            CmsPropertyDefinition.PROPERTY_GOOGLE_API_KEY,
                            true).getValue();

                        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(apiKey)) {
                            conf.put(CONFIG_API_KEY, apiKey);
                        }
                    } catch (CmsException e) {
                        LOG.error(e.getLocalizedMessage(), e);
                    }
                }
                config = conf.toString();
            } catch (JSONException e) {
                config = DEFAULT_CONFIG;
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        return config;
    }

    /**
     * @see org.opencms.widgets.I_CmsADEWidget#getCssResourceLinks(org.opencms.file.CmsObject)
     */
    public List<String> getCssResourceLinks(CmsObject cms) {

        return null;
    }

    /**
     * @see org.opencms.widgets.I_CmsADEWidget#getDefaultDisplayType()
     */
    public DisplayType getDefaultDisplayType() {

        return DisplayType.singleline;
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#getDialogWidget(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog, org.opencms.widgets.I_CmsWidgetParameter)
     */
    public String getDialogWidget(CmsObject cms, I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter param) {

        String value = param.getStringValue(cms);
        String localizedValue = value;
        if (CmsStringUtil.TRUE.equalsIgnoreCase(value) || CmsStringUtil.FALSE.equalsIgnoreCase(value)) {
            boolean booleanValue = Boolean.valueOf(value).booleanValue();
            if (booleanValue) {
                localizedValue = Messages.get().getBundle(widgetDialog.getLocale()).key(Messages.GUI_LABEL_TRUE_0);
            } else {
                localizedValue = Messages.get().getBundle(widgetDialog.getLocale()).key(Messages.GUI_LABEL_FALSE_0);
            }
        }

        String id = param.getId();
        StringBuffer result = new StringBuffer(16);
        result.append("<td class=\"xmlTd\">");
        result.append("<span class=\"xmlInput textInput\" style=\"border: 0px solid black;\">");
        if (CmsStringUtil.isNotEmpty(getConfiguration())) {
            result.append(getConfiguration());
        } else {
            result.append(localizedValue);
        }
        result.append("</span>");
        result.append("<input type=\"hidden\"");
        result.append(" name=\"");
        result.append(id);
        result.append("\" id=\"");
        result.append(id);
        result.append("\" value=\"");
        result.append(CmsEncoder.escapeXml(value));
        result.append("\">");
        result.append("</td>");

        return result.toString();
    }

    /**
     * @see org.opencms.widgets.A_CmsWidget#getHelpBubble(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog, org.opencms.widgets.I_CmsWidgetParameter)
     */
    @Override
    public String getHelpBubble(CmsObject cms, I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter param) {

        StringBuffer result = new StringBuffer(128);
        String locKey = getDisabledHelpKey(param);
        String locValue = widgetDialog.getMessages().key(locKey, true);
        if (locValue == null) {
            // there was no help message found for this key, so return a spacer cell
            return widgetDialog.dialogHorizontalSpacer(16);
        } else {
            result.append("<td>");
            result.append("<img name=\"img");
            result.append(locKey);
            result.append("\" id=\"img");
            result.append(locKey);
            result.append("\" src=\"");
            result.append(OpenCms.getLinkManager().substituteLink(cms, "/system/workplace/resources/commons/help.png"));
            result.append("\" alt=\"\" border=\"0\"");
            if (widgetDialog.useNewStyle()) {
                result.append(getJsHelpMouseHandler(widgetDialog, locKey, null));
            } else {
                result.append(
                    getJsHelpMouseHandler(
                        widgetDialog,
                        locKey,
                        CmsEncoder.escape(locValue, cms.getRequestContext().getEncoding())));
            }
            result.append("></td>");
            return result.toString();
        }
    }

    /**
     * @see org.opencms.widgets.A_CmsWidget#getHelpText(org.opencms.widgets.I_CmsWidgetDialog, org.opencms.widgets.I_CmsWidgetParameter)
     */
    @Override
    public String getHelpText(I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter param) {

        String helpId = getDisabledHelpKey(param);
        Set<String> helpIdsShown = widgetDialog.getHelpMessageIds();
        if (helpIdsShown.contains(helpId)) {
            // help hey has already been included in output
            return "";
        }
        helpIdsShown.add(helpId);

        // calculate the key
        String locValue = widgetDialog.getMessages().key(helpId, true);
        if (locValue == null) {
            // there was no help message found for this key, so return an empty string
            return "";
        } else {
            if (widgetDialog.useNewStyle()) {
                StringBuffer result = new StringBuffer(128);
                result.append("<div class=\"help\" id=\"help");
                result.append(helpId);
                result.append("\"");
                result.append(getJsHelpMouseHandler(widgetDialog, helpId, helpId));
                result.append(">");
                result.append(locValue);
                result.append("</div>\n");
                return result.toString();
            } else {
                return "";
            }
        }
    }

    /**
     * @see org.opencms.widgets.I_CmsADEWidget#getInitCall()
     */
    public String getInitCall() {

        return null;
    }

    /**
     * @see org.opencms.widgets.I_CmsADEWidget#getJavaScriptResourceLinks(org.opencms.file.CmsObject)
     */
    public List<String> getJavaScriptResourceLinks(CmsObject cms) {

        return null;
    }

    /**
     * @see org.opencms.widgets.I_CmsADEWidget#getWidgetName()
     */
    public String getWidgetName() {

        return CmsLocationPickerWidget.class.getName();
    }

    /**
     * @see org.opencms.widgets.I_CmsADEWidget#isInternal()
     */
    public boolean isInternal() {

        return true;
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#newInstance()
     */
    public I_CmsWidget newInstance() {

        return new CmsLocationPickerWidget(getConfiguration());
    }

    /**
     * Returns the localized help key for the provided widget parameter.<p>
     *
     * @param param the widget parameter to return the localized help key for
     *
     * @return the localized help key for the provided widget parameter
     */
    private String getDisabledHelpKey(I_CmsWidgetParameter param) {

        StringBuffer result = new StringBuffer(64);
        result.append(LABEL_PREFIX);
        result.append(param.getKey());
        result.append(HELP_POSTFIX);
        result.append(DISABLED_POSTFIX);
        return result.toString();
    }
}