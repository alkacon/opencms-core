/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/xmlwidgets/Attic/CmsXmlHtmlGalleryWidget.java,v $
 * Date   : $Date: 2005/02/17 12:44:32 $
 * Version: $Revision: 1.2 $
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

package org.opencms.workplace.xmlwidgets;

import org.opencms.file.CmsObject;
import org.opencms.i18n.CmsEncoder;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.galleries.A_CmsGallery;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.I_CmsXmlDocument;
import org.opencms.xml.types.I_CmsXmlContentValue;

/**
 * Provides an editor widget for {@link org.opencms.xml.types.CmsXmlStringValue} and accesses the available html galleries.<p>
 *
 * @author Andreas Zahner (a.zahner@alkacon.com)
 * 
 * @version $Revision: 1.2 $
 * @since 5.5.3
 */
public class CmsXmlHtmlGalleryWidget extends A_CmsXmlWidget {

    /**
     * Creates a new editor widget.<p>
     */
    public CmsXmlHtmlGalleryWidget() {

        // empty constructor is required for class registration
    }
    
    /**
     * @see org.opencms.workplace.xmlwidgets.I_CmsXmlWidget#getDialogIncludes(org.opencms.file.CmsObject, org.opencms.workplace.xmlwidgets.I_CmsWidgetDialog, org.opencms.xml.CmsXmlContentDefinition)
     */
    public String getDialogIncludes(
        CmsObject cms,
        I_CmsWidgetDialog widgetDialog,
        CmsXmlContentDefinition contentDefinition) {
        
        return getJSIncludeFile(CmsWorkplace.getSkinUri() + "components/widgets/htmlgallery.js");
    }
    
    /**
     * @see org.opencms.workplace.xmlwidgets.I_CmsXmlWidget#getDialogInitCall(org.opencms.file.CmsObject, org.opencms.workplace.xmlwidgets.I_CmsWidgetDialog)
     */
    public String getDialogInitCall(CmsObject cms, I_CmsWidgetDialog widgetDialog) {
    
        return "\tinitHtmlGallery();\n";
    }
    
    /**
     * @see org.opencms.workplace.xmlwidgets.I_CmsXmlWidget#getDialogInitMethod(org.opencms.file.CmsObject, org.opencms.workplace.xmlwidgets.I_CmsWidgetDialog, I_CmsXmlDocument)
     */
    public String getDialogInitMethod(
        CmsObject cms,
        I_CmsWidgetDialog widgetDialog,
        I_CmsXmlDocument document) {
        
        StringBuffer result = new StringBuffer(16);
        result.append("function initHtmlGallery() {\n");
        result.append("\thtmlGalleryPath = \"");
        result.append(A_CmsGallery.C_PATH_GALLERIES + A_CmsGallery.C_OPEN_URI_SUFFIX + "?" + A_CmsGallery.PARAM_GALLERY_TYPENAME + "=htmlgallery");
        result.append("\";\n");
        result.append("}\n");        
        return result.toString();
    }

    /**
     * @see org.opencms.workplace.xmlwidgets.I_CmsXmlWidget#getDialogWidget(org.opencms.file.CmsObject, org.opencms.workplace.xmlwidgets.I_CmsWidgetDialog, org.opencms.xml.types.I_CmsXmlContentValue)
     */
    public String getDialogWidget(
        CmsObject cms,
        I_CmsWidgetDialog widgetDialog,
        I_CmsXmlContentValue value) throws CmsXmlException {

        String id = getParameterName(value);
        StringBuffer result = new StringBuffer(128);       
        result.append("<td class=\"xmlTd\">");
        result.append("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"maxwidth\">"); 
        result.append("<input type=\"hidden\" value=\"");
        String fieldValue = value.getStringValue(cms);
        result.append(CmsEncoder.escapeXml(fieldValue));
        result.append("\" name=\"");
        result.append(id);
        result.append("\" id=\"");
        result.append(id);
        result.append("\">");
        result.append("<tr><td class=\"xmlTd\">");
        result.append("<div class=\"xmlHtmlGallery\" unselectable=\"on\" id=\"");
        result.append("html.");
        result.append(id);
        result.append("\"><div>");
        result.append("</td>");
        result.append(widgetDialog.buttonBarSpacer(1));
        result.append(widgetDialog.button("javascript:openHtmlGallery('" + A_CmsGallery.MODE_WIDGET + "',  '" + id + "');", null, "htmlgallery", "button.htmllist", widgetDialog.getSettings().getUserSettings().getEditorButtonStyle()));
        result.append(widgetDialog.button("javascript:resetHtmlGallery('" + id + "');", null, "erase", "button.erase", widgetDialog.getSettings().getUserSettings().getEditorButtonStyle()));
        result.append("</tr>");
        result.append("<script type=\"text/javascript\">checkHtmlContent('");
        result.append(id);
        result.append("');</script>");
        result.append("</table>");
        
        result.append("</td>");
        
        return result.toString();
    }
}