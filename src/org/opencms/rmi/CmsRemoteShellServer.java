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

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import org.apache.commons.logging.Log;

/**
 * This class is used to initialize the RMI mechanism and export the object used to access the remote shell.<p>
 */
public class CmsRemoteShellServer {

    /** The log instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsRemoteShellServer.class);

    /** Indicates whether the remote shell server has been initialized. */
    private static boolean m_initialized;

    /** The real instance of the shell provider. */
    private I_CmsRemoteShellProvider m_provider;

    /** The RMI registry. */
    private Registry m_registry;

    /** The port for the RMI registry. */
    private int m_port = CmsRemoteShellConstants.DEFAULT_PORT;

    /**
     * Creates a new instance.<p>
     *
     * @param port the port for the RMI registry
     */
    public CmsRemoteShellServer(int port) {
        m_port = port;
    }

    /**
     * Initializes the RMI registry and exports the remote shell provider to it.<p>
     */
    public void initServer() {

        if (m_initialized) {
            return;
        }
        try {
            m_registry = LocateRegistry.createRegistry(m_port);
            m_provider = new CmsRemoteShellProvider(m_port);
            I_CmsRemoteShellProvider providerStub = (I_CmsRemoteShellProvider)(UnicastRemoteObject.exportObject(
                m_provider,
                m_port));
            m_registry.bind(CmsRemoteShellConstants.PROVIDER, providerStub);
            m_initialized = true;
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
    }
}
