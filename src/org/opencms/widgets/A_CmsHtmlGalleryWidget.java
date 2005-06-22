/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/widgets/A_CmsHtmlGalleryWidget.java,v $
 * Date   : $Date: 2005/06/22 10:38:11 $
 * Version: $Revision: 1.5 $
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
import org.opencms.i18n.CmsEncoder;
import org.opencms.workplace.galleries.A_CmsGallery;

/**
 * Base class for non-editable "HTML display only" widget implementations.<p>
 *
 * @author Alexander Kandzior 
 * @author Andreas Zahner 
 * 
 * @version $Revision: 1.5 $
 * @since 5.7.3
 */
public abstract class A_CmsHtmlGalleryWidget extends A_CmsGalleryWidget {

    /**
     * Creates a new html gallery widget.<p>
     */
    protected A_CmsHtmlGalleryWidget() {

        // empty constructor is required for class registration
        this("");
    }

    /**
     * Creates a html gallery widget with the specified combo options.<p>
     * 
     * @param configuration the configuration (possible options) for the combo box
     */
    public A_CmsHtmlGalleryWidget(String configuration) {

        super(configuration);
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#getDialogWidget(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog, org.opencms.widgets.I_CmsWidgetParameter)
     */
    public String getDialogWidget(CmsObject cms, I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter param) {

        String id = param.getId();
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
        result.append("<td><table class=\"editorbuttonbackground\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tr>");
        result.append(widgetDialog.button(
            "javascript:open" + getNameUpper() + "Gallery('" + A_CmsGallery.MODE_WIDGET + "',  '" + id + "');",
            null,
            getNameLower() + "gallery",
            "button." + getNameLower() + "list",
            widgetDialog.getButtonStyle()));
        result.append(widgetDialog.button(
            "javascript:reset" + getNameUpper() + "Gallery('" + id + "');",
            null,
            "erase",
            "button.erase",
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

        return result.toString();
    }

    /**
     * @see org.opencms.widgets.A_CmsGalleryWidget#showPreview(java.lang.String)
     */
    public boolean showPreview(String value) {

        // not required for HTML display galleries
        return false;
    }
}