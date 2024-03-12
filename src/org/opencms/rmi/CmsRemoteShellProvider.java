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

package org.opencms.rmi;

import org.opencms.main.CmsLog;

import java.rmi.RemoteException;

import org.apache.commons.logging.Log;

/**
 * Remote object responsible for creating new remote shell instances.<p>
 */
public class CmsRemoteShellProvider implements I_CmsRemoteShellProvider {

    /** Log instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsRemoteShellProvider.class);

    /** The port to use for created CmsRemoteShell instances. */
    private int m_port;

    /**
     * Creates a new instance.<p>
     *
     * @param port the port to use for created CmsRemoteShell instances
     */
    public CmsRemoteShellProvider(int port) {
        m_port = port;
    }

    /**
     * @see org.opencms.rmi.I_CmsRemoteShellProvider#createShell(java.lang.String)
     */
    @Override
    public I_CmsRemoteShell createShell(String additionalCommandsNames) throws RemoteException {
        try {
            CmsRemoteShell shell = new CmsRemoteShell(additionalCommandsNames, m_port);
            return shell;
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new RemoteException("Remote error: " + e.getLocalizedMessage());
        }
    }
}
