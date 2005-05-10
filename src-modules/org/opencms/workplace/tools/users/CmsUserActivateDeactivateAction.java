/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/users/Attic/CmsUserActivateDeactivateAction.java,v $
 * Date   : $Date: 2005/05/10 12:04:58 $
 * Version: $Revision: 1.2 $
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
import org.opencms.workplace.list.A_CmsListToggleAction;
import org.opencms.workplace.list.I_CmsListDirectAction;

/**
 * Activate/deactivate action for a html list.<p>
 * 
 * @author Michael Moossen (m.moossen@alkacon.com) 
 * @version $Revision: 1.2 $
 * @since 5.7.3
 */
public class CmsUserActivateDeactivateAction extends A_CmsListToggleAction {

    /** The activation action. */
    private I_CmsListDirectAction m_actAction;
    /** The desactivation action. */
    private I_CmsListDirectAction m_deactAction;
    /** The cms context. */
    private CmsObject m_cms;

    /**
     * Default Constructor.<p>
     * 
     * @param listId the id of the associated list
     * @param id unique id
     * @param cms the cms context
     */
    protected CmsUserActivateDeactivateAction(String listId, String id, CmsObject cms) {

        super(listId, id);
        m_cms = cms;
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
        setActivationAction(actAction);
        setDeactivationAction(deactAction);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListToggleAction#selectAction()
     */
    public I_CmsListDirectAction selectAction() {

        if (getItem() != null) {
            String usrName = getItem().get(CmsUsersAdminTool.LIST_COLUMN_LOGIN).toString();
            try {
                CmsUser user = m_cms.readUser(usrName);
                if (user.getDisabled()) {
                    return m_actAction;
                }
            } catch (CmsException e) {
                // noop
            }
        }
        return m_deactAction;
    }

    /**
     * @see org.opencms.workplace.list.I_CmsHtmlIconButton#isEnabled()
     */
    public boolean isEnabled() {

        if (getItem() != null) {
            try {
                String usrName = getItem().get(CmsUsersAdminTool.LIST_COLUMN_LOGIN).toString();
                return !m_cms.userInGroup(usrName, OpenCms.getDefaultUsers().getGroupAdministrators());
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
        return "buttons/anchor.gif";
    }
    
    /**
     * Sets the activation Action.<p>
     *
     * @param actAction the activation Action to set
     */
    public void setActivationAction(I_CmsListDirectAction actAction) {

        m_actAction = actAction;
    }

    /**
     * Sets the deactivation Action.<p>
     *
     * @param deactAction the deactivation Action to set
     */
    public void setDeactivationAction(I_CmsListDirectAction deactAction) {

        m_deactAction = deactAction;
    }
}