/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/widgets/A_CmsGalleryWidget.java,v $
 * Date   : $Date: 2005/06/22 10:38:11 $
 * Version: $Revision: 1.4 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.widgets;

import org.opencms.file.CmsObject;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.galleries.A_CmsGallery;

/**
 * Base class for all gallery widget implementations.<p>
 *
 * @author Alexander Kandzior 
 * @author Andreas Zahner 
 * 
 * @version $Revision: 1.4 $
 * @since 5.5.3
 */
public abstract class A_CmsGalleryWidget extends A_CmsWidget {

    /**
     * Creates a new gallery widget.<p>
     */
    protected A_CmsGalleryWidget() {

        // empty constructor is required for class registration
        this("");
    }

    /**
     * Creates a new gallery widget with the given configuration.<p>
     * 
     * @param configuration the configuration to use
     */
    protected A_CmsGalleryWidget(String configuration) {

        super(configuration);
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#getDialogIncludes(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog)
     */
    public String getDialogIncludes(CmsObject cms, I_CmsWidgetDialog widgetDialog) {

        return getJSIncludeFile(CmsWorkplace.getSkinUri() + "components/widgets/" + getNameLower() + "gallery.js");
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#getDialogInitCall(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog)
     */
    public String getDialogInitCall(CmsObject cms, I_CmsWidgetDialog widgetDialog) {

        return "\tinit" + getNameUpper() + "Gallery();\n";
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#getDialogInitMethod(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog)
     */
    public String getDialogInitMethod(CmsObject cms, I_CmsWidgetDialog widgetDialog) {

        StringBuffer result = new StringBuffer(16);
        result.append("function init");
        result.append(getNameUpper());
        result.append("Gallery() {\n");
        result.append("\t");
        result.append(getNameLower());
        result.append("GalleryPath = \"");
        result.append(A_CmsGallery.C_PATH_GALLERIES);
        result.append(A_CmsGallery.C_OPEN_URI_SUFFIX);
        result.append("?");
        result.append(A_CmsGallery.PARAM_GALLERY_TYPENAME);
        result.append("=");
        result.append(getNameLower());
        result.append("gallery");
        result.append("\";\n");
        result.append("}\n");
        return result.toString();
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#getDialogWidget(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog, org.opencms.widgets.I_CmsWidgetParameter)
     */
    public String getDialogWidget(CmsObject cms, I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter param) {

        String id = param.getId();
        StringBuffer result = new StringBuffer(128);
        result.append("<td class=\"xmlTd\">");
        result.append("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tr><td>");
        result.append("<input class=\"xmlInputMedium");
        if (param.hasError()) {
            result.append(" xmlInputError");
        }
        result.append("\" value=\"");
        String value = param.getStringValue(cms);
        result.append(value);
        result.append("\" name=\"");
        result.append(id);
        result.append("\" id=\"");
        result.append(id);
        result.append("\" onkeyup=\"checkPreview('");
        result.append(id);
        result.append("');\"></td>");
        result.append(widgetDialog.dialogHorizontalSpacer(10));
        result.append("<td><table class=\"editorbuttonbackground\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tr>");

        result.append(widgetDialog.button(
            "javascript:open" + getNameUpper() + "Gallery('" + A_CmsGallery.MODE_WIDGET + "',  '" + id + "');",
            null,
            getNameLower() + "gallery",
            "button." + getNameLower() + "list",
            widgetDialog.getButtonStyle()));
        // create preview button
        String previewClass = "hide";
        if (CmsStringUtil.isNotEmpty(value) && value.startsWith("/")) {
            // show button if field value is not empty and starts with a "/"
            previewClass = "show";
        }
        result.append("<td class=\"");
        result.append(previewClass);
        result.append("\" id=\"preview");
        result.append(id);
        result.append("\">");
        result.append("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tr>");
        result.append(widgetDialog.button(
            "javascript:preview" + getNameUpper() + "('" + id + "');",
            null,
            "preview.png",
            "button.preview",
            widgetDialog.getButtonStyle()));
        result.append("</tr></table>");

        result.append("</td></tr></table>");

        result.append("</td>");
        result.append("</tr></table>");

        result.append("</td>");

        return result.toString();
    }

    /**
     * Returns the lower case name of the gallery, for example <code>"html"</code>.<p>
     * 
     * @return the lower case name of the gallery
     */
    public abstract String getNameLower();

    /**
     * Returns the upper case name of the gallery, for example <code>"Html"</code>.<p>
     * 
     * @return the upper case name of the gallery
     */
    public abstract String getNameUpper();

    /**
     * Returns <code>true</code> if the preview button should be shown.<p>
     * 
     * @param value the current widget value
     * @return <code>true</code> if the preview button should be shown
     */
    public abstract boolean showPreview(String value);
}