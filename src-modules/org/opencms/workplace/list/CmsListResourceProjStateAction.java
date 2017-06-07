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

import org.opencms.i18n.CmsMessageContainer;

/**
 * Displays an icon action for the project state.<p>
 *
 * @since 6.0.0
 */
public class CmsListResourceProjStateAction extends CmsListExplorerDirectAction {

    /**
     * Default Constructor.<p>
     *
     * @param id the unique id
     */
    public CmsListResourceProjStateAction(String id) {

        super(id);
    }

    /**
     * @see org.opencms.workplace.tools.I_CmsHtmlIconButton#getHelpText()
     */
    @Override
    public CmsMessageContainer getHelpText() {

        if ((super.getHelpText() == null) || super.getHelpText().equals(EMPTY_MESSAGE)) {
            return Messages.get().container(Messages.GUI_EXPLORER_LIST_ACTION_PROJECTSTATE_HELP_0);
        }
        return super.getHelpText();
    }

    /**
     * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#getIconPath()
     */
    @Override
    public String getIconPath() {

        return getResourceUtil().getIconPathProjectState();
    }

    /**
     * @see org.opencms.workplace.tools.I_CmsHtmlIconButton#getName()
     */
    @Override
    public CmsMessageContainer getName() {

        if (super.getName() == null) {
            if (getResourceUtil().getProjectState().isUnlocked()) {
                return EMPTY_MESSAGE;
            } else if (getResourceUtil().getProjectState().isModifiedInCurrentProject()) {
                return Messages.get().container(
                    Messages.GUI_EXPLORER_LIST_ACTION_INPROJECT_NAME_1,
                    getResourceUtil().getReferenceProject().getName());
            } else if (getResourceUtil().getProjectState().isModifiedInOtherProject()) {
                return Messages.get().container(
                    Messages.GUI_EXPLORER_LIST_ACTION_INPROJECT_NAME_1,
                    getResourceUtil().getLockedInProjectName());
            } else {
                return new CmsMessageContainer(null, getResourceUtil().getSystemLockInfo(false));
            }
        }
        return super.getName();
    }
}
