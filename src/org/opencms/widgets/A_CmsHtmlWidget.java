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
import org.opencms.i18n.CmsEncoder;
import org.opencms.workplace.CmsWorkplace;

import java.util.Map;

/**
 * Provides a widget that creates a rich input field using the matching component, for use on a widget dialog.<p>
 *
 * The matching component is determined by checking the installed editors for the best matching component to use.<p>
 *
 * @since 6.0.1
 */
public abstract class A_CmsHtmlWidget extends A_CmsWidget {

    /** The configured Html widget options. */
    private CmsHtmlWidgetOption m_htmlWidgetOption;

    /**
     * Creates a new html editing widget.<p>
     */
    public A_CmsHtmlWidget() {

        // empty constructor is required for class registration
        super();
    }

    /**
     * Creates a new html editing widget with the given configuration.<p>
     *
     * @param configuration the configuration to use
     */
    public A_CmsHtmlWidget(CmsHtmlWidgetOption configuration) {

        super();
        m_htmlWidgetOption = configuration;
    }

    /**
     * Creates a new html editing widget with the given configuration.<p>
     *
     * @param configuration the configuration to use
     */
    public A_CmsHtmlWidget(String configuration) {

        super(configuration);
    }

    /**
     * @see org.opencms.widgets.A_CmsWidget#getConfiguration()
     */
    @Override
    public String getConfiguration() {

        if (super.getConfiguration() != null) {
            return super.getConfiguration();
        }
        return CmsHtmlWidgetOption.createConfigurationString(getHtmlWidgetOption());
    }

    /**
     * Returns the configured Html widget options.<p>
     *
     * @return the configured Html widget options
     */
    public CmsHtmlWidgetOption getHtmlWidgetOption() {

        return m_htmlWidgetOption;
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#setConfiguration(java.lang.String)
     */
    @Override
    public void setConfiguration(String configuration) {

        super.setConfiguration(configuration);
        m_htmlWidgetOption = new CmsHtmlWidgetOption(configuration);
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
            String val = CmsEncoder.decode(values[0], CmsEncoder.ENCODING_UTF_8);
            param.setStringValue(cms, val);
        }
    }

    /**
     * Sets the configured Html widget options.<p>
     *
     * @param htmlWidgetOption the configured Html widget options
     */
    public void setHtmlWidgetOption(CmsHtmlWidgetOption htmlWidgetOption) {

        m_htmlWidgetOption = htmlWidgetOption;
    }

    /**
     * Returns the HTML for the OpenCms specific button row for galleries and links.<p>
     *
     * Use this method to generate a button row with OpenCms specific dialog buttons in the
     * {@link org.opencms.widgets.I_CmsWidget#getDialogWidget(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog, org.opencms.widgets.I_CmsWidgetParameter)}
     * method to obtain the buttons.<p>
     *
     * Overwrite the method if the integrated editor needs a specific button generation
     * (e.g. add format select or toggle source code button) or if some buttons should not be available.<p>
     *
     * @param widgetDialog the dialog where the widget is used on
     * @param paramId the id of the current widget
     * @return the html String for the OpenCms specific button row
     */
    protected String buildOpenCmsButtonRow(I_CmsWidgetDialog widgetDialog, String paramId) {

        StringBuffer result = new StringBuffer(2048);
        // flag indicating if at least one button is active
        boolean buttonsActive = false;

        // generate button row start HTML
        result.append(buildOpenCmsButtonRow(CmsWorkplace.HTML_START, widgetDialog));

        // build the link buttons
        if (getHtmlWidgetOption().showLinkDialog()) {
            result.append(
                widgetDialog.button(
                    "javascript:setActiveEditor('"
                        + paramId
                        + "');openLinkDialog('"
                        + Messages.get().getBundle(widgetDialog.getLocale()).key(Messages.GUI_BUTTON_LINKTO_0)
                        + "');",
                    null,
                    "link",
                    "button.linkto",
                    widgetDialog.getButtonStyle()));
            buttonsActive = true;
        }
        if (getHtmlWidgetOption().showAnchorDialog()) {
            result.append(
                widgetDialog.button(
                    "javascript:setActiveEditor('"
                        + paramId
                        + "');openAnchorDialog('"
                        + Messages.get().getBundle(widgetDialog.getLocale()).key(
                            Messages.ERR_EDITOR_MESSAGE_NOSELECTION_0)
                        + "');",
                    null,
                    "anchor",
                    Messages.GUI_BUTTON_ANCHOR_0,
                    widgetDialog.getButtonStyle()));
            buttonsActive = true;
        }

        if (!buttonsActive) {
            // no active buttons to show, return empty String
            return "";
        }

        // generate button row end HTML
        result.append(buildOpenCmsButtonRow(CmsWorkplace.HTML_END, widgetDialog));

        // show the active buttons
        return result.toString();

    }

    /**
     * Returns the start or end HTML for the OpenCms specific button row.<p>
     *
     * Use this method to generate the start and end html for the button row.<p>
     *
     * Overwrite the method if the integrated editor needs a specific layout for the button row start or end html.<p>
     *
     * @param segment the HTML segment (START / END)
     * @param widgetDialog the dialog where the widget is used on
     * @return the html String for the OpenCms specific button row
     */
    protected String buildOpenCmsButtonRow(int segment, I_CmsWidgetDialog widgetDialog) {

        StringBuffer result = new StringBuffer(256);

        if (segment == CmsWorkplace.HTML_START) {
            // generate line and start row HTML
            result.append(widgetDialog.buttonBarHorizontalLine());
            result.append(widgetDialog.buttonBar(CmsWorkplace.HTML_START));
            result.append(widgetDialog.buttonBarStartTab(0, 0));
        } else {
            // close button row and generate end line
            result.append(widgetDialog.buttonBar(CmsWorkplace.HTML_END));
            result.append(widgetDialog.buttonBarHorizontalLine());
        }

        return result.toString();
    }
}