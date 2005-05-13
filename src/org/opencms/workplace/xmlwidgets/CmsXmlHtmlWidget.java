/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/xmlwidgets/Attic/CmsXmlHtmlWidget.java,v $
 * Date   : $Date: 2005/05/13 13:35:38 $
 * Version: $Revision: 1.16 $
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

import java.util.Map;

/**
 * Provides an editor widget for {@link org.opencms.xml.types.CmsXmlHtmlValue}.<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.16 $
 * @since 5.5.0
 */
public class CmsXmlHtmlWidget extends A_CmsXmlWidget {

    /**
     * Creates a new editor widget.<p>
     */
    public CmsXmlHtmlWidget() {

        // empty constructor is required for class registration
    }

    /**
     * @see org.opencms.workplace.xmlwidgets.I_CmsXmlWidget#getDialogIncludes(org.opencms.file.CmsObject, org.opencms.workplace.xmlwidgets.I_CmsWidgetDialog)
     */
    public String getDialogIncludes(CmsObject cms, I_CmsWidgetDialog widgetDialog) {

        StringBuffer result = new StringBuffer(16);
        result.append("<script type=\"text/javascript\">\n<!--\n");
        result.append("\t_editor_url = \"");
        result.append(CmsWorkplace.getSkinUri());
        result.append("editors/htmlarea/\";\n");
        result.append("\t_editor_lang = \"");
        result.append(widgetDialog.getLocale());
        result.append("\";\n");
        result.append("//-->\n</script>\n");
        result.append(getJSIncludeFile(CmsWorkplace.getSkinUri() + "editors/htmlarea/htmlarea.js"));
        result.append("\n");
        result.append(getJSIncludeFile(CmsWorkplace.getSkinUri() + "components/widgets/htmlarea.js"));
        return result.toString();
    }

    /**
     * @see org.opencms.workplace.xmlwidgets.I_CmsXmlWidget#getDialogInitCall(org.opencms.file.CmsObject, org.opencms.workplace.xmlwidgets.I_CmsWidgetDialog)
     */
    public String getDialogInitCall(CmsObject cms, I_CmsWidgetDialog widgetDialog) {

        // the timeout setting prevents IE from jumping to the last html area widget
        return "\tinitHtmlArea();\n";
    }

    /**
     * @see org.opencms.workplace.xmlwidgets.I_CmsXmlWidget#getDialogInitMethod(org.opencms.file.CmsObject, org.opencms.workplace.xmlwidgets.I_CmsWidgetDialog)
     */
    public String getDialogInitMethod(CmsObject cms, I_CmsWidgetDialog widgetDialog) {

        StringBuffer result = new StringBuffer(8);
        result.append("function initHtmlArea() {\n");
        result.append("\tinitHtmlAreas();\n");
        result.append("}\n");
        return result.toString();
    }

    /**
     * @see org.opencms.workplace.xmlwidgets.I_CmsXmlWidget#getDialogWidget(org.opencms.file.CmsObject, org.opencms.workplace.xmlwidgets.I_CmsWidgetDialog, org.opencms.workplace.xmlwidgets.I_CmsWidgetParameter)
     */
    public String getDialogWidget(CmsObject cms, I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter param) {

        String id = param.getId();
        StringBuffer result = new StringBuffer(128);

        result.append("<td class=\"xmlTd\">");
        result.append("<textarea class=\"xmlInput maxwidth\" name=\"");
        result.append(id);
        result.append("\" id=\"");
        result.append(id);
        result.append("\" rows=\"15\" wrap=\"virtual\">");
        result.append(param.getStringValue(cms));
        result.append("</textarea>");
        result.append("</td>");

        return result.toString();
    }

    /**
     * @see org.opencms.workplace.xmlwidgets.I_CmsXmlWidget#setEditorValue(org.opencms.file.CmsObject, java.util.Map, org.opencms.workplace.xmlwidgets.I_CmsWidgetDialog, org.opencms.workplace.xmlwidgets.I_CmsWidgetParameter)
     */
    public void setEditorValue(
        CmsObject cms,
        Map formParameters,
        I_CmsWidgetDialog widgetDialog,
        I_CmsWidgetParameter param) {

        String[] values = (String[])formParameters.get(param.getId());
        if ((values != null) && (values.length > 0)) {
            String val = CmsEncoder.decode(values[0], CmsEncoder.C_UTF8_ENCODING);
            param.setStringValue(cms, val);
        }
    }
}