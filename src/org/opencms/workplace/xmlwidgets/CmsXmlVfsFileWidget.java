/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/xmlwidgets/Attic/CmsXmlVfsFileWidget.java,v $
 * Date   : $Date: 2004/12/02 09:07:58 $
 * Version: $Revision: 1.7 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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
import org.opencms.main.OpenCms;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.I_CmsWpConstants;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.I_CmsXmlDocument;
import org.opencms.xml.types.I_CmsXmlContentValue;

/**
 * Provides an editor widget for {@link org.opencms.xml.types.CmsXmlVfsFileValue}.<p>
 *
 * @author Andreas Zahner (a.zahner@alkacon.com)
 * 
 * @version $Revision: 1.7 $
 * @since 5.5.2
 */
public class CmsXmlVfsFileWidget extends A_CmsXmlWidget {

    /**
     * Creates a new editor widget.<p>
     */
    public CmsXmlVfsFileWidget() {

        // empty constructor is required for class registration
    }
    
    /**
     * @see org.opencms.workplace.xmlwidgets.I_CmsXmlWidget#getDialogIncludes(org.opencms.file.CmsObject, org.opencms.workplace.xmlwidgets.I_CmsWidgetDialog, org.opencms.xml.CmsXmlContentDefinition)
     */
    public String getDialogIncludes(
        CmsObject cms,
        I_CmsWidgetDialog widgetDialog,
        CmsXmlContentDefinition contentDefinition) {
        
        StringBuffer result = new StringBuffer(16);
        result.append(getJSIncludeFile(CmsWorkplace.getSkinUri() + "commons/tree.js"));
        result.append("\n");
        result.append(getJSIncludeFile(CmsWorkplace.getSkinUri() + "components/widgets/fileselector.js"));
        return result.toString();
    }
    
    /**
     * @see org.opencms.workplace.xmlwidgets.I_CmsXmlWidget#getDialogInitCall(org.opencms.file.CmsObject, org.opencms.workplace.xmlwidgets.I_CmsWidgetDialog)
     */
    public String getDialogInitCall(CmsObject cms, I_CmsWidgetDialog widgetDialog) {
    
        return "\tinitVfsFileSelector();\n";
    }
    
    /**
     * @see org.opencms.workplace.xmlwidgets.I_CmsXmlWidget#getDialogInitMethod(org.opencms.file.CmsObject, org.opencms.workplace.xmlwidgets.I_CmsWidgetDialog, I_CmsXmlDocument)
     */
    public String getDialogInitMethod(
        CmsObject cms,
        I_CmsWidgetDialog widgetDialog,
        I_CmsXmlDocument document) {
        
        StringBuffer result = new StringBuffer(16);
        result.append("function initVfsFileSelector() {\n");
        //initialize tree javascript, does parts of CmsTree.initTree(CmsObject, encoding, skinuri);
        result.append("\tinitResources(\"");
        result.append(widgetDialog.getEncoding());
        result.append("\", \"");
        result.append(I_CmsWpConstants.C_VFS_PATH_WORKPLACE);
        result.append("\", \"");
        result.append(CmsWorkplace.getSkinUri());
        result.append("\", \"");
        result.append(OpenCms.getSystemInfo().getOpenCmsContext());
        result.append("\");\n");
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
        result.append("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tr><td>"); 
        result.append("<input class=\"xmlInputMedium\" value=\"");
        result.append(value.getStringValue(cms));
        result.append("\" name=\"");
        result.append(id);
        result.append("\" id=\"");
        result.append(id);
        result.append("\"></td>");
        result.append(widgetDialog.buttonBarSpacer(1));
        result.append(widgetDialog.button("javascript:openTreeWin('EDITOR',  '" + id + "', document);", null, "folder", "button.search", widgetDialog.getSettings().getUserSettings().getEditorButtonStyle()));
        result.append("</tr></table>");
        
        result.append("</td>");
        
        return result.toString();
    }
}