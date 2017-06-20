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

package org.opencms.configuration;

/**
 * RMI shell server configuration.<p>
 */
public class CmsRemoteShellConfiguration {

    /** True if the remote shell should be enabled. */
    private boolean m_enabled;

    /** The port to use for creating the RMI registry used by the remote shell. */
    private int m_port;

    /**
     * Creates a new instance.<p>
     *
     * @param enabled true if the remote shell should be enabled
     * @param port the port to use for creating the RMI registry used by the remote shell.
     */
    public CmsRemoteShellConfiguration(boolean enabled, int port) {
        m_enabled = enabled;
        m_port = port;

    }

    /**
     * Gets the RMI registry port.<p>
     *
     * @return the RMI registry port
     */
    public int getPort() {

        return m_port;
    }

    /**
     * Returns true if the remote shell should be enabled.<p>
     *
     * @return true if enabled
     */
    public boolean isEnabled() {

        return m_enabled;

    }

}
