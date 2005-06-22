/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/projects/Attic/CmsPublishProjectAction.java,v $
 * Date   : $Date: 2005/06/22 10:38:16 $
 * Version: $Revision: 1.3 $
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

package org.opencms.workplace.tools.projects;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.workplace.list.A_CmsListTwoStatesAction;
import org.opencms.workplace.list.I_CmsListDirectAction;

/**
 * publish enabled/disabled action.<p>
 * 
 * @author Michael Moossen  
 * @version $Revision: 1.3 $
 * @since 5.7.3
 */
public class CmsPublishProjectAction extends A_CmsListTwoStatesAction {

    /**
     * Default Constructor.<p>
     * 
     * @param id the unique id
     * @param cms the cms context
     */
    protected CmsPublishProjectAction(String id, CmsObject cms) {

        super(id, cms);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListToggleAction#selectAction()
     */
    public I_CmsListDirectAction selectAction() {

        if (getItem() != null) {
            try {
                if (getCms().countLockedResources(new Integer(getItem().getId()).intValue()) == 0) {
                    return getFirstAction();
                }
            } catch (CmsException e) {
                // noop
            }
        }
        return getSecondAction();
    }
}