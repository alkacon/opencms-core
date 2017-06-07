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
import org.opencms.i18n.CmsMessages;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.xml.content.I_CmsXmlContentHandler.DisplayType;
import org.opencms.xml.types.A_CmsXmlContentValue;

import java.util.List;
import java.util.Locale;

/**
 * Provides an OpenCms Principal selection widget, for use on a widget dialog.<p>
 *
 * @since 6.5.6
 */
public class CmsPrincipalWidget extends A_CmsWidget implements I_CmsADEWidget {

    /** Configuration parameter to set the flags of the principals to display, optional. */
    public static final String CONFIGURATION_FLAGS = "flags";

    /** The the flags used in the popup window. */
    private Integer m_flags;

    /**
     * Creates a new principals selection widget.<p>
     */
    public CmsPrincipalWidget() {

        // empty constructor is required for class registration
        this("");
    }

    /**
     * Creates a new principals selection widget with the parameters to configure the popup window behaviour.<p>
     *
     * @param flags the group flags to restrict the group selection, can be <code>null</code>
     */
    public CmsPrincipalWidget(Integer flags) {

        m_flags = flags;
    }

    /**
     * @see org.opencms.widgets.I_CmsADEWidget#getDefaultDisplayType()
     */
    public DisplayType getDefaultDisplayType() {

        return DisplayType.singleline;
    }

    /**
     * Creates a new principals selection widget with the given configuration.<p>
     *
     * @param configuration the configuration to use
     */
    public CmsPrincipalWidget(String configuration) {

        super(configuration);
    }

    /**
     * Returns the needed java script for the search button.<p>
     *
     * @param id the id of the widget to generate the search button for
     * @param form the id of the form where to which the widget belongs
     *
     * @return javascript code
     */
    public String getButtonJs(String id, String form) {

        StringBuffer buttonJs = new StringBuffer(8);
        buttonJs.append("javascript:openPrincipalWin('");
        buttonJs.append(OpenCms.getSystemInfo().getOpenCmsContext());
        buttonJs.append("/system/workplace/commons/principal_selection.jsp");
        buttonJs.append("','" + form + "',  '");
        buttonJs.append(id);
        buttonJs.append("', document, '");
        if (m_flags != null) {
            buttonJs.append(m_flags);
        } else {
            buttonJs.append("null");
        }
        buttonJs.append("'");
        buttonJs.append(");");
        return buttonJs.toString();
    }

    /**
     * @see org.opencms.widgets.A_CmsWidget#getConfiguration()
     */
    @Override
    public String getConfiguration() {

        StringBuffer result = new StringBuffer(8);

        // append flags to configuration
        if (m_flags != null) {
            if (result.length() > 0) {
                result.append("|");
            }
            result.append(CONFIGURATION_FLAGS);
            result.append("=");
            result.append(m_flags);
        }
        return result.toString();
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

        return getConfiguration();
    }

    /**
     * @see org.opencms.widgets.I_CmsADEWidget#getCssResourceLinks(org.opencms.file.CmsObject)
     */
    public List<String> getCssResourceLinks(CmsObject cms) {

        return null;
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#getDialogIncludes(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog)
     */
    @Override
    public String getDialogIncludes(CmsObject cms, I_CmsWidgetDialog widgetDialog) {

        StringBuffer result = new StringBuffer(16);
        result.append(getJSIncludeFile(CmsWorkplace.getSkinUri() + "components/widgets/principalselector.js"));
        return result.toString();
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#getDialogWidget(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog, org.opencms.widgets.I_CmsWidgetParameter)
     */
    public String getDialogWidget(CmsObject cms, I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter param) {

        String id = param.getId();
        StringBuffer result = new StringBuffer(128);

        result.append("<td class=\"xmlTd\">");
        result.append(
            "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"maxwidth\"><tr><td style=\"width: 100%;\">");
        result.append("<input style=\"width: 99%;\" class=\"xmlInput");
        if (param.hasError()) {
            result.append(" xmlInputError");
        }
        result.append("\" value=\"");
        result.append(param.getStringValue(cms));
        result.append("\" name=\"");
        result.append(id);
        result.append("\" id=\"");
        result.append(id);
        result.append("\"></td>");
        result.append(widgetDialog.dialogHorizontalSpacer(10));
        result.append(
            "<td><table class=\"editorbuttonbackground\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tr>");

        result.append(
            widgetDialog.button(
                getButtonJs(id, "EDITOR"),
                null,
                "principal",
                org.opencms.workplace.Messages.GUI_DIALOG_BUTTON_SEARCH_0,
                widgetDialog.getButtonStyle()));
        result.append("</tr></table>");
        result.append("</td></tr></table>");

        result.append("</td>");

        return result.toString();
    }

    /**
     * Returns the flags, or <code>null</code> if all.<p>
     *
     * @return the flags, or <code>null</code> if all
     */
    public Integer getFlags() {

        return m_flags;
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

        return CmsPrincipalWidget.class.getName();
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

        return new CmsPrincipalWidget(getConfiguration());
    }

    /**
     * @see org.opencms.widgets.A_CmsWidget#setConfiguration(java.lang.String)
     */
    @Override
    public void setConfiguration(String configuration) {

        m_flags = null;
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(configuration)) {
            int flagsIndex = configuration.indexOf(CONFIGURATION_FLAGS);
            if (flagsIndex != -1) {
                // flags are given
                String flags = configuration.substring(CONFIGURATION_FLAGS.length() + 1);
                if (flags.indexOf('|') != -1) {
                    // cut eventual following configuration values
                    flags = flags.substring(0, flags.indexOf('|'));
                }
                try {
                    m_flags = Integer.valueOf(flags);
                } catch (Throwable t) {
                    // invalid flags
                }
            }
        }
        super.setConfiguration(configuration);
    }
}