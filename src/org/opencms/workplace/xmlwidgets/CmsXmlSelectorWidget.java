/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/xmlwidgets/Attic/CmsXmlSelectorWidget.java,v $
 * Date   : $Date: 2005/02/17 12:44:32 $
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

package org.opencms.workplace.xmlwidgets;

import org.opencms.file.CmsObject;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.util.StringTokenizer;

/**
 * Provides an editor widget for {@link org.opencms.xml.types.CmsXmlStringValue}.<p>
 * 
 * Creates a configurable select box widget.
 * The select box options have to be written in the "default" appinfo node and use the following syntax:<br>
 * valueattribute1*:displayed text 1|valueattribute2:displayed text 2<br>
 * The asterisk marks the preselected value when creating a new value, the displayed text is optional.
 * You can use localized keys for the displayed text like ${key.keyname}.<p>
 *
 * @author Andreas Zahner (a.zahner@alkacon.com)
 * 
 * @version $Revision: 1.4 $
 * @since 5.5.3
 */
public class CmsXmlSelectorWidget extends A_CmsXmlWidget {
    
    /** The delimiter that separates the value attribute from the displayed option text. */
    public static final char C_DELIM_ATTRS = ':';
    /** The delimiter that separates the option entries of the select box to create. */
    public static final String C_DELIM_OPTIONS = "|";
    /** The character that marks the preselected option of the select box. */
    public static final char C_PRESELECTED = '*';

    /**
     * Creates a new editor widget.<p>
     */
    public CmsXmlSelectorWidget() {

        // empty constructor is required for class registration
    }

    /**
     * @see org.opencms.workplace.xmlwidgets.I_CmsXmlWidget#getDialogWidget(org.opencms.file.CmsObject, org.opencms.workplace.xmlwidgets.I_CmsWidgetDialog, org.opencms.xml.types.I_CmsXmlContentValue)
     */
    public String getDialogWidget(
        CmsObject cms,
        I_CmsWidgetDialog widgetDialog,
        I_CmsXmlContentValue value) throws CmsXmlException {

        String id = getParameterName(value);       
        StringBuffer result = new StringBuffer(16);
                
        result.append("<td class=\"xmlTd\"><select class=\"xmlInput\" name=\"");
        result.append(id);
        result.append("\" id=\"");
        result.append(id);
        result.append("\">"); 
        
        // get select box options from default value String
        String defaultValue = value.getContentDefinition().getContentHandler().getDefault(cms, value, widgetDialog.getLocale());
        if (CmsStringUtil.isEmpty(defaultValue)) {
            defaultValue = "";
        }
        // tokenize the found String
        StringTokenizer T = new StringTokenizer(defaultValue, C_DELIM_OPTIONS);
        boolean isPreselected;
        String val;
        String label;
        String selected;
        int delimPos;
        while (T.hasMoreTokens()) {
            // generate the option tags
            String part = T.nextToken();
            // check preselection of current option
            isPreselected = part.indexOf(C_PRESELECTED) != -1;
            selected = "";
            delimPos = part.indexOf(C_DELIM_ATTRS);
            if (delimPos != -1) {
                // a special label text is given
                val = part.substring(0, delimPos);
                label = part.substring(delimPos + 1);
            } else {
                // no special label text present, use complete String
                val = part;
                label = val;
            }
            
            if (isPreselected) {
                // remove eventual preselected flag markers from Strings
                String preSelected = "" + C_PRESELECTED;
                val = CmsStringUtil.substitute(val, preSelected, "");
                label = CmsStringUtil.substitute(label, preSelected, "");
            }
            
            // check if current option is selected
            String fieldValue = value.getStringValue(cms);
            if ((CmsStringUtil.isEmpty(fieldValue) && isPreselected) || val.equals(fieldValue)) {
                selected = " selected=\"selected\"";
            }
            
            // create the option
            result.append("<option value=\"");
            result.append(val);
            result.append("\"");
            result.append(selected);
            result.append(">");
            result.append(label);
            result.append("</option>");
        }
        
        result.append("</select>");        
        result.append("</td>");

        return result.toString();
    }
   
}