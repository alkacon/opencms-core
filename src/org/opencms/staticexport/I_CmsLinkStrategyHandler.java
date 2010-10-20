/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/staticexport/Attic/I_CmsLinkStrategyHandler.java,v $
 * Date   : $Date: 2010/10/20 15:22:48 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.staticexport;

import org.opencms.file.CmsObject;

/**
 * @author  Ruediger Kurz 
 *
 * @version $Revision: 1.2 $ 
 * 
 * @since 8.0.0
 */
public interface I_CmsLinkStrategyHandler {

    /**
     * Returns the static export rfs name for a given vfs resource where the link to the 
     * resource includes request parameters.<p>
     * 
     * @param cms an initialized cms context
     * @param vfsName the name of the vfs resource
     * @param parameters the parameters of the link pointing to the resource
     * 
     * @return the static export rfs name for a give vfs resource
     */
    String getRfsName(CmsObject cms, String vfsName, String parameters);

    /**
     * Returns the VFS name from a given RFS name.<p>
     * 
     * The RFS name must not contain the RFS prefix.<p>
     * 
     * @param cms an initialized OpenCms user context
     * @param rfsName the name of the RFS resource
     * 
     * @return the name of the VFS resource
     */
    CmsStaticExportData getVfsNameInternal(CmsObject cms, String rfsName);

    /**
     * Checks if the static export is required for the given VFS resource.<p>
     * 
     * Please note that the given OpenCms user context is NOT used to read the resource.
     * The check for export is always done with the permissions of the "Export" user.
     * The provided user context is just used to get the current site root.<p>
     * 
     * Since the "Export" user always operates in the "Online" project, the resource
     * is also read from the "Online" project, not from the current project of the given 
     * OpenCms context.<p>
     * 
     * @param cms the current users OpenCms context
     * @param vfsName the VFS resource name to check
     * 
     * @return <code>true</code> if static export is required for the given VFS resource
     */
    boolean isExportLink(CmsObject cms, String vfsName);

    /**
     * Returns <code>true</code> if the given VFS resource should be transported through a secure channel.<p>
     * 
     * The secure mode is only checked in the "Online" project. 
     * If the given OpenCms context is currently not in the "Online" project,
     * <code>false</code> is returned.<p>
     * 
     * The given resource is read from the site root of the provided OpenCms context.<p>
     * 
     * @param cms the current users OpenCms context
     * @param vfsName the VFS resource name to check
     * 
     * @return <code>true</code> if the given VFS resource should be transported through a secure channel
     * 
     * @see CmsStaticExportManager#isSecureLink(CmsObject, String, String)
     */
    boolean isSecureLink(CmsObject cms, String vfsName);
}
