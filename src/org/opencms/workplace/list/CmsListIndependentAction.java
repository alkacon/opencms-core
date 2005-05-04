/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/list/CmsListIndependentAction.java,v $
 * Date   : $Date: 2005/05/04 15:16:17 $
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

package org.opencms.workplace.list;

import org.opencms.i18n.CmsMessageContainer;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;

/**
 * Default implementation of a independent action for a html list.<p>
 * 
 * @author Michael Moossen (m.moossen@alkacon.com) 
 * @version $Revision: 1.4 $
 * @since 5.7.3
 */
public class CmsListIndependentAction extends A_CmsListAction {

    /**
     * Default Constructor.<p>
     * 
     * @param listId the id of the associated list
     * @param id unique id
     * @param name the name
     * @param iconPath the link to the icon
     * @param helpText the help text
     * @param enabled if enabled
     * @param confirmationMessage the confirmation message
     */
    public CmsListIndependentAction(
        String listId,
        String id,
        CmsMessageContainer name,
        String iconPath,
        CmsMessageContainer helpText,
        boolean enabled,
        CmsMessageContainer confirmationMessage) {

        super(listId, id, name, iconPath, helpText, enabled, confirmationMessage);
    }

    /**
     * @see org.opencms.workplace.list.I_CmsHtmlButton#buttonHtml(CmsWorkplace)
     */
    public String buttonHtml(CmsWorkplace wp) {

        if (isEnabled()) {
            String onClic = getListId()
                + "ListIndepAction('"
                + getId()
                + "', '"
                + CmsStringUtil.escapeJavaScript(wp.resolveMacros(getConfirmationMessage().key(wp.getLocale())))
                + "');";
            return A_CmsHtmlIconButton.defaultButtonHtml(getId(), getName().key(wp.getLocale()), getHelpText().key(
                wp.getLocale()), isEnabled(), getIconPath(), onClic);
        }
        return "";
    }

    /** list action id constant. */
    public static final String LIST_ACTION_REFRESH = "refresh";

    /** list action id constant. */
    public static final String LIST_ACTION_PRINT = "print";

    /**
     * Creates a new list refresh action for the given list.<p>
     * 
     * @param listId the id of the associated list
     * 
     * @return a new list refresh action
     */
    public static final CmsListIndependentAction getDefaultRefreshListAction(String listId) {

        return new CmsListIndependentAction(listId, LIST_ACTION_REFRESH, Messages.get().container(
            Messages.GUI_LIST_ACTION_REFRESH_NAME_0), "list/reload.gif", Messages.get().container(
            Messages.GUI_LIST_ACTION_REFRESH_HELP_0), true, // enabled
            Messages.get().container(Messages.GUI_LIST_ACTION_REFRESH_CONF_0));
    }

    /**
     * Creates a new list print action for the given list.<p>
     * 
     * @param listId the id of the associated list
     * 
     * @return a new list print action
     */
    public static final CmsListIndependentAction getDefaultPrintListAction(String listId) {

        return new CmsListIndependentAction(listId, LIST_ACTION_PRINT, Messages.get().container(
            Messages.GUI_LIST_ACTION_PRINT_NAME_0), "list/print.gif", Messages.get().container(
            Messages.GUI_LIST_ACTION_PRINT_HELP_0), true, // enabled
            Messages.get().container(Messages.GUI_LIST_ACTION_PRINT_CONF_0));
    }
}