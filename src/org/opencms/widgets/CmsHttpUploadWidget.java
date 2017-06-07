/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
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

/**
 * Provides a http upload widget, for use on a widget dialog.<p>
 *
 * @since 6.5.6
 */
public class CmsHttpUploadWidget extends A_CmsWidget {

    /**
     * Creates a new input widget.<p>
     */
    public CmsHttpUploadWidget() {

        // empty constructor is required for class registration
        this("");
    }

    /**
     * Creates a new input widget with the given configuration.<p>
     *
     * @param configuration the configuration to use
     */
    public CmsHttpUploadWidget(String configuration) {

        super(configuration);
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#getDialogWidget(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog, org.opencms.widgets.I_CmsWidgetParameter)
     */
    public String getDialogWidget(CmsObject cms, I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter param) {

        String id = param.getId();

        StringBuffer result = new StringBuffer(16);

        result.append("<td class=\"xmlTd\">");
        result.append("<input type=\"file\" name=\"");
        result.append(id);
        // accept has to be set dynamically
        result.append("\" class=\"maxwidth\" accept=\"text/comma-separated-values\">");
        result.append("</td>");

        return result.toString();

    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#newInstance()
     */
    public I_CmsWidget newInstance() {

        return new CmsHttpUploadWidget(getConfiguration());
    }

}
