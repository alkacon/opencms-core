/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/xmlwidgets/Attic/CmsXmlStringWidget.java,v $
 * Date   : $Date: 2004/11/28 21:57:59 $
 * Version: $Revision: 1.8 $
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
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.I_CmsXmlDocument;
import org.opencms.xml.types.I_CmsXmlContentValue;
import org.opencms.xml.types.I_CmsXmlSchemaType;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * Provides an editor widget for {@link org.opencms.xml.types.CmsXmlStringValue}.<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.8 $
 * @since 5.5.0
 */
public class CmsXmlStringWidget extends A_CmsXmlWidget {

    /**
     * Creates a new editor widget.<p>
     */
    public CmsXmlStringWidget() {

        // empty constructor is required for class registration
    }

    /**
     * @see org.opencms.workplace.xmlwidgets.I_CmsXmlWidget#getDialogInitCall(org.opencms.file.CmsObject, org.opencms.workplace.xmlwidgets.I_CmsWidgetDialog)
     */
    public String getDialogInitCall(CmsObject cms, I_CmsWidgetDialog widgetDialog) {

        return "\twindow.setTimeout(\"initStringFields()\",50);\n";
    }

    /**
     * @see org.opencms.workplace.xmlwidgets.I_CmsXmlWidget#getDialogInitMethod(org.opencms.file.CmsObject, org.opencms.xml.I_CmsXmlDocument, org.opencms.workplace.xmlwidgets.I_CmsWidgetDialog, org.opencms.xml.CmsXmlContentDefinition, org.opencms.xml.types.I_CmsXmlContentValue)
     */
    public String getDialogInitMethod(
        CmsObject cms,
        I_CmsXmlDocument document,
        I_CmsWidgetDialog widgetDialog,
        CmsXmlContentDefinition contentDefinition,
        I_CmsXmlContentValue value) throws CmsXmlException {

        StringBuffer result = new StringBuffer(8);
        result.append("function initStringFields() {\n");

        Locale locale = widgetDialog.getElementLocale();

        List typeSequence = contentDefinition.getTypeSequence();
        Iterator i = typeSequence.iterator();
        while (i.hasNext()) {

            I_CmsXmlSchemaType type = (I_CmsXmlSchemaType)i.next();
            String name = type.getElementName();
            int count = document.getIndexCount(name, locale);

            for (int j = 0; j < count; j++) {

                I_CmsXmlContentValue value2 = document.getValue(name, locale, j);
                I_CmsXmlWidget widget = contentDefinition.getContentHandler().getEditorWidget(
                    value2,
                    document,
                    contentDefinition);

                if (this.equals(widget)) {
                    // add init methods for all elements that use the String widget

                    try {
                        I_CmsXmlContentValue contentValue = document.getValue(name, locale, j);

                        String id = getParameterName(contentValue);
                        String currValue = contentValue.getStringValue(cms, document);
                        currValue = CmsStringUtil.escapeJavaScript(currValue.trim());
                        result.append("\tdocument.forms[\"EDITOR\"].elements[\"");
                        result.append(id);
                        result.append("\"].value = \"");
                        result.append(currValue);
                        result.append("\";\n");
                    } catch (CmsXmlException e) {
                        if (OpenCms.getLog(this).isErrorEnabled()) {
                            OpenCms.getLog(this).error("Error accessing XML content node '" + name + "'", e);
                        }
                    }
                }
            }

        }

        result.append("");
        result.append("}\n");
        return result.toString();
    }

    /**
     * @see org.opencms.workplace.xmlwidgets.I_CmsXmlWidget#getDialogWidget(org.opencms.file.CmsObject, org.opencms.xml.I_CmsXmlDocument, org.opencms.workplace.xmlwidgets.I_CmsWidgetDialog, org.opencms.xml.CmsXmlContentDefinition, org.opencms.xml.types.I_CmsXmlContentValue)
     */
    public String getDialogWidget(
        CmsObject cms,
        I_CmsXmlDocument document,
        I_CmsWidgetDialog widgetDialog,
        CmsXmlContentDefinition contentDefinition,
        I_CmsXmlContentValue value) {

        String id = getParameterName(value);

        StringBuffer result = new StringBuffer(128);
        result.append("<tr><td class=\"xmlLabel\">");
        result.append(getMessage(widgetDialog, contentDefinition, value.getElementName()));
        result.append(": </td>");

        result.append(getHelpBubble(cms, widgetDialog, contentDefinition, value.getElementName()));

        result.append("<td class=\"xmlTd\"><input class=\"xmlInput maxwidth\" name=\"");
        result.append(id);
        result.append("\" id=\"");
        result.append(id);
        result.append("\">");
        result.append("</td>");

        result.append("</tr>\n");

        return result.toString();
    }
}