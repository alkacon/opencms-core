/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/list/CmsListResourceLockAction.java,v $
 * Date   : $Date: 2005/12/14 10:36:37 $
 * Version: $Revision: 1.1.2.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

import org.opencms.file.CmsObject;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.lock.CmsLock;

/**
 * Displays an icon action for the lock type.<p>
 * 
 * @author Michael Moossen  
 * 
 * @version $Revision: 1.1.2.1 $ 
 * 
 * @since 6.0.0 
 */
public class CmsListResourceLockAction extends CmsListExplorerDirectAction {

    /**
     * Default Constructor.<p>
     * 
     * @param id the unique id
     * @param cms the cms context
     * @param wp the workplace context
     */
    public CmsListResourceLockAction(String id, CmsObject cms, A_CmsListExplorerDialog wp) {

        super(id, cms, wp);
    }

    /**
     * @see org.opencms.workplace.tools.I_CmsHtmlIconButton#getHelpText()
     */
    public CmsMessageContainer getHelpText() {

        if (super.getHelpText() == null) {
            return Messages.get().container(Messages.GUI_EXPLORER_LIST_ACTION_LOCK_HELP_0);
        }
        return super.getHelpText();
    }

    /**
     * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#getIconPath()
     */
    public String getIconPath() {

        return getResourceUtil().getIconPathLock();
    }

    /**
     * @see org.opencms.workplace.tools.I_CmsHtmlIconButton#getName()
     */
    public CmsMessageContainer getName() {

        if (super.getName() == null) {
            if (getResourceUtil().getLockTypeId() != CmsLock.TYPE_UNLOCKED) {
                return Messages.get().container(
                    Messages.GUI_EXPLORER_LIST_ACTION_LOCK_NAME_2,
                    getResourceUtil().getLockedByName(),
                    getResourceUtil().getLockedInProjectName());
            } else {
                return Messages.get().container(Messages.GUI_EXPLORER_LIST_ACTION_UNLOCK_NAME_0);
            }
        }
        return super.getName();
    }
}
