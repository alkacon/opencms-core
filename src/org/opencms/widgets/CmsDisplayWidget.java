/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/widgets/CmsDisplayWidget.java,v $
 * Date   : $Date: 2005/06/22 10:38:11 $
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

package org.opencms.widgets;

import org.opencms.file.CmsObject;
import org.opencms.i18n.CmsEncoder;

/**
 * Provides a display only widget, for use on a widget dialog.<p>
 *
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.4 $
 * @since 5.5.0
 */
public class CmsDisplayWidget extends A_CmsWidget {

    /**
     * Creates a new input widget.<p>
     */
    public CmsDisplayWidget() {

        // empty constructor is required for class registration
        this("");
    }

    /**
     * Creates a new input widget with the given configuration.<p>
     * 
     * @param configuration the configuration to use
     */
    public CmsDisplayWidget(String configuration) {

        super(configuration);
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#getDialogWidget(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog, org.opencms.widgets.I_CmsWidgetParameter)
     */
    public String getDialogWidget(CmsObject cms, I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter param) {

        String id = param.getId();

        StringBuffer result = new StringBuffer(16);
        
        result.append("<td class=\"xmlTd\">"); 
        result.append("<span class=\"xmlInput textInput\" style=\"border: 0px solid black;\">");
        result.append(param.getStringValue(cms));
        result.append("</span>");
        result.append("<input type=\"hidden\"");
        result.append(" name=\"");
        result.append(id);
        result.append("\" id=\"");
        result.append(id);
        result.append("\" value=\"");
        result.append(CmsEncoder.escapeXml(param.getStringValue(cms)));
        result.append("\">");
        result.append("</td>");

        return result.toString();
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#newInstance()
     */
    public I_CmsWidget newInstance() {

        return new CmsDisplayWidget(getConfiguration());
    }
    
    
    /**
     * @see org.opencms.widgets.A_CmsWidget#getHelpBubble(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog, org.opencms.widgets.I_CmsWidgetParameter)
     */
    public String getHelpBubble(CmsObject cms, I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter param) {

        return "<td>&nbsp;</td>";
    }
    
    
    /**
     * @see org.opencms.widgets.A_CmsWidget#getHelpText(org.opencms.widgets.I_CmsWidgetDialog, org.opencms.widgets.I_CmsWidgetParameter)
     */
    public String getHelpText(I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter param) {

        return "";
    }
}