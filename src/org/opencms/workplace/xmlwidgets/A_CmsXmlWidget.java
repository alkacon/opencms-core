/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/xmlwidgets/Attic/A_CmsXmlWidget.java,v $
 * Date   : $Date: 2004/10/18 12:44:00 $
 * Version: $Revision: 1.3 $
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

import java.util.Map;

/**
 * Base class for XML editor widgets.<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.3 $
 * @since 5.5.0
 */
public abstract class A_CmsXmlWidget implements I_CmsXmlWidget {

    /** Prefix for message locales. */
    static final String C_MESSAGE_PREFIX = "editor.label.";
        
    /**
     * Creates a message for message locale with the correct prefix.<p>
     * 
     * @param editor reference to an editor object
     * @param contentDefintion the ContentDefinition or null
     * @param value the value to create the for
     * @return message key for message locales with the correct prefix
     */
    public String getMessage(CmsXmlContentEditor editor, CmsXmlContentDefinition contentDefintion, String value) {
        String contentDefinitionName = new String();
        // get the name of the content defintion if there is one
        if (contentDefintion != null) {
            contentDefinitionName = contentDefintion.getName();
        }
        // calculate the key
        String locKey = C_MESSAGE_PREFIX + contentDefinitionName + "." + value;
        String locValue = editor.key(locKey);
        if (locValue.startsWith("???")) {
            // there was no value found for this key, so use the unlocalised message key
            locValue = value;
        }
        return locValue;
    }    
    
    /**
     * @see org.opencms.workplace.xmlwidgets.I_CmsXmlWidget#getParameterName(org.opencms.xml.types.I_CmsXmlContentValue)
     */
    public String getParameterName(I_CmsXmlContentValue value) {

        StringBuffer result = new StringBuffer(128);
        result.append(value.getTypeName());
        result.append(".");
        result.append(value.getNodeName());
        result.append(".");
        result.append(value.getIndex());
        return result.toString();
    }

    /**
     * @see org.opencms.workplace.xmlwidgets.I_CmsXmlWidget#setEditorValue(org.opencms.file.CmsObject, org.opencms.xml.A_CmsXmlDocument, java.util.Map, org.opencms.workplace.editors.CmsXmlContentEditor, I_CmsXmlContentValue)
     */
    public void setEditorValue(
        CmsObject cms,
        A_CmsXmlDocument document,
        Map formParameters,
        CmsXmlContentEditor editor,
        I_CmsXmlContentValue value) throws CmsXmlException {

        String[] values = (String[])formParameters.get(getParameterName(value));
        if ((values != null) && (values.length > 0)) {
            value.setStringValue(cms, document, values[0]);
        }
    }
}