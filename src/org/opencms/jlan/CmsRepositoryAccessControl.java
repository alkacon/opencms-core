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

package org.opencms.jlan;

import org.alfresco.jlan.server.SrvSession;
import org.alfresco.jlan.server.auth.acl.AccessControl;
import org.alfresco.jlan.server.auth.acl.AccessControlManager;
import org.alfresco.jlan.server.core.SharedDevice;

/**
 * JLAN access control which just asks the CmsJlanRepository if a user can connect to it.<p>
 */
public class CmsRepositoryAccessControl extends AccessControl {

    /** The repository for which to control access. */
    private CmsJlanRepository m_repository;

    /**
     * Creates a new instance.<p>
     *
     * @param repository the repository for which to control access
     */
    public CmsRepositoryAccessControl(CmsJlanRepository repository) {

        super("viewPermission", "", 0);
        m_repository = repository;
    }

    /**
     * @see org.alfresco.jlan.server.auth.acl.AccessControl#allowsAccess(org.alfresco.jlan.server.SrvSession, org.alfresco.jlan.server.core.SharedDevice, org.alfresco.jlan.server.auth.acl.AccessControlManager)
     */
    @Override
    public int allowsAccess(SrvSession session, SharedDevice device, AccessControlManager manager) {

        String user = session.getClientInformation().getUserName();
        user = CmsJlanUsers.translateUser(user);
        if (m_repository.allowAccess(user)) {
            return AccessControl.Default;
        } else {
            return AccessControl.NoAccess;
        }
    }

}
