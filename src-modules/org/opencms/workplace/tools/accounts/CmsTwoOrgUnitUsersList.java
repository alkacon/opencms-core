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

package org.opencms.workplace.tools.accounts;

import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.list.A_CmsListDialog;
import org.opencms.workplace.list.CmsTwoListsDialog;

/**
 * Class for managing two organizational unit lists on the same dialog.<p>
 *
 * @since 6.5.6
 */
public class CmsTwoOrgUnitUsersList extends CmsTwoListsDialog {

    /**
     * Default constructor.<p>
     *
     * @param wp1 the workplace instance for the first list
     * @param wp2 the workplace instance for the second list
     */
    public CmsTwoOrgUnitUsersList(A_CmsListDialog wp1, A_CmsListDialog wp2) {

        super(wp1, wp2);
    }

    /**
     * @see org.opencms.workplace.list.CmsTwoListsDialog#defaultActionHtml()
     */
    @Override
    public String defaultActionHtml() {

        StringBuffer result = new StringBuffer(2048);
        result.append(defaultActionHtmlStart());
        result.append(defaultActionHtmlContent());
        result.append(customHtmlEnd());
        result.append(defaultActionHtmlEnd());
        return result.toString();
    }

    /**
     * Returns the custom html end code for this dialog.<p>
     *
     * @return custom html code
     */
    protected String customHtmlEnd() {

        StringBuffer result = new StringBuffer(512);
        result.append("<form name='actions' method='post' action='");
        result.append(getFirstWp().getDialogRealUri());
        result.append("' class='nomargin' onsubmit=\"return submitAction('ok', null, 'actions');\">\n");
        result.append(getFirstWp().allParamsAsHidden());
        result.append("<div class=\"dialogspacer\" unselectable=\"on\">&nbsp;</div>\n");
        result.append("<!-- button row start -->\n<div class=\"dialogbuttons\" unselectable=\"on\">\n");
        result.append("<input name='");
        result.append(CmsDialog.DIALOG_CONFIRMED);
        result.append("' type='button' value='");
        result.append(
            Messages.get().container(Messages.GUI_ORGUNITUSERS_BUTTON_CONFIRM_0).key(getFirstWp().getLocale()));
        result.append("' onclick=\"submitAction('");
        result.append(CmsDialog.DIALOG_CONFIRMED);
        result.append("', form);\" class='dialogbutton'>\n");
        result.append("<input name='");
        result.append(CmsDialog.DIALOG_CANCEL);
        result.append("' type='button' value='");
        result.append(
            Messages.get().container(Messages.GUI_ORGUNITUSERS_BUTTON_CANCEL_0).key(getFirstWp().getLocale()));
        result.append("' onclick=\"submitAction('");
        result.append(CmsDialog.DIALOG_CANCEL);
        result.append("', form);\" class='dialogbutton'>\n");
        result.append("</div>\n<!-- button row end -->\n");
        result.append("</form>\n");
        return result.toString();
    }
}
