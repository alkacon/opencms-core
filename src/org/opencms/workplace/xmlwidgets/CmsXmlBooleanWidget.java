/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/xmlwidgets/Attic/CmsXmlBooleanWidget.java,v $
 * Date   : $Date: 2004/10/15 15:19:39 $
 * Version: $Revision: 1.1 $
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
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.editors.CmsXmlContentEditor;
import org.opencms.xml.A_CmsXmlDocument;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.types.CmsXmlBooleanValue;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.util.Map;

/**
 * Provides an editor widget for {@link org.opencms.xml.types.CmsXmlBooleanValue}.<p>
 *
 * @author Andreas Zahner (a.zahner@alkacon.com)
 * 
 * @version $Revision: 1.1 $
 * @since 5.5.2
 */
public class CmsXmlBooleanWidget extends A_CmsXmlWidget {

    /**
     * Creates a new editor widget.<p>
     */
    public CmsXmlBooleanWidget() {

        // empty constructor is required for class registration
    }

    /**
     * @see org.opencms.workplace.xmlwidgets.I_CmsXmlWidget#getEditorWidget(org.opencms.file.CmsObject, org.opencms.xml.A_CmsXmlDocument, org.opencms.workplace.editors.CmsXmlContentEditor, org.opencms.xml.types.I_CmsXmlContentValue)
     */
    public String getEditorWidget(
        CmsObject cms,
        A_CmsXmlDocument document,
        CmsXmlContentEditor editor,
        I_CmsXmlContentValue value) {

        CmsXmlBooleanValue castValue = (CmsXmlBooleanValue)value;

        StringBuffer result = new StringBuffer(128);
        result.append("<tr><td class=\"xmlLabel\">");
        result.append(value.getNodeName());
        result.append(": </td><td colspan=\"3\" class=\"xmlTd\">");
        boolean booleanValue = castValue.getBooleanValue();
        String id = getParameterName(value);
        
        result.append("<input type=\"checkbox\" name=\"");
        result.append(id);
        result.append("\" value=\"true\"");
        if (booleanValue) {
            result.append(" checked=\"checked\"");    
        }
       
        result.append("></td></tr>\n");
        return result.toString();
    }

    /**
     * @see org.opencms.workplace.xmlwidgets.I_CmsXmlWidget#setEditorValue(org.opencms.file.CmsObject, org.opencms.xml.A_CmsXmlDocument, java.util.Map, org.opencms.workplace.editors.CmsXmlContentEditor, org.opencms.xml.types.I_CmsXmlContentValue)
     */
    public void setEditorValue(
        CmsObject cms,
        A_CmsXmlDocument document,
        Map formParameters,
        CmsXmlContentEditor editor,
        I_CmsXmlContentValue value) throws CmsXmlException {

        String[] values = (String[])formParameters.get(getParameterName(value));
        if ((values != null) && (values.length > 0)) {
            CmsXmlBooleanValue castValue = (CmsXmlBooleanValue)value;
            boolean castBooleanValue = castValue.getBooleanValue();
            String booleanValue = values[0].trim();
            if (CmsStringUtil.isNotEmpty(booleanValue)) {
                castBooleanValue = Boolean.valueOf(booleanValue).booleanValue();
            }
            value.setStringValue(cms, document, String.valueOf(castBooleanValue));
        } else {
            value.setStringValue(cms, document, "false");
        }
    }
}