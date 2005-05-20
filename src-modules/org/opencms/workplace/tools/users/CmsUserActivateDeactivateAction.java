/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/users/Attic/CmsUserActivateDeactivateAction.java,v $
 * Date   : $Date: 2005/05/20 16:55:03 $
 * Version: $Revision: 1.7 $
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

package org.opencms.workplace.tools.users;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.list.A_CmsListTwoStatesAction;
import org.opencms.workplace.list.I_CmsListDirectAction;

/**
 * Activate/deactivate action for a html list.<p>
 * 
 * @author Michael Moossen (m.moossen@alkacon.com) 
 * @version $Revision: 1.7 $
 * @since 5.7.3
 */
public class CmsUserActivateDeactivateAction extends A_CmsListTwoStatesAction {

    /**
     * Default Constructor.<p>
     * 
     * @param listId the id of the associated list
     * @param id unique id
     * @param cms the cms context
     */
    protected CmsUserActivateDeactivateAction(String listId, String id, CmsObject cms) {

        super(listId, id, cms);
    }

    /**
     * Full Constructor.<p>
     * 
     * @param listId the id of the associated list
     * @param id unique id
     * @param cms the cms context
     * @param actAction the first action
     * @param deactAction the second action
     */
    protected CmsUserActivateDeactivateAction(
        String listId,
        String id,
        CmsObject cms,
        I_CmsListDirectAction actAction,
        I_CmsListDirectAction deactAction) {

        this(listId, id, cms);
        setFirstAction(actAction);
        setSecondAction(deactAction);
    }
    
    /**
     * @see org.opencms.workplace.list.A_CmsListToggleAction#getName()
     */
    public CmsMessageContainer getName() {

        if (!isEnabled()) {
            return Messages.get().container(Messages.GUI_USERS_LIST_ACTION_ACTIVATE_DISABLED_NAME_0);
        }
        return super.getName();
    }
    
    /**
     * @see org.opencms.workplace.list.A_CmsListToggleAction#selectAction()
     */
    public I_CmsListDirectAction selectAction() {

        if (getItem() != null) {
            String usrId = getItem().getId();
            try {
                CmsUser user = getCms().readUser(new CmsUUID(usrId));
                if (user.getDisabled()) {
                    return getFirstAction();
                }
            } catch (CmsException e) {
                // noop
            }
        }
        return getSecondAction();
    }

    /**
     * @see org.opencms.workplace.tools.I_CmsHtmlIconButton#isEnabled()
     */
    public boolean isEnabled() {

        if (getItem() != null) {
            try {
                String usrName = getItem().get(CmsUsersAdminTool.LIST_COLUMN_LOGIN).toString();
                return !getCms().userInGroup(usrName, OpenCms.getDefaultUsers().getGroupAdministrators());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return super.isEnabled();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListToggleAction#getHelpText()
     */
    public CmsMessageContainer getHelpText() {

        if (isEnabled()) {
            return super.getHelpText();
        }
        return Messages.get().container(Messages.GUI_USERS_LIST_ACTION_ACTIVATE_DISABLED_HELP_0);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListToggleAction#getIconPath()
     */
    public String getIconPath() {

        if (isEnabled()) {
            return super.getIconPath();
        }
        return "tools/users/buttons/deactivate_disabled.gif";
    }

}