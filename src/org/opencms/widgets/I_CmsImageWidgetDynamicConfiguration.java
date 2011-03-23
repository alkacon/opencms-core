/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/widgets/I_CmsImageWidgetDynamicConfiguration.java,v $
 * Date   : $Date: 2011/03/23 14:50:14 $
 * Version: $Revision: 1.4 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2011 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
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

import java.util.List;

/**
 * Enables a dynamic configuration of values for the {@link CmsVfsImageWidget}.<p>
 * 
 * The following values can be configured dynamically:
 * <ul>
 * <li>The list of image format values for the corresponding format names</li>
 * <li>The type of the preselected image list (gallery or category)</li>
 * <li>The preselected image list (i.e. a gallery folder or category)</li>
 * </ul>
 *
 * @author Andreas Zahner 
 * 
 * @version $Revision: 1.4 $ 
 * 
 * @since 7.5.0 
 */
public interface I_CmsImageWidgetDynamicConfiguration {

    /**
     * Returns the matching format values for the given format select options.<p>
     * 
     * A format value should look like this:
     * <ul>
     * <li>320x480: The width and height as fixed values</li>
     * <li>320x?: A fixed width and dynamic height</li>
     * </ul>
     * 
     * @param cms an initialized instance of a CmsObject
     * @param widgetDialog the dialog where the widget is used on
     * @param param the widget parameter to generate the widget for
     * @param selectFormat the list of format select options ({@link CmsSelectWidgetOption}) for the widget
     * @param formatValues the list of predefined format values for the widget
     * @return  the matching format values for the given format select options
     */
    List getFormatValues(
        CmsObject cms,
        I_CmsWidgetDialog widgetDialog,
        I_CmsWidgetParameter param,
        List selectFormat,
        List formatValues);

    /**
     * Returns the required information for the initial image list to load.<p>
     * 
     * If a gallery should be shown, the path to the gallery must be specified,
     * for a category the category path.<p>
     * 
     * @param cms an initialized instance of a CmsObject
     * @param widgetDialog the dialog where the widget is used on
     * @param param the widget parameter to generate the widget for
     * @return the required information for the initial image list to load
     */
    String getStartup(CmsObject cms, I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter param);

    /**
     * Returns the type of the initial image list to load, either gallery or category.<p>
     * 
     * @param cms an initialized instance of a CmsObject
     * @param widgetDialog the dialog where the widget is used on
     * @param param the widget parameter to generate the widget for
     * @return the type of the initial image list to load, either gallery or category
     */
    String getType(CmsObject cms, I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter param);
}
