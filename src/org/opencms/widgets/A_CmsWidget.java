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
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsMessages;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Base class for XML editor widgets.<p>
 *
 * @since 6.0.0
 */
public abstract class A_CmsWidget implements I_CmsWidget {

    /** Inner class to generate the I_CmsWidgetDialog. */
    public class CmsDummyWidgetDialog implements I_CmsWidgetDialog {

        /** The locale of this widget. */
        private Locale m_locale;

        /** The massage of this widget. */
        private CmsMessages m_message;

        /** The resource being edited. */
        private CmsResource m_resource;

        /** Constructor.<p>
         * @param locale the locale of the dialog
         * @param message the message of the dialog
         */
        public CmsDummyWidgetDialog(Locale locale, CmsMessages message) {

            m_locale = locale;
            m_message = message;
        }

        /**
         * @see org.opencms.widgets.I_CmsWidgetDialog#button(java.lang.String, java.lang.String, java.lang.String, java.lang.String, int)
         */
        public String button(String href, String target, String image, String label, int type) {

            return null;
        }

        /**
         * @see org.opencms.widgets.I_CmsWidgetDialog#buttonBar(int)
         */
        public String buttonBar(int segment) {

            return null;
        }

        /**
         * @see org.opencms.widgets.I_CmsWidgetDialog#buttonBarHorizontalLine()
         */
        public String buttonBarHorizontalLine() {

            return null;
        }

        /**
         * @see org.opencms.widgets.I_CmsWidgetDialog#buttonBarSeparator(int, int)
         */
        public String buttonBarSeparator(int leftPixel, int rightPixel) {

            return null;
        }

        /**
         * @see org.opencms.widgets.I_CmsWidgetDialog#buttonBarSpacer(int)
         */
        public String buttonBarSpacer(int width) {

            return null;
        }

        /**
         * @see org.opencms.widgets.I_CmsWidgetDialog#buttonBarStartTab(int, int)
         */
        public String buttonBarStartTab(int leftPixel, int rightPixel) {

            return null;
        }

        /**
         * @see org.opencms.widgets.I_CmsWidgetDialog#dialogHorizontalSpacer(int)
         */
        public String dialogHorizontalSpacer(int width) {

            return null;
        }

        /**
         * @see org.opencms.widgets.I_CmsWidgetDialog#getButtonStyle()
         */
        public int getButtonStyle() {

            return 0;
        }

        /**
         * @see org.opencms.widgets.I_CmsWidgetDialog#getHelpMessageIds()
         */
        public Set<String> getHelpMessageIds() {

            return null;
        }

        /**
         * @see org.opencms.widgets.I_CmsWidgetDialog#getLocale()
         */
        public Locale getLocale() {

            return m_locale;
        }

        /**
         * @see org.opencms.widgets.I_CmsWidgetDialog#getMessages()
         */
        public CmsMessages getMessages() {

            return m_message;
        }

        /**
         * Gets the resource being edited.
         *
         * @return the resource being edited
         */
        public CmsResource getResource() {

            return m_resource;
        }

        /**
         * @see org.opencms.widgets.I_CmsWidgetDialog#getUserAgent()
         */
        public String getUserAgent() {

            return null;
        }

        /**
         * Sets the resource being edited.<p>
         *
         * @param resource the resource being edited
         */
        public void setResource(CmsResource resource) {

            m_resource = resource;
        }

        /**
         * @see org.opencms.widgets.I_CmsWidgetDialog#useNewStyle()
         */
        public boolean useNewStyle() {

            return false;
        }

    }

    /** Postfix for melp message locale. */
    public static final String HELP_POSTFIX = ".help";

    /** Prefix for message locales. */
    public static final String LABEL_PREFIX = "label.";

    /** The configuration options of this widget. */
    private String m_configuration;

    /**
     * Default constructor.<p>
     */
    protected A_CmsWidget() {

        setConfiguration("");
    }

    /**
     * Constructor for preprocessing the configuration string.<p>
     *
     * @param configuration the configuration string
     */
    protected A_CmsWidget(String configuration) {

        setConfiguration(configuration);
    }

    /**
     * Returns the localized help key for the provided widget parameter.<p>
     * @param param the widget parameter to return the localized help key for
     *
     * @return the localized help key for the provided widget parameter
     */
    public static String getHelpKey(I_CmsWidgetParameter param) {

        // calculate the key
        StringBuffer result = new StringBuffer(64);
        result.append(LABEL_PREFIX);
        result.append(param.getKey());
        result.append(HELP_POSTFIX);

        return result.toString();
    }

    /**
     * Returns the localized label key for the provided widget parameter.<p>
     * @param param the widget parameter to return the localized label key for
     *
     * @return the localized label key for the provided widget parameter
     */
    public static String getLabelKey(I_CmsWidgetParameter param) {

        // calculate the key
        StringBuffer result = new StringBuffer(64);
        result.append(LABEL_PREFIX);
        result.append(param.getKey());
        return result.toString();
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }
        if (obj instanceof A_CmsWidget) {
            // widgets are equal if they use the same class
            return getClass().getName().equals(obj.getClass().getName());
        }
        return false;
    }

    /**
     * Returns the configuration string.<p>
     *
     * @return the configuration string
     */
    public String getConfiguration() {

        return m_configuration;
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#getDialogHtmlEnd(org.opencms.file.CmsObject, I_CmsWidgetDialog, I_CmsWidgetParameter)
     */
    public String getDialogHtmlEnd(CmsObject cms, I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter value) {

        return getHelpText(widgetDialog, value);
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#getDialogIncludes(org.opencms.file.CmsObject, I_CmsWidgetDialog)
     */
    public String getDialogIncludes(CmsObject cms, I_CmsWidgetDialog widgetDialog) {

        return "";
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#getDialogInitCall(org.opencms.file.CmsObject, I_CmsWidgetDialog)
     */
    public String getDialogInitCall(CmsObject cms, I_CmsWidgetDialog widgetDialog) {

        return "";
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#getDialogInitMethod(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog)
     */
    public String getDialogInitMethod(CmsObject cms, I_CmsWidgetDialog widgetDialog) {

        return "";
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#getHelpBubble(org.opencms.file.CmsObject, I_CmsWidgetDialog, I_CmsWidgetParameter)
     */
    public String getHelpBubble(CmsObject cms, I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter param) {

        StringBuffer result = new StringBuffer(128);
        String locKey = getHelpKey(param);
        String locValue = widgetDialog.getMessages().key(locKey, true);
        if (!widgetDialog.useNewStyle()) {
            // use real ID for XML contents to avoid display issues
            locKey = param.getId();
        }
        if (locValue == null) {
            // there was no help message found for this key, so return a spacer cell
            return widgetDialog.dialogHorizontalSpacer(16);
        } else {
            result.append("<td>");
            result.append("<img id=\"img");
            result.append(locKey);
            result.append("\" ");
            result.append("title=\"");
            result.append(CmsEncoder.escapeXml(locValue));
            result.append("\" ");
            result.append("\" src=\"");
            result.append(OpenCms.getLinkManager().substituteLink(cms, "/system/workplace/resources/commons/help.png"));
            result.append("\" alt=\"\" border=\"0\"");
            if (widgetDialog.useNewStyle()) {
                // static divs are used in admin
                result.append(getJsHelpMouseHandler(widgetDialog, locKey, null));
            } else {
                // can't use method in CmsEncoder because we need to keep < > for HTML in help text
                locValue = CmsStringUtil.substitute(locValue, "\"", "&quot;");
                // dynamic help texts in xml content editor
                result.append(getJsHelpMouseHandler(widgetDialog, locKey, CmsStringUtil.escapeJavaScript(locValue)));
            }
            result.append("></td>");
            return result.toString();
        }
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#getHelpText(I_CmsWidgetDialog, I_CmsWidgetParameter)
     */
    public String getHelpText(I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter param) {

        String helpId = getHelpKey(param);
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
                // create no static divs for xml content editor
                return "";
            }

        }
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#getWidgetStringValue(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog, org.opencms.widgets.I_CmsWidgetParameter)
     */
    public String getWidgetStringValue(CmsObject cms, I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter param) {

        if (param != null) {
            return param.getStringValue(cms);
        }
        return null;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        return getClass().getName().hashCode();
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#isCompactViewEnabled()
     */
    public boolean isCompactViewEnabled() {

        return true;
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#setConfiguration(java.lang.String)
     */
    public void setConfiguration(String configuration) {

        m_configuration = configuration;
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#setEditorValue(org.opencms.file.CmsObject, java.util.Map, org.opencms.widgets.I_CmsWidgetDialog, org.opencms.widgets.I_CmsWidgetParameter)
     */
    public void setEditorValue(
        CmsObject cms,
        Map<String, String[]> formParameters,
        I_CmsWidgetDialog widgetDialog,
        I_CmsWidgetParameter param) {

        String[] values = formParameters.get(param.getId());
        if ((values != null) && (values.length > 0)) {
            param.setStringValue(cms, values[0]);
        }
    }

    /**
     * Returns the HTML for the JavaScript mouse handlers that show / hide the help text.<p>
     *
     * This is required since the handler differs between the "Dialog" and the "Administration" mode.<p>
     *
     * @param widgetDialog the dialog where the widget is displayed on
     * @param key the key for the help bubble
     * @param value the localized help text, has to be an escaped String for JS usage, is only used in XML content editor
     *
     * @return the HTML for the JavaScript mouse handlers that show / hide the help text
     */
    protected String getJsHelpMouseHandler(I_CmsWidgetDialog widgetDialog, String key, String value) {

        String jsShow;
        String jsHide;
        String keyHide;
        if (widgetDialog.useNewStyle()) {
            // Administration style
            jsShow = "sMH";
            jsHide = "hMH";
            keyHide = "'" + key + "'";
        } else {
            // Dialog style
            jsShow = "showHelpText";
            jsHide = "hideHelpText";
            keyHide = "";
        }
        StringBuffer result = new StringBuffer(128);
        result.append(" onmouseover=\"");
        result.append(jsShow);
        result.append("('");
        result.append(key);
        if (!widgetDialog.useNewStyle()) {
            result.append("', '");
            result.append(value);
        }
        result.append("');\" onmouseout=\"");
        result.append(jsHide);
        result.append("(");
        result.append(keyHide);
        result.append(");\"");

        return result.toString();
    }

    /**
     * Creates the tags to include external javascript files.<p>
     *
     * @param fileName the absolute path to the javascript file
     * @return the tags to include external javascript files
     */
    protected String getJSIncludeFile(String fileName) {

        StringBuffer result = new StringBuffer(8);
        result.append("<script type=\"text/javascript\" src=\"");
        result.append(fileName);
        result.append("\"></script>");
        return result.toString();
    }
}