/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/xmlwidgets/Attic/CmsXmlStringWidget.java,v $
 * Date   : $Date: 2004/12/07 16:53:59 $
 * Version: $Revision: 1.15 $
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
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.I_CmsXmlDocument;
import org.opencms.xml.types.CmsXmlNestedContentDefinition;
import org.opencms.xml.types.I_CmsXmlContentValue;
import org.opencms.xml.types.I_CmsXmlSchemaType;

import java.util.Iterator;
import java.util.Locale;

/**
 * Provides an editor widget for {@link org.opencms.xml.types.CmsXmlStringValue}.<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.15 $
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
        
        StringBuffer result = new StringBuffer(4);
        result.append("\tstringsPresent = true;\n");
        result.append("\twindow.setTimeout(\"initStringFields()\",50);\n");
        return result.toString();
    }

    /**
     * @see org.opencms.workplace.xmlwidgets.I_CmsXmlWidget#getDialogInitMethod(org.opencms.file.CmsObject, org.opencms.workplace.xmlwidgets.I_CmsWidgetDialog, I_CmsXmlDocument)
     */
    public String getDialogInitMethod(
        CmsObject cms,
        I_CmsWidgetDialog widgetDialog,
        I_CmsXmlDocument document) throws CmsXmlException {

        StringBuffer result = new StringBuffer(8);
        result.append("function initStringFields() {\n");

        Locale locale = widgetDialog.getElementLocale();
        // call init method with outer content definition
        result.append(getDialogInitMethod(document.getContentDefinition(), cms, document, locale, ""));

        result.append("\tstringsInserted = true;\n");
        result.append("}\n");
        return result.toString();
    }

    /**
     * @see org.opencms.workplace.xmlwidgets.I_CmsXmlWidget#getDialogWidget(org.opencms.file.CmsObject, org.opencms.workplace.xmlwidgets.I_CmsWidgetDialog, org.opencms.xml.types.I_CmsXmlContentValue)
     */
    public String getDialogWidget(
        CmsObject cms,
        I_CmsWidgetDialog widgetDialog,
        I_CmsXmlContentValue value) {

        String id = getParameterName(value);
        
        StringBuffer result = new StringBuffer(16);
                
        result.append("<td class=\"xmlTd\"><input class=\"xmlInput maxwidth\" name=\"");
        result.append(id);
        result.append("\" id=\"");
        result.append(id);
        result.append("\">");
        result.append("</td>");

        return result.toString();
    }
    
    /**
     * Recursive method that generates the javascript String initialization commands.<p>
     * 
     * @param contentDefinition the xml content definition
     * @param cms the CmsObject to access the API
     * @param document the document to check
     * @param locale the current Locale
     * @param pathPrefix the xpath prefix to use
     * @return the javascript String initizialization commands
     * @throws CmsXmlException if something goes wrong
     */
    private String getDialogInitMethod(CmsXmlContentDefinition contentDefinition, CmsObject cms, I_CmsXmlDocument document, Locale locale, String pathPrefix) throws CmsXmlException {
        
        StringBuffer result = new StringBuffer(8);
              
        Iterator i = contentDefinition.getTypeSequence().iterator();
        while (i.hasNext()) {

            I_CmsXmlSchemaType type = (I_CmsXmlSchemaType)i.next();
            String name = pathPrefix + type.getElementName();
            int count = document.getIndexCount(name, locale);

            for (int j = 0; j < count; j++) {
                
                I_CmsXmlContentValue value = document.getValue(name, locale, j);
                
                if (! type.isSimpleType()) {
                    // this is a nested type, recurse into it
                    CmsXmlNestedContentDefinition nestedSchema = (CmsXmlNestedContentDefinition)type;
                    // create the xpath
                    StringBuffer xPath = new StringBuffer(pathPrefix.length() + 16);
                    xPath.append(pathPrefix);
                    xPath.append(CmsXmlUtils.createXpathElement(type.getElementName(), value.getIndex() + 1));
                    xPath.append("/");
                    result.append(getDialogInitMethod(nestedSchema.getNestedContentDefinition(), cms, document, locale, xPath.toString()));
                } else {
                    // simple type, get the widget of it
                    I_CmsXmlWidget widget = contentDefinition.getContentHandler().getWidget(value);
    
                    if (this.equals(widget)) {
                        // add init methods for all elements that use the String widget   
                        try {    
                            String id = getParameterName(value);
                            String currValue = value.getStringValue(cms);
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
        }
          
        return result.toString();
    }
}