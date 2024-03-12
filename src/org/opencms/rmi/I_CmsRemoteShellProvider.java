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

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Used to create new instances of I_CmsRemoteShell.<p>
 */
public interface I_CmsRemoteShellProvider extends Remote {

    /**
     * Creates a new shell instance with the given additional commands classes.<p>
     *
     * @param additionalCommandsNames comma separated list of full qualified names of classes with additional commands
     * @return the new shell instance
     *
     * @throws RemoteException if RMI stuff goes wrong
     */
    I_CmsRemoteShell createShell(String additionalCommandsNames) throws RemoteException;

}
