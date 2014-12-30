/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.workplace.list;

import org.opencms.main.OpenCms;
import org.opencms.workplace.CmsWorkplace;

/**
 * Generates a CSV file for a list.<p>
 * 
 * @since 6.0.0 
 */
public class CmsListCsvExportIAction extends A_CmsListIndependentJsAction {

    /** List independent action id constant. */
    public static final String LIST_ACTION_ID = "iac";

    /**
     * Default constructor.<p>
     */
    public CmsListCsvExportIAction() {

        super(LIST_ACTION_ID);
        setName(Messages.get().container(Messages.GUI_LIST_ACTION_CSV_NAME_0));
        setHelpText(Messages.get().container(Messages.GUI_LIST_ACTION_CSV_HELP_0));
        setConfirmationMessage(Messages.get().container(Messages.GUI_LIST_ACTION_CSV_CONF_0));
        setIconPath("list/csv.png");
        setEnabled(true);
        setVisible(true);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListIndependentJsAction#jsCode(CmsWorkplace)
     */
    @Override
    public String jsCode(CmsWorkplace wp) {

        String url = OpenCms.getLinkManager().substituteLink(
            wp.getCms(),
            "/system/workplace/commons/list-csv.jsp?"
                + CmsListCsvExportDialog.PARAM_LISTCLASS
                + "="
                + wp.getClass().getName());
        String title = "CSV - " + ((A_CmsListDialog)wp).getList().getName().key(wp.getLocale());
        String opts = "toolbar=no,location=no,directories=no,status=yes,menubar=0,scrollbars=yes,resizable=yes,top=150,left=660,width=450,height=450";
        StringBuffer js = new StringBuffer(512);
        js.append("window.open('");
        js.append(url);
        js.append("', '");
        js.append(title);
        js.append("', '");
        js.append(opts);
        js.append("');");
        return js.toString();
    }
}
