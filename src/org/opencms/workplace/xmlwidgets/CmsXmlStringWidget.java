/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/xmlwidgets/Attic/CmsXmlStringWidget.java,v $
 * Date   : $Date: 2004/10/18 14:42:03 $
 * Version: $Revision: 1.5 $
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
import org.opencms.workplace.editors.CmsXmlContentEditor;
import org.opencms.xml.A_CmsXmlDocument;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.types.I_CmsXmlContentValue;



/**
 * Provides an editor widget for {@link org.opencms.xml.types.CmsXmlStringValue}.<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.5 $
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
     * @see org.opencms.workplace.xmlwidgets.I_CmsXmlWidget#getEditorWidget(org.opencms.file.CmsObject, org.opencms.xml.A_CmsXmlDocument, org.opencms.workplace.editors.CmsXmlContentEditor, org.opencms.xml.CmsXmlContentDefinition, org.opencms.xml.types.I_CmsXmlContentValue)
     */
    public String getEditorWidget(
        CmsObject cms,
        A_CmsXmlDocument document,
        CmsXmlContentEditor editor,
        CmsXmlContentDefinition contentDefinition,
        I_CmsXmlContentValue value) throws CmsXmlException {
           
        String id = getParameterName(value);
        
        StringBuffer result = new StringBuffer(128);
        result.append("<tr><td class=\"xmlLabel\">");
        result.append(getMessage(editor, contentDefinition, value.getNodeName()));
        result.append(": </td>");
        
        result.append(getHelpBubble(cms, editor, contentDefinition, value.getNodeName()));
        
        result.append("<td class=\"xmlTd\"><input class=\"xmlInput maxwidth\" value=\"");
        result.append(value.getStringValue(cms, document));
        result.append("\" name=\"");
        result.append(id);
        result.append("\" id=\"");
        result.append(id);
        result.append("\">");
        result.append("</td>");

        //result.append(getHelpText(editor, contentDefinition, value.getNodeName()));
        
        result.append("</tr>\n");
        

        return result.toString();
    }
}