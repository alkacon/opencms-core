/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/widgets/CmsTextareaWidget.java,v $
 * Date   : $Date: 2005/06/22 10:38:11 $
 * Version: $Revision: 1.6 $
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
 * Provides a standard HTML form textarea widget, for use on a widget dialog.<p>
 * 
 * Displays a textarea with 4 rows to enter String values conveniently.<p>
 *
 * @author Andreas Zahner 
 * 
 * @version $Revision: 1.6 $
 * @since 5.7.2
 */
public class CmsTextareaWidget extends A_CmsWidget {

    /** Default number of rows to display. */
    private static final int C_DEFAULT_ROWS_NUMBER = 4;

    /**
     * Creates a new textarea widget.<p>
     */
    public CmsTextareaWidget() {

        // default configuration is to display 4 rows
        this(C_DEFAULT_ROWS_NUMBER);
    }

    /**
     * Creates a new textarea widget with the given number of rows.<p>
     * 
     * @param rows the number of rows to display
     */
    public CmsTextareaWidget(int rows) {

        super("" + rows);
    }

    /**
     * Creates a new textarea widget with the given configuration.<p>
     * 
     * @param configuration the configuration to use
     */
    public CmsTextareaWidget(String configuration) {

        super(configuration);
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#getDialogWidget(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog, org.opencms.widgets.I_CmsWidgetParameter)
     */
    public String getDialogWidget(CmsObject cms, I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter param) {

        String id = param.getId();
        StringBuffer result = new StringBuffer(16);
        int rows = C_DEFAULT_ROWS_NUMBER;
        try {
            rows = new Integer(getConfiguration()).intValue();
        } catch (Exception e) {
            // ignore
        }

        result.append("<td class=\"xmlTd\">");
        result.append("<textarea class=\"xmlInput maxwidth");
        if (param.hasError()) {
            result.append(" xmlInputError");
        }
        result.append("\" name=\"");
        result.append(id);
        result.append("\" rows=\"");
        result.append(rows);
        result.append("\" wrap=\"virtual\" style=\"overflow:auto;\">");
        result.append(CmsEncoder.escapeXml(param.getStringValue(cms)));
        result.append("</textarea>");
        result.append("</td>");

        return result.toString();
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#newInstance()
     */
    public I_CmsWidget newInstance() {

        return new CmsTextareaWidget(getConfiguration());
    }

}