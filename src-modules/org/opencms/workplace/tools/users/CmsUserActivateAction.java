/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/users/Attic/CmsUserActivateAction.java,v $
 * Date   : $Date: 2005/06/03 16:29:19 $
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
import org.opencms.main.CmsException;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.list.A_CmsListTwoStatesAction;
import org.opencms.workplace.list.I_CmsListDirectAction;

/**
 * Activate/deactivate action for a html list.<p>
 * 
 * @author Michael Moossen (m.moossen@alkacon.com) 
 * @version $Revision: 1.2 $
 * @since 5.7.3
 */
public class CmsUserActivateAction extends A_CmsListTwoStatesAction {

    /**
     * Default Constructor.<p>
     * 
     * @param listId the id of the associated list
     * @param id unique id
     * @param cms the cms context
     */
    protected CmsUserActivateAction(String listId, String id, CmsObject cms) {

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
    protected CmsUserActivateAction(
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
}