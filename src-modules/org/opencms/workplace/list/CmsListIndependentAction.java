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

package org.opencms.workplace.list;

import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.tools.A_CmsHtmlIconButton;
import org.opencms.workplace.tools.CmsHtmlIconButtonStyleEnum;

/**
 * Default implementation of a independent action for a html list.<p>
 *
 * @since 6.0.0
 */
public class CmsListIndependentAction extends A_CmsListAction {

    /** List independent action id constant. */
    public static final String ACTION_EXPLORER_SWITCH_ID = "iaes";

    /**
     * Default Constructor.<p>
     *
     * @param id unique id
     */
    public CmsListIndependentAction(String id) {

        super(id);
    }

    /**
     * Help method to resolve the on clic text to use.<p>
     *
     * @param wp the workplace context
     *
     * @return the on clic text
     */
    protected String resolveOnClic(CmsWorkplace wp) {

        return "listIndepAction('"
            + getListId()
            + "','"
            + getId()
            + "', '"
            + CmsStringUtil.escapeJavaScript(wp.resolveMacros(getConfirmationMessage().key(wp.getLocale())))
            + "');";
    }

    /**
     * @see org.opencms.workplace.tools.I_CmsHtmlIconButton#buttonHtml(CmsWorkplace)
     */
    public String buttonHtml(CmsWorkplace wp) {

        if (!isVisible()) {
            return "";
        }
        return A_CmsHtmlIconButton.defaultButtonHtml(
            CmsHtmlIconButtonStyleEnum.SMALL_ICON_TEXT,
            getId(),
            getName().key(wp.getLocale()),
            getHelpText().key(wp.getLocale()),
            isEnabled(),
            getIconPath(),
            null,
            resolveOnClic(wp));
    }

    /**
     * Returns the default explorer switch action for explorer list dialogs.<p>
     *
     * @return the default explorer switch action
     */
    public static CmsListIndependentAction getDefaultExplorerSwitchAction() {

        CmsListIndependentAction defAction = new CmsListIndependentAction(ACTION_EXPLORER_SWITCH_ID);
        defAction.setName(Messages.get().container(Messages.GUI_LIST_ACTION_EXPLORER_SWITCH_NAME_0));
        defAction.setHelpText(Messages.get().container(Messages.GUI_LIST_ACTION_EXPLORER_SWITCH_HELP_0));
        defAction.setConfirmationMessage(Messages.get().container(Messages.GUI_LIST_ACTION_EXPLORER_SWITCH_CONF_0));
        defAction.setIconPath("list/explorer.png");
        defAction.setEnabled(true);
        defAction.setVisible(true);
        return defAction;
    }
}