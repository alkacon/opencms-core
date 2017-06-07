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
import org.opencms.workplace.galleries.A_CmsAjaxGallery;

/**
 * Base class for non-editable "HTML display only" widget implementations.<p>
 *
 * @since 6.0.0
 */
public abstract class A_CmsHtmlGalleryWidget extends A_CmsGalleryWidget {

    /**
     * Creates a html gallery widget with the specified combo options.<p>
     *
     * @param configuration the configuration (possible options) for the combo box
     */
    public A_CmsHtmlGalleryWidget(String configuration) {

        super(configuration);
    }

    /**
     * Creates a new html gallery widget.<p>
     */
    protected A_CmsHtmlGalleryWidget() {

        // empty constructor is required for class registration
        this("");
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#getDialogWidget(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog, org.opencms.widgets.I_CmsWidgetParameter)
     */
    @Override
    public String getDialogWidget(CmsObject cms, I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter param) {

        String id = param.getId();
        long idHash = id.hashCode();
        if (idHash < 0) {
            // negative hash codes will not work as JS variable names, so convert them
            idHash = -idHash;
            // add 2^32 to the value to ensure that it is unique
            idHash += 4294967296L;
        }
        StringBuffer result = new StringBuffer(128);
        result.append("<td class=\"xmlTd\">");
        result.append("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"maxwidth\">");
        result.append("<input type=\"hidden\" value=\"");
        String fieldValue = param.getStringValue(cms);
        result.append(CmsEncoder.escapeXml(fieldValue));
        result.append("\" name=\"");
        result.append(id);
        result.append("\" id=\"");
        result.append(id);
        result.append("\">");
        result.append("<tr><td style=\"width: 100%;\">");
        // note that using "xmlHtmlGallery" here is ok since this is the formatting display area which is identical
        // for all widgets based on this class
        result.append("<div class=\"xmlHtmlGallery\" unselectable=\"on\" id=\"");
        result.append(getNameLower());
        result.append(".");
        result.append(id);
        result.append("\"><div>");
        result.append("</td>");
        result.append(widgetDialog.dialogHorizontalSpacer(10));
        result.append(
            "<td><table class=\"editorbuttonbackground\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tr>");
        result.append(
            widgetDialog.button(
                "javascript:open"
                    + getNameUpper()
                    + "Gallery('"
                    + A_CmsAjaxGallery.MODE_WIDGET
                    + "',  '"
                    + id
                    + "' , '"
                    + idHash
                    + "');",
                null,
                getNameLower() + "gallery",
                Messages.getButtonName(getNameLower()),
                widgetDialog.getButtonStyle()));
        // reset button
        result.append(
            widgetDialog.button(
                "javascript:reset" + getNameUpper() + "Gallery('" + id + "');",
                null,
                "erase",
                Messages.GUI_BUTTON_ERASE_0,
                widgetDialog.getButtonStyle()));
        result.append("</tr></table>");
        result.append("</td></tr>");
        result.append("<script type=\"text/javascript\">check");
        result.append(getNameUpper());
        result.append("Content('");
        result.append(id);
        result.append("');</script>");
        result.append("</table>");

        result.append("</td>");

        // reads the configuration String for this widget
        CmsGalleryWidgetConfiguration configuration = new CmsGalleryWidgetConfiguration(
            cms,
            widgetDialog.getMessages(),
            param,
            getConfiguration());

        result.append("\n<script type=\"text/javascript\">");
        result.append("\nvar startupFolder").append(idHash).append(" = \"").append(configuration.getStartup()).append(
            "\";");
        result.append("\nvar startupType").append(idHash).append(" = \"").append(configuration.getType()).append("\";");
        result.append("\n</script>");

        return result.toString();
    }

    /**
     * @see org.opencms.widgets.I_CmsADEWidget#getWidgetName()
     */
    @Override
    public String getWidgetName() {

        return A_CmsHtmlGalleryWidget.class.getName();
    }

    /**
     * @see org.opencms.widgets.A_CmsGalleryWidget#showPreview(java.lang.String)
     */
    @Override
    public boolean showPreview(String value) {

        // not required for HTML display galleries
        return false;
    }

}