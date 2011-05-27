/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/Attic/CmsListItemWidgetUtil.java,v $
 * Date   : $Date: 2011/05/27 07:30:09 $
 * Version: $Revision: 1.5 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2011 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.gwt.client.ui;

import org.opencms.gwt.client.Messages;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.shared.CmsListInfoBean.StateIcon;

/**
 * List item widget util class.<p>
 * 
 * @author Ruediger Kurz
 * 
 * @version $Revision: 1.5 $
 * 
 * @since 8.0.0
 */
public final class CmsListItemWidgetUtil {

    /**
     * Hide the default constructor.<p>
     */
    private CmsListItemWidgetUtil() {

        // noop
    }

    /**
     * Sets the page icon for the given list item widget.<p>
     * 
     * @param listItem the list item to set the icon for
     * @param icon the icon to set
     */
    public static void setPageIcon(CmsListItemWidget listItem, StateIcon icon) {

        switch (icon) {
            case export:
                listItem.setIcon(I_CmsLayoutBundle.INSTANCE.listItemWidgetCss().export());
                listItem.setIconTitle(Messages.get().key(Messages.GUI_ICON_TITLE_EXPORT_0));
                break;
            case secure:
                listItem.setIcon(I_CmsLayoutBundle.INSTANCE.listItemWidgetCss().secure());
                listItem.setIconTitle(Messages.get().key(Messages.GUI_ICON_TITLE_SECURE_0));
                break;
            case redirect:
                listItem.setIcon(I_CmsLayoutBundle.INSTANCE.listItemWidgetCss().redirect());
                listItem.setIconTitle(Messages.get().key(Messages.GUI_ICON_TITLE_REDIRECT_0));
                break;
            case standard:
            default:
                listItem.setIcon(I_CmsLayoutBundle.INSTANCE.listItemWidgetCss().normal());
                listItem.setIconTitle(null);
                break;
        }
    }
}
