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
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsMessages;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.xml.content.I_CmsXmlContentHandler.DisplayType;
import org.opencms.xml.types.A_CmsXmlContentValue;
import org.opencms.xml.types.CmsXmlColorValue;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Provides a HTML "color picker" widget, for use on a widget dialog.<p>
 * 
 * @since 6.0.0 
 */
public class CmsColorpickerWidget extends A_CmsWidget implements I_CmsADEWidget {

    /**
     * Creates a new color picker widget.<p>
     */
    public CmsColorpickerWidget() {

        // empty constructor is required for class registration
        this("");
    }

    /**
     * Creates a new color picker widget with the given configuration.<p>
     * 
     * @param configuration the configuration to use
     */
    public CmsColorpickerWidget(String configuration) {

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

        return getConfiguration();
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
     * @see org.opencms.widgets.I_CmsWidget#getDialogIncludes(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog)
     */
    @Override
    public String getDialogIncludes(CmsObject cms, I_CmsWidgetDialog widgetDialog) {

        return getJSIncludeFile(CmsWorkplace.getSkinUri() + "components/widgets/colorpicker.js");
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#getDialogInitCall(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog)
     */
    @Override
    public String getDialogInitCall(CmsObject cms, I_CmsWidgetDialog widgetDialog) {

        return "\tinitColorPicker();\n";
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#getDialogInitMethod(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog)
     */
    @Override
    public String getDialogInitMethod(CmsObject cms, I_CmsWidgetDialog widgetDialog) {

        StringBuffer result = new StringBuffer(128);
        result.append("function initColorPicker() {\n");
        result.append("\tcolorPicker.title = \"");
        result.append(Messages.get().getBundle(widgetDialog.getLocale()).key(Messages.GUI_DIALOG_COLOR_TITLE_0));
        result.append("\";\n");
        result.append("\tcolorPicker.url=\"");
        result.append(CmsWorkplace.getSkinUri());
        result.append("components/js_colorpicker/index.html\";\n");
        result.append("}\n");
        return result.toString();
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#getDialogWidget(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog, org.opencms.widgets.I_CmsWidgetParameter)
     */
    public String getDialogWidget(CmsObject cms, I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter param) {

        StringBuffer result = new StringBuffer(16);
        result.append("<td class=\"xmlTd\">");
        String colorValue = param.getStringValue(cms);
        String id = param.getId();

        result.append("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tr><td>");
        result.append("<input type=\"text\"");
        result.append(" class=\"xmlInputSmall\" name=\"");
        result.append(id);
        result.append("\" value=\"");
        result.append(colorValue);
        // 19 chars for "InactiveCaptionText"
        result.append("\" maxlength=\"19\" onkeyup=\"previewColor('");
        result.append(id);
        result.append("');\"");
        result.append(" style=\"background-color: ");
        result.append(checkColor(colorValue));
        result.append("; color: ");
        result.append(getInputFontColor(colorValue));
        result.append(";\"></td>");

        result.append(widgetDialog.dialogHorizontalSpacer(10));
        result.append("<td><table class=\"editorbuttonbackground\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tr>");
        result.append(widgetDialog.button(
            "javascript:showColorPicker('" + id + "');",
            null,
            "color_fill",
            Messages.GUI_BUTTON_COLOR_0,
            widgetDialog.getButtonStyle()));
        result.append("</tr></table>");

        result.append("</td></tr></table>");

        result.append("</td>");
        return result.toString();
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

        return CmsColorpickerWidget.class.getName();
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

        return new CmsColorpickerWidget(getConfiguration());
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
            CmsXmlColorValue castValue = (CmsXmlColorValue)param;
            String castColorValue = castValue.getStringValue(cms);
            String colorValue = values[0].trim();
            if (CmsStringUtil.isNotEmpty(colorValue)) {
                castColorValue = colorValue;
            }
            param.setStringValue(cms, String.valueOf(castColorValue));
        }
    }

    /**
     * Check the stored color value to prevent display issues in the generated HTML ouput.<p>
     * 
     * @param color the color value to check
     * @return the checked color value
     */
    private String checkColor(String color) {

        if (color != null) {
            if (color.indexOf("#") == -1) {
                // add the "#" to the color string
                color = "#" + color;
            }
            int colLength = color.length();
            if ((colLength == 4) || (colLength == 7)) {
                return color;
            }
        }
        return "#FFFFFF";
    }

    /**
     * Returns the font color of the input field depending on the selected color value.<p>
     * 
     * @param backgroundColor the selected color value which is displayed as the input field background
     * @return the font color to use
     */
    private String getInputFontColor(String backgroundColor) {

        if ((backgroundColor != null) && (backgroundColor.indexOf("#") == 0)) {
            // remove the "#" from the color string
            backgroundColor = backgroundColor.substring(1);
            int colorValue = 50001;
            try {
                // calculate int value of color
                colorValue = Integer.parseInt(backgroundColor, 16);
            } catch (NumberFormatException nf) {
                // this should never happen    
            }
            if (colorValue < 50000) {
                // for dark colors set font color to white
                return "#FFFFFF";
            } else {
                // for other colors use black
                return "#000000";
            }
        }
        return "#000000";
    }
}