/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/xmlwidgets/Attic/I_CmsXmlWidget.java,v $
 * Date   : $Date: 2004/12/01 12:01:20 $
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
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.I_CmsXmlDocument;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.util.Map;

/**
 * Describes an editor widget for use in the OpenCms workplace.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.7 $
 * @since 5.5.0
 */
public interface I_CmsXmlWidget {
    
    /**
     * Generates the html to include at the end of the dialog form, if needed.<p>
     * 
     * @param cms an initialized instance of a CmsObject
     * @param widgetDialog the dialog where the widget is used on
     * @param value the XML content value to generate the widget for
     * 
     * @return the html to include at the end of the editor form
     * 
     * @throws CmsXmlException if something goes wrong
     */
    String getDialogHtmlEnd(
        CmsObject cms,
        I_CmsWidgetDialog widgetDialog,
        I_CmsXmlContentValue value) throws CmsXmlException;
    
    /**
     * Generates the necessary javascript inclusion code for an individual widget.<p>
     * 
     * @param cms an initialized instance of a CmsObject
     * @param widgetDialog the dialog where the widget is used on
     * @param contentDefinition the content definition object
     * 
     * @return the javascript inclusion code
     * 
     * @throws CmsXmlException if something goes wrong
     */
    String getDialogIncludes(
        CmsObject cms,
        I_CmsWidgetDialog widgetDialog,
        CmsXmlContentDefinition contentDefinition) throws CmsXmlException;
    
    /**
     * Generates the initialisation call method for an individual widget.<p>
     * 
     * @param cms an initialized instance of a CmsObject
     * @param widgetDialog the dialog where the widget is used on
     * 
     * @return the initialisation call method
     * 
     * @throws CmsXmlException if something goes wrong
     */
    String getDialogInitCall(
        CmsObject cms,
        I_CmsWidgetDialog widgetDialog) throws CmsXmlException;
    
    /**
     * Generates the initialization code for this XML content widget.<p>
     * 
     * @param cms an initialized instance of a CmsObject
     * @param widgetDialog the dialog where the widget is used on
     * @param document the XML content document instance to generate to widget for
     * 
     * @return the initialization code
     * 
     * @throws CmsXmlException if something goes wrong
     */
    String getDialogInitMethod(
        CmsObject cms,
        I_CmsWidgetDialog widgetDialog,
        I_CmsXmlDocument document) throws CmsXmlException;
    
    /**
     * Generates the editor widget for the provided XML content value.<p>
     * 
     * @param cms an initialized instance of a CmsObject
     * @param widgetDialog the dialog where the widget is used on
     * @param value the XML content value to generate the widget for
     * 
     * @return the HTML form for this content node
     * 
     * @throws CmsXmlException if something goes wrong
     */
    String getDialogWidget(
        CmsObject cms,
        I_CmsWidgetDialog widgetDialog,
        I_CmsXmlContentValue value) throws CmsXmlException;

    /**
     * Returns the name of the form parameter for the provided XML content value as a String.<p>
     * 
     * @param value the XML content value to generate the parameter name for
     * 
     * @return the name of the form parameter for the provided XML content value as a String
     */
    String getParameterName(I_CmsXmlContentValue value);

    /**
     * Sets the value of in the given XML content by reading the "right" 
     * value from the offered map of parameters.<p>
     * 
     * @param cms an initialized instance of a CmsObject
     * @param formParameters the map of parameters to get the value from
     * @param widgetDialog the dialog where the widget is used on
     * @param value the XML content value to set the editor value in
     * 
     * @throws CmsXmlException if something goes wrong
     */
    void setEditorValue(
        CmsObject cms,
        Map formParameters,
        I_CmsWidgetDialog widgetDialog,
        I_CmsXmlContentValue value) throws CmsXmlException;
}