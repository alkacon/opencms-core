/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/list/CmsListIndependentAction.java,v $
 * Date   : $Date: 2005/06/21 15:54:15 $
 * Version: $Revision: 1.12 $
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

import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.tools.A_CmsHtmlIconButton;
import org.opencms.workplace.tools.CmsHtmlIconButtonStyleEnum;

/**
 * Default implementation of a independent action for a html list.<p>
 * 
 * @author Michael Moossen (m.moossen@alkacon.com) 
 * @version $Revision: 1.12 $
 * @since 5.7.3
 */
public class CmsListIndependentAction extends A_CmsListAction {

    /** list action id constant. */
    public static final String LIST_ACTION_PRINT = "print";

    /**
     * Default Constructor.<p>
     * 
     * @param id unique id
     */
    public CmsListIndependentAction(String id) {

        super(id);
    }

    /**
     * Creates a new list print action.<p>
     * 
     * @return a new list print action
     */
    public static final CmsListIndependentAction getDefaultPrintListAction() {

        CmsListIndependentAction action = new CmsListIndependentAction(LIST_ACTION_PRINT);
        action.setName(Messages.get().container(Messages.GUI_LIST_ACTION_PRINT_NAME_0));
        action.setHelpText(Messages.get().container(Messages.GUI_LIST_ACTION_PRINT_HELP_0));
        action.setIconPath("list/print.gif");
        action.setEnabled(true);
        action.setConfirmationMessage(Messages.get().container(Messages.GUI_LIST_ACTION_PRINT_CONF_0));
        return action;
    }

    /**
     * @see org.opencms.workplace.tools.I_CmsHtmlIconButton#buttonHtml(CmsWorkplace)
     */
    public String buttonHtml(CmsWorkplace wp) {

        String onClic = "listIndepAction('"
            + getListId()
            + "','"
            + getId()
            + "', '"
            + CmsStringUtil.escapeJavaScript(wp.resolveMacros(getConfirmationMessage().key(wp.getLocale())))
            + "');";
        return A_CmsHtmlIconButton.defaultButtonHtml(
            CmsHtmlIconButtonStyleEnum.SMALL_ICON_TEXT,
            getId(),
            getName().key(wp.getLocale()),
            getHelpText().key(wp.getLocale()),
            isEnabled(),
            getIconPath(),
            onClic);
    }
}