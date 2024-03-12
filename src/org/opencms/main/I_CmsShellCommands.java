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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.main;

import org.opencms.file.CmsObject;

/**
 * Provides scriptable access to a class from the CmsShell.<p>
 *
 * @since 6.0.0
 */
public interface I_CmsShellCommands {

    /**
     * Provides access to the shell CmsObject and the shell itself.<p>
     *
     * @param cms the shell CmsObject
     * @param shell the CmsShell
     */
    void initShellCmsObject(CmsObject cms, CmsShell shell);

    /**
     * May be called after shell exit, can e.g. be used to output a goodbye message.<p>
     *
     * Please note: This method is not guaranteed to be called. For a shell that has more than
     * one shell command object initialized, only the exit method of one of those will be called.<p>
     */
    void shellExit();

    /**
     * May be called before shell startup, can e.g. be used to output a welcome message.<p>
     *
     * Please note: This method is not guaranteed to be called. For a shell that has more than
     * one shell command object initialized, only the start method of one of those will be called.<p>
     */
    void shellStart();
}
